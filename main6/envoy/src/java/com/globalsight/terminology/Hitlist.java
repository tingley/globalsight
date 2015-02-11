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

package com.globalsight.terminology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * A hitlist represents the result of a termbase search. It is a collection of
 * hits, which in turn is the set of found term, score, term id (and entry id).
 * </p>
 */
public class Hitlist implements Serializable
{
    private static final long serialVersionUID = 8578175454522257144L;

    public static final class Hit implements Serializable
    {
        private static final long serialVersionUID = 5099612869897819960L;
        public String m_term = "";
        public long m_conceptId = 0;
        public long m_termId = 0;
        public int m_score = 0;
        public String m_xml = "";

        public Hit(String p_term, long p_conceptId, long p_termId, int p_score,
                String p_xml)
        {
            m_term = p_term;
            m_conceptId = p_conceptId;
            m_termId = p_termId;
            m_score = p_score;
            m_xml = p_xml;
        }

        public String getTerm()
        {
            return m_term;
        }

        public long getConceptId()
        {
            return m_conceptId;
        }

        public long getTermId()
        {
            return m_termId;
        }

        public int getScore()
        {
            return m_score;
        }

        public String getXml()
        {
            StringBuffer result = new StringBuffer(256);

            result.append("<hit>");

            result.append("<score>");
            result.append(m_score);
            result.append("</score>");

            // because "&lt;" after encodeXmlEntities will become "&amp;lt;", so
            // so first pre-processing it.
            m_term = m_term.replaceAll("&lt;", "<");
            m_term = m_term.replaceAll("&gt;", ">");
            m_term = m_term.replaceAll("&apos;", "\'");
            m_term = m_term.replaceAll("&quot;", "\"");
            result.append("<term>");
            result.append(EditUtil.encodeXmlEntities(m_term));
            result.append("</term>");

            result.append("<conceptid>");
            result.append(m_conceptId);
            result.append("</conceptid>");

            result.append("<termid>");
            result.append(m_termId);
            result.append("</termid>");

            result.append("</hit>");

            return result.toString();
        }

        public String getDescXML()
        {
            return m_xml;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((m_term == null) ? 0 : m_term.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Hit other = (Hit) obj;
            if (m_term == null)
            {
                if (other.m_term != null)
                    return false;
            }
            else if (!m_term.equals(other.m_term))
                return false;
            return true;
        }

    }

    private static class ScoreComparator implements Comparator
    {
        public int compare(Object p_object1, Object p_object2)
        {
            Hit hit1 = (Hit) p_object1;
            Hit hit2 = (Hit) p_object2;

            int score1 = hit1.getScore();
            int score2 = hit2.getScore();

            if (score1 > score2)
            {
                return -1;
            }
            else if (score1 < score2)
            {
                return 1;
            }

            return 0;
        }
    }

    private static Comparator s_scoreComparator = new ScoreComparator();

    private ArrayList m_hits = new ArrayList();

    public Hitlist()
    {
    }

    public void clear()
    {
        m_hits.clear();
    }

    /**
     * Allows caller to reverse the final list if results were added in reverse
     * (backward) order instead of forward order.
     */
    public void reverse()
    {
        Collections.reverse(m_hits);
    }

    /**
     * Allows caller to add results from multiple queries and resort the list by
     * score.
     */
    public void sortByScore()
    {
        SortUtil.sort(m_hits, s_scoreComparator);
    }

    public void add(Hit p_hit)
    {
        m_hits.add(p_hit);
    }

    public void add(String p_term, long p_cid, long p_tid, int p_score,
            String p_xml)
    {
        m_hits.add(new Hit(p_term, p_cid, p_tid, p_score, p_xml));
    }

    public ArrayList getHits()
    {
        return m_hits;
    }

    public Iterator iterator()
    {
        return m_hits.iterator();
    }

    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<hitlist>");
        result.append("<hits>");

        for (int i = 0, max = m_hits.size(); i < max; i++)
        {
            Hit hit = (Hit) m_hits.get(i);

            result.append(hit.getXml());
        }

        result.append("</hits>");
        result.append("</hitlist>");

        return result.toString();
    }

}
