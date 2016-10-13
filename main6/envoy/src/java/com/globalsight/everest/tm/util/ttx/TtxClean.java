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

package com.globalsight.everest.tm.util.ttx;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Ttx;

import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.LocalizableElement;

import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.Text;


import org.dom4j.*;
import org.dom4j.io.SAXReader;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.*;
import java.io.*;

/**
 * <p>Trados-TTX Converter that writes out the original source document.</p>
 */
public class TtxClean
{
    static public final String s_TOOLNAME = "GlobalSight TtxClean";
    static public final String s_TOOLVERSION = "1.0";

    private Logger m_logger = null;

	private boolean m_cleanTarget = true;
    private Ttx m_header;
    private String m_version = Ttx.TTX_20;

    private int m_entryCount = 0;
    private int m_errorCount = 0;
    private String m_filename;

    private PrintWriter m_writer;

    //
    // Constructors
    //

    public TtxClean ()
    {
    }

    public TtxClean (Logger p_logger)
    {
        m_logger = p_logger;
    }

    // *************************************************

    public void debug(String p_message)
    {
        if (m_logger != null)
        {
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug(p_message);
            }
        }
        else
        {
            System.err.println(p_message);
        }
    }

    public void info(String p_message)
    {
        if (m_logger != null)
        {
            m_logger.info(p_message);
        }
        else
        {
            System.err.println(p_message);
        }
    }

    // *************************************************

    public String getBaseName(String p_name)
    {
        return p_name.substring(0, p_name.lastIndexOf("."));
    }

    public String getExtension(String p_name)
    {
        return p_name.substring(p_name.lastIndexOf(".") + 1);
    }

    public void startOutputFile(String p_base, String p_locale,
        String p_extension, String p_encoding)
        throws Exception
    {
        m_filename = p_base + "-" + p_locale + "." + p_extension;

        debug("Using encoding " + p_encoding);

        m_writer = new PrintWriter(new OutputStreamWriter(
            new BufferedOutputStream(new FileOutputStream(m_filename)),
            CodesetMapper.getJavaEncoding(p_encoding)));
    }

    public void closeOutputFile()
        throws Exception
    {
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.print(p_message);
    }

    // ******************************************

    /**
     * Returns the inner text like Element.getText() but for all
     * embedded text nodes.
     */
    static public String getInnerText(Element p_node)
    {
        StringBuffer result = new StringBuffer();

        List content = p_node.content();

        for (int i = 0, max = content.size(); i < max; i++)
        {
            Node node = (Node)content.get(i);

            if (node.getNodeType() == Node.TEXT_NODE)
            {
                result.append(node.getText());
            }
            else if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                result.append(getInnerText((Element)node));
            }
        }

        return result.toString();
    }

    private void removeNonTuElements(Element p_body)
    {
		ArrayList elems = new ArrayList();

		while (true)
		{
			elems.clear();

			findNonTuElements(elems, p_body);

			if (elems.size() == 0)
			{
				break;
			}

			for (int i = 0, max = elems.size(); i < max; i++)
			{
				Element elem = (Element)elems.get(i);

				removeElement(elem);
			}
		}
    }

    private void findNonTuElements(ArrayList p_result, Element p_element)
    {
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node)p_element.node(i);

            if (child instanceof Element)
            {
				findNonTuElements(p_result, (Element)child);
            }
        }

		String name = p_element.getName();

        if (!name.equals(Ttx.TU) && !name.equals(Ttx.TUV) && 
			!name.equals(Ttx.RAW))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Removes a element by pulling up its children into the parent node.
     */
    private void removeElement(Element p_element)
    {
        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the old content, inserting the <ut>'s content
        // instead of the <ut>.

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node)content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node)newContent.get(i);

            if (i == index)
            {
                parent.appendContent(p_element);
            }
            else
            {
                parent.add(node);
            }
        }
    }

	private void handleTu(Element p_body)
	{
		String locale = m_cleanTarget ? m_header.getTargetLanguage() : 
			m_header.getSourceLanguage();

		ArrayList elems = new ArrayList();

		findTuElements(elems, p_body);
		
		for (int i = 0, max = elems.size(); i < max; i++)
		{
			Element elem = (Element)elems.get(i);

			removeTuElement(elem, locale);
		}
	}

    private void findTuElements(ArrayList p_result, Element p_element)
    {
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node)p_element.node(i);

            if (child instanceof Element)
            {
				findTuElements(p_result, (Element)child);
            }
        }

		String name = p_element.getName();

        if (name.equals(Ttx.TU))
        {
            p_result.add(p_element);
        }
    }

    /**
     * Removes a TU element by pulling up the content of the TUV in
     * the given locale into the parent node.
     */
    private void removeTuElement(Element p_element, String p_locale)
    {
		// The source or target language TUV to replace the TU with.
		Element tuv = (Element)p_element.selectSingleNode(
			"//Tuv[@Lang='" + p_locale + "']");

        Element parent = p_element.getParent();
        int index = parent.indexOf(p_element);

        // We copy the current content, clear out the parent, and then
        // re-add the tuv content.

        ArrayList newContent = new ArrayList();
        List content = parent.content();

        for (int i = content.size() - 1; i >= 0; --i)
        {
            Node node = (Node)content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node)newContent.get(i);

            if (i == index)
            {
                parent.appendContent(tuv);
            }
            else
            {
                parent.add(node);
            }
        }
    }

    /**
     * Removes all TTX &lt;ut&gt; elements from the document body and
     * inserts source (or target TUV) from TUs.
     */
    private void processBody(Element p_body)
    {
		removeNonTuElements(p_body);
		handleTu(p_body);
    }

    public void setOldHeader(Element p_element)
    {
        m_header = new Ttx(p_element);
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String cleanTtx(String p_url, boolean p_cleanTarget, 
		String p_encoding)
        throws Exception
    {
		m_cleanTarget = p_cleanTarget;

        // File is called <file>.<ext>.<ttx>
        final String origName = getBaseName(p_url);
        final String baseName = getBaseName(origName);
        final String extension = getExtension(origName);

        info("Cleaning TTX file to " + 
			(m_cleanTarget ? "target" : "source") + ": `" + p_url + "'");

        m_entryCount = 0;

        // Reading from a file, need to use Xerces.
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        //reader.setEntityResolver(DtdResolver.getInstance());
        //reader.setValidation(true);

        // Fetch the version info early.
        reader.addHandler("/TRADOStag",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        m_version = element.attributeValue(Ttx.VERSION);
                    }

                    public void onEnd(ElementPath path)
                    {
                    }
                }
            );

        // Fetch the header info early.
        reader.addHandler("/TRADOStag/FrontMatter",
            new ElementHandler ()
                {
                    public void onStart(ElementPath path)
                    {
                    }

                    public void onEnd(ElementPath path)
                    {
                        Element element = path.getCurrent();

                        setOldHeader(element);
                    }
                }
            );

		// Read in the entire file (it's not too big normally).
        Document document = reader.read(p_url);

        Element body = (Element)
            document.getRootElement().selectSingleNode("//Body/Raw");

		// Remove <ut>, <df> and pull out one TUV.
        processBody(body);

        String content = getInnerText(body);
		String encoding;

		if (m_cleanTarget)
		{
			if (p_encoding != null)
			{
				encoding = p_encoding;
			}
			else
			{
				encoding = "UTF-8";
			}
		}
		else
		{
			// reuse original encoding
			encoding = m_header.getOriginalEncoding();
		}

		String locale;

		if (m_cleanTarget)
		{
			locale = m_header.getTargetLanguage();
		}
		else
		{
			locale = m_header.getSourceLanguage();
		}

        startOutputFile(baseName, locale, extension, encoding);
        writeEntry(content);
        closeOutputFile();

        info("Result written to file `" + m_filename + "'.");

        return m_filename;
    }

	static public void usage()
	{
		System.err.println("Usage: TtxClean [-source] [-e encoding] FILE\n");
		System.err.println("Cleans a Trados TTX file " +
			"to the target (or original source) document.\n" +
			"If the target encoding is not specified, UTF-8 is used.\n");
		System.exit(1);
	}

    static public void main(String[] argv)
        throws Exception
    {
        TtxClean a = new TtxClean();
		boolean cleanTarget = true;
		String encoding = null;
		int i;

        if (argv.length == 0)
        {
			usage();
        }

		for (i = 0; i < argv.length - 1; i++)
		{
			if (argv[i].equals("-s") || argv[i].equals("-source"))
			{
				cleanTarget = false;
				continue;
			}

			if (argv[i].equals("-e"))
			{
				if (i < argv.length - 2)
				{
					++i;
					encoding = argv[i];
				}
				else
				{
					usage();
				}
			}
		}

		a.cleanTtx(argv[i], cleanTarget, encoding);
    }
}
