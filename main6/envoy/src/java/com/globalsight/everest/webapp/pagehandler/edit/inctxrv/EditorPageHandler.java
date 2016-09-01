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
package com.globalsight.everest.webapp.pagehandler.edit.inctxrv;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.config.UserParamNames;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.online.CommentThreadView;
import com.globalsight.everest.edit.online.CommentView;
import com.globalsight.everest.edit.online.OnlineEditorConstants;
import com.globalsight.everest.edit.online.PageInfo;
import com.globalsight.everest.edit.online.PaginateInfo;
import com.globalsight.everest.edit.online.RenderingOptions;
import com.globalsight.everest.edit.online.SegmentFilter;
import com.globalsight.everest.edit.online.SegmentMatchResult;
import com.globalsight.everest.edit.online.SegmentView;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineHelper;
import com.globalsight.everest.webapp.pagehandler.edit.online.PreviewPageHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF.PreviewPDFHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.webservices.AmbassadorUtil;

/**
 * <p>
 * EditorPageHandler is responsible for:
 * </p>
 * <ol>
 * <li>Displaying the editor screen with both source and target page.</li>
 * <li>Showing the Segment Editor.</li>
 * </ol>
 */
public class EditorPageHandler extends PageHandler implements EditorConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(EditorPageHandler.class);

    private static int DEFAULT_VIEWMODE_IF_NO_PREVIEW = VIEWMODE_TEXT;

    /**
     * Determines whether PMs can edit all target pages.
     * 
     * WARNING: Care must be taken outside of the system - via phone or email -
     * to prevent multiple people from editing the same target page at the same
     * time.
     */
    static public boolean s_pmCanEditTargetPages = false;

    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_pmCanEditTargetPages = sc.getBooleanParameter("editalltargetpages.allowed");
        }
        catch (Throwable e)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Error when get 'editalltargetpages.allowed' configuration.");
            }
        }
    }

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
     * require. (Almost) All Main Editor pages (me_xx.jsp) go through this
     * handler.
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

        String action = p_request.getParameter("action");

        if (action != null
                && ("getSourceSegment".equals(action) || "getTargetSegment"
                        .equals(action)))
        {
            long tuId = Long.parseLong(p_request.getParameter("tuid"));
            long tuvId = Long.parseLong(p_request.getParameter("tuvid"));
            String subId = p_request.getParameter("subid");
            long tpid = state.getTargetPageId();
            boolean fromTargetList = "getSourceSegment".equals(action);

            SegmentView sv = state.getEditorManager().getSegmentView(tuId,
                    tuvId, subId, tpid, state.getSourceLocale().getId(),
                    state.getTargetLocale().getId(), state.getTmNames(),
                    state.getDefaultTermbaseName(), fromTargetList);
            String segment = null;
            if (fromTargetList)
            {
                segment = sv.getSourceSegment().getTextValue().trim()
                        + "_globalsight_sep_"
                        + sv.getTargetSegment().getTextValue().trim();
            }
            else
            {
                segment = sv.getTargetSegment().getTextValue();
            }
            segment = removeSpecialChars(segment);
            p_response.setContentType("text/html;charset=UTF-8");
            p_response.getWriter().write(segment);
            return;
        }

        String srcPageId = p_request
                .getParameter(WebAppConstants.SOURCE_PAGE_ID);
        String trgPageId = p_request
                .getParameter(WebAppConstants.TARGET_PAGE_ID);
        String jobId = p_request.getParameter(WebAppConstants.JOB_ID);
        String taskId = p_request.getParameter(WebAppConstants.TASK_ID);
        String dataFormat = p_request.getParameter("dataFormat");
        // Get user object for the person who has logged in.
        User user = TaskHelper.getUser(session);

        if (state != null)
        {
            if (state.getTmProfile() == null && taskId != null)
            {
                TranslationMemoryProfile tmProfile = TaskHelper
                        .getTask(Integer.parseInt(taskId)).getWorkflow()
                        .getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                state.setTmProfile(tmProfile);
                sessionMgr.setAttribute("currentTmProfile", tmProfile);
            }

            if (state.getTmProfile() == null && jobId != null)
            {
                TranslationMemoryProfile tmProfile = null;
                try
                {
                    tmProfile = ServerProxy.getJobHandler()
                            .getJobById(Long.parseLong(jobId)).getL10nProfile()
                            .getTranslationMemoryProfile();
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
                state.setTmProfile(tmProfile);
                sessionMgr.setAttribute("currentTmProfile", tmProfile);
            }

            if (state.getTmProfile() == null)
            {
                TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) sessionMgr
                        .getAttribute("currentTmProfile");

                if (tmProfile != null)
                {
                    state.setTmProfile(tmProfile);
                }
            }
        }

        Boolean assigneeValue = (Boolean) TaskHelper.retrieveObject(session,
                WebAppConstants.IS_ASSIGNEE);
        boolean isAssignee = assigneeValue == null ? true : assigneeValue
                .booleanValue();
        String pageSearchText = p_request
                .getParameter(JobManagementHandler.PAGE_SEARCH_TEXT);
        if (pageSearchText != null)
        {
            pageSearchText = URLDecoder.decode(pageSearchText, "UTF-8");
            sessionMgr.setAttribute(JobManagementHandler.PAGE_SEARCH_TEXT,
                    pageSearchText);
        }
        // this is ajax respose to json back;
        if (StringUtils.isNotBlank(dataFormat) && null != state)
        {
            renderJson(p_request, p_response, state, isAssignee);
            return;
        }
        
        // GBS-4281 show segment details
        String value = p_request.getParameter("param");
        if (value != null)
        {
            ResourceBundle bundle = PageHandler.getBundle(session);
            SegmentView view;
            String param[] = value.split("&");
            String tuid[] = param[0].split("=");
            String tuvid[] = param[1].split("=");
            String subid[] = param[2].split("=");
            long tuId = Long.valueOf(tuid[1]).longValue();
            long tuvId = Long.valueOf(tuvid[1]).longValue();
            long subId = Long.valueOf(subid[1]).longValue();
            Long targetPageId = state.getTargetPageId();
            long sourceLocaleId = state.getSourceLocale().getId();
            long targetLocaleId = state.getTargetLocale().getId();

            view = EditorHelper.getSegmentView(state, tuId, tuvId, subId,
                    targetPageId.longValue(), sourceLocaleId, targetLocaleId);

            JSONObject json = new JSONObject();
            ServletOutputStream out = p_response.getOutputStream();
            try
            {
                json.put("str_segmentFormat", view.getDataType());
                json.put("str_segmentType", view.getItemType());
                json.put("str_wordCount", String.valueOf(view.getWordCount()));
                String str_sid = view.getTargetTuv().getSid();
                if (str_sid == null || str_sid.trim().length() == 0)
                {
                    str_sid = "N/A";
                }
                json.put("str_sid", str_sid);
                String str_lastModifyUser = view.getTargetTuv()
                        .getLastModifiedUser();
                if (str_lastModifyUser == null
                        || str_lastModifyUser.equalsIgnoreCase("xlf")
                        || str_lastModifyUser.equalsIgnoreCase("Xliff"))
                {
                    str_lastModifyUser = "N/A";
                }
                json.put("str_lastModifyUser", str_lastModifyUser);
                try
                {
                    OnlineHelper helper = new OnlineHelper();
                    String str_sourceSegment = GxmlUtil.getInnerXml(view
                            .getSourceSegment());
                    String str_dataType = view.getDataType();

                    helper.setInputSegment(str_sourceSegment, "", str_dataType);

                    if (EditorConstants.PTAGS_VERBOSE.equals(state
                            .getPTagFormat()))
                    {
                        helper.getVerbose();
                    }
                    else
                    {
                        helper.getCompact();
                    }

                    String str_segementPtag = helper
                            .getPtagToNativeMappingTable();
                    if (StringUtil.isEmpty(str_segementPtag))
                    {
                        str_segementPtag = "N/A";
                    }
                    else
                    {
                        str_segementPtag = str_segementPtag.replace("<TR>", "<TR valign=top>").replace("<TD", "<TD noWrap"); 
                        str_segementPtag = str_segementPtag.replace("<tr>", "<TR valign=top>").replace("<td", "<TD noWrap");     
                    }
                    
                    String sourceSegment = getHtmlSegment(str_sourceSegment,str_dataType,state);
                    String sourceDIR = getDIR(state,str_sourceSegment,true);
                    json.put("str_segementPtag", str_segementPtag);
                    json.put("m_sourceSegment", sourceSegment);
                    json.put("m_sourceDIR", sourceDIR);
                    List<SegmentMatchResult> list = view.getTmMatchResults();
                    StringBuffer tmMatchesStr = new StringBuffer();
                    StringBuffer mtTranslationStr = new StringBuffer();
                    if (list.size()>0)
                    {
                        SegmentMatchResult matchResult = null;
                        int j = 1;
                        for (int i=0;i<list.size();i++)
                        {
                            matchResult = list.get(i);
                            String targetDIR = getDIR(state,matchResult.getMatchContent(),false);
                            if (matchResult.getTmName().endsWith("_MT"))
                            {
                                long sourcePageId = state.getSourcePageId();
                                MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                                      .getMtProfileBySourcePageId(sourcePageId,
                                              state.getTargetLocale());
                                if (mtProfile !=null)
                                {
                                    mtTranslationStr = mtTranslationStr.append(
                                            "<table><tr class=\"standardText\"><td style=\"width:120px\"><B>Target Match Type:</B></td>"
                                                    +"<td>"+matchResult.getMatchType()+"</td></tr>"
                                                    +"<tr class=\"standardText\"><td><B>"+bundle.getString("lb_tm_mt_engine")+":</B></td>"
                                                    +"<td>"+mtProfile.getMtEngine()+"</td></tr>"
                                                    +"<tr class=\"standardText\"><td><B>Target Match %:</B></td>"
                                                    +"<td>"+Math.round(matchResult.getMatchPercentage())+"%</td></tr>"
                                                    +"<tr class=\"standardText\"><td ><B>Source:</B></td>"
                                                    +"<td "+sourceDIR+">"+sourceSegment+"</td></tr>"
                                                    +"<tr class=\"standardText\"><td><B>Target:</B></td>"
                                                    +"<td "+targetDIR+">"+getHtmlSegment(matchResult.getMatchContent(),str_dataType,state)+"</td><tr>"
                                                    +"</table>");
                                }
                            }
                            else
                            {
                                tmMatchesStr = tmMatchesStr.append(
                                        "<table><tr class=\"standardText\"><td style=\"width:120px\"><B>Match&nbsp;&nbsp;"+j+":</B></td>"
                                              +"<td>"+"("+Math.round(matchResult.getMatchPercentage())+"%&nbsp;&nbsp;/&nbsp;&nbsp;"+ matchResult.getTmName()+")</td></tr>"
                                              +"<tr class=\"standardText\"><td><B>Target Match Type:</B></td>"
                                              +"<td>"+matchResult.getMatchType()+"</td></tr>"
                                              +"<tr class=\"standardText\"><td><B>Source:</B></td>"
                                              +"<td "+sourceDIR+">"+getHtmlSegment(matchResult.getMatchContentSource(),str_dataType,state)
                                              +"<tr class=\"standardText\"><td><B>Target:</B></td>"
                                              +"<td "+targetDIR+">"+getHtmlSegment(matchResult.getMatchContent(),str_dataType,state)+"</td></tr>"
                                              +"<tr height=\"10px\"><td></td></tr></table>");
                                j++;
                            }
                        }
                    }
                    json.put("mt_match", mtTranslationStr.toString());
                    json.put("tm_match", tmMatchesStr.toString());
                }
                catch (Exception e1)
                {
                    CATEGORY.error("Get segement tag information. ", e1);
                    throw new EnvoyServletException(e1);
                }
                out.write(json.toString().getBytes("UTF-8"));
            }
            catch (JSONException e)
            {
                CATEGORY.error("Get segement detail. ", e);
                throw new EnvoyServletException(e);
            }
            return;
        }

        // Reach here via link from "getInContextReviewLink()" API.
        try {
            String secret = p_request.getParameter("secret");
            if (secret != null)
            {
                secret = AmbassadorUtil.getDecryptionString(secret);
                HashMap paramsInSecret = InContextReviewHelper.parseSecret(secret);
                taskId = (String) paramsInSecret.get("taskId");
                Task tsk = ServerProxy.getTaskManager().getTask(Long.valueOf(taskId));
                TargetPage firstTp = (TargetPage) tsk.getWorkflow()
                        .getTargetPages(PrimaryFile.EXTRACTED_FILE).get(0);
                SourcePage firstSp = firstTp.getSourcePage();
                srcPageId = String.valueOf(firstSp.getId());
                trgPageId = String.valueOf(firstTp.getId());
            }
        }
        catch (Exception e)
        {
        }
        // Decide from which screen we've been called.

        // From Activity Details (Translator opening pages read-write or
        // read-only)
        if (taskId != null && srcPageId != null && trgPageId != null)
        {
            try
            {
                sessionMgr
                        .setAttribute(WebAppConstants.IS_FROM_ACTIVITY, "yes");
                // store jobId, target language and source page id for Lisa QA
                // report
                Task theTask = (Task) TaskHelper.retrieveObject(session,
                        WebAppConstants.WORK_OBJECT);
                if (theTask == null)
                {
                    theTask = ServerProxy.getTaskManager().getTask(Long.valueOf(taskId));
                    TaskHelper.storeObject(session, WebAppConstants.WORK_OBJECT, theTask);
                }
                sessionMgr.setAttribute(WebAppConstants.JOB_ID,
                        Long.toString(theTask.getJobId()));
                sessionMgr.setAttribute("taskStatus",
                        String.valueOf(theTask.getState()));
                sessionMgr.setAttribute(ReportConstants.TARGETLOCALE_LIST,
                        String.valueOf(theTask.getTargetLocale().getId()));
                sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID,
                        srcPageId);

                state = new EditorState();

                EditorHelper.initEditorManager(state);
                EditorHelper.initEditorOptions(state, session);

                sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);

                initializeFromActivity(state, session, user.getUserId(),
                        taskId, srcPageId, trgPageId, isAssignee, p_request,
                        uiLocale);

                initState(state, session);
            }
            catch (Exception e)
            {
            }
        }
        // From Job Details (Admin or PM opening pages read-only)
        else if (jobId != null && srcPageId != null)
        {
            // being assignee is not important when accessing editor from job
            // details.
            isAssignee = false;
            TaskHelper.storeObject(session, IS_ASSIGNEE,
                    new Boolean(isAssignee));

            state = new EditorState();
            EditorHelper.initEditorManager(state);
            EditorHelper.initEditorOptions(state, session);
            sessionMgr.setAttribute(WebAppConstants.EDITORSTATE, state);
            sessionMgr.setAttribute("taskStatus", "");
            // store jobId, target language and source page id for Lisa QA
            // report
            sessionMgr.setAttribute(WebAppConstants.JOB_ID,
                    Long.parseLong(jobId));
            sessionMgr.setAttribute(ReportConstants.TARGETLOCALE_LIST,
                    getTargetIDS(jobId, srcPageId));
            sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID, srcPageId);

            initializeFromJob(state, p_request, jobId, srcPageId, trgPageId,
                    uiLocale, user);

            initState(state, session);
        }

        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context,
                sessionMgr, state, user, isAssignee);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private String removeSpecialChars(String segment)
    {
        if (segment == null)
        {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < segment.length(); i++)
        {
            char ccc = segment.charAt(i);

            if (ccc == 9632)
            {
                continue;
            }

            if (i > 0)
            {
                char lastChar = sb.length() > 0 ? sb.charAt(sb.length() - 1)
                        : 'N';
                // ignore tab
                if (ccc == '\t' && lastChar == ' ')
                {
                    continue;
                }

                // ignore Hyphen char
                if (i < segment.length() - 1)
                {
                    char nextChar = segment.charAt(i + 1);
                    if (ccc == 173 && lastChar == ' '
                            && Character.isLetter(nextChar))
                    {
                        continue;
                    }
                }
            }

            sb.append(ccc);
        }
        
        String result = sb.toString().trim();
        result = StringUtil.replace(result, "\t", " ");
        result = StringUtil.replaceWithRE(result, "[ \t]{2,}", " ");

        return result;
    }

    private String getDIR(EditorState state, String segment, boolean isSource)
    {
        String dir = "";
        boolean rtlLocale = false;
        TuImpl tu = null;
        try
        {
            if (isSource)
            {
                rtlLocale = EditUtil.isRTLLocale(state.getSourceLocale());
            }
            else
            {
                rtlLocale = EditUtil.isRTLLocale(state.getTargetLocale());
            }
            // tu = SegmentTuUtil.getTuById(state.getTuId(), jobId);
            // boolean isLocalizable = tu.isLocalizable();
            if (rtlLocale && Text.containsBidiChar(segment))
            {
                dir = " DIR=rtl";
            }
        }
        catch (Exception ignore)
        {
        }
        return dir;
    }
    
    /**
     * Gets html segment and write back.
     * 
     * @throws Exception
     */
    private String getHtmlSegment(String segment,String str_dataType,EditorState state) throws Exception
    {
        OnlineHelper helper = new OnlineHelper();
        helper.setInputSegment(segment, "", str_dataType, Boolean.parseBoolean(str_dataType));
        
        String result = segment;
        if (EditorConstants.PTAGS_VERBOSE.equals(state.getPTagFormat()))
        {
            result = helper.makeVerboseColoredPtags(segment);
        }
        else
        {
            result = helper.makeCompactColoredPtags(segment);
        }
        return result;
    }
    
    private void renderJson(HttpServletRequest p_request,
            HttpServletResponse p_response, EditorState state,
            boolean isAssignee) throws IOException, EnvoyServletException
    {
        EditorState.Layout layout = state.getLayout();

        String jsonStr = "";
        p_response.setContentType("text/html;charset=UTF-8");
        String value = "3";

        // comment button
        if ((value = p_request.getParameter(WebAppConstants.REVIEW_MODE)) != null)
        {
            if ("Show Comments".equals(value))
            {
                state.setReviewMode();
            }
            else if (state.getUserIsPm())
            {
                state.setViewerMode();
            }
            else
            {
                state.setEditorMode();
            }
        }

        // lock button
        if ((value = p_request.getParameter("editAll")) != null)
        {
            if (state.canEditAll())
            {
                state.setEditAllState(Integer.parseInt(value));
            }
            else
            {
                // is not json format so jquery will no back
                jsonStr = "false";
            }
        }

        // Find Repeated Segments
        if ((value = p_request.getParameter(WebAppConstants.PROPAGATE_ACTION)) != null)
        {
            if (value.equalsIgnoreCase("Unmark Repeated"))
            {
                state.setNeedFindRepeatedSegments(false);
            }
            else
            {
                state.setNeedFindRepeatedSegments(true);
            }
        }

        // Show/Hide PTags
        if ((value = p_request.getParameter("pTagsAction")) != null)
        {
            if (value.equalsIgnoreCase("Show PTags"))
            {
                state.setNeedShowPTags(true);
            }
            else
            {
                state.setNeedShowPTags(false);
            }
        }

        boolean isGetJsonData = false;
        if ((value = p_request.getParameter("trgViewMode")) != null)
        {
            layout.setTargetViewMode(Integer.parseInt(value));
            isGetJsonData = true;
        }
        else if ((value = p_request.getParameter("srcViewMode")) != null)
        {
            layout.setSourceViewMode(Integer.parseInt(value));
            isGetJsonData = true;
        }
        else if (getSearchParamsInMap(p_request).size() > 0)
        {
            isGetJsonData = true;
        }
        if (isGetJsonData)
        {
            JSONObject forList = state.getEditorManager().getTargetJsonObject(state, isAssignee,
                    getSearchParamsInMap(p_request), true);
            JSONObject forInCtxRv = null;
            PaginateInfo paginateInfo = state.getPaginateInfo();
            if (paginateInfo.getTotalPageNum() > 1)
            {
                EditorState stateClone = EditorState.cloneState(state);
                PaginateInfo newpaginateInfo = new PaginateInfo(paginateInfo.getTotalSegmentNum(), 0, 1);
                stateClone.setPaginateInfo(newpaginateInfo);
                forInCtxRv = state.getEditorManager().getTargetJsonObject(stateClone, isAssignee,
                        getSearchParamsInMap(p_request), true);
            }
            else
            {
                forInCtxRv = forList;
            }
            
            try
            {
                JSONObject mainJson = new JSONObject();
                mainJson.put("forList", forList);
                mainJson.put("forInCtxRv", forInCtxRv);

                jsonStr = mainJson.toString();
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);

            }
            
            
            /*
            long _trgPageId = state.getTargetPageId().longValue();
            TargetPage _targetPage = ServerProxy.getPageManager().getTargetPage(_trgPageId);
            SourcePage _sourcePage = _targetPage.getSourcePage();

            List<TargetPage> tPages = new ArrayList<TargetPage>();
            List<SourcePage> sPages = new ArrayList<SourcePage>();

            Collection<SourcePage> sourcePages = _sourcePage.getRequest().getJob().getSourcePages();
            for (SourcePage sourcePage : sourcePages)
            {
                if (sourcePage.isActive() && !sourcePage.hasRemoved())
                {
                    String eid = sourcePage.getExternalPageId();
                    String p_eid = _sourcePage.getExternalPageId();

                    if (eid.startsWith("("))
                    {
                        eid = eid.substring(eid.indexOf(") ") + 2);
                    }
                    if (p_eid.startsWith("("))
                    {
                        p_eid = p_eid.substring(p_eid.indexOf(") ") + 2);
                    }

                    if (eid.equals(p_eid))
                    {
                        sPages.add(sourcePage);
                        tPages.add(sourcePage.getTargetPageByLocaleId(_targetPage
                                .getGlobalSightLocale().getId()));
                    }
                }
            }
            
            if (sPages.size() == 1)
            {
                jsonStr = state.getEditorManager().getTargetJsonData(state, isAssignee,
                        getSearchParamsInMap(p_request), true);
            }
            else
            {

                JSONObject mainJson = null;
                JSONArray targetjArray = new JSONArray();
                JSONArray sourcejArray = new JSONArray();
                PagePair currentPage = state.getCurrentPage();

                try
                {
                    for (int i = 0; i < sPages.size(); i++)
                    {
                        TargetPage targetPage = tPages.get(i);
                        SourcePage sourcePage = sPages.get(i);

                        ArrayList<PagePair> pagePairs = state.getPages();
                        for (PagePair pagePair : pagePairs)
                        {
                            if (pagePair.getSourcePageId().longValue() == sourcePage.getIdAsLong().longValue())
                            {
                                state.setCurrentPage(pagePair);
                                break;
                            }
                        }

                        String _jsonStr = state.getEditorManager().getTargetJsonData(state,
                                isAssignee, getSearchParamsInMap(p_request), true);

                        if (_jsonStr != null && _jsonStr.length() > 0)
                        {
                            mainJson = new JSONObject(_jsonStr);

                            JSONArray _targetjArray = (JSONArray) mainJson.get("target");
                            JSONArray _sourcejArray = (JSONArray) mainJson.get("source");

                            if (_targetjArray != null && _sourcejArray != null)
                            {
                                for (int ii = 0; ii < _targetjArray.length(); ii++)
                                {
                                    JSONObject _targetO = _targetjArray.getJSONObject(ii);
                                    JSONObject _sourceO = _sourcejArray.getJSONObject(ii);

                                    targetjArray.put(_targetO);
                                    sourcejArray.put(_sourceO);
                                }
                            }
                        }
                    }

                    state.setCurrentPage(currentPage);
                    mainJson = new JSONObject();
                    mainJson.put("target", targetjArray);
                    mainJson.put("source", sourcejArray);

                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);

                }
                jsonStr = mainJson.toString();
            } */
        }
        p_response.getWriter().write(jsonStr);
    }

    /**
     * Get the target language based on job id and source page id
     * 
     * @throws EnvoyServletException
     */
    private String getTargetIDS(String p_jobId, String p_srcPageId)
            throws EnvoyServletException
    {
        StringBuffer result = new StringBuffer();
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long.parseLong(p_jobId));
            Collection<Workflow> wfs = job.getWorkflows();
            for (Iterator<Workflow> it = wfs.iterator(); it.hasNext();)
            {
                Workflow wf = (Workflow) it.next();
                if (Workflow.CANCELLED.equals(wf.getState())
                        || Workflow.EXPORT_FAILED.equals(wf.getState())
                        || Workflow.IMPORT_FAILED.equals(wf.getState()))
                {
                    continue;
                }

                /*
                 * Canceled for GBS-2419 Collection targetPages =
                 * wf.getTargetPages(); for (Iterator itr =
                 * targetPages.iterator(); itr.hasNext();) { TargetPage tp =
                 * (TargetPage) itr.next(); if
                 * (p_srcPageId.equals(Long.toString(tp.getSourcePage()
                 * .getId()))) { return wf.getTargetLocale().getDisplayName(); }
                 * }
                 */

                result.append(wf.getTargetLocale().getId()).append(",");
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem getting job from database ", e);
            throw new EnvoyServletException(e);
        }

        if (result.length() > 0 && result.toString().endsWith(","))
        {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * Executes all actions sent in from the UI and updates the EditorState to
     * have correct data for the JSPs.
     */
    private void dispatchJSP(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context, SessionManager p_sessionMgr,
            EditorState p_state, User p_user, boolean p_isTaskAssignee)
            throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        User user = TaskHelper.getUser(session);
        String userId = user.getUserId();
        p_state.setUserName(userId);

        boolean bUpdateSource = false;
        boolean bUpdateTarget = false;

        EditorState.Layout layout = p_state.getLayout();
        String value;
        if ((value = p_request.getParameter("srcViewMode")) != null)
        {
            p_state.setSourceAsTarget("1".equals(p_request.getParameter("asSource")));
            layout.setSourceViewMode(Integer.parseInt(value));
            bUpdateSource = true;
        }
        if ((value = p_request.getParameter("trgViewMode")) != null)
        {
            p_state.setSourceAsTarget("1".equals(p_request.getParameter("asSource")));
            layout.setTargetViewMode(Integer.parseInt(value));
            bUpdateTarget = true;
        }
        if ((value = p_request.getParameter("singlePage")) != null)
        {
            layout.setSinglePage(Integer.parseInt(value));
        }
        if ((value = p_request.getParameter("singlePageSource")) != null)
        {
            layout.setSinglePageIsSource(Integer.parseInt(value));
        }
        if ((value = p_request.getParameter("horizontal")) != null)
        {
            layout.setHorizontal(Integer.parseInt(value));
        }
        if ((value = p_request.getParameter("editAll")) != null)
        {
            if (p_state.canEditAll())
            {
                p_state.setEditAllState(Integer.parseInt(value));
                bUpdateTarget = true;
            }
        }
        if ((value = p_request.getParameter("reviewMode")) != null)
        {
            layout.setSourceViewMode(VIEWMODE_DETAIL);
            layout.setTargetViewMode(VIEWMODE_DETAIL);
            p_state.setTargetPageHtml(null);
            p_state.setSourceAsTarget(false);

            if ("true".equals(value))
            {
                p_state.setReviewMode();
            }
            else if (p_state.getUserIsPm())
            {
                p_state.setViewerMode();
            }
            else
            {
                p_state.setEditorMode();
            }
        }

        if ((value = p_request.getParameter("cmtAction")) != null)
        {
            CommentView commentView = (CommentView) p_sessionMgr
                    .getAttribute(WebAppConstants.COMMENTVIEW);
            executeCommentCommand(p_state, commentView, p_request, p_user);
        }

        // Sat Mar 19 00:43:53 2005 CvdL
        // PM switched target locale in main editor. If there are no
        // GS tags, only the me_target frame is reloaded. If the page
        // has GS tags, the content frame gets reloaded (me_pane2 or
        // me_split) and the source page view + cache is invalidated.
        if ((value = p_request.getParameter("trgViewLocale")) != null)
        {
            // Clear comments from previous target locales.
            p_state.setCommentThreads(null);

            p_state.setTargetViewLocale(EditorHelper.getLocale(value));
            p_state.setTargetPageHtml(null);

            p_sessionMgr.setAttribute("trgViewLocale",
                    EditorHelper.getLocale(value).getDisplayName());

            if (p_state.hasGsaTags())
            {
                p_state.clearSourcePageHtml();
                EditorHelper.invalidateCachedTemplates(p_state);
            }
        }

        if ((value = p_request.getParameter("segmentFilter")) != null)
        {
            p_state.setSegmentFilter(p_request.getParameter("segmentFilter"));
        }
        p_request.setAttribute("segmentFilter", p_state.getSegmentFilter());

        // Save
        if ((value = p_request.getParameter("save")) != null)
        {
            long tuId = p_state.getTuId();
            long tuvId = p_state.getTuvId();
            long subId = p_state.getSubId();

            try
            {
                // Updated segment arrives in UTF-8, decode to Unicode
                value = EditUtil.utf8ToUnicode(value);
                SegmentView segmentView = (SegmentView) p_sessionMgr
                        .getAttribute(WebAppConstants.SEGMENTVIEW);
                EditorHelper.updateSegment(p_state, segmentView, tuId, tuvId,
                        subId, value, userId);

                // Delete the old pdf file for the Indd preview
                PreviewPDFHelper.deleteOldPdf(p_state.getTargetPageId()
                        .longValue(), p_state.getTargetLocale().getId());
                PreviewPageHandler.deleteOldPreviewFile(p_state
                        .getTargetPageId().longValue(), p_state
                        .getTargetLocale().getId());
            }
            catch (EnvoyServletException e)
            {
                // This should, of course, never fail. If it fails,
                // we just redisplay the current state.
                CATEGORY.error("ME ignoring update exception ", e);
            }
            catch (Exception e)
            {
                CATEGORY.error("ME ignoring update exception ", e);
            }

            bUpdateTarget = true;
            if (OnlineEditorConstants.SEGMENT_FILTER_ICE.equals(p_state
                    .getSegmentFilter()))
            {
                bUpdateSource = true;
                p_request.setAttribute("refreshSource", "true");
            }

            long targetPageId = p_state.getTargetPageId().longValue();
            long sourceLocaleId = p_state.getSourceLocale().getId();
            long targetLocaleId = p_state.getTargetLocale().getId();
            SegmentView segmentView = EditorHelper.getSegmentView(p_state,
                    tuId, tuvId, subId, targetPageId, sourceLocaleId,
                    targetLocaleId);
            p_sessionMgr.setAttribute(WebAppConstants.SEGMENTVIEW, segmentView);
        }

        // Sat Jun 07 00:56:22 2003 CvdL: remember the segment
        // last viewed in the Segment Editor so the Main Editor
        // can highlight it when it loads.
        // Wed May 11 23:48:48 2005 CvdL: reuse for opening editor
        // with a specific segment highlighted.
        setCurrentEditorSegment(p_state, p_request);

		boolean isContextReview = p_request.getParameter("isContextReview") != null ? true
				: false;
        
        // next & previous page
        if ((value = p_request.getParameter("refresh")) != null)
        {
            layout.setSourceViewMode(VIEWMODE_DETAIL);
            layout.setTargetViewMode(VIEWMODE_DETAIL);
            p_state.setSourceAsTarget(false);
            
            int i_direction = 0;
            if (!value.startsWith("0"))
                i_direction = Integer.parseInt(value);
            boolean fromActivity = false;
            String att = (String) p_sessionMgr
                    .getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
            if (att != null && att.equals("yes"))
            {
                fromActivity = true;
            }
            if (i_direction == -1) // previous file
			{
				previousPage(p_state, p_request.getSession(), fromActivity,
						isContextReview);
				while (!p_state.isFirstPage()
						&& (p_state.getTuIds() == null || p_state.getTuIds()
								.size() == 0))
				{
					previousPage(p_state, p_request.getSession(), fromActivity,
							isContextReview);
				}
			}
            else if (i_direction == 1) // next file
			{
				nextPage(p_state, p_request.getSession(), fromActivity,
						isContextReview);
				while (!p_state.isLastPage()
						&& (p_state.getTuIds() == null || p_state.getTuIds()
								.size() == 0))
				{
					nextPage(p_state, p_request.getSession(), fromActivity,
							isContextReview);
				}
			}
            else if (i_direction == -11) // previous page
            {
                bUpdateSource = true;
                bUpdateTarget = true;
                if (layout.isSinglePage())
                {
                    if (layout.singlePageIsSource())
                    {
                        bUpdateTarget = false;
                    }
                    else
                    {
                        bUpdateSource = false;
                    }
                }

                int oldCurrentPageNum = p_state.getPaginateInfo()
                        .getCurrentPageNum();
                int newCurrentPageNum = oldCurrentPageNum - 1;
                if (newCurrentPageNum < 1)
                {
                    p_state.getPaginateInfo().setCurrentPageNum(
                            p_state.getPaginateInfo().getTotalPageNum());
                }
                else
                {
                    p_state.getPaginateInfo().setCurrentPageNum(
                            newCurrentPageNum);
                }
            }
            else if (i_direction == 11) // next page
            {
                bUpdateSource = true;
                bUpdateTarget = true;
                if (layout.isSinglePage())
                {
                    if (layout.singlePageIsSource())
                    {
                        bUpdateTarget = false;
                    }
                    else
                    {
                        bUpdateSource = false;
                    }
                }

                int oldCurrentPageNum = p_state.getPaginateInfo()
                        .getCurrentPageNum();
                int newCurrentPageNum = oldCurrentPageNum + 1;
                if (newCurrentPageNum > p_state.getPaginateInfo()
                        .getTotalPageNum())
                {
                    p_state.getPaginateInfo().setCurrentPageNum(1);
                }
                else
                {
                    p_state.getPaginateInfo().setCurrentPageNum(
                            newCurrentPageNum);
                }
            }
            else if (value.startsWith("0")) // goto page
            {
            	i_direction = Integer.parseInt(value);
				if (value.equals("0"))
				{
					int oldCurrentPageNum = p_state.getPaginateInfo()
							.getCurrentPageNum();
					if (oldCurrentPageNum != i_direction)
					{
						i_direction = oldCurrentPageNum;
					}
				}
                bUpdateSource = true;
                bUpdateTarget = true;
                if (layout.isSinglePage())
                {
                    if (layout.singlePageIsSource())
                    {
                        bUpdateTarget = false;
                    }
                    else
                    {
                        bUpdateSource = false;
                    }
                }
                p_state.getPaginateInfo().setCurrentPageNum(i_direction);
            }
            else
            {
                // redisplay current page - set flag to update target view
                bUpdateTarget = true;
            }

            // When you modify "edit segment" page and check the "close comment"
            // checkbox and save it, in the comments page the stater need to
            // be refreshed. So here refresh the CommentThreadView in
            // the EditorState.
            CommentThreadView view = p_state.getCommentThreads();
            if (view != null)
            {
                String sortedBy = view.getSortedBy();
                view = EditorHelper.getCommentThreads(p_state);
                view.sort(sortedBy);
                p_state.setCommentThreads(view);
            }

            String currentSrcPageId = p_state.getCurrentPage()
                    .getSourcePageId().toString();
            p_sessionMgr.setAttribute(WebAppConstants.SOURCE_PAGE_ID,
                    currentSrcPageId);

            if (SegmentFilter.isFilterSegment(p_state))
            {
                bUpdateTarget = true;
                bUpdateSource = true;
                if (OnlineEditorConstants.SEGMENT_FILTER_ICE.equals(p_state
                        .getSegmentFilter()))
                {
                    p_request.setAttribute("refreshSource", "true");
                }
            }
        }

        // Click "Search" in popup editor.
        if ((value = p_request.getParameter("search")) != null)
        {
            p_sessionMgr.setAttribute("userNameList", p_state
                    .getEditorManager().getPageLastModifyUserList(p_state));

            p_sessionMgr.setAttribute("sidList", p_state.getEditorManager()
                    .getPageSidList(p_state));
            p_sessionMgr.setAttribute("from", "online");
        }

        // The user may have chosen preview mode as default, if that
        // is not available switch to TEXT.
        if (!EditUtil.hasPreviewMode(p_state.getPageFormat()))
        {
            if (layout.getSourceViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setSourceViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }

            if (layout.getTargetViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setTargetViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }
        }

        // Check for any offline uploads that affect this page.
        EditorHelper.checkSynchronizationStatus(p_state);

        // Thu Jan 09 23:58:56 2003 CvdL: Source views are computed on
        // demand, also when the editor is first opened. Check if
        // me_source or me_target is getting called and then update.
        if (bUpdateTarget || needTargetPageView(p_pageDescriptor, p_state))
        {
            HashMap<String, String> hm = getSearchParamsInMap(p_request);
            updateTargetPageView(p_state, p_request.getSession(),
                    p_isTaskAssignee, isIE(p_request), hm);
        }

        if (bUpdateSource || needSourcePageView(p_pageDescriptor, p_state))
        {
            HashMap<String, String> hm = getSearchParamsInMap(p_request);
            updateSourcePageView(p_state, p_request, p_isTaskAssignee,
                    isIE(p_request), hm);
        }

        // comment pane needs comment data (from me_comments.jsp)
        if (needComments(p_pageDescriptor))
        {
            CommentThreadView view = p_state.getCommentThreads();
            if (view == null)
            {
                view = EditorHelper.getCommentThreads(p_state);
                p_state.setCommentThreads(view);
            }
            if (view != null)
            {
                if ((value = p_request.getParameter("sortComments")) != null)
                {
                    view.sort(value);
                }
            }
        }
    }

    private boolean isIE(HttpServletRequest p_request)
    {
        return (p_request.getHeader("User-Agent").toLowerCase().indexOf("msie")) != -1 ? true
                : false;
    }

    private HashMap<String, String> getSearchParamsInMap(
            HttpServletRequest p_request)
    {
        HashMap<String, String> hm = new HashMap<String, String>();

        if (p_request.getParameter("searchByUser") != null)
        {
            String userId = p_request.getParameter("searchByUser");
            hm.put("userId", userId);
        }
        else if (p_request.getParameter("searchBySid") != null)
        {
            String sid = p_request.getParameter("searchBySid");
            hm.put("sid", sid);
        }

        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        if (sessionMgr.getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT) != null
                && sessionMgr
                        .getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT) != "")
        {
            String searchText = (String) sessionMgr
                    .getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT);
            hm.put("searchText", searchText);
        }
        return hm;
    }

	private void previousPage(EditorState p_state, HttpSession p_session,
			boolean p_fromActivity, boolean isContextReview)
			throws EnvoyServletException
	{
        ArrayList<EditorState.PagePair> pages = p_state.getPages();
        pages = (ArrayList<EditorState.PagePair>) getPagePairList(p_session,
                pages);
		if (isContextReview)
		{
			pages.removeAll(getRemovePages(pages));
		}
        int i_index = pages.indexOf(p_state.getCurrentPage());

        if (p_fromActivity)
        {
            boolean foundNonempty = false;
            boolean allEmptyBefore = true;
            while (i_index > 0)
            {
                --i_index;
                EditorState.PagePair pp = (EditorState.PagePair) pages
                        .get(i_index);

                if (!foundNonempty)
                {
                    p_state.setCurrentPage(pp);
                    p_state.setIsFirstPage(i_index == 0);
                    p_state.setIsLastPage(false);

                    initState(p_state, p_session);

                    if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                    {
                        if (EditorHelper.pmCanEditCurrentPage(p_state))
                        {
                            p_state.setReadOnly(false);
                            p_state.setAllowEditAll(true);
                            p_state.setEditAllState(EDIT_ALL);
                        }
                        else
                        {
                            p_state.setReadOnly(true);
                        }
                    }
                    foundNonempty = true;
                    continue;
                }

                if (foundNonempty && allEmptyBefore)
                {
                    allEmptyBefore = false;
                    break;
                }

            }
            if (foundNonempty && allEmptyBefore)
            {
                p_state.setIsFirstPage(true);
            }
        }
        else
        {
            if (i_index > 0)
            {
                --i_index;

                p_state.setCurrentPage((EditorState.PagePair) pages
                        .get(i_index));

                p_state.setIsFirstPage(i_index == 0);
                p_state.setIsLastPage(false);

                initState(p_state, p_session);

                if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                {
                    if (EditorHelper.pmCanEditCurrentPage(p_state))
                    {
                        p_state.setReadOnly(false);
                        p_state.setAllowEditAll(true);
                        p_state.setEditAllState(EDIT_ALL);
                    }
                    else
                    {
                        p_state.setReadOnly(true);
                    }
                }
            }
        }

    }

	private void nextPage(EditorState p_state, HttpSession p_session,
			boolean p_fromActivity, boolean isContextReview)
			throws EnvoyServletException
	{
        ArrayList<EditorState.PagePair> pages = p_state.getPages();
        pages = (ArrayList<EditorState.PagePair>) getPagePairList(p_session,
                pages);
		if (isContextReview)
		{
			pages.removeAll(getRemovePages(pages));
		}
        int i_index = pages.indexOf(p_state.getCurrentPage());

        if (p_fromActivity)
        {
            boolean foundNonempty = false;
            boolean allEmptyAfter = true;

            while (i_index >= 0 && i_index < (pages.size() - 1))
            {
                ++i_index;

                EditorState.PagePair pp = (EditorState.PagePair) pages
                        .get(i_index);

                if (!foundNonempty)
                {
                    p_state.setCurrentPage(pp);
                    p_state.setIsFirstPage(false);
                    p_state.setIsLastPage(i_index == (pages.size() - 1));

                    initState(p_state, p_session);

                    if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                    {
                        if (EditorHelper.pmCanEditCurrentPage(p_state))
                        {
                            p_state.setReadOnly(false);
                            p_state.setAllowEditAll(true);
                            p_state.setEditAllState(EDIT_ALL);
                        }
                        else
                        {
                            p_state.setReadOnly(true);
                        }
                    }
                    foundNonempty = true;
                    continue;
                }

                if (foundNonempty && allEmptyAfter)
                {
                    allEmptyAfter = false;
                    break;
                }

            }
            if (foundNonempty && allEmptyAfter)
            {
                p_state.setIsLastPage(true);
            }
        }

        else
        {
            if (i_index >= 0 && i_index < (pages.size() - 1))
            {
                ++i_index;

                p_state.setCurrentPage((EditorState.PagePair) pages
                        .get(i_index));

                p_state.setIsFirstPage(false);
                p_state.setIsLastPage(i_index == (pages.size() - 1));

                initState(p_state, p_session);

                if (p_state.getUserIsPm() && s_pmCanEditTargetPages)
                {
                    if (EditorHelper.pmCanEditCurrentPage(p_state))
                    {
                        p_state.setReadOnly(false);
                        p_state.setAllowEditAll(true);
                        p_state.setEditAllState(EDIT_ALL);
                    }
                    else
                    {
                        p_state.setReadOnly(true);
                    }
                }
            }
        }

    }
    
	@SuppressWarnings("static-access")
    private ArrayList<EditorState.PagePair> getRemovePages(
			ArrayList<EditorState.PagePair> pages)
	{
		ArrayList<EditorState.PagePair> newPages = new ArrayList<EditorState.PagePair>();
		com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper helper = new com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper();
		String companyId = CompanyThreadLocal.getInstance().getValue();
		boolean okForInContextReviewXml = helper.isXMLEnabled(companyId);
		boolean okForInContextReviewIndd = helper.isInDesignEnabled(companyId);
		boolean okForInContextReviewOffice = helper.isOfficeEnabled(companyId);
		boolean okForInContextReviewHtml = helper.isHTMLEnabled(companyId);
		FileProfile fp = null;
		try
		{
			for (EditorState.PagePair page : pages)
			{
				SourcePage sp = ServerProxy.getPageManager().getSourcePage(
						page.getSourcePageId());
				fp = ServerProxy.getFileProfilePersistenceManager()
						.readFileProfile(sp.getRequest().getDataSourceId());
				String pageNameLow = page.getPageName().toLowerCase();
				boolean isXml = pageNameLow.endsWith(".xml");
				boolean isInDesign = pageNameLow.endsWith(".indd")
						|| pageNameLow.endsWith(".idml");
				boolean isOffice = pageNameLow.endsWith(".docx")
						|| pageNameLow.endsWith(".pptx")
						|| pageNameLow.endsWith(".xlsx");
				boolean isHtml = pageNameLow.endsWith(".html") || pageNameLow.endsWith(".htm");
				boolean enableInContextReivew = false;
				if (isXml)
				{
					enableInContextReivew = okForInContextReviewXml ? FileProfileUtil
							.isXmlPreviewPDF(fp) : false;
				}
				if (isInDesign)
				{
					enableInContextReivew = okForInContextReviewIndd;
				}
				if (isOffice)
				{
					enableInContextReivew = okForInContextReviewOffice;
				}
				
				if (isHtml)
				{
				    enableInContextReivew = okForInContextReviewHtml;
				}
				
				if (!enableInContextReivew)
				{
					newPages.add(page);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return newPages;
	}

    private void updateSourcePageView(EditorState p_state,
            HttpServletRequest p_request, boolean p_isTaskAssignee,
            boolean p_isIE, HashMap p_searchMap) throws EnvoyServletException
    {
        int viewMode = p_state.getLayout().getSourceViewMode();
        int uiMode = getUiMode(p_state, p_isTaskAssignee);

        String html;
        // Update sourcePageHtml whatever it is null or not as it maybe is
        // "batch navigation".
        p_state.setRenderingOptions(initRenderingOptions(
                p_request.getSession(), uiMode, viewMode,
                UIConstants.EDITMODE_DEFAULT));
        if (viewMode == UIConstants.VIEWMODE_LIST)
        {
            p_state.setSourcePageHtml(viewMode, "");
        }
        else
        {
            p_state.getRenderingOptions().setFromIncontextReviewEdit(true);
            html = EditorHelper.getSourcePageView(p_state, false, p_searchMap);
            html = OfficeContentPostFilterHelper.fixHtmlForSkeleton(html);
            html = replaceImgForFirefox(html, p_isIE);
            p_state.setSourcePageHtml(viewMode, html);
        }
    }

    private void updateTargetPageView(EditorState p_state,
            HttpSession p_session, boolean p_isTaskAssignee, boolean p_isIE,
            HashMap p_searchMap) throws EnvoyServletException
    {
        int viewMode = p_state.getLayout().getTargetViewMode();
        int uiMode = getUiMode(p_state, p_isTaskAssignee);

        RenderingOptions renderingOptions = initRenderingOptions(p_session,
                uiMode, viewMode, UIConstants.EDITMODE_DEFAULT);
        p_state.setRenderingOptions(renderingOptions);
        renderingOptions.setFromIncontextReviewEdit(true);

        String html;
        html = EditorHelper.getTargetPageView(p_state, false, p_searchMap);

        html = OfficeContentPostFilterHelper.fixHtmlForSkeleton(html);
        html = replaceImgForFirefox(html, p_isIE);

        p_state.setTargetPageHtml(html);
    }

    /**
     * VML only works in IE, so replace image tag
     * 
     * @param p_html
     *            original html string
     * @param p_isIE
     *            is IE
     * @return For example: Original html: <v:shape alt="original.aspx"
     *         style='width:487.5pt;'><v:imagedata src="image001.jpg"
     *         o:title="original"/></v:shape> Return html: <img
     *         alt="original.aspx" style='width:487.5pt;' src="image001.jpg"
     *         o:title="original"/>
     */
    public String replaceImgForFirefox(String p_html, boolean p_isIE)
    {
        if (p_isIE)
        {
            return p_html;
        }
        else
        {
            String result = p_html;
            String shapeTag1 = "<v:shape ";
            String shapeTag2 = "</v:shape>";
            String imgTag = "<v:imagedata ";

            if (p_html.contains(shapeTag1) && p_html.contains(shapeTag2)
                    && p_html.contains(imgTag))
            {
                int shapePos1 = p_html.indexOf(shapeTag1);
                int shapePos2 = p_html.indexOf(shapeTag2) + shapeTag2.length();
                int imgPos = p_html.indexOf(imgTag, shapePos1);
                if ((shapePos1 < imgPos) && (imgPos < shapePos2))
                {
                    String str = p_html.substring(shapePos1, shapePos2);
                    int imgPos2 = str.indexOf(imgTag);
                    if (imgPos2 == str.lastIndexOf(imgTag))
                    {
                        String attr1 = str.substring(shapeTag1.length(),
                                str.indexOf(">"));
                        String attr2 = str.substring(imgPos2 + imgTag.length(),
                                str.indexOf("/>", imgPos2));
                        attr1 = attr1.replace("style='width:487.5pt;",
                                "style='width:375pt;");//
                        String repStr = "<img " + attr1 + " " + attr2 + " />";
                        result = p_html.replace(str, repStr);
                    }
                }
            }
            return result;
        }
    }

    /**
     * Creates a new RenderingOptions object based on the UI and view modes and
     * the current session.
     * 
     * Note: there should be two RenderingObjects kept in EditorState to keep
     * track of the different modes the editor frames can be in.
     */
    private RenderingOptions initRenderingOptions(HttpSession p_session,
            int p_uiMode, int p_viewMode, int p_editMode)
    {
        PermissionSet permSet = (PermissionSet) p_session
                .getAttribute(WebAppConstants.PERMISSIONS);
        return new RenderingOptions(p_uiMode, p_viewMode, p_editMode, permSet);
    }

    /**
     * Initializes editor state from an activity, i.e. when the editor is opened
     * by a localization participant.
     */
    private void initializeFromActivity(EditorState p_state,
            HttpSession p_session, String p_userId, String p_taskId,
            String p_srcPageId, String p_trgPageId, boolean p_isAssignee,
            HttpServletRequest p_request, Locale p_uiLocale)
            throws EnvoyServletException
    {
        PermissionSet perms = (PermissionSet) p_session
                .getAttribute(WebAppConstants.PERMISSIONS);
        p_state.setUserIsPm(false);

        // Reset all options because the state may be inherited from a
        // previous page.
        EditorHelper.initEditorOptions(p_state, p_session);

        // Initializes pages, target locales, excluded items,
        // termbases and editAll state.
        EditorHelper.initializeFromActivity(p_state, p_session, p_userId,
                p_taskId, p_request, p_uiLocale);

        setCurrentPageFromActivity(p_session, p_state, p_srcPageId);
        EditorState.PagePair currentPage = p_state.getCurrentPage();

        p_state.setTargetViewLocale(currentPage.getTargetPageLocale(new Long(
                p_trgPageId)));

        // Target page is viewed read-only when the activity/task has
        // not been accepted yet.
        String stateAsString = (String) TaskHelper.retrieveObject(p_session,
                TASK_STATE);

        int taskState = stateAsString == null ? WorkflowConstants.TASK_ALL_STATES
                : Integer.parseInt(stateAsString);

        // We need to set the state to read-only if the user is not
        // the assignee. This happens when a PM is viewing an
        // activity that belong's to his/her job.

        // In other words, if this task is assigned to the user but he
        // hasn't accepted it, it is read-only.
        boolean b_readOnly = true;
        if (p_isAssignee)
        {
            b_readOnly = EditorHelper.getTaskIsReadOnly(p_userId, p_taskId,
                    taskState);
        }

        p_state.setReadOnly(b_readOnly);

        // If the page is read-only, don't allow to unlock segments.
        if (b_readOnly)
        {
            p_state.setAllowEditAll(false);
        }

        // Set editAll state based on whether we can edit all or not.
        if (p_state.canEditAll())
        {
            p_state.setEditAllState(p_state.getOptions().getAutoUnlock() == true ? EDIT_ALL
                    : EDIT_DEFAULT);
        }
        else
        {
            p_state.setEditAllState(EDIT_DEFAULT);
        }

        // Indicate that main editor is in 'editor' mode -- see
        // dispatchJsp for switching to review mode.
        // Comments are turned ON by default in popup editor for a review
        // activity
        if (p_state.getIsReviewActivity())
        {
            p_state.setReviewMode();
        }
        else
        {
            p_state.setEditorMode();
        }
    }

    /**
     * Initializes editor state from a job, i.e. when the editor is opened by an
     * Admin or PM.
     */
    private void initializeFromJob(EditorState p_state,
            HttpServletRequest p_request, String p_jobId, String p_srcPageId,
            String p_trgPageId, Locale p_uiLocale, User p_user)
            throws EnvoyServletException
    {
        p_state.setUserIsPm(true);
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        PermissionSet perms = (PermissionSet) p_request.getSession()
                .getAttribute(WebAppConstants.PERMISSIONS);
        // Reset all options because the state may be inherited from a
        // previous page.
        EditorHelper.initEditorOptions(p_state, p_request.getSession());
        // Initializes pages, target locales, excluded items, and termbases
        EditorHelper.initializeFromJob(p_state, p_jobId, p_srcPageId,
                p_uiLocale, p_user.getUserId(), perms);

        if (p_trgPageId != null && p_trgPageId.length() > 0)
        {
            // If the PM requests a specific target page...

            setCurrentPage(p_request.getSession(), p_state, p_srcPageId,
                    p_trgPageId);

            EditorState.PagePair currentPage = p_state.getCurrentPage();

            p_state.setTargetViewLocale(currentPage
                    .getTargetPageLocale(new Long(p_trgPageId)));
        }
        else
        {
            // No target page/locale requested, find a suitable one.

            setCurrentPage(p_request.getSession(), p_state, p_srcPageId);

            // If no locale is set or the set locale doesn't exist in the
            // list of target locales in the job (fix for def_5545),
            // determine the default locale to display in target window.
            GlobalSightLocale viewLocale = p_state.getTargetViewLocale();
            Vector trgLocales = p_state.getJobTargetLocales();
            GlobalSightLocale local = (GlobalSightLocale) sessionMgr
                    .getAttribute("targetLocale");
            if (viewLocale == null || !trgLocales.contains(viewLocale))
            {
                if (trgLocales.contains(local))
                {
                    Iterator it = trgLocales.iterator();
                    while (it.hasNext())
                    {
                        GlobalSightLocale trgLocale = (GlobalSightLocale) it
                                .next();
                        if (local.getLocale().equals(trgLocale.getLocale()))
                        {

                            p_state.setTargetViewLocale((GlobalSightLocale) trgLocale);
                        }
                    }
                }
                else
                {
                    p_state.setTargetViewLocale((GlobalSightLocale) trgLocales
                            .elementAt(0));
                }
            }
        }

        // When coming from job page, target page is read only.
        // Fri Feb 20 20:18:44 2004 CvdL: Patch for HP: PMs can edit
        // all target pages any time at their own risk.
        if (s_pmCanEditTargetPages
                && EditorHelper.pmCanEditCurrentPage(p_state))
        {
            p_state.setReadOnly(false);
            p_state.setAllowEditAll(true);
            p_state.setEditAllState(EDIT_ALL);
        }
        else
        {
            p_state.setReadOnly(true);
        }

        // Indicate that main editor is in 'viewer' mode -- see
        // dispatchJsp for switching to review mode.
        // Comments are turned ON by default in popup editor from Job detail
        // page
        p_state.setReviewMode();
    }

    private void initState(EditorState p_state, HttpSession p_session)
            throws EnvoyServletException
    {
        p_state.setSourceLocale(p_state.getSourceLocale());

        ArrayList<Long> tuIds = EditorHelper.getTuIdsInPage(p_state,
                p_state.getSourcePageId());
        p_state.setTuIds(tuIds);

        PageInfo info = EditorHelper.getPageInfo(p_state,
                p_state.getSourcePageId());

        // Wed Mar 05 20:18:29 2003 CvdL: use pageinfo record in
        // me_(target|source)Menu to determine when preview mode is
        // available.
        p_state.setPageInfo(info);

        p_state.setPageFormat(info.getPageFormat());
        // discard PageInfo object for now - could add fields to this?

        // If we were in preview mode and the current page doesn't
        // have a preview mode, change the mode to TEXT.
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.removeElement("src_view_pdf");
        sessionMgr.removeElement("tgt_view_pdf");
        EditorState.Layout layout = p_state.getLayout();
        if (EditUtil.hasPDFPreviewMode(p_state))
        {
            if (layout.getSourceViewMode() == VIEWMODE_PREVIEW)
            {
                sessionMgr.setAttribute("src_view_pdf", Boolean.TRUE);
            }

            if (layout.getTargetViewMode() == VIEWMODE_PREVIEW)
            {
                sessionMgr.setAttribute("tgt_view_pdf", Boolean.FALSE);
            }
        }
        if (!EditUtil.hasPreviewMode(p_state.getPageFormat()))
        {
            if (layout.getSourceViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setSourceViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }

            if (layout.getTargetViewMode() == VIEWMODE_PREVIEW)
            {
                layout.setTargetViewMode(DEFAULT_VIEWMODE_IF_NO_PREVIEW);
            }
        }

        // Sat Feb 19 02:06:30 2005 CvdL Loading a new page, clear
        // offline upload status. This also clears the OEML's chached
        // target tuvs, which is ok when loading a new (or the
        // next/previous) page.
        EditorHelper.clearSynchronizationStatus(p_state);

        // Clear comments from previous pages.
        p_state.setCommentThreads(null);

        // Fri Jan 10 00:30:46 2003 CvdL: this used to precompute all
        // views. We don't do this anymore to ensure better
        // parallelism and on-demand computation (i.e. if user looks
        // only at target view we don't need source view).

        p_state.clearSourcePageHtml();
        p_state.setTargetPageHtml(null);

        String str_segmentNumPerPage = PageHandler.getUserParameter(p_session,
                UserParamNames.EDITOR_SEGMENTS_MAX_NUM).getValue();
        int int_segmentNumPerPage = Integer.parseInt(str_segmentNumPerPage);
        PaginateInfo pi = new PaginateInfo(tuIds.size(), int_segmentNumPerPage,
                1);
        p_state.setPaginateInfo(pi);
    }

    /**
     * Scans the pagelist for the first pair having the right source page id and
     * set the pair to be shown first.
     */
    private void setCurrentPage(HttpSession p_session, EditorState p_state,
            String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<EditorState.PagePair>) getPagePairList(p_session,
                pages);
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId);
            }

            if (pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_offset == 1);
        p_state.setIsLastPage(pages.size() == i_offset);
    }

    /**
     * Scans the pagelist for the first pair having the right source page id and
     * set the pair to be shown first.
     */
    private void setCurrentPageFromActivity(HttpSession p_session,
            EditorState p_state, String p_srcPageId)
    {
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<EditorState.PagePair>) getPagePairList(p_session, pages);
        Long srcPageId = new Long(p_srcPageId);
        int i_offset = 0;
        int offset = 0;
        boolean foundPage = false;
        boolean allEmptyBefore = true;
        boolean allEmptyAfter = true;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_offset;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId);
            }

            if (!foundPage && pair.getSourcePageId().equals(srcPageId))
            {
                p_state.setCurrentPage(pair);
                foundPage = true;
                offset = i_offset;
                continue;
            }

            if (foundPage && allEmptyAfter)
            {
                allEmptyAfter = false;
                break;
            }
            else if (!foundPage && allEmptyBefore)
            {
                allEmptyBefore = false;
            }
        }

        p_state.setIsFirstPage(offset == 1);
        p_state.setIsLastPage(pages.size() == offset);

        if (allEmptyBefore)
        {
            p_state.setIsFirstPage(true);
        }

        if (allEmptyAfter)
        {
            p_state.setIsLastPage(true);
        }

    }

    /**
     * Scans the pagelist for the pair having the right source and target page
     * id and set the pair to be shown first.
     */
    private void setCurrentPage(HttpSession p_session, EditorState p_state,
            String p_srcPageId, String p_trgPageId)
    {
        ArrayList pages = p_state.getPages();
        pages = (ArrayList<EditorState.PagePair>) getPagePairList(p_session,
                pages);
        Long srcPageId = new Long(p_srcPageId);
        Long trgPageId = new Long(p_trgPageId);
        int i_index = 0;

        for (int i = 0, max = pages.size(); i < max; i++)
        {
            EditorState.PagePair pair = (EditorState.PagePair) pages.get(i);
            ++i_index;

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Pagepair= " + pair.toString() + " p_srcPageId="
                        + p_srcPageId + " p_trgPageId=" + p_trgPageId);
            }

            // See if this page pair object is for this source page.
            // If so, see if this page pair object contains the
            // specified target page id.

            if (pair.getSourcePageId().equals(srcPageId)
                    && pair.getTargetPageLocale(trgPageId) != null)
            {
                p_state.setCurrentPage(pair);
                break;
            }
        }

        p_state.setIsFirstPage(i_index == 1);
        p_state.setIsLastPage(pages.size() == i_index);
    }

    /**
     * For better parallelism during frame loading: compute the source page view
     * only when the me_source page (ED5) is actually shown.
     */
    private boolean needSourcePageView(WebPageDescriptor p_pageDescriptor,
            EditorState p_state)
    {
        String pageName = p_pageDescriptor.getPageName();
        int srcViewMode = p_state.getLayout().getSourceViewMode();
        if (pageName.equals("inctxrvED5")
                && p_state.getSourcePageHtml(srcViewMode) == null)
        {
            return true;
        }

        return false;
    }

    /**
     * For better parallelism during frame loading: compute the target page view
     * only when the me_target page (ED8) is actually shown.
     */
    private boolean needTargetPageView(WebPageDescriptor p_pageDescriptor,
            EditorState p_state)
    {
        String pageName = p_pageDescriptor.getPageName();

        if (pageName.equals("inctxrvED8")
                && p_state.getTargetPageHtml() == null)
        {
            return true;
        }

        return false;
    }

    private boolean needComments(WebPageDescriptor p_pageDescriptor)
    {
        return p_pageDescriptor.getPageName().equals("inctxrvED15");
    }

    /**
     * Remembers the last segment being edited when the editor is closed so the
     * target page view can show it when it's getting reloaded.
     */
    private void setCurrentEditorSegment(EditorState p_state,
            HttpServletRequest p_request)
    {
        String tuId = p_request.getParameter("curTuId");
        String tuvId = p_request.getParameter("curTuvId");
        String subId = p_request.getParameter("curSubId");

        if (tuId != null)
        {
            p_state.setTuId(Long.parseLong(tuId));
            p_state.setTuvId(Long.parseLong(tuvId));
            p_state.setSubId(Long.parseLong(subId));
        }
    }

    public static File getTuvCommentImg(long tuvId)
    {
        String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
        File parentFilePath = new File(termImgPath.toString());
        File[] files = parentFilePath.listFiles();

        if (files != null && files.length > 0)
        {
            for (int j = 0; j < files.length; j++)
            {
                File file = files[j];
                String fileName = file.getName();

                if (fileName.lastIndexOf(".") > 0)
                {
                    String tempName = fileName.substring(0,
                            fileName.lastIndexOf("."));
                    String nowImgName = "tuv_" + Long.toString(tuvId);

                    if (tempName.equals(nowImgName))
                    {
                        return file;
                    }
                }
            }
        }

        return null;
    }

    private void shareImg(long tuId, long tuvId, boolean overwrite, long p_jobId)
    {
        File img = getTuvCommentImg(tuvId);

        if (img != null)
        {
            String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg/";
            String name = img.getName();
            String type = name.substring(name.indexOf("."));

            TuImpl tu = null;
            try
            {
                tu = SegmentTuUtil.getTuById(tuId, p_jobId);
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
            for (Object obj : tu.getTuvs(true, p_jobId))
            {
                TuvImpl tuv = (TuvImpl) obj;
                if (tuvId != tuv.getId())
                {
                    if (!overwrite)
                    {
                        File img2 = getTuvCommentImg(tuv.getId());
                        if (img2 != null)
                        {
                            continue;
                        }
                    }

                    File trg = new File(termImgPath + "tuv_"
                            + Long.toString(tuv.getId()) + type);
                    try
                    {
                        FileUtil.copyFile(img, trg);
                    }
                    catch (IOException e)
                    {
                        CATEGORY.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private void executeCommentCommand(EditorState p_state, CommentView p_view,
            HttpServletRequest p_request, User p_user)
            throws EnvoyServletException
    {
        String action = p_request.getParameter("cmtAction");
        String title = p_request.getParameter("cmtTitle");
        String comment = p_request.getParameter("cmtComment");
        String priority = p_request.getParameter("cmtPriority");
        String status = p_request.getParameter("cmtStatus");
        String category = p_request.getParameter("cmtCategory");

        title = EditUtil.utf8ToUnicode(title);
        comment = EditUtil.utf8ToUnicode(comment);

        String tuId = p_request.getParameter("tuId");
        String tuvId = p_request.getParameter("tuvId");
        String subId = p_request.getParameter("subId");
        String shareComment = p_request.getParameter("cmtShare");
        String overwriteComment = p_request.getParameter("cmtOverwrite");

        boolean update = false;
        boolean share = "true".equalsIgnoreCase(shareComment);
        boolean overwrite = "true".equalsIgnoreCase(overwriteComment);

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Comment " + action + " id " + tuId + "_" + tuvId
                    + "_" + subId + " status=" + status);
        }

        if (action.equals("create"))
        {
            EditorHelper.createComment(p_state, p_view, title, comment,
                    priority, status, category, p_user.getUserId(), share,
                    overwrite);

            // Recompute target page view (new icon)
            p_state.setTargetPageHtml(null);
            update = true;
        }
        else if (action.equals("edit"))
        {
            EditorHelper.editComment(p_state, p_view, title, comment, priority,
                    status, category, p_user.getUserId(), share, overwrite);
            update = true;
        }
        else if (action.equals("add"))
        {
            EditorHelper.addComment(p_state, p_view, title, comment, priority,
                    status, category, p_user.getUserId(), share, overwrite);
            update = true;
        }
        else if (action.equals("closeAllComments"))
        {
            ArrayList currentIssues = (ArrayList) (p_request.getSession()
                    .getAttribute("currentIssues"));
            EditorHelper.closeAllComment(p_state, currentIssues,
                    p_user.getUserId());
        }

        if (share && update)
        {
            SourcePage sp = null;
            try
            {
                sp = ServerProxy.getPageManager().getSourcePage(
                        p_state.getSourcePageId());
            }
            catch (Exception e)
            {
                CATEGORY.error("Problem getting source page", e);
                throw new EnvoyServletException(e);
            }
            long jobId = sp.getJobId();
            shareImg(Long.parseLong(tuId), Long.parseLong(tuvId), overwrite,
                    jobId);

            TuImpl tu = null;
            try
            {
                tu = SegmentTuUtil.getTuById(Long.parseLong(tuId), jobId);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            if (tu == null)
            {
                CATEGORY.error("Can not find tu with id: " + tuId);
            }
            else
            {
                @SuppressWarnings("unchecked")
                Map<Long, Tuv> tuvs = tu.getTuvAsSet(true, jobId);
                for (Map.Entry<Long, Tuv> entry : tuvs.entrySet())
                {
                    Long localeId = (Long) entry.getKey();
                    TuvImpl tuv = (TuvImpl) entry.getValue();

                    if (tuv.getId() == Long.parseLong(tuvId))
                    {
                        continue;
                    }
                    TargetPage tp = sp.getTargetPageByLocaleId(localeId);
                    if (tp == null)
                    {
                        continue;
                    }

                    String hql = "from IssueImpl i where "
                            + "i.levelObjectTypeAsString = :type "
                            + "and i.levelObjectId = :oId";
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("type", "S");
                    map.put("oId", tuv.getId());

                    IssueImpl issue = (IssueImpl) HibernateUtil.getFirst(hql,
                            map);
                    if (issue == null)
                    {
                        String key = CommentHelper.makeLogicalKey(tp.getId(),
                                tu.getId(), tuv.getId(), 0);
                        issue = new IssueImpl(Issue.TYPE_SEGMENT, tuv.getId(),
                                title, priority, status, category,
                                p_user.getUserId(), comment, key);
                        issue.setShare(share);
                        issue.setOverwrite(overwrite);

                        HibernateUtil.saveOrUpdate(issue);
                    }
                    else if (overwrite)
                    {
                        issue.setTitle(title);
                        issue.setPriority(priority);
                        issue.setStatus(status);
                        issue.setCategory(category);
                        issue.addHistory(p_user.getUserId(), comment);
                        issue.setShare(share);
                        issue.setOverwrite(overwrite);

                        HibernateUtil.saveOrUpdate(issue);
                    }
                }
            }
        }

        // Refresh and re-sort comments - if they were shown.
        CommentThreadView view = p_state.getCommentThreads();
        if (view != null)
        {
            String sortedBy = view.getSortedBy();
            view = EditorHelper.getCommentThreads(p_state);
            view.sort(sortedBy);
            p_state.setCommentThreads(view);
        }

        p_request.setAttribute("cmtRefreshOtherPane", Boolean.TRUE);
    }

    private int getUiMode(EditorState p_state, boolean p_isTaskAssignee)
    {
        int editorMode = 0;
        if (p_state.isReviewMode())
        {
            editorMode = (p_state.isReadOnly() && p_isTaskAssignee) ? UIConstants.UIMODE_REVIEW_READ_ONLY
                    : UIConstants.UIMODE_REVIEW;
        }
        else
        {
            editorMode = UIConstants.UIMODE_EDITOR;
        }

        return editorMode;
    }

    private List<EditorState.PagePair> getPagePairList(HttpSession p_session,
            List<EditorState.PagePair> pages)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List<Long> sourcePageIdList = (List<Long>) sessionMgr
                .getAttribute("sourcePageIdList");

        List<EditorState.PagePair> newPages = new ArrayList<EditorState.PagePair>();
        if (sourcePageIdList != null && sourcePageIdList.size() > 0)
        {
            for (int i = 0; i < pages.size(); i++)
            {
                EditorState.PagePair page = pages.get(i);
                if (sourcePageIdList.contains(page.getSourcePageId()))
                {
                    newPages.add(page);
                }
            }
        }
        else
        {
            newPages = pages;
        }
        return newPages;
    }
}
