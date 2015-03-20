package com.globalsight.connector.git;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.globalsight.connector.git.util.GitConnectorHelper;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FileUtil;

public class GitConnectorBasicHandler extends PageActionHandler
{
	
    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.gitconnector.GitConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();
        GitConnector gc = (GitConnector) form;
        
        GitConnectorHelper helper = new GitConnectorHelper(gc);
        File gitFolder = helper.getGitFolder();
        
        JSONObject json = new JSONObject();
        
        try 
        {
        	FileUtil.deleteFile(gitFolder);
        	
	        helper.gitConnectorClone();
	        
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
        	FileUtil.deleteFile(gitFolder);
        	out.close();
        	pageReturn();
        }
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
