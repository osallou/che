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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;

/**
 * Utility for putting stuffs to machine
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class MachineNormalizer {

  private MachineNormalizer() {}

  public static void normalizeMachine(
      String name, DockerContainerConfig container, InternalMachineConfig machineConfig)
      throws ValidationException {

    if (machineConfig.getAttributes().containsKey("memoryLimitBytes")) {
      try {
        container.setMemLimit(
            Long.parseLong(machineConfig.getAttributes().get("memoryLimitBytes")));
      } catch (NumberFormatException e) {
        throw new ValidationException(
            format("Value of attribute 'memoryLimitBytes' of machine '%s' is illegal", name));
      }
    }
    container.setExpose(
        container
            .getExpose()
            .stream()
            .map(expose -> expose.contains("/") ? expose : expose + "/tcp")
            .collect(toList()));
    for (ServerConfig serverConfig : machineConfig.getServers().values()) {
      String normalizedPort =
          serverConfig.getPort().contains("/")
              ? serverConfig.getPort()
              : serverConfig.getPort() + "/tcp";

      container.getExpose().add(normalizedPort);
    }
    container.setExpose(container.getExpose().stream().distinct().collect(Collectors.toList()));

    container.getEnvironment().putAll(machineConfig.getEnv());
  }
}
