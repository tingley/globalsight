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

package com.globalsight.everest.page.pageupdate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.everest.edit.online.UIConstants;
import com.globalsight.everest.page.PageTemplate;
import com.globalsight.everest.page.RenderingOptions;
import com.globalsight.everest.page.SnippetPageTemplate;
import com.globalsight.everest.page.pageimport.TemplateGenerator;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImplVo;
import com.globalsight.everest.tuv.TuvImplVo;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.gxml.GxmlRootElement;

/**
 * <p>Creates a preview of a GXML page in a given target locale by
 * interpreting GS tags.
 */
public class GxmlPreviewer
{
    private static final org.apache.log4j.Logger CATEGORY =
        org.apache.log4j.Logger.getLogger(
            GxmlPreviewer.class);

    static private final Long s_LONGZERO = new Long(0);
    static private final Integer s_INTZERO = new Integer(0);
    static private final Integer s_INTONE = new Integer(1);

    static private int s_counter = 0;

    //
    // Members
    //

    private DiplomatAPI m_diplomat = null;

    private String m_gxml;
    private String m_localeString;
    private GlobalSightLocale m_locale;

    //
    // Constructor
    //

    public GxmlPreviewer(String p_gxml, String p_locale)
    {
        m_gxml = p_gxml;
        m_localeString = p_locale;
    }

    //
    // Public Methods
    //

    public String getGxmlPreview()
        throws Exception
    {
        int counter = s_counter++;

        Logger.writeDebugFile("previewGxml-" +
            String.valueOf(counter) + ".xml", m_gxml);

        try
        {
            GxmlRootElement root = parseGxml(m_gxml);
            // Let obsolete data be garbage-collected.
            m_gxml = null;

            m_locale = getLocale(m_localeString);

            // For preview, we need the TUs (with fake IDs) but we don't
            // need the parsed TUVs because the template uses a HashMap
            // { tuid -> string } to populate itself.
            // Also, the blockid (paragraph id) does not matter.
            ArrayList tus = new ArrayList();
            ArrayList tuvs = new ArrayList();
            createTUs(root, m_locale, tus, tuvs);

            PageTemplate template = generateExportTemplate(root, tus);

            // This preview only makes sense if there are snippets (:
            if (containGsTags(root))
            {
                template = new SnippetPageTemplate(template, m_localeString);
            }

            for (int i = 0, max = tuvs.size(); i < max; i++)
            {
                TuvImplVo tuv = (TuvImplVo)tuvs.get(i);

                template.insertTuvContent(new Long(i + 1), tuv.getGxml());
            }

            String exportGxml = template.getPageData(
                new RenderingOptions(UIConstants.UIMODE_EXPORT, 0, 0));

            Logger.writeDebugFile("previewGxml-" + m_localeString +
                "-" + String.valueOf(counter) + ".xml", exportGxml);

            String targetHtml = mergeGxml(exportGxml);

            Logger.writeDebugFile("previewGxml-" + m_localeString +
                "-" + String.valueOf(counter) + ".html", targetHtml);

            return targetHtml;
        }
        catch (Throwable ex)
        {
            throw new Exception(ex.getMessage(), ex);
        }
    }

    private GxmlRootElement parseGxml(String p_gxml)
        throws Exception
    {
        GxmlFragmentReader reader =
            GxmlFragmentReaderPool.instance().getGxmlFragmentReader();

        try
        {
            return reader.parse(p_gxml);
        }
        finally
        {
            GxmlFragmentReaderPool.instance().freeGxmlFragmentReader(reader);
        }
    }

    private void createTUs(GxmlRootElement p_root,
        GlobalSightLocale p_locale, ArrayList p_tus, ArrayList p_tuvs)
        throws Exception
    {
        createTUs_1(p_root, p_locale, p_tus, p_tuvs);

        for (int i = 0, max = p_tus.size(); i < max; i++)
        {
            TuImplVo tu = (TuImplVo)p_tus.get(i);

            // assign dummy ID, just needs to be unique
            tu.setId(i + 1);
        }
    }

    private void createTUs_1(GxmlElement p_root,
        GlobalSightLocale p_locale, ArrayList p_tus, ArrayList p_tuvs)
        throws Exception
    {
        if (p_root == null)
        {
            return;
        }

        List elements = p_root.getChildElements();

        for (int i = 0, max = elements.size(); i < max; i++)
        {
            GxmlElement elem = (GxmlElement)elements.get(i);

            switch (elem.getType())
            {
            case GxmlElement.LOCALIZABLE:
                createLocalizableSegment(elem, p_locale, p_tus, p_tuvs);
                break;

            case GxmlElement.TRANSLATABLE:
                createTranslatableSegments(elem, p_locale, p_tus, p_tuvs);
                break;

            case GxmlElement.GS:
                createTUs_1(elem, p_locale, p_tus, p_tuvs);
                break;

            default:
                break;
            }
        }
    }

    private Tu createLocalizableSegment(GxmlElement p_elem,
        GlobalSightLocale p_sourceLocale, ArrayList p_tus, ArrayList p_tuvs)
    {
        Long blockId = s_LONGZERO;
        Integer wordcount = s_INTONE;

        // If no blockId present, then localizable is new.
        // If present, it may be modified - checked in loadNewTuTuvData().

        String temp;
        temp = p_elem.getAttribute(GxmlNames.LOCALIZABLE_BLOCKID);
        if (temp != null)
        {
            blockId = Long.valueOf(temp);
        }

        temp = p_elem.getAttribute(GxmlNames.LOCALIZABLE_WORDCOUNT);
        if (temp != null)
        {
            wordcount = Integer.valueOf(temp);
        }

        String datatype = p_elem.getAttribute(GxmlNames.LOCALIZABLE_DATATYPE);

        // dataType is optional on LOCALIZABLE
        if (datatype == null)
        {
            GxmlElement diplomat = GxmlElement.getGxmlRootElement(p_elem);

            datatype = diplomat.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            if (datatype == null)
            {
                throw new RuntimeException(
                    "<localizable> or <diplomat> must carry 'datatype' attribute.");
            }
        }

        String tuType = p_elem.getAttribute(GxmlNames.LOCALIZABLE_TYPE);

        if (tuType == null)
        {
            throw new RuntimeException(
                "<localizable> must carry 'type' attribute.");
        }

        TuImplVo tu = new TuImplVo();
        tu.setDataType(datatype);
        tu.setTuTypeName(tuType);
        tu.setLocalizableType('L');
        tu.setPid(blockId.longValue());
        p_tus.add(tu);

        TuvImplVo tuv = new TuvImplVo();
        tuv.setGlobalSightLocale(p_sourceLocale);
        tuv.setWordCount(wordcount != null ? wordcount.intValue() : 1);
        tuv.setGxml(p_elem.toGxml());
        tuv.setOrder(1);
        tu.addTuv(tuv);
        p_tuvs.add(tuv);

        /*
        String text = EditUtil.decodeXmlEntities(
            p_elem.toGxmlExcludeTopTags());
        p_tuvs.add(text);
        */

        if (CATEGORY.isDebugEnabled())
        {
            System.err.println("Incoming localizable: " + p_elem.toGxml());
        }

        return tu;
    }

    private void createTranslatableSegments(GxmlElement p_elem,
        GlobalSightLocale p_sourceLocale, ArrayList p_tus, ArrayList p_tuvs)
        throws Exception
    {
        Long blockId = s_LONGZERO;
        Integer wordcount = s_INTZERO;

        // If no blockId present, then segment is new.

        String temp;

        temp = p_elem.getAttribute(GxmlNames.TRANSLATABLE_BLOCKID);
        if (temp != null)
        {
            blockId = Long.valueOf(temp);
        }

        temp = p_elem.getAttribute(GxmlNames.TRANSLATABLE_WORDCOUNT);
        if (temp != null)
        {
            wordcount = Integer.valueOf(temp);
        }

        String tuType = p_elem.getAttribute(GxmlNames.TRANSLATABLE_TYPE);

        // set optional Gxml attribute "type" if not set
        if (tuType == null)
        {
            tuType = "text";
        }

        String datatype = p_elem.getAttribute(GxmlNames.TRANSLATABLE_DATATYPE);

        // dataType is optional on TRANSLATABLE/SEGMENT
        if (datatype == null)
        {
            GxmlElement diplomat = GxmlElement.getGxmlRootElement(p_elem);

            datatype = diplomat.getAttribute(GxmlNames.GXMLROOT_DATATYPE);
            if (datatype == null)
            {
                throw new RuntimeException(
                    "<translatable> or <diplomat> must carry 'datatype' attribute.");
            }
        }

        List segments = p_elem.getChildElements();

        for (int i = 0, max = segments.size(); i < max; i++)
        {
            GxmlElement segment = (GxmlElement)segments.get(i);

            TuImplVo tu = new TuImplVo();
            tu.setDataType(datatype);
            tu.setTuTypeName(tuType);
            tu.setLocalizableType('T');
            tu.setPid(blockId.longValue());

            Integer segWordCount =
                segment.getAttributeAsInteger(GxmlNames.SEGMENT_WORDCOUNT);

            // Note: Structural edits come in with blockId <= 0 but
            // in-place edits still have their original blockid, so
            // our caller loadNewTuTuvData() will have the final word
            // on which TUVs were modified and which not.

            String text = EditUtil.decodeXmlEntities(
                segment.toGxmlExcludeTopTags());

            if (CATEGORY.isDebugEnabled())
            {
                System.err.println("text (" + datatype + "): " + text);
            }

            // Paragraph-extract the segment.
            SegmentNode tmp = extractSegment(text, datatype, p_sourceLocale);
            String gxml = segmentNodeToXml(tmp, i + 1);
            segWordCount = new Integer(tmp.getWordCount());

            if (CATEGORY.isDebugEnabled())
            {
                System.err.println("extracted as: " + gxml);
            }

            TuvImplVo tuv = new TuvImplVo();
            tuv.setGlobalSightLocale(p_sourceLocale);
            tuv.setWordCount(segWordCount != null ? segWordCount.intValue() : 0);
            tuv.setOrder(i + 1);
            tuv.setGxml(gxml);
            tu.addTuv(tuv);

            p_tuvs.add(tuv);
            p_tus.add(tu);

            if (CATEGORY.isDebugEnabled())
            {
                System.err.println("Incoming segment: " + gxml);
            }
        }
    }

    private SegmentNode extractSegment(String p_segment, String p_datatype,
        GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        DiplomatAPI api = getDiplomatApi();

        api.setEncoding("Unicode");
        api.setLocale(p_sourceLocale.getLocale());
        api.setInputFormat(p_datatype);
        api.setSentenceSegmentation(false);
        api.setSegmenterPreserveWhitespace(true);

        if (EditUtil.isHtmlDerivedFormat(p_datatype))
        {
            api.setSourceString(p_segment);
        }
        else if (p_datatype.equals(IFormatNames.FORMAT_JAVASCRIPT))
        {
            // Extractor needs a complete input file... use a statement.
            api.setSourceString("var tmp = \"" + p_segment + "\"");
        }
        else
        {
            throw new RuntimeException("Source pages containing dataformat " +
                p_datatype + " cannot be updated.");
        }

        api.extract();

        Output output = api.getOutput();

        for (Iterator it = output.documentElementIterator(); it.hasNext(); )
        {
            Object o = it.next();

            if (o instanceof TranslatableElement)
            {
                TranslatableElement trans = (TranslatableElement)o;

                return (SegmentNode)(trans.getSegments().get(0));
            }
        }

        return null;
    }

    private String segmentNodeToXml(SegmentNode p_node, int p_segmentId)
    {
        StringBuffer result = new StringBuffer();

        result.append("<segment wordcount=\"");
        result.append(p_node.getWordCount());
        result.append("\" segmentId=\"");
        result.append(p_segmentId);
        result.append("\">");
        result.append(p_node.getSegment());
        result.append("</segment>");

        return result.toString();
    }

    private PageTemplate generateExportTemplate(GxmlRootElement p_doc,
        ArrayList p_tuList)
        throws Exception
    {
        TemplateGenerator tg = new TemplateGenerator();
        PageTemplate result = tg.generateExport(p_doc, p_tuList);
        return result;
    }

    private String mergeGxml(String p_gxml)
        throws Exception
    {
        DiplomatAPI api = getDiplomatApi();
        // Do not keep GS tags.
        return api.merge(p_gxml, false);
    }

    /**
     * Return true if the page (Gxml) contains at least one GS tag.
     */
    private boolean containGsTags(GxmlRootElement p_root)
    {
        int gsTag[] = { GxmlElement.GS};
        List tagElements = p_root.getChildElements(gsTag);

        return (tagElements.size() > 0);
    }

    private DiplomatAPI getDiplomatApi()
    {
        if (m_diplomat == null)
        {
            m_diplomat = new DiplomatAPI();
        }

        m_diplomat.reset();

        return m_diplomat;
    }

    /**
     * Wraps the code for getting the locale manager and any
     * exceptions.
     */
    private GlobalSightLocale getLocale(String p_locale)
        throws Exception
    {
        return ServerProxy.getLocaleManager().getLocaleByString(p_locale);
    }
}
