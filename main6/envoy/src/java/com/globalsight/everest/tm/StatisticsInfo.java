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

package com.globalsight.everest.tm;

import java.util.ArrayList;
import java.util.Locale;

import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.util.SortUtil;

/**
 * A data class holding TM statistics: source and target languages, number of
 * TUs, number of TUVs.
 */
public class StatisticsInfo
{
    static private class LanguageInfo
    {
        private Locale m_locale;
        private String m_language;
        private int m_tus;
        private int m_tuvs;
        private long m_localeID = 0;

        public String getLanguage()
        {
            return m_language;
        }

        public LanguageInfo(Locale p_locale, String p_language, int p_tus,
                int p_tuvs)
        {
            m_locale = p_locale;
            m_language = p_language;
            m_tus = p_tus;
            m_tuvs = p_tuvs;
        }

        public LanguageInfo(long p_localeID, Locale p_locale,
                String p_language, int p_tus, int p_tuvs)
        {
            m_locale = p_locale;
            m_language = p_language;
            m_tus = p_tus;
            m_tuvs = p_tuvs;
            m_localeID = p_localeID;
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<language>");
            result.append("<locale>");
            if ("in_id".equalsIgnoreCase(m_locale.toString()))
            {
                result.append("id_ID");
            }
            else
            {
                result.append(m_locale.toString());
            }
            result.append("</locale>");
            result.append("<name>");
            result.append(m_language);
            result.append("</name>");
            result.append("<tus>");
            result.append(m_tus);
            result.append("</tus>");
            result.append("<tuvs>");
            result.append(m_tuvs);
            result.append("</tuvs>");
            if (m_localeID != 0)
            {
                result.append("<localeID>");
                result.append(m_localeID);
                result.append("</localeID>");
            }
            result.append("</language>");

            return result.toString();
        }
    }

    static private class ProjectInfo
    {
        private String projectName;
        private int m_tus;
        private int m_tuvs;

        public String getProjectName()
        {
            return projectName;
        }

        public ProjectInfo(String projectName, int m_tus, int m_tuvs)
        {
            this.projectName = projectName;
            this.m_tus = m_tus;
            this.m_tuvs = m_tuvs;
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);
            result.append("<project>");
            result.append("<name>");
            result.append(projectName);
            result.append("</name>");
            result.append("<tus>");
            result.append(m_tus);
            result.append("</tus>");
            result.append("<tuvs>");
            result.append(m_tuvs);
            result.append("</tuvs>");
            result.append("</project>");

            return result.toString();
        }
    }

    //
    // Members
    //

    private String m_tm = "";
    private int m_tus = 0;
    private int m_tuvs = 0;

    /**
     * List of source and target languages. Source language (if known at all) is
     * kept as first element in the list.
     */
    private ArrayList m_languages = new ArrayList();
    private ArrayList m_projects = new ArrayList();

    //
    // Constructors
    //

    public StatisticsInfo()
    {
    }

    public StatisticsInfo(String p_tm, int p_tus, int p_tuvs)
    {
        m_tm = p_tm;
        m_tus = p_tus;
        m_tuvs = p_tuvs;
    }

    public String getTm()
    {
        return m_tm;
    }

    public void setTm(String p_tm)
    {
        m_tm = p_tm;
    }

    public int getTUs()
    {
        return m_tus;
    }

    public void setTUs(int p_tus)
    {
        m_tus = p_tus;
    }

    public int getTUVs()
    {
        return m_tuvs;
    }

    public void setTUVs(int p_tuvs)
    {
        m_tuvs = p_tuvs;
    }

    public void addLanguageInfo(Locale p_locale, String p_language, int p_tus,
            int p_tuvs)
    {
        m_languages.add(new LanguageInfo(p_locale, p_language, p_tus, p_tuvs));
    }

    public void addLanguageInfo(long localeID, Locale p_locale,
            String p_language, int p_tus, int p_tuvs)
    {
        m_languages.add(new LanguageInfo(localeID, p_locale, p_language, p_tus,
                p_tuvs));
    }

    /**
     * @return an xml string of the form <statistics> <tm>tm name</tm>
     *         <tus>number of TUs</tu> <tuvs>number of overall tuvs</tuvs>
     *         <languages> <language> <locale>locale present in TM</locale>
     *         <name>display name of locale</name> <tus>number of tus in this
     *         language</tus> <tuvs>number of tuvs in this language</tuvs>
     *         </language> </languages> </statistics>
     */
    public String asXML()
    {
        return asXML(false);
    }

    public String asXML(boolean includeProjects)
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<statistics>");
        result.append("<tm>");
        result.append(m_tm);
        result.append("</tm>");
        result.append("<tus>");
        result.append(m_tus);
        result.append("</tus>");
        result.append("<tuvs>");
        result.append(m_tuvs);
        result.append("</tuvs>");

        result.append("<languages>");

        for (int i = 0; i < m_languages.size(); ++i)
        {
            SortUtil.sort(m_languages,
                    new LanguageInfoComparator(Locale.getDefault()));
            LanguageInfo lang = (LanguageInfo) m_languages.get(i);
            result.append(lang.asXML());
        }
        result.append("</languages>");

        if (includeProjects)
        {
            SortUtil.sort(m_projects,
                    new ProjectInfoComparator(Locale.getDefault()));
            result.append("<projects>");
            for (int i = 0; i < m_projects.size(); ++i)
            {
                ProjectInfo project = (ProjectInfo) m_projects.get(i);
                result.append(project.asXML());
            }
            result.append("</projects>");
        }

        result.append("</statistics>");

        return result.toString();
    }

    public void addUpdateProjectInfo(String project, int tus, int tuvs)
    {
        m_projects.add(new ProjectInfo(project, tus, tuvs));
    }

    // Fix for GBS-1693
    static private class LanguageInfoComparator extends StringComparator
    {
        /**
         * Creates a LanguageInfoComparator with the given locale.
         */
        public LanguageInfoComparator(Locale p_locale)
        {
            super(p_locale);
        }

        public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
            LanguageInfo a = (LanguageInfo) p_A;
            LanguageInfo b = (LanguageInfo) p_B;

            String aValue;
            String bValue;
            int rv;

            aValue = a.getLanguage();
            bValue = b.getLanguage();
            rv = this.compareStrings(aValue, bValue);
            return rv;
        }
    }

    // Fix for GBS-1693
    static private class ProjectInfoComparator extends StringComparator
    {
        /**
         * Creates a ProjectInfoComparator with the given locale.
         */
        public ProjectInfoComparator(Locale p_locale)
        {
            super(p_locale);
        }

        public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
            ProjectInfo a = (ProjectInfo) p_A;
            ProjectInfo b = (ProjectInfo) p_B;

            String aValue;
            String bValue;
            int rv;

            aValue = a.getProjectName();
            bValue = b.getProjectName();
            rv = this.compareStrings(aValue, bValue);
            return rv;
        }
    }
}
