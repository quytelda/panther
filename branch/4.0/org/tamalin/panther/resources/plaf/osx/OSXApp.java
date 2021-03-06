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

package org.tamalin.panther.resources.plaf.osx;

import com.apple.eawt.*;
import com.apple.eawt.AppEvent.*;
import org.tamalin.panther.resources.plaf.EventHandler;

import javax.swing.*;
import java.awt.*;
/**
 * The OSXApp class handles Mac OS X specific events such as About Events,
 * Quit Events, and Preferences Events.
 * Date: Dec 7, 2010
 * Time: 8:58:40 PM
 */

/**
 * @author Quytelda K. Gaiwin
 * @since 4.0
 */

public class OSXApp
{
    public OSXApp(JMenuBar menu, Image img)
	{
		app = Application.getApplication();
        app.setDefaultMenuBar(menu);
        app.setDockIconImage(img);
	}

    public void setAboutHandler(final EventHandler handler)
    {
        AboutHandler about = new AboutHandler()
		{
			public void handleAbout(AboutEvent e)
			{
                handler.eventTriggered();
			}
		};

        app.setAboutHandler(about);
    }

    public void setQuitHandler(final EventHandler handler)
    {
        app.setQuitHandler(new QuitHandler()
        {

            public void handleQuitRequestWith(QuitEvent quitEvent, QuitResponse quitResponse)
            {
                boolean confirmed = handler.eventTriggered();

                if(confirmed)
                    quitResponse.performQuit();
                else
                    quitResponse.cancelQuit();
            }
        });
    }

    public void setPreferencesHandler(final EventHandler handler)
    {
        app.setPreferencesHandler(new PreferencesHandler()
        {

            public void handlePreferences(PreferencesEvent preferencesEvent)
            {
                handler.eventTriggered();
            }
        });
    }

    public void setReopenHandler(final EventHandler handler)
    {
        app.addAppEventListener(new AppReOpenedListener()
        {
            public void appReOpened(AppReOpenedEvent appReOpenedEvent)
            {
                handler.eventTriggered();
            }
        });
    }

    Application app = null;
}