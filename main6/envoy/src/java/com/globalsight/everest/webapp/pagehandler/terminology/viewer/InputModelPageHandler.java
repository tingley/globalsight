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

package com.globalsight.everest.webapp.pagehandler.terminology.viewer;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.edit.online.MTTranslationHandler;
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.java.InputModel;

public class InputModelPageHandler extends PageActionHandler
{
    static private final Logger CATEGORY = Logger
            .getLogger(MTTranslationHandler.class);
    //private String companyId = new String();
    private String id = new String();
    private String user = new String();
    private String name = new String();
    private String type = new String();
    private String value = new String();
    IUserdataManager manager = null;
    
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        //companyId = CompanyThreadLocal.getInstance().getValue();
        
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);
        manager =
            (IUserdataManager)sessionMgr.getAttribute(TERMBASE_USERDATA);
        
        id = request.getParameter("id");
        type = request.getParameter("type");
        user = request.getParameter("username");
        name = request.getParameter("name");
        value = request.getParameter("value");
    }
    
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }
    
    @ActionHandler(action = WebAppConstants.TERMBASE_ACTION_LOAD_OBJECT, formClass = "")
    public void loadModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map returnValue = new HashMap();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            InputModel model = manager.getObject(Long.parseLong(id));
            returnValue.put("id", model.getId());
            returnValue.put("userName", model.getUserName());
            returnValue.put("name", model.getName());
            returnValue.put("type", model.getType());
            returnValue.put("value", model.getValue());
            returnValue.put("isDefault", model.getIsDefault());
            returnValue.put("result", "sucess");
        }
        catch(Exception e) {
            returnValue.put("result", "error");
            result = "error";
            CATEGORY.error("Exception", e);
        }
        finally
        {
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = WebAppConstants.TERMBASE_ACTION_CREATE_OBJECT, formClass = "")
    public void createModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            manager.createObject(mapType(type), user, name, value);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Create object value = `" + value + "'");
            }
            
            result = "success";
        }
        catch(Exception e) {
            result = "error";
            CATEGORY.error("Exception", e);
        }
        finally
        {
            returnValue.put("model", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = WebAppConstants.TERMBASE_ACTION_MODIFY_OBJECT, formClass = "")
    public void modifyModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            manager.modifyObject(Long.parseLong(id), mapType(type), user, name, value);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Modify object value = `" + value + "'");
            }
            
            result = "success";
        }
        catch(Exception e) {
            result = "error";
            CATEGORY.error("Exception", e);
        }
        finally
        {
            returnValue.put("model", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = WebAppConstants.TERMBASE_ACTION_REMOVE_OBJECT, formClass = "")
    public void removeModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            result = manager.deleteObject(Long.parseLong(id));

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Delete object value = `" + value + "'");
            }
        }
        catch(Exception e) {
            result = "error";
            CATEGORY.error("Exception", e);
        }
        finally
        {
            returnValue.put("model", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = WebAppConstants.TERMBASE_ACTION_MAKE_DEFAULT_OBJECT, formClass = "")
    public void makeDefaultModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            if (manager.isSetDefault(Long.parseLong(id)))
            {
                result = "The default input model has been set!";
            }
            else
            {
                manager.makeDefaultObject(Long.parseLong(id));
                result = "success";
            }
        }
        catch(Exception e) {
            result = "error";
            CATEGORY.error("Exception", e);
        }
        finally
        {
            returnValue.put("model", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = WebAppConstants.TERMBASE_ACTION_UNSET_DEFAULT_OBJECT, formClass = "")
    public void unsetDefaultModel(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception {
        String result = "";
        Map<String, String> returnValue = new HashMap<String, String>();
        ServletOutputStream out = response.getOutputStream();
        
        try {
            manager.unsetDefaultObject(Long.parseLong(id));
            result = "success";
        }
        catch(Exception e) {
            result = "error";
            CATEGORY.error("Exception", e);
        }
        finally
        {
            returnValue.put("model", result);
            out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
            out.close();
            pageReturn();
        }
    }

    
    private int mapType(String p_type)
    {
        return Integer.parseInt(p_type);
    }
}
