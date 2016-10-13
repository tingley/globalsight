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
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

public class ModuleMappingRenameBasicHandler extends PageHandler implements ModuleMappingConstants {
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
	private ModuleMappingManagerLocal manager = new ModuleMappingManagerLocal();
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
            if (action.equals(CREATE_RENAME))
            {
            	sessionMgr.setAttribute(ModuleMappingConstants.MODULE_MAPPING_RENAME_KEY, null);
            }
            else if (action.equals(UPDATE_RENAME))
            {
                String id = (String)p_request.getParameter("id");
                ModuleMappingRename mmr = (ModuleMappingRename)manager.getModuleMappingRename(Long.parseLong(id));
                sessionMgr.setAttribute(MODULE_MAPPING_RENAME_KEY, mmr);
                p_request.setAttribute("edit", "true");
            }
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }
}
