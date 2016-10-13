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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a reusable object obeying the init()-reset() protocol.
 */
class TermWithinSentenceAligner
{
    // traceback pointer types
    static private final int USED = -1;
    static private final int STOP = 0;
    static private final int DIAGONAL_MATCH = 1;
    static private final int DIAGONAL_MISMATCH = 2;
    static private final int HORIZONTAL = 3;
    static private final int VERTICAL = 4;
    static private final int USED_DIAGONAL_MATCH = 6;
    static private final int USED_DIAGONAL_MISMATCH = 7;
    static private final int USED_HORIZONTAL = 8;
    static private final int USED_VERTICAL = 9;

    // default weights for alignment types
    static private final int DEFAULT_SPACE_IN_SEG_WEIGHT = -2;
    static private final int DEFAULT_SPACE_IN_TERM_WEIGHT = -3;
    static private final int DEFAULT_MATCH_WEIGHT = 2;
    static private final int DEFAULT_MISMATCH_WEIGHT = -2;

    private int[][] m_aligment = null;
    private int[][] m_traceback = null;
    private int m_maxi;
    private int m_maxj;
    private int m_mini;
    private int m_minj;
    private int m_totalMatches;
    private String m_segment;
    private String m_term;
    private boolean m_reset;

    //
    // Constructor
    //

    public TermWithinSentenceAligner()
    {
        m_segment = "";
        m_term = "";
        m_totalMatches = 0;
        m_reset = false;
    }

    private void init(int p_segmentLength, int p_termLength)
    {
        if ((m_aligment == null || m_traceback == null) ||
            (p_segmentLength > m_segment.length() || p_termLength > m_term.length()))
        {
            m_aligment = new int[p_segmentLength+1][p_termLength+1];
            m_traceback = new int[p_segmentLength+1][p_termLength+1];
        }
        else
        {
            if (m_reset)
            {
                for (int i = 1; i <= p_segmentLength; i++)
                {
                    for (int j = 1; j <= p_termLength; j++)
                    {
                        m_traceback[i][j] = STOP;
                    }
                }
            }
        }

        m_reset = false;
    }

    public void reset()
    {
        m_reset = true;
        m_totalMatches = 0;
    }

    /**
     * Finds the maximum of four numbers and returns it.
     */
    private static int max(int x1, int x2, int x3, int x4)
    {
        return Math.max(Math.max(x1, x2), Math.max(x3, x4));
    }

    /**
     * Aligns with default weights.
     */
    public TermAlignment getNextAlignment(String p_segment, String p_term)
    {
        return getNextAlignment(p_segment, p_term,
            DEFAULT_MATCH_WEIGHT,
            DEFAULT_MISMATCH_WEIGHT,
            DEFAULT_SPACE_IN_SEG_WEIGHT,
            DEFAULT_SPACE_IN_TERM_WEIGHT);
    }

    /**
     * Aligns with user defined weights.
     */
    private TermAlignment getNextAlignment(String p_segment, String p_term,
        int p_matchWeight, int p_mismatchWeight,
        int p_spaceInSegWeight, int p_spaceInTermWeight)
    {
        int n = p_segment.length();
        int m = p_term.length();
        int maxval = Integer.MIN_VALUE;  // negative infinity
        int val;
        int diagonal = 0;
        int horizontal = 0;
        int vertical = 0;
        boolean match = false;
        boolean lastMatch = false;

        init(n, m);

        m_maxi = n;
        m_maxj = m;

        m_segment = p_segment;
        m_term = p_term;

        for (int i = 1; i <= n; i++)
        {
            for (int j = 1; j <= m; j++)
            {
                if (!isUsed(m_traceback[i-1][j-1]))
                {
                    char ci = p_segment.charAt(i-1);
                    char cj = p_term.charAt(j-1);

                    if (ci == cj)
                    {
                        // Reduce weight for matching on space. There
                        // probably are other kinds of whitespace we
                        // should penalize.
                        if (ci == ' ')
                        {
                            diagonal = m_aligment[i-1][j-1] + (p_matchWeight-1);
                        }
                        else
                        {
                            diagonal = m_aligment[i-1][j-1] + p_matchWeight;
                        }

                        match = true;
                    }
                    else
                    {
                        diagonal = m_aligment[i-1][j-1] + p_mismatchWeight;
                        match = false;
                    }

                    horizontal = m_aligment[i-1][j] + p_spaceInSegWeight;
                    vertical = m_aligment[i][j-1] + p_spaceInTermWeight;

                    val = max(0, diagonal, horizontal, vertical);
                }
                else // we've already seen this alignment
                {
                    val = 0;
                }

                m_aligment[i][j] = val;

                if (val == 0)
                {
                    m_traceback[i][j] = STOP;
                }
                else if (val == diagonal)
                {
                    if (match)
                    {
                        setTraceBack(i, j, DIAGONAL_MATCH);
                    }
                    else
                    {
                        setTraceBack(i, j, DIAGONAL_MISMATCH);
                    }
                }
                else if (val == horizontal)
                {
                    setTraceBack(i, j, HORIZONTAL);
                }
                else if (val == vertical)
                {
                    setTraceBack(i, j, VERTICAL);
                }
                else // error
                {
                }

                if (val > maxval)
                {
                    maxval = val;
                    m_maxi = i;
                    m_maxj = j;
                }
            }
        }

        return findOptimalAlignment(m_maxi, m_maxj);
    }

    private boolean isUsed(int p_type)
    {
        if (p_type == USED_DIAGONAL_MATCH ||
            p_type == USED_DIAGONAL_MISMATCH ||
            p_type == USED_HORIZONTAL ||
            p_type == USED_VERTICAL)
        {
            return true;
        }

        return false;
    }

    private void setTraceBack(int p_i, int p_j, int p_type)
    {
        if (!isUsed(m_traceback[p_i][p_j]))
        {
            m_traceback[p_i][p_j] = p_type;
        }
    }

    private TermAlignment findOptimalAlignment(int p_starti, int p_startj)
    {
        int i = p_starti;
        int j = p_startj;
        int type;

        type = m_traceback[i][j];

        int totalUniqueMatches = 0;
        TermAlignment talign = new TermAlignment(m_segment.length(), m_term.length());

        while (type != STOP && !isUsed(type))
        {
            type = m_traceback[i][j];

            switch(type)
            {
            case DIAGONAL_MATCH:
                totalUniqueMatches++;
                talign.setMatch(i-1, j-1);
                m_traceback[i][j] = USED_DIAGONAL_MATCH;
                m_totalMatches++;
                i--;
                j--;
                m_mini = i;
                break;
            case DIAGONAL_MISMATCH:
                m_traceback[i][j] = USED_DIAGONAL_MISMATCH;
                i--;
                j--;
                break;
            case HORIZONTAL:
                m_traceback[i][j] = USED_HORIZONTAL;
                i--;
                break;
            case VERTICAL:
                m_traceback[i][j] = USED_VERTICAL;
                j--;
                break;
            default:
                break;
            }
        }

        if (p_starti == i && p_startj == j)
        {
            return null; // no more alignments left
        }

        talign.setStartPosition(m_mini);
        talign.setEndPosition(p_starti);
        talign.setTotalMatches(totalUniqueMatches);
        talign.setAlignmentScore(m_aligment[p_starti][p_startj]);

        return talign;
    }

    public String printAlignment()
    {
        StringBuffer segAlign = new StringBuffer();
        StringBuffer termAlign = new StringBuffer();

        int i = m_maxi;
        int j = m_maxj;
        int type = m_traceback[i][j];

        while (type != STOP)
        {
            type = m_traceback[i][j];

            switch(type)
            {
            case DIAGONAL_MATCH:
            case DIAGONAL_MISMATCH:
            case USED_DIAGONAL_MATCH:
            case USED_DIAGONAL_MISMATCH:
                segAlign.append(m_segment.charAt(i-1));
                termAlign.append(m_term.charAt(j-1));
                i--;
                j--;
                break;
            case HORIZONTAL:
            case USED_HORIZONTAL:
                segAlign.append('-');
                i--;
                break;
            case VERTICAL:
            case USED_VERTICAL:
                termAlign.append('-');
                j--;
                break;
            default:
                break;
            }
        }

        return "\n" + segAlign.reverse() + "\n" + termAlign.reverse();
    }
}
