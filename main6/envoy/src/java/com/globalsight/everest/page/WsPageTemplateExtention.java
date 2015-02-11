/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.everest.page;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.dom4j.io.SAXReader;

import com.globalsight.everest.edit.offline.xliff.ListViewWorkXLIFFWriter;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.LeverageMatchLingManagerLocal;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.tm2.leverage.Leverager;

public class WsPageTemplateExtention implements IPageTemplateExtention
{
    private boolean wfFinished = false;

    public WsPageTemplateExtention(boolean wfFinished)
    {
        this.wfFinished = wfFinished;
    }

    @Override
    public String processSkeleton(String skeletonStr, Tuv tuv, long p_jobId)
    {
        Tu tu = tuv.getTu(p_jobId);
        boolean isLocalized = isTuvLocalized(tuv);
        boolean isComplete = (tuv.getState().getValue() == TuvState.COMPLETE
                .getValue());
        // If the tuv state is "localized" or
        // "exact_match_localized"", add an attribute "isLocalized=yes"
        // to the translatable element, else add attribute
        // "isLocalized = no"
        if (isLocalized || isComplete)
        {
            String lastModUser = tuv.getLastModifiedUser();
            String sourceContent = tu.getSourceContent();

            if (lastModUser != null && lastModUser.indexOf("_MT") > -1)
            {
                // For GBS-1864 by York on 2011-03-07
                // Avoid to change "iws:segment-metadata"
                // content for "repetition" segments.
                if (Extractor.IWS_REPETITION.equalsIgnoreCase(sourceContent)
                        && !wfFinished)
                {
                    return replaceString(skeletonStr, "no");
                }
                else
                {
                    return replaceString(skeletonStr, PageTemplate.byMT);
                }
            }
            else if (lastModUser != null
                    && !lastModUser.equals(IFormatNames.FORMAT_TDA)
                    && !lastModUser.equals(IFormatNames.FORMAT_XLIFF_NAME)
                    && !lastModUser.equals(IFormatNames.FORMAT_PO))
            {
                return replaceString(skeletonStr, PageTemplate.byUser);
            }
            else if (lastModUser == null)
            {
                return replaceString(skeletonStr, PageTemplate.byLocalTM);
            }
        }
        else
        {
            return replaceString(skeletonStr, "no");
        }

        return skeletonStr;
    }

    private String replaceString(String skeletonStr, String replaceStr)
    {
        String trasStr = "<" + DiplomatNames.Element.TRANSLATABLE;
        skeletonStr = skeletonStr.replace(trasStr, trasStr + " "
                + DiplomatNames.Attribute.ISLOCALIZED + "=\"" + replaceStr
                + "\" ");
        return skeletonStr;
    }

    /**
     * If the specified Tuv is localized.
     * 
     * @param p_tuv
     * @return
     */
    private boolean isTuvLocalized(Tuv p_tuv)
    {
        if (p_tuv == null)
        {
            return false;
        }

        boolean result = false;
        result = (p_tuv.getState().getValue() == TuvState.LOCALIZED.getValue())
                || (p_tuv.getState().getValue() == TuvState.EXACT_MATCH_LOCALIZED
                        .getValue())
                || (p_tuv.getState().getValue() == TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED
                        .getValue())
                || (p_tuv.getState().getValue() == TuvState.UNVERIFIED_EXACT_MATCH
                        .getValue());

        return result;
    }

    @Override
    public String getAltTrans(Tuv sourceTuv, Tuv targetTuv, long p_jobId)
    {
        String altStr = new String();
        Tu tu = sourceTuv.getTu(p_jobId);
        // if the job creating file is from worldserver, then add the
        // leverage match results into the alt-trans parts
        if (tu.getGenerateFrom() != null
                && tu.getGenerateFrom().equals(TuImpl.FROM_WORLDSERVER))
        {
            LeverageMatchLingManagerLocal lmm = new LeverageMatchLingManagerLocal();
            SortedSet<LeverageMatch> lms = lmm.getTuvMatches(
                    sourceTuv.getIdAsLong(), targetTuv.getLocaleId(), "0",
                    false, p_jobId);

            List<LeverageMatch> list = new ArrayList<LeverageMatch>(lms);
            altStr = getAltTransOfMatch(list, p_jobId);
        }

        return altStr;
    }

    private String getAltTransOfMatch(List<LeverageMatch> p_list, long p_jobId)
    {
        String altStr = new String();
        ListViewWorkXLIFFWriter lvwx = new ListViewWorkXLIFFWriter();

        if (p_list != null)
        {
            LeverageMatch.orderMatchResult(p_list);

            SAXReader reader = new SAXReader();
            for (int i = 0; i < p_list.size(); i++)
            {
                LeverageMatch leverageMatch = p_list.get(i);

                if (judgeIfneedAdd(leverageMatch))
                {
                    altStr = altStr
                            + lvwx.getAltByMatch(leverageMatch, null, reader,
                                    p_jobId);
                }
            }
        }

        return altStr;
    }

    /*
     * Judge if need add the leverage match result into alt-trans when export
     * There are three condition need not add, because they are repeated with
     * the target content or the original alt-trans content.
     * 
     * 1. Auto-commit:
     * 
     * project_tm_index == -2(MT_PRIORITY) && score_num == 100 && Target tuv
     * content == leverage match content (no changed)
     * 
     * Not write this into "alt-trans".
     * 
     * 2. Target has valid content (not empty and not placeholder-only) Target
     * has valid content (LM data is from target) target is same with original
     * target(not modified by user) || trans type is "machine_translation_mt"
     * 
     * Not write this into "alt-trans".
     * 
     * 3. Target has NO valid content (empty or placeholder-only) && come from
     * xliff alt, not write this into "alt-trans".
     */
    private boolean judgeIfneedAdd(LeverageMatch lm)
    {
        try
        {
            SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                    lm.getSourcePageId());
            long jobId = sp.getJobId();
            Tuv sourceTuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                    lm.getOriginalSourceTuvId(), jobId);
            Tuv targetTuv = sourceTuv.getTu(jobId).getTuv(
                    lm.getTargetLocaleId(), jobId);
            // boolean isWSXlf = false;
            // if (TuImpl.FROM_WORLDSERVER.equalsIgnoreCase(sourceTuv.getTu(
            // companyId).getGenerateFrom()))
            // {
            // isWSXlf = true;
            // }

            String targetContent = targetTuv.getGxml();
            String originalTarget = sourceTuv.getTu(jobId)
                    .getXliffTargetGxml().getTextValue();

            if (lm.getProjectTmIndex() == Leverager.MT_PRIORITY
                    && lm.getScoreNum() == 100
                    && (lm.getMatchedText().equals(targetContent)))
            {
                // For GBS-1864 (if work-flow is in progress, and
                // source_content="repetition", MT translation
                // will NOT be written into "target", so need add MT translation
                // into "alt-trans".
                String sourceContentAtt = sourceTuv.getTu(jobId)
                        .getSourceContent();
                if (wfFinished
                        && Extractor.IWS_REPETITION
                                .equalsIgnoreCase(sourceContentAtt))
                {
                    return true;
                }
                else
                {
                    // auto committed by MT and not modified by user
                    return false;
                }

            }
            else if (!originalTarget.trim().isEmpty())
            {
                // The original target content is not empty and not modified by
                // user
                String transType = ((TuImpl) sourceTuv.getTu(jobId))
                        .getXliffTranslationType();

                if (lm.getMatchedText().equals(targetContent))
                {
                    return false;
                }
                else if ((transType != null && transType
                        .equals("machine_translation_mt"))
                        && targetTuv.getState().getValue() == TuvState.NOT_LOCALIZED
                                .getValue())
                {
                    return false;
                }
            }
            else if (originalTarget.trim().isEmpty())
            {
                // from max score alt trans target content
                if (lm.getProjectTmIndex() == Leverager.XLIFF_PRIORITY)
                {
                    return false;
                }
            }
        }
        catch (Exception e)
        {
        }

        return true;
    }
}
