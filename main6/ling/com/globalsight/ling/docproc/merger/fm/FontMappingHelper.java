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
package com.globalsight.ling.docproc.merger.fm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * This helper class to resolve the mapping relationship for font and locale
 */
public class FontMappingHelper
{
    private static final Object lock = new Object();
    private static List<FontMapping> fontMappingList = null;
    private static String ignoreKeys = "=maxTimeToWait=";

    static
    {
        init();
    }
    
    protected static String getPropertiesFile()
    {
        return "/properties/AdobeAdapter.properties";
    }
    
    protected static Logger getLogger()
    {
        return Logger.getLogger(FontMappingHelper.class);
    }

    /**
     * Get the default mapping font for target locale
     * 
     * @param targetLocale
     * @return the font
     */
    public static String getDefaultMappingFont(String targetLocale)
    {
        String targetFont = null;

        if (fontMappingList == null)
        {
            init();
        }

        for (FontMapping fm : fontMappingList)
        {
            if (fm.isDefault() && fm.accept(targetLocale))
            {
                targetFont = fm.getTargetFont();
                break;
            }
        }

        return targetFont;
    }

    /**
     * Get the list of FontMapping
     * 
     * @return
     */
    public static List<FontMapping> getFontMappingList()
    {
        return fontMappingList;
    }

    /**
     * Get the mapping font for source / target locale
     * 
     * @param sourceFont
     * @param targetLocale
     * @return a font or null if not found
     */
    public static String getMappingFont(String sourceFont, String targetLocale)
    {
        String targetFont = null;

        if (fontMappingList == null)
        {
            init();
        }

        for (FontMapping fp : fontMappingList)
        {
            if (fp.accept(sourceFont, targetLocale))
            {
                targetFont = fp.getFontByLocale(targetLocale);
                break;
            }
        }

        return targetFont;
    }

    /**
     * Check if there are font mapping for this locale
     * 
     * @param targetLocale
     * @return true if there are font mapping for this locale
     */
    public static boolean isLocaleWithFonts(String targetLocale)
    {
        if (fontMappingList == null)
        {
            init();
        }

        for (FontMapping fp : fontMappingList)
        {
            if (fp.accept(targetLocale))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isInddXml(String p_format, String p_content)
    {
        if ("xml".equalsIgnoreCase(p_format) && p_content != null
                && p_content.contains("</Inddgsstory>") && p_content.contains("</Root>")
                && p_content.contains("InddFontFamily="))
        {
            return true;
        }

        return false;
    }

    public static String processInddXml(String p_targetLocale, String p_content)
    {
        if (p_targetLocale == null || p_content == null)
        {
            return p_content;
        }

        if (!isLocaleWithFonts(p_targetLocale))
        {
            return p_content;
        }

        String defaultFont = getDefaultMappingFont(p_targetLocale);
        Pattern inddTagPattern = Pattern.compile("<Indd([^>]+)>");
        Matcher m = inddTagPattern.matcher(p_content);

        while (m.find())
        {
            String oriTag = m.group();
            String tag = m.group();
            String newTag = null;
            // handle InddFontFamily="MingLiU"
            if (tag.contains("InddFontFamily=\""))
            {
                StringIndex si = StringIndex.getValueBetween(new StringBuffer(tag), 0,
                        "InddFontFamily=\"", "\"");
                String oriFont = si.value;

                String mappingFont = getMappingFont(oriFont, p_targetLocale);
                if (mappingFont != null)
                {
                    newTag = tag.replace(oriFont, mappingFont);
                }
                else if (defaultFont != null)
                {
                    newTag = tag.replace(oriFont, defaultFont);
                }
            }

            if (newTag != null)
            {
                p_content = p_content.replace(oriTag, newTag);
            }
        }

        // handle [Bold-10-Helvetica]source: [/Bold-10-Helvetica]
        Pattern fontTagPattern = Pattern
                .compile("(\\[[^\\]/-]+-[\\d\\.]+-)([^\\]]+)(\\].*?\\[/[^\\]-]+-[\\d\\.]+-)([^\\]]+)(\\])", Pattern.DOTALL);
        Matcher fontTagM = fontTagPattern.matcher(p_content);
        while (fontTagM.find())
        {
            String oriContent = fontTagM.group();
            String g1 = fontTagM.group(1);
            String g3 = fontTagM.group(3);
            String g5 = fontTagM.group(5);
            String oriFont = fontTagM.group(2);
            String newContent = null;

            String mappingFont = getMappingFont(oriFont, p_targetLocale);
            if (mappingFont != null)
            {
                newContent = g1 + mappingFont + g3 + mappingFont + g5;
            }
            else if (defaultFont != null)
            {
                newContent = g1 + defaultFont + g3 + defaultFont + g5;
            }

            if (newContent != null)
            {
                p_content = p_content.replace(oriContent, newContent);
            }
        }

        return p_content;
    }

    /**
     * Init font pair
     */
    private static void init()
    {
        synchronized (lock)
        {
            if (fontMappingList != null)
            {
                return;
            }

            try
            {
                fontMappingList = new ArrayList<FontMapping>();
                Properties properties = new Properties();

                // load root properties first
                properties.load(FontMappingHelper.class.getResourceAsStream(getPropertiesFile()));

                for (Enumeration e = properties.keys(); e.hasMoreElements();)
                {
                    String key = (String) e.nextElement();
                    if (!ignoreKeys.contains(key))
                    {
                        String value = properties.getProperty(key);
                        if (key.contains("default"))
                        {
                            FontMapping fm = FontMappingParser.parseOne(key, value);
                            if (null != fm)
                            {
                                fontMappingList.add(fm);
                            }
                        }
                        else
                        {
                            List<FontMapping> fms = FontMappingParser.parse(key, value);
                            fontMappingList.addAll(fms);
                        }
                    }
                }
            }
            catch (Throwable ex)
            {
                getLogger().error("Failed to load font mapping from file: " + getPropertiesFile(), ex);
            }
        }
    }

    protected static void initForDebug(List<FontMapping> fms)
    {
        fontMappingList = fms;
    }
}
