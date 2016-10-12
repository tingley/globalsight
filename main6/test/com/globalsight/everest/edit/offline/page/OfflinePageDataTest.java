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
                true, 1000);
        Assert.assertSame("MT!", (String) arr.get(2));

        arr = opd.getSourceTargetText(null, match, sourceText, targetText,
                userId, isFromXliff, sourceLocal, targetLocal, false, 1000);
        Assert.assertSame("Google_MT", (String) arr.get(2));
    }

    @Test
    public void testHandlePageId()
    {
        String src = "12692,13121,13236,13291,13141,12296,12785,13251,12379,12370,12766,12402,12989,12650,12930,12841,12851,12730,13034,12786,12580,12790,12696,12770,12555,12684,13240,12882,12300,12956,12479,12468,12428,13261,13041,12842,13097,13169,13172,12406,12506,12385,12768,13062,12647,12990,13088,12945,13310,12548,12812,12290,12938,12462,13010,12513,12415,12323,12597,12662,12426,12486,12676,13117,12733,12813,13023,13174,12881,13215,13122,12349,12550,13089,12731,13296,12708,12525,12694,12530,12968,12944,12358,12831,12586,13053,12856,13042,13081,13082,13156,13007,12985,12992,13276,12772,12860,12570,12638,12912,12455,13281,13055,12495,13150,12805,12664,12686,12637,12439,12389,13290,12850,12463,12639,12667,12407,12752,12348,12665,12811,12355,13024,12457,12608,13232,13047,13067,12727,12622,13186,12825,13086,13264,12891,12908,12886,12685,12599,12477,12590,12854,13191,12574,13151,12673,12298,13216,13259,13247,12642,12863,13148,13063,12319,12326,12957,12535,12897,12478,13304,12763,12959,13234,13224,12799,13299,12528,12894,12916,12716,12475,12844,13262,12890,12616,12872,13153,13115,13048,12821,12295,12612,13256,13212,12778,12818,12376,13265,13164,12879,12331,13282,12351,13036,12549,12332,13181,12690,12301,12761,13204,12585,13269,12413,12739,13285,12896,13018,12531,12581,13116,12567,12593,12522,1 2661,12973";
        String expected = "12692,13121,13236,13291,13141,12296,12785,13251,12379,12370,12766,12402,12989,12650,12930,12841,12851,12730,13034,12786,12580,12790,12696,12770,12555,12684,13240,12882,12300,12956,12479,12468,12428,13261,13041,12842,13097,13169,13172,12406,12506,12385,12768,13062,12647,12990,13088,12945,13310,12548,12812,12290,12938,12462,13010,12513,12415,12323,12597,12662,12426,12486,12676,13117,12733,12813,13023,13174,12881,13215,13122,12349,12550,13089,12731,13296,12708,12525,12694,12530,12968,12944,12358,12831,12586,13053,12856,13042,13081,13082,13156,13007,12985,12992,13276,12772,12860,12570,12638,12912,12455,13281,13055,12495,13150,12805,12664,12686,12637,12439,12389,13290,12850,12463,12639,12667,12407,12752,12348,12665,12811,12355,13024,12457,12608,13232,13047,13067,12727,12622,13186,12825,13086,13264,12891,12908,12886,12685,12599,12477,12590,12854,13191,12574,13151,12673,12298,13216,13259,13247,12642,12863,13148,13063,12319,12326,12957,12535,12897,12478,13304,12763,12959,13234,13224,12799,13299,12528,12894,12916,12716,12475,12844,13262,12890,12616,12872,13153,13115,13048,12821,12295,12612,13256,13212,12778,12818,12376,13265,13164,12879,12331,13282,12351,13036,12549,12332,13181,12690,12301,12761,13204,12585,13269,12413,12739,13285,12896,13018,12531,12581,13116,12567,12593,12522,12661,12973";
        OfflinePageData opd = new OfflinePageData();
        opd.handlePageId(src);
        Assert.assertEquals(expected, opd.getPageId());
    }
}
