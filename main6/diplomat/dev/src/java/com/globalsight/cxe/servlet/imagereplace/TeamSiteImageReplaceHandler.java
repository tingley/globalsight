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
package com.globalsight.cxe.servlet.imagereplace;

/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
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

//  Java
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.PrintWriter;
import com.globalsight.ling.common.URLEncoder;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

//  Java Extensions
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

//  DOM,SAX
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//  Regular expression library
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

//  GlobalSight
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.cxe.adapter.teamsite.Exporter;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.cxe.util.cms.teamsite.TeamSiteExchange;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;
import com.globalsight.cxe.entity.cms.teamsite.store.BackingStore;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.database.TableBranchLanguage;
import com.globalsight.diplomat.util.database.TableLangs;
import com.globalsight.util.GeneralException;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * This is the servlet for exporting images back to Team Site. 
 *
 */
public class TeamSiteImageReplaceHandler extends HttpServlet
{
    private Logger theLogger = Logger.getLogger();
    private static String s_TSServerName;
    private static String s_TSServerPort;
    private static String s_TSServerType;
    private static final char UNIX_SEPARATOR = '/';
    private static final char WIN_SEPARATOR = '\\';

    public TeamSiteImageReplaceHandler()
    throws ServletException
    {
        try
        {
            theLogger.setLogname("CxeServlets");
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }
    
    /** 
     * This method processing the POST request made to upload image back to
     * Team Site.
     * <p>
     * All parameters are passed in as an URLEncoded.
     *
     * @param p_request Request object
     * @param p_response Response object
     * @throws ServletException Exception encountered during this servlet execution.
     * @throws IOException Exception encountered during this servlet execution.
     */     
    public void doPost(HttpServletRequest p_request, 
                       HttpServletResponse p_response)
        throws ServletException, IOException
    {
        try
        {
            //  Go ahead and process the request
            //
            processRequest(p_request, p_response);
            p_response.setStatus(HttpServletResponse.SC_OK);
        }
        catch (IOException ioe)
        {
            theLogger.println(Logger.ERROR, "Problem saving image file: " 
                              + p_request.getParameter(ExportConstants.IMAGE_FILENAME));
            p_response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new ServletException(ioe.getMessage());
        }
    }
    
  
    /** 
     * <p>The real method for processing the POST request for uploading image
     * back to TeamSite.
     * </p>
     * <p>The is a rather long method, which could potentially be broken down
     * to two methods, just for ease of readability only. The first part of
     * the method spends time reading the EventFlow XML to determine the source
     * and target information. Using the information, it determines the 
     * work area path and relative path of the image file. Using the work area
     * path, along with source and target information, it checks if there are
     * any TeamSite branches defined in system.
     * </p>
     * <p>Finally, it calls another method that posts an HTTP request to
     * TeamSite with file and it's contents.
     * </p>
     * <p>Majority of code has been bootlegged from TSImportFileOperation and
     * it is a cadidate for some refactoring.
     * </p>
     * @param p_request Http Request object
     * @param p_response Http Response object
     * @throws ServletException Exception encountered during this servlet execution.
     * @throws IOException Exception encountered during this servlet execution.
     */     
    private void processRequest(HttpServletRequest p_request,
                                HttpServletResponse p_response)
        throws ServletException, IOException 
    {
        String statusMessage = "";
        String eventFlowXml = p_request.getParameter(ExportConstants.EVENT_FLOW_XML);
        String localeSubDir = p_request.getParameter(ExportConstants.LOCALE_SUBDIR);
        String imageSrcLocation = p_request.getParameter(ExportConstants.IMAGE_FILENAME);
        String imageContents = p_request.getParameter(ExportConstants.IMAGE_DATA);


        
        theLogger.println(Logger.DEBUG_A, ExportConstants.LOCALE_SUBDIR+"="+localeSubDir);
        theLogger.println(Logger.DEBUG_A, ExportConstants.IMAGE_FILENAME+"="+imageSrcLocation);
        theLogger.println(Logger.DEBUG_A, ExportConstants.EVENT_FLOW_XML+"="+eventFlowXml);
        
        try 
        {
            String sourceLocale = null;
            String targetLocale = null;
            String filename = null;
            String sourceFilename = null;
            String workAreaPath = null;
            String relPath = null;
            String teamsiteMode = null;
            TeamSiteServer ts = null;
            String hostName = null;
            String storeName = null;
            String baseHref = null;
            BackingStore bStore = null;
            Vector tsBackingStores = null;

            //  Parse the eventFlowXml using DOM
            StringReader sr = new StringReader(eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);
            Element root = parser.getDocument().getDocumentElement();
            NodeList attributeList, categoryList, nl;
            
            // get baseHref
            NodeList bl = root.getElementsByTagName("baseHref");
            Element baseElement = (Element) bl.item(0);
            baseHref = baseElement.getFirstChild().getNodeValue();

            hostName = baseHref.substring(baseHref.indexOf("://") + 3, baseHref.lastIndexOf(":"));
            storeName = baseHref.substring(baseHref.indexOf(Exporter.TS_BASEHREF_MOUNT) + Exporter.TS_BASEHREF_MOUNT.length() + 1);
            storeName = storeName.substring(0, storeName.indexOf("/"));

            try
            {
                ts = (TeamSiteServer)ServerProxy.
                    getTeamSiteServerPersistenceManager().getTeamSiteServerByName(hostName);

                tsBackingStores = new Vector(ServerProxy
                    .getTeamSiteServerPersistenceManager()
                    .getBackingStoresByTeamSiteServer(ts));
                for(int i=0; i<tsBackingStores.size(); i++)
                {
                    BackingStore bs = (BackingStore)tsBackingStores.elementAt(i);
                    if(bs.getName().equals(storeName))
                    {
                        bStore = bs;
                    }
                }
            }
            catch(GeneralException ge)
            {
                throw new ServletException(ge);
            }
            s_TSServerName = ts.getName();
            s_TSServerPort = (new Integer( ts.getExportPort())).toString();
            s_TSServerType = ts.getOS();

            //  Get source locale
            nl = root.getElementsByTagName("source");
            Element sourceElement = (Element) nl.item(0);
            Element localeElement = (Element)sourceElement.getElementsByTagName("locale").item(0);
            sourceLocale = localeElement.getFirstChild().getNodeValue();
            theLogger.println(Logger.DEBUG_A,"sourceLocale = " + sourceLocale);
            
            //  Get target locale
            nl = root.getElementsByTagName("target");
            Element targetElement = (Element) nl.item(0);
            teamsiteMode = targetElement.getAttribute("databaseMode");
            theLogger.println(Logger.INFO,"The teamsite mode is " + teamsiteMode);
            localeElement = (Element)targetElement.getElementsByTagName("locale").item(0);
            targetLocale = localeElement.getFirstChild().getNodeValue();
            if (targetLocale.equalsIgnoreCase("unknown"))
                targetLocale = localeSubDir;
            theLogger.println(Logger.DEBUG_A,"targetLocale = " + targetLocale);
            
            //  Get filename
            attributeList = sourceElement.getElementsByTagName("da");
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
            theLogger.println(Logger.DEBUG_A,"filename = " + filename);
            
            //  Get needed TeamSite category parameters
            categoryList = root.getElementsByTagName("category");
            for (int l=0; l<categoryList.getLength();l++) 
            {
                Element categoryElement = (Element)categoryList.item(l);
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
            theLogger.println(Logger.DEBUG_A,"sourceFilename = " +sourceFilename);
            //
            //  Done Parsing with Event flow XML
            
            //  Find the "DIR" argument
            workAreaPath = findWorkAreaPath(sourceFilename);
            theLogger.println(Logger.DEBUG_A,"(Pass1)workAreaPath = " + workAreaPath);
            
            //  Find the "RELPATH" argument and the file name
            if (imageSrcLocation.startsWith("/"))
            {
                //  Drop the file at the root 
                //
                relPath = imageSrcLocation;
            }
            else
            {
                //  The file is relative to the directory from work area path
                //  of the source directory. Remove the file name from the
                //  path and append with the image source location specified
                //
                relPath = sourceFilename.substring(workAreaPath.length());
                relPath = relPath.substring(0, relPath.lastIndexOf('/') + 1)
                          + imageSrcLocation;
            }
            //  Strip out any leading source locale directory from the
            //  relative path of file.
            //
            int idx = relPath.indexOf('/', 1);
            if (idx >= 1)
            {
                String relSrc;
                relSrc = relPath.substring(1, idx);
                if (relSrc.equals(sourceLocale))
                    relPath = relPath.substring(idx);
            }
            theLogger.println(Logger.DEBUG_A,"(Pass1)relPath = " + relPath);
            
            //  Determine TeamSite target branches (if any)
            //
            //  if (( sourceBranch == DBsourceBranch) && (target language == DB target language))
            //      write translated file in DBtargetBranch
            //  else
            //      write translated file in sourceBranch with relPath having "locale" prefix
            
            //  Find target language
            String langStr = targetLocale.substring(0,targetLocale.indexOf("_"));
            String countryStr = targetLocale.substring(targetLocale.indexOf("_")+1);
            TableLangs theTargetLanguage = TableLangs.findLanguageByLocale(langStr, countryStr);
            theLogger.println(Logger.DEBUG_A, "TargetLanguage id = " + theTargetLanguage.getID() +
                                           " langStr = " +langStr+
                                           " countryStr = "+countryStr);
            //  Find sourceBranch
            int startIndex = filename.indexOf("TSLink");
            String sourceBranch = filename.substring(startIndex+7);
            startIndex = sourceBranch.indexOf("WORKAREA");
            sourceBranch = sourceBranch.substring(0, startIndex-1); // do not include ending "/"
            
            int serverId = (new Long( ts.getId())).intValue();
            int storeId = (new Long(bStore.getId())).intValue();
            //  Get branch language information
            TableBranchLanguage branchLanguage = TableBranchLanguage
                                                    .findBranchLanguage(sourceBranch, 
                                                                        theTargetLanguage.getID(), serverId, storeId);
            theLogger.println(Logger.DEBUG_A, "sourceBranch = " + sourceBranch+
                                           " TargetLanguage id = "+theTargetLanguage.getID()+
                                           " branchLanguage = " + 
                                           ((branchLanguage != null) ? 
                                                branchLanguage.getSourceBranch() : "null"));
            if ( branchLanguage != null ) 
            {
                // (( sourceBranch == DBsourceBranch) && (target language == DB target language))
                String DBtargetBranch = branchLanguage.getTargetBranch();
                //  create the converted file path by replacing the old branch 
                //  name with the new one in the original file path.
                startIndex = workAreaPath.indexOf(sourceBranch);
                String prefixPath = workAreaPath.substring(0, startIndex);    // e.g. "/iw4/iwmnt/default/"
                String endPath = workAreaPath.substring(prefixPath.length() + sourceBranch.length());
                workAreaPath = prefixPath + DBtargetBranch + endPath;         // fixed "DIR" for the multiple branch case
                theLogger.println(Logger.DEBUG_A,"DBtargetBranch = " + DBtargetBranch);
                theLogger.println(Logger.DEBUG_A,"sourceBranch = " + sourceBranch);
                theLogger.println(Logger.DEBUG_A,"prefixPath = " + prefixPath);
                theLogger.println(Logger.DEBUG_A,"endPath = " + endPath);
            }
            else // single branch case -- prepend the target locale
            {
                relPath = "/" + targetLocale + relPath;
            }
            
            theLogger.println(Logger.DEBUG_A, "(Pass2)workAreaPath = " + workAreaPath);
            theLogger.println(Logger.DEBUG_A, "(Pass2)relPath = " + relPath);
            
            //  Now write the file out
            //
            theLogger.println(Logger.DEBUG_A,"Writing replaced image file out.");
            if (!writeTargetFile(relPath, workAreaPath, imageContents, teamsiteMode, sourceLocale)) 
            {
                String msg = "Problem posting image file " + imageSrcLocation +
                                " to TeamSite";
                theLogger.println(Logger.ERROR, msg);
                p_response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                throw new ServletException(msg);
            }
            
        }
        catch (SAXException se)
        {
            theLogger.printStackTrace(Logger.ERROR,
                "Unable to parse EventFlowXml. Cannot determine where to export the file.", se);
            p_response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException(se.getMessage());
        }
    }
    
    /**
     * <p>Post the target file (in this case, the replaced image) back to the
     * TeamSite target branch.
     * </p>
     * @param relPath the path of the replaced image
     * @param dir the work area path (effectively document root)
     * @param content the file contents. In this case (for image) the content
     * is in Base64 encoding.
     *
     * @return true if it succeeds, false if not
     */
    private boolean writeTargetFile(String relPath, String dir, String content, String teamsiteMode, String sourceLocale)
    {
        // obtain the directory and the name for the target file -- match / 
        // followed by any number of alphanumeric characters _ and .
        //
        RE regularExpression = null;
        try
        {
            regularExpression = new RE("/([a-zA-Z0-9_.]+)$");
        }
        catch (RESyntaxException exc)
        {
            theLogger.println(Logger.DEBUG_A,"caught RESyntaxException "+exc.getMessage());
            return false;
        }
        boolean result = regularExpression.match(relPath);
        String targetFileName = null;
        if (result)
        {
            targetFileName = regularExpression.getParen(1);
            String targetFileNamePre = "/"+targetFileName;
            if (relPath.equals(targetFileNamePre)) 
            {
                relPath = "";
            }
            else 
            {
                // determine relPath and do not include leading or trailing "/"
                // ("1" and minus "1" in following expression)
                relPath = relPath.substring(1, relPath.length() - targetFileName.length() - 1);
            }
            theLogger.println(Logger.DEBUG_A, "(Pass3) relPath = " + relPath);
            theLogger.println(Logger.DEBUG_A, "targetFileName = " + targetFileName);
        }
        else
        {
            // did not work
            theLogger.println(Logger.DEBUG_A, "no regular expression match to relPath");
            return false;
        }
        
        //  Send the image file to TeamSite.
        //
        try
        {
            URL tsURL = null;
            if (s_TSServerType.equalsIgnoreCase("nt"))
            {
                tsURL = new URL(TeamSiteExchange.TS_HTTP+s_TSServerName+":"+
                                s_TSServerPort+TeamSiteExchange.TS_IMPORT_NT);
            }
            else if (s_TSServerType.equalsIgnoreCase("unix"))
            {
                tsURL = new URL(TeamSiteExchange.TS_HTTP+s_TSServerName+":"+
                                s_TSServerPort+TeamSiteExchange.TS_IMPORT_UNIX);
            }
            theLogger.println(Logger.DEBUG_A, "TeamSite URL: " + tsURL.toString());

            HttpURLConnection tsConnection = (HttpURLConnection)tsURL.openConnection();
            tsConnection.setRequestMethod("POST");
            tsConnection.setDoOutput(true);

            PrintWriter out = new PrintWriter(tsConnection.getOutputStream());

            out.print("datafile=" + URLEncoder.encode(content, "UTF-8") + "&");
            out.print("dir=" + URLEncoder.encode(dir, "UTF-8") + "&");
            out.print("relpath=" + URLEncoder.encode(relPath, "UTF-8") + "&");
            out.print("filename=" + URLEncoder.encode(targetFileName, "UTF-8") + "&");
            out.print("filetype=" + URLEncoder.encode("binary", "UTF-8") + "&");
            out.print("session=" + URLEncoder.encode("teamsiteSession", "UTF-8") + "&");
            out.print("teamsiteMode=" + URLEncoder.encode(teamsiteMode, "UTF-8") + "&");
            out.print("sourceLocale=" + URLEncoder.encode(sourceLocale, "UTF-8") + "\n\n");
            out.close();
    
            //  Read the response from TeamSite
            //
            BufferedReader in = new BufferedReader (new InputStreamReader(tsConnection.getInputStream()));
            String inputLine = null;
            String lastLine = null;

            while ((inputLine = in.readLine()) != null)
            {
                theLogger.println(Logger.DEBUG_A,inputLine);
                lastLine = inputLine;
            }
            in.close();
         
            //  Now check the LastLine to see what the returned status from the
            //  Teamsite Integration CGI script was
            //
            StringTokenizer st = new StringTokenizer(lastLine,"=");
            st.nextToken(); //skip status
            String status = st.nextToken();
            theLogger.println(Logger.DEBUG_A, "Status returned from Teamsite CGI: " + status);
            if (status != null && status.startsWith("OK"))
                return true;
            else
                return false;
        }
        catch (Exception exc)
        {
            theLogger.println(Logger.DEBUG_A, "Caught exception " + exc);
            theLogger.println(Logger.DEBUG_A, "Caught exception message " + exc.getMessage());
            return false;
        }
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
}
