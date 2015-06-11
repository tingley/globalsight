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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Reads a TMX file and splits it into smaller chunks.
 */
public class TmxSplitter
{
    private int m_entryCount = 0;
    private int m_fileCount = 0;

    private PrintWriter m_writer;

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

    public TmxSplitter()
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

    public void startFile(String p_base, String p_extension) throws Exception
    {
        m_fileCount++;

        String filename = p_base + "-";
        if (m_fileCount < 10)
            filename += "00" + m_fileCount;
        else if (m_fileCount < 100)
            filename += "0" + m_fileCount;
        else
            /* if (m_fileCount >= 100) */filename += "" + m_fileCount;

        filename += "." + p_extension;

        log("Starting file " + filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(filename)),
                "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

        // Parser should read and build header.
        Tmx temp = new Tmx();
        temp.setTmxVersion(m_version);

        m_writer.println(temp.getTmxDeclaration());

        m_writer.println("<tmx version=\"" + m_version + "\" >");
        m_writer.println(m_header.asXML());
        m_writer.println("<body>");
    }

    public void closeFile() throws Exception
    {
        m_writer.println("</body>");
        m_writer.println("</tmx>");
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.println(p_message);
    }

    public void split(String p_url, String p_numEntries) throws Exception
    {
        final int maxEntries = Integer.parseInt(p_numEntries);
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
                    startFile(baseName, extension);
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

                if (m_entryCount % maxEntries == 0)
                {
                    try
                    {
                        closeFile();
                        startFile(baseName, extension);
                    }
                    catch (Exception ex)
                    {
                        log(ex.toString());
                        System.exit(1);
                    }
                }
            }

            public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                writeEntry(element.asXML());

                // prune the current element to reduce memory
                element.detach();

                element = null;
            }
        });

        Document document = reader.read(p_url);

        closeFile();

        // all done
    }

    static public void main(String[] argv) throws Exception
    {
        TmxSplitter a = new TmxSplitter();

        if (argv.length != 2)
        {
            System.err
                    .println("Usage: TmxSplitter FILE NUMENTRIES\n"
                            + "\tSplits a FILE after NUMENTRIES entries.\n"
                            + "\tOutput files are named FILE-001.ext, FILE-002.EXT etc."
                            + "\tTo determine the number of entries, use TmxAnalyzer.\n");
            System.exit(1);
        }

        a.split(argv[0], argv[1]);
    }
}
