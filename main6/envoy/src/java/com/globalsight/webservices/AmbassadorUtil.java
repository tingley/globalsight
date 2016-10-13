/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.webservices;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GlobalSightLocale;

/**
 * Utilities for web service APIs.
 * 
 * @author YorkJin
 * @since 2014-02-17
 * @version 8.5.3
 */
public class AmbassadorUtil
{
    private static final Logger logger = Logger.getLogger(AmbassadorUtil.class);

    // Key for AES encryption and decryption.
    public static final String AES_ENCRYPTION_KEY = "QSF866D7";

    /**
     * Get locale by locale name such as "zh_CN".
     * 
     * @param localeName
     *            -- locale name in "LANG_COUNTRY" style.
     * @return GlobalSightLocale
     * @throws WebServiceException
     */
    public static GlobalSightLocale getLocaleByName(String localeName)
            throws WebServiceException
    {
        localeName = ImportUtil.normalizeLocale(localeName.trim());
        try
        {
            return ImportUtil.getLocaleByName(localeName);
        }
        catch (Exception e)
        {
            logger.warn("getLocaleByName() : Fail to get GlobalSightLocale by locale name: '"
                    + localeName + "'");
            throw new WebServiceException("Unable to get locale by : "
                    + localeName);
        }
    }

    /**
     * If public URL is enabled, return the public URL, otherwise return the CAP
     * login URL.
     * 
     * @return String
     */
    public static String getCapLoginOrPublicUrl()
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        boolean usePublicUrl = "true".equalsIgnoreCase(config
                .getStringParameter("cap.public.url.enable"));
        if (usePublicUrl)
        {
            return config.getStringParameter("cap.public.url");
        }
        else
        {
            return config.getStringParameter("cap.login.url");
        }
    }

    /**
     * Get a random string
     * 
     * @return String
     */
    public static synchronized String getRandomFeed()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        String randomStr = String.valueOf((new Random()).nextInt(999999999));
        while (randomStr.length() < 9)
        {
            randomStr = "1" + randomStr;
        }
        return randomStr;
    }

    public static String encryptionString(String str) throws Exception
    {
        byte[] byteRe = enCrypt(str, AES_ENCRYPTION_KEY);
        return parseByte2HexStr(byteRe);
    }

    public static String getDecryptionString(String str) throws Exception
    {
        byte[] encrytByte = parseHexStr2Byte(str);
        return deCrypt(encrytByte, AES_ENCRYPTION_KEY);
    }

    private static byte[] enCrypt(String content, String strKey)
            throws Exception
    {
        KeyGenerator keygen;
        SecretKey desKey;
        Cipher c;
        byte[] cByte;
        String str = content;

        keygen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
        secureRandom.setSeed(strKey.getBytes());
        keygen.init(128, secureRandom);

        desKey = keygen.generateKey();
        c = Cipher.getInstance("AES");

        c.init(Cipher.ENCRYPT_MODE, desKey);

        cByte = c.doFinal(str.getBytes("UTF-8"));

        return cByte;
    }

    private static String deCrypt(byte[] src, String strKey) throws Exception
    {
        KeyGenerator keygen;
        SecretKey desKey;
        Cipher c;
        byte[] cByte;

        keygen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
        secureRandom.setSeed(strKey.getBytes());
        keygen.init(128, secureRandom);

        desKey = keygen.generateKey();
        c = Cipher.getInstance("AES");

        c.init(Cipher.DECRYPT_MODE, desKey);

        cByte = c.doFinal(src);

        return new String(cByte, "UTF-8");
    }

    private static String parseByte2HexStr(byte buf[])
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++)
        {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    private static byte[] parseHexStr2Byte(String hexStr)
    {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++)
        {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * Change list to string comma separated.
     * 
     * @return String like "string1,string2,string3".
     */
    public static String listToString(Collection<String> objects)
    {
        StringBuilder buffer = new StringBuilder();
        int counter = 0;
        for (String str : objects)
        {
            if (counter > 0)
            {
                buffer.append(",");
            }
            counter++;
            buffer.append(str);
        }

        return buffer.toString();
    }
}
