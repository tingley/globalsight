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
package com.globalsight.terminology.util;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;
import java.io.*;

/**
 * Reads MultiTerm MTF files and splits the file into multiple output files.
 */
public class MtfSplitter
{
    private int m_entryCount = 0;
    private int m_fileCount = 0;

    private PrintWriter m_writer;

    //
    // Constructors
    //

    public MtfSplitter ()
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

    public void startFile(String p_base, String p_extension)
        throws Exception
    {
        m_fileCount++;

        String filename = p_base + "-";
        if      (m_fileCount <   10) filename += "00" + m_fileCount;
        else if (m_fileCount <  100) filename += "0"  + m_fileCount;
        else /*if (m_fileCount >= 100)*/ filename += "" + m_fileCount;

        filename += "." + p_extension;

        m_writer = new PrintWriter(new OutputStreamWriter(
            new BufferedOutputStream(new FileOutputStream(filename)),
            "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        m_writer.println("<mtf>");
    }

    public void closeFile()
        throws Exception
    {
        m_writer.println("</mtf>");
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.println(p_message);
    }

    public void split(String p_url, String p_numEntries)
        throws Exception
    {
        final int maxEntries = Integer.parseInt(p_numEntries);
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        m_entryCount = 0;

        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        log("Splitting document `" + p_url + "'");

        startFile(baseName, extension);

        // enable element complete notifications to conserve memory
        reader.addHandler("/mtf/conceptGrp",
            new ElementHandler ()
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
                }
            );

        Document document = reader.read(p_url);

        closeFile();

        // all done
    }

    static public void main(String[] argv)
        throws Exception
    {
        MtfSplitter a = new MtfSplitter();

        if (argv.length < 2)
        {
            System.err.println(
                "Usage: MtfSplitter FILE NUMENTRIES\n" +
                "\tSplits a FILE after NUMENTRIES entries.\n" +
                "\tOutput files are named FILE-001.ext, FILE-002.EXT etc." +
                "\tTo determine the number of entries, use MtfAnalyzer.\n");
            System.exit(1);
        }

        a.split(argv[0], argv[1]);
    }
}
