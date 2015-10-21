package com.globalsight.connector.git;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.connector.git.form.GitConnectorFilter;
import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.GitConnectorComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class GitConnectorMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(GitConnectorMainHandler.class);

    private List<?> allConns = null;

    @ActionHandler(action = "save", formClass = "com.globalsight.cxe.entity.gitconnector.GitConnector")
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        GitConnector connector = (GitConnector) form;
        String id = request.getParameter("companyId");
        if(StringUtil.isNotEmpty(id))
        {
        	connector.setCompanyId(Long.parseLong(id));
        }
        checkNull(connector);
        HibernateUtil.saveOrUpdate(connector);
        
        GitConnectorHelper helper = new GitConnectorHelper(connector);
        File gitFolder = helper.getGitFolder();
        String gitFolderPath = gitFolder.getPath();
        int idlength = String.valueOf(connector.getId()).length();
        File tempFile = new File(gitFolderPath.substring(0, gitFolderPath.length() - idlength) + "-1");
        if(tempFile.exists())
        {
        	tempFile.renameTo(gitFolder);
        }
    }
    
    private void checkNull(GitConnector connector)
    {
    	if(connector.getUsername() == null)
        {
        	connector.setUsername("");
        }
    	if(connector.getPassword() == null)
        {
        	connector.setPassword("");
        }
    	if(connector.getPrivateKeyFile() == null)
        {
        	connector.setPrivateKeyFile("");
        }
        if(connector.getEmail() == null)
        {
        	connector.setEmail("");
        }
    }

    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("gitConnectorIds");
        for (String id : ids)
        {
            long cId = Long.parseLong(id);
            GitConnector c = HibernateUtil.get(GitConnector.class, cId);
            c.setIsActive(false);
            HibernateUtil.update(c);
        }
    }

    @SuppressWarnings("unchecked")
    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.git.form.GitConnectorFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        allConns = GitConnectorManagerLocal.getAllConnectors();
        GitConnectorFilter filter = (GitConnectorFilter) form;
        allConns = filter.filter((List<GitConnector>) allConns);

        request.setAttribute("nameFilter", filter.getNameFilter());
        request.setAttribute("urlFilter", filter.getUrlFilter());
        request.setAttribute("usernameFilter", filter.getUsernameFilter());
        request.setAttribute("companyNameFilter", filter.getCompanyNameFilter());
        request.setAttribute("branchFilter", filter.getBranchFilter());
        request.setAttribute("emailFilter", filter.getEmailFilter());
    }

    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        if (allConns == null)
        {
            allConns = GitConnectorManagerLocal.getAllConnectors();
        }

        Integer orgSize = (Integer) session.getAttribute("gitConnectorPageSize");
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
            
            session.setAttribute("gitConnectorPageSize", size);
        }

        setTableNavigation(request, session, allConns,
                new GitConnectorComparator(uiLocale), size,
                "gitConnectorList", "gitConnectorKey");
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
        allConns = null;
        clearSessionExceptTableInfo(request.getSession(false),
                "gitConnectorKey");

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
