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

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * This class is an abstract class for retrieving data from System 3 table.
 */
public abstract class DataTable
{
    protected static final REProgram SUBFLOW_PATTERN = getReProgram("\\[%%(\\d+)\\]");
    protected static final REProgram MOVEABLE_PATTERN = getReProgram(" moveable=");
    protected static final REProgram ERASEABLE_PATTERN = getReProgram(" eraseable=");

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
     * 
     * @param p_connection
     *            Connection object to System 3 database
     * @param p_sourceLocale
     *            five character source locale
     */
    protected DataTable(Connection p_connection)
    {
        m_connection = p_connection;
    }

    /**
     * Retrieve all the data from data table.
     */
    abstract public void query() throws Exception;

    /**
     * get the next segment. query() must be called prior to calling this
     * method.
     * 
     * @return System3Segment object. If no more object found, null is retured.
     */
    abstract public System3Segment nextSegment() throws Exception;

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
