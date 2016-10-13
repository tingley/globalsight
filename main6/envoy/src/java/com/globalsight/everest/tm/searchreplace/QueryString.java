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
package com.globalsight.everest.tm.searchreplace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm2.indexer.NgramTokenizer;
import com.globalsight.ling.tm2.indexer.StopWord;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm2.indexer.WordTokenizer;
import com.globalsight.util.GlobalSightLocale;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * QueryString stores a query string for concordance search. The class divedes
 * the query string into words and provides various representation of them such
 * as word token and n-gram token for tm index search, etc.
 */

public class QueryString
{
    static private Logger c_category = Logger.getLogger(QueryString.class);

    private static final REProgram SPACES = createSearchPattern("\\s+");

    public static REProgram createSearchPattern(String p_pattern)
    {
        REProgram pattern = null;
        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }
        return pattern;
    }

    private String m_queryString;
    private HashSet m_wordList;
    private GlobalSightLocale m_locale;

    public QueryString(String p_queryString, GlobalSightLocale p_sourceLocale)
    {
        m_locale = p_sourceLocale;

        // trim whitespaces
        m_queryString = p_queryString.trim();

        // split to words

        // TODO: Word breaking for languages that doesn't use
        // space for word separator. The reason not to use
        // WordBreakIterator here is that it recognizes *
        // (asterisk) as word breaking character.
        RE matcher = new RE(SPACES);
        String[] wordList = matcher.split(m_queryString);

        m_wordList = new HashSet();
        for (int i = 0; i < wordList.length; i++)
        {
            m_wordList.add(wordList[i]);
        }
    }

    /**
     * getNonWildcardTokensForQuery returns tokens for querying TM indexes. The
     * parameter specifies if word or n-gram tokens are returned. The tokens
     * don't contain wildcard characters.
     *
     * @param p_wordTmIndex
     *            If true, word tokens are returned. If false, n-gram tokens are
     *            returned.
     * @return List of words (String)
     */
    public List getNonWildcardTokensForQuery(boolean p_wordTmIndex)
            throws Exception
    {
        List tokens = null;

        if (p_wordTmIndex)
        {
            tokens = getNonWildcardWordsForQuery();
        }
        else
        {
            tokens = getNonWildcardNgramTokens();
        }

        return tokens;
    }

    /**
     * getNonWildcardWordsForExistenceCheck returns a word list for checking the
     * existence of those words in a segment string. The words don't contain
     * wildcard characters.
     *
     * @param p_preserveCase
     *            If true, preserve the case of the words. If false, words are
     *            down cased.
     * @return List of words (String)
     */
    public List getNonWildcardWordsForExistenceCheck(boolean p_preserveCase)
    {
        ArrayList list = new ArrayList();

        for (Iterator it = m_wordList.iterator(); it.hasNext();)
        {
            String word = (String) it.next();
            if (!containsWildcard(word))
            {
                if (!p_preserveCase)
                {
                    // down case the word
                    word = word.toLowerCase(m_locale.getLocale());
                }

                list.add(word);
            }
        }

        return list;
    }

    /**
     * getNonWildcardWordForQuery returns word list for querying word based TM
     * index. The words don't contain wildcard characters.
     *
     * @return List of words (String)
     */
    private List getNonWildcardWordsForQuery() throws Exception
    {
        StopWord stopWord = StopWord.getStopWord(m_locale);

        ArrayList list = new ArrayList();

        for (Iterator it = m_wordList.iterator(); it.hasNext();)
        {
            String word = (String) it.next();
            if (!containsWildcard(word))
            {
                // limit the length of the token. Database column for
                // the word token has only 300 byte length.
                word = Text.substrUtf8LimitLen(word,
                        WordTokenizer.TOKEN_COLUM_LEN);

                // down case the word
                word = word.toLowerCase(m_locale.getLocale());

                // / filter out the stop words
                if (!stopWord.isStopWord(word))
                {
                    list.add(word);
                }
            }
        }

        return list;
    }

    /**
     * getNonWildcardNgramTokens returns n-gram token list for querying n-gram
     * based TM index. The n-gram tokens don't contain wildcard characters.
     *
     * @return List of n-gram tokens (String)
     */
    private List getNonWildcardNgramTokens() throws Exception
    {
        NgramTokenizer ngramTokenizer = new NgramTokenizer();

        ArrayList list = new ArrayList();

        for (Iterator it = m_wordList.iterator(); it.hasNext();)
        {
            String word = (String) it.next();

            // down case the word
            word = word.toLowerCase(m_locale.getLocale());

            // replace literal asterisk '\*' with U+FFFF
            word = Text.replaceString(word, "\\*", "\uffff");

            // make n-gram tokens
            List tokens = ngramTokenizer
                    .tokenize(word, 0, 0, 0, m_locale, true);

            for (Iterator itToken = tokens.iterator(); itToken.hasNext();)
            {
                Token token = (Token) itToken.next();
                String tokenString = token.getTokenString();

                if (tokenString.indexOf('*') == -1)
                {
                    // replace U+FFFF back with '*' (just '*' is
                    // literal asterisk in SQL)
                    tokenString = Text
                            .replaceString(tokenString, "\uffff", "*");

                    list.add(tokenString);
                }
            }
        }

        return list;
    }

    /**
     * getWildcardTokensForQuery returns tokens for querying TM indexes. The
     * parameter specifies if word or n-gram tokens are returned. The tokens
     * contain wildcard characters and they are formatted suitable for using in
     * SQL LIKE clause.
     *
     * @param p_wordTmIndex
     *            If true, word tokens are returned. If false, n-gram tokens are
     *            returned.
     * @return List of words (String)
     */
    public List getWildcardTokensForQuery(boolean p_wordTmIndex)
            throws Exception
    {
        List tokens = null;

        if (p_wordTmIndex)
        {
            tokens = getWildcardWordsForQuery();
        }
        else
        {
            tokens = getWildcardNgramTokens();
        }

        return tokens;
    }

    /**
     * getWildcardWordsForExistenceCheck returns a word list for checking the
     * existence of those words in a segment string. The words contain wildcard
     * characters and they are formatted suitable for using in regular
     * expression.
     *
     * @param p_preserveCase
     *            If true, preserve the case of the words. If false, words are
     *            down cased.
     * @return List of words (String). RegExp formatted.
     */
    public List getWildcardWordsForExistenceCheck(boolean p_preserveCase)
    {
        ArrayList list = new ArrayList();

        for (Iterator it = m_wordList.iterator(); it.hasNext();)
        {
            String word = (String) it.next();
            if (containsWildcard(word))
            {
                if (!p_preserveCase)
                {
                    // down case the word
                    word = word.toLowerCase(m_locale.getLocale());
                }

                // make regexp string
                list.add(makeRegExpPattern(word));
            }
        }

        return list;
    }

    /**
     * getWildcardWordForQuery returns word list for querying word based TM
     * index. The words contain wild card characters and they are formatted
     * suitable for using in SQL LIKE clause.
     *
     * @return List of words (String). SQL formatted.
     */
    private List getWildcardWordsForQuery()
    {
        ArrayList list = new ArrayList();

        for (Iterator it = m_wordList.iterator(); it.hasNext();)
        {
            String word = (String) it.next();
            if (containsWildcard(word))
            {
                // don't limit the word length. We only hope that
                // the word is no longer 300 bytes.

                // down case the word
                word = word.toLowerCase(m_locale.getLocale());

                // make SQL wildcard query string
                list.add(makeWildcardQueryString(word, '%'));
            }
        }

        return list;
    }

    /**
     * getWildcardNgramTokens returns n-gram token list for querying n-gram
     * based TM index. The n-gram tokens contain wildcard characters and they
     * are formatted suitable for using in SQL LIKE clause.
     *
     * @return List of n-gram tokens (String). SQL formatted.
     */
    private List getWildcardNgramTokens() throws Exception
    {
        NgramTokenizer ngramTokenizer = new NgramTokenizer();

        ArrayList list = new ArrayList();

        for (Iterator it = m_wordList.iterator(); it.hasNext();)
        {
            String word = (String) it.next();

            // down case the word
            word = word.toLowerCase(m_locale.getLocale());

            // replace literal asterisk '\*' with U+FFFF
            word = Text.replaceString(word, "\\*", "\uffff");

            // make n-gram tokens
            List tokens = ngramTokenizer
                    .tokenize(word, 0, 0, 0, m_locale, true);

            for (Iterator itToken = tokens.iterator(); itToken.hasNext();)
            {
                Token token = (Token) itToken.next();
                String tokenString = token.getTokenString();

                int idx = -1;
                if ((idx = tokenString.indexOf('*')) != -1)
                {
                    // Here assuming N_GRAM_LENGTH == 3...
                    // split a token like this "a*c" into "a*" and "*c"
                    if (idx == 1)
                    {
                        String newTokenString = tokenString.substring(0, 2);
                        tokenString = tokenString.substring(1, 3);

                        // formatt newTokenString and add it to the list
                        newTokenString = makeWildcardQueryString(
                                newTokenString, '%');
                        newTokenString = Text.replaceString(newTokenString,
                                "\uffff", "*");

                        list.add(newTokenString);
                    }

                    // make SQL query string
                    tokenString = makeWildcardQueryString(tokenString, '%');

                    // replace U+FFFF back with '*' (just '*' is
                    // literal asterisk in SQL)
                    tokenString = Text
                            .replaceString(tokenString, "\uffff", "*");

                    list.add(tokenString);
                }
            }
        }

        return list;
    }

    /**
     * Test if a string contains wildcard character '*'. An escaped wildcard
     * character '\*' is a literal asterisk '*'. The method compares the number
     * of instances of '\*' and '*' in a string and if the numbers are
     * different, it reports that the string contains wildcard characters.
     *
     * @param p_string
     *            String to be examined.
     * @return true if the string contains wildcard characters.
     */
    private boolean containsWildcard(String p_string)
    {
        // test the occurence of '*'
        int wildcardNum = 0;
        for (int i = 0; i < p_string.length();)
        {
            int idx = p_string.indexOf("*", i);
            if (idx == -1)
            {
                break;
            }
            else
            {
                wildcardNum++;
                i = idx + 1;
            }
        }

        // no wildcard character was found
        if (wildcardNum == 0)
        {
            return false;
        }

        // test the occurence of '\*'
        int literalNum = 0;
        for (int i = 0; i < p_string.length();)
        {
            int idx = p_string.indexOf("\\*", i);
            if (idx == -1)
            {
                break;
            }
            else
            {
                literalNum++;
                i = idx + 2;
            }
        }

        return wildcardNum != literalNum;
    }

    /**
     * '*' is the only allowed wildcard char the user can enter. '*' is escaped
     * using '\' to make '*' literal.
     *
     * p_escapeChar is an escape character used in LIKE predicate to escape '%'
     * and '_' wildcards.
     */
    static private String makeWildcardQueryString(String p_queryString,
            char p_escapeChar)
    {
        String pattern = p_queryString;
        String escape = String.valueOf(p_escapeChar);
        String asterisk = "*";
        String percent = "%";
        String underScore = "_";

        // '&' -> '&&' (escape itself)
        pattern = Text.replaceString(pattern, escape, escape + escape);

        // '%' -> '&%' (escape wildcard char)
        pattern = Text.replaceString(pattern, percent, escape + percent);

        // '_' -> '&_' (escape wildcard char)
        pattern = Text.replaceString(pattern, underScore, escape + underScore);

        // '*' -> '%' (change wildcard) '\*' -> '*' (literal *)
        pattern = Text.replaceChar(pattern, '*', '%', '\\');

        if (c_category.isDebugEnabled())
        {
            c_category.debug("search + replace pattern = " + pattern);
        }

        return pattern;
    }

    private String makeRegExpPattern(String p_pattern)
    {
        // special characters that need to be escaped. The trick is
        // that '\' (backslash) must be the first one to be escaped.
        String[] specialCharList =
        { "\\", "(", ")", "[", "]", "+", "?", ".", "^", "$", "{", "}", "|" };
        String escape = "\\";

        String pattern = p_pattern;

        // replace '\*' with U+FFFF
        pattern = Text.replaceString(pattern, "\\*", "\uffff");

        // escape special characters
        for (int i = 0; i < specialCharList.length; i++)
        {
            String specialChar = specialCharList[i];
            pattern = Text.replaceString(pattern, specialChar, escape
                    + specialChar);
        }

        // '*' -> '\S*' (any non space character sequence)
        pattern = Text.replaceString(pattern, "*", "\\S*");

        // replace U+FFFF with '\*'
        pattern = Text.replaceString(pattern, "\uffff", "\\*");

        if (c_category.isDebugEnabled())
        {
            c_category.debug("ReExec pattern = " + pattern);
        }

        return pattern;
    }

}
