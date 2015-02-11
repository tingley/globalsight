package com.globalsight.everest.integration.ling.tm2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTu;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTuv;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTu;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTuv;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.GlobalSightLocale;

public class LeverageMatchLingManagerLocalTest
{
    private static final String MAX_ORDER_NUM = "select max(order_num) from leverage_match "
            + "where order_num > 0 "
            + "and order_num < "
            + TmCoreManager.LM_ORDER_NUM_START_REMOTE_TM
            + " "
            + "and original_source_tuv_id = ? "
            + "and target_locale_id = ? "
            + "and sub_id = ? ";

    /**
     * For saveLeverageResults(...) APIs.
     */
    @Test
    public void testGetNonClobMatches()
    {
        Connection conn = null;
        PreparedStatement ps = null;
        try
        {
            conn = DbUtil.getConnection();
            ps = conn.prepareStatement(MAX_ORDER_NUM);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        long sourcePageId = 1234;
        GlobalSightLocale sourceLocale = new GlobalSightLocale("en", "US", true);
        GlobalSightLocale targetLocale = new GlobalSightLocale("zh", "CN",
                false);

        // levMatches :: sourceTuv
        String text = "This is my book.";
        long tuId = 1000;
        String type = "text";
        BaseTmTuv sourceTuv = TmUtil.createTmSegment(text, tuId, sourceLocale,
                type, true);
        sourceTuv.setId(1001);
        assertEquals("Segment is different!",
                "<segment>This is my book.</segment>", sourceTuv.getSegment());
        assertTrue(sourceTuv.getTu().isTranslatable());
        // levMatches :: leverageOptions :: tmProfile
        TranslationMemoryProfile tmProfile = new TranslationMemoryProfile();
        tmProfile.setId(1002);
        tmProfile
                .setMultipleExactMatches(TranslationMemoryProfile.LATEST_EXACT_MATCH);
        tmProfile.setNumberOfMatchesReturned(5);
        tmProfile.setFuzzyMatchThreshold(75);
        // levMatches :: leverageOptions :: levLocales
        LeveragingLocales levLocales = new LeveragingLocales();
        Set<GlobalSightLocale> leveragingLocales = new HashSet();
        leveragingLocales.add(targetLocale);
        levLocales.setLeveragingLocale(targetLocale, leveragingLocales);
        // levMatches :: leverageOptions
        LeverageOptions leverageOptions = new LeverageOptions(tmProfile,
                levLocales);
        // levMatches
        LeverageMatches levMatches = new LeverageMatches(sourceTuv,
                leverageOptions);

        // Create an in progress match
        long srcId = 1003;
        long jobId = 1004; // src.job_id
        String srcType = "text"; // src.type
        LeveragedInProgressTu ipMatchTu = new LeveragedInProgressTu(srcId,
                jobId, srcType, true, sourceLocale);
        ipMatchTu.setMatchState(MatchState.IN_PROGRESS_TM_EXACT_MATCH);
        ipMatchTu.setScore(100);
        // LeveragedInProgressTuv srcIpMatchTuv = new LeveragedInProgressTuv(
        // srcId, "<segment>This is my book.</segment>", sourceLocale);
        // ipMatchTu.addTuv(srcIpMatchTuv);
        long trgId = 1005; // trg.id
        long originalTuId = 1006; // trg.tu_id
        String trgSegment = "<segment>This is my book. - York edited in Chinese - In Progress.</segment>";
        LeveragedInProgressTuv trgIpMatchTuv = new LeveragedInProgressTuv(
                trgId, trgSegment, targetLocale);
        trgIpMatchTuv.setJobDataTuId(originalTuId);
        ipMatchTu.addTuv(trgIpMatchTuv);
        // add this to levMatches.
        levMatches.add(ipMatchTu);

        // Create a static match from gold TM
        LeveragedSegmentTu staticMatchTu = new LeveragedSegmentTu(1007, 1,
                "html", "text", true, sourceLocale);
        staticMatchTu.setScore(100);
        staticMatchTu.setMatchState(MatchState.SEGMENT_TM_EXACT_MATCH);
        staticMatchTu.setFromWorldServer(false);
        staticMatchTu.setTranslatable();
        String sid = "keyAsSID";

        String segmentData = "<segment>This is my book. - York edited in Chinese - static match.</segment>";
        LeveragedSegmentTuv ltuv = new LeveragedSegmentTuv(1008, segmentData,
                targetLocale);
        ltuv.setTu(staticMatchTu);
        ltuv.setOrgSid(null);
        ltuv.setSid(sid);
        ltuv.setOrder(1);
        long time = (new Date()).getTime();
        ltuv.setModifyDate(new Timestamp(time));
        ltuv.setModifyUser("modifyUser");
        ltuv.setCreationDate(new Timestamp(time));
        ltuv.setCreationUser("creationUser");
        ltuv.setUpdatedProject("updateProject");
        staticMatchTu.addTuv(ltuv);
        levMatches.add(staticMatchTu);

        LeverageMatchLingManagerLocal lmlm = new LeverageMatchLingManagerLocal();
        Collection<LeverageMatch> results = (Collection) ClassUtil.testMethod(
                lmlm, "getNonClobMatches", ps, levMatches, targetLocale,
                leverageOptions, sourcePageId, null);
        assertTrue(results.size() == 2);
        for (LeverageMatch lm : results)
        {
            int projectTmIndex = lm.getProjectTmIndex();
            if (projectTmIndex == Leverager.IN_PROGRESS_TM_PRIORITY)
            {
                assertTrue("MatchedTuvId error", lm.getMatchedTuvId() == -1);
                assertEquals("MatchType error!", "IN_PROGRESS_TM_EXACT_MATCH",
                        lm.getMatchType());
                assertTrue("TmId error", lm.getTmId() == 1004);
                assertTrue("MatchedTableType error",
                        lm.getMatchedTableType() == 5);
                assertTrue(lm.getSourcePageId() == 1234);
            }
            else if (projectTmIndex == Leverager.HIGHEST_PRIORTIY)
            {
                assertTrue("MatchedTuvId error", lm.getMatchedTuvId() == 1008);
                assertEquals("MatchType error!", "SEGMENT_TM_EXACT_MATCH",
                        lm.getMatchType());
                assertTrue("TmId error", lm.getTmId() == 1);
                assertTrue("MatchedTableType error",
                        lm.getMatchedTableType() == 1);
            }
        }

        DbUtil.silentClose(ps);
        DbUtil.silentReturnConnection(conn);
    }

    @Test
    public void testDeleteLeverageMatches()
    {
        LeverageMatchLingManagerLocal lmlm = new LeverageMatchLingManagerLocal();
        long originalSourceTuvId = 1000;
        String subId = "0";
        long targetLocaleId = 87;

        String hql = (String) ClassUtil.testMethod(lmlm, "getDeleteHql",
                originalSourceTuvId, subId, targetLocaleId, null);
        String expectedHql = " from LeverageMatch lm  WHERE lm.originalSourceTuvId = :TUV_ID AND lm.subId = :SUB_ID AND lm.targetLocale = :TARGET_LOCALE_ID";
        assertEquals("Hql is incorrect!", expectedHql, hql);
        HashMap map = (HashMap) ClassUtil.testMethod(lmlm, "getParamMap",
                originalSourceTuvId, subId, targetLocaleId, null);
        String expectedMapContenct = "{TARGET_LOCALE_ID=87, TUV_ID=1000, SUB_ID=0}";
        assertEquals("Hql is incorrect!", expectedMapContenct, map.toString());
    }

}
