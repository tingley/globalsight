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
package com.globalsight.everest.tm.searchreplace;

import org.apache.log4j.Logger;


import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.segmenttm.TuReader;

import com.globalsight.util.GlobalSightLocale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

/**
 * This class makes a query for TM concordance.
 * 
 * @deprecated This class appears to be unused.
 */
public class TuvConcordanceQuery
{
    static private Logger c_category =
        Logger.getLogger(
            TuvConcordanceQuery.class);

    static private String SEARCH_CASE_SENSITIVE;
    static private String SEARCH_CASE_INSENSITIVE;

    static
    {
        // construct a query string
        SEARCH_CASE_INSENSITIVE = "SELECT DISTINCT tu.id " +
            "FROM project_tm_tuv_t tuv1, project_tm_tuv_t tuv2, " +
            "     project_tm_tu_t tu " +
            "WHERE tuv1.tu_id = tu.id " +
            "  AND tuv2.tu_id = tu.id " +
            "  AND tuv1.locale_id = ? " +
            "  AND tuv2.locale_id = ? " +
            "  AND tu.tm_id = ? ";

        SEARCH_CASE_SENSITIVE = SEARCH_CASE_INSENSITIVE;

        SEARCH_CASE_SENSITIVE +=
            " AND tuv1.segment_string like ? escape '&'";
        // TODO: use NLS_LOWER instead of LOWER
        SEARCH_CASE_INSENSITIVE +=
            " AND LOWER(tuv1.segment_string) like LOWER(?) escape '&'";
    }


    private Connection m_connection;
    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;

    //
    // Constructor
    //

    public TuvConcordanceQuery(Connection p_connection)
    {
        m_connection = p_connection;
    }


    //
    // Public Methods
    //

    public ArrayList query(long p_tmId, String p_searchPattern,
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
        boolean p_caseSensitiveSearch)
        throws Exception
    {
        ArrayList result = new ArrayList();

        PreparedStatement stmt=null;
        ResultSet rset=null;
        try
        {
            m_sourceLocale = p_sourceLocale;
            m_targetLocale = p_targetLocale;
            String queryPattern = makeWildcardQueryString(p_searchPattern, '&');
            if (p_caseSensitiveSearch)
            {
                stmt = m_connection.prepareStatement(SEARCH_CASE_SENSITIVE);
            }
            else
            {
                stmt = m_connection.prepareStatement(SEARCH_CASE_INSENSITIVE);
            }

            stmt.setLong(1, p_sourceLocale.getId());
            stmt.setLong(2, p_targetLocale.getId());
            stmt.setLong(3, p_tmId);
            stmt.setString(4, queryPattern);
            rset = stmt.executeQuery();
            StringBuffer debugMsg = new StringBuffer("Concordance results: ");
            while (rset.next())
            {
                debugMsg.append(rset.getLong(1) + ", ");
                result.add(new Long(rset.getLong(1)));
            }

            debugMsg.append(" --- " + result.size() + " TUs.");
            if (c_category.isDebugEnabled())
            {
                c_category.debug(debugMsg.toString());                
            }
        }
        finally
        {
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
        }

        return result;
    }

    //
    // Private Methods
    //

    /**
     * '*' is the only allowed wildcard char the user can enter. '*'
     * is escaped using '\' to make '*' literal.
     *
     * p_escapeChar is an escape character used in LIKE predicate to
     * escape '%' and '_' wildcards.
     */
    static private String makeWildcardQueryString(String p_queryString,
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
        if (pattern.endsWith(asterisk) &&
            pattern.charAt(pattern.length() - 2) != '\\')
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

        // Add '%' to the beginning and the end of the string (because
        // the segment text is enclosed with <segment></segment> or
        // <localizable></localizable>)
        pattern = percent + pattern + percent;

        pattern = "<%>" + pattern + "</%>";

        if (c_category.isDebugEnabled())
        {
            c_category.debug("search + replace pattern = " + pattern);
        }

        return pattern;
    }
}
