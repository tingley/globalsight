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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.FileUtil;

/**
 * This helper class to resolve the mapping relationship for font and locale
 */
public class IDMLFontMappingHelper
{
    private static String ignoreKeys = "=maxTimeToWait=";
    private static String propertiesFile = "/properties/IdmlAdapter.properties";
    private List<FontMapping> fontMappingList = null;

    protected static Logger getLogger()
    {
        return Logger.getLogger(IDMLFontMappingHelper.class);
    }

    /**
     * Get the default mapping font for target locale
     * 
     * @param targetLocale
     * @return the font
     */
    public String getDefaultMappingFont(String targetLocale)
    {
        String targetFont = null;

        if (fontMappingList == null)
        {
            fontMappingList = getCompanyFontMapping();
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
    public List<FontMapping> getFontMappingList()
    {
        return getCompanyFontMapping();
    }

    /**
     * Get the mapping font for source / target locale
     * 
     * @param sourceFont
     * @param targetLocale
     * @return a font or null if not found
     */
    public String getMappingFont(String sourceFont, String targetLocale)
    {
        String targetFont = null;

        if (fontMappingList == null)
        {
            fontMappingList = getCompanyFontMapping();
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
    public boolean isLocaleWithFonts(String targetLocale)
    {
        if (fontMappingList == null)
        {
            fontMappingList = getCompanyFontMapping();
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

    public void processIDMLFont(String zipDir, String targetLocale)
            throws IOException
    {
        if (!isLocaleWithFonts(targetLocale))
        {
            return;
        }

        // process fonts xml
        String fileName = "Fonts.xml";
        String fileDir = "Resources";
        String reAll = "<Font Self=([^>]+)>";
        String before = "FontFamily=\"";
        String after = "\"";
        String deleteRe = null;
        processOneFile(zipDir, targetLocale, fileName, fileDir, reAll, before,
                after, deleteRe);

        // process styles xml
        String stylefileName = "Styles.xml";
        String stylefileDir = "Resources";
        String stylereAll = "<AppliedFont type=\"string\">([^<>]+)</AppliedFont>";
        String stylebefore = "<AppliedFont type=\"string\">";
        String styleafter = "</AppliedFont>";
        String deleteREStyle = " FontStyle=\"([^>]+?)\"";
        processOneFile(zipDir, targetLocale, stylefileName, stylefileDir,
                stylereAll, stylebefore, styleafter, deleteREStyle);

        // process story files
        String storyDir = "Stories";
        String reAllStory = "<AppliedFont type=\"string\">([^<>]+)</AppliedFont>";
        String beforeStory = "<AppliedFont type=\"string\">";
        String afterStory = "</AppliedFont>";
        String deleteREStory = " FontStyle=\"([^>]+?)\"";
        File storyDirFile = new File(zipDir + File.separator + storyDir);
        File[] storyFiles = storyDirFile.listFiles(new StoryFilenameFilter());

        if (storyFiles != null && storyFiles.length > 0)
        {
            for (File file : storyFiles)
            {
                processOneFile(zipDir, targetLocale, file.getName(), storyDir,
                        reAllStory, beforeStory, afterStory, deleteREStory);
            }
        }
    }

    private void processOneFile(String zipDir, String targetLocale,
            String fileName, String fileDir, String reAll, String before,
            String after, String deleteRE) throws IOException
    {
        File backupFile = new File(zipDir + "." + fileDir + "." + fileName);
        File oriFontFile = new File(zipDir + File.separator + fileDir
                + File.separator + fileName);

        // backup file first
        if (!backupFile.exists())
        {
            if (oriFontFile.exists())
            {
                oriFontFile.renameTo(backupFile);
            }
        }

        if (backupFile.exists())
        {
            String content = FileUtil.readFile(backupFile, "UTF-8");

            if (!content.contains(before))
            {
                backupFile.renameTo(oriFontFile);
                return;
            }

            String defaultFont = getDefaultMappingFont(targetLocale);
            Pattern inddTagPattern = Pattern.compile(reAll);
            Matcher m = inddTagPattern.matcher(content);
            ArrayList<String> handled = new ArrayList<String>();

            while (m.find())
            {
                String oriTag = m.group();
                String tag = m.group();
                String newTag = null;
                if (handled.contains(oriTag))
                {
                    continue;
                }

                // handle FontFamily="Times"
                if (tag.contains(before))
                {
                    StringIndex si = StringIndex.getValueBetween(
                            new StringBuffer(tag), 0, before, after);
                    String oriFont = si.value;

                    String mappingFont = getMappingFont(oriFont, targetLocale);
                    if (mappingFont != null)
                    {
                        newTag = tag.replace(
                                getFontFamilyStr(oriFont, before, after),
                                getFontFamilyStr(mappingFont, before, after));
                    }
                    else if (defaultFont != null)
                    {
                        newTag = tag.replace(
                                getFontFamilyStr(oriFont, before, after),
                                getFontFamilyStr(defaultFont, before, after));
                    }
                }

                if (newTag != null)
                {
                    content = content.replace(oriTag, newTag);
                    handled.add(oriTag);
                }
            }

            // delete something from file
            if (deleteRE != null)
            {
                handled.clear();
                Pattern deletePattern = Pattern.compile(deleteRE);
                Matcher deleteM = deletePattern.matcher(content);
                while (deleteM.find())
                {
                    String oriText = deleteM.group();
                    String newText = "";

                    if (handled.contains(oriText))
                    {
                        continue;
                    }
                    else
                    {
                        content = content.replace(oriText, newText);
                        handled.add(oriText);
                    }
                }
            }

            FileUtil.writeFile(oriFontFile, content, "UTF-8");
        }
    }

    private static String getFontFamilyStr(String font, String before,
            String after)
    {
        return before + font + after;
    }

    public static void restoreIDMLFont(String zipDir)
    {
        // process fonts xml
        String fileName = "Fonts.xml";
        String fileDir = "Resources";
        restoreOneFile(zipDir, fileName, fileDir);
        
        // process fonts xml
        String stylefileName = "Styles.xml";
        String stylefileDir = "Resources";
        restoreOneFile(zipDir, stylefileName, stylefileDir);

        // handle story files
        String storyDir = "Stories";
        File storyDirFile = new File(zipDir + File.separator + storyDir);
        File[] storyFiles = storyDirFile.listFiles(new StoryFilenameFilter());

        if (storyFiles != null && storyFiles.length > 0)
        {
            for (File file : storyFiles)
            {
                restoreOneFile(zipDir, file.getName(), storyDir);
            }
        }
    }

    private static void restoreOneFile(String zipDir, String fileName,
            String fileDir)
    {
        File backupFile = new File(zipDir + "." + fileDir + "." + fileName);
        File oriFontFile = new File(zipDir + File.separator + fileDir
                + File.separator + fileName);

        if (backupFile.exists())
        {
            if (oriFontFile.exists())
            {
                oriFontFile.delete();
            }

            backupFile.renameTo(oriFontFile);
        }
    }

    /**
     * Init font pair
     */
    private List<FontMapping> getCompanyFontMapping()
    {
        if (fontMappingList != null)
        {
            return fontMappingList;
        }

        InputStream is = null;

        try
        {
            fontMappingList = new ArrayList<FontMapping>();
            Properties properties = new Properties();

            is = SystemConfiguration.getCompanyFileStream(propertiesFile);
            properties.load(is);

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
                        List<FontMapping> fms = FontMappingParser.parse(key,
                                value);
                        fontMappingList.addAll(fms);
                    }
                }
            }
        }
        catch (Throwable ex)
        {
            getLogger().error(
                    "Failed to load font mapping from file: " + propertiesFile,
                    ex);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                }
            }
        }

        return fontMappingList;
    }

    protected void initForDebug(List<FontMapping> fms)
    {
        fontMappingList = fms;
    }
}

class StoryFilenameFilter implements FilenameFilter
{
    @Override
    public boolean accept(File dir, String name)
    {
        if (name.startsWith("Story_") && name.endsWith(".xml"))
        {
            return true;
        }

        return false;
    }
}
