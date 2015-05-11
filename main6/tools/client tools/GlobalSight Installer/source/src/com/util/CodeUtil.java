package com.util;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CodeUtil
{
    public static final String AES_ENCRYPTION_KEY = "QSF866D6";

    public static String getDecryptionString(String str) throws Exception
    {
        byte[] encrytByte = parseHexStr2Byte(str);
        return deCrypt(encrytByte, AES_ENCRYPTION_KEY);
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
}
