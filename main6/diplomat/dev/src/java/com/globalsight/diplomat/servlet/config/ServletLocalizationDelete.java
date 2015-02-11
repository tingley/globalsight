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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.globalsight.diplomat.servlet.config.LocalizationProfile;

public class ServletLocalizationDelete extends ServletLocalization 
{
    protected final String TEMPLATE_NAME = "templateName";
		
	/////////////////////////////////////////////////
	protected String preparePage(HttpServletRequest p_request,
        HttpServletResponse p_response)
	{
	    
	    String idString = p_request.getParameter(TEMPLATE_NAME);	    
	    long id = (new Long(idString)).longValue();  

	    LocalizationProfile profile = new LocalizationProfile(id);
	    profile.deleteEntry();
	    profile = null;
	    
	    return super.preparePage(p_request, p_response);
	}
}