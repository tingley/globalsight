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
package com.globalsight.everest.webapp.servlet;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.edit.online.RenderingOptions;
import com.globalsight.everest.page.TemplateException;
import com.globalsight.everest.page.TemplateManager;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.snippet.Snippet;
import com.globalsight.everest.snippet.SnippetException;
import com.globalsight.everest.snippet.SnippetLibrary;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;

import org.dom4j.DocumentHelper;
import org.dom4j.Document;
import org.dom4j.Node;

import java.io.*;
import java.util.*;

import java.rmi.RemoteException;

/**
 * Access to snippet library and template manipulation.
 */
public class SnippetLibraryServlet
    extends HttpServlet
    implements WebAppConstants, UIConstants
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SnippetLibraryServlet.class);

    private static SnippetLibrary  m_library = null;
    private static TemplateManager m_templateManager = null;

    public SnippetLibraryServlet ()
        throws ServletException
    {
        super();

        try
        {
            m_library = ServerProxy.getSnippetLibrary();
            m_templateManager = ServerProxy.getTemplateManager();
        }
        catch (GeneralException ex)
        {
            CATEGORY.error("could not initialize remote snippet library", ex);
        }
    }

    public void doGet (HttpServletRequest p_request,
        HttpServletResponse p_response)
        throws IOException, ServletException
    {
        doPost(p_request, p_response);
    }

    /**
     * Extracts and executes a request for the SnippetLibrary to
     * create, modify, or delete snippets.
     */
    public void doPost (HttpServletRequest request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        try
        {
            String result = null;

            HttpSession session = request.getSession();
            String userId = PageHandler.getUser(session).getUserId();

            SessionManager sessionMgr = (SessionManager)session.getAttribute(
                WebAppConstants.SESSION_MANAGER);

            EditorState state = (EditorState)sessionMgr.getAttribute(
                WebAppConstants.EDITORSTATE);

            response.setContentType("text/xml; charset=UTF-8");
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            Document dom = DocumentHelper.parseText(readBody(request));

            Node commandNode = dom.selectSingleNode("/*/request");
            if (commandNode == null)
            {
                returnBadRequest(response);
            }

            String command = commandNode.getText();
            if (command == null || command.length() == 0)
            {
                returnBadRequest(response);
            }

            if (command.equals(SNIPPET_ACTION_GETPAGE))
            {
                // retrieve page interpreted in a locale
                result = getPage(dom, state, request.getSession());
            }
            else if (command.equals(SNIPPET_ACTION_GETSNIPPET))
            {
                // retrieve the snippet for a specific position
                // (by name + locale + version)
                result = getSnippet(dom);
            }
            else if (command.equals(SNIPPET_ACTION_GETSNIPPETS))
            {
                // retrieve the snippets for a position (by name + locale)
                result = getSnippets(dom);
            }
            else if (command.equals(SNIPPET_ACTION_CREATESNIPPET))
            {
                result = createSnippet(userId, dom);
            }
            else if (command.equals(SNIPPET_ACTION_CREATESNIPPETGETSNIPPET))
            {
                result = createSnippetGetSnippet(userId, dom);
            }
            else if (command.equals(SNIPPET_ACTION_MODIFYSNIPPET))
            {
                result = modifySnippet(userId, dom);
            }
            else if (command.equals(SNIPPET_ACTION_MODIFYSNIPPETGETSNIPPET))
            {
                result = modifySnippetGetSnippet(userId, dom);
            }
            else if (command.equals(SNIPPET_ACTION_REMOVESNIPPET))
            {
                // deletes a snippet.
                result = removeSnippet(userId, dom);
            }
            else if (command.equals(SNIPPET_ACTION_ADDSNIPPETGETPAGE))
            {
                // add a snippet and recompute the page
                result = addSnippetGetPage(userId, dom, state,
                    request.getSession());
            }
            else if (command.equals(SNIPPET_ACTION_MODIFYSNIPPETGETPAGE))
            {
                // modify a snippet and recompute the page
                result = modifySnippetGetPage(userId, dom, state,
                    request.getSession());
            }
            else if (command.equals(SNIPPET_ACTION_REMOVESNIPPETGETPAGE))
            {
                // remove a snippet and recompute the page
                result = removeSnippetGetPage(userId, dom, state,
                    request.getSession());
            }
            else if (command.equals(SNIPPET_ACTION_DELETECONTENTGETPAGE))
            {
                // delete content and recompute the page
                result = deleteContentGetPage(userId, dom, state,
                    request.getSession());
            }
            else if (command.equals(SNIPPET_ACTION_UNDELETECONTENTGETPAGE))
            {
                // un-delete content and recompute the page
                result = undeleteContentGetPage(userId, dom, state,
                    request.getSession());
            }
            // Mon Jul 11 21:32:17 2005 Amb6.6 source page editing additions
            else if (command.equals(SNIPPET_ACTION_GETGENERICNAMES))
            {
                result = getGenericSnippetNames();
            }
            else if (command.equals(SNIPPET_ACTION_GETSNIPPETSBYLOCALE))
            {
                result = getSnippetsByLocale(dom);
            }
            else if (command.equals(SNIPPET_ACTION_GETGENERICSNIPPET))
            {
                result = getGenericSnippet(dom);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("action=" + command + " result=\n" + result);
            }

            if (result == null)
            {
                returnBadRequest(response);
            }
            else
            {
                returnXml(response, result);
            }
        }
        catch (SnippetException ex)
        {
            CATEGORY.warn(ex.getMessage(), ex);

            returnError(response, ex);
        }
        catch (Throwable ex)
        {
            CATEGORY.warn(ex.getMessage(), ex);

            returnError(response, ex);
        }
    }


    /** invalid arguments */
    public void returnBadRequest(HttpServletResponse response)
        throws IOException
    {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "bad request");
    }


    /** request failed */
    public void returnError(HttpServletResponse response,
        SnippetException exception)
        throws IOException
    {
        // should be SC_OK + <exception>message</exception>
        //response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        // "<exception>" + exception.getMessage() + "@@@@@" +
        //    exception.getStackTrace() + "</exception>");
        returnXml(response, "<exception>" +
            EditUtil.encodeXmlEntities(exception.getMessage()) + "@@@@@" +
            EditUtil.encodeXmlEntities(exception.getStackTraceString()) +
            "</exception>");
    }


    /** request failed */
    public void returnError(HttpServletResponse response,
        Throwable exception)
        throws IOException
    {
        returnXml(response, "<exception>" +
            EditUtil.encodeXmlEntities(exception.getMessage()) + "@@@@@" +
            EditUtil.encodeXmlEntities(
                GeneralException.getStackTraceString(exception)) + "</exception>");
    }


    /**
     * Request succeeded without result (SC_NO_CONTENT, not supported
     * by Msxml2.XMLHTTP.
     */
    public void returnOk(HttpServletResponse response)
    {
        response.setStatus(HttpServletResponse.SC_OK);
    }


    /** request succeeded with result */
    public void returnXml(HttpServletResponse response, String xml)
        throws IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        out.print(xml);
        out.flush();
    }

    /** action not implemented */
    public void returnNotImplemented(HttpServletResponse response)
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
    public String readBody(HttpServletRequest request)
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
            throw ex;
        }

        return result.toString();
    }


    //
    // Helper Methods
    //

    private String getPage(Document p_request, EditorState p_state,
        HttpSession p_session)
        throws Exception
    {
        long pageId = 0L;
        String locale = null;
        String role = null;
        int viewMode = 0;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            pageId = Long.parseLong(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            role = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            viewMode = Integer.parseInt(node.getText());
        }

        if (pageId == 0L || locale == null || role == null || viewMode == 0)
        {
            CATEGORY.error("Invalid args, expected 4 - " + p_request.asXML());
            throw new Exception("invalid argument");
        }

        return getPage(p_session, p_state, viewMode, false);
    }

    /**
     * Returns the page according to the user's view, state and permissions.
     */
    private String getPage(HttpSession p_session, EditorState p_state,
        int viewMode, boolean p_dirtyTemplate)
        throws Exception
    {
        SessionManager mgr = (SessionManager)p_session.getAttribute(
            WebAppConstants.SESSION_MANAGER);
        PermissionSet permSet = (PermissionSet) p_session.getAttribute(
            WebAppConstants.PERMISSIONS);
        p_state.setRenderingOptions(
            new RenderingOptions(UIConstants.UIMODE_SNIPPETS, viewMode,
                                 UIConstants.EDITMODE_DEFAULT, permSet));

        String html = EditorHelper.getTargetPageView(p_state, p_dirtyTemplate);

        return "<pagedata><page>" + EditUtil.encodeXmlEntities(html) +
            "</page></pagedata>";
    }

    /**
     * For the source page editor - retrieve a generic snippet.
     */
    private String getGenericSnippet(Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        if (name == null)
        {
            CATEGORY.error("Invalid args, expected 1 - " + p_request.asXML());
            throw new Exception("getGenericSnippet: invalid arguments");
        }

        Snippet snippet = m_library.getSnippet(name, (GlobalSightLocale)null, 0);

        if (snippet != null)
        {
            return snippetToXml(snippet);
        }

        return null;
    }

    /**
     * For the Snippet Library Dialog - edit this specific snippet.
     */
    private String getSnippet(Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String locale = null;
        String version = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            version = node.getText();
        }

        if (name == null || locale == null || version == null)
        {
            CATEGORY.error("Invalid args, expected 3 - " + p_request.asXML());
            throw new Exception("getSnippet: invalid arguments");
        }

        Snippet snippet = m_library.getSnippet(name, locale,
            Long.parseLong(version));

        return snippetToXml(snippet);
    }


    /**
     * For the Snippet Select Dialog: creates a snippet in a locale as
     * a new version and returns a list of all snippets with the
     * snippet's name and in its locale.
     */
    private String getSnippets(Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String locale = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        if (name == null || locale == null)
        {
            CATEGORY.error("Invalid args, expected 2 - " + p_request.asXML());
            throw new Exception("getSnippets: invalid arguments");
        }

        ArrayList snippets = m_library.getSnippets(name, locale);

        return snippetsToXml(snippets);
    }


    /**
     * For the Snippet Select Dialog: creates a snippet in a locale as
     * a new version and returns a list of all snippets with the
     * snippet's name and in its locale.
     */
    private String createSnippet(String p_user, Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String desc = null;
        String locale = null;
        String id = null;
        String value = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            desc = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            id = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            value = node.getText();
        }

        if (name == null || desc == null || locale == null ||
            id == null || value == null)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("createSnippet: invalid arguments");
        }

        // add the specified snippet and validate its content against
        // its generic snippet
        m_library.addSnippet(p_user, name, desc, locale, id, value, true);

        ArrayList snippets = m_library.getSnippets(name, locale);

        return snippetsToXml(snippets);
    }

    private String createSnippetGetSnippet(String p_user, Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String desc = null;
        String locale = null;
        String id = null;
        String value = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            desc = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            id = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            value = node.getText();
        }

        if (name == null || desc == null || locale == null ||
            id == null || value == null)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("createSnippet: invalid arguments");
        }

        // add the specified snippet and validate its content against
        // its generic snippet
        Snippet snippet = m_library.addSnippet(
            p_user, name, desc, locale, id, value, true);

        return snippetToXml(snippet);
    }

    /**
     * For the Snippet Select Dialog: modifies a snippet in a locale
     * and returns a list of all snippets with the snippet's name and
     * in its locale.
     */
    private String modifySnippet(String p_user, Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String desc = null;
        String locale = null;
        String id = null;
        String value = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            desc = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            id = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            value = node.getText();
        }

        if (name == null || desc == null || locale == null ||
            id == null || value == null)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("modifySnippet: invalid argument");
        }

        // modify the snippet and validate its content
        m_library.modifySnippet(p_user, name, desc, locale, id, value, true);

        ArrayList snippets = m_library.getSnippets(name, locale);

        return snippetsToXml(snippets);
    }

    /**
     * For source page editing: modifies a snippet and returns it.
     */
    private String modifySnippetGetSnippet(String p_user, Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String desc = null;
        String locale = null;
        String id = null;
        String value = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            desc = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            id = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            value = node.getText();
        }

        if (name == null || desc == null || locale == null ||
            id == null || value == null)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("modifySnippet: invalid argument");
        }

        // modify the snippet and validate its content
        Snippet snippet = m_library.modifySnippet(
            p_user, name, desc, locale, id, value, true);

        return snippetToXml(snippet);
    }

    /**
     * For the Snippet Select Dialog: removes a snippet in a locale
     * and returns a list of all snippets with the snippet's name and
     * in its locale.
     *
     * (Need a separate function "removeGenericSnippet" for admin.)
     */
    private String removeSnippet(String p_user, Document p_request)
        throws Exception, RemoteException, SnippetException
    {
        String name = null;
        String locale = null;
        String id = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            id = node.getText();
        }

        if (name == null || locale == null || id == null)
        {
            CATEGORY.error("Invalid args, expected 3 - " + p_request.asXML());
            throw new Exception("removeSnippet: invalid arguments");
        }

        m_library.removeSnippet(p_user, name, locale, id);

        ArrayList snippets = m_library.getSnippets(name, locale);

        return snippetsToXml(snippets);
    }

    /**
     * For the Snippet Select Dialog: modifysnippetgetpage
     */
    private String modifySnippetGetPage(String p_user, Document p_request,
        EditorState p_state, HttpSession p_session)
        throws Exception, RemoteException, SnippetException
    {
        long pageId = 0L;
        String locale = null;
        String role = null;
        int viewMode = 0;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            pageId = Long.parseLong(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            role = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            viewMode = Integer.parseInt(node.getText());
        }

        String name = null;
        String desc = null;
        String id = null;
        String value = null;

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[6]");
        if (node != null)
        {
            desc = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[7]");
        if (node != null)
        {
            id = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[8]");
        if (node != null)
        {
            value = node.getText();
        }

        if (pageId == 0L || locale == null || role == null || viewMode == 0 ||
            name == null || desc == null || id == null || value == null)
        {
            CATEGORY.error("Invalid args, expected 8 - " + p_request.asXML());
            throw new Exception("modifySnippetGetPage: invalid arguments");
        }

        // modify the specified snippet and validate the format
        m_library.modifySnippet(p_user, name, desc, locale, id, value, true);

        return getPage(p_session, p_state, viewMode, true);
    }


    /**
     * For the Snippet Select Dialog: addsnippetgetpage
     */
    private String addSnippetGetPage(String p_user, Document p_request,
        EditorState p_state, HttpSession p_session)
        throws RemoteException, SnippetException, TemplateException, Exception
    {
        long pageId = 0L;
        String locale = null;
        String role = null;
        int viewMode = 0;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            pageId = Long.parseLong(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            role = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            viewMode = Integer.parseInt(node.getText());
        }

        int position = 0;
        String name = null;
        String snippetLocale = null;
        long id = 0L;

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            position = Integer.parseInt(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[6]");
        if (node != null)
        {
            name = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[7]");
        if (node != null)
        {
            snippetLocale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[8]");
        if (node != null)
        {
            id = Long.parseLong(node.getText());
        }

        if (pageId == 0L || locale == null || role == null || viewMode == 0 ||
            position == 0 || name == null || snippetLocale == null || id == 0L)
        {
            CATEGORY.error("Invalid args, expected 8 - " + p_request.asXML());
            throw new Exception("addSnippetGetPage: invalid arguments");
        }

        m_templateManager.addSnippet(p_user, pageId, locale, position,
            name, snippetLocale, id);

        return getPage(p_session, p_state, viewMode, true);
    }

    /**
     * For the Snippet Select Dialog: removesnippetgetpage
     */
    private String removeSnippetGetPage(String p_user, Document p_request,
        EditorState p_state, HttpSession p_session)
        throws Exception, RemoteException, SnippetException
    {
        long pageId = 0L;
        String locale = null;
        String role = null;
        int viewMode = 0;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            pageId = Long.parseLong(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            role = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            viewMode = Integer.parseInt(node.getText());
        }

        int position = 0;

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            position = Integer.parseInt(node.getText());
        }

        if (pageId == 0L || locale == null || role == null || viewMode == 0 ||
            position == 0)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("removeSnippetGetPage: invalid arguments");
        }

        m_templateManager.deleteSnippet(p_user, pageId, locale, position);

        return getPage(p_session, p_state, viewMode, true);
    }

    /**
     * For the Snippet Select Dialog: deletecontentgetpage
     */
    private String deleteContentGetPage(String p_user, Document p_request,
        EditorState p_state, HttpSession p_session)
        throws Exception, RemoteException,
               SnippetException, TemplateException
    {
        long pageId = 0L;
        String locale = null;
        String role = null;
        int viewMode = 0;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            pageId = Long.parseLong(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            role = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            viewMode = Integer.parseInt(node.getText());
        }

        int position = 0;

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            position = Integer.parseInt(node.getText());
        }

        if (pageId == 0L || locale == null || role == null || viewMode == 0 ||
            position == 0)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("deleteContentGetPage: invalid arguments");
        }

        m_templateManager.deleteContent(p_user, pageId, locale, position);

        return getPage(p_session, p_state, viewMode, true);
    }


    /**
     * For the Snippet Select Dialog: undeletecontentgetpage
     */
    private String undeleteContentGetPage(String p_user, Document p_request,
        EditorState p_state, HttpSession p_session)
        throws Exception, RemoteException,
               SnippetException, TemplateException
    {
        long pageId = 0L;
        String locale = null;
        String role = null;
        int viewMode = 0;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            pageId = Long.parseLong(node.getText());
        }

        node = p_request.selectSingleNode("/*/arg[2]");
        if (node != null)
        {
            locale = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[3]");
        if (node != null)
        {
            role = node.getText();
        }

        node = p_request.selectSingleNode("/*/arg[4]");
        if (node != null)
        {
            viewMode = Integer.parseInt(node.getText());
        }

        int position = 0;

        node = p_request.selectSingleNode("/*/arg[5]");
        if (node != null)
        {
            position = Integer.parseInt(node.getText());
        }

        if (pageId == 0L || locale == null || role == null || viewMode == 0 ||
            position == 0)
        {
            CATEGORY.error("Invalid args, expected 5 - " + p_request.asXML());
            throw new Exception("undeleteContentGetPage: invalid arguments");
        }

        m_templateManager.undeleteContent(p_user, pageId, locale, position);

        return getPage(p_session, p_state, viewMode, true);
    }


    /**
     * Returns a list of names of generic snippets.
     */
    private String getGenericSnippetNames()
        throws Exception
    {
        ArrayList result = m_library.getGenericSnippetNames();

        return snippetNamesToXml(result);
    }

    /**
     * Returns a list of snippets that can be used in the specified
     * locale, including generic snippets (sorted by name+id).
     */
    private String getSnippetsByLocale(Document p_request)
        throws Exception
    {
        String locale = null;
        Node node;

        node = p_request.selectSingleNode("/*/arg[1]");
        if (node != null)
        {
            locale = node.getText();
        }

        if (locale == null)
        {
            CATEGORY.error("Invalid args, expected 1 - " + p_request.asXML());
            throw new Exception("getSnippetsByLocale: invalid arguments");
        }

        ArrayList result = m_library.getSnippetsByLocale(locale);

        return snippetsToXml(result);
    }

    private String snippetNamesToXml(ArrayList p_names)
    {
        StringBuffer result = new StringBuffer();

        result.append("<names>");

        for (int i = 0, max = p_names.size(); i < max; ++i)
        {
            String name = (String)p_names.get(i);

            result.append("<name>").append(name).append("</name>");
        }

        result.append("</names>");

        return result.toString();
    }

    private String snippetsToXml(ArrayList p_snippets)
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<snippets>");

        if (p_snippets != null && p_snippets.size() > 0)
        {
            for (int i = 0; i < p_snippets.size(); ++i)
            {
                Snippet s = (Snippet)p_snippets.get(i);

                result.append(snippetToXml(s));
            }
        }

        result.append("</snippets>");

        return result.toString();
    }

    private String snippetToXml(Snippet p_snippet)
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<snippet>");
        result.append("<name>");
        result.append(EditUtil.encodeXmlEntities(p_snippet.getName()));
        result.append("</name>");
        result.append("<description>");
        result.append(EditUtil.encodeXmlEntities(p_snippet.getDescription()));
        result.append("</description>");
        result.append("<locale>");
        GlobalSightLocale gslocale = p_snippet.getLocale();
        result.append(gslocale != null ? gslocale.toString() : "");
        result.append("</locale>");
        result.append("<displayLocale>");
        result.append(gslocale != null ? gslocale.getDisplayName() : "");
        result.append("</displayLocale>");
        result.append("<id>");
        result.append(p_snippet.getId());
        result.append("</id>");
        result.append("<value>");
        result.append(EditUtil.encodeXmlEntities(p_snippet.getContent()));
        result.append("</value>");
        result.append("</snippet>");

        return result.toString();
    }
}
