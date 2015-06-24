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

package com.globalsight.ling.util;

import com.globalsight.ling.util.Arguments;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/** 
 */
public class RemoveNonSegmentTags
{
    private final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    private String m_strConnect;
    private String m_strUser;
    private String m_strPasswd;

    // The connections to the Oracle databases.
    private Connection m_connection;

    public RemoveNonSegmentTags(String connect, String user, String passwd)
            throws Exception
    {
        m_strConnect = connect;
        m_strUser = user;
        m_strPasswd = passwd;
        m_connection = getDatabaseConnection(m_strConnect, m_strUser,
                m_strPasswd);
    }

    public void done()
    {
        if (m_connection != null)
        {
            try
            {
                m_connection.close();
            }
            catch (Exception ex)
            {
            }
            m_connection = null;
        }
    }

    /*
     * Connects to an Oracle database and returns a Java Connection object.
     */
    private Connection getDatabaseConnection(String url, String user,
            String passwd) throws Exception
    {
        Connection o_connection = null;

        System.out
                .println("Connecting to " + m_strConnect + " as " + m_strUser);

        try
        {
            // We need to preload the thin client jdbc driver
            Class.forName(DRIVER);
        }
        catch (ClassNotFoundException ex)
        {
            System.err.println("Error: please add the Oracle JDBC library "
                    + "(O816Classes12.zip) to your CLASSPATH.");
            throw ex;
        }

        o_connection = DriverManager.getConnection(url, user, passwd);

        // Create Oracle DatabaseMetaData object
        DatabaseMetaData meta = o_connection.getMetaData();
        System.out.println("Connected.  JDBC driver is " + meta.getDriverName()
                + " " + meta.getDriverVersion() + ".");

        return o_connection;
    }

    /*
     * Get segments from items table with HTML tags and remove any tags that are
     * not segment legal
     */
    private void processSegments() throws Exception
    {
        String sql = "select TEXT from ITEMS where TEXT like '<%>'";
        String segment;

        try
        {
            Statement query = m_connection.createStatement();
            ResultSet rs = query.executeQuery(sql);
            rs.setFetchDirection(ResultSet.FETCH_FORWARD);
            rs.setFetchSize(100);

            while (rs.next())
            {
                segment = rs.getString(1);

            }

            rs.close();
        }
        catch (SQLException ex)
        {
            System.err.println("Error: can't read segments from items table");
            throw ex;
        }
    }

    public static void main(String argv[])
    {
        String str_connect = null;
        String str_user = null;
        String str_passwd = null;
        RemoveNonSegmentTags tagRemover = null;

        try
        {
            Arguments getopt = new Arguments();
            int c;

            getopt.setUsage(new String[]
            {
                    "Usage: java com.globalsight.ling.util.RemoveNonSegmentTags -c connect -u user -p passwd",
                    "",
                    "Removes all segment illegal HTML tags (defined by 4.x HTML extractor)",
                    "from segments in an GlobalSight 2.x items table.", "",
                    "  -h: show this help.",
                    "  -c connect: the Java connect string for the oracle DB",
                    "  -u user:    the user for 2.x DB login.",
                    "  -p passwd:  the password 2.x DB login." });

            getopt.parseArgumentTokens(argv, new char[]
            { 'c', 'u', 'p' });

            while ((c = getopt.getArguments()) != -1)
            {
                switch (c)
                {
                    case 'j':
                    case 'J':
                        str_connect = getopt.getStringParameter();
                        break;
                    case 'p':
                    case 'P':
                        str_passwd = getopt.getStringParameter();
                        break;
                    case 'u':
                    case 'U':
                        str_user = getopt.getStringParameter();
                        break;
                    case 'h':
                    case 'H':
                    case '?':
                    default:
                        getopt.printUsage();
                        System.exit(1);
                        break;
                }
            }

            // check required args
            if (str_connect == null || str_user == null || str_passwd == null)
            {
                getopt.printUsage();
                System.exit(1);
            }

            tagRemover = new RemoveNonSegmentTags(str_connect, str_user,
                    str_passwd);
            tagRemover.processSegments();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        finally
        {
            if (tagRemover != null)
            {
                tagRemover.done();
            }
        }

        System.exit(0);
    }
}
