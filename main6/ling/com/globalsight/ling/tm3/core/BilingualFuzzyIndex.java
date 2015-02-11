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
import java.sql.SQLException;
import java.util.List;

import com.globalsight.ling.tm3.core.persistence.BatchStatementBuilder;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;

/**
 * Fuzzy index implementation for Bilingual TMs.
 * 
 * <p>
 */
class BilingualFuzzyIndex<T extends TM3Data> extends FuzzyIndex<T>
{

    BilingualFuzzyIndex(StorageInfo<T> storage)
    {
        super(storage);
    }

    @Override
    protected void indexFingerprints(Connection conn, List<Long> fingerprints,
            TM3Tuv<T> tuv) throws SQLException
    {
        try
        {
            BatchStatementBuilder sb = new BatchStatementBuilder("INSERT INTO ")
                    .append(getStorage().getFuzzyIndexTableName())
                    .append(" (fingerprint, tuvId, tuId, tuvCount, isSource) ")
                    .append("VALUES (?, ?, ?, ?, ?)");
            int tuvCount = fingerprints.size();
            for (Long fp : fingerprints)
            {
                sb.addBatch(fp, tuv.getId(), tuv.getTu().getId(), tuvCount,
                        tuv.isSource());
            }
            SQLUtil.execBatch(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    @Override
    public void deleteFingerprints(Connection conn, TM3Tuv<T> tuv)
            throws SQLException
    {
        try
        {
            StatementBuilder sb = new StatementBuilder();
            sb.append("DELETE FROM ")
                    .append(getStorage().getFuzzyIndexTableName())
                    .append(" WHERE tuvId = ?").addValue(tuv.getId());
            SQLUtil.exec(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

}
