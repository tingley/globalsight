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

package com.globalsight.everest.tm.importer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.tm.Tm;
import com.globalsight.importer.IImportManagerImpl;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.persistence.error.BatchException;
import com.globalsight.ling.tm2.persistence.error.ErrorRecorder;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.progress.ProcessStatus;

/**
 * <p>
 * The RMI interface implementation for the TMX Importer.
 * </p>
 */
public class ImportManager extends IImportManagerImpl implements Serializable
{
    private static final long serialVersionUID = -7258414295466917464L;

    private static final Logger LOGGER = Logger
            .getLogger(ImportManager.class);

    /**
     * The size of batches that are sent to the termbase.
     */
    static final public int BATCHSIZE = 1000;

    // Update after every FLUSH_NUMBER entry.
    private static int FLUSH_NUMBER = 200;

    private Tm tm = null;

    public ImportManager(Tm p_tm, SessionInfo p_session)
            throws ImporterException
    {
        super(p_session);

        tm = p_tm;

        LOGGER.debug("Default TM import manager created.");
    }

    protected ImportManager(Tm p_tm, SessionInfo p_session,
            String p_filename, String p_options) throws ImporterException
    {
        super(p_filename, p_options, p_session);

        tm = p_tm;
    }

    protected ImportOptions createImportOptions() throws ImporterException
    {
        return new com.globalsight.everest.tm.importer.ImportOptions();
    }

    protected ImportOptions createImportOptions(String p_options)
            throws ImporterException
    {
        com.globalsight.everest.tm.importer.ImportOptions result = new com.globalsight.everest.tm.importer.ImportOptions();

        result.init(p_options);

        return result;
    }

    /**
     * With all ImportOptions set, start the actual import.
     */
    protected void runImport() throws ImporterException
    {
        LOGGER.info("Importing TM file " + getFileName());

        ErrorRecorder.init(new File(getFileName()));
        ArrayList entries = new ArrayList(BATCHSIZE);
        ReaderResult result;

        int counter = 0, errCounter = 0, errorTuv = 0;

        ImportOptions options = getOptions();
        int expectedEntries = options.getExpectedEntryCount();

        int syncMode = mapSyncMode((com.globalsight.everest.tm.importer.ImportOptions) options);
        LOGGER.info("Expected entry count == " + expectedEntries + " in ["
                + tm.getName() + "], mode == " + syncMode);

        String sourceTmName = ((com.globalsight.everest.tm.importer.ImportOptions) options)
                .getSourceTmName();

        try
        {
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

                // for gbs-753:When import tmx,if the "file options" >
                // "import file format" is
                // "WorldServer TMX", remove "\" before "{" and "}".
                String import_file_format = options.getFileOptions().m_type;
                if ("tmx-worldserver".equalsIgnoreCase(import_file_format))
                {
                    try
                    {
                        SegmentTmTu stt = (SegmentTmTu) result
                                .getResultObject();
                        Set allLocales = stt.getAllTuvLocales();
                        if (allLocales != null && allLocales.size() > 0)
                        {
                            Iterator allLocalesIt = allLocales.iterator();
                            while (allLocalesIt.hasNext())
                            {
                                GlobalSightLocale gsl = (GlobalSightLocale) allLocalesIt
                                        .next();
                                // may contains more than one tuv for one locale
                                // in one tu
                                Collection tuvList = stt.getTuvList(gsl);
                                if (tuvList != null && tuvList.size() > 0)
                                {
                                    Iterator tuvIt = tuvList.iterator();
                                    while (tuvIt.hasNext())
                                    {
                                        BaseTmTuv btt = (BaseTmTuv) tuvIt
                                                .next();
                                        String segment = btt.getSegment();
                                        if (segment != null
                                                && !"".equals(segment.trim()))
                                        {
                                            segment = segment.replace("\\{",
                                                    "{").replace("\\}", "}");
                                            btt.setSegment(segment);
                                        }
                                        stt.addTuv(btt);
                                    }
                                }
                            }
                        }

                        result.setResultObject(stt);
                    }
                    catch (Exception ex)
                    {
                        LOGGER.error("Failed to escape '\\' before { and } for tmx-worldserver file.");
                    }
                }

                entries.add(result.getResultObject());

                if (entries.size() == BATCHSIZE)
                {
                    try
                    {
                        flushEntries(entries, syncMode, sourceTmName);
                    }
                    catch (BatchException e)
                    {
                        ++errCounter;
                        ErrorRecorder.record(new File(getFileName()), e,
                                m_listener, errorTuv);
                        errorTuv += e.getTuvs().size();
                        LOGGER.error("error adding entries", e);
                    }
                    catch (Throwable ex)
                    {
                        ++errCounter;

                        LOGGER.error("error adding entries", ex);

                        String msg = ProcessStatus
                                .getStringFormattedFromResBundle(m_listener,
                                        "lb_import_cannot_add_entries_pattern",
                                        "Can't add a batch of {0} entries",
                                        entries.size());
                        showStatus(counter, expectedEntries,
                                msg + ": " + ex.getMessage());
                    }

                    entries.clear();
                }

                if (counter % FLUSH_NUMBER == 0)
                {
                    showStatus(counter, expectedEntries, "");
                }
            }

            // flush remaining entries
            if (entries.size() > 0)
            {
                try
                {
                    flushEntries(entries, syncMode, sourceTmName);
                }
                catch (BatchException e)
                {
                    ++errCounter;
                    ErrorRecorder.record(new File(getFileName()), e,
                            m_listener, errorTuv);
                    errorTuv += e.getTuvs().size();

                    LOGGER.error("error adding entries", e);
                }
                catch (Exception ex)
                {
                    ++errCounter;

                    LOGGER.error("error adding entries", ex);

                    String msg = ProcessStatus.getStringFormattedFromResBundle(
                            m_listener, "lb_import_cannot_add_entries_pattern",
                            "Can't add a batch of {0} entries", entries.size());
                    showStatus(counter, expectedEntries,
                            msg + ": " + ex.getMessage());
                }

                entries.clear();
            }

            if (errorTuv > 0)
            {
                String pattern = "<div style=\"color: red;\">Ignored {0} tuv(s) with errors</div><br><A CLASS='standardHREF' HREF='#' onclick=''lookFile(\"{1}\")''>Click here to view it.</A><br><br>";
                String message = ProcessStatus.getStringFormattedFromResBundle(
                        m_listener, "lb_import_tm_tuv_error", pattern,
                        errorTuv, ErrorRecorder.getStorePath(getFileName()));
                showStatus(counter, expectedEntries, message);
            }

            LOGGER.info("Done importing TM file " + getFileName());
        }
        catch (IOException ignore)
        {
            LOGGER.error("client error", ignore);
            // Our client's web-client just died, return.
        }
        catch (Throwable ignore)
        {
            LOGGER.error("client error", ignore);
            // Our client's web-client just died, return.
        }
        finally
        {
            // We're done, bump progress bar to 100%.
            // String message = "import finished; " + counter
            // + " entries processed, " + errCounter + " error"
            // + (errCounter == 1 ? "" : "s");

            String pattern = "import finished; {0} entries processed, {1} error(s)";
            String message = ProcessStatus.getStringFormattedFromResBundle(
                    m_listener, "lb_import_result_pattern", pattern, counter,
                    errCounter);
            try
            {
                speak(counter, 100, message);
            }
            catch (Throwable ignore)
            {
            }
        }
    }

    protected IReader createReader(ImportOptions p_options)
            throws ImporterException
    {
        return new TmxReader(p_options, tm);
    }

    private int mapSyncMode(
            com.globalsight.everest.tm.importer.ImportOptions p_options)
    {
        String mode = p_options.getSyncMode();

        if (mode.equals(com.globalsight.everest.tm.importer.ImportOptions.SYNC_OVERWRITE))
        {
            return TmCoreManager.SYNC_OVERWRITE;
        }
        else if (mode
                .equals(com.globalsight.everest.tm.importer.ImportOptions.SYNC_MERGE))
        {
            return TmCoreManager.SYNC_MERGE;
        }
        else if (mode
                .equals(com.globalsight.everest.tm.importer.ImportOptions.SYNC_DISCARD))
        {
            return TmCoreManager.SYNC_DISCARD;
        }

        return -1;
    }

    private void flushEntries(ArrayList p_entries, int p_syncMode,
            String p_sourceTmName) throws Exception
    {
        LingServerProxy.getTmCoreManager().saveToSegmentTm(tm,
                p_entries, p_syncMode, p_sourceTmName);
    }

    protected void showStatus(int p_current, int p_expected, String p_message)
            throws IOException
    {
        int percentComplete = (int) ((p_current * 1.0 / p_expected * 1.0) * 100.0);

        if (percentComplete > 100)
        {
            percentComplete = 100;
        }

        speak(p_current, percentComplete, p_message);

    }
    
    /**
     * Simulates the conversion of lines to entries and returns error messages,
     * but doesn't write data to the database.
     */
    protected void runTestImport() throws ImporterException
    {
        LOGGER.debug("**TESTING** import of file " + getFileName());

        ReaderResult result;

        int counter = 0;
        int errCounter = 0;
        int expectedEntries = getOptions().getExpectedEntryCount();

        try
        {
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

                if (counter % FLUSH_NUMBER == 0)
                {
                    showStatus(counter, expectedEntries, "");
                }
            }
        }
        catch (IOException ignore)
        {
            // Our client's web-client just died.
            LOGGER.info("client died, aborting import ("
                    + ignore.getMessage() + ")");
        }
        catch (Throwable ignore)
        {
            LOGGER.error("unexpected error, aborting import ("
                    + ignore.getMessage() + ")");
        }
        finally
        {
            // We're done, bump progress bar to 100%.
            String message = "test finished; " + counter
                    + " entries processed, " + errCounter + " error"
                    + (errCounter == 1 ? "" : "s");
            try
            {
                speak(counter, 100, message);
            }
            catch (Throwable ignore)
            {
            }
        }
    }
}
