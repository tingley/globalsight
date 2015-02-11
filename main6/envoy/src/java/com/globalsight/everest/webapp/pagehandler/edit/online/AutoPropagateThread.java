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

package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.util.comparator.TuvComparator;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.edit.SegmentUtil;
import com.globalsight.util.edit.SegmentUtil2;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.TextNode;

public class AutoPropagateThread implements Runnable
{
    private static final Logger logger = Logger
            .getLogger(AutoPropagateThread.class);

    private static final PageManager pageManager = ServerProxy.getPageManager();
    private static final TuvManager tuvManager = ServerProxy.getTuvManager();
    private static final LocaleManager localeManager = ServerProxy
            .getLocaleManager();
    // For worldServer XLF file only
    private int count = 0;
    private DiplomatAPI api = null;

    private String targetPageId;

    private String tuScope;

    private String specTus;

    private String tuvScope;

    public String getTargetPageId()
    {
        return targetPageId;
    }

    public void setTargetPageId(String targetPageId)
    {
        this.targetPageId = targetPageId;
    }

    public String getTuScope()
    {
        return tuScope;
    }

    public void setTuScope(String tuScope)
    {
        this.tuScope = tuScope;
    }

    public String getSpecTus()
    {
        return specTus;
    }

    public void setSpecTus(String specTus)
    {
        this.specTus = specTus;
    }

    public String getTuvScope()
    {
        return tuvScope;
    }

    public void setTuvScope(String tuvScope)
    {
        this.tuvScope = tuvScope;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public String getPickup()
    {
        return pickup;
    }

    public void setPickup(String pickup)
    {
        this.pickup = pickup;
    }

    private User user;

    private String pickup;

    // Indicate propagate progress
    private static int propagatePercentage = 0;

    public void run()
    {
        try
        {
            TargetPage tp = pageManager.getTargetPage(Long
                    .parseLong(targetPageId));
            SourcePage sp = tp.getSourcePage();
            String companyId = String.valueOf(sp.getCompanyId());
            GlobalSightLocale sourceLocale = localeManager.getLocaleById(sp
                    .getLocaleId());
            GlobalSightLocale targetLocale = localeManager.getLocaleById(tp
                    .getLocaleId());

            HashMap<Long, List<TuImpl>> repGroupsMap = new HashMap<Long, List<TuImpl>>();
            List<Tuv> targetTuvs = new ArrayList<Tuv>();
            if ("currentFile".equals(tuScope))
            {
                Long spId = sp.getIdAsLong();
                Collection<TuImpl> currentFileRepTus = tuvManager
                        .getRepTusBySourcePageId(spId);
                repGroupsMap = getRepGroups(currentFileRepTus);
                targetTuvs.addAll(tuvManager.getTargetTuvsForStatistics(tp));
            }
            else if ("allFiles".equals(tuScope))
            {
                List<TuImpl> allTus = new ArrayList<TuImpl>();
                Job job = sp.getRequest().getJob();
                Collection srcPages = job.getSourcePages();
                for (Iterator spIter = srcPages.iterator(); spIter.hasNext();)
                {
                    SourcePage sourcePage = (SourcePage) spIter.next();
                    Collection<TuImpl> repTus = tuvManager
                            .getRepTusBySourcePageId(sourcePage.getIdAsLong());
                    allTus.addAll(repTus);
                    for (Iterator it = sourcePage.getTargetPages().iterator(); it
                            .hasNext();)
                    {
                        TargetPage trgPage = (TargetPage) it.next();
                        if (targetLocale.getId() == trgPage.getLocaleId())
                        {
                            targetTuvs.addAll(tuvManager
                                    .getTargetTuvsForStatistics(trgPage));
                        }
                    }
                }
                repGroupsMap = getRepGroups(allTus);
            }
            else if ("specifiedTus".equals(tuScope))
            {
                String[] tuIds = specTus.split(",");
                repGroupsMap = getRepGroupsForSpecTuIds(companyId, tuIds,
                        tp.getLocaleId());

                Job job = sp.getRequest().getJob();
                Collection srcPages = job.getSourcePages();
                for (Iterator spIter = srcPages.iterator(); spIter.hasNext();)
                {
                    SourcePage sourcePage = (SourcePage) spIter.next();
                    for (Iterator it = sourcePage.getTargetPages().iterator(); it
                            .hasNext();)
                    {
                        TargetPage trgPage = (TargetPage) it.next();
                        if (targetLocale.getId() == trgPage.getLocaleId())
                        {
                            targetTuvs.addAll(tuvManager
                                    .getTargetTuvsForStatistics(trgPage));
                        }
                    }
                }
                
            }
            logger.info("Propagate repetition group size is: "
                    + repGroupsMap.size());

            // Cache the target TUVs in map in advance.
            HashMap<Long, Tuv> targetTuvsMap = new HashMap<Long, Tuv>();
            for (Tuv tuv : targetTuvs)
            {
                targetTuvsMap.put(tuv.getTu(companyId).getId(), tuv);
            }

            propagatePercentage = 0;// Initialize this to 0
            if (repGroupsMap.size() > 0)
            {
                int total = repGroupsMap.size();
                int repGroupCount = 0;
                for (Iterator iter = repGroupsMap.entrySet().iterator(); iter
                        .hasNext();)
                {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Long tuIdAsKey = (Long) entry.getKey();
                    @SuppressWarnings("unchecked")
                    List<TuImpl> loopGroup = (List<TuImpl>) entry.getValue();
                    if (loopGroup.size() > 1)
                    {
                        // Get applying TUV
                        Tuv applyingTuv = getApplyingTuv(tuScope, tuIdAsKey,
                                loopGroup, pickup, targetLocale, targetTuvsMap,
                                companyId);
                        // Decide flags
                        boolean isTagOrderChanged = false;
                        boolean hasSubSegments = false;
                        if (applyingTuv != null)
                        {
                            Tuv srcTuvOfApplyingTuv = applyingTuv.getTu(
                                    companyId).getTuv(sourceLocale.getId(),
                                    companyId);
                            isTagOrderChanged = isTargOrderChanged(srcTuvOfApplyingTuv,
                                    applyingTuv);
                            hasSubSegments = applyingTuv
                                    .getSubflowsAsGxmlElements() != null
                                    && applyingTuv.getSubflowsAsGxmlElements()
                                            .size() > 0;
                        }
                        
                        // ignore order change for html
                        boolean oriIsOrderChanged = isTagOrderChanged;
                        if (isTagOrderChanged && 
                                "html".equalsIgnoreCase(applyingTuv.getTu(companyId).getDataType()))
                        {
                            isTagOrderChanged = false; 
                        }
                        
                        // If tag order is changed or has sub
                        // segments,not propagate.
                        if (applyingTuv != null && !isTagOrderChanged
                                && !hasSubSegments)
                        {
                            propagateToOthers(applyingTuv, loopGroup, tuvScope,
                                    sourceLocale, targetLocale, user,
                                    targetTuvsMap, companyId, oriIsOrderChanged);
                        }
                    }
                    repGroupCount++;
                    propagatePercentage = Math.round(repGroupCount * 100
                            / total);
                }
            }
            // Add this to ensure the progressBar will go to end 100.
            propagatePercentage = 100;
        }
        catch (Throwable e)
        {
            logger.error("Error when propgate repetitions : " + e.getMessage(),
                    e);
        }
    }

    private HashMap<Long, List<TuImpl>> getRepGroups(Collection<TuImpl> p_tus)
    {
        HashMap<Long, List<TuImpl>> result = new HashMap<Long, List<TuImpl>>();

        if (p_tus == null || p_tus.size() == 0)
        {
            return result;
        }

        for (Iterator tusIter = p_tus.iterator(); tusIter.hasNext();)
        {
            TuImpl tu = (TuImpl) tusIter.next();
            long repeatedTuId = 0;
            if (tu.isRepeated())
            {
                repeatedTuId = tu.getId();
            }
            else if (tu.getRepetitionOfId() > 0)
            {
                repeatedTuId = tu.getRepetitionOfId();
            }

            if (repeatedTuId > 0)
            {
                List<TuImpl> tuGroup = (List<TuImpl>) result.get(repeatedTuId);
                if (tuGroup != null)
                {
                    tuGroup.add(tu);
                }
                else
                {
                    List<TuImpl> newGroup = new ArrayList<TuImpl>();
                    newGroup.add(tu);
                    result.put(repeatedTuId, newGroup);
                }
            }
        }

        return result;
    }

    /**
     * Get the repetition TU groups in map<SpecifiedTuID, List<TuImpl>>.
     * 
     * If one specified TU "has" the target TUV specified by target locale, and
     * this target TUV "has been" localized, it will be applied to others.
     */
    private HashMap<Long, List<TuImpl>> getRepGroupsForSpecTuIds(
            String companyId, String[] p_tuIds, long p_targetLocaleId)
            throws TuvException, RemoteException
    {
        HashMap<Long, List<TuImpl>> result = new HashMap<Long, List<TuImpl>>();
        if (p_tuIds == null || p_tuIds.length == 0)
        {
            return result;
        }
        // Filter to get "valid" TUs.
        List<Tu> repTus = new ArrayList<Tu>();
        for (int i = 0; i < p_tuIds.length; i++)
        {
            long tuId = Long.parseLong(p_tuIds[i]);
            Tu tu = tuvManager.getTuForSegmentEditor(tuId, companyId);
            Tuv targetTuv = tu.getTuv(p_targetLocaleId, companyId);
            if ((tu.isRepeated() || tu.getRepetitionOfId() > 0)
                    && targetTuv != null && this.isLocalized(targetTuv))
            {
                repTus.add(tu);
            }
        }
        //
        for (Iterator iter = repTus.iterator(); iter.hasNext();)
        {
            Tu tu = (Tu) iter.next();
            List<TuImpl> currentGroup = tuvManager.getRepTusByTuId(tu.getId(),
                    companyId);
            if (currentGroup != null && currentGroup.size() > 1)
            {
                List<TuImpl> existedGroup = (List<TuImpl>) result.get(tu
                        .getIdAsLong());
                if (existedGroup == null)
                {
                    result.put(tu.getIdAsLong(), currentGroup);
                }
            }
        }

        return result;
    }

    /**
     * Determine the Tuv to be populated into other target Tuvs for tuScope:
     * "Propagate to All Job Files" and "Propagate in Current File".
     * 
     * @throws RemoteException
     * @throws TuvException
     */
    private Tuv getApplyingTuv(String p_tuScope, long p_tuIdAsKey,
            List<TuImpl> p_group, String p_pickup,
            GlobalSightLocale p_targetLocale, HashMap<Long, Tuv> targetTuvsMap,
            String companyId) throws TuvException, RemoteException
    {
        if (p_group == null || p_targetLocale == null)
        {
            return null;
        }

        Tuv applyingTuv = null;
        if ("specifiedTus".equals(p_tuScope))
        {
            applyingTuv = getApplyingTuvForSpecifiedTus(p_tuIdAsKey, p_group,
                    p_targetLocale, companyId);
        }
        else
        {
            applyingTuv = getApplyingTuvForOthers(p_group, p_pickup,
                    p_targetLocale, targetTuvsMap);
        }

        return applyingTuv;
    }

    /**
     * Determine the applying TUV when TU scope "Specified TUs" is selected.
     * 
     * @param p_tuIdAsKey
     *            - The specified TuID whose target TUV will be applying to
     *            others.
     * @param p_group
     *            - All Tus in one repetition group.
     * @param p_targetLocale
     *            - Target locales.
     * @return - Tuv that will be applied to the others in repetition group.
     * @throws RemoteException
     * @throws TuvException
     */
    private Tuv getApplyingTuvForSpecifiedTus(long p_tuIdAsKey,
            List<TuImpl> p_group, GlobalSightLocale p_targetLocale,
            String companyId) throws TuvException, RemoteException
    {
        Tuv applyingTuv = null;
        for (Iterator iter = p_group.iterator(); iter.hasNext();)
        {
            Tu tu = (Tu) iter.next();
            if (tu.getIdAsLong() == p_tuIdAsKey)
            {
                Tuv targetTuv = tuvManager.getTuvForSegmentEditor(tu.getId(),
                        p_targetLocale.getId(), companyId);
                if (targetTuv != null && this.isLocalized(targetTuv))
                {
                    applyingTuv = targetTuv;
                }
            }
        }

        return applyingTuv;
    }

    /**
     * Determine the Tuv to be populated into other target Tuvs for tuScope:
     * "Propagate to All Job Files" and "Propagate in Current File".
     * 
     * @param p_group
     *            - All Tus in one repetition group.
     * @param p_pickup
     *            - "Latest" or "Oldest".
     * @param p_targetLocale
     *            - Target locales.
     * @return - Tuv that will be applied to the others in repetition group.
     */
    private Tuv getApplyingTuvForOthers(List<TuImpl> p_group, String p_pickup,
            GlobalSightLocale p_targetLocale, HashMap<Long, Tuv> targetTuvsMap)
    {
        if (p_pickup == null || p_pickup.isEmpty())
        {
            p_pickup = "latest";
        }

        // Filter to get all "localized" Tuvs in the Tu group.
        List<Tuv> targetTuvs = new ArrayList<Tuv>();
        for (Iterator iter = p_group.iterator(); iter.hasNext();)
        {
            TuImpl tu = (TuImpl) iter.next();
            Tuv targetTuv = targetTuvsMap.get(tu.getId());
            if (targetTuv != null && this.isLocalized(targetTuv))
            {
                targetTuvs.add(targetTuv);
            }
        }
        if (targetTuvs.size() == 0)
        {
            return null;
        }

        // Sort target TUVs by last modified date.
        Collections.sort(targetTuvs, new TuvComparator(
                TuvComparator.LAST_MODIFIED, Locale.US));

        // Get applying Tuv
        Tuv applyingTuv = null;
        if ("oldest".equals(p_pickup))
        {
            applyingTuv = targetTuvs.get(0);
        }
        else
        {
            applyingTuv = targetTuvs.get(targetTuvs.size() - 1);
        }

        return applyingTuv;
    }

    /**
     * Propagate the applying Tuv to the others.
     * 
     * @throws Exception
     */
    private void propagateToOthers(Tuv p_applyingTuv, List<TuImpl> p_group,
            String p_tuvScope, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale, User p_user,
            HashMap<Long, Tuv> p_targetTuvsMap, String companyId, boolean isOrderChanged)
            throws Exception
    {
        String gxml = p_applyingTuv.getGxml();
        List<TuvImpl> tuvs = new ArrayList<TuvImpl>();

        for (TuImpl tu : p_group)
        {
            Tuv sourceTuv = tu.getTuv(p_sourceLocale.getId(), companyId);
            Tuv targetTuv = p_targetTuvsMap.get(tu.getId());

            // If need update target TUV.
            boolean updateTargetTuv = false;
            if (targetTuv.getId() != p_applyingTuv.getId())
            {
                updateTargetTuv = needUpdateTargetTuv(p_tuvScope, targetTuv);
            }

            // Update target TUV.
            if (updateTargetTuv)
            {
                if (isWorldServerXliff(tu))
                {
                    gxml = adjustOriginalSegmentAttributeValues(p_applyingTuv,
                            sourceTuv);
                }
                // Handle "id", "x","i" attributes sequence etc.
                if ("xlf".equalsIgnoreCase(tu.getDataType())
                        || "html".equalsIgnoreCase(tu.getDataType()))
                {
                    gxml = adjustSegmentAttributeValues(sourceTuv, gxml,
                            companyId);
                }

                boolean canBeModified = SegmentUtil2.canBeModified(targetTuv,
                        gxml, companyId);
                
                // some tag are missing, but they are repetitions, if the 
                // first repetition can be modified, then all can
                if (isOrderChanged && "html".equalsIgnoreCase(tu.getDataType()))
                {
                    canBeModified = true;
                }
                
                if (canBeModified)
                {
                    gxml = getTargetGxmlFitForItsOwnSourceContent(sourceTuv,
                            gxml, companyId);
                    TuvImpl changedTargetTuv = modifyTargetTuv(targetTuv, gxml,
                            p_user);
                    tuvs.add(changedTargetTuv);
                }
            }
        }

        if (tuvs.size() > 0)
        {
            SegmentTuvUtil.updateTuvs(tuvs, Long.parseLong(companyId));
        }
    }

    /**
     * Judge if this TUV is localized.
     */
    private boolean isLocalized(Tuv p_targetTuv)
    {
        boolean isLocalized = false;
        if (p_targetTuv == null)
        {
            return false;
        }
        TuvState tuvState = p_targetTuv.getState();
        String state = "";
        if (tuvState != null)
        {
            state = tuvState.getName();
        }
        if (TuvState.LOCALIZED.getName().equals(state)
                || TuvState.EXACT_MATCH_LOCALIZED.getName().equals(state)
                || TuvState.ALIGNMENT_LOCALIZED.getName().equals(state)
                || TuvState.UNVERIFIED_EXACT_MATCH.getName().equals(state)
                || TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED.getName()
                        .equals(state))
        {
            isLocalized = true;
        }

        return isLocalized;
    }

    /**
     * For IWS XLF/XLZ source files, the segments IDs starts with 1 in one whole
     * file.When Auto-Propagate,should ensure the IDs are still in sequence.
     */
    private String adjustOriginalSegmentAttributeValues(Tuv p_applyingTuv,
            Tuv p_sourceTuv)
    {
        // Get the "id" list from original source content
        List<String> idListFromSourceTuv = new ArrayList<String>();
        String sourceGxml = p_sourceTuv.getGxml();
        String sourceLocaleCode = p_sourceTuv.getGlobalSightLocale()
                .getLocaleCode();
        String unwrappedSrcContent = SegmentUtil.restoreSegment(sourceGxml,
                sourceLocaleCode);
        StringBuffer sb = new StringBuffer();
        sb.append("<segment>").append(unwrappedSrcContent).append("</segment>");
        GxmlElement sourceGxmlElement = SegmentUtil2.getGxmlElement(sb
                .toString());
        idListFromSourceTuv = SegmentUtil2.getAttValuesByName(
                sourceGxmlElement, "id");

        // Get GxmlElement of original applying target content.
        String applyingGxml = p_applyingTuv.getGxml();
        int index = applyingGxml.indexOf(">");
        String startSegment = applyingGxml.substring(0, index + 1);
        String applyingLocaleCode = p_applyingTuv.getGlobalSightLocale()
                .getLocaleCode();
        String unwrappedApplyingTrgContent = SegmentUtil.restoreSegment(
                applyingGxml, applyingLocaleCode);
        StringBuffer sb2 = new StringBuffer();
        sb2.append("<segment>").append(unwrappedApplyingTrgContent)
                .append("</segment>");
        GxmlElement applyingGxmlElement = SegmentUtil2.getGxmlElement(sb2
                .toString());

        // Replace the "id" values with those from original source TUV.
        this.count = 0;
        resetAttributeValues(applyingGxmlElement, idListFromSourceTuv, "id");

        // Re-wrap the replaced Gxml.
        String result = applyingGxmlElement.toGxml("xlf");
        result = GxmlUtil.stripRootTag(result);
        if (this.api == null)
        {
            api = new DiplomatAPI();
        }
        else
        {
            api.reset();
        }
        SegmentNode sn = SegmentUtil2.extractSegment(api, result, "xlf",
                p_sourceTuv.getGlobalSightLocale());
        if (sn != null)
        {
            result = sn.getSegment();
            result = encodeTranslationResult(result);
            result = startSegment + result + "</segment>";
        }

        return result;
    }

    private String adjustSegmentAttributeValues(Tuv p_sourceTuv,
            String p_targetGxml, String companyId)
    {
        // Get values in list for "id" and "x" attributes
        List<String> idList = new ArrayList<String>();
        List<String> xList = new ArrayList<String>();
        List<String> iList = new ArrayList<String>();
        GxmlElement sourceGxmlElement = p_sourceTuv.getGxmlElement();
        idList = SegmentUtil2.getAttValuesByName(sourceGxmlElement, "id");
        xList = SegmentUtil2.getAttValuesByName(sourceGxmlElement, "x");
        iList = SegmentUtil2.getAttValuesByName(sourceGxmlElement, "i");

        GxmlElement targetGxmlElement = SegmentUtil2
                .getGxmlElement(p_targetGxml);
        this.count = 0;
        resetAttributeValues(targetGxmlElement, idList, "id");
        this.count = 0;
        resetAttributeValues(targetGxmlElement, xList, "x");
        this.count = 0;
        resetAttributeValues(targetGxmlElement, iList, "i");

        String dataType = p_sourceTuv.getDataType(companyId);
        String result = targetGxmlElement.toGxml(dataType);

        return result;
    }

    private void resetAttributeValues(GxmlElement element,
            List<String> attValueList, String p_attName)
    {
        if (element == null)
        {
            return;
        }

        String currentAttValue = element.getAttribute(p_attName);
        if (currentAttValue != null)
        {
            // Set new attribute value
            String newAttValue = null;
            if (attValueList != null && attValueList.size() > count)
            {
                newAttValue = (String) attValueList.get(count);
            }
            else
            {
                newAttValue = String.valueOf(count + 1);
            }
            element.setAttribute(p_attName, newAttValue);
            // Set new text node value
            if (element.getChildElements() != null)
            {
                Iterator it = element.getChildElements().iterator();
                while (it.hasNext())
                {
                    GxmlElement ele = (GxmlElement) it.next();
                    if (ele.getType() == GxmlElement.TEXT_NODE)
                    {
                        TextNode textNode = (TextNode) ele;
                        String nodeValue = textNode.getTextNodeValue();
                        String currentTextNodeValue = "{" + currentAttValue
                                + "}";
                        if (nodeValue.equals(currentTextNodeValue))
                        {
                            StringBuffer newNodeValue = new StringBuffer();
                            newNodeValue.append("{").append(newAttValue)
                                    .append("}");
                            textNode.setTextBuffer(newNodeValue);
                        }
                    }
                }
            }
            
            this.count++;
        }
        // add missing first X, X=1 
        else if ("bpt".equals(element.getName()) && attValueList != null 
                && attValueList.size() > count && "x".equals(p_attName))
        {
            String newAttValue = (String) attValueList.get(count);
            element.setAttribute(p_attName, newAttValue);
            this.count++;
        }

        // Loop deeper
        if (element.getChildElements() != null)
        {
            Iterator childIt = element.getChildElements().iterator();
            while (childIt.hasNext())
            {
                GxmlElement ele = (GxmlElement) childIt.next();
                resetAttributeValues(ele, attValueList, p_attName);
            }
        }
    }

    /**
     * Keep same with original source TUV content.
     * 
     * @param p_segString
     * @return
     */
    private String encodeTranslationResult(String p_segString)
    {
        String result = null;
        try
        {
            StringBuffer sb = new StringBuffer();
            sb.append("<segment>").append(p_segString).append("</segment>");
            GxmlElement gxmlElement = SegmentUtil2
                    .getGxmlElement(sb.toString());

            String gxml = gxmlElement.toGxml("xlf");
            result = GxmlUtil.stripRootTag(gxml);
        }
        catch (Exception e)
        {
            result = p_segString;
        }

        return result;
    }

    private boolean isWorldServerXliff(Tu p_tu)
    {
        if (TuImpl.FROM_WORLDSERVER.equalsIgnoreCase(p_tu.getGenerateFrom()))
        {
            return true;
        }

        return false;
    }

    /**
     * Determine if need update target TUV content by user options.
     */
    private boolean needUpdateTargetTuv(String p_tuvScope, Tuv p_targetTuv)
    {
        boolean updateTargetTuv = false;

        if ("all".equals(p_tuvScope))
        {
            updateTargetTuv = true;
        }
        else if ("localizedOnly".equals(p_tuvScope)
                && this.isLocalized(p_targetTuv))
        {
            updateTargetTuv = true;
        }
        else if ("unlocalizedOnly".equals(p_tuvScope)
                && !this.isLocalized(p_targetTuv))
        {
            updateTargetTuv = true;
        }

        return updateTargetTuv;
    }

    /**
     * Return a GXML which includes the content from "p_gxml" and the tags info
     * from "p_sourceTuv".
     * 
     * @param p_tuv
     * @param p_gxml
     * @return
     * @throws Exception
     */
    private String getTargetGxmlFitForItsOwnSourceContent(Tuv p_sourceTuv,
            String p_gxml, String companyId) throws Exception
    {
        String srcGxml = p_sourceTuv.getGxml();
        int index = srcGxml.indexOf(">");
        String startSegment = srcGxml.substring(0, index + 1);

        OnlineTagHelper sourceTagHelper = new OnlineTagHelper();
        sourceTagHelper.setInputSegment(p_sourceTuv.getGxmlExcludeTopTags(),
                "", p_sourceTuv.getDataType(companyId));
        sourceTagHelper.getCompact();// This step is required

        OnlineTagHelper targetTagHelper = new OnlineTagHelper();
        targetTagHelper.setInputSegment(GxmlUtil.stripRootTag(p_gxml), "",
                p_sourceTuv.getDataType(companyId));
        String compact = targetTagHelper.getCompact();
        // Combine source tag info and target content info
        String targetGxml = sourceTagHelper.getTargetDiplomat(compact);
        StringBuffer sb = new StringBuffer();
        sb.append(startSegment).append(targetGxml).append("</segment>");

        return sb.toString();
    }

    private TuvImpl modifyTargetTuv(Tuv p_targetTuv, String p_targetGxml,
            User p_user)
    {
        TuvImpl targetTuv2 = (TuvImpl) p_targetTuv;
        targetTuv2.setGxml(p_targetGxml);
        targetTuv2.setState(TuvState.LOCALIZED);
        targetTuv2.setLastModified(new Date());
        targetTuv2.setLastModifiedUser(p_user.getUserId());
        targetTuv2.setExactMatchKey(GlobalSightCrc.calculate(targetTuv2
                .getExactMatchFormat()));

        return targetTuv2;
    }

    /**
     * Check if the tag order in target TUV is changed or not comparing with
     * source TUV.
     */
    private boolean isTargOrderChanged(Tuv sourceTuv, Tuv targetTuv)
    {
        boolean isTagOrderChanged = false;

        List<String> xList1 = new ArrayList<String>();
        GxmlElement sourceGxmlElement = sourceTuv.getGxmlElement();
        xList1 = getAttValuesByName(xList1, sourceGxmlElement, "x");

        List<String> xList2 = new ArrayList<String>();
        GxmlElement targetGxmlElement = targetTuv.getGxmlElement();
        xList2 = getAttValuesByName(xList2, targetGxmlElement, "x");

        int size = Math.min(xList1.size(), xList2.size());
        if (size > 0)
        {
            for (int i = 0; i < size; i++)
            {
                String xValue1 = (String) xList1.get(i);
                String xValue2 = (String) xList2.get(i);
                if (!xValue1.equals(xValue2))
                {
                    isTagOrderChanged = true;
                    break;
                }
            }
        }

        return isTagOrderChanged;
    }
    
    /**
     * Get specified attribute's values in List from GxmlElement.
     */
    private List<String> getAttValuesByName(List<String> list, GxmlElement element,
            String attName)
    {
        if (element != null)
        {
            String value = element.getAttribute(attName);
            if (value != null)
            {
                list.add(value);
            }
            // ignore missing first X, X=1 
            else if ("bpt".equals(element.getName()) && list.isEmpty())
            {
                list.add("1");
            }
            
            if (element.getChildElements() != null)
            {
                Iterator childIt = element.getChildElements().iterator();
                while (childIt != null && childIt.hasNext())
                {
                    GxmlElement ele = (GxmlElement) childIt.next();
                    getAttValuesByName(list, ele, attName);
                }
            }
        }

        return list;
    }

    public int getPropagatePercentage()
    {
        return propagatePercentage;
    }
}
