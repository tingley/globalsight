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

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.util.comparator.PerplexityComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.Assert;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

import net.sf.json.JSONObject;

/**
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger.getLogger(PerplexityMainHandler.class);

    private List<PerplexityService> perplexitys = null;

    /**
     * Get list of all perplexity.
     */
    private void dataForTable(HttpServletRequest request) throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

        if (perplexitys == null)
        {
            perplexitys = PerplexityManager.getAllPerplexity();
        }

        Integer orgSize = (Integer) session.getAttribute("perplexityPageSize");
        int size = orgSize == null ? 10 : orgSize;
        String numOfPerPage = request.getParameter("numOfPageSize");
        if (StringUtil.isNotEmpty(numOfPerPage))
        {
            try
            {
                size = Integer.parseInt(numOfPerPage);
            }
            catch (Exception e)
            {
                size = Integer.MAX_VALUE;
            }

            session.setAttribute("perplexityPageSize", size);
        }

        setTableNavigation(request, session, perplexitys, new PerplexityComparator(uiLocale), size,
                "perplexityList", "perplexityKey");
    }

    @ActionHandler(action = "save", formClass = "com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityService", loadFromDb = true, formToken = FormUtil.Forms.NEW_PERPLEXITY)
    public void save(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        logger.debug("Saving perplexity service...");

        PerplexityService perplexityService = (PerplexityService) form;
        if (!PerplexityManager.isExistName(perplexityService.getName(),
                Long.toString(perplexityService.getId())))
        {
            HibernateUtil.saveOrUpdate(perplexityService);
        }

        logger.debug("Saving perplexity service finished.");
    }

    @ActionHandler(action = "filter", formClass = "com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.PerplexityServiceFilter")
    public void filter(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        perplexitys = PerplexityManager.getAllPerplexity();
        PerplexityServiceFilter filter = (PerplexityServiceFilter) form;
        perplexitys = filter.filter(perplexitys);

        request.setAttribute("nameFilter", filter.getNameFilter());
        request.setAttribute("urlFilter", filter.getUrlFilter());
        request.setAttribute("descriptionFilter", filter.getDescriptionFilter());
    }
    
    @ActionHandler(action = "canRemove", formClass = "")
    public void canRemove(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        String idAll = request.getParameter("ids");
        String[] ids = idAll.split(",");
        
        StringBuffer names = new StringBuffer();
        
        String hql = "from WorkflowTemplateInfo w where w.perplexityService.id = ?";
        for (String id : ids)
        {
            WorkflowTemplateInfo wf = (WorkflowTemplateInfo) HibernateUtil.getFirst(hql, Long.parseLong(id));
            if (wf == null)
                continue;
            
            PerplexityService perplexityService = HibernateUtil.get(PerplexityService.class, Long.parseLong(id));
            if (perplexityService != null)
            {
                if (names.length() > 0)
                    names.append("\n");
                
                names.append(perplexityService.getName() + ": " + wf.getName());
            }
        }
        
        JSONObject ob = new JSONObject();
        ob.put("canRemove", names.length() == 0);
        ob.put("names", names.toString());
        
        response.setContentType("text/xml;charset=utf-8");
        PrintWriter out = response.getWriter();  
        out.print(ob.toString());  
        out.flush();  
        pageReturn();
    }

    @ActionHandler(action = AttributeConstant.REMOVE, formClass = "")
    public void remove(HttpServletRequest request, HttpServletResponse response, Object form)
            throws Exception
    {
        String[] ids = request.getParameterValues("selectPerplexityIds");
        for (String id : ids)
        {
            delete(id);
        }
    }

    /**
     * Delete PerplexityService with id.
     * 
     * @param id
     */
    private void delete(String id)
    {
        Assert.assertNotEmpty(id, "id");

        long attId = Long.parseLong(id);
        PerplexityService perplexityService = HibernateUtil.get(PerplexityService.class, attId);
        if (perplexityService != null)
        {
            try
            {
                HibernateUtil.delete(perplexityService);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void afterAction(HttpServletRequest request, HttpServletResponse response)
    {
        dataForTable(request);
    }

    @Override
    public void beforeAction(HttpServletRequest request, HttpServletResponse response)
    {
        perplexitys = null;
        clearSessionExceptTableInfo(request.getSession(false), "perplexityKey");

    }
}
