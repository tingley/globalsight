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
public class JobStateReporter
{
    public static final String TS_BASEHREF_MOUNT = "iw-mount";

    private TeamSiteServer m_TSServer = null;
    private BackingStore m_TSStore = null;
    private CxeMessage m_cxeMessage;
    private GlobalSightCategory m_logger;
    private String m_eventFlowXml;
    private String m_TeamSiteJobState;
    private String m_messageData;
    private String m_taskId;
    private String m_callbackImmediately;

    /**
     * Creates a teamsite exporter
     * 
     * @param p_cxeMessage
     *                 incoming cxe msg
     * @param p_logger logger to use
     * @exception Exception
     */
    public JobStateReporter(CxeMessage p_cxeMessage, GlobalSightCategory p_logger) throws Exception
    {
        m_cxeMessage = p_cxeMessage;
        m_eventFlowXml = p_cxeMessage.getEventFlowXml();
        HashMap params = m_cxeMessage.getParameters();
        m_TeamSiteJobState = (String)params.get("TeamSiteJobState"); // IMPORT_FAIL 
        if(m_TeamSiteJobState == null || m_TeamSiteJobState == "")
        {
            m_TeamSiteJobState ="No Status Info is available";
        }
        m_messageData = (String)params.get("TeamSiteMessage");  // Details about failure
        if(m_messageData == null || m_messageData == "")
        {
            m_messageData ="No Message data is available";
        }
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

        // get taskId
        NodeList tid = elem.getElementsByTagName("taskId");
        Element taskElement = (Element) tid.item(0);
        m_taskId = taskElement.getFirstChild().getNodeValue();

        // get callbackImmediately
        NodeList cbi = elem.getElementsByTagName("callbackImmediately");
        Element cbiElement = (Element) cbi.item(0);
        m_callbackImmediately = cbiElement.getFirstChild().getNodeValue();

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
     * send the Import success/fail status to TeamSite
     *
     * @param input the GlobalSightInput event
     * @exception    BrokerException field access problem
     * @exception    AdapterException something went wrong
     */
    public CxeMessage sendTeamSiteJobState() throws Exception
    {
        String taskId = null;
        String tsURL = null;
        writeStatus();
        //now deliver a status event
        CxeMessageType type = CxeMessageType.getCxeMessageType(CxeMessageType.TEAMSITE_IGNORE_EVENT);
        CxeMessage outgoingMsg = new CxeMessage(type);
        outgoingMsg.setEventFlowXml(m_eventFlowXml);
        HashMap params = m_cxeMessage.getParameters();
        params.put("EventFlowXml", m_eventFlowXml);
        params.put("Exception", null);
        outgoingMsg.setParameters(params);
        return outgoingMsg;
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
    private void writeStatus()
    throws Exception
    {
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
        m_logger.debug("teamsite URL: " + tsURL.toString() );

        HttpURLConnection tsConnection = (HttpURLConnection)tsURL.openConnection();
        tsConnection.setRequestMethod("POST");
        tsConnection.setDoOutput(true);

        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(tsConnection.getOutputStream()));
        if(m_taskId != null && !m_taskId.equals("null") )
        {
            out.print("taskId=" + m_taskId + "&");
        }
        if(m_callbackImmediately != null && !m_callbackImmediately.equals("null") )
        {
            out.print("callbackImmediately=" + URLEncoder.encode(m_callbackImmediately) + "&");
        }
        String session = TeamSiteExchange.getTeamsiteSession(m_TSServer);
        if (session == null)
        {
           session = URLEncoder.encode("teamsite_session");
        }
        out.print("teamsiteJobState=" + URLEncoder.encode(m_TeamSiteJobState) + "&");
        out.print("teamsiteMessage=" + URLEncoder.encode(m_messageData) + "&");
        out.print("session=" + session + "&");
        out.print("isimport=" + URLEncoder.encode("2") + "&");
        out.print("sourceLocale=" + URLEncoder.encode("en_US"));
        out.print("\n\n");
        out.close();
        BufferedReader in = new BufferedReader (new InputStreamReader(
                                                                     tsConnection.getInputStream()));
        String inputLine = null;
        String lastLine = null;
        StringTokenizer st;

        while ((inputLine = in.readLine()) != null)
        {
            lastLine = inputLine;
        }
        //now check the LastLine to see what the returned status from the
        //Teamsite Integration CGI script was
        st = new StringTokenizer(lastLine,"=");
        st.nextToken(); //skip status
        String status = st.nextToken();
        if (status != null && status.startsWith("OK"))
        {
            m_logger.debug( "Status returned from Teamsite CGI: " + status);
        }
        else
        {
            m_logger.debug( "Status returned from Teamsite CGI: " + status);
        }
        in.close();
    }
}

