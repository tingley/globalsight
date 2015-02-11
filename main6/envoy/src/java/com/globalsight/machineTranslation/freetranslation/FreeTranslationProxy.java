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
package com.globalsight.machineTranslation.freetranslation;

import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.AbstractTranslator;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.ling.common.Text;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Acts as a proxy to the Freetranslation Machine Translation Service.
 */
public class FreeTranslationProxy
    extends AbstractTranslator
    implements MachineTranslator
{
    //
    // Private Data
    //
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            FreeTranslationProxy.class);

    private static final String ENGINE_NAME = "Freetranslation";

    public static final String URL_FREETRANSLATION =
        "http://ets.freetranslation.com";
    public static final String URL_FREETRANSLATION_ZH =
        "http://ets6.freetranslation.com";

    private static final Pattern FREETRANSLATION_RESULT = Pattern.compile(
        "<textarea name=\"dsttext\" [^>]*?>(.*?)</textarea>",
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
        s_supportedLanguagePairs.add("en_es");
        s_supportedLanguagePairs.add("en_fr");
        s_supportedLanguagePairs.add("en_it");
        s_supportedLanguagePairs.add("en_nl");
        s_supportedLanguagePairs.add("en_no");
        s_supportedLanguagePairs.add("en_pt");
        s_supportedLanguagePairs.add("en_ru");
        s_supportedLanguagePairs.add("en_zh");
        s_supportedLanguagePairs.add("en_zt");

        s_supportedLanguagePairs.add("de_en");
        s_supportedLanguagePairs.add("es_en");
        s_supportedLanguagePairs.add("fr_en");
        s_supportedLanguagePairs.add("it_en");
        s_supportedLanguagePairs.add("nl_en");
        s_supportedLanguagePairs.add("pt_en");
        s_supportedLanguagePairs.add("ru_en");
    }

    //
    // Constructor
    //

    public FreeTranslationProxy()
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
            // http://ets.freetranslation.com for Western languages,
            // ets6 for chinese target lang.
            URL url;

            if (p_targetLocale.getLanguage().equals("zh"))
            {
                url = new URL(URL_FREETRANSLATION_ZH);
            }
            else
            {
                url = new URL(URL_FREETRANSLATION);
            }

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            // Switch to POST
            conn.setDoOutput(true);

            // get something we can write to that will send ISO codes
            OutputStreamWriter out =
                new OutputStreamWriter(conn.getOutputStream());

            // build the POST data (query string)
            // use URLEncoder to do URL encoding
            StringBuffer postdata = new StringBuffer();
            postdata.append("sequence=core");
            postdata.append("&mode=html");
            postdata.append("&template=results_en-us.htm");
            // Posting a Unicode string works like this: convert the
            // string to UTF-8 and then url-encode it.
            postdata.append("&srctext=" + URLEncoder.encode(p_string, "UTF-8"));
            postdata.append("&language=" +
                p_sourceLocale.getDisplayLanguage(Locale.US) + "/" +
                p_targetLocale.getDisplayLanguage(Locale.US));

            // send the POST data, the connection handles all the other stuff
            out.write(postdata.toString());
            out.close();

            // Encodings are way off: windows-1252 for western
            // languages, and US-ASCII for Chinese.
            //System.err.println("Encoding: " + conn.getContentEncoding());
            //System.err.println("ContentType: " + conn.getContentType());

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

    static private String getResult(StringBuffer p_html)
    {
        // Encoding is not *in* the result, and the headers contain
        // phony values: windows-1252 for en->de, US-ASCII for en->zh.

        Matcher re = FREETRANSLATION_RESULT.matcher(p_html);

        if (re.find())
        {
            String res = p_html.substring(re.start(1), re.end(1));

            /* Is this right? No, it's not.
            try
            {
                res = new String(res.getBytes("ISO8859_1"), "Cp1252");
            }
            catch (Throwable ignore)
            {
            }
            */

            return res;
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
        MachineTranslator mt = new FreeTranslationProxy();
        String s = "Hello World";

        if (argv.length == 1)
        {
            s = argv[0];
        }

        String res = mt.translate(Locale.US, Locale.GERMAN, s);
        System.err.println(res);
    }
}
