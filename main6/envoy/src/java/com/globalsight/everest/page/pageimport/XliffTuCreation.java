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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.globalsight.everest.comment.IssueEditionRelation;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.docproc.extractor.xliff.XliffAlt;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;

public class XliffTuCreation implements IXliffTuCreation
{
    protected HashMap<String, String> attributeMap;
    private XliffAlt alt = new XliffAlt();

    @Override
    public void setAttribute(HashMap<String, String> map)
    {
        // TODO Auto-generated method stub
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
            
            boolean isMadCapLingo = "true".equals(attributeMap.get("isMadCapLingo"));
            
            String mrkId = elem.getAttribute("xliffSegSourceMrkId");
            String mrkIndex = elem.getAttribute("xliffSegSourceMrkIndex");
            GxmlElement seg = (GxmlElement) childs.get(0);
            ArrayList<Tu> array = (ArrayList<Tu>) p_lg.getTus(false);

            TuImpl tuPre = getRightTu(mrkId, mrkIndex, array, isMadCapLingo);
            TuvImpl tuvPre = (TuvImpl) tuPre.getTuv(p_sourceLocale.getId(),
                    p_jobId);

            setGSEditon(p_request, tuvPre, elem);

            TuImpl tu = getRightTu(mrkId, mrkIndex, p_tuList, isMadCapLingo);
            
            if (Text.isBlank(seg.getTextValue()))
            {
                // Tuv srcTuv =
                // tuPre.getTuv(p_sourceLocale.getId());
                // tuPre.setXliffTarget(srcTuv.getGxml());
                tuPre.setXliffTarget(seg.toGxml());
                tu.setXliffTarget(seg.toGxml());
            }
            else
            {
                // String str =
                // tuvPre.encodeGxmlAttributeEntities(seg.toGxml(pageDataType));
                tuPre.setXliffTarget(seg.toGxml(GxmlElement.XLF));
                tu.setXliffTarget(seg.toGxml("xlf"));
            }

            if (attributeMap.get("xliffTargetLan") != null)
            {
                tuPre.setXliffTargetLanguage(attributeMap.get("xliffTargetLan"));
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

            return false;
        }
        else if (xliffpart != null && xliffpart.equals("altTarget"))
        {
            String altLanguage = elem.getAttribute("altLanguage");
            String altQuality = elem.getAttribute("altQuality");
            String altMid = elem.getAttribute("altMid");
            GxmlElement seg = (GxmlElement) elem.getChildElements().get(0);
            ArrayList<Tu> array = (ArrayList<Tu>) p_lg.getTus(false);
            
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
            alt.setSegment(seg.toGxml(GxmlElement.XLF));
            alt.setLanguage(altLanguage);
            alt.setQuality(altQuality);
            alt.setTuv(tuvPre);
            tuvPre.addXliffAlt(alt);

            array.set(array.indexOf(tuPre), tuPre);

            return false;
        }
        else if (xliffpart != null && xliffpart.equals("altSource"))
        {
            GxmlElement seg = (GxmlElement) elem.getChildElements().get(0);

            alt = new XliffAlt();
            alt.setSourceSegment(seg.toGxml(GxmlElement.XLF));
            return false;
        }

        return false;
    }

    private TuImpl getRightTu(String mrkId, String index, ArrayList<Tu> tuList, boolean isSVersion)
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
            tuPre = (TuImpl) tuList.get(tuList.size() - 1);
        }

        return tuPre;
    }

    private void setGSEditon(Request p_request, TuvImpl tuvPre, GxmlElement elem)
    {
        // for transmitting GS Edition segment comments.
        if (p_request.getEditionJobParams() != null)
        {
            try
            {
                if (elem.getAttribute("tuID") != null)
                {
                    long oldTuID = Long.parseLong(elem.getAttribute("tuID"));

                    HashMap editionParaMap = (HashMap) p_request
                            .getEditionJobParams().get("segComments");
                    HashMap issueMap = (HashMap) editionParaMap.get(oldTuID);

                    IssueEditionRelation ie = new IssueEditionRelation();
                    ie.setTuv(tuvPre);
                    ie.setOriginalTuId(oldTuID);

                    if (issueMap != null)
                    {
                        ie.setOriginalTuvId((Long) issueMap
                                .get("LevelObjectId"));

                        Vector historyVec = (Vector) issueMap.get("HistoryVec");
                        String originalIssueHistoryId = "";

                        for (int x = 0; x < historyVec.size(); x++)
                        {
                            HashMap history = (HashMap) historyVec.get(x);

                            if (x == historyVec.size() - 1)
                            {
                                originalIssueHistoryId = originalIssueHistoryId
                                        + "" + history.get("HistoryID");
                            }
                            else
                            {
                                originalIssueHistoryId = originalIssueHistoryId
                                        + history.get("HistoryID") + ",";
                            }
                        }

                        ie.setOriginalIssueHistoryId(originalIssueHistoryId);
                    }

                    Set ieSet = tuvPre.getIssueEditionRelation();

                    if (ieSet != null)
                    {
                        ieSet.add(ie);
                        tuvPre.setIssueEditionRelation(ieSet);
                    }
                    else
                    {
                        HashSet hs = new HashSet();
                        hs.add(ie);
                        tuvPre.setIssueEditionRelation(hs);
                    }
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
