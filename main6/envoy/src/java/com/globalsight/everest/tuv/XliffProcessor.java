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

package com.globalsight.everest.tuv;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.util.GlobalSightLocale;

/**
 * Used to process the tuv from xliff file.
 * 
 * @author Walter.Xu
 * @since 2011-11-17
 * @version 8.2
 */
public class XliffProcessor implements IXliffProcessor
{
    private XliffAlt maxScoreAlt = new XliffAlt();
    private double maxAltTransScore;

    public XliffAlt getMaxScoreAlt()
    {
        return this.maxScoreAlt;
    }

    public double getMaxAltTransScore()
    {
        return this.maxAltTransScore;
    }

    @SuppressWarnings("rawtypes")
    public void addAltTrans(Tuv tuv, Tuv p_sourceTuv,
            GlobalSightLocale p_targetLocale, long p_jobId)
    {
        String xlfOrPoTargetLan = getTargetLanguage(
                (TuImpl) p_sourceTuv.getTu(p_jobId),
                p_sourceTuv.getGlobalSightLocale(), p_targetLocale);

        Set<XliffAlt> altSet = new HashSet<XliffAlt>();
        if (p_sourceTuv.getXliffAlt(false) != null
                && p_sourceTuv.getXliffAlt(false).size() > 0)
        {
            altSet.addAll(p_sourceTuv.getXliffAlt(false));
        }

        maxScoreAlt = new XliffAlt();
        maxAltTransScore = 0;

        if (altSet != null)
        {
            Iterator ite = altSet.iterator();
            while (ite.hasNext())
            {
                XliffAlt alt = (XliffAlt) ite.next();

                double altTransScore = 0;
                try
                {
                    altTransScore = Double.parseDouble(alt.getQuality());
                }
                catch (Exception ignore)
                {
                }

                boolean isValidXlfAltTrans = isValidXlfAltTrans(alt,
                        xlfOrPoTargetLan, p_targetLocale);

                if (isValidXlfAltTrans)
                {
                    XliffAlt xa = new XliffAlt();
                    xa.setSegment(alt.getSegment());
                    xa.setSourceSegment(alt.getSourceSegment());
                    xa.setLanguage(p_targetLocale.getLanguage());
                    xa.setQuality(alt.getQuality());
                    xa.setTuv((TuvImpl) tuv);
                    tuv.addXliffAlt(xa);

                    if (altTransScore > maxAltTransScore)
                    {
                        maxAltTransScore = altTransScore;
                        maxScoreAlt = xa;
                    }
                }
            }
        }
    }

    private boolean isValidXlfAltTrans(XliffAlt p_altTrans,
            String p_xlfOrPoTargetLan, GlobalSightLocale p_targetLocale)
    {
        boolean isValidXlfAltTrans = false;

        String targetLanguage = p_targetLocale.getLanguage();

        if (p_altTrans.getLanguage() == null
                && p_xlfOrPoTargetLan != null
                && p_xlfOrPoTargetLan.indexOf(targetLanguage.toLowerCase()) > -1)
        {
            isValidXlfAltTrans = true;
        }

        String altTransLang = p_altTrans.getLanguage();
        if (altTransLang != null
                && (altTransLang.equalsIgnoreCase(targetLanguage) || altTransLang
                        .toLowerCase().startsWith(targetLanguage.toLowerCase())))
        {
            isValidXlfAltTrans = true;
        }

        return isValidXlfAltTrans;
    }

    public String getTargetLanguage(TuImpl p_tu,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
    {
        String targetLanguage = p_tu.getXliffTargetLanguage();

        if (targetLanguage != null)
        {
            targetLanguage = targetLanguage.toLowerCase();
        }

        return targetLanguage;
    }

}
