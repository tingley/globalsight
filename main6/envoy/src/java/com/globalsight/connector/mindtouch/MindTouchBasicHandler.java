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
package com.globalsight.connector.mindtouch;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.connector.mindtouch.util.MindTouchHelper;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnectorTargetServer;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

public class MindTouchBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(MindTouchBasicHandler.class);

    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.mindtouch.MindTouchConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();

        try 
        {
        	// Test source server
            MindTouchConnector mtc = (MindTouchConnector) form;
            MindTouchHelper srcHelper = new MindTouchHelper(mtc);
            JSONObject json = new JSONObject();
            String errorMsg = "";
            if(StringUtil.isNotEmpty(srcHelper.doTest()))
            {
            	errorMsg = "Failed to connect to MindTouch source server";
            }
            srcHelper.shutdownHttpClient();

            // Test target servers
            if (StringUtil.isEmpty(errorMsg))
            {
                String targetLocaleStr = request.getParameter("targetLocaleStr");
                if(StringUtil.isNotEmpty(targetLocaleStr))
                {
                	String[] targetLocales = targetLocaleStr.split(",");
					for (String targetLocale : targetLocales)
                	{
                		if(StringUtil.isNotEmpty(targetLocale))
                		{
                			mtc.setUrl(request.getParameter("targetUrl" + targetLocale));
                			mtc.setUsername(request.getParameter("targetUsername" + targetLocale));
                			mtc.setPassword(request.getParameter("targetPassword" + targetLocale));
                			MindTouchHelper trgHelper = new MindTouchHelper(mtc);
                			if(StringUtil.isNotEmpty(trgHelper.doTest()))
                            {
                            	errorMsg = "Failed to connect to MindTouch source server for locale: " + targetLocale;
                            	break;
                            }
                			trgHelper.shutdownHttpClient();
                		}
                	}
                }
            }

            json.put("error", errorMsg);
            out.write(json.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            JSONObject json = new JSONObject();
            json.put("error", bundle.getString("error_mindtouch_connector"));
            out.write(json.toString().getBytes("UTF-8"));
        }
        finally
        {
            out.close();
            pageReturn();
        }
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
        String id = request.getParameter("id");
        if (id != null)
        {
            MindTouchConnector connector = HibernateUtil.get(
                    MindTouchConnector.class, Long.parseLong(id));
            request.setAttribute("mindtouch", connector);

            List<MindTouchConnectorTargetServer> targetServers = 
            		MindTouchManager.getAllTargetServers(Long.parseLong(id));
            request.setAttribute("targetServers", targetServers);
        }

        String names = "";
        List mtConnectors = MindTouchManager.getAllConnectors();
		for (Object o : mtConnectors)
        {
            MindTouchConnector mtc = (MindTouchConnector) o;
            if (id != null && id.equals("" + mtc.getId()))
            {
                continue;
            }
            names += "," + mtc.getName() + ",";
        }

        request.setAttribute("names", names);
        request.setAttribute("targetLocales", MindTouchHelper.getAllTargetLocales());

		try
		{
			Vector allLocales = ServerProxy.getLocaleManager()
					.getAvailableLocales();
			request.setAttribute("allAvailableLocales", allLocales);
		}
		catch (Exception e)
		{
			logger.error(e);
		}

		response.setCharacterEncoding("utf-8");
    }
}
