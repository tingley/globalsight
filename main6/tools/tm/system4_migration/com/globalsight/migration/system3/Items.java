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
package com.globalsight.migration.system3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import com.sun.org.apache.regexp.internal.RE;

/**
 * This class is responsible for retrieving data from ITEMS table.
 */
public class Items extends DataTable
{
    private Langs m_langs = null;
    private PreparedStatement m_statement = null;
    private ResultSet m_resultset = null;
    private Set m_subflowItems = null;
    private String m_sourceLocale = null;

    private static final String SOURCE_QUERY = "SELECT ITEMNUM, DFID, ITEMTYPE, DATATYPE, "
            + "TEXT FROM ITEMS WHERE MAXFLAG = 'Y' AND LANGID = ?";
    // STATUS = 1 means uptodate
    private static final String TARGET_QUERY = "SELECT TEXT, LANGID FROM ITEMS WHERE MAXFLAG = 'Y' AND "
            + "STATUS = 1 AND LANGID <> ? AND ITEMNUM = ? AND DFID = ?";
    private static final String SUBFLOW_QUERY = "SELECT TEXT FROM ITEMS WHERE MAXFLAG = 'Y' AND "
            + "LANGID = ? AND ITEMNUM = ? AND DFID = ?";

    private static final Set TRANSLATABLE_TYPES = setTranslatableTypesHash();

    /**
     * The only Constructor
     * 
     * @param p_connection
     *            Connection object to System 3 database
     * @param p_sourceLocale
     *            five character source locale
     */
    public Items(Connection p_connection, String p_sourceLocale)
            throws Exception
    {
        super(p_connection);
        m_sourceLocale = p_sourceLocale;
        m_langs = new Langs(p_connection);
    }

    /**
     * Retrieve all the data from ITEMS table.
     */
    public void query() throws Exception
    {
        // reset the subflow item number cache
        m_subflowItems = new HashSet(500);

        // get the source locale id
        int id = m_langs.getId(m_sourceLocale);

        // retrieve the source data
        m_statement = m_connection.prepareStatement(SOURCE_QUERY);
        m_statement.setInt(1, id);
        m_resultset = m_statement.executeQuery();
    }

    /**
     * get the next segment. query() must be called prior to calling this
     * method.
     * 
     * @return System3Segment object. If no more object found, null is retured.
     */
    public System3Segment nextSegment() throws Exception
    {
        if (!nextItem())
        {
            // no more data
            m_statement.close();
            return null;
        }

        System3Segment segment = new System3Segment();
        int itemnum = m_resultset.getInt(1); // ITEMNUM
        int dfid = m_resultset.getInt(2); // DFID

        segment.setItemType(m_resultset.getString(3)); // ITEMTYPE
        segment.setDataType(m_resultset.getString(4)); // DATATYPE

        // determine localize type from item type
        segment.setTranslatable(TRANSLATABLE_TYPES.contains(segment
                .getItemType()) ? true : false);

        // set the source text
        String text = m_resultset.getString(5); // TEXT
        text = replaceSubflow(text, m_sourceLocale, dfid);
        text = replaceObsoleteAttribName(text);
        segment.addText(m_sourceLocale, text);

        // set the target texts
        addTargetSegments(segment, itemnum, dfid);

        return segment;
    }

    // replace [%%nnn] stuff in p_text with a real string. The real
    // string is retrieved from the DB by giving parameters, itemnum,
    // p_locale and p_dfid.
    private String replaceSubflow(String p_text, String p_locale, int p_dfid)
            throws Exception
    {
        int start = 0;
        StringBuffer replaced = new StringBuffer(p_text.length() * 2);
        RE findSubflowTag = new RE();
        findSubflowTag.setProgram(SUBFLOW_PATTERN);

        while (findSubflowTag.match(p_text, start))
        {
            // replace [%%nnn] with a real item
            replaced.append(p_text.substring(start,
                    findSubflowTag.getParenStart(0)));
            int itemnum = Integer.parseInt(findSubflowTag.getParen(1));
            replaced.append(getSubflow(itemnum, p_dfid, p_locale));

            // put the itemnum-dfid in the exclude item list
            m_subflowItems.add(Integer.toString(itemnum) + "-"
                    + Integer.toString(p_dfid));

            start = findSubflowTag.getParenEnd(0);
        }
        // add the end of the string
        replaced.append(p_text.substring(start));

        return replaced.toString();
    }

    // move the resultset forward. It examins if the record is subflow
    // and skip it if it is. It returns false if no more record found.
    private boolean nextItem() throws Exception
    {
        boolean next = false;
        while (m_resultset.next())
        {
            int itemnum = m_resultset.getInt(1);
            int dfid = m_resultset.getInt(2);

            // skip the subflows
            String subflowId = Integer.toString(itemnum) + "-"
                    + Integer.toString(dfid);
            if (!m_subflowItems.contains(subflowId))
            {
                next = true;
                break;
            }
            else
            {
                // to save the memory
                m_subflowItems.remove(subflowId);
            }

        }
        return next;
    }

    // retrieve a subflow segment from the DB with given itemnum, dfid
    // and locale parameters
    private String getSubflow(int p_itemnum, int p_dfid, String p_locale)
            throws Exception
    {
        PreparedStatement statement = m_connection
                .prepareStatement(SUBFLOW_QUERY);
        statement.setInt(1, m_langs.getId(p_locale));
        statement.setInt(2, p_itemnum);
        statement.setInt(3, p_dfid);
        ResultSet resultset = statement.executeQuery();

        resultset.next();
        String subflow = resultset.getString(1);
        statement.close();

        return subflow;
    }

    // add all the target segments that have the given itemnum and
    // dfid to System3Segment object.
    private void addTargetSegments(System3Segment p_segment, int p_itemnum,
            int p_dfid) throws Exception
    {
        PreparedStatement statement = m_connection
                .prepareStatement(TARGET_QUERY);
        statement.setInt(1, m_langs.getId(m_sourceLocale));
        statement.setInt(2, p_itemnum);
        statement.setInt(3, p_dfid);
        ResultSet resultset = statement.executeQuery();

        // add each locale's segment
        while (resultset.next())
        {
            String text = resultset.getString(1);
            int lang_id = resultset.getInt(2);
            String locale = m_langs.getName(lang_id);

            if (text != null)
            {
                text = replaceSubflow(text, locale, p_dfid);
                text = replaceObsoleteAttribName(text);
                p_segment.addText(locale, text);
            }
        }

        statement.close();
    }

    private static Set setTranslatableTypesHash()
    {
        // list all the types that is translatable
        Set set = new HashSet();
        set.add("string");
        set.add("text");
        set.add("abbr");
        set.add("accesskey");
        set.add("alt");
        set.add("char");
        set.add("label");
        set.add("prompt");
        set.add("standby");
        set.add("summary");
        set.add("title");
        set.add("value");
        set.add("meta-content");
        return set;
    }

}
