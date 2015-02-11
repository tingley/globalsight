package com.globalsight.ling.tm3.core;

import java.util.Comparator;

import com.globalsight.ling.tm3.core.TM3LeverageResults.MatchComparator;

/**
 * Data representing a fuzzy match candidate that can be 
 * scored or used to bootstrap a full TU with additional
 * help from the database.
 */
class FuzzyCandidate<T extends TM3Data> {

    private long id;
    private long tuId;
    private T content;
    private int score;
    private long fingerprint;
    
    FuzzyCandidate(long id, long tuId, long fingerprint, T content) {
        this.id = id;
        this.tuId = tuId;
        this.fingerprint = fingerprint;
        this.content = content;
    }
    
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getTuId() {
        return tuId;
    }
    public void setTuId(long tuId) {
        this.tuId = tuId;
    }
    public T getContent() {
        return content;
    }
    public void setContent(T content) {
        this.content = content;
    }
    public long getFingerprint() {
        return fingerprint;
    }
    public void setFingerprint(long fp) {
        this.fingerprint = fp;
    }
    public int getScore() {
        return score;
    }
    public void setScore(int score) {
        this.score = score;
    }

    static final CandidateComparator COMPARATOR = new CandidateComparator();
    static class CandidateComparator implements Comparator<FuzzyCandidate<?>> {
        @Override
        public int compare(FuzzyCandidate<?> o1, FuzzyCandidate<?> o2) {
            int v = (o2.getScore() - o1.getScore());
            if (v == 0) {
                // Use fingerprints as a surrogate ordering that is stable but 
                // not tied to database order
                v = ltoi(o1.getFingerprint() - o2.getFingerprint());
                if (v == 0) {
                    // Fall back on DB ordering in the (unlikely) case of fingerprint collision
                    v = ltoi(o1.getId() - o2.getId());                    
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

    @Override
    public String toString() {
        return content + " (" + score + "); TU " + getTuId() + " TUV " + getId();
    }
}
