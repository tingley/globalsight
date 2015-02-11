package com.globalsight.ling.tm3.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.globalsight.ling.tm3.core.TM3Data;

class Trigrammer {

    // Start/end of segment boundary
    private static final Long BOUNDARY = -1l; 

    static class Trigram {
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
	
    Trigrammer() {
    }

    // Like everything else, this is a slapdash affair.  There's lots of 
    // normalization I would normally use.
    List<Trigram> getTrigrams(TM3Data data) {
        List<Long> fingerprints = new ArrayList<Long>();
        fingerprints.add(BOUNDARY);
        for (Long tok : data.tokenize()) {
            fingerprints.add(tok);
        }
        fingerprints.add(BOUNDARY);
        List<Trigram> trigrams = new ArrayList<Trigram>();
        for (int i = 0; i + 2 < fingerprints.size(); i++) {
            trigrams.add(new Trigram(fingerprints.get(i),
                                     fingerprints.get(i+1),
                                     fingerprints.get(i+2)));
        }
        return trigrams;
    }
}
