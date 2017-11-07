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
package org.eclipse.che.workspace.infrastructure.openshift.provision.volume;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.project.pvc.WorkspacePVCStrategy;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link PersistentVolumeClaimProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class PersistentVolumeClaimProvisionerTest {

  private static final String WORKSPACE_ID = "workspace132";

  @Mock private InternalEnvironment environment;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private WorkspacePVCStrategy pvcStrategy;

  private PersistentVolumeClaimProvisioner provisioner;

  @BeforeMethod
  public void setup() {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
    provisioner = new PersistentVolumeClaimProvisioner(false, pvcStrategy);
  }

  @Test
  public void doNothingWhenPvcDisabled() throws Exception {
    provisioner.provision(environment, osEnv, runtimeIdentity);

    verify(runtimeIdentity, never()).getWorkspaceId();
    verify(environment, never()).getMachines();
  }

  @Test
  public void testPrepareWorkspacePVCUsingConfiguredStrategy() throws Exception {
    provisioner = new PersistentVolumeClaimProvisioner(true, pvcStrategy);
    provisioner.provision(environment, osEnv, runtimeIdentity);

    verify(pvcStrategy).prepare(environment, osEnv, WORKSPACE_ID);
  }
}
