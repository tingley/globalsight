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

import java.util.HashMap;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.tm2.leverage.MatchState;

public class WSMatchesProcess extends XliffMatchesProcess
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
		// 1. For WS XLF file, if it has a fuzzy match whose TM score is between
		// [threshold,100), keep target same with source.
		// 2. The "translation_type" is "machine_translation_mt", keep target
		// same with source.
		if ((tmScore >= threshold && tmScore < 100)
				|| tu.isXliffTranslationMT())
		{
            tu.addTuv(targetTuv);
            p_appliedTuTuvMap.put(tu, targetTuv);
            return true;
        }

		// 3. If "xliff_translation_type='manual_translation'", keep target same
		// with source.
        boolean isManualTranslation = 
                Extractor.IWS_TRANSLATION_MANUAL
                    .equalsIgnoreCase(tu.getXliffTranslationType());
        if (isManualTranslation)
        {
        	addTargetTuvToTu2(tu, targetTuv, trg, p_appliedTuTuvMap);
        	return true;
        }

        // 4. 
        boolean hasSameTags = compareTags(src, trg);
		if (tmScore == 100 && (hasSameTags || tu.isXliffLocked()))
        {
			addTargetTuvToTu2(tu, targetTuv, trg, p_appliedTuTuvMap);
			return true;
        }

        return false;
    }

	private void addTargetTuvToTu2(TuImpl tu, Tuv targetTuv, String trg,
			HashMap<Tu, Tuv> p_appliedTuTuvMap)
    {
        boolean isManualTranslation = 
                Extractor.IWS_TRANSLATION_MANUAL
                    .equalsIgnoreCase(tu.getXliffTranslationType());
        if (!trg.isEmpty())
        {
            targetTuv.setGxml(tu.getXliffTarget());
        }
        
        targetTuv.setMatchType(
                MatchState.XLIFF_EXACT_MATCH.getName());
        targetTuv.setLastModifiedUser(
                    IFormatNames.FORMAT_XLIFF_NAME.toUpperCase());

        // If lock_status="locked",set target TUV state to
		// "EXACT_MATCH_LOCALIZED" to ensure it will be populated into storage
		// TM when job is finished (GBS-1771).
        if (tu.isXliffLocked())
        {
            targetTuv.setState(TuvState.EXACT_MATCH_LOCALIZED);
        }
        else if (isManualTranslation)
        {
            targetTuv.setState(TuvState.LOCALIZED);
        }
        else
        {
            targetTuv.setState(TuvState.NOT_LOCALIZED);
        }
        
        tu.addTuv(targetTuv);
        p_appliedTuTuvMap.put(tu, targetTuv);
    }
}
