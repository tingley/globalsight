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
package com.globalsight.cxe.adapter.serviceware;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.globalsight.ling.common.URLEncoder;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.XmlParser;

/**
 * Wraps calls to the ServiceWare HTTP API
 */
public class ServiceWareAPI
{
    static private Logger s_logger = Logger.getLogger(ServiceWareAPI.class);
    private static String s_apiUrl = null;
    private static String s_username = null;
    private static String s_password = null;
    private static String s_KArea = null;
    private static final String PROPERTY_FILE = "/properties/ServiceWareAdapter.properties";

    static
    {
        try
        {
            URL url = ServiceWareAPI.class.getResource(PROPERTY_FILE);
            Properties props = new Properties();
            File f = new File(url.toURI().getPath());
            props.load(new FileInputStream(f));
            s_apiUrl = props.getProperty("apiUrl");
            s_username = props.getProperty("username");
            s_password = props.getProperty("password");
            s_KArea = props.getProperty("KArea");
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Cannot use the ServiceWare API since properties are not set.",
                    e);
        }
    }

    /**
     * Tries to connect to serviceware, and returns the sessionID.
     * 
     * @return String
     * @exception Exception
     */
    public static String connect() throws Exception
    {
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=CreateSession&UserName=");
        url.append(s_username);
        url.append("&Password=");
        url.append(s_password);
        url.append("&KArea=");
        url.append(URLEncoder.encode(s_KArea));
        s_logger.debug("URL=" + url.toString());
        String xml = readXml(url.toString());
        s_logger.debug("XML is: " + xml);
        // now parse the XML to get the sessionId
        XmlParser xmlp = XmlParser.hire();
        Document d = xmlp.parseXml(xml);
        Element root = d.getRootElement();
        List nodes = root
                .selectNodes("/CreateSessionResponse/return/SessionID");
        Node node = (Node) nodes.get(0);
        String sessionId = node.getText();
        XmlParser.fire(xmlp);
        return sessionId;
    }

    /**
     * Closes the ServiceWare session
     * 
     * @param p_sessionId
     * @exception Exception
     */
    public static void disconnect(String p_sessionId) throws Exception
    {
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=CloseSession&SessionID=");
        url.append(p_sessionId);
        String xml = readXml(url.toString());
        s_logger.debug("Disconnect result is: " + xml);
    }

    /**
     * Invokes the URL to use the API and returns the resultant XML document
     * 
     * @param p_url
     *            ServiceWare call
     * @return
     * @exception Exception
     */
    private static String readXml(String p_url) throws Exception
    {
        URL u = new URL(p_url);
        StringBuffer xml = new StringBuffer();
        InputStream is = u.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s;
        while (null != (s = br.readLine()))
        {
            xml.append(s);
            xml.append("\r\n"); // replace the lost newline
        }
        return xml.toString();
    }

    /**
     * Gets the specified Knowledge Object
     * 
     * @param p_sessionId
     * @param p_koid
     * @return
     * @exception Exception
     */
    public static String getKnowledgeObjectXml(String p_sessionId, String p_koid)
            throws Exception
    {
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=GetKOs&SessionID=");
        url.append(p_sessionId);
        url.append("&KOIDs=");
        url.append(p_koid);
        s_logger.debug("URL=" + url.toString());
        String xml = readXml(url.toString());
        s_logger.debug("XML is: " + xml);
        return xml;
    }

    /**
     * Gets the specified Concept Object
     * 
     * @param p_sessionId
     * @param p_conceptId
     * @return
     * @exception Exception
     */
    public static String getConceptObjectXml(String p_sessionId,
            String p_conceptId) throws Exception
    {
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=GetConcepts&SessionID=");
        url.append(p_sessionId);
        url.append("&ConceptIDs=");
        url.append(p_conceptId);
        s_logger.debug("URL=" + url.toString());
        String xml = readXml(url.toString());
        s_logger.debug("XML is: " + xml);
        return xml;
    }

    /**
     * Returns a Hashtable containing all the Knowledge Object Ids and their
     * names
     * 
     * @param p_sessionId
     * @return
     * @exception Exception
     */
    public static Hashtable getKnowledgeObjects(String p_sessionId)
            throws Exception
    {
        Hashtable ht = new Hashtable();
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=GetKOs&SessionID=");
        url.append(p_sessionId);
        s_logger.debug("URL=" + url.toString());
        String xml = readXml(url.toString());
        s_logger.debug("XML is: " + xml);

        XmlParser xmlp = XmlParser.hire();
        Document d = xmlp.parseXml(xml);
        Element root = d.getRootElement();
        List idnodes = root
                .selectNodes("/GetKOsResponse/return/SessionKOs/KO/KOID");
        List namenodes = root
                .selectNodes("/GetKOsResponse/return/SessionKOs/KO/Name");
        for (int i = 0; i < idnodes.size(); i++)
        {
            Node id = (Node) idnodes.get(i);
            Node name = (Node) namenodes.get(i);
            s_logger.debug("Inserting: " + id.getText() + "," + name.getText());
            ht.put(id.getText(), name.getText());
        }
        XmlParser.fire(xmlp);
        return ht;
    }

    /**
     * Updates the given knowledge object with the new name and short
     * description
     * 
     * @param p_sessionId
     * @param p_KOID
     * @param p_newName
     * @param p_newShortDesc
     * @param p_targetLocale
     * @return
     * @exception Exception
     */
    public static String updateKnowledgeObject(String p_sessionId,
            String p_KOID, String p_newName, String p_newShortDesc,
            String p_targetLocale) throws Exception
    {
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=UpdateKO&SessionID=");
        url.append(p_sessionId);
        url.append("&KOID=");
        url.append(p_KOID);
        url.append("&KOName=");
        url.append(URLEncoder.encode(p_newName));
        url.append("&ShortDescription=");
        url.append(URLEncoder.encode(p_newShortDesc));
        url.append("&Language=");
        Locale targetLocale = GlobalSightLocale
                .makeLocaleFromString(p_targetLocale);
        String swLanguageName = getServiceWareLanguageName(targetLocale);
        url.append(URLEncoder.encode(swLanguageName));
        url.append("&UpdateOption=OverwriteAll");

        s_logger.debug("URL=" + url.toString());
        String xml = readXml(url.toString());
        s_logger.debug("XML is: " + xml);
        return xml;
    }

    /**
     * Updates the given concept object with the new name and short description
     * 
     * @param p_sessionId
     * @param p_KOID
     * @param p_newName
     * @param p_newShortDesc
     * @param p_targetLocale
     * @return
     * @exception Exception
     */
    public static String updateConceptObject(String p_sessionId,
            String p_conceptId, String p_newName, String p_newShortDesc,
            String p_targetLocale) throws Exception
    {
        StringBuffer url = new StringBuffer(s_apiUrl);
        url.append("?Action=UpdateConcept&SessionID=");
        url.append(p_sessionId);
        url.append("&ConceptID=");
        url.append(p_conceptId);
        url.append("&Name=");
        url.append(URLEncoder.encode(p_newName));
        url.append("&ShortDescription=");
        url.append(URLEncoder.encode(p_newShortDesc));
        url.append("&Language=");
        Locale targetLocale = GlobalSightLocale
                .makeLocaleFromString(p_targetLocale);
        String swLanguageName = getServiceWareLanguageName(targetLocale);
        url.append(URLEncoder.encode(swLanguageName));
        url.append("&UpdateOption=OverwriteAll");

        s_logger.debug("URL=" + url.toString());
        String xml = readXml(url.toString());
        s_logger.debug("XML is: " + xml);
        return xml;
    }

    public static String getServiceWareLanguageName(Locale p_locale)
    {
        // Japanese, Japan (ja_JP)
        StringBuffer sb = new StringBuffer(p_locale.getDisplayLanguage());
        sb.append(", ");
        sb.append(p_locale.getDisplayCountry());
        sb.append(" (");
        sb.append(p_locale.toString());
        sb.append(")");
        return sb.toString();
    }

    /**
     * Takes in a serviceware xml and gets all IDs, names, and short
     * descriptions from it for updating ServiceWare
     * 
     * @param p_sessionId
     * @param p_serviceWareXml
     * @return
     */
    public static void updateKnowledgeAndConceptObjects(String p_sessionId,
            String p_serviceWareXml, String p_targetLocale) throws Exception
    {
        XmlParser xmlp = XmlParser.hire();
        Document d = xmlp.parseXml(p_serviceWareXml);
        Element root = d.getRootElement();
        String koID = root.selectSingleNode("/serviceWareXml/knowledgeObj/@id")
                .getText();
        String koName = root.selectSingleNode(
                "/serviceWareXml/knowledgeObj/knowledgeObjName").getText();
        String koShortDesc = root.selectSingleNode(
                "/serviceWareXml/knowledgeObj/knowledgeObjShortDesc").getText();
        ServiceWareAPI.updateKnowledgeObject(p_sessionId, koID, koName,
                koShortDesc, p_targetLocale);

        // now update the concepts
        List conceptNodes = root
                .selectNodes("/serviceWareXml/knowledgeObj/concept");
        for (int i = 0; i < conceptNodes.size(); i++)
        {
            Node conceptNode = (Node) conceptNodes.get(i);
            String conceptId = conceptNode.selectSingleNode("@id").getText();
            String conceptName = conceptNode.selectSingleNode("conceptName")
                    .getText();
            String conceptShortDesc = conceptNode.selectSingleNode(
                    "conceptShortDesc").getText();
            ServiceWareAPI.updateConceptObject(p_sessionId, conceptId,
                    conceptName, conceptShortDesc, p_targetLocale);
        }
        XmlParser.fire(xmlp);
    }

    public static String makeIsoSafe(String s) throws Exception
    {
        return new String(s.getBytes("UTF8"), "ISO8859_1");
    }

    /**
     * Is the ServiceWareIntegration installed?
     * 
     * @return true | false
     */
    public static Boolean isInstalled()
    {
        return new Boolean(ServiceWareAdapter.isInstalled());
    }
}
