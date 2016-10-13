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

package com.globalsight.everest.webapp.pagehandler;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.StringUtil;

public abstract class PageActionHandler extends PageHandler
{
    static private final Logger logger = Logger
            .getLogger(PageActionHandler.class);
    private ThreadLocal<Boolean> isReturn = new ThreadLocal<Boolean>();
    private HttpServletRequest request = null;

    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page descriptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        this.request = p_request;
        isReturn.set(null);

        beforeAction(p_request, p_response);
        if (isReturn.get() != null)
        {
            return;
        }

        callAction(p_request, p_response);
        // gbs-1389: forward restricted access to initial page
        Object isRestrictedAccess = p_request.getAttribute("restricted_access");
        if (isRestrictedAccess != null
                && ((Boolean) isRestrictedAccess).booleanValue() == true)
        {
            return;
        }

        if (isReturn.get() != null)
        {
            return;
        }

        afterAction(p_request, p_response);
        if (isReturn.get() != null)
        {
            return;
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
    
    /**
     * Gets attribute from request, session manager and session.
     * 
     * @param key
     * @return
     */
    protected Object get(String key)
    {
        Object ob = this.request.getAttribute(key);
        if (ob != null)
            return ob;
        
        ob = this.request.getParameterValues(key);
        if (ob != null)
            return ob;
        
        HttpSession session = request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        
        ob = sessionManager.getAttribute(key);
        if (ob != null)
            return ob;
        
        ob = session.getAttribute(key);
        if (ob != null)
            return ob;
        
        return null;
    }

    protected void pageReturn()
    {
        isReturn.set(Boolean.TRUE);
    }

    private void callAction(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        String action = p_request.getParameter("action");
        if (action == null)
        {
            return;
        }

        Method[] ms = this.getClass().getMethods();
        for (Method m : ms)
        {
            if (m.isAnnotationPresent(ActionHandler.class))
            {
                ActionHandler handler = m.getAnnotation(ActionHandler.class);
                if (action.matches(handler.action()))
                {
                    String form = handler.formClass();
                    Object actionForm = null;
                    if (form != null && form.trim().length() > 0)
                    {
                        actionForm = loadForm(p_request, form,
                                handler.loadFromDb());
                    }

                    String formToken = handler.formToken();
                    if (formToken != null && formToken.trim().length() > 0)
                    {
                        boolean notDuplicate = FormUtil
                                .isNotDuplicateSubmisson(p_request, formToken);
                        if (!notDuplicate)
                        {
                            return;
                        }
                    }

                    try
                    {
                        m.invoke(this, p_request, p_response, actionForm);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                    }

                    break;
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Object loadForm(HttpServletRequest request, String classPath,
            boolean loadFromDb)
    {
        if (classPath == null)
        {
            return null;
        }

        classPath = classPath.trim();
        if (classPath.length() == 0)
        {
            return null;
        }

        Class clazz = null;
        Object object = null;
        try
        {
            clazz = this.getClass().getClassLoader().loadClass(classPath);

            if (loadFromDb)
            {
                String id = request.getParameter("id");
                if (id != null)
                {
                    id = id.trim();
                    long obId = Long.parseLong(id);
                    if (obId > 0)
                    {
                        object = HibernateUtil.get(clazz, obId);
                    }
                }
            }

            if (object == null)
            {
                object = clazz.newInstance();
            }
        }
        catch (Exception e1)
        {
            logger.error(e1.getMessage(), e1);
            return null;
        }

        Map<String, String[]> parameters = request.getParameterMap();
        Method[] ms = clazz.getMethods();

        for (Method m : ms)
        {
            String name = m.getName();
            Class[] parameterTypes = m.getParameterTypes();

            if ("setCompanyId".equals(m.getName()))
            {
                String id = CompanyThreadLocal.getInstance().getValue();
                try
                {
                    m.invoke(object, Long.parseLong(id));
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            else if (name.startsWith("set") && name.length() > 4
                    && parameterTypes.length == 1)
            {
                String key = Character.toLowerCase(name.charAt(3))
                        + name.substring(4);
                String[] value = parameters.get(key);

				if (value != null && StringUtil.isNotEmpty(value[0]))
				{
				    PropertyEditor editor = getEdit(parameterTypes[0]);
                    if (editor != null)
                    {
                        editor.setAsText(getAsString(value));
                        try
                        {
                            m.invoke(object, editor.getValue());
                        }
                        catch (Exception e)
                        {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }

        return object;
    }

    private String getAsString(String[] object)
    {
        StringBuilder result = new StringBuilder();
        for (String s : object)
        {
            if (result.length() > 0)
            {
                result.append(",");
            }
            result.append(s);
        }

        return result.toString();
    }

    private PropertyEditor getEdit(Class<?> clazz)
    {
        return PropertyEditorManager.findEditor(clazz);
    }

    protected boolean getCheckBoxParameter(HttpServletRequest request,
            String name)
    {
        return "true".equals(request.getParameter(name));
    }

    public abstract void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException;

    public abstract void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException;

    public HttpServletRequest getRequest()
    {
        return request;
    }

    public void setRequest(HttpServletRequest request)
    {
        this.request = request;
    }
}
