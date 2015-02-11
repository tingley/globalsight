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

package com.globalsight.ling.util;

import com.globalsight.ling.util.Arguments;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

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
        m_connection = getDatabaseConnection(m_strConnect, m_strUser, m_strPasswd);                
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
     * Connects to an Oracle database and returns a Java Connection
     * object.
    */
    private Connection getDatabaseConnection(String url, String user, String passwd)
        throws Exception
    {
        Connection o_connection = null;
        
        System.out.println("Connecting to " + m_strConnect + " as " + m_strUser); 
        
        try
        {
            // We need to preload the thin client jdbc driver
            Class.forName(DRIVER);
        }
        catch (ClassNotFoundException ex)
        {
            System.err.println("Error: please add the Oracle JDBC library " +
              "(O816Classes12.zip) to your CLASSPATH.");
            throw ex;
        }

        o_connection = DriverManager.getConnection(url, user, passwd);

            // Create Oracle DatabaseMetaData object
            DatabaseMetaData meta = o_connection.getMetaData();
            System.out.println("Connected.  JDBC driver is " +
              meta.getDriverName() + " " + meta.getDriverVersion() + ".");      

        return o_connection;
    }
    
    /*
     * Get segments from items table with HTML tags and remove any tags
     * that are not segment legal
    */
    private void processSegments()
        throws Exception
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
            Arguments getopt = new Arguments ();
            int c;

            getopt.setUsage(new String[]
                    {
 "Usage: java com.globalsight.ling.util.RemoveNonSegmentTags -c connect -u user -p passwd",
 "",
 "Removes all segment illegal HTML tags (defined by 4.x HTML extractor)",
 "from segments in an GlobalSight 2.x items table.", 
 "",
 "  -h: show this help.",
 "  -c connect: the Java connect string for the oracle DB",
 "  -u user:    the user for 2.x DB login.",
 "  -p passwd:  the password 2.x DB login."
                    } );

            getopt.parseArgumentTokens(argv,
              new char[] {'c','u','p'});

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
            
            tagRemover = new RemoveNonSegmentTags(str_connect, str_user, str_passwd);    
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
