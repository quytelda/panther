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

package org.tamalin.panther.file;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 * This class opens the requested file.
 * The file is specified in the constructor.
 *
 * @author Quytelda K. Gaiwin
 * @since 3.1.0
 */
public class FileOpener implements Runnable
{
    public FileOpener(File openFile)
    {
        //Initialize variables.
        file = openFile;
    }

    public void run()
    {
        try
        {
            String text = open();
            finishedText = text;

        }
        catch (IOException ex)
        {
            javax.swing.JOptionPane.showMessageDialog(null, "There was an error reading the file.  Please make sure that you have permission to read the file.", "Error Reading File", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public String open() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            Scanner in = new Scanner(file);
            try
            {
                while (in.hasNextLine())
                {
                    String line = in.nextLine();
                    sb.append(line + "\n");
                }
            }
            finally
            {
                in.close();
            }
        }
        catch (IOException ex)
        {
            throw ex;
        }
        return sb.toString();
    }

    /**
     * Retrieves the text that was stored inside the file.
     *
     * @return The text contents of the given file.
     */
    public String getText()
    {
        return finishedText;
    }

    File file;
    private String finishedText = "Blank Defualt";
}
