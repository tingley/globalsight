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

package com.globalsight.everest.persistence.tuv;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * Helper for getting max tu and tuv ids.
 */
public class SegmentTuTuvIndexUtil
{
    static private final Logger logger = Logger
            .getLogger(SegmentTuTuvIndexUtil.class);

    private static long DEFAULT_VALUE = 1;

    private static final String SEQ_TU = "TRANSLATION_UNIT_SEQ";

    private static final String SEQ_TUV = "TRANSLATION_UNIT_VARIANT_SEQ";

    private static final String SQL_GET_MAX_TU_ID = "select count from sequence where name='"
            + SEQ_TU + "'";

    private static final String SQL_GET_MAX_TUV_ID = "select count from sequence where name='"
            + SEQ_TUV + "'";

    private static final String SQL_SEQ_UPDATE_TU = "update sequence set count=? where name='"
            + SEQ_TU + "'";

    private static final String SQL_SEQ_UPDATE_TUV = "update sequence set count=? where name='"
            + SEQ_TUV + "'";

    private static Map<String, Long> maxIds = new HashMap<String, Long>(2);
    static
    {
        maxIds.put(SEQ_TU, getMaxTuId() + 1);
        maxIds.put(SEQ_TUV, getMaxTuvId() + 1);
    }

    private synchronized static long allocateIds(String sequence,
            long increasedCount)
    {
        long id = maxIds.get(sequence);
        maxIds.put(sequence, id + increasedCount);
        return id;
    }

    private static long getMaxTuId()
    {
        long maxTuId = DEFAULT_VALUE;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            ps = connection.prepareStatement(SQL_GET_MAX_TU_ID);
            rs = ps.executeQuery();
            if (rs.next())
            {
                maxTuId = rs.getLong(1);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getMaxTuId()." + e.getMessage());
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return maxTuId;
    }

    private static long getMaxTuvId()
    {
        long maxTuvId = DEFAULT_VALUE;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            connection = DbUtil.getConnection();
            ps = connection.prepareStatement(SQL_GET_MAX_TUV_ID);
            rs = ps.executeQuery();
            if (rs.next())
            {
                maxTuvId = rs.getLong(1);
            }
        }
        catch (Exception e)
        {
            logger.error("Error when getMaxTuvId()." + e.getMessage());
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(connection);
        }

        return maxTuvId;
    }

    /**
     * Updates tu sequence count in 'sequence' table.
     */
    public static void updateTuSequence(Connection connection) throws Exception
    {
        PreparedStatement ps = null;
        try
        {
            ps = connection.prepareStatement(SQL_SEQ_UPDATE_TU);
            ps.setLong(1, maxIds.get(SEQ_TU));
            ps.execute();
            if (!connection.getAutoCommit())
            {
                connection.commit();
            }
        }
        catch (Exception e)
        {
            logger.error("Error when updateTuSequence()." + e.getMessage());
            throw e;
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
    }

    /**
     * Updates tuv sequence count in 'sequence' table.
     */
    public static void updateTuvSequence(Connection connection) throws Exception
    {
        PreparedStatement ps = null;
        try
        {
            ps = connection.prepareStatement(SQL_SEQ_UPDATE_TUV);
            ps.setLong(1, maxIds.get(SEQ_TUV));
            ps.execute();
            if (!connection.getAutoCommit())
            {
                connection.commit();
            }
        }
        catch (Exception e)
        {
            logger.error("Error when updateTuvSequence()." + e.getMessage());
            throw e;
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
    }

    public static void setTuIds(Collection<Tu> tus)
    {
        if (tus == null || tus.size() == 0)
        {
            return;
        }

        long tuId = allocateIds(SEQ_TU, tus.size());

        for (Iterator<Tu> tuIter = tus.iterator(); tuIter.hasNext();)
        {
            TuImpl tu = (TuImpl) tuIter.next();
            tu.setId(tuId++);
        }
    }

    public static void setTuvId(Tuv tuv)
    {
        if (tuv == null)
            return;

        long tuvId = allocateIds(SEQ_TUV, 1);

        ((TuvImpl) tuv).setId(tuvId);
    }

    public static void setTuvIds(Collection<Tuv> tuvs)
    {
        if (tuvs == null || tuvs.size() == 0)
        {
            return;
        }

        long tuvId = allocateIds(SEQ_TUV, tuvs.size());

        for (Iterator<Tuv> it = tuvs.iterator(); it.hasNext();)
        {
            TuvImpl tuv = (TuvImpl) it.next();
            tuv.setId(tuvId++);
        }
    }
}
