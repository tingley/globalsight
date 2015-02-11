package com.globalsight.ling.tm2.segmenttm;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;

import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.log.GlobalSightCategory;

class Tm2SegmentResultSet implements SegmentResultSet {

    static private final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(Tm2SegmentResultSet.class);
    
    private TuReader tuReader;
    private int batchStartIndex = 0;
    private boolean hasBatch = false;
    private SegmentTmTu nextTu = null;
    
    private Session session;
    private List<Long> ids;
    private int batchSize;
    private int max;
    
    public Tm2SegmentResultSet(Session session, List<Long> tuIds, int batchSize) {
        this.session = session;
        this.ids = tuIds;
        this.batchSize = batchSize;
        this.max = ids.size();
        this.tuReader = new TuReader(session); 
    }
    
    public void finish() {
        TmUtil.closeStableSession(session);
    }
    
    /**
     * Load the next batch.
     * @return true on sucess, false if no elements remain to load
     */
    private boolean loadBatch() {
        // End the previous batch, if there is one
        if (batchStartIndex > 0) {
            tuReader.batchReadDone();
        }
        if (batchStartIndex >= max) {
            return false;
        }
        try {
            tuReader.batchReadTus(ids, batchStartIndex, 
                    Math.min(batchStartIndex + batchSize, max), null);
            batchStartIndex += batchSize;
            return true;
        }
        catch (SQLException e) {
            CATEGORY.warn("Failed to load TU batch", e);
            return false;
        }
    }
    
    private SegmentTmTu getNextTu() {
        try {
            return tuReader.getNextTu();
        }
        catch (Exception e){
            CATEGORY.warn("Failed to get next TU", e);
            return null;
        }
    }
    
    @Override
    public boolean hasNext() {
        if (!hasBatch) {
            hasBatch = loadBatch();
            if (!hasBatch) {
                return false;
            }
        }
        nextTu = getNextTu();
        if (nextTu == null) { 
            // End of batch; try again
            hasBatch = false;
            return hasNext();
        }
        return true;
    }

    @Override
    public SegmentTmTu next() {
        SegmentTmTu tu = nextTu;
        nextTu = null;
        return tu;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }      
}