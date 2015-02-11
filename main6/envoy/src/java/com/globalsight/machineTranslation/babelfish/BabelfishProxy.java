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
package com.globalsight.machineTranslation.babelfish;

import org.apache.log4j.Logger;

import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.AbstractTranslator;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Acts as a proxy to the Babelfish Machine Translation Service.
 */
public class BabelfishProxy 
	extends AbstractTranslator
	implements MachineTranslator
{
    //
	// Private Data 
	//
    private static final Logger CATEGORY =
		Logger.getLogger(
			BabelfishProxy.class);

    private static final String ENGINE_NAME = "Babelfish";

    public static final String URL_BABELFISH = 
		"http://babelfish.altavista.com/babelfish/tr";

    private static final Pattern BABELFISH_RESULT = Pattern.compile(
        "<!--\\s+Target\\s+text[^>]*?>" +
        "(.*?)" +
        "<!--\\s+end:\\s+Target\\s+text[^>]*?>",
		Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private static final Pattern BABELFISH_RESULT2 = Pattern.compile(
        // Pick out the second row and delete tags with EditUtil.
        ".*?<tr>.*?</tr>.*?<tr>(.*?)</tr>.*",
		Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * Hash Set of all supported language pairs
     */
    private static final HashSet s_supportedLanguagePairs;

    // insert all supported language pairs into the hash set
    static
    {
        s_supportedLanguagePairs = new HashSet(50);
        s_supportedLanguagePairs.add("en_de");
        s_supportedLanguagePairs.add("en_el");
        s_supportedLanguagePairs.add("en_es");
        s_supportedLanguagePairs.add("en_fr");
        s_supportedLanguagePairs.add("en_it");
        s_supportedLanguagePairs.add("en_ja");
        s_supportedLanguagePairs.add("en_ko");
        s_supportedLanguagePairs.add("en_nl");
        s_supportedLanguagePairs.add("en_pt");
        s_supportedLanguagePairs.add("en_ru");
        s_supportedLanguagePairs.add("en_zh");
        s_supportedLanguagePairs.add("en_zt");

        s_supportedLanguagePairs.add("de_en");
        s_supportedLanguagePairs.add("el_en");
        s_supportedLanguagePairs.add("es_en");
        s_supportedLanguagePairs.add("fr_en");
        s_supportedLanguagePairs.add("it_en");
        s_supportedLanguagePairs.add("ja_en");
        s_supportedLanguagePairs.add("ko_en");
        s_supportedLanguagePairs.add("nl_en");
        s_supportedLanguagePairs.add("pt_en");
        s_supportedLanguagePairs.add("ru_en");
        s_supportedLanguagePairs.add("zh_en");
        s_supportedLanguagePairs.add("zt_en");

        s_supportedLanguagePairs.add("fr_de");
        s_supportedLanguagePairs.add("fr_el");
        s_supportedLanguagePairs.add("fr_es");
        s_supportedLanguagePairs.add("fr_it");
        s_supportedLanguagePairs.add("fr_nl");
        s_supportedLanguagePairs.add("fr_pt");

        s_supportedLanguagePairs.add("de_fr");
        s_supportedLanguagePairs.add("el_fr");
        s_supportedLanguagePairs.add("es_fr");
        s_supportedLanguagePairs.add("it_fr");
        s_supportedLanguagePairs.add("nl_fr");
        s_supportedLanguagePairs.add("pt_fr");
    }

	//
	// Constructor
	//

    public BabelfishProxy() 
		throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_NAME;
    }

    /**
     * Returns true if the given locale pair is supported for MT.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale,
		Locale p_targetLocale)
		throws MachineTranslationException
    {
        StringBuffer langPair = new StringBuffer();

		langPair.append(mapLanguage(p_sourceLocale));
        langPair.append("_");
        langPair.append(mapLanguage(p_targetLocale));

        return s_supportedLanguagePairs.contains(langPair.toString());
    }

    /**
     * Actually does the real work of communicating with the MT engine.
     */
    protected String doTranslation(Locale p_sourceLocale,
		Locale p_targetLocale, String p_string)
		throws MachineTranslationException
    {
        try
        {
            URL url = new URL(URL_BABELFISH);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            // Switch to POST
            conn.setDoOutput(true);

            // get something we can write to that will send ISO codes
            OutputStreamWriter out =
                new OutputStreamWriter(conn.getOutputStream());

            // build the POST data (query string)
            // use URLEncoder to do URL encoding
            StringBuffer postdata = new StringBuffer();
            postdata.append("doit=done");
            postdata.append("&tt=urltext");
            postdata.append("&intl=1");
            postdata.append("&urltext=");
            // Posting a Unicode string works like this: convert the
            // string to UTF-8 and then url-encode it.
            postdata.append(URLEncoder.encode(p_string, "UTF-8"));
            postdata.append("&lp=" + mapLanguage(p_sourceLocale) + "_" +
                mapLanguage(p_targetLocale));

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("query is: " + postdata);
            }

            // send the POST data, the connection handles all the other stuff
            out.write(postdata.toString());
            out.close();

            // get the response and fetch the translation
            BufferedReader r = new BufferedReader(
                new InputStreamReader (conn.getInputStream()));

            StringBuffer buf = new StringBuffer();
            String s;
            while ((s = r.readLine()) != null)
            {
                buf.append(s);
				buf.append("\n"); 
            }

            String result = getResult(buf);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("result: " + EditUtil.toJavascript(result));
            }

            return result != null ? result : p_string;
        }
        catch (Throwable t)
        {
            CATEGORY.debug("error: ", t);

            return p_string;
        }
    }

    // <!-- Target text (content) -->
    // <table width=400 cellpadding=0 cellspacing=0 border=0>
    // <tr><form action="http://www.altavista.com/web/results" method=get>
    // <td valign=top>
    // <b class=m><font color=#0000000>Auf deutsch:</font></b></td>
    // </tr>
    // <tr> <td bgcolor=white class=s><div style=padding:10px;>
    // Ein R?uber ist im Holz.</div></td> </tr>
    // <tr> <td class=s> <input type="hidden" name="kls" value="0">
    // <input type="hidden" name="ienc" value="utf8"><br>
    // <input type=submit name=search value="Search the web with this text">
    // </td>
    // </tr></form>
    // </table>
    // <!-- end: Target text (content) -->

    static private String getResult(StringBuffer p_html)
    {
        Matcher re = BABELFISH_RESULT.matcher(p_html);

        if (re.find())
        {
            String res = p_html.substring(re.start(1), re.end(1));

            Matcher re2 = BABELFISH_RESULT2.matcher(res);
            if (re2.find())
            {
                res = res.substring(re2.start(1), re2.end(1));

                res = EditUtil.stripTags(res);
                res = EditUtil.decodeXmlEntities(res);
                res = res.trim();

                // Babelfish results are in UTF-8
                // p_html = EditUtil.utf8ToUnicode(p_html);

                return res;
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("result not found in\n" + res);
            }

            return null;
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("result not found in\n" + p_html);
        }

        return null;
    }

	static public void main (String argv[])
		throws Exception
	{
		MachineTranslator mt = new BabelfishProxy();
		String s = "Hello World";

		if (argv.length == 1)
		{
			s = argv[0];
		}

		String res = mt.translate(Locale.US, Locale.GERMAN, s);
		System.err.println(res);
	}
}
