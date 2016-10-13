package com.globalsight.ling.tm3.core;

/**
 * An individual leverage result.
 */
public abstract class TM3LeverageMatch<T extends TM3Data> {
    private TM3Tu<T> tu;
    private TM3Tuv<T> tuv;
    
    public TM3LeverageMatch(TM3Tu<T> tu, TM3Tuv<T> matchingTuv) {
        this.tu = tu;
        this.tuv = matchingTuv;
    }
    
    /**
     * Indicates whether or not this is an exact match.
     * @return
     */
    public abstract boolean isExact();
    
    /**
     * Return a matching score between 0 and 100.  100 also implies an
     * exact match.
     * @return score
     */
    public abstract int getScore();

    /**
     * The match candidate.  Since this returns the entire TU, the actual
     * translation candidate(s) can be accessed with 
     * {@link TM3Tu#getLocaleTuvs}.
     * @return TM3Tu
     */
    public TM3Tu<T> getTu() {
        return tu;
    }
    
    /**
     * The TUV that was matched.  This may be equivalent to getTu().getSourceTuv(),
     * or it may be one of the target TUVs, depending on how the matching was 
     * performed.
     * 
     * @return
     */
    public TM3Tuv<T> getTuv() {
        return tuv;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TM3LeverageMatch<?>)) {
            return false;
        }
        return tuv.equals(((TM3LeverageMatch<?>)o).getTuv());
    }
    
    @Override
    public int hashCode() {
        return tuv.hashCode();
    }

    @Override
    abstract public String toString();
}
