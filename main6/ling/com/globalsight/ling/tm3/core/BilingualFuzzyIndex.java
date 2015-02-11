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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Override
    protected StatementBuilder getFuzzyLookupQuery(List<Long> fingerprints,
            TM3Locale keyLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttrs, boolean lookupTarget)
    {
        StatementBuilder sb = new StatementBuilder();
        sb.append("SELECT tuvId, tuId, SUM(1) as score FROM ")
                .append(getStorage().getFuzzyIndexTableName())
                .append(" AS idx");
        if (!inlineAttrs.isEmpty())
        {
            sb.append(", ").append(getStorage().getTuTableName())
                    .append(" AS tu");
        }
        sb.append(" WHERE ");
        sb.append("(idx.fingerprint = ?").addValue(fingerprints.get(0));
        for (int i = 1; i < fingerprints.size(); i++)
        {
            sb.append(" OR idx.fingerprint = ?").addValue(fingerprints.get(i));
        }
        sb.append(")");
        // Add minimum and max bounds on the data length
        int min = fingerprints.size() / 3;
        int max = fingerprints.size() * 3;
        if (min > 0)
        {
            sb.append(" AND idx.tuvCount > ?").addValue(min);
        }
        sb.append(" AND idx.tuvCount < ?").addValue(max);
        if (!lookupTarget)
        {
            sb.append(" AND isSource = 1");
        }
        if (!inlineAttrs.isEmpty())
        {
            sb.append(" AND idx.tuId = tu.id");
            for (Map.Entry<TM3Attribute, Object> e : inlineAttrs.entrySet())
            {
                sb.append(" AND tu." + e.getKey().getColumnName() + " = ?");
                sb.addValue(e.getValue());
            }
        }
        sb.append(" GROUP BY tuvId ORDER BY score DESC");
        if (targetLocales != null)
        {
            // an exists subselect seems simpler, but mysql bug 46947 causes
            // exists subselects to take locks even in repeatable read
            List<Long> targetLocaleIds = new ArrayList<Long>();
            for (TM3Locale locale : targetLocales)
            {
                targetLocaleIds.add(locale.getId());
            }
            sb = new StatementBuilder()
                    .append("SELECT DISTINCT result.* FROM (").append(sb)
                    .append(") AS result, ")
                    .append(getStorage().getTuvTableName() + " AS targetTuv ")
                    .append("WHERE ")
                    .append("targetTuv.tuId = result.tuId AND ")
                    .append("targetTuv.localeId IN")
                    .append(SQLUtil.longGroup(targetLocaleIds));
        }
        return sb;
    }

}
