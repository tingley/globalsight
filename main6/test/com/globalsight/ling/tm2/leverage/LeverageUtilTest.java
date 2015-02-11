package com.globalsight.ling.tm2.leverage;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.system.MockEnvoySystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;

public class LeverageUtilTest
{
    @SuppressWarnings("serial")
    @Before
    public void setup()
    {
        SystemConfiguration.setDebugInstance(new MockEnvoySystemConfiguration(
                new HashMap<String, String>()
                {
                    {
                        put("systemConfiguration", "true");
                    }
                }));
    }

    @After
    public void teardown()
    {
        // Remove the custom SystemConfiguration
        SystemConfiguration.setDebugInstance(null);
    }

    /**
     * For GBS-1841: All "translated" segments should be ICE.
     */
    @Test
    public void testIsPoDataType()
    {
        int index = 0;

        // case 1
        TuvImpl tuv = new TuvImpl();
        tuv.setId(1000);
        TuImpl tu = new TuImpl();
        tu.setDataType("PO");
        tu.addTuv(tuv);
        tuv.setTu(tu);
        List<Tuv> sourceTuvs1 = new ArrayList<Tuv>();
        sourceTuvs1.add(tuv);

        Types types1 = new Types(false, MatchTypeStatistics.SEGMENT_PO_EXACT,
                MatchTypeStatistics.SEGMENT_PO_EXACT,
                LeverageMatchLingManager.EXACT, MatchState.PO_EXACT_MATCH);
        Map<String, Types> matchTypes1 = new HashMap<String, Types>();
        matchTypes1.put(tuv.getId() + "-0", types1);
        MatchTypeStatistics matchTypeStatistics1 = new MatchTypeStatistics(75);
        matchTypeStatistics1.setMatchTypes(matchTypes1);
        boolean result = LeverageUtil.isPoXlfICE(index, sourceTuvs1,
                matchTypeStatistics1, "1000");
        assertTrue(result);

        // case 2
        SegmentTmTuv segTmTuv = new SegmentTmTuv();
        segTmTuv.setId(1001);
        BaseTmTu tmTu = new SegmentTmTu();
        tmTu.setFormat("PO");
        tmTu.addTuv(segTmTuv);
        segTmTuv.setTu(tmTu);
        List<SegmentTmTuv> sourceTuvs2 = new ArrayList<SegmentTmTuv>();
        sourceTuvs2.add(segTmTuv);

        Types types2 = new Types(false, MatchTypeStatistics.SEGMENT_TM_EXACT,
                MatchTypeStatistics.SEGMENT_TM_EXACT,
                LeverageMatchLingManager.EXACT,
                MatchState.SEGMENT_TM_EXACT_MATCH);
        Map<String, Types> matchTypes2 = new HashMap<String, Types>();
        matchTypes2.put(segTmTuv.getId() + "-0", types2);
        MatchTypeStatistics matchTypeStatistics2 = new MatchTypeStatistics(75);
        matchTypeStatistics2.setMatchTypes(matchTypes2);

        result = LeverageUtil.isPoXlfICE(index, sourceTuvs2,
                matchTypeStatistics2, "1000");
        assertTrue(!result);
    }

}
