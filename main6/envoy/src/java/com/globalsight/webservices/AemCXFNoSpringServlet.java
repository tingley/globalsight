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

	private static final String CXF_ADDRESS = "/WebService4AEM";

	@Override
	public void loadBus(ServletConfig servletConfig)
	{
		super.loadBus(servletConfig);

        Bus bus = this.getBus();
		BusFactory.setDefaultBus(bus);
		Endpoint.publish(CXF_ADDRESS, new WebService4AEMImpl());

		logger.info("Webservice URL for AEM is: http(s)://hostname:port/globalsight/aemServices/WebService4AEM?wsdl");
	}
}
