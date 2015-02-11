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

package com.globalsight.everest.page.pageimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.tm2.leverage.MatchState;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.ling.tw.internal.XliffInternalTag;
import com.globalsight.util.edit.GxmlUtil;

public class XliffMatchesProcess implements IXliffMatchesProcessor
{

    @Override
    public boolean addTargetTuvToTu(TuImpl tu, 
                                    Tuv sourceTuv, 
                                    Tuv targetTuv,
                                    String src,
                                    String trg,
                                    HashMap<Tu, Tuv> p_appliedTuTuvMap, 
                                    float tmScore, 
                                    long threshold,
                                    boolean isPO)
    {
        String textValue = tu.getXliffTargetGxml().getTextValue();
        if(textValue.trim().isEmpty()) {
            trg = "";
        }
        // if target is not same with source,check tags in
        // them for "offline uploading".
        // if they have not same tags,target content
        // can't be saved to target tuv even it is different
        // with source content.(GBS-1211).
        boolean hasSameTags = compareTags(src, trg);

        if (tmScore == 100) {
            if (hasSameTags || "no".equalsIgnoreCase(tu.getTranslate())) 
            {
                if (!trg.isEmpty()) {
                    targetTuv.setGxml(tu.getXliffTarget());
                }

                if (isPO) {
                    targetTuv.setMatchType(
                        MatchState.PO_EXACT_MATCH.getName());
                } else {
                    targetTuv.setMatchType(
                        MatchState.XLIFF_EXACT_MATCH.getName());
                }

                targetTuv.setLastModifiedUser(isPO ?
                    IFormatNames.FORMAT_PO.toUpperCase() : 
                        IFormatNames.FORMAT_XLIFF_NAME.toUpperCase());

                if (isPO || "no".equalsIgnoreCase(tu.getTranslate()))
                {
                    targetTuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
                }
                else if ("Translated and reviewed".equalsIgnoreCase(tu.getPassoloState())
                        || "Translated".equalsIgnoreCase(tu.getPassoloState()))
                {
                    // Adds for Passolo TUV.
                    targetTuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
                }
                else
                {
                    targetTuv.setState(TuvState.NOT_LOCALIZED);
                }

                tu.addTuv(targetTuv);
                p_appliedTuTuvMap.put(tu, targetTuv);
                return true;
            } else {
                tu.addTuv(targetTuv);
                p_appliedTuTuvMap.put(tu, targetTuv);

                return true;
            }
        }
        
        return false;
    }
    
    protected String getSourceString(Tuv sourceTuv) {
        String sourceGxmlWithoutTopTags = sourceTuv.getGxmlExcludeTopTags();
        String src = sourceGxmlWithoutTopTags.trim();
        
        return src;
    }
    
    protected String getTargetString(Tuv targetTuv, long p_jobId)
    {
        // "trg"
        String targetGxml = targetTuv.getTu(p_jobId).getXliffTargetGxml()
                .toGxml();
        targetGxml = ((TuvImpl) targetTuv)
                .encodeGxmlAttributeEntities(targetGxml);
        String targetGxmlWithoutTopTags =
            GxmlUtil.stripRootTag(targetGxml);
        String trg = targetGxmlWithoutTopTags.trim();
        
        return trg;
    }
    
    protected boolean compareTags(String str1, String str2)
    {
        Map str1Map = null;
        Map str2Map = null;
        try {
            str1Map = convertSegment2Pseudo(str1);
            str2Map = convertSegment2Pseudo(str2);
        } catch (Exception ex) {
            return false;
        }

        boolean hasSameTags = true;
        List str1Keys = null;
        if (str1Map != null) {
            str1Keys = new ArrayList(str1Map.keySet());
        }
        List str2Keys = null;
        if (str2Map != null) {
            str2Keys = new ArrayList(str2Map.keySet());
        }

        int str1Size = 0;
        int str2Size = 0;
        if (str1Keys != null) {
            str1Size = str1Keys.size();
        }
        if (str2Keys != null) {
            str2Size = str2Keys.size();
        }

        if (str1Size != str2Size) {
            hasSameTags = false;
        } else if (str1Size == 0) {
            // do nothing
        } else {
            for (int i = 0; i < str1Keys.size(); i++) {
                String key1 = (String) str1Keys.get(i);
                if (!str2Keys.contains(key1)) {
                    hasSameTags = false;
                }
            }
        }

        return hasSameTags;
    }
    
    private Map convertSegment2Pseudo(String textContent)
            throws DiplomatBasicParserException
    {
        if (textContent == null || "".equals(textContent.trim()))
        {
            return null;
        }

        PseudoData PTagData = null;
        // Create PTag resources
        PTagData = new PseudoData();
        PTagData.setMode(2);

        // configure addable ptags for this format
        PTagData.setAddables("html");

        // convert the current source text and
        // set the native map to represent source tags
        textContent = XliffInternalTag.revertXliffInternalText(textContent);
        TmxPseudo.tmx2Pseudo(textContent, PTagData);

        return PTagData.getPseudo2TmxMap();
    }

}
