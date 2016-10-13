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
package com.globalsight.cxe.adapter.documentum;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The <code>ParseFileAttrXml</code> class used to parse a XML string into a set of
 * FileAttr objects, and save them into a HashMap(key=attrName, value=FileAttr).
 *  
 */
public class FileAttrXmlParser {

    public static Map parseFileAttrXml(InputStream fileAttrXmlStream)
        throws IOException {
        
        Map fileAtrrMap = new HashMap();

        try {
            XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
            parser.setContentHandler(new FileAttrXMLHandler(fileAtrrMap));
            if (fileAttrXmlStream != null) {
                parser.parse(new InputSource(fileAttrXmlStream));
            }
        } catch (SAXException saxEx) {
            System.err.println("Failed to parse file attribute xml file.");
            saxEx.printStackTrace();
        }

        return fileAtrrMap;
    }
}

/**
 * The <code>FileAttrXMLHandler</code> class describes how to parse the XML string, it's necessary to specify
 * a Handler for SAX to parse xml string.
 * 
 */
class FileAttrXMLHandler extends DefaultHandler {
    
    private Map fileAttrMap = null;
    private FileAttr fileAttr= null;
    
    private boolean isFileAttr = false;
    private boolean isValue = false;
    
    private String name = null;
    private Integer datatype = null;
    private Boolean isRepeating = null;
    private String value = null;

    private static final String FILEATTR_TAG_STR = "fileattr";
    private static final String NAME_TAG_ATTR = "name";
    private static final String DATATYPE_TAG_ATTR = "datatype";
    private static final String REPEAT_TAG_ATTR = "isrepeating";

    private static final String ATTRVALUE_TAG_STR = "value";

    public FileAttrXMLHandler(Map fileAttrMap) {
        this.fileAttrMap = fileAttrMap;
    }

    public void startElement(String nsURI, String strippedName,
            String tagName, Attributes attributes) throws SAXException {
        
        if (tagName.equalsIgnoreCase(FILEATTR_TAG_STR)) {
            
            name = attributes.getValue(NAME_TAG_ATTR);
            datatype =Integer.valueOf(attributes.getValue(DATATYPE_TAG_ATTR));
            isRepeating = Boolean.valueOf(attributes.getValue(REPEAT_TAG_ATTR));
            
            isFileAttr = true;
        } else if (tagName.equalsIgnoreCase(ATTRVALUE_TAG_STR)) {
            isValue = true;
        }
    }

    public void characters(char[] ch, int start, int length) {

        if (isFileAttr) {

            fileAttr = new FileAttr();
            fileAttr.setName(name);
            fileAttr.setDatatype(datatype.intValue());
            fileAttr.setRepeating(isRepeating.booleanValue());
            fileAttrMap.put(name, fileAttr);

            isFileAttr = false;
        } else if (isValue) {
            value = new String(ch, start, length);
            fileAttr.addValue(value);
            isValue = false;
        } 
    }
    
}