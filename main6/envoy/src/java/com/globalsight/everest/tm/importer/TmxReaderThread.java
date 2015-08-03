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

package com.globalsight.everest.tm.importer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.importer.ImportOptions;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.util.IntHolder;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;
import com.globalsight.util.UTC;
import com.globalsight.util.edit.EditUtil;

/**
 * Reads TMX files and produces TU objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class TmxReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TmxReaderThread.class);

    // The default value of a TMX type attribute, in case it's missing.
    static public final String DEFAULT_TYPE = "text";

    static private int s_threadCount = 0;

    private ReaderResultQueue m_results;
    private ImportOptions m_options;
    private Tm m_database;
    private int m_count = 0;
    private ReaderResult m_result = null;

    // TMX header info
    private Tmx m_tmx;
    private String m_tmxVersion;
    private int m_tmxLevel;

    // Import TUs having a source TUV of this locale (can be "all").
    private boolean m_importAllSources = false;
    private String m_sourceLocale;
    // Import these target TUVs.
    private boolean m_importAllTargets = false;
    private ArrayList m_targetLocales;

    // Normalized source language from TMX header.
    private String m_defaultSrcLang;

    public TmxReaderThread (ReaderResultQueue p_queue,
        ImportOptions p_options, Tm p_database)
    {
        m_results = p_queue;
        m_options = p_options;
        m_database = p_database;

        com.globalsight.everest.tm.importer.ImportOptions options =
            (com.globalsight.everest.tm.importer.ImportOptions)m_options;

        m_tmxLevel = ImportUtil.getTmxLevel(m_options);

        m_sourceLocale = options.getSelectedSourceLocale();
        if (m_sourceLocale.equalsIgnoreCase("all"))
        {
            m_sourceLocale = null;
            m_importAllSources = true;
        }

        m_targetLocales = options.getSelectedTargetLocales();
        if (m_targetLocales.size() == 1)
        {
            String locale = (String)m_targetLocales.get(0);

            if (locale.equalsIgnoreCase("all"))
            {
                m_targetLocales = null;
                m_importAllTargets = true;
            }
        }

        this.setName("TMX Reader Thread " + s_threadCount++);
    }

    public void run()
    {
        try
        {
            SAXReader reader = new SAXReader();
            reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

            // Read the DTD and validate.
            reader.setEntityResolver(DtdResolver.getInstance());
            reader.setValidation(true);

            reader.addHandler("/tmx",
                new ElementHandler()
                    {
                        public void onStart(ElementPath path)
                        {
                            Element element = path.getCurrent();

                            m_tmxVersion = element.attributeValue(Tmx.VERSION);
                        }

                        public void onEnd(ElementPath path)
                        {
                        }
                    }
                );

            reader.addHandler("/tmx/header",
                new ElementHandler()
                    {
                        public void onStart(ElementPath path)
                        {
                        }

                        public void onEnd(ElementPath path)
                        {
                            Element element = path.getCurrent();
                            element.detach();

                            m_tmx = new Tmx(element);
                            m_tmx.setTmxVersion(m_tmxVersion);

                            m_defaultSrcLang = ImportUtil.normalizeLocale(
                                m_tmx.getSourceLang());
                        }
                    }
                );

            // enable pruning to call me back as each Element is complete
            reader.addHandler("/tmx/body/tu",
                new ElementHandler ()
                    {
                        public void onStart(ElementPath path)
                        {
                            m_count++;
                        }

                        public void onEnd(ElementPath path)
                        {
                            Element element = path.getCurrent();
                            element.detach();

                            m_result = m_results.hireResult();

                            try
                            {
                                // Normalize spelling of locales.
                                normalizeTu(element);
                                // Filter out targets not to be imported.
                                filterTu(element);
                                // Validate we have source and target.
                                validateTu(element);

                                // Create TU objects
                                SegmentTmTu tu = createTu(element);

                                if (CATEGORY.isDebugEnabled())
                                {
                                    CATEGORY.debug(tu.toDebugString(true));
                                }

                                m_result.setResultObject(tu);
                            }
                            catch (Throwable ex)
                            {
                                String msg = "Entry " + m_count + ": " +
                                    ex.getMessage();

                                m_result.setError(msg);

                                if (CATEGORY.isDebugEnabled())
                                {
                                    CATEGORY.debug(msg, ex);
                                }
                                else
                                {
                                    CATEGORY.warn(msg);
                                }
                            }

                            boolean done = m_results.put(m_result);
                            m_result = null;

                            // Stop reading the TMX file.
                            if (done)
                            {
                                throw new ThreadDeath();
                            }
                        }
                    }
                );

            String url = m_options.getFileName();

            Document document = reader.read(url);
        }
        catch (ThreadDeath ignore)
        {
            CATEGORY.info("ReaderThread: interrupted.");
        }
        catch (Throwable ignore)
        {
            // Should never happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            CATEGORY.error("unexpected error", ignore);
        }
        finally
        {
            if (m_result != null)
            {
                m_results.fireResult(m_result);
            }

            m_results.producerDone();
            m_results = null;

            CATEGORY.debug("ReaderThread: done.");
        }
    }

    /**
     * Normalizes the spelling of the "lang" elements.
     */
    private void normalizeTu(Element p_tu)
        throws Exception
    {
        // Header default source lang normalized when header is read.
        // Locales read from m_options were normalized by TmxReader.

        String lang = p_tu.attributeValue(Tmx.SRCLANG);
        if (lang != null)
        {
            lang = ImportUtil.normalizeLocale(lang);
            p_tu.addAttribute(Tmx.SRCLANG, lang);
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");
        for (int i = 0, max = nodes.size(); i < max; i++)
        {
            Element elem = (Element)nodes.get(i);

            lang = elem.attributeValue(Tmx.LANG);
            lang = ImportUtil.normalizeLocale(lang);

            elem.addAttribute(Tmx.LANG, lang);
        }
    }

    /**
     * Filters the source and target TUVs to import.
     */
    private void filterTu(Element p_tu)
    {
        String srcLang = p_tu.attributeValue(Tmx.SRCLANG);
        if (srcLang == null)
        {
            srcLang = m_defaultSrcLang;
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");
        for (int i = 0, max = nodes.size(); i < max; i++)
        {
            Element elem = (Element)nodes.get(i);

            String tuvLang = elem.attributeValue(Tmx.LANG);

            // Is this the TU's source TUV?
            if (tuvLang.equalsIgnoreCase(srcLang))
            {
                continue;
            }

            // Treat as target TUV, should it be imported?
            if (m_importAllTargets || m_targetLocales.contains(tuvLang))
            {
                continue;
            }

            // Nope, remove.
            elem.detach();
        }
    }

    /**
     * Validates a TU by checking it contains a TUV in a source
     * language that should be imported. Also checks if there are more
     * than 2 TUVs.
     */
    private void validateTu(Element p_tu)
        throws Exception
    {
        boolean b_found = false;

        String tuvLang = null;
        String srcLang = p_tu.attributeValue(Tmx.SRCLANG);
        if (srcLang == null)
        {
            srcLang = m_defaultSrcLang;
        }

        // can't use xpath here because xml:lang won't be matched
        List nodes = p_tu.selectNodes("./tuv");

        if (nodes.size() < 2)
        {
            throw new Exception(
                "TU contains less than 2 TUVs (after filtering), ignoring");
        }

        for (int i = 0, max = nodes.size(); i < max; i++)
        {
            Element elem = (Element)nodes.get(i);

            tuvLang = elem.attributeValue(Tmx.LANG);
            if (tuvLang.equalsIgnoreCase(srcLang))
            {
                b_found = true;
                break;
            }
        }

        if (!b_found)
        {
            throw new Exception(
                "TU is missing TUV in source language " + srcLang);
        }

        if (!m_importAllSources && !tuvLang.equalsIgnoreCase(m_sourceLocale))
        {
            throw new Exception(
                "TU has no source TUV in " + m_sourceLocale + ", ignoring");
        }
    }

    /**
     * Converts a DOM TU to a GS SegmentTmTu, thereby converting any TMX
     * format specialities as best as possible.
     */
    private SegmentTmTu createTu(Element p_root)
        throws Exception
    {
        SegmentTmTu result = new SegmentTmTu();

        // Optional TU attributes:

        // Original TU id, if known
        String id = p_root.attributeValue(Tmx.TUID);
        if (id != null && id.length() > 0)
        {
            try
            {
                long lid = Long.parseLong(id);
                result.setId(lid);
            }
            catch (Throwable ignore)
            {
                // <TU tuid> can be an alphanumeric token.
                // If it is not a simple number, we ignore it.
            }
        }

        // Datatype of the TU (html, javascript etc)
        String format = p_root.attributeValue(Tmx.DATATYPE);
        if (format == null || format.length() == 0)
        {
            format = m_tmx.getDatatype();
        }
        result.setFormat(format);

        // Locale of Source TUV (use default from header)
        String lang = p_root.attributeValue(Tmx.SRCLANG);

        if (lang == null || lang.length() == 0)
        {
            lang = m_defaultSrcLang;
        }

        try
        {
            String locale = ImportUtil.normalizeLocale(lang);
            result.setSourceLocale(ImportUtil.getLocaleByName(locale));
        }
        catch (Throwable ex)
        {
            CATEGORY.warn("invalid locale " + lang);

            throw new Exception("cannot handle locale " + lang);
        }

        // TODO: other optional attributes
        String usageCount = p_root.attributeValue(Tmx.USAGECOUNT);
        String usageDate = p_root.attributeValue(Tmx.LASTUSAGEDATE);
        //String tool = p_root.attributeValue(Tmx.CREATIONTOOL);
        //String toolversion = p_root.attributeValue(Tmx.CREATIONTOOLVERSION);
        // used in createTuv()
        //String creationDate = p_root.attributeValue(Tmx.CREATIONDATE);
        //String creationUser = p_root.attributeValue(Tmx.CREATIONID);
        //String changeDate = p_root.attributeValue(Tmx.CHANGEDATE);
        //String changeUser = p_root.attributeValue(Tmx.CHANGEID);

        // GlobalSight-defined properties:

        // Segment type (text, css-color, etc)
        String segmentType = "text";

        Node node = p_root.selectSingleNode(
            ".//prop[@type = '" + Tmx.PROP_SEGMENTTYPE + "']");

        if (node != null)
        {
            segmentType = node.getText();
        }
        result.setType(segmentType);

        //Read SID
        node = p_root.selectSingleNode(
                ".//prop[@type= '" + Tmx.PROP_TM_UDA_SID + "']");
        if (node != null) {
            result.setSID(node.getText());
        }
        
        // TU type (T or L)
        boolean isTranslatable = true;
        node = p_root.selectSingleNode(
            ".//prop[@type = '" + Tmx.PROP_TUTYPE + "']");

        if (node != null)
        {
            isTranslatable = node.getText().equals(Tmx.VAL_TU_TRANSLATABLE);
        }

        if (isTranslatable)
        {
            result.setTranslatable();
        }
        else
        {
            result.setLocalizable();
        }
        
        // prop with Att::
        List propNodes = p_root.elements("prop");
        for (int i = 0; i < propNodes.size(); i++)
        {
            Element elem = (Element) propNodes.get(i);
            ProjectTmTuTProp prop = createProp(result, elem);

            if (prop != null)
                result.addProp(prop);
        }

        // TUVs
        List nodes = p_root.elements("tuv");
        for (int i = 0; i < nodes.size(); i++)
        {
            Element elem = (Element)nodes.get(i);

            SegmentTmTuv tuv = createTuv(result, elem);

            result.addTuv(tuv);
        }

		if (com.globalsight.everest.tm.importer.ImportOptions.TYPE_TMX_WORLD_SERVER
				.equals(m_options.getFileType()))
		{
			result.setFromWorldServer(true);
		}
        
        return result;
    }

    /**
     * Converts a DOM TUV to a GS SegmentTmTuv, thereby converting any
     * TMX format specialities as best as possible.
     *
     * Note: if the attributes in one TUV are incorrect and can not be
     * repaired so we cannot create correct GXML (which depends on
     * correct i/x attributes and type), we need to process all TUVs
     * together; if one encounters an error, all TUVs should be
     * imported without tags as Level 1.
     *
     * @param p_root the TUV node in the DOM structure.
     */
    private SegmentTmTuv createTuv(SegmentTmTu p_tu, Element p_root)
        throws Exception
    {
        SegmentTmTuv result = new SegmentTmTuv();
        result.setOrgSegment(p_root.asXML());
        
        // need to set backpointer to tuv, or SegmentTmTuv.equals() fails.
        result.setTu(p_tu);

        // language of the TUV "EN-US", case insensitive
        String lang = p_root.attributeValue(Tmx.LANG);

        try
        {
            String locale = ImportUtil.normalizeLocale(lang);
            result.setLocale(ImportUtil.getLocaleByName(locale));
        }
        catch (Throwable ex)
        {
            throw new Exception("unknown locale " + lang + ",you can create it in system then retry.");
        }

        // Creation user - always set to a known value
        String user = p_root.attributeValue(Tmx.CREATIONID);
        if (user == null)
        {
            user = p_root.getParent().attributeValue(Tmx.CREATIONID);
        }

        result.setCreationUser(user != null ? user : Tmx.DEFAULT_USER);

        // Modification user - only set if known
        user = p_root.attributeValue(Tmx.CHANGEID);
        if (user == null)
        {
            user = p_root.getParent().attributeValue(Tmx.CHANGEID);
        }

        if (user != null)
        {
            result.setModifyUser(user);
        }

        // Timestamps (should be expressed using java.util.Date).
        // In TMX, timestamps use the short form: yyyymmddThhmmssZ,
        // so prepare for both short and long form.
        Date now = new Date();
        Date date;

        // Creation date - always set to a known value
        String ts = p_root.attributeValue(Tmx.CREATIONDATE);
        if (ts == null)
        {
            ts = p_root.getParent().attributeValue(Tmx.CREATIONDATE);
        }

        if (ts != null)
        {
            date = UTC.parseNoSeparators(ts);
            if (date == null)
            {
                date = UTC.parse(ts);
            }
            result.setCreationDate(new Timestamp(date.getTime()));
        }
        else
        {
            result.setCreationDate(new Timestamp(now.getTime()));
        }

        // Modification date - only set if known (note: currently
        // AbstractTmTuv sets the modification date to NOW)
        ts = p_root.attributeValue(Tmx.CHANGEDATE);
        if (ts == null)
        {
            ts = p_root.getParent().attributeValue(Tmx.CHANGEDATE);
        }

        if (ts != null)
        {
            date = UTC.parseNoSeparators(ts);
            if (date == null)
            {
                date = UTC.parse(ts);
            }
            result.setModifyDate(new Timestamp(date.getTime()));
        }
        else
        {
            // If no "changedate", set it same as "creationdate".
            result.setModifyDate(result.getCreationDate());
        }

		ts = p_root.attributeValue(Tmx.LASTUSAGEDATE);
		if (ts == null)
		{
			ts = p_root.getParent().attributeValue(Tmx.LASTUSAGEDATE);
		}
		if (ts != null)
		{
			date = UTC.parseNoSeparators(ts);
			if (date == null)
			{
				date = UTC.parse(ts);
			}
			result.setLastUsageDate(new Timestamp(date.getTime()));
		}

		List tuvPropNodes = p_root.elements("prop");
		for (int i = 0; i < tuvPropNodes.size(); i++)
		{
			Element elem = (Element) tuvPropNodes.get(i);
			String type = elem.attributeValue("type");
			String value = elem.getText();
			if (Tmx.PROP_PREVIOUS_HASH.equalsIgnoreCase(type))
			{
				result.setPreviousHash(Long.parseLong(value));
			}
			else if (Tmx.PROP_NEXT_HASH.equalsIgnoreCase(type))
			{
				result.setNextHash(Long.parseLong(value));
			}
			else if (Tmx.PROP_JOB_ID.equalsIgnoreCase(type))
			{
				result.setJobId(Long.parseLong(value));
			}
			else if (Tmx.PROP_JOB_NAME.equalsIgnoreCase(type))
			{
				result.setJobName(value);
			}
			else if (Tmx.PROP_CREATION_PROJECT.equalsIgnoreCase(type))
			{
				result.setUpdatedProject(value);
			}
		}
        // Segment text: need to produce root elements <translatable>
        // and <localizable> depending on TU type.
        StringBuffer segment = new StringBuffer();

        if (p_tu.isTranslatable())
        {
            segment.append("<segment>");
        }
        else
        {
            segment.append("<localizable>");
        }

        segment.append(getSegmentValue(p_root));

        if (p_tu.isTranslatable())
        {
            segment.append("</segment>");
        }
        else
        {
            segment.append("</localizable>");
        }

        result.setSid(p_tu.getSID());
        //End of Added
        result.setSegment(segment.toString());

        return result;
    }

    private ProjectTmTuTProp createProp(SegmentTmTu p_tu, Element p_root) throws Exception
    {
        ProjectTmTuTProp result = null;
        String type = p_root.attributeValue("type");

        if (type != null && type.startsWith(ProjectTmTuTProp.TYPE_ATT_PREFIX))
        {
            result = new ProjectTmTuTProp();
            result.setPropType(type);
            result.setPropValue(p_root.getText());
        }
        
        // When a TM is imported: for each TUV: for each TUV attribute defined on the TM: 
        // if the attribute exists in the imported TMX file, then the attribute is created 
        // and the value is set from the imported TM. 
        if (result != null && m_database instanceof ProjectTM)
        {
            String attName = result.getAttributeName();
            ProjectTM ptm = (ProjectTM) m_database;
            List<String> tmAttNames = ptm.getAllTMAttributenames();
            
            // ignore the tu attributes if not defined in TM.
            if (!tmAttNames.contains(attName))
            {
                result = null;
            }
        }

        return result;
    }

    /**
     * Reads the segment content from the <seg> element and fixes any
     * missing sub locType attributes and sub id values.
     *
     * @param p_root the TUV node in the DOM structure.
     * @return the segment text or XML value, encoded as XML.
     */
    private String getSegmentValue(Element p_root)
    {
        StringBuffer result = new StringBuffer();

        Element seg = p_root.element("seg");

        // TODO for all formats: strip the TMX 1.4 <hi> element.
        seg = removeHiElements(seg);

        if (m_tmxLevel == ImportUtil.TMX_LEVEL_1)
        {
            // Level 1: discard any embedded TMX tags.
            result.append(EditUtil.encodeXmlEntities(seg.getText()));
        }
        else if (m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_RTF ||
            m_tmxLevel == ImportUtil.TMX_LEVEL_TRADOS_HTML)
        {
            // Trados TMX: converted to native GXML in analysis phase,
            // this is a no-op.
            result.append(EditUtil.encodeXmlEntities(seg.getText()));
        }
        else
        {
            // Level 2 and native: preserve embedded TMX tags.
            try
            {
                // First, ensure we have all G-TMX attributes (i, x, type)
                seg = validateSegment(p_root, seg);

                // Thu May 15 18:15:26 2003 CvdL
                // For now, strip out all sub segments.
                seg = removeSubElements(seg);

                result.append(ImportUtil.getInnerXml(seg));
            }
            catch (Throwable ex)
            {
                // On error, import as level 1.
                result.append(EditUtil.encodeXmlEntities(seg.getText()));
            }

        }

        return result.toString();
    }

    /**
     * Removes all TMX 1.4 <hi> elements from the segment. <hi> is
     * special since it does not surround embedded tags but text,
     * which must be pulled out of the <hi> and added to the
     * parent segment.
     */
    private Element removeHiElements(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findHiElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element hi = (Element)elems.get(i);

            removeHiElement(hi);
        }

        return p_seg;
    }

    /**
     * Removes the given TMX 1.4 <hi> element from the segment. <hi>
     * is special since it does not surround embedded tags but text,
     * which must be pulled out of the <hi> and added to the
     * parent segment.
     */
    private void removeHiElement(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <hi>'s content
        // instead of the <hi>.

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node)content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node)newContent.get(i);

            if (i == index)
            {
                parent.appendContent(p_element);
            }
            else
            {
                parent.add(node);
            }
        }
    }

    private void findHiElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <hi> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node)p_element.node(i);

            if (child instanceof Element)
            {
                findHiElements(p_result, (Element)child);
            }
        }

        if (p_element.getName().equals("hi"))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Removes all TMX 1.4 <sub> elements from the segment. <sub> is
     * special since it does not only surround embedded tags but also
     * text, which must be pulled out of the <sub> and added to the
     * parent tag.
     */
    private Element removeSubElements(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findSubElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element sub = (Element)elems.get(i);

            removeSubElement(sub);
        }

        return p_seg;
    }

    /**
     * Removes the given TMX 1.4 <sub> element from the segment. <sub>
     * is special since it does not only surround embedded tags but
     * also text, which must be pulled out of the <sub> and added to
     * the parent tag.
     */
    private void removeSubElement(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <sub>'s textual
        // content instead of the <sub> (this clears any embedded TMX
        // tags in the subflow).

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node)content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node)newContent.get(i);

            if (i == index)
            {
                parent.addText(p_element.getText());
            }
            else
            {
                parent.add(node);
            }
        }
    }

    private void findSubElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <sub> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node)p_element.node(i);

            if (child instanceof Element)
            {
                findSubElements(p_result, (Element)child);
            }
        }

        if (p_element.getName().equals("sub"))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Ensures all the GXML attributes we need are present in the
     * segment. This includes i, x, type and pos.
     *
     * Problem is, GXML depends entirely on the x attribute, which is
     * not mandatory for most of the elements, so we'll have to invent
     * one. Type is also not mandatory, so we will need to infer one
     * (like "unknown").
     *
     * Strategy for x: if x is missing, we assign it based on an
     * incremental counter. The assumption being that if one x is
     * missing, all x's are missing and by assigning incremental
     * values we don't create any duplicates. (But we may still get
     * them wrong...)
     *
     * <pre>
     * Element   1.1   1.2   1.3   1.4 (M-andatory, O-ptional)
     * ===============================
     * BPT i      M     M           M
     * BPT type   o     o           o
     * BPT x      o     o           M
     * EPT i      M     M           M
     * HI type    -     o           o
     * HI x       -     o           o
     * IT pos     M     M           M
     * IT type    o     o           o
     * IT x       o     o           o
     * PH assoc   o     o           o
     * PH type    o     o           o
     * PH x       o     o           o
     * UT x       o     o           o   DEPRECATED
     * ------------------------------
     * sorted by attribute:
     * ------------------------------
     * BPT x      o     o           M
     * HI x       -     o           o
     * IT x       o     o           o
     * PH x       o     o           o
     * UT x       o     o           o   DEPRECATED
     *
     * BPT i      M     M           M
     * EPT i      M     M           M
     *
     * BPT type   o     o           o
     * HI type    -     o           o
     * PH type    o     o           o
     * IT type    o     o           o
     *
     * IT pos     M     M           M
     * PH assoc   o     o           o
     * </pre>
     *
     * @throws Exception if a required attribute is not present or an
     * optional attribute cannot be set.
     */
    private Element validateSegment(Element p_tuv, Element p_seg)
        throws Exception
    {
        return validateSegment(p_tuv, p_seg, new IntHolder(1));
    }

    private Element validateSegment(Element p_tuv, Element p_seg,
        IntHolder p_x_count)
        throws Exception
    {
        String attr;

        List elems = p_seg.elements();

        for (Iterator it = elems.iterator(); it.hasNext(); )
        {
            Element elem = (Element)it.next();
            String name = elem.getName();

            if (name.equals("bpt"))
            {
                attr = elem.attributeValue("x");  // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("i");  // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                        "A <bpt> tag is lacking the mandatory i attribute.");
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    //elem.addAttribute("type", DEFAULT_TYPE);
                }
            }
            else if (name.equals("ept"))
            {
                attr = elem.attributeValue("i");  // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                        "A <ept> tag is lacking the mandatory i attribute.");
                }
            }
            else if (name.equals("it"))
            {
                attr = elem.attributeValue("x");  // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("pos");  // mandatory
                if (attr == null || attr.length() == 0)
                {
                    throw new Exception(
                        "A <it> tag is lacking the mandatory pos attribute.");
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", DEFAULT_TYPE);
                }
            }
            else if (name.equals("ph"))
            {
                attr = elem.attributeValue("x");  // mandatory only in 1.4
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("x", String.valueOf(p_x_count.inc()));
                }

                attr = elem.attributeValue("type");
                if (attr == null || attr.length() == 0)
                {
                    elem.addAttribute("type", DEFAULT_TYPE);
                }

                // GXML doesn't care about assoc, just preserve it.
                // attr = elem.attributeValue("assoc");
            }
            else if (name.equals("ut"))
            {
                // TMX level 2 does not allow UT. We can either remove
                // it, or look inside and guess what it may be.
                it.remove();
                continue;
            }

            // Recurse into any subs.
            validateSubs(p_tuv, elem, p_x_count);
        }

        return p_seg;
    }

    /**
     * Validates the sub elements inside a TMX tag. This means adding
     * a <sub locType="..."> attribute.
     *
     * TODO.
     */
    private void validateSubs(Element p_tuv, Element p_elem,
        IntHolder p_x_count)
        throws Exception
    {
        List subs = p_elem.elements("sub");

        for (int i = 0, max = subs.size(); i < max; i++)
        {
            Element sub = (Element)subs.get(i);

            validateSegment(p_tuv, sub, p_x_count);
        }
    }
}
