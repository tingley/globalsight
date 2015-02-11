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

package com.globalsight.everest.webapp.pagehandler.edit.sourcepage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.online.OnlineEditorException;
import com.globalsight.everest.edit.online.PageInfo;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * sourcepage.EditorPageHandler shows the source page editor.
 * </p>
 */
public class EditorPageHandler extends PageHandler implements EditorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EditorPageHandler.class);

    //
    // Constructor
    //
    public EditorPageHandler()
    {
        super();
    }

    //
    // Interface Methods: PageHandler
    //

    /**
     * Prepares the EditorState object that all invocations of this PageHandler
     * require.
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        EditorState state = (EditorState) sessionMgr
                .getAttribute(WebAppConstants.EDITORSTATE);

        if (state == null)
        {
            state = new EditorState();

            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);

            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);
        }

        String srcPageId = (String) p_request
                .getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String jobId = (String) p_request.getParameter(WebAppConstants.JOB_ID);

        // Get user object for the person who has logged in.
        User user = TaskHelper.getUser(session);

        // Must get called from Job Details (Admin or PM)
        if (jobId != null && srcPageId != null)
        {
            initializeFromJob(state, p_request, jobId, srcPageId, uiLocale,
                    user);

            initState(state, session);
        }

        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context, state);
    }

    //
    // Private Methods
    //

    /**
     * Executes all actions sent in from the UI and updates the EditorState to
     * have correct data for the JSPs.
     */
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context, EditorState p_state)
            throws ServletException, IOException, EnvoyServletException
    {
        String action = (String) p_request.getParameter("action");

        if (action != null)
        {
            if (action.equals("load"))
            {
                String gxml = loadSourcePage(p_state);

                p_response.setContentType("text/xml;charset=UTF-8");
                p_response.setHeader("Pragma", "No-cache");
                p_response.setHeader("Cache-Control", "no-cache");
                p_response.setDateHeader("Expires", 0);

                returnXml(p_response, gxml);

                return;
            }
            else if (action.equals("validate"))
            {
                String result = validateSourcePage(p_request, p_state);

                p_response.setContentType("text/xml;charset=UTF-8");
                p_response.setHeader("Pragma", "No-cache");
                p_response.setHeader("Cache-Control", "no-cache");
                p_response.setDateHeader("Expires", 0);

                returnXml(p_response, result);

                return;
            }
            else if (action.equals("preview"))
            {
                String result = previewPage(p_request, p_state);

                p_response.setContentType("text/xml;charset=UTF-8");
                p_response.setHeader("Pragma", "No-cache");
                p_response.setHeader("Cache-Control", "no-cache");
                p_response.setDateHeader("Expires", 0);

                returnXml(p_response, result);

                return;
            }
            else if (action.equals("save"))
            {
                String result = updateSourcePage(p_request, p_state);

                p_response.setContentType("text/xml;charset=UTF-8");
                p_response.setHeader("Pragma", "No-cache");
                p_response.setHeader("Cache-Control", "no-cache");
                p_response.setDateHeader("Expires", 0);

                returnXml(p_response, result);

                return;
            }
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    //
    // Initialization logic for jobs
    //

    /**
     * Initializes editor state from a job, i.e. when the editor is opened by an
     * Admin or PM.
     */
    private void initializeFromJob(EditorState p_state,
            HttpServletRequest p_request, String p_jobId, String p_srcPageId,
            Locale p_uiLocale, User p_user) throws EnvoyServletException
    {
        p_state.setUserIsPm(true);
        HttpSession session = p_request.getSession();
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);

        // Reset all options because the state may be inherited from a
        // previous page.
        EditorHelper.initEditorOptions(p_state, p_request.getSession());

        // Initializes pages, target locales, excluded items, and termbases
        EditorHelper.initializeFromJob(p_state, p_jobId, p_srcPageId,
                p_uiLocale, p_user.getUserId(), perms);

        setCurrentPage(p_state, p_srcPageId);

        // When coming from job page, page is read/write .
        p_state.setReadOnly(false);

        // N/A Snippets can not be edited.
        p_state.setAllowEditSnippets(false);

        // Indicate that source page editor is in 'edit' mode.
        p_state.setEditorMode();
    }

    private void initState(EditorState p_state, HttpSession p_session)
            throws EnvoyServletException
    {
        PageInfo info = EditorHelper.getPageInfo(p_state,
                p_state.getSourcePageId());

        p_state.setPageInfo(info);
        p_state.setPageFormat(info.getPageFormat());

        p_state.clearSourcePageHtml();
        p_state.setTargetPageHtml(null);
    }

    /**
     * Scans the pagelist for the first pair having the right source page id and
     * set the pair to be shown first.
     */
    private void setCurrentPage(EditorState p_state, String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_offset == 1);
        p_state.setIsLastPage(pages.size() == i_offset);
    }

    public String loadSourcePage(EditorState p_state)
    {
        try
        {
            return EditorHelper.getSourcePageGxml(p_state);
        }
        catch (OnlineEditorException ex)
        {
            // Be silent about expected exceptions.
            if (!OnlineEditorException.MSG_SOURCEPAGE_NOT_EDITABLE.equals(ex
                    .getMessageKey()))
            {
                CATEGORY.error("error when loading source page GXML", ex);
            }

            return "<exception>" + EditUtil.encodeXmlEntities(ex.getMessage())
                    + "</exception>";
        }
        catch (Throwable ex)
        {
            CATEGORY.error("error when loading source page GXML", ex);

            return "<exception>" + EditUtil.encodeXmlEntities(ex.getMessage())
                    + "</exception>";
        }
    }

    public String validateSourcePage(HttpServletRequest p_request,
            EditorState p_state)
    {
        try
        {
            String gxml = readInputStream(p_request.getInputStream());

            ArrayList errors = EditorHelper.validateSourcePageGxml(p_state,
                    gxml);

            if (errors != null)
            {
                return makeErrorList(errors);
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error(
                    "error validating GXML for source page "
                            + p_state.getSourcePageId(), ex);

            return "<exception>"
                    + EditUtil.encodeXmlEntities(ex.getMessage() + "@@@@@"
                            + GeneralException.getStackTraceString(ex))
                    + "</exception>";
        }

        return "<ok></ok>";
    }

    public String previewPage(HttpServletRequest p_request, EditorState p_state)
    {
        String locale = p_request.getParameter("locale");

        try
        {
            String gxml = readInputStream(p_request.getInputStream());

            String result = EditorHelper.getGxmlPreview(p_state, gxml, locale);

            return "<preview>" + EditUtil.encodeXmlEntities(result)
                    + "</preview>";
        }
        catch (Throwable ex)
        {
            CATEGORY.error(
                    "error previewing GXML for source page "
                            + p_state.getSourcePageId() + " in locale "
                            + locale, ex);

            return "<exception>"
                    + EditUtil.encodeXmlEntities(ex.getMessage() + "@@@@@"
                            + GeneralException.getStackTraceString(ex))
                    + "</exception>";
        }
    }

    public String updateSourcePage(HttpServletRequest p_request,
            EditorState p_state)
    {
        try
        {
            String gxml = readInputStream(p_request.getInputStream());

            ArrayList errors = EditorHelper.updateSourcePageGxml(p_state, gxml);

            if (errors != null)
            {
                return makeErrorList(errors);
            }
        }
        catch (Throwable ex)
        {
            CATEGORY.error(
                    "error when updating source page "
                            + p_state.getSourcePageId(), ex);

            return "<exception>"
                    + EditUtil.encodeXmlEntities(ex.getMessage() + "@@@@@"
                            + GeneralException.getStackTraceString(ex))
                    + "</exception>";
        }

        return "<ok></ok>";
    }

    private String makeErrorList(ArrayList p_errors)
    {
        StringBuffer result = new StringBuffer("<errors>");

        for (int i = 0, max = p_errors.size(); i < max; i++)
        {
            String error = (String) p_errors.get(i);

            result.append("<error>");
            result.append(EditUtil.encodeXmlEntities(error));
            result.append("</error>");
        }

        result.append("</errors>");

        return result.toString();
    }

    public String readInputStream(javax.servlet.ServletInputStream p_stream)
            throws IOException
    {
        StringBuffer result = new StringBuffer();

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p_stream, "UTF-8"));

        String line;
        while ((line = reader.readLine()) != null)
        {
            result.append(line).append('\n');
        }

        return result.toString();
    }

    /** Request succeeded with result. */
    public void returnXml(HttpServletResponse p_response, String p_xml)
            throws IOException
    {
        p_response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = p_response.getWriter();
        out.print(p_xml);
        out.flush();
    }
}
