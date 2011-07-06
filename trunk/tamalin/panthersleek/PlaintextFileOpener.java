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

import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;

/**
 * This class reads plaintext files from the disk in a separate thread
 * so that the AWT thread will not freeze while the operation is finishing.
 *
 * @author Quytelda K. Gaiwin
 */
public class PlaintextFileOpener implements Runnable
{
    public PlaintextFileOpener(File f)
    {
        file = f;
        textDeposits = new ArrayList<TextDepositor>(1);
    }

    public void addTextDeposit(TextDepositor deposit)
    {
        if (!textDeposits.contains(deposit))
            textDeposits.add(deposit);
    }

    public void run()
    {
        String text = "";

        try
        {
            Scanner in = new Scanner(file);

            try
            {
                StringBuilder sb = new StringBuilder();
                boolean hasNextLine = in.hasNextLine();

                while (hasNextLine)
                {
                    String nextLine = in.nextLine();
                    hasNextLine = in.hasNextLine();

                    if (hasNextLine)
                    {
                        nextLine += "\n";
                    }
                    sb.append(nextLine);
                }
                text = sb.toString();
            }
            finally
            {
                in.close();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        for (TextDepositor t : textDeposits)
        {
            t.loadText(text);
        }
    }

    private ArrayList<TextDepositor> textDeposits;
    private File file;
}
