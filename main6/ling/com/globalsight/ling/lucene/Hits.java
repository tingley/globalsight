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

package com.globalsight.ling.lucene;

import com.globalsight.ling.lucene.IndexDocument;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Hits holds the list of hits returned from Lucene or whatever other
 * engine is used for fuzzy indexes. The original text (term, segment,
 * etc) can either be retrieved from the index, or added to the hit by
 * the caller.
 */
public class Hits
{
    //
    // Internal Classes
    //
    static public class Hit
    {
        public long m_mainId;
        public long m_subId;
        public String m_text;
        public float m_score;

        /*package-private*/Hit (String p_mainId, String p_subId,
            String p_text, float p_score)
        {
            m_mainId = Long.parseLong(p_mainId);
            m_subId = Long.parseLong(p_subId);
            m_text = p_text;
            m_score = p_score;
        }

        public long getMainId()
        {
            return m_mainId;
        }

        public long getSubId()
        {
            return m_subId;
        }

        public String getText()
        {
            return m_text;
        }

        /**
         * If text wasn't stored in the index, caller can retrieve it
         * from another data source and store it in the hit.
         */
        public void setText(String p_text)
        {
            m_text = p_text;
        }

        public float getScore()
        {
            return m_score;
        }
    }

    //
    // Members
    //
    private ArrayList m_hits;

    /*package-private*/
    Hits (IndexSearcher searcher, ScoreDoc[] p_hits,
        int end, int begin, float p_minScore, String text) throws IOException
    {
        dealHits(searcher, p_hits, end, begin, p_minScore, text);
    }
    
    Hits(IndexSearcher searcher, ScoreDoc[] p_hits, int end, float p_minScore,
            String text) throws IOException
    {
        dealHits(searcher, p_hits, end, 0, p_minScore, text);
    }

    private void dealHits(IndexSearcher searcher, ScoreDoc[] p_hits, int end,
            int begin, float p_minScore, String text) throws IOException
    {
        m_hits = new ArrayList(end);
        text = text.toLowerCase();
        
        if(p_hits.length < end) {
            end = p_hits.length;
        }

        for (int i = begin, max = end; i < max; i++)
        {
            float score = p_hits[i].score;

            if (score < p_minScore)
            {
                break;
            }

            Document doc =  searcher.doc(p_hits[i].doc);
            
            String str = doc.get(IndexDocument.TEXT).toLowerCase();

            if(text.indexOf(str) > -1 
                    || str.indexOf(text) > -1) {
                m_hits.add(new Hit(doc.get(IndexDocument.MAINID),
                    doc.get(IndexDocument.SUBID), doc.get(IndexDocument.TEXT),
                    score));
            }
        }
    }

    public int size()
    {
        return m_hits.size();
    }

    public Hit getHit(int i)
    {
        return (Hit)m_hits.get(i);
    }

    public long getMainId(int i)
    {
        return getHit(i).m_mainId;
    }

    public long getSubId(int i)
    {
        return getHit(i).m_subId;
    }

    public String getText(int i)
    {
        return getHit(i).m_text;
    }

    public float getScore(int i)
    {
        return getHit(i).m_score;
    }
}
