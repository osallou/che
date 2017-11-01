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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftObjectUtil.newVolumeMount;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for executing simple commands in a Persistent Volume on OpenShift.
 *
 * <p>Creates a short-lived Pod using a CentOS image which mounts a specified PVC and executes a
 * command (either {@code mkdir -p <path>} or {@code rm -rf <path}). Reports back whether the pod
 * succeeded or failed. Supports multiple paths for one command.
 *
 * <p>For mkdir commands, an in-memory list of created workspaces is stored and used to avoid
 * calling mkdir unnecessarily. However, this list is not persisted, so dir creation is not tracked
 * between restarts.
 *
 * @author amisevsk
 * @author Anton Korneta
 */
@Singleton
public class CommonPVCStrategyHelper {

  private static final Logger LOG = LoggerFactory.getLogger(CommonPVCStrategyHelper.class);

  static final String IMAGE_PULL_POLICY = "IfNotPresent";
  static final String POD_PHASE_SUCCEEDED = "Succeeded";
  static final String POD_RESTART_POLICY = "Never";
  static final String POD_PHASE_FAILED = "Failed";
  static final String[] MKDIR_WORKSPACE_COMMAND = new String[] {"mkdir", "-p"};
  static final String[] RMDIR_WORKSPACE_COMMAND = new String[] {"rm", "-rf"};

  private final String projectName;
  private final String jobImage;
  private final String jobMemoryLimit;
  private final String projectsPath;
  private final OpenShiftProjectFactory factory;

  enum Command {
    REMOVE,
    MAKE
  }

  @Inject
  CommonPVCStrategyHelper(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      @Named("che.openshift.jobs.image") String jobImage,
      @Named("che.openshift.jobs.memorylimit") String jobMemoryLimit,
      @Named("che.workspace.projects.storage") String projectsPath,
      OpenShiftProjectFactory factory) {
    this.jobImage = jobImage;
    this.projectName = projectName;
    this.jobMemoryLimit = jobMemoryLimit;
    this.projectsPath = projectsPath;
    this.factory = factory;
  }

  /**
   * Creates a pod with {@code command} and reports whether it succeeded
   *
   * @param jobName name of pod job
   * @param pvcName name of the PVC to mount
   * @param command command to execute in PVC.
   * @param wsDirs list of arguments attached to command. A list of directories to create/delete.
   */
  void performJobPod(
      String jobName, String workspaceId, String pvcName, Command command, String... wsDirs)
      throws InfrastructureException {
    if (wsDirs.length == 0 || isNullOrEmpty(projectName)) {
      return;
    }
    final String[] jobCommand = getCommand(command, projectsPath, wsDirs);
    final Container container =
        new ContainerBuilder()
            .withName(jobName)
            .withImage(jobImage)
            .withImagePullPolicy(IMAGE_PULL_POLICY)
            .withNewSecurityContext()
            .withPrivileged(false)
            .endSecurityContext()
            .withCommand(jobCommand)
            .withVolumeMounts(newVolumeMount(pvcName, projectsPath, null))
            .withNewResources()
            .withLimits(singletonMap("memory", new Quantity(jobMemoryLimit)))
            .endResources()
            .build();

    final Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(jobName)
            .endMetadata()
            .withNewSpec()
            .withContainers(container)
            .withVolumes(newVolume(pvcName, pvcName))
            .withRestartPolicy(POD_RESTART_POLICY)
            .endSpec()
            .build();

    final OpenShiftProject osProject = factory.create(workspaceId);
    osProject.pods().create(pod);
    osProject
        .pods()
        .wait(
            jobName,
            5,
            watchedPod -> {
              if (watchedPod.getStatus() == null) {
                return false;
              }
              final String phase = watchedPod.getStatus().getPhase();
              switch (phase) {
                case POD_PHASE_FAILED:
                  LOG.info("Pod command {} failed", Arrays.toString(jobCommand));
                  // fall through
                case POD_PHASE_SUCCEEDED:
                  try {
                    osProject.pods().delete(jobName);
                  } catch (InfrastructureException ignore) {
                  }
                  return POD_PHASE_SUCCEEDED.equals(phase);
                default:
                  // waits until timeout reached
                  return false;
              }
            });
  }

  private String[] getCommand(Command commandType, String mountPath, String... dirs) {
    final String[] command;
    switch (commandType) {
      case MAKE:
        command = MKDIR_WORKSPACE_COMMAND;
        break;
      case REMOVE:
        command = RMDIR_WORKSPACE_COMMAND;
        break;
      default:
        throw new IllegalArgumentException(format("Unsupported command type %s", commandType));
    }

    final String[] dirsWithPath =
        Arrays.stream(dirs)
            .map(dir -> mountPath + (dir.startsWith("/") ? dir : '/' + dir))
            .toArray(String[]::new);

    final String[] fullCommand = new String[command.length + dirsWithPath.length];

    System.arraycopy(command, 0, fullCommand, 0, command.length);
    System.arraycopy(dirsWithPath, 0, fullCommand, command.length, dirsWithPath.length);
    return fullCommand;
  }
}
