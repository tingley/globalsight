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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.diplomat.util.StringBundle;

public class ServletLocalizationRoutes extends ServletLocalization
{
    protected final String STAGE = "localization.stage";
    protected final String ORDER = "localization.order";
    protected final String NEXT_PAGE = "../servlet/ServletLocalizationSave";
    protected final String DISPLAY_PAGE = "/jsp/LocalizationRoutes.jsp";
    protected final String TM_LEVERAGE_FROM = "localization.tmLeverageFrom";
    protected final String EXACT_MATCH = "localization.exactMatch";
    protected final String EXACT_MATCHTO = "localization.exactMatchTO";
    protected final String PAGE_TM = "localization.pageTm";
    protected final String NAME = "name";
    protected final String SOURCE = "source";
    protected final String SOURCE_CHARACTER_SET = "sourceCharacterSet";
    protected final String CHARACTER_SET = "characterSet";
    protected final String TARGET = "target";
    protected final String USER = "localization.user";
    protected final String DURATION = "localization.duration";
    protected final String DAYS = "localization.days";
    protected final String CHOOSE_USER = "localization.chooseUser";
    protected final int COLUMN = 1; // number of language tables per row

    /////////////////////////////////////////////////
    protected String generateLanguageTable(String p_longName, int p_languageID,
            int p_sourceLanguage, HttpSession p_session)
    {
        // retrieve the resource strings
        String locale = (String) p_session.getAttribute("uilang");

        StringBundle rscBundle = StringBundle.getInstance();
        rscBundle.setBundle("config", locale);
        HashMap strings = rscBundle.getBundle("config", locale);

        Vector stages = (Vector) p_session.getAttribute(STAGE_LIST);
        GSA_UserAdapter adapter = GSA_UserAdapter.getInstance();
        Vector users = adapter.retrieveUsers(p_sourceLanguage, p_languageID);

        String stageHTML = "";

        stageHTML += "<TABLE width=500>\n";
        stageHTML += "<TR><TD COLSPAN=4 ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>"
                + p_longName;
        stageHTML += "</B></FONT></TD></TR>\n";
        stageHTML += "<TR cellpadding=0><TD>&nbsp;</TD></TR>";
        stageHTML += "<TR cellpadding=0>\n";
        stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>"
                + (String) strings.get(STAGE) + "</B></FONT></TD>\n";
        stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>"
                + (String) strings.get(ORDER) + "</B></FONT></TD>\n";
        stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>"
                + (String) strings.get(USER) + "</B></FONT></TD>\n";
        stageHTML += "<TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>"
                + (String) strings.get(DURATION) + "</B></FONT></TD></TR>\n";

        for (int i = 0; i < stages.size(); ++i)
        {
            stageHTML += "<TR><TD ALIGN=Center BGCOLOR=White><FONT face=arial size=-1><B>"
                    + (String) stages.elementAt(i) + "</B></FONT></TD>\n";
            stageHTML += "<TD><SELECT NAME='lang" + p_languageID + "_"
                    + (i + 1) + "'>\n";
            stageHTML += "<OPTION VALUE=0>Skip\n";

            // calculate the stages
            for (int j = 0; j < stages.size(); ++j)
            {
                int stage = j + 1;
                if (j == i)
                    stageHTML += "<OPTION SELECTED VALUE=" + stage + ">"
                            + stage + "\n";
                else
                    stageHTML += "<OPTION VALUE=" + stage + ">" + stage + "\n";
            } // end stage number loop

            stageHTML += "</SELECT></TD>\n";

            // calculate the users
            stageHTML += "<TD><SELECT NAME='user" + p_languageID + "_"
                    + (i + 1) + "'>\n";
            stageHTML += "<OPTION VALUE=0>" + (String) strings.get(CHOOSE_USER)
                    + "\n";
            for (int j = 0; j < users.size(); ++j)
            {
                stageHTML += "<OPTION VALUE="
                        + ((GSA_User) users.elementAt(j)).getCode() + ">"
                        + ((GSA_User) users.elementAt(j)).getName() + "\n";
            }
            stageHTML += "</SELECT></TD>\n";

            // add duration
            stageHTML += "<TD><INPUT TYPE=Text SIZE=3 VALUE=1 NAME='duration"
                    + p_languageID + "_" + (i + 1) + "'>"
                    + (String) strings.get(DAYS) + "</TD>\n";

            stageHTML += "</TR>\n";
        } // end stages for loop

        stageHTML += "</TABLE>\n";
        return stageHTML;
    }

    /////////////////////////////////////////////////
    protected String translationMemory(int p_languageID, HttpSession p_session)
    {
        // retrieve the resource strings
        String locale = (String) p_session.getAttribute("uilang");

        StringBundle rscBundle = StringBundle.getInstance();
        rscBundle.setBundle("config", locale);
        HashMap strings = rscBundle.getBundle("config", locale);

        Vector langAbbreviation = (Vector) p_session
                .getAttribute(LANGUAGE_LIST);

        String parentLanguage = "";

        // find the abbreviated language, i.e., en_US
        for (int i = 0; i < langAbbreviation.size(); ++i)
        {
            UserLanguages abbreviation = (UserLanguages) langAbbreviation
                    .elementAt(i);

            if (abbreviation.getID() == p_languageID)
            {
                parentLanguage = abbreviation.getAbbreviation();
                // separate the parent language, i.e., we want "en" - discard "_US"
                parentLanguage = parentLanguage.substring(0, parentLanguage
                        .indexOf("_"));
                break;
            }
        }

        // find the leverage languages
        String options = "";
        int numOptions = 0;
        Vector leverageLangIds = new Vector();
        for (int i = 0; i < langAbbreviation.size(); ++i)
        {
            UserLanguages abbreviation = (UserLanguages) langAbbreviation
                    .elementAt(i);

            if (abbreviation.getAbbreviation().startsWith(parentLanguage))
            {
                int id = abbreviation.getID();
                String longName = abbreviation.getLongName();
                // build the options string
                if (options.equals(""))
                    options += "<OPTION SELECTED VALUE=" + id + ">" + longName
                            + "\n";
                else
                    options += "<OPTION VALUE=" + id + ">" + longName + "\n";
                numOptions++;
            }
        }

        // build the radio button selection list
        String tm = "<TR><TD><TABLE WIDTH=500><TR><TD>\n";
        String isChecked = (numOptions > 0) ? " CHECKED" : "";
        String isDisabled = (numOptions > 0) ? " " : " disabled=true ";

        tm += "<INPUT TYPE=Checkbox NAME='tm" + p_languageID + "' " + isChecked
                + " onClick=\"javascript:disableTmMatch(this,tmLang"
                + p_languageID + ",tmMatch" + p_languageID + ")\" VALUE=\"1\">"
                + (String) strings.get(TM_LEVERAGE_FROM);
        tm += "&nbsp;&nbsp;&nbsp;&nbsp; <SELECT NAME='tmLang" + p_languageID
                + "'" + isDisabled + ">";
        tm += options;
        tm += "</SELECT>\n";
        tm += "</TD></TR>\n";

        //now add in the Match Style radio buttons
        tm += "<TR><TD>\n";
        tm += "<INPUT TYPE=RADIO NAME='tmMatch" + p_languageID + "' VALUE=0 "
                + isDisabled + ">" + (String) strings.get(EXACT_MATCH)
                + "</TD></TR>\n";
        tm += "<TR><TD>\n";
        tm += "<INPUT TYPE=RADIO NAME='tmMatch" + p_languageID
                + "' CHECKED VALUE=1 " + isDisabled + ">"
                + (String) strings.get(EXACT_MATCHTO) + "</TD></TR>\n";
        tm += "<TR><TD><INPUT TYPE=CHECKBOX NAME=pageTM" + p_languageID
                + " CHECKED>" + (String) strings.get(PAGE_TM);
        tm += "</TD></TR></TABLE></TD></TR>\n";
        return tm;
    }

    /////////////////////////////////////////////////
    protected String preparePage(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        int sourceLanguage = Integer.parseInt(p_request.getParameter(SOURCE));
        // save previous page data
        HttpSession session = p_request.getSession(false);
        String name = p_request.getParameter(NAME);
        String source = p_request.getParameter(SOURCE);
        String encoding = p_request.getParameter(SOURCE_CHARACTER_SET);

        session.setAttribute(NAME, name);
        session.setAttribute(SOURCE, source);
        session.setAttribute(SOURCE_CHARACTER_SET, encoding);

        // retrieve the list of languages
        Vector languages = (Vector) session.getAttribute(LANGUAGE_LIST);

        // retrieve selected target languages and character sets
        Vector targetLanguages = new Vector();
        Enumeration keys = p_request.getParameterNames();
        while (keys.hasMoreElements())
        {
            String element = (String) keys.nextElement();
            if (element.startsWith(TARGET))
            {
                // this is a target language
                Integer id = new Integer(element.substring(TARGET.length()));
                for (int i = 0; i < languages.size(); ++i)
                {
                    if (((UserLanguages) languages.elementAt(i)).getID() == id
                            .intValue())
                        targetLanguages.add(languages.elementAt(i));
                }
            }
            else if (element.startsWith(CHARACTER_SET))
            {
                // save the character set
                session.setAttribute(element, new Integer(p_request
                        .getParameter(element)));
            }
        }

        String stageHTML = "<TABLE width=\"500\">\n";
        for (int i = 0; i < targetLanguages.size(); ++i)
        {
            String longName = ((UserLanguages) targetLanguages.elementAt(i))
                    .getLongName();
            int languageID = ((UserLanguages) targetLanguages.elementAt(i))
                    .getID();
            stageHTML += "<TR><TD WIDTH=205>\n";
            stageHTML += generateLanguageTable(longName, languageID,
                    sourceLanguage, session);
            stageHTML += "\n</TD></TR>\n<TR><TD>";
            stageHTML += translationMemory(languageID, session);
            stageHTML += "\n</TD></TR>\n";
        }

        stageHTML += "</TABLE>\n";

        // save the stage numbers for our javascript
        // i.e., new Array (1, 2, 3, 4, 5)
        Vector stageList = (Vector) session.getAttribute(STAGE_LIST);

        String stages = "";
        for (int i = 0; i < stageList.size(); ++i)
        {
            if (i != 0)
                stages += "," + (i + 1);
            else
                stages += i + 1;
        }

        //save our language ids for our javascript verification
        // i.e., new Array ('2','4','5')
        String languageScript = "";
        for (int i = 0; i < targetLanguages.size(); ++i)
        {
            if (0 == i)
                languageScript += "'"
                        + ((UserLanguages) targetLanguages.elementAt(i))
                                .getID() + "'";
            else
                languageScript += ", '"
                        + ((UserLanguages) targetLanguages.elementAt(i))
                                .getID() + "'";
        }

        // add additional data to our session
        session.setAttribute("languageTables", stageHTML);
        session.setAttribute("stages", stages);
        session.setAttribute("languages", languageScript);
        session.setAttribute("action", p_response.encodeURL(nextPage()));

        return DISPLAY_PAGE;
    }

    /////////////////////////////////////////////////
    protected String nextPage()
    {
        return NEXT_PAGE;
    }
}
