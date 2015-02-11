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

import org.apache.regexp.RE;
import org.apache.regexp.REProgram;
import org.apache.regexp.RECompiler;
import org.apache.regexp.RESyntaxException;

/**
 * This class is an abstract class for retrieving data from System 3 table.
 */
public abstract class DataTable
{
    protected static final REProgram SUBFLOW_PATTERN
        = getReProgram("\\[%%(\\d+)\\]");
    protected static final REProgram MOVEABLE_PATTERN
        = getReProgram(" moveable=");
    protected static final REProgram ERASEABLE_PATTERN
        = getReProgram(" eraseable=");
    
    protected static final String WITH_MOVABLE = " movable=";
    protected static final String WITH_ERASABLE = " erasable=";
    
    private static REProgram getReProgram(String p_pattern)
    {
        REProgram pattern = null;
        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }
        return pattern;
    }


    protected Connection m_connection = null;

    /**
     * Constructor
     * @param p_connection Connection object to System 3 database
     * @param p_sourceLocale five character source locale
     */
    protected DataTable(Connection p_connection)
    {
        m_connection = p_connection;
    }
    
    /**
     * Retrieve all the data from data table.
     */
    abstract public void query()
        throws Exception;


    /**
     * get the next segment. query() must be called prior to calling
     * this method.
     * @return System3Segment object. If no more object found, null is
     * retured.
     */
    abstract public System3Segment nextSegment()
        throws Exception;
    
        
    // replace obsolete attribute names such as "moveable" and
    // "eraseable" to "movable" and "erasable", respectably.
    protected String replaceObsoleteAttribName(String p_text)
    {
        RE substMoveable = new RE();
        substMoveable.setProgram(MOVEABLE_PATTERN);
        String replaced_moveable = substMoveable.subst(p_text, WITH_MOVABLE);

        RE substEraseable = new RE();
        substEraseable.setProgram(ERASEABLE_PATTERN);
        return substEraseable.subst(replaced_moveable, WITH_ERASABLE);
    }
        

}
