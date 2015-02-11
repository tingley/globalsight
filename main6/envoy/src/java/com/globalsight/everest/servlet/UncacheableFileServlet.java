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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.File;
import org.apache.tools.zip.ZipOutputStream;
import org.apache.tools.zip.ZipEntry;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

/**
 * The UncacheableFileServlet can be used get files so they are not
 * cached. Files can also be retrieved in a zip file.
 */
public class UncacheableFileServlet extends HttpServlet
{
    static public final int BUFSIZE = 4096;
    
    /**
    * Sets the response header to expire in one second and then lets the FileServlet
    * handle the request.
    * @param p_request -- the request
    * @param p_response -- the response
    * @throws ServletException, IOException
    */
    public void service(HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws ServletException, IOException
    {
    	// gbs-1389: restrict direct access to shutdown page without
		// shutdown permission.
    	HttpSession session = p_request.getSession(false);
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.LOGS_VIEW)) 
		{
			p_response.sendRedirect(p_request.getContextPath());
			return;
		}
        boolean zipFile = false;
        
        String docHome = getInitParameter("docHome");
        String fileName = p_request.getParameter("file");

        // securyty consideration. This servlet returns a file only in
        // "docHome" directory.
        if(fileName.indexOf('/') != -1 || fileName.indexOf('\\') != -1)
        {
            throw new ServletException("File "
                + fileName + " cannot be accessible.");
        }
        
        File file = new File(docHome, fileName);

        String zipParam = p_request.getParameter("zip");
        if(zipParam != null && zipParam.equals("true"))
        {
            zipFile = true;
        }
        
        if(zipFile)
        {
            p_response.setContentType("application/zip");
            p_response.setHeader("Content-Disposition",
                "attachment; filename=" + fileName + ".zip;");
        }
        else
        {
            p_response.setContentType("text/plain; charset=UTF-8");
        }
        
        if (p_request.isSecure())
        {
            PageHandler.setHeaderForHTTPSDownload(p_response);
        }
        else
        {
            p_response.setHeader("Cache-Control", "no-cache");
        }

        writeOutFile(file, p_response, zipFile);
    }

    /**
     * Writes out the specified file to the responses output stream.
     * Also places it in a zip file if p_putInZip is set to true.
     *
     */
    protected void writeOutFile(File p_file, HttpServletResponse p_response,
                              boolean p_putInZip)
        throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(
            new FileInputStream(p_file));
        OutputStream out = p_response.getOutputStream();
        if(p_putInZip)
        {
            out = new ZipOutputStream(out);
            ((ZipOutputStream)out).putNextEntry(new ZipEntry(p_file.getName()));
        }
        
        byte[] buf = new byte[BUFSIZE];
        int readLen = 0;
        
        while((readLen = in.read(buf, 0, BUFSIZE)) != -1)
        {
            out.write(buf, 0, readLen);
        }
        in.close();

        if(p_putInZip)
        {
            ((ZipOutputStream)out).closeEntry();
            ((ZipOutputStream)out).finish();
        }
    }
}

