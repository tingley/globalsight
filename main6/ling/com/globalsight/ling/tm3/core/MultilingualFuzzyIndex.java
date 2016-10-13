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
package com.globalsight.ling.tm3.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.BatchStatementBuilder;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;

/**
 * Used for both dedicated and shared multilingual TMs.
 */
class MultilingualFuzzyIndex<T extends TM3Data> extends FuzzyIndex<T>
{
    MultilingualFuzzyIndex(StorageInfo<T> storage)
    {
        super(storage);
    }

    @Override
    protected void index(Connection conn, TM3Tuv<T> tuv) throws SQLException
    {
        try
        {
            List<Long> fingerprints = getFingerprints(tuv.getContent());
            BatchStatementBuilder sb = new BatchStatementBuilder(getInsertSql());
            int tuvCount = fingerprints.size();
            for (Long fp : fingerprints)
            {
                sb.addBatch(fp, tuv.getId(), tuv.getTu().getId(), tuv
                        .getLocale().getId(), tuvCount, tuv.isSource());
            }
            SQLUtil.execBatch(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    @Override
    protected void index(Connection conn, List<TM3Tuv<T>> tuvs)
            throws SQLException
    {
        PreparedStatement ps = null;
        try
        {
            ps = conn.prepareStatement(getInsertSql());

            int counter = 0;
            int tuvCount = 0;
            for (TM3Tuv<T> tuv : tuvs)
            {
                List<Long> fingerprints = getFingerprints(tuv.getContent());
                tuvCount = fingerprints.size();
                for (Long fp : fingerprints)
                {
                    ps.setLong(1, fp);
                    ps.setLong(2, tuv.getId());
                    ps.setLong(3, tuv.getTu().getId());
                    ps.setLong(4, tuv.getLocale().getId());
                    ps.setLong(5, tuvCount);
                    ps.setBoolean(6, tuv.isSource());

                    ps.addBatch();
                }

                counter++;
                if (counter == 100)
                {
                    ps.executeBatch();
                    counter = 0;
                    ps.clearBatch();
                }
            }

            if (counter > 0)
            {
                ps.executeBatch();
                ps.clearBatch();
            }
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentClose(ps);
        }
    }

    private String getInsertSql()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("INSERT INTO ")
                .append(getStorage().getFuzzyIndexTableName())
                .append(" (fingerprint, tuvId, tuId, localeId, tuvCount, isSource) ")
                .append("VALUES (?, ?, ?, ?, ?, ?)");
        return sb.toString();
    }
}
