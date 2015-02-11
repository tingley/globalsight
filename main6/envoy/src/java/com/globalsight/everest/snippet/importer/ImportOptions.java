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

package com.globalsight.everest.snippet.importer;

import org.apache.log4j.Logger;

import com.globalsight.importer.ImporterException;

// globalsight
import com.globalsight.util.edit.EditUtil;

// dom
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;


/**
 * This class collects all the options related to importing a snippet
 * file. There are two sets of options:
 *
 * (FileOptions - filename, type, encoding; inherited)
 *
 * and Synchronization Options determining how entries are added to
 * the database when duplicates exists.
 */
public class ImportOptions
    extends com.globalsight.importer.ImportOptions
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ImportOptions.class);

    // file types
    public static final String TYPE_UNKNOWN = "unknown";
    public static final String TYPE_HTML = "html";
    public static final String TYPE_XML = "xml";
    public static final String TYPE_CSV = "csv";

    //
    // Internal Classes
    //

    /**
     * Specifies the synchronization behavior during import, i.e. how
     * duplicates are detected, and how they are dealt with.
     */
    public class SyncOptions
    {
        /** One of "add_as_new", "conceptid", "language" */
        public String m_syncMode = "";

        /** If "language", this is the language name */
        public String m_syncLanguage = "";

        /** One of "overwrite", "merge", "discard" */
        public String m_syncAction = "";

        public String asXML()
        {
            StringBuffer result = new StringBuffer(128);

            result.append("<syncOptions>");
            result.append("<syncMode>");
            result.append(EditUtil.encodeXmlEntities(m_syncMode));
            result.append("</syncMode>");
            result.append("<syncLanguage>");
            result.append(EditUtil.encodeXmlEntities(m_syncLanguage));
            result.append("</syncLanguage>");
            result.append("<syncAction>");
            result.append(EditUtil.encodeXmlEntities(m_syncAction));
            result.append("</syncAction>");
            result.append("</syncOptions>");

            return result.toString();
        }
    }

    //
    // Private Members
    //
    private SyncOptions m_syncOptions = new SyncOptions();

    //
    // Constructors
    //

    //
    // Overwritten Abstract Methods
    //

    /**
     * Returns an ImportOptions object XML string.  For easy post
     * processing in Java make sure to not use any white space or
     * newlines.
     */
    public String getOtherXml()
    {
        return m_syncOptions.asXML();
    }

    /**
     * Reads and validates a ImportOptions XML string.
     *
     * Xml Format:
     */
    public void initOther(Element p_root)
        throws ImporterException
    {
        try
        {
            Node node = p_root.selectSingleNode("//syncOptions");

            m_syncOptions.m_syncMode = node.valueOf("syncMode");
            m_syncOptions.m_syncLanguage = node.valueOf("syncLanguage");
            m_syncOptions.m_syncAction = node.valueOf("syncAction");
        }
        catch (Exception e)
        {
            // cast exception and throw
            error(e.getMessage(), e);
        }
    }
}

