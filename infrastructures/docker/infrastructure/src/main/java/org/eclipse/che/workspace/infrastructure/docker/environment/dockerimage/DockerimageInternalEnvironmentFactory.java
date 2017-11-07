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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage;

import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainersStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.docker.environment.MachineNormalizer;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerimageInternalEnvironmentFactory extends InternalEnvironmentFactory {

  public static final String TYPE = "dockerimage";

  private final EnvironmentValidator validator;
  private final ContainersStartStrategy startStrategy;

  @Inject
  public DockerimageInternalEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      EnvironmentValidator validator,
      ContainersStartStrategy startStrategy) {
    super(installerRegistry, recipeRetriever);
    this.startStrategy = startStrategy;
    this.validator = validator;
  }

  @Override
  public InternalEnvironment create(final Environment environment)
      throws InfrastructureException, ValidationException {

    EnvironmentImpl envCopy = new EnvironmentImpl(environment);
    if (envCopy.getRecipe().getLocation() != null) {
      // move image from location to content
      envCopy.getRecipe().setContent(environment.getRecipe().getLocation());
      envCopy.getRecipe().setLocation(null);
    }
    return super.create(envCopy);
  }

  @Override
  protected InternalEnvironment create(
      Map<String, InternalMachineConfig> machines, InternalRecipe recipe, List<Warning> warnings)
      throws InfrastructureException, ValidationException {

    DockerEnvironment dockerEnvironment = dockerEnv(machines, recipe);
    validator.validate(machines, dockerEnvironment);
    return new DockerimageInternalEnvironment(machines, recipe, warnings, dockerEnvironment);
  }

  private DockerEnvironment dockerEnv(
      Map<String, InternalMachineConfig> machines, InternalRecipe recipe)
      throws ValidationException {

    Map.Entry<String, InternalMachineConfig> entry = machines.entrySet().iterator().next();
    DockerEnvironment dockerEnv = new DockerEnvironment();
    DockerContainerConfig container = new DockerContainerConfig();
    dockerEnv.getContainers().put(entry.getKey(), container);
    container.setImage(recipe.getContent());
    MachineNormalizer.normalizeMachine(entry.getKey(), container, entry.getValue());

    return dockerEnv;
  }
}
