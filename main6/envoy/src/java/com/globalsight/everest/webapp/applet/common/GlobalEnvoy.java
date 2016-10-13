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

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Color;
import java.awt.TextField;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Vector;
import java.util.Date;

/**
 * GlobalEnvoy is a global class with static methods that are
 * used by all client classes.
 */

public class GlobalEnvoy
{
    // The current locale (we need the locale for word wrapping purposes).
    private static Locale m_locale = null;
    // the parent component (the envoy's main frame)
    private static Component m_component = null;

    //
    // Helper Methods
    //

    /**
     * Set the current locale to be the specified locale.
     * @param p_locale - The locale to be set.
     */
    public static void setLocale(Locale p_locale)
    {
        m_locale = p_locale;
    }

    /**
     * Get the current locale for formatting purposes.
     * @return The current locale.
     */
    public static Locale getLocale()
    {
        return m_locale == null ? (m_locale = Locale.getDefault()) : m_locale;
    }

    /**
     * Set the parent component to be the specified component.
     * @param p_component - The component to be set.
     */
    public static void setParentComponent(Component p_component)
    {
        m_component = p_component;
    }

    /**
     * Get the parent component.
     * @return The parent component.
     */
    public static Component getParentComponent()
    {
        return m_component;
    }


    /**
     * Get the width of the string (label's string).  This method is
     * used for setting the width of a label or a button based on the
     * string's width.
     * @param label - The string used for width calculation.
     * @return The width of the string.
     */
    public static int getStringWidth(String p_label)
    {
        FontMetrics fontMetrics = getFontMetrics();
        return getStringWidth(fontMetrics, p_label);
    }


    /**
     * Calculate the required width for displaying the labels.  The
     * labels should have the same fixed length for alignment and this
     * method will go through the list of all labels and set the
     * result width based on the longest label.
     * @param stringList - a list of strings for width calculation.
     * @return The string's width.
     */
    public static int getStringWidth(Vector stringList)
    {
        int result = 0;
        FontMetrics fontMetrics = getFontMetrics();

        // for (int i = stringList.size(); --i>=0;)
        for (int i=0; i<stringList.size(); i++)
        {
            // get the string width from the global class
            int width = getStringWidth(fontMetrics,
                (String)stringList.elementAt(i));

            if (width > result)
            {
                result = width;
            }
        }

        return result;
    }


    /**
     * Get the string representation of a date object based on the locale.
     * @param p_date The date to be displayed.
     * @return The string representation of a date.
     */
    public static String getDisplayDate(Date p_date)
    {
        DateFormat dateFormatter =
            DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale());

        String date = dateFormatter.format(p_date);

        return date;
    }

    /**
     * Determines whether the textfield contains valid text.  The only
     * valid characters for a name can be alphanumeric characters 
     * including space), hyphen ('-') and underscore ('_').
     */
    public static boolean hasValidText(TextField p_textField)
    {
        String textValue = p_textField.getText();
        int length = textValue == null ? -1 : textValue.length();
        
        boolean isValidChar = true;

        for (int i = 0; isValidChar && i < length; i++)
        {
            char charValue = textValue.charAt(i);
            isValidChar = Character.isLetterOrDigit(charValue) || 
                (charValue == '-' || 
                 charValue == '_' || 
                 charValue == ' ');

            if (!isValidChar)
            {
                p_textField.select(i, i+1);
                p_textField.requestFocus();
            }
        }

        return isValidChar;
    }

    //
    // Private Methods
    //

    // get the string width of a particular string with a fontmetrics
    private static int getStringWidth(FontMetrics p_fontMetrics, String p_label)
    {
        return p_fontMetrics == null ? 70 : p_fontMetrics.stringWidth(p_label);
    }

    // get the FontMetrics object that should be used for string width
    // measurement
    private static FontMetrics getFontMetrics()
    {
        FontMetrics fontMetrics = null;
        if (m_component != null)
        {
            fontMetrics = m_component.getGraphics() == null ?
                m_component.getFontMetrics(new Font("Courier", Font.PLAIN, 12)) :
                m_component.getGraphics().getFontMetrics();
        }

        return fontMetrics;
    }
}
