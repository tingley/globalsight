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

package com.globalsight.terminology.termleverager.recognizer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecord;
import com.globalsight.terminology.termleverager.TermLeverageResult.SourceTerm;
import com.globalsight.terminology.termleverager.TermLeverageResult.TargetTerm;

/**
 *
 */
public class TermRecognizer
{
    static private final double PASS_THRESHOLD = 0.69;
    static private final int MINIMUM_MATCH_SIZE = 4;
    static private final int MAXIMUM_OVERLAPS = 1;

    private class BreakPosition
    {
        public int m_start;
        public int m_end;

        public BreakPosition(int p_start, int p_end)
        {
            m_start = p_start;
            m_end = p_end;
        }
    }

    private Locale m_locale;
    private GlobalsightBreakIterator m_wordBreaker;
    private TermWithinSentenceAligner m_termAligner;

    //
    // Constructor
    //

    public TermRecognizer(Locale p_srcLocale,
        GlobalsightBreakIterator p_wordBreaker)
    {
        m_locale = p_srcLocale;
        m_wordBreaker = p_wordBreaker;
        m_termAligner = new TermWithinSentenceAligner();
    }

    //
    // Public Methods
    //

    /**
     * Weed out noisy terms that got matched in this segment.
     */
    public void recognizeTerms(ArrayList p_matches, String p_segment)
    {
        String segment = p_segment.toLowerCase(m_locale);

        ArrayList noisyTerms = new ArrayList();
        ArrayList wordBreakPositions = null;

        // Word break if the locale uses whitespace between words.
        if (hasSpaceBetweenWords(m_locale))
        {
            segment = " " + segment + " ";
            wordBreakPositions = findWordBreakPositions(segment);
        }

        // Examine each source term.
        for (Iterator it = p_matches.iterator(); it.hasNext(); )
        {
            MatchRecord rec = (MatchRecord)it.next();

            String term = rec.getMatchedSourceTerm().toLowerCase(m_locale);

            // Fuzzy search terms in the segment via string aligment.
            if (!fuzzySearch(rec, segment, term, wordBreakPositions))
            {
                noisyTerms.add(rec);
            }
        }

        // Remove noisy terms if any.
        if (!noisyTerms.isEmpty())
        {
            p_matches.removeAll(noisyTerms);
        }
    }

    //
    // Private Methods
    //

    private ArrayList findWordBreakPositions(String p_segment)
    {
        ArrayList result = new ArrayList();
        int end;
        int start;

        m_wordBreaker.setText(p_segment);

        start = m_wordBreaker.first();
        for (end = m_wordBreaker.next();
             end != GlobalsightBreakIterator.DONE;
             start = end, end = m_wordBreaker.next())
        {
            //result.addLast(new BreakPosition(start, end));

            //for (int p = start; p < end; p++)
            {
                //we found at least one "letter" - must be a "word".
                //if (Character.isLetter(p_segment.charAt(p)))
                {
                    result.add(new BreakPosition(start, end));
                    //break;
                }
            }
        }

        return result;
    }

    private boolean fuzzySearch(MatchRecord p_rec, String p_segment,
        String p_term, ArrayList p_wordBreakPositions)
    {
        TermAlignment alignment;
        int termLength = p_term.length();

        // keep track of all unique character alignments in the term
        BitSet uniqueMatches = new BitSet(termLength);

        // keep track of "overlaps" - i.e., multiple alignments
        // on the same character
        BitSet allOverlaps = new BitSet(termLength);

        // overlaps for the current alignment
        BitSet currentOverlaps;

        // alignment statistics
        int alignmentsWithOverlapCount = 0;
        int overlapCount = 0;
        int uniqueMatchCount = 0;
        int currentMatchCount = 0;
        double score = 0;

        // all the valid alignments
        HashMap validAlignments = new HashMap(10);
        String segmentMatch = null;

        // visit every alignment until we see a stopping condition
        // at that point record the score and give it a pass or fail
        m_termAligner.reset();

        boolean stop = false;
        boolean lessThanOrEqual = false;
        boolean greaterThanOrEqual = false;
        boolean duplicateMatch = false;

        // Japanese and Chinese only get one alignment pass (no
        // reordering allowed).
        boolean onePass = !wordReorderingAllowed(m_locale);

        while (!stop)
        {
            alignment = m_termAligner.getNextAlignment(p_segment, p_term);

            duplicateMatch = false;

            if (alignment != null)
            {
                currentOverlaps = alignment.termOverlaps(uniqueMatches);
                //allOverlaps.or(currentOverlaps);

                // Count as unique matches only if the match
                // is >= MINIMUM_MATCH_SIZE. Anything smaller is
                // probably noise.
                currentMatchCount = alignment.getTotalMatches();
                segmentMatch = p_segment.substring(alignment.getStartPosition(),
                    alignment.getEndPosition()).trim();
                lessThanOrEqual =
                    lessThanOrEqualToMinimumMatchSize(alignment, p_rec);
                greaterThanOrEqual =
                    greaterThanOrEqualToMinimumMatchSize(alignment, p_rec);

                if (validAlignments.containsKey(segmentMatch))
                {
                    duplicateMatch = true;
                }

                // overlaps are usually noisy matching substrings
                // we keep a threshold and stop finding matches
                // after we hit >= MAXIMUM_OVERLAPS
                overlapCount = countSetBits(currentOverlaps);

                // don't count as overlap if it's a duplicate
                // may be the same word twice in the sentence
                if (overlapCount > 0 && !duplicateMatch)
                {
                    alignmentsWithOverlapCount++;
                }

                int numBits = countSetBits(alignment.getTermMatches());
                score = (double)numBits/(double)termLength;
                // don't count the matches if we are smaller than MINIMUM_MATCH_SIZE
                // but if we've already got 100% let it through no matter the size
                // so we don't miss small terms (IE, XML etc..)
                if (greaterThanOrEqual || score >= 1.0
                    /*&& alignmentsWithOverlapCount < 1*/)
                {
                    uniqueMatches.or(alignment.getTermMatches());

                    if (!duplicateMatch)
                    {
                        validAlignments.put(segmentMatch, alignment);
                    }
                }

                uniqueMatchCount = countSetBits(uniqueMatches);
                score = (double)uniqueMatchCount / (double)termLength;
            }

            // stopping conditions
            // TODO JEH 4/24/2002: Reevaluate these rules. We need a test suite so
            // we can run experiments to see the true effect of changing these.
            if (alignment == null ||
                alignmentsWithOverlapCount >= MAXIMUM_OVERLAPS ||
                onePass || // only one pass for Japanese and Chinese
                lessThanOrEqual ||
                score >= 0.96)
            {
                stop = true;
            }
        }

        // set the new score based on alignment
        if (score >= PASS_THRESHOLD)
        {
            // null if we have no chance of accurate word breaking
            if (p_wordBreakPositions != null)
            {
                score = scoreBasedOnWordBoundries(validAlignments,
                    uniqueMatchCount, p_wordBreakPositions);
            }
        }

        if (score >= PASS_THRESHOLD)
        {
            if (score > 1.0)
            {
                // strange case that happens if the term is a non-word
                score = 1.0;
            }

            p_rec.setScore((int)(score*100));

            return true;
        }

        return false;
    }

    /**
     * Clean this logic up later. We plan to have a factory that will
     * give us a matching parameter object that will contain all the
     * rules for that locale.
     */
    private boolean greaterThanOrEqualToMinimumMatchSize(
        TermAlignment p_alignment, MatchRecord p_rec)
    {
        if (!singleCharacterWords(m_locale))
        {
            return (p_alignment.getTotalMatches() >= MINIMUM_MATCH_SIZE);
        }

        // Must be Japanese or Chinese.  Better rule for Japanese is
        // to treat kanji and non-kanji differently.
        return (p_alignment.getTotalMatches() >= 1);
    }

    /**
     * Clean this logic up later. We plan to have a factory that will
     * give us a matching parameter object that will contain all the
     * rules for that locale.
     */
    private boolean lessThanOrEqualToMinimumMatchSize(
        TermAlignment p_alignment, MatchRecord p_rec)
    {
        if (!singleCharacterWords(m_locale))
        {
            return (p_alignment.getTotalMatches() <= MINIMUM_MATCH_SIZE);
        }

        // Must be Japanese or Chinese.  TODO: better rule for
        // Japanese is to treat kanji and non-kanji differently.
        return (p_alignment.getTotalMatches() <= 1);
    }

    private double scoreBasedOnWordBoundries(HashMap p_validAlignments,
        int p_uniqueMatchCount, ArrayList p_wordBreakPositions)
    {
        double score = 0.0;
        TermAlignment ta = null;
        int segmentMatchLength = 0;

        for (Iterator it = p_validAlignments.values().iterator(); it.hasNext();)
        {
            ta = (TermAlignment)it.next();
            segmentMatchLength += wordMatchLengths(ta, p_wordBreakPositions);
        }

        if (ta == null)
        {
            return 0.0;
        }

        double fuzzyScore = 2.0 * ((double)p_uniqueMatchCount) /
            (double)(segmentMatchLength + ta.getTermLength());
        double concordanceScore = (double)p_uniqueMatchCount /
            (double)ta.getTermLength();

        // weighted score - fuzzy score worth more than concordance hit
        //return (0.90*fuzzyScore) + (0.10*concordanceScore);
        return fuzzyScore;
    }

    /**
     * Add up the lengths of all the words that match. Substract any
     * overlapping matches.
     */
    private int wordMatchLengths(TermAlignment p_ta,
        ArrayList p_wordBreakPositions)
    {
        int segmentMatchLength = 0;

        for (int i = 0, max = p_wordBreakPositions.size(); i < max; i++)
        {
            BreakPosition bp = (BreakPosition)p_wordBreakPositions.get(i);

            if (alignmentMatchesWord(bp, p_ta))
            {
                segmentMatchLength += (bp.m_end - bp.m_start);
            }
        }

        return segmentMatchLength;
    }

    /**
     * Return true if the alignment overlaps the word boundary, false
     * otherwise.
     */
    private boolean alignmentMatchesWord(BreakPosition p_bp, TermAlignment p_ta)
    {
        int s1 = p_ta.getStartPosition();
        int e1 = p_ta.getEndPosition();
        int s2 = p_bp.m_start;
        int e2 = p_bp.m_end;
        if (((s2 > s1 && s2 < e1) || (e2 > s1 && e2 < e1))
            ||
            ((s1 > s2 && s1 < e2) || (e1 > s2 && e1 < e2)))
        {
            return true;
        }

        return false;
    }

    private int countSetBits(BitSet p_bits)
    {
        int count = 0;
        for (int i = 0; i < p_bits.length(); i++)
        {
            if (p_bits.get(i))
            {
                count++;
            }
        }

        return count;
    }

    private boolean hasSpaceBetweenWords(Locale p_locale)
    {
        if (p_locale.equals(Locale.JAPAN) ||
            p_locale.equals(Locale.SIMPLIFIED_CHINESE) ||
            p_locale.equals(Locale.TRADITIONAL_CHINESE))
        {
            return false;
        }

        return true;
    }

    private boolean wordReorderingAllowed(Locale p_locale)
    {
        if (p_locale.equals(Locale.KOREA) ||
            p_locale.equals(Locale.JAPAN) ||
            p_locale.equals(Locale.SIMPLIFIED_CHINESE) ||
            p_locale.equals(Locale.TRADITIONAL_CHINESE))
        {
            return false;
        }

        return true;
    }

    private boolean singleCharacterWords(Locale p_locale)
    {
        if (p_locale.equals(Locale.KOREA) ||
            p_locale.equals(Locale.JAPAN) ||
            p_locale.equals(Locale.SIMPLIFIED_CHINESE) ||
            p_locale.equals(Locale.TRADITIONAL_CHINESE))
        {
            return true;
        }

        return false;
    }
}
