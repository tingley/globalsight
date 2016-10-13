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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

public class DownloadFileStorageFilesWithoutLoginServlet extends HttpServlet
{
    private static final long serialVersionUID = 206073587763581811L;

    private static final long SUPER_COMPANY_ID = 1;

    public Logger CATEGORY = Logger
            .getLogger(DownloadFileStorageFilesWithoutLoginServlet.class);

    static public final int BUFSIZE = 4096;

    /**
     * Write out the fileStorage files to the response's buffered stream.
     * 
     * @param p_request
     *            -- the request
     * @param p_response
     *            -- the response
     * @throws ServletException
     * @throws IOException
     */
    public void service(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        String docHome = getInitParameter("docHome");
        String companyName = p_request.getParameter("companyName");

        // URL:
        // http://10.10.211.117:80/globalsight/GlobalSight/CommentReference2/1010/General/1001.txt?companyName=york
        // Actual:
        // C:\Welocalize\FileStorage\york\GlobalSight\CommentReference\1010\General\1001.txt
        StringBuffer filePath = new StringBuffer();
        if (companyName != null)
        {
            Company company = getCompanyByCompanyName(companyName);
            if (company == null || SUPER_COMPANY_ID != company.getId())
            {
                filePath.append(companyName);
            }
        }
        filePath.append(File.separator).append("GlobalSight")
                .append(File.separator).append("CommentReference");
        String url = p_request.getRequestURI().toString();
        String decodedUrl = com.globalsight.ling.common.URLDecoder.decode(url,
                "UTF-8");
        String fileName = decodedUrl;

        int index = decodedUrl.indexOf(WebAppConstants.COMMENT_REFERENCE2);
        if (index >= 0)
        {
            fileName = decodedUrl.substring(index
                    + WebAppConstants.COMMENT_REFERENCE2.length() + 1);
            filePath.append(File.separator).append(fileName);
        }
        else
        {
            // invalid or incorrect use of this servlet
            CATEGORY.warn("Invalid request for " + fileName + ", not under /"
                    + WebAppConstants.COMMENT_REFERENCE);
            p_response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(docHome, filePath.toString());
        if (!file.exists())
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Requested image `" + fileName
                        + "' does not exist.");
            }
            p_response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Sending image " + fileName);
            }

            // set the content type appropriately
            p_response.setContentType("application/octet-stream");

            if (p_request.isSecure())
            {
                PageHandler.setHeaderForHTTPSDownload(p_response);
            }

            writeOutFile(file, p_response, false);
        }
        catch (Throwable ignore)
        {
            // client may have closed the connection, ignore
        }
    }

    /**
     * Writes out the specified file to the responses output stream. Also places
     * it in a zip file if p_putInZip is set to true.
     * 
     */
    protected void writeOutFile(File p_file, HttpServletResponse p_response,
            boolean p_putInZip) throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                p_file));
        OutputStream out = p_response.getOutputStream();
        if (p_putInZip)
        {
            out = new ZipOutputStream(out);
            ((ZipOutputStream) out)
                    .putNextEntry(new ZipEntry(p_file.getName()));
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
            ((ZipOutputStream) out).closeEntry();
            ((ZipOutputStream) out).finish();
        }
    }

    private Company getCompanyByCompanyName(String p_companyName)
    {
        Company company = null;

        try
        {
            company = ServerProxy.getJobHandler().getCompany(p_companyName);
        }
        catch (Exception e)
        {

        }

        return company;
    }
}
