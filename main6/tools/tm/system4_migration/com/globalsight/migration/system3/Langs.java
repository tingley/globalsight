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

import java.util.Map;
import java.util.HashMap;

/**
 * This class is responsible for retrieving data from LANGS table.
 */
public class Langs
{
    private Connection m_connection = null;
    private Map m_nameByIdMap = null;
    private Map m_idByNameMap = null;
    
    private static final String LOCALE_NAME_BY_ID
        = "SELECT NAME FROM LANGS WHERE LANG_ID = ?";
    private static final String ID_BY_LOCALE_NAME
        = "SELECT LANG_ID FROM LANGS WHERE NAME = ?";
    
    /**
     * The only Constructor
     * @param p_connection Connection object to System 3 database
     */
    public Langs(Connection p_connection)
    {
        m_connection = p_connection;
        m_nameByIdMap = new HashMap();
        m_idByNameMap = new HashMap();
    }
    
    /**
     * get locale name by LANG_ID
     * @param p_id id of LANG_ID column
     * @return five character locale name
     */
    public String getName(int p_id)
        throws Exception
    {
        Integer id = new Integer(p_id);
        String locale = (String)m_nameByIdMap.get(id);
        if(locale == null)
        {
            // get the name from DB
            PreparedStatement stmt
                = m_connection.prepareStatement(LOCALE_NAME_BY_ID);
            stmt.setInt(1, p_id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            locale = rs.getString("NAME");
            stmt.close();

            // cache the data
            m_nameByIdMap.put(id, locale);
            m_idByNameMap.put(locale, id);
        }
        return locale;
    }
    

    /**
     * get id by locale name
     * @param p_name five character locale name
     * @return lang id
     */
    public int getId(String p_name)
        throws Exception
    {
        Integer id = (Integer)m_idByNameMap.get(p_name);
        if(id == null)
        {
            // get the id from DB
            PreparedStatement stmt
                = m_connection.prepareStatement(ID_BY_LOCALE_NAME);
            stmt.setString(1, p_name);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int id_int = rs.getInt("LANG_ID");
            stmt.close();

            // cache the data
            id = new Integer(id_int);
            m_nameByIdMap.put(id, p_name);
            m_idByNameMap.put(p_name, id);
        }
        return id.intValue();
    }
    

}
