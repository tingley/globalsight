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

package com.globalsight.terminology;

import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;

import java.util.ArrayList;

/**
 * A data class holding termbase statistics: number of languages,
 * number of entries, number of entries per language and so on.
 */
public class StatisticsInfo
{
    static public final String INDEX_UNKNOWN = "unknown";
    static public final String INDEX_OK = "ok";
    static public final String INDEX_NOTAVAILABLE = "unavailable";

    static private class LanguageInfo
    {
        private String m_language = "";
        private int m_terms = 0;
        private int m_fuzzyIndexedCount = -1;
        private int m_fulltextIndexedCount = -1;

        public LanguageInfo()
        {
        }

        public LanguageInfo(String p_language, int p_num)
        {
            m_language = p_language;
            m_terms = p_num;
        }

        public String getLanguage()
        {
            return m_language;
        }

        public void setLanguage(String p_language)
        {
            m_language = p_language;
        }

        public int getTerms()
        {
            return m_terms;
        }

        public void setTerms(int p_num)
        {
            m_terms = p_num;
        }

        public int getFuzzyIndexedCount()
        {
            return m_fuzzyIndexedCount;
        }

        public void setFuzzyIndexedCount(int p_num)
        {
            m_fuzzyIndexedCount = p_num;
        }

        public int getFulltextIndexedCount()
        {
            return m_fuzzyIndexedCount;
        }

        public void setFulltextIndexedCount(int p_num)
        {
            m_fuzzyIndexedCount = p_num;
        }
    }

    //
    // Members
    //

    private String m_termbase = "";
    private int m_concepts = 0;
    private int m_terms = 0;

    private String m_indexStatus = INDEX_UNKNOWN;
    private int m_fulltextCount = 0;

    /** List of entry counts per language. */
    private ArrayList m_languageInfo = new ArrayList();

    //
    // Constructors
    //

    public StatisticsInfo()
    {
    }

    /*
    public StatisticsInfo(String p_termbase, int p_concepts)
    {
        m_termbase = p_termbase;
        m_concepts = p_concepts;
    }
    */

    public String getTermbase()
    {
        return m_termbase;
    }

    public void setTermbase(String p_termbase)
    {
        m_termbase = p_termbase;
    }

    public int getConcepts()
    {
        return m_concepts;
    }

    public void setConcepts(int p_num)
    {
        m_concepts = p_num;
    }

    public int getTerms()
    {
        return m_terms;
    }

    public void setTerms(int p_num)
    {
        m_terms = p_num;
    }

    /** How many descrips on concept-level have been fulltext-indexed? */
    public int getFulltextCount()
    {
        return m_fulltextCount;
    }

    public void setFulltextCount(int p_num)
    {
        m_fulltextCount = p_num;
    }

    public String getIndexStatus()
    {
        return m_indexStatus;
    }

    public void setIndexStatus(String p_arg)
    {
        m_indexStatus = p_arg;
    }

    public void addLanguageInfo(String p_language, int p_num)
    {
        m_languageInfo.add(new LanguageInfo(p_language, p_num));
    }

    public void setFuzzyIndexedCount(String p_language, int p_num)
    {
        LanguageInfo info = getLanguageInfo(p_language);

        if (info != null)
        {
            info.setFuzzyIndexedCount(p_num);
        }
    }

    public void setFulltextIndexedCount(String p_language, int p_num)
    {
        LanguageInfo info = getLanguageInfo(p_language);

        if (info != null)
        {
            info.setFulltextIndexedCount(p_num);
        }
    }

    private LanguageInfo getLanguageInfo(String p_language)
    {
        for (int i = 0, max = m_languageInfo.size(); i < max; i++)
        {
            LanguageInfo info = (LanguageInfo)m_languageInfo.get(i);

            if (info.getLanguage().equalsIgnoreCase(p_language))
            {
                return info;
            }
        }

        return null;
    }

    /**
     * @return an xml string of the form
     *   <statistics>
     *     <termbase>termbase name</termbase>
     *     <concepts>number of entries</concepts>
     *     <terms>number of overall terms</terms>
     *     <indexes>
     *       <index>
     *         <language>index name</language>
     *         <terms>number of terms</terms>
     *       </index>
     *     </indexes>
     *   </statistics>
     */
    public String asXML()
    {
        StringBuffer result = new StringBuffer();

        result.append("<statistics>\n");
        result.append("<termbase>");
        result.append(m_termbase);
        result.append("</termbase>\n");
        result.append("<concepts>");
        result.append(m_concepts);
        result.append("</concepts>\n");
        result.append("<terms>");
        result.append(m_terms);
        result.append("</terms>\n");

        result.append("<indexstatus>");
        result.append(m_indexStatus);
        result.append("</indexstatus>\n");
        result.append("<fulltextcount>");
        result.append(m_fulltextCount);
        result.append("</fulltextcount>\n");

        result.append("<indexes>\n");
        for (int i = 0; i < m_languageInfo.size(); ++i)
        {
            LanguageInfo linfo = (LanguageInfo)m_languageInfo.get(i);

            result.append("<index>\n");

            result.append("<language>");
            result.append(linfo.getLanguage());
            result.append("</language>\n");

            result.append("<terms>");
            result.append(linfo.getTerms());
            result.append("</terms>\n");

            result.append("<fuzzycount>");
            result.append(linfo.getFuzzyIndexedCount());
            result.append("</fuzzycount>\n");

            result.append("<fulltextcount>");
            result.append(linfo.getFulltextIndexedCount());
            result.append("</fulltextcount>\n");

            result.append("</index>\n");
        }
        result.append("</indexes>\n");

        result.append("</statistics>");

        return result.toString();
    }
}
