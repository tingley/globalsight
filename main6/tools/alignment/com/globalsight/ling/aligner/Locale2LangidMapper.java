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

package com.globalsight.ling.aligner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.Hashtable;
import java.text.MessageFormat;

import com.globalsight.ling.aligner.AlignerException;
import com.globalsight.ling.aligner.AlignerExceptionConstants;

/**
 * Maps locale string to LANGID in ITEMS table.
 */
public class Locale2LangidMapper
{

	private static Connection dbConnection = null;
    private static Hashtable localeTable = null;

	public void setConnection(Connection connection)
	{
		dbConnection = connection;	
        localeTable = mapLocale2Langid();
	}

    public static int map(String locale)
        throws AlignerException
    {
        Integer id = (Integer)localeTable.get(locale);
        if(id == null)
        {
            Object[] args = {locale};
            throw new AlignerException
                (AlignerExceptionConstants.INVALID_LOCALE,
                 MessageFormat.format
                 (AlignerResources.getResource("InvalidLocale"), args));
        }
        return id.intValue();
    }

    private static Hashtable mapLocale2Langid() 
    {
        Hashtable locale_h = new Hashtable();

		try
		{
			String locale_sql = "SELECT LANG_ID, NAME FROM LANGS ORDER BY LANG_ID";
			PreparedStatement locale_stmt = dbConnection.prepareStatement(locale_sql);
			ResultSet rs = locale_stmt.executeQuery();

			while(rs.next())
			{
				locale_h.put(rs.getString(2),new Integer(rs.getInt(1)) );
			}
		}
		catch(SQLException e)
		{
			System.out.println("Could not create Locale Hashtable: " + e.toString());
		}
        return locale_h;
    }

    public Locale2LangidMapper()
	{
		super();
	}

    // test code
//      static public void main(String[] args)
//          throws AlignerException
//      {
//          int id = Locale2LangidMapper.map(args[0]);
//          System.out.println("LANGID = " + id);
//      }
    
}
