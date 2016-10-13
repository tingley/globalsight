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

import com.globalsight.ling.docproc.CtrlCharConverter;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

/**
 * <p>Takes DiplomatXml input and converts C0 control codes to PUA
 * characters to avoid XML parser errors.</p>
 *
 * <p>This step runs right after extraction to ensure correct input in
 * further steps that use XML parsers (like word counting).</p>
 */
public class DiplomatCtrlCharConverter
{
    // Private Variables

    private String m_diplomat = null;
    private Output m_output = null;

    //
    // Constructor
    //

    public DiplomatCtrlCharConverter()
    {
    }

    //
    // Public Methods
    //

    /**
     * <p>Takes Diplomat input from an <code>Output</code> object and
     * converts C0 control codes to PUA characters to avoid XML parser
     * errors.
     */
    public void convertChars(Output p_diplomat)
        throws ExtractorException
    {
        m_diplomat = null;
        m_output = p_diplomat;

        convertChars();
    }

    /**
     * Returns the converted Diplomat XML as <code>Output</code>
     * object.
     */
    public Output getOutput()
    {
        return m_output;
    }

    //
    // Private Methods
    //

    /**
     * Converts C0 control codes to PUA characters to avoid XML
     * parser errors.
     */
    private void convertChars()
    {
        Iterator it = m_output.documentElementIterator();
        while (it.hasNext())
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;

                    if (elem.hasSegments())
                    {
                        ArrayList segments = elem.getSegments();

                        for (int i = 0, max = segments.size(); i < max; i++)
                        {
                            SegmentNode node = (SegmentNode) segments.get(i);

                            convertChars(node, node.getSegment());
                        }
                    }
                    else
                    {
                        convertChars(elem, elem.getChunk());
                    }

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    LocalizableElement elem = (LocalizableElement) de;
                    convertChars(elem, elem.getChunk());
                    break;
                }
                case DocumentElement.SKELETON:
                {
                    SkeletonElement elem = (SkeletonElement) de;
                    convertChars(elem, elem.getSkeleton());
                    break;
                }
            }
        }
    }

    private void convertChars(SegmentNode p_element, String p_segment)
    {
        if (p_segment != null)
        {
            String newSegment = CtrlCharConverter.convertToPua(p_segment);

            p_element.setSegment(newSegment);
        }
    }

    private void convertChars(TranslatableElement p_element, String p_segment)
    {
        if (p_segment != null)
        {
            String newSegment = CtrlCharConverter.convertToPua(p_segment);

            p_element.setChunk(newSegment);
        }
    }

    private void convertChars(LocalizableElement p_element, String p_segment)
    {
        if (p_segment != null)
        {
            String newSegment = CtrlCharConverter.convertToPua(p_segment);

            p_element.setChunk(newSegment);
        }
    }

    private void convertChars(SkeletonElement p_element, String p_segment)
    {
        if (p_segment != null)
        {
            String newSegment = CtrlCharConverter.convertToPua(p_segment);

            p_element.setSkeleton(newSegment);
        }
    }
}

