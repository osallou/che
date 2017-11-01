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
package org.eclipse.che.api.workspace.server.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * Representation of {@link Environment} which holds internal representations of environment
 * components to ease implementation of {@link RuntimeInfrastructure}.
 *
 * @author Alexander Garagatyi
 * @author gazarenkov
 */
public abstract class InternalEnvironment {
  protected final InternalRecipe recipe;
  protected final Map<String, InternalMachineConfig> machines;
  protected final List<Warning> warnings;

  protected InternalEnvironment(
      Environment environment, InstallerRegistry installerRegistry, RecipeRetriever recipeRetriever)
      throws InfrastructureException {

    this.machines = new HashMap<>();
    this.warnings = new ArrayList<>();
    this.recipe = recipeRetriever.getRecipe(environment.getRecipe());

    for (Map.Entry<String, ? extends MachineConfig> machineEntry :
        environment.getMachines().entrySet()) {
      MachineConfig machineConfig = machineEntry.getValue();

      List<Installer> installers = null;
      try {
        installers = installerRegistry.getOrderedInstallers(machineConfig.getInstallers());
      } catch (InstallerException e) {
        throw new InfrastructureException(e);
      }

      this.machines.put(
          machineEntry.getKey(),
          new InternalMachineConfig(
              installers,
              machineConfig.getServers(),
              machineConfig.getEnv(),
              machineConfig.getAttributes()));
    }
  }

  /** Returns environment recipe which includes recipe content. */
  public InternalRecipe getRecipe() {
    return recipe;
  }

  /**
   * Returns unmodifiable map of internal machines configs which include all information about
   * machine configuration which may be needed by infrastructure implementation.
   */
  public Map<String, InternalMachineConfig> getMachines() {
    return Collections.unmodifiableMap(machines);
  }

  /** Adds an {@link Warning}. */
  public void addWarning(Warning warning) {
    warnings.add(warning);
  }

  /**
   * Returns the list of the warnings indicating that the environment violates some non-critical
   * constraints or some preferable configuration is missing so defaults are used.
   */
  public List<? extends Warning> getWarnings() {
    return Collections.unmodifiableList(warnings);
  }
}
