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
package com.globalsight.everest.segmentationhelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.sun.org.apache.regexp.internal.RE;

/**
 * SegmentationRule is datastructure of a segmentation rule file, it holds all
 * information of a segmentation rule file, currently it supports SRX
 * 2.0(http://www.lisa.org/standards/srx/srx20.html).
 * 
 * 
 */
public class SegmentationRule implements Serializable
{

    static private final Logger CATEGORY = Logger
            .getLogger(SegmentationRule.class);

    private String m_rootName = null;

    private String version = null;

    /**
     * All of Rules a segmentation rule file contains, keys are languagename, a
     * value is a ArrayList contains all the rules a languagename key
     * associated.
     */
    private HashMap<String, ArrayList<Rule>> m_rules = new HashMap<String, ArrayList<Rule>>();

    /**
     * All of languagemaps a segmentation rule file contains.
     */
    private ArrayList<LanguageMap> m_languageMaps = new ArrayList<LanguageMap>();

    /**
     * A datastructure hold information of header element of a segmentatin rule
     * file.
     */
    private SrxHeader m_header = null;

    public SegmentationRule()
    {
        m_rootName = null;
        m_rules = null;
        m_languageMaps = null;
        m_header = null;
    }

    public SegmentationRule(String p_rootName,
            HashMap<String, ArrayList<Rule>> p_rules,
            ArrayList<LanguageMap> p_languageMaps, SrxHeader p_header)
    {
        m_rootName = p_rootName;
        m_rules = p_rules;
        m_languageMaps = p_languageMaps;
        m_header = p_header;
    }

    public SrxHeader getHeader()
    {
        return m_header;
    }

    public void setHeader(SrxHeader p_header)
    {
        this.m_header = p_header;
    }

    public ArrayList<LanguageMap> getLanguageMaps()
    {
        return m_languageMaps;
    }

    public void setLanguageMap(ArrayList<LanguageMap> p_map)
    {
        m_languageMaps = p_map;
    }

    public String getRootName()
    {
        return m_rootName;
    }

    public void setRootName(String p_name)
    {
        m_rootName = p_name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public HashMap<String, ArrayList<Rule>> getRules()
    {
        return m_rules;
    }

    public void setRules(HashMap<String, ArrayList<Rule>> p_rules)
    {
        this.m_rules = p_rules;
    }

    public ArrayList<Rule> getRulesBylanguageName(String p_languageName)
    {
        ArrayList<Rule> rules = m_rules.get(p_languageName);
        return rules;
    }

    public ArrayList<Rule> getRulesBylanguageCode(String p_languageCode)
            throws Exception
    {
        ArrayList<Rule> rules = new ArrayList<Rule>();
        try
        {

            for (int i = 0; i < m_languageMaps.size(); i++)
            {
                LanguageMap langMap = m_languageMaps.get(i);
                String pattern = langMap.getLanguagePattern();

                RE re = new RE(pattern);
                if (re.match(p_languageCode))
                {
                    String languageName = langMap.getLanguageruleName();

                    if (m_header.isCascade())
                    {
                        // We should take all rules from associated
                        // languagenames.
                        rules.addAll(this.getRulesBylanguageName(languageName));
                    }
                    else
                    {
                        // We only take rules from the first match languagename
                        rules = this.getRulesBylanguageName(languageName);
                        CATEGORY.info("Only taking rules from the first match languagename ...");
                        break;
                    }

                }
            }

        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Exception in getRulesBylanguageCode(String p_languageCode)",
                    e);
            throw new Exception(e.getMessage());
        }
        return rules;
    }

    /**
     * Get rules by locale, since SRX doesn't support locale regular expression,
     * if the language pattern is not locale, we will get the language code from
     * p_locale and get rules using language code.
     * 
     * @param p_locale
     * @return Arraylist filled with rules
     * @throws Exception
     */
    public ArrayList<Rule> getRulesByLocale(String p_locale) throws Exception
    {
        p_locale = p_locale.trim();
        int index = p_locale.indexOf("_");
        RE re = null;
        String language = p_locale.substring(0, index);
        ArrayList<Rule> rules = new ArrayList<Rule>();
        try
        {
            for (int i = 0; i < m_languageMaps.size(); i++)
            {
                LanguageMap langMap = m_languageMaps.get(i);
                String pattern = langMap.getLanguagePattern();
                re = new RE(pattern);

                if (pattern.trim().equalsIgnoreCase(p_locale))
                {
                    // languagepattern is not language code regular expression,
                    // just locale string as "en_US"(SRX2.0 is not supported).
                    CATEGORY.info("locale equals language pattern ignorecasely ");
                    CATEGORY.info("Locale is :" + p_locale
                            + "language pattern is: " + pattern);
                    String languageName = langMap.getLanguageruleName();
                    rules = this.getRulesBylanguageName(languageName);
                    break;

                }
                else
                {
                    // languagepattern is language code regular
                    // expression (specification of SRX2.0).
                    if (re.match(language))
                    {
                        String languageName = langMap.getLanguageruleName();

                        if (m_header.isCascade())
                        {
                            // Cascade is allowed, get rules from all associated
                            // languagerulenames corresponding to matched
                            // languagepattern.
                            rules.addAll(this
                                    .getRulesBylanguageName(languageName));
                            continue;
                        }
                        else
                        {
                            // Cascade is not allowed.
                            CATEGORY.debug("Cascade is not allowed ...");
                            rules = this.getRulesBylanguageName(languageName);
                            break;
                        }
                    }
                }
            }

        }
        catch (Exception e)
        {
            CATEGORY.error("Exception while getting rule by locale string", e);
            throw new Exception(e.getMessage());
        }
        return rules;

    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ROOTNAME" + "\n");
        sb.append("m_rootName :");
        sb.append(m_rootName);
        sb.append("HEADER" + "\n");
        sb.append(m_header.toString());
        sb.append("RULES" + "\n");
        Set<String> langNameSet = m_rules.keySet();
        TreeSet<String> langName = new TreeSet<String>(langNameSet);
        Iterator<String> iter = langName.iterator();
        while (iter.hasNext())
        {
            String name = iter.next();
            sb.append(name + "\n");
            ArrayList<Rule> rules = (ArrayList<Rule>) m_rules.get(name);
            for (int j = 0; j < rules.size(); j++)
            {
                Rule rule = rules.get(j);
                sb.append(rule.toString() + "\n");
            }

        }

        sb.append("MAPRULES************" + "\n");
        for (int i = 0; i < m_languageMaps.size(); i++)
        {
            LanguageMap langMap = m_languageMaps.get(i);
            sb.append(langMap.toString());
        }

        return sb.toString();
    }

}
