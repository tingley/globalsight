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

package com.globalsight.everest.projecthandler.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.globalsight.calendar.ReservedTime;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.exporter.ExportOptions;
import com.globalsight.exporter.IWriter;
import com.globalsight.util.SessionInfo;

/**
 * Writes entries to a CSV file as directed by the conversion settings in the
 * supplied export options.
 */
public class CsvWriter implements IWriter
{
    private static final Logger s_logger = Logger.getLogger(CsvWriter.class);

    //
    // Private Member Variables
    //
    private Project m_project;
    private com.globalsight.everest.projecthandler.exporter.ExportOptions m_options;
    private PrintWriter m_output;
    private String m_filename;

    //
    // Constructors
    //

    public CsvWriter(ExportOptions p_options, Project p_project)
    {
        m_project = p_project;
        setExportOptions(p_options);
    }

    //
    // Interface Implementation -- IWriter
    //

    public void setExportOptions(ExportOptions p_options)
    {
        m_options = (com.globalsight.everest.projecthandler.exporter.ExportOptions) p_options;
    }

    /**
     * Analyzes export options and returns an updated ExportOptions object with
     * a status whether the options are syntactically correct.
     */
    public ExportOptions analyze()
    {
        return m_options;
    }

    /**
     * Writes the file header.
     */
    public void writeHeader(SessionInfo p_session) throws IOException
    {
        m_filename = m_options.getFileName();

        String directory = ExportUtil.getExportDirectory();
        String companyId = CompanyThreadLocal.getInstance().getValue();

        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
        {
            directory = directory
                    + CompanyWrapper.getCompanyNameById(companyId);

            File companyDir = new File(directory);
            if (!companyDir.exists())
            {
                companyDir.mkdirs();
            }

            directory += "/";
        }

        String encoding = m_options.getJavaEncoding();

        if (encoding == null || encoding.length() == 0)
        {
            throw new IOException("invalid encoding " + m_options.getEncoding());
        }

        String filename = directory + m_filename;

        new File(filename).delete();

        m_output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), encoding)));

        if (isUnicodeEncoding(encoding))
        {
            m_output.print("\uFEFF");
        }

        // TODO - The hard-coded values should be replaced...
        // if header is true, write the column headers
        if (m_options.getHeader().equalsIgnoreCase("true"))
        {
            String separator = m_options.getSeparatorChar();

            m_output.println("Username" + separator + "Subject" + separator
                    + "Type" + separator + "Starts at" + separator + "Ends at");
        }

        checkIOError();
    }

    /**
     * Writes the file trailer and closes the file.
     */
    public void writeTrailer(SessionInfo p_session) throws IOException
    {
        m_output.close();
    }

    /**
     * Writes the next few entries to the file.
     * 
     * @param p_entries
     *            ArrayList of TU objects.
     */
    public void write(ArrayList p_entries, SessionInfo p_session)
            throws IOException
    {
        int size = p_entries.size();
        for (int i = 0; i < size; ++i)
        {
            write(p_entries.get(i), p_session);
        }
    }

    /**
     * Writes a single entry to the export file.
     */
    public void write(Object p_entry, SessionInfo p_session) throws IOException
    {
        if (p_entry == null)
        {
            return;
        }

        String separator = m_options.getSeparatorChar();
        String dateFormat = m_options.getSubType();

        try
        {
            ReservedTime o = (ReservedTime) p_entry;

            // Goes through lengths not to throw an IO exception,
            // will check below.
            TimeZone tz = o.getUserFluxCalendar().getTimeZone();

            StringBuffer sb = new StringBuffer();

            sb.append(UserUtil.getUserNameById(o.getUserFluxCalendar()
                    .getOwnerUserId()));
            sb.append(separator);
            sb.append(o.getDisplaySubject());
            sb.append(separator);
            sb.append(o.getType());
            sb.append(separator);
            sb.append(formatDate(o.getStartDate(), dateFormat, tz));
            sb.append(separator);
            sb.append(formatDate(o.getEndDate(), dateFormat, tz));

            m_output.println(sb.toString());
        }
        catch (Throwable ignore)
        {
            s_logger.error("Can't handle entry, skipping.", ignore);
        }

        checkIOError();
    }

    //
    // Private Methods
    //

    /*
     * Since PrintWriter.println() method does not throw any exception we'll
     * check for IO errors.
     */
    private void checkIOError() throws IOException
    {
        if (m_output.checkError())
        {
            throw new IOException("write error");
        }
    }

    /*
     * Create a Timestamp object by parsing the given date and the format
     * pattern.
     * 
     * @param p_date - A string representation of date (i.e. 01/30/04).
     * 
     * @param p_dateFormatPattern - The format pattern (i.e. MM/dd/yy).
     * 
     * @param p_timeZone - The time zone used for date creation.
     * 
     * @return The Timestamp object created based on the given string values.
     */
    private String formatDate(Date p_date, String p_dateFormatPattern,
            TimeZone p_timeZone)
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat(p_dateFormatPattern);

            sdf.setTimeZone(p_timeZone);
            return sdf.format(p_date);
        }
        catch (Exception e)
        {
            StringBuffer sb = new StringBuffer();
            sb.append("Failed to format date (date, format patter): (");
            sb.append(p_date);
            sb.append(",  ");
            sb.append(p_dateFormatPattern);
            sb.append(")");
            s_logger.error(sb.toString(), e);
            return p_date.toString();
        }
    }

    private boolean isUnicodeEncoding(String p_encoding)
    {
        if (p_encoding.toLowerCase().startsWith("utf")
                || p_encoding.toLowerCase().startsWith("unicode"))
        {
            return true;
        }

        return false;
    }
}
