/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.util.crypto;

import org.apache.log4j.Logger;


import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * This utility class provides the functionality of a cryptographic cipher 
 * for encryption and decryption using a generated system-wide key or a key
 * provided by the caller of the methods. 
 */

public class Crypto
{
    // Constants
    private static String KEY_DIR = "key";
    private static final String KEY_NAME = "GS_key.pers";

    private static final Logger s_logger =
        Logger.getLogger(
            Crypto.class.getName());

    // The fully qualified name of the file that stores the generated key
    private String m_keyFileName = null;
    // The security key used for encryption/decryption
    private SecretKeySpec m_secretKeySpec = null;
    // Singleton instance
    private static Crypto s_crypto = new Crypto();
    

    /**
     * Private constructor -- (Singleton)
     */
    private Crypto()
    {
        try
        {       
            String installDir =
                SystemConfiguration.getInstance().getStringParameter(
                    SystemConfigParamNames.INSTALLATION_DATA_DIRECTORY);

            //key file name is like:
            //C:\globalsight\Ambassador_xxx\install\data\key\GS_key.pers
            StringBuffer sb = new StringBuffer(installDir);
            sb.append(File.separator);
            sb.append(KEY_DIR);
            sb.append(File.separator);
            sb.append(KEY_NAME);
            m_keyFileName = sb.toString();

            retrieveKey();
        }
        catch(Exception e)
        {
            // do nothing since the retrieveKey method would take care of key generation
        }
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Decrypt the given encrypted value based on the system key.
     * @param p_encryptedValue - The string to be decrypted.
     * @return The original string representation of the encrypted value.
     */
    public static String decrypt(String p_encryptedValue)
        throws Exception
    {
        return decrypt(p_encryptedValue, null);
    }

    /**
     * Decrypt the given encrypted value and return the original string.
     * @param p_encryptedValue - The string to be decrypted.
     * @param p_key - The key required for initializing the cipher.
     * @return The original string representation of the encrypted value.
     */
    public static String decrypt(String p_encryptedValue, Key p_key)
        throws Exception
    {
        return s_crypto._decrypt(p_encryptedValue, p_key);
    }

    /**
     * Encrypt the given value based on the system key.
     * @param p_value - The value to be encrypted using the system key.
     * @return The encrypted version of the value as a string.
     */
    public static String encrypt(String p_value)
        throws Exception
    {
        return encrypt(p_value, null);
    }

    /**
     * Encrypt the given value based on the provided key.
     * @param p_value - The value to be encrypted using the given key.
     * @return The encrypted version of the value as a string.
     */
    public static String encrypt(String p_value, Key p_key)
        throws Exception
    {
        return s_crypto._encrypt(p_value, p_key);
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Methods
    //////////////////////////////////////////////////////////////////////
    

    //////////////////////////////////////////////////////////////////////
    //  Begin: Private Methods
    //////////////////////////////////////////////////////////////////////
    /*
     * Create a new secret key and save it as our system-wide key.
     */
    private void createKey()
        throws Exception
    {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); 
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();        
        m_secretKeySpec = new SecretKeySpec(raw, "AES");
    }

    /**
     * Perform the decryption process based on the given key
     */
    private String _decrypt(String p_encryptedValue, Key p_key)
        throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, 
                    p_key == null ? m_secretKeySpec : p_key);

        byte[] original = cipher.doFinal(
            p_encryptedValue.getBytes("ISO8859_1"));

        return new String(original);
    }

    /*
     * Perform the encryption process based on the given key. 
     */
    private String _encrypt(String p_value, Key p_key)
        throws Exception
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init (Cipher.ENCRYPT_MODE, 
                     p_key == null ? m_secretKeySpec : p_key);

        byte[] encrypted = cipher.doFinal(p_value.getBytes());
        return new String(encrypted, "ISO8859_1");
    }

    /*
    * Retrieve the secret key that was serialized.
    */
    private void retrieveKey()
        throws Exception
    {
        try
        {
            ObjectInputStream is = new ObjectInputStream(
                new FileInputStream(m_keyFileName));
            m_secretKeySpec = (SecretKeySpec) is.readObject();
            is.close();
        }
        catch (FileNotFoundException fnfe)
        {
            s_logger.error("Could not find encryption key:",fnfe);
            throw fnfe;
        }
    }



    /*
    * Saves the DirectoryMap to the data store
    */
    private void saveKey()
        throws Exception
    {
        ObjectOutputStream os = new ObjectOutputStream(
            new FileOutputStream(m_keyFileName));
        os.writeObject(m_secretKeySpec);
        os.close();
    }    

    //////////////////////////////////////////////////////////////////////
    //  End: Private Methods
    //////////////////////////////////////////////////////////////////////
}
