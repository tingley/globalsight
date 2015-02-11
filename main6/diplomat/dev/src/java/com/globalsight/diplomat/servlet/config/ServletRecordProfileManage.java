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

import com.globalsight.diplomat.servlet.config.DJ_RecordProfile;
import com.globalsight.diplomat.servlet.config.DJ_ConnectionProfile;
import com.globalsight.diplomat.servlet.config.DJ_URL_List;
import com.globalsight.diplomat.servlet.config.DJ_KnownFormat;
import com.globalsight.diplomat.util.Logger;

public class ServletRecordProfileManage extends HttpServlet 
{    		
    private final String DISPLAY_PAGE = "/jsp/RecordProfileManage.jsp";
    private final String DELETE = "Delete";
    private final String RECORD_NAME = "recordName";
    private final String PROFILE_OBJECTS = "profileObjects";
    private final String CONNECTION_OBJECTS = "connectionObjects";
    private final String URL_OBJECTS = "urlObjects";
    private final String RULE_OBJECTS = "ruleObjects";
    private final String DATA_TYPES = "dataTypes";

 public ServletRecordProfileManage() throws ServletException
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
	    // Did the user clicked on the delete button?
	    if (p_request.getParameter(DELETE) != null)
	    {
            long id = (new Long(p_request.getParameter(RECORD_NAME))).longValue();
            if (id > 0)
            {
                DJ_RecordProfile profile = new DJ_RecordProfile(id);
                profile.deleteEntry();
            }
	    }
	   
	    // writes lists of objects in the session
        retrieveObjects(p_request);

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
	private void retrieveObjects(HttpServletRequest p_request)
	{
	    // retrieve the names
	    Vector profileObjects = DJ_RecordProfile.retrieveRecordProfiles();
	    Vector connectionObjects = DJ_ConnectionProfile.retrieveConnectionProfiles();
	    Vector urlObjects = DJ_URL_List.retrieveURLProfiles();
	    Vector ruleObjects = DJ_XML_Rule.retrieveXMLProfiles();
	    Vector typeObjects = DJ_KnownFormat.retrieveFileProfilesByName();
	    
	    // write to the session
	    HttpSession session = p_request.getSession(true);
	    session.setAttribute(PROFILE_OBJECTS, profileObjects);
	    session.setAttribute(CONNECTION_OBJECTS, connectionObjects);
	    session.setAttribute(URL_OBJECTS, urlObjects);
	    session.setAttribute(RULE_OBJECTS, ruleObjects);
	    session.setAttribute(DATA_TYPES, typeObjects);
	}

    protected Logger theLogger = Logger.getLogger();
}
