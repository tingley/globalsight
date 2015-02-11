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

package com.globalsight.everest.snippet.importer;

import org.apache.log4j.Logger;

import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.IImportManagerImpl;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.importer.ImporterException;
import com.globalsight.util.ReaderResult;


import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SessionInfo;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.snippet.Snippet;
import com.globalsight.everest.snippet.SnippetException;
import com.globalsight.everest.snippet.SnippetImpl;
import com.globalsight.everest.snippet.SnippetLibrary;

import java.rmi.RemoteException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * <p>The RMI interface implementation for a Snippet File Importer
 * which needs to read through a file, parse out the snippets and
 * import (persist) them into the system.</p>
 */
public class SnippetImporter
    extends IImportManagerImpl
{
    private static final Logger c_logger =
        Logger.getLogger(
            SnippetImporter.class);

    private static SnippetLibrary c_library = null;

    //
    // Constructor
    //

    public SnippetImporter(SessionInfo p_session)
        throws ImporterException
    {
        super(p_session);

        init();
    }

    public SnippetImporter(String p_filename, String p_options)
        throws ImporterException
    {
        super(p_filename, p_options);

        init();
    }

    /**
     * Connect to the snippet library for adding/modifying snippets.
     */
    private void init()
        throws ImporterException
    {
        if (c_library == null)
        {
            try
            {
                c_library = ServerProxy.getSnippetLibrary();
            }
            catch (GeneralException ge)
            {
                String message = "Couldn't retrieve the snippet library.";

                c_logger.error(message, ge);

                String args[] = {message};
                throw new ImporterException(
                    ImporterException.MSG_IMPORT_FAILED, args, ge);
            }
        }
    }

    //
    // Overwritten Abstract Methods
    //

    protected ImportOptions createImportOptions()
        throws ImporterException
    {
        return new com.globalsight.everest.snippet.importer.ImportOptions();
    }

    protected ImportOptions createImportOptions(String p_options)
        throws ImporterException
    {
        com.globalsight.everest.snippet.importer.ImportOptions result =
            new com.globalsight.everest.snippet.importer.ImportOptions();

        result.init(p_options);

        return result;
    }

    /**
     * With all ImportOptions set, run a test import.
     */
    protected void runTestImport()
        throws ImporterException
    {
        ReaderResult result;
        String message;
        int counter = 0, warnCounter = 0, errCounter = 0;
        int expectedEntries = getOptions().getExpectedEntryCount();

        if (expectedEntries == 0)
        {
            message =
                "The import file does not contain any snippets. " +
                "Perhaps you chose the wrong encoding?";

            try
            {
                speak(0, 100, message);
            }
            catch (Throwable ignore) {}

            return;
        }

        HashSet names = new HashSet();

        try
        {
            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == ReaderResult.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;

                    c_logger.error("couldn't read snippet " + counter +
                        ": " + result.getMessage());

                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                Snippet s = (Snippet)result.getResultObject();

                try
                {
                    // check if snippet exists and issue warning
                    GlobalSightLocale nullLocale = null;
                    ArrayList existing =
                        c_library.getSnippets(s.getName(), nullLocale);

                    if (existing != null && existing.size() > 0)
                    {
                        ++warnCounter;

                        message = "WARNING: snippet `" + s.getName() +
                            "' already exists.<BR>The snippet and all its " +
                            "derived copies will be DELETED during import.";

                        showStatus(counter, expectedEntries, message);
                        continue;
                    }
                    else
                    {
                        // Remember new snippets in the file but not in db
                        if (names.contains(s.getName()))
                        {
                            ++warnCounter;

                            message = "WARNING: duplicate snippet `" +
                                s.getName() + "' in input file.";

                            showStatus(counter, expectedEntries, message);
                            continue;
                        }
                        else
                        {
                            names.add(s.getName());
                        }
                    }
                }
                catch (Throwable ex)
                {
                    c_logger.error("Error checking snippet " + s.getName(),
                        ex);

                    showStatus(counter, expectedEntries,
                        "Snippet " + counter + ": " + ex.getMessage());
                    continue;
                }

                showStatus(counter, expectedEntries, "");
            }
        }
        catch (IOException ignore)
        {
            c_logger.error("client error", ignore);
            // Our client's web-client just died, return.
        }
        catch (Throwable ignore)
        {
            c_logger.error("client error", ignore);
            // Our client's web-client just died, return.
        }
        finally
        {
            // We're done, bump progress bar to 100%.
            message = "<BR>Test import finished: " +
                counter + " entries processed, " +
                errCounter + " error" + (errCounter == 1 ? "" : "s") + ", " +
                warnCounter + " warning" + (warnCounter == 1 ? "" : "s") + ".";

            try
            {
                speak(counter, 100, message);
            }
            catch (Throwable ignore) {}
        }
    }

    /**
     * With all ImportOptions set, start the actual import.
     */
    protected void runImport()
        throws ImporterException
    {
        ReaderResult result;

        int counter = 0, errCounter = 0;
        int expectedEntries = getOptions().getExpectedEntryCount();

        if (expectedEntries == 0)
        {
            String message =
                "The import file does not contain any snippets. " +
                "Perhaps you chose the wrong encoding?";

            try
            {
                speak(0, 100, message);
            }
            catch (Throwable ignore) {}

            return;
        }

        try
        {
            while (m_reader.hasNext())
            {
                result = m_reader.next();
                ++counter;

                if (result.getStatus() == ReaderResult.ERROR)
                {
                    // Couldn't read the entry, output message and continue.
                    ++errCounter;

                    c_logger.error("couldn't read snippet " + counter +
                        ": " + result.getMessage());

                    showStatus(counter, expectedEntries, result.getMessage());
                    continue;
                }

                Snippet s = null;

                try
                {
                    s = (Snippet)result.getResultObject();

                    // call modify - does an add if it doesn't exist.
                    c_library.modifySnippet(m_session.getUserName(), s, true);
                }
                catch (Throwable ex)
                {
                    c_logger.error("Error adding/modifying snippet ", ex);
                    showStatus(counter, expectedEntries,
                        "Snippet " + counter + ": " + ex.getMessage());
                    continue;
                }

                showStatus(counter, expectedEntries, "");
            }
        }
        catch (IOException ignore)
        {
            c_logger.error("client error", ignore);
            // Our client's web-client just died, return.
        }
        catch (Throwable ignore)
        {
            c_logger.error("client error", ignore);
            // Our client's web-client just died, return.
        }
        finally
        {
            // We're done, bump progress bar to 100%.
            String message = "<BR>Import finished: " + counter +
                " entries processed, " + errCounter +
                " error" + (errCounter == 1 ? "" : "s") + ".";
            try
            {
                speak(counter, 100, message);
            }
            catch (Throwable ignore) {}
        }
    }

    /**
     *  Create the specific SnippetFileReader to read through the file.
     */
    protected IReader createReader(ImportOptions p_options)
        throws ImporterException
    {
        return new SnippetFileReader (p_options);
    }
}
