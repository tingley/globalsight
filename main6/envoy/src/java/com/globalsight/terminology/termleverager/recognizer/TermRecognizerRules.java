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

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Locale;

class TermRecognizerRules
{
    static private final String FUZZY_THRESHOLD = "fuzzy_threshold";
    static private final String MINIMUM_MATCH_SIZE = "minimum_match_size";
    static private final String MINIMUM_OVERLAPS = "minimum_overlaps";
    static private final String WORD_BREAK = "word_break";
    static private final String ALLOW_WORD_REORDERING = "allow_word_reordering";
    static private final String COUNT_WHITESPACE_AS_MATCH = "count_whitespace_as_match";
    static private final String LOWER_CASE = "lower_case";
    static private final String FUZY_WEIGHT = "fuzzy_weight";
    static private final String CONCORDANCE_WEIGHT = "concordance_weight";
    static private final String SPACE_IN_SEGMENT_WEIGHT = "space_in_segment_weight";
    static private final String SPACE_IN_TERM_WEIGHT = "space_in_term_weight";
    static private final String MATCH_WEIGHT = "match_weight";
    static private final String MISMATCH_WEIGHT = "mismatch_weight";
    static private final String PENALTY_FOR_WHITESPACE_MATCH = "penalty_for_whitespace_match";

    // rule parameters
    private double m_fuzzyThreshold;
    private int m_minimumMatchSize;
    private int m_minimumOverlaps;
    private boolean m_wordBreak;
    private boolean m_allowWordReordering;
    private boolean m_countWhitespaceAsMatch;
    private boolean m_lowerCase;
    private double m_fuzzyWeight;
    private double m_concordanceWeight;
    private int m_spaceInSegmentWeight;
    private int m_spaceInTermWeight;
    private int m_matchWeight;
    private int m_mismatchWeight;
    private int m_penaltyForWhitespaceMatch;

    private Locale m_locale;

    //
    // Constructor
    //
    public TermRecognizerRules(Locale p_locale, ResourceBundle p_resource)
    {
        m_locale = p_locale;
        loadProperties(p_resource);
    }

    public void loadProperties(ResourceBundle p_resource)
    {
        String value;

        value = p_resource.getString(FUZZY_THRESHOLD);
        m_fuzzyThreshold = Double.parseDouble(value);

        value = p_resource.getString(MINIMUM_MATCH_SIZE);
        m_minimumMatchSize = Integer.parseInt(value);

        value = p_resource.getString(MINIMUM_OVERLAPS);
        m_minimumOverlaps = Integer.parseInt(value);

        value = p_resource.getString(WORD_BREAK);
        m_wordBreak = Boolean.getBoolean(value);

        value = p_resource.getString(ALLOW_WORD_REORDERING);
        m_allowWordReordering = Boolean.getBoolean(value);

        value = p_resource.getString(COUNT_WHITESPACE_AS_MATCH);
        m_countWhitespaceAsMatch = Boolean.getBoolean(value);

        value = p_resource.getString(LOWER_CASE);
        m_lowerCase = Boolean.getBoolean(value);

        value = p_resource.getString(FUZY_WEIGHT);
        m_fuzzyWeight = Double.parseDouble(value);

        value = p_resource.getString(CONCORDANCE_WEIGHT);
        m_concordanceWeight = Double.parseDouble(value);

        value = p_resource.getString(SPACE_IN_SEGMENT_WEIGHT);
        m_spaceInSegmentWeight = Integer.parseInt(value);

        value = p_resource.getString(SPACE_IN_TERM_WEIGHT);
        m_spaceInTermWeight = Integer.parseInt(value);

        value = p_resource.getString(MATCH_WEIGHT);
        m_matchWeight = Integer.parseInt(value);

        value = p_resource.getString(MISMATCH_WEIGHT);
        m_mismatchWeight = Integer.parseInt(value);

        value = p_resource.getString(PENALTY_FOR_WHITESPACE_MATCH);
        m_penaltyForWhitespaceMatch = Integer.parseInt(value);
    }

    public double getFuzzyThreshold()
    {
        return m_fuzzyThreshold;
    }

    public int getMinimumMatchSize()
    {
        return m_minimumMatchSize;
    }

    public int getMinimumOverlaps()
    {
        return m_minimumOverlaps;
    }

    public boolean wordBreak()
    {
        return m_wordBreak;
    }

    public boolean allowWordReordering()
    {
        return m_allowWordReordering;
    }

    public boolean countWhitespaceAsMatch()
    {
        return m_countWhitespaceAsMatch;
    }

    public boolean lowerCase()
    {
        return m_lowerCase;
    }

    public double getFuzzyWeight()
    {
        return m_fuzzyWeight;
    }

    public double getConcordanceWeight()
    {
        return m_concordanceWeight;
    }

    public int getSpaceInSegmentWeight()
    {
        return m_spaceInSegmentWeight;
    }

    public int getSpaceInTermWeight()
    {
        return m_spaceInTermWeight;
    }

    public int getMatchWeight()
    {
        return m_matchWeight;
    }

    public int getMismatchWeight()
    {
        return m_mismatchWeight;
    }

    public int getPenaltyForWhitespaceMatch()
    {
        return m_penaltyForWhitespaceMatch;
    }
}
