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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.hibernate.Transaction;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.ExtractedFile;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UpdatedSourcePage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.statistics.StatisticsService;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.everest.util.applet.AddFileVo;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ProcessStatus;
import com.globalsight.util.SortUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.zip.ZipIt;

public class AddSourceHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(AddSourceHandler.class);
    public static final String CAN_ADD_DELETE_SOURCE_FILES = "canAddDeleteSourceFiles";
    public static final String BEFORE_DELETE_SOURCE_FILES = "beforeDeleteSourceFiles";
    public static final String DELETE_SOURCE_FILES = "deleteSourceFiles";
    public static final String DOWNLOAD_SOURCE_FILES = "downloadSourceFiles";
    public static final String UPLOAD_SOURCE_FILES = "uploadSourceFiles";
    public static final String CAN_UPDATE_WORKFLOW = "canUpdateWorkFlow";
    public static final String SHOW_DELETE_PROGRESS = "showDeleteProgress";
    public static final String SHOW_UPDATE_PROGRESS = "showUpdateProgress";
    public static final String CHECK_PAGE_EXIST = "checkPageExist";

    static public final String ZIP_FILE_NAME = "AllFiles.zip";
    static public final int BUFSIZE = 4096;
    private static int SPLIT_SIZE = 2 * 1000 * 1024; // 2M

    private ResourceBundle bundle = null;
    private static Map<String, Integer[]> DELETE_PROGRESS = new HashMap<String, Integer[]>();
    private static Map<String, ProcessStatus> UPDATE_PROGRESS = new HashMap<String, ProcessStatus>();

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
	{
		HttpSession session = request.getSession();
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		String id = request.getParameter("jobId");
		String currentId = CompanyThreadLocal.getInstance().getValue();
		request.setAttribute("jobId", id);
		request.setAttribute("companyId", currentId);
		try
		{
			String targetLocaleId = request
					.getParameter(JobManagementHandler.PAGE_TARGET_LOCAL);
			if (targetLocaleId != null)
			{
				GlobalSightLocale locale = ServerProxy.getLocaleManager()
						.getLocaleById(Long.parseLong(targetLocaleId));
				sessionMgr.setAttribute("targetLocale", locale);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		String pageSearchText = request
				.getParameter(JobManagementHandler.PAGE_SEARCH_TEXT);
		try
		{
			if (pageSearchText != null)
			{
				pageSearchText = URLDecoder.decode(pageSearchText, "UTF-8");
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		sessionMgr.setAttribute(JobManagementHandler.PAGE_SEARCH_TEXT,
				pageSearchText);
		bundle = PageHandler.getBundle(request.getSession());
	}

    @ActionHandler(action = CAN_UPDATE_WORKFLOW, formClass = "")
    public void canUpdateWorkflow(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String id = request.getParameter("jobId");
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            if (Job.ADD_FILE.equalsIgnoreCase(job.getState()))
            {
                out.write(bundle.getString("msg_access_workflow_no"));
            }

            pageReturn();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    @ActionHandler(action = CHECK_PAGE_EXIST, formClass = "")
    public void checkPageExist(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String id = request.getParameter("sourcePageId");
            if (id != null)
            {
                SourcePage page = HibernateUtil.get(SourcePage.class,
                        Long.parseLong(id));
                if (page == null)
                {
                    ResourceBundle bundle = getBundle(request.getSession(false));
                    out.write(bundle.getString("msg_no_source_page"));
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    @ActionHandler(action = CAN_ADD_DELETE_SOURCE_FILES, formClass = "")
    public void canAddDeleteSourceFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String id = request.getParameter("jobId");
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            String errorMsgkey = job.canAddSourceFiles();
            if (errorMsgkey != null)
            {
                ResourceBundle bundle = getBundle(request.getSession(false));
                out.write(bundle.getString(errorMsgkey));
            }
            pageReturn();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    private List<Long> getOtherSourcePage(Job job, List<Long> pIds)
    {
        List<Long> otherIds = new ArrayList<Long>();
        Set<Long> ids = new HashSet<Long>();
        for (Long id : pIds)
        {
            SourcePage sPage = HibernateUtil.get(SourcePage.class, id);
            File file = sPage.getFile();

            for (Object ob : job.getSourcePages())
            {
                SourcePage page = (SourcePage) ob;
                File f = page.getFile();

                if (pIds.contains(page.getId()))
                {
                    continue;
                }

                if (file.getPath().equals(f.getPath()))
                {
                    ids.add(page.getId());
                }
            }
        }

        otherIds.addAll(ids);
        return otherIds;
    }

    @ActionHandler(action = BEFORE_DELETE_SOURCE_FILES, formClass = "")
    public void beforeDeleteSourceFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String id = request.getParameter("jobId");
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            
            String pIds = request.getParameter("pIds");
            String[] allIds = pIds.split(",");

            String errorMsgkey = job.canAddSourceFiles();
            if (errorMsgkey == null)
                errorMsgkey = xliffCheck(allIds);
            
            if (errorMsgkey != null)
            {
                ResourceBundle bundle = getBundle(request.getSession(false));
                String s = "({\"error\" : "
                        + JsonUtil.toJson(bundle.getString(errorMsgkey)) + "})";
                out.write(s);
            }
            else
            {
                List<Long> ids = new ArrayList<Long>();
                for (String pId : allIds)
                {
                    ids.add(Long.parseLong(pId));
                }

                List<Long> otherIds = getOtherSourcePage(job, ids);

                if (ids.size() + otherIds.size() >= job.getSourcePages().size())
                {
                    String msg = bundle.getString("msg_change_all_file");
                    String s = "({\"confirm\" : " + JsonUtil.toJson(msg) + "})";
                    out.write(s);
                }
                else if (otherIds.size() > 0)
                {
                    StringBuffer msg = new StringBuffer(
                            bundle.getString("msg_remove_other_file"));

                    msg.append("\n\n");
                    for (int i = 0; i < otherIds.size(); i++)
                    {
                        Long pId = otherIds.get(i);
                        SourcePage page = HibernateUtil.get(SourcePage.class,
                                pId);
                        msg.append(i + 1).append(". ")
                                .append(page.getDisplayPageName()).append("\n");
                    }

                    String s = "({\"confirm\" : "
                            + JsonUtil.toJson(msg.toString()) + "})";

                    out.write(s);
                }
            }

            pageReturn();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    private String xliffCheck(String[] p_ids)
    {
        SourcePage sp = null;
        
        boolean fail = false;
        String externalPageId = "", tmp = "";
        FileProfileImpl fp = null;
        long fpId = -1l;
        String baseDocDir = AmbFileStoragePathUtils.getCxeDocDirPath();
        String filePath = "";
        try
        {
            for (String id : p_ids)
            {
                sp = ServerProxy.getPageManager().getSourcePage(Long.parseLong(id));
                externalPageId = sp.getExternalPageId();
                tmp = externalPageId.toLowerCase();
                if (!XliffFileUtil.isXliffFile(externalPageId))
                    continue;

                filePath = baseDocDir + File.separator + externalPageId;

                fpId = sp.getRequest().getFileProfileId();
                fp = (FileProfileImpl) ServerProxy
                        .getFileProfilePersistenceManager().getFileProfileById(
                                fpId, false);

                if ((!fp.isActive() && fp.getKnownFormatTypeId() == XliffFileUtil.KNOWN_FILE_FORMAT_XLIFF)
                        || XliffFileUtil.containsFileTag(filePath))
                {
                    fail = true;
                }
            }
            return fail ? "msg_removing_file_error" : null;
        }
        catch (Exception e)
        {
            return e.getMessage();
        }
    }

    private Map<String, String> updateSouceFiles(long jobId,
            List<String> paths, String randomNum)
    {
        Map<String, String> error = new HashMap<String, String>();

        if (paths.size() == 0)
        {
            return error;
        }

        JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
        String errorMsgKey = job.canAddSourceFiles();
        if (errorMsgKey != null)
        {
            throw new IllegalArgumentException(bundle.getString(errorMsgKey));
        }

        AddFileVo vo = new AddFileVo();
        vo.setJobId(jobId);
        vo.setLocale(job.getSourceLocale().toString());
        List<Long> pageIds = new ArrayList<Long>();
        List<Long> fileProfileIds = new ArrayList<Long>();
        List<String> filePaths = new ArrayList<String>();
        List<UpdatedSourcePage> updatedPages = new ArrayList<UpdatedSourcePage>();

        for (String path : paths)
        {
            File f = new File(path);
            String name = f.getName();
            try
            {
                List<Long> ids = getPageId(job, name);

                SourcePage page = HibernateUtil.get(SourcePage.class,
                        ids.get(0));
                String externalPageId = page.getExternalPageId().replace("\\",
                        "/");
                // Remove source locale
                String tmpStr = externalPageId.substring(externalPageId
                        .indexOf("/") + 1);
                boolean fromWebService = false;
                if (tmpStr.startsWith("webservice/"))
                {
                    fromWebService = true;
                    // Remove "webservice"
                    tmpStr = tmpStr.substring(tmpStr.indexOf("/") + 1);
                }
                String originalJobName = tmpStr.substring(0,
                        tmpStr.indexOf("/"));
                String fPath = tmpStr.substring(tmpStr.indexOf("/") + 1);

                GlobalSightLocale locale = job.getSourceLocale();
                StringBuffer nPath = new StringBuffer(locale.toString());
                nPath.append(File.separator);
                if (fromWebService)
                {
                    nPath.append("webservice").append(File.separator);
                }

                String newPath = nPath.append(originalJobName)
                        .append(File.separator).append(fPath).toString();

                File targetFile = new File(
                        AmbFileStoragePathUtils.getCxeDocDir(), newPath);

                if (!targetFile.exists())
                {
                    targetFile = page.getFile();
                }

                FileUtils.copyFile(new File(path), targetFile);

                long fpId = page.getRequest().getFileProfileId();
                if (fpId < 0
                        || HibernateUtil
                                .get(FileProfileImpl.class, fpId, false) == null)
                {
                    error.put(name,
                            bundle.getString("msg_no_mapped_file_profile"));
                    continue;
                }

                UpdatedSourcePage updatedSourcePage = new UpdatedSourcePage();
                updatedSourcePage.init(page);
                updatedSourcePage.setExternalPageId(newPath);
                updatedPages.add(updatedSourcePage);

                pageIds.addAll(ids);
                fileProfileIds.add(fpId);
                filePaths.add(newPath);
            }
            catch (Exception e)
            {
                if (e instanceof IllegalArgumentException)
                {

                }
                else
                {
                    logger.error(e.getMessage(), e);
                }

                error.put(name, e.getMessage());
                continue;
            }
        }

        if (pageIds.size() > 0)
        {

            try
            {
                deletePages(jobId, pageIds, randomNum);
                // UpdateSourcePageManager.removeUpdatedPage(jobId);
                HibernateUtil.save(updatedPages);
                vo.setFilePaths(filePaths);
                vo.setFileProfileIds(fileProfileIds);
                JmsHelper.sendMessageToQueue(vo,
                        JmsHelper.JMS_ADD_SOURCE_FILE_QUEUE);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }

        return error;
    }

    private List<Long> getPageId(JobImpl job, String name)
    {
        String regex = "^\\(([\\d,]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(name);

        List<Long> pageIds = new ArrayList<Long>();
        if (match.find())
        {
            String ids = match.group(1);
            String[] allIds = ids.split(",");

            boolean found = true;
            for (String pId : allIds)
            {
                long id = Long.parseLong(pId);
                SourcePage page = HibernateUtil.get(SourcePage.class, id);
                if (page == null)
                {
                    found = false;
                    break;
                }

                pageIds.add(id);
            }

            if (found)
            {
                return pageIds;
            }
        }

        pageIds = new ArrayList<Long>();

        String sName = name.replaceFirst(regex, "");

        String path = null;
        for (Object ob : job.getSourcePages())
        {
            SourcePage page = (SourcePage) ob;
            File f = page.getFile();
            String pName = f.getName();
            if (name.equals(pName) || sName.equals(pName))
            {
                if (path == null)
                {
                    path = f.getPath();
                }

                if (!path.equals(f.getPath()))
                {
                    throw new IllegalArgumentException(
                            bundle.getString("msg_no_mapped_file"));
                }

                pageIds.add(page.getId());
            }
        }

        if (pageIds.size() == 0)
        {
            throw new IllegalArgumentException(
                    bundle.getString("msg_no_mapped_file"));
        }

        return pageIds;
    }

    @ActionHandler(action = UPLOAD_SOURCE_FILES, formClass = "")
    public void updateSouceFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Update souce files ...");

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String randomNum = request.getParameter("randomNum");
            ProcessStatus status = UPDATE_PROGRESS.get(randomNum);
            if (status == null)
            {
                status = new ProcessStatus();
                UPDATE_PROGRESS.put(randomNum, status);
            }

            FileUploader uploader = new FileUploader();
            uploader.addListener(status);
            File file = uploader.upload(request);
            if ("".equals(uploader.getName()))
            {
                throw new IllegalArgumentException(
                        bundle.getString("msg_no_file_remove"));
            }

            String jobId = uploader.getFieldValue("jobId");

            String sysTemp = System.getProperty("java.io.tmpdir");
            String tempRoot = sysTemp + File.separator + "globalsight";
            File dir = new File(tempRoot);
            dir.mkdirs();
            File trg = new File(dir.getPath() + File.separator
                    + uploader.getName());
            FileUtil.copyFile(file, trg);

            List<String> paths = new ArrayList<String>();
            if (trg.getName().endsWith(".zip"))
            {
                List<String> names = ZipIt.unpackZipPackage(trg.getPath());
                for (String name : names)
                {
                    paths.add(tempRoot + File.separator + name);
                }
                trg.delete();
            }
            else
            {
                paths.add(trg.getPath());
            }

            if (file.exists())
            {
                file.delete();
            }

            StringBuffer result = new StringBuffer();

            Map<String, String> error = updateSouceFiles(Long.parseLong(jobId),
                    paths, randomNum);
            if (error.size() > 0)
            {
                result.append("<html><body onload='parent.popupUploadErrorMessage()'><div id='uploadFileErroInfo' style='margin:20px 50px'>");
                StringBuffer html = new StringBuffer();
                html.append(bundle.getString("msg_add_failed"));
                html.append("<table CLASS='listborder' CELLPADDING='3' style='width:480px; ' CELLSPACING='0' BORDER='0'>");
                html.append("<tr CLASS=\"tableHeadingBasic\" style='padding:5px; height:24px;'><td>");
                html.append(bundle.getString("lb_file")).append("</td><td>");
                html.append(bundle.getString("lb_error")).append("</td></tr>");
                html.append("<tr height='3'/>");

                int i = 0;
                for (String path : error.keySet())
                {
                    String style = i % 2 == 0 ? "tableRowOdd" : "tableRowEven";
                    html.append("<tr CLASS='").append(style);
                    html.append("'style='padding:5px; height:24px;'><td>")
                            .append(path).append("</td><td>");
                    html.append(error.get(path)).append("</td></tr>");

                    i++;
                }

                result.append(html.toString());
                result.append("</div></body></html>");
            }
            else
            {
                result.append("<script type='text/javascript'>");
                result.append("parent.refreshJobPage();");
                result.append("</script>");
            }

            out.write(result.toString());
        }
        catch (Exception e)
        {
            StringBuffer result = new StringBuffer();
            result.append("<script type='text/javascript'>");
            result.append("alert(\"" + e.getMessage() + "\")");
            result.append("</script>");
            out.write(result.toString());
            logger.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
            out.flush();
            pageReturn();
        }

        logger.debug("Updating souce files finished.");
    }

    private void deletePages(long jobId, List<Long> ids, String randomNum)
            throws Exception
    {
        JobImpl job = HibernateUtil.get(JobImpl.class, jobId);

        int deleteNum = 0;

        Integer[] nums = new Integer[2];
        nums[0] = 0;
        nums[1] = ids.size();
        DELETE_PROGRESS.put(randomNum, nums);

        for (long pid : ids)
        {
            deleteNum++;

            Transaction tx = HibernateUtil.getTransaction();

            try
            {
                SourcePage page = HibernateUtil.get(SourcePage.class, pid);
                RequestImpl r = (RequestImpl) page.getRequest();

                List<TargetPage> tPages = new ArrayList<TargetPage>();
                tPages.addAll(page.getTargetPages());

                for (TargetPage tPage : tPages)
                {
                    page.getTargetPages().remove(tPage);

                    WorkflowImpl w = (WorkflowImpl) tPage.getWorkflowInstance();
                    if (w != null)
                    {
                        Set tps = w.getTargetPagesSet();
                        tps.remove(tPage);
                        w.setTargetPagesSet(tps);
                        HibernateUtil.update(w);                        
                    }

                    ExtractedFile f = tPage.getExtractedFile();
                    if (f != null)
                    {
                        f.getLeverageGroups().clear();
                        HibernateUtil.update(tPage);
                    }

                    HibernateUtil.update(page);
                    HibernateUtil.delete(tPage);
                }

                ExtractedFile f = page.getExtractedFile();
                if (f != null)
                {
                    for (Object ob : f.getLeverageGroups())
                    {
                        LeverageGroupImpl lg = (LeverageGroupImpl) ob;
                        lg.getSourcePageSet().remove(page);
                        HibernateUtil.update(lg);
                    }

                    f.getLeverageGroups().clear();
                    HibernateUtil.update(page);

                    String hql2 = "from PageTemplate p where p.sourcePage.id="
                            + page.getId();
                    List<PageTemplate> templates = (List<PageTemplate>) HibernateUtil
                            .search(hql2);
                    for (PageTemplate p : templates)
                    {
                        String hql = "from TemplatePart t where t.pageTemplate.id="
                                + p.getId();
                        HibernateUtil.delete(HibernateUtil.search(hql));
                        HibernateUtil.delete(p);
                    }
                }

                long num = r.getBatchInfo().getPageNumber();

                job.removeRequest(r);

                for (Object ob : job.getRequestSet())
                {
                    Request req = (Request) ob;
                    BatchInfo info = req.getBatchInfo();
                    info.setPageCount(job.getPageCount());

                    long n = info.getPageNumber();
                    if (n > num)
                    {
                        info.setPageNumber(n - 1);
                    }
                }

                HibernateUtil.update(job);
                HibernateUtil.delete(r);
                HibernateUtil.delete(page);

                HibernateUtil.commit(tx);

                nums[0] = deleteNum;
                DELETE_PROGRESS.put(randomNum, nums);
            }
            catch (Exception e)
            {
                nums[0] = -1;
                DELETE_PROGRESS.put(randomNum, nums);

                HibernateUtil.rollback(tx);
                logger.error(e.getMessage(), e);
                throw e;
            }
        }

        // Update the targetPage word-counts after delete files.
        List<Workflow> workflowList = new ArrayList<Workflow>(
                job.getWorkflows());
        for (Workflow workflow : workflowList)
        {
            StatisticsService.calculateTargetPagesWordCount(workflow, job
                    .getL10nProfile().getTranslationMemoryProfile()
                    .getJobExcludeTuTypes());
        }
        // Update the workflow word-counts after delete files.
        StatisticsService.calculateWorkflowStatistics(workflowList, job
                .getL10nProfile().getTranslationMemoryProfile()
                .getJobExcludeTuTypes());
        SegmentTuTuvCacheManager.clearCache();

        nums[0] = -1;
        DELETE_PROGRESS.put(randomNum, nums);
    }

    @ActionHandler(action = SHOW_DELETE_PROGRESS, formClass = "")
    public void showDeleteProgress(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String randomNum = request.getParameter("randomNum");
            String number = request.getParameter("number");

            Integer[] nums = DELETE_PROGRESS.get(randomNum);

            int i = 100;
            while (nums == null || nums[0] == Integer.parseInt(number))
            {
                i--;
                Thread.sleep(200);
                nums = DELETE_PROGRESS.get(randomNum);

                if (i < 0)
                {
                    break;
                }
            }

            int num = 0;
            int total = 10;

            if (nums != null)
            {
                num = nums[0];
                total = nums[1];
            }

            if (num > -1)
            {
                String s = "({\"number\" : " + JsonUtil.toJson(num)
                        + ", \"total\" : " + JsonUtil.toJson(total) + "})";
                out.write(s);
            }

            pageReturn();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    @ActionHandler(action = SHOW_UPDATE_PROGRESS, formClass = "")
    public void showUpdateProgress(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String randomNum = request.getParameter("randomNum");
            String number = request.getParameter("number");

            ProcessStatus status = UPDATE_PROGRESS.get(randomNum);

            int i = 100;
            while (status == null
                    || status.getPercentage() == Integer.parseInt(number))
            {
                i--;
                Thread.sleep(200);

                if (i < 0)
                {
                    break;
                }
            }

            long num = 0;
            long total = 10;

            if (status != null)
            {
                if (!status.isFinished())
                {
                    num = status.getSize();
                    total = status.getTotalSize() * 3;
                }
                else
                {
                    Integer[] nums = DELETE_PROGRESS.get(randomNum);

                    i = 100;
                    while (nums == null || nums[0] == Integer.parseInt(number))
                    {
                        i--;
                        Thread.sleep(200);
                        nums = DELETE_PROGRESS.get(randomNum);

                        if (i < 0)
                        {
                            break;
                        }
                    }

                    num = -1;
                    if (nums != null && nums[0] > -1)
                    {
                        num = nums[0] * 2 + nums[1];
                        total = nums[1] * 3;
                    }
                }

                if (num != -1)
                {
                    String s = "({\"number\" : " + JsonUtil.toJson(num)
                            + ", \"total\" : " + JsonUtil.toJson(total) + "})";
                    out.write(s);
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    @ActionHandler(action = DELETE_SOURCE_FILES, formClass = "")
    public void deleteSourceFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try
        {
            String id = request.getParameter("jobId");
            String[] ids = request.getParameter("pageIds").split(",");
            List<Long> pIds = new ArrayList<Long>();
            for (String pId : ids)
            {
                pIds.add(Long.parseLong(pId));
            }

            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(id));
            pIds.addAll(getOtherSourcePage(job, pIds));
            if (pIds.size() >= job.getSourcePages().size())
            {
                HttpSession session = request.getSession(false);
                SessionManager sessionMgr = (SessionManager) session
                        .getAttribute(SESSION_MANAGER);
                String userId = ((User) sessionMgr
                        .getAttribute(WebAppConstants.USER)).getUserId();
                WorkflowHandlerHelper.cancelJob(userId,
                        WorkflowHandlerHelper.getJobById(Long.parseLong(id)),
                        null);

                String s = "({\"discard\" : " + JsonUtil.toJson("true") + "})";
                out.write(s);
            }
            else
            {
                deletePages(Long.parseLong(id), pIds,
                        request.getParameter("randomNum"));
            }

            pageReturn();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }

            pageReturn();
        }
    }

    public static File getZip(String[] ids) throws Exception
    {
        List<SourcePage> sPages = null;
        Map<File, List<Long>> files = new HashMap<File, List<Long>>();

        for (String id : ids)
        {
            SourcePage page = HibernateUtil.get(SourcePage.class,
                    Long.parseLong(id));

            File file = page.getFile();
            List<Long> pIds = files.get(file);
            if (pIds != null)
            {
                if (!pIds.contains(page.getId()))
                {
                    pIds.add(page.getId());
                }
            }
            else
            {
                pIds = new ArrayList<Long>();
                files.put(file, pIds);

                if (sPages == null)
                {
                    Job job = page.getRequest().getJob();
                    sPages = (List) job.getSourcePages();
                }

                for (SourcePage sPage : sPages)
                {
                    File f = sPage.getFile();
                    if (f != null)
                    {
                        if (f.getPath().equals(file.getPath()))
                        {
                            pIds.add(sPage.getId());
                        }
                    }
                }

                SortUtil.sort(pIds);
            }
        }

        File downLoadFile = File.createTempFile("GSDownloadSource", ".zip");
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(
                downLoadFile));

        ZipEntry[] zipEntries = new ZipEntry[files.size()];
        String fileName = null;

        int i = 0;
        for (File file : files.keySet())
        {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            long length = file.length();

            List<Long> allIds = files.get(file);
            StringBuffer prefix = new StringBuffer("(");

            boolean start = true;
            for (Long id : allIds)
            {
                if (!start)
                {
                    prefix.append(",");
                }
                prefix.append(id);
                start = false;
            }
            prefix.append(")").append(file.getName());

            // create new zip entry
            fileName = prefix.toString();
            zipEntries[i] = new ZipEntry(fileName);

            // put the entry into zip file
            zos.putNextEntry(zipEntries[i]);

            int chunks = (int) length / SPLIT_SIZE;
            for (int m = 0; m < chunks; m++)
            {
                byte[] arr = new byte[SPLIT_SIZE];
                dis.readFully(arr);
                // write bytes of zip entry
                zos.write(arr);
            }
            if ((int) length % SPLIT_SIZE > 0)
            {
                byte[] arr = new byte[(int) length % SPLIT_SIZE];
                dis.readFully(arr);
                // write bytes of zip entry
                zos.write(arr);

            }
            dis.close();

            i++;
        }
        // close zos
        zos.close();

        return downLoadFile;
    }

    @ActionHandler(action = DOWNLOAD_SOURCE_FILES, formClass = "")
    public void downloadSourceFiles(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        logger.debug("Download files...");

        try
        {
            String[] ids = request.getParameter("pageIds").split(",");
            File downLoadFile = getZip(ids);
            CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
            commentFilesDownload.sendFileToClient(request, response,
                    ZIP_FILE_NAME, downLoadFile);
            downLoadFile.delete();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            pageReturn();
        }

        logger.debug("Download files finished.");
    }
}
