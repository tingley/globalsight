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
import com.globalsight.everest.webapp.pagehandler.PageHandler;

/**
 * The ImageServlet can be used to view images contained in imported documents,
 * i.e. images that are stored in the CXEDOCS directory.
 */
public class ImageFileServlet extends UncacheableFileServlet
{
    public Logger CATEGORY = Logger.getLogger(ImageFileServlet.class);

    /**
     * Write out the image to the response's buffered stream.
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

        // strip off the first CXEDOCS part of the url since it is not
        // part of the directory structure
        String url = p_request.getRequestURI().toString();
        String decodedUrl = com.globalsight.ling.common.URLDecoder.decode(url,
                "UTF-8");
        String fileName = decodedUrl;

        int index = decodedUrl.indexOf(WebAppConstants.VIRTUALDIR_CXEDOCS);
        if (index >= 0)
        {
            fileName = decodedUrl.substring(index
                    + WebAppConstants.VIRTUALDIR_CXEDOCS.length());
        }
        else
        {
            // invalid or incorrect use of this servlet
            CATEGORY.warn("Invalid request for " + fileName + ", not under "
                    + WebAppConstants.VIRTUALDIR_CXEDOCS);
            p_response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = new File(docHome, fileName);
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
}
