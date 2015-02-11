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
 * Copyright (c) 2003 GlobalSight Corporation. All rights reserved.
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
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.util.Date;

//  Java Extensions
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

//  GlobalSight
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GeneralException;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.Base64;

//DOM,SAX
import java.io.StringReader;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is the servlet for exporting images back to the temp location used
 * by the ms office adapter
 *
 */
public class MsOfficeImageReplaceHandler extends HttpServlet
{
    private Logger theLogger = Logger.getLogger();
    private static final char UNIX_SEPARATOR = '/';
    private static final char WIN_SEPARATOR = '\\';

    public MsOfficeImageReplaceHandler()
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
     * the ms office converter's directory structure.
     * <p>
     * The file is uploaded into the directory as defined in the image source
     * location as defined in one of the parameter to POST request.
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
        String imageSrcLocation = null;
        try
        {
            String eventFlowXml = p_request.getParameter(ExportConstants.EVENT_FLOW_XML);
            String locale = p_request.getParameter(ExportConstants.TARGET_LOCALE);
            String imageContents = p_request.getParameter(ExportConstants.IMAGE_DATA);
            imageSrcLocation = p_request.getParameter(ExportConstants.IMAGE_FILENAME);

            //Write the file out to C:\WINFILES\word\fr_FR\234foo_files\img003.jpg
            String targetFileName = findTargetFileName(eventFlowXml,locale,imageSrcLocation);
            theLogger.println(Logger.INFO, "Exporting image " + imageSrcLocation +
                          " to " + targetFileName);

            File targetFile = new File(targetFileName);
            targetFile.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
     
            bos.write(Base64.decodeToByteArray(imageContents));
            bos.flush();
            bos.close();
            fos.close();

            //set the response code as ok
            p_response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = p_response.getWriter();
            out.println("OK"); //just write something out
            out.close();
        }
        catch (Exception ex)
        {
            p_response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            theLogger.printStackTrace(Logger.ERROR, "Problem saving image file: " + imageSrcLocation, ex);
            throw new ServletException(ex);
        }
    }

        /**
        * Takes in the directory name and replaces "/" and "\" with the appropriate
        * File.separator for the current operating system. Also removes the leading
        * and trailing separators.
        */
        private String makeDirectoryNameOperatingSystemSafe(String p_dirName)
        {
            //first check if it's a UNC pathname and we're on Windows. If so leave it alone.
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows") && p_dirName.startsWith("\\\\"))
                return p_dirName;

            String newName = p_dirName.replace(UNIX_SEPARATOR,File.separatorChar);
            newName = newName.replace(WIN_SEPARATOR,File.separatorChar);
            if (newName.endsWith(File.separator))
                newName = newName.substring(0,newName.length() - 1);

            return newName;
        }

        private String findTargetFileName(String p_eventFlowXml, String p_locale, String p_imageSrcLocation)
        throws Exception
        {
            StringBuffer targetFileName = new StringBuffer();
            StringReader sr = new StringReader(p_eventFlowXml);
            InputSource is = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(is);
            Element root = parser.getDocument().getDocumentElement();
            NodeList nl = root.getElementsByTagName("da");
            String convDir = null;
            String safeBaseFileName = null;
            for (int i=0; i < nl.getLength(); i++)
            {
                Element e = (Element) nl.item(i);
                if (e.getAttribute("name").equals("msoffice_dir"))
                {
                    Element dv = (Element) e.getElementsByTagName("dv").item(0);
                    convDir = dv.getFirstChild().getNodeValue();
                    theLogger.println(Logger.DEBUG_D,"conv dir is " + convDir);
                }
                else if (e.getAttribute("name").equals("safeBaseFileName"))
                {
                    Element dv = (Element) e.getElementsByTagName("dv").item(0);
                    safeBaseFileName = dv.getFirstChild().getNodeValue();
                    theLogger.println(Logger.DEBUG_D,"safeBaseFileName is " + safeBaseFileName);
                }

                if (convDir != null && safeBaseFileName != null)
                    break;
            }

            targetFileName.append(convDir);
            targetFileName.append(File.separator);
            targetFileName.append(p_locale);
            targetFileName.append(File.separator);
            if (safeBaseFileName.endsWith(".ppt"))
            {
                targetFileName.append(safeBaseFileName.substring(0,safeBaseFileName.indexOf(".")));
                targetFileName.append("_files");
                targetFileName.append(File.separator);
            }
            targetFileName.append(p_imageSrcLocation);

            String newName = targetFileName.toString().replace(UNIX_SEPARATOR,File.separatorChar);
            return newName.replace(WIN_SEPARATOR,File.separatorChar);
        }

        private String findRelativePath(String p_originalFileName)
        throws Exception
        {
            //strip off the leading directory name and the basefilename
            int sidx = p_originalFileName.indexOf(File.separator) + 1;
            int eidx = p_originalFileName.lastIndexOf(File.separator);
            String relpath = "";
            if (sidx < eidx)
                relpath = p_originalFileName.substring(sidx,eidx);

            theLogger.println(Logger.DEBUG_D,"orig filename is " + relpath);
            return relpath;
        }

        //change the image location from a fully qualified URL to a relative path
        private String makeRelativePath(String p_imageName)
        {
            String a = p_imageName.replace('/',File.separatorChar);
            if (a.startsWith("http"))
            {
                //fully qualified, so remove the http:// or https://
                //assume the URL is http://foo:port/dir1/image.jpg
                int i = a.indexOf(File.separator); // http:/
                i = a.indexOf(File.separator,i+1); //http://
                i = a.indexOf(File.separator,i+1); //http://foo:port/
                a = a.substring(i+1); //return the dir1/image.jpg
                StringBuffer warning = new StringBuffer("Cannot use the image name \"");
                warning.append(p_imageName);
                warning.append("\" because it is a fully qualified URL.\r\n");
                warning.append("Replacing with: \"");
                warning.append(a);
                warning.append("\"");
                theLogger.println(Logger.WARNING, warning.toString());
            }
            
            return a;
        }
}

