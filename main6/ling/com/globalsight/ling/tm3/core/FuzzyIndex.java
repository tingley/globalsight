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
            boolean lookupTarget) throws SQLException
    {
        return lookupFingerprints(getFingerprints(key), keyLocale,
                targetLocales, inlineAttributes, customAttributes, maxResults,
                lookupTarget);
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
    public void index(Connection conn, TM3Tuv<T> tuv) throws SQLException
    {
        indexFingerprints(conn, getFingerprints(tuv.getContent()), tuv);
    }

    protected abstract void indexFingerprints(Connection conn,
            List<Long> fingerprints, TM3Tuv<T> tuv) throws SQLException;

    public abstract void deleteFingerprints(Connection conn, TM3Tuv<T> tuv)
            throws SQLException;

    // Note for implementors: targetLocales may be null, but will not be empty
    protected abstract StatementBuilder getFuzzyLookupQuery(
            List<Long> fingerprints, TM3Locale keyLocale,
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttrs, boolean lookupTarget);

    protected List<FuzzyCandidate<T>> lookupFingerprints(
            List<Long> fingerprints, TM3Locale keyLocale,
            Set<? extends TM3Locale> targetLocales,
            Map<TM3Attribute, Object> inlineAttributes,
            Map<TM3Attribute, String> customAttributes, int maxResults,
            boolean lookupTarget) throws SQLException
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
                targetLocales, inlineAttributes, lookupTarget);
        if (customAttributes.size() > 0)
        {
            sb = getAttributeMatchWrapper(sb, customAttributes);
        }
        sb = addLimit(sb, maxResults);
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

    protected StatementBuilder addLimit(StatementBuilder inner, int maxResults)
    {
        if (maxResults < Integer.MAX_VALUE)
        {
            inner.append(" LIMIT " + maxResults * 3);
        }
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
