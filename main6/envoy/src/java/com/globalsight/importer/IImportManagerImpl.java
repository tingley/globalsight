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

package com.globalsight.importer;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.l18n.L18nable;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.progress.IProcessStatusListener;

/**
 * <p>The RMI interface implementation for a basic Importer.
 *    That needs to read through a file, parse out pieces of
 *    it and import into the system.</p>
 *    It also provides feedback to the user as the import progresses.
 */
public abstract class IImportManagerImpl
    implements IImportManager, L18nable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            IImportManagerImpl.class);

    //
    // Private Members
    //

    // stores user and date information about the request if
    // needed during the processing
    protected SessionInfo m_session = null;
    // the file name to import
    private String m_filename = null;
    protected IProcessStatusListener m_listener = null;
    protected IReader m_reader = null;
    protected boolean m_deleteAfterImport = false;
    private ImportOptions m_options = null;
    private static int counter = 0;
    private ResourceBundle bundle;

    //
    // Constructor
    //

    protected IImportManagerImpl(SessionInfo p_session)
        throws ImporterException
    {
        m_session = p_session;
        m_options = createImportOptions();
    }

    // For snippet import, session is irrelevant.
    protected IImportManagerImpl(String p_filename, String p_options)
        throws ImporterException
    {
        m_filename = p_filename;
        m_options = createImportOptions(p_options);
        m_options.setFileName(m_filename);

        m_reader = createReader(m_options);
    }

    protected IImportManagerImpl(String p_filename, String p_options,
        SessionInfo p_session)
        throws ImporterException
    {
        m_session = p_session;
        m_filename = p_filename;
        m_options = createImportOptions(p_options);
        m_options.setFileName(m_filename);

        m_reader = createReader(m_options);
    }

    protected String getFileName()
    {
        return m_filename;
    }

    protected ImportOptions getOptions()
    {
        return m_options;
    }

    /**
     * Attaches an import event listener.
     */
    public void attachListener(IProcessStatusListener p_listener)
    {
        m_listener = p_listener;
    }

    /**
     * Detaches an import event listener.
     */
    public void detachListener(IProcessStatusListener p_listener)
    {
        m_listener = null;
    }

    /**
     * Sets the import options that guide the import process.
     * @param options an XML string.
     */
    public void setImportOptions(String p_options)
        throws ImporterException
    {
        m_options.init(p_options);

        if (m_reader != null)
        {
            m_reader.setImportOptions(m_options);
        }
    }

    /**
     * Returns ImportOptions as XML string.
     */
    public String getImportOptions()
        throws ImporterException
    {
        return m_options.getXml();
    }

    /**
     * Sets the name of the file to be imported.
     */
    public void setImportFile(String p_filename, boolean p_deleteAfterImport)
        throws ImporterException
    {
        m_filename = p_filename;
        m_deleteAfterImport = p_deleteAfterImport;
        m_options.setFileName(m_filename);

        m_reader = createReader(m_options);
    }

    /**
     * Validates the file format.
     */
    public String analyzeFile()
        throws ImporterException
    {
        if (m_reader instanceof L18nable)
        {
            L18nable l18nable = (L18nable) m_reader;
            l18nable.setBundle(bundle);
        }
        m_options = m_reader.analyze();
        return m_options.getXml();
    }

    public void doTestImport()
        throws ImporterException
    {
        m_reader = createReader(m_options);
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runTestImport();
                }
                catch(Exception e)
                {
                    CATEGORY.error("ImportManager::doTestImport", e);
                }
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("IMPORTER" + String.valueOf(counter++));
        t.start();
    }

    public void doImport()
        throws ImporterException
    {
        m_reader = createReader(m_options);
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                try
                {
                    runImport();
                    if (m_deleteAfterImport)
                    {
                        boolean success = new File(getFileName()).delete();
                        if (! success)
                        {
                            CATEGORY.warn("Failed to delete imported file "
                                    + getFileName());
                        };
                    }

                }
                catch(Exception e)
                {
                    CATEGORY.error("ImportManager::doTestImport", e);
                }
            }
        };

        //Thread t = new Thread(runnable);
        Thread t = new MultiCompanySupportedThread(runnable);
        //Thread t = new TmImportThread(CompanyThreadLocal.getInstance().getValue());
        t.setName("IMPORTER" + String.valueOf(counter++));
        t.start();
    }

    /**
     * Create the appropriate ImportOptions for this import.
     */
    abstract protected ImportOptions createImportOptions()
        throws ImporterException;
    abstract protected ImportOptions createImportOptions(String p_options)
        throws ImporterException;

    /**
     * With all ImportOptions set, runs a test import.
     */
    abstract protected void runTestImport()
        throws ImporterException;

    /**
     * With all ImportOptions set, starts the actual import.
     */
    abstract protected void runImport()
        throws ImporterException;

    /*
     * Create the appropriate reader to read the file.
     */
    abstract protected IReader createReader(ImportOptions p_options)
        throws ImporterException;

    //
    // Private Methods
    //

    /** Notifies the event listener of the current import status. */
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
     * is not flooded with traffic but still believes import has not
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
    
    @Override
    public void setBundle(ResourceBundle bundle)
    {
        this.bundle = bundle;
    }
}
