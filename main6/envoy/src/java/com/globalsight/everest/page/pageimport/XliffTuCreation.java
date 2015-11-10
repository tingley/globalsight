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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.request.Request;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.ling.docproc.extractor.xliff20.XliffHelper;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;

public class XliffTuCreation implements IXliffTuCreation
{
    protected HashMap<String, String> attributeMap;
    
    // for xliff 1.0 
    private XliffAlt alt = new XliffAlt();
    
    // for xliff 2.0
    private Map<String, List<XliffAlt>> alts = new HashMap<String, List<XliffAlt>>();

    @Override
    public void setAttribute(HashMap<String, String> map)
    {
        attributeMap = map;
    }

    @Override
    public boolean transProcess(Request p_request, String xliffpart,
            GxmlElement elem, LeverageGroup p_lg, ArrayList p_tuList,
            GlobalSightLocale p_sourceLocale, long p_jobId)
    {
        if (xliffpart != null && xliffpart.equals("source"))
        {
            return true;
        }
        else if (xliffpart != null && xliffpart.equals("seg-source"))
        {
            return true;
        }
        else if (xliffpart != null && xliffpart.equals("target"))
        {
            List childs = elem.getChildElements();
            if (childs == null)
            {
                return false;
            }

            boolean isMadCapLingo = "true".equals(attributeMap
                    .get("isMadCapLingo"));

            String mrkId = elem.getAttribute("xliffSegSourceMrkId");
            String mrkIndex = elem.getAttribute("xliffSegSourceMrkIndex");
            GxmlElement seg = (GxmlElement) childs.get(0);
            ArrayList<Tu> array = (ArrayList<Tu>) p_lg.getTus(false);

            // "array" and "p_tuList" will be different?
            TuImpl tuPre = getRightTu(mrkId, mrkIndex, array, isMadCapLingo);
            TuImpl tu = getRightTu(mrkId, mrkIndex, p_tuList, isMadCapLingo);

            if (Text.isBlank(seg.getTextValue()))
            {
                tuPre.setXliffTarget(seg.toGxml());
                tu.setXliffTarget(seg.toGxml());
            }
            else
            {
                tuPre.setXliffTarget(seg.toGxml(IFormatNames.FORMAT_XLIFF));
                tu.setXliffTarget(seg.toGxml(IFormatNames.FORMAT_XLIFF));
            }

            if (attributeMap.get(XliffHelper.MARK_XLIFF_TARGET_LANG) != null)
            {
                tuPre.setXliffTargetLanguage(attributeMap
                        .get(XliffHelper.MARK_XLIFF_TARGET_LANG));
            }

            if (attributeMap.get("generatFrom") != null)
            {
                tuPre.setGenerateFrom(attributeMap.get("generatFrom"));
            }

            String state = elem.getAttribute("passoloState");
            if (state != null)
            {
                tuPre.setPassoloState(state);
            }
            
            // for xliff 2.0
            String mId = elem.getAttribute(XliffHelper.MRK_ID);
            if (mId != null)
            {
                List<XliffAlt> altList = alts.get(mId);
                if (altList != null)
                {
                    TuvImpl tuvPre = (TuvImpl) tuPre.getTuv(p_sourceLocale.getId(),
                            p_jobId);
                    
                    for (XliffAlt a : altList)
                    {
                        a.setTuv(tuvPre);
                        tuvPre.addXliffAlt(a);
                    }
                }
            }

            // Not required, but nice to have.
            setDefaultValueForXliffTargetColumn(p_tuList);

            return false;
        }
        else if (xliffpart != null && xliffpart.equals("altTarget"))
        {
            String altLanguage = elem.getAttribute("altLanguage");
            String altQuality = elem.getAttribute("altQuality");
            String altMid = elem.getAttribute("altMid");
            GxmlElement seg = (GxmlElement) elem.getChildElements().get(0);
            ArrayList<Tu> array = (ArrayList<Tu>) p_lg.getTus(false);
            boolean isXliff20 = XliffHelper.XLIFF_VERSION_20.equals(elem
                    .getAttribute(XliffHelper.XLIFF_VERSION));

            if (isXliff20)
            {
                List<XliffAlt> altList = alts.get(altMid);
                if (altList == null)
                {
                    altList = new ArrayList<XliffAlt>();
                    alts.put(altMid, altList);
                }
                
                alt.setSegment(seg.toGxml(IFormatNames.FORMAT_XLIFF));
                alt.setLanguage(altLanguage);
                alt.setQuality(altQuality);
                
                altList.add(alt);
            }
            else
            {
                TuImpl tuPre = null;
                if (altMid == null)
                {
                    for (int i = array.size() - 1; i >= 0; i--)
                    {
                        Tu tu = array.get(i);
                        String tuMrkId = tu.getXliffMrkId();
                        if (tuMrkId == null || "1".equals(tuMrkId))
                        {
                            tuPre = (TuImpl) tu;
                            break;
                        }
                    }
                }
                else
                {
                    for (int i = array.size() - 1; i >= 0; i--)
                    {
                        Tu tu = array.get(i);
                        String tuMrkId = tu.getXliffMrkId();

                        if ("true".equals(attributeMap.get("isMadCapLingo")))
                        {
                            boolean parsed = false;
                            int paredResult = -1;
                            if (tuMrkId != null)
                            {
                                try
                                {
                                    paredResult = Integer.parseInt(tuMrkId);
                                    parsed = true;
                                }
                                catch (Exception ex)
                                {
                                    parsed = false;
                                }
                            }

                            if (parsed)
                            {
                                tuMrkId = "" + (paredResult - 1);
                            }
                        }

                        if (altMid.equals(tuMrkId))
                        {
                            tuPre = (TuImpl) tu;
                            break;
                        }
                    }
                }

                if (tuPre == null)
                {
                    tuPre = (TuImpl) array.get(array.size() - 1);
                }

                TuvImpl tuvPre = (TuvImpl) tuPre.getTuv(p_sourceLocale.getId(),
                        p_jobId);
                alt.setSegment(seg.toGxml(IFormatNames.FORMAT_XLIFF));
                alt.setLanguage(altLanguage);
                alt.setQuality(altQuality);
                alt.setTuv(tuvPre);
                tuvPre.addXliffAlt(alt);

                array.set(array.indexOf(tuPre), tuPre);
            }

            return false;
        }
        else if (xliffpart != null && xliffpart.equals("altSource"))
        {
            boolean isXliff20 = XliffHelper.XLIFF_VERSION_20.equals(elem
                    .getAttribute(XliffHelper.XLIFF_VERSION));
            
            GxmlElement seg = (GxmlElement) elem.getChildElements().get(0);
            alt = new XliffAlt();
            alt.setSourceSegment(seg.toGxml(IFormatNames.FORMAT_XLIFF));
            return false;
        }

        return false;
    }

    private TuImpl getRightTu(String mrkId, String index, ArrayList<Tu> tuList,
            boolean isSVersion)
    {
        TuImpl tuPre = null;

        if (isSVersion)
        {
            if (index != null)
            {
                for (int i = tuList.size() - 1; i >= 0; i--)
                {
                    Tu tu = tuList.get(i);

                    if (index.equals(tu.getXliffMrkIndex()))
                    {
                        tuPre = (TuImpl) tu;
                        break;
                    }
                }
            }
        }
        else
        {
            if (mrkId != null)
            {
                for (int i = tuList.size() - 1; i >= 0; i--)
                {
                    Tu tu = tuList.get(i);

                    if (mrkId.equals(tu.getXliffMrkId()))
                    {
                        tuPre = (TuImpl) tu;
                        break;
                    }
                }
            }
        }

        if (tuPre == null)
        {
            if (tuList != null && tuList.size() > 0)
            {
                tuPre = (TuImpl) tuList.get(tuList.size() - 1);
            }
        }

        return tuPre;
    }

    /**
     * For XLF based formats, "XLIFF_TARGET_SEGMENT" column CAN be null, but to
     * be more common, we set default value if null.
     * 
     * @param tus
     */
    private void setDefaultValueForXliffTargetColumn(Collection<Tu> tus)
    {
        if (tus == null || tus.size() == 0)
            return;

        for (Iterator it = tus.iterator(); it.hasNext();)
        {
            Tu tu = (Tu) it.next();
            if (tu.getXliffTarget() == null)
            {
                tu.setXliffTarget("<segment segmentId=\"1\"> </segment>");
            }
        }
    }
}
