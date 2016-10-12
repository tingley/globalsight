package com.globalsight.everest.integration.ling.tm2;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.util.ClassUtil;

public class MatchTypeStatisticsTest
{
    private MatchTypeStatistics ms = null;
    private int threshold = 0;
        
    private List<LeverageMatch> matchList = new ArrayList<LeverageMatch>();
    
    @Before
    public void setUp()
    {
        LeverageMatch match = new LeverageMatch();
        match.setOriginalSourceTuvId(1);
        match.setSubId("0");
        match.setMatchType("STATISTICS_MATCH");
        match.setScoreNum((float)73.684);
        match.setTmId(1003);
        
        LeverageMatch match1 = new LeverageMatch();
        match1.setOriginalSourceTuvId(2);
        match1.setSubId("0");
        match1.setMatchType("FUZZY_MATCH");
        match1.setScoreNum((float)85.714);
        match1.setTmId(1003);
        
        LeverageMatch match2 = new LeverageMatch();
        match2.setOriginalSourceTuvId(3);
        match2.setSubId("0");
        match2.setMatchType("FUZZY_MATCH");
        match2.setScoreNum((float)95.652);
        match2.setTmId(1003);
        
        matchList.add(match);
        matchList.add(match1);
        matchList.add(match2);
    }
    
    @Test
    public void testAddMatchType() throws Exception
    {
        for (int i = 0; i < matchList.size(); i++)
        {
            LeverageMatch match = matchList.get(i);
            threshold = 75;
            ms = new MatchTypeStatistics(threshold);
            ClassUtil.testMethod(ms, "addMatchType", match);
            Types types = ms.getTypes(i + 1, "0");

            float score = match.getScoreNum();
            if (score < 100 && score >= 95) {
                Assert.assertEquals(MatchTypeStatistics.THRESHOLD_HI_FUZZY,
                        types.getStatisticsMatchTypeByThreshold());
            } else if (score < 95 && score >= 85) {
                Assert.assertEquals(
                        MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY,
                        types.getStatisticsMatchTypeByThreshold());
            } else if (score < 85 && score >= 75) {
                Assert.assertEquals(
                        MatchTypeStatistics.THRESHOLD_MED_FUZZY,
                        types.getStatisticsMatchTypeByThreshold());
            } else {
                Assert.assertEquals(
                        MatchTypeStatistics.THRESHOLD_NO_MATCH,
                        types.getStatisticsMatchTypeByThreshold());
            }
        }
    }
    
    @Test
    public void testDeterminIsSubLevMatch()
    {
        threshold = 75;
        ms = new MatchTypeStatistics(threshold);
        float matchPoint = 74.568f;
        Boolean result = (Boolean) ClassUtil.testMethod(ms,
                "determinIsSubLevMatch", matchPoint);
        Assert.assertTrue(result == true);

        matchPoint = 49.512f;
        result = (Boolean) ClassUtil.testMethod(ms, "determinIsSubLevMatch",
                matchPoint);
        Assert.assertTrue(result != true);
    }
    
    @Test
    public void testDetermineLingManagerType()
    {
        threshold = 75;
        ms = new MatchTypeStatistics(threshold);

        float matchPoint = 100;
        MatchState matchState = MatchState.SEGMENT_TM_EXACT_MATCH;
        int result = (Integer) ClassUtil.testMethod(ms,
                "determineLingManagerType", matchPoint, matchState);
        Assert.assertEquals(LeverageMatchLingManager.EXACT, result);

        matchPoint = 85.01f;
        matchState = MatchState.FUZZY_MATCH;
        result = (Integer) ClassUtil.testMethod(ms, "determineLingManagerType",
                matchPoint, matchState);
        Assert.assertEquals(LeverageMatchLingManager.FUZZY, result);

        matchState = MatchState.NOT_A_MATCH;
        result = (Integer) ClassUtil.testMethod(ms, "determineLingManagerType",
                matchPoint, matchState);
        Assert.assertEquals(LeverageMatchLingManager.NO_MATCH, result);
    }
    
    @Test
    public void testDetermineStatisticsTypeForFuzzyMatch()
    {
        threshold = 75;
        ms = new MatchTypeStatistics(threshold);

        float matchPoint = 74.562f;
        int result = (Integer) ClassUtil.testMethod(ms,
                "determineStatisticsTypeForFuzzyMatch", matchPoint);
        Assert.assertEquals(MatchTypeStatistics.LOW_FUZZY, result);

        matchPoint = 95.00f;
        result = (Integer) ClassUtil.testMethod(ms,
                "determineStatisticsTypeForFuzzyMatch", matchPoint);
        Assert.assertEquals(MatchTypeStatistics.HI_FUZZY, result);
    }
    
    @Test
    public void testDetermineCostingStatisticsTypeForFuzzyMatch()
    {
        threshold = 95;
        float matchPoint = 95.23f;
        ms = new MatchTypeStatistics(threshold);
        int result = (Integer) ClassUtil.testMethod(ms,
                "determineCostingStatisticsTypeForFuzzyMatch", matchPoint);
        Assert.assertEquals(MatchTypeStatistics.THRESHOLD_HI_FUZZY, result);

        threshold = 90;
        matchPoint = 94.23f;
        ms = new MatchTypeStatistics(threshold);
        result = (Integer) ClassUtil.testMethod(ms,
                "determineCostingStatisticsTypeForFuzzyMatch", matchPoint);
        Assert.assertEquals(MatchTypeStatistics.THRESHOLD_MED_HI_FUZZY, result);

        threshold = 75;
        matchPoint = 80.23f;
        ms = new MatchTypeStatistics(threshold);
        result = (Integer) ClassUtil.testMethod(ms,
                "determineCostingStatisticsTypeForFuzzyMatch", matchPoint);
        Assert.assertEquals(MatchTypeStatistics.THRESHOLD_MED_FUZZY, result);

        threshold = 70;
        matchPoint = 74.23f;
        ms = new MatchTypeStatistics(threshold);
        result = (Integer) ClassUtil.testMethod(ms,
                "determineCostingStatisticsTypeForFuzzyMatch", matchPoint);
        Assert.assertEquals(MatchTypeStatistics.THRESHOLD_LOW_FUZZY, result);
    }

}
