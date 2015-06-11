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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects;
import com.globalsight.ling.docproc.extractor.html.IHtmlHandler;
import com.globalsight.ling.docproc.extractor.html.ParseException;
import com.globalsight.ling.docproc.extractor.html.Parser;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * OBSOLETE: USE TradosHtmlTmxToGxml.
 *
 * The functionality of this class has been moved into the HTML Extractor.
 *
 * Tool to convert Trados TMX files to GXML TMX files.
 *
 * Use TradosTmxToRtf to read a Trados TMX file and write it out as an RTF file
 * that Word can read. When the file is saved from within Word as HTML, use this
 * class to parse the HTML, extract segments, and write the result as GXML TMX.
 *
 * Notes: for RTF-type segments: - skip leading
 * <p>
 * <span> - skip trailing </span>
 * </p>
 * - skip trailing <o:p></o:p>
 *
 * for HTML-type segments: - skip leading
 * <p>
 * - skip trailing
 * </p>
 * - decode internal <span mso-spacerun>...</span> tags - decode HTML entities
 * in string and extract
 */
public class WordHtmlToTmx2 implements IHtmlHandler
{
    private Logger m_logger = null;

    private PrintWriter m_writer;

    private String m_version = "";
    private Tmx m_header = null;
    private String m_filename;

    private HtmlEntities m_htmlDecoder = new HtmlEntities();
    private DiplomatAPI m_diplomat = new DiplomatAPI();
    private Parser m_parser;
    private int m_entryCount = 0;
    private boolean m_skipping = true;
    private ArrayList m_tags = new ArrayList();
    private String m_html = null;
    private String m_tmx = null;

    // A hack: I can't get rid of two TAB characters showing up at the
    // beginning of the RTF doc loaded into Word. Word then writes out
    // two <span mso-tab-counts> which we have to get rid of.
    private boolean m_firstTuv = true;

    private static final REProgram SEG_SEARCH_PATTERN = createSearchPattern("<seg>(.*?)</seg>");

    private static final REProgram SPACERUN_SEARCH_PATTERN = createSearchPattern("<span(.|[:space:])+?style=\"mso-spacerun[^>]*>(.*?)</span>");

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

    public WordHtmlToTmx2()
    {
    }

    public WordHtmlToTmx2(Logger p_logger)
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

    /**
     * Called once at the beginning of a new document.
     */
    public void handleStart()
    {
    }

    /**
     * Called once at the end of a document.
     */
    public void handleFinish()
    {
    }

    /**
     * Handle an HTML start tag including its attributes.
     */
    public void handleStartTag(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("P"))
        {
            m_skipping = false;
        }

        if (m_skipping)
        {
            return;
        }

        m_tags.add(t);
    }

    /**
     * Handle an HTML end tag.
     */
    public void handleEndTag(HtmlObjects.EndTag t)
    {
        if (m_skipping)
        {
            return;
        }

        m_tags.add(t);

        if (t.tag.equalsIgnoreCase("P"))
        {
            handleTU();

            m_entryCount++;

            if (m_entryCount % 1000 == 0)
            {
                debug("Entry " + m_entryCount);
            }

            m_tags.clear();
            m_skipping = true;
        }
    }

    /**
     * Handle end of line characters.
     */
    public void handleNewline(HtmlObjects.Newline t)
    {
        if (m_skipping)
        {
            return;
        }

        m_tags.add(t);
    }

    /**
     * Handle text (#PCDATA).
     */
    public void handleText(HtmlObjects.Text t)
    {
        if (m_skipping)
        {
            return;
        }

        t.text = m_htmlDecoder.decodeString(t.text, null);

        m_tags.add(t);
    }

    /**
     * Handle an HTML comment <code>&lt;!-- --&gt;</code>.
     */
    public void handleComment(HtmlObjects.Comment t)
    {
    }

    /**
     * Handle an HTML declaration <code>&lt;!DOCTYPE  &gt;</code>.
     */
    public void handleDeclaration(HtmlObjects.Declaration t)
    {
    }

    /**
     * Handle an HTML processing instruction <code>&lt;?  ?&gt;</code>.
     */
    public void handlePI(HtmlObjects.PI t)
    {
    }

    /**
     * Handle a ColdFusion comment <code>&lt;!--- ---&gt;</code>.
     */
    public void handleCfComment(HtmlObjects.CfComment c)
    {
    }

    /**
     * Handle a ColdFusion start tag including its attributes.
     */
    public void handleCFStartTag(HtmlObjects.CFTag t)
    {
    }

    /**
     * Handle a ColdFusion end tag.
     */
    public void handleCFEndTag(HtmlObjects.EndTag t)
    {
    }

    /**
     * Handle a ASP/JSP tag; the script text is included in the argument
     * <code>t</code>.
     */
    public void handleXsp(HtmlObjects.Xsp t)
    {
    }

    /**
     * Handle the <code>&lt;script&gt;</code> tag; the script text is included
     * in the argument <code>t</code>.
     */
    public void handleScript(HtmlObjects.Script t)
    {
    }

    /**
     * Handle the <code>&lt;java&gt;</code> tag; the java text is included in
     * the argument <code>t</code>.
     */
    public void handleJava(HtmlObjects.Java t)
    {
    }

    /**
     * Handle the <code>&lt;style&gt;</code> tag; the style text is included in
     * the argument <code>t</code>.
     */
    public void handleStyle(HtmlObjects.Style t)
    {
    }

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag; the ColdFusion script text
     * is included in the argument <code>t</code>.
     */
    public void handleCFScript(HtmlObjects.CFScript t)
    {
    }

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag with SQL statements inside;
     * the SQL text is included in the argument <code>t</code>.
     */
    public void handleCFQuery(HtmlObjects.CFQuery t)
    {
    }

    // *************************************************

    public void handleTU()
    {
        // handle segment
        StringBuffer tu = new StringBuffer();

        // Remove tags inserted by Word: either <p>...</p>, or
        // <p><span>...<o:p></o:p></span></p>.
        cleanupRawTags(m_tags);

        for (int i = 0; i < m_tags.size(); i++)
        {
            Object o = m_tags.get(i);
            tu.append(o.toString());
        }

        // System.err.println("TU = " + tu + "\n");

        String temp = tu.toString();

        try
        {
            // 1) \r\n leads to strange segmentation for some reason
            // (each line becomes one segment), so fix that.
            temp = normalizeEOL(temp);

            // 2) Multiple whitespace in an mso-spacerun, normalize.
            temp = removeMsoSpace(temp);
        }
        catch (Throwable ex)
        {
            debug(ex.toString());
        }

        // System.err.println("TU = " + temp + "\n");

        // Gather data for TUVs that consist of multiple segments
        // (according to System4 tag definition and paragraph
        // segmentation). Keep data around and print it at the end.
        ArrayList preambles = new ArrayList();
        ArrayList segments = new ArrayList();
        String postamble = null;

        String pre = "";
        String post = "";
        String segment = "";
        RE re = new RE(SEG_SEARCH_PATTERN, RE.MATCH_CASEINDEPENDENT
                | RE.MATCH_SINGLELINE);
        while (re.match(temp))
        {
            pre = temp.substring(0, re.getParenStart(0));
            segment = re.getParen(1);
            post = temp.substring(re.getParenEnd(0));
            temp = post;

            preambles.add(pre);

            // Decode the segment to restore original HTML formatting
            segment = m_htmlDecoder.decodeStringBasic(segment);

            // System.err.println("New Segment = " + segment + "\n");

            Output output = extractSegment(segment);
            ArrayList gxmlSegments = getSegments(output);

            segments.add(gxmlSegments);

            if (false)
            {
                System.err.println("\tPRE=" + pre);
                System.err.println("\tSEG=" + segment);
                // System.err.println("\tXML=" + gxml);
                System.err.println("\tPOS=" + post);
            }
        }

        postamble = post;

        // Now output multiple TUs, one for each System4 segment in
        // the original Trados TMX paragraph.
        StringBuffer toWrite = new StringBuffer();

        int max = ((ArrayList) segments.get(0)).size();

        // Loop over the System4 segments and collect output
        try
        {
            for (int i = 0; i < max; i++)
            {
                // Loop over TUVs in TU
                for (int j = 0; j < preambles.size(); j++)
                {
                    String preamble = (String) preambles.get(j);
                    String gxml = (String) (((ArrayList) segments.get(j))
                            .get(i));

                    toWrite.append(preamble);
                    toWrite.append("<seg>");
                    // should trim() the GXML segment...
                    toWrite.append(gxml.trim());
                    toWrite.append("</seg>");
                }

                toWrite.append(post);
                toWrite.append("\r\n");
            }

            // only output if no errors
            writeEntry(toWrite.toString());
        }
        catch (Throwable ex)
        {
            debug("Error in TU " + m_entryCount + ":\n" + tu + "\n");

            for (int i = 0; i < segments.size(); i++)
            {
                ArrayList segs = (ArrayList) segments.get(i);

                for (int j = 0; j < segs.size(); j++)
                {
                    String gxml = (String) segs.get(j);
                    debug("Segment: " + gxml);
                }

                debug("\n");
            }
        }
    }

    public String normalizeEOL(String p_string) throws Exception
    {
        String result = p_string;

        result = RegEx.substituteAll(result, "\\r", "");
        result = RegEx.substituteAll(result, "\\n", " ");

        return result;
    }

    public String removeMsoSpace(String p_string)
    {
        StringBuffer result = new StringBuffer();

        RE re = new RE(SPACERUN_SEARCH_PATTERN, RE.MATCH_CASEINDEPENDENT
                | RE.MATCH_SINGLELINE);

        String temp = p_string;
        while (re.match(temp))
        {
            result.append(temp.substring(0, re.getParenStart(0)));

            for (int i = 0, max = re.getParen(2).length(); i < max; i++)
            {
                result.append(" ");
            }

            temp = temp.substring(re.getParenEnd(0));
        }

        result.append(temp);

        return result.toString();
    }

    public Output extractSegment(String p_segment)
    {
        try
        {
            m_diplomat.reset();
            m_diplomat.setSourceString(p_segment);
            m_diplomat.setInputFormat(IFormatNames.FORMAT_WORD_HTML);
            m_diplomat.setEncoding("unicode");
            m_diplomat.setLocale(Locale.US);
            m_diplomat.setSentenceSegmentation(false);

            m_diplomat.extract();

            return m_diplomat.getOutput();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // After System4 paragraph segmentation, return all translatable
    // and localizable segments (the real segments) in a list.
    public ArrayList getSegments(Output p_output)
    {
        ArrayList result = new ArrayList();

        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;

                    if (elem.hasSegments())
                    {
                        ArrayList segments = elem.getSegments();

                        for (int i = 0, max = segments.size(); i < max; i++)
                        {
                            SegmentNode node = (SegmentNode) segments.get(i);
                            result.add(node.getSegment());
                        }
                    }

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    LocalizableElement elem = (LocalizableElement) de;
                    result.add(elem.getChunk());

                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }

        return result;
    }

    /**
     * Remove tags inserted by Word: either
     * <p>
     * ...
     * </p>
     * , or
     * <p>
     * <span>...<o:p></o:p></span>
     * </p>
     * .
     */
    private void cleanupRawTags(ArrayList p_tags)
    {
        boolean b_removeOP = false;

        // try to detect the <o:p> case
        if (p_tags.size() >= 6)
        {
            try
            {
                HtmlObjects.Tag tag = (HtmlObjects.Tag) p_tags.get(p_tags
                        .size() - 4);

                if (tag.tag.equals("o:p"))
                {
                    b_removeOP = true;
                }
            }
            catch (Throwable ignore)
            {
            }
        }

        if (b_removeOP)
        {
            // skip leading <p><span>
            p_tags.remove(0);
            p_tags.remove(0);

            // and trailing </span></p>
            p_tags.remove(p_tags.size() - 1);
            p_tags.remove(p_tags.size() - 1);

            // and trailing <o:p></o:p>
            p_tags.remove(p_tags.size() - 1);
            p_tags.remove(p_tags.size() - 1);
        }
        else
        {
            // skip leading <p>
            p_tags.remove(0);

            // and trailing </p>
            p_tags.remove(p_tags.size() - 1);
        }

        // A hack: I can't get rid of two TAB characters showing up at the
        // beginning of the RTF doc loaded into Word. Word then writes out
        // two <span mso-tab-counts> which we have to get rid of.
        if (m_firstTuv)
        {
            m_firstTuv = false;

            if (p_tags.size() >= 6)
            {
                try
                {
                    HtmlObjects.Tag tag1 = (HtmlObjects.Tag) p_tags.get(0);
                    HtmlObjects.Tag tag2 = (HtmlObjects.Tag) p_tags.get(3);

                    String original1 = tag1.original;
                    String original2 = tag2.original;

                    if (original1.indexOf("span") >= 0
                            && original2.indexOf("span") >= 0
                            && original1.indexOf("mso-tab-count") >= 0
                            && original2.indexOf("mso-tab-count") >= 0)
                    {
                        p_tags.remove(0); // <span>
                        p_tags.remove(0); // nbsps
                        p_tags.remove(0); // </span>
                        p_tags.remove(0); // <span>
                        p_tags.remove(0); // nbsps
                        p_tags.remove(0); // </span>
                    }
                }
                catch (Throwable ignore)
                {
                }
            }
        }
    }

    public String getBaseName(String p_name)
    {
        return p_name.substring(0, p_name.lastIndexOf("."));
    }

    public String getExtension(String p_name)
    {
        return p_name.substring(p_name.lastIndexOf(".") + 1);
    }

    public void startOutputFile(String p_base) throws Exception
    {
        m_filename = p_base + "-gxml.tmx";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(m_filename)),
                "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        m_writer.println(m_header.getTmxDeclaration());
        m_writer.println(m_header.getTmxXml());
        m_writer.println(m_header.getHeaderXml());
        m_writer.println("<body>");
    }

    public void closeOutputFile() throws Exception
    {
        m_writer.println("</body>");
        m_writer.println("</tmx>");
        m_writer.close();
    }

    public void writeEntry(String p_tmx)
    {
        m_writer.print(p_tmx);
    }

    public void createHeader()
    {
        m_header = new Tmx();
        m_header.setTmxVersion(Tmx.TMX_GS);
        m_header.setCreationTool("GlobalSight TradosTmxToGTmx");
        m_header.setCreationToolVersion("1.0");
        m_header.setSegmentationType("sentence");
        m_header.setOriginalFormat("html");
        m_header.setAdminLang("en_US");
        m_header.setSourceLang("en_US"); // TODO
        m_header.setDatatype("text"); // TODO
        // TODO
    }

    public Reader createInputReader(String p_url) throws Exception
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(
                p_url), "UTF8"));
    }

    public void createParser(String p_url) throws Exception
    {
        Reader inputReader = createInputReader(p_url);

        m_parser = new Parser(inputReader);
        m_parser.setHandler(this);

        try
        {
            m_parser.parse();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public String convertHtmlToTmx(String p_url) throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        info("Converting HTML file `" + p_url + "'");

        createHeader();
        startOutputFile(baseName);

        createParser(p_url);

        closeOutputFile();

        info("Processed " + m_entryCount + " TUs into file `" + m_filename
                + "'");

        return m_filename;
    }

    static public void main(String[] argv) throws Exception
    {
        WordHtmlToTmx2 a = new WordHtmlToTmx2();

        if (argv.length != 1)
        {
            System.err.println("Usage: WordHtmlToTmx2 FILE\n");
            System.exit(1);
        }

        a.convertHtmlToTmx(argv[0]);
    }

    @Override
    public void handleSpecialChar(
            com.globalsight.ling.docproc.extractor.html.HtmlObjects.Text t)
    {
        // TODO Auto-generated method stub

    }
}
