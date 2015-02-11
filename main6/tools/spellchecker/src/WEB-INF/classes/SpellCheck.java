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

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import spell.*;

/**
 * Spell Checker Servlet. Handles all spell-checking-related requests.
 *
 * @param locale locale that the text and dictionary is in.
 * @param uilocale locale to use for UI messages.
 *
 * @param dict dictionaries to use for spell checking
 * @param customdict user-specific dictionary to add words to
 *
 * @param wordtoadd word to add to custom dictionary
 *
 * @param wordtosuggest misspelled Word to get suggestions for
 * @param texttocheck text to check, only returns misspelled words, no
 * suggestions
 *
 */
public class SpellCheck
    extends HttpServlet
{
    private String m_basePath = "/globalsight/spellchecker/jsp/";

    private String m_noMisspellingMessage = "No mis-spellings found";

    /*
    public void init(ServletConfig conf)
        throws ServletException
    {
        super.init(conf);

        String temp;

        if ((temp = conf.getInitParameter("basePath")) != null)
        {
            if (!temp.equals(""))
            {
                m_basePath = temp;

                if (!m_basePath.endsWith("/"))
                {
                    m_basePath += "/";
                }

                System.out.println("User Specified basePath: " + m_basePath);
            }
        }
    }
    */

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        doGet(req, resp);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        resp.setContentType("text/html;charset=UTF-8");

        String localeString = req.getParameter("locale");
        String uilocaleString = req.getParameter("uilocale");
        String dict = req.getParameter("dict");
        String customDict = req.getParameter("customDict");
        String wordToAdd = req.getParameter("wordtoadd");
        String wordtosuggest = req.getParameter("wordtosuggest");
        String texttocheck = req.getParameter("texttocheck");

        customDict = EditUtil.unescape(customDict);

        // If this is a quick call to add a word to the custom dictionary,
        // add it and bail out.
        if (wordToAdd != null)
        {
            addword(customDict, EditUtil.unescape(wordToAdd));
            return;
        }

        // Build list of dictionaries to check
        ArrayList dictionarytoUse = new ArrayList();
        dictionarytoUse.add(dict);

        if (customDict != null && !customDict.equals(""))
        {
            dictionarytoUse.add(customDict);
        }

        // Find out what to check, a text or single word.
        if (texttocheck != null)
        {
            wordtosuggest = texttocheck;
        }

        wordtosuggest = EditUtil.unescape(wordtosuggest);

        Locale locale = makeLocale(localeString);
        Locale uilocale = makeLocale(uilocaleString);

        // run the spell checker
        SpellChecker spl = new SpellChecker(dictionarytoUse, locale);
        SpellCheckResult res = spl.doSpell(wordtosuggest);

        // evaluate results
        StringBuffer mispelledWords = new StringBuffer();
        StringBuffer suggestionList = new StringBuffer();
        String firstWord = "";
        boolean haveFirstWord = false;

        // Enumerate words in the text
        ArrayList words = res.getWords();
        for (int i = 0, max = words.size(); i < max; i++)
        {
            Word nextWord = (Word)words.get(i);

            if (nextWord == null)
            {
                continue;
            }

            if (!nextWord.isCorrect())
            {
                if (!haveFirstWord)
                {
                    firstWord = nextWord.getText();
                    haveFirstWord = true;
                }

                mispelledWords.append(nextWord.getText() + "|XDE|");

                // Should we collect suggestions for a single word?
                if (texttocheck == null)
                {
                    int idx = 0;
                    ArrayList suggestions = nextWord.getSuggestions();
                    for (int j = 0, jmax = suggestions.size(); j < jmax; j++)
                    {
                        String suggestion = (String)suggestions.get(j);

                        if (idx == 0)
                        {
                            wordtosuggest = suggestion;
                        }
                        idx++;

                        suggestionList.append(suggestion + ",,");
                    }
                }
            }
        }

        // Display suggestions for a single word.
        if (texttocheck == null)
        {
            displaySuggestions(resp, suggestionList.toString(), wordtosuggest);
        }
        else
        {
            displayMispellings(resp, mispelledWords.toString(), firstWord);
        }
    }


    public void displaySuggestions(HttpServletResponse response,
        String p_suggestionList, String p_wordtosuggest)
    {
        try
        {
            PrintWriter out = response.getWriter();

            out.println("<html><head>");
            out.println("<script src=\"" + m_basePath +
                "spellcheck.js\"></script>");
            out.println("</head>");
            out.println("<body onload=\"Checkaword();\">");
            out.println("<script>");
            out.println("var form = parent.mainframe.document.spellcheckform;");

            out.println("function appendOption(Vu,Valu){");
            out.println(" var x,y;");
            out.println(" y=form.suggestionlist;");
            out.println(" x=y.outerHTML;x=x.substring(0,x.length-9);");
            out.println(" x+='<option'+((Valu)?' value='+Valu:'')+'>'+((Vu)?Vu:'');");
            out.println(" y.outerHTML=x+'</option></select>'");
            out.println("}");

            out.println("form.suggestionlist.options.length=0;");
            out.println("var values='" +
                EditUtil.toJavascript(p_suggestionList) + "'.split(',,');");
            out.println("var icount=0;");

            out.println("if(isMSIE&&lBrowserVer<=5.0&&navigator.userAgent.indexOf(\"Win\")>-1){");
            out.println("for (var i in values) {");
            out.println(" if(values[i]!=''){");
            out.println("  appendOption(values[i],values[i]);");
            out.println("  icount++;");
            out.println(" }");
            out.println("}");
            out.println("}else{");
            out.println(" for (var i in values) {");
            out.println("  if(values[i]!=''){");
            out.println("   form.suggestionlist.options[icount] = new Option();");
            out.println("   form.suggestionlist.options[icount].text = values[i];");
            out.println("   icount++;");
            out.println("  }");
            out.println(" }");
            out.println("}");
            out.println("if (icount > 0) {");
            out.println(" form.suggestionlist.options.selectedIndex = 0;");
            out.println(" form.tchangeto.value = form.suggestionlist.options[0].text;");
            out.println("} else {");
            out.println(" form.tchangeto.value = '" +
                EditUtil.toJavascript(p_wordtosuggest) + "';");
            out.println("}");
            out.println("form.tchangeto.focus();");
            out.println("function Checkaword(){");
            out.println(" scRefreshText();");
            out.println("}");
            out.println("</script>");
            out.println("</body></html>");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void displayMispellings(HttpServletResponse response,
        String p_mispelledWords, String p_firstWord)
    {
        try
        {
            PrintWriter out = response.getWriter();

            out.println("<html><head>");

            out.println("<script src=\"" + m_basePath +
                "spellcheck.js\"></script>");
            out.println("</head>");
            out.println("<body onload=\"Checkaword();\">");
            out.println("//Populate Arrays and display initial text.");
            out.println("<script>");

            if (p_mispelledWords.length() > 0)
            {
                out.println("//Get our first suggestion.");
                out.println("function Checkaword(){");
                out.println("if (typeof(parent.mainframe.mainFrameLoaded)=='undefined' ||");
                out.println("!parent.mainframe.mainFrameLoaded ||");
                out.println("typeof(parent.mainframe.document.spellcheckform) == 'undefined' ||");
                out.println("typeof(parent.mainframe.document.spellcheckform.MispelledWords) == 'undefined')");
                out.println("{");
                out.println(" setTimeout('Checkaword()',300);");
                out.println(" return;");
                out.println("}");

                out.println("parent.mainframe.document.spellcheckform.MispelledWords.value='" +
                    EditUtil.toJavascript(p_mispelledWords) + "';");
                out.println("scCheckWord('" +
                    EditUtil.toJavascript(p_firstWord) + "');");
                out.println("}");
            }
            else
            {
                out.println("function Checkaword(){}");
                out.println("alert('" + m_noMisspellingMessage + "');");
                out.println("scEnableSpellCheck();");
                out.println("parent.close();");
            }
            out.println("</script>");
            out.println("</body></html>");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void addword(String p_customDict, String p_wordtoAdd)
        throws IOException
    {
        SpellIndex index = SpellIndexList.getSpellIndex(p_customDict);
        index.addWord(p_wordtoAdd);
    }

    private Locale makeLocale(String p_locale)
    {
        if (p_locale.length() == 5)
        {
            return new Locale(p_locale.substring(0, 2),
                p_locale.substring(3, 5));
        }
        else
        {
            return new Locale(p_locale.substring(0, 2));
        }
    }
}

// Local Variables:
// compile-command: "javac -classpath '.;D:/Apps/Weblogic/weblogic81/server/lib/weblogic.jar' SpellCheck.java"
// End:
