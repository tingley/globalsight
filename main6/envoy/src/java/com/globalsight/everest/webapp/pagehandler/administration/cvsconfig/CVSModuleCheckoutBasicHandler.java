package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;

import com.globalsight.everest.cvsconfig.CVSConfigException;
import com.globalsight.everest.cvsconfig.CVSModule;
import com.globalsight.everest.cvsconfig.CVSRepository;
import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.cvsconfig.CVSUtil;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CVSServerComparator;
import com.globalsight.everest.util.comparator.CompanyComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;

public class CVSModuleCheckoutBasicHandler extends PageHandler {
	private CVSServerManagerLocal manager = new CVSServerManagerLocal();

	public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
			HttpServletRequest p_request, HttpServletResponse p_response,
			ServletContext p_context) throws ServletException, IOException,
			EnvoyServletException {
		try {
			HttpSession session = p_request.getSession(false);
			String action = p_request.getParameter("action");
            SessionManager sessionMgr = (SessionManager)session.getAttribute(SESSION_MANAGER);
            String id = (String)p_request.getParameter("id");
            if (CVSConfigConstants.CHECKOUT.equals(action)) {
                CVSModule module = manager.getModule(Long.parseLong(id));
                module.setLastCheckout(new java.util.Date().toString());
                manager.updateModule(module);
                sessionMgr.setAttribute(CVSConfigConstants.CVS_MODULE_KEY, module);
            }
            
		} catch (GeneralException ge)
	        {
	            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
	        }
	        catch (HibernateException e)
	        {
	            throw new EnvoyServletException(e);
	        }

	        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
	}

}
