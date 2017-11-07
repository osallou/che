/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.zdb.ide;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.Map;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerManager;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.BreakpointManager;
import org.eclipse.che.ide.api.debug.DebuggerServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.debug.DebuggerDescriptor;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.debug.AbstractDebugger;
import org.eclipse.che.plugin.debugger.ide.debug.DebuggerLocationHandlerManager;
import org.eclipse.che.plugin.zdb.ide.configuration.ZendDbgConfigurationType;

/**
 * Zend PHP debugger.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDebugger extends AbstractDebugger {

  public static final String ID = "zend-debugger";

  @Inject
  public ZendDebugger(
      DebuggerServiceClient service,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
      DtoFactory dtoFactory,
      LocalStorageProvider localStorageProvider,
      EventBus eventBus,
      NotificationManager notificationManager,
      AppContext appContext,
      DebuggerManager debuggerManager,
      BreakpointManager breakpointManager,
      DebuggerLocalizationConstant constant,
      RequestHandlerManager requestHandlerManager,
      DebuggerLocationHandlerManager debuggerLocationHandlerManager) {
    super(
        service,
        transmitter,
        configurator,
        dtoFactory,
        localStorageProvider,
        eventBus,
        debuggerManager,
        notificationManager,
        appContext,
        breakpointManager,
        constant,
        requestHandlerManager,
        debuggerLocationHandlerManager,
        ID);
  }

  @Override
  protected DebuggerDescriptor toDescriptor(Map<String, String> connectionProperties) {
    return new DebuggerDescriptor(
        "Zend Debugger",
        "Zend Debugger, port: "
            + connectionProperties.get(ZendDbgConfigurationType.ATTR_DEBUG_PORT));
  }
}
