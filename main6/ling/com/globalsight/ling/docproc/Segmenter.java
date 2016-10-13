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

import java.text.CharacterIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

// JDK iterators
//import java.text.BreakIterator;

// ICU iterators
//import com.ibm.text.BreakIterator;
//import com.ibm.text.RuleBasedBreakIterator;

//
// Let's see how others do segmentation:
//

// SDLX.  Copyright © 2000 SDL International.
//
// By default, SDLX splits the source and translation files into
// segments based on these rules:
//
//  · paragraph end (that is, when encountering a carriage return or a
//    line feed character)
//
//  · cell end (when in a table)
//
//  · list item end (for HTML)
//
//  · sentence end (that is, a full stop followed by a space)
//
// The sentence end rule may cause problems, because often there are
// abbreviations and acronyms which do not constitute the end of the
// sentence.  However, it is possible to over-ride this rule.
//
// To over-ride the sentence end rule
//
//  1 Create a text file called nosplit.txt.
//
//  2 Each line of the file should contain one text item which does
//    not constitute the end of a sentence.
//
//  3 Save the file in the SDLX installation directory (by default,
//    this is C:\Program Files\sdlx ).
//
// A typical nosplit.txt file might look like this:
//
// etc.
// ltd.
// 1.
// 2.
// 3.
// 4.
// 5.
// 6.
// 7.
// 8.
// 9.
// 0.
// e.g.
// i.e.
// ...
//
// This prevents SDLX splitting the text on the four abbreviations,
// numbers, and ellipsis (provided it is composed of three dots, not
// the ellipsis character).

// ========================================================================

// Taken From: Translator's Workbench Help, (c) 1994-2000 by TRADOS GmbH, Stuttgart, Germany
//
// Default End Rules
//
// By default, Translator's Workbench regards the full stop,
// exclamation mark, question mark, tab character or colon as segment
// boundary markers or stop characters when the following conditions
// are met:
//
// · The stop character is preceded by at least one word. Words that
// precede the stop character are known as leading words. Source
// segments that consist of numbers only (for example in a table cell)
// are ignored, because numeric values do not generally change in
// translation.
//
// · The stop character is followed by at least one space, except in
// the case of the tab character. Spaces that occur after the stop
// character are known as trailing whitespaces.
//
// · A closing quotation mark (" or ) or parenthesis (")") may also
// occur in between the stop character and the trailing whitespace.
//
// Using the Full Stop (dot) in Abbreviations and Ordinal Numbers
//
// The full stop (dot) may be used to indicate the end of an
// abbreviation or ordinal number rather than the end of a
// sentence. For this reason, Translator's Workbench uses a particular
// set of rules to determine the textual function of the full stop
// whenever it is encountered. You can customise these rules by
// altering the settings and options in the Segmentation Rules tab
// (Setup dialog box). In the following instances, Translator's
// Workbench does not regard the dot as a stop character and therefore
// continues reading the source text:
//
// · The character before the full stop is an uppercase letter and is
// not part of a word (Leading Words is set to a minimum value of 1).
//
// · The character before the full stop is a number (the Number Stop
// option is not selected).
//
// · The string before the full stop is assumed to be an abbreviation
// (the Guessed Abbreviation option is not selected).
//
// · The string before the full stop is on the Workbench abbreviation
// list or a user-defined abbreviation list (the List Abbreviation
// option is not selected).
//
// · The full stop is followed by a word found on the Workbench list
// of ordinal followers or a user-defined list of ordinal followers
// (the Ordinal Follower option is not selected).
//
// Default Skip Rule
//
// The only default skip rule is the Semicolon skip rule, according to
// which the semicolon (;) does not indicate the end of a segment. You
// can change this by modifying the semicolon skip rule.

// ========================================================================

// Atril's Deja Vu (manual downloadable from www.atril.com):
//
// - Regular expressions insert breaks after !, ? and . when
//   followed by whitespace.
// - Exception rules prevent a break if it is followed by a
//   lowercase character.
//

/**
 * <p>
 * Segmenter is a wrapper around the Java BreakIterator that extends the
 * sentence iterator with heuristics and rules to correct common segmenting
 * mistakes.
 * </p>
 *
 * <p>
 * Heuristics:
 * </p>
 * <ul>
 * <li>A dot is not interpreted as a segment boundary marker if it is preceded
 * by an abbreviation.</li>
 * <li>A dot is not interpreted as a segment boundary marker if it is preceded
 * by only a single letter.</li>
 * <li>A dot is not interpreted as a segment boundary marker if it is followed
 * by a word which is included in a list of ordinal followers (nouns that
 * typically follow numbers). <I>(Not implemented yet)</I></li>
 * <li>A question or exclamation mark that is <b>not</b> followed by whitespace
 * does not end the segment. Most likely it is followed by punctuation (as in
 * "He said 'Yow!'") or text ("'!important' is a CSS keyword.")</li>
 * </ul>
 *
 * <p>
 * <b>Limitiations:</b> this class only implements forward-scanning using
 * <code>next()</code>. The other methods are implemented but do <B>not</B> work
 * as intended.
 * </p>
 */
public class Segmenter
// extends BreakIterator
{
    //
    // Public Constants
    //
    static public final int DONE = GlobalsightBreakIterator.DONE;

    //
    // Private Members
    //

    private GlobalsightBreakIterator m_bi;
    private String m_text;
    private int m_textLength;

    /**
     * A builtin list of language-dependent abbreviations which are loaded from
     * abbreviations.properties.
     */
    private RE re_builtinAbbrevs = null;

    /**
     * A user-definable list of language-dependent abbreviations which are
     * loaded from abbreviations.properties.
     */
    private RE re_abbreviations = null;

    /**
     * A user-definable list of language-dependent ordinal followers which are
     * loaded from abbreviations.properties. Ordinal followers turn numbers
     * followed by periods into ordinals, as in DE
     * "Am 24. Dezember ist Weihnachten."
     *
     */
    private RE re_ordinalFollowers = null;

    /**
     * <p>
     * A builtin regexp matching a single uppercase character followed by a
     * period. This is most likely <em>not</em> a word that can end a sentence.
     * </p>
     */
    // static private final String SINGLECHAR =
    // "(^|[:space:])[:alpha:]\\.[:space:]*$";
    private RE re_singleChar = null;

    /**
     * <p>
     * A builtin regexp matching an exclamation or question mark at the end of a
     * segment if it's <b>not</b> followed by whitespace.
     * </p>
     *
     * <p>
     * Example: <em>"What's up?", he asked.</em> is segmented like this:
     * <em>"What's up?</em> and <em>", he asked.</em>
     * </p>
     */
    // static private final String ENDINGPUNCT = "(!|\\?)$";
    private RE re_endingPunct = null;

    /**
     * <p>
     * A builtin regexp matching an exclamation or question mark at the end of a
     * segment that is followed by closing quotes and optional whitespace.
     * </p>
     *
     * <p>
     * Example: <em>"What's up?", he asked.</em> is segmented like this:
     * <em>"What's up?</em> and <em>", he asked.</em>
     * </p>
     */
    // static private final String ENDINGPUNCT2 =
    // "(!|\\?)['|\"][:space:]*$";
    private RE re_endingPunct2 = null;

    // static private final String STARTINGPUNCT2 =
    // "^(,|;|[:space:]*\\(?[:lower:])";
    private RE re_startingPunct2 = null;

    /**
     * <p>
     * A builtin regexp matching a comma, semicolon, period, exclamation mark or
     * question mark at the <strong>beginning</strong> of a sentence. These
     * characters do not start a sentence in any language.
     * </p>
     */
    // static private final String STARTINGPUNCT3 =
    // "^(,|;|!|\\.|\\?)";
    private RE re_startingPunct3 = null;

    /**
     * <p>
     * A regexp matching digits followed by a period and space. Used for ordinal
     * followers detection (in e.g. German).
     * </p>
     */
    // static private final String ENDINGDIGITS =
    // "(^|[:space:])[:digit:]+\\.[:space:]*$";
    private RE re_endingDigits = null;

    private int i_current = 0;
    private int i_start = 0;
    private int i_end = 0;
    private int i_skipped = 0;

    //
    // Constructors
    //

    public Segmenter(Locale locale)
    {
        this("", locale);
    }

    public Segmenter(String text, Locale locale)
    {
        m_bi = GlobalsightRuleBasedBreakIterator.getSentenceInstance(locale);
        m_bi.setText(text);
        m_text = text;
        m_textLength = m_text.length();

        // initialize precompiled expressions
        re_singleChar = new RE(
                SegmenterRegexps.SINGLECHAR_Pattern.getProgram(),
                RE.MATCH_NORMAL);

        re_endingPunct = new RE(
                SegmenterRegexps.ENDINGPUNCT_Pattern.getProgram(),
                RE.MATCH_NORMAL);

        re_endingPunct2 = new RE(
                SegmenterRegexps.ENDINGPUNCT2_Pattern.getProgram(),
                RE.MATCH_NORMAL);

        re_startingPunct2 = new RE(
                SegmenterRegexps.STARTINGPUNCT2_Pattern.getProgram(),
                RE.MATCH_NORMAL);

        re_startingPunct3 = new RE(
                SegmenterRegexps.STARTINGPUNCT3_Pattern.getProgram(),
                RE.MATCH_NORMAL);

        re_endingDigits = new RE(
                SegmenterRegexps.ENDINGDIGITS_Pattern.getProgram(),
                RE.MATCH_NORMAL);

        // load language-dependent builtin abbreviations
        // eventually this if-else can be sorted common languages first
        if (locale.getLanguage().equals("de"))
        {
            re_builtinAbbrevs = new RE(
                    SegmenterRegexps.ABBR_DE_Pattern.getProgram(),
                    RE.MATCH_NORMAL);
        }
        else if (locale.getLanguage().equals("en"))
        {
            re_builtinAbbrevs = new RE(
                    SegmenterRegexps.ABBR_EN_Pattern.getProgram(),
                    RE.MATCH_NORMAL);
        }
        else if (locale.getLanguage().equals("es"))
        {
            re_builtinAbbrevs = new RE(
                    SegmenterRegexps.ABBR_ES_Pattern.getProgram(),
                    RE.MATCH_NORMAL);
        }
        else if (locale.getLanguage().equals("fr"))
        {
            re_builtinAbbrevs = new RE(
                    SegmenterRegexps.ABBR_FR_Pattern.getProgram(),
                    RE.MATCH_NORMAL);
        }
        else if (locale.getLanguage().equals("nl"))
        {
            re_builtinAbbrevs = new RE(
                    SegmenterRegexps.ABBR_NL_Pattern.getProgram(),
                    RE.MATCH_NORMAL);
        }

        // load a user-defined abbreviation list and ordinal followers
        ResourceBundle res;
        String regex;

        try
        {
            res = ResourceBundle.getBundle("properties/abbreviations", locale);

            try
            {
                regex = res.getString("abbreviations");

                if (regex != null && regex.length() > 0)
                {
                    // System.err.println("abbreviation regex: " + regex);
                    re_abbreviations = new RE(regex, RE.MATCH_NORMAL);
                }
            }
            catch (RESyntaxException e)
            {
                re_abbreviations = null;
                System.err
                        .println("Segmenter: abbreviation regex for language "
                                + locale.toString() + " contains errors:\n"
                                + e.toString());
            }
            catch (MissingResourceException e)
            {
                // fine, no regexp, no smart segmenting
            }

            try
            {
                regex = res.getString("followers");

                if (regex != null && regex.length() > 0)
                {
                    // System.err.println("followers regex: " + regex);
                    re_ordinalFollowers = new RE(regex, RE.MATCH_NORMAL);
                }
            }
            catch (RESyntaxException e)
            {
                re_ordinalFollowers = null;
                System.err
                        .println("Segmenter: ordinal followers regex for language "
                                + locale.toString()
                                + " contains errors:\n"
                                + e.toString());
            }
            catch (MissingResourceException e)
            {
                // fine, no regexp, no smart segmenting
            }
        }
        catch (MissingResourceException e)
        {
            // fine, no properties file at all
        }
    }

    //
    // Class Overrides (Interface Implementation) -- BreakIterator
    //

    public int first()
    {
        i_current = m_bi.first();
        return i_current;
    }

    public int last()
    {
        i_current = m_bi.last();
        return i_current;
    }

    public int current()
    {
        i_current = m_bi.current();
        return i_current;
    }

    /** @deprecated */
    public int following(int i)
    {
        i_current = m_bi.following(i);
        return i_current;
    }

    public int next()
    {
        i_start = this.current();
        i_current = m_bi.next();

        try
        {
            // Examine the segment for segmentation mistakes

            String str_segment;
            String str_nextSegment;

            while (i_current < m_textLength
                    && i_current != GlobalsightBreakIterator.DONE)
            {
                str_segment = m_text.substring(i_start, i_current);

                // System.err.println(i_start + ":" + i_current +
                // " `" + str_segment + "'");

                // 1. if the segment ends with a single character,
                // scan again.
                if (re_singleChar.match(str_segment, 0))
                {
                    // System.err.println("** single char match");
                    i_current = m_bi.next();
                    continue;
                }

                // 2. if the segment ends with a non-terminating
                // builtin abbreviation, scan again.
                else if (re_builtinAbbrevs != null
                        && re_builtinAbbrevs.match(str_segment, 0))
                {
                    // System.err.println("** builtin abbrev match: " +
                    // str_segment);
                    i_current = m_bi.next();
                    continue;
                }

                // 3. correct an error in the JDK BreakIterator: a
                // break is always inserted after ? and ! regardless
                // of following puntucation or letters ("!important"
                // is '"!' plus 'important"'). It seems safe to
                // append the next segment without looking at what it
                // starts with.
                else if (re_endingPunct.match(str_segment, 0))
                {
                    // System.err.println("** ending !? match");
                    i_current = m_bi.next();
                    continue;
                }

                // 4. correct another error in the JDK Breakiterator:
                // a break is inserted after ? and then after " in
                // '"What's up?", he asked.'

                // Note this is not true for ICU, but there are cases
                // where a period and a comma are separated, as in "A
                // geometric figure is, e.g., a circle." ==} "A
                // geometric figure is, e.g." plus ", a circle" in nl.
                else if (re_endingPunct2.match(str_segment, 0))
                {
                    str_nextSegment = m_text.substring(i_current);
                    // System.err.println("** ending !?\"' --> " +
                    // str_nextSegment);

                    if (re_startingPunct2.match(str_nextSegment, 0))
                    {
                        i_current = m_bi.next();
                        continue;
                    }
                }

                // 5. check ordinal followers in languages that have them (DE)
                else if (re_ordinalFollowers != null
                        && re_endingDigits.match(str_segment, 0))
                {
                    str_nextSegment = m_text.substring(i_current);
                    // System.err.println("** digits -> " + str_nextSegment);

                    if (re_ordinalFollowers.match(str_nextSegment, 0))
                    {
                        i_current = m_bi.next();
                        continue;
                    }
                }

                // 6. if the segment ends with a user-defined
                // abbreviation, scan again.
                else if (re_abbreviations != null
                        && re_abbreviations.match(str_segment, 0))
                {
                    // System.err.println("** user abbrev match --> " +
                    // str_segment);
                    i_current = m_bi.next();
                    continue;
                }

                // 7. if the next segment would start with a
                // non-beginning character (comma, semicolon, period,
                // exclamation mark or question mark), scan again.
                else
                {
                    str_nextSegment = m_text.substring(i_current);

                    if (re_startingPunct3.match(str_nextSegment, 0))
                    {
                        i_current = m_bi.next();
                        continue;
                    }
                }

                break;
            }
        }
        catch (Exception e)
        {
            System.err.println("Segmenter: error during regexp match: " + e);
        }

        // done, do bookkeeping
        i_end = i_current;

        return i_current;
    }

    /**
     * @deprecated This function is not correctly implemented by this wrapper.
     */
    public int next(int i)
    {
        while (i > 0)
        {
            this.next();
            --i;
        }
        while (i < 0)
        {
            this.previous();
            ++i;
        }

        return i_current;
    }

    /**
     * @deprecated This function is not correctly implemented by this wrapper.
     */
    public int previous()
    {
        i_current = m_bi.previous();
        return i_current;
    }

    public CharacterIterator getText()
    {
        return m_bi.getText();
    }

    public void setText(String text)
    {
        m_bi.setText(text);
        m_text = text;
        m_textLength = m_text.length();
        i_current = 0;
    }

    /**
     * Required by the BreakIterator Interface but not useful for this class
     * because we need to know the original String and CharacterIterator won't
     * give it to us.
     *
     * @deprecated use setText(String)
     */
    public void setText(CharacterIterator text)
    {
        m_bi.setText(text);
        i_current = 0;
    }

}
