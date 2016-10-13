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
package com.globalsight.ling.lucene.analysis;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.util.CharArraySet;

import com.globalsight.ling.tm2.lucene.LuceneUtil;

/**
 * Loader for text files that represent a list of stopwords.
 */
public class WordlistLoader
{
    static public CharArraySet getWordSet(File wordfile)
        throws IOException
    {
        return getWordSet(wordfile, "Cp1252");
    }

    /**
     * Loads a text file and adds every line as an entry to a HashSet
     * (omitting leading and trailing whitespace).
     *
     * Every line of the file should contain only one word. The words
     * need to be in lowercase if you make use of an Analyzer which
     * uses LowerCaseFilter (like GermanAnalyzer).
     *
     * @param wordfile File containing the wordlist
     * @return A HashSet with the file's words
     */
    static public CharArraySet getWordSet(File wordfile, String p_encoding)
        throws IOException
    {
        CharArraySet result = LuceneUtil.newCharArraySet();
        Reader reader = null;
        LineNumberReader lnr = null;

        try
        {
            reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(wordfile), p_encoding));
            lnr = new LineNumberReader(reader);
            String word = null;
            while ((word = lnr.readLine()) != null)
            {
                result.add(word.trim());
            }
        }
        finally
        {
            if (lnr != null) lnr.close();
            if (reader != null) reader.close();
        }

        return result;
    }

    /**
     * @param path      Path to the wordlist
     * @param wordfile  Name of the wordlist
     *
     * @deprecated Use {@link #getWordSet(File)} getWordSet(File)} instead
     */
    static public Hashtable getWordtable(String path, String wordfile)
        throws IOException
    {
        return getWordtable(new File(path, wordfile));
    }

    /**
     * @param wordfile  Complete path to the wordlist
     *
     * @deprecated Use {@link #getWordSet(File)} getWordSet(File)} instead
     */
    static public Hashtable getWordtable(String wordfile)
        throws IOException
    {
        return getWordtable(new File(wordfile));
    }

    /**
     * @param wordfile  File object that points to the wordlist
     *
     * @deprecated Use {@link #getWordSet(File)} getWordSet(File)} instead
     */
    static public Hashtable getWordtable(File wordfile)
        throws IOException
    {
        CharArraySet wordSet = getWordSet(wordfile);
        Hashtable result = makeWordTable(wordSet);
        return result;
    }

    /**
     * Builds a wordlist table, using words as both keys and values
     * for backward compatibility.
     *
     * @param wordSet   stopword set
     *
     * @deprecated Use {@link #getWordSet(File)} getWordSet(File)} instead
     */
    private static Hashtable makeWordTable(CharArraySet wordSet)
    {
        Hashtable table = new Hashtable();

        for (Iterator iter = wordSet.iterator(); iter.hasNext(); )
        {
            String word = (String)iter.next();
            table.put(word, word);
        }

        return table;
    }

    /**
     * Reads a stemsdictionary. Each line contains:
     * word \t stem
     * i.e. tab seperated.
     *
     * @return Stem dictionary that overrules the stemming algorithm.
     */
    public static HashMap getStemDict(File wordstemfile)
    {
        if (wordstemfile == null)
        {
            return new HashMap();
        }

        HashMap result = new HashMap();

        try
        {
            LineNumberReader lnr =
                new LineNumberReader(new FileReader(wordstemfile));

            String line;
            String[] wordstem;
            while ((line = lnr.readLine()) != null)
            {
                wordstem = line.split("\t", 2);
                result.put(wordstem[0], wordstem[1]);
            }
        }
        catch (IOException e)
        {
        }

        return result;
    }
}
