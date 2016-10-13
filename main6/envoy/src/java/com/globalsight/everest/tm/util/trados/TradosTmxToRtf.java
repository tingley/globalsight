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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Tmx;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * <p>
 * Trados-TMX Converter for RTF.
 * </p>
 *
 * <p>
 * This tool reads a Trados TMX file and writes it out as RTF that Word can
 * read. When the file is saved from within Word as HTML, use StylesheetRemover
 * to remove the possibly very large stylesheet, and then WordHtmlToTmx to parse
 * the HTML, extract segments, and write the result as GXML TMX.
 * </p>
 */
public class TradosTmxToRtf
{
    private Logger m_logger = null;

    private int m_entryCount = 0;
    private int m_fileCount = 0;
    private String m_filename;

    private PrintWriter m_writer;

    private String m_version = "";
    private Tmx m_header = null;

    private static final REProgram PARAGRAPH_SEARCH_PATTERN = createSearchPattern("\\\\pard?");

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

    public TradosTmxToRtf()
    {
    }

    public TradosTmxToRtf(Logger p_logger)
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

    public void startOutputFile(String p_base) throws Exception
    {
        m_fileCount++;

        m_filename = p_base + ".rtf";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(m_filename)),
                "UTF8"));

        m_writer.println("{\\rtf1\\ansi\\ansicpg1252\\uc0\\deff0\\deflang1033\\deflangfe1033");
    }

    public void closeOutputFile() throws Exception
    {
        m_writer.println("}");
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.println(p_message);
    }

    public void writeOtherRtfHeader()
    {
        m_writer.println("{\\colortbl;\n" + "\\red0\\green0\\blue0;\n"
                + "\\red0\\green0\\blue255;\n" + "\\red0\\green255\\blue255;\n"
                + "\\red0\\green255\\blue0;\n" + "\\red255\\green0\\blue255;\n"
                + "\\red255\\green0\\blue0;\n" + "\\red255\\green255\\blue0;\n"
                + "\\red255\\green255\\blue255;\n"
                + "\\red0\\green0\\blue128;\n" + "\\red0\\green128\\blue128;\n"
                + "\\red0\\green128\\blue0;\n" + "\\red128\\green0\\blue128;\n"
                + "\\red128\\green0\\blue0;\n" + "\\red128\\green128\\blue0;\n"
                + "\\red128\\green128\\blue128;\n"
                + "\\red192\\green192\\blue192;\n" + "}");

        m_writer.println("{\\info{\\title TMX CONVERSION}"
                + "{\\author CvdL}{\\operator CvdL}"
                + "{\\creatim\\yr2003\\mo7\\dy8\\hr21\\min46}"
                + "{\\revtim\\yr2003\\mo7\\dy8\\hr21\\min47}"
                + "{\\version1}{\\edmins1}{\\nofpages1}"
                + "{\\nofwords0}{\\nofchars0}"
                + "{\\*\\company GlobalSight}{\\nofcharsws0}{\\vern8269}" + "}");

        m_writer.println("\\widowctrl\\ftnbj\\aenddoc\\noxlattoyen\\expshrtn\\noultrlspc\\dntblnsbdb\\nospaceforul\\formshade\\horzdoc\\dgmargin\\dghspace180\\dgvspace180\\dghorigin1800\\dgvorigin1440\\dghshow1\\dgvshow1\n"
                + "\\jexpand\\viewkind1\\viewscale129\\viewzk2\\pgbrdrhead\\pgbrdrfoot\\splytwnine\\ftnlytwnine\\htmautsp\\nolnhtadjtbl\\useltbaln\\alntblind\\lytcalctblwd\\lyttblrtgr\\lnbrkrule \\fet0\\sectd \\linex0\\endnhere\\sectlinegrid360\\sectdefaultcl \n"
                + "{\\*\\pnseclvl1 \\pnucrm\\pnstart1\\pnindent720\\pnhang{\\pntxta .}}\n"
                + "{\\*\\pnseclvl2\\pnucltr\\pnstart1\\pnindent720\\pnhang{\\pntxta .}}\n"
                + "{\\*\\pnseclvl3\\pndec\\pnstart1\\pnindent720\\pnhang{\\pntxta .}}\n"
                + "{\\*\\pnseclvl4\\pnlcltr\\pnstart1\\pnindent720\\pnhang{\\pntxta )}}\n"
                + "{\\*\\pnseclvl5\\pndec\\pnstart1\\pnindent720\\pnhang{\\pntxtb (}{\\pntxta )}}\n"
                + "{\\*\\pnseclvl6\\pnlcltr\\pnstart1\\pnindent720\\pnhang{\\pntxtb (}{\\pntxta )}}\n"
                + "{\\*\\pnseclvl7\\pnlcrm\\pnstart1\\pnindent720\\pnhang{\\pntxtb (}{\\pntxta )}}\n"
                + "{\\*\\pnseclvl8\\pnlcltr\\pnstart1\\pnindent720\\pnhang{\\pntxtb (}{\\pntxta )}}\n"
                + "{\\*\\pnseclvl9\\pnlcrm\\pnstart1\\pnindent720\\pnhang{\\pntxtb (}{\\pntxta )}}\n"
                + "\\pard\\plain \\ql \\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 \n"
                + "\\fs24\\lang1033\\langfe1033\\cgrid\\langnp1033\\langfenp1033 ");
    }

    /**
     * Encodes a Unicode char to a series of RTF escaped chars in unicode
     * (\uDDDDD) where DDDDD is the decimal code of the char.
     */
    private String encodeChar(char ch)
    {
        short code = (short) ch;

        /*
         * if (code == 0x5c || code == 0x7b || code == 0x7d) // {,\,} { return
         * "\\" + ch; } else
         */if (0x20 <= code && code < 0x80)
        {
            return String.valueOf(ch);
        }
        else
        // if (code > 0xff || code < 0)
        {
            StringBuffer sb = new StringBuffer(8);

            sb.append("\\u");
            sb.append(code);
            sb.append(" ");

            return sb.toString();
        }
    }

    private String replaceUnicodeChars(String p_string)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);
            result.append(encodeChar(ch));
        }

        return result.toString();
    }

    /**
     * Removes all TMX 1.4 <ut> elements from the segment. <ut> is special since
     * it does not surround embedded tags but text, witch must be pulled out of
     * the <ut> and added to the parent segment.
     */
    private Element removeUtElements(Element p_seg)
    {
        ArrayList elems = new ArrayList();

        findUtElements(elems, p_seg);

        for (int i = 0; i < elems.size(); i++)
        {
            Element ut = (Element) elems.get(i);

            removeUtElement(ut);
        }

        return p_seg;
    }

    /**
     * Removes the given TMX 1.4 <ut> element from the segment. <ut> is special
     * since it does not surround embedded tags but text, witch must be pulled
     * out of the <ut> and added to the parent segment.
     */
    private void removeUtElement(Element p_element)
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
            Node node = (Node) content.get(i);

            newContent.add(node.detach());
        }

        Collections.reverse(newContent);
        parent.clearContent();

        for (int i = 0, max = newContent.size(); i < max; ++i)
        {
            Node node = (Node) newContent.get(i);

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

    private void findUtElements(ArrayList p_result, Element p_element)
    {
        // Depth-first traversal: add embedded <ut> to the list first.
        for (int i = 0, max = p_element.nodeCount(); i < max; i++)
        {
            Node child = (Node) p_element.node(i);

            if (child instanceof Element)
            {
                findUtElements(p_result, (Element) child);
            }
        }

        if (p_element.getName().equals("ut"))
        {
            p_result.add(p_element);
        }
    }

    public String removeRtfParagraphs(String p_text)
    {
        RE re = new RE(PARAGRAPH_SEARCH_PATTERN, RE.MATCH_SINGLELINE);

        return re.subst(p_text, "");
    }

    public void setOldHeader(Element p_element)
    {
        m_header = new Tmx(p_element);
    }

    public void writeDummyParagraph()
    {
        String sourceLang = m_header.getSourceLang();

        if (sourceLang == null || sourceLang.length() == 0)
        {
            sourceLang = Tmx.DEFAULT_SOURCELANG;
        }

        writeEntry("SOURCELANG='" + sourceLang + "' \\par");

        /*
         * To write the entire header, use this: String xml =
         * m_header.getHeaderXml(); writeEntry(xml); writeEntry("\\par\n");
         */
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String convertToRtf(String p_url) throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        info("Converting TMX file to RTF: `" + p_url + "'");

        startOutputFile(baseName);

        m_entryCount = 0;

        // Reading from a file, need to use Xerces.
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        reader.setEntityResolver(DtdResolver.getInstance());
        reader.setValidation(true);

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
                setOldHeader(element);

                Element prop = (Element) element
                        .selectSingleNode("/prop[@type='RTFFontTable']");

                if (prop != null)
                    writeEntry(prop.getText());

                prop = (Element) element
                        .selectSingleNode("/prop[@type='RTFStyleSheet']");

                if (prop != null)
                    writeEntry(prop.getText());

                writeOtherRtfHeader();

                writeDummyParagraph();

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

                element = removeUtElements(element);

                writeEntry(replaceUnicodeChars(removeRtfParagraphs(element
                        .asXML())));
                writeEntry("\\par");

                // prune the current element to reduce memory
                element.detach();

                element = null;

                if (m_entryCount % 1000 == 0)
                {
                    debug("Entry " + m_entryCount);
                }
            }
        });

        Document document = reader.read(p_url);

        closeOutputFile();

        info("Processed " + m_entryCount + " TUs into file `" + m_filename
                + "'");

        return m_filename;
    }

    static public void main(String[] argv) throws Exception
    {
        TradosTmxToRtf a = new TradosTmxToRtf();

        if (argv.length != 1)
        {
            System.err.println("Usage: TradosTmxToRtf FILE\n");
            System.exit(1);
        }

        a.convertToRtf(argv[0]);
    }
}
