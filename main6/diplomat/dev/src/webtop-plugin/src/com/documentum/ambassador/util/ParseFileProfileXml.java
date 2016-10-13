/* Copyright (c) 2004-2005, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
package com.documentum.ambassador.util;

import java.io.IOException;
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
 * The <code>ParseFileProfileXml</code> class used to parse a XML string into
 * a set of File Profile objects, and save them into a HashMap(key=fpId,
 * value=FileProfile).
 * 
 */
public class ParseFileProfileXml
{

    public static Map parseFPXml(String p_file_str) throws SAXException,
            IOException
    {

        XMLReader parser = XMLReaderFactory.createXMLReader();
        Map fileProfileMap = new HashMap();
        parser.setContentHandler(new HowToHandler(fileProfileMap));
        if (p_file_str != null)
        {
            parser.parse(new InputSource((new StringReader(p_file_str))));
            return fileProfileMap;
        }
        else
        {
            return null;
        }
    }
}

/**
 * The <code>HowToHandler</code> class describes how to parse the XML string,
 * it's necessary to specify a Handler for SAX to parse xml string.
 * 
 */
class HowToHandler extends DefaultHandler
{

    private static final String FILEPROFILE_TAG_STR = "fileProfile";

    private static final String ID_TAG_STR = "id";

    private static final String NAME_TAG_STR = "name";

    private static final String SOURCELOCALE_TAG_STR = "sourceLocale";

    private static final String DESC_TAG_STR = "description";

    private static final String FILEEXTENSION_TAG_STR = "fileExtension";

    private static final String TARGETLOCALE_TAG_STR = "targetLocale";

    private Map fpMap = null;
    private FileProfile fileProfile = null;

    private boolean isFileProfile = false;
    private boolean isId = false;
    private boolean isName = false;
    private boolean isSourceLocale = false;
    private boolean isDesc = false;
    private boolean isFileExtension = false;
    private boolean isTargetLocale = false;

    private String id = null;
    private String name = null;
    private String sourceLocale = null;
    private String description = null;
    private String fileExtension = null;
    private String targetLocale = null;

    public HowToHandler(Map fpMap)
    {
        this.fpMap = fpMap;
    }

    public void startElement(String nsURI, String strippedName, String tagName,
            Attributes attributes) throws SAXException
    {

        if (tagName.equalsIgnoreCase(FILEPROFILE_TAG_STR))
        {
            isFileProfile = true;
        }
        else if (tagName.equalsIgnoreCase(ID_TAG_STR))
        {
            isId = true;
        }
        else if (tagName.equalsIgnoreCase(NAME_TAG_STR))
        {
            isName = true;
        }
        else if (tagName.equalsIgnoreCase(DESC_TAG_STR))
        {
            isDesc = true;
        }
        else if (tagName.equalsIgnoreCase(FILEEXTENSION_TAG_STR))
        {
            isFileExtension = true;
        }
        else if (tagName.equalsIgnoreCase(SOURCELOCALE_TAG_STR))
        {
            isSourceLocale = true;
        }
        else if (tagName.equalsIgnoreCase(TARGETLOCALE_TAG_STR))
        {
            isTargetLocale = true;
        }
    }

    public void characters(char[] ch, int start, int length)
    {

        if (isFileProfile)
        {
            fileProfile = new FileProfile();
            isFileProfile = false;
        }
        else if (isId)
        {
            id = new String(ch, start, length);
            fileProfile.setId(id);
            fpMap.put(id, fileProfile);
            isId = false;
        }
        else if (isName)
        {
            name = new String(ch, start, length);
            if (fileProfile != null)
            {
                fileProfile.setName(name);
            }
            isName = false;
        }
        else if (isDesc)
        {
            description = new String(ch, start, length);
            fileProfile.setDescription(description);
            isDesc = false;
        }
        else if (isFileExtension)
        {
            fileExtension = new String(ch, start, length);
            fileProfile.addFileExtension(fileExtension);
            isFileExtension = false;
        }
        else if (isSourceLocale)
        {
            sourceLocale = new String(ch, start, length);
            fileProfile.setSourceLocale(sourceLocale);
            isSourceLocale = false;
        }
        else if (isTargetLocale)
        {
            targetLocale = new String(ch, start, length);
            fileProfile.addTargetLocale(targetLocale);
            isTargetLocale = false;
        }
    }
}