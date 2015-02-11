package com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.cvsconfig.CVSServer;
import com.globalsight.everest.cvsconfig.CVSServerManagerLocal;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMapping;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingComparator;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingManagerLocal;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingRename;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingRenameComparator;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

public class ModuleMappingNewHandler extends PageHandler implements ModuleMappingConstants {
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
	private static final GlobalSightCategory logger =
        (GlobalSightCategory)GlobalSightCategory.getLogger(ModuleMappingNewHandler.class.getName());
	
	private CVSServerManagerLocal manager = new CVSServerManagerLocal();
	
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        SessionManager sessionMgr = (SessionManager)session.getAttribute(SESSION_MANAGER);

        try
        {
        	Vector sourceLocales = ServerProxy.getLocaleManager().getAllSourceLocales();
        	Vector targetLocales = new Vector();
        	
            Collections.sort(sourceLocales, new Comparator() {
            	public int compare(Object o1, Object o2) {
            		return ((GlobalSightLocale) o1).getDisplayName(Locale.US).compareToIgnoreCase(((GlobalSightLocale) o2).getDisplayName(Locale.US));
            	}
            });
            
            String cvsServer = p_request.getParameter("cvsServer");
            cvsServer = cvsServer == null ? "" : cvsServer;
            String sourceModule = p_request.getParameter("sourceModule");
            sourceModule = sourceModule == null ? "" : sourceModule;
            String sourceLocale = p_request.getParameter("sourceLocale");
            long sourceLocaleId = -1L;
            if (sourceLocale != null)
            	sourceLocaleId = Long.parseLong(sourceLocale);
            
            if (action.equals("Change")) {
            	if (sourceLocaleId > 0) {
	            	targetLocales = ServerProxy.getLocaleManager().getTargetLocales(ServerProxy.getLocaleManager().getLocaleById(sourceLocaleId));
	                Collections.sort(targetLocales, new Comparator() {
	                	public int compare(Object o1, Object o2) {
	                		return ((GlobalSightLocale) o1).getDisplayName(Locale.US).compareToIgnoreCase(((GlobalSightLocale) o2).getDisplayName(Locale.US));
	                	}
	                });
            	}
            }

            p_request.setAttribute(SOURCE_LOCALE_PAIRS, sourceLocales);
            p_request.setAttribute(TARGET_LOCALE_PAIRS, targetLocales);
            p_request.setAttribute("cvsServer", cvsServer);
            p_request.setAttribute("sourceLocale", sourceLocale);
            p_request.setAttribute("sourceModule", sourceModule);

            ArrayList servers = (ArrayList)manager.getAllServer();
            p_request.setAttribute(CVSConfigConstants.CVS_SERVER_LIST, servers);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
}
