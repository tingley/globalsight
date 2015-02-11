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

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Vector;

import com.globalsight.diplomat.servlet.config.ServletLocalization;

public class ServletLocalizationLanguage extends ServletLocalization
{
	protected final String NEXT_ROUTE = "/servlet/ServletLocalizationRoutes";
	protected final String NEXT_ROUTE_EDIT = "/servlet/ServletLocalizationRoutesEdit";
	protected final String DISPLAY_PAGE = "/jsp/LocalizationTargetLanguage.jsp";
	protected final String TEMPLATE_NAME = "templateName";
	protected final String PROFILE = "profile";
	protected final String CURRENT_NAME = "currentName";
	protected final String STATUS = "status";
	protected final String NEW = "New";
  protected final String COPY = "Copy";
	protected final String MODIFY = "Modify";

	/////////////////////////////////////////////////
	protected String preparePage (HttpServletRequest p_request,
        HttpServletResponse p_response)
	{
	    HttpSession session = p_request.getSession(false);

	    if (p_request.getParameter(MODIFY) != null)
	    {
	        session.setAttribute(MODIFY_PROFILE, new Boolean(true));

	        String idString = p_request.getParameter(TEMPLATE_NAME);
	        long id = (new Long(idString)).longValue();
	        LocalizationProfile profile = new LocalizationProfile(id);
	        session.setAttribute(CURRENT_NAME, profile.getName());
	        session.setAttribute(PROFILE, profile);
	        session.setAttribute("action", p_response.encodeURL(NEXT_ROUTE_EDIT) );
      }
      else if (p_request.getParameter(COPY) != null)
	    {
	        session.setAttribute(MODIFY_PROFILE, new Boolean(true));

	        String idString = p_request.getParameter(TEMPLATE_NAME);
	        long id = (new Long(idString)).longValue();
	        LocalizationProfile profile = new LocalizationProfile(id);
	        // clear the name and the id
	        profile.setID(0);
	        profile.setName(new String(""));
	        session.setAttribute(CURRENT_NAME, profile.getName());
	        session.setAttribute(PROFILE, profile);
	        session.setAttribute("action", p_response.encodeURL(NEXT_ROUTE_EDIT) );
	        session.setAttribute(STATUS, COPY);
        }
	    else
	    {
	        session.removeAttribute(PROFILE);
	        session.setAttribute(MODIFY_PROFILE, new Boolean(false));
	        session.setAttribute(CURRENT_NAME, new String(""));
	        session.setAttribute("action", p_response.encodeURL(NEXT_ROUTE));
	        session.setAttribute(STATUS, NEW);
	    }

		return DISPLAY_PAGE;
	}
}