package com.globalsight.ling.tm2.segmenttm;

/**
 * Separated from TMCordanceQuery.java(public static class), used for saving
 * search results by lucene
 * 
 * @author leon
 * 
 */
public class TMidTUid
{
    private long tmId;
    private long tuId;
    private float score;

    public TMidTUid(long tmId, long tuId, float score)
    {
        this.tuId = tuId;
        this.tmId = tmId;
        this.score = score;
    }

    public TMidTUid(long tmId, long tuId)
    {
        this.tuId = tuId;
        this.tmId = tmId;
    }

    public long getTmId()
    {
        return this.tmId;
    }

    public long getTuId()
    {
        return this.tuId;
    }

    public boolean equals(Object o)
    {
        if (o == null || !o.getClass().equals(getClass()))
        {
            return false;
        }
        TMidTUid other = (TMidTUid) o;
        return other.tmId == tmId && other.tuId == tuId;
    }

    public int hashCode()
    {
        return (int) tmId * 524287 + (int) tuId;
    }

    public String toString()
    {
        return "(TM " + tmId + ", TU " + tuId + ")";
    }

    public float getMatchScore()
    {
        return this.score;
    }
}
