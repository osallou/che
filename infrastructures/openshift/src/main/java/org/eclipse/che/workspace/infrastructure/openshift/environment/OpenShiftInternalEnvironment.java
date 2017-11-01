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

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.RecipeRetriever;

/** @author Sergii Leshchenko */
public class OpenShiftInternalEnvironment extends InternalEnvironment {

  private final OpenShiftEnvironmentParser openShiftEnvironmentParser;
  private OpenShiftEnvironment osEnv;

  public OpenShiftInternalEnvironment(
      Environment environment,
      InstallerRegistry registry,
      RecipeRetriever recipeRetriever,
      OpenShiftEnvironmentParser openShiftEnvironmentParser)
      throws InfrastructureException {
    super(environment, registry, recipeRetriever);
    this.openShiftEnvironmentParser = openShiftEnvironmentParser;
  }

  public OpenShiftEnvironment getOpenShiftEnvironment()
      throws InfrastructureException, ValidationException {
    if (osEnv == null) {
      osEnv = openShiftEnvironmentParser.parse(this);
    }
    return osEnv;
  }
}
