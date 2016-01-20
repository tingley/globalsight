/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.offline.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryState;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * SendDownloadFileHandler is responsible for creating a download file and
 * sending it to the user.
 */
public class SendDownloadFileHelper implements WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(SendDownloadFileHelper.class);

    // Constructor
    public SendDownloadFileHelper()
    {
    }

    public void doSendDownloadFile(HttpServletRequest p_request,
            HttpServletResponse p_response) throws EnvoyServletException,
            IOException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        String action = p_request.getParameter(DOWNLOAD_ACTION);
        OEMProcessStatus status = (OEMProcessStatus) sessionMgr
                .getAttribute(DOWNLOAD_STATUS);

        DownloadParams downloadParams = null;
        File tmpFile = null;

        if (action.equals(DOWNLOAD_ACTION_START_DOWNLOAD))
        {
            // Create DownloadParams.
            downloadParams = getDownloadParams(p_request);

            tmpFile = null;

            // Create a download zip file in the background.
            try
            {
                status = new OEMProcessStatus(downloadParams);
                OfflineEditManager odm = ServerProxy.getOfflineEditManager();
                odm.attachListener(status);
                odm.processDownloadRequest(downloadParams);
                sessionMgr.setAttribute(DOWNLOAD_STATUS, status);
                sessionMgr.setAttribute(DOWNLOAD_MANAGER, odm);
                sessionMgr.setAttribute(DOWNLOAD_PARAMS, downloadParams);
            }
            catch (Exception ex)
            {
                // exception logged at higher level
                throw new EnvoyServletException(ex);
            }
        }
        else if (action.equals(DOWNLOAD_ACTION_REFRESH))
        {
            // do nothing
        }
        else if (action.equals(DOWNLOAD_ACTION_CANCEL))
        {
            // Interrupt the backend process and clean up session.
            // We're going to show a new JSP and start all over.
            status.interrupt();

            sessionMgr.removeElement(DOWNLOAD_STATUS);
            sessionMgr.removeElement(DOWNLOAD_MANAGER);
        }
        else if (action.equals(DOWNLOAD_ACTION_DONE))
        {
            downloadParams = (DownloadParams) sessionMgr
                    .getAttribute(DOWNLOAD_PARAMS);

            if (status != null)
            {
                tmpFile = (File) status.getResults();
                String downloadFileName = "";
                // GBS-633 Downloaded folder naming convention:
                // <jobid>_<targetlocale> instead of <jobid>_<task id>
                // before
                if (downloadParams.isSupportFilesOnlyDownload())
                {
                    downloadFileName = downloadParams.getTruncatedJobName()
                            + AmbassadorDwUpConstants.FILE_NAME_BREAK
                            + getTargetLocaleCode(downloadParams)
                            + AmbassadorDwUpConstants.FILE_NAME_BREAK
                            + AmbassadorDwUpConstants.SUPPORTFILES_PACKAGE_SUFFIX
                            + ".zip";
                }
                else
                {
                    downloadFileName = downloadParams.getTruncatedJobName()
                            + AmbassadorDwUpConstants.FILE_NAME_BREAK
                            + getTargetLocaleCode(downloadParams) + ".zip";
                }

                if (tmpFile != null)
                {
                    // Set HTTP header
                    // Sample HTTP response header for a zip file
                    // Cache-Control: max-age=86400
                    // Connection: close
                    // Date: Tue, 26 Jun 2001 00:28:23 GMT
                    // Server: Apache/1.3.19 (Unix)
                    // Content-Length: 155836
                    // Content-Type: application/zip
                    // Expires: Wed, 27 Jun 2001 00:28:23 GMT
                    // Client-Date: Tue, 26 Jun 2001 00:41:29 GMT
                    // Client-Peer: 192.18.97.36:80
                    // Content-Disposition: attachment;
                    // filename=servlet-2_3-pfd2-docs.zip
                    // downloadFileName = URLEncoder.encode(downloadFileName,
                    // "UTF-8");
                    p_response.setContentType("application/zip");
                    p_response.setHeader("Content-Disposition",
                            "attachment; filename=\"" + downloadFileName
                                    + "\";");
                    if (p_request.isSecure())
                    {
                        PageHandler.setHeaderForHTTPSDownload(p_response);
                    }

                    p_response.setContentLength((int) tmpFile.length());

                    // Send the data to the client
                    byte[] inBuff = new byte[4096];
                    FileInputStream fis = new FileInputStream(tmpFile);
                    int bytesRead = 0;
                    while ((bytesRead = fis.read(inBuff)) != -1)
                    {
                        p_response.getOutputStream()
                                .write(inBuff, 0, bytesRead);
                    }

                    if (bytesRead > 0)
                    {
                        p_response.getOutputStream()
                                .write(inBuff, 0, bytesRead);
                    }

                    fis.close();

                    OfflineEditHelper.deleteFile(tmpFile);

                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("Sent a download package "
                                + downloadFileName + " to "
                                + p_request.getRemoteHost());
                    }
                }
                else
                // download aborted
                {
                    // Do nothing because download was aborted or no file was
                    // produced.
                    // If download was aborted, the progress bar's status
                    // message should
                    // should contain an updated error message (via speak()).
                    if (CATEGORY.isDebugEnabled())
                    {
                        CATEGORY.debug("Download was aborted. No package was produced for "
                                + downloadFileName
                                + " to "
                                + p_request.getRemoteHost());
                    }
                }
            }

            sessionMgr.removeElement(DOWNLOAD_STATUS);
            sessionMgr.removeElement(DOWNLOAD_MANAGER);
        }
    }

    private String getTargetLocaleCode(DownloadParams p_downloadParams)
    {
        String targetLocale = p_downloadParams.getTargetLocale().getLanguage()
                + "_" + p_downloadParams.getTargetLocale().getCountryCode();

        return targetLocale;
    }

    //
    // Private Methods
    //

    // create a DownloadParams object
    private DownloadParams getDownloadParams(HttpServletRequest p_request)
            throws EnvoyServletException
    {

        HttpSession session = p_request.getSession(false);

        Locale locale = (Locale) session.getAttribute(UILOCALE);
        String uiLocale = locale.getLanguage() + "_" + locale.getCountry();

        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

        // These are the defaults for a glossary only download
        List pageIdList = new ArrayList();
        List pageNameList = new ArrayList();
        List<Boolean> canUseUrlList = new ArrayList<Boolean>();
        int TMEditType = -1;
        Vector excludeTypes = null;
        int editorId = -1;
        int platformId = -1;
        String encoding = null;
        int ptagFormat = -1;
        int fileFormat = -1;
        int resInsMode = -1;

        Task task = null;
        try
        {
            long taskId;
            String taskIdParam = p_request
                    .getParameter(WebAppConstants.TASK_ID);
            if (taskIdParam != null && taskIdParam != "")
            {
                taskId = TaskHelper.getLong(taskIdParam);
                task = ServerProxy.getTaskManager().getTask(taskId);
            }
            else
            {
                task = getTask(p_request);
                task = ServerProxy.getTaskManager().getTask(task.getId());
            }
            TaskHelper.storeObject(session, WORK_OBJECT, task);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        // workflow id
        long workflowId = task.getWorkflow().getId();

        // job name
        String jobName = getJobName(task);

        // activity type
        Activity act = new Activity();

        try
        {
            act = ServerProxy.getJobHandler().getActivity(task.getTaskName());
        }
        catch (Exception e)
        {
        }

        // create page id and name list
        getPageIdList(p_request, pageIdList, pageNameList);
        if (pageIdList != null && pageIdList.size() <= 0)
        {
            pageIdList = pageNameList = null;
        }

        // can use url list (legacy stuff we never used but are
        // keeping for the future)
        if (pageIdList != null)
        {
            for (int i = 0; i < pageIdList.size(); i++)
            {
                canUseUrlList.add(Boolean.FALSE);
            }
        }

        List primarySourceFiles = getPSFList(p_request);

        // create glossary name list
        List supportFileList = getSupportFileList(p_request);

        // get stf list
        List stfList = getSTFList(p_request);

        // get download options for pages if pages are included
        if (pageIdList != null)
        {
            // allow exact match editing
            L10nProfile l10nProfile = task.getWorkflow().getJob()
                    .getL10nProfile();
            TMEditType = getEditAllState(p_request, l10nProfile);

            excludeTypes = l10nProfile.getTranslationMemoryProfile()
                    .getJobExcludeTuTypes();
            editorId = getEditorId(p_request);
            platformId = getPlatformId(p_request, editorId);
            encoding = p_request
                    .getParameter(OfflineConstants.ENCODING_SELECTOR);
            ptagFormat = getPtagFormat(p_request);
            fileFormat = getFileFormat(p_request);
            resInsMode = getResourceInsertionMode(p_request);
        }

        String displayExactMatch = p_request
                .getParameter(OfflineConstants.DISPLAY_EXACT_MATCH);

        DownloadParams params = new DownloadParams(jobName, null, "",
                Long.toString(workflowId), Long.toString(task.getId()),
                pageIdList, pageNameList, canUseUrlList, primarySourceFiles,
                stfList, editorId, platformId, encoding, ptagFormat, uiLocale,
                task.getSourceLocale(), task.getTargetLocale(), true,
                fileFormat, excludeTypes, TMEditType, supportFileList,
                resInsMode, user);

        params.setActivityType(act.getDisplayName());
        params.setJob(task.getWorkflow().getJob());
        params.setTermFormat(p_request
                .getParameter(OfflineConstants.TERM_SELECTOR));
        params.setConsolidateTermFiles(p_request
                .getParameter(OfflineConstants.CONSOLIDATE_TERM) != null);
        params.setPopulate100(p_request
                .getParameter(OfflineConstants.POPULATE_100) != null);
        params.setPopulateFuzzy(p_request
                .getParameter(OfflineConstants.POPULATE_FUZZY) != null);
        params.setDisplayExactMatch(displayExactMatch);
        params.setSessionId(p_request.getSession().getId());
        params.setConsolidateTmxFiles(p_request
                .getParameter(OfflineConstants.CONSOLIDATE_TMX) != null);
        params.setPreserveSourceFolder(p_request
                .getParameter(OfflineConstants.PRESERVE_SOURCE_FOLDER) != null);
        params.setIncludeXmlNodeContextInformation(p_request
                .getParameter(OfflineConstants.INCLUDE_XML_NODE_CONTEXT_INFORMATION) != null);
        params.setConsolidateFileType(p_request
                .getParameter("consolidateFileType"));
        if (p_request.getParameter("wordCountForDownload") != null
                && !p_request.getParameter("wordCountForDownload").equals(""))
            params.setWordCountForDownload(Integer.parseInt(p_request
                    .getParameter("wordCountForDownload")));
        params.setIncludeRepetitions(p_request
                .getParameter(OfflineConstants.INCLUDE_REPETITIONS) != null);
        params.setExcludeFullyLeveragedFiles(p_request
                .getParameter(OfflineConstants.EXCLUDE_FULLY_LEVERAGED_FILES) != null);
        params.setPenalizedReferenceTmPre(p_request
                .getParameter(UserParamNames.DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PRE) != null);
        params.setPenalizedReferenceTmPer(p_request
                .getParameter(UserParamNames.DOWNLOAD_OPTION_PENALIZED_REFERENCE_TM_PER) != null);
        boolean changeCreationId = false;
        boolean changeSeparateTMFile = false;
        try
        {
            String strchangeCreationId = p_request
                    .getParameter(OfflineConstants.CHANGE_CREATION_ID_FOR_MT_SEGMENTS);
            if (strchangeCreationId != null && strchangeCreationId.equals("on"))
            {
                changeCreationId = true;
            }

            String strchangeSeparateTMFile = p_request
                    .getParameter(OfflineConstants.CHANGE_SEPARATE_TM_FILE);
            if (strchangeSeparateTMFile != null
                    && strchangeSeparateTMFile.equals("on"))
            {
                changeSeparateTMFile = true;
            }
        }
        catch (Exception ex)
        {
        }
        params.setChangeCreationIdForMTSegments(changeCreationId);
        params.setChangeSeparateTMFile(changeSeparateTMFile);

        try
        {
            params.verify();
        }
        catch (Exception e)
        {
            // logged at higher level
            throw new EnvoyServletException(e);
        }

        return params;
    }

    private Task getTask(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);

        Task task = (Task) TaskHelper.retrieveObject(session, WORK_OBJECT);

        if (task == null)
        {
            EnvoyServletException e = new EnvoyServletException("TaskNotFound",
                    null, null);
            CATEGORY.error(e.getMessage(), e);
            throw e;
        }

        return task;
    }

    /**
     * Get source page ID list and corresponding external page ID list. Note
     * that "zero" word-count page will be ignored for offline downloading.
     */
    public void getAllPageIdList(Task task, List<Long> pageIdList,
            List<String> pageNameList)
    {
        List<SourcePage> sourcePages = task.getSourcePages();
        Long targetLocaleId = task.getTargetLocale().getIdAsLong();

        for (SourcePage page : sourcePages)
        {
            TargetPage tp = page.getTargetPageByLocaleId(targetLocaleId);
            if (tp != null && tp.getWordCount().getTotalWordCount() > 0)
            {
                pageIdList.add(page.getIdAsLong());
                pageNameList.add(page.getExternalPageId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void getPageIdList(HttpServletRequest p_request, List p_pageIdList,
            List p_pageNameList) throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        Task task = (Task) TaskHelper.retrieveObject(session, WORK_OBJECT);

        String[] idList = null;
        String acceptDownload = p_request
                .getParameter(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD);

        // If this is an accept-download?
        if (acceptDownload != null
                && acceptDownload.toLowerCase().equals("true"))
        {
            // Simulate page selection here.
            // we do not record page selections per/job in cookies,
            // the default for auto-download is ALL pages are included
            // Note: download is driven by the source page ids and the
            // target locale.
            List pages = task.getSourcePages();
            sortSourcePage(pages);

            for (Iterator it = pages.iterator(); it.hasNext();)
            {
                SourcePage page = (SourcePage) it.next();
                p_pageIdList.add(new Long(page.getId()));
                p_pageNameList.add(page.getExternalPageId());
            }
        }
        else
        // else we get them via the normal download page
        {
            idList = p_request
                    .getParameterValues(OfflineConstants.PAGE_CHECKBOXES);

            if (idList != null)
            {
                sortSourcePageIds(idList, p_pageIdList, p_pageNameList);
            }
        }
    }

    private void sortSourcePageIds(String[] idList, List p_pageIdList,
            List p_pageNameList)
    {
        List<SourcePage> sps = new ArrayList<SourcePage>();
        Long pageId = null;
        SourcePage page = null;

        for (int i = 0; idList != null && i < idList.length; i++)
        {
            try
            {
                pageId = new Long(idList[i]);
                page = (SourcePage) ServerProxy.getPageManager().getSourcePage(
                        pageId.longValue());
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }

            sps.add(page);
        }

        sortSourcePage(sps);

        for (SourcePage sp : sps)
        {
            p_pageIdList.add(sp.getId());
            p_pageNameList.add(sp.getExternalPageId());
        }
    }

    private void sortSourcePage(List<SourcePage> sps)
    {
        SortUtil.sort(sps, new Comparator<SourcePage>()
        {
            private String getMainFileName(String p_filename)
            {
                int index = p_filename.indexOf(")");
                if (index > 0 && p_filename.startsWith("("))
                {
                    index++;
                    while (Character.isSpace(p_filename.charAt(index)))
                    {
                        index++;
                    }

                    return p_filename.substring(index, p_filename.length());
                }

                return p_filename;
            }

            /**
             * Extracts the sub-file part of an MsOffice multipart file:
             * "(header) en_US/ppt.ppt" --&gt; "(header)".
             */
            private String getSubFileName(String p_filename)
            {
                int index = p_filename.indexOf(")");
                if (index > 0 && p_filename.startsWith("("))
                {
                    return p_filename.substring(0, p_filename.indexOf(")") + 1);
                }

                return "";
            }

            private int compareSubName(String subName, String subName2)
            {
                if (subName == null || subName.length() == 0)
                    return -1;

                if (subName2 == null || subName2.length() == 0)
                    return 1;

                if (subName.equalsIgnoreCase(subName2))
                    return 0;

                boolean mat1 = subName.matches("\\(\\D+\\d+\\)")
                        || subName.matches("\\(\\D+\\d+ hyperlinks\\)");
                boolean mat2 = subName2.matches("\\(\\D+\\d+\\)")
                        || subName2.matches("\\(\\D+\\d+ hyperlinks\\)");

                if (mat1 && !mat2)
                    return -1;

                if (!mat1 && !mat2)
                {
                    return subName.compareTo(subName2);
                }

                if (mat1 && mat2)
                {
                    Pattern p = Pattern.compile("\\((\\D+)(\\d+)");
                    Matcher m = p.matcher(subName);
                    Matcher m2 = p.matcher(subName2);
                    if (m.find() && m2.find())
                    {
                        String n1 = m.group(1);
                        String n2 = m2.group(1);

                        String num1 = m.group(2);
                        String num2 = m2.group(2);

                        int i1 = Integer.parseInt(num1);
                        int i2 = Integer.parseInt(num2);

                        if (!n1.equalsIgnoreCase(n2))
                            return n1.compareTo(n2);

                        if (i1 != i2)
                            return i1 - i2;

                        // (sheet11 hyperlinks) with (sheet11)
                        return subName.length() - subName2.length();
                    }
                }

                return 0;
            }

            @Override
            public int compare(SourcePage sp1, SourcePage sp2)
            {
                String aMainName = this.getMainFileName(sp1.getExternalPageId());
                String aSubName = this.getSubFileName(sp1.getExternalPageId());
                String bMainName = this.getMainFileName(sp2.getExternalPageId());
                String bSubName = this.getSubFileName(sp2.getExternalPageId());

                int rv = aMainName.compareTo(bMainName);
                if (rv == 0)
                {
                    rv = compareSubName(aSubName, bSubName);
                }

                return rv;
            }
        });
    }

    public List<Long> getAllPSFList(Task task) throws EnvoyServletException
    {
        List<Long> sourceForDownload = new ArrayList<Long>();
        Iterator<TargetPage> it = task.getWorkflow()
                .getTargetPages(PrimaryFile.UNEXTRACTED_FILE).iterator();
        while (it.hasNext())
        {
            TargetPage aPTF = it.next();
            sourceForDownload.add(aPTF.getSourcePage().getIdAsLong());
        }
        return sourceForDownload.size() > 0 ? sourceForDownload : null;
    }

    /* get the unextracted Primary Source Files */
    private List getPSFList(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        List<Long> sourceForDownload = new ArrayList<Long>();
        String acceptDownload = (String) p_request
                .getParameter(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD);

        // Is this is an accept-download request?
        if (acceptDownload != null
                && acceptDownload.toLowerCase().equals("true"))
        {
            // Simulate SecondaryTargetFile selection here.
            // we do not record file selections per/job in cookies,
            // the default for auto-download is to include all STFs
            Task task = getTask(p_request);
            Iterator it = task.getWorkflow()
                    .getTargetPages(PrimaryFile.UNEXTRACTED_FILE).iterator();
            while (it.hasNext())
            {
                TargetPage aPTF = (TargetPage) it.next();
                sourceForDownload.add(aPTF.getSourcePage().getIdAsLong());
            }
        }
        else
        {
            String[] userChoices = p_request
                    .getParameterValues(OfflineConstants.PRI_SOURCE_CHECKBOXES);
            if (userChoices != null)
            {
                for (int i = 0; i < userChoices.length; i++)
                {
                    sourceForDownload.add(new Long(userChoices[i]));
                }
            }
        }

        return sourceForDownload.size() > 0 ? sourceForDownload : null;
    }

    private int getEditAllState(HttpServletRequest p_request,
            L10nProfile p_l10nProfile) throws EnvoyServletException
    {
        String editExact = p_request
                .getParameter(OfflineConstants.TM_EDIT_TYPE);
        return getEditAllState(editExact, p_l10nProfile);
    }

    public int getEditAllState(String editExact, L10nProfile p_l10nProfile)
    {
        try
        {
            return StringUtil.isNotEmpty(editExact) ? Integer
                    .parseInt(editExact) : p_l10nProfile.getTMEditType();
        }
        catch (Exception e)
        {
            return AmbassadorDwUpConstants.TM_EDIT_TYPE_NONE;
        }
    }

    private int getEditorId(HttpServletRequest p_request)
            throws EnvoyServletException
    {

        String editor = p_request
                .getParameter(OfflineConstants.EDITOR_SELECTOR);
        return getEditorId(editor);

    }

    public int getEditorId(String editor)
    {
        int result;
        if (editor == null || editor.equals(OfflineConstants.EDITOR_WIN2000))
        {
            result = AmbassadorDwUpConstants.EDITOR_WIN_WORD2000;
        }
        else if (editor == null
                || editor.equals(OfflineConstants.EDITOR_WIN2000_ANDABOVE))
        {
            result = AmbassadorDwUpConstants.EDITOR_WIN_WORD2000_ANDABOVE;
        }
        else if (editor.equals(OfflineConstants.EDITOR_WIN97))
        {
            result = AmbassadorDwUpConstants.EDITOR_WIN_WORD97;
        }
        else if (editor.equals(OfflineConstants.EDITOR_MAC2001))
        {
            result = AmbassadorDwUpConstants.EDITOR_MAC_WORD2001;
        }
        else if (editor.equals(OfflineConstants.EDITOR_MAC98))
        {
            result = AmbassadorDwUpConstants.EDITOR_MAC_WORD98;
        }
        else if (editor.equals(OfflineConstants.EDITOR_OTHER))
        {
            result = AmbassadorDwUpConstants.EDITOR_OTHER;
        }
        else if (editor.equals(OfflineConstants.EDITOR_TRADOS_TAGEDITOR))
        {
            // for now, we just fake the constant since we invoke the
            // same RTF writer.
            result = AmbassadorDwUpConstants.EDITOR_WIN_WORD2000;
        }
        else if (editor.equals(OfflineConstants.EDITOR_XLF_VALUE))
        {
            result = AmbassadorDwUpConstants.EDITOR_XLIFF;
        }
        else if (editor.equals(OfflineConstants.EDITOR_OMEGAT))
        {
            result = AmbassadorDwUpConstants.EDITOR_OMEGAT;
        }
        else
        {
            String[] args = new String[2];
            args[0] = OfflineConstants.EDITOR_SELECTOR;
            args[1] = editor;
            throw new EnvoyServletException("WrongRequestParameter", args, null);
        }

        return result;
    }

    public int getPlatformId(HttpServletRequest p_request, int p_editorId)
    {
        int result;

        if (p_editorId == AmbassadorDwUpConstants.EDITOR_WIN_WORD2000
                || p_editorId == AmbassadorDwUpConstants.EDITOR_WIN_WORD97)
        {
            result = AmbassadorDwUpConstants.PLATFORM_WIN32;
        }
        else if (p_editorId == AmbassadorDwUpConstants.EDITOR_MAC_WORD2001
                || p_editorId == AmbassadorDwUpConstants.EDITOR_MAC_WORD98)
        {
            result = AmbassadorDwUpConstants.PLATFORM_MAC;
        }
        else
        {
            String userAgent = p_request.getHeader("User-Agent");

            if (userAgent.indexOf("Windows") != -1)
            {
                result = AmbassadorDwUpConstants.PLATFORM_WIN32;
            }
            else if (userAgent.indexOf("Mac") != -1)
            {
                result = AmbassadorDwUpConstants.PLATFORM_MAC;
            }
            else
            {
                result = AmbassadorDwUpConstants.PLATFORM_UNIX;
            }
        }

        return result;
    }

    private int getPtagFormat(HttpServletRequest p_request)
            throws EnvoyServletException
    {

        String format = p_request.getParameter(OfflineConstants.PTAG_SELECTOR);

        return getPtagFormat(format);
    }

    public int getPtagFormat(String format)
    {
        int result;
        if (format == null || format.equals(OfflineConstants.PTAG_COMPACT))
        {
            result = PseudoConstants.PSEUDO_COMPACT;
        }
        else if (format.equals(OfflineConstants.PTAG_VERBOSE))
        {
            result = PseudoConstants.PSEUDO_VERBOSE;
        }
        else
        {
            String[] args = new String[2];
            args[0] = OfflineConstants.PTAG_SELECTOR;
            args[1] = format;
            throw new EnvoyServletException("WrongRequestParameter", args, null);
        }

        return result;

    }

    private int getFileFormat(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String fileFormat = p_request
                .getParameter(OfflineConstants.FORMAT_SELECTOR);
        return getFileFormat(fileFormat);
    }

    public int getFileFormat(String fileFormat) throws EnvoyServletException
    {
        int result;

        if (fileFormat == null
                || fileFormat.equals(OfflineConstants.FORMAT_RTF))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_RTF_PARA_VIEW))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_RTF_TRADOS))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF;
        }
        else if (fileFormat
                .equals(OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF_OPTIMIZED;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_TEXT))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TXT;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_XLF_NAME_12))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_XLF_VALUE_20))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF20;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_TTX_VALUE))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TTX;
        }
        else if (fileFormat.equals(OfflineConstants.FORMAT_OMEGAT_VALUE))
        {
            result = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT;
        }
        else
        {
            String[] args = new String[2];
            args[0] = OfflineConstants.FORMAT_SELECTOR;
            args[1] = fileFormat;
            throw new EnvoyServletException("WrongRequestParameter", args, null);
        }

        return result;
    }

    private String getJobName(Task p_task)
    {
        String result = p_task.getWorkflow().getJob().getJobName();

        if (result == null || result.length() <= 0)
        {
            result = "NoJobName";
        }

        return result;
    }

    public List<Long> getAllSTFList(Task task) throws EnvoyServletException
    {
        List<Long> result = new ArrayList<Long>();
        Iterator it = task.getWorkflow().getSecondaryTargetFiles().iterator();
        while (it.hasNext())
        {
            SecondaryTargetFile aSTF = (SecondaryTargetFile) it.next();
            result.add(aSTF.getIdAsLong());
        }
        return result.size() > 0 ? result : null;
    }

    private List getSTFList(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        List<Long> result = new ArrayList<Long>();
        String acceptDownload = p_request
                .getParameter(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD);

        // Is this is an accept-download request?
        if (acceptDownload != null
                && acceptDownload.toLowerCase().equals("true"))
        {
            // Simulate SecondaryTargetFile selection here.
            // we do not record file selections per/job in cookies,
            // the default for auto-download is to include all STFs
            Task task = getTask(p_request);
            Iterator it = task.getWorkflow().getSecondaryTargetFiles()
                    .iterator();
            while (it.hasNext())
            {
                SecondaryTargetFile aSTF = (SecondaryTargetFile) it.next();
                result.add(aSTF.getIdAsLong());
            }
        }
        else
        {
            String[] userChoices = p_request
                    .getParameterValues(OfflineConstants.STF_CHECKBOXES);
            if (userChoices != null)
            {
                for (int i = 0; i < userChoices.length; i++)
                {
                    result.add(new Long(userChoices[i]));
                }
            }
        }

        return result.size() > 0 ? result : null;
    }

    @SuppressWarnings("unchecked")
    private List getSupportFileList(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        List result = new ArrayList();
        HttpSession session = p_request.getSession();
        String acceptDownload = p_request
                .getParameter(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD);

        // Is this is an accept-download request?
        if (acceptDownload != null
                && acceptDownload.toLowerCase().equals("true"))
        {
            // Simulate glossary selection here.
            // we do not record glossary selections per/job in cookies,
            // the default for auto-download is no glossaries included
            // no-op
        }
        else
        {
            GlossaryState glossaryState = (GlossaryState) session
                    .getAttribute(OfflineConstants.DOWNLOAD_GLOSSARY_STATE);
            List allGlossaryFileList = glossaryState.getGlossaries();

            if (allGlossaryFileList == null)
            {
                EnvoyServletException e = new EnvoyServletException(
                        "GlossaryListNotFound", null, null);
                throw e;
            }
            else
            {
                String[] idList = p_request
                        .getParameterValues(OfflineConstants.GLOSSARY_CHECKBOXES);
                if (idList != null)
                {
                    for (int glossaryId, i = 0; i < idList.length; i++)
                    {
                        try
                        {
                            glossaryId = Integer.parseInt((idList[i]));
                        }
                        catch (NumberFormatException e)
                        {
                            throw new EnvoyServletException(e);
                        }

                        result.add(allGlossaryFileList.get(glossaryId));
                    }
                }
            }
        }

        return result.size() > 0 ? result : null;
    }

    public List getAllSupportFileList(Task task)
    {
        List result = new ArrayList();
        DownloadPageHandler handler = new DownloadPageHandler();
        GlossaryState glossaryState = handler.getGlossaryState(task);
        result = glossaryState.getGlossaries();

        return result.size() > 0 ? result : null;
    }

    private int getResourceInsertionMode(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String resInsMode = p_request
                .getParameter(OfflineConstants.RES_INS_SELECTOR);
        return getResourceInsertionMode(resInsMode);
    }

    public int getResourceInsertionMode(String resInsMode)
            throws EnvoyServletException
    {
        if (resInsMode == null
                || resInsMode.equals(OfflineConstants.RES_INS_ATNS))
        {
            return AmbassadorDwUpConstants.MAKE_RES_ATNS;
        }
        else if (resInsMode.equals(OfflineConstants.RES_INS_LINK))
        {
            return AmbassadorDwUpConstants.MAKE_SINGLE_RES_LINK;
        }
        else if (resInsMode.equals(OfflineConstants.RES_INS_TMX_PLAIN))
        {
            return AmbassadorDwUpConstants.MAKE_RES_TMX_PLAIN;
        }
        else if (resInsMode.equals(OfflineConstants.RES_INS_TMX_14B))
        {
            return AmbassadorDwUpConstants.MAKE_RES_TMX_14B;
        }
        else if (resInsMode.equals(OfflineConstants.RES_INX_TMX_BOTH))
        {
            return AmbassadorDwUpConstants.MAKE_RES_TMX_BOTH;
        }
        else if (resInsMode.equals(OfflineConstants.RES_INS_NONE))
        {
            return AmbassadorDwUpConstants.MAKE_RES_NONE;
        }
        else
        {
            String[] args = new String[2];
            args[0] = OfflineConstants.RES_INS_SELECTOR;
            args[1] = resInsMode;
            throw new EnvoyServletException("WrongRequestParameter", args, null);
        }

    }
}
