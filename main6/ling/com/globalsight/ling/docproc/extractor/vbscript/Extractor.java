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
package com.globalsight.ling.docproc.extractor.vbscript;

import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.NativeEnDecoder;
import com.globalsight.ling.common.NativeEnDecoderException;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistryException;

public class Extractor
    extends AbstractExtractor
{
    //
    // Private Member Variables
    //

    private XmlEntities m_xmlEncoder = new XmlEntities();
    private NativeEnDecoder m_parentEncoder = null;

    //
    // Constructor
    //

    public Extractor()
    {
        super();
    }

    //
    // Public Methods
    //

    public void loadRules()
        throws ExtractorException
    {
    }

    /**
     * Start the extraction process.
     */
    public void extract()
        throws ExtractorException
    {
        if (isEmbedded())
        {
            m_parentEncoder = getEncoder(getParentType());
            embExtract();
        }
        else
        {
            stdExtract();
        }
    }

    /**
     * Standard Extraction of VBScript.
     * @author Thierry Sourbier
     */
    public void stdExtract()
        throws ExtractorException
    {
        Parser parser = new Parser(readInput());
        VBToken token = parser.getNextToken();

        while (token.m_nType != VBToken.EOF)
        {
            int len = token.m_strContent.length();

            if (token.m_nType == VBToken.STRING && len > 2 && !exclude())
            {
                // TODO: check for <. If found in the string, call the
                // HTML parser.
                getOutput().addSkeleton(token.m_strContent.substring(0, 1));

                getOutput().addTranslatable(
                    token.m_strContent.substring(1, len - 1));

                try
                {
                    getOutput().setTranslatableAttrs(
                        ExtractorRegistry.FORMAT_VBSCRIPT, "string");
                }
                catch (DocumentElementException ignore)
                {
                }

                getOutput().addSkeleton(token.m_strContent.substring(len - 1));
            }
            else
            {
                if (token.m_nType == VBToken.COMMENT)
                {
                    // skip comment character (')
                    readMetaMarkup(token.m_strContent.substring(1));
                }

                getOutput().addSkeleton(token.m_strContent);
            }

            token = parser.getNextToken();
        }
    }

    public void embExtract()
        throws ExtractorException
    {
        Parser parser = new Parser(readInput());

        // TODO: replace this SAX like look and hook in the real parser
        VBToken token = parser.getNextToken();

        while (token.m_nType != VBToken.EOF)
        {
            int len = token.m_strContent.length();

            if (token.m_nType == VBToken.STRING && len > 2 && !exclude())
            {
                addToEmbeddedString(treatEmbEntities(
                    token.m_strContent.substring(0, 1)));

                addToEmbeddedString("<sub locType=\"translatable\"");
                addToEmbeddedString(" datatype=\"");
                addToEmbeddedString(ExtractorRegistry.FORMAT_JAVASCRIPT);
                addToEmbeddedString("\" type=\"string\">");

                addToEmbeddedString(treatEmbEntities(
                    token.m_strContent.substring(1, len - 1)));

                addToEmbeddedString("</sub>");

                addToEmbeddedString(treatEmbEntities(
                    token.m_strContent.substring(len - 1)));
            }
            else
            {
                if (token.m_nType == VBToken.COMMENT)
                {
                    // skip comment character (')
                    readMetaMarkup(token.m_strContent.substring(1));
                }

                addToEmbeddedString(treatEmbEntities(token.m_strContent));
            }

            token = parser.getNextToken();
        }
    }

    //
    // Private Methods
    //

    private String treatStdEntities(String s)
    {
        return m_xmlEncoder.encodeStringBasic(s);
    }

    private String treatEmbEntities(String s)
    {
        String result = s;

        try
        {
            result = treatStdEntities(m_parentEncoder.encode(s));
        }
        catch (NativeEnDecoderException ex)
        {
            // TODO: ignore for now
            // ex.printStackTrace();
        }

        return result;
    }
}
