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

package com.globalsight.diplomat.servlet.config;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;

import java.util.Vector;

import com.globalsight.diplomat.servlet.config.DJ_TaskQueue;
import com.globalsight.diplomat.util.Logger;

public class ServletTaskProfileManage extends HttpServlet 
{    		
    private final String DISPLAY_PAGE = "/jsp/TaskProfileManage.jsp";
    private final String DELETE = "Delete";
    private final String TASK_ID = "taskID";
    private final String PROFILE_OBJECTS = "profileObjects";
        
	/////////////////////////////////////////////////
	public void doGet(HttpServletRequest p_request,
      HttpServletResponse p_response)
      throws ServletException, IOException 
	{
	    // Did the user clicked on the delete button?
	    if (p_request.getParameter(DELETE) != null)
	    {
            long id = (new Long(p_request.getParameter(TASK_ID))).longValue();
            if (id > 0)
            {
                DJ_TaskQueue profile = new DJ_TaskQueue(id);
                profile.deleteEntry();
            }
	    }
	   	    
        retrieveNames(p_request);

		RequestDispatcher requestDispatcher = 
			getServletContext().getRequestDispatcher(DISPLAY_PAGE);
		if (requestDispatcher != null)
			requestDispatcher.forward(p_request, p_response);
  	}
  	
  	/////////////////////////////////////////////////
	public void doPost(HttpServletRequest p_request,
    	HttpServletResponse p_response)
    	throws ServletException, IOException 
	{
		doGet(p_request, p_response);
	}
	
	/////////////////////////////////////////////////
	private void retrieveNames(HttpServletRequest p_request)
	{
	    // retrieve the names
	    Vector profiles = DJ_TaskQueue.retrieveTaskProfiles();
	    
	    // write to the session
	    HttpSession session = p_request.getSession(true);
	    session.setAttribute(PROFILE_OBJECTS, profiles);
	}
    private Logger theLogger = Logger.getLogger();
}
