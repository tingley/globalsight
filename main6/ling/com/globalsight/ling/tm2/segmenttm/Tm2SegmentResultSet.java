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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;

class Tm2SegmentResultSet implements SegmentResultSet
{

    static private final Logger CATEGORY = Logger
            .getLogger(Tm2SegmentResultSet.class);

    private TuReader tuReader;
    private int batchStartIndex = 0;
    private boolean hasBatch = false;
    private SegmentTmTu nextTu = null;

    private List<Long> ids;
    private int batchSize;
    private int max;

    public Tm2SegmentResultSet(Connection conn, List<Long> tuIds, int batchSize)
    {
        this.ids = tuIds;
        this.batchSize = batchSize;
        this.max = ids.size();
        this.tuReader = new TuReader(conn);
    }

    public void finish()
    {
    }

    /**
     * Load the next batch.
     * 
     * @return true on sucess, false if no elements remain to load
     */
    private boolean loadBatch()
    {
        // End the previous batch, if there is one
        if (batchStartIndex > 0)
        {
            tuReader.batchReadDone();
        }
        if (batchStartIndex >= max)
        {
            return false;
        }
        try
        {
            tuReader.batchReadTus(ids, batchStartIndex,
                    Math.min(batchStartIndex + batchSize, max), null);
            batchStartIndex += batchSize;
            return true;
        }
        catch (SQLException e)
        {
            CATEGORY.warn("Failed to load TU batch", e);
            return false;
        }
    }

    private SegmentTmTu getNextTu()
    {
        try
        {
            return tuReader.getNextTu();
        }
        catch (Exception e)
        {
            CATEGORY.warn("Failed to get next TU", e);
            return null;
        }
    }

    @Override
    public boolean hasNext()
    {
        if (!hasBatch)
        {
            hasBatch = loadBatch();
            if (!hasBatch)
            {
                return false;
            }
        }
        nextTu = getNextTu();
        if (nextTu == null)
        {
            // End of batch; try again
            hasBatch = false;
            return hasNext();
        }
        return true;
    }

    @Override
    public SegmentTmTu next()
    {
        SegmentTmTu tu = nextTu;
        nextTu = null;
        return tu;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}