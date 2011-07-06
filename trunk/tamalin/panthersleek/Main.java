/*
 * Copyright 2010 Quytelda Gaiwin
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
package org.tamalin.panthersleek;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Panther Sleek main class.
 * @author Quytelda K. Gaiwin
 */
public class Main
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            /* Parse the command line arguments. */
            for (int i = 0; i < args.length; i++)
            {
                if ((args[i]).equals("-h") || args[i].equals("--help"))
                {
                    //print help
                    System.out.println("Panther Sleek\nUsage: panthersleek [options]");
                    System.out.println("-h\t--help\thelp\n\tDisplays this message, then exits.");
                    System.out.println("-l\t--lang\tchange language: [en][zh]\n\tChanges "
                            + "the default language to English or Chinese respectively.");
                    System.out.println("-v\t--verbose\tRun the program in verbose mode.");
                    return;
                }
                else if (args[i].equals("-r") || args[i].equals("--recover"))
                {
                    System.out.println("Attempting to recover...");
                    File dir = Panther.getFileStorageDir();
                    boolean deleted = false;

                    if (dir.exists())
                    {
                        deleted = dir.delete();
                    }

                    if (!deleted)
                    {
                        JOptionPane.showMessageDialog(null, "Unable to delete old configuration files!", "Recovery", JOptionPane.ERROR_MESSAGE);
                    }
                    else
                    {
                        System.out.println("Recovery successful!  You can now restart the program.");
                    }

                    return;
                }
                else if(args[i].equals("-v") || args[i].equals("--verbose"))
                {
                    // Set program in verbose mode
                    Logger.getLogger(Panther.class.getName()).setLevel(Level.ALL);
                }
            }
        }

        /* Configure for use on Mac OS X. */
        if (System.getProperty("os.name").contains("Mac"))
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Panther Sleek");
        }

        Panther panther = new Panther(args);

        /*
         * Size the window to 1/2 the dimensions of the screen.
         */
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int scrnWidth = (toolkit.getScreenSize().width / 2);
        int scrnHeight = (toolkit.getScreenSize().height / 2);
        panther.setSize(scrnWidth, scrnHeight);

        // Center the window in the display.
        int ww = panther.getWidth();
        int wh = panther.getHeight();
        Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
        int sw = ss.width;
        int sh = ss.height;
        int x = (sw / 2) - (ww / 2);
        int y = (sh / 2) - (wh / 2);

        panther.setLocation(x, y);

        /* Show the program's main window. */
        panther.setVisible(true);
        panther.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
