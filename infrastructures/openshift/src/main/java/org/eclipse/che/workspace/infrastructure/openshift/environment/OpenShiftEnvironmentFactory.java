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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.RecipeRetriever;

/** @author Sergii Leshchenko */
public class OpenShiftEnvironmentFactory extends InternalEnvironmentFactory {

  private final OpenShiftEnvironmentParser openShiftEnvironmentParser;

  @Inject
  public OpenShiftEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      OpenShiftEnvironmentParser openShiftEnvironmentParser) {
    super(installerRegistry, recipeRetriever);
    this.openShiftEnvironmentParser = openShiftEnvironmentParser;
  }

  @Override
  public OpenShiftInternalEnvironment create(Environment environment)
      throws InfrastructureException, ValidationException {
    OpenShiftInternalEnvironment internalEnvironment =
        new OpenShiftInternalEnvironment(
            environment, installerRegistry, recipeRetriever, openShiftEnvironmentParser);
    return internalEnvironment;
  }
}
