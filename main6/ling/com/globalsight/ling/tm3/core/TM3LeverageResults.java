package com.globalsight.ling.tm3.core;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A set of results for a single leverage operation.  
 */
public class TM3LeverageResults<T extends TM3Data> {
    private T source;
    private Map<TM3Attribute, Object> attributes;
    
    private SortedSet<TM3LeverageMatch<T>> matches = 
        new TreeSet<TM3LeverageMatch<T>>(COMPARATOR);
    
    public TM3LeverageResults(T source, Map<TM3Attribute, Object> attributes) {
        this.source = source;
        this.attributes = attributes;
    }
    
    /**
     * Return the match key that was used to search the TM.
     * @return
     */
    public T getSource() {
        return source;
    }

    /**
     * Return the attributes, if any, that were specified on the search.  
     * @return attribute map, possibly empty (if no attributes were specified)
     */
    public Map<TM3Attribute, Object> getSourceAttributes() {
        return attributes;
    }
    
    /**
     * Drop all but the lowest 'max' results in this result set.
     * @param max
     */
    void keepHighest(int max) {
        if (matches.size() < max) {
            return;
        }
        
        int count = 0;
        // Note that SortedSet.headSet() is backed by itself, so
        // it will hold a reference to the results we don't need 
        // any more.  Hence, the copy.
        SortedSet<TM3LeverageMatch<T>> newMatches = 
            new TreeSet<TM3LeverageMatch<T>>(COMPARATOR);
        for (TM3LeverageMatch<T> match : matches) {
            if (++count > max) {
                break;
            }
            newMatches.add(match);
        }
        matches = newMatches;
    }
    
    /**
     * Returns matches in descending order of match score.  
     * 
     * @return set of {@link TM3LeverageMatch}, possibly empty if no
     *      matches were found
     */
    public SortedSet<TM3LeverageMatch<T>> getMatches() {
        return matches;
    }
    
    void addExactMatch(TM3Tu<T> segment, TM3Tuv<T> tuv) {
        matches.add(new ExactMatch(segment, tuv));
    }
    
    void addFuzzyMatch(TM3Tu<T> segment, TM3Tuv<T> tuv, int score) {
        matches.add(new FuzzyMatch(segment, tuv, score));
    }
    
    class ExactMatch extends TM3LeverageMatch<T> {
        ExactMatch(TM3Tu<T> segment, TM3Tuv<T> tuv) {
            super(segment, tuv);
        }

        @Override
        public int getScore() {
            return 100;
        }

        @Override
        public boolean isExact() {
            return true;
        }

        @Override
        public String toString() {
            return "[" + getTuv() + ", exact]";
        }
    }
    
    class FuzzyMatch extends TM3LeverageMatch<T> {
        private int score;
        
        FuzzyMatch(TM3Tu<T> segment, TM3Tuv<T> tuv, int score) {
            super(segment, tuv);
            this.score = score;
        }

        @Override
        public int getScore() {
            return score;
        }

        @Override
        public boolean isExact() {
            return false;
        }

        @Override
        public String toString() {
            return "[" + getTuv() + ", fuzzy score " + getScore() + "]";
        }
    }
    
    static final MatchComparator COMPARATOR = new MatchComparator();
    static class MatchComparator implements Comparator<TM3LeverageMatch<?>> {
        @Override
        public int compare(TM3LeverageMatch<?> o1, TM3LeverageMatch<?> o2) {
            int v = (o2.getScore() - o1.getScore());
            if (v == 0) {
                // Needed to make this consistent with equals
                if (o1.getTuv().equals(o2.getTuv())) {
                    return 0;
                }
                // Use fingerprints as a surrogate ordering that is stable but 
                // not tied to database order
                v = ltoi(o1.getTuv().getFingerprint() - o2.getTuv().getFingerprint());
                if (v == 0) {
                    // Fall back on DB ordering in the (unlikely) case of fingerprint collision
                    v = ltoi(o1.getTu().getId() - o2.getTu().getId());                    
                }
            }
            return v;
        }
    }
    
    // Safely convert a long into a value we can return from a comparator.
    // (Casting may overflow the integer.)
    private static int ltoi(long l) {
        return (l < 0) ? -1 : 
                (l == 0) ? 0 : 1;
    }
}
