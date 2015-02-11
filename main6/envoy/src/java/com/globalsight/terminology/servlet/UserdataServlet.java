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

import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.TermbaseException;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.GeneralException;

import org.dom4j.DocumentFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This servlet manages user data stored with termbases.  Client code
 * is in terminology/management/protocol.js and inputmodel_js.jsp.
 */
public class UserdataServlet
    extends HttpServlet
    implements WebAppConstants
{
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            UserdataServlet.class);

    public UserdataServlet()
        throws ServletException
    {
        super();
    }

    public void doGet (HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        doPost(p_request, p_response);
    }

    /**
     * Extracts and executes a request to the termbase's
     * UserDataManager to create, modify, or delete user objects.
     */
    public void doPost (HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/xml; charset=UTF-8");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        try
        {
            //  For multi company.
            String companyName = UserUtil.getCurrentCompanyName(request);
            if (companyName != null)
            {
                CompanyThreadLocal.getInstance().setValue(companyName);
            }
            
            HttpSession session = request.getSession();
            SessionManager sessionMgr = (SessionManager)session.getAttribute(
                WebAppConstants.SESSION_MANAGER);

            String userId = PageHandler.getUser(session).getUserId();

            IUserdataManager manager =
                (IUserdataManager)sessionMgr.getAttribute(TERMBASE_USERDATA);

            String action = (String)request.getParameter(TERMBASE_ACTION);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Userdata action=" + action);
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

            String xml = readBody(request);

            XmlParser parser = null;
            Document dom;

            try
            {
                parser = XmlParser.hire();
                dom = parser.parseXml(xml);
            }
            finally
            {
                XmlParser.fire(parser);
                parser = null;
            }

            // <userdatarequest><action><type><username><name><value></>
            // DOM4J uses Jaxen which prints entities surrounded by spaces
            // String type = dom.valueOf("//type");
            // String user = dom.valueOf("//username");
            // String name = dom.valueOf("//name");
            // String value = dom.valueOf("//value");
            String type = EntryUtils.getInnerText(
                (Element)dom.selectSingleNode("//type"));
            String user = EntryUtils.getInnerText(
                (Element)dom.selectSingleNode("//username"));
            String name = EntryUtils.getInnerText(
                (Element)dom.selectSingleNode("//name"));
            String value = EntryUtils.getInnerText(
                (Element)dom.selectSingleNode("//value"));

            if (action.equals(TERMBASE_ACTION_LOAD_OBJECT))
            {
                String result = manager.getObject(mapType(type), user, name);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Load object value = `" + value + "'");
                }

                returnXml(response, result);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_CREATE_OBJECT))
            {
                manager.createObject(mapType(type), user, name, value);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Create object value = `" + value + "'");
                }

                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_MODIFY_OBJECT))
            {
                manager.modifyObject(mapType(type), user, name, value);

                if (CATEGORY.isDebugEnabled())
                {
                    CATEGORY.debug("Modify object value = `" + value + "'");
                }

                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_REMOVE_OBJECT))
            {
                manager.deleteObject(mapType(type), user, name);

                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_MAKE_DEFAULT_OBJECT))
            {
                manager.makeDefaultObject(mapType(type), user, name);

                returnOk(response);
                return;
            }
            else if (action.equals(TERMBASE_ACTION_UNSET_DEFAULT_OBJECT))
            {
                manager.unsetDefaultObject(mapType(type), user);

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

    private int mapType(String p_type)
    {
        return Integer.parseInt(p_type);
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
            // Try reading input as byte array and converting to unicode.
            // Don't use request.getReader().
            ServletInputStream stream = request.getInputStream();
            byte[] temp = new byte[1024];
            int len;
            // TODO/FIXME: Attempt to read an entire line of data so
            // the Unicode conversion doesn't fail on incomplete UTF-8
            // data. Problem is what is posted is a serialized XML
            // object that most likely doesn't contain line breaks...
            while ((len = stream.readLine(temp, 0, 1024)) != -1)
            {
                String chars = new String(temp, 0, len, "UTF-8");
                result.append(chars);
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
