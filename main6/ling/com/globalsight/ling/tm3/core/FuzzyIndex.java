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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;

abstract class FuzzyIndex<T extends TM3Data>
{
    private StorageInfo<T> storage;

    FuzzyIndex(StorageInfo<T> storage)
    {
        this.storage = storage;
    }

    public StorageInfo<T> getStorage()
    {
        return storage;
    }

    /**
     * Return a set of fuzzy match candidates in order of their initial match
     * score. This requires an active JDBC connection.
     * 
     * @param open
     *            JDBC connection
     * @param key
     *            TUV data in the appropriate locale for this index
     * @param keyLocale
     *            locale of the key
     * @param targetLocales
     *            candidates must have a tuv in one of these locales (or null
     *            for no such restriction)
     * @param attributes
     *            attribute values to match
     * @param max
     *            maximum number of results to return
     * @return list of TM3Tu representing segments which are potential matches
     *         for the key. This list is sorted in decreasing order of
     *         relevance.
     */
    public List<FuzzyCandidate<T>> lookup(T key, TM3Locale keyLocale,
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, int maxResults,
            boolean lookupTarget, Long tm3TmId) throws SQLException
    {
        return lookupFingerprints(getFingerprints(key), keyLocale,
                targetLocales, inlineAttributes, customAttributes, maxResults,
                lookupTarget, tm3TmId);
    }

    /**
     * Index a segment with the associated key. This requires an active JDBC
     * connection.
     * 
     * @param conn
     *            open JDBC connection
     * @param key
     *            TUV data in the appropriate locale for this index
     * @param keyLocale
     *            locale of the key
     * @param segment
     *            TU which will be indexed by the key field
     */
    protected abstract void index(Connection conn, TM3Tuv<T> tuv)
            throws SQLException;

    protected abstract void index(Connection conn, List<TM3Tuv<T>> tuvs)
            throws SQLException;

    public void deleteFingerprints(Connection conn, TM3Tuv<T> tuv)
            throws SQLException
    {
        try
        {
            StatementBuilder sb = new StatementBuilder();
            sb.append("DELETE FROM ")
                    .append(getStorage().getFuzzyIndexTableName())
                    .append(" WHERE tuvId = ?").addValues(tuv.getId());
            SQLUtil.exec(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    public void deleteFingerprints(Connection conn, List<TM3Tuv<T>> tuvs)
            throws SQLException
    {
        try
        {
            StringBuilder tuvIds = new StringBuilder();
            for (TM3Tuv<T> tuv : tuvs)
            {
                if (tuvIds.length() > 0)
                {
                    tuvIds.append(",");
                }
                tuvIds.append(tuv.getId());
            }

            StatementBuilder sb = new StatementBuilder();
            sb.append("DELETE FROM ")
                    .append(getStorage().getFuzzyIndexTableName())
                    .append(" WHERE tuvId IN (").append(tuvIds).append(")");
            SQLUtil.exec(conn, sb);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
    }

    private List<FuzzyCandidate<T>> lookupFingerprints(
            List<Long> fingerprints, TM3Locale keyLocale,
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, int maxResults,
            boolean lookupTarget, Long tm3TmId) throws SQLException
    {
        // There can be 0 fingerprints if there is a tokenizer problem or
        // certain types of degenerate query strings
        if (fingerprints.size() == 0)
        {
            return Collections.emptyList();
        }
        // avoid an awkward case in getFuzzyLookupQuery
        if (targetLocales != null && targetLocales.isEmpty())
        {
            return Collections.emptyList();
        }

        StatementBuilder sb = getFuzzyLookupQuery(fingerprints, keyLocale,
                targetLocales, inlineAttributes, customAttributes, maxResults,
                lookupTarget, tm3TmId);

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            PreparedStatement ps = sb.toPreparedStatement(conn);
            ResultSet rs = SQLUtil.execQuery(ps);
            List<Long> ids = new ArrayList<Long>();
            while (rs.next())
            {
                long tuvId = rs.getLong(1);
                long tuId = rs.getLong(2); // Currently unused!
                int score = rs.getInt(3); // Currently unused!
                ids.add(tuvId);
            }
            ps.close();
            return getStorage().getTuStorage().loadFuzzyCandidates(ids,
                    keyLocale);
        }
        catch (Exception e)
        {
            throw new SQLException(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    // Note for implementors: targetLocales may be null, but will not be empty
    private StatementBuilder getFuzzyLookupQuery(List<Long> fingerprints,
            TM3Locale keyLocale, Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttrs,
            Map<TM3Attribute, String> customAttributes, int maxResults,
            boolean lookupTarget, Long tm3TmId)
    {
        StatementBuilder sb = new StatementBuilder();
        sb.append("SELECT tuvId, tuId, SUM(1) as score FROM ")
                .append(getFuzzyIndexTableName(tm3TmId))
                .append(" AS idx");
        if (!inlineAttrs.isEmpty())
        {
            sb.append(", ").append(getStorage().getTuTableName())
                    .append(" AS tu");
        }
        sb.append(" WHERE ");
        sb.append("(idx.fingerprint = ?").addValue(fingerprints.get(0));
		// For GBS-3934: Query at most 300 finger-prints to avoid performance
		// problem for large segment. Most TM data finger-prints number is less
		// than 300.
		int maxfpNum = fingerprints.size() <= 300 ? fingerprints.size() : 300;
        for (int i = 1; i < maxfpNum; i++)
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

        if (this instanceof MultilingualFuzzyIndex)
        {
            sb.append(" AND idx.localeId = ?").addValue(keyLocale.getId());            
        }
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

        sb.append(" GROUP BY tuvId, tuId ORDER BY score DESC");
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
        if (customAttributes.size() > 0)
        {
            sb = getAttributeMatchWrapper(sb, customAttributes);
        }
        sb = addLimit(sb, maxResults, targetLocales);

        return sb;
    }

    /**
     * Get the fuzzy index table name for specified TM3 tmId which must belong
     * to the same company with current "storage".
     * 
     * @param tm3TmId
     * @return fuzzy index table name like
     *         TM3_INDEX_SHARED_[COMPANY_ID]_[TM3_TM_ID]
     */
    private String getFuzzyIndexTableName(Long tm3TmId)
    {
        String fuzzyIndexTableName = getStorage().getFuzzyIndexTableName();
        if (!fuzzyIndexTableName.endsWith("_" + tm3TmId))
        {
            int index = fuzzyIndexTableName.lastIndexOf("_");
            fuzzyIndexTableName = fuzzyIndexTableName.substring(0, index + 1)
                    + tm3TmId;
        }
        
        return fuzzyIndexTableName;
    }

    protected List<Long> getFingerprints(T key)
    {
        List<Trigrammer.Trigram> trigrams = new Trigrammer().getTrigrams(key);
        // Remove duplicates, since our queries can not handle them properly.
        // This may cause slightly odd results for pathological cases.
        Set<Long> tset = new HashSet<Long>();
        for (Trigrammer.Trigram t : trigrams)
        {
            tset.add(t.getValue());
        }
        return new ArrayList<Long>(tset);
    }

    protected StatementBuilder addLimit(StatementBuilder inner, int maxResults,
            Set<? extends TM3Locale> targetLocales)
    {
        int max1 = 30;
        if (maxResults < Integer.MAX_VALUE)
        {
            max1 = maxResults * 3;
        }

        int max2 = 0;
        if (targetLocales != null)
        {
            max2 = 10 * targetLocales.size();
        }

        inner.append(" LIMIT " + Math.max(max1, max2));
        return inner;
    }

    protected StatementBuilder getAttributeMatchWrapper(StatementBuilder inner,
            Map<TM3Attribute, String> attributes)
    {
        StatementBuilder sb = new StatementBuilder()
                .append("SELECT dummy.tuvId, dummy.tuId, dummy.score FROM (")
                .append(inner).append(") as dummy");
        getStorage().attributeJoinFilter(sb, "dummy.tuId", attributes);
        sb.append(" WHERE 1"); // Odd
        return sb;
    }

}
