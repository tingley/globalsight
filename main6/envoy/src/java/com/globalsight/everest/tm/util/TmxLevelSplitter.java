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

package com.globalsight.everest.tm.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Reads a TMX file and splits it into two files: one that contains level 1
 * segments (text only), and level 2 segments (with tags).
 */
public class TmxLevelSplitter
{
    private int m_entryCount = 0;
    private int m_textCount = 0;
    private int m_tagsCount = 0;

    private PrintWriter m_writer1;
    private PrintWriter m_writer2;

    private String m_version = "";
    private Element m_header = null;

    private static final REProgram DTD_SEARCH_PATTERN = createSearchPattern("tmx\\d+\\.dtd");

    private static REProgram createSearchPattern(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }

        return pattern;
    }

    //
    // Constructors
    //

    public TmxLevelSplitter()
    {
    }

    public String getBaseName(String p_name)
    {
        return p_name.substring(0, p_name.lastIndexOf("."));
    }

    public String getExtension(String p_name)
    {
        return p_name.substring(p_name.lastIndexOf(".") + 1);
    }

    public void log(String p_message)
    {
        System.err.println(p_message);
    }

    public void startFiles(String p_base, String p_extension) throws Exception
    {
        String textfile = p_base + "-text." + p_extension;
        String tagsfile = p_base + "-tags." + p_extension;

        m_writer1 = startFile(textfile);
        m_writer2 = startFile(tagsfile);
    }

    public PrintWriter startFile(String p_filename) throws Exception
    {
        log("Starting file " + p_filename);

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(p_filename)),
                "UTF8"));

        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

        // Parser should read and build header.
        Tmx temp = new Tmx();
        temp.setTmxVersion(m_version);

        writer.println(temp.getTmxDeclaration());

        writer.println("<tmx version=\"" + m_version + "\" >");
        writer.println(m_header.asXML());
        writer.println("<body>");

        return writer;
    }

    public void closeFiles() throws Exception
    {
        m_writer1.println("</body>");
        m_writer1.println("</tmx>");
        m_writer1.close();
        m_writer1 = null;

        m_writer2.println("</body>");
        m_writer2.println("</tmx>");
        m_writer2.close();
        m_writer2 = null;
    }

    public void writeTextEntry(String p_message)
    {
        m_writer1.println(p_message);
    }

    public void writeTagsEntry(String p_message)
    {
        m_writer2.println(p_message);
    }

    public boolean containsTags(Element p_tu)
    {
        List segs = p_tu.selectNodes("tuv/seg");

        for (int i = 0, max = segs.size(); i < max; i++)
        {
            Element seg = (Element) segs.get(i);

            if (containsTags2(seg))
            {
                return true;
            }
        }

        return false;
    }

    public boolean containsTags2(Element p_seg)
    {
        for (int i = 0, max = p_seg.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_seg.node(i);

            if (child instanceof Element)
            {
                return true;
            }
        }

        return false;
    }

    public void split(String p_url) throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        m_entryCount = 0;

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        reader.setEntityResolver(DtdResolver.getInstance());
        reader.setValidation(true);

        log("Splitting document `" + p_url + "'");

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
                Element element = path.getCurrent();

                m_version = element.attributeValue("version");
            }

            public void onEnd(ElementPath path)
            {
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/header", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                m_header = element;

                try
                {
                    startFiles(baseName, extension);
                }
                catch (Exception ex)
                {
                    log(ex.toString());
                    System.exit(1);
                }

                // prune the current element to reduce memory
                element.detach();

                element = null;
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu", new ElementHandler()
        {
            public void onStart(ElementPath path)
            {
                ++m_entryCount;
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                if (containsTags(element))
                {
                    writeTagsEntry(element.asXML());

                    m_tagsCount++;
                }
                else
                {
                    writeTextEntry(element.asXML());

                    m_textCount++;
                }

                // prune the current element to reduce memory
                element.detach();

                element = null;
            }
        });

        Document document = reader.read(p_url);

        closeFiles();

        log("Processed " + m_entryCount + " TUs, " + m_textCount
                + " level 1 (text), " + m_tagsCount + " level 2 (tags)");

        // all done
    }

    static public void main(String[] argv) throws Exception
    {
        TmxLevelSplitter a = new TmxLevelSplitter();

        if (argv.length != 1)
        {
            System.err
                    .println("Usage: TmxLevelSplitter FILE\n"
                            + "\tSplits a FILE into two files, one that contains\n"
                            + "\tthe level 1 segments (text only) and another that\n"
                            + "\tcontains the level 2 segments (with tags).\n"
                            + "\tOutput files are named FILE-text.EXT and FILE-tags.EXT.");
            System.exit(1);
        }

        a.split(argv[0]);
    }
}
