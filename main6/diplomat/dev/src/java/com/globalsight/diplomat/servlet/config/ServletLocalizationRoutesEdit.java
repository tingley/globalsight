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
import java.util.Enumeration;
import java.util.HashMap;

import com.globalsight.diplomat.servlet.config.GSA_UserAdapter;
import com.globalsight.diplomat.servlet.config.GSA_User;
import com.globalsight.diplomat.servlet.config.ServletLocalizationRoutes;
import com.globalsight.diplomat.servlet.config.LocalizationProfile;
import com.globalsight.diplomat.servlet.config.LocalizationProfileRouting;
import com.globalsight.diplomat.util.StringBundle;

public class ServletLocalizationRoutesEdit extends ServletLocalizationRoutes
{
    protected final String PROFILE = "profile";
    protected final String NEXT_PAGE = "../servlet/ServletLocalizationSaveEdit";

	/////////////////////////////////////////////////
	protected String generateLanguageTable(String p_longName, int p_languageID,
	    int p_sourceLanguage, HttpSession p_session)
	{
	    // retrieve the resource strings
	    String locale = (String)p_session.getAttribute("uilang");

	    StringBundle rscBundle = StringBundle.getInstance();
	    rscBundle.setBundle("config", locale);
	    HashMap strings = rscBundle.getBundle("config", locale);

	    // find the target language
	    Vector routingSet = null;
	    LocalizationProfile profile = (LocalizationProfile) p_session.getAttribute(PROFILE);
	    Vector targetList = profile.getLanguageList();
	    for (int i=0; i<targetList.size(); ++i)
	    {
	        LocalizationProfileTarget currentLanguage = (LocalizationProfileTarget)targetList.elementAt(i);
	        if (currentLanguage.getLanguage() == p_languageID)
	        {
	            routingSet = currentLanguage.getRoutingList();
	            break;
	        }
	    }

	    Vector stages = (Vector)p_session.getAttribute(STAGE_LIST);
	    GSA_UserAdapter adapter = GSA_UserAdapter.getInstance();
	    Vector users = adapter.retrieveUsers(p_sourceLanguage, p_languageID);

	    String stageHTML = "";

	    stageHTML += "<TABLE width=500>\n";
	    stageHTML += "<TR><TD COLSPAN=4 ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>" + p_longName;
	    stageHTML += "</B></FONT></TD></TR>\n";
	    stageHTML += "<TR cellpadding=0><TD>&nbsp</TD></TR>";
	    stageHTML += "<TR cellpadding=0><TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>" + (String)strings.get(STAGE) + "</B></FONT></TD>\n";
	    stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>" + (String)strings.get(ORDER) + "</B></FONT></TD>\n";
	    stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>" + (String)strings.get(USER) + "</B></FONT></TD>\n";
	    stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>" + (String)strings.get(DURATION) + "</B></FONT></TD></TR>\n";

	    for (int i=0; i<stages.size(); ++i)
	    {

        stageHTML += "<TR><TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>" + (String)stages.elementAt(i) + "</B></FONT></TD>\n";
		    stageHTML += "<TD><SELECT NAME='lang" + p_languageID + "_" + (i+1) + "'>\n";

		    LocalizationProfileRouting routing = null;

		    if (routingSet != null)
		    {
		        // find the right stage
		        for (int k=0; k<stages.size(); ++k)
		        {
		            LocalizationProfileRouting currentRouting = (LocalizationProfileRouting)routingSet.elementAt(k);
		            if (currentRouting.getStage() == (i+1)) {
		                routing = currentRouting;
		                break;
		            }
		        }
            }
		    if ( (routing != null) && (0 == routing.getSequence()) )
		        stageHTML += "<OPTION SELECTED VALUE=0>Skip\n";
		    else
		        stageHTML += "<OPTION VALUE=0>Skip\n";


		    // calculate the stages
		    for (int j=0; j<stages.size(); ++j)
		    {
		        int stage = j+1;

		        if ( ((j == i) && (routing == null)) ||
		            ( (routing != null) && (routing.getSequence() == stage) ) )
		                stageHTML += "<OPTION SELECTED VALUE=" + stage + ">" + stage + "\n";
		        else
		            stageHTML += "<OPTION VALUE=" + stage + ">" + stage + "\n";
		    }   // end stage number loop

		    stageHTML += "</SELECT></TD>\n";

		    // calculate the users
		    stageHTML += "<TD><SELECT NAME='user" + p_languageID + "_" + (i+1) + "'>\n";
		    stageHTML += "<OPTION VALUE=0>"+(String)strings.get(CHOOSE_USER)+"\n";
		    for (int j=0; j<users.size(); ++j)
		    {
		        int userCode = ((GSA_User)users.elementAt(j)).getCode();
		        if ( (routing != null) && (routing.getUser() == userCode) )
		            stageHTML += "<OPTION SELECTED VALUE=";
		        else
		            stageHTML += "<OPTION VALUE=";
		        stageHTML += userCode + ">" + ((GSA_User)users.elementAt(j)).getName() + "\n";
		    }
		    stageHTML += "</SELECT></TD>\n";

		    // add duration
		    if (routing != null)
		        stageHTML += "<TD><INPUT TYPE=Text SIZE=3 VALUE=" + routing.getDuration() +" NAME='duration" +
		            p_languageID + "_" + (i+1) + "'>" + (String)strings.get(DAYS) + "</TD>\n";
		    else
		        stageHTML += "<TD><INPUT TYPE=Text SIZE=3 VALUE=0 NAME='duration" + p_languageID + "_" +
		            (i+1) + "'>" + (String)strings.get(DAYS) + "</TD>\n";
		    stageHTML += "</TR>\n";

	    }   // end stages for loop

	    stageHTML += "</TABLE>\n";

	    return stageHTML;
	}

	/////////////////////////////////////////////////
	protected String translationMemory (int p_languageID, HttpSession p_session)
	{
	    // retrieve the resource strings
	    String locale = (String)p_session.getAttribute("uilang");

	    StringBundle rscBundle = StringBundle.getInstance();
	    rscBundle.setBundle("config", locale);
	    HashMap strings = rscBundle.getBundle("config", locale);

	    // retrieve the data
	    LocalizationProfile profile = (LocalizationProfile) p_session.getAttribute(PROFILE);
	    Vector targetLanguages = (Vector)profile.getLanguageList();

	    boolean pageTM = false;
            int tmMatchStyle = 0;
	    int leverageLang = 0;

	    for (int i=0; i<targetLanguages.size(); ++i)
	    {
	        LocalizationProfileTarget target = (LocalizationProfileTarget)targetLanguages.elementAt(i);
	        // find the right target language
	        if (target.getLanguage() == p_languageID)
	        {
	            leverageLang = target.getLanguageTM();
	            pageTM = target.getPageTM();
                    tmMatchStyle = target.getTM_MatchStyle();
	            break;
	        }
	    }

	    Vector langAbbreviation = (Vector)p_session.getAttribute(LANGUAGE_LIST);

	    String parentLanguage = "";

	    // find the abbreviated language, i.e., en_US
	    for (int i=0; i<langAbbreviation.size(); ++i)
	    {
	        UserLanguages abbreviation = (UserLanguages)
	            langAbbreviation.elementAt(i);

	        if (abbreviation.getID() == p_languageID)
	        {
	            parentLanguage = abbreviation.getAbbreviation();
	            // separate the parent language, i.e., we want "en" - discard "_US"
	            parentLanguage = parentLanguage.substring(0, parentLanguage.indexOf("_"));
	            break;
	        }
	    }

	    // find the leverage languages
	    String options = "";
	    Vector leverageLangIds = new Vector();
	    for (int i=0; i<langAbbreviation.size(); ++i)
	    {
	        UserLanguages abbreviation = (UserLanguages)
	            langAbbreviation.elementAt(i);

	        if (abbreviation.getAbbreviation().startsWith(parentLanguage))
	        {
	            int id = abbreviation.getID();
	            String longName = abbreviation.getLongName();
	            // build the options string
	            String selected = "";
	            if (id == leverageLang)
	                selected = "SELECTED";
	            options += "<OPTION " + selected + " VALUE=" + id + ">" + longName + "\n";
	        }
        }

	    // build the radio button selection list

	    String tm = "<TR><TD><TABLE WIDTH=500><TR><TD>\n";

            String isDisabled = (leverageLang > 0) ? " " : " disabled=true ";

            //yuck. reference a javascript function on the JSP
	    if (leverageLang > 0)
	        tm += "<INPUT TYPE=Checkbox NAME='tm" + p_languageID + "' CHECKED onClick=\"javascript:disableTmMatch(this,tmLang" + p_languageID + ",tmMatch" + p_languageID + ")\" VALUE=\"1\">" + (String)strings.get(TM_LEVERAGE_FROM);
	    else
	        tm += "<INPUT TYPE=Checkbox NAME='tm" + p_languageID +
                   "' onClick=\"javascript:disableTmMatch(this,tmLang" + p_languageID + ",tmMatch" + p_languageID +")\" VALUE=\"1\">"
                   + (String)strings.get(TM_LEVERAGE_FROM);
	    tm += "&nbsp;&nbsp;&nbsp;&nbsp; <SELECT NAME='tmLang" + p_languageID + "'" + isDisabled + ">";
	    tm += options;
	    tm += "</SELECT>\n";
	    tm += "</TD></TR>\n";

            //now add in the Match Style radio buttons
            tm += "<TR><TD>\n";
            String isChecked = (tmMatchStyle == 0) ? "CHECKED" : "";
            tm += "<INPUT TYPE=RADIO NAME='tmMatch" + p_languageID + "' " + isChecked + isDisabled + " VALUE=0>" +
               (String)strings.get(EXACT_MATCH) + "</TD></TR>\n";
            tm += "<TR><TD>\n";
            isChecked = (tmMatchStyle == 1) ? "CHECKED" : "";
            tm += "<INPUT TYPE=RADIO NAME='tmMatch" + p_languageID + "' " + isChecked + isDisabled +
               " VALUE=1>" + (String)strings.get(EXACT_MATCHTO) + "</TD></TR>\n";
            
	    if (pageTM)
	        tm += "<TR><TD><INPUT TYPE=CHECKBOX NAME=pageTM" + p_languageID + " CHECKED>" + (String)strings.get(PAGE_TM);
      else
          tm += "<TR><TD><INPUT TYPE=CHECKBOX NAME=pageTM" + p_languageID + ">" + (String)strings.get(PAGE_TM);
	    tm += "</TD></TR></TABLE></TD></TR>\n";

	    return tm;
	}

	/////////////////////////////////////////////////
	protected String nextPage() { return NEXT_PAGE; }

}
