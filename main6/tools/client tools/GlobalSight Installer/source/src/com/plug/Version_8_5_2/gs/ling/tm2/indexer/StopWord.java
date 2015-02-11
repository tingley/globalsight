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
package com.plug.Version_8_5_2.gs.ling.tm2.indexer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;
import com.util.ServerUtil;

/**
 * StopWord object holds a list of stop words for a specified locale.
 */

public class StopWord
{
    private static final Logger c_logger = Logger.getLogger(StopWord.class.getName());

    private static final String STOP_WORD_FILE = "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/tm/StopWordList_";

    // StopWord object cache
    // Key: language code (en, fr, etc)
    // Value: StopWord object
    private static Map c_stopWordCache = new Hashtable();

    private Set m_stopWords = new HashSet();
    
    private boolean m_isFileExists = true;

    // factory method
    static public StopWord getStopWord(GlobalSightLocale p_locale) throws Exception
    {
        StopWord sw = (StopWord) c_stopWordCache.get(p_locale);
        if (sw == null)
        {
            sw = new StopWord(p_locale);
            c_stopWordCache.put(p_locale, sw);
        }

        return sw;
    }
    
    public boolean isConfigExists()
    {
        return m_isFileExists;
    }

    // constructor
    private StopWord(GlobalSightLocale p_locale) throws Exception
    {
        // common stop words (symbols)

        // [SP]!"#$%&'()*+,-./0123456789:;<=>?@
        for (int i = 0x20; i < 0x41; i++)
        {
            m_stopWords.add(String.valueOf((char) i));
        }

        // [\]^_`abcdefghijklmnopqrstuvwxyz{|}~
        for (int i = 0x5b; i < 0x80; i++)
        {
            m_stopWords.add(String.valueOf((char) i));
        }

        // Latin-1 symbols
        for (int i = 0xa0; i < 0xc0; i++)
        {
            m_stopWords.add(String.valueOf((char) i));
        }

        // Unicode punctuation
        for (int i = 0x2000; i < 0x206F; i++)
        {
            m_stopWords.add(String.valueOf((char) i));
        }

        // get stop word list for a specified language
        readStopWordFromFile(p_locale);

    }

    public boolean isStopWord(String p_word)
    {
        return m_stopWords.contains(p_word);
    }

    public Set getStodWords()
    {
        return m_stopWords;
    }

    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[[");

        Iterator it = m_stopWords.iterator();
        while (it.hasNext())
        {
            sb.append((String) it.next()).append(",");
        }
        sb.append("]]");

        return sb.toString();
    }

    private void readStopWordFromFile(GlobalSightLocale p_locale) throws Exception
    {
        String path = ServerUtil.getPath() + STOP_WORD_FILE
                + p_locale.getLanguageCode().toLowerCase();
        java.io.File f = new java.io.File(path);
        InputStream in = null;
        if (f != null && f.exists())
        {
            in = new FileInputStream(f);
        }
        else
        {
            m_isFileExists = false;
        }

        if (in != null)
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));

            String line;
            while ((line = br.readLine()) != null)
            {
                if (!(line.startsWith(" ") || line.startsWith("|") || line.startsWith("\t") || line
                        .length() == 0))
                {
                    line = line.replaceAll("[\\s|].*", "");
                    m_stopWords.add(line);
                }
            }

            br.close();
        }
        else
        {
            c_logger.warn("Couldn't find a stop word file for " + p_locale.toString());
        }
    }

}
