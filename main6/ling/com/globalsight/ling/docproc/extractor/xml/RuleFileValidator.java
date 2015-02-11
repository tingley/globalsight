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
package com.globalsight.ling.docproc.extractor.xml;

import com.sun.msv.verifier.jarv.TheFactoryImpl;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;

import java.io.StringReader;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.ResourceBundle;

import com.globalsight.ling.docproc.ExtractorException;

/**
 * Validate a XML rule file against its RELAX NG schema. A wrapper of
 * Sun Multi-Schema XML Validator.
 */
public class RuleFileValidator
    implements ErrorHandler, EntityResolver
{
    static final private Schema m_ruleSchema = createRuleFileSchema();
    
    static private Schema createRuleFileSchema()
    {
        VerifierFactory factory = new TheFactoryImpl();
        InputSource in = new InputSource(
            RuleFileValidator.class.getResourceAsStream("/properties/schemarules.rng"));
        Schema schema = null;
        try
        {
            schema = factory.compileSchema(in);
        }
        catch(Exception e)
        {
            throw new RuntimeException(e.toString());
        }
        return schema;
    }
    

    private String m_error;
    private ResourceBundle m_bundle;
    
    public boolean validate(String p_ruleFile)
        throws ExtractorException
    {
        try
        {
            Verifier verifier = m_ruleSchema.newVerifier();
            verifier.setErrorHandler(this);
            verifier.setEntityResolver(this);
            InputSource in = new InputSource(new StringReader(p_ruleFile));

            if(verifier.verify(in))
            {
                return true;
            }
        }
        catch(SAXParseException e)
        {
            String pattern = "The XML rule file is not Valid. Line {0} column {1}";
            if (m_bundle != null && m_bundle.containsKey("lb_xml_rule_validate_error_pattern"))
            {
                pattern = m_bundle.getString("lb_xml_rule_validate_error_pattern");
            }
            
            m_error = java.text.MessageFormat.format(pattern, e.getLineNumber(), e
                    .getColumnNumber())
                    + ": " + e.getMessage();
        }
        catch(Exception ee)
        {
            throw new ExtractorException(ee);
        }
        
        return false;
    }
    
    public void setResourceBundle(ResourceBundle p_bundle)
    {
        m_bundle = p_bundle;
    }

    public String getErrorMessage()
    {
        return m_error;
    }
    

    // ErrorHandler methods 

    public void fatalError(SAXParseException e)
        throws SAXException
    {
        error(e);
    }

    public void error(SAXParseException e)
        throws SAXException
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
