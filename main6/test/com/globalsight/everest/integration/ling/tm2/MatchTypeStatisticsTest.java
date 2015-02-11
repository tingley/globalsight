package com.globalsight.everest.integration.ling.tm2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MatchTypeStatisticsTest
{
    private MatchTypeStatistics ms = null;
    private int threshold = 0;
//    LeverageMatch match = null;
    
    // word count calculated by threshold
    private static final int THRESHOLD_NO_MATCH = 11;
    private static final int THRESHOLD_HI_FUZZY = 12;
    private static final int THRESHOLD_MED_HI_FUZZY = 13;
    private static final int THRESHOLD_MED_FUZZY = 14;
    
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
    public void testAddMatchTypeForCosting() throws Exception
    {
        for (int i = 0; i < matchList.size(); i++)
        {
            LeverageMatch match = matchList.get(i);
            threshold = 75;
            ms = new MatchTypeStatistics(threshold);
            Method method = ms.getClass().getDeclaredMethod(
                    "addMatchTypeForCosting", LeverageMatch.class);
            method.setAccessible(true);
            method.invoke(ms, match);
            Types types = ms.getTypesByThreshold(i + 1, "0");
            
            float score = match.getScoreNum();
            if (score < 100 && score >= 95)
            {
                Assert.assertEquals(THRESHOLD_HI_FUZZY, types.getStatisticsMatchType());
            }
            else if (score < 95 && score >= 85)
            {
                Assert.assertEquals(THRESHOLD_MED_HI_FUZZY, types.getStatisticsMatchType());
            }
            else if (score < 85 && score >= 75) 
            {
                Assert.assertEquals(THRESHOLD_MED_FUZZY, types.getStatisticsMatchType());
            }
            else 
            {
                Assert.assertEquals(THRESHOLD_NO_MATCH, types.getStatisticsMatchType());
            }
        }
    }
}
