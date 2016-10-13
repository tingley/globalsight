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

import com.globalsight.diplomat.servlet.config.DJ_XML_Rule;
import com.globalsight.diplomat.util.Logger;

public class ServletXMLProfileManage extends HttpServlet 
{    		
    private final String MANAGE_PAGE = "/jsp/XMLProfileManage.jsp";
    private final String MODIFY_PAGE = "/servlet/ServletXMLProfile";
    private final String DELETE = "Delete";
    private final String MODIFY = "Modify";
    private final String XML_NAME = "xmlName";
    private final String PROFILE_OBJECTS = "profileObjects";
    private final String DEPENDENCY = "dependency";

    public ServletXMLProfileManage() throws ServletException
    {
	try {
	    theLogger.setLogname("DiplomatServlets");
	}catch (IOException e) {
	    throw new ServletException(e);
	}
    }
        
	/////////////////////////////////////////////////
	public void doGet(HttpServletRequest p_request,
      HttpServletResponse p_response)
      throws ServletException, IOException 
	{
	    String nextPage = MANAGE_PAGE;
	    
	    HttpSession session = p_request.getSession(true);
	    // remove the status from the session
        session.removeAttribute(DEPENDENCY);
        
        // Did the user clicked on the delete button?
	    if (p_request.getParameter(DELETE) != null)
	    {
            long id = (new Long(p_request.getParameter(XML_NAME))).longValue();
            if ((id > 0) && (!hasDependency(p_request)))
            {
                DJ_XML_Rule profile = new DJ_XML_Rule(id);
                profile.deleteEntry();
            }
	    }
	    else if (p_request.getParameter(MODIFY) != null)
	    {
	        long id = (new Long(p_request.getParameter(XML_NAME))).longValue();
                if ((id > 0) /* && (!hasDependency(p_request))*/ )
                   nextPage = MODIFY_PAGE;
	    }
	   
	    // write the list of names in javascript
	    //	    theLogger.println(Logger.DEBUG_D, "ServletXmlProfileManage: getting names");
        retrieveNames(p_request);
	//	theLogger.println(Logger.DEBUG_D, "ServletXmlProfileManage: got names");
		RequestDispatcher requestDispatcher = 
			getServletContext().getRequestDispatcher(nextPage);
		//		theLogger.println(Logger.DEBUG_D, "ServletXmlProfileManage: forwarding to page " + nextPage);
		if (requestDispatcher != null)
			requestDispatcher.forward(p_request, p_response);
		else {
		    theLogger.println(Logger.ERROR, "ServletXmlProfileManage: the request displatcher is null. The CXE webserver must be restarted.");
		}
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
	    //	    theLogger.println(Logger.DEBUG_D, "ServletXmlProfileManage: in retriveNames");
	    Vector profiles = DJ_XML_Rule.retrieveXMLProfiles();
	    //	    theLogger.println(Logger.DEBUG_D, "ServletXmlProfileManage: got profiles");
	    // write to the session
	    HttpSession session = p_request.getSession(true);
	    session.setAttribute(PROFILE_OBJECTS, profiles);
	}
	
	/////////////////////////////////////////////////
	private boolean hasDependency(HttpServletRequest p_request)
	{
	    long id = (new Long(p_request.getParameter(XML_NAME))).longValue();
	    boolean dependent = true;
	    HttpSession session = p_request.getSession(false);
        DJ_XML_Rule profile = new DJ_XML_Rule(id);
        Dependency dependency = profile.checkDependencies();
        if (dependency == null) 
            dependent = false;
        else 
            session.setAttribute(DEPENDENCY, dependency);
        
        return dependent;
	}

    private Logger theLogger = Logger.getLogger();
}
