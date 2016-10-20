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
package com.globalsight.ling.tm2.segmenttm;

/**
 * Separated from TMCordanceQuery.java(public static class), used for saving
 * search results by lucene
 * 
 * @author leon
 * 
 */
public class TMidTUid implements Comparable
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

    @Override
    public int compareTo(Object o)
    {
        TMidTUid other = null;

        if (o instanceof TMidTUid)
        {
            other = (TMidTUid) o;
        }
        else
        {
            // If comparing to the other type, this class comes first.
            return -1;
        }
        long result = this.tuId - other.tuId;
        if (result == 0)
        {
            result = this.tmId - other.tmId;
        }
        return new Long(result).intValue();
    }
}
