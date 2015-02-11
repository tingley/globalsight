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
package com.globalsight.machineTranslation.systran;

import org.apache.log4j.Logger;

import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.AbstractTranslator;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.XmlParser;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import com.globalsight.ling.common.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Acts as a proxy to the Systran machine translation system.
 */
public class SystranProxy
    extends AbstractTranslator
    implements MachineTranslator
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SystranProxy.class);

    private static final String ENGINE_NAME = "Systran";

    //Systrans API values
    private String m_id; //systran ID value (mesa group id)
    private String m_url; //URL to use to talk to systran

    /**
     * Hash Set of all supported language pairs
     */
    private static final HashSet s_supportedLanguagePairs;

    // insert all supported language pairs into the hash set
    // Supported language pairs are: fr_de, it_en, en_nl, pt_fr, el_en, es_fr,
    // en_pt, de_fr, en_el, en_ru, zt_en, en_zh, fr_nl, en_es, nl_fr, en_ko, en_it, pt_
    // en, fr_pt, en_zt, zh_en, it_fr, fr_el, es_en, en_de, fr_en, de_en, fr_es, en_ja,
    // ru_en, el_fr, ko_en, fr_it, ja_en, nl_en, en_fr
    static
    {
        s_supportedLanguagePairs = new HashSet(50);
        s_supportedLanguagePairs.add("fr_de");
        s_supportedLanguagePairs.add("it_en");
        s_supportedLanguagePairs.add("en_nl");
        s_supportedLanguagePairs.add("pt_fr");
        s_supportedLanguagePairs.add("el_en");
        s_supportedLanguagePairs.add("es_fr");
        s_supportedLanguagePairs.add("en_pt");
        s_supportedLanguagePairs.add("de_fr");
        s_supportedLanguagePairs.add("en_el");
        s_supportedLanguagePairs.add("en_ru");
        s_supportedLanguagePairs.add("zt_en");
        s_supportedLanguagePairs.add("en_zh");
        s_supportedLanguagePairs.add("fr_nl");
        s_supportedLanguagePairs.add("en_es");
        s_supportedLanguagePairs.add("nl_fr");
        s_supportedLanguagePairs.add("en_ko");
        s_supportedLanguagePairs.add("en_it");
        s_supportedLanguagePairs.add("pt_en");
        s_supportedLanguagePairs.add("fr_pt");
        s_supportedLanguagePairs.add("en_zt");
        s_supportedLanguagePairs.add("zh_en");
        s_supportedLanguagePairs.add("it_fr");
        s_supportedLanguagePairs.add("fr_el");
        s_supportedLanguagePairs.add("es_en");
        s_supportedLanguagePairs.add("en_de");
        s_supportedLanguagePairs.add("fr_en");
        s_supportedLanguagePairs.add("de_en");
        s_supportedLanguagePairs.add("fr_es");
        s_supportedLanguagePairs.add("en_ja");
        s_supportedLanguagePairs.add("ru_en");
        s_supportedLanguagePairs.add("el_fr");
        s_supportedLanguagePairs.add("ko_en");
        s_supportedLanguagePairs.add("fr_it");
        s_supportedLanguagePairs.add("ja_en");
        s_supportedLanguagePairs.add("nl_en");
        s_supportedLanguagePairs.add("en_fr");
    }

    /**
     * Creates a SystranProxy object for talking to Systran
     * This also reads in the property values.
     * If there is a problem finding the API property
     * values, then the SystranProxy will not function
     * properly
     */
    public SystranProxy()
        throws MachineTranslationException
    {
        super();

        try
        {
            ResourceBundle netegrityProperties = ResourceBundle.getBundle(
                SystranProxy.class.getName());

            m_url = netegrityProperties.getString("systran.url");
            m_id = netegrityProperties.getString("systran.id");

            CATEGORY.info("systran.url=" + m_url);
            CATEGORY.info("systran.id=" + m_id);
        }
        catch (Exception e)
        {
            throw new MachineTranslationException(
                "Could not find Systran properties to initialize MT.", e);
        }
    }

    /**
     * Returns the MT engine name: SysTran
     *
     * @return name
     */
    public String getEngineName()
    {
        return ENGINE_NAME;
    }

    /**
     * Returns true if the given locale pair is supported for MT.
     *
     * Supported language pairs are: fr_de, it_en, en_nl, pt_fr, el_en, es_fr,
     * en_pt, de_fr, en_el, en_ru, zt_en, en_zh, fr_nl, en_es, nl_fr, en_ko, en_it, pt_
     * en, fr_pt, en_zt, zh_en, it_fr, fr_el, es_en, en_de, fr_en, de_en, fr_es, en_ja,
     *  ru_en, el_fr, ko_en, fr_it, ja_en, nl_en, en_fr
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @return true | false
     * @exception MachineTranslationException
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
     * Actually does the real work of communicating with Systran
     *
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_string
     * @return
     * @exception MachineTranslationException
     */
    protected String doTranslation(Locale p_sourceLocale,
        Locale p_targetLocale, String p_string)
        throws MachineTranslationException
    {
        StringBuffer sb = new StringBuffer();

        try
        {
            URL url = new URL(m_url);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(
                conn.getOutputStream(), "UTF8");
            wr.write(preparePost(p_sourceLocale,p_targetLocale,p_string));
            wr.flush();
            wr.close();

            String l = null;
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                conn.getInputStream(), "UTF8"));

            while ((l = rd.readLine()) != null)
            {
                if (l.length() > 0)
                {
                    sb.append(l);
                    sb.append("\r\n");
                }
            }

            rd.close();

            return parseSystranXmlForResult(sb.toString());
        }
        catch (MachineTranslationException mte)
        {
            throw mte;
        }
        catch (Exception e)
        {
            throw new MachineTranslationException(
                "Failed to execute machine translation",
                (Throwable)e);
        }
    }

    /**
     * Prepare the HTTP Post line
     *
     * @param p_sourceLocale
     * @param p_targetLocale
     * @param p_string
     * @return
     * @exception Exception
     */
    private String preparePost(Locale p_sourceLocale,
        Locale p_targetLocale, String p_string)
        throws Exception
    {
        StringBuffer line = new StringBuffer();

        line.append("api=1");
        line.append("&id=").append(URLEncoder.encode(m_id, "UTF-8"));
        line.append("&src=").append(mapLanguage(p_sourceLocale));
        line.append("&tgt=").append(mapLanguage(p_targetLocale));
        line.append("&charset=UTF-8");
        line.append("&text=").append(URLEncoder.encode(p_string, "UTF-8"));

        return line.toString();
    }

    /**
     * Parses the Systran XML and returns the result of the
     * translation
     *
     * @param p_systranXml
     * @return
     * @exception Exception -- XML related exceptions
     * @exception MachineTranslationException -- thrown if Systran
     * reports a bad language pair or other problem
     */
    private String parseSystranXmlForResult(String p_systranXml)
        throws MachineTranslationException, Exception
    {
        CATEGORY.debug("SystranXml:\r\n" + p_systranXml);

        XmlParser xmlParser = XmlParser.hire();

        Document doc = xmlParser.parseXml(p_systranXml);
        Element root = doc.getRootElement();
        Node node = root.selectSingleNode("/translation/target");
        String error = node.valueOf("error");
        String result = node.getText();

        XmlParser.fire(xmlParser);

        if (error!=null && error.length() > 0)
        {
          CATEGORY.error("SystranXml:\r\n" + p_systranXml);
          throw new MachineTranslationException(error);
        }

        return node.getText();
    }


    public static void main (String args[]) throws Exception
    {
        if (args.length != 4)
        {
            System.out.println(
                "USAGE: SystranProxy <sourceLocale> <targetLocale> <file> (true|false)");
            System.out.println("       true if file is a gxml containing a single segment.");
            return;
        }

        Locale srcLocale = GlobalSightLocale.makeLocaleFromString(args[0]);
        Locale tgtLocale = GlobalSightLocale.makeLocaleFromString(args[1]);
        File filename = new File(args[2]);
        FileInputStream fis = new FileInputStream(filename);
        int size = (int) filename.length();
        byte[] bytes = new byte[size];
        fis.read(bytes);
        String content = new String(bytes);
        SystranProxy proxy = new SystranProxy();
        String result = "";

        System.err.println("args[3]="+args[3]);

        if ("true".equals(args[3]))
        {
            result = proxy.translateSegment(srcLocale,tgtLocale,content);
        }
        else
        {
            result = proxy.translate(srcLocale,tgtLocale,content);
        }

        System.out.println(result);
    }
}

