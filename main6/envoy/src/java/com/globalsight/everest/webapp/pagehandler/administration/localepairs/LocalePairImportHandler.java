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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

public class LocalePairImportHandler extends PageHandler implements
		LocalePairConstants, WebAppConstants
{
	private static final Logger logger = Logger
			.getLogger(LocalePairImportHandler.class);
	private Map<String, Integer> filter_percentage_map = new HashMap<String, Integer>();
	private Map<String, String> filter_error_map = new HashMap<String, String>();

	public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
			HttpServletRequest p_request, HttpServletResponse p_response,
			ServletContext p_context) throws ServletException, IOException,
			EnvoyServletException
	{
		HttpSession session = p_request.getSession(false);
		String sessionId = session.getId();
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		String companyId = CompanyThreadLocal.getInstance().getValue();
		boolean isSuperAdmin = ((Boolean) session
				.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

		String action = p_request.getParameter("action");
		try
		{
			if (LocalePairConstants.IMPORT.equals(action))
			{
				if (isSuperAdmin)
				{
					importLocalePair(p_request);
					p_request.setAttribute("currentId", companyId);
				}
			}
			else if ("startUpload".equals(action))
			{
				File uploadedFile = this.uploadFile(p_request);
				if (isSuperAdmin)
				{
					String importToCompId = p_request.getParameter("companyId");
					session.setAttribute("importToCompId", importToCompId);
				}
				session.setAttribute("uploading_filter", uploadedFile);
			}
			else if ("doImport".equals(action))
			{
				int count = 0;
				if (sessionMgr.getAttribute("count") != null)
				{
					count = (Integer) sessionMgr.getAttribute("count");
					if (count == 1)
					{
						count++;
						sessionMgr.setAttribute("count", count);
					}
				}
				else
				{
					count++;
					sessionMgr.setAttribute("count", count);
				}
				if (session.getAttribute("uploading_filter") != null)
				{
					filter_percentage_map.clear();// .remove(sessionId);
					filter_error_map.clear();// .remove(sessionId);
					File uploadedFile = (File) session
							.getAttribute("uploading_filter");
					String importToCompId = (String) session
							.getAttribute("importToCompId");

					session.removeAttribute("importToCompId");
					session.removeAttribute("uploading_filter");
					DoImport imp = new DoImport(sessionId, uploadedFile,
							companyId, importToCompId);
					imp.start();
				}
				else
				{
					logger.error("No uploaded user info file.");
				}
			}
			else if ("refreshProgress".equals(action))
			{
				this.refreshProgress(p_request, p_response, sessionId);
				return;
			}
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

	private void importLocalePair(HttpServletRequest p_request)
			throws EnvoyServletException, RemoteException
	{
		String hql = "select id from Company";
		List<Long> companyIdList = (List<Long>) HibernateUtil.search(hql);
		p_request.setAttribute("companyIdList", companyIdList);
	}

	/**
	 * Upload the properties file to FilterConfigurations/import folder
	 * 
	 * @param request
	 */
	private File uploadFile(HttpServletRequest request)
	{
		File f = null;
		try
		{
			String tmpDir = AmbFileStoragePathUtils.getFileStorageDirPath()
					+ File.separator + "GlobalSight" + File.separator
					+ "LocalePairs" + File.separator + "import";
			boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
			if (isMultiPart)
			{
				DiskFileItemFactory factory = new DiskFileItemFactory();
				factory.setSizeThreshold(1024000);
				ServletFileUpload upload = new ServletFileUpload(factory);
				List<?> items = upload.parseRequest(request);
				for (int i = 0; i < items.size(); i++)
				{
					FileItem item = (FileItem) items.get(i);
					if (!item.isFormField())
					{
						String filePath = item.getName();
						if (filePath.contains(":"))
						{
							filePath = filePath
									.substring(filePath.indexOf(":") + 1);
						}
						String originalFilePath = filePath.replace("\\",
								File.separator).replace("/", File.separator);
						String fileName = tmpDir + File.separator
								+ originalFilePath;
						f = new File(fileName);
						f.getParentFile().mkdirs();
						item.write(f);
					}
				}
			}
			return f;
		}
		catch (Exception e)
		{
			logger.error("File upload failed.", e);
			return null;
		}
	}

	/**
	 * Import the user info into system
	 * 
	 * @param request
	 * @param response
	 * @param sessionId
	 */
	private void refreshProgress(HttpServletRequest request,
			HttpServletResponse response, String sessionId)
	{
		HttpSession session = request.getSession(false);
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		int count = 0;
		if (sessionMgr.getAttribute("count") != null)
		{
			count = (Integer) sessionMgr.getAttribute("count");
		}
		else
		{
			count++;
			sessionMgr.setAttribute("count", count);
		}
		try
		{
			int percentage;
			if (filter_percentage_map.get(sessionId) == null)
			{
				percentage = 0;
			}
			else
			{
				if (count == 1)
				{
					percentage = 0;
				}
				else
				{
					percentage = filter_percentage_map.get(sessionId);
				}
			}

			String msg;
			if (filter_error_map.get(sessionId) == null)
			{
				msg = "";
			}
			else
			{
				if (count == 1)
				{
					msg = "";
				}
				else
				{
					msg = filter_error_map.get(sessionId);
				}
			}
			count++;
			sessionMgr.setAttribute("count", count);

			response.setContentType("text/html;charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.write(String.valueOf(percentage + "&" + msg));
			writer.close();
			if (percentage == 100)
			{
				sessionMgr.removeElement("count");
			}
		}
		catch (Exception e)
		{
			logger.error("Refresh failed.", e);
		}

	}

	private class DoImport extends MultiCompanySupportedThread
	{
		private Map<String, String> localePairMap = new HashMap<String, String>();
		private Map<String, GlobalSightLocale> localeMap = new HashMap<String, GlobalSightLocale>();
		private File uploadedFile;
		private String companyId;
		private String sessionId;
		private String importToCompId;

		public DoImport(String sessionId, File uploadedFile, String companyId,
				String importToCompId)
		{
			this.sessionId = sessionId;
			this.uploadedFile = uploadedFile;
			this.companyId = companyId;
			this.importToCompId = importToCompId;
		}

		public void run()
		{
			CompanyThreadLocal.getInstance().setIdValue(this.companyId);
			this.analysisAndImport(uploadedFile);
		}

		private void analysisAndImport(File uploadedFile)
		{
			Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();

			try
			{
				String[] keyArr = null;
				String key = null;
				String strKey = null;
				String strValue = null;
				InputStream is;
				is = new FileInputStream(uploadedFile);
				Properties prop = new Properties();
				prop.load(is);
				Enumeration enum1 = prop.propertyNames();
				while (enum1.hasMoreElements())
				{
					// The key profile
					strKey = (String) enum1.nextElement();
					key = strKey.substring(0, strKey.lastIndexOf('.'));
					keyArr = strKey.split("\\.");
					// Value in the properties file
					strValue = prop.getProperty(strKey);
					Set<String> keySet = map.keySet();
					if (keySet.contains(key))
					{
						Map<String, String> valueMap = map.get(key);
						Set<String> valueKey = valueMap.keySet();
						if (!valueKey.contains(keyArr[2]))
						{
							valueMap.put(keyArr[2], strValue);
						}
					}
					else
					{
						Map<String, String> valueMap = new HashMap<String, String>();
						valueMap.put(keyArr[2], strValue);
						map.put(key, valueMap);
					}
				}
				// Data analysis
				analysisData(map);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void analysisData(Map<String, Map<String, String>> map)
		{
			if (map.isEmpty())
				return;

			Map<String, List> dataMap = new HashMap<String, List>();
			List<LocalePair> localePairList = new ArrayList<LocalePair>();
			List<GlobalSightLocale> globalSightLocaleList = new ArrayList<GlobalSightLocale>();
			Set<String> keySet = map.keySet();
			Iterator it = keySet.iterator();
			while (it.hasNext())
			{
				String key = (String) it.next();
				String[] keyArr = key.split("\\.");
				Map<String, String> valueMap = map.get(key);
				if (!valueMap.isEmpty())
				{
					if (keyArr[0].equalsIgnoreCase("LocalPairs"))
					{
						LocalePair localePair = putDataIntoLocalePair(valueMap);
						localePairList.add(localePair);
					}
					else if (keyArr[0].equalsIgnoreCase("Locale"))
					{
						GlobalSightLocale globalSightLocale = putDataIntoLocale(valueMap);
						globalSightLocaleList.add(globalSightLocale);
					}
				}
			}

			if (localePairList.size() > 0)
				dataMap.put("LocalPairs", localePairList);

			if (globalSightLocaleList.size() > 0)
				dataMap.put("Locale", globalSightLocaleList);

			// Storing data
			storeDataToDatabase(dataMap);
		}

		private void storeDataToDatabase(Map<String, List> dataMap)
		{
			if (dataMap.isEmpty())
				return;
			int i = 0;
			int size = dataMap.keySet().size();

			try
			{
				if (dataMap.containsKey("Locale"))
				{
					i++;
					storeGlobalSightLocaleData(dataMap);
					this.cachePercentage(i, size);
					Thread.sleep(100);
				}

				if (dataMap.containsKey("LocalPairs"))
				{
					i++;
					storeLocalePairData(dataMap);
					this.cachePercentage(i, size);
					Thread.sleep(100);
				}

				addMessage("<b>Imported successfully !</b>");
			}
			catch (Exception e)
			{
				logger.error("Failed to import Locale Pairs.", e);
				addToError(e.getMessage());
			}
		}

		private void storeGlobalSightLocaleData(Map<String, List> dataMap)
		{
			List<GlobalSightLocale> globalSightLocaleList = dataMap.get("Locale");
			LocaleManagerWLRemote localeMangerLocal = ServerProxy.getLocaleManager();

			GlobalSightLocale locale = null;
			try
			{
				for (int i = 0; i < globalSightLocaleList.size(); i++)
				{
					locale = globalSightLocaleList.get(i);
					long oldId = locale.getId();
					GlobalSightLocale gslInDb = localeMangerLocal.addLocale(locale);
					localeMap.put(String.valueOf(oldId), gslInDb);
				}
			}
			catch (Exception e)
			{
				logger.error("Error when upload GlobalSight Locale data", e);
				addToError("Upload GlobalSight Locale data failed!");
			}
		}

		private void storeLocalePairData(Map<String, List> dataMap)
		{
			LocalePair localePair = null;
			List<LocalePair> localePairList = dataMap.get("LocalPairs");
			LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
			try
			{
				for (int i = 0; i < localePairList.size(); i++)
				{
					localePair = localePairList.get(i);
					if (localePairMap.containsKey(String.valueOf(localePair
							.getId())))
					{
						String value = localePairMap.get(String
								.valueOf(localePair.getId()));
						String[] valueArr = value.split("\\|");
						if (valueArr[0].endsWith(".source"))
						{
							String sourceId = valueArr[0].substring(0,
									valueArr[0].indexOf("."));
							String targetId = valueArr[1].substring(0,
									valueArr[1].indexOf("."));
							GlobalSightLocale source = localeMap.get(sourceId);
							GlobalSightLocale target = localeMap.get(targetId);

							if (source != null)
								localePair.setSource(source);
							if (target != null)
								localePair.setTarget(target);
						}
						else if (valueArr[0].endsWith(".target"))
						{
							String targetId = valueArr[0].substring(0,
									valueArr[0].indexOf("."));
							String sourceId = valueArr[1].substring(0,
									valueArr[1].indexOf("."));
							GlobalSightLocale source = localeMap.get(sourceId);
							GlobalSightLocale target = localeMap.get(targetId);

							if (source != null)
								localePair.setSource(source);
							if (target != null)
								localePair.setTarget(target);
						}
					}

                    localeMgr.addSourceTargetLocalePair(localePair.getSource(),
                            localePair.getTarget(), localePair.getCompanyId());
				}
			}
			catch (Exception e)
			{
				String msg = "Upload LocalePair data failed !";
				logger.warn(msg);
				addToError(msg);
			}
		}

		private LocalePair putDataIntoLocalePair(Map<String, String> valueMap)
		{
			LocalePair localePair = new LocalePair();
			String key = null;
			StringBuffer value = new StringBuffer();
			;
			String keyField = null;
			String valueField = null;
			Set<String> valueKey = valueMap.keySet();
			Iterator itor = valueKey.iterator();
			while (itor.hasNext())
			{
				keyField = (String) itor.next();
				valueField = valueMap.get(keyField);
				if (keyField.equalsIgnoreCase("ID"))
				{
					key = valueField;
					localePair.setId(Long.parseLong(valueField));
				}
				else if (keyField.equalsIgnoreCase("SOURCE_LOCALE_ID"))
				{
					value.append(valueField).append(".source").append("|");
				}
				else if (keyField.equalsIgnoreCase("TARGET_LOCALE_ID"))
				{
					value.append(valueField).append(".target").append("|");
				}
				else if (keyField.equalsIgnoreCase("COMPANY_ID"))
				{
					if (importToCompId != null && !importToCompId.equals("-1"))
					{
						localePair.setCompanyId(Long.parseLong(importToCompId));
					}
					else
					{
						localePair.setCompanyId(Long.parseLong(companyId));
					}
				}
				else if (keyField.equalsIgnoreCase("IS_ACTIVE"))
				{
					localePair.setIsActive(Boolean.parseBoolean(valueField));
				}
			}
			if (value.toString().endsWith("|"))
			{
				String valueStr = value.toString().substring(0,
						value.toString().lastIndexOf("|"));
				localePairMap.put(key, valueStr);
			}

			return localePair;
		}

		private GlobalSightLocale putDataIntoLocale(Map<String, String> valueMap)
		{
			GlobalSightLocale locale = new GlobalSightLocale();
			String keyLocale = null;
			String valueLocale = null;
			Set<String> valueKey = valueMap.keySet();
			Iterator itor = valueKey.iterator();
			while (itor.hasNext())
			{
				keyLocale = (String) itor.next();
				valueLocale = valueMap.get(keyLocale);
				if (keyLocale.equalsIgnoreCase("ID"))
				{
					locale.setId(Long.parseLong(valueLocale));
				}
				else if (keyLocale.equalsIgnoreCase("ISO_LANG_CODE"))
				{
					locale.setLanguage(valueLocale);
				}
				else if (keyLocale.equalsIgnoreCase("ISO_COUNTRY_CODE"))
				{
					locale.setCountry(valueLocale);
				}
				else if (keyLocale.equalsIgnoreCase("IS_UI_LOCALE"))
				{
					locale.setIsUiLocale(Boolean.parseBoolean(valueLocale));
				}
			}

			return locale;
		}

		private void cachePercentage(double per, int size)
		{
			int percentage = (int) (per * 100 / size);
			filter_percentage_map.put(sessionId, percentage);
		}

		private void addToError(String msg)
		{
			String former = filter_error_map.get(sessionId) == null ? ""
					: filter_error_map.get(sessionId);
			filter_error_map.put(sessionId, former + "<p style='color:red'>"
					+ msg);
		}

		private void addMessage(String msg)
		{
			String former = filter_error_map.get(sessionId) == null ? ""
					: filter_error_map.get(sessionId);
			filter_error_map.put(sessionId, former + "<p style='color:blue'>"
					+ msg);
		}
	}
}