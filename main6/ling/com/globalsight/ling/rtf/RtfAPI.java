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
//
// Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
package com.globalsight.ling.rtf;


import java.io.*;
import java.io.Reader;

import org.apache.log4j.Logger;

/**
 * Main API class of this package: parses RTF files or input streams
 * into an internal object model that is returned as RtfDocument. Also
 * allows to run an optimizer on the raw object model to minimize
 * paragraphs and controls.
 */
public class RtfAPI
{
    static private final Logger CATEGORY =
        Logger.getLogger(
            RtfAPI.class);

    //
    // Private Members
    //
    private RtfReader m_reader = new RtfReader();
    private RtfOptimizer m_optimizer = new RtfOptimizer();

    //
    // Constructor
    //

    public RtfAPI()
    {
    }

    //
    // Package-Private Methods
    //

    static void debug(String p_message)
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug(p_message);
        }
    }

    static void debug(String p_message, Throwable p_exception)
    {
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug(p_message, p_exception);
        }
    }

    //
    // Public Methods
    //

    /**
     * Static main for debugging.
     */
    static public void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("Usage: RtfAPI file <dump> <trace>\n" +
                "\tdump = 'true|false', trace = 'true|false'\n");
            System.exit(1);
        }

        String filename = args[0];
        boolean dump = false;
        boolean trace = false;

        if (args.length == 2 && args[1].equalsIgnoreCase("true"))
        {
            dump = true;
        }

        if (args.length == 3 && args[2].equalsIgnoreCase("true"))
        {
            trace = true;
        }

        parse(filename, dump, trace);
    }

    /**
     * Static method for debugging: parses a file and optionally
     * writes out the resulting document structure to System.out.
     */
    static public RtfDocument parse(String p_rtfInputfile,
        boolean p_dump, boolean p_trace)
    {
        try
        {
            RtfReader reader = new RtfReader();

            try
            {
                reader.init(p_rtfInputfile);
            }
            catch (FileNotFoundException ex)
            {
                CATEGORY.error("File not found: " + p_rtfInputfile +
                    " (" + ex.getMessage() + ")");
                return null;
            }

            PrintWriter pw = new PrintWriter (
                new OutputStreamWriter (System.out, "UTF-8"));

            RtfAnalyser ana = new RtfAnalyser(reader);
            RtfDocument doc = ana.parse();

            if (p_dump)
            {
                pw.println();
                pw.println("***Dump of the DOCRTF intermediate structure");
                doc.Dump(pw);
                pw.println("***Dump ends here");
            }

            pw.flush();
            // pw.close(); // don't close System.out
            reader.exit();

            return doc;
        }
        catch (IOException ex)
        {
            CATEGORY.error("RtfParser: unexpected IO exception", ex);
            return null;
        }
        catch (Exception ex)
        {
            CATEGORY.error("RtfParser: unexpected exception", ex);
            return null;
        }
    }

    /**
     * Extractor Interface. Parses from a java.io.Reader and returns
     * an RtfDocument.
     */
    public RtfDocument parse(Reader p_reader)
        throws Exception
    {
        m_reader.init(p_reader);

        try
        {
            long now = System.currentTimeMillis();

            // RtfAnalyser is not a reusable object.
            RtfAnalyser ana = new RtfAnalyser(m_reader);
            RtfDocument doc = ana.parse();
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Parsing took "
                        + (System.currentTimeMillis() - now) + "ms");
            }
            return doc;
        }
        catch (Exception ex)
        {
            CATEGORY.error("Error parsing RTF", ex);
            throw ex;
        }
        finally
        {
            m_reader.exit();
        }
    }

    /**
     * Extractor Interface. Optimizes the paragraphs and text
     * sequences by collapsing text runs that share the same
     * properties and removing unnecessary RTF controls.
     */
    public RtfDocument optimize(RtfDocument p_doc)
    {
        long now = System.currentTimeMillis();

        m_optimizer.init(p_doc);
        RtfDocument result = m_optimizer.optimize();
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Optimization took "
                    + (System.currentTimeMillis() - now) + "ms");
        }
        return result;
    }

    /**
     * Returns the visible text of an RTF document without embedded
     * formatting.
     */
    static public String getText(RtfDocument p_doc)
    {
        StringWriter sw = new StringWriter (1024);
        PrintWriter  pw = new PrintWriter (sw);

        p_doc.toText(pw);

        return sw.toString();
    }
}
