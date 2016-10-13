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
package com.plug.Version_8_5_2.gs.ling.tm2.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.Version;

import com.plug.Version_8_5_2.gs.util.GlobalSightLocale;
import com.plug.Version_8_5_2.gs.ling.common.DiplomatBasicParser;
import com.plug.Version_8_5_2.gs.ling.common.Text;
import com.plug.Version_8_5_2.gs.ling.lucene.GSAttributeImpl;
import com.plug.Version_8_5_2.gs.ling.tm2.FuzzyIndexFormatHandler;
import com.plug.Version_8_5_2.gs.ling.tm2.indexer.Token;

/**
 * TM Lucene index utility class.
 */

public class LuceneUtil
{
    private static final Logger c_logger = Logger.getLogger(LuceneUtil.class);
    
    public static Version VERSION = Version.LUCENE_44;

    // private static final String GOLD_TM_ROOT = "GlobalSight/GoldTmIndex";

    public static CharArraySet newCharArraySet()
    {
        return new CharArraySet(Version.LUCENE_44, 20, false);
    }

    public static org.apache.lucene.analysis.Token getNextToken(TokenStream input)
            throws IOException
    {
        org.apache.lucene.analysis.Token token = null;
        if (input.incrementToken())
        {
            CharTermAttribute ccc = input.addAttribute(CharTermAttribute.class);
            Iterator<AttributeImpl> attIt = input.getAttributeImplsIterator();

            if (attIt == null || !attIt.hasNext())
            {
                return null;
            }

            AttributeImpl att = attIt.next();
            if (att instanceof GSAttributeImpl)
            {
                token = ((GSAttributeImpl) att).getToken();
            }

            if (token == null && ccc != null && ccc.length() > 0)
            {
                String ttt = ccc.toString();
                token = new org.apache.lucene.analysis.Token(ttt, 0, ttt.length());
            }
        }

        return token;
    }

    /**
     * Create GlobalSight TM tokens from a provided segment string using
     * GsAnalyzer.
     * 
     * @param p_text
     *            fuzzy match format string
     * @return List of c.g.l.tm2.index.Tokens
     */
    public static List<Token> createGsTokens(String p_text, GlobalSightLocale p_locale)
            throws Exception
    {
        GsAnalyzer analyzer = new GsAnalyzer(p_locale);
        TokenStream tokenStream = analyzer.tokenStream("blah",
                new StringReader(p_text));

        tokenStream.reset();
        // GSAttribute gsAtt = tokenStream.addAttribute(GSAttribute.class);
        // org.apache.lucene.analysis.Token luceneToken = null;
        List<String> tokens = new ArrayList<String>();

        while (tokenStream.incrementToken())
        {
            // luceneToken = gsAtt.getToken();

            CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
            tokens.add(termAtt.toString());

        }
        tokenStream.close();
        return buildTokenList(tokens);
    }

    /**
     * Create GlobalSight TM tokens from a provided segment string using
     * GsAnalyzer. This method is suitable for use with TM3 fuzzy indices, and
     * does two things differently than createGsTokens(): 1) It returns tokens
     * in the order in which they appear 2) It does not collapse duplicate
     * tokens (and correspondingly does not return count information)
     * 
     * @param p_text
     *            fuzzy match format string
     * @return List of Strings, each representing one token
     */
    public static List<String> createTm3Tokens(String p_text, GlobalSightLocale p_locale)
            throws Exception
    {
        GsAnalyzer analyzer = new GsAnalyzer(p_locale);
        TokenStream tokenStream = analyzer.tokenStream("blah", new StringReader(p_text));
        tokenStream.reset();

        List<String> tokens = new ArrayList<String>();
        while (tokenStream.incrementToken())
        {
            CharTermAttribute termAtt = tokenStream.getAttribute(CharTermAttribute.class);
            tokens.add(termAtt.toString());
        }
        tokenStream.close();

        return tokens;
    }

    /**
     * Given a list of Strings representing token text, collapse into a list of
     * tokens by removing duplicates and calculating count data. Note that the
     * order of the returned token list is not necessarily the same as the list
     * of Strings.
     */
    public static List<Token> buildTokenList(List<String> text)
    {
        Map<String, Integer> m = new HashMap<String, Integer>();
        for (String s : text)
        {
            Integer i = m.get(s);
            if (i == null)
            {
                m.put(s, Integer.valueOf(1));
            }
            else
            {
                m.put(s, Integer.valueOf(1 + i));
            }
        }
        int totalTokenCount = text.size();
        List<Token> tokens = new ArrayList<Token>();
        for (Map.Entry<String, Integer> e : m.entrySet())
        {
            tokens.add(new Token(e.getKey(), 0, 0, 0, e.getValue(), totalTokenCount, true));
        }
        return tokens;
    }

    public static String normalizeTuvData(String segment, GlobalSightLocale locale)
            throws Exception
    {
        FuzzyIndexFormatHandler handler = new FuzzyIndexFormatHandler();
        DiplomatBasicParser diplomatParser = new DiplomatBasicParser(handler);

        diplomatParser.parse(segment);

        // add spaces at the beginning and the end of the string
        String fuzzyIndexFormat = " " + handler.toString() + " ";
        // normalize white space
        fuzzyIndexFormat = Text.normalizeWhiteSpaceForTm(fuzzyIndexFormat);
        // down case the string
        return fuzzyIndexFormat.toLowerCase(locale.getLocale());
    }
}
