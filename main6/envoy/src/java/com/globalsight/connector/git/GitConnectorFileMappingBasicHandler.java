package com.globalsight.connector.git;

import java.rmi.RemoteException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class GitConnectorFileMappingBasicHandler extends PageActionHandler
{

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
    	 String gitConnectorId = request.getParameter("gitConnectorId");
         if (gitConnectorId != null)
         {
             GitConnector connector = HibernateUtil.get(
             		GitConnector.class, Long.parseLong(gitConnectorId));
             request.setAttribute("gitConnector", connector);
         }
         
         String id = request.getParameter("id");
         if (id != null)
         {
             GitConnectorFileMapping gcfm = HibernateUtil.get(
             		GitConnectorFileMapping.class, Long.parseLong(id));
             request.setAttribute("gitConnectorFileMappingKey", gcfm);
             
             List<GitConnectorFileMapping> list = (List<GitConnectorFileMapping>) 
             					GitConnectorManagerLocal.getAllSonFileMappings(Long.parseLong(id));
             
             if(list != null && list.size() > 0)
             {
             	request.setAttribute("subFolderMapped", "1");
             }
             else
             {
             	request.setAttribute("subFolderMapped", "0");
             }
         }
         
		try 
		{
			Vector sourceLocales = ServerProxy.getLocaleManager()
					.getAllSourceLocales();
			Vector targetLocales = ServerProxy.getLocaleManager()
					.getAllTargetLocales();
			SortUtil.sort(sourceLocales, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
							.compareToIgnoreCase(
									((GlobalSightLocale) o2)
											.getDisplayName(Locale.US));
				}
			});
			SortUtil.sort(targetLocales, new Comparator() {
				public int compare(Object o1, Object o2) {
					return ((GlobalSightLocale) o1).getDisplayName(Locale.US)
							.compareToIgnoreCase(
									((GlobalSightLocale) o2)
											.getDisplayName(Locale.US));
				}
			});

			request.setAttribute("sourceLocalePairs", sourceLocales);
			request.setAttribute("targetLocalePairs", targetLocales);

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
