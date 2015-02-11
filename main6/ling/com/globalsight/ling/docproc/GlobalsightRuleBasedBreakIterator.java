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
package com.globalsight.ling.docproc;

import com.ibm.text.RuleBasedBreakIterator;
import java.text.CharacterIterator;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * This class needs to be documented.
 */
public class GlobalsightRuleBasedBreakIterator
{
    private static final String RULE_FILE_NAME =
        "com.globalsight.ling.docproc.BreakIteratorRules";

    public static final String SENTENCE_BREAK = "SentenceBreakRules";
    public static final String CHARACTER_BREAK = "CharacterBreakRules";
    public static final String WORD_BREAK = "WordBreakRules";
    public static final String LINE_BREAK = "LineBreakRules";

    public static final int SENTENCE_BREAK_NUM = 1;
    public static final int CHARACTER_BREAK_NUM = 2;
    public static final int WORD_BREAK_NUM = 3;
    public static final int LINE_BREAK_NUM = 4;

    private static final int CHARACTER_INDEX = 0;
    private static final int WORD_INDEX = 1;
    private static final int LINE_INDEX = 2;
    private static final int SENTENCE_INDEX = 3;

    // Use ICU or Java break iterators? Default is Java, for backwards
    // compatibility.
    private static boolean s_useICU = false;

    static {
        try
        {
            String value;

            ResourceBundle res =
                ResourceBundle.getBundle("properties/Diplomat", Locale.US);

            try
            {
                value = res.getString("segmentation_use_icu");
                if (value != null && value.equalsIgnoreCase("true"))
                {
                    s_useICU = true;
                }
            }
            catch (MissingResourceException e) {}
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    /**
     * Static class, no constructor.
     */
    private GlobalsightRuleBasedBreakIterator()
    {
    }

    private static String getRules(String p_iteratorType, Locale p_locale)
    {
        if (p_iteratorType == null || p_iteratorType.length() <= 0)
        {
            return "";
        }

        return ResourceBundle.getBundle(RULE_FILE_NAME, p_locale).
            getString(p_iteratorType);
    }

    /**
     * Returns a new instance of BreakIterator that locates word
     * boundaries.  This function assumes that the text being analyzed
     * is in the default locale's language.
     * @return An instance of BreakIterator that locates word boundaries.
     */
    public static GlobalsightBreakIterator getWordInstance()
    {
        return getWordInstance(Locale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates word
     * boundaries.
     * @param locale A locale specifying the language of the text to be
     * analyzed.
     * @return An instance of BreakIterator that locates word boundaries.
     */
    public static GlobalsightBreakIterator getWordInstance(Locale p_locale)
    {
        return getBreakInstance(p_locale, WORD_INDEX, WORD_BREAK);
    }

    /**
     * Returns a new instance of BreakIterator that locates legal line-
     * wrapping positions.  This function assumes the text being broken
     * is in the default locale's language.
     * @return A new instance of BreakIterator that locates legal line-
     * wrapping positions.
     */
    public static GlobalsightBreakIterator getLineInstance()
    {
        return getLineInstance(Locale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates legal line-
     * wrapping positions.
     * @param locale A Locale specifying the language of the text being broken.
     * @return A new instance of BreakIterator that locates legal
     * line-wrapping positions.
     */
    public static GlobalsightBreakIterator getLineInstance(Locale p_locale)
    {
        return getBreakInstance(p_locale, LINE_INDEX, LINE_BREAK);
    }

    /**
     * Returns a new instance of BreakIterator that locates
     * logical-character boundaries.  This function assumes that the text
     * being analyzed is in the default locale's language.
     * @return A new instance of BreakIterator that locates
     * logical-character boundaries.
     */
    public static GlobalsightBreakIterator getCharacterInstance()
    {
        return getCharacterInstance(Locale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates
     * logical-character boundaries.
     * @param locale A Locale specifying the language of the text being
     * analyzed.
     * @return A new instance of BreakIterator that locates
     * logical-character boundaries.
     */
    public static GlobalsightBreakIterator getCharacterInstance(Locale p_locale)
    {
        return getBreakInstance(p_locale, CHARACTER_INDEX, CHARACTER_BREAK);
    }

    /**
     * Returns a new instance of BreakIterator that locates sentence
     * boundaries.  This function assumes the text being analyzed is in
     * the default locale's language.
     * @return A new instance of BreakIterator that locates sentence
     * boundaries.
     */
    public static GlobalsightBreakIterator getSentenceInstance()
    {
        return getSentenceInstance(Locale.getDefault());
    }

    /**
     * Returns a new instance of BreakIterator that locates sentence
     * boundaries.
     * @param locale A Locale specifying the language of the text being
     * analyzed.
     * @return A new instance of BreakIterator that locates sentence
     * boundaries.
     */
    public static GlobalsightBreakIterator getSentenceInstance(Locale p_locale)
    {
        return getBreakInstance(p_locale, SENTENCE_INDEX, SENTENCE_BREAK);
    }

    private static GlobalsightBreakIterator getBreakInstance(Locale p_locale,
        int p_type, String p_rulesName)
    {
        GlobalsightBreakIterator result =
            createBreakInstance(p_locale, p_rulesName);

        return result;
    }

    private static GlobalsightBreakIterator createBreakInstance(
        Locale p_locale, String p_rulesName)
    {
        GlobalsightBreakIterator result = null;

        // try to find a rule file with the right rule labels
        try
        {
            result = new GlobalsightBreakIterator(
                new RuleBasedBreakIterator(getRules(p_rulesName, p_locale)));
        }
        catch (MissingResourceException e)
        {
            switch (getRuleNumber(p_rulesName))
            {
            case SENTENCE_BREAK_NUM:
                if (s_useICU)
                {
                    result = new GlobalsightBreakIterator(
                        (com.ibm.text.RuleBasedBreakIterator)
                        RuleBasedBreakIterator.getSentenceInstance(p_locale));
                }
                else
                {
                    result = new GlobalsightBreakIterator(
                        BreakIterator.getSentenceInstance(p_locale));
                }
                break;
            case CHARACTER_BREAK_NUM:
                if (s_useICU)
                {
                    result = new GlobalsightBreakIterator(
                        (com.ibm.text.RuleBasedBreakIterator)
                        RuleBasedBreakIterator.getCharacterInstance(p_locale));
                }
                else
                {
                    result = new GlobalsightBreakIterator(
                        BreakIterator.getCharacterInstance(p_locale));
                }
                break;
            case WORD_BREAK_NUM:
                if (s_useICU)
                {
                    result = new GlobalsightBreakIterator(
                        (com.ibm.text.RuleBasedBreakIterator)
                        RuleBasedBreakIterator.getWordInstance(p_locale));
                }
                else
                {
                    result = new GlobalsightBreakIterator(
                        BreakIterator.getWordInstance(p_locale));
                }
                break;
            case LINE_BREAK_NUM:
                if (s_useICU)
                {
                    result = new GlobalsightBreakIterator(
                        (com.ibm.text.RuleBasedBreakIterator)
                        RuleBasedBreakIterator.getLineInstance(p_locale));
                }
                else
                {
                    result = new GlobalsightBreakIterator(
                        BreakIterator.getLineInstance(p_locale));
                }
                break;
            default:
                break;
            }
        }

        return result;
    }

    private static int getRuleNumber(String p_ruleName)
    {
        if (p_ruleName.equals(SENTENCE_BREAK))
        {
            return SENTENCE_BREAK_NUM;
        }
        else if (p_ruleName.equals(CHARACTER_BREAK))
        {
            return CHARACTER_BREAK_NUM;
        }
        else if (p_ruleName.equals(WORD_BREAK))
        {
            return WORD_BREAK_NUM;
        }
        else if (p_ruleName.equals(LINE_BREAK))
        {
            return LINE_BREAK_NUM;
        }

        return -1;
    }
}
