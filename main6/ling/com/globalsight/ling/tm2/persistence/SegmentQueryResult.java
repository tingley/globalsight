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
package com.globalsight.ling.tm2.persistence;


import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;

/**
 * SegmentQueryResult is a wrapper of ResultSet object(s).
 * 
 * The derived class of this class is returned from TuRetriever
 * implemented class. This class walk through the result of the query
 * issued in TuRetriever class and return a BaseTmTu object along with
 * its BaseTmTuv objects one by one.
 *
 * Subclass of this class must implement next(), getTuId(),
 * createTu(), createTuv() methods. All are protected classes and are
 * used in getNextTu() method.
 *
 * The result set returned by the query should have Tu and Tuv data in
 * one row and they must be sorted on Tu.id so all Tuv data that
 * belong to a Tu are returned next to each other. So, when getTuId()
 * returns the same value, we know these data belong to the same Tu.
 *
 * Sample code:
 *
 * SegmentQueryResult result = tuRetriever.query();
 * while((tu = result.getNextTu()) != null)
 * {
 *     // do something with tu
 *     ...
 * }
 * tuRetriever.close();
 *
 */

public abstract class SegmentQueryResult
{
    private long m_prevTuId = 0;
    private BaseTmTu m_tu = null;

    /**
     * Get the next Tu object. This method reads data from the
     * database and compose a Tu object which contains references of
     * Tuvs that belong to the Tu. When no more Tu is found, null is
     * returned.
     *
     * @return BaseTmTu object or null when there is no Tu object
     */
    public BaseTmTu getNextTu()
        throws Exception
    {
        BaseTmTu prevTu = null;
        
        while(next())
        {
            long tuId = getTuId();
            if(tuId != m_prevTuId)
            {
                // At the very first iteration of while at the very
                // first call to this method, m_tu is null so prevTu
                // is also null
                prevTu = m_tu;
                
                // create new Tu
                m_tu = createTu();
                m_prevTuId = tuId;
            }

            // create new Tuv
            BaseTmTuv tuv = createTuv();
            m_tu.addTuv(tuv);

            // When prevTu holds a previous Tu, it is returned
            if(prevTu != null)
            {
                break;
            }
        }

        // When there is no more data in the ResultSet, the execution
        // point exits the while loop without setting prevTu. So set
        // it here.
        if(prevTu == null)
        {
            prevTu = m_tu;

            // set m_tu null so that the next call to this method
            // returns null
            m_tu = null;
        }

        return prevTu;
    }


    /**
     * Advances a row ahead just like ResultSet does.
     */
    protected abstract boolean next()
        throws Exception;


    /**
     * Returns Tu id
     */
    protected abstract long getTuId()
        throws Exception;


    /**
     * An abstract method to create Tu object. It is called in
     * getNextTu() method.
     */
    protected abstract BaseTmTu createTu()
        throws Exception;
    

    /**
     * An abstract method to create Tuv object. It is called in
     * getNextTu() method.
     */
    protected abstract BaseTmTuv createTuv()
        throws Exception;
    
}
