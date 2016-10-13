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
package com.globalsight.ling.docproc.extractor.rc;

import java.util.Iterator;

import com.globalsight.ling.common.JSEscapeSequence;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;

/**
 * <P>
 * Implements IParseEvents and responds to the events fired by the Java parser.
 * </P>
 * 
 * <p>
 * The Java ExtractionHandler honors GSA comments (see the
 * {@link com.globalsight.ling.docproc.AbstractExtractor AbstractExtractor}
 * class) and lets extraction of strings be guided by them.
 * </p>
 */
public class ExtractionHandler implements IParseEvents
{
    //
    // Private Transient Member Variables
    //

    private Output m_output = null;
    private EFInputData m_input = null;
    private Extractor m_extractor = null;
    private boolean m_whiteToTranslatable = false;
    private XmlEntities m_xmlEncoder = new XmlEntities();

    /**
     * <p>
     * The encoder for skeleton inside embedded pieces. When an embedded
     * extractor writes skeleton, it outputs to a string that the parent
     * extractor adds to the content of a &lt;bpt&gt; tag - without further
     * escaping. So an embedded extractor must first use the parent's encoder to
     * escape special parent chars, and then call the standard XML encoder.
     * </p>
     */
    private NativeEnDecoder m_parentEncoder = null;

    //
    // Constructors
    //

    /**
     * <P>
     * Returns a new handler that knows about its input data, the output object,
     * and the extractor that created it.
     * <P>
     */
    public ExtractionHandler(EFInputData p_input, Output p_output,
            Extractor p_extractor) throws ExtractorException
    {
        super();

        m_input = p_input;
        m_output = p_output;
        m_extractor = p_extractor;
    }

    //
    // Interface Implementation -- IParseEvents
    //

    public void handleStart()
    {
    }

    public void handleFinish()
    {
    }

    public void addLocalizable(String s)
    {
        m_output.addTranslatable(s);
    }

    public void addSkeleton(String s)
    {
        m_output.addSkeleton(s);
    }

    /**
     * Adds a value of a string table.
     * 
     * @param s
     *            Should start and end with '"'.
     */
    public void addStringTableValue(String s)
    {
        addString(s);
    }

    private void addString(String s)
    {
        String content = s.substring(1, s.length() - 1);
        if (content.trim().length() == 0)
        {
            m_output.addSkeleton(s);
        }
        else
        {
            m_output.addSkeleton("\"");
            m_output.addTranslatable(content);
            m_output.addSkeleton("\"");
        }
    }
    
    @Override
    public void addCaption(String s)
    {
        addString(s);
    }

    @Override
    public void addAuto3stateText(String s)
    {
        addString(s);
    }

    @Override
    public void addAutoCheckBox(String s)
    {
        addString(s);
    }

    @Override
    public void addPushBox(String s)
    {
        addString(s);
    }

    @Override
    public void addState3Text(String s)
    {
        addString(s);
    }

    @Override
    public void addCheckBoxText(String s)
    {
        addString(s);
    }

    @Override
    public void addControlText(String s)
    {
        addString(s);
    }

    @Override
    public void addctext(String s)
    {
        addString(s);
    }

    @Override
    public void addDefPushButton(String s)
    {
        addString(s);
    }

    @Override
    public void addGroupBoxText(String s)
    {
        addString(s);
    }

    @Override
    public void addLtext(String s)
    {
        addString(s);
        
    }

    @Override
    public void addPushButtonText(String s)
    {
        addString(s);
    }

    @Override
    public void addRadioButtonText(String s)
    {
        addString(s);
    }

    @Override
    public void addRtext(String s)
    {
        addString(s);
    }

    @Override
    public void addMenuItemText(String s)
    {
        addString(s);
    }

    @Override
    public void addPopupText(String s)
    {
        addString(s);
    }

}
