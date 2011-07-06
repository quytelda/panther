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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tamalin.panthersleek;

import java.io.*;
import java.security.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.crypto.spec.*;

/**
 * @author Quytelda K. Gaiwin
 */
public class KeyImporterExporter implements Runnable
{

    public KeyImporterExporter(File f, PantherKey k)
    {
        mode = EXPORT_MODE;
        file = f;
        key = k;
        depositors = new ArrayList<KeyDepositor>();
    }

    public KeyImporterExporter(File f)
    {
        mode = 1;
        file = f;
        depositors = new ArrayList<KeyDepositor>();
    }

    public void run()
    {
        if (mode == EXPORT_MODE)
        {
            FileOutputStream output;
            try
            {
                output = new FileOutputStream(file);
                byte[] keybytes = key.getKey().getEncoded();
                output.write(keybytes);
                output.close();
            }
            catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            FileInputStream input;
            try //Try to read a key in from file.
            {
                input = new FileInputStream(file);
                byte[] keybytes = new byte[(int) file.length()];
                input.read(keybytes);
                PantherKey newKey = null;

                //Try to convert the read bytes into a key, and import it.
                try
                {
                    Key tmp = new SecretKeySpec(keybytes, Panther.getAlgorithm());
                    String dispname = JOptionPane.showInputDialog(null, "What would you like to call this key?", "Key Name?", JOptionPane.INFORMATION_MESSAGE);
                    newKey = new PantherKey(tmp, Panther.getNumberOfKeys(), dispname);
                }
                finally
                {
                    input.close();
                }

                for (KeyDepositor depositor : depositors)
                {
                    depositor.loadKey(newKey);
                }
            }
            catch (FileNotFoundException ex)
            {
                ex.printStackTrace();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public void installKeyDepositor(KeyDepositor depositor)
    {
        depositors.add(depositor);
    }

    private File file;
    private PantherKey key;
    private int mode;
    private final int EXPORT_MODE = 0;
    private ArrayList<KeyDepositor> depositors;
}