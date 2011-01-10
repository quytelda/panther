package org.tamalin.panther.resources.plaf.osx;

import com.apple.eawt.*;
import com.apple.eawt.AppEvent.*;
import org.tamalin.panther.resources.plaf.EventHandler;

import javax.swing.*;
import java.awt.*;
/**
 * File skeleton generated by IntelliJ IDEA.
 * @author Quytelda K. Gaiwin
 * Date: Dec 7, 2010
 * Time: 8:58:40 PM
 */

/**
 * @author Quytelda K. Gaiwin
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