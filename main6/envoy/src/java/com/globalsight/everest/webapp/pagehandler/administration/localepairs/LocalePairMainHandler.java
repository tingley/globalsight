/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.administration.localepairs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocalePairComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.ExportUtil;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.dependencychecking.LocalePairDependencyChecker;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;

public class LocalePairMainHandler extends PageHandler implements
		LocalePairConstants, WebAppConstants
{
	private final static String NEW_LINE = "\r\n";
	private List<Long> localeIdList;

	/**
	 * 
	 * @param pageDescriptor
	 *            the page desciptor
	 * @param request
	 *            the original request sent from the browser
	 * @param response
	 *            the original response object
	 * @param context
	 *            context the Servlet context
	 */
	public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
			HttpServletRequest p_request, HttpServletResponse p_response,
			ServletContext p_context) throws ServletException, IOException,
			EnvoyServletException
	{
		HttpSession session = p_request.getSession(false);
		String action = p_request.getParameter("action");
		SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
		try
		{
			if (isPost(p_request))
			{
				if (LocalePairConstants.FILTER.equals(action)) 
				{
					handleFilters(p_request, sessionMgr, action);
				}
				if (LocalePairConstants.CANCEL.equals(action))
				{
					resetSessionTableInfo(session,
							LocalePairConstants.LP_KEY);
				}
				else if (LocalePairConstants.CREATE.equals(action))
				{
					createLocalePair(p_request, session);
				}
				else if (LocalePairConstants.CREATE_LOCALE.equals(action))
				{
					createLocale(p_request, session);
				}
				else if (LocalePairConstants.REMOVE.equals(action))
				{
					removeLocalePair(p_request, session);
				}
				else if (LocalePairConstants.EXPORT.equals(action))
				{
					exportLocalePair(p_request, p_response, session);
					return;
				}
			}

			dataForTable(p_request, session);
		}
		catch (NamingException ne)
		{
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
					ne);
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
		super.invokePageHandler(p_pageDescriptor, p_request, p_response,
				p_context);
	}

	/**
	 * Check if any objects have dependencies on this Locale Pair. This should
	 * be called BEFORE attempting to remove a Locale Pair.
	 * <p>
	 * 
	 */
	private String checkDependencies(LocalePair p_pair, HttpSession session)
			throws RemoteException, GeneralException
	{
		ResourceBundle bundle = PageHandler.getBundle(session);
		LocalePairDependencyChecker depChecker = new LocalePairDependencyChecker();

		Hashtable catDeps = depChecker.categorizeDependencies(p_pair);

		StringBuffer deps = new StringBuffer();
		if (catDeps.size() == 0)
		{
			return null;
		}

		deps.append("<span class=\"errorMsg\">");
		deps.append("# Dependencies in Locale Pair(s) ( "+p_pair.toString()+" ):");
		for (Enumeration e = catDeps.keys(); e.hasMoreElements();)
		{
			String key = (String) e.nextElement();
			deps.append("<p>*** " + bundle.getString(key) + " ***<br>");
			Vector values = (Vector) catDeps.get(key);
			for (int i = 0; i < values.size(); i++)
			{
				deps.append((String) values.get(i));
				deps.append("<br>");
			}
		}
		deps.append("</span>");
		deps.append("<hr style='border:1px dotted #FF0000;padding-left:4px;padding-right:4px;padding-top:1px;padding-bottom:1px'><br>");
		return deps.toString();
	}

	private void exportLocalePair(HttpServletRequest p_request,
			HttpServletResponse p_response, HttpSession p_session)
			throws EnvoyServletException, RemoteException
	{
		SessionManager sessionMgr = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
		String currentId = CompanyThreadLocal.getInstance().getValue();
		long companyId = Long.parseLong(currentId);
		localeIdList = new ArrayList<Long>();
		// create property file
		File propertyFile = createPropertyFile(user.getUserName(), companyId);
		// get property file name
		String fileName = propertyFile.getName();

		LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
		String id = (String) p_request.getParameter("id");
		String[] idsArr = null;
		if (id != null && !id.equals(""))
		{
			idsArr = id.split(",");
			if (idsArr != null)
			{
				for (int n = 0; n < idsArr.length; n++)
				{
					long localePairId = Long.parseLong(idsArr[n]);
					LocalePair localePair = localeMgr
							.getLocalePairById(localePairId);
					propertiesInputLocalePair(propertyFile, localePair);
				}
				ExportUtil.writeToResponse(p_response, propertyFile, fileName);
			}
		}
	}

	private void propertiesInputLocalePair(File propertyFile,
			LocalePair localePair)
	{
		StringBuffer buffer = new StringBuffer();
		if (localePair != null)
		{
			buffer.append("##LocalPairs.").append(localePair.getCompanyId())
					.append(".").append(localePair.getId()).append(".begin")
					.append(NEW_LINE);
			buffer.append("LocalPairs.").append(localePair.getId())
					.append(".ID = ").append(localePair.getId())
					.append(NEW_LINE);
			buffer.append("LocalPairs.").append(localePair.getId())
					.append(".SOURCE_LOCALE_ID = ")
					.append(localePair.getSource().getId()).append(NEW_LINE);
			buffer.append("LocalPairs.").append(localePair.getId())
					.append(".TARGET_LOCALE_ID = ")
					.append(localePair.getTarget().getId()).append(NEW_LINE);
			buffer.append("LocalPairs.").append(localePair.getId())
					.append(".COMPANY_ID = ").append(localePair.getCompanyId())
					.append(NEW_LINE);
			buffer.append("LocalPairs.").append(localePair.getId())
					.append(".IS_ACTIVE = ").append(localePair.getIsActive())
					.append(NEW_LINE);
			buffer.append("##LocalPairs.").append(localePair.getCompanyId())
					.append(".").append(localePair.getId()).append(".end")
					.append(NEW_LINE).append(NEW_LINE);

			writeToFile(propertyFile, buffer.toString().getBytes());

			if (localePair.getSource() != null
					&& !localeIdList.contains(localePair.getSource().getId()))
			{
				propertiesInputLocale(propertyFile, localePair.getSource(),
						localePair.getCompanyId());
				localeIdList.add(localePair.getSource().getId());
			}
			if (localePair.getTarget() != null
					&& !localeIdList.contains(localePair.getTarget().getId()))
			{
				propertiesInputLocale(propertyFile, localePair.getTarget(),
						localePair.getCompanyId());
				localeIdList.add(localePair.getTarget().getId());
			}

		}
	}

	private void propertiesInputLocale(File propertyFile,
			GlobalSightLocale locale, Long companyId)
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("##Locale.").append(companyId).append(".")
				.append(locale.getId()).append(".begin").append(NEW_LINE);
		buffer.append("Locale.").append(locale.getId()).append(".ID = ")
				.append(locale.getId()).append(NEW_LINE);
		buffer.append("Locale.").append(locale.getId())
				.append(".ISO_LANG_CODE = ").append(locale.getLanguage())
				.append(NEW_LINE);
		buffer.append("Locale.").append(locale.getId())
				.append(".ISO_COUNTRY_CODE = ").append(locale.getCountry())
				.append(NEW_LINE);
		buffer.append("Locale.").append(locale.getId())
				.append(".IS_UI_LOCALE = ").append(locale.isIsUiLocale())
				.append(NEW_LINE);
		buffer.append("##Locale.").append(companyId).append(".")
				.append(locale.getId()).append(".end").append(NEW_LINE)
				.append(NEW_LINE);

		writeToFile(propertyFile, buffer.toString().getBytes());
	}

	/**
	 * Removes a source/target locale pair from the database; also removes all
	 * container roles relying on this pair.
	 */
	private void removeLocalePair(HttpServletRequest p_request,
			HttpSession p_session) throws EnvoyServletException,
			RemoteException
	{
		String ids = (String) p_request.getParameter("id");
		try
		{   
			if (ids != null && ids.length()>0)
			{
				String[] idsArr = ids.split(",");
				LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
				LocalePair pair = null;
				String deps = null;
				StringBuffer msg = new StringBuffer();
				for(String id : idsArr)
				{	
					pair = localeMgr.getLocalePairById(Long.parseLong(id.trim()));
					
					// check dependencies first
					deps = checkDependencies(pair, p_session);
					if (deps == null)
					{
						// removes the locale pair and all the roles associated with it
						localeMgr.removeSourceTargetLocalePair(pair);
					}
					else
					{
						msg.append(deps);	
					}
				}
				if (msg.length()>0)
				{
					ResourceBundle bundle = PageHandler.getBundle(p_session);

					Object[] args = { bundle.getString("lb_locale_pair")+"(s)" };
					msg.insert(0,"<span class=\"errorMsg\">"+MessageFormat.format(bundle.getString("msg_dependency"),
							args)+"<span><br><br>");
					SessionManager sessionMgr = (SessionManager) p_session
							.getAttribute(WebAppConstants.SESSION_MANAGER);
					sessionMgr.setAttribute(DEPENDENCIES, msg.toString());
				}
			}

		}
		catch (Exception lme)
		{
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
					lme);
		}
	}

	/**
	 * Adds a source/target locale pair.
	 */
	private void createLocalePair(HttpServletRequest p_request,
			HttpSession p_session) throws EnvoyServletException,
			RemoteException
	{
		String srcId = (String) p_request.getParameter("sourceLocale");
		String targId = (String) p_request.getParameter("targetLocale");

		try
		{
			LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
			GlobalSightLocale srcLocale = localeMgr.getLocaleById(Long
					.parseLong(srcId));
			GlobalSightLocale targLocale = localeMgr.getLocaleById(Long
					.parseLong(targId));
			String companyId = CompanyThreadLocal.getInstance().getValue();
			localeMgr.addSourceTargetLocalePair(srcLocale, targLocale,
					Long.parseLong(companyId));
		}
		catch (Exception lme)
		{
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
					lme);
		}
	}

	/**
	 * Adds a locale.
	 */
	private void createLocale(HttpServletRequest p_request,
			HttpSession p_session) throws EnvoyServletException,
			RemoteException
	{
		String language = (String) p_request.getParameter("language");
		String country = (String) p_request.getParameter("country");

		try
		{
			LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
			GlobalSightLocale locale = new GlobalSightLocale(language, country,
					false);
			localeMgr.addLocale(locale);
		}
		catch (Exception lme)
		{
			throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
					lme);
		}
	}

	private void dataForTable(HttpServletRequest p_request,
			HttpSession p_session) throws RemoteException, NamingException,
			GeneralException
	{
		LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
		// Get all locale pairs
		Vector lps = localeMgr.getSourceTargetLocalePairs();

		LocalePairFilter lpfilter = new LocalePairFilter();
		List arrlps = lpfilter.filter(p_session, lps);
		Locale uiLocale = (Locale) p_session
				.getAttribute(WebAppConstants.UILOCALE);
		// Get the number per page
		int numPerPage = getNumPerPage(p_request, p_session);

		setTableNavigation(p_request, p_session, arrlps, new LocalePairComparator(
				uiLocale), numPerPage, LP_LIST, LP_KEY);
	}

	private int getNumPerPage(HttpServletRequest p_request,
			HttpSession p_session)
	{
		int result = 10;

		SessionManager sessionManager = (SessionManager) p_session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		String lpNumPerPage = p_request.getParameter("numOfPageSize");
		if (StringUtil.isEmpty(lpNumPerPage))
		{
			lpNumPerPage = (String) sessionManager.getAttribute("lpNumPerPage");
		}

		if (lpNumPerPage != null)
		{
			sessionManager.setAttribute("lpNumPerPage", lpNumPerPage.trim());
			if ("all".equalsIgnoreCase(lpNumPerPage))
			{
				result = Integer.MAX_VALUE;
			}
			else
			{
				try
				{
					result = Integer.parseInt(lpNumPerPage);
				}
				catch (NumberFormatException ignore)
				{
					result = 10;
				}
			}
		}

		return result;
	}

	/**
	 * Create property file
	 * 
	 * @param userName
	 * @param companyId
	 * @return File
	 * */
	private static File createPropertyFile(String userName, Long companyId)
	{
		StringBuffer filePath = new StringBuffer();
		filePath.append(AmbFileStoragePathUtils.getFileStorageDirPath())
				.append(File.separator).append("GlobalSight")
				.append(File.separator).append("LocalePairs")
				.append(File.separator).append("export");
		File file = new File(filePath.toString());
		file.mkdirs();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = "LocalePairs_" + userName + "_"
				+ sdf.format(new Date()) + ".properties";
		File propertiesFile = new File(file, fileName);

		return propertiesFile;
	}

	private static void writeToFile(File writeInFile, byte[] bytes)
	{
		writeInFile.getParentFile().mkdirs();

		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(writeInFile, true);
			fos.write(bytes);
		}
		catch (Exception e)
		{
		}
		finally
		{
			try
			{
				fos.close();
			}
			catch (IOException e)
			{

			}
		}
	}

	private void handleFilters(HttpServletRequest p_request,
	        SessionManager sessionMgr, String action)
	{
	    String lpSourceFilter = (String) p_request.getParameter("lpSourceFilter");
	    String lpTargetFilter = (String) p_request.getParameter("lpTargetFilter");
	    String lpCompanyFilter = (String) p_request.getParameter("lpCompanyFilter");
		if (lpSourceFilter == null) {
			lpSourceFilter = "";
		}
		if (lpTargetFilter == null) {
			lpTargetFilter = "";
		}
		if (lpCompanyFilter == null) {
			lpCompanyFilter = "";
		}
	    sessionMgr.setAttribute(LocalePairConstants.FILTER_SOURCELOCALE, lpSourceFilter);
	    sessionMgr.setAttribute(LocalePairConstants.FILTER_TARGETLOCALE, lpTargetFilter);
	    sessionMgr.setAttribute(LocalePairConstants.FILTER_COMPANY, lpCompanyFilter);
	}

    private  void resetSessionTableInfo(HttpSession p_session, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);

        Integer sortType = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.SORTING);
        Boolean reverseSort = (Boolean) sessionMgr.getAttribute(p_key
                + TableConstants.REVERSE_SORT);
        Integer lastPage = (Integer) sessionMgr.getAttribute(p_key
                + TableConstants.LAST_PAGE_NUM);
		String lpSourceFilter = (String) sessionMgr
				.getAttribute(LocalePairConstants.FILTER_SOURCELOCALE);
		String lpTargetFilter = (String) sessionMgr
				.getAttribute(LocalePairConstants.FILTER_TARGETLOCALE);
		String lpCompanyFilter = (String) sessionMgr
				.getAttribute(LocalePairConstants.FILTER_COMPANY);

	    sessionMgr.clear();

	    sessionMgr.setAttribute(p_key + TableConstants.SORTING, sortType);
        sessionMgr.setAttribute(p_key + TableConstants.REVERSE_SORT, reverseSort);
        sessionMgr.setAttribute(p_key + TableConstants.LAST_PAGE_NUM, lastPage);
	    sessionMgr.setAttribute(LocalePairConstants.FILTER_SOURCELOCALE, lpSourceFilter);
	    sessionMgr.setAttribute(LocalePairConstants.FILTER_TARGETLOCALE, lpTargetFilter);
	    sessionMgr.setAttribute(LocalePairConstants.FILTER_COMPANY, lpCompanyFilter);
    }

}