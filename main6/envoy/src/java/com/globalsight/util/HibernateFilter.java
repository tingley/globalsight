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

package com.globalsight.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class HibernateFilter implements Filter
{

    public void destroy()
    {
    }

    /**
     * Closes Hibernate Session after the request.
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        // Hack to log entry and return of JSPs. This could be in its own
        // filter. Only log JSPs because other activities we care about have
        // their own entry points.
        ActivityLog.Start activityStart = null;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String servletPath = httpRequest.getServletPath();
        if (servletPath.endsWith(".jsp"))
        {
            Map<Object, Object> activityArgs = new HashMap<Object, Object>();
            activityArgs.put("jsp", servletPath);
            HttpSession session = httpRequest.getSession(false);
            activityArgs.put(
                    "user",
                    session == null ? null : UserUtil
                            .getUserNameById((String) session
                                    .getAttribute(WebAppConstants.USER_NAME)));
            activityStart = ActivityLog.start(HibernateFilter.class,
                    "doFilter", activityArgs);
        }
        try
        {
            chain.doFilter(request, response);
        }
        catch (IllegalStateException e)
        {
            // TODO: handle exception
        }
        finally
        {
            HibernateUtil.closeSession();
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }

    public void init(FilterConfig arg0) throws ServletException
    {
    }
}
