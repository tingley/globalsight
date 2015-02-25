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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.globalsight.connector.mindtouch.util.MindTouchHelper;
import com.globalsight.cxe.entity.mindtouch.MindTouchConnector;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class MindTouchBasicHandler extends PageActionHandler
{
    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.mindtouch.MindTouchConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();

        try 
        {
            MindTouchConnector mtc = (MindTouchConnector) form;
            MindTouchHelper helper = new MindTouchHelper(mtc);
            String testResult = helper.doTest();
            JSONObject json = new JSONObject();
            if (testResult == null || "".equals(testResult))
            {
                json.put("error", "");
            }
            else
            {
                json.put("error", testResult);
            }
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
        }

        String names = "";
        List mtConnectors = MindTouchManager.getAllConnectors();
        for (Object o :mtConnectors)
        {
            MindTouchConnector mtc = (MindTouchConnector) o;
            if (id != null && id.equals("" + mtc.getId()))
            {
                continue;
            }
            names += "," + mtc.getName() + ",";
        }

        request.setAttribute("names", names);

        response.setCharacterEncoding("utf-8");
    }
}
