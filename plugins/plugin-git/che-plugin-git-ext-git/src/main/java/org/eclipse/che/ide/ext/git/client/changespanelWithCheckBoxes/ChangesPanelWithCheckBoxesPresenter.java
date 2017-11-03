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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.ComparePresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelPresenter;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.resource.Path;

/**
 * Presenter for displaying list of changed files.
 *
 * @author Igor Vinokur
 */
public class ChangesPanelWithCheckBoxesPresenter extends ChangesPanelPresenter
    implements ChangesPanelViewWithCheckBoxes.ActionDelegate {

  private final ChangesPanelViewWithCheckBoxes view;
  private List<String> changedFiles;

  @Inject
  public ChangesPanelWithCheckBoxesPresenter(
      GitLocalizationConstant locale, ChangesPanelViewWithCheckBoxes view, ComparePresenter comparePresenter) {
    super(locale, view, comparePresenter);
    this.view = view;
    changedFiles = new ArrayList<>();
  }

  public void setMarkedCheckBoxes(Set<Path> paths) {
    view.setMarkedCheckBoxes(paths);
  }

  @Override
  public List<String> getChangedFiles() {
    return changedFiles;
  }

  @Override
  public void onFileNodeCheckBoxValueChanged(Path path, boolean newCheckBoxValue) {
    if (newCheckBoxValue) {
      changedFiles.add(path.toString());
    } else {
      changedFiles.remove(path.toString());
    }
  }
}
