package com.globalsight.connector.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.connector.git.form.GitConnectorFileMappingFilter;
import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GitConnectorFileMappingComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class GitConnectorFileMappingMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(GitConnectorFileMappingMainHandler.class);

    private List<?> allFileMappings = null;

    @ActionHandler(action = "save", formClass = "")
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
    	GitConnectorFileMapping gcfm = null;
    	ArrayList<GitConnectorFileMapping> existGCFMs = new ArrayList<GitConnectorFileMapping>();
        try
        {
			String srcLocale = ServerProxy
					.getLocaleManager()
					.getLocaleById(Integer.parseInt
							(request.getParameter("sourceLocale"))).toString();
            String srcMappingPath = request.getParameter("sourceMappingPath");
            String tarLocale = "", targetMappingPath = "", subFolder = "";
            long gcId = Long.parseLong(request.getParameter("gitConnectorId"));
            long companyId = Long.parseLong(request.getParameter("companyId"));
            boolean createSubFolder = false;
            GitConnector gc = GitConnectorManagerLocal.getGitConnectorById(gcId);
            GitConnectorHelper gcHelper = new GitConnectorHelper(gc);
            File gcFolder = gcHelper.getGitFolder();
            String prefixStr = gcFolder.getPath().substring(0,
            		gcFolder.getPath().lastIndexOf(File.separator) + 1);
            File tmpFile = new File(prefixStr + srcMappingPath);

            // Get information from request
            int count = Integer.parseInt(request.getParameter("count"));
            for (int i = 0; i < count; i++)
            {
                tarLocale = request.getParameter("targetLocale" + i);
                if (tarLocale == null || tarLocale.equals(""))
                    continue;
                tarLocale = ServerProxy.getLocaleManager()
                        .getLocaleById(Integer.parseInt(tarLocale)).toString();
                targetMappingPath = request.getParameter("targetMappingPath" + i);
                subFolder = request.getParameter("subfolder" + i);
                createSubFolder = "1".equals(subFolder);
                if (tmpFile.isFile())
                    createSubFolder = false;

                gcfm = new GitConnectorFileMapping();
                gcfm.setCompanyId(companyId);
                gcfm.setSourceLocale(srcLocale);
                gcfm.setSourceMappingPath(srcMappingPath);
                gcfm.setTargetLocale(tarLocale);
                gcfm.setTargetMappingPath(targetMappingPath);
                gcfm.setGitConnectorId(gcId);
                if (GitConnectorManagerLocal.isFileMappingExist(gcfm) == null)
                {
                	HibernateUtil.save(gcfm);
                	long parentId = gcfm.getId();

                    if (createSubFolder)
                    {
                    	ArrayList<String> subFolders = getAllSubFolders(gcFolder.getPath() + File.separator + srcMappingPath);
                        String srcPath = "", tarPath = "";
                        for (String path : subFolders)
                        {
                            srcPath = srcMappingPath + File.separator + path;
                            tarPath = targetMappingPath + File.separator + path;

                            gcfm = new GitConnectorFileMapping();
                            gcfm.setCompanyId(companyId);
                            gcfm.setSourceLocale(srcLocale);
                            gcfm.setSourceMappingPath(srcPath);
                            gcfm.setTargetLocale(tarLocale);
                            gcfm.setTargetMappingPath(tarPath);
                            gcfm.setGitConnectorId(gcId);
                            gcfm.setParentId(parentId);

                            if (GitConnectorManagerLocal.isFileMappingExist(gcfm) == null)
                            {
                            	HibernateUtil.saveOrUpdate(gcfm);
                            }
                            else
                            {
                            	existGCFMs.add(gcfm);
                            }
                        }
                    }
                }
                else
                {
                	existGCFMs.add(gcfm);
                }
            }
            
            request.setAttribute("gitmsg", existGCFMs);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
    
    @ActionHandler(action = "update", formClass = "")
    public void update(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        try
        {
			String srcLocale = ServerProxy.getLocaleManager().getLocaleById(
					Integer.parseInt(request.getParameter("sourceLocale")))
					.toString();
			String srcMappingPath = request.getParameter("sourceMappingPath");	
			String targetLocale = ServerProxy.getLocaleManager().getLocaleById(
					Integer.parseInt(request.getParameter("targetLocale")))
					.toString();
			String targetMappingPath = request.getParameter("targetMappingPath");
            boolean subfolder = "1".equals(request.getParameter("subfolder"));
            long companyId = Long.parseLong(request.getParameter("companyId"));
            long gcId = Long.parseLong(request.getParameter("gitConnectorId"));
            GitConnector gc = GitConnectorManagerLocal.getGitConnectorById(gcId);
            GitConnectorHelper gcHelper = new GitConnectorHelper(gc);
            File gcFolder = gcHelper.getGitFolder();
            String prefixStr = gcFolder.getPath().substring(0,
            		gcFolder.getPath().lastIndexOf(File.separator) + 1);
            File tmpFile = new File(prefixStr + srcMappingPath);
            if (tmpFile.isFile())
                subfolder = false;

            long gcfmId = Long.parseLong(request.getParameter("id"));
            GitConnectorFileMapping temp = 
            	GitConnectorManagerLocal.getGitConnectorFileMappingById(gcfmId);
            temp.setSourceLocale(srcLocale);
            temp.setSourceMappingPath(srcMappingPath);
            temp.setTargetLocale(targetLocale);
            temp.setTargetMappingPath(targetMappingPath);
            if (GitConnectorManagerLocal.isFileMappingExist(temp) == null)
    		{
    			HibernateUtil.saveOrUpdate(temp);
    			
    			if (subfolder)
    			{
    				List<GitConnectorFileMapping> gcfms = (List<GitConnectorFileMapping>) GitConnectorManagerLocal
    														.getAllSonFileMappings(gcfmId);
    				for (GitConnectorFileMapping o : gcfms) 
    				{
    					HibernateUtil.delete(o);
    				}
    				
    				GitConnectorFileMapping gcfm = null;
    				ArrayList<String> subFolders = getAllSubFolders(gcFolder.getPath() + File.separator + srcMappingPath);
    				String srcPath = "", tarPath = "";
    				for (String path : subFolders)
    				{
    					srcPath = srcMappingPath + File.separator + path;
    					tarPath = targetMappingPath + File.separator + path;
    					
    					gcfm = new GitConnectorFileMapping();
    					gcfm.setCompanyId(companyId);
    					gcfm.setSourceLocale(srcLocale);
    					gcfm.setSourceMappingPath(srcPath);
    					gcfm.setTargetLocale(targetLocale);
    					gcfm.setTargetMappingPath(tarPath);
    					gcfm.setGitConnectorId(gcId);
    					gcfm.setParentId(gcfmId);
    					
    					if (GitConnectorManagerLocal.isFileMappingExist(gcfm) == null)
    					{
    						HibernateUtil.saveOrUpdate(gcfm);
    					}
    				}
    			}
    		}
    			
            if(!subfolder)
            {
            	List<GitConnectorFileMapping> gcfms = (List<GitConnectorFileMapping>) GitConnectorManagerLocal
            											.getAllSonFileMappings(gcfmId);
            	for (GitConnectorFileMapping o : gcfms) 
            	{
            		HibernateUtil.delete(o);
            	}
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	String[] ids = request.getParameter("gcfmIds").split(",");
        for (String id : ids)
        {
            long gcfmId = Long.parseLong(id);
            GitConnectorFileMapping c = HibernateUtil.get(GitConnectorFileMapping.class, gcfmId);
            c.setIsActive(false);
            HibernateUtil.delete(c);
            
            List<GitConnectorFileMapping> gcfms = (List<GitConnectorFileMapping>) 
            				GitConnectorManagerLocal.getAllSonFileMappings(c.getId());
            for(GitConnectorFileMapping gcfm: gcfms)
            {
            	HibernateUtil.delete(gcfm);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.git.form.GitConnectorFileMappingFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	String gitConnectorIdString = request.getParameter("gitConnectorId");
    	allFileMappings = GitConnectorManagerLocal.getAllFileMappings(Long.parseLong(gitConnectorIdString));
        GitConnectorFileMappingFilter filter = (GitConnectorFileMappingFilter) form;
        allFileMappings = filter.filter((List<GitConnectorFileMapping>) allFileMappings);

        request.setAttribute("sourceLocaleFilter", filter.getSourceLocaleFilter());
        request.setAttribute("sourceMappingPathFilter", filter.getSourceMappingPathFilter());
        request.setAttribute("targetLocaleFilter", filter.getTargetLocaleFilter());
        request.setAttribute("targetMappingPathFilter", filter.getTargetMappingPathFilter());
        request.setAttribute("companyNameFilter", filter.getCompanyNameFilter());
    }

    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        if (allFileMappings == null)
        {
        	String gitConnectorIdString = request.getParameter("gitConnectorId");
        	allFileMappings = GitConnectorManagerLocal.getAllFileMappings(Long.parseLong(gitConnectorIdString));
        }

        Integer orgSize = (Integer) session.getAttribute("gitConnectorFileMappingPageSize");
        int size = orgSize == null ? 10 : orgSize;
        String numOfPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                size = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                size = Integer.MAX_VALUE;
            }
            
            session.setAttribute("gitConnectorFileMappingPageSize", size);
        }

        setTableNavigation(request, session, allFileMappings,
                new GitConnectorFileMappingComparator(uiLocale), size,
                "gitConnectorFileMappingList", "gitConnectorFileMappingKey");
    }
    
    private ArrayList<String> getAllSubFolders(String p_srcModule)
    {
        ArrayList<String> result = new ArrayList<String>();
        if (p_srcModule == null || "".equals(p_srcModule))
            return result;
        ArrayList<String> tmp = processSubFolder(p_srcModule);
        int len = p_srcModule.length();
        for (String t : tmp)
        {
            t = t.substring(len + 1);
            result.add(t);
        }
        return result;
    }
    
    private ArrayList<String> processSubFolder(String p_srcModule)
    {
        ArrayList<String> folders = new ArrayList<String>();
        try
        {
            File folder = new File(p_srcModule);
            File[] list = folder.listFiles();
            File tmp = null;
            String tmpPath = "";
            if (list != null)
            {
                for (int i = 0; i < list.length; i++)
                {
                    tmp = list[i];
                    if (tmp.isDirectory()
                            && !".GIT".equals(tmp.getName().toUpperCase()))
                    {
                        tmpPath = tmp.getAbsolutePath();
                        folders.add(tmpPath);
                        folders.addAll(processSubFolder(tmpPath));
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error::" + e.toString());
        }
        return folders;
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
    	String id = request.getParameter("gitConnectorId");
        if (id != null)
        {
            GitConnector connector = HibernateUtil.get(
            		GitConnector.class, Long.parseLong(id));
            request.setAttribute("gitConnector", connector);
        }
    	
    	allFileMappings = null;
        clearSessionExceptTableInfo(request.getSession(false),
                "gitConnectorFileMappingKey");

        response.setCharacterEncoding("utf-8");
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        dataForTable(request);
    }
}
