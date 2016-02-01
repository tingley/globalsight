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
package com.globalsight.ling.docproc;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.common.DiplomatBasicParser;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;

/**
 * <p>
 * Takes DiplomatXml input and inserts missing "x" attributes and replaces nbsp
 * characters by PH placeholders.
 * </p>
 * 
 * <p>
 * This step runs after segmentation (and includes code common in both
 * segmenters) and after word counting.
 * </p>
 */
public class DiplomatPostProcessor implements DiplomatBasicHandler
{
    /** State constant when parsing a top-level segment or subflow. */
    static private final Integer s_TEXT = new Integer(0);
    /** State constant when parsing a TMX tag BPT/EPT/PH/IT. */
    static private final Integer s_TAG = new Integer(1);

    // Private Variables

    private String m_diplomat = null;
    private Output m_output = null;

    private DiplomatBasicParser m_diplomatParser = null;
    private StringBuffer m_currentSegment = new StringBuffer();
    private int m_externalMatchingCount = 1;
    private Stack m_tmxStateStack = null;
    private String formatName = null;

    //
    // Constructor
    //

    public DiplomatPostProcessor()
    {
        super();

        m_diplomatParser = new DiplomatBasicParser(this);
    }

    //
    // Public Methods
    //

    /**
     * <p>
     * Takes Diplomat input from an <code>Output</code> object, inserts "x"
     * attributes and wraps nbsp. The result is kept in an internal
     * <code>Output</code>object.
     * 
     * <p>
     * The result can be retrieved as the Output object itself (
     * <code>getOutput()</code>), or as string (<code>getDiplomatXml()</code>).
     */
    public void postProcess(Output p_diplomat) throws ExtractorException
    {
        m_diplomat = null;
        m_output = p_diplomat;

        try
        {
            doPostProcess();
        }
        catch (Exception ex)
        {
            throw new ExtractorException(ExtractorExceptionConstants.DIPLOMAT_XML_PARSE_ERROR, ex);
        }
    }

    /**
     * Takes Diplomat input from a string. Converts it to our internal
     * <code>Output</code> data structure and then performs post processing.
     */
    public String postProcess(String p_diplomat) throws ExtractorException
    {
        m_diplomat = p_diplomat;

        try
        {
            convertToOutput();

            doPostProcess();
        }
        catch (Exception ex)
        {
            throw new ExtractorException(ExtractorExceptionConstants.DIPLOMAT_XML_PARSE_ERROR, ex);
        }

        return getDiplomatXml();
    }

    /**
     * Returns the segmented Diplomat XML as string.
     */
    public String getDiplomatXml()
    {
        String diplomatOut = DiplomatWriter.WriteXML(m_output);

        return diplomatOut;
    }

    /**
     * Returns the segmented Diplomat XML as <code>Output</code> object.
     */
    public Output getOutput()
    {
        return m_output;
    }

    //
    // Private Methods
    //

    private void convertToOutput() throws DiplomatReaderException
    {
        DiplomatReader m_diplomatReader = new DiplomatReader(m_diplomat);
        m_output = m_diplomatReader.getOutput();
    }

    /**
     * Inserts any missing "x" attributes and replaces nbsp with PH
     * placeholders.
     */
    private void doPostProcess() throws DiplomatBasicParserException
    {
        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            String translationChunk = null;
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;

                    m_externalMatchingCount = 1;

                    ArrayList segments = elem.getSegments();

                    for (int i = 0, max = segments.size(); i < max; i++)
                    {
                        SegmentNode node = (SegmentNode) segments.get(i);

                        updateSegmentInNode(node, node.getSegment());
                    }

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    LocalizableElement elem = (LocalizableElement) de;

                    m_externalMatchingCount = 1;

                    updateSegmentInNode(elem, elem.getChunk());
                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }
    }

    /**
     * Adds a new segment to the original translatable node, wraps nbsp in PH
     * and inserts an "x" attribute in all BPT/PH/IT.
     */
    private void updateSegmentInNode(SegmentNode p_element, String p_segment)
            throws DiplomatBasicParserException
    {
        if (p_segment != null)
        {
            String newSegment = addExternalMatchingAttributes(p_segment);

            p_element.setSegment(newSegment);
        }
    }

    /**
     * Updates the original localizable node by wrapping nbsp in PH and
     * inserting an "x" attribute in all BPT/PH/IT.
     */
    private void updateSegmentInNode(LocalizableElement p_element, String p_segment)
            throws DiplomatBasicParserException
    {
        if (p_segment != null)
        {
            String newSegment = addExternalMatchingAttributes(p_segment);

            p_element.setChunk(newSegment);
        }
    }

    /**
     * <p>
     * Wraps all nbsp characters in a TMX PH tag.
     * </p>
     */
    private String wrapNbsp(String p_text)
    {
        int len = p_text.length();
        StringBuffer result = new StringBuffer(len);

        for (int i = 0; i < len; i++)
        {
            char ch = p_text.charAt(i);

            if (ch == '\u00A0')
            {
                // Write out TMX in place of the \u00A0 char (with "x")
                result.append("<ph type=\"x-nbspace\" erasable=\"yes\" x=\"");
                result.append(m_externalMatchingCount);
                result.append("\">");
                if (isXmlFormat())
                {
                    // GBS-3577, GBS-4216
                    result.append("&amp;#160;");
                }
                else
                {
                    result.append("&amp;nbsp;");
                }
                result.append("</ph>");

                m_externalMatchingCount++;
            }
            else
            {
                result.append(ch);
            }
        }

        return result.toString();
    }

    private boolean isXmlFormat()
    {
        return IFormatNames.FORMAT_XML.equals(formatName)
                || IFormatNames.FORMAT_AUTHORIT_XML.equals(formatName);
    }

    /**
     * Splice in x attributes for bpt, it, ph and ut tags. Restart the "x" count
     * for each segment.
     */
    // A hack. I'm sure this could be done without reparsing
    // the segment all over again, but this is faster and much cleaner
    // to implement than regexes.
    // I'll think about using DiplomatBasicParser for other operations
    // that currently use regexes. Or maybe DOM will make it first.
    // - JEH
    private String addExternalMatchingAttributes(String p_segment)
            throws DiplomatBasicParserException
    {
        m_currentSegment.setLength(0);

        m_tmxStateStack = new Stack();
        m_tmxStateStack.push(s_TEXT);

        m_diplomatParser.parse(p_segment);

        return m_currentSegment.toString();
    }

    public void handleEndTag(String p_name, String p_originalTag)
            throws DiplomatBasicParserException
    {
        Integer state = (Integer) m_tmxStateStack.pop();

        m_currentSegment.append(p_originalTag);
    }

    public void handleStartTag(String p_name, Properties p_attributes, String p_originalString)
            throws DiplomatBasicParserException
    {
        // Add x to all tags except ept and sub.
        // If sub contains bpt,ph,it,ut we add x to them as well
        // but this case should be rare and not cause any ill side
        // effects. Might want to skip all sub child elements in
        // the future
        if (p_name.equals(DiplomatNames.Element.EPT))
        {
            m_tmxStateStack.push(s_TAG);

            m_currentSegment.append(p_originalString);
        }
        else if (p_name.equals(DiplomatNames.Element.SUB))
        {
            m_tmxStateStack.push(s_TEXT);

            m_currentSegment.append(p_originalString);
        }
        else
        // need to add x to the end
        {
            m_tmxStateStack.push(s_TAG);

            m_currentSegment.append('<');
            m_currentSegment.append(p_name);

            Enumeration attributeList = p_attributes.propertyNames();

            while (attributeList.hasMoreElements())
            {
                String key = (String) attributeList.nextElement();

                if (key.equals("x"))
                {
                    continue; // skip an existing x, we add it back later
                }

                String value = p_attributes.getProperty(key);

                m_currentSegment.append(' ');
                m_currentSegment.append(key);
                m_currentSegment.append("=\"");
                m_currentSegment.append(value);
                m_currentSegment.append('"');
            }

            // add x
            m_currentSegment.append(" x=\"");
            m_currentSegment.append(m_externalMatchingCount);
            m_currentSegment.append("\">");

            m_externalMatchingCount++;
        }
    }

    public void handleText(String p_text) throws DiplomatBasicParserException
    {
        Integer state = (Integer) m_tmxStateStack.peek();

        if (state == s_TEXT && !OfficeXmlHelper.OFFICE_XML.equalsIgnoreCase(formatName)
                && !OfficeContentPostFilterHelper.isOfficeFormat(formatName)
                && !IFormatNames.FORMAT_JAVAPROP.equalsIgnoreCase(formatName)
                && !IFormatNames.FORMAT_HTML.equalsIgnoreCase(formatName))
        {
            m_currentSegment.append(wrapNbsp(p_text));
        }
        else
        {
            m_currentSegment.append(p_text);
        }
    }

    public void handleStart() throws DiplomatBasicParserException
    {
    }

    public void handleStop() throws DiplomatBasicParserException
    {
    }

    public String getFormatName()
    {
        return formatName;
    }

    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
    }
}
