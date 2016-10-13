package com.globalsight.everest.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.GlobalSightLocale;


public class StatisticsServiceTest
{
    private static final int THRESHOLD = 75;
    private static final int TIMES = 5;
    
    private StatisticsService statisticsService = null;
    private MatchTypeStatistics p_matches = null;
    private SegmentRepetition p_segmentRepetition = null;
    private List<SegmentTmTuv> tuvList = new ArrayList<SegmentTmTuv>();
    
    private int hiFuzzyWordCount = 0;
    private int medFuzzyWordCount = 0;
    private int medHiFuzzyWordCount = 0;
    private int lowFuzzyWordCount = 0;
    private int noMatchWordCount = 0;
    private int repetitionWordCount = 0;
    private int exactWordCount = 0;
    private int totalWordCount = 0;
    private int thresholdHiFuzzyWordCount = 0;
    private int thresholdMedFuzzyWordCount = 0;
    private int thresholdMedHiFuzzyWordCount = 0;
    private int thresholdLowFuzzyWordCount = 0;
    private int thresholdNoMatchWordCount = 0;
    
    private SegmentTypeForTest hiFuzzySegment = null;
    private SegmentTypeForTest medHiFuzzySegment = null;
    private SegmentTypeForTest medFuzzySegment = null;
    private SegmentTypeForTest lowFuzzySegment = null;
    private SegmentTypeForTest noMatchSegment = null;
    private SegmentTypeForTest exactSegment = null;
    
    @Before
    public void setUp()
    {
        exactSegment = new SegmentTypeForTest(100.0f, 20);
        hiFuzzySegment = new SegmentTypeForTest(95.6f, 18);
        medHiFuzzySegment = new SegmentTypeForTest(85.7f, 15);
        medFuzzySegment = new SegmentTypeForTest(76.0f, 15);
        lowFuzzySegment = new SegmentTypeForTest(73.0f, 13);
        noMatchSegment = new SegmentTypeForTest(0.0f, 4);
        
        tuvList = initTuv();
        p_matches = initMatchType();
        p_segmentRepetition = initSegmentRepetition();
        statisticsService = new StatisticsService();
    }
        
    @Test
    public void testCalculateTargetPageWordCounts() throws Exception
    {
        TargetPage tp = new TargetPage();
        PageWordCounts pwc = (PageWordCounts) ClassUtil.testMethod(
                statisticsService, "calculateTargetPageWordCounts", tuvList,
                p_matches, null, new HashMap());
        tp.setWordCount(pwc);
        
        Assert.assertEquals(tp.getWordCount().getHiFuzzyWordCount(),
                hiFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getMedHiFuzzyWordCount(),
                medHiFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getMedFuzzyWordCount(),
                medFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getLowFuzzyWordCount(),
                lowFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getRepetitionWordCount(),
                repetitionWordCount);
        Assert.assertEquals(tp.getWordCount().getTotalWordCount(),
                totalWordCount);
        Assert.assertEquals(tp.getWordCount().getThresholdHiFuzzyWordCount()
                .intValue(), thresholdHiFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getThresholdMedHiFuzzyWordCount()
                .intValue(), thresholdMedHiFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getThresholdMedFuzzyWordCount()
                .intValue(), thresholdMedFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getThresholdLowFuzzyWordCount()
                .intValue(), thresholdLowFuzzyWordCount);
        Assert.assertEquals(tp.getWordCount().getThresholdNoMatchWordCount()
                .intValue(), thresholdNoMatchWordCount);
    }
    
    @After
    public void tearDown()
    {
        exactSegment = null;
        hiFuzzySegment = null;
        medHiFuzzySegment = null;
        medFuzzySegment = null;
        lowFuzzySegment = null;
        noMatchSegment = null;
        
        tuvList = null;
        p_matches = null;
        p_segmentRepetition = null;
        statisticsService = null;
        
        hiFuzzyWordCount = 0;
        medFuzzyWordCount = 0;
        medHiFuzzyWordCount = 0;
        lowFuzzyWordCount = 0;
        noMatchWordCount = 0;
        repetitionWordCount = 0;
        exactWordCount = 0;
        totalWordCount = 0;
        thresholdHiFuzzyWordCount = 0;
        thresholdMedFuzzyWordCount = 0;
        thresholdMedHiFuzzyWordCount = 0;
        thresholdLowFuzzyWordCount = 0;
        thresholdNoMatchWordCount = 0;
    }
    
    
    
    // #########################################################
    // #################### private methods ####################
    // #########################################################
    private List<SegmentTmTuv> initTuv()
    {
        List<SegmentTmTuv> tmp = new ArrayList<SegmentTmTuv>();
        SegmentTmTu tu = new SegmentTmTu();
        tu.setType("text");
        
        GlobalSightLocale locale = new GlobalSightLocale("en", "US", true);
        // 100% match
        for (int i = 0; i < TIMES; i++)
        {
            SegmentTmTuv tuv = new SegmentTmTuv(i,
                    "<segment segmentId=\"1\" wordcount=\"20\">We have over 400 staff " +
                    "members in 12 offices located in the USA, Ireland, Germany, " +
                    "The Netherlands, China and Japan.</segment>", locale);
            tuv.setWordCount(exactSegment.getWordcount());
            tuv.setTu(tu);
            
            exactWordCount += exactSegment.getWordcount();
            totalWordCount += exactSegment.getWordcount();
            tmp.add(tuv);
        }
        // 95.6% high fuzzy match
        for (int i = 0; i < TIMES; i++)
        {
            SegmentTmTuv tuv = new SegmentTmTuv(i + TIMES,
                    "<segment segmentId=\"1\" wordcount=\"18\">We have over 400 staff " +
                    "members in 12 offices located in the USA, Ireland, Germany, " +
                    "The Netherlands, China.</segment>", locale);
            tuv.setWordCount(hiFuzzySegment.getWordcount());
            tuv.setTu(tu);
            
            if (i < 1)
            {
                hiFuzzyWordCount += hiFuzzySegment.getWordcount();
            }
            else 
            {
                repetitionWordCount += hiFuzzySegment.getWordcount();
            }
            totalWordCount += hiFuzzySegment.getWordcount();
            tmp.add(tuv);
        }
        // 85.7% medhigh fuzzy match
        for (int i = 0; i < TIMES; i++)
        {
            SegmentTmTuv tuv = new SegmentTmTuv(i + TIMES * 2,
                    "<segment segmentId=\"1\" wordcount=\"15\">We have over 400 staff " +
                    "members in 12 offices located in the USA, Ireland, Germany.</segment>",
                    locale);
            tuv.setWordCount(medHiFuzzySegment.getWordcount());
            tuv.setTu(tu);
            
            if (i < 1)
            {
                medHiFuzzyWordCount += medHiFuzzySegment.getWordcount();
            }
            else 
            {
                repetitionWordCount += medHiFuzzySegment.getWordcount();
            }
            totalWordCount += medHiFuzzySegment.getWordcount();
            tmp.add(tuv);
        }
        // 76% med fuzzy match
        for (int i = 0; i < TIMES; i++)
        {
            SegmentTmTuv tuv = new SegmentTmTuv(i + TIMES * 3,
                    "<segment segmentId=\"1\" wordcount=\"15\">We have over 400 staff " +
                    "members in 12 offices located in the USA, Ireland IBM.</segment>", locale);
            tuv.setWordCount(medFuzzySegment.getWordcount());
            tuv.setTu(tu);
            
            if (i < 1)
            {
                medFuzzyWordCount += medFuzzySegment.getWordcount();
            }
            else 
            {
                repetitionWordCount += medFuzzySegment.getWordcount();
            }
            totalWordCount += medFuzzySegment.getWordcount();
            tmp.add(tuv);
        }
        // 73% low fuzzy match
        for (int i = 0; i < TIMES; i++)
        {
            SegmentTmTuv tuv = new SegmentTmTuv(i + TIMES * 4,
                    "<segment segmentId=\"1\" wordcount=\"13\">We have over 400 staff " +
                    "members in 12 offices located in the USA.</segment>", locale);
            tuv.setWordCount(lowFuzzySegment.getWordcount());
            tuv.setTu(tu);
            
            if (i < 1)
            {
                lowFuzzyWordCount += lowFuzzySegment.getWordcount();
            }
            else 
            {
                repetitionWordCount += lowFuzzySegment.getWordcount();
            }
            totalWordCount += lowFuzzySegment.getWordcount();
            tmp.add(tuv);
        }
        // no match
        for (int i = 0; i < TIMES; i++)
        {
            SegmentTmTuv tuv = new SegmentTmTuv(i + TIMES * 5,
                    "<segment segmentId=\"1\" wordcount=\"4\">We have nothing here.</segment>",
                    locale);
            tuv.setWordCount(noMatchSegment.getWordcount());
            tuv.setTu(tu);
            
            if (i < 1)
            {
                noMatchWordCount += noMatchSegment.getWordcount();
            }
            else 
            {
                repetitionWordCount += noMatchSegment.getWordcount();
            }
            totalWordCount += noMatchSegment.getWordcount();
            tmp.add(tuv);
        }
        
        return tmp;
    }
    
    private MatchTypeStatistics initMatchType()
    {
        MatchTypeStatisticsForTest mft = new MatchTypeStatisticsForTest(THRESHOLD);
        // init match map
        Map<String, Types> map = new HashMap<String, Types>();
        
        for (int i = 0; i < TIMES; i++)
        {
            Types exactType = new Types(
                    MatchTypeStatistics.SEGMENT_TM_EXACT,
                    MatchTypeStatistics.SEGMENT_TM_EXACT,
                    LeverageMatchLingManager.EXACT,
                    MatchState.SEGMENT_TM_EXACT_MATCH, false);
            map.put(i + "-null", exactType);
        }
        for (int i = 0; i < TIMES; i++)
        {
            Types hiFuzzyType = new Types(MatchTypeStatistics.HI_FUZZY,
                    getThresholdMatchType(hiFuzzySegment.getMatchScore(),
                            THRESHOLD, hiFuzzySegment.getWordcount(), i),
                    LeverageMatchLingManager.FUZZY, MatchState.FUZZY_MATCH ,false);
            map.put((i + TIMES) + "-null", hiFuzzyType);
        }
        for (int i = 0; i < TIMES; i++)
        {
            Types medHiFuzzyType = new Types(
                    MatchTypeStatistics.MED_HI_FUZZY, getThresholdMatchType(
                            medHiFuzzySegment.getMatchScore(), THRESHOLD,
                            medHiFuzzySegment.getWordcount(), i),
                    LeverageMatchLingManager.FUZZY, MatchState.FUZZY_MATCH, false);
            map.put((i + TIMES * 2) + "-null", medHiFuzzyType);
        }
        for (int i = 0; i < TIMES; i++)
        {
            Types medFuzzyType = new Types(
                    MatchTypeStatistics.MED_FUZZY, getThresholdMatchType(
                            medFuzzySegment.getMatchScore(), THRESHOLD,
                            medFuzzySegment.getWordcount(), i),
                    LeverageMatchLingManager.FUZZY, MatchState.FUZZY_MATCH, false);
            map.put((i + TIMES * 3) + "-null", medFuzzyType);
        }
        for (int i = 0; i < TIMES; i++)
        {
            Types lowFuzzyType = new Types(
                    MatchTypeStatistics.LOW_FUZZY, getThresholdMatchType(
                            lowFuzzySegment.getMatchScore(), THRESHOLD,
                            lowFuzzySegment.getWordcount(), i),
                    LeverageMatchLingManager.STATISTICS, MatchState.FUZZY_MATCH, false);
            map.put((i + TIMES * 4) + "-null", lowFuzzyType);
        }
        for (int i = 0; i < TIMES; i++)
        {
            Types noMatchType = new Types(MatchTypeStatistics.NO_MATCH,
                    getThresholdMatchType(noMatchSegment.getMatchScore(),
                            THRESHOLD, noMatchSegment.getWordcount(), i),
                    LeverageMatchLingManager.NO_MATCH,
                    MatchState.STATISTICS_MATCH, false);
            map.put((i + TIMES * 5) + "-null", noMatchType);
        }
        
        mft.init(map);
        return mft;
    }
    
    private int getThresholdMatchType(float matchScore, int threshold,
            int wordCount, int i)
    {
        int statisticsType = MatchTypeStatistics.THRESHOLD_NO_MATCH;
        if (matchScore < threshold) 
        {
            statisticsType = MatchTypeStatistics.THRESHOLD_NO_MATCH;
            if (i < 1) thresholdNoMatchWordCount += wordCount;
        }
        else 
        {
            if (threshold >= 95)
            {
                statisticsType = MatchTypeStatistics.THRESHOLD_HI_FUZZY;
                if (i < 1) thresholdHiFuzzyWordCount += wordCount;
            }
            else if (threshold >= 85) 
            {
                if (matchScore >= 95)
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_HI_FUZZY;
                    if (i < 1) thresholdHiFuzzyWordCount += wordCount;
                }
                else if (matchScore < 95) 
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY;
                    if (i < 1) thresholdMedHiFuzzyWordCount += wordCount;
                }
            }
            else if (threshold >= 75) 
            {
                if (matchScore >= 95)
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_HI_FUZZY;
                    if (i < 1) thresholdHiFuzzyWordCount += wordCount;
                }
                else if (matchScore >= 85 && matchScore < 95) 
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY;
                    if (i < 1) thresholdMedHiFuzzyWordCount += wordCount;
                }
                else if (matchScore < 85) 
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_MED_FUZZY;
                    if (i < 1) thresholdMedFuzzyWordCount += wordCount;
                }
            }
            else if (threshold >= 50) 
            {
                if (matchScore >= 95)
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_HI_FUZZY;
                    if (i < 1) thresholdHiFuzzyWordCount += wordCount;
                }
                else if (matchScore >= 85 && matchScore < 95) 
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY;
                    if (i < 1) thresholdMedHiFuzzyWordCount += wordCount;
                }
                else if (matchScore >= 75 && matchScore < 85) 
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_MED_FUZZY;
                    if (i < 1) thresholdMedFuzzyWordCount += wordCount;
                }
                else if (matchScore < 75) 
                {
                    statisticsType = MatchTypeStatistics.THRESHOLD_LOW_FUZZY;
                    if (i < 1) thresholdLowFuzzyWordCount += wordCount;
                }
            }
        }
        return statisticsType;
    }
    
    private SegmentRepetition initSegmentRepetition()
    {
        SegmentRepetition sr = new SegmentRepetition(tuvList);
        return sr;
    }
    
    // #################################################################
    // #################### private sub classes ########################
    // #################################################################
    private class MatchTypeStatisticsForTest extends MatchTypeStatistics
    {
        public MatchTypeStatisticsForTest(int threshold)
        {
            super(threshold);
        }

        public void init(Map<String, Types> matchTypes)
        {
            setMatchTypes(matchTypes);
        }
    }
    
    private class SegmentTypeForTest
    {
        private float matchScore;
        private int wordcount;
        public SegmentTypeForTest(float m_matchScore, int m_wordcount)
        {
            matchScore = m_matchScore;
            wordcount = m_wordcount;
        }
        public float getMatchScore()
        {
            return matchScore;
        }
        public int getWordcount()
        {
            return wordcount;
        }
    }
    
}
