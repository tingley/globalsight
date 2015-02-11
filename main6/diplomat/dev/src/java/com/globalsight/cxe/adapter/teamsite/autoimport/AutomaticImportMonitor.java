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
package com.globalsight.cxe.adapter.teamsite.autoimport;

import com.globalsight.cxe.adapter.filesystem.autoImport.DirectoryMap;
import com.globalsight.cxe.adapter.filesystem.autoImport.DirectoryMapEntry;
import com.globalsight.cxe.adapter.filesystem.autoImport.DirectoryMapPrinter;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.file.DirectoryMonitor;
import com.globalsight.util.file.DelimitedFileReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.common.URLDecoder;
import java.net.URLConnection;

import java.io.File;
import java.net.ServerSocket;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.Date;

/**
 * An AutomaticImportMonitor can be used to monitor a number
 * of directories for changes to trigger an import in CXE.
 *
 * It uses a directory mapping file
 * of the format:
 * Directory | JobPrefixName | FileProfileName
 *
 * It will run separately in a low priority thread.
 * It will also save its state of its DirectoryMap so that on
 * subsequent runs, file changes during system downtime
 * will be noticed.
 */
public class AutomaticImportMonitor
    implements Runnable
{
    /****** PRIVATE SECTION ********/
    private static final org.apache.log4j.Logger s_logger =
        org.apache.log4j.Logger.getLogger("AutomaticImportMonitor");

    private static final String BATCH_SIZE_ALL = "all";
    private static final String BATCH_SIZE_ONE = "one";
    private static final String AUTOMATIC_IMPORT = "AutomaticImport";

    private static final String PROPERTY_FILE = "/properties/autoTeamSiteImport.properties";
    private static final String MAP_FILE = "/properties/autoTeamSiteImport.map";
    private static final String TEAMSITE_PROPERTY_FILE = "teamsiteParams.properties";

    private static final String PROP_BATCH_SIZE = "batchSize";
    private static final String PROP_POLL_FREQ = "pollingFrequency";
    private static final String PROP_DISABLE_AI = "disableAutoImport";
    private static final String PROP_ASSUME_NEW = "assumeUnencounteredFilesAreNew";
    private static final String PROP_AMB_SERVER = "ambServer";
    private static final String PROP_TEAMSITE_SERVER = "teamsiteServer";
    private static final String PROP_MOUNT_DIR = "mountDir";
    private static final String PROP_DEFAULT_STORE = "defaultStore";
    private static final String PROP_MAIN_DIR = "defaultMain";
    private static final String PROP_USER_NAME = "userName";
    private static final String PROP_USER_PRIVILEGE = "userRole";

    private static final long DEFAULT_POLLING_FREQUENCY = 60000; //60 seconds
    private static final long MILLISECONDS_PER_SECOND = 1000;

    private static String DATASTORE_FILE_DIR = File.separator + "pers";
    private static final String DATASTORE_FILE_EXTENSION = "_aim.pers";
    private static final String EQUALS = "=";
    private static final String AMPERSAND = "&";
    private static final String SUB_PAGE_TYPE = "sub_directory";
    private static final String PAGE_TYPE = "workarea";

    //PRIVATE MEMBER VARIABLES

    // Singleton
    private static AutomaticImportMonitor s_automaticImportMonitor = null;

    protected void finalize()
    {
        s_logger.info("AutomaticImportMonitor: instance " + this.hashCode() +
            " garbage collected");
    }

    private ServerSocket server = null;
    private Thread thread = null;
    private boolean stopping = false;
    private String directory = null;
    private Vector handlers = null;

    private String m_name;
    private String m_mapFileName;
    private String m_baseDirectory;
    private DirectoryMap m_directoryMap;
    private String m_dataStoreFileName;
    private long m_pollingFrequency; //milliseconds
    private String m_batchSize;
    private boolean m_disableAutomaticImportMonitor;
    private boolean m_assumeUnencounteredFilesAreNewOnFirstScan = true;

    private static long m_tsPropertyFileLastModTime = 0;
    private static File m_teamsitePropertyFile;
    private static String m_ambServer;
    private static String m_teamsiteServer;
    private static String m_mountDir;
    private static String m_defaultStore;
    private static String m_mainDir;
    private static String m_teamsiteUserName;
    private static String m_teamsiteUserPrivilege;

    private long m_propertyFileLastModTime;
    private File m_propertyFile;
    private Thread m_thread;
    private boolean m_keepLooping;
    private boolean m_needToSaveState = false;

    //PRIVATE CONSTRUCTORS

    /**
     * Constructs an AutomaticImportMonitor
     *
     * @param p_name -- an ID for the AutomaticImportMonitor
     * @param p_baseDirectory -- the Docs Directory from which all files
     * and directories are relative
     */
    private AutomaticImportMonitor(String p_name, String p_baseDirectory)
        throws Exception
    {
        // set up the path to the log file
        try
        {
            //set the log directory for the error files
            //String logDirectory = SystemConfiguration.getInstance().getStringParameter(SystemConfigParamNames.SYSTEM_LOGGING_DIRECTORY);
            // Hardcoding the log directory
            // to /tmp
            String logDirectory = "/tmp";
            //set the absolute path
            DATASTORE_FILE_DIR = logDirectory + DATASTORE_FILE_DIR;
        }
        catch (Exception e)
        {
            s_logger.error("The log directory couldn't be found in the system configuration\r\n" +
                " for logging Automatic Import Monitoring messages.");
        }

        URL url;

        m_name = p_name;

        m_baseDirectory = p_baseDirectory;

        if (!m_baseDirectory.endsWith(File.separator))
        {
            m_baseDirectory = m_baseDirectory + File.separator;
        }

        m_dataStoreFileName = DATASTORE_FILE_DIR + File.separator +
            m_name + DATASTORE_FILE_EXTENSION;

        m_propertyFileLastModTime = 0;

        m_propertyFile = new File (getPropertyFilePath(PROPERTY_FILE));
        m_mapFileName = getPropertyFilePath(MAP_FILE);

        m_thread = null;
        m_keepLooping = false;
        m_directoryMap = null;
    }

    /**
     * Gets the Property file path name as a System Resource
     * @param propertyFile basename of the property file
     * @throws FileNotFoundException
     * @return String -- propety file path name
     * @throws URISyntaxException 
     */
    private static String getPropertyFilePath(String p_propertyFile)
        throws FileNotFoundException, URISyntaxException
    {
        URL url = AutomaticImportMonitor.class.getResource(p_propertyFile);
        if (url == null)
        {
            throw new FileNotFoundException("Property file " + p_propertyFile +
                " not found");
        }
        return URLDecoder.decode(url.toURI().getPath(), "UTF-8");
    }


    //PRIVATE METHODS

    /**
     * Creates the directory map as a new directory map
     */
    private void createDirectoryMap()
    {
        m_directoryMap = new DirectoryMap(m_name, m_mapFileName, m_baseDirectory);
        s_logger.debug("Creating brand new directory map");
    }

    /**
     * Restores the DirectoryMap from the data store or creates a new
     * one from the mapping file if the data store is empty or does not exist
     */
    private void restoreState()
        throws Exception
    {
        try
        {
            ObjectInputStream is = new ObjectInputStream(
                new FileInputStream(m_dataStoreFileName));

            s_logger.info("Reading existing directory map from " +
                m_dataStoreFileName);

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
    private void saveState()
        throws Exception
    {
        s_logger.debug("Saving DirectoryMap state to " + m_dataStoreFileName);

        File dataStoreDir = new File (DATASTORE_FILE_DIR);
        dataStoreDir.mkdirs();

        ObjectOutputStream os = new ObjectOutputStream(
            new FileOutputStream(m_dataStoreFileName));
        os.writeObject(m_directoryMap);
        os.close();

        m_needToSaveState = false;
    }


    /**
     * Reads the property file for properties.
     * Re-reads the file if it has changed since the last read.
     */
    private void updateProperties()
        throws Exception
    {
        long propertyFileLastModTime = m_propertyFile.lastModified();
        if (propertyFileLastModTime > m_propertyFileLastModTime)
        {
            s_logger.info("Updating auto-import properties.");
            m_propertyFileLastModTime = propertyFileLastModTime;

            Properties props = new Properties();
            props.load(new FileInputStream(m_propertyFile));

            //get batch size [all|one]
            m_batchSize = props.getProperty(PROP_BATCH_SIZE).toLowerCase();
            if (!(m_batchSize.equals(BATCH_SIZE_ONE) ||
                m_batchSize.equals(BATCH_SIZE_ALL)))
            {
                m_batchSize = BATCH_SIZE_ONE;
            }

            //get the polling frequence in seconds
            String pollingFreq = props.getProperty(PROP_POLL_FREQ);
            try
            {
                long pollFreqInSeconds = Long.parseLong(pollingFreq);
                m_pollingFrequency = pollFreqInSeconds * MILLISECONDS_PER_SECOND;
            }
            catch (NumberFormatException nfe)
            {
                s_logger.error("Unable to use specified polling frequency",nfe);
                m_pollingFrequency = DEFAULT_POLLING_FREQUENCY;
            }

            if (m_pollingFrequency == 0)
                m_pollingFrequency = DEFAULT_POLLING_FREQUENCY;

            //get whether auto import is disabled
            String disableAutomaticImportMonitor = props.getProperty(PROP_DISABLE_AI);
            m_disableAutomaticImportMonitor = Boolean.valueOf(
                disableAutomaticImportMonitor).booleanValue();

            //get whether we should assume unencountered files are new
            String assumeUnencounteredFilesAreNewOnFirstScan =
                props.getProperty(PROP_ASSUME_NEW);
            m_assumeUnencounteredFilesAreNewOnFirstScan = Boolean.valueOf(
                assumeUnencounteredFilesAreNewOnFirstScan).booleanValue();

            s_logger.info("Auto-import properties are: (" + m_batchSize + ","
                + m_pollingFrequency + "," + m_disableAutomaticImportMonitor +
                "," + m_assumeUnencounteredFilesAreNewOnFirstScan +
                ")");
        }
    }

    /**
     * Reads the teamsite property file for properties.
     * Re-reads the file if it has changed since the last read.
     */
    private static void updateTeamsiteProperties()
        throws Exception
    {
        m_teamsitePropertyFile = new File (getPropertyFilePath(TEAMSITE_PROPERTY_FILE));
        long tsPropertyFileLastModTime = m_teamsitePropertyFile.lastModified();
        if (tsPropertyFileLastModTime > m_tsPropertyFileLastModTime)
        {
            m_tsPropertyFileLastModTime = tsPropertyFileLastModTime;

            Properties props = new Properties();
            props.load(new FileInputStream(m_teamsitePropertyFile));

            //get GlobalSight server to connect to
            m_ambServer = props.getProperty(PROP_AMB_SERVER);

            //get TeamSite Server
            m_teamsiteServer = props.getProperty(PROP_TEAMSITE_SERVER);

            //get mount directory
            m_mountDir = props.getProperty(PROP_MOUNT_DIR);

            //get default store directory
            m_defaultStore = props.getProperty(PROP_DEFAULT_STORE);

            //get main directory
            m_mainDir = props.getProperty(PROP_MAIN_DIR);

            //get teamsite username
            m_teamsiteUserName = props.getProperty(PROP_USER_NAME);

            //get teamsite user's privilege level
            m_teamsiteUserPrivilege = props.getProperty(PROP_USER_PRIVILEGE);

            s_logger.info("Teamsite-import properties are: (" +
                m_mountDir + "," +
                m_teamsiteServer + "," +
                m_defaultStore + "," +
                m_mainDir + "," +
                m_teamsiteUserName + "," +
                m_teamsiteUserPrivilege + ")");
        }
    }

    /**
     * Processes one directory by polling it for changes and then
     * importing the new or modified files
     */
    private void processDirectory(DirectoryMapEntry p_mapEntry)
    {
        DirectoryMonitor monitor = null;
        String directoryName = null;

        try
        {
            monitor = p_mapEntry.getDirectoryMonitor();
            directoryName = monitor.getDirectoryName();

            s_logger.debug("Processing directory :: " + directoryName);

            monitor.detectChanges();
            if (monitor.changesWereDetected())
            {
                s_logger.debug("Changes are detected");
                m_needToSaveState = true;
                String  fileProfile = findFileProfile(p_mapEntry);
                Set filesToImport = removeInappropriateFiles(
                    monitor.newAndModifiedFiles(), null);
                importFiles(filesToImport, p_mapEntry, fileProfile);
            }

            s_logger.debug("Done Processing directory " + directoryName);
        }
        catch (Exception e)
        {
            s_logger.error("Problem processing directory  " + directoryName, e);
        }
    }

    /**
     * Removes the files from the Set that do not have the appropriate
     * file extension for the associated file profile
     *
     * @param p_filesToImport
     * @param p_fileProfile file profile associated with directory
     * @exception Exception
     */
    private Set removeInappropriateFiles(Set p_filesToImport, String p_fileProfile)
        throws Exception
    {
        //Object[] extensions = ServerProxy.getFileProfilePersistenceManager().getFileExtensionsByFileProfile(p_fileProfile).toArray();
        // Here setting extension to null. Will have to find out a way
        // to support extensions
        Object[] extensions = null;
        if (extensions == null || extensions.length == 0)
        {
            s_logger.debug("No extensions for file profile, so importing all files.");
        }
        return p_filesToImport;
    }

    /**
     * Finds the file profile for the file profile associated with the
     * DirectoryMapEntry
     *
     * @param mapEntry -- the directory map entry
     * @return the file profile ID as a String
     * @throws Exception
     */
    private String findFileProfile(DirectoryMapEntry p_mapEntry)
        throws Exception
    {
        String fpName = p_mapEntry.getFileProfileNames()[0];
        return fpName;
    }


    /**
     * Publishes an event to import the given set of files using
     * the appropriate batch size as set in the property file
     * and using the appropriate file profile and job name
     *
     * @param files -- the set of files tom import
     * @param mapEntry -- the directory map entry
     * @throws Exception
     */
    private void importFiles(Set p_files, DirectoryMapEntry p_mapEntry,
        String p_fileProfile)
        throws Exception
    {
        s_logger.debug("Importing files");

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

            s_logger.debug("Importing file " + filename);

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

            s_logger.debug("Sending to Translate Servlet for Import " + filename);

            sendToServletForImport(jobName, p_fileProfile, filename);
        }
    }


    /**
     * Returns the filename relative to the base directory.
     *
     * @param p_filename -- a full path file name
     * @return the path of the file relative to m_baseDirectory
     */
    private String relativePathName(String p_fullPathFileName)
    {
        int index = p_fullPathFileName.indexOf(m_baseDirectory) +
            m_baseDirectory.length();
        return p_fullPathFileName.substring(index);
    }

    /**
     * Performs the automatic import monitor and processing.
     */
    private void doAutomaticImport()
    {
        try
        {
            updateProperties();
            updateTeamsiteProperties();

            if (!m_disableAutomaticImportMonitor)
            {
                boolean mapWasUpdated = m_directoryMap.updateDirectoryMapIfNeeded(
                    m_assumeUnencounteredFilesAreNewOnFirstScan);

                if (mapWasUpdated)
                {
                    s_logger.debug("Map needed to be updated.");
                    m_needToSaveState = true;

                }

                ArrayList directoryMapEntries = m_directoryMap.getDirectoryMapEntries();

                s_logger.debug("There are " + directoryMapEntries.size() +
                    " directory map entries to process.");

                for (int i = 0; i < directoryMapEntries.size(); i++)
                {
                    processDirectory((DirectoryMapEntry)directoryMapEntries.get(i));
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

    //SINGLETON ACCESS AND CREATION

    /**
     * Returns the singleton AutomaticImportMonitor.
     * If it has not been initialized yet, then null is returned.
     */
    public static AutomaticImportMonitor getInstance()
    {
        return s_automaticImportMonitor;
    }

    /**
     * initializes the Singleton AutomaticImportMonitor
     * with a name. This name is used when persisting
     * out the AutomaticImportMonitor's DirectoryMap
     *
     * @param the name of the AutomaticImportMonitor
     * @param p_baseDirectory -- the Docs Directory from which all files
     * and directories are relative
     */
    public static void initializeMonitor(String p_name,
        String p_baseDirectory)
        throws Exception
    {
        if (s_automaticImportMonitor == null)
        {
            s_automaticImportMonitor = new AutomaticImportMonitor(p_name,
                p_baseDirectory);
        }
    }
    public static void initialize()
        throws Exception
    {
        updateTeamsiteProperties();
        String baseDirectoryToMonitor = m_mountDir +
            File.separator +
            m_defaultStore +
            m_mainDir;
        initializeMonitor(AUTOMATIC_IMPORT, baseDirectoryToMonitor);
    }

    //PUBLIC METHODS

    /**
     * Starts the AutomaticImportMonitor up in a separate low priority
     * thread. It will now start monitoring directories for changes.
     * The thread will be named after the AutomaticImportMonitor with "_AIM"
     * appended to the name.
     */
    public void startup()
        throws Exception
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

    //PUBLIC INTERFACE METHODS

    /**
     * Runs in a separate thread and continually polls the directories
     * in the DirectoryMap for changes. Any file changes trigger file
     * imports.
     *
     * NOTE: This will only run in a low priority
     * thread created by the AutomaticImportMonitor.
     */
    public void run()
    {
        //prevent this object from being run in another thread
        if (m_thread == null)
        {
            return;
        }

        while (m_keepLooping)
        {
            s_logger.info("running... " + new Date());

            doAutomaticImport();
            sleepUntilNextPoll();
        }
    }

    //ACCESSORS/MUTATORS FOR ATTRIBUTES

    /**
     * Gets the filname of the mapping file
     * @return the directory map file name
     */
    public String getDirectoryMapFileName()
    {
        return m_mapFileName;
    }

    private void sendToServletForImport(String p_jobName,
        String p_fileProfile, String p_fileName)
    {
        try
        {
            s_logger.debug("Calling the URL");

            //URL m_url = new URL("http://10.0.0.114:7001/TranslateServlet?");
            URL m_url = new URL(m_ambServer);
            URLConnection conn = m_url.openConnection();
            conn.setDoOutput(true);

            String p_line = createParameters(p_jobName, p_fileProfile, p_fileName);
            // The string has been URLencoded and consists of ASCII
            // characters only. Still we have to write UTF8.
            OutputStreamWriter wr =
                new OutputStreamWriter(conn.getOutputStream(), "UTF8");
            wr.write(p_line);
            wr.flush();
            wr.close();
            // Read lines from the output
            String l = null;
            BufferedReader rd = new BufferedReader(new
                InputStreamReader(conn.getInputStream()));
            while ((l = rd.readLine()) != null)
            {
                s_logger.info(l);
            }
            rd.close();
        }
        catch (Exception e)
        {
            s_logger.debug("Exception while Calling the URL " + e);
        }
    }

    public String createParameters(String p_jobName, String p_fileProfile,
        String p_fileName)
    {
        File file = new File(p_fileName);
        // Get the number of bytes in the file
        String fileSize = (new Long(file.length())).toString();

        String fileNameOnly = p_fileName.substring(p_fileName.lastIndexOf(File.separator)+ 1);
        String archivePath = m_mountDir + File.separator + m_defaultStore;
        String vpath = p_fileName.substring(p_fileName.indexOf(m_defaultStore) - 1, p_fileName.lastIndexOf(File.separator));
        String directoryName = vpath.substring(vpath.lastIndexOf(File.separator) + 1);
        String directoryPath = p_fileName.substring(0, p_fileName.lastIndexOf(File.separator) );
        String tempAreaName = p_fileName.substring(p_fileName.indexOf("WORKAREA") + 9);
        String areaName = tempAreaName.substring(0, tempAreaName.indexOf(File.separator));
        String areaPath = p_fileName.substring(0, p_fileName.indexOf(areaName)+ areaName.length() );
        String branchPath  = p_fileName.substring(0, p_fileName.indexOf("WORKAREA") -1 );
        String branchName  = branchPath.substring(branchPath.lastIndexOf(File.separator) + 1);
        StringBuffer line = new StringBuffer();
        
        line.append("user_name");
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_teamsiteUserName, "UTF-8"));
        line.append(AMPERSAND);

        line.append("stage");
        line.append(EQUALS);
        line.append("import");
        line.append(AMPERSAND);

        line.append("autoImport");
        line.append(EQUALS);
        line.append("autoImport");
        line.append(AMPERSAND);

        line.append("user_role");
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_teamsiteUserPrivilege, "UTF-8"));
        line.append(AMPERSAND);

        line.append("teamsite_server");
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_teamsiteServer, "UTF-8"));
        line.append(AMPERSAND);

        line.append("mount_path");
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_mountDir, "UTF-8"));
        line.append(AMPERSAND);

        line.append("jobName");
        line.append(EQUALS);
        line.append(URLEncoder.encode(p_jobName, "UTF-8"));
        line.append(AMPERSAND);

        line.append("fileProfile");
        line.append(EQUALS);
        line.append(URLEncoder.encode(p_fileProfile, "UTF-8"));
        line.append(AMPERSAND);

        line.append("subpage_type");
        line.append(EQUALS);
        line.append(URLEncoder.encode(SUB_PAGE_TYPE, "UTF-8"));
        line.append(AMPERSAND);

        line.append("page_type");
        line.append(EQUALS);
        line.append(URLEncoder.encode(PAGE_TYPE, "UTF-8"));
        line.append(AMPERSAND);

        line.append("vpath");
        line.append(EQUALS);
        line.append(URLEncoder.encode(vpath, "UTF-8"));
        line.append(AMPERSAND);

        line.append("directory_name");
        line.append(EQUALS);
        line.append(URLEncoder.encode(directoryName, "UTF-8"));
        line.append(AMPERSAND);

        line.append("directory_path");
        line.append(EQUALS);
        line.append(URLEncoder.encode(directoryPath, "UTF-8"));
        line.append(AMPERSAND);

        line.append("area_name");
        line.append(EQUALS);
        line.append(URLEncoder.encode(areaName, "UTF-8"));
        line.append(AMPERSAND);

        line.append("area_path");
        line.append(EQUALS);
        line.append(URLEncoder.encode(areaPath, "UTF-8"));
        line.append(AMPERSAND);

        line.append("branch_name");
        line.append(EQUALS);
        line.append(URLEncoder.encode(branchName, "UTF-8"));
        line.append(AMPERSAND);

        line.append("branch_path");
        line.append(EQUALS);
        line.append(URLEncoder.encode(branchPath, "UTF-8"));
        line.append(AMPERSAND);

        line.append("archive_name");
        line.append(EQUALS);
        line.append(URLEncoder.encode(m_defaultStore, "UTF-8"));
        line.append(AMPERSAND);

        line.append("archive_path");
        line.append(EQUALS);
        line.append(URLEncoder.encode(archivePath, "UTF-8"));
        line.append(AMPERSAND);

        line.append("type_0");
        line.append(EQUALS);
        line.append("file");
        line.append(AMPERSAND);

        line.append("size_0");
        line.append(EQUALS);
        line.append(URLEncoder.encode(fileSize, "UTF-8"));
        line.append(AMPERSAND);

        line.append("name_0");
        line.append(EQUALS);
        line.append(URLEncoder.encode(fileNameOnly, "UTF-8"));
        line.append(AMPERSAND);

        line.append("path_0");
        line.append(EQUALS);
        line.append(URLEncoder.encode(p_fileName, "UTF-8"));
        line.append(AMPERSAND);

        return line.toString();
    }
}

