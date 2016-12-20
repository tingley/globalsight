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
package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;

/**
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityBasicHandler extends PageActionHandler
{
    @ActionHandler(action = "test", formClass = "com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        ServletOutputStream out = response.getOutputStream();

        try 
        {
            PerplexityService perplexityService = (PerplexityService) form;
            PerplexityScoreHelper helper = new PerplexityScoreHelper();
            String accessToken = helper.getToken(perplexityService.getUrl(), perplexityService.getUserName(), perplexityService.getPassword());
            
            JSONObject ob = new JSONObject();
            ob.put("canUse", accessToken != null);
            out.write(ob.toString().getBytes("UTF-8"));
        } 
        catch (Exception e) 
        {
            JSONObject ob = new JSONObject();
            ob.put("canUse", false);
            out.write(ob.toString().getBytes("UTF-8"));
        } 
        finally 
        {
            out.close();
            pageReturn();
        }
    }
    
    @ActionHandler(action = "new", formClass = "")
    public void newPerplexity(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_PERPLEXITY);
    }
    
    @ActionHandler(action = "edit", formClass = "")
    public void editPerplexity(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        FormUtil.addSubmitToken(request, FormUtil.Forms.NEW_PERPLEXITY);
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        String id = request.getParameter("id");
        if (id != null)
        {
            PerplexityService perplexity = HibernateUtil.get(PerplexityService.class, Long
                    .parseLong(id));
            request.setAttribute("perplexity", perplexity);
        }
        
        String names = "";
        List<PerplexityService> es = PerplexityManager.getAllPerplexity();
        for (PerplexityService e :es)
        {
            if (id != null && id.equals("" + e.getId()))
            {
                continue;
            }
            names += "," + e.getName() + ",";
        }
        
        request.setAttribute("names", names);
    }
    
    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }
}
