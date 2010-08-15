/*
 * Copyright 2010 Quytelda K. Gaiwin
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.tamalin.panther;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

import java.awt.*;

/**
 * This class is loaded on the Macintosh platform to help with integration.
 * It adds an ApplicationListener to the current Application object generated for
 * the Mac OS X platform.
 * Date: Aug 6, 2010
 * Time: 6:38:24 PM
 * @author Quytelda K. Gaiwin
 * @version 1.0
 * @since 4.0
 */

public class MacAppHandler implements ApplicationListener
{
    public MacAppHandler()
    {
        app = Application.getApplication();
        app.setEnabledPreferencesMenu(true);
        app.setEnabledAboutMenu(true);
    }

    public void init(Panther client, Image icon)
    {
        c = client;
        app = Application.getApplication();
        app.setDockIconImage(icon);
        app.setEnabledPreferencesMenu(true);
        app.setEnabledAboutMenu(true);
        app.addApplicationListener(this);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        c.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
    }

    public void handleAbout(ApplicationEvent applicationEvent)
    {
        c.showAbout();
        applicationEvent.setHandled(true);
    }

    public void handleOpenApplication(ApplicationEvent applicationEvent)
    {
        /* No need to do anything here. */
    }

    public void handleOpenFile(ApplicationEvent applicationEvent)
    {
        /* This functionality has already been implemented. */
    }

    public void handlePreferences(ApplicationEvent applicationEvent)
    {
        c.showPreferences();
    }

    public void handlePrintFile(ApplicationEvent applicationEvent)
    {
        /* Print functionality has not yet been implemented. */
    }

    public void handleQuit(ApplicationEvent applicationEvent)
    {
        //TODO: implement exit functionality
        System.out.println("Exiting...");
        applicationEvent.setHandled(true);
    }

    public void handleReOpenApplication(ApplicationEvent applicationEvent)
    {
        //TODO: reimplement functionality.
    }

    private Panther c;
    private Application app;
}
