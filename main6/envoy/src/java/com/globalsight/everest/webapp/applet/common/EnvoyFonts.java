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
package com.globalsight.everest.webapp.applet.common;

import java.awt.Font;

/**
 * The EnvoyFonts class is accessed statically to get fonts
 * for the applets to handle both european and japanese UIs
 */
public class EnvoyFonts
{
    static private Boolean s_useArial = null;

    //Arial fonts (for European UIs)
    public static final Font s_headerFont =
        new Font(EnvoyAppletConstants.ARIAL_FONT_NAME, Font.BOLD, 12);
    public static final Font s_titleFont =
        new Font(EnvoyAppletConstants.ARIAL_FONT_NAME, Font.BOLD, 18);
    public static final Font s_cellFont =
        new Font(EnvoyAppletConstants.ARIAL_FONT_NAME, Font.PLAIN, 12);
    public static final Font s_dialogFont =
        new Font(EnvoyAppletConstants.ARIAL_FONT_NAME, Font.PLAIN, 12);

    //ArialUnicodeMS fonts (for Asian UIs)
    public static final Font s_headerFontUnicode =
        new Font(EnvoyAppletConstants.ARIAL_UNICODE_FONT_NAME, Font.BOLD, 12);
    public static final Font s_titleFontUnicode =
        new Font(EnvoyAppletConstants.ARIAL_UNICODE_FONT_NAME, Font.BOLD, 18);
    public static final Font s_cellFontUnicode =
        new Font(EnvoyAppletConstants.ARIAL_UNICODE_FONT_NAME, Font.PLAIN, 12);
    public static final Font s_dialogFontUnicode =
        new Font(EnvoyAppletConstants.ARIAL_UNICODE_FONT_NAME, Font.PLAIN, 12);

    static public void setUILocale(String p_uiLocaleName)
    {
        s_useArial = isArialUseable(p_uiLocaleName);
    }
    
    static private boolean isArialUseable(String locale)
    {
        boolean isUnicodeLocale = (locale.startsWith("ja") || locale.startsWith("zh"));
        
        return !isUnicodeLocale;
    }
    
    static public boolean useArial()
    {
        if (s_useArial == null)
        {
            String locale = GlobalEnvoy.getLocale().toString();
            System.out.println("locale: " + locale);
            s_useArial = isArialUseable(locale);
        }
        
        return s_useArial.booleanValue();
    }

    static public Font getHeaderFont()
    {
        return useArial() ? s_headerFont : s_headerFontUnicode;
    }

    static public Font getTitleFont()
    {
        return useArial() ? s_titleFont : s_titleFontUnicode;
    }

    static public Font getCellFont()
    {
        return useArial() ? s_cellFont : s_cellFontUnicode;
    }

    static public Font getDialogFont()
    {
        return useArial() ? s_dialogFont : s_dialogFontUnicode;
    }
}
