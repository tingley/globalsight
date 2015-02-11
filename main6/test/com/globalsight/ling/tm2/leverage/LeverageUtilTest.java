package com.globalsight.ling.tm2.leverage;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.system.MockSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;

public class LeverageUtilTest
{
    @Before
    public void setup() 
    {
        SystemConfiguration.setDebugInstance(new MockSystemConfiguration(
                new HashMap<String, String>() {{
                    put("systemConfiguration", "true");
                }}));
    }
    
    @After
    public void teardown() {
        // Remove the custom SystemConfiguration
        SystemConfiguration.setDebugInstance(null);
    }

    /**
     * For GBS-1841: All "translated" segments should be ICE.
     */
    @Test
    public void testIsPoDataType()
    {
        List sourceTuvs = new ArrayList();
        int index = 0;

        // case 1
        Tuv tuv = new TuvImpl();
        TuImpl tu = new TuImpl();
        tu.setDataType("PO");
        tu.addTuv(tuv);
        tuv.setTu(tu);
        sourceTuvs.add(tuv);
        
        boolean result = LeverageUtil.isPoDataType(index, sourceTuvs);
        assertTrue(result);
        
        // case 2
        SegmentTmTuv segTmTuv = new SegmentTmTuv();
        BaseTmTu tmTu = new SegmentTmTu();
        tmTu.setFormat("PO");
        tmTu.addTuv(segTmTuv);
        segTmTuv.setTu(tmTu);
        sourceTuvs.clear();
        sourceTuvs.add(segTmTuv);

        result = LeverageUtil.isPoDataType(index, sourceTuvs);
        assertTrue(result);
    }

}
