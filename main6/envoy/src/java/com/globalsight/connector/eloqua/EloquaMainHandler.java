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
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.form.EloquaConnectFilter;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.EloquaConnectorComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

/**
 * XmldtdFilePageHandler, A page handler to produce the entry page (index.jsp)
 * for XmldtdFile management.
 */
public class EloquaMainHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(EloquaMainHandler.class);
    
    private List<?> allConns = null;

    @ActionHandler(action = "save", formClass = "com.globalsight.cxe.entity.eloqua.EloquaConnector", loadFromDb = true)
    public void save(HttpServletRequest request, HttpServletResponse response,
            Object form) throws Exception
    {
        logger.debug("Saving eloqua connector...");
        EloquaConnector connector = (EloquaConnector) form;
        HibernateUtil.saveOrUpdate(connector);
        logger.debug("Saving eloqua connector finished.");
    }
    
    @ActionHandler(action = "remove", formClass = "")
    public void remove(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        String[] ids = request.getParameterValues("selectEloquaConnectorIds");
        for (String id : ids)
        {
            long cId = Long.parseLong(id);
            EloquaConnector c = HibernateUtil.get(EloquaConnector.class, cId);
            HibernateUtil.delete(c);
        }
    }
    
    @ActionHandler(action = "filter", formClass = "com.globalsight.connector.eloqua.form.EloquaConnectFilter")
    public void filter(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
        allConns = EloquaManager.getAllConnector();
        EloquaConnectFilter filter = (EloquaConnectFilter) form;
        allConns = filter.filter((List<EloquaConnector>) allConns);

        request.setAttribute("nameFilter", filter.getNameFilter());
        request.setAttribute("companyFilter", filter.getCompanyFilter());
        request.setAttribute("companyNameFilter", filter.getCompanyNameFilter());
        request.setAttribute("urlFilter", filter.getUrlFilter());
        request.setAttribute("descriptionFilter", filter.getDescriptionFilter());
    }

    @ActionHandler(action = "test", formClass = "")
    public void test(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
    {
    	ResourceBundle bundle = PageHandler.getBundle(request.getSession());
    	ServletOutputStream out = response.getOutputStream();

    	JSONObject ob = new JSONObject();
		try 
		{
			String id = request.getParameter("id");
	    	if (id == null)
	    	{
	            ob.put("canUse", false);
	            ob.put("error", bundle.getString("error_eloqua_connector"));
	    		return;
	    	}
	    	
	    	EloquaConnector conn = HibernateUtil.get(EloquaConnector.class, Long.parseLong(id));
	    	if (conn == null)
	    	{
	    	    ob.put("canUse", false);
                ob.put("error", bundle.getString("error_eloqua_connector"));
	    		return;
	    	}
	    	
	    	EloquaHelper helper = new EloquaHelper(conn);
	    	boolean result = helper.doTest();
			if (result)
			{
				HttpSession session = request.getSession();
				SessionManager sessionManager = (SessionManager) session
						.getAttribute(WebAppConstants.SESSION_MANAGER);
				sessionManager.setAttribute("EloquaConnector", conn);
				ob.put("canUse", true);
                ob.put("error", bundle.getString("error_eloqua_connector"));
			}
			else
			{
			    ob.put("canUse", false);
                ob.put("error", bundle.getString("error_eloqua_connector"));
			}
		} 
		catch (Exception e) 
		{
		    logger.error(e);
		    ob.put("canUse", false);
            ob.put("error", bundle.getString("error_eloqua_connector"));
		} 
		finally 
		{
		    out.write(ob.toString().getBytes("UTF-8"));
			out.close();
			pageReturn();
		}
    }

    /**
     * Get list of all rules.
     */
    private void dataForTable(HttpServletRequest request)
            throws GeneralException
    {
        HttpSession session = request.getSession(false);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        if (allConns == null)
        {
            allConns = EloquaManager.getAllConnector();
        }
        
        Integer orgSize = (Integer) session.getAttribute("eloquaConnPageSize");
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
            
            session.setAttribute("eloquaConnPageSize", size);
        }
        
        setTableNavigation(request, session, allConns
                , new EloquaConnectorComparator(uiLocale),
                size, "eloquaConnectList", "eloquaConnectKey");
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        dataForTable(request);
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        allConns = null;
        clearSessionExceptTableInfo(request.getSession(false),
        		"eloquaConnectKey");

    }
}
