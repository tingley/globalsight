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
package com.globalsight.cxe.adapter.teamsite;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.cms.teamsite.TeamSiteExchange;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.log.GlobalSightCategory;
import java.io.BufferedInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.cxe.util.CxeProxy;


/**
 * Handles the process of importing content from TeamSite
 */
public class Importer
{
    private static final String TS_BASEHREF_MOUNT = "/iw-mount/";

    private String TS_DEFAULT = null;
    private String TSserverName = null;
    private String TSWebserverPort = null;
    private String TSserverPort = null;
    private String TSMountDir = null;
    private String TSServerType = null;
    private String TSReImport = null;

    private CxeMessage m_cxeMessage;
    private GlobalSightCategory m_logger;
    private String m_localizationProfile = null;
    private String m_locale = null;
    private String m_languageCode = null;
    private String m_countryCode = null;
    private String m_preExtractEvent=null;
    private String m_preMergeEvent=null;
    private String m_postMergeEvent=null;
    private String m_charset= null;
    private String m_tmpFileName = null;
    private String m_urlhead = null;
    private String m_formatType = null;
    private String m_fileSystemProfile = null;
    private String m_baseHref = "";
    private String m_importRequestType = null;

    /**
     * Constructor.
     */
    public Importer(CxeMessage p_cxeMessage, GlobalSightCategory p_logger) throws Exception
    {
        m_cxeMessage = p_cxeMessage;
        m_logger = p_logger;
    }

    /**
     * Imports a file from teamsite. The file is determined by parameters associated
     * with the CxeMessage this Importer uses.
     *
     * @exception Exception
     */
    public CxeMessage performImport() throws Exception
    {
        HashMap params = m_cxeMessage.getParameters();
        String servername = (String)params.get("TeamSiteServer");       // teamsite server name
        String storename = (String)params.get("TeamSiteStore");       // teamsite store name
        String taskId = (String)params.get("TeamSiteTaskId");       // teamsite task Id
        String overwriteSource = (String)params.get("TeamSiteOverwriteSource");       // to overwrite or not to
        String callbackImmediately = (String)params.get("TeamSiteCallbackImmediately");  // to callback immediately
        m_logger.info( "Importer::performImport TeamSite Server is " + servername);
        m_logger.info( "Importer::performImport TeamSite Store is " + storename);
        m_logger.info( "Importer::performImport TeamSite taskId is " + taskId);
        m_logger.info( "Importer::performImport TeamSite overwriteSource is " + overwriteSource);
        m_logger.info( "Importer::performImport TeamSite callbackImmediately is " + callbackImmediately);
        setServerDetails(servername, storename);
        String filename = (String)params.get("FileName");               // the converted file path for the source file in the GlobalSight directory
        String sourceFilename = (String)params.get("SourceFileName");   // the original file path of the source file in the teamsite directory
        Integer filesize = (Integer) params.get("FileSize");                 // size of file
        m_charset = (String)params.get("Charset");                 // the character set associated to this file
        m_locale = (String)params.get("Locale");                   // the locale for this file
        String batchId = (String)params.get("BatchId");
        Integer pageCount = (Integer)params.get("PageCount");                 // the number of files submitted in this batch
        Integer pageNumber = (Integer)params.get("PageNum");                  // the number of this file within  the batch
        Integer docPageCount = (Integer)params.get("DocPageCount");                 // the number of files submitted in this batch
        Integer docPageNumber = (Integer)params.get("DocPageNum");                  // the number of this file within  the batch
        m_localizationProfile = (String)params.get("LocalizationProfile");
        m_fileSystemProfile = (String)params.get("DataSourceProfile");
        String jobName = (String)params.get("JobName");
        String userName = (String)params.get("UserName");
        m_importRequestType = (String) params.get(CxeProxy.IMPORT_TYPE);

        boolean overrideAsUnextracted = ((Boolean)params.get(
                "OverrideFileProfileAsUnextracted")).booleanValue();
        m_preExtractEvent = CxeMessageType.getCxeMessageType(CxeMessageType.HTML_IMPORTED_EVENT).getName();
        m_preMergeEvent= CxeMessageType.getCxeMessageType(CxeMessageType.HTML_LOCALIZED_EVENT).getName();
        m_postMergeEvent = CxeMessageType.getCxeMessageType(CxeMessageType.TEAMSITE_EXPORT_EVENT).getName();
        m_formatType = DiplomatAPI.FORMAT_HTML;
        String eventFlowXml = null;
        m_logger.debug( "Importer::performImport setting internal values");

        setInternalVals(overrideAsUnextracted);
        int index = determineUrlAndTmpFileName(filename,sourceFilename);
        eventFlowXml = makeEventFlowXml(
            filename, filesize, batchId, pageCount, pageNumber, docPageCount, docPageNumber,
            sourceFilename, userName, jobName, servername, storename, taskId, overwriteSource, callbackImmediately);

        m_logger.info("Importer::Reading file: " + sourceFilename + "; expected size " + filesize);
        TeamSiteServer tss = ServerProxy.getTeamSiteServerPersistenceManager()
        .getTeamSiteServerByName(servername);
        
        String session = TeamSiteExchange.getTeamsiteSession(tss);
        if (session == null)
        {
           session = URLEncoder.encode("teamsite_session");
        }
        MessageData md = httpFileImport(filename.substring(index+6), filesize.intValue(),
                                        taskId, overwriteSource, callbackImmediately, session);
        m_logger.info("Read file: " + sourceFilename + "; size " + md.getSize());
        Logger.writeDebugFile("tssa_content.txt",md);

        // create the CxeMessage to return
        CxeMessageType type = CxeMessageType.getCxeMessageType(m_preExtractEvent);
        CxeMessage outputMsg = new CxeMessage(type);
        outputMsg.setEventFlowXml(eventFlowXml);
        outputMsg.setParameters(new HashMap());
        outputMsg.setMessageData(md);
        return outputMsg;
    }

    public void setServerDetails(String p_serverName, String p_storeName)
    {
        try
        {
            m_logger.info( "Importer::server name is " + p_serverName);
            m_logger.info( "Importer::store name is " + p_storeName);
            TeamSiteServer ts = ServerProxy.getTeamSiteServerPersistenceManager()
                                                 .getTeamSiteServerByName(p_serverName);

            m_logger.info( "Importer::TeamSite Server is " + ts);
            TSserverName = p_serverName;
            TS_DEFAULT = p_storeName;
            TSWebserverPort = (new Integer(ts.getImportPort())).toString();
            TSserverPort = (new Integer(ts.getExportPort())).toString();
            TSMountDir = ts.getMount();
            TSServerType = ts.getOS();
            TSReImport = (new Boolean(ts.getLocaleSpecificReimportSetting())).toString();
        }
        catch(Exception e)
        {
            m_logger.error( "TeamSite Importer::Exception is " + e);
        }
    }

    /**
     * Invokes a URL to communicate with TeamSite and downloads the
     * selected file from TeamSite.
     *
     * @param urlhead
     * @param filename
     * @param filesize
     * @param content
     * @return
     * @exception Exception
     */
    private MessageData httpFileImport(String filename, int filesize,
                                       String p_taskId, String p_overwriteSource,
                                       String p_callbackImmediately, String session)
    throws Exception
    {
        FileMessageData fmd = MessageDataFactory.createFileMessageData();
        URL url;
        URLConnection connection = null ;
        InputStream is = null;
        InputStreamReader isr = null;
        String urltmp = null;

        if (TSServerType.equalsIgnoreCase("nt"))
        {
            urltmp = m_urlhead+TeamSiteExchange.TS_IMPORT_NT;
        }
        else if (TSServerType.equalsIgnoreCase("unix"))
        {
            urltmp = m_urlhead+TeamSiteExchange.TS_IMPORT_UNIX;
        }

        urltmp += "?filename=" + TSMountDir + "/" + TS_DEFAULT
            + "/" + URLEncoder.encode(filename)
            + "&" + "taskId=" + p_taskId
            + "&" + "overwriteSource=" + p_overwriteSource
            + "&" + "callbackImmediately=" + p_callbackImmediately
            + "&isimport=1&session=" + session;
        m_logger.debug( "urltmp is " + urltmp);

        url = new URL(urltmp);
        connection = url.openConnection();
        connection.connect();
        is = connection.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = new BufferedOutputStream(fmd.getOutputStream());
        byte[] buf = new byte[filesize];
        int n = 0;
        int ntot = 0;
        //read only filesize bytes since there may be more
        while (n != -1) {
          ntot += n;
          if (ntot == filesize)
             break;
          n = bis.read(buf, ntot, filesize - ntot);
        }
        is.close();
        bos.write(buf,0,filesize);
        bos.close();
        return fmd;
    }

    //private members
    private String makeEventFlowXml(String p_convertedFileName,
                                    Integer p_fileSize, String batchId,
                                    Integer pageCount, Integer pageNumber,
                                    Integer docPageCount, Integer docPageNumber,
                                    String sourceFilename,
                                    String userName, String jobName,
                                    String p_serverName, String p_storeName,
                                    String p_taskId,
                                    String p_overwriteSource,
                                    String p_callbackImmediately
                                    )
    {
        String eventFlowXml = null;
        StringBuffer b = new StringBuffer(XmlUtil.formattedEventFlowXmlDtd());
        b.append("<eventFlowXml>\n");
        b.append("<preMergeEvent>");
        b.append(m_preMergeEvent);
        b.append("</preMergeEvent>\n");
        b.append("<postMergeEvent>");
        b.append(m_postMergeEvent);
        b.append("</postMergeEvent>\n");
        b.append("<ServerName>");
        b.append(p_serverName);
        b.append("</ServerName>\n");
        b.append("<StoreName>");
        b.append(p_storeName);
        b.append("</StoreName>\n");
        b.append("<taskId>");
        b.append(p_taskId);
        b.append("</taskId>\n");
        b.append("<overwriteSource>");
        b.append(p_overwriteSource);
        b.append("</overwriteSource>\n");
        b.append("<callbackImmediately>");
        b.append(p_callbackImmediately);
        b.append("</callbackImmediately>\n");
        b.append("<batchInfo l10nProfileId=\"");
        b.append(m_localizationProfile);
        b.append("\" processingMode=\"automatic\">\n<batchId>");
        b.append(batchId);
        b.append("</batchId>\n");
        b.append("<pageCount>");
        b.append(pageCount.toString());
        b.append("</pageCount>\n");
        b.append("<pageNumber>");
        b.append(pageNumber.toString());
        b.append("</pageNumber>\n");
        b.append("<docPageCount>");
        b.append(docPageCount);
        b.append("</docPageCount>\n");
        b.append("<docPageNumber>");
        b.append(docPageNumber);
        b.append("</docPageNumber>\n");
        b.append("<jobName>");
        b.append(jobName);
        b.append("</jobName>\n");
        b.append("<baseHref>");
        b.append(m_baseHref);
        b.append("</baseHref>\n");
        b.append("<displayName>");
        b.append(m_tmpFileName);
        b.append("</displayName></batchInfo>\n");
        b.append("<source name=\"TeamsiteSourceAdapter\" dataSourceType=\"teamsite\" dataSourceId=\"");
        b.append(m_fileSystemProfile);
        b.append("\" formatType=\"");
        b.append(m_formatType);
        b.append("\" pageIsCxePreviewable=\"true\" ");
        b.append("importRequestType=\"").append(m_importRequestType);
        b.append("\">\n<locale>");
        b.append(m_locale);
        b.append("</locale>\n");
        b.append("<charset>");
        b.append(m_charset);
        b.append("</charset>\n");
        b.append("<ConvertedFileName>");
        b.append(p_convertedFileName);
        b.append("</ConvertedFileName>\n");
        b.append("<FileSize>");
        b.append(p_fileSize);
        b.append("</FileSize>\n");
        b.append("<da name=\"Filename\"><dv>");
        b.append(m_tmpFileName);
        b.append("</dv></da>\n");
        b.append("</source>\n");

        b.append("<target name=\"TeamsiteTargetAdapter\">\n");
        b.append("<locale>unknown</locale>\n");
        b.append("<charset>unknown</charset>\n");
        b.append("<da name=\"Filename\"><dv>");
        b.append(m_tmpFileName);
        b.append("</dv></da>\n</target>\n");
        b.append("<category name=\"TeamSite\">\n<da name=\"JobName\"><dv>");
        b.append(jobName);
        // the original path of the source file in the TS file system
        b.append("</dv></da>\n<da name=\"SourceFileName\"><dv>");
        b.append(sourceFilename);
        b.append("</dv></da>\n<da name=\"UserName\"><dv>");
        b.append(userName);
        b.append("</dv></da></category></eventFlowXml>\n");
        eventFlowXml = b.toString();
        Logger.writeDebugFile("tssa_ef.xml", eventFlowXml);
        return eventFlowXml;
    }

    /**
     * Queries known format info and file profile info to set
     * some internal values
     *
     * @exception Exception
     */
    private void setInternalVals(boolean p_overrideAsUnextracted)
        throws Exception
    {
        Connection connection =null;
        Statement query = null;
        String sql = null;
        ResultSet results = null;

        //now try to find the file profile type
        try
        {
            connection =  ConnectionPool.getConnection();
            m_logger.debug("Getting file system profile info from the database:");
            sql = new String ("SELECT KNOWN_FORMAT_TYPE.FORMAT_TYPE, KNOWN_FORMAT_TYPE.PRE_EXTRACT_EVENT, KNOWN_FORMAT_TYPE.PRE_MERGE_EVENT" +
                              " FROM KNOWN_FORMAT_TYPE, FILE_PROFILE" +
                              " WHERE FILE_PROFILE.ID=" + m_fileSystemProfile +
                              " AND KNOWN_FORMAT_TYPE.ID=FILE_PROFILE.KNOWN_FORMAT_TYPE_ID");
            query = connection.createStatement();
            results = query.executeQuery(sql);
            if (results.next())
            {
                m_formatType = results.getString(1);
                m_preExtractEvent = p_overrideAsUnextracted ?
                    CxeMessageType.getCxeMessageType(
                        CxeMessageType.UNEXTRACTED_IMPORTED_EVENT).getName() :
                    results.getString(2);

                m_preMergeEvent = p_overrideAsUnextracted ?
                    CxeMessageType.getCxeMessageType(
                        CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT).getName() :
                    results.getString(3);
            }
            else
            {
                throw new Exception("No known format type association for the file profile. Using defaults.");
            }
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);


            // recover localization profile id, codeset, and locale from database
            String languageCode = null;
            String countryCode = null;
            sql = new String ("SELECT FILE_PROFILE.L10N_PROFILE_ID, FILE_PROFILE.CODE_SET," +
                              " L10N_PROFILE.SOURCE_LOCALE_ID, LOCALE.ISO_LANG_CODE, LOCALE.ISO_COUNTRY_CODE" +
                              " FROM FILE_PROFILE, L10N_PROFILE, LOCALE" +
                              " WHERE FILE_PROFILE.ID=" + m_fileSystemProfile +
                              " AND L10N_PROFILE.ID=FILE_PROFILE.L10N_PROFILE_ID" +
                              " AND LOCALE.ID=L10N_PROFILE.SOURCE_LOCALE_ID");
            query = connection.createStatement();
            results = query.executeQuery(sql);
            if (results.next())
            {
                m_localizationProfile = results.getString(1);
                m_charset = results.getString(2);
                m_languageCode = results.getString(4);
                m_countryCode = results.getString(5);
            }
            else
            {
                throw new Exception("Localization profile id, codeset, or locale not in database");
            }

            m_locale = m_languageCode + "_" + m_countryCode;
        }
        finally
        {
            ConnectionPool.silentClose(results);
            ConnectionPool.silentClose(query);
            ConnectionPool.silentReturnConnection(connection);
        }
    }

    private int determineUrlAndTmpFileName(String p_filename, String p_sourceFilename) throws Exception
    {
        int index = p_filename.indexOf("TSLink");
        if (index < 0)
            throw new Exception("The string \"TSLink\" not in file path name. It must be there.");
        if (TSserverPort.equals(""))
            m_urlhead = "http://"+TSserverName;
        else
            m_urlhead =  "http://"+TSserverName+":"+TSserverPort;

        if (TSReImport.equals("true"))
        {
            // The following line is a quick fix for patch I Bug 6733
            m_tmpFileName = p_filename.substring(0, p_filename.lastIndexOf("/") + 1) +
                            "teamsite_reimport_" +
                            m_localizationProfile + "_" +
                            p_filename.substring(p_filename.lastIndexOf("/") + 1 );
        }
        else
        {
            m_tmpFileName = p_filename;
        }
        m_logger.debug(" TSImportFileOperation MODIFIED filename:" + m_tmpFileName);
        m_logger.debug(" TSImportFileOperation TS_DEFAULT:" + TS_DEFAULT);
        m_baseHref = "http://" + TSserverName + ":" + TSWebserverPort  +TS_BASEHREF_MOUNT+
                     p_sourceFilename.substring(p_sourceFilename.indexOf(TS_DEFAULT));
        m_logger.debug(" TSImportFileOperation baseHref:" + m_baseHref);
        return index;
    }
}

