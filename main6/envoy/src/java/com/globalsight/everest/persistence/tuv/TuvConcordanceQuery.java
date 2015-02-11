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

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuType;
import com.globalsight.everest.tuv.CustomTuType;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.common.Text;


import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;


/**
 * This class makes a query for TM concordance.
 */
public class TuvConcordanceQuery
{
    private static Logger c_category =
        Logger.getLogger(
            TuvConcordanceQuery.class.getName());

    // This is a list of returned columns from the query in order. The
    // query is created based on this list. Use columnIdx() method to
    // get a column index to use in ResultSet.getXXX() method.
    private static final String[] COLUMN_LIST
        = {"tuv1.id", "tuv1.order_num", "tuv1.locale_id", "tuv1.tu_id",
           "tuv1.is_indexed", "tuv1.segment_string", "tuv1.word_count",
           "tuv1.exact_match_key", "tuv1.state", "tuv2.id",
           "tuv2.order_num", "tuv2.locale_id", "tuv2.tu_id",
           "tuv2.is_indexed", "tuv2.segment_string", "tuv2.word_count",
           "tuv2.exact_match_key", "tuv2.state", "tu.id", "tu.order_num",
           "tu.tm_id", "tu.data_type", "tu.tu_type", "tu.localize_type",
           "tu.leverage_group_id", "tu.pid" };

    private static String SEARCH_CASE_SENSITIVE;
    private static String SEARCH_CASE_INSENSITIVE;
    private static Map RESULTSET_MAP;

    static
    {
        // construct a query string
        SEARCH_CASE_INSENSITIVE = "SELECT ";
        SEARCH_CASE_INSENSITIVE += COLUMN_LIST[0];
        for(int i = 1; i < COLUMN_LIST.length; i++)
        {
            SEARCH_CASE_INSENSITIVE += ", " + COLUMN_LIST[i];
        }
        SEARCH_CASE_INSENSITIVE
            += " FROM translation_unit_variant tuv1, "
            + "translation_unit_variant tuv2, translation_unit tu "
            + "WHERE tuv1.tu_id = tu.id AND tuv2.tu_id = tu.id AND "
            + "tuv1.locale_id = ? AND tuv2.locale_id = ? AND "
            + "tuv1.state <> 'OUT_OF_DATE' AND tuv2.state <> 'OUT_OF_DATE' "
            + "AND ";

        SEARCH_CASE_SENSITIVE = SEARCH_CASE_INSENSITIVE;
        SEARCH_CASE_SENSITIVE += "tuv1.segment_string like ? escape '&'";
        // TODO: use NLS_LOWER instead of LOWER
        SEARCH_CASE_INSENSITIVE
            += "LOWER(tuv1.segment_string) like LOWER(?) escape '&'";


        // construct a map of column name and index
        RESULTSET_MAP = new HashMap();
        for(int i = 0; i < COLUMN_LIST.length; i++)
        {
            RESULTSET_MAP.put(COLUMN_LIST[i], new Integer(i + 1));
        }
    }


    private Connection m_connection;
    private ResultSet m_resultSet;
    private PreparedStatement m_statement;
    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;
    private Map m_levGrpIdMap;


    public TuvConcordanceQuery(Connection p_connection)
    {
        m_connection = p_connection;
    }


    public void query(String p_searchPattern,
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
        boolean p_caseSensitiveSearch)
        throws Exception
    {
        m_levGrpIdMap = new HashMap(100);

        m_sourceLocale = p_sourceLocale;
        m_targetLocale = p_targetLocale;

        String queryPattern = makeWildcardQueryString(p_searchPattern, '&');

        if (p_caseSensitiveSearch)
        {
            m_statement =
                m_connection.prepareStatement(SEARCH_CASE_SENSITIVE);
        }
        else
        {
            m_statement =
                m_connection.prepareStatement(SEARCH_CASE_INSENSITIVE);
        }

        m_statement.setLong(1, p_sourceLocale.getId());
        m_statement.setLong(2, p_targetLocale.getId());
        m_statement.setString(3, queryPattern);
        m_resultSet = m_statement.executeQuery();
    }


    public Tu next()
        throws Exception
    {
        if (!m_resultSet.next())
        {
            // no more data
            m_resultSet.close();
            m_statement.close();
            return null;
        }

        LeverageGroup levGrp = getLeverageGroup();
        Tu tu = getTu(levGrp);
        Tuv sourceTuv = getSourceTuv(tu, m_sourceLocale);
        Tuv targetTuv = getTargetTuv(tu, m_targetLocale);

        return tu;
    }


    private static int columnIdx(String p_columnName)
    {
        Integer idx = (Integer)RESULTSET_MAP.get(p_columnName);

        if (idx == null)
        {
            // programing error
            throw new RuntimeException(
                "TuvConcordanceQuery.columnIdx(): Column name not found");
        }

        return idx.intValue();
    }


    private static TuvImpl createTuvObject(Tu p_tu,
        GlobalSightLocale p_locale, long p_id,
        long p_exactMatchKey, long p_order, String p_segment,
        String p_state, int p_wordCount, String p_isIndexed)
        throws Exception
    {
        TuvImpl tuv = (TuvImpl)ServerProxy.getTuvManager().createTuv(
            p_wordCount, p_locale, null);

        p_tu.addTuv(tuv);

        tuv.setTu(p_tu);
        tuv.setId(p_id);
        tuv.setExactMatchKey(p_exactMatchKey);
        tuv.setOrder(p_order);
        tuv.setGxml(p_segment);
        tuv.setState(TuvState.valueOf(p_state));

        if (p_isIndexed.startsWith("Y"))
        {
            tuv.makeIndexed();
        }

        return tuv;
    }


    private static TuImpl createTuObject(long p_id, String p_datatype,
        String p_localizableType, long p_order, long p_pid, long p_tmId,
        String p_tuType, LeverageGroup p_leverageGroup)
        throws Exception
    {
        TuType itemType;

        try
        {
            itemType = TuType.valueOf(p_tuType);
        }
        catch(TuvException e)
        {
            itemType = new CustomTuType(p_tuType);
        }

        TuImpl tu = (TuImpl)ServerProxy.getTuvManager().createTu(p_tmId,
            p_datatype, itemType, p_localizableType.charAt(0), p_pid);

        tu.setId(p_id);
        tu.setOrder(p_order);
        tu.setLeverageGroup(p_leverageGroup);
        return tu;
    }


    private static LeverageGroup createLeverageGroupObject(long p_id)
    {
        LeverageGroupImpl lev = new LeverageGroupImpl();

        lev.setId(p_id);

        return lev;
    }


    // '*' is the only allowed wildcard char the user can enter. '*'
    // is escaped using '\' to make '*' literal.
    //
    // p_escapeChar is an escape character used in LIKE predicate to
    // escape '%' and '_' wildcards.
    private static String makeWildcardQueryString(String p_queryString,
        char p_escapeChar)
    {
        String pattern = p_queryString;
        String escape = String.valueOf(p_escapeChar);
        String asterisk = "*";
        String percent = "%";
        String underScore = "_";

        // remove the first and the last '*' from the string
        if (pattern.startsWith(asterisk))
        {
            pattern = pattern.substring(1);
        }

        if (pattern.endsWith(asterisk)
            && pattern.charAt(pattern.length() - 2) != '\\')
        {
            pattern = pattern.substring(0, pattern.length() - 1);
        }

        // '&' -> '&&' (escape itself)
        pattern = Text.replaceString(pattern, escape, escape + escape);

        // '%' -> '&%' (escape wildcard char)
        pattern = Text.replaceString(pattern, percent, escape + percent);

        // '_' -> '&_' (escape wildcard char)
        pattern = Text.replaceString(pattern, underScore, escape + underScore);

        // '*' -> '%'  (change wildcard) '\*' -> '*' (literal *)
        pattern = Text.replaceChar(pattern, '*', '%', '\\');

        // Add '%' to the biginning and the end of the string (because
        // the segment text is enclosed with <segment></segment> or
        // <localizabel></localizabel>)
        pattern = percent + pattern + percent;

        c_category.debug(pattern);

        return pattern;
    }


    private LeverageGroup getLeverageGroup()
        throws Exception
    {
        Long levGrpId = new Long(
            m_resultSet.getLong(columnIdx("tu.leverage_group_id")));
        LeverageGroup levGrp = (LeverageGroup)m_levGrpIdMap.get(levGrpId);

        if (levGrp == null)
        {
            levGrp = createLeverageGroupObject(levGrpId.longValue());
            m_levGrpIdMap.put(levGrpId, levGrp);
        }

        return levGrp;
    }



    private Tu getTu(LeverageGroup p_leverageGroup)
        throws Exception
    {
        return createTuObject(m_resultSet.getLong(columnIdx("tu.id")),
            m_resultSet.getString(columnIdx("tu.data_type")),
            m_resultSet.getString(columnIdx("tu.localize_type")),
            m_resultSet.getLong(columnIdx("tu.order_num")),
            m_resultSet.getLong(columnIdx("tu.pid")),
            m_resultSet.getLong(columnIdx("tu.tm_id")),
            m_resultSet.getString(columnIdx("tu.tu_type")),
            p_leverageGroup);
    }


    private Tuv getSourceTuv(Tu p_tu, GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        return createTuvObject(p_tu, p_sourceLocale,
            m_resultSet.getLong(columnIdx("tuv1.id")),
            m_resultSet.getLong(columnIdx("tuv1.exact_match_key")),
            m_resultSet.getLong(columnIdx("tuv1.order_num")),
            m_resultSet.getString(columnIdx("tuv1.segment_string")),
            m_resultSet.getString(columnIdx("tuv1.state")),
            m_resultSet.getInt(columnIdx("tuv1.word_count")),
            m_resultSet.getString(columnIdx("tuv1.is_indexed")));
    }

    private Tuv getTargetTuv(Tu p_tu, GlobalSightLocale p_targetLocale)
        throws Exception
    {
        return createTuvObject(p_tu, p_targetLocale,
            m_resultSet.getLong(columnIdx("tuv2.id")),
            m_resultSet.getLong(columnIdx("tuv2.exact_match_key")),
            m_resultSet.getLong(columnIdx("tuv2.order_num")),
            m_resultSet.getString(columnIdx("tuv2.segment_string")),
            m_resultSet.getString(columnIdx("tuv2.state")),
            m_resultSet.getInt(columnIdx("tuv2.word_count")),
            m_resultSet.getString(columnIdx("tuv2.is_indexed")));
    }

}
