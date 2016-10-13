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

package galign.helpers.util;

import galign.helpers.util.RegexUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.UnsupportedEncodingException;

// TODO: this class need to be instantiable so search-replace code can
// use one instance for 1000s of strings.

/**
 * This class implements smart search/replace in strings.
 */
public final class CaseUtil
{
    private String m_search;
    private String m_replace;
    private boolean m_caseInsensitive;
    private boolean m_smartReplace;

    private Pattern m_pattern;

    public CaseUtil(String p_search, String p_replace,
        boolean p_caseInsensitive, boolean p_smartReplace)
    {
        m_search = p_search;
        m_replace = p_replace;
        m_caseInsensitive = p_caseInsensitive;
        m_smartReplace = p_smartReplace;

        // May have to remove asterisk, it gets mapped "*" -> ".*"
        if (p_caseInsensitive)
        {
            m_pattern = Pattern.compile (
                RegexUtil.simplePatternToFullRegularExpression(p_search),
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        }
        else
        {
            m_pattern = Pattern.compile (
                RegexUtil.simplePatternToFullRegularExpression(p_search));
        }
    }

    /**
     * Replaces a search string in text with a replace string, thereby
     * matching the replaced text's case with the matched text's case.
     *
     * smartReplace() ignores case while searching for occurrences to
     * replace -- provided `case-insensitive-search' is true.
     *
     * If the SMARTREPLACE argument is false, the replace text is
     * inserted as is.
     *
     * If the SMARTREPLACE argument is true and, in addition, when the
     * REPLACE argument is all or partly lower case, replacement
     * commands try to preserve the case pattern of each occurrence.
     * Thus,
     *
     *     smartReplace("foo", "bar")
     *
     * replaces a lower case `foo' with a lower case `bar', an
     * all-caps `FOO' with `BAR', and a capitalized `Foo' with `Bar'.
     * (These three alternatives -- lower case, all caps, and
     * capitalized, are the only ones that smartReplace() can
     * distinguish.)
     *
     * If upper-case letters are used in the replacement string, they
     * remain upper case every time that text is inserted.  If
     * upper-case letters are used in the search string, the
     * replacement text is always substituted exactly as given, with
     * no case conversion.
     */
    public String smartReplace(String p_text)
    {
        Matcher matcher = m_pattern.matcher(p_text);

        if (!m_smartReplace)
        {
            return matcher.replaceAll(m_replace);
        }
        else
        {
            StringBuffer result = new StringBuffer();
            int pos = 0;

            while (matcher.find(pos))
            {
                result.append(p_text.substring(pos, matcher.start()));

                String match = p_text.substring(
                    matcher.start(), matcher.end());

                result.append(getSmartCasedText(m_search, m_replace, match));

                pos = matcher.end();
            }

            result.append(p_text.substring(pos));

            return result.toString();
        }
    }

    /** @see #smartReplace(String) */
    static public String smartReplace(String p_text, String p_search,
        String p_replace, boolean p_caseInsensitive, boolean p_smartReplace)
    {
        Pattern pattern;

        // May have to remove asterisk, it gets mapped "*" -> ".*"
        if (p_caseInsensitive)
        {
            pattern = Pattern.compile (
                RegexUtil.simplePatternToFullRegularExpression(p_search),
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        }
        else
        {
            pattern = Pattern.compile (
                RegexUtil.simplePatternToFullRegularExpression(p_search));
        }

        Matcher matcher = pattern.matcher(p_text);

        if (!p_smartReplace)
        {
            return matcher.replaceAll(p_replace);
        }
        else
        {
            StringBuffer result = new StringBuffer();
            int pos = 0;

            while (matcher.find(pos))
            {
                result.append(p_text.substring(pos, matcher.start()));

                String match = p_text.substring(
                    matcher.start(), matcher.end());

                result.append(getSmartCasedText(p_search, p_replace, match));

                pos = matcher.end();
            }

            result.append(p_text.substring(pos));

            return result.toString();
        }
    }

    /** @see #smartReplace(String) */
    static public String getSmartCasedText(String p_search, String p_replace,
        String p_match)
    {
        // assert(p_search.length() > 1);
        // assert(p_match.length() > 1);

        if (p_replace.length() == 0)
        {
            return p_replace;
        }

        boolean replaceAllUpper = isAllUpperCase(p_replace);
        if (replaceAllUpper)
        {
            return p_replace;
        }

        boolean searchContainsUpper = isSomeUpperCase(p_search);
        if (searchContainsUpper)
        {
            return p_replace;
        }

        boolean matchAllUpper = isAllUpperCase(p_match);
        if (matchAllUpper)
        {
            return p_replace.toUpperCase();
        }

        boolean matchTitleCase = isTitleCase(p_match);
        if (matchTitleCase)
        {
            StringBuffer result = new StringBuffer();

            char ch = p_replace.charAt(0);
            if (Character.isLetter(ch))
            {
                if (Character.isTitleCase(ch))
                {
                    result.append(ch);
                }
                else
                {
                    result.append(Character.toTitleCase(ch));
                }
            }
            else
            {
                result.append(ch);
            }

            result.append(p_replace.substring(1));

            return result.toString();
        }

        return p_replace;
    }

    static public boolean isAllUpperCase(String p_text)
    {
        for (int i = 0, max = p_text.length(); i < max; i++)
        {
            char ch = p_text.charAt(i);

            if (Character.isLetter(ch) && !Character.isUpperCase(ch))
            {
                return false;
            }
        }

        return true;
    }

    static public boolean isSomeUpperCase(String p_text)
    {
        for (int i = 0, max = p_text.length(); i < max; i++)
        {
            char ch = p_text.charAt(i);

            if (Character.isLetter(ch) && Character.isUpperCase(ch))
            {
                return true;
            }
        }

        return false;
    }

    static public boolean isTitleCase(String p_text)
    {
        if (p_text.length() == 0)
        {
            return false;
        }

        // Can you believe it? This doesn't work...
        // return Character.isTitleCase(p_text.charAt(0));

        char ch = p_text.charAt(0);

        return Character.isLetter(ch) && Character.isUpperCase(ch);
    }

    /*
      Emacs test here.

      lower case test f-o-o -> b-a-r
      f-o-o     foo    b-a-r
      F-O-O     FOO    B-A-R
      F-o-o     Foo    B-a-r
      F-o-O     FoO    B-a-r

      mixed case test f-o-o -> b-A-r
      f-o-o     foo    b-A-r
      F-O-O     FOO    B-A-R
      F-o-o     Foo    B-A-r
      F-o-O     FoO    B-A-r

      mixed case test f-o-o -> B-a-r
      f-o-o     foo    B-a-r
      F-O-O     FOO    B-A-R
      F-o-o     Foo    B-a-r
      F-o-O     FoO    B-a-r

      mixed case test f-O-o -> b-A-r
      f-o-o     foo   <no match>
      F-O-O     FOO
      F-o-o     Foo
      F-o-O     FoO
      f-O-o     fOo   <replace text as is, b-A-r>
     */

    static public void test(String s, String r, String m, String e)
    {
        System.err.println("search=" + s + " replace=" + r + " match=" + m);
        System.err.println("-->" + getSmartCasedText(s, r, m) +
            " --- expected " + e);
    }

    static public void main (String[] argv)
        throws Exception
    {
        test("foo", "bar", "foo", "bar");
        test("foo", "bar", "FOO", "BAR");
        test("foo", "bar", "Foo", "Bar");
        test("foo", "bar", "FoO", "Bar");

        test("foo", "bAr", "foo", "bAr");
        test("foo", "bAr", "FOO", "BAR");
        test("foo", "bAr", "Foo", "BAr");
        test("foo", "bAr", "FoO", "BAr");

        test("foo", "Bar", "foo", "Bar");
        test("foo", "Bar", "FOO", "BAR");
        test("foo", "Bar", "Foo", "Bar");
        test("foo", "Bar", "FoO", "Bar");
    }
}
