package com.globalsight.ling.tm3.integration.segmenttm;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.UPDATED_BY_PROJECT;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.leverage.LeverageMatches;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTu;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTuv;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3LeverageMatch;
import com.globalsight.ling.tm3.core.TM3LeverageResults;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3MatchType;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSDataFactory;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.util.GlobalSightLocale;

/**
 * This code gathers up some of the clutter of leveraging segments in TM3.
 */
class Tm3LeveragerAcrossMutipleTms
{
    private static final Logger LOGGER = Logger
            .getLogger(Tm3LeveragerAcrossMutipleTms.class);
    
    private static final int MAX_HITS = 10;
    private TM3Manager mgr = DefaultManager.create();

    private TM3Tm<GSTuvData> tm;
    private GlobalSightLocale srcLocale;
    private LeverageOptions leverageOptions;
    private Map<BaseTmTuv, LeverageMatches> progress;
    private TM3MatchType matchType;
    private List<Tm> projectTms = new ArrayList<Tm>();
    private Map<TM3Tm<GSTuvData>, Tm> tmMap = new HashMap<TM3Tm<GSTuvData>, Tm>();

    Tm3LeveragerAcrossMutipleTms(List<Tm> projectTms, TM3Tm<GSTuvData> tm,
            GlobalSightLocale srcLocale, LeverageOptions options,
            Map<BaseTmTuv, LeverageMatches> progress)
    {
        this.tm = tm;
        this.srcLocale = srcLocale;
        this.leverageOptions = options;
        this.progress = progress;
        this.matchType = leverageOptions.leverageOnlyExactMatches() ?
                TM3MatchType.EXACT : TM3MatchType.ALL;

        for (Tm projectTm : projectTms)
        {
            TM3Tm<GSTuvData> tm3Tm = getTM3Tm(projectTm);
            if (tm3Tm != null)
            {
                this.projectTms.add(projectTm);
                this.tmMap.put(tm3Tm, projectTm);
            }
            else
            {
                LOGGER.warn("TM " + projectTm.getId()
                        + " is not a TM3 TM, will not be leveraged");
            }
        }
    }

    void leverageSegment(BaseTmTuv srcTuv, Map<TM3Attribute, Object> attrs)
    {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("leverageSegment: " + srcTuv.toDebugString());
        }

        // fix for GBS-2448, user could search target locale in TM Search Page,
        // if not from TM Search Page, keep old logic(by isMultiLingLeveraging
        // of FileProfile)
        boolean lookupTarget;
        if (leverageOptions.isFromTMSearchPage()) {
            lookupTarget = true;
        } else {
            lookupTarget = leverageOptions.isMultiLingLeveraging();
        }
        List<Long> tm3TmIds = new ArrayList<Long>();
        for (Tm pTm : projectTms)
        {
            tm3TmIds.add(pTm.getTm3Id());
        }

        Set<GlobalSightLocale> trgLocales = leverageOptions
                .getLeveragingLocales().getAllLeveragingLocales();
        TM3LeverageResults<GSTuvData> results = tm.findMatches(new GSTuvData(
                srcTuv), srcLocale, trgLocales, attrs, matchType, lookupTarget,
                MAX_HITS, leverageOptions.getMatchThreshold(), tm3TmIds);

        // NB in this conversion, we lose which tuv was matched, only which
        // tus; identical tus will later be coalesced by
        // leverageDataCenter.addLeverageResultsOfSegmentTmMatching
        // which tuv will globalsight pick as the match? probably random
        LeverageMatches lm = progress.get(srcTuv);
        if (lm == null) {
            lm = new LeverageMatches(srcTuv, leverageOptions);
            progress.put(srcTuv, lm);
        }

        int order = 0;
        for (TM3LeverageMatch<GSTuvData> match : results.getMatches())
        {
            TM3Tu<GSTuvData> tu = match.getTu();
            TM3Tuv<GSTuvData> tmSrcTuv = match.getTuv();
            TM3Tm<GSTuvData> tm = tu.getTm();
            Tm projectTm = tmMap.get(tm);
            
            if (tm != null && projectTm == null)
            {
                try
                {
                    projectTm = ServerProxy.getProjectHandler()
                            .getProjectTMByTm3id(tm.getId());
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException(
                            "Non-existent project tm for TM3 id: " + tm.getId(),
                            e);
                }
                tmMap.put(tm, projectTm);
            }

            TM3Attribute typeAttr = TM3Util.getAttr(tm, TYPE);
            TM3Attribute formatAttr = TM3Util.getAttr(tm, FORMAT);
            TM3Attribute sidAttr = TM3Util.getAttr(tm, SID);
            TM3Attribute translatableAttr = TM3Util.getAttr(tm, TRANSLATABLE);
            TM3Attribute fromWsAttr = TM3Util.getAttr(tm, FROM_WORLDSERVER);
            TM3Attribute projectAttr = TM3Util.getAttr(tm, UPDATED_BY_PROJECT);            
            
            LeveragedSegmentTu ltu = new LeveragedSegmentTu(tu.getId(),
                    projectTm.getId(), (String) tu.getAttribute(formatAttr),
                    (String) tu.getAttribute(typeAttr), true, srcLocale);
            ltu.setScore(match.getScore());
            ltu.setMatchState(getMatchState(leverageOptions, match.getScore()));
            ltu.setFromWorldServer((Boolean) tu.getAttribute(fromWsAttr));
            if ((Boolean) tu.getAttribute(translatableAttr))
            {
                ltu.setTranslatable();
            }
            else
            {
                ltu.setLocalizable();
            }
            
            String sid = (String) tu.getAttribute(sidAttr);
            
            for (TM3Tuv<GSTuvData> tuv : tu.getAllTuv())
            {
                // Do not return unwanted TUVs.
                GlobalSightLocale tuvLocale = (GlobalSightLocale) tuv.getLocale();
                if (!tuvLocale.equals(srcLocale)
                        && !trgLocales.contains(tuvLocale))
                {
                    continue;
                }

                LeveragedSegmentTuv ltuv = new LeveragedSegmentTuv(tuv.getId(),
                        tuv.getContent().getData(),
                        (GlobalSightLocale) tuv.getLocale());
                ltuv.setTu(ltu);
                ltuv.setOrgSid(srcTuv.getSid());
                ltuv.setSid(sid);
                ltuv.setOrder(order);

                ltuv.setModifyDate(getModifyDate(tuv));
                ltuv.setModifyUser(tuv.getModifyUser());
                ltuv.setCreationDate(getCreationDate(tuv));
                ltuv.setCreationUser(tuv.getCreationUser());
                ltuv.setUpdatedProject((String) tu.getAttribute(projectAttr));
                ltuv.setLastUsageDate(getLastUsageDate(tuv));
                ltuv.setJobId(tuv.getJobId());
                ltuv.setJobName(tuv.getJobName());
                // for hash ICE, it should rely on source pre-next hash
                ltuv.setPreviousHash(tmSrcTuv.getPreviousHash());
                ltuv.setNextHash(tmSrcTuv.getNextHash());
                if (tuv.getSid() != null)
                {
                	ltuv.setSid(tuv.getSid());
                }

                ltu.addTuv(ltuv);
            }
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Score " + ltu.getScore() + ": "
                        + ltu.getFirstTuv(srcLocale));
            }
            lm.add(ltu);
            order++;
        }
    }

    private MatchState getMatchState(LeverageOptions options, int score)
    {
        MatchState state = MatchState.FUZZY_MATCH;

        if (score == 100) {
            state = MatchState.SEGMENT_TM_EXACT_MATCH;
        }
        else if (score < options.getMatchThreshold()) {
            state = MatchState.STATISTICS_MATCH;
        }
        return state;
    }

    private TM3Tm<GSTuvData> getTM3Tm(Tm tm)
    {
        TM3Tm<GSTuvData> tm3tm = mgr.getTm(new GSDataFactory(), tm.getTm3Id());
        if (tm3tm == null)
        {
            throw new IllegalArgumentException("Non-existent tm3 tm: "
                    + tm.getTm3Id());
        }

        return tm3tm;
    }

    private Timestamp getCreationDate(TM3Tuv<GSTuvData> tuv)
    {
        Date creationDate = tuv.getCreationDate();
        if (creationDate != null)
        {
            return new Timestamp(creationDate.getTime());
        }

        return null;
    }

    private Timestamp getModifyDate(TM3Tuv<GSTuvData> tuv)
    {
        Date modifyDate = tuv.getModifyDate();
        if (modifyDate != null)
        {
            return new Timestamp(modifyDate.getTime());
        }

        return null;
    }

    private Timestamp getLastUsageDate(TM3Tuv<GSTuvData> tuv)
    {
        Date lastUsageDate = tuv.getLastUsageDate();
        if (lastUsageDate != null)
        {
            return new Timestamp(lastUsageDate.getTime());
        }

        return null;
    }
}
