package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Collection;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import org.hibernate.HibernateException;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.cvsconfig.*;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CVSRepositoryComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class CVSRepositoryMainHandler extends PageHandler {
    private CVSServerManagerLocal manager = new CVSServerManagerLocal();
    private static final Logger logger = Logger.getLogger(CVSRepositoryMainHandler.class.getName());

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException {
        try {
            HttpSession session = p_request.getSession(false);
            String action = p_request.getParameter("action");
            if (CVSConfigConstants.CREATE.equals(action)) {
                //Add new CVS repository configuration
                createCVSRepository(p_request); 
            } else if (CVSConfigConstants.UPDATE.equals(action)) {
                updateCVSRepository(p_request);
            } else if (CVSConfigConstants.REMOVE.equals(action)) {
            	removeCVSRepository(p_request);
            }
            
            dataForTable(p_request, session);
            
        } catch (NamingException ne)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
            }
            catch (RemoteException re)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
            }
            catch (GeneralException ge)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
            }
            catch (HibernateException e)
            {
                throw new EnvoyServletException(e);
            }

            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    private void dataForTable
        (HttpServletRequest p_request, HttpSession p_session) 
         throws RemoteException, NamingException, GeneralException
    {
        Vector servers = vectorizedCollection(manager.getAllRepository());
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);
    
        setTableNavigation(p_request, p_session, servers,
                       new CVSRepositoryComparator(uiLocale),
                       10,
                       CVSConfigConstants.CVS_REPOSITORY_LIST, 
                       CVSConfigConstants.CVS_REPOSITORY_KEY);
    }

    private void createCVSRepository(HttpServletRequest p_request) 
        throws CVSConfigException, RemoteException {
        CVSServerManagerLocal manager = new CVSServerManagerLocal();
        CVSRepository repository = new CVSRepository();

        long serverID = 
            Integer.parseInt(p_request.getParameter("selectServer"));
        CVSServer cs = manager.getServer(serverID);
        repository.setServer(cs);
        String name = p_request.getParameter(CVSConfigConstants.REPOSITORY_NAME);
        if (!manager.isRepositoryExist(name)) {
	        repository.setName(name);
	        String repositoryName = p_request.getParameter(CVSConfigConstants.REPOSITORY_CVS); 
	        repository.setRepository(repositoryName);
	        repository.setFolderName(p_request.getParameter(CVSConfigConstants.REPOSITORY_FOLDERNAME));
	        String loginUser = p_request.getParameter(CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER);
	        repository.setLoginUser(loginUser);
	        String loginPwd = p_request.getParameter(CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD);
	        repository.setLoginPwd(loginPwd);
	        
	        StringBuilder sb = new StringBuilder(":");
	        sb.append(cs.getProtocol()==0?"pserver":"ext").append(":").append(loginUser);
	        if (cs.getProtocol()==0) {
	        	sb.append(":").append(loginPwd);
	        }
	        sb.append("@").append(cs.getHostIP()).append(":");
	        sb.append("/").append(repositoryName);
	        repository.setCVSRootEnv(sb.toString());
	        
	        manager.addRepository(repository);
        }
    }
    
    private void updateCVSRepository(HttpServletRequest p_request) 
        throws CVSConfigException, RemoteException {
        CVSRepository repository = null;
        HttpSession session = null;
       
        try {
            session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session.getAttribute
                (WebAppConstants.SESSION_MANAGER); 
            repository = (CVSRepository)sessionMgr.getAttribute
                (CVSConfigConstants.CVS_REPOSITORY);
            CVSServerManagerLocal manager = new CVSServerManagerLocal();
            
            String oldFolder = repository.getFolderName();
            //set parameter
            repository.setName(p_request.getParameter(CVSConfigConstants.REPOSITORY_NAME));
            String repositoryName = p_request.getParameter(CVSConfigConstants.REPOSITORY_CVS); 
            repository.setRepository(repositoryName);
            String newFolder = p_request.getParameter(CVSConfigConstants.REPOSITORY_FOLDERNAME);
            repository.setFolderName(newFolder);
            String loginUser = p_request.getParameter(CVSConfigConstants.CVS_REPOSITORY_LOGIN_USER);
            repository.setLoginUser(loginUser);
            String loginPwd = p_request.getParameter(CVSConfigConstants.CVS_REPOSITORY_LOGIN_PASSWORD);
            repository.setLoginPwd(loginPwd);
            
            long serverID = Integer.parseInt(p_request.getParameter("selectServer"));
            CVSServer cs = manager.getServer(serverID);
            repository.setServer(cs);

	        StringBuilder sb = new StringBuilder(":");
	        sb.append(cs.getProtocol()==0?"pserver":"ext").append(":").append(loginUser);
	        if (cs.getProtocol()==0) {
	        	sb.append(":").append(loginPwd);
	        }
	        sb.append("@").append(cs.getHostIP()).append(":");
	        sb.append("/").append(repositoryName);
	        repository.setCVSRootEnv(sb.toString());

            manager.updateRepository(repository);
            
            if (!oldFolder.equalsIgnoreCase(newFolder)) {
            	//user modify the folder name
                String path = CVSUtil.getBaseDocRoot().concat(repository.getServer().getSandbox().concat(File.separator).concat(oldFolder));
                FileUtils.deleteAllFilesSilently(path); 
            	
                CVSUtil.createFolder(repository.getServer().getSandbox().concat(File.separator).concat(newFolder));
            }
        } catch (Exception e) {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }
    
    private void removeCVSRepository(HttpServletRequest p_request) throws CVSConfigException, RemoteException {
    	try {
    		HttpSession session = p_request.getSession(false);
			SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
			ResourceBundle bundle = getBundle(session);

			CVSServerManagerLocal manager = new CVSServerManagerLocal();
			long id = Long.parseLong(p_request.getParameter("id"));
			CVSRepository repos = manager.getRepository(id);
			for (CVSModule r : repos.getModuleSet()) {
				if (r.isActive()) {
					sessionMgr.setAttribute("cvsmsg", bundle.getString("msg_cnd_remove_repository"));
					return;
				}
			}
			
			String folderName = CVSUtil.getBaseDocRoot().concat(repos.getServer().getSandbox()).concat(File.separator).concat(repos.getFolderName());
			manager.removeRepository(id);

			//Delete the sandbox
			FileUtils.deleteAllFilesSilently(folderName); 
		} catch (Exception e) {
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
		}
    }

}
