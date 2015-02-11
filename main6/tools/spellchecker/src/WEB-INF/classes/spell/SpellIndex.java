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

package spell;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.*;

/**
 * <p>Main Spell Checker class (initially inspired by the David
 * Spencer code)</p>
 *
 * <pre>
 * SpellIndex sc = new SpellIndex (spellDirectory);
 *
 * //To index a field of a user index
 * sc.indexDictionary(new LuceneDictionary(my_lucene_reader, a_field));
 *
 * //To index a file containing words (in UTF-8)
 * sc.indexDictionary(new FileDictionary(new File("myfile.txt")));
 * </pre>
 *
 * @author Nicolas Maisonneuve
 * @author Cornelis Van Der Laan
 * @version 2.0
 */
public class SpellIndex
{
    static public int MINWORDLEN = 3;

    /**
     * Field name for each word in the ngram index.
     */
    public static final String F_WORD = "word";

    /**
     * The spell index directory.
     */
    Directory spellindex;

    private static Analyzer s_analyzer = new WhitespaceAnalyzer();

    /**
     * Boost values for start and end grams.
     */
    private float bStart = 2.0f;
    private float bEnd = 1.0f;

    float min = 0.5f;

    //
    // Constructor
    //

    public SpellIndex(Directory gramIndex)
    {
        this.setSpellIndex(gramIndex);
    }

    public void setSpellIndex(Directory spellindex)
    {
        this.spellindex = spellindex;
    }

    /**
     * Set the accuracy 0 &lt; min &lt; 1, default 0.5.
     * @param min float
     */
    public void setAccuracy(float min)
    {
        this.min = min;
    }

    /**
     * Suggest similar words.
     * @param word String the word you want a spell check done on.
     * @param num_sug int the number of suggest words
     * @throws IOException
     * @return list of SuggestWord objects, sorted by score
     */
    public ArrayList suggestSimilar(String word, int num_sug)
        throws IOException
    {
        return this.suggestSimilar(word, num_sug, null, null, false);
    }


    /**
     * Suggest similar words (restricted to a field of a user index or not).
     *
     * @param word String the word you want a spell check done on
     * @param num_sug int the number of suggest words
     * @param ir the indexReader of the user index (can be null, see
     * parameter "field")
     * @param field String the field of the user index: if field is
     * not null, the suggested words are restricted to the words
     * present in this field.
     * @param morePopular boolean return only the suggest words that
     * are more frequent than the searched word (only if restricted
     * mode = (indexReader!=null and field!=null)
     * @throws IOException
     * @return String[] the list of the suggest words sorted by these
     * two criteria: 1) the edit distance, 2) (only if restricted
     * mode) the popularity of the suggest words in the field of the
     * user index
     */
    public ArrayList suggestSimilar(String word, int num_sug,
        IndexReader ir, String field, boolean morePopular)
        throws IOException
    {
        final TRStringDistance sd = new TRStringDistance(word);
        final int wordlen = word.length();

        final int goalFreq = (morePopular && ir != null) ?
            ir.docFreq(new Term(field, word)) : 0;

        // Return the word if it exists in the index and caller
        // doesn't want a more popular word.
        if (!morePopular && goalFreq > 0)
        {
            ArrayList result = new ArrayList();
            SuggestWord sugg = new SuggestWord();
            sugg.string = word;
            sugg.score = 1.0f;
            result.add(sugg);
            return result;
        }

        // Don't query index if word is too short
        if (wordlen < MINWORDLEN)
        {
            return new ArrayList();
        }

        BooleanQuery query = new BooleanQuery();
        String[] grams;
        String key;

        for (int ng = getMin(wordlen); ng <= getMax(wordlen); ng++)
        {
            key = "gram" + ng; // form key

            // form word into ngrams (allow dups too)
            grams = formGrams(word, ng);

            if (grams.length == 0)
            {
                continue; // hmm
            }

            // should we boost prefixes?
            if (bStart > 0)
            {
                // matches start of word
                add(query, "start" + ng, grams[0], bStart);
            }

            // should we boost suffixes?
            if (bEnd > 0)
            {
                // matches end of word
                add(query, "end" + ng, grams[grams.length-1], bEnd);
            }

            for (int i = 0; i < grams.length; i++)
            {
                add(query, key, grams[i]);
            }
        }

        IndexSearcher searcher = new IndexSearcher(this.spellindex);
        Hits hits = searcher.search(query);
        SuggestWordQueue sugqueue = new SuggestWordQueue(num_sug);

        // go thru more than 'maxr' matches in case the distance filter triggers
        int stop = Math.min(hits.length(), 10 * num_sug);
        SuggestWord sugword = new SuggestWord();
        for (int i = 0; i < stop; i++)
        {
            sugword.string = hits.doc(i).get(F_WORD); // get orig word

            if (sugword.string == word)
            {
                // don't suggest a word for itself, that would be silly
                continue;
            }

            //edit distance/normalize with the min word length
            sugword.score = 1.0f - ((float) sd.getDistance(sugword.string) /
                Math.min(sugword.string.length(), wordlen));

            if (sugword.score < min)
            {
                continue;
            }

            // use the user index
            if (ir != null)
            {
                // freq in the index
                sugword.freq = ir.docFreq(new Term(field, sugword.string));

                // don't suggest a word that is not present in the field
                if ((morePopular && goalFreq > sugword.freq) ||
                    sugword.freq < 1)
                {
                    continue;
                }
            }

            sugqueue.insert(sugword);

            if (sugqueue.size() == num_sug)
            {
                //if queue full, maintain the min score
                min = ((SuggestWord) sugqueue.top()).score;
            }

            sugword = new SuggestWord();
        }

        // convert to ArrayList
        ArrayList result = new ArrayList(sugqueue.size());

        for (int i = sugqueue.size() - 1; i >= 0; i--)
        {
            result.add(sugqueue.pop());
        }

        searcher.close();

        return result;
    }


    /**
     * Add a clause to a boolean query.
     */
    private static void add(BooleanQuery q, String k, String v, float boost)
    {
        Query tq = new TermQuery(new Term(k, v));
        tq.setBoost(boost);
        q.add(new BooleanClause(tq, false, false));
    }


    /**
     * Add a clause to a boolean query.
     */
    private static void add(BooleanQuery q, String k, String v)
    {
        q.add(new BooleanClause(new TermQuery(new Term(k, v)), false, false));
    }


    /**
     * Form all ngrams for a given word.
     * @param text the word to parse
     * @param ng the ngram length e.g. 3
     * @return an array of all ngrams in the word and note that
     * duplicates are not removed
     */
    private static String[] formGrams(String text, int ng)
    {
        int len = text.length();
        String[] res = new String[len - ng + 1];

        for (int i = 0; i < len - ng + 1; i++)
        {
            res[i] = text.substring(i, i + ng);
        }

        return res;
    }


    /**
     * Returns the number of words in the index.
     */
    public int getWordCount()
        throws IOException
    {
        IndexReader reader = getReader();
        int result = reader.numDocs();
        reader.close();

        return result;
    }


    public ArrayList getWords()
        throws IOException
    {
        ArrayList result = new ArrayList();

        IndexReader reader = getReader();

        TermEnum terms = reader.terms(new Term(F_WORD, ""));
        while (terms.term() != null)
        {
            result.add(terms.term().text());
            terms.next();
        }

        terms.close();
        reader.close();

        return result;
    }

    /**
     * Tests if the word exist in the index.
     */
    public boolean exist(String word)
        throws IOException
    {
        IndexReader reader = getReader();
        boolean result = reader.docFreq(new Term(F_WORD, word)) > 0;
        reader.close();

        return result;
    }


    private IndexReader getReader()
        throws IOException
    {
        return IndexReader.open(spellindex);
    }


    /**
     * Deletes all words from this index.
     */
    public void clearIndex()
        throws IOException
    {
        IndexReader.unlock(spellindex);
        IndexWriter writer = new IndexWriter(spellindex, null, true);
        writer.close();
    }


    /**
     * Index a Dictionary from either a file or another Lucene index.
     * @param dict the dictionary to index
     * @throws IOException
     */
    public void indexDictionary(Dictionary dict)
        throws IOException
    {
        RAMDirectory ramdir = new RAMDirectory();
        IndexWriter ramwriter = new IndexWriter(ramdir, s_analyzer, true);
        ramwriter.mergeFactor = 10000;

        Iterator iter = dict.getWordsIterator();
        while (iter.hasNext())
        {
            String word = (String) iter.next();

            // Don't add existing words
            if (this.exist(word))
            {
                // if the word already exist in the gramindex
                continue;
            }

            // add the word and index it if it's long enough
            Document doc = createDocument(word);
            ramwriter.addDocument(doc);
        }

        ramwriter.optimize();

        IndexReader.unlock(spellindex);

        IndexWriter writer = new IndexWriter(spellindex, s_analyzer, false);

        writer.addIndexes(new Directory[] { ramdir } );
        writer.optimize();
        writer.close();

        ramwriter.close();
        ramdir.close();
    }


    /**
     * Add a word to the index.
     * @throws IOException
     */
    public void addWord(String p_word)
        throws IOException
    {
        if (!this.exist(p_word))
        {
            // IndexReader.unlock(spellindex);

            IndexWriter writer = new IndexWriter(spellindex, s_analyzer, false);

            // add the word and index it if it's long enough
            Document doc = createDocument(p_word);
            writer.addDocument(doc);

            // close writer
            writer.optimize();
            writer.close();
        }
    }


    /**
     * Removes a word from the index.
     * @throws IOException
     */
    public void removeWord(String p_word)
        throws IOException
    {
        if (p_word == null || p_word.length() == 0)
        {
            return;
        }

        IndexReader reader = getReader();
        reader.delete(new Term(F_WORD, p_word));
        reader.close();

        IndexWriter writer = new IndexWriter(spellindex, s_analyzer, false);
        writer.optimize();
        writer.close();
    }


    static private int getMin (int l)
    {
        if (l > 5)
        {
            return 3;
        }
        if (l == 5)
        {
            return 2;
        }
        return 1;
    }


    static private int getMax (int l)
    {
        if (l > 5)
        {
            return 4;
        }
        if (l == 5)
        {
            return 3;
        }
        return 2;
    }


    static private Document createDocument (String text)
    {
        Document doc = new Document();
        doc.add(Field.Keyword(F_WORD, text)); // orig term

        int len = text.length();
        if (len >= MINWORDLEN)
        {
            addGram(text, doc, getMin(len), getMax(len));
        }

        return doc;
    }


    static private void addGram (String text, Document doc, int ng1, int ng2)
    {
        int len = text.length();
        for (int ng = ng1; ng <= ng2; ng++)
        {
            String key = "gram" + ng;
            String end = null;
            for (int i = 0; i < len - ng + 1; i++)
            {
                String gram = text.substring(i, i + ng);
                doc.add(Field.Keyword(key, gram));
                if (i == 0)
                {
                    doc.add(Field.Keyword("start" + ng, gram));
                }
                end = gram;
            }
            if (end != null)
            {
                // may not be present if len==ng1
                doc.add(Field.Keyword("end" + ng, end));
            }
        }
    }

    protected void finalize ()
        throws Throwable
    {
    }
}
