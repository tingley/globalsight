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

/**
 * This class is used to show the mapping relationship for font and locale
 */
public class FontMapping
{
    private String sourceLocale;
    private String targetLocale;
    private String sourceFont;
    private String targetFont;
    private boolean isDefault = false;

    public String getSourceLocale()
    {
        return sourceLocale;
    }

    public void setSourceLocale(String sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public String getSourceFont()
    {
        return sourceFont;
    }

    public void setSourceFont(String sourceFont)
    {
        this.sourceFont = sourceFont;
    }

    public String getTargetFont()
    {
        return targetFont;
    }

    public void setTargetFont(String targetFont)
    {
        this.targetFont = targetFont;
    }

    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    public boolean isDefault()
    {
        return isDefault;
    }

    public String getFontByLocale(String locale)
    {
        if (locale.equalsIgnoreCase(sourceLocale))
        {
            return sourceFont;
        }

        if (locale.equalsIgnoreCase(targetLocale))
        {
            return targetFont;
        }

        return null;
    }

    public String toString()
    {
        return sourceLocale + "|" + targetLocale + "=" + sourceFont + "|" + targetFont;
    }

    public String getKey()
    {
        return "FontPair-" + sourceLocale + "|" + targetLocale;
    }

    public String getValue()
    {
        return sourceFont + "|" + targetFont;
    }

    /**
     * Check if this font mapping accepts the target locale
     * 
     * @param p_targetLocale
     * @return
     */
    public boolean accept(String p_targetLocale)
    {
        if (p_targetLocale != null && p_targetLocale.equalsIgnoreCase(targetLocale))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if this font mapping accepts this source font and target locale
     * 
     * @param p_sourceFont
     * @param p_targetLocale
     * @return
     */
    public boolean accept(String p_sourceFont, String p_targetLocale)
    {
        if (p_sourceFont != null && p_sourceFont.equalsIgnoreCase(sourceFont)
                && p_targetLocale != null && p_targetLocale.equalsIgnoreCase(targetLocale))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if this font mapping accepts this locale pair
     * 
     * @param p_sourceLocale
     * @param p_targetLocale
     * @return
     */
    public boolean acceptLocalePair(String p_sourceLocale, String p_targetLocale)
    {
        if (p_sourceLocale != null && p_sourceLocale.equalsIgnoreCase(sourceLocale)
                && p_targetLocale != null && p_targetLocale.equalsIgnoreCase(targetLocale))
        {
            return true;
        }

        return false;
    }
}
