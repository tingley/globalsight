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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import org.dom4j.Document;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IWriter;
import com.globalsight.terminology.Entry;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.util.HtmlUtil;
import com.globalsight.terminology.util.MappingContext;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.UTC;

/**
 * Writes an entry to a printable HTML file as directed by the
 * conversion settings in the supplied export options.
 */
public class HtmlWriter
    implements IWriter, TermbaseExceptionMessages
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            HtmlWriter.class);

    static public final String HTML_HEADER_START =
        "<HTML xmlns:m='http://www.w3.org/1998/Math/MathML'>\r\n<HEAD>\r\n" +
        "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />\r\n" +
        "<OBJECT ID='MathPlayer' CLASSID='clsid:32F66A20-7614-11D4-BD11-00104BD3F987'></OBJECT>\r\n" +
        "<?IMPORT NAMESPACE='m' IMPLEMENTATION='#MathPlayer' ?>\r\n";

    static public final String HTML_TITLE_START =
        "<TITLE>";

    static public final String HTML_TITLE_END =
        "</TITLE>\r\n";

    static public final String HTML_STYLESHEET =
        "<STYLE>\r\n" +
        "BODY {\r\n" +
        "       background-color: white;\r\n" +
        "       font-family: Arial Unicode MS, Arial, Helvetica, sans-serif;\r\n" +
        "       font-size: x-small;\r\n" +
        "       margin: 0px;\r\n" +
        "     }\r\n" +
        "\r\n" +
        "/* To avoid automatic paragraph spaces: */\r\n" +
        "P, UL, OL { margin-top: 0px; margin-bottom: 0px; }\r\n" +
        "\r\n" +
        "A { color: blue; }\r\n" +
        "\r\n" +
        ".vconceptGrp\r\n" +
        "              {\r\n" +
        "                border: 1px solid white;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vlanguageGrp,\r\n" +
        ".vsourceLanguageGrp,\r\n" +
        ".vtargetLanguageGrp\r\n" +
        "              {\r\n" +
        "                margin-left: 10px;\r\n" +
        "                margin-top: 0.1em;\r\n" +
        "                border: 1px solid white;\r\n" +
        "              }\r\n" +
        ".vtermGrp\r\n" +
        "              {\r\n" +
        "                margin-left: 10px;\r\n" +
        "                margin-top: 0.1em;\r\n" +
        "                border: 1px solid white;\r\n" +
        "              }\r\n" +
        ".vtransacGrp\r\n" +
        "              {\r\n" +
        "                margin-left: 10px;\r\n" +
        "                display: block;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vconceptlabel\r\n" +
        "              {\r\n" +
        "                font-weight: bold;\r\n" +
        "                margin-right: 0.5ex;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vconcept\r\n" +
        "              {\r\n" +
        "                font-weight: bold;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vlanguagelabel\r\n" +
        "              {\r\n" +
        "                display: none;\r\n" +
        "                margin-right: 0.5ex;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vlanguage\r\n" +
        "              {\r\n" +
        "                font-weight: bold;\r\n" +
        "                font-style: italic;\r\n" +
        "                color: cadetblue;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vtermlabel\r\n" +
        "              {\r\n" +
        "                display: none;\r\n" +
        "                margin-right: 0.5ex;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vsourceLanguageGrp .vsearchterm\r\n" +
        "              {\r\n" +
        "                font-size: small;\r\n" +
        "                font-weight: bold;\r\n" +
        "                color: darkmagenta;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vtargetLanguageGrp .vsearchterm\r\n" +
        "              {\r\n" +
        "                font-size: medium;\r\n" +
        "                font-weight: bold;\r\n" +
        "                color: blue;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vlanguageGrp .vsearchterm\r\n" +
        "              {\r\n" +
        "                font-size: small;\r\n" +
        "                font-weight: bold;\r\n" +
        "                color: black;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vsourceLanguageGrp .vterm\r\n" +
        "              {\r\n" +
        "                font-size: small;\r\n" +
        "                font-weight: bold;\r\n" +
        "                color: mediumorchid;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vtargetLanguageGrp .vterm\r\n" +
        "              {\r\n" +
        "                font-size: medium;\r\n" +
        "                font-weight: bold;\r\n" +
        "                color: blue;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vlanguageGrp .vterm\r\n" +
        "              {\r\n" +
        "                font-size: small;\r\n" +
        "                font-weight: bold;\r\n" +
        "                color: black;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vfakeConceptGrp\r\n" +
        "              {\r\n" +
        "                display: block;\r\n" +
        "              }\r\n" +
        ".vfakeTermGrp {\r\n" +
        "                border: 1px solid white;\r\n" +
        "              }\r\n" +
        "\r\n" +
        ".vfieldGrp\r\n" +
        "              {\r\n" +
        "                margin-left: 10px;\r\n" +
        "                border: 1px solid white;\r\n" +
        "              }\r\n" +
        ".vfieldlabel\r\n" +
        "              {\r\n" +
        "                font-weight: bold;\r\n" +
        "                margin-right: 0.5ex;\r\n" +
        "              }\r\n" +
        ".vfieldvalue\r\n" +
        "              {\r\n" +
        "                font-weight: medium;\r\n" +
        "              }\r\n" +
        ".vtransaclabel\r\n" +
        "              {\r\n" +
        "                font-size: xx-small;\r\n" +
        "                font-weight: bold;\r\n" +
        "                margin-right: 0.5ex;\r\n" +
        "              }\r\n" +
        ".vtransacvalue\r\n" +
        "              {\r\n" +
        "                font-size: xx-small;\r\n" +
        "                font-style: italic;\r\n" +
        "                color: gray;\r\n" +
        "              }\r\n" +
        "</STYLE>\r\n";

    static public final String HTML_END_HEADER_BODY_START =
        "</HEAD>\r\n<BODY>";

    static public final String HTML_BODY_END =
        "</BODY>\r\n</HTML>\r\n";

    static public final String HTML_HORIZONTAL_RULE = "<HR width='100%'/>\r\n";

    //
    // Private Member Variables
    //
    private com.globalsight.terminology.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;
    private Termbase m_database;
    private MappingContext m_context;

    //
    // Constructors
    //

    public HtmlWriter(ExportOptions p_options, Termbase p_database)
    {
        setExportOptions(p_options);

        m_database = p_database;

        try
        {
            m_context = new MappingContext(p_database.getDefinition());
        }
        catch (TermbaseException ex)
        {
            CATEGORY.error("Unexpected termbase definition error", ex);
        }
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
        String filename = directory + m_filename;

        // HTML files get written as Unicode (in UTF-8).
        m_output = new PrintWriter(new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")));

        m_output.print(HTML_HEADER_START);
        m_output.print(HTML_TITLE_START);
        m_output.print(getTitle());
        m_output.print(HTML_TITLE_END);
        m_output.print(HTML_STYLESHEET);
        m_output.print(HTML_END_HEADER_BODY_START);

        checkIOError();
    }

    /**
     * Generates an HTML header block with creation date, document
     * title etc.
     */
    private String getTitle()
    {
        StringBuffer result = new StringBuffer();

        String title =
            "GlobalSight Termbase \u00AB" + m_database.getName() + "\u00BB";

        result.append(title);
        result.append(" printed ");
        result.append(UTC.valueOf(new Date()));

        return result.toString();
    }

    /**
     * Writes the file trailer (eg for TBX).
     */
    public void writeTrailer(SessionInfo p_session)
        throws IOException
    {
        m_output.print(HTML_BODY_END);

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

        try
        {
            Entry entry = new Entry(entryXml);

            Document dom = entry.getDom();

            // Do we filter out system fields?
            if (m_options.getSystemFields().equalsIgnoreCase("false"))
            {
                dom = ExportUtil.removeSystemFields(dom);
            }

            entry.setDom(dom);

            m_output.println(HtmlUtil.xmlToHtml(
                entry.getDom().getRootElement(), m_context));
            m_output.print(HTML_HORIZONTAL_RULE);

            checkIOError();
        }
        catch (TermbaseException ex)
        {
            throw new IOException("internal error: " + ex.toString());
        }
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
