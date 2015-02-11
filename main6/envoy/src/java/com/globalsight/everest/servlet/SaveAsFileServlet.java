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

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.File;
import org.apache.tools.zip.ZipOutputStream;
import org.apache.tools.zip.ZipEntry;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;


/**
 * The SaveAsFileServlet can be use to download any file as binary, so
 * that the Browser shows the save-as dialog.
 *
 * See MIME Type Detection in Internet Explorer
 * (http://msdn.microsoft.com/workshop/networking/moniker/overview/appendix_a.asp)
 *
 * Use in a web page like this:
 * function downloadFile()
 * {
 *   // window.open('/exports?file=' + g_filename + '&zip=true', '_blank');
 *   idDownload.location.href = '/exports?file=' + g_filename + '&zip=true';
 * }
 *
 * <IFRAME id="idDownload" NAME='download' WIDTH='0' HEIGHT='0'></IFRAME>
 */
public class SaveAsFileServlet extends HttpServlet
{
    private static final long serialVersionUID = 1322251918171863486L;

    static public final int BUFSIZE = 4096;

    /**
     * Sets the response header to expire in one second and then lets
     * the FileServlet handle the request.
     * @param p_request -- the request
     * @param p_response -- the response
     * @throws ServletException, IOException
     */
    public void service(HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws ServletException, IOException
    {
        boolean zipFile = false;
        String docHome = getInitParameter("docHome");
        String currentCompany = UserUtil.getCurrentCompanyName(p_request);
        String fileName = p_request.getParameter("file");
        String fileType = p_request.getParameter("fileType");

        if(p_request.getParameter("xliff") != null)
        {
            String DOCROOT = "/";
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc.getStringParameter(
                SystemConfiguration.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\"))) {
                root = root + "/";
            }

            DOCROOT = root + "_Exports_";
             
            String companyName = p_request.getParameter("companyName");
            docHome = DOCROOT + "/" + companyName;  
        }
        else
        {
            CompanyThreadLocal.getInstance().setValue(currentCompany);

            // Security consideration. This servlet returns a file only in
            // "docHome" directory.
            if(fileName.indexOf('/') != -1 || fileName.indexOf('\\') != -1)
            {
                throw new ServletException("File " + fileName +
                " is not accessible.");
            }

            if(p_request.getServletPath().equals("/alignerPackages"))
            {
        	    docHome = 
        		    AmbFileStoragePathUtils.getAlignerPackageDir().getAbsolutePath();
            }
            else if (p_request.getServletPath().equals("/downloadresource"))
            {
                // do nothing
            }
            else if("tm".equalsIgnoreCase(fileType))
            {
                String companyId = CompanyWrapper.getCompanyIdByName(currentCompany);
                docHome = AmbFileStoragePathUtils.getFileStorageDirPath(companyId);
                docHome += File.separator + AmbFileStoragePathUtils.TM_EXPORT_FILE_SUB_DIR;
            }
            else 
            {
            	if(!CompanyWrapper.getCompanyIdByName(currentCompany).
            			equals(CompanyWrapper.SUPER_COMPANY_ID))
            	{
            		docHome = docHome + "/" + currentCompany;
            	}
            }
        }
        File file = new File(docHome, fileName);

        String zipParam = p_request.getParameter("zip");
        if (zipParam != null && zipParam.equals("true"))
        {
            zipFile = true;
        }

        if (zipFile)
        {
            String basename = fileName;
            int index = fileName.lastIndexOf(".");
            if (index >= 0)
            {
                basename = fileName.substring(0, index);
            }

            // For IE: x-msdownload, x-zip-compressed
            p_response.setContentType("application/zip");
            p_response.setHeader("Content-Disposition",
                "attachment; filename=\"" + basename + ".zip\";");
        }
        else
        {
            p_response.setContentType("application/octet-stream");
            p_response.setContentLength((int)file.length());
            p_response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\";");
        }

        // If the file cannot be saved in IE's temporary cache or if
        // the expiration time is set to immediate, the files cannot
        // be downloaded!!!
        //p_response.setHeader("Cache-Control", "no-cache");
        
        if (p_request.isSecure())
        {
            PageHandler.setHeaderForHTTPSDownload(p_response);
        }

        writeOutFile(file, p_response, zipFile);
    }

    /**
     * Writes out the specified file to the responses output stream.
     * Also places it in a zip file if p_putInZip is set to true.
     */
    protected void writeOutFile(File p_file, HttpServletResponse p_response,
        boolean p_putInZip)
        throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(
            new FileInputStream(p_file));
        OutputStream out = p_response.getOutputStream();
        if (p_putInZip)
        {
            out = new ZipOutputStream(out);
            ((ZipOutputStream)out).setMethod(ZipOutputStream.DEFLATED);
            ((ZipOutputStream)out).setLevel(9);
            ((ZipOutputStream)out).putNextEntry(
                new ZipEntry(p_file.getName()));
        }

        byte[] buf = new byte[BUFSIZE];
        int readLen = 0;

        while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
        {
            out.write(buf, 0, readLen);
        }
        in.close();

        if (p_putInZip)
        {
            ((ZipOutputStream)out).closeEntry();
            ((ZipOutputStream)out).finish();
        }
    }
}

