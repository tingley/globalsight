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
package com.globalsight.ling.tm2.lucene;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.indexer.Token;

/**
 * TM Lucene index utility class.
 */

public class LuceneUtil
{
    private static final GlobalSightCategory c_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            LuceneUtil.class);

//    private static final String GOLD_TM_ROOT = "GlobalSight/GoldTmIndex";

    
    /**
     * Returns a File object representing a Lucene index directory for
     * specified TM id and locale. If p_create is true, such directory
     * will be created if it doesn't exist. If p_create is false and
     * such directory doesn't exist, null will be returned.
     *
     * @param p_tmId Tm id
     * @param p_locale Locale
     * @return File object or null (see above)
     */
    public static File getGoldTmIndexDirectory(
        long p_tmId, GlobalSightLocale p_locale, boolean p_create)
        throws Exception
    {
//        SystemConfiguration sc = SystemConfiguration.getInstance();
//        String docRoot = sc.getStringParameter(
//            SystemConfiguration.FILE_STORAGE_DIR);
//
//        File tmIndexRoot = new File(docRoot, GOLD_TM_ROOT);
        File tmIndexRoot = AmbFileStoragePathUtils.getGoldTmIndexDir(p_tmId);
        tmIndexRoot = new File(tmIndexRoot, Long.toString(p_tmId));
        tmIndexRoot = new File(tmIndexRoot, p_locale.toString());

        if(p_create)
        {
            tmIndexRoot.mkdirs();
        }
        else if(! tmIndexRoot.exists())
        {
            tmIndexRoot = null;
        }
        
        c_logger.debug("Get gold tm index directory: " + tmIndexRoot);

        return tmIndexRoot;
    }


    /**
     * Returns a File object representing a root directory of a Lucene
     * index directory for a Tm.  This method returns a parent
     * directory of what getGoldTmIndexDirectory would return.  This
     * method returns a File object even if the directory doesn't
     * exist.
     *
     * @param p_tmId Tm id
     * @return File object
     */
    public static File getGoldTmIndexParentDir(long p_tmId)
        throws Exception
    {
//        SystemConfiguration sc = SystemConfiguration.getInstance();
//        String docRoot = sc.getStringParameter(
//            SystemConfiguration.FILE_STORAGE_DIR);
//
//        File tmIndexRoot = new File(docRoot, GOLD_TM_ROOT);
        File tmIndexDir = new File(AmbFileStoragePathUtils.getGoldTmIndexDir(),
                Long.toString(p_tmId));

        c_logger.debug("Get gold tm index parent directory: " + tmIndexDir);
        
        return tmIndexDir;
    }

    
    /**
     * Create GlobalSight TM tokens from a provided segment string
     * using GsAnalyzer.
     *
     * @param p_text fuzzy match format string
     * @return List of c.g.l.tm2.index.Tokens
     */
    public static List<Token> createGsTokens(
        String p_text, GlobalSightLocale p_locale)
        throws Exception
    {
        GsAnalyzer analyzer = new GsAnalyzer(p_locale);
        TokenStream tokenStream = analyzer.tokenStream(
            "blah", new StringReader(p_text));

        org.apache.lucene.analysis.Token luceneToken = null;
        List<String> tokens = new ArrayList<String>();
        while((luceneToken = tokenStream.next()) != null)
        {
            tokens.add(luceneToken.termText());
        }
        tokenStream.close();
        return buildTokenList(tokens);
    }

    /**
     * Create GlobalSight TM tokens from a provided segment string
     * using GsAnalyzer.  This method is suitable for use with TM3
     * fuzzy indices, and does two things differently than createGsTokens():
     * 1) It returns tokens in the order in which they appear
     * 2) It does not collapse duplicate tokens (and correspondingly does
     *    not return count information)
     *
     * @param p_text fuzzy match format string
     * @return List of Strings, each representing one token
     */
    public static List<String> createTm3Tokens(
        String p_text, GlobalSightLocale p_locale)
        throws Exception
    {
        GsAnalyzer analyzer = new GsAnalyzer(p_locale);
        TokenStream tokenStream = analyzer.tokenStream(null, 
                            new StringReader(p_text));

        org.apache.lucene.analysis.Token luceneToken = null;
        List<String> tokens = new ArrayList<String>();
        while((luceneToken = tokenStream.next()) != null)
        {
            tokens.add(luceneToken.termText());
        }
        tokenStream.close();
        
        return tokens;
    }

    /**
     * Given a list of Strings representing token text, collapse
     * into a list of tokens by removing duplicates and calculating
     * count data.  Note that the order of the returned token list
     * is not necessarily the same as the list of Strings.
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
            tokens.add(new Token(e.getKey(), 0, 0, 0,
                       e.getValue(), totalTokenCount, true));
        }
        return tokens;
    }
}
