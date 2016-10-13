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
package com.globalsight.connector.eloqua;

import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class EloquaBasicHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(EloquaBasicHandler.class);
    
    @ActionHandler(action = "connect", formClass = "com.globalsight.cxe.entity.eloqua.EloquaConnector", loadFromDb = true)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        pageReturn();
        
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();
        EloquaConnector conn = (EloquaConnector) form;
        EloquaHelper helper = new EloquaHelper(conn);
        
        if (helper.doTest()) 
        {
            HttpSession session = request.getSession();
            SessionManager sessionManager = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            sessionManager.setAttribute("EloquaConnector", conn);
            logger.debug("Saving eloqua connector...");
            HibernateUtil.saveOrUpdate(conn);
            logger.debug("Saving eloqua connector finished.");
            
            JSONObject ob = new JSONObject();
            ob.put("id", conn.getId());
            out.write(ob.toString().getBytes("UTF-8"));
        }
        else
        {
            JSONObject ob = new JSONObject();
            ob.put("error", bundle.getString("error_eloqua_connector"));
            out.write(ob.toString().getBytes("UTF-8"));
        }       
    }

    @ActionHandler(action = "test", formClass = "com.globalsight.cxe.entity.eloqua.EloquaConnector")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        ServletOutputStream out = response.getOutputStream();

        try 
        {
            EloquaConnector conn = (EloquaConnector) form;
            
            EloquaHelper helper = new EloquaHelper(conn);
            
            JSONObject ob = new JSONObject();
            if (helper.doTest()) 
            {
                HttpSession session = request.getSession();
                SessionManager sessionManager = (SessionManager) session
                        .getAttribute(WebAppConstants.SESSION_MANAGER);
                sessionManager.setAttribute("EloquaConnector", conn);
                
                ob.put("canUse", true);
                ob.put("url", conn.getUrl());
                
            }
            else
            {
                ob.put("canUse", false);
                ob.put("error", bundle.getString("error_eloqua_connector"));
            }
            
            out.write(ob.toString().getBytes("UTF-8"));
        } 
        catch (Exception e) 
        {
            String s = "({\"error\" : "
                    + JsonUtil.toObjectJson(bundle.getString("error_eloqua_connector")) + "})";
            out.write(s.getBytes("UTF-8"));
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

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        String id = request.getParameter("id");
        if (id != null)
        {
        	EloquaConnector connector = HibernateUtil.get(EloquaConnector.class, Long
                    .parseLong(id));
            request.setAttribute("eloqua", connector);
        }
        
        String names = "";
        List es = EloquaManager.getAllConnector();
        for (Object o :es)
        {
        	EloquaConnector e = (EloquaConnector) o;
        	if (id != null && id.equals("" + e.getId()))
        	{
        		continue;
        	}
        	names += "," + e.getName() + ",";
        }
        
        request.setAttribute("names", names);
    }
}
