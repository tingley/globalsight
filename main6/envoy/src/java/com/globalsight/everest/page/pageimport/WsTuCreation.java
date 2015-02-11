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

import com.globalsight.everest.request.Request;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;

public class WsTuCreation extends XliffTuCreation
{
    @Override
    public boolean transProcess(Request p_request, String xliffpart,
            GxmlElement elem, LeverageGroup p_lg, ArrayList p_tuList,
            GlobalSightLocale p_sourceLocale, long p_jobId)
    {
        boolean flag = super.transProcess(p_request, xliffpart, elem, p_lg,
                p_tuList, p_sourceLocale, p_jobId);

        String xliffTranslationType = elem
                .getAttribute(Extractor.IWS_TRANSLATION_TYPE);
        String xliffTMScore = elem
                .getAttribute(Extractor.IWS_TM_SCORE);
        String xliffSourceContent = elem
                .getAttribute(Extractor.IWS_SOURCE_CONTENT);
        String xliffLockStatus = elem
                .getAttribute(Extractor.IWS_LOCK_STATUS);

        if (xliffpart != null && xliffpart.equals("target"))
        {
            ArrayList<Tu> array = (ArrayList<Tu>) p_lg.getTus(false);
            TuImpl tuPre = (TuImpl) array.get(array.size() - 1);

            if (xliffTranslationType != null
                    && xliffTranslationType.length() > 0)
            {
                tuPre.setXliffTranslationType(xliffTranslationType);
            }

            if (xliffTMScore != null && xliffTMScore.length() > 0)
            {
                tuPre.setIwsScore(xliffTMScore);
            }

            if (xliffSourceContent != null
                    && xliffSourceContent.length() > 0)
            {
                tuPre.setSourceContent(xliffSourceContent);
            }

            if (xliffLockStatus != null
                    && xliffLockStatus.equals("locked"))
            {
                tuPre.setXliffLocked(true);
            }
        }

        return flag;
    }
}
