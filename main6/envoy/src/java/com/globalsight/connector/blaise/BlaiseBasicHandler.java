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
package com.globalsight.connector.blaise;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.connector.blaise.util.BlaiseHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;

public class BlaiseBasicHandler extends PageActionHandler
{
	static private final Logger logger = Logger
			.getLogger(BlaiseBasicHandler.class);

    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.blaise.BlaiseConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();

        try 
        {
        	BlaiseConnector blac = (BlaiseConnector) form;
        	BlaiseHelper helper = new BlaiseHelper(blac);
            JSONObject json = new JSONObject();
            String errorMsg = helper.doTest();
            json.put("error", errorMsg);
            out.write(json.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            JSONObject json = new JSONObject();
            json.put("error", bundle.getString("error_blaise_connector"));
            out.write(json.toString().getBytes("UTF-8"));
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    @Override
	public void beforeAction(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			EnvoyServletException
    {
		String id = request.getParameter("id");
		if (id != null && !"-1".equals(id))
		{
			BlaiseConnector connector = BlaiseManager
					.getBlaiseConnectorById(Long.parseLong(id));
			request.setAttribute("blaise", connector);
		}

        String names = "";
		for (Object o : BlaiseManager.getAllConnectors())
        {
            BlaiseConnector mtc = (BlaiseConnector) o;
            if (id != null && id.equals("" + mtc.getId()))
            {
                continue;
            }
            names += "," + mtc.getName() + ",";
        }
        request.setAttribute("names", names);

        response.setCharacterEncoding("utf-8");
    }

	@Override
	public void afterAction(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			EnvoyServletException
	{

	}
}
