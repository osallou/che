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
package org.eclipse.che.workspace.infrastructure.openshift.provision.env;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftInternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/**
 * Converts environment variables in {@link MachineConfig} to OpenShift environment variables.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class EnvVarsConverter implements ConfigurationProvisioner {
  @Override
  public void provision(OpenShiftInternalEnvironment osEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Pod pod : osEnv.getPods().values()) {
      String podName = pod.getMetadata().getName();
      for (Container container : pod.getSpec().getContainers()) {
        String containerName = container.getName();
        String machineName = podName + "/" + containerName;
        InternalMachineConfig machineConf = osEnv.getMachines().get(machineName);

        if (machineConf != null) {
          machineConf
              .getEnv()
              .forEach(
                  (key, value) -> {
                    container.getEnv().removeIf(env -> key.equals(env.getName()));
                    container.getEnv().add(new EnvVar(key, value, null));
                  });
        }
      }
    }
  }
}
