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

package com.globalsight.exporter;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>The RMI interface implementation for an Exporter.</p>
 *
 * <p>Export is implememted by a Producer-Consumer pipeline that reads
 * entries from a database and writes them to the export file.</p>
 */
public abstract class IExportManagerImpl
    implements IExportManager
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            IExportManagerImpl.class);

    /**
     * The size of batches that are read from the database or written
     * to the file. (?)
     */
    static final private int BATCHSIZE = 10;

    //
    // Private Members
    //
    protected SessionInfo m_session;
    private String m_filename = null;
    private IProcessStatusListener m_listener = null;
    private IReader m_reader = null;
    private IWriter m_writer = null;
    private ExportOptions m_options = null;
    private static int counter = 0;

    //
    // Constructor
    //
    protected IExportManagerImpl(SessionInfo p_session)
    {
        m_session = p_session;
    }

    /**
     * Finishes construction of the needed objects after the *derived*
     * classes have been fully initialized (the derived class must
     * call this method in its constructor: super.init() ).
     */
    protected void init()
        throws ExporterException
    {
        m_options = createExportOptions();
        m_reader = createReader(m_options);
    }

    /**
     * Attaches an export event listener.
     */
    public void attachListener(IProcessStatusListener p_listener)
    {
        m_listener = p_listener;
    }

    /**
     * Detaches an export event listener.
     */
    public void detachListener(IProcessStatusListener p_listener)
    {
        m_listener = null;
    }

    /**
     * Sets the export options that guide the export process.
     * @param options an XML string.
     */
    public void setExportOptions(String p_options)
        throws ExporterException
    {
        m_options.init(p_options);

        m_reader.setExportOptions(m_options);

        // TODO: recreate writer if FILETYPE is changed
        if (m_writer == null)
        {
            m_writer = createWriter(m_options);
        }
        else
        {
            m_writer.setExportOptions(m_options);
        }
    }

    /**
     * Returns ExportOptions as XML string.
     */
    public String getExportOptions()
        throws ExporterException
    {
        return m_options.getXml();
    }

    public ExportOptions getExportOptionsObject()
    {
        return m_options;
    }

    /**
     * Sets the name of the file to be exported.
     */
    public ExportOptions setExportFile(String p_filename)
        throws ExporterException
    {
        m_filename = p_filename;

        String fileName = m_options.getFileName();
		if (fileName == null || fileName.equals(""))
		{
			m_options.setFileName(m_filename);
		}

        return m_options;
    }

    /**
     * Gets the name of the file to export to.
     */
    public String getExportFile()
        throws ExporterException
    {
        return m_filename;
    }

    /**
     * Analyzes export options and termbase and returns a count of how
     * many entries will be exported.
     *
     * For CSV files: also analyzes the columns and proposes column types.
     *
     * @return newly computed ExportOptions as XML string.
     */
    public String analyze()
        throws ExporterException
    {
        CATEGORY.info("Starting database analysis");

        // We need an output file. Grab one.
        m_options = setExportFile(createFilename(m_options));

        m_reader.setExportOptions(m_options);
        m_options = m_reader.analyze();

        m_writer.setExportOptions(m_options);
        m_options = m_writer.analyze();

        return m_options.getXml();
    }

	public String analyzeTm() throws ExporterException
	{
		CATEGORY.info("Starting database analysis");

		m_options = setExportFile(createFilename(m_options));
		m_reader.setExportOptions(m_options);
		m_writer.setExportOptions(m_options);

		return m_options.getXml();
	}

    public void doExport()
        throws ExporterException
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runExport();
                }
                catch(Exception e)
                {
                    CATEGORY.error("ExportManager::doTestImport", e);
                }
            }
        };

        MultiCompanySupportedThread t = new MultiCompanySupportedThread(runnable);
        t.setName("EXPORTER" + String.valueOf(counter++));
        t.start();
    }
    /**
     * With all ExportOptions set, start the actual export.
     */
    public void runExport()
        throws ExporterException
    {
        CATEGORY.info("Starting database export to file " + m_filename);
        String tmIdentifyKey = null;
        if (m_options instanceof com.globalsight.everest.tm.exporter.ExportOptions)
        {
            com.globalsight.everest.tm.exporter.ExportOptions options = (com.globalsight.everest.tm.exporter.ExportOptions) m_options;
            tmIdentifyKey = options.getIdentifyKey();
        }

        ArrayList entries = new ArrayList(BATCHSIZE);
        ReaderResult result;

        int counter = 0, errCounter = 0;
        int expectedEntries = m_options.getExpectedEntryCount();

        try
        {
            m_writer.writeHeader(m_session);

            m_reader.start();

            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == result.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;
                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                entries.add(result.getResultObject());

                if (entries.size() == BATCHSIZE)
                {
                    try
                    {
                        m_writer.write(entries, m_session);
                    }
                    catch (IOException ex)
					{
						CATEGORY.error("error writing entries", ex);
					}

                    entries.clear();
                }

                showStatus(counter, expectedEntries, "");
            }

            // flush remaining entries
            if (entries.size() > 0)
            {
                try
				{
					m_writer.write(entries, m_session);
				}
                catch (IOException ex)
				{
					CATEGORY.error("error writing entries", ex);
				}
            }

            m_writer.writeTrailer(m_session);
        }
        catch (IOException ignore)
		{
            ExportUtil.handleTmExportFlagFile(tmIdentifyKey, "failed", true);
			CATEGORY.error("client error", ignore);
		}
        catch (Throwable ignore)
		{
            ExportUtil.handleTmExportFlagFile(tmIdentifyKey, "failed", true);
			CATEGORY.error("client error", ignore);
		}
        finally
        {
            m_reader.stop();

            ExportUtil.handleTmExportFlagFile(tmIdentifyKey, "inprogress", false);

            // We're done, bump progress bar to 100%.
//            String message = "export finished; " + counter +
//                " entries processed, " + errCounter +
//                " error" + (errCounter == 1 ? "" : "s");
            String pattern = "export finished; {0} entries processed, {1} error(s)";
            String message = ProcessStatus.getStringFormattedFromResBundle(m_listener,
                    "lb_export_result_pattern", pattern, counter, errCounter);
            try
            {
                speak(counter, 100, message);
            }
            catch (Throwable ignore) {}
        }
    }


    /**
     * Create the appropriate ExportOptions for this export.
     */
    abstract protected ExportOptions createExportOptions()
        throws ExporterException;

    /**
     * Create the appropriate ExportOptions for this export.
     */
    abstract protected ExportOptions createExportOptions(String p_options)
        throws ExporterException;

    /**
     * Returns a new file name to use for this export.
     */
    abstract protected String createFilename(ExportOptions p_options)
        throws ExporterException;

    /**
     * Create the appropriate reader to read the database.
     */
    abstract protected IReader createReader(ExportOptions p_options)
        throws ExporterException;

    /**
     * Create the appropriate writer to write the export file.
     */
    abstract protected IWriter createWriter(ExportOptions p_options)
        throws ExporterException;

    //
    // Private Methods
    //

    /** Notifies the event listener of the current export status. */
    protected void speak(int p_entryCount, int p_percentage, String p_message)
        throws RemoteException, IOException
    {
        IProcessStatusListener listener = m_listener;

        if (listener != null)
        {
            listener.listen(p_entryCount, p_percentage, p_message);
        }
    }

    /**
     * Helper method to speak only when appropriate so the web-client
     * is not flooded with traffic but still believes export has not
     * died.
     */
    protected void showStatus(int p_current, int p_expected, String p_message)
        throws IOException
    {
        int percentComplete =
            (int)((p_current * 1.0 / p_expected * 1.0) * 100.0);

        if (percentComplete > 100)
        {
            percentComplete = 100;
        }

        // Decide when to update the user's display.
        //   With error message: always
        //
        //   For   1-  10 expected entries, always update
        //   For  11- 100 expected entries, update after every 5th entry
        //   For 101-1000 expected entries, update after every 20th
        //   For more than 1000 entries, update after every 50th
        //
        if ((p_message.length() > 0) ||
            (p_expected <    10) ||
            (p_expected >=   10 && p_expected <   100 &&
                (p_current %  5 == 0)) ||
            (p_expected >=  100 && p_expected <  1000 &&
                (p_current % 20 == 0)) ||
            (p_expected >= 1000 &&
                (p_current % 50 == 0)))
        {
            speak(p_current, percentComplete, p_message);
        }
    }
}
