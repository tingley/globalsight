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

package com.globalsight.cxe.adapter.database;

//globalsight
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.globalsight.cxe.adapter.database.target.DatabaseWriteException;
import com.globalsight.cxe.adapter.database.target.DatabaseWriter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.diplomat.util.previewUrlXml.PreviewUrlXmlException;
import com.globalsight.diplomat.util.previewUrlXml.PreviewUrlXmlSubstituter;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.ling.common.Transcoder;
import com.globalsight.ling.common.TranscoderException;

/**
 * This class exists to do the work required by the DbTargetAdapter.
 * ExportDatabaseOperation.
 */
public class ExportDatabaseOperationHelper
{
    /**
     * Constructs a helper object
     * 
     * @throws DatabaseAdapterException
     */
    public ExportDatabaseOperationHelper(CxeMessage p_cxeMessage)
            throws DatabaseAdapterException
    {
        m_errorArgs = new String[1];
        m_errorArgs[0] = "CxeDatabaseTargetAdapter";
        setEventFlowXml(p_cxeMessage.getEventFlowXml());
        setPrsXml(p_cxeMessage.getMessageData());
        m_dbwriter = new DatabaseWriter();
    }

    /*
     * Sets the EventFlowXml used by the helper Also parses the XML to get the
     * root element
     * 
     * @throws DatabaseAdapterException
     */
    public void setEventFlowXml(String p_eventFlowXml)
            throws DatabaseAdapterException
    {
        m_eventFlowXml = p_eventFlowXml;
        try
        {
            m_eventFlowXmlElement = XmlUtil.parseForRootElement(m_eventFlowXml);
        }
        catch (Exception e)
        {
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs, e);
        }
    }

    /*
     * Sets the PrsXml used by the helperAlso parses the XML to get the root
     * element
     * 
     * @throws DatabaseAdapterException
     */
    public void setPrsXml(MessageData p_messageData)
            throws DatabaseAdapterException
    {
        try
        {
            byte[] binaryPrsXml = readBytesFromMessageData(p_messageData);
            m_prsXml = transcode(binaryPrsXml, getTargetCharset());
            m_databaseMode = null;
            m_prsXmlElement = XmlUtil.parseForRootElement(m_prsXml);

            // re-set the PrsXml to just the filled record otherwise when we
            // write back to the DB, we'll have GLOBALSIGHT_UNKOWN_SEGMENT in
            // the data
            if (getDatabaseMode().equals("preview"))
            {
                setFilledRecord();

                // now go through the PrsXmlElement and remove records that do
                // not have the
                // same sequence number
                String sequenceNumber = m_filledRecordElement
                        .getAttribute("sequenceNumber");
                String s = null;
                NodeList nl = m_prsXmlElement.getElementsByTagName("record");
                Node theNode;
                Node prsXmlNode = (Node) m_prsXmlElement;

                // keep an array of the nodes to remove so it won't affect
                // the NodeList traversal
                Node childrenToRemove[] = new Node[nl.getLength() - 1];
                int numChildrenToRemove = 0;
                for (int i = 0; i < nl.getLength(); i++)
                {
                    theNode = nl.item(i);
                    Element e = (Element) theNode;
                    s = e.getAttribute("sequenceNumber");
                    if (!sequenceNumber.equals(s))
                    {
                        childrenToRemove[numChildrenToRemove++] = theNode;
                    }
                }

                // now actually remove the children
                for (int j = 0; j < numChildrenToRemove; j++)
                {
                    prsXmlNode.removeChild(childrenToRemove[j]);
                }

                // now re-create the XML string
                int bufsiz = m_prsXml.length();
                OutputFormat oformat = new OutputFormat("xml", "UTF8", true);
                StringWriter stringWriter = new StringWriter(bufsiz);
                XMLSerializer xmlSerializer = new XMLSerializer(oformat);
                xmlSerializer.setOutputCharStream(stringWriter);
                xmlSerializer.serialize(m_prsXmlElement);
                m_prsXml = stringWriter.toString();
                Logger.writeDebugFile("newPrsXml.xml", m_prsXml);
            }
        }
        catch (Exception e)
        {
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs, e);
        }
    }

    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    public String getPrsXml()
    {
        return m_prsXml;
    }

    public Element getEventFlowXmlElement()
    {
        return m_eventFlowXmlElement;
    }

    public Element getPrsXmlElement()
    {
        return m_prsXmlElement;
    }

    /** Deletes the old PrsXml file from the DB if the database mode is final */
    public void deleteOldPrsXml() throws DatabaseAdapterException
    {
        Connection connection = null;
        try
        {
            if (getDatabaseMode().intern() == "final")
            {
                String quotedId = Utility.quote(m_prsXmlElement
                        .getAttribute("id")); // get the ID of the PrsXml file
                theLogger.println(Logger.DEBUG_C, "Deleting PrsXml id "
                        + quotedId + " from the DB.");
                connection = ConnectionPool.getConnection();
                connection.setAutoCommit(false);
                String sql = "DELETE FROM PRSXML_STORAGE WHERE NAME like ?";
                PreparedStatement st = connection.prepareStatement(sql);
                st.setString(1, quotedId);
                int rows = st.executeUpdate();
                connection.commit();
                st.close();
                if (rows == 1)
                    theLogger.println(Logger.DEBUG_C, "Deleted one row.");
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Unable to get database connection: ", cpe);
            throw new DatabaseAdapterException("DbConnectionEx", m_errorArgs,
                    cpe);
        }
        catch (SQLException sqle)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Unable to delete PrsXml from DB: ", sqle);
            rollback(connection);
            throw new DatabaseAdapterException("SqlExceptionEx", m_errorArgs,
                    sqle);
        }
        catch (Exception ioe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Unable to parse PrsXml for PrsXml id: ", ioe);
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs,
                    ioe);

        }
        finally
        {
            returnConnection(connection);
        }
    }

    /** Writes the content back to the database */
    public void writeToDatabase() throws DatabaseAdapterException
    {
        try
        {
            /* write back the data to the database */
            theLogger.println(Logger.INFO,
                    "Now writing content back to the database.");
            TaskXml taskxml = new TaskXml();
            taskxml.setEventFlowXml(m_eventFlowXml);
            taskxml.setPaginatedResultSetXml(m_prsXml);
            m_dbwriter.write(taskxml);
        }
        catch (DatabaseWriteException db_we)
        {
            String sqlStatement = db_we.getSqlStatement();
            if (sqlStatement.length() > 0)
            {
                Exception w = db_we.getWrappedException();
                String niceMsg = "The following SQL statement failed ("
                        + w.getMessage().trim() + "):\n"
                        + db_we.getSqlStatement();
                theLogger.println(Logger.ERROR, niceMsg);
            }

            theLogger.printStackTrace(Logger.ERROR,
                    "Problem writing to the customer db.", db_we);
            throw new DatabaseAdapterException("DbExceptionEx", m_errorArgs,
                    db_we);
        }
    }

    /*
     * This function extracts the PreviewUrlXml from the database based on the
     * record profile ID. It gets the original and target PrsXml snippets and
     * then uses the content in them to do the argument substitution in the
     * PreviewUrlXml.
     */
    public String findPreviewUrlXml() throws DatabaseAdapterException
    {
        // only do this if the databaseMode is not final
        if (getDatabaseMode().intern() == "final")
            return "";
        Connection connection = null;

        String subbedPreviewUrlXml = ""; // the complete after-substitution
                                         // preview URL XML
        try
        {
            // first get the record profile Id from the PrsXml snippet
            String recordProfileId = m_filledRecordElement
                    .getAttribute("recordProfileId");

            // now get the previewUrlXml out of the DB using the record profile
            // id
            connection = ConnectionPool.getConnection();
            String sql = new String(
                    "SELECT XML_URL FROM URL_LIST UL, CUSTOMER_DB_ACCESS_PROFILE CP"
                            + " WHERE CP.ID=?"
                            + " and UL.ID = CP.PREVIEW_URL_ID");
            PreparedStatement query = connection.prepareStatement(sql);
            query.setString(1, recordProfileId);
            ResultSet results = query.executeQuery();
            String previewUrlXml = null;
            while (results.next())
            {
                previewUrlXml = results.getString(1);
            }

            if (previewUrlXml != null)
            {
                theLogger.println(Logger.DEBUG_C, "Got previewUrlXml file");
                Logger.writeDebugFile("origPreviewUrl.xml", previewUrlXml);

                /* do the parameter substitution */
                theLogger.println(Logger.DEBUG_C,
                        "Now doing the parameter substitution");
                String originalPrsXmlSnippet = findOriginalPrsXmlSnippet();
                PreviewUrlXmlSubstituter subber = new PreviewUrlXmlSubstituter(
                        previewUrlXml, originalPrsXmlSnippet, m_filledRecord);

                subbedPreviewUrlXml = subber.doSubstitution();
                Logger.writeDebugFile("subbedPreviewUrl.xml",
                        subbedPreviewUrlXml);
            }
            else
            {
                theLogger.println(Logger.WARNING,
                        "There is no PreviewUrlXml for database source profile ID "
                                + recordProfileId);
            }
            query.close();

            if (subbedPreviewUrlXml.length() == 0)
                theLogger.println(Logger.WARNING,
                        "No preview url xml in database to preview.");
        }
        catch (ConnectionPoolException cnfe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Could connect to GlobalSight database", cnfe);
            throw new DatabaseAdapterException("DbConnectionEx", m_errorArgs,
                    cnfe);
        }
        catch (SQLException sqle)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "SQL error while processing Preview URL XML.", sqle);
            throw new DatabaseAdapterException("SqlExceptionEx", m_errorArgs,
                    sqle);
        }
        catch (PreviewUrlXmlException puxe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Parsing error while processing Preview Url Xml", puxe);
            throw new DatabaseAdapterException("Preview", m_errorArgs, puxe);
        }
        catch (DatabaseAdapterException dae)
        {
            throw dae;
        }
        catch (Exception e)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Error while preparing prs xml snippets for write back", e);
            throw new DatabaseAdapterException("Preview", m_errorArgs, e);
        }
        finally
        {
            returnConnection(connection);
        }

        return subbedPreviewUrlXml;
    }

    // ////////////////////////
    // PRIVATE member functions
    // ////////////////////////

    private String getTargetCharset() throws DatabaseAdapterException
    {
        String targetEncoding = "UTF-8";
        try
        {
            NodeList nl = m_eventFlowXmlElement.getElementsByTagName("target");
            Element targetElement = (Element) nl.item(0);
            Element charsetElement = (Element) targetElement
                    .getElementsByTagName("charset").item(0);
            targetEncoding = charsetElement.getFirstChild().getNodeValue();
            theLogger.println(Logger.DEBUG_C, "target charset = "
                    + targetEncoding);
        }
        catch (Exception e)
        {
            theLogger
                    .printStackTrace(
                            Logger.WARNING,
                            "Extracting target charset from eventFlowXml. Using default of UTF-8",
                            e);
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs, e);
        }
        return targetEncoding;
    }

    // convert the xml from binary to the right encoding
    private String transcode(byte[] p_binaryXml, String p_targetEncoding)
            throws DatabaseAdapterException
    {
        String xml = "";
        try
        {
            Transcoder t = new Transcoder();
            String javaname = CodesetMapper.getJavaEncoding(p_targetEncoding);
            xml = t.toUnicode(p_binaryXml, javaname);
        }
        catch (TranscoderException te)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Converting content to target charset", te);
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs, te);
        }
        return xml;
    }

    // database mode is either "final" or "preview"
    private String getDatabaseMode() throws DatabaseAdapterException
    {
        if (m_databaseMode == null)
        {
            try
            {
                NodeList nl = m_eventFlowXmlElement
                        .getElementsByTagName("target");
                Element target = (Element) nl.item(0);
                m_databaseMode = target.getAttribute("databaseMode");
                theLogger.println(Logger.DEBUG_C, "Read databaseMode: "
                        + m_databaseMode);
            }
            catch (Exception e)
            {
                theLogger
                        .printStackTrace(
                                Logger.ERROR,
                                "Could not extract target's databaseMode from EventFlowXml: ",
                                e);
                throw new DatabaseAdapterException("CxeInternalEx",
                        m_errorArgs, e);
            }
        }

        return m_databaseMode;
    }

    // retrieves the original PrsXmlSnippet from the database that corresponds
    // to the snippet
    // received by the DB Target Adapter for preview export
    private String findOriginalPrsXmlSnippet() throws DatabaseAdapterException
    {
        String originalSnippet = "";
        Connection connection = null;

        try
        {
            // if anything goes wrong at all just return the empty string -- no
            // preview
            Element elem = m_prsXmlElement;
            String id = elem.getAttribute("id"); // get the ID of the PrsXml
                                                 // file

            String sequenceNumber = m_filledRecordElement
                    .getAttribute("sequenceNumber"); // get the row id of this
                                                     // row
            String recordProfileId = m_filledRecordElement
                    .getAttribute("recordProfileId"); // get the row id of this
                                                      // row

            // now find the original PrsXml file with the same ID
            String originalPrsXml = null;
            theLogger.println(Logger.DEBUG_C,
                    "Now getting the original PrsXml for id " + id);
            String quotedId = Utility.quote(id);

            String sql = new String(
                    "SELECT PRSXML FROM PRSXML_STORAGE WHERE NAME like ?");
            connection = ConnectionPool.getConnection();
            PreparedStatement query = connection.prepareStatement(sql);
            query.setString(1, quotedId);
            ResultSet results = query.executeQuery();

            while (results.next())
            {
                originalPrsXml = results.getString(1);
            }
            if (originalPrsXml == null)
            {
                theLogger.println(Logger.ERROR, "No PrsXml in DB for id: "
                        + quotedId);
                throw new DatabaseAdapterException("CxeInternalEx",
                        m_errorArgs, null);
            }

            query.close();

            // now get the snippet from the original
            theLogger.println(Logger.DEBUG_C,
                    "Now getting the original PrsXml row snippet");
            elem = XmlUtil.parseForRootElement(originalPrsXml);
            NodeList nl = elem.getElementsByTagName("record");
            OutputFormat oformat = new OutputFormat("xml", "UTF8", true);
            oformat.setOmitDocumentType(true);
            oformat.setOmitComments(true);
            oformat.setOmitXMLDeclaration(true);
            oformat.setPreserveSpace(true);
            oformat.setIndenting(true);
            int bufsiz = m_prsXml.length();
            StringWriter stringWriter = new StringWriter(bufsiz);
            XMLSerializer xmlSerializer = new XMLSerializer(oformat);
            xmlSerializer.setOutputCharStream(stringWriter);

            for (int i = 0; i < nl.getLength(); i++)
            {
                Element rec = (Element) nl.item(i);
                String sn = rec.getAttribute("sequenceNumber");
                if (sn.equals(sequenceNumber))
                {
                    // found the right now
                    xmlSerializer.serialize(rec);
                    originalSnippet = new String(
                            "<?xml version=\"1.0\"?>\n<paginatedResultSetXml>\n"
                                    + stringWriter.toString()
                                    + "\n</paginatedResultSetXml>");
                    break;
                }
            }
            Logger.writeDebugFile("orig_snippet.xml", originalSnippet);
        }
        catch (ConnectionPoolException cnfe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Could connect to GlobalSight database", cnfe);
            throw new DatabaseAdapterException("DbConnectionEx", m_errorArgs,
                    cnfe);
        }
        catch (SQLException sqle)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "SQL error while processing PrsXml snippet.", sqle);
            throw new DatabaseAdapterException("SqlExceptionEx", m_errorArgs,
                    sqle);
        }
        catch (SAXException saxe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Parsing error while processing PrsXml snippet.", saxe);
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs,
                    saxe);
        }
        catch (IOException ioe)
        {
            theLogger.printStackTrace(Logger.ERROR,
                    "Parsing error while processing PrsXml snippet.", ioe);
            throw new DatabaseAdapterException("CxeInternalEx", m_errorArgs,
                    ioe);
        }
        finally
        {
            returnConnection(connection);
        }

        return originalSnippet;
    }

    // rollback the database from whatever just happened
    private void rollback(Connection p_connection)
    {
        if (p_connection != null)
        {
            // attempt a roll-back
            try
            {
                p_connection.rollback();
            }
            catch (Exception e)
            {
                theLogger.printStackTrace(Logger.ERROR, "Could not rollback: ",
                        e);
            }
        }
    }

    private void returnConnection(Connection p_connection)
    {
        if (p_connection != null)
        {
            try
            {
                ConnectionPool.returnConnection(p_connection);
            }
            catch (Exception e)
            {
            }
        }
    }

    // since the whole template comes back instead of just a snippet, we need
    // to get just whichever element was filled. The others have a constant in
    // them
    // and should be ignored
    private void setFilledRecord() throws Exception
    {
        OutputFormat oformat = new OutputFormat("xml", "UTF8", true);
        NodeList nl = m_prsXmlElement.getElementsByTagName("record");
        Element record = null;
        int bufsiz = m_prsXml.length();

        boolean foundRecord = false;
        String s = null;
        for (int i = 0; i < nl.getLength() && !foundRecord; i++)
        {
            XMLSerializer xmlSerializer = new XMLSerializer(oformat);
            StringWriter stringWriter = new StringWriter(bufsiz);
            xmlSerializer.setOutputCharStream(stringWriter);

            // first get the record
            record = (Element) nl.item(i);

            // now convert it to a string
            xmlSerializer.serialize(record);
            s = stringWriter.toString();
            stringWriter.close();

            if (s.indexOf(ExportConstants.DEFAULT_SEGMENT) == -1)
            {
                foundRecord = true;
                m_filledRecordElement = record;
                m_filledRecord = s;
                theLogger.println(Logger.DEBUG_D, "Found filled record: "
                        + m_filledRecord);
            }
        }

        if (!foundRecord)
        {
            theLogger.println(Logger.ERROR,
                    "Could not find filled record for substitution.");
        }
    }

    private byte[] readBytesFromMessageData(MessageData p_messageData)
            throws IOException
    {
        int size = (int) p_messageData.getSize();
        byte buffer[] = new byte[size];
        BufferedInputStream bis = new BufferedInputStream(
                p_messageData.getInputStream());
        bis.read(buffer, 0, size);
        bis.close();
        return buffer;
    }

    // ////////////////////////
    // PRIVATE member data
    // ////////////////////////
    private String m_eventFlowXml;
    private String m_prsXml;
    private Element m_eventFlowXmlElement;
    private Element m_prsXmlElement;
    private Element m_filledRecordElement;
    private String m_filledRecord;
    private String[] m_errorArgs;
    private String m_databaseMode;
    private DatabaseWriter m_dbwriter;
    private static Logger theLogger = Logger.getLogger();
}
