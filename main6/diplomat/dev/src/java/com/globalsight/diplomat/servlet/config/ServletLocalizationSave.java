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
import java.util.TreeSet;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Vector;

import com.globalsight.diplomat.servlet.config.LocalizationProfile;
import com.globalsight.diplomat.servlet.config.LocalizationProfileRouting;
import com.globalsight.diplomat.servlet.config.LocalizationProfileTarget;
import com.globalsight.diplomat.util.Logger;

public class ServletLocalizationSave extends ServletLocalization 
{
    protected final String DISPLAY_PAGE = "/jsp/saveWindow.jsp";
    protected final String LANG = "lang";
	protected final String USER = "user";
	protected final String DURATION = "duration";
	protected final String PAGE_TM = "pageTM";
	protected final String TMLANG = "tmLang";
   	protected final String TMMATCH = "tmMatch";
	protected final String TM = "tm";
	protected final String PROFILE = "profile";
	protected final String NAME = "name";
	protected final String SOURCE = "source";
	protected final String SOURCE_CHARACTER_SET = "sourceCharacterSet";
	protected final String CHARACTER_SET = "characterSet";
	
	/////////////////////////////////////////////////
	protected String preparePage(HttpServletRequest p_request, HttpServletResponse p_response)
	{
           theLogger.println(Logger.DEBUG_D,"ServletLocalizationSave: preparePage()");
		HttpSession session = p_request.getSession(false);
		if (session != null)
		{
		    // Retrieve the stages so we know how many stages in the system
		    Vector stages = (Vector)session.getAttribute(STAGE_LIST);

		    String name = (String)session.getAttribute(NAME);
		    int sourceLanguage = (new Integer((String)session.getAttribute(SOURCE))).intValue();
		    int sourceCharacterSet = Integer.parseInt((String)session.getAttribute(SOURCE_CHARACTER_SET));

		    Enumeration keys = p_request.getParameterNames();

		    TreeSet allStages = new TreeSet();
		    TreeSet allUsers = new TreeSet();
		    TreeSet allDurations = new TreeSet();

                    theLogger.println(Logger.DEBUG_D,"ServletLocalizationSave: cycling through keys");
		    while (keys.hasMoreElements())
		    {
		        String key = (String) keys.nextElement();
			theLogger.println(Logger.DEBUG_D,"ServletLocalizationSave: key is "
					  + key);
		        if (key.startsWith(LANG))
		            allStages.add(key);
		        else if (key.startsWith(USER))
		            allUsers.add(key);
		        else if (key.startsWith(DURATION))
		            allDurations.add(key);
		    }

		    // The number of different languages is the list of all stages
		    // divided by the number of stages
		    int differentLanguages = allStages.size()/stages.size();
		    theLogger.println(Logger.DEBUG_D, "ServletLocalizationSave: allStages "
				      + allStages.size());
		    theLogger.println(Logger.DEBUG_D, "ServletLocalizationSave: stage size "
				      + stages.size());
		    theLogger.println(Logger.DEBUG_D, "ServletLocalizationSave: differentLanguages "
				      + differentLanguages);
		    Vector routes = new Vector();
		    
            Iterator iteratorStage = allStages.iterator();
            Iterator iteratorUser = allUsers.iterator();
            Iterator iteratorDuration = allDurations.iterator();
            
            int[] languageID = new int[differentLanguages];
            // create a list of routes per language
            for (int i=0; i<differentLanguages; ++i)
            {
                Vector route = new Vector();
                for (int j=0; j<stages.size(); ++j)
                {
                    String currentStage = (String)iteratorStage.next();
                    String currentUser = (String)iteratorUser.next();
                    String currentDuration = (String)iteratorDuration.next();
                    if (0 == j) // determine the language id
                    {
                        // we know the format is lang3_1
                        // 3 is the language id and 1 is the stage
                        String id = currentStage.substring(LANG.length());
                        // we removed lang and have the 3_1 left
                        int index = id.indexOf("_");
                        id = id.substring(0, index);
                        languageID[i] = (new Integer(id)).intValue();
                    }
                    route.add(new LocalizationProfileRouting(j+1,
                        (new Integer(p_request.getParameter(currentStage))).intValue(),
                        (new Integer(p_request.getParameter(currentUser))).intValue(),
                        (new Integer(p_request.getParameter(currentDuration))).intValue() ));
                } // end stage loop
                
                // add route to our list of routes - Vectors of Vectors
                routes.add(route);
                
            } // end language loop

            // Create the target language profiles
            Vector targetLanguages = new Vector();
            for (int i=0; i<differentLanguages; ++i)
            {
                boolean pageTM = false;
                String pageString = p_request.getParameter(PAGE_TM + languageID[i]);
                int tmMatchStyle = 0;
                int tmLang = 0;
                int tm=0;
                try {
                   tm = Integer.parseInt(p_request.getParameter(TM + languageID[i]));
                   tmLang =Integer.parseInt(p_request.getParameter(TMLANG + languageID[i]));
                   tmMatchStyle = Integer.parseInt(p_request.getParameter(TMMATCH + languageID[i]));
                }
                catch (Exception e) {
                   tm = 0;
                   tmLang = 0;
                   tmMatchStyle = 0;
                }
                
                if (pageString != null)
                    pageTM = true;
                    
                // calculate the character set
                String key = CHARACTER_SET + languageID[i];
                int characterSet = ((Integer) session.getAttribute(key)).intValue();
                    
                targetLanguages.add( new LocalizationProfileTarget(languageID[i], 
                    tmLang, pageTM, tmMatchStyle, characterSet, (Vector)routes.elementAt(i)) );
            }
	    theLogger.println(Logger.DEBUG_D, "There are " + targetLanguages.size()
			      + " target langs.");

            // Create a new profile
            LocalizationProfile profile = new LocalizationProfile(name, sourceLanguage, sourceCharacterSet, targetLanguages);
            // Set the id to the previous profile so we can replace the old with the new
            // zero value indicates new profile only
            profile.setID(previousID(p_request));
	    theLogger.println(Logger.DEBUG_D, "Created l10n profile " + profile.getID());
            
            try {
                profile.update();
            }
            catch (NoSuchFieldException e) {
		theLogger.printStackTrace(Logger.ERROR,"Localization Profile no such field exception", e);
            }
	    } // end if session
	    return DISPLAY_PAGE;    
	}
	
	/////////////////////////////////////////////////
	protected long previousID(HttpServletRequest p_request) { return 0; }	
}
