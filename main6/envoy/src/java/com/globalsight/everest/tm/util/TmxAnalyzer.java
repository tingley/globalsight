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

package com.globalsight.everest.tm.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Reads Trados TMX files and counts how many TUs they contain.
 */
public class TmxAnalyzer
{
    private int m_tuCount = 0;
    private int m_tuvCount = 0;
    private int m_localeCount = 0;

    // Locale info
    private HashSet m_locales = new HashSet();

    // TMX header info
    private Tmx m_tmx;
    private String m_tmxVersion = "";

    private static final REProgram DTD_SEARCH_PATTERN = createSearchPattern("tmx\\d+\\.dtd");

    private static REProgram createSearchPattern(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }
        return pattern;
    }

    //
    // Constructors
    //

    public TmxAnalyzer()
    {
    }

    public void log(String p_message)
    {
        System.out.println(p_message);
    }

    public void analyze(String p_url) throws Exception
    {
        m_tuCount = 0;
        m_tuvCount = 0;
        m_localeCount = 0;
        m_locales = new HashSet();
        m_tmxVersion = "";
        m_tmx = null;

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        reader.setEntityResolver(DtdResolver.getInstance());
        reader.setValidation(true);

        log("Analyzing document: " + p_url);

        reader.addHandler("/tmx", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
                Element element = path.getCurrent();

                m_tmxVersion = element.attributeValue("version");
            }

            public void onEnd(ElementPath path)
            {
            }
        });

        reader.addHandler("/tmx/header", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                element.detach();

                m_tmx = new Tmx(element);
                m_tmx.setTmxVersion(m_tmxVersion);
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
                ++m_tuCount;

                if (m_tuCount % 1000 == 0)
                {
                    log("TU " + m_tuCount);
                }
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                List tuvs = element.selectNodes("//tuv");

                m_tuvCount += tuvs.size();

                for (int i = 0, max = tuvs.size(); i < max; i++)
                {
                    Element tuv = (Element) tuvs.get(i);

                    String locale = tuv.attributeValue("lang");
                    m_locales.add(locale);
                }

                // prune the current element to reduce memory
                element.detach();

                element = null;
            }
        });

        Document document = reader.read(p_url);

        m_localeCount = m_locales.size();

        log("File: " + p_url);
        log("TMX version: " + m_tmxVersion);
        log("Total TUs: " + m_tuCount);
        log("Total TUVs: " + m_tuvCount);
        log("Total Locales: " + m_localeCount);

        for (Iterator it = m_locales.iterator(); it.hasNext();)
        {
            String locale = (String) it.next();

            log(locale);
        }

        // all done
    }

    static public void main(String[] argv) throws Exception
    {
        TmxAnalyzer a = new TmxAnalyzer();

        if (argv.length != 1)
        {
            System.err.println("Usage: TmxAnalyzer FILE\n"
                    + "\tAnalyzes a TMX file for syntactical correctness.\n"
                    + "\tPrints out the number of TUs and TUVs at the end.");
            System.exit(1);
        }

        a.analyze(argv[0]);
    }
}
