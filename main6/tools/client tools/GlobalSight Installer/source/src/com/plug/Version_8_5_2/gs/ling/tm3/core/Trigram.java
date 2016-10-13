package com.plug.Version_8_5_2.gs.ling.tm3.core;

import java.util.Arrays;

/**
 * Compute fingerprints for database matching.  This provides 
 * a default implementation of fingerprint calculation that takes
 * the low 8 bytes of an MD5 hash of the input.  Other schemes
 * are possible.
 */
public  class Trigram {
    long[] fingerprints;

    public Trigram(Long fp0, Long fp1, Long fp2) {
        fingerprints = new long[3];
        fingerprints[0] = fp0.longValue();
        fingerprints[1] = fp1.longValue();
        fingerprints[2] = fp2.longValue();
    }

    public String toString() {
        return Arrays.toString(fingerprints);
    }
    
    public Long getValue() {
        return fingerprints[0] + 31 *
                    (fingerprints[1] + 31 * fingerprints[2]);
    }

}