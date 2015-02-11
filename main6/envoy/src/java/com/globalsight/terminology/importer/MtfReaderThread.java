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

package com.globalsight.terminology.importer;

import org.apache.log4j.Logger;

import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;

import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;


import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;

/**
 * Reads MultiTerm MTF files and produces Entry objects by putting
 * ReaderResult objects into a ReaderResultQueue.
 */
public class MtfReaderThread
    extends Thread
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            MtfReaderThread.class);

    private ReaderResultQueue m_results;
    private ReaderResult m_result = null;
    private ImportOptions m_options;
    private Termbase m_termbase;
    private DocumentFactory m_factory;
    private int m_count = 0;

    //
    // Constructor
    //
    public MtfReaderThread (ReaderResultQueue p_queue,
        ImportOptions p_options, Termbase p_termbase)
    {
        m_results = p_queue;
        m_options = p_options;
        m_termbase = p_termbase;
        m_factory = new DocumentFactory();
    }

    //
    // Thread methods
    //
    public void run()
    {
        try
        {
            SAXReader reader = new SAXReader();
            reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

            // enable pruning to call me back as each Element is complete
            reader.addHandler("/mtf/conceptGrp",
                new ElementHandler ()
                    {
                        public void onStart(ElementPath path)
                        {
                            m_count++;
                        }

                        public void onEnd(ElementPath path)
                        {
                            Element element = path.getCurrent();

                            // prune the current element to reduce memory
                            element.detach();

                            m_result = m_results.hireResult();

                            try
                            {
                                // Convert MultiTerm to GlobalSight.
                                element = convertMtf(element);

                                Document doc =
                                    m_factory.createDocument(element);
                                Entry entry = new Entry(doc);

                                if (CATEGORY.isDebugEnabled())
                                {
                                    CATEGORY.debug(entry.getXml());
                                }

                                m_result.setResultObject(entry);
                            }
                            catch (Throwable ex)
                            {
                                String msg = "Entry " + m_count + ": " +
                                    ex.getMessage();

                                m_result.setError(msg);

                                if (CATEGORY.isDebugEnabled())
                                {
                                    CATEGORY.debug(msg, ex);
                                }
                                else
                                {
                                    CATEGORY.warn(msg, ex);
                                }
                            }

                            boolean done = m_results.put(m_result);
                            m_result = null;

                            // Stop reading the XML file.
                            if (done)
                            {
                                throw new ThreadDeath();
                            }
                        }
                    }
                );

            String url = m_options.getFileName();

            Document document = reader.read(url);
        }
        catch (ThreadDeath ignore)
        {
            CATEGORY.info("ReaderThread: interrupted.");
        }
        catch (Throwable ignore)
        {
            // Should never happen, and I don't know how to handle
            // this case other than passing the exception in
            // m_results, which I won't do for now.
            CATEGORY.error("unexpected error", ignore);
        }
        finally
        {
            if (m_result != null)
            {
                m_results.fireResult(m_result);
                m_result = null;
            }

            m_results.producerDone();
            m_results = null;

            CATEGORY.debug("ReaderThread: done.");
        }
    }

    /**
     * Converts a MultiTerm MTF concept group to a GlobalSight concept
     * group. Differences:
     *
     *  - <system type="entryClass|status"> -->
     *    <descrip type="entryClass|status">
     *
     *  - <language type="English" lang="EN" /> -->
     *    <language name="English" locale="en_US" />
     *
     *  - <descripGrp><descrip type="note"> -->
     *    <noteGrp><note>
     *
     *  - <descripGrp><descrip type="source"> -->
     *    <sourceGrp><source>
     *
     *  - descripGrp is recursive, must map to noteGrps or delete.
     *
     *  - remove empty descripGrp <descrip type="Graphic"/>
     */
    private Element convertMtf(Element p_elem)
    {
        List nodes;
        Node node;
        Element elem;
        Iterator it;
        ListIterator lit;

        // fix <system>
        nodes = p_elem.elements("system");
        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();
            p_elem.remove(elem);
            p_elem.addElement("descrip").
                addAttribute("type", elem.attributeValue("type")).
                addText(elem.getText());
        }

        // fix Graphic; we cannot handle them, so remove them
        nodes = p_elem.selectNodes("descripGrp[descrip/@type='Graphic']");
        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();
            p_elem.remove(elem);
        }

        // convert <descripGrp><descrip type="note"> to noteGrp
        nodes = p_elem.selectNodes(".//descripGrp[descrip/@type='note']");
        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();

            convertToNoteGrp(elem);
        }

        // convert <descripGrp><descrip type="source"> to sourceGrp
        nodes = p_elem.selectNodes(".//descripGrp[descrip/@type='source']");
        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();

            convertToSourceGrp(elem);
        }

        // Convert recursive descripGrps to noteGrps if possible.
        convertRecursiveDescripGrps(p_elem, ".//conceptGrp/descripGrp");
        convertRecursiveDescripGrps(p_elem, ".//languageGrp/descripGrp");
        convertRecursiveDescripGrps(p_elem, ".//termGrp/descripGrp");

        // Remove the recursive descripGrps that are left over.
        // (In case there are doubly recursive descrips and stuff.)
        removeRecursiveDescripGrps(p_elem);

        // fix <language>
        nodes = p_elem.selectNodes("languageGrp/language");

        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();

            Attribute nameAttr = elem.attribute("type");
            Attribute langAttr = elem.attribute("lang");

            String langName = nameAttr.getValue();
            String langLocale = langAttr.getValue();

            // locales in entries consist of 2 letter codes.
            langLocale = langLocale.substring(0,2).toLowerCase();

            elem.remove(nameAttr);
            elem.remove(langAttr);

            elem.addAttribute("name", langName);
            elem.addAttribute("locale", langLocale);
        }

        return p_elem;
    }

    private void convertToNoteGrp(Element p_elem)
    {
        Element noteGrp = p_elem.createCopy("noteGrp");

        // noteGrps contain no other fields, remove all child
        // nodes, remembering the <descrip type="note"> itself
        for (ListIterator lit = noteGrp.elements().listIterator();
             lit.hasNext(); )
        {
            Element child = (Element)lit.next();

            if (child.getName().equals("descrip"))
            {
                Element note = child.createCopy("note");
                note.remove(note.attribute("type"));

                lit.set(note);
            }
            else
            {
                lit.remove();
            }
        }

        Element parent = p_elem.getParent();
        parent.content().set(parent.indexOf(p_elem), noteGrp);
    }

    private void convertToSourceGrp(Element p_elem)
    {
        Element sourceGrp = p_elem.createCopy("sourceGrp");

        // sourceGrp contains noteGrp, remove all non-noteGrp
        // children, remembering the <descrip type="source"> itself
        for (ListIterator lit = sourceGrp.elements().listIterator();
             lit.hasNext(); )
        {
            Element child = (Element)lit.next();

            if (child.getName().equals("descrip"))
            {
                Element source = child.createCopy("source");
                source.remove(source.attribute("type"));

                lit.set(source);
            }
            else if (!child.getName().equals("noteGrp"))
            {
                lit.remove();
            }
        }

        Element parent = p_elem.getParent();
        parent.content().set(parent.indexOf(p_elem), sourceGrp);
    }

    private void convertRecursiveDescripGrps(Element p_elem, String p_path)
    {
        List nodes = p_elem.selectNodes(p_path);
        for (Iterator it = nodes.iterator(); it.hasNext(); )
        {
            Element descripGrp = (Element)it.next();

            for (ListIterator lit = descripGrp.elements().listIterator();
                 lit.hasNext(); )
            {
                Element elem = (Element)lit.next();

                if (elem.getName().equals("descripGrp"))
                {
                    convertToNoteGrp(elem);
                }
            }
        }
    }

    private void removeRecursiveDescripGrps(Element p_elem)
    {
        List nodes = p_elem.selectNodes(".//descripGrp[descripGrp]");
        for (Iterator it = nodes.iterator(); it.hasNext(); )
        {
            Element descripGrp = (Element)it.next();

            for (ListIterator lit = descripGrp.elements().listIterator();
                 lit.hasNext(); )
            {
                Element elem = (Element)lit.next();

                if (elem.getName().equals("descripGrp"))
                {
                    elem.detach();
                }
            }
        }
    }
}
