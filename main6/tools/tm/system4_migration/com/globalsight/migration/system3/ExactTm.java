/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.migration.system3;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import org.apache.regexp.RE;

/**
 * This class is responsible for retrieving data from EXACTTM table.
 */
public class ExactTm
    extends DataTable
{
    private PreparedStatement m_statement = null;
    private ResultSet m_resultset = null;
    private String m_dataType = null;
    

    private static final String SOURCE_QUERY
        = "SELECT TM.SOURCETEXT, SRC_LANG.NAME, TM.TRANSTEXT, TRG_LANG.NAME "
        + "FROM EXACTTM TM, LANGPAIRS LP, LANGS SRC_LANG, LANGS TRG_LANG "
        + "WHERE TM.PAIRID = LP.PAIRID AND LP.SOURCELANG = SRC_LANG.LANG_ID "
        + "AND LP.TARGETLANG = TRG_LANG.LANG_ID";
    
    /**
     * The only Constructor
     * @param p_connection Connection object to System 3 database
     * @param p_sourceLocale five character source locale
     */
    public ExactTm(Connection p_connection, String p_dataType)
        throws Exception
    {
        super(p_connection);
        m_dataType = p_dataType;
    }
    
    /**
     * Retrieve all the data from ITEMS table.
     */
    public void query()
        throws Exception
    {
        // retrieve the source data
        m_statement = m_connection.prepareStatement(SOURCE_QUERY);
        m_resultset = m_statement.executeQuery();
    }


    /**
     * get the next segment. query() must be called prior to calling
     * this method.
     * @return System3Segment object. If no more object found, null is
     * retured.
     */
    public System3Segment nextSegment()
        throws Exception
    {
        if(!m_resultset.next())
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
        
        String sourceText = m_resultset.getString(1);   // TM.SOURCETEXT
        String sourceLocale = m_resultset.getString(2); // SRC_LANG.NAME
        String targetText = m_resultset.getString(3);   // TM.TRANSTEXT
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
    private String replaceSubflow(String p_text)
        throws Exception
    {
        RE substSubflow = new RE();
        substSubflow.setProgram(SUBFLOW_PATTERN);
        return substSubflow.subst(p_text, "");
    }
    
    

}
