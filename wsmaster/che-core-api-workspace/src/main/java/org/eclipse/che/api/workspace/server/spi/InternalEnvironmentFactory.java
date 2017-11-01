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

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.installer.server.InstallerRegistry;

/**
 * Factory for Environment specific internal representation Related but not really bound to some
 * specific Infrastructure to let Infrastructure apply multiple different implementations, some of
 * which can be considered as a "native format", while others as rather "supported, adopted formats"
 *
 * <p>Expected to be bound as a MapBinder with unique String as a key, like: MapBinder<String,
 * InternalEnvironmentFactory> environmentFactories = MapBinder.newMapBinder(binder(), String.class,
 * InternalEnvironmentFactory.class);
 * environmentFactories.addBinding("uniq_name").to(SubclassOfInternalEnvironmentFactory.class);
 *
 * @author gazarenkov
 */
public abstract class InternalEnvironmentFactory {

  protected final InstallerRegistry installerRegistry;
  protected final RecipeRetriever recipeRetriever;

  public InternalEnvironmentFactory(
      InstallerRegistry installerRegistry, RecipeRetriever recipeRetriever) {
    this.installerRegistry = installerRegistry;
    this.recipeRetriever = recipeRetriever;
  }

  /**
   * validates internals of Environment and creates instance of InternalEnvironment
   *
   * @param environment the environment
   * @return InternalEnvironment
   * @throws InfrastructureException if infrastructure specific error occures
   * @throws ValidationException if validation fails
   */
  public abstract InternalEnvironment create(Environment environment)
      throws InfrastructureException, ValidationException;
}
