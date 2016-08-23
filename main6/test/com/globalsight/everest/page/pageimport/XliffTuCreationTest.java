package com.globalsight.everest.page.pageimport;

import static org.junit.Assert.*;

import org.junit.Test;

import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.tuv.*;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;
import java.util.*;

public class XliffTuCreationTest
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
        GlobalSightLocale p_sourceLocale = new GlobalSightLocale("en", "US", false);
        p_sourceLocale.setId(32);
        sourceTuv.setGlobalSightLocale(p_sourceLocale);
        tu.addTuv(sourceTuv);
        
        String xliffpart = "source";
        
        IXliffTuCreation tucreation = new XliffTuCreation();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("xliffTargetLan", "fr_fr");
        map.put("generatFrom", "worldserver");
        tucreation.setAttribute(map);
        
        Request p_request = new RequestImpl();
        GxmlElement seg = sourceTuv.getGxmlElement();
        boolean flag = 
                tucreation.transProcess(p_request, 
                        xliffpart, seg, lg, p_tuList, p_sourceLocale, 1000);
        
        assertTrue(flag);
        
        xliffpart = "target";
        flag = 
                tucreation.transProcess(p_request, 
                        xliffpart, seg, lg, p_tuList, p_sourceLocale, 1000);
        
        assertTrue(!flag);
        assertTrue(tu.getXliffTarget().equals("MobileMe Help"));
        
        //test the 
        TuvImpl tuv_temp = new TuvImpl();
        String blankSeg = "<segment segmentId=\"1\" wordcount=\"0\"> </segment>";
        tuv_temp.setSegmentString(blankSeg);
        GxmlElement seg2 = tuv_temp.getGxmlElement();
        seg2.setAttribute("passoloState", "ready");
        
        flag = 
                tucreation.transProcess(p_request, 
                        xliffpart, seg2, lg, p_tuList, p_sourceLocale, 1000);
        assertTrue(!flag);
        assertTrue(tu.getXliffTarget().trim().equals(""));
        assertTrue(tu.getXliffTargetLanguage().equals("fr_fr"));
        assertTrue(tu.getGenerateFrom().equals("worldserver"));
        assertTrue(tu.getPassoloState().equals("ready"));
        
        
        xliffpart = "altSource";
        
        flag = tucreation.transProcess(p_request, 
                        xliffpart, seg, lg, p_tuList, p_sourceLocale, 1000);
        assertTrue(!flag);
        assertTrue(sourceTuv.getXliffAlt(false) == null);

        xliffpart = "altTarget";
        seg.setAttribute("altLanguage", "fr");
        seg.setAttribute("altQuality", "88");
        flag = tucreation.transProcess(p_request, xliffpart, seg, lg, p_tuList,
                p_sourceLocale, 1000);
        assertTrue(!flag);
        assertTrue(sourceTuv.getXliffAlt(false).size() == 1);
        
        Iterator ite = sourceTuv.getXliffAlt(false).iterator();
        XliffAlt alt = (XliffAlt)ite.next();
        assertTrue(alt.getLanguage().equals("fr"));
        assertTrue(alt.getQuality().equals("88"));
    }
}
