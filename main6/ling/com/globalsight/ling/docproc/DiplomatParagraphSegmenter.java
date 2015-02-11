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

import com.globalsight.ling.docproc.DiplomatReaderException;
import com.globalsight.ling.docproc.DiplomatSegmenterException;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.common.DiplomatNames;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

/**
 * Takes an unsegmented DiplomatXml input and produces a
 * paragraph-segmented version.  Basically all we do is wrap
 * <segment> tags around the content of each <translatable>.
*/
public class DiplomatParagraphSegmenter
{
    //
    // Private Variables
    //

    private String m_diplomat = null;
    private Output m_output = null;

    //
    // Constructor
    //

    public DiplomatParagraphSegmenter()
    {
    }

    //
    // Public Methods
    //

    /**
     * Treat tags representing whitespace as white during segmentation.
     */
    public void setPreserveWhitespace(boolean p_flag)
    {
        // noop
    }

    /**
     * <p>Takes Diplomat input from an <code>Output</code> object,
     * segments each "TranslatableElement" node and keeps the result
     * in an internal <code>Output</code>object.
     *
     * <p>The result can be retrieved as the Output object itself
     * (<code>getOutput()</code>), or as string
     * (<code>getDiplomatXml()</code>).
     */
    public void segment(Output p_diplomat)
        throws DiplomatSegmenterException
    {
        m_diplomat = null;
        m_output = p_diplomat;

        doParagraphSegmentation();
    }

    /**
     * Takes Diplomat input from a string.  Converts it to our
     * Diplomat internal data structure and then performs segmentation
     * on each "TranslatableElement" node.
     */
    public String segment(String p_diplomat)
        throws DiplomatSegmenterException
    {
        m_diplomat = p_diplomat;

        try
        {
            convertToOutput();
        }
        catch (DiplomatReaderException e)
        {
            throw new DiplomatSegmenterException(
                DiplomatSegmenterExceptionConstants.SEGMENTER_ERROR, e);
        }

        doParagraphSegmentation();

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
     * Returns the segmented Diplomat XML as <code>Output</code>
     * object.
     */
    public Output getOutput()
    {
        return m_output;
    }

    //
    // Private Methods
    //

    private void convertToOutput()
        throws DiplomatReaderException
    {
        DiplomatReader m_diplomatReader = new DiplomatReader(m_diplomat);
        m_output = m_diplomatReader.getOutput();
    }

    /**
     * Adds &lt;segment&gt; inside each &lt;translatable&gt;.
     */
    private void doParagraphSegmentation()
    {
        Iterator it = m_output.documentElementIterator();
        while (it.hasNext())
        {
            DocumentElement de = (DocumentElement)it.next();

            switch (de.type())
            {
            case DocumentElement.TRANSLATABLE:
            {
                TranslatableElement elem = (TranslatableElement)de;

                elem.addSegment(new SegmentNode(elem.getChunk()));

                elem.setChunk(null);
                break;
            }
            default:
                // skip all others
                break;
            }
        }
    }
}
