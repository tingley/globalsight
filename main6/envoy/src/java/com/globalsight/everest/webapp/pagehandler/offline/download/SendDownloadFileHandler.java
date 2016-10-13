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

package com.globalsight.everest.webapp.pagehandler.offline.download;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;


/**
 * SendDownloadFileHandler is responsible for creating a download file
 * and sending it to the user.
 */
public class SendDownloadFileHandler
    extends PageHandler
{
//    private static final Logger CATEGORY =
//        Logger.getLogger(
//            SendDownloadFileHandler.class);

    // Constructor
    public SendDownloadFileHandler()
    {
    }

    /**
     * Invokes this PageHandler
     *
     * @param p_thePageDescriptor the page desciptor
     * @param p_theRequest the original request sent from the browser
     * @param p_theResponse the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        // invoke download
        SendDownloadFileHelper helper = new SendDownloadFileHelper();
        helper.doSendDownloadFile(p_request, p_response);
        String state = p_request.getParameter(DOWNLOAD_ACTION);
        if (state != null && !state.equals(DOWNLOAD_ACTION_DONE))
        {
            super.invokePageHandler(p_pageDescriptor, p_request,
                p_response, p_context);
        }
    }
}
