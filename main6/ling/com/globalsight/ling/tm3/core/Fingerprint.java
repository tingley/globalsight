package com.globalsight.ling.tm3.core;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Compute fingerprints for database matching.  This provides 
 * a default implementation of fingerprint calculation that takes
 * the low 8 bytes of an MD5 hash of the input.  Other schemes
 * are possible.
 */
public class Fingerprint {

    public static long fromString(String data) { 
        return getMD5asLong(data);
    }
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static byte[] getMD5(String s) {
        MessageDigest digest;
        try {
            digest = java.security.MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // can't happen
        }
        digest.update(s.getBytes(UTF8));
        return digest.digest();
    }

    private static long getMD5asLong(String s) {
        byte[] bytes = getMD5(s);
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.put(bytes, 0, 8);
        return buf.getLong(0);
    }
}
