package com.globalsight.everest.page.pageimport;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.docproc.extractor.xliff20.XliffHelper;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;

public class WsTuCreationTest
{

    @Test
    public void test()
    {
        TuImpl tu = new TuImpl();
        ArrayList<TuImpl> p_tuList = new ArrayList<TuImpl>();
        p_tuList.add(tu);
        LeverageGroup lg = new LeverageGroupImpl();
        lg.addTu(tu);
        TuvImpl sourceTuv = new TuvImpl();
        String segment = "<segment segmentId=\"1\" wordcount=\"2\">MobileMe Help</segment>";
        sourceTuv.setSegmentString(segment);
        GlobalSightLocale p_sourceLocale = new GlobalSightLocale("en", "US",
                false);
        p_sourceLocale.setId(32);
        sourceTuv.setGlobalSightLocale(p_sourceLocale);
        tu.addTuv(sourceTuv);

        String xliffpart = "target";

        IXliffTuCreation tucreation = new WsTuCreation();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(XliffHelper.MARK_XLIFF_TARGET_LANG, "fr_fr");
        map.put("generatFrom", "worldserver");
        tucreation.setAttribute(map);

        Request p_request = new RequestImpl();
        GxmlElement seg = sourceTuv.getGxmlElement();

        seg.setAttribute(Extractor.IWS_TRANSLATION_TYPE, "machine_translation");
        seg.setAttribute(Extractor.IWS_TM_SCORE, "88");
        seg.setAttribute(Extractor.IWS_SOURCE_CONTENT, "repeated");
        seg.setAttribute(Extractor.IWS_LOCK_STATUS, "locked");

        boolean flag = tucreation.transProcess(p_request, xliffpart, seg, lg,
                p_tuList, p_sourceLocale, 1000);

        assertTrue(!flag);
        assertTrue(tu.getXliffTarget().equals("MobileMe Help"));
        assertTrue(tu.getIwsScore().equals("88"));
        assertTrue(tu.getXliffTranslationType().equals("machine_translation"));
        assertTrue(tu.getSourceContent().equals("repeated"));
        assertTrue(tu.isXliffLocked());
    }

}
