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

import java.security.*;
import javax.crypto.*;

/**
 * @author Quytelda K. Gaiwin
 */
public class ByteCipher
{
    public ByteCipher(String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        cipher = Cipher.getInstance(algorithm);
    }

    public byte[] decrypt(Key key, byte[] bytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(bytes);
    }

    public byte[] encrypt(Key key, byte[] bytes) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException
    {
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(bytes);
    }

    private Cipher cipher;
}
