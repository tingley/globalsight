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
package com.globalsight.ling.lucene.analysis.pl.stempel;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Stemmer class is a convenient facade for other stemmer-related classes. The
 * core stemming algorithm and its implementation is taken verbatim from the
 * Egothor project ( <a href="http://www.egothor.org">www.egothor.org </a>).
 *
 * <p>Even though the stemmer tables supplied in the distribution
 * package are built for Polish language, there is nothing
 * language-specific here.</p>
 *
 * @author Andrzej Bialecki &lt;ab@getopt.org&gt;
 */
public class Stemmer
{
    /**
     * Minimum length of input words to be processed. Shorter words
     * are returned unchanged.
     */
    public int MIN_LENGTH = 3;

    Trie stemmer = null;
    String tableResPath = null;

    /**
     * Create a Stemmer using stemmer table loaded from resource path
     * pointed to by System property
     * <code>org.getopt.stempel.table</code>. If this property is
     * missing, it is assumed that the included
     * <code>stemmer_2000.out</code> table is to be used.
     */
    public Stemmer()
    {
        this("/com/globalsight/ling/lucene/analysis/pl/stempel/stemmer_2000.out");
    }

    /**
     * Create a Stemmer using selected stemmer table
     *
     * @param stemmerTable resource path to stemmer table. This
     * resource will be looked up using this class's ClassLoader.
     */
    public Stemmer(String stemmerTable)
    {
        if (stemmerTable == null)
        {
            return;
        }

        tableResPath = stemmerTable;

        try
        {
            DataInputStream in = new DataInputStream(new BufferedInputStream(
                getClass().getResourceAsStream(tableResPath)));
            String method = in.readUTF().toUpperCase();
            if (method.indexOf('M') < 0)
            {
                stemmer = new Trie(in);
            }
            else
            {
                stemmer = new MultiTrie2(in);
            }

            in.close();
        }
        catch (IOException x)
        {
            x.printStackTrace();
            stemmer = null;
        }
    }

    /**
     * Return resource path to the stemmer table, or null if
     * initialized * with preloaded table.
     */
    public String getTableResPath()
    {
        return tableResPath;
    }

    /**
     * Create a Stemmer using pre-loaded stemmer table
     *
     * @param stemmer pre-loaded stemmer table
     */
    public Stemmer(Trie stemmer)
    {
        this.stemmer = stemmer;
    }

    /**
     * Stem a word. For performance reasons words shorter than MIN_LENGTH
     * characters are not processed, but simply returned.
     *
     * @param word input word to be stemmed.
     * @param hideMissing if true, and the stem could not be found,
     * return the input word. If false, return null in such case.
     * @return stemmed word, or null if the stem could not be generated.
     */
    public String stem(String word, boolean hideMissing)
    {
        if (word == null)
        {
            return null;
        }
        if (word.length() <= MIN_LENGTH)
        {
            return word;
        }

        String cmd = stemmer.getLastOnPath(word);
        if (cmd == null)
        {
            if (hideMissing)
            {
                return word;
            }
            else
            {
                return null;
            }
        }

        StringBuffer res = Diff.apply(new StringBuffer(word), cmd);

        if (res.length() > 0)
            return res.toString();
        else if (hideMissing)
            return word;
        else
            return null;
    }

    /**
     * Testing method. Stemmer table file name is taken from the first
     * argument, and the second argument is the word to stem. If one
     * argument is given, the default table is assumed, and the
     * argument is a word to stem.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
        throws Exception
    {
        Stemmer s;
        if (args.length > 1)
        {
            s = new Stemmer(args[0]);
            System.out.println(s.stem(args[1], false));
        }
        else
        {
            s = new Stemmer();
            System.out.println(s.stem(args[0], false));
        }
    }
}
