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
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.integration.ling.tm2.Types;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;

public class ReportGeneratorUtil
{
    /**
     * Sets the cell value for internal text.
     * 
     * @since GBS-4663
     */
    public static void setCellForInternalText(Cell p_cell, String p_content, boolean p_rtlLocale,
            Font p_internalFont, Font p_contentFont)
    {
        // GBS-4735, remove ignored string
        p_content = StringUtil.replace(p_content, ReportGenerator.IGNORED_STRING_X000D, "");
        if (p_content.indexOf(GxmlElement.GS_INTERNAL_BPT) != -1)
        {
            String contentWithoutMark = StringUtil.replace(p_content, GxmlElement.GS_INTERNAL_BPT,
                    "");
            contentWithoutMark = StringUtil.replace(contentWithoutMark, GxmlElement.GS_INTERNAL_EPT,
                    "");
            contentWithoutMark = p_rtlLocale ? EditUtil.toRtlString(contentWithoutMark)
                    : contentWithoutMark;
            XSSFRichTextString ts = new XSSFRichTextString(contentWithoutMark);
            while (p_content.indexOf(GxmlElement.GS_INTERNAL_BPT) != -1)
            {
                int internalBpt = p_content.indexOf(GxmlElement.GS_INTERNAL_BPT);
                p_content = p_content.substring(0, internalBpt)
                        + p_content.substring(internalBpt + GxmlElement.GS_INTERNAL_BPT.length());
                int internalEpt = p_content.indexOf(GxmlElement.GS_INTERNAL_EPT);
                p_content = p_content.substring(0, internalEpt)
                        + p_content.substring(internalEpt + GxmlElement.GS_INTERNAL_EPT.length());

                ts.applyFont(p_rtlLocale ? internalBpt + 1 : internalBpt,
                        p_rtlLocale ? internalEpt + 1 : internalEpt, p_internalFont);
                ts.applyFont(p_rtlLocale ? internalEpt + 1 : internalEpt,
                        contentWithoutMark.length(), p_contentFont);
            }
            p_cell.setCellValue(ts);
        }
        else
        {
            p_cell.setCellValue(p_rtlLocale ? EditUtil.toRtlString(p_content) : p_content);
        }
    }

    /**
     * Gets TM Matches.
     */
    public static StringBuilder getMatches(Map fuzzyLeverageMatchMap,
            MatchTypeStatistics tuvMatchTypes, Vector<String> excludedItemTypes, List sourceTuvs,
            List targetTuvs, ResourceBundle bundle, Tuv sourceTuv, Tuv targetTuv, long p_jobId)
    {
        StringBuilder matches = new StringBuilder();

        Set fuzzyLeverageMatches = (Set) fuzzyLeverageMatchMap.get(sourceTuv.getIdAsLong());
        if (LeverageUtil.isIncontextMatch(sourceTuv, sourceTuvs, targetTuvs, tuvMatchTypes,
                excludedItemTypes, p_jobId))
        {
            matches.append(bundle.getString("lb_in_context_match"));
        }
        else if (LeverageUtil.isExactMatch(sourceTuv, tuvMatchTypes))
        {
            matches.append(StringUtil.formatPCT(100));
        }
        else if (fuzzyLeverageMatches != null)
        {
            int count = 0;
            for (Iterator ite = fuzzyLeverageMatches.iterator(); ite.hasNext();)
            {
                LeverageMatch leverageMatch = (LeverageMatch) ite.next();
                if ((fuzzyLeverageMatches.size() > 1))
                {
                    matches.append(++count).append(", ")
                            .append(StringUtil.formatPCT(leverageMatch.getScoreNum()))
                            .append("\r\n");
                }
                else
                {
                    matches.append(StringUtil.formatPCT(leverageMatch.getScoreNum()));
                    break;
                }
            }
        }
        else
        {
            matches.append(bundle.getString("lb_no_match_report"));
        }

        if (matches.indexOf("100%") == -1
                && matches.indexOf(bundle.getString("lb_in_context_match")) == -1)
        {
            if (targetTuv.isRepeated())
            {
                matches.append("\r\n")
                        .append(bundle.getString("jobinfo.tradosmatches.invoice.repeated"));
            }
            else if (targetTuv.getRepetitionOfId() > 0)
            {
                matches.append("\r\n")
                        .append(bundle.getString("jobinfo.tradosmatches.invoice.repetition"));
            }
        }

        // GBS-3905: mt translation into target tuv directly
        if (targetTuv.getLastModifiedUser() != null
                && targetTuv.getLastModifiedUser().toLowerCase().endsWith("_mt"))
        {
            matches.append("\r\n").append("MT Match");
        }
        else
        {
            // mt translation to tm, then search out for target tuv
            Types type = tuvMatchTypes.getTypes(sourceTuv.getId(), LeverageUtil.DUMMY_SUBID);
            if (type != null)
            {
                LeverageMatch lm = type.getLeverageMatch();
                if ((lm.getMtName() != null && lm.getMtName().toLowerCase().endsWith("_mt"))
                        || (lm.getCreationUser() != null
                                && lm.getCreationUser().toLowerCase().endsWith("_mt")))
                {
                    matches.append("\r\n").append("MT Match");
                }
            }
        }

        return matches;
    }
}
