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

package com.globalsight.terminology.termleverager;

import com.globalsight.terminology.termleverager.TermLeverageMatch;

import com.globalsight.ling.tm.TuvLing;
import com.globalsight.everest.tuv.Tuv;

import java.util.*;
import java.io.Serializable;

/**
 * Helper data class that wraps the match results for all TUVs.
 *
 * The tree is as follows:
 *
 * For each TUV, there is a list of MatchRecords:
 *
 * TUV --> [list of MatchRecord]
 *
 *   MatchRecord = [TUV, score, priority, pointer to SourceTerm]
 *
 *     SourceTerm = [tbid, cid, tid, term, list of TargetTerms]
 *
 *       TargetTerm = [tid, term, locale, priority]
 *
 * First, for each TUV, the MatchRecords and SourceTerms are created.
 * SourceTerms are cached (like String.intern()) so that only one
 * instance of each SourceTerm is in memory, not many redundant copies.
 *
 * In the second phase, the TargetTerms are attached to the SourceTerms,
 * again caching the string objects to conserve memory.
 *
 * Other interesting properties of this model:
 * - a source term exists only once [tbid,tid]
 * - target terms also exist only once for all source matches
 *   from the same concept [tbid,cid] because all source matches
 *   are synonyms of each other and have the same translations.
 */
public class TermLeverageResult
    implements Serializable
{
    //
    // Private Classes
    //

    static public class MatchRecordList extends ArrayList
    {
        public String toString()
        {
            StringBuffer result = new StringBuffer();

            for (int i = 0, max = size(); i < max; i++)
            {
                MatchRecord match = (MatchRecord)get(i);

                result.append(match.toString());
                result.append("\n");
            }

            return result.toString();
        }
    };

    static public class MatchRecord
    {
        public TuvLing m_tuv;
        public int m_score;
        public SourceTerm m_sourceTerm;

        public MatchRecord(TuvLing p_tuv, int p_score)
        {
            m_tuv = p_tuv;
            m_score = p_score;
        }

        public int getScore()
        {
            return m_score;
        }

        public void setScore(int p_arg)
        {
            m_score = p_arg;
        }

        public long getSourceTuvId()
        {
            return m_tuv.getId();
        }

        public long getTermbaseId()
        {
            return m_sourceTerm.m_tbid;
        }

        public long getConceptId()
        {
            return m_sourceTerm.m_cid;
        }
        
        public String getSourceDescXML()
        {
        	return m_sourceTerm.m_xml;
        }

        public long getMatchedSourceTermId()
        {
            return m_sourceTerm.m_tid;
        }

        public String getMatchedSourceTerm()
        {
            return m_sourceTerm.m_term;
        }

        public SourceTerm getSourceTerm()
        {
            return m_sourceTerm;
        }

        public String toString()
        {
            StringBuffer result = new StringBuffer();

            result.append("Match Record for TUV " + m_tuv.getId() +
                " score " + m_score + "\n");
            result.append(m_sourceTerm.toString("  "));

            return result.toString();
        }
    }

    static public class SourceTerm
        implements Comparable
    {
        public long m_tbid;
        public long m_cid;
        public long m_tid;
        public String m_term;
        public String m_xml;
        public ArrayList m_targetTerms;

        public SourceTerm(long p_tbid, long p_cid, long p_tid, String p_term, String p_xml)
        {
            m_tbid = p_tbid;
            m_cid = p_cid;
            m_tid = p_tid;
            m_term = p_term;
            m_xml = p_xml;
        }

        public ArrayList getTargetTerms()
        {
            return m_targetTerms;
        }

        public void setTargetTerms(ArrayList p_arg)
        {
            m_targetTerms = p_arg;
        }

        public int compareTo(Object p_other)
        {
            SourceTerm other = (SourceTerm)p_other;

            long cmp;

            cmp = this.m_tbid - other.m_tbid;
            if (cmp < 0)
            {
                return -1;
            }
            else if (cmp > 0)
            {
                return 1;
            }

            cmp = this.m_cid - other.m_cid;
            if (cmp < 0)
            {
                return -1;
            }
            else if (cmp > 0)
            {
                return 1;
            }

            cmp = this.m_tid - other.m_tid;
            if (cmp < 0)
            {
                return -1;
            }
            else if (cmp > 0)
            {
                return 1;
            }

            return 0;
        }

        public boolean equals(Object p_other)
        {
            SourceTerm other = (SourceTerm)p_other;

            if (m_tbid == other.m_tbid &&
                m_cid  == other.m_cid &&
                m_tid  == other.m_tid)
            {
                return true;
            }

            return false;
        }

        public String toString()
        {
            return toString("");
        }

        public String toString(String p_prefix)
        {
            StringBuffer result = new StringBuffer();

            result.append(p_prefix);
            result.append("Source Term [");
            result.append(m_tbid);
            result.append(",");
            result.append(m_cid);
            result.append(",");
            result.append(m_tid);
            result.append("] `");
            result.append(m_term);
            result.append("'\n");

            if (m_targetTerms != null)
            {
                for (int i = 0, max = m_targetTerms.size(); i < max; i++)
                {
                    TargetTerm target = (TargetTerm)m_targetTerms.get(i);

                    result.append(p_prefix);
                    result.append(target.toString("  "));
                    result.append("\n");
                }
            }

            return result.toString();
        }
    }

    static public class TargetTerm
    {
        public long m_tid;
        public String m_term;
        public String m_locale; // match's locale from TB, not the page's locale

        // TODO: term penalties not implemented yet.
        // public String m_status; // proposed, reviewed, approved
        // public String m_usage;  // preferred, admitted, deprecated

        public TargetTerm(long p_tid, String p_term, String p_locale)
        {
            m_tid = p_tid;
            m_term = p_term;
            m_locale = p_locale;
        }

        public String getLocale()
        {
            return m_locale;
        }

        public long getMatchedTargetTermId()
        {
            return m_tid;
        }

        public String getMatchedTargetTerm()
        {
            return m_term;
        }

        public String getUsage()
        {
            // return m_usage;
            return "";
        }

        public String getStatus()
        {
            // return m_status;
            return "";
        }

        public String toString()
        {
            return toString("");
        }

        public String toString(String p_prefix)
        {
            StringBuffer result = new StringBuffer();

            result.append(p_prefix);
            result.append("Target Term [");
            result.append(m_tid);
            result.append(",");
            result.append(m_locale);
            result.append("] `");
            result.append(m_term);
            result.append("'");

            return result.toString();
        }
    }

    //
    // Members
    //

    // [tuv id -> MatchRecordList (= ArrayList of MatchRecord)]
    private HashMap m_matches = new HashMap();

    // Cache for SourceTerms (works like String.intern()).
    // Could be a HashMap but when registering target terms, range
    // queries on the concept alone [tbid,cid] are needed to associate
    // target matches with all source terms of the same concept.
    private TreeMap m_sourceCache = new TreeMap();

    //
    // Constructor
    //

    public TermLeverageResult()
    {
    }

    //
    // Public Methods
    //

    public void addSourceHit(TuvLing p_tuv, long p_tbid, long p_cid, long p_tid,
        String p_term, int p_score, String p_xml)
    {
        MatchRecord rec = new MatchRecord(p_tuv, p_score);

        // Find a cached SourceTerm or register a new one. This works
        // like String.intern() in that it only keeps a single SourceTerm
        // object in memory (the temporary SourceTerm tmp is garbage).
        SourceTerm tmp = new SourceTerm(p_tbid, p_cid, p_tid, p_term, p_xml);
        SourceTerm src = (SourceTerm)m_sourceCache.get(tmp);

        if (src == null)
        {
            src = tmp;

            m_sourceCache.put(src, src);
        }

        rec.m_sourceTerm = src;

        Object key = p_tuv.getIdAsLong();
        MatchRecordList records = (MatchRecordList)m_matches.get(key);
        if (records == null)
        {
            records = new MatchRecordList();
            m_matches.put(key, records);
        }

        records.add(rec);
    }

    /**
     * Adds a target term to all source terms that it is a translation of.
     */
    public void addTargetTerm(long p_tbid, long p_cid, long p_tid,
        String p_term, String p_locale, String p_xml)
    {
        // Find the first source match and put the target in its
        // target list, then update all other source matches in the
        // same concept with the same target list (since all target
        // translation of all source synonyms are the same).
        ArrayList targets = null;

        // Enumerate source matches that are synonyms in the same concept.
        SourceTerm keyStart = new SourceTerm(p_tbid, p_cid, 0, null, p_xml);
        SourceTerm keyEnd   = new SourceTerm(p_tbid, p_cid, Long.MAX_VALUE, null, p_xml);

        SortedMap sources = m_sourceCache.subMap(keyStart, keyEnd);
        for (Iterator it = sources.values().iterator(); it.hasNext(); )
        {
            SourceTerm src = (SourceTerm)it.next();

            // First time around, fetch the list of target terms.
            if (targets == null)
            {
                targets = src.getTargetTerms();

                // First target term for this concept, create new list.
                if (targets == null)
                {
                    targets = new ArrayList();
                    src.setTargetTerms(targets);
                }

                // If target term is already in the list, abort the whole show.
                // This is a no-op for the first term of this source.
                for (int i = 0, max = targets.size(); i < max; i++)
                {
                    TargetTerm tmp = (TargetTerm)targets.get(i);

                    if (tmp.m_tid == p_tid)
                    {
                        return;
                    }
                }

                // Append the current target to the list of translations.
                TargetTerm target = new TargetTerm(p_tid, p_term, p_locale);

                targets.add(target);
            }

            // Associate targets with current source term.
            src.setTargetTerms(targets);
        }
    }

    public int size()
    {
        return m_matches.size();
    }

    public boolean hasMatchForTuv(Long p_id)
    {
        return m_matches.containsKey(p_id);
    }

    public boolean hasMatchForTuv(TuvLing p_tuv)
    {
        return hasMatchForTuv(p_tuv.getIdAsLong());
    }

    public boolean hasMatchForTuv(Tuv p_tuv)
    {
        return hasMatchForTuv(p_tuv.getIdAsLong());
    }

    /**
     * Returns a reference to the match list for a given TUV that
     * can (and will be) modified by TermLeverager.
     */
    public MatchRecordList getMatchesForTuv(Long p_id)
    {
        return (MatchRecordList)m_matches.get(p_id);
    }

    public MatchRecordList getMatchesForTuv(TuvLing p_tuv)
    {
        return getMatchesForTuv(p_tuv.getIdAsLong());
    }

    public MatchRecordList getMatchesForTuv(Tuv p_tuv)
    {
        return getMatchesForTuv(p_tuv.getIdAsLong());
    }

    public ArrayList getAllTuvIds()
    {
        ArrayList result = new ArrayList();

        result.addAll(m_matches.keySet());

        return result;
    }

    public Iterator getRecordIterator()
    {
        return m_matches.values().iterator();
    }

    /**
     * Returns the matches for all source segments as one big list.
     * This is probably only useful for single-tuv queries and debugging.
     * In any case, the result list is grouped by TUV.
     */
    public ArrayList getAllMatchRecords()
    {
        ArrayList result = new ArrayList();

        for (Iterator it = m_matches.values().iterator(); it.hasNext(); )
        {
            result.addAll((ArrayList)it.next());
        }

        return result;
    }

    public void removeSourceWithoutTarget()
    {
        for (Iterator it = getRecordIterator(); it.hasNext(); )
        {
            MatchRecordList records = (MatchRecordList)it.next();

            for (int i = 0, max = records.size(); i < max; i++)
            {
                MatchRecord match = (MatchRecord)records.get(i);

                ArrayList targets = match.getSourceTerm().getTargetTerms();

                if (targets == null || targets.size() == 0)
                {
                    records.remove(i);
                    --i;
                    --max;
                }
            }

            // No matches for TUV, remove from result list.
            if (records.size() == 0)
            {
                it.remove();
            }
        }
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        for (Iterator it = getRecordIterator(); it.hasNext(); )
        {
            MatchRecordList records = (MatchRecordList)it.next();

            result.append(records.toString());
        }

        return result.toString();
    }

    /**
     * For dynamic leveraging: expands this object into a flat list of
     * TermLeverageMatch objects.
     *
     * DO NOT call this method when dealing with large data because it
     * will become huge.
     */
    public ArrayList<TermLeverageMatchResult> toTermLeverageMatchResult()
    {
        ArrayList<TermLeverageMatchResult> result = new ArrayList<TermLeverageMatchResult>();

        // Copy the compact in-memory representation into the object
        // that the editor expects.
        for (Iterator it = getRecordIterator(); it.hasNext(); )
        {
            // Write out the priority in increasing order but spread it
            // across all target locales: [en]0,1[fr]2,3[en]4,5[fr]6,7.
//            int priority = 0;

            ArrayList records = (ArrayList)it.next();
            for (int ii = 0, maxi = records.size(); ii < maxi; ii++)
            {
                MatchRecord match = (MatchRecord)records.get(ii);

                TermLeverageMatchResult o = new TermLeverageMatchResult();

                o.setSource(match.getMatchedSourceTerm(),
                    match.getConceptId(), match.getMatchedSourceTermId(),
                    match.getScore(), match.getSourceDescXML());

                ArrayList targets = match.getSourceTerm().getTargetTerms();
                for (int jj = 0, maxj = targets.size(); jj < maxj; jj++)
                {
                    TargetTerm target = (TargetTerm)targets.get(jj);

                    o.addTarget(target.getMatchedTargetTerm(),
                        match.getConceptId(), target.getMatchedTargetTermId(),
                        match.getScore(), "");
                }

                result.add(o);
            }

            // Can have only one results for one source TUV.
            break;
        }

        return result;
    }

}
