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

package com.globalsight.terminology.indexer;

import org.apache.log4j.Logger;

import com.globalsight.terminology.indexer.IndexObject;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.EntryUtils;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.importer.ImportUtil;
import com.globalsight.terminology.util.XmlParser;

import com.globalsight.ling.lucene.Index;


import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;
import java.io.IOException;

/**
 * Adds termbase entries to the fuzzy and fulltext indexes defined on
 * the termbase.
 */
public class Writer
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            Writer.class);

    private Index m_index;

    //
    // Constructors
    //
    private Termbase m_termbase = null;

    public Writer (Termbase p_termbase)
    {
        m_termbase = p_termbase;
    }

    //
    // Public Methods
    //

    /**
     * Prepares the index for batch-add (re-creation).
     */
    public void startBatchIndexing(Index p_index)
        throws IOException
    {
        m_index = p_index;

        m_index.close();
        m_index.batchOpen();
    }

    /**
     * Ends re-creation of the index by persisting all changes and
     * re-opening the index.
     */
    public void endIndex()
        throws IOException
    {
        if (m_index != null)
        {
            Index temp = m_index;
            m_index = null;
            temp.batchDone();
        }
    }

    /**
     * Adds a list of XML fragments to a full text index.
     *
     * @param p_entries ArrayList of IndexObjects.
     */
    public void indexXml(ArrayList p_entries)
        throws IOException
    {
        ArrayList cache = new ArrayList();

        for (int i = 0; i < p_entries.size(); ++i)
        {
            indexXml(p_entries.get(i), cache);
        }
    }

    /**
     * Adds a list of terms to a fuzzy index.
     *
     * @param p_entries ArrayList of IndexObjects.
     */
    public void indexTerms(ArrayList p_entries)
        throws IOException
    {
        for (int i = 0, max = p_entries.size(); i < max; ++i)
        {
            indexTerm(p_entries.get(i));
        }
    }

    /**
     * Adds text fields in a single XML fragment to a fulltext index.
     *
     * @param p_object an IndexObject containing an XML string
     * representing an entry fragment.
     * @see Entry
     */
    public void indexXml(Object p_object, ArrayList p_texts)
        throws IOException
    {
        IndexObject object = (IndexObject)p_object;

        long cid = object.m_cid;
        long tid = object.m_tid;
        String xml = object.m_text;

        if (xml == null || xml.length() == 0)
        {
            return;
        }

        try
        {
            Document dom = parseFragment(xml);
            Element root = dom.getRootElement();

            p_texts.clear();
            getIndexableText(root, p_texts);

            for (int i = 0, max = p_texts.size(); i < max; i++)
            {
                String text = (String)p_texts.get(i);

                m_index.batchAddDocument(cid, tid, text);
            }
        }
        catch (IOException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    /**
     * Adds terms to a fuzzy index.
     *
     * @param p_object an IndexObject containing a term.
     * @see Entry
     */
    public void indexTerm(Object p_object)
        throws IOException
    {
        IndexObject object = (IndexObject)p_object;

        long cid = object.m_cid;
        long tid = object.m_tid;
        String term = object.m_text;

        m_index.batchAddDocument(cid, tid, term);
    }

    //
    // Private Methods
    //

    private Document parseFragment(String p_xml)
        throws IOException
    {
        // remove the illegal character such as "x0x"
        p_xml = ImportUtil.removeIllegalXmlChar(p_xml);
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseXml("<mlah>" + p_xml + "</mlah>");
        }
        catch (Exception ex)
        {
            throw new IOException(ex.getMessage());
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    private void getIndexableText(Element p_root, ArrayList p_result)
    {
        if (p_root == null || p_root.nodeCount() == 0)
        {
            return;
        }

        List elements = p_root.elements();

        for (int i = 0, max = elements.size(); i < max; i++)
        {
            Element elem = (Element)elements.get(i);
            String name = elem.getName();

            Element field = null;
            String type = null;

            if (name.equals("descripGrp"))
            {
                getIndexableText(elem, p_result);
            }
            else if (name.equals("sourceGrp"))
            {
                getIndexableText(elem, p_result);
            }
            else if (name.equals("noteGrp"))
            {
                getIndexableText(elem, p_result);
            }
            else if (name.equals("descrip"))
            {
                field = elem;
                type = field.attributeValue("type");
            }
            else if (name.equals("source"))
            {
                field = elem;
                type = "source";
            }
            else if (name.equals("note"))
            {
                field = elem;
                type = "note";
            }

            if (field == null || type == null)
            {
                continue;
            }

            if (isIndexableField(type))
            {
                p_result.add(EntryUtils.getInnerText(field));
            }
        }
    }

    // Fri Nov 12 02:32:18 2004 CvdL
    //
    // This method should use the termbase definition exclusively. The
    // definition must include all default fields (definition, pos
    // etc) and specify whether they are indexed or not. Custom fields
    // must also have the "indexed" flag set. For now we hardcode the
    // usual suspects and index user-defined text fields.
    //
    // The correct implementation will require UI changes.
    //
    private boolean isIndexableField(String p_type)
    {
        // This if-then must go away, use definition.
        if (p_type.equals("definition") ||
            p_type.equals("context") ||
            p_type.equals("example") ||
            p_type.equals("source") ||
            p_type.equals("note"))
        {
            return true;
        }

        // The definition should maintain the "indexed" property
        // of custom fields but as life goes, it doesn't. The UI
        // does not allow to set "is indexed" and the definition
        // manipulation code does not fill it in based on the
        // field type. But we know that custom text fields receive
        // the type "text-text", so let's dispatch on that.

        return p_type.startsWith("text");

        /*
        try
        {
            return m_termbase.isIndexedField(p_type);
        }
        catch (Exception ignore)
        {
            return false;
        }
        */
    }
}
