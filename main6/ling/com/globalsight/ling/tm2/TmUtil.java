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
package com.globalsight.ling.tm2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.TuvSegmentBaseHandler;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.tm.LingManagerException;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTu;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTuv;
import com.globalsight.ling.tm2.leverage.LeveragedTu;
import com.globalsight.ling.tm2.leverage.LeveragedTuv;
import com.globalsight.ling.tm2.leverage.SegmentIdMap;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.GxmlNames;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;

/**
 * Collection of utility methods.
 */
public class TmUtil
{
    private static Logger c_logger = Logger.getLogger(TmUtil.class.getName());

    public static final String X_NBSP = "x-nbspace";
    public static final String X_MSO_SPACERUN = "x-mso-spacerun";
    public static final String X_MSO_TAB = "x-mso-tab";

    /**
     * Create Tuvs suitable for storing in Segment TM. Tuvs in Segment TM don't
     * have native formatting codes and their subflows are separated out from
     * their main text.
     * 
     * This method takes a Tu and break up its Tuvs and create one or more Tus.
     * Each original Tuv should have the same number of subflows. If not, an
     * exception is thrown.
     * 
     * @param p_tu
     *            BaseTmTu object
     * @param p_sourceLocale
     *            source locale. Segment TM Tu needs to know the source locale
     * @return a Collection of SegmentTmTu objects that owns one or more
     *         SegmentTmTuv objects
     */
    static public Collection<SegmentTmTu> createSegmentTmTus(BaseTmTu p_tu,
            GlobalSightLocale p_sourceLocale) throws Exception
    {
        Map<String, SegmentTmTu> newTus = new HashMap<String, SegmentTmTu>();

        for (GlobalSightLocale locale : p_tu.getAllTuvLocales())
        {
            for (BaseTmTuv tuv : p_tu.getTuvList(locale))
            {
                Collection splitSegments = tuv.prepareForSegmentTm();

                // check the number of segments returned. The number
                // should be the same for all Tuvs.
                if (newTus.size() != 0 && newTus.size() != splitSegments.size())
                {
                    throw new LingManagerException("SegmentTmTuNumDifferent",
                            null, null);
                }

                Iterator itSplit = splitSegments.iterator();
                while (itSplit.hasNext())
                {
                    // With a stricter error checking, subId's
                    // discripancy should be checked here.

                    AbstractTmTuv.SegmentAttributes segAtt = (AbstractTmTuv.SegmentAttributes) itSplit
                            .next();
                    String subId = segAtt.getSubId();
                    SegmentTmTu segmentTmTu = (SegmentTmTu) newTus.get(subId);
                    if (segmentTmTu == null)
                    {
                        if (p_tu instanceof LeveragedTu)
                        {
                            segmentTmTu = new LeveragedSegmentTu(p_tu.getId(),
                                    p_tu.getTmId(), segAtt.getFormat(),
                                    segAtt.getType(), segAtt.isTranslatable(),
                                    p_sourceLocale);
                            ((LeveragedTu) segmentTmTu)
                                    .setMatchState(((LeveragedTu) p_tu)
                                            .getMatchState());
                            ((LeveragedTu) segmentTmTu)
                                    .setScore(((LeveragedTu) p_tu).getScore());
                            ((LeveragedTu) segmentTmTu)
                                    .setMatchTableType(((LeveragedTu) p_tu)
                                            .getMatchTableType());
                            ((LeveragedTu) segmentTmTu)
                                    .setSourceContent(((LeveragedTu) p_tu)
                                            .getSourceContent());
                        }
                        else
                        {
                            segmentTmTu = new SegmentTmTu(p_tu.getId(),
                                    p_tu.getTmId(), segAtt.getFormat(),
                                    segAtt.getType(), segAtt.isTranslatable(),
                                    p_sourceLocale);
                        }

                        segmentTmTu.setSubId(subId);

                        segmentTmTu.setSourceTmName(p_tu.getSourceTmName());
                        segmentTmTu
                                .setFromWorldServer(p_tu.isFromWorldServer());
                        segmentTmTu.setSourceContent(p_tu.getSourceContent());

                        if (p_tu instanceof SegmentTmTu)
                        {
                            segmentTmTu.setProps(((SegmentTmTu) p_tu)
                                    .getProps());
                            segmentTmTu.setSID(((SegmentTmTu) p_tu).getSID());
                        }

                        newTus.put(subId, segmentTmTu);
                    }

                    // normalize white space for non white space
                    // preserving format
                    String segmentString = segAtt.getText();
                    if (DiplomatAPI.isWsNonPreservingFormat(segmentTmTu
                            .getFormat()))
                    {
                        segmentString = Text
                                .normalizeWhiteSpaceForTm(segmentString);
                    }

                    SegmentTmTuv segmentTmTuv = null;
                    if (p_tu instanceof LeveragedTu)
                    {
                        segmentTmTuv = new LeveragedSegmentTuv(tuv.getId(),
                                segmentString, locale);
                        ((LeveragedTuv) segmentTmTuv)
                                .setMatchState(((LeveragedTuv) tuv)
                                        .getMatchState());
                        ((LeveragedTuv) segmentTmTuv)
                                .setScore(((LeveragedTuv) tuv).getScore());
                    }
                    else
                    {
                        segmentTmTuv = new SegmentTmTuv(tuv.getId(),
                                segmentString, locale);
                        if (tuv instanceof SegmentTmTuv)
                        {
                            SegmentTmTuv segmentTuv = (SegmentTmTuv) tuv;
                            segmentTmTuv.setOrgSegment(segmentTuv
                                    .getOrgSegment());
                        }
                    }

                    segmentTmTuv.setExactMatchKey();
                    segmentTmTuv.setCreationUser(tuv.getCreationUser());
                    segmentTmTuv.setCreationDate(tuv.getCreationDate());
                    segmentTmTuv.setModifyUser(tuv.getModifyUser());
                    segmentTmTuv.setModifyDate(tuv.getModifyDate());
                    segmentTmTuv.setWordCount(segAtt.getWordCount());
                    segmentTmTuv.setUpdatedProject(tuv.getUpdatedProject());
                    segmentTmTuv.setSid(tuv.getSid());
                    segmentTmTuv.setLastUsageDate(tuv.getLastUsageDate());
                    segmentTmTuv.setJobId(tuv.getJobId());
                    segmentTmTuv.setJobName(tuv.getJobName());
                    segmentTmTuv.setPreviousHash(tuv.getPreviousHash());
                    segmentTmTuv.setNextHash(tuv.getNextHash());

                    segmentTmTu.addTuv(segmentTmTuv);
                }
            }
        }
        return newTus.values();
    }

    /**
     * Compose a complete segment text form fragmented text (a main text and its
     * subflows) taking native formatting code from an original source segment
     * text
     * 
     * @param p_originalGxml
     *            original source segment text
     * @param p_separatedSegmentMap
     *            Map of fragmented text. Key is sub Id (String) and Value is
     *            segment text (String)
     */
    static public String composeCompleteText(String p_originalGxml,
            Map p_separatedSegmentMap) throws Exception
    {
        // create DOM of the original source segment
        GxmlElement sourceDom;
        GxmlFragmentReader reader = null;

        try
        {
            reader = GxmlFragmentReaderPool.instance().getGxmlFragmentReader();
            sourceDom = reader.parseFragment(p_originalGxml);
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
            reader = null;
        }

        // get the main text of the fragmented segment
        String mainText = (String) p_separatedSegmentMap.get(SegmentTmTu.ROOT);
        if (mainText == null)
        {
            mainText = p_originalGxml;
        }
        else
        {
            // inject formatting codes and subflows to the main text from
            // the source text
            mainText = injectCodes(mainText, sourceDom);
        }

        // substitute subflow contents (for now,only when all subIds have extact
        // matches, this methods will succeed)
        try
        {
            mainText = substituteSubflows(mainText, p_separatedSegmentMap);
        }
        catch (Exception e)
        {
            c_logger.warn(e);
        }

        return mainText;
    }

    /**
     * Build SegmentIdMap from a Collection of BaseTmTuv.
     * 
     * @param p_segments
     *            Collection of BaseTmTuv
     * @return SegmentIdMap
     */
    static public SegmentIdMap buildSegmentIdMap(Collection p_segments)
    {
        SegmentIdMap map = new SegmentIdMap(p_segments.size());
        Iterator itTuv = p_segments.iterator();
        while (itTuv.hasNext())
        {
            BaseTmTuv tuv = (BaseTmTuv) itTuv.next();
            long id = tuv.getId();
            String subId = null;
            if (tuv instanceof SegmentTmTuv)
            {
                subId = ((SegmentTmTu) tuv.getTu()).getSubId();
            }

            map.put(id, subId, tuv);
        }

        return map;
    }

    // inject formatting codes and subflows to the top level text of
    // fragmented segment from a GXML DOM created from an original
    // source segment text
    static private String injectCodes(String p_mainText, GxmlElement p_sourceDom)
            throws Exception
    {
        CodeInjectionHandler handler = new CodeInjectionHandler(p_sourceDom);
        DiplomatBasicParser diplomatParser = new DiplomatBasicParser(handler);

        diplomatParser.parse(p_mainText);
        return handler.toString();
    }

    // replace subflow contents in a main gxml text with fragmented
    // segments that are mapped with its sub id stored in a Map object
    static private String substituteSubflows(String p_mainText,
            Map p_separatedSegmentMap) throws Exception
    {
        SubflowSubstitutionHandler handler = new SubflowSubstitutionHandler(
                p_separatedSegmentMap);
        DiplomatBasicParser diplomatParser = new DiplomatBasicParser(handler);

        diplomatParser.parse(p_mainText);
        return handler.toString();
    }

    // handler to inject native formatting codes
    private static class CodeInjectionHandler extends TuvSegmentBaseHandler
    {
        private GxmlElement m_sourceDom;
        private StringBuffer m_content = new StringBuffer(200);
        private Map m_iAttrEptMap = new HashMap();
        private Map m_bptPos = new HashMap();
        private Map m_phPos = new HashMap();
        private Map m_itPos = new HashMap();

        CodeInjectionHandler(GxmlElement p_sourceDom)
        {
            m_sourceDom = p_sourceDom;
            m_sourceDom.normalizeAllDescendentsType();
        }

        // Overridden method
        public void handleText(String p_text)
        {
            // Text is always added. Do not decode string.
            m_content.append(p_text);
        }

        // Overridden method
        public void handleStartTag(String p_name, Properties p_attributes,
                String p_originalString) throws DiplomatBasicParserException
        {
        	p_name = p_name.toLowerCase();
            if (p_name.equals(GxmlNames.SEGMENT)
                    || p_name.equals(GxmlNames.LOCALIZABLE))
            {
                // Preserve <segment> or <localizable> tags in the
                // main text, which is usually a matched target
                // segment.
                m_content.append(p_originalString);
            }
            else if (p_name.equals(GxmlNames.BPT)
                    || p_name.equals(GxmlNames.PH)
                    || p_name.equals(GxmlNames.IT))
            {
                String tuType = p_attributes.getProperty(GxmlNames.BPT_TYPE);
                if (tuType == null)
                {
                    tuType = "none";
                }
                tuType = TmxTypeMapper.normalizeType(tuType);

                int pos = 0;
                int elementType = 0;
                String attName = GxmlNames.BPT_TYPE;

                if (p_name.equals(GxmlNames.BPT))
                {
                    elementType = GxmlElement.BPT;
                    pos = getTypePosition(tuType, m_bptPos);
                }
                else if (p_name.equals(GxmlNames.PH))
                {
                    elementType = GxmlElement.PH;
                    pos = getTypePosition(tuType, m_phPos);
                }
                else if (p_name.equals(GxmlNames.IT))
                {
                    elementType = GxmlElement.IT;

                    String itPos = p_attributes.getProperty(GxmlNames.IT_POS);
                    if (itPos.equals("end"))
                    {
                        attName = GxmlNames.IT_POS;
                        tuType = itPos;
                    }

                    pos = getTypePosition(tuType, m_itPos);
                }

                // get an element with the same Tu type and the same
                // position in a segment from the source
                GxmlElement orgElem = m_sourceDom
                        .getNthDescendantByAttributeValue(attName, tuType,
                                elementType, pos);

                if (p_attributes.getProperty(GxmlNames.BPT_TYPE) == null
                        && "yes".equalsIgnoreCase(p_attributes
                                .getProperty(GxmlNames.INTERNAL)))
                {
                    orgElem = m_sourceDom.getNthDescendantByAttributeNone(
                            attName, elementType, pos);
                }

                String tagGxml = null;
                if (orgElem != null)
                {
                    if (p_name.equals(GxmlNames.BPT))
                    {
                        String iId = p_attributes.getProperty(GxmlNames.BPT_I);
                        GxmlElement bptCopy = new GxmlElement(orgElem);
                        bptCopy.setAttribute(GxmlNames.BPT_I, iId);
                        tagGxml = bptCopy.toGxml();
                    }
                    else
                    {
                        tagGxml = orgElem.toGxml();
                    }
                }
                else if (isReplacableTag(p_attributes, tuType))
                {
                    // We erase erasable tags from matched segment
                    // if the original source doesn't have
                    // corresponding tags. Some of the tags
                    // (e.g. nbsp) will be replaced with actual
                    // characters.
                    tagGxml = replaceErasableTag(tuType);
                }
                else
                {
                    // When we encounter a tag that is not in the
                    // original source, we just ignore it as opposed
                    // to throwing an exception as we originally did.
                    tagGxml = "";
                }

                m_content.append(tagGxml);

                if (p_name.equals(GxmlNames.BPT))
                {
                    String iId = p_attributes.getProperty(GxmlNames.BPT_I);
                    if (orgElem != null)
                    {
                        setEptForIattrFromGxmlElem(iId, orgElem);
                    }
                    else
                    {
                        setEptForIattrForErasable(iId, tuType);
                    }
                }
            }
            else if (p_name.equals(GxmlNames.EPT))
            {
                // get an ept string from the map
                String iId = p_attributes.getProperty(GxmlNames.BPT_I);
                String eptString = (String) m_iAttrEptMap.get(iId);

                m_content.append(eptString);
            }
            else
            {
                throw new DiplomatBasicParserException(
                        "Found non conforming gxml tag " + p_name);
            }

        }

        public void handleEndTag(String p_name, String p_originalTag)
        {
            if (p_name.equals(GxmlNames.SEGMENT)
                    || p_name.equals(GxmlNames.LOCALIZABLE))
            {
                // This tag should be the end of the text
                m_content.append(p_originalTag);
            }

            // end tags for the other elements are taken care of in
            // handleStartTag method
        }

        public String toString()
        {
            return m_content.toString();
        }

        private boolean isReplacableTag(Properties p_attributes, String p_tuType)
        {
            String erasable = p_attributes.getProperty(GxmlNames.BPT_ERASEABLE);
            if ((erasable != null && erasable.equals("yes"))
                    || p_tuType.equals("x-nbspace")
                    || p_tuType.equals("x-mso-spacerun")
                    || p_tuType.equals("x-mso-tab"))
            {
                return true;
            }

            return false;
        }

        private String replaceErasableTag(String p_tuType)
        {
            String erasableTag = "";

            if (p_tuType.equals("x-nbspace"))
            {
                erasableTag = "\u00A0";
            }
            else if (p_tuType.equals("x-mso-spacerun"))
            {
                erasableTag = "  ";
            }
            else if (p_tuType.equals("x-mso-tab"))
            {
                erasableTag = "\t";
            }

            return erasableTag;
        }

        private void setEptForIattrFromGxmlElem(String p_iId,
                GxmlElement p_originalGxmlElement)
                throws DiplomatBasicParserException
        {
            String originalIvalue = p_originalGxmlElement
                    .getAttribute(GxmlNames.BPT_I);

            // get an ept element with the same i id from the source
            GxmlElement orgEpt = m_sourceDom.getDescendantByAttributeValue(
                    GxmlNames.EPT_I, originalIvalue, GxmlElement.EPT);
            if (orgEpt == null)
            {
                throw new DiplomatBasicParserException(
                        "Corresponding <ept> couldn't be found in the "
                                + "source segment .");
            }

            GxmlElement eptCopy = new GxmlElement(orgEpt);
            eptCopy.setAttribute(GxmlNames.EPT_I, p_iId);

            // get the text representation of the element
            String eptString = eptCopy.toGxml();
            m_iAttrEptMap.put(p_iId, eptString);
        }

        private void setEptForIattrForErasable(String p_iId, String p_tuType)
                throws DiplomatBasicParserException
        {
            // String erasableTag = null;

            // if(p_tuType.equals("bold"))
            // {
            // erasableTag = "<ept i=\"" + p_iId + "\">&lt;/b&gt;</ept>";
            // }
            // else if(p_tuType.equals("italic"))
            // {
            // erasableTag = "<ept i=\"" + p_iId + "\">&lt;/i&gt;</ept>";
            // }
            // else if(p_tuType.equals("ulined"))
            // {
            // erasableTag = "<ept i=\"" + p_iId + "\">&lt;/u&gt;</ept>";
            // }
            // else
            // {
            // throw new DiplomatBasicParserException(
            // "Unrecognized erasable tag type \""
            // + p_tuType + "\" found.");
            // }

            // m_iAttrEptMap.put(p_iId, erasableTag);

            // We erase erasable tags.
            m_iAttrEptMap.put(p_iId, "");
        }

        private int getTypePosition(String p_tuType, Map p_typePos)
        {
            int ret;

            Integer pos = (Integer) p_typePos.get(p_tuType);
            if (pos == null)
            {
                ret = 1;
            }
            else
            {
                ret = pos.intValue() + 1;
            }
            p_typePos.put(p_tuType, new Integer(ret));

            return ret;
        }
    }

    // handler to substitute subflow contents
    static private class SubflowSubstitutionHandler extends
            TuvSegmentBaseHandler
    {
        // RE objects are not thread-safe, but new RE(REProgram) are.
        static private final REProgram openingTopTagProgram = compile("^<[^>]+>");
        static private final REProgram closingTopTagProgram = compile("</[^>]+>$");

        private final RE openingTopTag = new RE(openingTopTagProgram);
        private final RE closingTopTag = new RE(closingTopTagProgram);

        private Map m_separatedSegmentMap;
        private StringBuffer m_content = new StringBuffer(200);
        private boolean m_addText = true;

        static private REProgram compile(String p_pattern)
        {
            RECompiler compiler = new RECompiler();
            try
            {
                return compiler.compile(p_pattern);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e.getMessage());
            }
        }

        SubflowSubstitutionHandler(Map p_separatedSegmentMap)
        {
            m_separatedSegmentMap = p_separatedSegmentMap;
        }

        // Overridden method
        public void handleText(String p_text)
        {
            if (m_addText)
            {
                // All text except for subflow contents are added.
                // Do not decode string.
                m_content.append(p_text);
            }
        }

        // Overridden method
        public void handleStartTag(String p_name, Properties p_attributes,
                String p_originalString) throws DiplomatBasicParserException
        {
            // all opening tags are preserved
            m_content.append(p_originalString);

            if (p_name.equals(GxmlNames.SUB))
            {
                String subId = p_attributes.getProperty(GxmlNames.SUB_ID);

                // get a subflow with the same sub id from fragmented
                // segment
                String subflow = (String) m_separatedSegmentMap.get(subId);
                if (subflow == null)
                {
                    throw new DiplomatBasicParserException(
                            "Subflow with the id \"" + subId + "\""
                                    + " is missing from fragmented segment.");
                }

                // strip the top tag from the subflow
                subflow = stripTopTag(subflow);

                // get the text representation of the element
                m_content.append(subflow);

                m_addText = false;
            }
        }

        public void handleEndTag(String p_name, String p_originalTag)
        {
            // all closing tags are preserved
            m_content.append(p_originalTag);

            if (p_name.equals(GxmlNames.SUB))
            {
                m_addText = true;
            }
        }

        public String toString()
        {
            return m_content.toString();
        }

        // strip off the top opening and closing tags
        private String stripTopTag(String p_text)
        {
            String result;
            result = openingTopTag.subst(p_text, "");
            return closingTopTag.subst(result, "");
        }
    }

    /**
     * Get BaseTmTuv from a TUV (moved here from "InProgressTmManagerLocal")
     */
    public static BaseTmTuv createTmSegment(Tuv p_tuv, String p_subId,
            long p_jobId) throws LingManagerException
    {
        BaseTmTuv result = null;

        try
        {
            GlobalSightLocale locale = p_tuv.getGlobalSightLocale();
            Tu originalTu = p_tuv.getTu(p_jobId);
            PageTmTu tu = new PageTmTu(originalTu.getId(), 0,
                    originalTu.getDataType(), originalTu.getTuType(),
                    !originalTu.isLocalizable());
            PageTmTuv tuv = new PageTmTuv(p_tuv.getId(), p_tuv.getGxml(),
                    locale);
            tuv.setSid(p_tuv.getSid());
            tu.addTuv(tuv);

            Collection segmentTus = TmUtil.createSegmentTmTus(tu, locale);
            for (Iterator it = segmentTus.iterator(); it.hasNext();)
            {
                SegmentTmTu segmentTu = (SegmentTmTu) it.next();
                if (segmentTu.getSubId().equals(p_subId))
                {
                    result = segmentTu.getFirstTuv(locale);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }

        return result;
    }

    /**
     * Get BaseTmTuv from a TUV (moved here from "InProgressTmManagerLocal")
     */
    public static BaseTmTuv createTmSegment(String p_text, long p_tuId,
            GlobalSightLocale p_locale, String p_type, boolean p_isTranslatable)
            throws LingManagerException
    {
        BaseTmTuv result = null;

        try
        {
            if (p_isTranslatable)
            {
                p_text = addSegmentTag(p_text);
            }
            else
            {
                p_text = addLocalizableTag(p_text);
            }

            PageTmTu tu = new PageTmTu(p_tuId, 0, "unknown", p_type,
                    p_isTranslatable);
            PageTmTuv tuv = new PageTmTuv(0, p_text, p_locale);
            tu.addTuv(tuv);

            Collection segmentTus = TmUtil.createSegmentTmTus(tu, p_locale);
            for (Iterator it = segmentTus.iterator(); it.hasNext();)
            {
                SegmentTmTu segmentTu = (SegmentTmTu) it.next();
                if (segmentTu.getSubId().equals(SegmentTmTu.ROOT))
                {
                    result = segmentTu.getFirstTuv(p_locale);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LingManagerException(e);
        }

        return result;
    }

    // add the top <segment> tag if it doesn't exist
    private static String addSegmentTag(String p_text)
    {
        if (!p_text.startsWith("<segment"))
        {
            p_text = "<segment>" + p_text + "</segment>";
        }

        return p_text;
    }

    // add the top <localizable> tag if it doesn't exist
    private static String addLocalizableTag(String p_text)
    {
        if (!p_text.startsWith("<localizable"))
        {
            p_text = "<localizable>" + p_text + "</localizable>";
        }

        return p_text;
    }

}
