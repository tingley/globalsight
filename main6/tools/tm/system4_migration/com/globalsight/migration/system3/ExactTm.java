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

import com.sun.org.apache.regexp.internal.RE;

/**
 * This class is responsible for retrieving data from EXACTTM table.
 */
public class ExactTm extends DataTable
{
    private PreparedStatement m_statement = null;
    private ResultSet m_resultset = null;
    private String m_dataType = null;

    private static final String SOURCE_QUERY = "SELECT TM.SOURCETEXT, SRC_LANG.NAME, TM.TRANSTEXT, TRG_LANG.NAME "
            + "FROM EXACTTM TM, LANGPAIRS LP, LANGS SRC_LANG, LANGS TRG_LANG "
            + "WHERE TM.PAIRID = LP.PAIRID AND LP.SOURCELANG = SRC_LANG.LANG_ID "
            + "AND LP.TARGETLANG = TRG_LANG.LANG_ID";

    /**
     * The only Constructor
     * 
     * @param p_connection
     *            Connection object to System 3 database
     * @param p_sourceLocale
     *            five character source locale
     */
    public ExactTm(Connection p_connection, String p_dataType) throws Exception
    {
        super(p_connection);
        m_dataType = p_dataType;
    }

    /**
     * Retrieve all the data from ITEMS table.
     */
    public void query() throws Exception
    {
        // retrieve the source data
        m_statement = m_connection.prepareStatement(SOURCE_QUERY);
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
        if (!m_resultset.next())
        {
            // no more data
            m_statement.close();
            return null;
        }

        System3Segment segment = new System3Segment();
        // set the default values to System3Segment because of the
        // lack of information in EXACTTM table
        segment.setItemType("text");
        segment.setDataType(m_dataType);
        segment.setTranslatable(true);

        String sourceText = m_resultset.getString(1); // TM.SOURCETEXT
        String sourceLocale = m_resultset.getString(2); // SRC_LANG.NAME
        String targetText = m_resultset.getString(3); // TM.TRANSTEXT
        String targetLocale = m_resultset.getString(4); // TRG_LANG.NAME

        // set the source texts
        sourceText = replaceSubflow(sourceText);
        sourceText = replaceObsoleteAttribName(sourceText);
        segment.addText(sourceLocale, sourceText);

        // set the target texts
        targetText = replaceSubflow(targetText);
        targetText = replaceObsoleteAttribName(targetText);
        segment.addText(targetLocale, targetText);

        return segment;
    }

    // replace [%%nnn] in p_text with an empty string. The referenced
    // item by %%nnn is most likely not existing any more (Teamsite
    // behavior).
    private String replaceSubflow(String p_text) throws Exception
    {
        RE substSubflow = new RE();
        substSubflow.setProgram(SUBFLOW_PATTERN);
        return substSubflow.subst(p_text, "");
    }

}
