/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.webservices;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.apache.log4j.Logger;

public class AemCXFNoSpringServlet extends CXFNonSpringServlet
{
	private static final long serialVersionUID = 5796300039115205931L;

	private static final Logger logger = Logger
			.getLogger(AemCXFNoSpringServlet.class);

	private static String PORT_FOR_AEM_SERVICE = "port_for_aem_service";
	private static String PATH_FOR_AEM_SERVICE = "path_for_aem_service";
	private static String CLASS_FOR_AEM_SERVICE = "class_for_aem_service";
	
	@SuppressWarnings("rawtypes")
	public void loadBus(ServletConfig servletConfig)
	{
		super.loadBus(servletConfig);
		Bus bus = this.getBus();
		BusFactory.setDefaultBus(bus);

		// Get init-params from web.xml file for Adobe Experience Manager
		// services.
		String port = null;
		String path = null;
		String className = null;
		Enumeration<String> enums = getInitParameterNames();
		while (enums.hasMoreElements())
		{
			String key = enums.nextElement();
			String value = getInitParameter(key);
			if (PORT_FOR_AEM_SERVICE.equalsIgnoreCase(key))
			{
				port = value;
			}
			else if (PATH_FOR_AEM_SERVICE.equalsIgnoreCase(key))
			{
				path = value;
			}
			else if (CLASS_FOR_AEM_SERVICE.equalsIgnoreCase(key))
			{
				className = value;
			}
		}
		logger.info(" " + PORT_FOR_AEM_SERVICE + "==" + port);
		logger.info(" " + PATH_FOR_AEM_SERVICE + "==" + path);
		logger.info(" " + CLASS_FOR_AEM_SERVICE + "==" + className);

		// Get a url for AEM like "http://hostname:port/services/WebService4AEM";
		String url = AmbassadorUtil.getCapLoginOrPublicUrl().toLowerCase()
				.replace("\\", "/");
		if (url.endsWith("/globalsight"))
		{
			url = url.substring(0, url.lastIndexOf("/globalsight"));
		}
		int index = url.lastIndexOf(":");
		if (index > 5)
		{
			url = url.substring(0, url.lastIndexOf(":"));
		}
		url += ":" + port;
		if (!path.startsWith("/"))
		{
			url += "/";
		}
		url += path;
		logger.info("The public address for AEM is: " + url + "?wsdl");

		// Publish the service
		try
		{
			Class clz = Class.forName(className);
			try
			{
				 Endpoint.publish(url, clz.newInstance());
			}
			catch (InstantiationException e)
			{
				logger.error(e);
			}
			catch (IllegalAccessException e)
			{
				logger.error(e);
			}
		}
		catch (ClassNotFoundException e)
		{
			logger.error(e);
		}
	}
}
