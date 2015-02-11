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

package com.globalsight.everest.coti.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.ling.docproc.extractor.xml.GsDOMParser;
import com.globalsight.util.GlobalSightLocale;

/**
 * COTI.XML parser class
 * 
 * @author Wayzou
 * 
 */
public class COTIXmlParseHandler implements EntityResolver, ErrorHandler
{

    /**
     * Overrides EntityResolver#resolveEntity.
     * 
     * The purpose of this method is to read Schemarules.dtd from resource and
     * feed it to the validating parser, but what it really does is returning a
     * null byte array to the XML parser.
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException
    {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    // ErrorHandler interface methods

    public void error(SAXParseException e) throws SAXException
    {
        String s = e.getMessage();
        // ignore below errors
        if (s.matches("Attribute .*? was already specified for element[\\s\\S]*"))
        {
            return;
        }

        throw new SAXException("XML parse error at\n  line "
                + e.getLineNumber() + "\n  column " + e.getColumnNumber()
                + "\n  Message:" + e.getMessage());
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
        error(e);
    }

    public void warning(SAXParseException e)
    {
        System.err.println("XML parse warning at\n  line " + e.getLineNumber()
                + "\n  column " + e.getColumnNumber() + "\n  Message:"
                + e.getMessage());
    }
    
}
