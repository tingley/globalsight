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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.globalsight.everest.edit.offline.xliff.ListViewWorkXLIFFWriter;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.LeverageMatchLingManagerLocal;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.ling.tm2.leverage.Leverager;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;

/**
 * <p>
 * The PageTemplate represents an outline of the page containing
 * non-translation-unit contextual information mixed with references to
 * translation units.
 * </p>
 * 
 * <p>
 * There are types of PageTemplates, one for each process's purpose.
 * </p>
 */
public class PageTemplate extends PersistentObject
{
    private static final long serialVersionUID = 3462604430091153330L;

    static private final Logger c_logger = Logger
            .getLogger(PageTemplate.class);

    static public final int TYPE_EXPORT = 1;
    static public final int TYPE_STANDARD = 2;
    static public final int TYPE_DETAIL = 3;
    static public final int TYPE_OFFLINE = TYPE_DETAIL;
    static public final int TYPE_PREVIEW = 4;

    static public final String byUser = "LocalizedByUser";
    static public final String byMT = "LocalizedByMT";
    static public final String byLocalTM = "LocalizedByLocalTM";

    private final static String REG_INTERNAL_BPT = "<bpt[^>]*?internal=\"yes\"[^>]*?i=\"(\\d*?)\"[^>]*?/>";
    private final static String REG_INTERNAL_BPT_EPT = "<bpt[^>]*?internal=\"yes\"[^>]*?i=\"%n%\"[^>]*?/>(.*?)<ept[^>]*?i=\"%n%\"[^>]*?/>";
    private final static String REG_SEGMENT = "<segment[^>]*?>(.*?)</segment>";

    // Need to have a map that contains the corresponding string value of
    // the page template type (the value stored in db)
    static private Map s_typeMap = new HashMap(4);
    static
    {
        s_typeMap.put(new Integer(TYPE_EXPORT), "EXP");
        s_typeMap.put(new Integer(TYPE_STANDARD), "STD");
        s_typeMap.put(new Integer(TYPE_DETAIL), "DTL");
        s_typeMap.put(new Integer(TYPE_PREVIEW), "PRV");
    }

    private int m_type;

    private long target_locale_id;

    // template parts - this relationship is NOT mapped.
    // must use PageManager.getTemplatePartsForSourcePage() and the
    // set them within PageTemplate
    protected List m_templateParts = new ArrayList();

    /** HashMap of TuId (Key)and Tuv content (value) */
    protected HashMap m_tuvContents = null;

    private SourcePage m_sourcePage = null;

    private HashMap<String, String> mapOfSheetTabs = new HashMap<String, String>();
    private boolean isTabsTrip = false;

    public boolean isTabsTrip()
    {
        return isTabsTrip;
    }

    public void setTabsTrip(boolean isTabsTrip)
    {
        this.isTabsTrip = isTabsTrip;
    }

    public HashMap<String, String> getMapOfSheetTabs()
    {
        return mapOfSheetTabs;
    }

    public void setMapOfSheetTabs(HashMap<String, String> mapOfSheetTabs)
    {
        this.mapOfSheetTabs = mapOfSheetTabs;
    }

    public PageTemplate()
    {
    }

    public PageTemplate(int p_type)
    {
        m_type = p_type;
    }

    /**
     * Copy constructor used for derived class SnippetPageTemplate.
     */
    public PageTemplate(PageTemplate p_other)
    {
        // m_id is not maintained
        m_type = p_other.m_type;
        m_templateParts = p_other.m_templateParts;
        m_tuvContents = p_other.m_tuvContents;
        m_sourcePage = p_other.m_sourcePage;
    }

    /**
     * Set the template parts of this PageTemplate. Note that this method is
     * only used during import for persisting template parts of this page
     * template.
     * 
     * @param p_templateParts
     *            - The templateParts to be set.
     */
    public void setTemplatePartsForPersistence(ArrayList p_templateParts)
    {
        if (m_templateParts.size() == 0)
        {
            m_templateParts = p_templateParts;
        }
    }

    /**
     * Set the template parts of this PageTemplate. Note that a template part
     * should be set (by calling getTemplatePartsForSourcePage method of
     * PageManager before calling getPageData() method.
     * 
     * @param p_templateParts
     *            - The templateParts to be set.
     */
    public void setTemplateParts(List p_templateParts)
    {
        m_templateParts = p_templateParts;
    }

    public List getTemplateParts()
    {
        return m_templateParts;
    }

    /**
     * Update the template part with the same order with the one passed in. The
     * content has probably changed.
     */
    public void updateTemplatePart(TemplatePart p_part)
    {
        // Replace the template part in the array list that has the
        // same order.
        for (int i = 0; i < m_templateParts.size(); i++)
        {
            TemplatePart part = (TemplatePart) m_templateParts.get(i);

            if (part.getOrder() == p_part.getOrder())
            {
                m_templateParts.set(i, p_part);
                return;
            }
        }
    }

    /**
     * Get the PageTemplate type.
     * 
     * @return template type.
     */
    public long getType()
    {
        return m_type;
    }

    /**
     * Get the string representation of the page template type.
     * 
     * @return The page template type as stored in database.
     */
    public String getTypeAsString()
    {
        return getTypeAsString(m_type);
    }

    /**
     * Get the string representation of the type passed in.
     * 
     * @return The type as a string or null if not valid.
     */
    static public String getTypeAsString(int p_type)
    {
        return (String) s_typeMap.get(new Integer(p_type));
    }

    /**
     * Inserts the content of a TUV into m_TuvContent List. It is an ordered
     * list containing all the TUV contents for a page in the same order as they
     * appear in the page.
     * 
     * @param p_tuvContent
     *            the tuv content to be inserted.
     */
    public void insertTuvContent(Long p_tuId, String p_tuvContent)
    {
        if (m_tuvContents == null)
        {
            m_tuvContents = new HashMap();
        }

        m_tuvContents.put(p_tuId, p_tuvContent);
    }

    /**
     * For re-using this template, allow clearing the tuv content.
     */
    public void clearTuvContent()
    {
        m_tuvContents = null;
    }

    /**
     * Return the page as a string with all the translated text filled in.
     */
    public String getPageData(RenderingOptions p_options) throws PageException
    {
        // don't need to pass the rendering options on for
        // a simple page template
        return this.getPageData();
    }

    /**
     * Get the string representation of page template with TU ID data replaced
     * by Tuv content.
     * 
     * @return The template of the page as a string.
     * 
     * @deprecated This one is for my special friend ExportMDB and you should
     *             not use it.
     */
    public String getPageData() throws PageException
    {
        // if no skeleton in the page
        if (m_templateParts == null)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_INVALID_PARTS,
                    null, null);
        }

        int partsSize = m_templateParts.size();
        // If no TUVs are filled in and there is more than one
        // template part raise an error.
        if (m_tuvContents == null && partsSize > 1)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_TUVS_NOT_FILLED,
                    null, null);
        }

        StringBuffer result = new StringBuffer(partsSize * 100);
        TemplatePart part;
        int i = 0;
        String altStr = new String();

        for (Iterator it = m_templateParts.iterator(); it.hasNext();)
        {
            part = (TemplatePart) it.next();

            // append the skeleton part
            if (part.getSkeleton() != null)
            {
                if (c_logger.isDebugEnabled())
                {
                    System.err.println("ske part: " + part.getSkeleton());
                }

                String tempStr = part.getSkeleton();

                // only export can set the target_locale_id value
                if (target_locale_id != 0)
                {
                    String trgEnd = "&lt;/target&gt;";

                    // if the job creating file is from worldserver, then add
                    // the
                    // leverage match results into the alt-trans parts
                    if (tempStr.indexOf(trgEnd) > -1 && !altStr.isEmpty())
                    {
                        int index1 = tempStr.indexOf(trgEnd) + trgEnd.length();
                        String str0 = tempStr.substring(0, index1);
                        String str1 = tempStr.substring(index1,
                                tempStr.length());
                        tempStr = str0 + EditUtil.encodeXmlEntities(altStr)
                                + str1;
                        altStr = new String();
                    }

                    if (part.getTuId() > 0)
                    {
                        Tuv tuv = part.getTuv(target_locale_id);
                        Tu tu = tuv.getTu();
                        String trasStr = "<"
                                + DiplomatNames.Element.TRANSLATABLE;

                        boolean isLocalized = isTuvLocalized(tuv);
                        boolean isComplete = (tuv.getState().getValue() == TuvState.COMPLETE
                                .getValue());
                        // If the tuv state is "localized" or
                        // "exact_match_localized"", add an attribute "isLocalized=yes"
                        // to the translatable element, else add attribute
                        // "isLocalized = no"
                        if (isLocalized || isComplete)
                        {
                            boolean wfFinished = isWfFinished();
                            if (tu.getGenerateFrom() != null
                                    && tu.getGenerateFrom().equals(
                                            TuImpl.FROM_WORLDSERVER))
                            {
                                String lastModUser = tuv.getLastModifiedUser();
                                String sourceContent = tu.getSourceContent();

                                if (lastModUser != null
                                        && lastModUser.indexOf("_MT") > -1)
                                {
                                    // For GBS-1864 by York on 2011-03-07
                                    // Avoid to change "iws:segment-metadata"
                                    // content for "repetition" segments.
                                    if (Extractor.IWS_REPETITION
                                            .equalsIgnoreCase(sourceContent)
                                            && !wfFinished)
                                    {
                                        tempStr = tempStr
                                                .replace(
                                                        trasStr,
                                                        trasStr
                                                                + " "
                                                                + DiplomatNames.Attribute.ISLOCALIZED
                                                                + "=\"no\" ");
                                    }
                                    else
                                    {
                                        // Default behavior
                                        tempStr = tempStr
                                                .replace(
                                                        trasStr,
                                                        trasStr
                                                                + " "
                                                                + DiplomatNames.Attribute.ISLOCALIZED
                                                                + "=\"" + byMT
                                                                + "\" ");
                                    }
                                }
                                else if (lastModUser != null
                                        && !lastModUser
                                                .equals(IFormatNames.FORMAT_TDA)
                                        && !lastModUser
                                                .equals(IFormatNames.FORMAT_XLIFF_NAME)
                                        && !lastModUser
                                                .equals(IFormatNames.FORMAT_PO))
                                {
                                    tempStr = tempStr
                                            .replace(
                                                    trasStr,
                                                    trasStr
                                                            + " "
                                                            + DiplomatNames.Attribute.ISLOCALIZED
                                                            + "=\"" + byUser
                                                            + "\" ");
                                }
                                else if (lastModUser == null)
                                {
                                    tempStr = tempStr
                                            .replace(
                                                    trasStr,
                                                    trasStr
                                                            + " "
                                                            + DiplomatNames.Attribute.ISLOCALIZED
                                                            + "=\"" + byLocalTM
                                                            + "\" ");
                                }
                            }
                        }
                        else
                        {
                            tempStr = tempStr.replace(trasStr, trasStr + " "
                                    + DiplomatNames.Attribute.ISLOCALIZED
                                    + "=\"no\" ");
                        }
                    }
                }

                result.append(tempStr);
            }

            // if there is a TU in this template part, append it too.
            if (part.getTuId() > 0)
            {
                // If the TU has not been set, this will output "null"
                // into the result string. Caller needs to make sure
                // all TUs have been set to values.
                String tuvString = (String) m_tuvContents.get(part
                        .getTuIdAsLong());
                Tu tu = part.getTu();
                Tuv targetTuv = part.getTuv(target_locale_id);
                Tuv sourceTuv = part.getTuv(m_sourcePage.getLocaleId());

                // For XLF file format, revert original target content in some
                // cases.
                if (tu.getDataType().equals(IFormatNames.FORMAT_XLIFF)
                        && tu instanceof TuImpl)
                {
                    // For GBS-1864 by York on 2011-03-07
                    boolean revertTrgCase1 = sourceTuv.getGxml().equals(
                            targetTuv.getGxml())
                            && targetTuv.getState().getValue() == TuvState.NOT_LOCALIZED
                                    .getValue();

                    boolean wfFinished = isWfFinished();
                    String sourceContent = tu.getSourceContent();
                    boolean isMtlastModified = (targetTuv.getLastModifiedUser() != null && targetTuv
                            .getLastModifiedUser().indexOf("_MT") > -1);
                    boolean revertTrgCase2 = (!wfFinished
                            && Extractor.IWS_REPETITION
                                    .equalsIgnoreCase(sourceContent) && isMtlastModified);
                    if (revertTrgCase1 || revertTrgCase2)
                    {
                        tuvString = tu.getXliffTarget();

                        // If the target in source xliff file is empty like
                        // "<target/>", when import, an extra space is added in
                        // TU "<segment segmentId="1"> </segment>". This has
                        // special purpose. When export,remove this extra space.
                        if (tuvString != null)
                        {
                            String tmpTuvString = GxmlUtil
                                    .stripRootTag(tuvString);
                            if (tmpTuvString != null
                                    && " ".equals(tmpTuvString))
                            {
                                tuvString = GxmlUtil.resetInnerText(tuvString,
                                        "");
                            }
                        }
                    }
                }
                // For PO file format, revert original target content in some
                // cases.
                else if (IFormatNames.FORMAT_PO.equals(tu.getDataType())
                        && tu instanceof TuImpl)
                {
                    boolean revertTrgCase1 = sourceTuv.getGxml().equals(
                            targetTuv.getGxml())
                            && targetTuv.getState().getValue() == TuvState.NOT_LOCALIZED
                                    .getValue();
                    if (revertTrgCase1)
                    {
                        tuvString = tu.getXliffTarget();
                    }
                }

                // if the job creating file is from worldserver, then add the
                // leverage match results into the alt-trans parts
                if (tu.getGenerateFrom() != null
                        && tu.getGenerateFrom().equals(TuImpl.FROM_WORLDSERVER))
                {
                    LeverageMatchLingManagerLocal lmm = new LeverageMatchLingManagerLocal();
                    SortedSet lms = lmm.getTuvMatches(sourceTuv.getIdAsLong(),
                            targetTuv.getLocaleId(), "0", false);

                    List list = new ArrayList(lms);
                    altStr = getAltTransOfMatch(list);
                }

                if (c_logger.isDebugEnabled())
                {
                    System.err.println("tuv part: " + tuvString);
                }
                if (isTabsTrip)
                {
                    String sheetName = buildSheetName(i + 1);
                    if (mapOfSheetTabs.get(sheetName) == null)
                    {
                        if (result.indexOf(sheetName) != -1)
                        {
                            i++;
                            mapOfSheetTabs.put(sheetName,
                                    getSheetValue(tuvString));
                        }
                    }
                }

                result.append(tuvString);
            }
        }

        return result.toString();
    }

    /**
     * Gets the translated sheet name.
     */
    private String getSheetValue(String segment)
    {
        String newSegment = segment;
        Pattern p = Pattern.compile(REG_INTERNAL_BPT);
        Matcher m = p.matcher(segment);
        while (m.find())
        {
            String i = m.group(1);
            String regex = REG_INTERNAL_BPT_EPT.replace("%n%", i);
            Pattern p2 = Pattern.compile(regex);
            Matcher m2 = p2.matcher(segment);
            if (m2.find())
            {
                String matchedSegment = m2.group();
                String internalSegment = m2.group(1);
                newSegment = newSegment
                        .replace(matchedSegment, internalSegment);
                m = p.matcher(newSegment);
            }
            else
            {
                return null;
            }
        }
        Pattern p3 = Pattern.compile(REG_SEGMENT);
        Matcher m3 = p3.matcher(newSegment);
        if (m3.find())
        {
            return m3.group(1);
        }

        return null;
    }

    /**
     * Judge if the workflow is finished for specified source page and target
     * locale ID.
     * 
     * @return boolean
     */
    private boolean isWfFinished()
    {
        boolean result = false;

        try
        {
            TargetPage wantedTP = null;
            Set trgPages = m_sourcePage.getTargetPages();
            Iterator trgPageIter = trgPages.iterator();
            while (trgPageIter.hasNext())
            {
                TargetPage tp = (TargetPage) trgPageIter.next();
                if (tp.getGlobalSightLocale().getId() == target_locale_id)
                {
                    wantedTP = tp;
                    break;
                }
            }

            if (wantedTP != null)
            {
                String wfState = wantedTP.getWorkflowInstance().getState();
                if (Workflow.LOCALIZED.equals(wfState)
                        || Workflow.EXPORTED.equals(wfState)
                        || Workflow.ARCHIVED.equals(wfState))
                {
                    result = true;
                }
            }
        }
        catch (Exception ex)
        {

        }

        return result;
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

    private String getAltTransOfMatch(List<LeverageMatch> list)
    {
        String altStr = new String();
        ListViewWorkXLIFFWriter lvwx = new ListViewWorkXLIFFWriter();

        if (list != null)
        {
            LeverageMatch.orderMatchResult(list);

            for (int i = 0; i < list.size(); i++)
            {
                LeverageMatch leverageMatch = list.get(i);

                if (judgeIfneedAdd(leverageMatch))
                {
                    altStr = altStr + lvwx.getAltByMatch(leverageMatch, null);
                }
            }
        }

        return altStr;
    }

    /*
     * Judge if need add the leverage match result into alt-trans when export
     * There are three condition need not add, because they are repeated with
     * the target content or the original alt-trans content. 1. Auto-commit:
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
     * Not write this into "alt-trans". 3. Target has NO valid content (empty or
     * placeholder-only) && come from xliff alt, not write this into
     * "alt-trans".
     */
    private boolean judgeIfneedAdd(LeverageMatch lm)
    {
        try
        {
            Tuv sourceTuv = ServerProxy.getTuvManager().getTuvForSegmentEditor(
                    lm.getOriginalSourceTuvId());
            Tuv targetTuv = sourceTuv.getTu().getTuv(lm.getTargetLocaleId());
            boolean isWSXlf = false;
            if (TuImpl.FROM_WORLDSERVER.equalsIgnoreCase(sourceTuv.getTu()
                    .getGenerateFrom()))
            {
                isWSXlf = true;
            }

            String targetContent = targetTuv.getGxml();
            String originalTarget = sourceTuv.getTu().getXliffTargetGxml()
                    .getTextValue();

            if (lm.getProjectTmIndex() == Leverager.MT_PRIORITY
                    && lm.getScoreNum() == 100
                    && (lm.getMatchedText().equals(targetContent) || isWSXlf))
            {
                // For GBS-1864 (if work-flow is in progress, and
                // source_content="repetition", MT translation
                // will NOT be written into "target", so need add MT translation
                // into "alt-trans".
                String sourceContentAtt = sourceTuv.getTu().getSourceContent();
                if (!isWfFinished()
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
                String transType = ((TuImpl) sourceTuv.getTu())
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

    /**
     * Get the string representation of page template with TU ID data replaced
     * by Tuv content.
     * 
     * @param p_tuIdList
     *            :Only parts whose tuIds are in this list are returned.
     * 
     * @return The template of the page as a string.
     * 
     */
    public String getPageData(List p_tuIdList) throws PageException
    {
        // if no skeleton in the page
        if (m_templateParts == null)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_INVALID_PARTS,
                    null, null);
        }

        int partsSize = m_templateParts.size();
        // If no TUVs are filled in and there is more than one
        // template part raise an error.
        if (m_tuvContents == null && partsSize > 1)
        {
            throw new PageException(
                    PageException.MSG_PAGETEMPLATE_GETPAGEDATA_TUVS_NOT_FILLED,
                    null, null);
        }

        StringBuffer result = new StringBuffer(partsSize * 100);
        TemplatePart part;
        int i = 0;
        for (Iterator it = m_templateParts.iterator(); it.hasNext();)
        {
            part = (TemplatePart) it.next();

            // if there is a TU in this template part, append it too.
            if (part.getTuId() > 0 && p_tuIdList.contains(part.getTuIdAsLong()))
            {
                // append the skeleton part
                if (part.getSkeleton() != null)
                {
                    if (c_logger.isDebugEnabled())
                    {
                        System.err.println("ske part: " + part.getSkeleton());
                    }

                    result.append(part.getSkeleton());
                }

                // If the TU has not been set, this will output "null"
                // into the result string. Caller needs to make sure
                // all TUs have been set to values.
                String tuv = (String) m_tuvContents.get(part.getTuIdAsLong());

                if (c_logger.isDebugEnabled())
                {
                    System.err.println("tuv part: " + tuv);
                }
                if (isTabsTrip)
                {
                    String sheetName = buildSheetName(i + 1);
                    if (mapOfSheetTabs.get(sheetName) == null)
                    {
                        if (result.indexOf(sheetName) != -1)
                        {
                            i++;
                            mapOfSheetTabs.put(sheetName, getSheetValue(tuv));
                        }
                    }
                }

                result.append(tuv);
            }
        }

        return result.toString();
    }

    private String buildSheetName(int i)
    {
        StringBuilder sb = new StringBuilder("sheet");
        if (i < 10)
        {
            sb.append("00").append(i);
        }
        else if (i < 100)
        {
            sb.append("0").append(i);
        }
        else
        {
            sb.append("i");
        }
        sb.append(".html");
        return sb.toString();
    }

    /**
     * Gets the Set of valid (interpreted) Tu ids for a given GS-tagged source
     * page. Overwritten in SnippetPageTemplate. Called by upload/download and
     * the online editor.
     */
    public HashSet getInterpretedTuIds() throws PageException
    {
        return null;
    }

    /**
     * <p>
     * Sets the source page object that this template belongs to.
     * </p>
     * 
     * @param p_sourcePage
     *            - The source page object that this template belongs to.
     */
    public void setSourcePage(SourcePage p_sourcePage)
    {
        m_sourcePage = p_sourcePage;
    }

    /**
     * Debug method to print this object
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append(" type=");
        sb.append(m_type);
        sb.append(" #parts=");
        sb.append(m_templateParts != null ? m_templateParts.size() : -1);
        sb.append(" source page id=");
        sb.append(m_sourcePage == null ? -1 : m_sourcePage.getId());
        sb.append(" m_tuvContents=");
        sb.append(m_tuvContents != null ? m_tuvContents.toString() : "null");

        return sb.toString();
    }

    public String getTypeValue()
    {
        return getTypeAsString(m_type);
    }

    public void setTypeValue(String typeValue)
    {
        Set keys = s_typeMap.keySet();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext())
        {
            Integer key = (Integer) iterator.next();
            String type = (String) s_typeMap.get(key);
            if (type.equalsIgnoreCase(typeValue))
            {
                this.m_type = key.intValue();
                break;
            }
        }
    }

    public SourcePage getSourcePage()
    {
        return m_sourcePage;
    }

    public void setTargetLocale(long p_localeId)
    {
        target_locale_id = p_localeId;
    }
}
