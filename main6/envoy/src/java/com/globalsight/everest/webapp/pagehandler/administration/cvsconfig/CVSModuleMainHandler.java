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

import org.hibernate.HibernateException;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.cvsconfig.*;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingManagerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CVSModuleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class CVSModuleMainHandler extends PageHandler {
    private CVSServerManagerLocal manager = new CVSServerManagerLocal();

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException {
        try {
            HttpSession session = p_request.getSession(false);
            String action = p_request.getParameter("action");
            
            if (CVSConfigConstants.CREATE.equals(action)) {
                //Add new CVS module configuration
                createCVSModule(p_request); 
            } else if (CVSConfigConstants.UPDATE.equals(action)) {
                updateCVSModule(p_request);
            } else if (CVSConfigConstants.REMOVE.equals(action)) {
            	removeCVSModule(p_request);
            }
            
            dataForTable(p_request, session);
            
        } catch (NamingException ne)
            {
                throw new 
                    EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
            }
            catch (RemoteException re)
            {
                throw new 
                    EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
            }
            catch (GeneralException ge)
            {
                throw new 
                    EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
            }
            catch (HibernateException e)
            {
                throw new EnvoyServletException(e);
            }

            super.invokePageHandler(p_pageDescriptor,
                                    p_request, p_response, p_context);
    }

    private void dataForTable
        (HttpServletRequest p_request, HttpSession p_session) 
         throws RemoteException, NamingException, GeneralException
    {
        Vector modules = vectorizedCollection(manager.getAllModule());
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);
    
        setTableNavigation(p_request, p_session, modules,
                       new CVSModuleComparator(uiLocale),
                       10,
                       CVSConfigConstants.CVS_MODULE_LIST, 
                       CVSConfigConstants.CVS_MODULE_KEY);
    }

    private void createCVSModule(HttpServletRequest p_request) 
        throws CVSConfigException, RemoteException {
        CVSServerManagerLocal manager = new CVSServerManagerLocal();
        CVSModule module = new CVSModule();

        String name = p_request.getParameter(CVSConfigConstants.MODULE_NAME);
        if (!manager.isModuleExist(name)) {
	        long ID = Integer.parseInt(p_request.getParameter("selectServer"));
	        CVSServer cs = manager.getServer(ID);
	        module.setServer(cs);
	        module.setName(name);
	        module.setModulename(p_request.getParameter("moduleNames"));
	        module.setBranch(p_request.getParameter("branchName"));
	        manager.addModule(module);
        } else {
        	
        }
    }
    
    private void updateCVSModule(HttpServletRequest p_request) 
        throws CVSConfigException, RemoteException {
        CVSModule module = null;
        HttpSession session = null;
       
        try {
            session = p_request.getSession(false);
            SessionManager sessionMgr = (SessionManager) session.getAttribute
                (WebAppConstants.SESSION_MANAGER); 
            module = (CVSModule)sessionMgr.getAttribute
                (CVSConfigConstants.CVS_MODULE);
            CVSServerManagerLocal manager = new CVSServerManagerLocal();
            
            //set parameter
            module.setName(p_request.getParameter(CVSConfigConstants.MODULE_NAME));
            module.setModulename(p_request.getParameter("moduleNames"));
            module.setBranch(p_request.getParameter("branchName"));
            long ID = Integer.parseInt(p_request.getParameter("selectServer"));
	        CVSServer cs = manager.getServer(ID);
	        module.setServer(cs);
            manager.updateModule(module);
        } catch (Exception e) {
            throw new 
                EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }
    
    private void removeCVSModule(HttpServletRequest p_request) throws CVSConfigException, RemoteException {
    	try {
    		HttpSession session = p_request.getSession(false);
			SessionManager sessionMgr = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER); 
			ResourceBundle bundle = getBundle(session);
			
			CVSServerManagerLocal manager = new CVSServerManagerLocal();
			long id = Long.parseLong(p_request.getParameter("id"));
			CVSModule module = manager.getModule(id);
			
			ModuleMappingManagerLocal mmManager = new ModuleMappingManagerLocal();
//			if (mmManager.hasModuleMapping(id)) {
//				sessionMgr.setAttribute("cvsmsg", bundle.getString("msg_cnd_remove_module"));
//				return;
//			}
			String folderName = module.getRealPath();
			manager.removeModule(id);

			//Delete the sandbox
			FileUtils.deleteAllFilesSilently(folderName); 
		} catch (Exception e) {
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
		}
    }

}
