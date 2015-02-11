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

import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.cms.teamsite.TeamSiteExchange;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.diplomat.util.ConfigParameters;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.diplomat.util.database.TableBranchLanguage;
import com.globalsight.diplomat.util.database.TableLangs;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.Base64;
import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Vector;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * The TSExportFileOperation object implements the file write for
 * the TeamsiteTargetAdapter.
 * It takes in an input event (TeamsiteMergedEvent) containing the fullpath of a file
 * to write and then writes the file to the file system
 */
public class Exporter
{
    public static final String TS_BASEHREF_MOUNT = "iw-mount";

    private TeamSiteServer m_TSServer = null;
    private BackingStore m_TSStore = null;
    private String m_TemplateList = "none";
    private boolean m_isDcr = false;
    private CxeMessage m_cxeMessage;
    private GlobalSightCategory m_logger;
    private String m_eventFlowXml;

    /**
     * Creates a teamsite exporter
     * 
     * @param p_cxeMessage
     *                 incoming cxe msg
     * @param p_logger logger to use
     * @exception Exception
     */
    public Exporter(CxeMessage p_cxeMessage, GlobalSightCategory p_logger) throws Exception
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        m_logger = p_logger;
        //parse the eventFlowXml using DOM
        StringReader sr = new StringReader(m_eventFlowXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
        parser.parse(is);
        Element elem = parser.getDocument().getDocumentElement();

        String baseHref = null;
        // get baseHref
        NodeList bl = elem.getElementsByTagName("baseHref");
        Element baseElement = (Element) bl.item(0);
        baseHref = baseElement.getFirstChild().getNodeValue();

        String hostName = baseHref.substring(baseHref.indexOf("://") + 3, baseHref.lastIndexOf(":"));
        String storeName = baseHref.substring(baseHref.indexOf(TS_BASEHREF_MOUNT) + TS_BASEHREF_MOUNT.length() + 1);
        storeName = storeName.substring(0, storeName.indexOf("/"));

        TeamSiteServer ts = (TeamSiteServer)ServerProxy.
            getTeamSiteServerPersistenceManager().getTeamSiteServerByName(hostName);

        m_TSServer = ts;
        Vector tsBackingStores = new Vector(ServerProxy
            .getTeamSiteServerPersistenceManager()
            .getBackingStoresByTeamSiteServer(ts));
        for(int i=0; i<tsBackingStores.size(); i++)
        {
            BackingStore bs = (BackingStore)tsBackingStores.elementAt(i);
            if(bs.getName().equals(storeName))
            {
                m_TSStore = bs;
            }
        }
    }

    /**
     * Process a file read request.
     *
     * @param input the GlobalSightInput event
     * @exception    BrokerException field access problem
     * @exception    AdapterException something went wrong
     */
    public CxeMessage exportFile() throws Exception
    {
        String sourceLocale = null;
        String targetLocale = null;
        String filename = null;
        String sourceFilename = null;
        String taskId = null;
        String overwriteSource = null;
        String callbackImmediately = null;
        String relPath = null;
        String newRelPath = null;
        String workAreaPath = null;
        String teamsiteMode = null;
        String sourceCharset = null;
        String targetCharset = null;
        String absDir = null;          
        String absTargetDir = null;          
        String tsURL = null;
        String tsHostName = null;
        String tsMountDir = null;
        String relDir = null;
        String relTargetDir = null;
        String tsProxyPort = null;
        String baseHref = null;
        String sourceHref = null;
        String targetHref = null;
        String capLoginUrl = null;
        String teamsiteBranches = "off";
        teamsiteBranches = "on";
        String prefixPath = "";
        String endPath = "";
        String sourceHrefStart = "";
        long exportedTime = 0;
        String cxeRequestType = "";
        //parse the eventFlowXml using DOM
        StringReader sr = new StringReader(m_eventFlowXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false); //don't validate
        parser.parse(is);
        Element elem = parser.getDocument().getDocumentElement();

        // get cxeRequestType
        NodeList crt = elem.getElementsByTagName("cxeRequestType");
        Element requestTypeElement = (Element) crt.item(0);
        cxeRequestType = requestTypeElement.getFirstChild().getNodeValue();

        // get baseHref
        NodeList bl = elem.getElementsByTagName("baseHref");
        Element baseElement = (Element) bl.item(0);
        baseHref = baseElement.getFirstChild().getNodeValue();

        // get taskId
        NodeList tid = elem.getElementsByTagName("taskId");
        if( tid != null && tid.getLength() != 0 ) 
        {
            Element taskElement = (Element) tid.item(0);
            taskId = taskElement.getFirstChild().getNodeValue();
        } 
        else
        {
            taskId = "null";
        }

        // get overwriteSource
        NodeList ows = elem.getElementsByTagName("overwriteSource");
        Element owsElement = (Element) ows.item(0);
        overwriteSource = owsElement.getFirstChild().getNodeValue();

        // get callbackImmediately
        NodeList cbi = elem.getElementsByTagName("callbackImmediately");
        Element cbiElement = (Element) cbi.item(0);
        callbackImmediately = cbiElement.getFirstChild().getNodeValue();

        //get source locale
        NodeList nl = elem.getElementsByTagName("source");
        Element sourceElement = (Element) nl.item(0);
        Element localeElement = (Element)sourceElement.getElementsByTagName("locale").item(0);
        sourceLocale = localeElement.getFirstChild().getNodeValue();
        Element charsetElement = (Element)sourceElement.getElementsByTagName("charset").item(0);
        sourceCharset = charsetElement.getFirstChild().getNodeValue();

        //get target locale
        nl = elem.getElementsByTagName("target");
        Element targetElement = (Element) nl.item(0);
        teamsiteMode = targetElement.getAttribute("databaseMode");
        m_logger.debug("The teamsite mode is " + teamsiteMode);
        localeElement = (Element)targetElement.getElementsByTagName("locale").item(0);
        targetLocale = localeElement.getFirstChild().getNodeValue();
        charsetElement = (Element)targetElement.getElementsByTagName("charset").item(0);
        targetCharset = charsetElement.getFirstChild().getNodeValue();

        //get filename
        NodeList attributeList = targetElement.getElementsByTagName("da");
        boolean notFound = true;
        for (int k=0; k<attributeList.getLength() && notFound;k++)
        {
            Element attrElement = (Element)attributeList.item(k);
            String name = attrElement.getAttribute("name");
            if (name != null && name.equals("Filename"))
            {
                NodeList values = attrElement.getElementsByTagName("dv");
                Element valElement = (Element) values.item(0);
                filename = valElement.getFirstChild().getNodeValue();
                notFound = false;
            }
        }

        // get needed TeamSite category parameters
        NodeList categorylist = elem.getElementsByTagName("category");
        for (int l=0; l<categorylist.getLength();l++)
        {
            Element categoryElement = (Element)categorylist.item(l);
            String name = categoryElement.getAttribute("name");
            if (name != null && name.equals("TeamSite"))
            {
                attributeList = categoryElement.getElementsByTagName("da");
                for (int m=0;m<attributeList.getLength();m++)
                {
                    Element attrElement = (Element)attributeList.item(m);
                    String sfname = attrElement.getAttribute("name");
                    if (sfname != null && sfname.equals("SourceFileName"))
                    {
                        NodeList values = attrElement.getElementsByTagName("dv");
                        Element valElement = (Element) values.item(0);
                        sourceFilename = valElement.getFirstChild().getNodeValue();
                    }
                }
            }
        }

        //parse the eventFlowXml to find out filename and locale to use to export
        m_logger.debug("sourceLocale = " + sourceLocale);
        m_logger.debug("targetLocale = " + targetLocale);
        m_logger.debug("filename = " + filename);
        m_logger.debug("sourceFilename = " +sourceFilename);

        sourceHref = baseHref.substring(0, baseHref.lastIndexOf('/'));
        m_logger.debug( "sourceHref= " + sourceHref);

        String fileName = baseHref.substring(baseHref.lastIndexOf('/') + 1); 

        // find source language
        String srcLang = sourceLocale.substring(0,sourceLocale.indexOf("_"));
        String srcCountry = sourceLocale.substring(sourceLocale.indexOf("_")+1);
        // find target language
        String langStr = targetLocale.substring(0,targetLocale.indexOf("_"));
        String countryStr = targetLocale.substring(targetLocale.indexOf("_")+1);
        TableLangs theTargetLanguage = TableLangs.findLanguageByLocale(langStr, countryStr);
        // find sourceBranch
        int startIndex = filename.indexOf("TSLink");
        String sourceBranch = filename.substring(startIndex+7);
        startIndex = sourceBranch.indexOf("WORKAREA");
        sourceBranch = sourceBranch.substring(0, startIndex-1);         // do not include ending "/"
        m_logger.debug("sourceBranch = " + sourceBranch+" TargetLanguage id "+theTargetLanguage.getID());

        int serverId = new Long(m_TSServer.getId()).intValue();
        int storeId = new Long(m_TSStore.getId()).intValue();
        TeamSiteServer tss =(TeamSiteServer)ServerProxy.getTeamSiteServerPersistenceManager()
                                 .readTeamSiteServer(serverId);

        tsHostName = tss.getName();
        // get branch language information
        TableBranchLanguage branchLanguage = TableBranchLanguage.
            findBranchLanguage(sourceBranch, theTargetLanguage.getID(),serverId,storeId);
        String DBtargetBranch = null;
        String DBsourceBranch = null;

        // find the "DIR" argument
        workAreaPath = findWorkAreaPath(sourceFilename);   // this is the dir argument to the .ctl file
        m_logger.debug("workAreaPath = " + workAreaPath);

        // find the "RELPATH"argument and the file name
        relPath = sourceFilename.substring(workAreaPath.length());
        absDir = sourceFilename.substring(0,sourceFilename.indexOf(relPath));
        SystemConfiguration config = SystemConfiguration.getInstance();
        tsMountDir = tss.getMount();
        relDir = absDir.substring(tsMountDir.length() + 1);
        tsProxyPort = (new Integer(tss.getProxyPort())).toString();
        String tsPort =(new Integer(tss.getExportPort())).toString(); 
        capLoginUrl = config.getStringParameter(TeamSiteExchange.CAP_LOGIN_URL);
        String session = TeamSiteExchange.getTeamsiteSession(tss);
        if (session == null)
        {
           session = URLEncoder.encode("teamsite_session");
        }
        if (m_TSServer.getOS().equalsIgnoreCase("nt"))
        {
            tsURL = TeamSiteExchange.TS_HTTP + m_TSServer.getName() + ":" + 
                tsPort + TeamSiteExchange.TS_GENERATE_NT + "?session=" + session;
        }
        else if (m_TSServer.getOS().equalsIgnoreCase("unix"))
        {
            tsURL = TeamSiteExchange.TS_HTTP + m_TSServer.getName() + ":" +
                tsPort + TeamSiteExchange.TS_GENERATE_UNIX + "?session=" + session;
        }

        // handle the multiple branch case
        String relSrc = relPath.substring(1);
        if (relSrc.indexOf('/') >= 0)
        {
            relSrc = relSrc.substring(0, relSrc.indexOf('/'));
        }
        String hSourceLocale = srcLang + "-" + srcCountry; 
        //if(relSrc.equals(sourceLocale) || relSrc.equals(hSourceLocale) || relSrc.equals(srcLang))
        //if(relSrc.equals(hSourceLocale))
        if (relSrc.equals(sourceLocale))
        {
            relPath = relPath.substring(relPath.indexOf('/', 1));
        }

        m_logger.debug("relPath = " + relPath);
        m_logger.debug("branchLanguage A = " + branchLanguage);


        if ( branchLanguage == null )  // preview or final mode no branches
        {
            teamsiteBranches = "off";
            relPath = "/" + targetLocale + relPath;
        }
        else
        {
            teamsiteBranches = "on";
            DBtargetBranch = branchLanguage.getTargetBranch();
            DBsourceBranch = branchLanguage.getSourceBranch();
            String toAppend = sourceBranch.substring(DBsourceBranch.length());
            // create the converted file path by replacing the old branch name with the new one in the original file path.
            startIndex = workAreaPath.indexOf(sourceBranch);
            prefixPath = workAreaPath.substring(0, startIndex);    // e.g. "/iw4/iwmnt/default/"
            endPath = workAreaPath.substring(prefixPath.length() + sourceBranch.length());
            workAreaPath = prefixPath + DBtargetBranch + toAppend + endPath;         // fixed "DIR" for the multiple branch case
            absTargetDir = workAreaPath;
            relTargetDir = absTargetDir.substring(tsMountDir.length() + 1);
            sourceHrefStart = sourceHref.substring(0, sourceHref.indexOf(sourceBranch));
            m_logger.debug( "sourceHrefStart= " + sourceHrefStart);
            m_logger.debug( "targetHref= " + targetHref);
            m_logger.debug("DBtargetBranch = " + DBtargetBranch);
            m_logger.debug("sourceBranch = " + sourceBranch);
            m_logger.debug("prefixPath = " + prefixPath);
            m_logger.debug("endPath = " + endPath);
            m_logger.debug("workAreaPath 2 = " + workAreaPath);
            m_logger.debug("teamsiteMode = " + teamsiteMode);
        }
        m_isDcr = false;

        //if the file is a PDF, then make sure we write it out with a .doc extension since
        //we currently do *not* round-trip to PDF
        if (relPath.toLowerCase().endsWith(".pdf"))
            relPath = relPath.substring(0,relPath.length() - 4) + ".doc";

        writeTargetFile(relPath, workAreaPath, teamsiteMode, 
                        sourceLocale, targetLocale, teamsiteBranches, 
                        taskId, overwriteSource, callbackImmediately, 
                        DBsourceBranch, DBtargetBranch, cxeRequestType, session);
        exportedTime = System.currentTimeMillis();

        if (( branchLanguage != null ))  // preview or final mode Branches defined.
        {
            String tmpPath = "";
            if (m_isDcr)
            {
                tmpPath = "";
            }
            else
            {
                if ( teamsiteBranches.equals("off") )
                {
                    tmpPath = relPath.substring(relPath.indexOf('/') + targetLocale.length() + 1, relPath.lastIndexOf('/')) ;
                }
                else
                {
                    if (relPath.lastIndexOf('/') > 0)
                    {
                        if (relPath.lastIndexOf('/')  > 0)
                        {
                            tmpPath = relPath.substring(0, relPath.lastIndexOf('/')) ;
                        }
                    }
                }
            }
            targetHref = sourceHrefStart  
                         + DBtargetBranch 
                         + endPath 
                         + tmpPath
                         + "/"
                         + TeamSiteExchange.PREVIEW_PREFIX
                         + "_"
                         + targetLocale 
                         + "_" + relPath.substring(relPath.lastIndexOf('/') + 1 ); 
            m_logger.debug("targetHref 2 = " + targetHref);
        }
        else // preview or final single branch -- prepend the target locale
        {
            if (m_isDcr)
            {
                targetHref = sourceHref + "/" + TeamSiteExchange.PREVIEW_PREFIX 
                            + "_" + targetLocale + "_" + fileName;
            }
            else
            {
                int sidx = sourceHref.indexOf(sourceLocale);
                int slen = sourceLocale.length();
                StringBuffer thref = new StringBuffer();
                if (sidx == -1)
                {
                    thref.append(sourceHref);
                    thref.append("/").append(targetLocale);
                    thref.append("/").append(TeamSiteExchange.PREVIEW_PREFIX).append("_").append(targetLocale).append("_").append(fileName);
                }
                else
                {
                    thref.append(sourceHref.substring(0, sidx));
                    thref.append(targetLocale);
                    thref.append(sourceHref.substring(sidx + slen));
                    thref.append("/").append(TeamSiteExchange.PREVIEW_PREFIX).append("_");
                    thref.append(targetLocale).append("_").append(fileName);
                }

                targetHref = thref.toString();
                if (targetHref.toLowerCase().endsWith(".pdf"))
                    targetHref = targetHref.substring(0,targetHref.length() - 4) + ".doc";
            }
            absTargetDir = absDir;
            relTargetDir = relDir;
        }
        m_logger.debug("relPath (w name) = " + relPath + " workAreaPath=" + workAreaPath );

        if ( teamsiteBranches.equals("off") )
        {
            newRelPath = relPath.substring(relPath.indexOf(targetLocale) + 
                                           targetLocale.length() , relPath.lastIndexOf('/'));
        }
        else
        {
            if (relPath.lastIndexOf('/') > 0)
            {
                if (relPath.lastIndexOf('/')  > 0)
                {
                    newRelPath = relPath.substring(0, relPath.lastIndexOf('/'));
                }
            }
        }
        String sourceTarget[] = { sourceLocale, targetLocale};
        m_logger.debug(" Teamsite targetCharset: " + targetCharset);
        String encoding[] = { sourceCharset, targetCharset};
        String args[] = {"filename", "relpath", "absdir", "reldir", "tsurl", "tshost"};
        String values[] = {filename.substring(filename.lastIndexOf('/')+1), newRelPath, absDir, relDir, tsURL, tsHostName};
        String targetValues[] = {filename.substring(filename.lastIndexOf('/')+1), newRelPath, absTargetDir, relTargetDir, tsURL, tsHostName};
        int i = 0;
        String dcrtpls[] = null;
        m_logger.debug("baseHref :" + baseHref);
        m_logger.debug("targetHref :" + targetHref);
        String hrefs[] = { baseHref, targetHref};
        if (m_TemplateList.equals("none"))
        {
            // do nothing...
        }
        else
        {
            StringTokenizer st = new StringTokenizer(m_TemplateList,"|");
            int numTokens=st.countTokens( );
            dcrtpls = new String[numTokens];
            while (st.hasMoreTokens())
            {
                dcrtpls[i++] = st.nextToken();
            }
        }

        //now deliver a status event
        CxeMessageType type = CxeMessageType.getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
        CxeMessage outgoingMsg = new CxeMessage(type);
        outgoingMsg.setEventFlowXml(m_eventFlowXml);
        HashMap params = m_cxeMessage.getParameters();
        params.put("EventFlowXml", m_eventFlowXml);
        params.put("Exception", null);
        params.put("ExportedTime", new Long(exportedTime)); //time the file was given back to Interwoven
        outgoingMsg.setParameters(params);
        BaseAdapter.preserveOriginalFileContent(m_cxeMessage.getMessageData(),
                                                outgoingMsg.getParameters());

        if (teamsiteMode.equals("final"))
        {
            params.put("PreviewUrlXml", "");
        }
        else
        {
            if (m_TemplateList.equals("none"))
            {
                params.put("PreviewUrlXml", generateNonDCRPreviewUrlXml(sourceTarget, encoding, hrefs));
            }
            else
            {
                params.put("PreviewUrlXml", generateDCRPreviewUrlXml(sourceTarget, encoding, dcrtpls, args, values, targetValues, capLoginUrl));
            }
        }

        return outgoingMsg;
    }

    // Generate the PreviewUrlXml
    private String generateDCRPreviewUrlXml(String sourceTarget[], 
                                            String encoding[], 
                                            String dcrtpls[], 
                                            String args[], 
                                            String values[],
                                            String targetValues[],
                                            String capLoginUrl  )
    {
        String PreviewUrlXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        PreviewUrlXml += "<previewUrlXml>\n";

        for (int i = 0; i < sourceTarget.length; i++)
        {
            PreviewUrlXml += "\t<locale name=\"" + sourceTarget[i] + "\">\n";
            if (i == 0)
            {
                PreviewUrlXml += "\t\t<sourceUrls encoding=\"" + encoding[i] + "\">\n";
            }
            else
            {
                PreviewUrlXml += "\t\t<targetUrls encoding=\"" + encoding[i] + "\">\n";
            }
            for (int j = 0; j < dcrtpls.length; j++)
            {
                PreviewUrlXml += "\t\t\t<url type=\"get\">\n";
                PreviewUrlXml += "\t\t\t\t<label>" + dcrtpls[j] + "</label>\n";
                PreviewUrlXml += "\t\t\t\t<href> " + capLoginUrl.substring(0, capLoginUrl.lastIndexOf('/'))+ "/TSPreviewServlet" + "</href>\n";
                PreviewUrlXml += "\t\t\t\t<arg>\n";
                PreviewUrlXml += "\t\t\t\t\t<parameter>template</parameter>\n";
                PreviewUrlXml += "\t\t\t\t\t<value substitution_source=\"none\">" + dcrtpls[j] + "</value>\n";
                PreviewUrlXml += "\t\t\t\t</arg>\n";
                PreviewUrlXml += "\t\t\t\t<arg>\n";
                PreviewUrlXml += "\t\t\t\t\t<parameter>locale</parameter>\n";
                PreviewUrlXml += "\t\t\t\t\t<value substitution_source=\"none\">" + sourceTarget[i] + "</value>\n";
                PreviewUrlXml += "\t\t\t\t</arg>\n";
                PreviewUrlXml += "\t\t\t\t<arg>\n";
                PreviewUrlXml += "\t\t\t\t\t<parameter>type</parameter>\n";
                if (i == 0)
                {
                    PreviewUrlXml += "\t\t\t\t\t<value substitution_source=\"none\">Source</value>\n";
                }
                else
                {
                    PreviewUrlXml += "\t\t\t\t\t<value substitution_source=\"none\">Target</value>\n";
                }
                PreviewUrlXml += "\t\t\t\t</arg>\n";
                if (i == 0)
                {
                    for (int k = 0; k < args.length; k++)
                    {
                        PreviewUrlXml += "\t\t\t\t<arg>\n";
                        PreviewUrlXml += "\t\t\t\t\t\t<parameter>" + args[k] + "</parameter>\n";
                        PreviewUrlXml += "\t\t\t\t\t\t<value substitution_source=\"none\">" + values[k] + "</value>\n";
                        PreviewUrlXml += "\t\t\t\t</arg>\n";
                    }
                }
                else
                {
                    for (int k = 0; k < args.length; k++)
                    {
                        PreviewUrlXml += "\t\t\t\t<arg>\n";
                        PreviewUrlXml += "\t\t\t\t\t\t<parameter>" + args[k] + "</parameter>\n";
                        PreviewUrlXml += "\t\t\t\t\t\t<value substitution_source=\"none\">" + targetValues[k] + "</value>\n";
                        PreviewUrlXml += "\t\t\t\t</arg>\n";
                    }
                }
                PreviewUrlXml += "\t\t\t</url>\n";
            }
            if (i == 0)
            {
                PreviewUrlXml += "\t\t</sourceUrls>\n";
            }
            else
            {
                PreviewUrlXml += "\t\t</targetUrls>\n";
            }
            PreviewUrlXml += "\t</locale>\n";

        }
        PreviewUrlXml += "</previewUrlXml>";
        return PreviewUrlXml;
    }

    private String generateNonDCRPreviewUrlXml(String sourceTarget[], 
                                               String encoding[],
                                               String hrefs[])
    {
        String PreviewUrlXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        PreviewUrlXml += "<previewUrlXml>\n";

        for (int i = 0; i < sourceTarget.length; i++)
        {
            PreviewUrlXml += "\t<locale name=\"" + sourceTarget[i] + "\">\n";
            if (i == 0)
            {
                PreviewUrlXml += "\t\t<sourceUrls encoding=\"" + encoding[i] + "\">\n";
            }
            else
            {
                PreviewUrlXml += "\t\t<targetUrls encoding=\"" + encoding[i] + "\">\n";
            }
            PreviewUrlXml += "\t\t\t<url type=\"get\">\n";
            PreviewUrlXml += "\t\t\t\t<label>" + sourceTarget[i] + "</label>\n";
            PreviewUrlXml += "\t\t\t\t<href>" + hrefs[i] + "</href>\n";
            PreviewUrlXml += "\t\t\t</url>\n";
            if (i == 0)
            {
                PreviewUrlXml += "\t\t</sourceUrls>\n";
            }
            else
            {
                PreviewUrlXml += "\t\t</targetUrls>\n";
            }
            PreviewUrlXml += "\t</locale>\n";
        }
        PreviewUrlXml += "</previewUrlXml>";
        return PreviewUrlXml;
    }

    /**
     *  given a file path, returns the file path up to the work area
     *
     * @param the absolute path to a file name
     *
     * @return the partial file name up to the work area including the trailing slash
     */
    private String findWorkAreaPath(String sourceFileName)
    {
        int workAreaIndex = sourceFileName.indexOf("WORKAREA/");
        String waPath = sourceFileName.substring(0, workAreaIndex + "WORKAREA/".length());
        String wbPath = sourceFileName.substring(workAreaIndex + "WORKAREA/".length());
        String workAreaName = wbPath.substring(0, wbPath.indexOf("/"));      // include the slash
        return waPath + workAreaName;
    }

    /**
     * Writes output file - writes the translated file back to the Teamsite target branch
     * It also writes the control file for the Crontab process on Unix
     *
     *  @param targetFilePath the path of the translated target file
     *
     * @exception
     *
     */
    private void writeTargetFile(String relPath, String dir, 
                                 String teamsiteMode, String sourceLocale, 
                                 String targetLocale, String teamsiteBranches, 
                                 String taskId, String overwriteSource,
                                 String callbackImmediately, String sourceBranch,
                                 String targetBranch, String cxeRequestType, String session)
    throws Exception
    {
        boolean rv = false;

        int lastIndex = relPath.lastIndexOf("/");
        String targetFileName = relPath.substring(lastIndex + 1);
        m_logger.debug("targetFileName " + targetFileName);

        // determine relPath and do not include leading or trailing "/"
        if (lastIndex >= 1)
            relPath = relPath.substring(1, lastIndex);
        else
            relPath = "";

        m_logger.debug("new relPath " + relPath);

        URL tsURL = null;
        if (m_TSServer.getOS().equalsIgnoreCase("nt"))
        {
            tsURL = new URL(TeamSiteExchange.TS_HTTP+m_TSServer.getName()+":"+
                            m_TSServer.getExportPort()+TeamSiteExchange.TS_IMPORT_NT);
        }
        else if (m_TSServer.getOS().equalsIgnoreCase("unix"))
        {
            tsURL = new URL(TeamSiteExchange.TS_HTTP+m_TSServer.getName()+":"+
                            m_TSServer.getExportPort()+TeamSiteExchange.TS_IMPORT_UNIX);
        }
        m_logger.info("Writing back to TeamSite:(" + dir + "," + relPath + "," + targetFileName);
        m_logger.debug("teamsite URL: " + tsURL.toString() );

        HttpURLConnection tsConnection = (HttpURLConnection)tsURL.openConnection();
        tsConnection.setRequestMethod("POST");
        tsConnection.setDoOutput(true);

        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(tsConnection.getOutputStream(), "ISO8859_1"));
        String fileContent = readContentAsString();
        //transmit all files as base64 encoded
        String base64EncodedContent = Base64.encodeToString(fileContent);
        out.print("datafile=" + URLEncoder.encode(base64EncodedContent) + "&");
        out.print("filetype=binary&");
        out.print("dir=" + URLEncoder.encode(dir) + "&");
        out.print("teamsiteMode=" + URLEncoder.encode(teamsiteMode) + "&");
        out.print("cxeRequestType=" + URLEncoder.encode(cxeRequestType) + "&");
        out.print("relpath=" + URLEncoder.encode(relPath) + "&");
        out.print("filename=" + URLEncoder.encode(targetFileName) + "&");
        out.print("session=" + session + "&");
        out.print("targetLocale=" + URLEncoder.encode(targetLocale) + "&");
        out.print("teamsiteBranches=" + URLEncoder.encode(teamsiteBranches) + "&");
        out.print("taskId=" + URLEncoder.encode(taskId) + "&");
        out.print("overwriteSource=" + URLEncoder.encode(overwriteSource) + "&");
        out.print("callbackImmediately=" + URLEncoder.encode(callbackImmediately) + "&");
        if(sourceBranch != null && targetBranch != null)
        {
            out.print("sourceBranch=" + URLEncoder.encode(sourceBranch) + "&");
            out.print("targetBranch=" + URLEncoder.encode(targetBranch));
            out.print("&");
        }
        out.print("sourceLocale=" + URLEncoder.encode(sourceLocale));
        out.print("\n\n");
        out.close();

        BufferedReader in = new BufferedReader (new InputStreamReader(
                                                                     tsConnection.getInputStream()));
        String inputLine = null;
        String lastLine = null;
        String templateLine = "none";
        StringTokenizer st;

        while ((inputLine = in.readLine()) != null)
        {
            m_logger.debug(inputLine);
            if (inputLine.indexOf("templates=") > -1)
            {
                templateLine = inputLine;
            }
            if (inputLine.indexOf("dcr=dcr") > -1)
            {
                m_logger.debug("TSExportFile::This is DCR");
                m_isDcr = true;
            }
            if (inputLine.indexOf("dcr=none") > -1)
            {
                m_logger.debug("TSExportFile::This is NOT DCR");
                m_isDcr = false;
            }
            lastLine = inputLine;
        }
        in.close();
        if (templateLine.equals("none"))
        {
            m_TemplateList = "none";
        }
        else
        {
            // Let's get the list of templates for presentation.
            st = new StringTokenizer(templateLine,"=");
            st.nextToken(); //skip "templates"
            m_TemplateList = st.nextToken();
            m_logger.debug( "TemplateList returned from Teamsite CGI: " + m_TemplateList);
        }

        //now check the LastLine to see what the returned status from the
        //Teamsite Integration CGI script was
        st = new StringTokenizer(lastLine,"=");
        st.nextToken(); //skip status
        String status = st.nextToken();
        if (status != null && status.startsWith("OK"))
        {
            m_logger.debug( "Status returned from Teamsite CGI: " + status);
            rv = true;
        }
        else
        {
            m_logger.error( "Status returned from Teamsite CGI: " + status);
            rv= false;
        }

        if (rv==false)
            throw new Exception ("Failed to post file to teamsite: "+ relPath);
    }


    /**
     * Reads the content and returns it as a String in the ISO-8859-1 encoding.
     * Deletes the original message data file
     * 
     * @return String
     */
    private String readContentAsString() throws Exception
    {
        FileMessageData fmd = (FileMessageData) m_cxeMessage.getMessageData();
        File f = fmd.getFile();
        int len = (int) f.length();
        StringBuffer content = new StringBuffer(len);
        char buf[] = new char[32768];
        InputStreamReader reader = new InputStreamReader(new FileInputStream(f));
        int n = -1;
        do
        {
            n = reader.read(buf,0,buf.length);
            if (n > 0)
                content.append(buf,0,n);
        } 
        while (n>0);
        reader.close();
        m_cxeMessage.setDeleteMessageData(true);

        return content.toString();
    }
}

