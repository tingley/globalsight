package com.globalsight.everest.tuv;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.util.GlobalSightLocale;

public class TestXliffProcessor
{
    @Test
    public void test()
    {
        TuvImpl sourceTuv = new TuvImpl();
        TuImpl tu = new TuImpl();
        tu.setXliffTargetLanguage("fr_FR");
        sourceTuv.setTu(tu);
        tu.addTuv(sourceTuv);
        XliffAlt xa = new XliffAlt();
        xa.setSegment("alt test1");
        xa.setSourceSegment("alt source segment 1");
        xa.setLanguage("fr");
        xa.setQuality("86");
        xa.setTuv(sourceTuv);
        sourceTuv.addXliffAlt(xa);

        XliffAlt xa2 = new XliffAlt();
        xa2.setSegment("alt test2");
        xa2.setSourceSegment("alt source segment 2");
        xa2.setQuality("88");
        xa2.setTuv(sourceTuv);
        sourceTuv.addXliffAlt(xa2);

        XliffAlt xa3 = new XliffAlt();
        xa3.setSegment("alt test2");
        xa3.setSourceSegment("alt source segment 2");
        xa3.setLanguage("span");
        xa3.setQuality("90");
        xa3.setTuv(sourceTuv);
        sourceTuv.addXliffAlt(xa3);

        TuvImpl targetTuv = new TuvImpl();

        XliffProcessor processor = new XliffProcessor();
        GlobalSightLocale p_targetLocale = new GlobalSightLocale("fr", "FR",
                false);
        processor.addAltTrans(targetTuv, sourceTuv, p_targetLocale, "1000");

        XliffAlt maxAlt = processor.getMaxScoreAlt();
        assertTrue(maxAlt.getQuality().equals("88"));
        assertTrue(targetTuv.getXliffAlt(false).size() == 2);
    }

}
