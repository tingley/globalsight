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
package com.globalsight.ling.docproc.extractor.html;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * Dynamic extraction rules that overwrite static rules loaded from property
 * files (Tags, Styles). The MS-Office Extractors subclass this class and
 * initialize it with appropriate defaults for Office files converted to HTML.
 */
public class DynamicRules
{
    private static final Logger CATEGORY = Logger.getLogger(DynamicRules.class);
    //
    // Protected Members - overwritten in subclasses. Since an
    // instance of this class is always used by ExtractionRules, all
    // rules are set to defaults for any old HTML file.
    //

    // HTML rules.

    /** Excel: do not extract scripts */
    protected boolean m_extractScripts = true;
    /** Excel: do not extract stylesheets */
    protected boolean m_extractStylesheets = false;
    /** ??? do not extract XML */
    protected boolean m_extractXml = false;

    /** Excel: do not extract segments consisting of only numbers */
    protected boolean m_extractNumbers = false;

    /** All: do not extract meta charset */
    protected boolean m_extractCharset = false;

    /** All: disables/enables Tags.properties localizable attributes */
    private final HashMap m_localizableAttributes = new HashMap();

    /** All: disables/enables Styles.properties styles */
    private final HashMap m_extractableStyles = new HashMap();

    /** All: disables/enables Styles.properties prefix list */
    private final HashMap m_extractableStylePrefixes = new HashMap();

    /** Word: marks paragraph style names as unextractable. */
    protected final HashMap m_unextractableWordParaStyles = new HashMap();

    /** Word: marks character style names as unextractable. */
    protected final HashMap m_unextractableWordCharStyles = new HashMap();

    /** Word: marks character style names as internal text. */
    protected final HashMap m_selectedInternalTextStyles = new HashMap();

    /** Excel: marks cell style names as unextractable. */
    protected final HashMap m_unextractableExcelCellStyles = new HashMap();

    //
    // Constructor
    //

    public DynamicRules()
    {
    }

    public final boolean doExtractScripts()
    {
        return m_extractScripts;
    }

    public final boolean doExtractStylesheets()
    {
        return m_extractStylesheets;
    }

    public final boolean doExtractXml()
    {
        return m_extractXml;
    }

    public final boolean doExtractNumbers()
    {
        return m_extractNumbers;
    }

    public final boolean doExtractCharset()
    {
        return m_extractCharset;
    }

    public boolean isExtractCharset()
    {
        return m_extractCharset;
    }

    public void setExtractCharset(boolean charset)
    {
        m_extractCharset = charset;
    }

    public final boolean canLocalizeAttribute(String p_tag, String p_attr)
    {
        String key = p_tag + "." + p_attr;
        key = key.toLowerCase();

        Boolean b = (Boolean) m_localizableAttributes.get(key);

        if (b != null)
        {
            return b.booleanValue();
        }

        return true;
    }

    protected final void enableLocalizableAttribute(String p_tag, String p_attr)
    {
        String key = p_tag + "." + p_attr;
        key = key.toLowerCase();

        m_localizableAttributes.put(key, Boolean.TRUE);
    }

    protected final void disableLocalizableAttribute(String p_tag, String p_attr)
    {
        String key = p_tag + "." + p_attr;
        key = key.toLowerCase();

        m_localizableAttributes.put(key, Boolean.FALSE);
    }

    public final boolean canExtractStyle(String p_style)
    {
        String key = p_style.toLowerCase();

        Boolean b = (Boolean) m_extractableStyles.get(key);

        if (b != null)
        {
            return b.booleanValue();
        }

        // Check the prefix defaults
        if (!m_extractableStylePrefixes.isEmpty())
        {
            for (Iterator it = m_extractableStylePrefixes.keySet().iterator(); it
                    .hasNext();)
            {
                String prefix = (String) it.next();

                if (key.startsWith(prefix))
                {
                    return ((Boolean) m_extractableStylePrefixes.get(prefix))
                            .booleanValue();
                }
            }
        }

        return true;
    }

    public final boolean canExtractWordParaStyle(String p_style)
    {
        String key = normalizeWordStyle(p_style);

        Boolean b = (Boolean) m_unextractableWordParaStyles.get(key);

        if (b != null)
        {
            return b.booleanValue();
        }

        return true;
    }

    public final boolean canExtractWordCharStyle(String p_style)
    {
        String key = normalizeWordStyle(p_style);

        Boolean b = (Boolean) m_unextractableWordCharStyles.get(key);

        if (b != null)
        {
            return b.booleanValue();
        }

        return true;
    }

    public final boolean isInternalTextCharStyle(String p_style)
    {
        String key = normalizeWordStyle(p_style);

        Boolean b = (Boolean) m_selectedInternalTextStyles.get(key);

        if (b != null)
        {
            return b.booleanValue();
        }

        return false;
    }

    public final boolean canExtractExcelCellStyle(String p_style)
    {
        Boolean b = (Boolean) m_unextractableExcelCellStyles.get(p_style);

        if (b != null)
        {
            return b.booleanValue();
        }

        return true;
    }

    protected final void enableStyle(String p_style)
    {
        String key = p_style.toLowerCase();

        m_extractableStyles.put(key, Boolean.TRUE);
    }

    protected final void disableStyle(String p_style)
    {
        String key = p_style.toLowerCase();

        m_extractableStyles.put(key, Boolean.FALSE);
    }

    protected final void enableStylePrefix(String p_prefix)
    {
        String key = p_prefix.toLowerCase();

        m_extractableStylePrefixes.put(key, Boolean.TRUE);
    }

    protected final void disableStylePrefix(String p_prefix)
    {
        String key = p_prefix.toLowerCase();

        m_extractableStylePrefixes.put(key, Boolean.FALSE);
    }

    //
    // Support routines for loading from property files.
    //

    static protected final void fillBooleanMap(Properties props, String key,
            HashMap map)
    {
        String value = props.getProperty(key);

        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String tag = tok.nextToken().trim().toLowerCase();
            map.put(tag, Boolean.FALSE);
        }
    }

    static protected final void fillExcelCellMap(Properties props, String key,
            HashMap map)
    {
        String value = props.getProperty(key);

        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String style = tok.nextToken().trim();
            map.put(style, Boolean.FALSE);
        }
    }

    static protected final void fillBooleanMap(ResourceBundle res, String key,
            HashMap map)
    {
        String value = res.getString(key);

        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String tag = tok.nextToken().trim().toLowerCase();
            map.put(tag, Boolean.FALSE);
        }
    }

    static protected final void fillWordStyleMap(ResourceBundle res,
            String key, HashMap map)
    {
        String value = res.getString(key);

        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String style = tok.nextToken().trim();

            style = normalizeWordStyle(style);

            map.put(style, Boolean.FALSE);
        }
    }

    static protected final void fillExcelCellMap(ResourceBundle res,
            String key, HashMap map)
    {
        String value = res.getString(key);

        StringTokenizer tok = new StringTokenizer(value, ",");
        while (tok.hasMoreTokens())
        {
            String style = tok.nextToken().trim();
            map.put(style, Boolean.FALSE);
        }
    }

    /**
     * <p>
     * Word renames styles when saving to HTML to conform with HTML and CSS
     * syntax. Whitespace and special chars are stripped.
     * </p>
     * 
     * Example: PARA_DO_NOT_TRANSLATE -> PARADONOTTRANSLATE <br>
     * Example: PARA DO_NOT TRANSLATE -> PARADONOTTRANSLATE
     * 
     * <p>
     * Multiple styles mapping to the same normalized name get distinguished by
     * a number suffix, e.g. PARADONOTTRANSLATE0.
     * </p>
     */
    public static String normalizeWordStyle(String p_style)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_style.length(); i < max; i++)
        {
            char ch = p_style.charAt(i);

            if (Character.isLetterOrDigit(ch))
            {
                result.append(ch);
            }
        }

        return result.toString();
    }

    protected final void loadProperties(String p_filename)
    {
        try
        {
            String prefix = "/";
            String suffix = ".properties";
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append(p_filename).append(suffix);
            // ResourceBundle res =
            // ResourceBundle.getBundle(filename, Locale.US);
            // ResourceBundle reports error when finding property file with
            // space in the path. Use Properties instead.
            URL url = DynamicRules.class.getResource(sb.toString());
            if (url == null)
            {
                CATEGORY.warn("Property file " + p_filename
                        + " not found. Using property file " + p_filename);
                sb = new StringBuilder();
                sb.append(prefix).append(p_filename).append(suffix);
                url = DynamicRules.class.getResource(sb.toString());
            }
            Properties props = new Properties();
            props.load(new FileInputStream(url.toURI().getPath()));

            Enumeration<Object> keys = props.keys();
            while (keys.hasMoreElements())
            {
                String key = (String) keys.nextElement();
                String tmp = key.toLowerCase();

                if (tmp.equals("extractscripts"))
                {
                    m_extractScripts = !(props.getProperty(key)
                            .equalsIgnoreCase("false"));
                }
                else if (tmp.equals("extractstylesheets"))
                {
                    m_extractStylesheets = !(props.getProperty(key)
                            .equalsIgnoreCase("false"));
                }
                else if (tmp.equals("extractxml"))
                {
                    m_extractXml = !(props.getProperty(key)
                            .equalsIgnoreCase("false"));
                }
                else if (tmp.equals("extractcharset"))
                {
                    m_extractCharset = !(props.getProperty(key)
                            .equalsIgnoreCase("false"));
                }
                else if (tmp.equals("extractnumbers"))
                {
                    m_extractNumbers = !(props.getProperty(key)
                            .equalsIgnoreCase("false"));
                }
                else if (tmp.startsWith("dontextractlocalizableattributes"))
                {
                    fillBooleanMap(props, key, m_localizableAttributes);
                }
                else if (tmp.startsWith("dontextractstyles"))
                {
                    fillBooleanMap(props, key, m_extractableStyles);
                }
                else if (tmp.startsWith("dontextractstyleprefixes"))
                {
                    fillBooleanMap(props, key, m_extractableStylePrefixes);
                }/*
                  * read these properties from filter else if
                  * (tmp.equals("unextractablewordparagraphstyles")) {
                  * fillWordStyleMap(res, key, m_unextractableWordParaStyles); }
                  * else if (tmp.equals("unextractablewordcharacterstyles")) {
                  * fillWordStyleMap(res, key, m_unextractableWordCharStyles); }
                  */
                else if (tmp.equals("unextractableexcelcellstyles"))
                {
                    fillExcelCellMap(props, key, m_unextractableExcelCellStyles);
                }
            }
        }
        catch (MissingResourceException e)
        {
            System.err.println("DynamicRules initialization error:");
            e.printStackTrace();
            // Log an error with Logger class.
        }
        catch (Throwable e)
        {
            System.err.println("DynamicRules initialization error:");
            e.printStackTrace();
            // Log an error with Logger class.
        }
    }
}
