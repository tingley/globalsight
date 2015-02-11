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
package com.globalsight.cxe.adapter.filesystem.autoImport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.globalsight.cxe.entity.fileextension.FileExtension;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
//import com.globalsight.log.ActivityLog;
import com.globalsight.util.file.DirectoryMonitor;

/**
 * An AutomaticImportMonitor can be used to monitor a number of directories for
 * changes to trigger an import in CXE.
 * 
 * It uses a directory mapping file of the format: Directory | JobPrefixName |
 * FileProfileName
 * 
 * It will run separately in a low priority thread. It will also save its state
 * of its DirectoryMap so that on subsequent runs, file changes during system
 * downtime will be noticed.
 */
public class AutomaticImportMonitor implements Runnable
{
    /****** PRIVATE SECTION ********/
    private static final org.apache.log4j.Logger s_logger = org.apache.log4j.Logger
            .getLogger(AutomaticImportMonitor.class);

    private static final String BATCH_SIZE_ALL = "all";
    private static final String BATCH_SIZE_ONE = "one";

    private static final String PROPERTY_FILE = "/properties/autoFileImport.properties";
    private static final String MAP_FILE = "/properties/autoFileImport.map";

    private static final String PROP_BATCH_SIZE = "batchSize";
    private static final String PROP_POLL_FREQ = "pollingFrequency";
    private static final String PROP_DISABLE_AI = "disableAutoImport";
    private static final String PROP_ASSUME_NEW = "assumeUnencounteredFilesAreNew";

    private static final long DEFAULT_POLLING_FREQUENCY = 60000; // 60 seconds
    private static final long MILLISECONDS_PER_SECOND = 1000;

    private static String DATASTORE_FILE_DIR = File.separator + "pers";
    private static final String DATASTORE_FILE_EXTENSION = "_aim.pers";

    // PRIVATE MEMBER VARIABLES

    // The AutomaticImportMonitor is a singleton
    private static AutomaticImportMonitor s_automaticImportMonitor = null;

    private String m_name;
    private String m_baseDirectory;
    private DirectoryMap m_directoryMap;
    private String m_dataStoreFileName;
    private long m_pollingFrequency; // milliseconds
    private String m_batchSize;
    private boolean m_disableAutoImport;
    private boolean m_assumeUnencounteredFilesAreNewOnFirstScan = true;

    private long m_propertyFileLastModTime;
    private File m_propertyFile;
    private Thread m_thread;
    private boolean m_keepLooping;
    private boolean m_needToSaveState = false;

    // PRIVATE CONSTRUCTORS
    /**
     * Constructs an AutomaticImportMonitor
     * 
     * @param p_name
     *            -- an ID for the AutomaticImportMonitor
     * @param p_baseDirectory
     *            -- the Docs Directory from which all files and directories are
     *            relative
     */
    private AutomaticImportMonitor(String p_name, String p_baseDirectory)
            throws Exception
    {
        // set up the path to the log file
        try
        {
            // set the log directory for the error files
            String storageDirectory = SystemConfiguration
                    .getInstance()
                    .getStringParameter(SystemConfigParamNames.FILE_STORAGE_DIR);
            // set the absolute path
            DATASTORE_FILE_DIR = storageDirectory + DATASTORE_FILE_DIR;
        }
        catch (Exception e)
        {
            s_logger.error("The log directory couldn't be found in the system configuration\r\n"
                    + " for logging Automatic Import Monitoring messages.");
        }

        URL url;

        m_name = p_name;
        m_baseDirectory = p_baseDirectory;

        if (!m_baseDirectory.endsWith(File.separator))
        {
            m_baseDirectory = m_baseDirectory + File.separator;
        }

        m_dataStoreFileName = DATASTORE_FILE_DIR + File.separator + m_name
                + DATASTORE_FILE_EXTENSION;

        m_propertyFileLastModTime = 0;

        m_propertyFile = new File(getPropertyFilePath(PROPERTY_FILE));
        m_thread = null;
        m_keepLooping = false;
        m_directoryMap = null;
    }

    /**
     * Gets the Property file path name as a System Resource
     * 
     * @param propertyFile
     *            basename of the property file
     * @throws FileNotFoundException
     * @return String -- propety file path name
     * @throws URISyntaxException
     */
    private String getPropertyFilePath(String p_propertyFile)
            throws FileNotFoundException, URISyntaxException
    {
        URL url = AutomaticImportMonitor.class.getResource(p_propertyFile);

        if (url == null)
        {
            throw new FileNotFoundException("Property file " + p_propertyFile
                    + " not found");
        }

        try
        {
            return url.toURI().getPath();
        }
        catch (URISyntaxException e)
        {
            return url.getFile();
        }
    }

    // PRIVATE METHODS

    /**
     * Creates the directory map as a new directory map
     */
    private void createDirectoryMap()
    {
        m_directoryMap = new DirectoryMap(m_name, MAP_FILE, m_baseDirectory);

        s_logger.debug("Creating brand new directory map");
    }

    /**
     * Restores the DirectoryMap from the data store or creates a new one from
     * the mapping file if the data store is empty or does not exist
     */
    private void restoreState() throws Exception
    {
        try
        {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(
                    m_dataStoreFileName));

            s_logger.info("Reading existing directory map from "
                    + m_dataStoreFileName);

            m_directoryMap = (DirectoryMap) is.readObject();
            is.close();
        }
        catch (FileNotFoundException fnfe)
        {
            s_logger.debug("Could not find file " + m_dataStoreFileName);
            createDirectoryMap();
        }
    }

    /**
     * Saves the DirectoryMap to the data store
     */
    private void saveState() throws Exception
    {
        s_logger.debug("Saving DirectoryMap state to " + m_dataStoreFileName);
        File dataStoreDir = new File(DATASTORE_FILE_DIR);
        dataStoreDir.mkdirs();

        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(
                m_dataStoreFileName));
        os.writeObject(m_directoryMap);
        os.close();
        m_needToSaveState = false;
    }

    /**
     * Reads the property file for properties. Re-reads the file if it has
     * changed since the last read.
     */
    private void updateProperties() throws Exception
    {
        long propertyFileLastModTime = m_propertyFile.lastModified();
        if (propertyFileLastModTime > m_propertyFileLastModTime)
        {
            s_logger.info("Updating auto-import properties.");
            m_propertyFileLastModTime = propertyFileLastModTime;

            Properties props = new Properties();
            props.load(new FileInputStream(m_propertyFile));

            // get batch size [all|one]
            m_batchSize = props.getProperty(PROP_BATCH_SIZE).toLowerCase();
            if (!(m_batchSize.equals(BATCH_SIZE_ONE) || m_batchSize
                    .equals(BATCH_SIZE_ALL)))
                m_batchSize = BATCH_SIZE_ONE;

            // get the polling frequence in seconds
            String pollingFreq = props.getProperty(PROP_POLL_FREQ);
            try
            {
                long pollFreqInSeconds = Long.parseLong(pollingFreq);
                m_pollingFrequency = pollFreqInSeconds
                        * MILLISECONDS_PER_SECOND;
            }
            catch (NumberFormatException nfe)
            {
                s_logger.error("Unable to use specified polling frequency", nfe);
                m_pollingFrequency = DEFAULT_POLLING_FREQUENCY;
            }

            if (m_pollingFrequency == 0)
            {
                m_pollingFrequency = DEFAULT_POLLING_FREQUENCY;
            }

            // get whether auto import is disabled
            String disableAutoImport = props.getProperty(PROP_DISABLE_AI);
            m_disableAutoImport = Boolean.valueOf(disableAutoImport)
                    .booleanValue();

            // get whether we should assume unencountered files are new
            String assumeUnencounteredFilesAreNewOnFirstScan = props
                    .getProperty(PROP_ASSUME_NEW);
            m_assumeUnencounteredFilesAreNewOnFirstScan = Boolean.valueOf(
                    assumeUnencounteredFilesAreNewOnFirstScan).booleanValue();

            s_logger.info("Auto-import properties are: (" + m_batchSize + ","
                    + m_pollingFrequency + "," + m_disableAutoImport + ","
                    + m_assumeUnencounteredFilesAreNewOnFirstScan + ")");
        }
    }

    /**
     * Processes one directory by polling it for changes and then importing the
     * new or modified files
     */
    private void processDirectory(DirectoryMapEntry p_mapEntry)
    {
        DirectoryMonitor monitor = null;
        String directoryName = null;

        try
        {
            monitor = p_mapEntry.getDirectoryMonitor();
            directoryName = monitor.getDirectoryName();

            s_logger.debug("Processing directory: " + directoryName);

            monitor.detectChanges();
            if (monitor.changesWereDetected())
            {
                m_needToSaveState = true;
                FileProfile fileProfile = findFileProfile(p_mapEntry);
                Set filesToImport = removeInappropriateFiles(
                        monitor.newAndModifiedFiles(), fileProfile);
                importFiles(filesToImport, p_mapEntry, fileProfile);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Problem processing directory  " + directoryName, e);
        }
    }

    /**
     * Removes the files from the Set that do not have the appropriate file
     * extension for the associated file profile. The check is case insensitive.
     * That is, .doc and .DOC are viewed as the same extension for this purpose.
     * 
     * @param p_filesToImport
     * @param p_fileProfile
     *            file profile associated with directory
     * @exception Exception
     */
    private Set removeInappropriateFiles(Set p_filesToImport,
            FileProfile p_fileProfile) throws Exception
    {
        Object[] extensions = ServerProxy.getFileProfilePersistenceManager()
                .getFileExtensionsByFileProfile(p_fileProfile).toArray();

        if (extensions == null || extensions.length == 0)
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("No extensions for file profile "
                        + p_fileProfile.getName() + ", so importing all files.");                
            }

            return p_filesToImport;
        }

        HashSet returnSet = new HashSet(p_filesToImport.size());
        HashSet extSet = new HashSet();
        for (int i = 0; i < extensions.length; i++)
        {
            FileExtension fe = (FileExtension) extensions[i];
            String extName = fe.getName();
            extSet.add(extName.toLowerCase());
        }

        Iterator iter = p_filesToImport.iterator();
        while (iter.hasNext())
        {
            String fileName = (String) iter.next();
            int idx = fileName.lastIndexOf(".");
            String ext = fileName.substring(idx + 1).toLowerCase();
            if (extSet.contains(ext.toLowerCase()) == false)
            {
                // this file's extension is not in the extension set so remove
                // the file
                s_logger.warn("Ignoring file " + fileName
                        + " because extension ." + ext
                        + " does not match fileprofile "
                        + p_fileProfile.getName());
            }
            else
            {
                returnSet.add(fileName);
            }
        }

        return returnSet;
    }

    /**
     * Finds the file profile ID for the file profile associated with the
     * DirectoryMapEntry
     * 
     * @param mapEntry
     *            -- the directory map entry
     * @return the file profile ID as a long
     * @throws Exception
     */
    private long findFileProfileId(DirectoryMapEntry p_mapEntry)
            throws Exception
    {
        long fileProfileId = ServerProxy.getFileProfilePersistenceManager()
                .getFileProfileIdByName(p_mapEntry.getFileProfileNames()[0]);

        if (fileProfileId == -1)
        {
            // just log the error for now since the ImportOperation
            // will generate an import error when given the invalid ID
            s_logger.warn("The File Profile "
                    + p_mapEntry.getFileProfileNames()[0]
                    + " does not exist. The file cannot be imported");
        }

        return fileProfileId;
    }

    /**
     * Finds the file profile for the file profile associated with the
     * DirectoryMapEntry
     * 
     * @param mapEntry
     *            -- the directory map entry
     * @return the file profile ID as a long
     * @throws Exception
     */
    private FileProfile findFileProfile(DirectoryMapEntry p_mapEntry)
            throws Exception
    {
        String fpName = p_mapEntry.getFileProfileNames()[0];
        return ServerProxy.getFileProfilePersistenceManager()
                .getFileProfileByName(fpName);
    }

    /**
     * Publishes an event to import the given set of files using the appropriate
     * batch size as set in the property file and using the appropriate file
     * profile and job name
     * 
     * @param files
     *            -- the set of files tom import
     * @param mapEntry
     *            -- the directory map entry
     * @throws Exception
     */
    private void importFiles(Set p_files, DirectoryMapEntry p_mapEntry,
            FileProfile p_fileProfile) throws Exception
    {
        Object[] files = p_files.toArray();
        String filename;
        String batchId = null;
        boolean useSameBatchId;
        int pageCount;
        int pageNum;
        // Each page is a full document on its own
        int docPageCount = 1;
        int docPageNum = 1;
        String jobName = null;

        if (m_batchSize.equals(BATCH_SIZE_ALL))
        {
            useSameBatchId = true;
            jobName = p_mapEntry.nextJobName();
            batchId = jobName + System.currentTimeMillis();
            pageCount = files.length;
            pageNum = 0;
        }
        else
        {
            useSameBatchId = false;
            pageCount = 1;
            pageNum = 1;
        }

        for (int i = 0; i < files.length; i++)
        {
            filename = (String) files[i];

            if (!useSameBatchId)
            {
                // strip off the path
                int beginIndex = filename.lastIndexOf(File.separatorChar) + 1;

                // strip off the extension
                int endIndex = filename.lastIndexOf('.');
                if (endIndex == -1)
                {
                    jobName = filename.substring(beginIndex);
                }
                else
                {
                    jobName = filename.substring(beginIndex, endIndex);
                }
                batchId = jobName + System.currentTimeMillis();
            }
            else
            {
                pageNum++;
            }

            publishEventToCxe(jobName, batchId, pageNum, pageCount, docPageNum,
                    docPageCount, filename, p_fileProfile.getId());
        }
    }

    /**
     * Publishes a FILE_SELECTED_EVENT to CXE so that the file can be imported
     * from the docs directory with the appropriate batch and job information.
     * 
     * @param jobName
     *            -- the name of the resulting job if the l10nprofile is by
     *            batch
     * @param batchId
     *            -- an identifier for the batch
     * @param pageNum
     *            -- number of this page in this batch
     * @param pageCount
     *            -- total number of pages
     * @param docPageNum
     *            -- number of this page in its document
     * @param docPageCount
     *            -- total number of pages in document
     * @param fileName
     *            -- name of the file to import
     * @param fileProfileId
     *            -- the ID of the file profile to use
     */
    private void publishEventToCxe(String p_jobName, String p_batchId,
            int p_pageNum, int p_pageCount, int p_docPageNum,
            int p_docPageCount, String p_filename, long p_fileProfileId)
            throws Exception
    {
        String fileProfileId = Long.toString(p_fileProfileId);
        String relativeFileName = p_filename
                .substring(m_baseDirectory.length());
        Integer pageCount = new Integer(p_pageCount);
        Integer pageNum = new Integer(p_pageNum);
        Integer docPageCount = new Integer(p_docPageCount);
        Integer docPageNum = new Integer(p_docPageNum);

        s_logger.info("Auto import requesting file: " + relativeFileName
                + " with jobname " + p_jobName + " and fileprofile "
                + fileProfileId);

        CxeProxy.importFromFileSystem(relativeFileName, p_jobName, null,
                p_batchId, fileProfileId, pageCount, pageNum, docPageCount,
                docPageNum, Boolean.TRUE, CxeProxy.IMPORT_TYPE_L10N,
                User.SYSTEM_USER_ID, new Integer(0));
    }

    /**
     * Returns the filename relative to the base directory.
     * 
     * @param p_filename
     *            -- a full path file name
     * @return the path of the file relative to m_baseDirectory
     */
    private String relativePathName(String p_fullPathFileName)
    {
        int index = p_fullPathFileName.indexOf(m_baseDirectory)
                + m_baseDirectory.length();
        return p_fullPathFileName.substring(index);
    }

    /**
     * Performs the automatic import monitor and processing.
     */
    private void doAutomaticImport()
    {
//        ActivityLog.Start activityStart = ActivityLog.start(
//                AutomaticImportMonitor.class, "doAutomaticImport");
        try
        {
            updateProperties();

            if (!m_disableAutoImport)
            {
                boolean mapWasUpdated = m_directoryMap
                        .updateDirectoryMapIfNeeded(m_assumeUnencounteredFilesAreNewOnFirstScan);

                if (mapWasUpdated)
                {
                    s_logger.debug("Map needed to be updated.");
                    m_needToSaveState = true;

                }

                ArrayList directoryMapEntries = m_directoryMap
                        .getDirectoryMapEntries();

                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("There are " + directoryMapEntries.size()
                            + " directory map entries to process.");                    
                }

                for (int i = 0; i < directoryMapEntries.size(); i++)
                {
                    processDirectory((DirectoryMapEntry) directoryMapEntries
                            .get(i));
                }
            }

            if (m_needToSaveState)
            {
                saveState();
            }
        }
        catch (Exception e)
        {
            s_logger.error("Problem during Automatic Import", e);
            m_keepLooping = false;
        }
        finally
        {
//            activityStart.end();
        }
    }

    /**
     * Sleeps until next polling cycle
     */
    private void sleepUntilNextPoll()
    {
        try
        {
            Thread.sleep(m_pollingFrequency);
        }
        catch (InterruptedException ie)
        {
        }
    }

    /****** PUBLIC SECTION ********/

    // SINGLETON ACCESS AND CREATION

    /**
     * Returns the singleton AutomaticImportMonitor. If it has not been
     * initialized yet, then null is returned.
     */
    public static AutomaticImportMonitor getInstance()
    {
        return s_automaticImportMonitor;
    }

    /**
     * initializes the Singleton AutomaticImportMonitor with a name. This name
     * is used when persisting out the AutomaticImportMonitor's DirectoryMap
     * 
     * @param the
     *            name of the AutomaticImportMonitor
     * @param p_baseDirectory
     *            -- the Docs Directory from which all files and directories are
     *            relative
     */
    public static void initialize(String p_name, String p_baseDirectory)
            throws Exception
    {
        if (s_automaticImportMonitor == null)
        {
            s_automaticImportMonitor = new AutomaticImportMonitor(p_name,
                    p_baseDirectory);
        }
    }

    // PUBLIC METHODS

    /**
     * Starts the AutomaticImportMonitor up in a separate low priority thread.
     * It will now start monitoring directories for changes. The thread will be
     * named after the AutomaticImportMonitor with "_AIM" appended to the name.
     */
    public void startup() throws Exception
    {
        restoreState();

        m_keepLooping = true;
        m_thread = new Thread(this, m_name + "_AIM");
        m_thread.setPriority(Thread.MIN_PRIORITY);
        m_thread.start();
    }

    /**
     * Shuts down the thread running the AutomaticImportMonitor
     */
    public void shutdown(boolean p_waitForThreadDeath)
    {
        m_keepLooping = false;

        if (p_waitForThreadDeath)
        {
            try
            {
                m_thread.join();
            }
            catch (InterruptedException ie)
            {
            }
        }
    }

    // PUBLIC INTERFACE METHODS

    /**
     * Runs in a separate thread and continually polls the directories in the
     * DirectoryMap for changes. Any file changes trigger file imports.
     * 
     * NOTE: This will only run in a low priority thread created by the
     * AutomaticImportMonitor.
     */
    public void run()
    {
        // prevent this object from being run in another thread
        if (m_thread == null)
        {
            return;
        }

        while (m_keepLooping)
        {
            doAutomaticImport();
            sleepUntilNextPoll();
        }
    }

    // ACCESSORS/MUTATORS FOR ATTRIBUTES

    /**
     * Gets the filname of the mapping file
     * 
     * @return the directory map file name
     */
    public String getDirectoryMapFileName()
    {
        return MAP_FILE;
    }
}
