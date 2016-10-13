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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.WebAppConstants;

/**
 * The StfFilesServlet can be used to view StfFiles contained in imported
 * documents, i.e. StfFiles that are stored in the FileStorage directory.
 */
public class StfFilesServlet extends UncacheableFileServlet
{
    private static final long serialVersionUID = 4989706150134879251L;

    public Logger CATEGORY = Logger.getLogger(StfFilesServlet.class);

    /**
     * Write out the StfFiles to the response's buffered stream.
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

        // strip off the first StfFiles part of the url since it is not
        // part of the directory structure
        String url = p_request.getRequestURI();
        String decodedUrl = com.globalsight.ling.common.URLDecoder.decode(url,
                "UTF-8");
        String fileName = decodedUrl;

        int index = decodedUrl.indexOf(WebAppConstants.STF_FILES_URL_MAPPING);
        if (index >= 0)
        {
            fileName = decodedUrl.substring(index
                    + WebAppConstants.STF_FILES_URL_MAPPING.length());
            String[] rags = fileName.split("/");
            if (rags.length > 0)
            {
                // start with company name
                StringBuffer sb = new StringBuffer(rags[0]);
                sb.append("/GlobalSight/SecondaryTargetFiles");
                for (int i = 1; i < rags.length; i++)
                {
                    sb.append("/");
                    sb.append(rags[i]);
                }
                fileName = sb.toString();
            }
        }
        else
        {
            // invalid or incorrect use of this servlet
            CATEGORY.warn("Invalid request for " + fileName + ", not under "
                    + WebAppConstants.STF_FILES_URL_MAPPING);
            p_response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(docHome, fileName);
        if (!file.exists())
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Requested StfFiles `" + fileName
                        + "' does not exist.");
            }
            p_response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Sending StfFiles " + fileName);
            }

            // set the content type appropriately
            p_response.setContentType("application/octet-stream");

            writeOutFile(file, p_response, false);
        }
        catch (Throwable ignore)
        {
            // client may have closed the connection, ignore
        }
    }
}
