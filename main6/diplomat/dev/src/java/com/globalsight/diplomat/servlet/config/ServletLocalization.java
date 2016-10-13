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
import java.util.Enumeration;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Vector;

import com.globalsight.diplomat.servlet.config.LocalizationProfile;
import com.globalsight.diplomat.servlet.config.GSA_UserAdapter;
import com.globalsight.diplomat.util.Logger;

public class ServletLocalization extends HttpServlet 
{
    protected final String DISPLAY_PAGE = "/jsp/LocalizationManage.jsp";
	protected final String JOB_TEMPLATE_ID = "JOB_TEMPLATE_ID";
	protected final String LANGUAGE_LIST = "languageList";
	protected final String STAGE_LIST = "stageList";
	protected final String PROFILE_OBJECTS = "profileObjects";
	protected final String MODIFY_PROFILE = "modifyProfile";
	protected final String UILANG = "uilang";
	protected final String CHARACTER_SET_LIST = "characterSetList";

 public ServletLocalization()
    {
	try {
	    theLogger.setLogname("DiplomatServlets");
	}catch (IOException e) {}
    }
			
	/////////////////////////////////////////////////
	public void doGet(HttpServletRequest p_request,
      HttpServletResponse p_response)
      throws ServletException, IOException 
	{
	    String displayPage = "";
	    HttpSession session = p_request.getSession(true);
	    
	    // If the session timed out, display the first page
	    if (session.isNew())
	        displayPage = initialPage(p_request, p_response);
	    else
	        displayPage = preparePage(p_request, p_response);

		RequestDispatcher requestDispatcher = 
			getServletContext().getRequestDispatcher(displayPage);
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
	protected String initialPage(HttpServletRequest p_request,
        HttpServletResponse p_response)
	{
	    return preparePage(p_request, p_response);
	}
	
	/////////////////////////////////////////////////
	protected String preparePage(HttpServletRequest p_request,
        HttpServletResponse p_response)
	{
	    HttpSession session = p_request.getSession(true);
	    
	    Vector profiles = LocalizationProfile.retrieveLocalizationProfile();
	    session.setAttribute( PROFILE_OBJECTS, profiles);
	    
	    // add objects that are common for this session
	    GSA_UserAdapter adapter = GSA_UserAdapter.getInstance();
	    Vector languages = adapter.retrieveLanguages((String)session.getAttribute(UILANG));
	    Vector stages = adapter.retrieveStages();
	    Vector characterSets = adapter.retrieveCharacterSets();
	    session.setAttribute(LANGUAGE_LIST, languages);
	    session.setAttribute(STAGE_LIST, stages);
	    session.setAttribute(CHARACTER_SET_LIST, characterSets);
	    
	    return DISPLAY_PAGE;    
	}
    protected Logger theLogger = Logger.getLogger();
}
