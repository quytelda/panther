/*
 * Copyright 2011 Quytelda K. Gaiwin
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
import java.io.IOException;
import java.util.logging.Level;

/**
 * This class is loaded on the Macintosh platform to help with integration.
 * It adds an ApplicationListener to the current Application object generated for
 * the Mac OS X platform.
 * Date: Aug 6, 2010
 * Time: 6:38:24 PM
 * @author Quytelda K. Gaiwin
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
        try
        {
            c.cleanUp();
        }
        catch(IOException ex)
        {
            Panther.getLogger().log(Level.WARNING, "Unable to save preferences file.", ex);
        }
        c.dispose();
        applicationEvent.setHandled(true);
        System.exit(0);
    }

    public void handleReOpenApplication(ApplicationEvent applicationEvent)
    {
        /* Reshow the application if it is hidden. */
        if(!c.isVisible())
            c.setVisible(true);
    }

    private Panther c;
    private Application app;
}
