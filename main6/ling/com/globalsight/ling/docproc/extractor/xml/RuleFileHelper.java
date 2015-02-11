package com.globalsight.ling.docproc.extractor.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DiplomatCtrlCharConverter;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.tw.internal.EmbedOnlineInternalTag;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;

public class RuleFileHelper
{
    static private final Logger s_logger = Logger
            .getLogger(RuleFileHelper.class);

    /**
     * This method is used to test xml rule.
     * 
     * @param sourceXml
     *            the input xml content.
     * @param rule
     *            the xml rule.
     * @return the segments extracted from source xml according xml rule.
     * @throws ExtractorException
     */
    public static String extractXmlFileWithRule(String sourceXml, String rule)
            throws ExtractorException
    {
        EFInputData m_input = new EFInputData();
        // Fixing bug GBS-536:
        // Add parameter "UTF-8" to indicate the consistent character set,
        // or in the old way, it'll use platform's default character encoding
        // m_input.setInput(sourceXml.getBytes());
        try
        {
            m_input.setInput(sourceXml.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            s_logger.info(e.getMessage());
            m_input.setInput(sourceXml.getBytes());
        }
        m_input.setLocale(new Locale("en", "US"));
        m_input.setCodeset("UTF-8");
        AbstractExtractor extractor = new Extractor();

        // for test purpose
        //XMLRuleFilter filter = FilterHelper.getXmlFilter(2);
        Output m_output = new Output();
        extractor.init(m_input, m_output);
        extractor.loadRules(rule);
        //extractor.setMainFilter(filter);
        extractor.extract();

        // Convert C0 control codes to PUA characters to avoid XML
        // parser error. Doing it on the output object is much cheaper
        // than on the final string and also ensures correct input in
        // extraction steps that use XML parsers (like word counting).
        DiplomatCtrlCharConverter dc = new DiplomatCtrlCharConverter();
        dc.convertChars(m_output);
        m_output = dc.getOutput();

        DiplomatSegmenter ds = new DiplomatSegmenter();
        ds.setPreserveWhitespace(true);
        try
        {
            ds.segment(m_output);
        }
        catch (Exception e)
        {
            throw new ExtractorException(e);
        }

        m_output = ds.getOutput();
        String gxml = DiplomatWriter.WriteXML(m_output);
        return getSegments(gxml);
    }

    private static String getSegments(String gxml)
    {
        StringBuffer segments = new StringBuffer();
        SAXReader saxReader = new SAXReader();
        Document document = null;
        // Fixing bug GBS-536:
        // Add parameter "UTF-8" to indicate the consistent character set,
        // or in the old way, it'll use platform's default character encoding
        // InputStream in = new ByteArrayInputStream(gxml.getBytes());
        InputStream in = null;
        try
        {
            in = new ByteArrayInputStream(gxml.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            s_logger.info(e.getMessage());
            in = new ByteArrayInputStream(gxml.getBytes());
        }

        try
        {
            document = saxReader.read(in);
        }
        catch (DocumentException e)
        {
            s_logger.error("Error when read: " + gxml, e);
            throw new ExtractorException(e);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                }
            }
        }
        Element root = document.getRootElement();
        for (Iterator i = root.elementIterator("translatable"); i.hasNext();)
        {
            Element trans = (Element) i.next();
            List segs = trans.elements("segment");

            if (segs != null && segs.size() > 0)
                for (Object object : segs)
                {
                    Element seg = (Element) object;
                    segments.append(getSegment(seg.asXML()));
                    segments.append("\n");
                }
        }
        return segments.toString();
    }

    private static String getSegment(String segString)
    {
        TuvImpl tuv = new TuvImpl();
        InternalTextUtil util = new InternalTextUtil(
                new EmbedOnlineInternalTag());
        try
        {
            segString = util.preProcessInternalText(segString).getSegment();
        }
        catch (DiplomatBasicParserException e)
        {
            s_logger.error(e.getMessage(), e);
        }
        tuv.setSegmentString(segString);
        List subflows = tuv.getSubflowsAsGxmlElements();
        StringBuffer seg = new StringBuffer();
        seg.append(GxmlUtil.getDisplayHtml(tuv.getGxmlElement(), "html", 3).trim());
        String subSeg;
        for (int i = 0; i < subflows.size(); i++)
        {
            GxmlElement subElmt = (GxmlElement) subflows.get(i);
            subSeg = GxmlUtil.getDisplayHtml(subElmt, "html", 3);
            seg.append("\n");
            seg.append(subSeg);
        }

        return seg.toString();
    }

}
