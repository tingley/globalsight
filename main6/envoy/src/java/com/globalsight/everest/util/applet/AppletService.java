package com.globalsight.everest.util.applet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.AddingSourcePage;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.FileUploader;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;

public class AppletService extends HttpServlet
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(AppletService.class);
    private static final long serialVersionUID = 1L;
    private HttpServletRequest request;
    HttpServletResponse response;
    // private PrintWriter writer;
    // private ServletOutputStream out;
    private long companyId;

    public void service(HttpServletRequest request, HttpServletResponse response)
    {
        this.request = request;
        this.response = response;
        response.setCharacterEncoding("utf-8");
        setCompanyId();

        String method = request.getParameter("action");
        try
        {
            // writer = response.getWriter();
            AppletService.class.getMethod(method).invoke(AppletService.this);
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not invoke the method:" + method);
        }
    }
    
    public void canAddSourceFile()
    {
        PrintWriter writer = null;
        try
        {
            String jobId = request.getParameter("jobId");
            JobImpl job = HibernateUtil.get(JobImpl.class, Long.parseLong(jobId));
            String errorMsgKey = job.canAddSourceFiles();
            if (errorMsgKey != null)
            {
                writer = response.getWriter();
                writer.write(errorMsgKey);
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error(ex);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }

    public void addSouceFile()
    {
        try
        {
            String xml = request.getParameter("xml");
            AddFileVo vo = XmlUtil.string2Object(AddFileVo.class, xml);
            
            List<String> filePaths = vo.getFilePaths();
            List<String> realPaths = new ArrayList<String>();
            List<Long> fpIds = vo.getFileProfileIds();
            String locale = vo.getLocale();
            
            JobImpl job = HibernateUtil.get(JobImpl.class, vo.getJobId());
            String errorMsgKey = job.canAddSourceFiles();
            if (errorMsgKey != null)
            {
                ResourceBundle b = PageHandler.getBundle(request.getSession());
                CATEGORY.error(b.getString(errorMsgKey));
                return;
            }
            
            if (locale == null)
            {
                locale = job.getSourceLocale().toString();
                vo.setLocale(locale);
            }
            
            File root = AmbFileStoragePathUtils.getCxeDocDir();
            for (String path : filePaths)
            {
                File targetFile = new File(root, path);
                
                if (!targetFile.exists())
                {
                    StringBuffer newPath = new StringBuffer(locale).append(File.separator);
                    
                    if (job.isFromWebService())
                    {
                        newPath.append("webservice").append(File.separator);
                    }
                    
                    String realJobPath = getRealJobNamePath(job);
                    newPath.append(realJobPath).append(
                            File.separator).append(path).toString();
                    path = newPath.toString();
                    
                    targetFile = new File(root, path);
                }

                realPaths.add(path);
            }
            vo.setFilePaths(realPaths);
            
            for (int i = 0; i < realPaths.size(); i++)
            {
                FileProfileImpl fp = HibernateUtil.get(FileProfileImpl.class, fpIds.get(i), false);
                
                AddingSourcePage page = new AddingSourcePage();
                page.setJobId(vo.getJobId());
                page.setExternalPageId(realPaths.get(i));
                page.setL10nProfileId(fpIds.get(i));
                page.setDataSource(fp.getName());
                
                HibernateUtil.save(page);
            }
            
            JmsHelper.sendMessageToQueue(vo,
                    JmsHelper.JMS_ADD_SOURCE_FILE_QUEUE);
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }
    }

    public void uploadFile()
    {
        PrintWriter writer = null;
        try
        {
            FileUploader uploader = new FileUploader();
            File file = uploader.upload(request);

            long jobId = Long.parseLong(uploader.getFieldValue("jobId"));
            companyId = Long.parseLong(uploader
                    .getFieldValue("currentCompanyId"));
            CompanyThreadLocal.getInstance().setIdValue("" + companyId);

            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);

            GlobalSightLocale locale = job.getSourceLocale();
            String realJobNamePath = getRealJobNamePath(job);
            StringBuffer newPath = new StringBuffer(locale.toString());
            newPath.append(File.separator);
            if (job.isFromWebService())
            {
                newPath.append("webservice").append(File.separator);
            }

            newPath.append(realJobNamePath).append(File.separator).append(
                    uploader.getFieldValue("path"));
            File targetFile = new File(AmbFileStoragePathUtils.getCxeDocDir(),
                    newPath.toString());
            
            if (!file.renameTo(targetFile))
            {
                FileUtils.copyFile(file, targetFile);
            }

            writer = response.getWriter();
            writer.write(file.getAbsolutePath());

        }
        catch (Exception ex)
        {
            CATEGORY.error(ex);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }

    private void setCompanyId()
    {
        String companyName = UserUtil.getCurrentCompanyName(request);
        if (companyName == null)
        {
            return;
        }

        try
        {
            companyId = ServerProxy.getJobHandler().getCompany(companyName)
                    .getIdAsLong();
            CompanyThreadLocal.getInstance().setIdValue("" + companyId);
        }
        catch (Exception e)
        {
            CATEGORY.error("Can not get the Company!");
        }
    }

    private String subPath(String path, String jobName)
    {
        path = path.replace("\\", "/");
        String s = "/" + jobName + "/";
        int index = path.indexOf(s);
        if (index > 0)
        {
            path = path.substring(index + s.length());
        }

        return path;
    }

    public void getResource() throws IOException
    {
        String name = request.getParameter("name");
        String pageLocale = request.getParameter("pageLocale");
        
        Map<String, String> resource = new HashMap<String, String>();
        List<String> keys = AppletConstant.ALL_RESOURCE.get(name);
        
        ResourceBundle b;
        if (pageLocale != null)
        {
            b = PageHandler.getBundleByLocale(pageLocale);
        }
        else
        {
            b = PageHandler.getBundle(request.getSession());
        }
        
        for (String key : keys)
        {
            try
            {
                resource.put(key, b.getString(key));
            }
            catch (Exception e)
            {
                CATEGORY.error(e);
            }
        }

        ObjectOutputStream out = null;
        try
        {
            out = new ObjectOutputStream(response.getOutputStream());
            out.writeObject(resource);
        }
        catch (IOException e)
        {
            CATEGORY.error(e);
        }
        finally
        {
            if (out != null)
            {
                out.flush();
                out.close();
            }
        }

    }

    public void getFiles()
    {
        long jobId = Long.parseLong(request.getParameter("jobId"));
        JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
        String realJobNamePath = getRealJobNamePath(job);
        List<SourcePage> pages = (List) job.getSourcePages();
        ExistFiles existFiles = new ExistFiles();
        for (SourcePage page : pages)
        {
            existFiles.addFiles(subPath(page.getDisplayPageName(), realJobNamePath));
        }

        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            writer.write(XmlUtil.object2String(existFiles));
            writer.close();
        }
        catch (IOException e)
        {
            CATEGORY.error(e);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }
    }

    public void getFileProfiles()
    {
        Iterator<FileProfile> fileProfileIter = null;
        try
        {
            Collection<FileProfile> results = ServerProxy
                    .getFileProfilePersistenceManager().getAllFileProfiles();
            fileProfileIter = results.iterator();
        }
        catch (Exception e)
        {
            String message = "Unable to get file profiles from db.";
            CATEGORY.error(message, e);
            return;
        }

        ProfileInfo info = new ProfileInfo();
        List<FileProfileVo> fileProfiles = new ArrayList<FileProfileVo>();
        FileProfile fileProfile = null;
        while (fileProfileIter.hasNext())
        {
            fileProfile = fileProfileIter.next();
            FileProfileVo vo = new FileProfileVo();
            vo.init(fileProfile);
            fileProfiles.add(vo);
        }

        info.setFileProfiles(fileProfiles);

        PrintWriter writer = null;
        try
        {
            writer = response.getWriter();
            writer.write(XmlUtil.object2String(info));
        }
        catch (IOException e)
        {
            CATEGORY.error(e);
        }
        finally
        {
            if (writer != null)
            {
                writer.flush();
                writer.close();
            }
        }

    }
    
    private String getRealJobNamePath(JobImpl p_job) {
        if (p_job == null || p_job.getSourcePages() == null)
            return "";
        
        String realJobPath = "";
        ArrayList sps = new ArrayList(p_job.getSourcePages());
        if (sps != null && sps.size() > 0) {
            SourcePage sp = (SourcePage)sps.get(0);
            String tmp = sp.getExternalPageId();
            tmp = tmp.substring(tmp.indexOf("\\") + 1);
            if (p_job.isFromWebService()) {
                tmp = tmp.substring(tmp.indexOf("\\") + 1);
            }
            realJobPath = tmp.substring(0, tmp.indexOf("\\"));
        }
        return realJobPath;
    }
}