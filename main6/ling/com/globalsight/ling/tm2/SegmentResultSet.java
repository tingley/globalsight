package com.globalsight.ling.tm2;

import java.util.Iterator;

/**
 * Represents a (potentially large) set of SegmentTmTu in the 
 * database.   
 */
public interface SegmentResultSet extends Iterator<SegmentTmTu> {
    /**
     * Release resources held by this result set.
     */
    public void finish();
}
