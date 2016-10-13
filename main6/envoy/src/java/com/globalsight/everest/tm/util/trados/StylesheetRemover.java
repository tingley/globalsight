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

package com.globalsight.everest.tm.util.trados;


import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;

/**
 * <p>Trados-TMX Converter for RTF.</p>
 *
 * <p>Use TradosTmxToRtf to read a Trados TMX file and write it out as
 * RTF that Word can read. When the file is saved from within Word as
 * HTML, use this class to remove the possibly very large stylesheet,
 * and then use WordHtmlToTmx to parse the HTML, extract segments, and
 * write the result as GXML TMX.</p>
 *
 * <p>Note: the stylesheet element "<style>" should be as sparse in
 * syntax as possible because we use only a simple line reader to
 * detect it and remove its content.</p>
 */
public class StylesheetRemover
{
    private Logger m_logger = null;

    private LineNumberReader m_reader;
    private PrintWriter m_writer;

    private String m_filename;

    //
    // Constructors
    //

    public StylesheetRemover ()
    {
    }

    public StylesheetRemover (Logger p_logger)
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

    public void createInputReader(String p_url)
        throws Exception
    {
        m_reader = new LineNumberReader(new InputStreamReader(
            new FileInputStream(p_url), "UTF8"));
    }

    public void startOutputFile(String p_base)
        throws Exception
    {
        m_filename = p_base + "-nostyle.html";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
            new BufferedOutputStream(new FileOutputStream(m_filename)),
            "UTF8"));
    }

    public void closeInputFile()
        throws Exception
    {
        m_reader.close();
    }

    public void closeOutputFile()
        throws Exception
    {
        m_writer.close();
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String removeStylesheet(String p_url)
        throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        info("Removing stylesheet from file `" + p_url + "'");

        createInputReader(p_url);
        startOutputFile(baseName);

        boolean inStyle = false;
        String line;
        while ((line = m_reader.readLine()) != null)
        {
            String temp = line;
            if (temp.startsWith("<style>"))
            {
                inStyle = true;
                continue;
            }
            else if (temp.startsWith("</style>"))
            {
                inStyle = false;
                continue;
            }

            if (!inStyle)
            {
                m_writer.println(line);
            }
        }

        closeOutputFile();
        closeInputFile();

        info("Stylesheet removed, file = `" + m_filename + "'");

        return m_filename;
    }

    static public void main(String[] argv)
        throws Exception
    {
        StylesheetRemover a = new StylesheetRemover();

        if (argv.length != 1)
        {
            System.err.println("Usage: StylesheetRemover FILE\n");
            System.exit(1);
        }

        a.removeStylesheet(argv[0]);
    }
}
