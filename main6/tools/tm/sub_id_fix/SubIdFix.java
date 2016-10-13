/*
 * Copyright (c) 2005 GlobalSight Corporation. All rights reserved.
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

import java.sql.*;
import java.util.*;
import java.io.*;
import java.text.*;

import oracle.jdbc.driver.OracleResultSet;
import oracle.sql.CLOB;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

/**
 * <p>This class connects to a GlobalSight database and check the sub
 * id inconsistency in the Page TM.
 *
 * <p> The inconsistency can occur when the segments get merged. The
 * problem has been solved after 6.4.
 */
public class SubIdFix
{
    private static final int EOF = -1;

//    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
//    private static final String CONNECT_THIN_CLIENT = "jdbc:oracle:thin:@";
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String CONNECT_THIN_CLIENT = "jdbc:mysql://";

    /** Helperclass to connect to Oracle. */
    public static class ConnectData
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


    //
    // Private Members
    //
    private ConnectData m_connectData = null;
    private Connection  m_connection = null;

    //
    // Main Routine
    //
    public static void main(String args[])
        throws Exception
    {
        ConnectData connectData = null;

        // Allow specification of connect data.
        if (args.length >= 5)
        {
            String server = args[0];
            String user   = args[1];
            String passwd = args[2];
            String sid    = args[3];
            String port   = args[4];

            connectData = new ConnectData(server, user, passwd, sid, port);
        }

        if (connectData == null)
        {
            System.err.println(
                "Usage: java SubIdFix server user password instance port");

            System.exit(1);
        }

        SubIdFix x = new SubIdFix(connectData);
        
        x.checkSubId();
    }


    //
    // Constructor
    //

    public SubIdFix(ConnectData p_connectData)
        throws Exception
    {
        m_connectData = p_connectData;
        m_connection = connect();
    }

    //
    // Public Methods
    //

    /**
     * check the sub id inconsistency in Page TM.
     */
    public void checkSubId()
        throws Exception
    {
        Statement stmt = m_connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "select id, segment_string, segment_clob from page_tm_tuv_t");

        while (rs.next())
        {
            long id = rs.getLong(1);
            String segment = rs.getString(2);
            if(segment == null)
            {
                segment = readClob(rs, 3);
            }

            examineSub(id, segment);
        }

        rs.close();
        stmt.close();
    }
    

    private String readClob(ResultSet p_rs, int p_columnIndex)
        throws Exception
    {
        CLOB clob = ((OracleResultSet)p_rs).getCLOB(p_columnIndex);

        String result = "";

        if (clob != null)
        {
            Reader r = clob.getCharacterStream();
            StringBuffer sb = new StringBuffer();
            int charsRead = 0;
            char[] buffer = new char[clob.getChunkSize()];
            while ((charsRead = r.read(buffer)) != EOF)
            {
                sb.append(buffer, 0, charsRead);
            }
            r.close();
            result = sb.toString();
        }
        return result;
    }


    private void examineSub(long p_id, String p_segment)
        throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        
        SAXParser parser = factory.newSAXParser();
        try
        {
            parser.parse(new InputSource(new StringReader(p_segment)),
                new GxmlHandler(p_id));
        }
        catch(SAXException e)
        {
            e.printStackTrace();
        }
        
    }
    

    private class GxmlHandler
        extends DefaultHandler
    {
        long m_tuvId;
        
        public GxmlHandler(long p_tuvId)
        {
            m_tuvId = p_tuvId;
        }
        
        public void startElement(String uri, String localName,
            String qName, Attributes attributes)
            throws SAXException
        {
            try
            {
                if(qName.equals("sub"))
                {
                    String subIdStr = attributes.getValue("id");
                    int subId = Integer.parseInt(subIdStr);
                    if(subId > 99)
                    {
                        System.out.println(
                            "id " + m_tuvId + " has sub id " + subId);
                    }
                }
            }
            catch(Exception e)
            {
                SAXException se;
                if(e instanceof SAXException)
                {
                    se = (SAXException)e;
                }
                else
                {
                    se = new SAXException(e);
                }
                
                throw se;
            }
        }
        

        // ErrorHandler interface methods

        public void error(SAXParseException e)
            throws SAXException
        {
            throw new SAXException("GXML parse error at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage()
                + "\n  Tuv id:" + m_tuvId);
        }

        public void fatalError(SAXParseException e)
            throws SAXException
        {
            error(e);
        }

        public void warning(SAXParseException e)
        {
            System.err.println("GXML parse warning at\n  line "
                + e.getLineNumber()
                + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
        }
    }


    public Connection connect()
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

}
