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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.sun.jndi.toolkit.url.UrlUtil;

/**
 * The UncacheableFileServlet can be used get files so they are not cached.
 * Files can also be retrieved in a zip file.
 */
public class CompanyRelatedUncacheableFileServlet extends HttpServlet
{
    private static final long serialVersionUID = -2008264458473698612L;

    static public final int BUFSIZE = 4096;
    private static final String SUPER_COMPANY_ID = "1";
    private static Map<String, String> contentTypeMap = new HashMap<String, String>();

    static
    {
        contentTypeMap
                .put("docx",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        contentTypeMap
                .put("dotx",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        contentTypeMap
                .put("pptx",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        contentTypeMap
                .put("potx",
                        "application/vnd.openxmlformats-officedocument.presentationml.template");
        contentTypeMap
                .put("xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        contentTypeMap
                .put("xltx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        contentTypeMap.put("xml", "application/vnd.sun.xml.calc.template");
        contentTypeMap.put("pdf", "application/pdf");
        contentTypeMap.put("css", "text/css");
        contentTypeMap.put("doc", "application/msword");
        contentTypeMap.put("exe", "application/octet-stream");
        contentTypeMap.put("gz", "application/x-gzip");
        contentTypeMap.put("mif", "application/x-mif");
        contentTypeMap.put("rtf", "application/rtf");
    }

    private static List<String> NOT_CHANGE_NAME_FILES = new ArrayList<String>();
    static
    {
        NOT_CHANGE_NAME_FILES.add("inx");
    }

    /**
     * Sets the response header to expire in one second and then lets the
     * FileServlet handle the request.
     * 
     * @param p_request
     *            -- the request
     * @param p_response
     *            -- the response
     * @throws ServletException
     *             , IOException
     */
    public void service(HttpServletRequest p_request,
            HttpServletResponse p_response) throws ServletException,
            IOException
    {
        try
        {
            boolean zipFile = false;

            String docHome = getInitParameter("docHome");

            WorkObject wo = (WorkObject) TaskHelper.retrieveObject(
                    p_request.getSession(), WebAppConstants.WORK_OBJECT);

            String companyName = "";
            if (wo == null)
            {
                companyName = UserUtil.getCurrentCompanyName(p_request);
            }
            else
            {
                if (wo instanceof Task)
                {
                    Task task = (Task) wo;
                    long companyId = task.getCompanyId();
                    companyName = ServerProxy.getJobHandler()
                            .getCompanyById(companyId).getName();
                }
                else if (wo instanceof Job)
                {
                    Job job = (Job) wo;
                    long companyId = job.getCompanyId();
                    companyName = ServerProxy.getJobHandler()
                            .getCompanyById(companyId).getName();
                }
            }
            // String fileName = p_request.getParameter("file");
            //
            // // securyty consideration. This servlet returns a file only in
            // // "docHome" directory.
            // if(fileName.indexOf('/') != -1 || fileName.indexOf('\\') != -1)
            // {
            // throw new ServletException("File "
            // + fileName + " cannot be accessible.");
            // }

            // File file = new File(docHome, fileName);

            // For Global LP

            String companyId = CompanyWrapper.getCompanyIdByName(companyName);
            String fileFullPath;

            if (SUPER_COMPANY_ID.equals(companyId))
            {
                fileFullPath = new StringBuffer().append(docHome)
                        .append(p_request.getServletPath())
                        .append(p_request.getPathInfo()).toString();
            }
            else
            {
                fileFullPath = new StringBuffer().append(docHome)
                        .append(File.separator).append(companyName)
                        .append(p_request.getServletPath())
                        .append(p_request.getPathInfo()).toString();
            }

            File file = new File(fileFullPath);
            String fileName = file.getName();
            fileName = UrlUtil.encode(fileName, "utf-8");

            String zipParam = p_request.getParameter("zip");
            if (zipParam != null && zipParam.equals("true"))
            {
                zipFile = true;
            }

            if (zipFile)
            {
                p_response.setContentType("application/zip");
                p_response.setHeader("Content-Disposition",
                        "attachment; filename=" + fileName + ".zip;");
            }
            else
            {
                // String ext = getExtension(fileName);
                // String contentType = getContentTypeByExtension(ext);
                p_response.setContentType("application/octet-stream");
                String attachment = "attachment; filename=\"" + fileName
                        + "\";";
                p_response.setHeader("Content-Disposition", attachment);
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
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getContentTypeByExtension(String p_ext)
    {
        String ext = "";

        if (p_ext != null)
        {
            ext = p_ext.toLowerCase();
        }

        if (contentTypeMap != null && contentTypeMap.containsKey(ext))
        {
            return contentTypeMap.get(ext);
        }

        return "text/plain";
    }

    private String getExtension(String p_fileName)
    {
        if (p_fileName != null)
        {
            int i = p_fileName.lastIndexOf('.');
            if (i > 0 && i < p_fileName.length() - 1)
            {
                return p_fileName.substring(i + 1).toLowerCase();
            }
            ;
        }

        return "";
    }

    /**
     * Writes out the specified file to the responses output stream. Also places
     * it in a zip file if p_putInZip is set to true.
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
}