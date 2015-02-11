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

package com.globalsight.tools.reindexer;

import java.sql.Connection;
import java.sql.Statement;


/**
 * Run analyze command on the index table.
 */
public class AnalyzeIndexTable
{
    private static final String ANALYZE_TABLE
        = "analyze table segment_tm_token_t estimate statistics sample 10 percent";

    private Connection m_connection;

    public AnalyzeIndexTable(Connection p_connection)
    {
        m_connection = p_connection;
    }


    public void analyze()
    {
        Statement st = null;

        try
        {
            st = m_connection.createStatement();
            st.executeUpdate(ANALYZE_TABLE);
            st.close();
            m_connection.commit();
        }
        catch(Exception e)
        {
            System.err.println("Warning: Failed to run analyze command");
            e.printStackTrace(System.err);
        }
    }
}

