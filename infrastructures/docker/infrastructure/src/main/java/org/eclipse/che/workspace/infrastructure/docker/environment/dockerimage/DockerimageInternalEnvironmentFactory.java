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

import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainersStartStrategy;
import org.eclipse.che.workspace.infrastructure.docker.environment.EnvironmentValidator;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class DockerimageInternalEnvironmentFactory extends InternalEnvironmentFactory {

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
  public InternalEnvironment create(Environment environment)
      throws InfrastructureException, ValidationException {

    // workaround to make it work for former recipes where image was written in location
    EnvironmentImpl envCopy = new EnvironmentImpl(environment);
    if (environment.getRecipe().getLocation() != null) {
      // move image from location to content
      envCopy.getRecipe().setContent(environment.getRecipe().getLocation());
      envCopy.getRecipe().setLocation(null);
    }

    return new DockerimageInternalEnvironment(
        envCopy, installerRegistry, recipeRetriever, validator, startStrategy);
  }
}
