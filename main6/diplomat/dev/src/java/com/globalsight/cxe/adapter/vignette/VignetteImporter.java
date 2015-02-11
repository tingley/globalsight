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

package com.globalsight.cxe.adapter.vignette;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.vignette.VignetteConnection;
import com.vignette.cms.client.beans.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.HashMap;

/**
 * Helper class used by the ImportFromVignetteOperation
 */
public class VignetteImporter
{
    private static final String PROP_FILE = "/properties/vignette.properties";

    // Private Members
    private String[] m_errorArgs = null;
    private String m_objectId = null;
    private String m_path = null;
    private String m_returnPath = null;
    private String m_fileProfileId = null;
    private String m_jobName = null;
    private String m_batchId = null;
    private String m_displayName = null;
    private String m_baseHref = null;
    private String m_codeset = null;
    private String m_locale = null;
    private String m_l10nProfileId = null;
    private String m_formatType = null;
    private String m_preExtractEvent = null;
    private String m_preMergeEvent = null;
    private StringBuffer m_filename = null;
    private int m_pageCount;
    private int m_pageNum;
    private int m_docPageCount;
    private int m_docPageNum;
    private String m_targetProjectMid = null;
    private String m_sourceProjectMid = null;
    private String m_returnStatus = null;
    private String m_versionFlag = null;
    private String m_defaultWorkflow = null;
    private boolean m_overrideAsUnextracted = false;
    private org.apache.log4j.Logger m_logger = null;
    private String m_importRequestType =null;

    //Vignette  variables for testing
    private CMS cms = null;
    private String tempDir = "C:\temp";
    private VignetteConnection m_conn = null;

    /**
     * Constructs a VignetteImporter helper object
     *
     * @param p_conn -- the VignetteConnection to use
     * @param p_errorArgs -- error args to use when throwing a VignetteAdapterException
     */
    public VignetteImporter(CxeMessage p_cxeMessage, org.apache.log4j.Logger p_logger, VignetteConnection p_conn)
    {
        m_logger = p_logger;
        m_conn = p_conn;
        m_errorArgs = new String[2];
        m_errorArgs[0] = p_logger.getName();

        HashMap params = p_cxeMessage.getParameters();
        m_objectId = (String)params.get("ObjectId");
        m_path = (String)params.get("Path");
        m_returnPath = (String)params.get("ReturnPath");
        m_displayName = m_path;
        m_returnStatus = (String)params.get("ReturnStatus");
        m_versionFlag = (String)params.get("VersionFlag");
        m_defaultWorkflow = (String)params.get("DefaultWorkflow");
        m_fileProfileId = (String)params.get("FileProfileId");
        m_jobName= (String)params.get("JobName");
        m_batchId = (String)params.get("BatchId");
        m_pageCount = ((Integer)params.get("PageCount")).intValue();
        m_pageNum = ((Integer)params.get("PageNum")).intValue();
        m_docPageCount = ((Integer)params.get("DocPageCount")).intValue();
        m_docPageNum = ((Integer)params.get("DocPageNum")).intValue();
        m_overrideAsUnextracted = ((Boolean)params.get(
                "OverrideFileProfileAsUnextracted")).booleanValue();
        m_importRequestType =(String) params.get(CxeProxy.IMPORT_TYPE);

    //the src and target project Mids were stuck together in order to avoid modifying active events
        String targetProjectMid = (String)params.get("TargetProjectMid");
    StringTokenizer st = new StringTokenizer(targetProjectMid,"|");
    m_sourceProjectMid = st.nextToken();
        m_targetProjectMid = st.nextToken();

        //the baseHref is used to display images on the page.
        m_baseHref = "";
    }


    /**
     * Writes the Vignette content to a temp file
     */
    private void writeStaticFileToTemp() throws Exception
    {
        CMSObject cmsobj = m_conn.cms.findByManagementId(this.m_objectId);
        StaticFile staticFile = (StaticFile) cmsobj;

        if (staticFile == null)
            throw new Exception ("Vignette returned null for objectId " +
                                 m_objectId + " path " + m_path);

        m_logger.info( "staticFile is " + staticFile);
        m_filename = new StringBuffer(tempDir);
        m_filename.append(File.separator);
        m_filename.append("vign");
        m_filename.append(System.currentTimeMillis());

        staticFile.extractContents(m_filename.toString()); //writes the file out to the temp directory
    }

    /**
    * Reads the file content from Vignette through Vignette's API
    *
    * @return byte[] -- the file in a byte array
    * @exception VignetteAdapterException
    */
    public MessageData readContent() throws VignetteAdapterException
    {
        try
        {
            readVignetteProperties();
            writeStaticFileToTemp();

            File theFile = new File (m_filename.toString());
            int filesize = (int) theFile.length();
            m_logger.info( "Reading vignette file: " + m_filename.toString() + " of size " + filesize);
            return new FileMessageData(m_filename.toString());
        }
        catch (Exception e)
        {
            m_logger.error(
                                     "Could not get read in vignette file " + m_filename, e);

            throw new VignetteAdapterException("InputOutputIm",m_errorArgs, e);
        }
        catch (Throwable t)
        {
            m_logger.error( "Problem using Vignette API", t);
            throw new VignetteAdapterException("InputOutputIm",m_errorArgs, null);
        }
    }


    /**
     * Gets data needed to construct the event flow XML
     * @exception VignetteAdapterException
     */
    private void getEventFlowXmlData() throws VignetteAdapterException
    {
        //now try to find the file data source profile type
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            connection =  ConnectionPool.getConnection();

            m_logger.debug(
                             "Getting file profile id info from the database:");

            StringBuffer sql = new StringBuffer ("SELECT KNOWN_FORMAT_TYPE.FORMAT_TYPE, KNOWN_FORMAT_TYPE.PRE_EXTRACT_EVENT, KNOWN_FORMAT_TYPE.PRE_MERGE_EVENT, FILE_PROFILE.CODE_SET, L10N_PROFILE.ID, LOCALE.ISO_LANG_CODE, LOCALE.ISO_COUNTRY_CODE");
            sql.append(" FROM KNOWN_FORMAT_TYPE, FILE_PROFILE, L10N_PROFILE, LOCALE");
            sql.append(" WHERE FILE_PROFILE.ID=?");
            sql.append(" AND KNOWN_FORMAT_TYPE.ID=FILE_PROFILE.KNOWN_FORMAT_TYPE_ID");
            sql.append(" AND L10N_PROFILE.ID=FILE_PROFILE.L10N_PROFILE_ID");
            sql.append(" AND LOCALE.ID=L10N_PROFILE.SOURCE_LOCALE_ID");

            query = connection.prepareStatement(sql.toString());
            query.setString(1,m_fileProfileId);
            results = query.executeQuery();
            if (results.next())
            {
                m_formatType = results.getString(1);
                m_preExtractEvent = m_overrideAsUnextracted ?
                    CxeMessageType.getCxeMessageType(
                        CxeMessageType.UNEXTRACTED_IMPORTED_EVENT).getName() :
                    results.getString(2);

                m_preMergeEvent = m_overrideAsUnextracted ?
                    CxeMessageType.getCxeMessageType(
                        CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT).getName() :
                    results.getString(3);

                m_codeset = results.getString(4);
                m_l10nProfileId = results.getString(5);
                String lang = results.getString(6);
                String country = results.getString(7);
                m_locale = lang + "_" + country;
            }
            else
            {
               m_logger.error("No file profile " + m_fileProfileId + " exists in DB.");
                m_errorArgs[2] = m_fileProfileId;
                throw new VignetteAdapterException ("FileProfileIm", m_errorArgs, null);
            }
        }
        catch (ConnectionPoolException cpe)
        {
            m_logger.error( "Could not get connection to DB: ", cpe);
            throw new VignetteAdapterException("DbConnectionIm",m_errorArgs, cpe);
        }
        catch (SQLException sqle)
        {
            m_logger.error(
                                     "Could not query file profile, and format type from the DB: ",
                                     sqle);

            throw new VignetteAdapterException("SqlExceptionIm",m_errorArgs, sqle);
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    /**
     * Creates the EventFlowXml. Assume going back to the VignetteTargetAdapter
     *
     * @return the string of Event Flow Xml
     * @exception VignetteAdapterException
     */
    public String makeEventFlowXml() throws VignetteAdapterException
    {
        getEventFlowXmlData();

        String l10nProfileId = null;
        String eventFlowXml = null;
        StringBuffer b = new StringBuffer(XmlUtil.formattedEventFlowXmlDtd());
        b.append("<eventFlowXml>\n");
        b.append("<preMergeEvent>");
        b.append(m_preMergeEvent);
        b.append("</preMergeEvent>\n");
        b.append("<postMergeEvent>");
        b.append(CxeMessageType.getCxeMessageType(CxeMessageType.VIGNETTE_EXPORT_EVENT).getName());
        b.append("</postMergeEvent>\n");
        b.append("<batchInfo l10nProfileId=\"");
        b.append(m_l10nProfileId);
        b.append("\" processingMode=\"automatic\">\n<batchId>");
        b.append(EditUtil.encodeXmlEntities(m_batchId));
        b.append("</batchId>\n");
        b.append("<pageCount>");
        b.append(Integer.toString(m_pageCount));
        b.append("</pageCount>\n");
        b.append("<pageNumber>");
        b.append(Integer.toString(m_pageNum));
        b.append("</pageNumber>\n");
        b.append("<docPageCount>");
        b.append(Integer.toString(m_docPageCount));
        b.append("</docPageCount>\n");
        b.append("<docPageNumber>");
        b.append(Integer.toString(m_docPageNum));
        b.append("</docPageNumber>\n");
        b.append("<displayName>");
        b.append(EditUtil.encodeXmlEntities(m_displayName));
        b.append("</displayName>\n");
        b.append("<baseHref>");
        b.append(m_baseHref);
        b.append("/");
        b.append("</baseHref>\n");
        b.append("<jobName>");

    Date curDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("D.H.mm");
    String jobName = m_jobName + sdf.format(curDate);
        b.append(EditUtil.encodeXmlEntities(jobName));
        b.append("</jobName></batchInfo>\n");
        b.append("<source name=\"VignetteSourceAdapter\" ");
        b.append("dataSourceType=\"vignette\" dataSourceId=\"");
        b.append(m_fileProfileId);
        b.append("\" formatType=\"");
        b.append(m_formatType);
        b.append("\" pageIsCxePreviewable=\"false\" importRequestType=\"");
        b.append(m_importRequestType).append("\">\n<locale>");
        b.append(m_locale);
        b.append("</locale>\n");
        b.append("<charset>");
        b.append(m_codeset);
        b.append("</charset>\n");
        b.append("</source>\n");
        b.append("<target name=\"VignetteTargetAdapter\">\n");
        b.append("<locale>unknown</locale>\n"); //the locale and charset get filled in by CXE later
        b.append("<charset>unknown</charset>\n");
        b.append("</target>\n");

        //add on some data that the Vignette target adapter will need later
        b.append("<category name=\"Vignette\">\n");
        b.append("<da name=\"ObjectId\"><dv>").append(this.m_objectId).append("</dv></da>\n");
        b.append("<da name=\"Path\"><dv>").append(this.m_path).append("</dv></da>\n");
        b.append("<da name=\"ReturnPath\"><dv>").append(this.m_returnPath).append("</dv></da>\n");
        b.append("<da name=\"TargetProjectMid\"><dv>").append(this.m_targetProjectMid).append("</dv></da>\n");
        b.append("<da name=\"SourceProjectMid\"><dv>").append(this.m_sourceProjectMid).append("</dv></da>\n");
        b.append("<da name=\"ReturnStatus\"><dv>").append(this.m_returnStatus).append("</dv></da>\n");
        b.append("<da name=\"VersionFlag\"><dv>").append(this.m_versionFlag).append("</dv></da>\n");
        b.append("<da name=\"DefaultWorkflow\"><dv>").append(this.m_defaultWorkflow).append("</dv></da>\n");
        b.append("</category>\n");

        b.append("</eventFlowXml>\n");
        eventFlowXml = b.toString();

        Logger.writeDebugFile("eventFlow.xml", eventFlowXml);

        return eventFlowXml;
    }

    public CxeMessageType getPreExtractEvent() throws Exception
    {
        return CxeMessageType.getCxeMessageType(m_preExtractEvent);
    }

    /**
    * Gets the property file
    * @throws Exception
    * @return String -- propety file path name
    */
    private File getPropertyFile() throws Exception
    {
        URL url = VignetteImporter.class.getResource(PROP_FILE);
        if (url == null)
            throw new FileNotFoundException("Property file " + PROP_FILE + " not found");
        return new File(url.toURI().getPath());
    }


    /**
     * Reads connection information from vignette.properties
     */
    private void readVignetteProperties() throws Exception
    {
        Properties props = new Properties();
        props.load(new FileInputStream(getPropertyFile()));
        tempDir =  props.getProperty("tempDir"); //temp network mapped dir
    }
}

