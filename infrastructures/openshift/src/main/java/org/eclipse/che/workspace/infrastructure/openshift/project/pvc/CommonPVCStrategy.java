/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project.pvc;

import static org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil.getWsAgentServerMachine;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.openshift.project.pvc.CommonPVCStrategyHelper.Command.MAKE;
import static org.eclipse.che.workspace.infrastructure.openshift.project.pvc.CommonPVCStrategyHelper.Command.REMOVE;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;
import org.eclipse.che.workspace.infrastructure.openshift.Names;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common PVC for each workspace in one OpenShift project.
 *
 * <p>Note that subpaths are used for resolving of backed up data path collisions. Subpaths
 * evaluated as following: '{workspaceId}/{workspace data folder}'. Workspace data folder it's a
 * configured path where workspace projects, logs or any other data located. The number of
 * workspaces that can simultaneously store backups in one PV is limited only by the storage
 * capacity. The number of workspaces that can be running simultaneously depends on access mode
 * configuration and Che configuration limits.
 *
 * @author Anton Korneta
 */
public class CommonPVCStrategy implements WorkspacePVCStrategy {

  public static final String COMMON_STRATEGY = "common";

  private static final int COUNT_THREADS = 4;
  private static final String PVC_PREPARE_POD_PREFIX = "pod-pvc-prepare-";
  private static final String PVC_CLEANUP_POD_PREFIX = "pod-pvc-cleanup-";

  private static final Logger LOG = LoggerFactory.getLogger(CommonPVCStrategy.class);

  private final String pvcQuantity;
  private final String pvcName;
  private final String pvcAccessMode;
  private final String projectsPath;
  private final CommonPVCStrategyHelper pvcHelper;
  private final ExecutorService executor;

  @Inject
  public CommonPVCStrategy(
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      @Named("che.workspace.projects.storage") String projectFolderPath,
      CommonPVCStrategyHelper pvcHelper) {
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.projectsPath = projectFolderPath;
    this.pvcHelper = pvcHelper;
    this.executor =
        Executors.newFixedThreadPool(
            COUNT_THREADS,
            new ThreadFactoryBuilder()
                .setNameFormat("CommonPVCStrategy-ThreadPool-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(false)
                .build());
  }

  @Override
  public void prepare(InternalEnvironment env, OpenShiftEnvironment osEnv, String workspaceId)
      throws InfrastructureException {
    pvcHelper.performJobPod(
        PVC_PREPARE_POD_PREFIX + workspaceId, workspaceId, pvcName, MAKE, workspaceId);
    final PersistentVolumeClaim pvc = osEnv.getPersistentVolumeClaims().get(pvcName);
    if (pvc != null) {
      return;
    }
    final String machineWithSources =
        getWsAgentServerMachine(env)
            .orElseThrow(() -> new InfrastructureException("Machine with ws-agent not found"));
    osEnv.getPersistentVolumeClaims().put(pvcName, newPVC(pvcName, pvcAccessMode, pvcQuantity));
    for (Pod pod : osEnv.getPods().values()) {
      final PodSpec podSpec = pod.getSpec();
      for (Container container : podSpec.getContainers()) {
        final String machine = Names.machineName(pod, container);
        if (machine.equals(machineWithSources)) {
          final String subPath =
              workspaceId + (projectsPath.startsWith("/") ? projectsPath : '/' + projectsPath);
          container.getVolumeMounts().add(newVolumeMount(pvcName, projectsPath, subPath));
          podSpec.getVolumes().add(newVolume(pvcName, pvcName));
          return;
        }
      }
    }
  }

  @Override
  public void cleanup(String workspaceId) throws InfrastructureException {
    executor.execute(
        ThreadLocalPropagateContext.wrap(
            () -> {
              try {
                pvcHelper.performJobPod(
                    PVC_CLEANUP_POD_PREFIX + workspaceId,
                    workspaceId,
                    pvcName,
                    REMOVE,
                    workspaceId);
              } catch (InfrastructureException ex) {
                LOG.warn(
                    "Cleanup of PVC {} for the workspace {} failed due {}",
                    pvcName,
                    workspaceId,
                    ex.getMessage());
              }
            }));
  }

  @PreDestroy
  private void shutdown() {
    if (!executor.isShutdown()) {
      executor.shutdown();
      try {
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
          executor.shutdownNow();
          if (!executor.awaitTermination(60, TimeUnit.SECONDS))
            LOG.error("Couldn't shutdown Common PVC strategy thread pool");
        }
      } catch (InterruptedException ignored) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
      LOG.info("Common PVC strategy thread pool is terminated");
    }
  }
}
