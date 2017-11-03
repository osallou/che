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
package org.eclipse.che.ide.ext.git.client.changespanelWithCheckBoxes;

import java.util.List;
import java.util.Set;

import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.resource.Path;

/**
 * The view of {@link ChangesPanelWithCheckBoxesPresenter}.
 *
 * @author Igor Vinokur
 */
public interface ChangesPanelViewWithCheckBoxes extends ChangesPanelView {
  interface ActionDelegate {
    void onFileNodeCheckBoxValueChanged(Path path, boolean newCheckBoxValue);

    List<String> getChangedFiles();
  }

  void setDelegate(ChangesPanelViewWithCheckBoxes.ActionDelegate delegate);

  void setMarkedCheckBoxes(Set<Path> paths);
}
