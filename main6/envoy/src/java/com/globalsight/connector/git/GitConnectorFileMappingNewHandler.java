package com.globalsight.connector.git;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class GitConnectorFileMappingNewHandler extends PageActionHandler
{
	
	@ActionHandler(action = "changeSourceLocale", formClass = "")
    public void update(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
		Vector targetLocales = new Vector();
		
		String sourceLocale = request.getParameter("sourceLocale");
		long sourceLocaleId = -1L;
		if (sourceLocale != null)
			sourceLocaleId = Long.parseLong(sourceLocale);
		
		if (sourceLocaleId > 0) {
			targetLocales = ServerProxy.getLocaleManager()
					.getTargetLocales(
							ServerProxy.getLocaleManager().getLocaleById(
									sourceLocaleId));
			SortUtil.sort(targetLocales, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((GlobalSightLocale) o1).getDisplayName(
							Locale.US).compareToIgnoreCase(
							((GlobalSightLocale) o2)
									.getDisplayName(Locale.US));
				}
			});
		}
		
		request.setAttribute("targetLocalePairs", targetLocales);
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
        String id = request.getParameter("gitConnectorId");
        if (id != null)
        {
        	GitConnector connector = GitConnectorManagerLocal
        								.getGitConnectorById(Long.parseLong(id));
            request.setAttribute("gitConnector", connector);
        }
        

		try 
		{
			Vector sourceLocales = ServerProxy.getLocaleManager()
					.getAllSourceLocales();
			Vector targetLocales = new Vector();
			
			SortUtil.sort(sourceLocales, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
					.compareToIgnoreCase(
							((GlobalSightLocale) o2)
							.getDisplayName(Locale.US));
				}
			});
			
			String sourceMappingPath = request.getParameter("sourceMappingPath");
			sourceMappingPath = sourceMappingPath == null ? "" : sourceMappingPath;
			String sourceLocale = request.getParameter("sourceLocale");
			
			request.setAttribute("sourceLocalePairs", sourceLocales);
			request.setAttribute("targetLocalePairs", targetLocales);
			request.setAttribute("sourceLocale", sourceLocale);
			request.setAttribute("sourceMappingPath", sourceMappingPath);
		} 
		catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    ge);
        }
        response.setCharacterEncoding("utf-8");
    }
}
