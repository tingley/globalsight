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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Vector;

import com.globalsight.diplomat.servlet.config.ServletLocalization;
import com.globalsight.diplomat.servlet.config.LocalizationProfile;

public class ServletLocalizationLanguageEdit extends ServletLocalizationLanguage
{
	protected final String NEXT_PAGE = "/diplomat/servlet/ServletLocalizationRoutesEdit";
	protected final String TEMPLATE_NAME = "templateName";
	protected final String PROFILE = "profile";
				
	/////////////////////////////////////////////////
	protected String preparePage (HttpServletRequest p_request,
        HttpServletResponse p_response)
	{
	    String idString = p_request.getParameter(TEMPLATE_NAME);
	    long id = (new Long(idString)).longValue();
	    LocalizationProfile profile = new LocalizationProfile(id);
	    
	    HttpSession session = p_request.getSession(false);
	    Vector languages = (Vector)session.getAttribute(LANGUAGE_LIST);
	    
	    //String names = retrieveNames();
		String source = sourceLanguages(languages, profile);
		String target = targetLanguages(languages, profile);
				
		session.setAttribute(PROFILE, profile);	
		//session.setAttribute("title", TITLE);
		//session.setAttribute("names", names);
		session.setAttribute("currentName", profile.getName());
		session.setAttribute("sourceLanguages", source);
		session.setAttribute("targetLanguages", target);
		session.setAttribute("action", NEXT_PAGE);
		
		return DISPLAY_PAGE;
	}			
				
	/////////////////////////////////////////////////
	// List of source languages using html option tags
	// NOTE: need to sort the long names
	protected String sourceLanguages(Vector p_languages, LocalizationProfile p_profile)
	{
	    String html = "";
	    for (int i=0; i<p_languages.size(); ++i)
	    {
	        GSA_Language language = (GSA_Language) p_languages.elementAt(i);
	        int languageID = language.getID();
	        
	        if (languageID == p_profile.getSourceLanguage())
	            html += "<OPTION SELECTED VALUE='" + languageID + "'>" + language.getLanguage() +"\n";
	        else
	            html += "<OPTION VALUE='" + languageID + "'>" + language.getLanguage() +"\n";
	    }
	    return html;
	}
	
	/////////////////////////////////////////////////
	// List of target languages using html check boxes
	// NOTE: need to sort the long names
	protected String targetLanguages(Vector p_languages, LocalizationProfile p_profile)
	{
	    Vector selectedLanguages = p_profile.getLanguageList();
	    
	    String html = "";
	    for (int i=0; i<p_languages.size(); ++i)
	    {
	        GSA_Language language = (GSA_Language) p_languages.elementAt(i);
	        int languageID = language.getID();
	        
	        boolean found = false;
	        
	        // determine if language is previously selected
	        for (int j=0; j<selectedLanguages.size(); ++j)
	        {
	            LocalizationProfileTarget target = (LocalizationProfileTarget)selectedLanguages.elementAt(j);
	            if (languageID == target.getLanguage())
	            {
	                found = true;
	                break;
	            }
	        }
	        
	        if (found)
	            html += "<INPUT CHECKED TYPE=CHECKBOX NAME='target";
	        else
	            html += "<INPUT TYPE=CHECKBOX NAME='target";
	        html += language.getID() + "' VALUE='" + language.getID() + "'> &nbsp&nbsp " + language.getLanguage() +
	            "\n<BR>\n";
	    }
	    return html;
	}
}