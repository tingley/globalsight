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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.terminology.util.XmlParser;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * A definition defines languages and entries stored in a termbase. It includes
 * the language names and locales, and the fields.
 * </p>
 */
public class Definition implements FieldTypes, TermbaseExceptionMessages
{
    private static final Logger CATEGORY = Logger.getLogger(Definition.class);

    static public final String s_propertyFile = "properties/Terminology";
    static public final String s_definitionKey = "default_definition";

    static public final String s_defaultDefinition = "<definition><name></name><description></description>"
            + "<languages></languages><fields></fields><indexes></indexes>"
            + "</definition>";

    public final class Language
    {
        private String m_name;
        private String m_locale;
        private boolean m_hasTerms;

        public Language(String p_name, String p_locale, String p_hasTerms)
        {
            m_name = p_name.trim();
            m_locale = p_locale.trim();
            m_hasTerms = Boolean.valueOf(p_hasTerms).booleanValue();
        }

        public String getName()
        {
            return m_name;
        }

        public String getLocale()
        {
            return m_locale;
        }

        public boolean hasTerms()
        {
            return m_hasTerms;
        }

        // Equality is defined as "same name + same locale"
        public boolean equals(Object p_other)
        {
            if (p_other instanceof Definition.Language)
            {
                Definition.Language other = (Definition.Language) p_other;

                if (other.m_name.equals(m_name)
                        && other.m_locale.equals(m_locale))
                {
                    return true;
                }
            }

            return false;
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<language>");

            result.append("<name>");
            result.append(EditUtil.encodeXmlEntities(m_name));
            result.append("</name>");

            result.append("<locale>");
            result.append(EditUtil.encodeXmlEntities(m_locale));
            result.append("</locale>");

            result.append("<hasterms>");
            result.append(m_hasTerms);
            result.append("</hasterms>");

            result.append("</language>");

            return result.toString();
        }
    }

    public final class Field
    {
        private String m_name;
        private String m_type;
        private boolean m_system;
        private boolean m_indexed;
        private String m_values;

        public Field(String p_name, String p_type, String p_system,
                String p_indexed, String p_values)
        {
            m_name = p_name.trim();
            m_type = p_type.trim();
            m_system = Boolean.getBoolean(p_system);
            m_indexed = Boolean.getBoolean(p_indexed);
            m_values = p_values.trim();
        }

        public String getName()
        {
            return m_name;
        }

        public String getType()
        {
            return m_type;
        }

        public boolean getSystem()
        {
            return m_system;
        }

        public boolean getIndexed()
        {
            return m_indexed;
        }

        public String getValues()
        {
            return m_values;
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<field>");

            result.append("<name>");
            result.append(EditUtil.encodeXmlEntities(m_name));
            result.append("</name>");

            result.append("<type>");
            result.append(m_type);
            result.append("</type>");

            result.append("<system>");
            result.append(m_system);
            result.append("</system>");

            result.append("<indexed>");
            result.append(m_indexed);
            result.append("</indexed>");

            result.append("<values>");
            result.append(EditUtil.encodeXmlEntities(m_values));
            result.append("</values>");

            result.append("</field>");

            return result.toString();
        }
    }

    public final class Index
    {
        static public final String TYPE_FUZZY = "fuzzy";
        static public final String TYPE_FULLTEXT = "fulltext";

        // Empty string for concept-level full-text index.
        private String m_languageName;
        private String m_locale;
        private String m_type;

        public Index(String p_languageName, String p_locale, String p_type)
        {
            m_languageName = p_languageName.trim();
            m_locale = p_locale.trim();
            m_type = p_type.trim();
        }

        public String getLanguageName()
        {
            return m_languageName;
        }

        public String getLocale()
        {
            return m_locale;
        }

        public String getType()
        {
            return m_type;
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<index>");

            result.append("<languagename>");
            result.append(EditUtil.encodeXmlEntities(m_languageName));
            result.append("</languagename>");

            result.append("<locale>");
            result.append(EditUtil.encodeXmlEntities(m_locale));
            result.append("</locale>");

            result.append("<type>");
            result.append(m_type);
            result.append("</type>");

            result.append("</index>");

            return result.toString();
        }
    }

    //
    // Private & Protected Constants
    //
    private String m_name = "";
    private String m_description = "";
    private ArrayList m_languages = new ArrayList();
    private ArrayList m_fields = new ArrayList();
    private ArrayList m_indexes = new ArrayList();

    //
    // Constructors
    //

    // Wed Jul 21 23:24:56 2004 Changed the constructor from protected
    // to public so util/MappingContext can create an object to render
    // UI display strings.
    public Definition(String p_definition) throws TermbaseException
    {
        init(p_definition);
    }

    protected Definition(String p_name, String p_definition)
            throws TermbaseException
    {
        init(p_name, p_definition);
    }

    //
    // Package-Private Methods
    //

    // Called by Termbase.rename().
    void setName(String p_name)
    {
        m_name = p_name;
    }

    //
    // Public Methods
    //

    public String getName()
    {
        return m_name;
    }

    public String getDescription()
    {
        return m_description;
    }

    public ArrayList getLanguages()
    {
        return m_languages;
    }

    public Language getLanguage(String p_name)
    {
        // Could use sorted arrays and binary search or hash map.
        for (int i = 0, max = m_languages.size(); i < max; ++i)
        {
            Language lang = (Language) m_languages.get(i);

            if (lang.getName().equalsIgnoreCase(p_name))
            {
                return lang;
            }
        }

        return null;
    }
    
    public List<Language> getLanguages(String p_names)
    {
    	List<Language> langs = new ArrayList<Language>();
    	String[] langStrs = p_names.split(",");
        for (int i = 0, max = m_languages.size(); i < max; ++i)
        {
            Language lang = (Language) m_languages.get(i);

            for(String langName : langStrs)
            {
            	if (lang.getName().equalsIgnoreCase(langName))
            	{
            		langs.add(lang);
            		break;
            	}
            }
        }
        if(langs.size() > 0)
        	return langs;

        return null;
    }

    public ArrayList getFields()
    {
        return m_fields;
    }

    public ArrayList getIndexes()
    {
        return m_indexes;
    }

    /**
     * Given a language name, returns that language's locale as string.
     */
    public String getLocaleByLanguage(String p_language)
    {
        Language lang = getLanguage(p_language);
        if (lang != null)
        {
            return lang.getLocale();
        }
        else
        {
            return null;
        }
    }

    /**
     * Given a locale, returns the language names that contain terms in that
     * locale. Normally this is one language only, but some termbases may
     * contain a general language like Spanish and a specific language like
     * Spanish (Costa Rica). In that case, when searching for es_CR, both
     * languages are returned (in no specific order).
     * 
     * @return an ArrayList of Strings.
     */
    public ArrayList getLanguagesByLocale(String p_locale)
    {
        ArrayList result = new ArrayList(2);
        p_locale = p_locale.trim();

        // Thu Apr 01 22:30:23 2004 GSDEF 9929: Termbase uses new
        // language codes while all JDKs - and sometimes GlobalSight by
        // mistake - use the old codes. Fix the locale.
        String locale = EditUtil.toRFC1766(p_locale);

        // Case 1: the search locale is a major language. Only one
        // language (that contains terms) should exist.
        if (locale.length() == 2)
        {
            for (int i = 0, max = m_languages.size(); i < max; ++i)
            {
                Language lang = (Language) m_languages.get(i);

                if (lang.hasTerms() && lang.getLocale().equals(locale))
                {
                    result.add(lang.getName());
                    break;
                }
            }
        }
        // Case 2: the search locale is a country-specific language.
        // Two languages (that contain terms) may exist.
        else
        {
            String prefix = locale.substring(0, 2);
            for (int i = 0, max = m_languages.size(); i < max; ++i)
            {
                Language lang = (Language) m_languages.get(i);

                if (lang.hasTerms()
                        && (lang.getLocale().equals(locale)
                                || lang.getLocale().equals(prefix) || lang
                                .getLocale().equals(p_locale)))
                {
                    result.add(lang.getName());
                }
            }
        }

        return result;
    }

    /**
     * Returns true if the field of type TYPE has the isIndexed property set to
     * true.
     */
    public boolean isIndexedField(String p_type)
    {
        for (int i = 0, max = m_fields.size(); i < max; i++)
        {
            Field field = (Field) m_fields.get(i);

            if (field.getType().equals(p_type))
            {
                return field.getIndexed();
            }
        }

        return false;
    }

    /**
     * Returns a termbase definition as XML string. For easy post-processing in
     * Java make sure to not use any white space or newlines.
     */
    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<definition>");

        result.append("<name>");
        result.append(EditUtil.encodeXmlEntities(m_name));
        result.append("</name>");

        result.append("<description>");
        result.append(EditUtil.encodeXmlEntities(m_description));
        result.append("</description>");

        result.append("<languages>");

        // fix for GBS-1693
        SortUtil.sort(m_languages, new LanguageComparator(Locale.getDefault()));
        for (int i = 0, max = m_languages.size(); i < max; i++)
        {
            Language lang = (Language) m_languages.get(i);

            result.append(lang.asXML());
        }
        result.append("</languages>");

        result.append("<fields>");

        // fix for GBS-1693
        SortUtil.sort(m_fields, new FieldComparator(Locale.getDefault()));
        for (int i = 0, max = m_fields.size(); i < max; i++)
        {
            Field field = (Field) m_fields.get(i);

            result.append(field.asXML());
        }
        result.append("</fields>");

        result.append("<indexes>");
        for (int i = 0, max = m_indexes.size(); i < max; i++)
        {
            Index index = (Index) m_indexes.get(i);

            result.append(index.asXML());
        }
        result.append("</indexes>");

        result.append("</definition>");

        return result.toString();
    }

    /**
     * <p>
     * Validates a definition (which is valid according to the DTD) to ensure it
     * is logically correct and usable by GlobalSight.
     * </p>
     * 
     * Tests:
     * <ul>
     * <li>Languages must have a non-empty name and locale</li>
     * <li>Fields must have a non-empty name and type</li>
     * <li>Indexes that are empty or refer to non-existing languages are
     * removed.</li>
     * </ul>
     * 
     * <p>
     * This method should not trust incoming data and be very strict about the
     * rules. Note that null checks are performed in init().
     * </p>
     */
    public void validate() throws TermbaseException
    {
        // Validate languages.
        for (Iterator it = m_languages.iterator(); it.hasNext();)
        {
            Language lang = (Language) it.next();

            // Languages must have a non-empty name and locale >= 2
            if (lang.getName().length() == 0 || lang.getLocale().length() < 2)
            {
                it.remove();
                continue;
            }
        }

        // Validate custom fields.
        for (Iterator it = m_fields.iterator(); it.hasNext();)
        {
            Field field = (Field) it.next();

            // Fields must have a non-empty name and type
            if (field.getName().length() == 0 || field.getType().length() == 0)
            {
                it.remove();
                continue;
            }
        }

        // Validate indexes.
        for (Iterator it = m_indexes.iterator(); it.hasNext();)
        {
            Index index = (Index) it.next();

            // Indexes must refer to existing languages (note: the
            // empty language denotes the concept full text index).
            String lang = index.getLanguageName();
            if (lang.length() > 0 && getLanguage(lang) == null)
            {
                it.remove();
                continue;
            }

            // Index type must be one that we know
            String type = index.getType();
            if (!type.equals(Index.TYPE_FUZZY)
                    && !type.equals(Index.TYPE_FULLTEXT))
            {
                it.remove();
                continue;
            }

            // Index locale must be >= 2
            String locale = index.getLocale();
            if (locale.length() < 2)
            {
                it.remove();
                continue;
            }
        }
    }

    //
    // Private Methods
    //

    /**
     * Reads and validates a Termbase Definition given in an XML string and
     * overwrites any name in the Definition with the given name.
     */
    private void init(String p_name, String p_definition)
            throws TermbaseException
    {
        init(p_definition);
        m_name = p_name;
    }

    /**
     * Reads and validates a Termbase Definition given in an XML string.
     */
    private void init(String p_definition) throws TermbaseException
    {
        XmlParser parser = null;
        Document dom;

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_definition);
        }
        finally
        {
            XmlParser.fire(parser);
        }

        try
        {
            Element root = dom.getRootElement();
            Node nameNode = root.selectSingleNode("/definition/name");
            Node descNode = root.selectSingleNode("/definition/description");

            if (nameNode != null)
            {
                m_name = nameNode.getText();
            }

            if (descNode != null)
            {
                m_description = descNode.getText();
            }

            List langs = root.selectNodes("/definition/languages/language");

            if (langs.size() == 0)
            {
                error("no languages defined", null);
            }

            for (int i = 0, max = langs.size(); i < max; ++i)
            {
                Element lang = (Element) langs.get(i);

                String name = lang.valueOf("name");
                String locale = lang.valueOf("locale");
                String hasTerms = lang.valueOf("hasterms");

                if (name == null || locale == null || hasTerms == null)
                {
                    error("incomplete language definition", null);
                }

                m_languages.add(new Language(name, locale, hasTerms));
            }

            List fields = root.selectNodes("/definition/fields/field");

            for (int i = 0, max = fields.size(); i < max; ++i)
            {
                Element field = (Element) fields.get(i);

                String name = field.valueOf("name");
                String type = field.valueOf("type");
                String system = field.valueOf("system");
                String indexed = field.valueOf("indexed");
                String values = field.valueOf("values");

                if (name == null || type == null || system == null
                        || indexed == null || values == null)
                {
                    error("incomplete field definition", null);
                }

                m_fields.add(new Field(name, type, system, indexed, values));
            }

            List indexes = root.selectNodes("/definition/indexes/index");

            for (int i = 0, max = indexes.size(); i < max; ++i)
            {
                Element index = (Element) indexes.get(i);

                String languageName = index.valueOf("languagename");
                String locale = index.valueOf("locale");
                String type = index.valueOf("type");

                if (languageName == null || locale == null || type == null)
                {
                    error("incomplete index definition", null);
                }

                m_indexes.add(new Index(languageName, locale, type));
            }
        }
        catch (TermbaseException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }

    /**
     * Throws an INVALID_DEFINITION exception.
     */
    private void error(String p_reason, Exception p_exception)
            throws TermbaseException
    {
        String[] args =
        { p_reason };

        throw new TermbaseException(MSG_INVALID_DEFINITION, args, p_exception);
    }

    /**
     * Returns a default termbase definition for a new termbase. The definition
     * is read from the property file "/Terminology.properties". This method is
     * package-private; call it through ITermbaseManager.
     */
    static protected String getDefaultDefinition()
    {
        try
        {
            ResourceBundle res = ResourceBundle.getBundle(s_propertyFile,
                    Locale.US);

            String result = res.getString(s_definitionKey);

            // Quickly check for typos
            XmlParser parser = null;

            try
            {
                parser = XmlParser.hire();
                Document dom = parser.parseXml(result);
            }
            finally
            {
                XmlParser.fire(parser);
            }

            return result;
        }
        catch (TermbaseException e)
        {
            CATEGORY.warn("The default termbase definition in file "
                    + s_propertyFile + " is not valid XML.");

            return s_defaultDefinition;
        }
        catch (MissingResourceException e)
        {
            // Missing resource or missing key; ignore.
            return s_defaultDefinition;
        }
    }

    /**
     * Used for Language sort. Fix for GBS-1693
     * 
     * @author leon
     * 
     */
    public final class LanguageComparator extends StringComparator
    {
        /**
         * Creates a LanguageComparator with the given locale.
         */
        public LanguageComparator(Locale p_locale)
        {
            super(p_locale);
        }

        /**
         * Performs a comparison of two Language objects.
         */
        public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
            Language a = (Language) p_A;
            Language b = (Language) p_B;

            String aValue;
            String bValue;
            int rv;

            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);

            return rv;
        }
    }

    /**
     * Used for Field sort. Fix for GBS-1693
     * 
     * @author leon
     * 
     */
    public final class FieldComparator extends StringComparator
    {
        /**
         * Creates a FieldComparator with the given locale.
         */
        public FieldComparator(Locale p_locale)
        {
            super(p_locale);
        }

        /**
         * Performs a comparison of two Filed objects.
         */
        public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
            Field a = (Field) p_A;
            Field b = (Field) p_B;

            String aValue;
            String bValue;
            int rv;

            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);

            return rv;
        }

    }
}
