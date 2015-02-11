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

package com.globalsight.everest.aligner;

import org.apache.log4j.Logger;

import com.globalsight.util.XmlParser;
import com.globalsight.util.edit.EditUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;
import java.io.Serializable;

/**
 * This class collects all the options related to uploading an Aligner
 * Package.  There are the following sets of options:
 *
 * TM Options - TM name to import into, merge settings
 */
public class AlignerPackageUploadOptions
    implements Serializable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            AlignerPackageUploadOptions.class);

    /**
     * How to deal with duplicate TUs: merge file data into TM data,
     * overwrite TM data from file content (think of added/removed
     * TUVs), or discard file TU.
     */
    public static final String SYNC_OVERWRITE = "overwrite";
    public static final String SYNC_MERGE = "merge";
    public static final String SYNC_DISCARD = "discard";

    //
    // Private Classes
    //

    /**
     * FileOptions describes the file name, file type, and primary
     * file encoding.
     */
    public class FileOptions
    {
        /** The file name of the file to be imported. */
        public String m_fileName = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<fileOptions>");
            result.append("<fileName>");
            result.append(EditUtil.encodeXmlEntities(m_fileName));
            result.append("</fileName>");
            result.append("</fileOptions>");

            return result.toString();
        }
    }

    /**
     * Specifies the options needed to upload and import aligned files
     * into a TM: TM name, merge settings.
     */
    public class TmOptions
    {
        public String m_tmName = "";
        public String m_syncMode = SYNC_MERGE;

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<tmOptions>");
            result.append("<tmName>");
            result.append(EditUtil.encodeXmlEntities(m_tmName));
            result.append("</tmName>");
            result.append("<syncMode>");
            result.append(EditUtil.encodeXmlEntities(m_syncMode));
            result.append("</syncMode>");
            result.append("</tmOptions>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private FileOptions m_fileOptions = new FileOptions();
    private TmOptions m_tmOptions = new TmOptions();

    //
    // Constructors
    //
    public AlignerPackageUploadOptions()
    {
        super();
    }

    //
    // Public Methods
    //

    public String getFileName()
    {
        return m_fileOptions.m_fileName;
    }

    public String getTmName()
    {
        return m_tmOptions.m_tmName;
    }

    public String getSyncMode()
    {
        return m_tmOptions.m_syncMode;
    }

    /**
     * Returns an AlignerPackageUploadOptions object XML string.  For
     * easy post processing in Java, make sure to not use any white
     * space or newlines.
     */
    public String getXml()
    {
        StringBuffer result = new StringBuffer(256);

        result.append("<alignerPackageUploadOptions>");

        result.append(m_fileOptions.asXML());
        result.append(m_tmOptions.asXML());

        result.append("</alignerPackageUploadOptions>");

        return result.toString();
    }

    /**
     * Initializes this object from an XML string.
     */
    public void init(String p_options)
        throws Exception
    {
        XmlParser parser = null;
        Document dom = null;
        Element elem;

        try
        {
            parser = XmlParser.hire();
            dom = parser.parseXml(p_options);

            Element root = dom.getRootElement();

            elem = (Element)root.selectSingleNode("//fileOptions");
            m_fileOptions.m_fileName = elem.elementText("fileName");

            elem = (Element)root.selectSingleNode("//tmOptions");
            m_tmOptions.m_tmName = elem.elementText("tmName");
            m_tmOptions.m_syncMode = elem.elementText("syncMode");
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }
}
