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

package com.globalsight.cxe.servlet.cms.vignette;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.vignette.VignetteImportRequester;

/**
 * The VignetteImportServlet takes in an HTTP POST containing all the data
 * needed to check a file out of Vignette.
 * 
 * The HTTP parameters expected are: SrcMid(multi),Path(multi), FileProfileId,
 * TargetProjectMid, ReturnStatus, VersionFlag, JobName
 */
public class VignetteImportServlet extends HttpServlet
{
    private Logger s_logger = Logger.getLogger(VignetteImportServlet.class);

    /**
     * Constructs the VignetteImportServlet.
     * 
     * @exception ServletException
     */
    public VignetteImportServlet() throws ServletException
    {
    }

    /**
     * Handles the import request by publishing a WebMethods event
     * (VignetteSelectedFileEvent) containing the posted data.
     * 
     * This writes out some HTML showing what was posted.
     * 
     * @param request
     * @param response
     * @exception IOException
     * @exception ServletException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String queryString = request.getQueryString();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Vignette Import Servlet Results</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");
        out.println("<p>POST parameters are:<br>");
        try
        {
            // get all the parameters from the request
            String[] srcMids = request
                    .getParameterValues(VignetteImportRequester.SRC_MID);
            String[] paths = request
                    .getParameterValues(VignetteImportRequester.PATH);
            String fileProfileId = request
                    .getParameter(VignetteImportRequester.FILEPROFILEID);
            String targetProjectMid = request
                    .getParameter(VignetteImportRequester.TARGET_PROJECT_MID);
            String returnStatus = request
                    .getParameter(VignetteImportRequester.RETURN_STATUS);
            String versionFlag = request
                    .getParameter(VignetteImportRequester.VERSION_FLAG);
            String jobName = request
                    .getParameter(VignetteImportRequester.JOB_NAME);

            // create a unique batch ID
            String batchId = jobName
                    + Long.toString(System.currentTimeMillis());

            out.println("<ol><li>batchId is " + batchId + "</li>");
            out.println("<ol><li>fileProfileId is " + fileProfileId + "</li>");
            out.println("<li> srcMid/path list is: <ul>");

            int pageCount = srcMids.length;
            for (int i = 0; i < pageCount; i++)
            {
                out.println("<li>" + srcMids[i] + "(" + paths[i] + ")</li>");
            }
            out.println("</ul></ol><br><br>");

            for (int j = 0; j < pageCount; j++)
            {
                s_logger.info("Publishing vignette request for srcMid "
                        + srcMids[j] + ", path " + paths[j]);
                CxeProxy.importFromVignette(jobName, batchId,
                        j + 1 /* pageNum */, pageCount, 1, 1, srcMids[j],
                        paths[j], fileProfileId, targetProjectMid,
                        returnStatus, versionFlag, CxeProxy.IMPORT_TYPE_L10N);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Cannot publish event to WebMethods", e);
            out.println(e);
        }
        out.println("</body>");
        out.println("</html>");
    }
}
