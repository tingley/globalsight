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
package com.globalsight.everest.servlet;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;

/**
 * <p>This servlet provides an API to build a globalization profile. The
 * globalization profile is a runtime object that contains one localization
 * profile and more than one file profile. The servlet provides command to get
 * list of localization profiles, list of file profiles, and creating a runtime
 * globalization profile. All objects are represented in XML.
 * </p>
 * <p>This generic servlet returns the results as an XML document to the
 * response of both <CODE>POST</CODE> and <CODE>GET</CODE> HTTP request. This
 * generic servlet is not a defined as an abstract class because it will allow
 * any application to use it as is, if they don't want any additional processing.
 * </p>
 * <p>If an application needs any additional processing, subclass this class and
 * overload {@link #doGet(HttpServletRequest, HttpServletResponse)} and
 * {@link #doPost(HttpServletRequest, HttpServletResponse)} methods. To
 * retrieve the XML document after the request is processed, just call
 * {@link #processRequest(HttpServletRequest, HttpServletResponse)} to get the
 * XML document as string.
 * </p>
 */
public class GenericGlobalizationInfoServlet 
    extends HttpServlet 
{
    /**
     * <p>Named constants
     * </p>
     */
    private static final String CMD = "cmd";
    private static final String GETLOCALIZATIONPROFILES = "getLocalizationProfiles";
    private static final String GETFILEPROFILES = "getFileProfiles";
    private static final String CREATEGLOBALIZATIONPROFILE = "createGlobalizationProfile";
    private static final String L10NPROFILEID = "l10nprofileID";
    private static final String FILEPROFILEID = "fileprofileID";
    
    /**
     * <p>Global Objects
     * </p>
     * <li>c_logger - Logger
     * <li>s_dbf - XML document builder factory
     * <li>s_db - XML document builder
     * <li>s_domi - XML runtime DOM implementor
     */
    private static final Logger c_logger =
        Logger
        .getLogger(CapExportServlet.class.getName());
    
    private static DocumentBuilderFactory s_dbf;
    private static DocumentBuilder s_db;
    private static DOMImplementation s_domi;
   
    /**
     * <p>Initializes the servlet.</p>
     * @param config config
     * @exception ServletException Defines a general exception a servlet can
     * throw when it encounters difficulty.
     */
    public void init(ServletConfig config)
        throws ServletException 
    {
        try 
        {
            s_dbf = DocumentBuilderFactory.newInstance();
            s_db = s_dbf.newDocumentBuilder();
            s_domi = s_db.getDOMImplementation();
        }
        catch (ParserConfigurationException pce) 
        {
            throw new ServletException(pce.getMessage());
        }
        super.init(config);
    }
    
    /**
     * <p>Destroys the servlet.</p>
     */
    public void destroy() 
    {
        super.destroy();
    }
    
    /**
     * <p>Process HTTP requests. Processes the "GET" method.
     * </p>
     * @see GenericGlobalizationInfoServlet#doPost(HttpServletRequest, HttpServletResponse).
     * @param p_request The HttpServletRequest.
     * @param p_response The HttpServletResponse.
     * @exception IOException Signals that an I/O related error has
     * occured.
     * @exception ServletException Defines a general exception a servlet can
     * throw when it encounters difficulty.
     */
    protected void doGet(HttpServletRequest p_request, HttpServletResponse p_response)
        throws ServletException, IOException 
    {
        doPost(p_request, p_response);
    }
    
    /**
     * <p>Process HTTP requests. Processes the "POST" method.
     * </p>
     * <p>This is the default method to process "POST" actions. It calls
     * <code>processRequest()</code> method to create an XML document which
     * is then returned in the resposne stream without any additional processing.
     * </p>
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse).
     * @see #processRequest(HttpServletRequest, HttpServletResponse).
     * @param p_request The HttpServletRequest.
     * @param p_response The HttpServletResponse.
     * @exception IOException Signals that an I/O related error has
     * occured.
     * @exception ServletException Defines a general exception a servlet can
     * throw when it encounters difficulty.
     */
    protected void doPost(HttpServletRequest p_request, HttpServletResponse p_response)
        throws ServletException, IOException 
    {
        String xmlDoc = processRequest(p_request, p_response);
        
        if (xmlDoc.startsWith("<?xml"))
            p_response.setContentType("text/xml");
        else
            p_response.setContentType("text/html");
        
        PrintWriter out = p_response.getWriter();
        
        out.println(xmlDoc);
        out.close();
    }
    
    /**
     * <p>Returns a short description of the servlet.
     * </p>
     * @return Short description of the servlet
     */
    public String getServletInfo() 
    {
        return "Generic Globalization Information Servlet";
    }
    
    /**
     * <p>Default processing of HTTP request for both HTTP <code>GET</code>
     * and <code>POST</code> methods.
     * </p>
     * <p>This method will execute appropriate method based on the "cmd" parameter
     * defined in the HTTP request. It creates an XML string which is returned
     * to the invoker and it can dispense it appropriately. The command
     * and it's action is defined below
     * </p>
     * <p><table border="1" cellpadding="4">
     * <tr bgcolor="#CCCCFF">
     * <td>cmd=</td>
     * <td>Result XML</td>
     * <tr>
     * <td>getLocalizationProfiles</td>
     * <td>XML document containing list of localization profiles defined in
     * GlobalSight.
     * </td>
     * </tr>
     * <tr>
     * <td>getFileProfiles</td>
     * <td>XML document containing list of file profiles defined in GlobalSight.
     * </td>
     * </tr>
     * <tr>
     * <td>createGlobalizationProfile</td>
     * <td>XML document containing globalization profile.
     * </td>
     * </tr>
     * </table>
     * </p>
     * @param p_request The HttpServletRequest.
     * @param p_response The HttpServletResponse.
     * @exception java.io.IOException Signals that an I/O related error has
     * occured.
     * @exception ServletException Defines a general exception a servlet can
     * throw when it encounters difficulty.
     * @return XML document
     */
    protected String processRequest(HttpServletRequest p_request, 
                                    HttpServletResponse p_response)
        throws ServletException, IOException 
    {
        String cmd = p_request.getParameter(CMD);
        String responseXML;
        
        if (cmd.equalsIgnoreCase(GETLOCALIZATIONPROFILES))
            responseXML = getLocalizationProfilesXML();
        else if (cmd.equalsIgnoreCase(GETFILEPROFILES))
            responseXML = getFileProfilesXML();
        else if (cmd.equalsIgnoreCase(CREATEGLOBALIZATIONPROFILE))
            responseXML = createGlobalizationProfile(p_request);
        else
        {
            StringWriter sw = new StringWriter();
    
            createExceptionHTML(new Exception("Invalid Command - " + cmd), sw);
            responseXML = sw.toString();
        }
        
        return responseXML;
    }
    
    /**
     * <p>Return an XML document containing list of localization profiles
     * defined in GlobalSight. The DTD used for this document is
     * <CODE>gsLocalizationProfileList.dtd</CODE>.
     * </p>
     * @return XML document containing localization profiles defined in GlobalSight.
     */
    protected String getLocalizationProfilesXML() 
    {
        StringWriter sw = new StringWriter();
        
        try 
        {
            DocumentType dt = s_domi.createDocumentType("gsLocalizationProfileList", 
                                null, 
                                "/globalsight/includes/dtds/gsLocalizationProfileList.dtd");
            Document doc = s_domi.createDocument(null, "gsLocalizationProfileList", dt);
            Element root = doc.getDocumentElement();

            //  Get collection of localization profiles defined in system
            //
            Collection l10nProfiles = ServerProxy
                                      .getProjectHandler()
                                      .getAllL10nProfiles();
            
            for (Iterator iter = l10nProfiles.iterator(); iter.hasNext();)
            {
                BasicL10nProfile l10nprofile = (BasicL10nProfile)iter.next();
                
                Element profile = doc.createElement("profile");
                addElement(doc, profile, "id", String.valueOf(l10nprofile.getId()));
                addElement(doc, profile, "name", l10nprofile.getName());
                addElement(doc, profile, "description", l10nprofile.getDescription());
                addElement(doc, profile, "sourceLocale", l10nprofile.getSourceLocale().toString());
                
                GlobalSightLocale[] targetLocales = l10nprofile.getTargetLocales();
                for (int i = 0; i < targetLocales.length; i++)
                {
                    addElement(doc, profile, "targetLocale", targetLocales[i].toString());
                }
                root.appendChild(profile);
            }

            // Serialize it
            //
            OutputFormat of = new OutputFormat("XML", "UTF-8", true);
            XMLSerializer xs = new XMLSerializer(sw, of);
            xs.serialize(doc);
        }
        catch (GeneralException ge)
        {     
            createExceptionHTML(ge, sw);
        }
        catch (NamingException ne)
        {
            createExceptionHTML(ne, sw);
        }
        catch (IOException ioe) 
        {
            createExceptionHTML(ioe, sw);
        }

        return sw.toString();
    }
    
    /**
     * <p>Return an XML document containing list of file profiles defined in
     * GlobalSight. The DTD used for this document is
     * <CODE>gsFileProfileList.dtd</CODE>.
     * </p>
     * @return XML document containing file profiles defined in GlobalSight.
     */
    protected String getFileProfilesXML() 
    {
        StringWriter sw = new StringWriter();

        try 
        {
            DocumentType dt = s_domi.createDocumentType("gsFileProfileList", 
                                null, 
                                "/globalsight/includes/dtds/gsFileProfileList.dtd");
            Document doc = s_domi.createDocument(null, "gsFileProfileList", dt);
            Element root = doc.getDocumentElement();

            //  Create an hash table of known format types defined in system
            //
            Collection knownFormatTypes = ServerProxy
                                          .getFileProfilePersistenceManager()
                                          .getAllKnownFormatTypes();
            Hashtable knownFormatHash = new Hashtable();
            for (Iterator iter = knownFormatTypes.iterator(); iter.hasNext();)
            {
                KnownFormatType kft = (KnownFormatType)iter.next();
                knownFormatHash.put(new Long(kft.getId()), kft.getName());
            }
            
            //  Get collection of files profiles defined in system
            //
            Collection fileProfiles = ServerProxy
                                      .getFileProfilePersistenceManager()
                                      .getAllFileProfiles();

            for (Iterator iter = fileProfiles.iterator(); iter.hasNext();)
            {
                FileProfile fileprofile = (FileProfile)iter.next();
                
                Element profile = doc.createElement("profile");
                addElement(doc, profile, "id", String.valueOf(fileprofile.getId()));
                addElement(doc, profile, "name", fileprofile.getName());
                addElement(doc, profile, "description", fileprofile.getDescription());

                //  Lookup the file format type for this file profile from
                //  hash table of known format types
                //
                Long formatId = new Long(fileprofile.getKnownFormatTypeId());
                String formatType = (String)knownFormatHash.get(formatId);
                addElement(doc, profile, "format", formatType);
                
                //  Get file extensions defined for this file profile
                //
                Collection fileExtensions = ServerProxy
                                            .getFileProfilePersistenceManager()
                                            .getFileExtensionsByFileProfile(fileprofile);
                for (Iterator iter2 = fileExtensions.iterator(); iter2.hasNext();)
                {
                    FileExtension fe = (FileExtension)iter2.next();
                    addElement(doc, profile, "fileExtension", "." + fe.getName());
                }
                root.appendChild(profile);
            }
            
            // Serialize it
            //
            OutputFormat of = new OutputFormat("XML", "UTF-8", true);
            XMLSerializer xs = new XMLSerializer(sw, of);
            xs.serialize(doc);
        }
        catch (GeneralException ge)
        {
            createExceptionHTML(ge, sw);
        }
        catch (NamingException ne)
        {
            createExceptionHTML(ne, sw);
        }
        catch (IOException ioe) 
        {
            createExceptionHTML(ioe, sw);
        }

        
        return sw.toString();
    }
    
    /**
     * <p>Return an XML document containing globalization profile.
     * The globalization profile is a runtime object which is created
     * using one localization profile and one or more file profiles
     * defined in GlobalSight. The DTD used for this document is
     * <CODE>gsGlobalizationProfile.dtd</CODE>.
     * </p>
     * @param p_request The HttpServletRequest.
     * @return XML document containing globalization profile.
     */
    protected String createGlobalizationProfile(HttpServletRequest p_request) 
    {
        StringWriter sw = new StringWriter();
        
        try 
        {
            DocumentType dt = s_domi.createDocumentType("gsGlobalizationProfile", 
                                null, 
                                "/globalsight/includes/dtds/gsGlobalizationProfile.dtd");
            Document doc = s_domi.createDocument(null, "gsGlobalizationProfile", dt);
            Element root = doc.getDocumentElement();
            
            String l10nProfileID = p_request.getParameter(L10NPROFILEID);
            addElement(doc, root, "localizationProfileID", l10nProfileID);
            
            String[] fileProfileID = p_request.getParameterValues(FILEPROFILEID);
            for (int i = 0; i < fileProfileID.length; i++)
            {
                addElement(doc, root, "fileProfileID", fileProfileID[i]);
            }
            
            // Serialize it
            //
            OutputFormat of = new OutputFormat("XML", "UTF-8", true);
            XMLSerializer xs = new XMLSerializer(sw, of);
            xs.serialize(doc);
        }
        catch (IOException ioe) 
        {
            createExceptionHTML(ioe, sw);
        }
        
        return sw.toString();
    }
    
    /**
     * <p>Private methods
     * </p>
     */
    
    /**
     * <p>Create an HTML document with exception and stack trace
     * </p>
     */
    private void createExceptionHTML(Exception p_exception, StringWriter p_writer) 
    {
        PrintWriter pw = new PrintWriter(p_writer, true);
        
        pw.println("<html>");
        pw.println("<head>");
        pw.println("<title>Exception Report</title>");
        pw.println("</head>");
        pw.println("<h2>" + p_exception.getMessage() + "</h2><p>");
        pw.println("<body><code><pre>");
        p_exception.printStackTrace(pw);
        pw.println("</pre></code></body>");
        pw.println("</html>");
    }
    
    /**
     * <p>Add a new element to the document and add it the parent.
     * </p>
     */
    private Element addElement(Document doc, Element parent, String name, String value) 
    {
        Element item;
        item = doc.createElement(name);
        item.appendChild(doc.createTextNode(value));
        parent.appendChild(item);
        parent.appendChild(doc.createTextNode("\n"));
        
        //  Return newly created element in case it is a container of more
        //  elements.
        //
        return item;
    }
}
