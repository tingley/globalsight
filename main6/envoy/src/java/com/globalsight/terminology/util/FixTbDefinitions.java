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
package com.globalsight.terminology.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import com.globalsight.terminology.Definition;
import com.globalsight.terminology.TermbaseException;

/**
 * Reads out all termbases and their definition from a GlobalSight database.
 * Fixes the definitions that are incorrect.
 */
public class FixTbDefinitions
{
    static private final int EOF = -1;

    static private final String DRIVER = "com.mysql.jdbc.Driver";
    static private final String CONNECT_THIN_CLIENT = "jdbc:mysql://";
    static private final String PROPERTIES = "db_connection";

    /** Helperclass to connect to database. */
    static public class ConnectData
    {
        public String m_server = "(default)";
        public String m_user = null;
        public String m_password = null;
        public String m_sid = null;
        public int    m_portNumber = 1521;
        public String m_connectString = null;

        public ConnectData(String p_connectString,
            String p_user, String p_password)
        {
            m_connectString = p_connectString;
            m_user = p_user;
            m_password = p_password;
        }

        public ConnectData(String p_server, String p_user, String p_passwd,
            String p_sid, String p_port)
        {
            m_server   = p_server;
            m_user     = p_user;
            m_password = p_passwd;
            m_sid      = p_sid;
            m_portNumber = Integer.parseInt(p_port);
        }

        public String getConnectString()
        {
            if (m_connectString == null)
            {
                m_connectString = CONNECT_THIN_CLIENT + m_server +
                    ":" + m_portNumber + "/" + m_sid;
            }

            return m_connectString;
        }
    }

    static public class TbDefinition
    {
        public long m_id;
        public String m_name;
        public String m_desc;
        public String m_definition;

        public TbDefinition(long p_id, String p_name, String p_desc,
            String p_definition)
        {
            m_id = p_id;
            m_name = p_name;
            m_desc = p_desc;
            m_definition = p_definition;
        }
    }

    //
    // Private Members
    //
    private ConnectData m_connectData = null;
    private Connection  m_connection = null;

    private DateFormat m_dateFormat = null;
    private boolean m_fixerrors;

    private ArrayList m_termbases = new ArrayList();

    //
    // Main Routine
    //
    public static void main(String args[])
        throws FileNotFoundException, ClassNotFoundException,
               SQLException, IOException, TermbaseException
    {
        ConnectData connectData = null;
        boolean fixerrors = false;

        // Allow specification of connect data.
        if (args.length >= 5)
        {
            String server = args[0];
            String user   = args[1];
            String passwd = args[2];
            String sid    = args[3];
            String port   = args[4];

            connectData = new ConnectData(server, user, passwd, sid, port);

            if (args.length > 5)
            {
                fixerrors = true;
            }
        }
        else if (args.length >= 0)
        {
            connectData = getConnectData();

            if (args.length > 1)
            {
                fixerrors = true;
            }
        }

        if (connectData == null)
        {
            System.err.println(
                "Usage: java FixTbDefinitions " +
                "[server user password instance port] -fix\n" +
                "\n" +
                "Reads and validates GlobalSight termbase definitions.\n" +
                "Any errors are printed to stderr.\n" +
                "\n" +
                "If the argument '-fix' is present, errors are corrected.");

            System.exit(1);
        }

        FixTbDefinitions x = new FixTbDefinitions(connectData, fixerrors);
        x.readTbDefinitions();
    }

    private static ConnectData getConnectData()
    {
        ConnectData result = null;

        String connectString = null;
        String user = null;
        String password = null;

        try
        {
            ResourceBundle res =
                ResourceBundle.getBundle(PROPERTIES, Locale.US);

            Enumeration keys = res.getKeys();
            while (keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                String tmp = key.toLowerCase();

                if (tmp.equals("connect_string"))
                {
                    connectString = res.getString(key);
                }
                else if (tmp.equals("user_name"))
                {
                    user = res.getString(key);
                }
                else if (tmp.equals("password"))
                {
                    password = res.getString(key);
                }
            }

            result = new ConnectData(connectString, user, password);
        }
        catch (Throwable t)
        {
        }

        return result;
    }

    //
    // Constructor
    //

    public FixTbDefinitions(ConnectData p_connectData, boolean p_fixerrors)
    {
        m_connectData = p_connectData;
        m_fixerrors = p_fixerrors;

        m_dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
    }

    //
    // Public Methods
    //

    public void trace(String p_message)
    {
        System.err.println(m_dateFormat.format(new java.util.Date()) +
            "; " + p_message);
    }

    /**
     * Reads all termbase definitions.
     */
    public void readTbDefinitions()
        throws FileNotFoundException, ClassNotFoundException,
               SQLException, IOException, TermbaseException
    {
        m_connection = connect();
        trace("Connected to " + m_connectData.m_server);
        ResultSet rs = null;
        PreparedStatement stmt=null;

        try {
            stmt = m_connection.prepareStatement(
                "select tbid, tb_name, tb_description, tb_definition " +
                "from tb_termbase ");
            rs = stmt.executeQuery();

            while (rs.next())
            {
                long tbid = rs.getLong(1);
                String tbname = rs.getString(2);
                String tbdesc = rs.getString(3);
                String tbdef = rs.getString(4);
                m_termbases.add(new TbDefinition(tbid, tbname, tbdesc, tbdef));
            }
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(stmt);
            for (int i = 0, max = m_termbases.size(); i < max; i++)
            {
                TbDefinition tbdef = (TbDefinition)m_termbases.get(i);
                Definition def = new Definition(tbdef.m_definition);
                // Validate TB definition. This removes invalid nodes from the XML.
                def.validate();

                if (!tbdef.m_definition.equals(def.getXml()))
                {
                    System.err.println("Termbase definition for TB " + tbdef.m_id +
                                       " is incorrect.\n" +
                                       "It should be\n" + def.getXml() + "\n");

                    if (m_fixerrors)
                    {
                        System.err.println("Updating termbase definition for TB " +
                                           tbdef.m_id);
                        writeClob(tbdef.m_id, def.getXml());
                    }
                }
            }
            m_connection.commit();
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(stmt);
        }
    }


    private Connection connect()
        throws SQLException, ClassNotFoundException
    {
        Connection result = null;

        try
        {
            // We need to load the thin client jdbc driver.
            Class.forName(DRIVER);
        }
        catch (ClassNotFoundException e)
        {
            // e.printStackTrace();
            throw e;
        }

        result = DriverManager.getConnection (
            m_connectData.getConnectString(),
            m_connectData.m_user, m_connectData.m_password);

        result.setAutoCommit(false);

        return result;
    }

    private void writeClob(long p_id, String p_value)
        throws SQLException, IOException
    {
        ResultSet rs = null;
        PreparedStatement stmt=null;
        try
        {
        	stmt = m_connection.prepareStatement(
                    "update tb_termbase " +
                    "set tb_definition = ? " +
                    "where tbid = " + p_id);
        	stmt.setString(1, p_value);
            stmt.executeUpdate();
            m_connection.commit();
            SqlUtil.silentClose(stmt);
        	
            trace("Definition for termbase id=" + p_id + " updated.");
        }
        finally
        {
            SqlUtil.silentClose(rs);
            SqlUtil.silentClose(stmt);
        }
    }
}

// test on qa3 globalsight password st3gsor 1521

