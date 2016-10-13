package com.globalsight.connector.git;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

public class GitConnectorBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(GitConnectorBasicHandler.class);

    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.gitconnector.GitConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();
        GitConnector gc = (GitConnector) form;
        String id = request.getParameter("companyId");
        if(StringUtil.isNotEmpty(id))
        {
        	gc.setCompanyId(Long.parseLong(id));
        }
        
        GitConnectorHelper helper = new GitConnectorHelper(gc);
        File gitFolder = helper.getGitFolder();
        
        JSONObject json = new JSONObject();
        try 
        {
        	boolean needReCloneLocalFiles = true;
        	// for "edit"
        	if (gc.getIdAsLong() > 0)
        	{
				GitConnector gcInDb = HibernateUtil.get(GitConnector.class,
						gc.getIdAsLong());
				needReCloneLocalFiles = needReCloneLocalFiles(gc, gcInDb);
        	}

        	if (needReCloneLocalFiles)
        	{
        		FileUtil.deleteFile(gitFolder);

        		helper.gitConnectorClone();
        	}
	        json.put("error", "");
    		out.write(json.toString().getBytes("UTF-8"));
        } 
        catch (Exception e) 
        {
        	FileUtil.deleteFile(gitFolder);

        	json.put("error", bundle.getString("error_git_connector"));
        	out.write(json.toString().getBytes("UTF-8"));
        	
        	e.printStackTrace();
        }
        finally
        {
        	out.close();
        	pageReturn();
        }
    }

	private boolean needReCloneLocalFiles(GitConnector newGc,
			GitConnector originalGc)
    {
		if (StringUtil.isEmpty(newGc.getName())
				|| StringUtil.isEmpty(newGc.getUrl()))
		{
			return false;
		}

		// for http URL
		if (newGc.getUrl().toLowerCase().startsWith("http"))
		{
			if (newGc.getName().equals(originalGc.getName())
				&& newGc.getUrl().equals(originalGc.getUrl())
				&& isSameValue(newGc.getUsername(), originalGc.getUsername())
				&& isSameValue(newGc.getPassword(), originalGc.getPassword()))
			{
				return false;
			}
		}
		// for ssh URL
		else
		{
			if (newGc.getName().equals(originalGc.getName())
					&& newGc.getUrl().equals(originalGc.getUrl())
					&& isSameValue(newGc.getPassword(), originalGc.getPassword())
					&& isSameValue(newGc.getPrivateKeyFile(), originalGc.getPrivateKeyFile()))
			{
				return false;
			}
		}

		// default true
		return true;
    }

	private boolean isSameValue(String val1, String val2)
	{
		if (val1 == null && val2 == null)
			return true;

		if (val1 != null && val1.equals(val2))
			return true;

		return false;
	}

	@Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }

    @SuppressWarnings("rawtypes")
    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        String id = request.getParameter("id");
        if (id != null)
        {
            GitConnector connector = HibernateUtil.get(
            		GitConnector.class, Long.parseLong(id));
            request.setAttribute("gitConnector", connector);
        }

        String names = "";
        List gitConnectors = GitConnectorManagerLocal.getAllConnectors();
        for (Object o :gitConnectors)
        {
        	GitConnector gc = (GitConnector) o;
            if (id != null && id.equals("" + gc.getId()))
            {
                continue;
            }
            names += "," + gc.getName() + ",";
        }

        request.setAttribute("names", names);

        response.setCharacterEncoding("utf-8");
    }
}
