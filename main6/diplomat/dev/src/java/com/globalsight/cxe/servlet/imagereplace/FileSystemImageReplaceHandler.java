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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.util.Base64;

/**
 * This is the servlet for exporting images back to the target file system.
 * 
 */
public class FileSystemImageReplaceHandler extends HttpServlet
{
    private static final long serialVersionUID = 4099361074061801148L;
    private static final Logger CATEGORY = Logger
            .getLogger(FileSystemImageReplaceHandler.class);
    private static final char UNIX_SEPARATOR = '/';
    private static final char WIN_SEPARATOR = '\\';

    /**
     * This method processing the POST request made to upload image back to the
     * target file system.
     * <p>
     * The file is uploaded into the directory as defined in the image source
     * location as defined in one of the parameter to POST request.
     * <p>
     * All parameters are passed in as an URLEncoded.
     * 
     * @param p_request
     *            Request object
     * @param p_response
     *            Response object
     * @throws ServletException
     *             Exception encountered during this servlet execution.
     * @throws IOException
     *             Exception encountered during this servlet execution.
     */
    public void doPost(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        String imageSrcLocation = null;
        try
        {
            String eventFlowXml = p_request
                    .getParameter(ExportConstants.EVENT_FLOW_XML);
            String exportLocation = p_request
                    .getParameter(ExportConstants.EXPORT_LOCATION);
            String localeSubDir = p_request
                    .getParameter(ExportConstants.LOCALE_SUBDIR);

            if (localeSubDir.startsWith("/") || localeSubDir.startsWith("\\"))
                localeSubDir = localeSubDir.substring(1);

            imageSrcLocation = p_request
                    .getParameter(ExportConstants.IMAGE_FILENAME);
            String imageContents = p_request
                    .getParameter(ExportConstants.IMAGE_DATA);

            exportLocation = makeDirectoryNameOperatingSystemSafe(exportLocation);
            imageSrcLocation = makeRelativePath(imageSrcLocation);
            CATEGORY.info("Exporting image " + imageSrcLocation
                    + " to export directory " + exportLocation);

            // Write out the file to the location defined. The file destination
            // include the export directory prepended. The location looks like
            // this
            //
            // <cxe_document_root>/<export_dir>/<image_source_location>
            //
            String originalFileName = findOriginalFileName(eventFlowXml);
            String relPath = findRelativePath(originalFileName);

            StringBuffer targetFilename = new StringBuffer(exportLocation);
            targetFilename.append(File.separator);
            targetFilename.append(localeSubDir);

            if (!imageSrcLocation.startsWith((File.separator)))
            {
                targetFilename.append(File.separator);
                targetFilename.append(relPath);
                targetFilename.append(File.separator);
            }
            targetFilename.append(imageSrcLocation);

            CATEGORY
                    .debug("Target image file is: " + targetFilename.toString());

            File targetFile = new File(targetFilename.toString());

            // Create a directory structure
            //
            targetFile.getParentFile().mkdirs();

            FileOutputStream fos = new FileOutputStream(targetFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // Decode the image contents from Base64 string into a
            // byte array and write it out
            //
            bos.write(Base64.decodeToByteArray(imageContents));
            bos.flush();
            bos.close();
            fos.close();

            // set the response code as ok
            p_response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = p_response.getWriter();
            out.println("OK"); // just write something out
            out.close();
        }
        catch (Exception ex)
        {
            p_response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            CATEGORY.error(ex.getMessage(), ex);
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
        // first check if it's a UNC pathname and we're on Windows. If so leave
        // it alone.
        String os = System.getProperty("os.name");
        if (os.startsWith("Windows") && p_dirName.startsWith("\\\\"))
            return p_dirName;

        String newName = p_dirName.replace(UNIX_SEPARATOR, File.separatorChar);
        newName = newName.replace(WIN_SEPARATOR, File.separatorChar);
        if (newName.endsWith(File.separator))
            newName = newName.substring(0, newName.length() - 1);

        return newName;
    }

    private String findOriginalFileName(String p_eventFlowXml) throws Exception
    {
        StringReader sr = new StringReader(p_eventFlowXml);
        InputSource is = new InputSource(sr);
        DOMParser parser = new DOMParser();
        parser.setFeature("http://xml.org/sax/features/validation", false);
        parser.parse(is);
        Element root = parser.getDocument().getDocumentElement();
        Element source = (Element) root.getElementsByTagName("source").item(0);
        NodeList da = source.getElementsByTagName("da");
        String filename = null;
        for (int i = 0; i < da.getLength(); i++)
        {
            Element e = (Element) da.item(i);
            if (e.getAttribute("name").equals("Filename"))
            {
                Element dv = (Element) e.getElementsByTagName("dv").item(0);
                filename = dv.getFirstChild().getNodeValue();
                CATEGORY.debug("orig filename is " + filename);
                break;
            }
        }
        return filename;
    }

    private String findRelativePath(String p_originalFileName) throws Exception
    {
        // strip off the leading directory name and the basefilename
        int sidx = p_originalFileName.indexOf(File.separator) + 1;
        int eidx = p_originalFileName.lastIndexOf(File.separator);
        String relpath = "";
        if (sidx < eidx)
            relpath = p_originalFileName.substring(sidx, eidx);

        CATEGORY.debug("orig filename is " + relpath);
        return relpath;
    }

    // change the image location from a fully qualified URL to a relative path
    private String makeRelativePath(String p_imageName)
    {
        String a = p_imageName.replace('/', File.separatorChar);
        if (a.startsWith("http"))
        {
            // fully qualified, so remove the http:// or https://
            // assume the URL is http://foo:port/dir1/image.jpg
            int i = a.indexOf(File.separator); // http:/
            i = a.indexOf(File.separator, i + 1); // http://
            i = a.indexOf(File.separator, i + 1); // http://foo:port/
            a = a.substring(i + 1); // return the dir1/image.jpg
            StringBuffer warning = new StringBuffer(
                    "Cannot use the image name \"");
            warning.append(p_imageName);
            warning.append("\" because it is a fully qualified URL.\r\n");
            warning.append("Replacing with: \"");
            warning.append(a);
            warning.append("\"");
            CATEGORY.warn(warning.toString());
        }

        return a;
    }
}
