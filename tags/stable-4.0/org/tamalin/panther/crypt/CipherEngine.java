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

package org.tamalin.panther.crypt;

import org.tamalin.panther.Updatable;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.tamalin.panther.Panther;

/**
 * The CipherEngine class carries out all encryption and decryption operations using information passed to it by the calling method,
 * presumably through the constructor.  The CipherEngine class implements the runnable interface, so it can be run in it's own thread.
 *
 * @author Quytelda K. Gaiwin
 * @since 4.0
 */
public class CipherEngine implements Runnable
{
    /**
     * Creates a new instance of CipherEngine from the given algorithm.
     *
     * @param alg The Cipher Algorithm
     * @throws NoSuchAlgorithmException the cipher algorithm is invalid
     * @throws NoSuchPaddingException   There was an error with the padding.
     */
    public CipherEngine(String alg) throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        algorithm = alg;
        cipher = Cipher.getInstance(alg);
    }

    public void init(byte[] d, int m, char[] password, Updatable p) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        /* Set the mode and store the data. */
        data = d;
        mode = m;
        parent = p;

        /* Create an encryption key from the given password. */
        Key key = makeKey(password);

        /* For security reasons, the password array must be overwritten. */
        /* Overwrite the password array with 0s. */
        for (int i = 0; i < password.length; i++)
            password[i] = 0;

        /* Initialize the Cipher object. */
        cipher.init(mode, key);
    }

    public void run()
    {
        /* Try to encrypt/decrypt the data, then update the result to the Updatable. */
        try
        {
            byte[] result = cipher.doFinal(data);
            parent.updateFromBytes(result);
        }
        catch (IllegalBlockSizeException ex)
        {
            Panther.getLogger().log(Level.SEVERE, "Data follows an illegal block pattern.", ex);
            JOptionPane.showMessageDialog(null, "This file is invalid (Illegal Block Size)!", "Decryption Failed", JOptionPane.ERROR_MESSAGE);
        }
        catch (BadPaddingException ex)
        {
            JOptionPane.showMessageDialog(null, "Decryption failed: Access is denied.", "Decryption Failed", JOptionPane.ERROR_MESSAGE);
            Panther.getLogger().log(Level.SEVERE, "Bad Padding Scheme", ex);
        }
    }

    private Key makeKey(char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        /* Digest the password into a sequence of bytes the right length. */
        MessageDigest digest = MessageDigest.getInstance("MD5");
        for (char character : password)
            digest.update((byte) character);

        byte[] passKey = digest.digest();

        /* Create the key key. */
        return new SecretKeySpec(passKey, algorithm);
    }

    public int getMode()
    {
        return mode;
    }

    private byte[] data = null;
    private int mode;
    private String algorithm;
    private Cipher cipher;
    private Updatable parent;
}


