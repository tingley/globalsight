package com.globalsight.everest.edit.offline.page;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.unittest.util.ServerUtil;
import com.globalsight.ling.tm2.leverage.Leverager;

public class OfflinePageDataTest
{
    @BeforeClass
    public static void setUp() throws Exception
    {
        ServerUtil
                .initServerInstance(com.globalsight.everest.tuv.TuvManagerWLImpl.class);
    }

    /**
     * Only for GBS-1954,"getSourceTargetText(...)" is not fully tested here.
     */
    @Test
    public void testGetSourceTargetText()
    {
        OfflinePageData opd = new OfflinePageData();

        LeverageMatch match = new LeverageMatch();
        match.setMatchedText("Sample Document in Chinese");
        match.setProjectTmIndex(Leverager.MT_PRIORITY);
        match.setOriginalSourceTuvId(-1);
        match.setMtName("Google_MT");

        String sourceText = "<segment segmentId=\"1\" wordcount=\"2\"><bpt i=\"1\" type=\"x-span\" x=\"1\">&lt;span style=&apos;font-family:&quot;Arial&quot;,&quot;sans-serif&quot;&apos;&gt;</bpt>Sample Document<ept i=\"1\">&lt;/span&gt;</ept></segment>";
        String targetText = "<segment segmentId=\"1\" wordcount=\"2\"><bpt i=\"1\" type=\"x-span\" x=\"1\">&lt;span style=&apos;font-family:&quot;Arial&quot;,&quot;sans-serif&quot;&apos;&gt;</bpt>Sample Document in Chinese<ept i=\"1\">&lt;/span&gt;</ept></segment>";
        String userId = "yorkadmin";
        boolean isFromXliff = false;
        String sourceLocal = "en_US";
        String targetLocal = "zh_CN";

        ArrayList arr = opd.getSourceTargetText(null, match, sourceText,
                targetText, userId, isFromXliff, sourceLocal, targetLocal,
                true, "1000");
        Assert.assertSame("MT!", (String) arr.get(2));

        arr = opd.getSourceTargetText(null, match, sourceText, targetText,
                userId, isFromXliff, sourceLocal, targetLocal, false, "1000");
        Assert.assertSame("Google_MT", (String) arr.get(2));
    }

}
