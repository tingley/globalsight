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

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DirectoryFilter implements Filter
{
	private final String FOLDER_EXPORTS = "_Exports_";
	
	private final String FOLDER_IMPORTS = "_Imports_";
	
	private final String FOLDER_PROJECTS = "_Projects_";
	
	private final String ERRORTUVS_FILE_SUFFIX = "-errorTuvs.html";
	
	private final String LOG_FILE_SUFFIX = "-log.html";
	
    public void destroy()
    {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        try
        {
        	HttpServletRequest req = (HttpServletRequest) request;
        	HttpServletResponse res = (HttpServletResponse) response;
        	String uri = req.getRequestURI();
        	// gbs-1389: restrict direct access to folders that are not allowed
        	String contextPath = req.getContextPath();
        	File file = new File(req.getRealPath(""));
			if (file.isDirectory()) 
			{
				String[] files = file.list();
				for (int i = 0; i < files.length; i++) 
				{
					if (uri.contains(contextPath + "/" + FOLDER_EXPORTS)
							|| (uri
									.contains(contextPath + "/"
											+ FOLDER_IMPORTS)
									&& !uri.endsWith(ERRORTUVS_FILE_SUFFIX) && !uri
									.endsWith(LOG_FILE_SUFFIX))
							|| uri
									.contains(contextPath + "/"
											+ FOLDER_PROJECTS)
							|| (uri.contains(contextPath + "/" + files[i]) && (new File(
									req.getRealPath(uri
											.replace(contextPath, "")))
									.isDirectory()))) 
					{
						res.sendRedirect(req.getContextPath());
						break;
					}
				}

			}
            chain.doFilter(request, response);
        }
        finally
        {
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }
}
