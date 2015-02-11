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

package com.globalsight.everest.edit.offline.page.terminology;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import com.globalsight.diplomat.util.XmlUtil;
import com.globalsight.terminology.Hitlist;
import com.globalsight.terminology.Hitlist.Hit;
import com.globalsight.terminology.termleverager.TermLeverageMatchResult;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <code>TermHelp</code> is the abstract class for all <i>"TermHelp"</i>s. It
 * privates a means to represent terminologies. All termonologies are organized
 * as concepts. One concept has one soure terminology and some matched target
 * terminologies. You can get more information from {@link Hitlist.Hit}
 * 
 * <p>
 * <code>TermHelp</code> helps you to serialize terminologies, so you can
 * migrate it to other systems or applications. The detail format depends on the
 * particular implements.
 * 
 * <p>
 * Before serialization, all terminologies will be merged to make sure no
 * duplicate concept exists. And all concepts will be sorted according to its
 * source terminology.
 * 
 * <p>
 * You can override the {@link #getContent()} to redefine the way, which used to
 * organize concepts, if you find the provided template is not appropriate.
 */
public abstract class TermHelp
{
    protected static Locale LOCALE = new Locale("en", "US");

    private String userName = "";
    private Locale srcLocale;
    private Locale trgLocale;
    private boolean format = true;
    private StringBuilder content = new StringBuilder();
    private HashMap<Hitlist.Hit, HashSet<String>> entries = new HashMap<Hit, HashSet<String>>();

    /**
     * Gets all sorted source terminologies.
     * 
     * @return All sorted source terminologies.
     */
    protected List<Hitlist.Hit> getSortedSource()
    {
        List<Hitlist.Hit> sources = new ArrayList<Hit>();
        sources.addAll(entries.keySet());
        SortUtil.sort(sources, new Comparator<Hitlist.Hit>()
        {
            @Override
            public int compare(Hit o1, Hit o2)
            {
                return o1.getTerm().compareTo(o2.getTerm());
            }
        });
        return sources;
    }

    /**
     * Retrieves useful information from the argement (<code>terms</code>),
     * organize it and then merge it into <code>entries</code>.
     * 
     * <p>
     * <strong>Note:</strong> duplicate terminologies will be removed during
     * merging.
     * 
     * @param terms
     *            The matched termilologies.
     */
    protected void setDatas(List<TermLeverageMatchResult> terms)
    {
        for (TermLeverageMatchResult match : terms)
        {
            Hitlist.Hit src = match.getSourceHit();
            HashSet<String> targets = entries.get(src);
            if (targets == null)
            {
                targets = new HashSet<String>();
                entries.put(src, targets);
            }

            String target = match.getFirstTargetTerm();
            while (target != null)
            {
                targets.add(target);
                target = match.getNextTargetTerm();
            }
        }
    }

    /**
     * Sets source locale, target locale and terminologies.
     * 
     * @param terms
     *            Matched terminologies, it is original data.
     * @param srcLocale
     *            The source locale.
     * @param trgLocale
     *            The target locale.
     */
    protected void init(List<TermLeverageMatchResult> terms, Locale srcLocale,
            Locale trgLocale)
    {
        setSrcLocale(srcLocale);
        setTrgLocale(trgLocale);
        setDatas(terms);
    }

    /**
     * Converts some terminologies to a string.
     * 
     * @param terms
     *            The terminologies to be converted.
     * @param srcLocale
     *            The source locale.
     * @param trgLocale
     *            the target locale.
     * @return The converted result.
     */
    public String convert(List<TermLeverageMatchResult> terms,
            Locale srcLocale, Locale trgLocale)
    {
        init(terms, srcLocale, trgLocale);

        content.append(getHead());
        content.append(getContent());
        content.append(getEnd());

        String result = content.toString();
        if (format)
            result = XmlUtil.format(result);

        return result;
    }

    // /**
    // * Gets the pattern of language group.
    // * <p>
    // * The following argements is needed for formation:
    // * <ul>
    // * <li>language. "English" for example.</li>
    // * <li>country. "US" for example.</li>
    // * <li>terminology.</li>
    // * </ul>
    // *
    // * @return The pattern of language group.
    // */
    // public abstract String getLanguagePattern();

    public abstract String getLanguage(Locale locale, String terminology,
            String descXML);

    /**
     * Gets the formated concept.
     * 
     * @param languageGrp
     *            The formated language group.
     * @param conceptId
     *            The id of concept, may be useful for formation.
     * @return The formated concept.
     */
    public abstract String getConcept(String languageGrp, long conceptId);

    /**
     * Get all content that you want to insert before the information about
     * concept.
     * 
     * @return The head string.
     */
    public abstract String getHead();

    /**
     * 
     * Get all content that you want to insert after the information about
     * concept.
     * 
     * @return The end string.
     */
    public abstract String getEnd();

    /**
     * Gets the content that between the head string and end string. It includes
     * all information about all terminologies.
     * 
     * @return Formated string, about all terminologies.
     */
    public String getContent()
    {
        StringBuilder content = new StringBuilder();
        for (Hitlist.Hit src : getSortedSource())
        {
            String languageGrp = getLanguageGrp(src, entries.get(src),
                    src.getDescXML());
            String concept = getConcept(languageGrp, src.getConceptId());
            content.append(concept);
        }
        return content.toString();
    }

    /**
     * Desides whether formate the convert result, the default value is ture.
     * <p>
     * System will format the result as xml format, so please set the value to
     * false if you find the returned value if not meet your expect.
     * 
     * @param isFormat
     *            ture of false.
     */
    public void setFormat(boolean isFormat)
    {
        format = isFormat;
    }

    public boolean getFormat()
    {
        return format;
    }

    /**
     * Gets the formated string about the language group, the string will
     * include all informations about the source terminology and terget
     * terminologies.
     * 
     * <p>
     * The language group is a part of concept, so the returned string is used
     * to format concept.
     * 
     * @param source
     *            The source teminology of the language group.
     * @param targets
     *            The target teminologies of the language group.
     * @param descXML
     * @return The formated language group.
     */
    protected String getLanguageGrp(Hitlist.Hit source,
            HashSet<String> targets, String descXML)
    {
        StringBuilder languages = new StringBuilder();

        String src = getLanguage(getSrcLocale(),
                EditUtil.encodeXmlEntities(source.getTerm()), descXML);
        languages.append(src);

        for (String target : targets)
        {
            String tar = getLanguage(getTrgLocale(),
                    EditUtil.encodeXmlEntities(target), "");
            languages.append(tar);
        }

        return languages.toString();
    }

    public HashMap<Hitlist.Hit, HashSet<String>> getEntries()
    {
        return entries;
    }

    public void setEntries(HashMap<Hitlist.Hit, HashSet<String>> entries)
    {
        this.entries = entries;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Locale getSrcLocale()
    {
        return srcLocale;
    }

    public void setSrcLocale(Locale srcLocale)
    {
        this.srcLocale = srcLocale;
    }

    public Locale getTrgLocale()
    {
        return trgLocale;
    }

    public void setTrgLocale(Locale trgLocale)
    {
        this.trgLocale = trgLocale;
    }
}
