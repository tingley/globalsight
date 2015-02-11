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

package com.globalsight.terminology.exporter;

import org.apache.log4j.Logger;

import com.globalsight.terminology.exporter.ExportUtil;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.exporter.IWriter;
import com.globalsight.exporter.ExportOptions;

import com.globalsight.terminology.Entry;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;

import com.globalsight.util.FileUtil;
import com.globalsight.util.SessionInfo;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;
import java.io.*;

/**
 * Writes an entry to a MultiTerm iX MTF file as directed by the
 * conversion settings in the supplied export options.
 */
public class MtfWriter
    implements IWriter, TermbaseExceptionMessages
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            MtfWriter.class);

    //
    // Private Member Variables
    //
    private com.globalsight.terminology.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;

    //
    // Constructors
    //

    public MtfWriter (ExportOptions p_options)
    {
        setExportOptions(p_options);
    }

    //
    // Interface Implementation -- IWriter
    //

    public void setExportOptions(ExportOptions p_options)
    {
        m_options = (com.globalsight.terminology.exporter.ExportOptions)
            p_options;
    }

    /**
     * Analyzes export options and returns an updated ExportOptions
     * object with a status whether the options are syntactically
     * correct, and column descriptors in case of CSV files.
     */
    public ExportOptions analyze()
    {
        return m_options;
    }

    /**
     * Writes the file header (eg for TBX).
     */
    public void writeHeader(SessionInfo p_session)
        throws IOException
    {
        m_filename = m_options.getFileName();

        //Export termbase to a folder with company specified.
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String directory = ExportUtil.getExportDirectory();
        if (!companyId.equals(CompanyWrapper.SUPER_COMPANY_ID))
        {
        	directory = 
        		directory + CompanyWrapper.getCompanyNameById(companyId) + "/";
        	new File(directory).mkdir();
        }
        String encoding = m_options.getJavaEncoding();

        if (encoding == null || encoding.length() == 0)
        {
            throw new IOException("invalid encoding " +
                m_options.getEncoding());
        }

        // We support only Unicode encodings for XML files: UTF-8 and
        // UTF-16 (little and big endian)
        String ianaEncoding = m_options.getEncoding();
        if (ianaEncoding.toUpperCase().startsWith("UTF-16"))
        {
            ianaEncoding = "UTF-16";
        }

        String filename = directory + m_filename;

        // XML files get written as Unicode (in UTF-8).
        FileOutputStream fos = new FileOutputStream(filename);
        FileUtil.writeBom(fos, ianaEncoding);
        m_output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                fos, encoding)));

        m_output.print("<?xml version=\"1.0\" encoding=\"");
        m_output.print(ianaEncoding);
        m_output.println("\" ?>");
        m_output.println("<mtf>");

        checkIOError();
    }

    /**
     * Writes the file trailer (eg for TBX).
     */
    public void writeTrailer(SessionInfo p_session)
        throws IOException
    {
        m_output.println("</mtf>");

        m_output.close();
    }

    /**
     * Writes the next few entries to the file.
     *
     * @param p_entries ArrayList of String objects that are the XML
     * representation of the entries.
     */
    public void write(ArrayList p_entries, SessionInfo p_session)
        throws IOException
    {
        for (int i = 0; i < p_entries.size(); ++i)
        {
            write(p_entries.get(i), p_session);
        }
    }

    /**
     * Writes a single entry to the export file.
     *
     * @param p_object an XML string representing the entry.
     * @see Entry
     */
    public void write(Object p_object, SessionInfo p_session)
        throws IOException
    {
        String entryXml = (String)p_object;
        entryXml = entryXml.replaceAll("<concept>[^<]*</concept>", "");

        try
        {
            Entry entry = new Entry(entryXml);

            Document dom = entry.getDom();

            // Do we filter out system fields?
            if (m_options.getSystemFields().equalsIgnoreCase("false"))
            {
                dom = ExportUtil.removeSystemFields(dom);
            }

            entry.setDom(convertToMtf(dom));

            String xml = entry.getXml();
            m_output.println(xml);

            checkIOError();
        }
        catch (TermbaseException ex)
        {
            throw new IOException("internal error: " + ex.toString());
        }
    }

    /**
     * Converts a GlobalSight concept group to a MultiTerm iX concept
     * group. Differences:
     *
     *  - concept level <descrip type="entryClass|status"> -->
     *    <system type="entryClass|status">
     *
     *  - <language name="English" locale="en_US" /> -->
     *    <language type="English" lang="EN" />
     *
     *  - <noteGrp><note> -->
     *    <descripGrp><descrip type="note"></descripGrp>
     *
     *  - <note> -->  (should not be produced but could be in old data)
     *    <descripGrp><descrip type="note"></descripGrp>
     *
     *  - <sourceGrp><source></sourceGrp> -->
     *    <descripGrp><descrip type="source"></descripGrp>
     *
     *  - descripGrp is not recursive
     */
    private Document convertToMtf(Document p_elem)
    {
        List nodes;
        Node node;
        Element root = p_elem.getRootElement();
        Element elem;
        Iterator it;
        ListIterator lit;

        if (false && CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("gt2mtf init: " + p_elem.asXML());
        }

        // rewrite <descrip type=entryClass> (only one on concept level)
        nodes = root.selectNodes("descrip[@type='entryClass']");
        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();
            Element parent = elem.getParent();
            parent.remove(elem);
            parent.addElement("system").
                addAttribute("type", "entryClass").
                addText(elem.getText());
        }

        // rewrite <descrip type=status> (?? used in MTF?)
        nodes = root.selectNodes("descrip[@type='status']");
        for (it = nodes.iterator(); it.hasNext(); )
           {
            elem = (Element)it.next();
            Element parent = elem.getParent();
            parent.remove(elem);
            parent.addElement("system").
                addAttribute("type", "status").
                addText(elem.getText());
        }

        // rewrite <noteGrp>
        while (true)
        {
            // refresh the node list, we're rewriting the structure
            node = root.selectSingleNode("//noteGrp");
            if (node == null)
            {
                break;
            }

            elem = (Element)node;

            Element parent = elem.getParent();
            parent.remove(elem);

            Element newNote = parent.addElement("descripGrp");
            Element note = null;

            // copy all child nodes but remember the <note>
            for (lit = elem.elements().listIterator(); lit.hasNext(); )
            {
                Element child = (Element)lit.next();

                if (child.getName().equals("note"))
                {
                    note = child;
                }
                else
                {
                    lit.remove();
                    newNote.add(child);
                }
            }

            // create new <descrip type="note"> with note's value
            newNote.addElement("descrip").
                addAttribute("type", "note").
                addText(note.getText());
        }

        // rewrite single <note>, if any are left in the entry
        while (true)
        {
            // refresh the node list, we're rewriting the structure
            node = root.selectSingleNode("//note");
            if (node == null)
            {
                break;
            }

            Element note = (Element)node;

            Element parent = note.getParent();
            parent.remove(note);

            Element newNote = parent.addElement("descripGrp");
            newNote.addElement("descrip").
                addAttribute("type", "note").
                addText(note.getText());
        }

        // rewrite <sourceGrp>
        while (true)
        {
            // refresh the node list, we're rewriting the structure
            node = root.selectSingleNode("//sourceGrp");
            if (node == null)
            {
                break;
            }

            elem = (Element)node;

            Element parent = elem.getParent();
            parent.remove(elem);

            Element newSource = parent.addElement("descripGrp");
            Element source = null;

            // copy all child nodes but remember the <source>
            for (lit = elem.elements().listIterator(); lit.hasNext(); )
            {
                Element child = (Element)lit.next();

                if (child.getName().equals("source"))
                {
                    source = child;
                }
                else
                {
                    lit.remove();
                    newSource.add(child);
                }
            }

            // create new <descrip type="source"> with source's value
            newSource.addElement("descrip").
                addAttribute("type", "source").
                addText(source.getText());
        }

        // rewrite <language>
        nodes = root.selectNodes("//languageGrp/language");

        for (it = nodes.iterator(); it.hasNext(); )
        {
            elem = (Element)it.next();

            Attribute nameAttr = elem.attribute("name");
            Attribute langAttr = elem.attribute("locale");

            String langName = nameAttr.getValue();
            String langLocale = langAttr.getValue();

            // locales in MTF consist of 2 letter codes (uppercase).
            langLocale = langLocale.substring(0, 2).toUpperCase();

            elem.remove(nameAttr);
            elem.remove(langAttr);

            elem.addAttribute("type", langName);
            elem.addAttribute("lang", langLocale);
        }

        if (false && CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("gt2mtf done: " + p_elem.asXML());
        }

        return p_elem;
    }

    private void checkIOError()
        throws IOException
    {
        // The JDK is so incredibly inconsistent (aka, stupid).
        // PrintWriter.println() does not throw exceptions.
        if (m_output.checkError())
        {
            throw new IOException("write error");
        }
    }
}
