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

package org.tamalin.panther.file;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class asks the user where to save the file,
 * and saves the file when the 'Save File' button is clicked.
 *
 * @author Tamalin
 * @since 3.1.0S2
 */
public class FileSaveRunnable implements Runnable
{
    /**
     * Constructs a new FileSaveRunnable that writes text to a specified file.
     * It creates a new file if the chosen file doesn't exist.
     *
     * @param target   The target file to write to.
     * @param fileData A byte array that will be written to file.
     */
    public FileSaveRunnable(byte[] fileData, File target) throws IOException
    {
        data = fileData;
        file = target;

        /* Ensure that the file actually exists. */
        /* If the file doesn't exist, create it. */
        if (!file.exists())
        {
            /* Attempt to create the file. */
            boolean created = file.createNewFile();

            /* Handle any problems. */
            if (!created)
            {
                JOptionPane.showMessageDialog(null, "There was a problem creating the file.  Please make sure that you have read and write permissions to the area where the file is located, and that it is not write-protected.", "Error Creating File", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void run()
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream(file);
            fos.write(data);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    byte[] data;
    File file;
}
