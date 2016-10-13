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

package com.globalsight.diplomat.servlet.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Vector;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.diplomat.util.Logger;

import com.globalsight.diplomat.servlet.config.GSA_User;
import com.globalsight.diplomat.servlet.config.UserLanguages;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.diplomat.servlet.config.SystemCharacterSetLanguage;

public class GSA_UserAdapter
{
    private final String DEFAULT_LANGUAGE = "en_US";

    private final String SYSTEM_LANGS = "SYSTEM_LANGS";
    private final String LANG_NAMES = "LANG_NAMES";
    private final String DISPLAY_LANG_ID = "DISPLAY_LANG_ID";
    private final String LANG_ID = "LANG_ID";
    private final String LANGS = "LANGS";
    private final String NAME = "NAME";
    private final String EXPORT_DIR = "EXPORT_DIR";
    private final String LONG_NAME = "LONG_NAME";
    private final String CHARSET = "CHARSET";
    private final String CHARSETS = "CHARSETS";
    private final String CHARSET_ID = "CHARSET_ID";
    private final String LANGS_CHARSETS = "LANGS_CHARSETS";


    private final String USERPAIRS = "USERPAIRS";
    private final String USERCODE = "USERCODE";
    private final String TRANSLATIONID = "TRANSLATIONID";
    private final String PAIRID = "PAIRID";
    private final String ASUSERS = "ASUSERS";
    private final String LANGPAIRS = "LANGPAIRS";
    private final String SOURCELANG = "SOURCELANG";
    private final String TARGETLANG = "TARGETLANG";
    private final String USERNAME = "USERNAME";
    private final String USERPASSWORD = "USERPASSWORD";
    private final String USERFULLNAME = "USERFULLNAME";
    private final String USERTYPE = "USERTYPE";
    private final String USERPHONENUMBER = "USERPHONENUM";
    private final String USERFAXNUMBER = "USERFAXNUM";
    private final String USEREMAIL = "USEREMAIL";
    private final String USERADDRESS = "USERADDRESS";
    private final String USERCOMMENT = "USERCOMMENT";
    private final String USERLANGUAGE = "USERLANG";
    private final String USERHOMEPAGE = "USERHOMEPAGE";
    private final String TRANSLATIONTABLE = "TRANSLATIONTABLE";
    private final String TRANSLATIONTYPE = "TRANSLATIONTYPE";

    private static GSA_UserAdapter m_instance = null;

    /////////////////////////////////////////////////
    private GSA_UserAdapter ()
    {}

    /////////////////////////////////////////////////
    public static GSA_UserAdapter getInstance()
    {
        if (null == m_instance)
            m_instance = new GSA_UserAdapter();
        return m_instance;
    }

    /////////////////////////////////////////////////
    // retrieve character sets for all languages in the system_langs table
    public Vector retrieveCharacterSets()
    {
        String sql = "SELECT S." + LANG_ID + ", L." + CHARSET_ID + ", C." +
            CHARSET + " FROM " + SYSTEM_LANGS + " S, " + LANGS_CHARSETS + " L, " +
            CHARSETS + " C WHERE S." + LANG_ID + " = L." + LANG_ID + " AND L." +
            CHARSET_ID + " = C." + CHARSET_ID + " ORDER BY C." + CHARSET;

        Connection connection = null;
        Vector sets = new Vector();

        try
        {
	    connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(sql);
            while ( results.next() )
            {
                int langID = results.getInt(LANG_ID);
                int charSetID = results.getInt(CHARSET_ID);
                String characterSet = results.getString(CHARSET);
                sets.add( new SystemCharacterSetLanguage(langID, charSetID, characterSet) );
            }
            query.close();
        }
	catch (ConnectionPoolException cpe) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", cpe);
	}
	catch (SQLException e) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", e);
	}
	finally {
	    try { ConnectionPool.returnConnection(connection); }
	    catch (ConnectionPoolException cpe) {}
	}
        return sets;
    }

    /////////////////////////////////////////////////
    public Vector retrieveStages()
    {
        Connection connection = null;
        String sql = "SELECT " + TRANSLATIONTYPE + " FROM " + TRANSLATIONTABLE +
            " ORDER BY " + TRANSLATIONID;
        Vector stages = new Vector();

        try
        {
	    connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(sql);
            while ( results.next() )
            {
                String stage = results.getString(TRANSLATIONTYPE);
                stages.addElement(stage);
            }
            query.close();
        }
	catch (ConnectionPoolException cpe) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", cpe);
	}
	catch (SQLException e) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", e);
	}
	finally {
	    try { ConnectionPool.returnConnection(connection); }
	    catch (ConnectionPoolException cpe) {}
	}

        return stages;
    }

    /////////////////////////////////////////////////
    public Vector retrieveLanguages()
    {
        return retrieveLanguages(DEFAULT_LANGUAGE);
    }

    /////////////////////////////////////////////////
    public Vector retrieveLanguages(String p_userLanguage)
    {
        // p_userlanguage is java language name, i.e., en_US

        String sql = "SELECT * FROM " + SYSTEM_LANGS + " S, (SELECT * FROM " + LANG_NAMES +
            " WHERE " + DISPLAY_LANG_ID + " = (SELECT " + LANG_ID + " FROM " +  LANGS +
            " WHERE " + NAME + " = " + Utility.quote(p_userLanguage) + ")) A, " + LANGS +
            " L WHERE A." + LANG_ID + " = S." + LANG_ID + " AND " + "A." + LANG_ID +
           " = L." + LANG_ID + " order by " + LONG_NAME;
        Vector languages = new Vector();
        Connection connection = null;
        try
        {
	    connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(sql);
            while ( results.next() )
            {
                int id = results.getInt(LANG_ID);
                String exportDir = results.getString(EXPORT_DIR);
                String longName = results.getString(LONG_NAME);
                String abbreviation = results.getString(NAME);

                languages.addElement(new UserLanguages(id, exportDir, longName, abbreviation));
            }
            query.close();
        }
	catch (ConnectionPoolException cpe) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", cpe);
	}
	catch (SQLException e) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", e);
	}
	finally {
	    try { ConnectionPool.returnConnection(connection); }
	    catch (ConnectionPoolException cpe) {}
	}
        return languages;
    }

    /////////////////////////////////////////////////
    public Vector retrieveUsers(int p_sourceLanguage, int p_targetLanguage)
    {
        Connection connection = null;
        String sql = "SELECT * FROM " + ASUSERS + " X, (SELECT " +
            USERCODE + " FROM " + USERPAIRS + " WHERE " + PAIRID +
            " = (SELECT " + PAIRID + " FROM " + LANGPAIRS + " WHERE " +
            SOURCELANG + " = " + p_sourceLanguage + " AND " + TARGETLANG +
            " = " + p_targetLanguage + ")) Y WHERE X." + USERCODE +
            " = Y." + USERCODE;

        Vector users = new Vector();
        try
        {
	    connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(sql);
            while ( results.next() )
            {
                int code = results.getInt(USERCODE);
                String name = results.getString(USERNAME);
                String password = results.getString(USERPASSWORD);
                String fullName = results.getString(USERFULLNAME);
                int type = results.getInt(USERTYPE);
                String phone = results.getString(USERPHONENUMBER);
                String fax = results.getString(USERFAXNUMBER);
                String email = results.getString(USEREMAIL);
                String address = results.getString(USERADDRESS);
                String comment = results.getString(USERCOMMENT);
                int language = results.getInt(USERLANGUAGE);
                String homePage = results.getString(USERHOMEPAGE);

                users.addElement(new GSA_User(code, name, password, fullName,
                    type, phone, fax, email, address, comment, language, homePage));
            }
            query.close();
        }
	catch (ConnectionPoolException cpe) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", cpe);
	}
	catch (SQLException e) {
	    theLogger.printStackTrace(Logger.ERROR, "GSA_UserAdapter", e);
	}
	finally {
	    try { ConnectionPool.returnConnection(connection); }
	    catch (ConnectionPoolException cpe) {}
	}
        return users;
    }

    private Logger theLogger = Logger.getLogger();
}
