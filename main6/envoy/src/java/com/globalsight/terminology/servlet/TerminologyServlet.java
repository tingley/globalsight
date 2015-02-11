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
package com.globalsight.terminology.servlet;

import org.apache.log4j.Logger;

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;

/**
 * This servlet creates and updates Termbases.  Client code is in
 * terminology/management/protocol.js and definition.jsp.
 */
public class TerminologyServlet
    extends HttpServlet
    implements WebAppConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TerminologyServlet.class);

    private static ITermbaseManager m_manager = null;

    public TerminologyServlet ()
        throws ServletException
    {
        super();

        try
        {
            m_manager = ServerProxy.getTermbaseManager();
        }
        catch (GeneralException ex)
        {
            CATEGORY.error("could not initialize remote TermbaseManager", ex);
        }
    }

    public void doGet (HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        doPost(p_request, p_response);
    }

    /**
     * Extracts and executes a request to the TermbaseManager to
     * create, modify, or delete a termbase.
     */
    public void doPost (HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        try
        {
            // For multi company.
            String companyName = UserUtil.getCurrentCompanyName(request);
            if (companyName != null)
            {
                CompanyThreadLocal.getInstance().setValue(companyName);
            }
            
            HttpSession session = request.getSession();
            String userId = PageHandler.getUser(session).getUserId();
            
            response.setContentType("text/xml; charset=UTF-8");
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            String action = (String)request.getParameter(TERMBASE_ACTION);
            String tbid = (String)request.getParameter(TERMBASE_TB_ID);
            String name = null;

            if (tbid != null && !tbid.equals("-1"))
            {
                name = m_manager.getTermbaseName(Long.parseLong(tbid));
            }

            if (action == null)
            {
                returnBadRequest(response);
                return;
            }
            else if (action.equals(""))
            {
                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_NEW))
            {
                boolean isAdmin = UserUtil.isInPermissionGroup(userId,
                        "Administrator");
                boolean isSuperAdmin = UserUtil.isInPermissionGroup(userId,
                        "SuperAdministrator");
                String xml = readBody(request);

                ITermbase tb = m_manager.create(userId, "", xml);
                if (tb != null && !isSuperAdmin && !isAdmin)
                {
                    ProjectTMTBUsers ptb = new ProjectTMTBUsers();
                    long tbId = ServerProxy.getTermbaseManager().getTermbaseId(
                            tb.getName());
                    ptb.addUsers(userId, Long.toString(tbId), "TB");
                }
                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_MODIFY))
            {
                String xml = readBody(request);

                m_manager.updateDefinition(name, xml, userId, "");

                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_DELETE))
            {
                m_manager.delete(name, userId, "");

                returnOk(response);
                return;
            }

            returnBadRequest(response);
        }
        catch (TermbaseException ex)
        {
            returnError(response, ex);
        }
        catch (Throwable ex)
        {
            returnError(response, ex);
        }
    }


    /** invalid arguments */
    private void returnBadRequest(HttpServletResponse response)
        throws IOException
    {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "bad request");
    }


    /** request failed */
    private void returnError(HttpServletResponse response,
        TermbaseException exception)
        throws IOException
    {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            exception.getMessage() + "@@@@@" + exception.getStackTraceString());
    }


    /** request failed */
    private void returnError(HttpServletResponse response,
        Throwable exception)
        throws IOException
    {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            exception.getMessage() + "@@@@@" +
            GeneralException.getStackTraceString(exception));
    }


    /**
     * Request succeeded without result (SC_NO_CONTENT, not supported
     * by Msxml2.XMLHTTP.
     */
    private void returnOk(HttpServletResponse response)
    {
        response.setStatus(HttpServletResponse.SC_OK);
    }


    /** request succeeded with result */
    private void returnXml(HttpServletResponse response, String xml)
        throws IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        out.print(xml);
        out.flush();
    }

    /** action not implemented */
    private void returnNotImplemented(HttpServletResponse response)
        throws IOException
    {
        response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
            "not implemented");
    }

    /**
     * Reads the body of a posted request into a string. The request
     * has to be posted as UTF-8 and this routine will convert it to
     * UCS2.
     */
    private String readBody(HttpServletRequest request)
        throws IOException
    {
        StringBuffer result = new StringBuffer();

        try
        {
            // Convert input to Unicode.
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), "UTF-8"));

            String line;

            while ((line = reader.readLine()) != null)
            {
                result.append(line).append('\n');
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("IN body UCS2 = `" + result.toString() + "'");
            }
        }
        catch (IOException ex)
        {
            CATEGORY.error("IO exception", ex);
        }

        return result.toString();
    }
}
