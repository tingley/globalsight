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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Hashtable;

import com.globalsight.util.GlobalSightLocale;


/**
 * Responsible for creating GlobalSightLocale object.
 */
public class LocaleManager
{
    private static final String LOCALE_BY_ID
        = "SELECT iso_lang_code, iso_country_code, is_ui_locale "
        + "FROM locale WHERE id = ?";
    
    private Connection m_connection;
    private PreparedStatement m_stmt;

    public LocaleManager(Connection p_connection)
        throws Exception
    {
        m_connection = p_connection;
        m_stmt = m_connection.prepareStatement(LOCALE_BY_ID);
    }


    public GlobalSightLocale getGlobalSightLocale(long p_localeId)
        throws Exception
    {
        GlobalSightLocale loc = null;
        ResultSet rs = null;
        
        try
        {
            m_stmt.setLong(1, p_localeId);
            rs = m_stmt.executeQuery();
            rs.next();

            loc = new GlobalSightLocale(rs.getString("iso_lang_code"),
                rs.getString("iso_country_code"),
                rs.getString("is_ui_locale").equals("Y") ? true : false);
            loc.setId(p_localeId);
        }
        finally
        {
            if(rs != null)
            {
                rs.close();
            }
        }

        return loc;
    }
    
    
}

