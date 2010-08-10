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

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * This is the main class where the program starts.  It contains the main method and parses
 * the arguments provided at the command prompt.
 * Date: Aug 6, 2010
 * Time: 10:51:47 PM
 *
 * @author Quytelda K. Gaiwin
 * @version 1.0
 * @since 4.0
 */

public class Main
{
    /**
     * The main method.  This is where the program is started.  In the main method, the arguments are taken into account,
     * and the user interface is started.  Also the default locale is discovered using <code>Locale locale = Locale.getDefault();</code>
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        /* Parse the command line arguments, if there are any. */
        if (args.length > 0)
        {
            if (args[0].equals("-h") || args[0].equals("--help") || args[0].equals("-a") || args[0].equals("--ayudas") || args[0].equals("--aiuto"))
            {

            }
            else if (args[0].equals("-v") || args[0].equals("--version"))
            {

            }
            else
            {

            }
        }

        /* -------------MAC OS X ONLY-------------- */
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Panther");
        }


        /* Run the program in the AWT Event Dispatch Thread. */
        EventQueue.invokeLater(new Runnable()
        {

            public void run()
            {
                Panther m = new Panther(Locale.getDefault());
                m.pack();
                m.setVisible(true);
                m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });

    }
}