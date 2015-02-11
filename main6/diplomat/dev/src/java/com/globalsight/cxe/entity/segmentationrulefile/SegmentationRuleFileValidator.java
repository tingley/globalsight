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
package com.globalsight.cxe.entity.segmentationrulefile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.ling.docproc.ExtractorException;
import com.sun.msv.verifier.jarv.TheFactoryImpl;

/**
 * Validate a segmentation rule file against its RELAX NG schema. A wrapper of
 * Sun Multi-Schema XML Validator.
 * 
 * Get more information from SegmentationRuleFileType.java
 */
public class SegmentationRuleFileValidator implements ErrorHandler,
        EntityResolver
{
    static private final Logger s_logger = Logger
            .getLogger(SegmentationRuleFileValidator.class);
    
    private static Schema SCHEMA = null;
    static
    {
        VerifierFactory factory = new TheFactoryImpl();
        InputSource in = new InputSource(
                SegmentationRuleFileValidator.class
                        .getResourceAsStream("/properties/SRX2.0.xsd"));
        try
        {
            SCHEMA = factory.compileSchema(in);
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
        }
    }

    private String m_error;

    public boolean validate(String p_ruleFile, int p_type)
            throws ExtractorException
    {
        // for Test
        // Schema m_ruleSchema = createRuleFileSchema();
        try
        {
            Verifier verifier = SCHEMA.newVerifier();
            verifier.setErrorHandler(this);
            verifier.setEntityResolver(this);
            InputSource in = new InputSource(new StringReader(p_ruleFile));

            if (verifier.verify(in))
            {
                return true;
            }
        }
        catch (SAXParseException e)
        {
            m_error = "The segmentation rule file is not Valid. Line "
                    + e.getLineNumber() + " column " + e.getColumnNumber()
                    + ": " + e.getMessage();
        }
        catch (Exception ee)
        {
            throw new ExtractorException(ee);
        }

        return false;
    }

    public String getErrorMessage()
    {
        return m_error;
    }

    // ErrorHandler methods

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void error(SAXParseException e) throws SAXException
    {
        throw e;
    }

    public void warning(SAXParseException e)
    {
        // ignore warnings
    }

    /**
     * Overrides EntityResolver#resolveEntity.
     * 
     * Reference to external entities are not allowed in rule files.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

}
