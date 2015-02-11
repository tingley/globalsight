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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.common.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.globalsight.cxe.adapter.filesystem.autoImport.DirectoryMap;
import com.globalsight.cxe.adapter.filesystem.autoImport.DirectoryMapEntry;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.file.DelimitedFileReader;
import com.globalsight.util.file.DirectoryMonitor;

/**
 * An AutomaticImporter can be used to import files from TeamSite workflow It
 * uses a directory mapping file of the format: areaVpath | JobPrefixName |
 * FileProfileName | space separated extensions | overwriteSource |
 * callbackImmediately
 * 
 */
public class AutomaticImporter
{
    /** **** PRIVATE SECTION ******* */
    private static final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger("AutomaticImporter");
    private static final String AUTOMATIC_IMPORT = "AutomaticImport";

    private static final String PROPERTY_FILE = "/properties/autoTeamSiteImport.properties";
    private static final String MAP_FILE = "/properties/autoTeamSiteImport.map";
    private static final String TEAMSITE_PROPERTY_FILE = "/properties/teamsiteParams.properties";

    private static final String PROP_AMB_SERVER = "ambServer";
    private static final String PROP_TEAMSITE_SERVER = "teamsiteServer";
    private static final String PROP_MOUNT_DIR = "mountDir";
    private static final String PROP_DEFAULT_STORE = "defaultStore";
    private static final String PROP_MAIN_DIR = "defaultMain";
    private static final String PROP_USER_NAME = "userName";
    private static final String PROP_USER_PRIVILEGE = "userRole";

    private static final String EQUALS = "=";
    private static final String AMPERSAND = "&";
    private static final String SUB_PAGE_TYPE = "sub_directory";
    private static final String PAGE_TYPE = "workarea";

    private static final String MAP_FILE_DELIMITER = "|";
    private static final String MAP_FILE_COMMENT = "#";
    //PRIVATE MEMBER VARIABLES

    // Singleton
    private static AutomaticImporter s_AutomaticImporter = null;

    protected void finalize()
    {

        s_logger.info("AutomaticImporter: instance " + this.hashCode()
                + " garbage collected");
    }

    private ServerSocket server = null;
    private String directory = null;
    private Vector handlers = null;

    private String m_name;
    private String m_mapFileName;
    private DirectoryMap m_directoryMap;
    private String m_jobName;
    private String m_fileProfile;
    private String m_extensions;
    private String m_overwriteSource;
    private String m_callbackImmediately;

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

    //PRIVATE CONSTRUCTORS
    /**
     * Constructs an AutomaticImporter
     */
    private AutomaticImporter() throws Exception
    {
        m_mapFileName = getPropertyFilePath(MAP_FILE);
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
    private static String getPropertyFilePath(String p_propertyFile)
            throws FileNotFoundException, URISyntaxException
    {
        URL url = AutomaticImporter.class.getResource(p_propertyFile);
        if (url == null)
            throw new FileNotFoundException("Property file " + p_propertyFile
                    + " not found");
        return URLDecoder.decode(url.toURI().getPath(), "UTF-8");
    }

    //PRIVATE METHODS

    /**
     * Reads the teamsite property file for properties. Re-reads the file if it
     * has changed since the last read.
     */
    private static void getTeamsiteProperties() throws Exception
    {
        m_teamsitePropertyFile = new File(
                getPropertyFilePath(TEAMSITE_PROPERTY_FILE));
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

            s_logger.info("Teamsite-import properties are: (" + m_mountDir
                    + "," + m_teamsiteServer + "," + m_defaultStore + ","
                    + m_mainDir + "," + m_teamsiteUserName + ","
                    + m_teamsiteUserPrivilege + ")");

        }
    }

    /**
     * Processes files to be sent to GlobalSight
     */
    private void processFiles(String[] p_files, String p_taskId)
    {
        s_logger.info("Importing files");
        String filename;
        String batchId = null;
        int pageCount;
        int pageNum;
        String jobName = null;
        pageCount = 1;
        pageNum = 1;
        String[] filesToImport = removeInappropriateFiles(p_files);
        sendToServletForImport("TeamSite_" + p_taskId, filesToImport, p_taskId);

    }

    /**
     * reads the parameters file and creates a map to be used to find the file
     * profile and job name information corresponding to a areaVpath
     */
    private void getFileProfileAndJobNameInfo(String p_areaVpath)
            throws Exception
    {
        List lines = DelimitedFileReader.readLinesWithTokens(m_mapFileName,
                MAP_FILE_DELIMITER, MAP_FILE_COMMENT);
        String[] tokens;
        String directoryName;
        String fullPathDirectoryName;
        String jobPrefixName;
        String fileProfile;
        String extensions;
        String overwriteSource;
        String callbackImmediately;
        DirectoryMapEntry mapEntry;
        DirectoryMonitor monitor;
        HashSet listedDirectoryNames = new HashSet();
        int index = 0;

        for (int i = 0; i < lines.size(); i++)
        {
            tokens = (String[]) lines.get(i);
            index = 0;
            directoryName = tokens[index++];
            jobPrefixName = tokens[index++];
            fileProfile = tokens[index++];
            extensions = tokens[index++];
            overwriteSource = tokens[index++];
            callbackImmediately = tokens[index++];
            if (directoryName.equals(p_areaVpath))
            {
                m_jobName = jobPrefixName;
                m_fileProfile = fileProfile;
                m_extensions = extensions;
                m_overwriteSource = overwriteSource;
                m_callbackImmediately = callbackImmediately;
                break;
            }
        }
    }

    /**
     * Performs the automatic import monitor and processing.
     */
    private void doAutomaticImport(String p_taskId, String[] p_files)
    {
        try
        {
            getTeamsiteProperties();
            processFiles(p_files, p_taskId);
        }
        catch (Exception e)
        {
            s_logger.error("Problem during Automatic Import", e);
        }
    }

    /** **** PUBLIC SECTION ******* */

    //PUBLIC INTERFACE METHODS
    /**
     * Gets the filname of the mapping file
     * 
     * @return the directory map file name
     */
    public String getDirectoryMapFileName()
    {
        return m_mapFileName;
    }

    private void sendToServletForImport(String p_jobName, String[] p_files,
            String p_taskId)
    {
        try
        {
            s_logger.info("Calling the URL");
            //URL m_url = new URL("http://10.0.0.114:7001/TranslateServlet?");
            URL m_url = new URL(m_ambServer);
            URLConnection conn = m_url.openConnection();
            conn.setDoOutput(true);

            String p_line = createParameters(p_jobName, p_files, p_taskId);
            // The string has been URLencoded and consists of ASCII
            // characters only. Still we have to write UTF8.
            OutputStreamWriter wr = new OutputStreamWriter(conn
                    .getOutputStream(), "UTF8");
            wr.write(p_line);
            wr.flush();
            wr.close();
            // Read lines from the output
            String l = null;
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                    .getInputStream()));
            while ((l = rd.readLine()) != null)
            {
                s_logger.info(l);
            }
            rd.close();
        }
        catch (Exception e)
        {
            s_logger.error("Exception while Calling the URL " + e);
        }
    }

    public String createParameters(String p_jobName, String[] p_files,
            String p_taskId)
    {
        StringBuffer line = new StringBuffer();

        for (int i = 0; i < p_files.length; i++)
        {
            String fileName = p_files[i];
            // This information is common to all files
            // so getting it once should be enough
            if (i == 0)
            {
                String archivePath = m_mountDir + "/" + m_defaultStore;
                String vpath = fileName
                        .substring(fileName.indexOf(m_defaultStore) - 1,
                                fileName.lastIndexOf("/"));
                String directoryName = vpath
                        .substring(vpath.lastIndexOf("/") + 1);
                String tempAreaName = fileName.substring(fileName
                        .indexOf("WORKAREA") + 9);
                String areaName = tempAreaName.substring(0, tempAreaName
                        .indexOf("/"));
                String areaPath = fileName.substring(0, fileName
                        .indexOf(areaName)
                        + areaName.length());
                String branchPath = fileName.substring(0, fileName
                        .indexOf("WORKAREA") - 1);
                //                    String branchName  = branchPath.substring(branchPath.lastIndexOf("/") + 1);
                String branchName = branchPath.substring(branchPath
                        .lastIndexOf("/"));
                String directoryPath = fileName.substring(0, fileName
                        .lastIndexOf("/"));

                line.append(URLEncoder.encode("user_name", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_teamsiteUserName, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("stage", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode("import", "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("autoImport", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode("autoImport", "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("user_role", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_teamsiteUserPrivilege,
                        "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("teamsite_server", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_teamsiteServer, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("mount_path", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_mountDir, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("jobName", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(p_jobName, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("fileProfile", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_fileProfile, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("subpage_type", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(SUB_PAGE_TYPE, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("page_type", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(PAGE_TYPE, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("vpath", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(vpath, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("archive_name", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_defaultStore, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("archive_path", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(archivePath, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("branch_name", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(branchName, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("branch_path", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(branchPath, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("area_name", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(areaName, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("area_path", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(areaPath, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("directory_path", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(directoryPath, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("directory_name", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(directoryName, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("task_id", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(p_taskId, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("overwriteSource", "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_overwriteSource, "UTF-8"));
                line.append(AMPERSAND);

                line.append(URLEncoder.encode("callbackImmediately",
                        "UTF-8"));
                line.append(EQUALS);
                line.append(URLEncoder.encode(m_callbackImmediately,
                        "UTF-8"));
                line.append(AMPERSAND);
            }

            // Rest of the information is specific to
            // each file. 
            fileName = m_mountDir + fileName;
            File file = new File(fileName);
            // Get the number of bytes in the file
            String fileSize = (new Long(file.length())).toString();
            String fileNameOnly = fileName
                    .substring(fileName.lastIndexOf("/") + 1);

            line.append(URLEncoder.encode("type_" + i, "UTF-8"));
            line.append(EQUALS);
            line.append(URLEncoder.encode("file", "UTF-8"));
            line.append(AMPERSAND);

            line.append(URLEncoder.encode("size_" + i, "UTF-8"));
            line.append(EQUALS);
            line.append(URLEncoder.encode(fileSize, "UTF-8"));
            line.append(AMPERSAND);

            line.append(URLEncoder.encode("name_" + i, "UTF-8"));
            line.append(EQUALS);
            line.append(URLEncoder.encode(fileNameOnly, "UTF-8"));
            line.append(AMPERSAND);

            line.append(URLEncoder.encode("path_" + i, "UTF-8"));
            line.append(EQUALS);
            line.append(URLEncoder.encode(fileName, "UTF-8"));
            line.append(AMPERSAND);
        }
        return line.toString();
    }

    public static void main(String args[])
    {
        for (int i = 0; i < args.length; i++)
        {
            s_logger.info("ARG " + i + " " + args[i]);
        }

        /**
         * The arguments passed to the external cgi are 0. jobid 1824 1. task_id
         * 1828 2. area vpath \store2003\main\SourceEnglishGautam\WORKAREA\HTML
         * 3. file0 en_US\gautamp\HPData_jan20\HTML\options_supplies.html 4.
         * file1 en_US\gautamp\HPData_jan20\HTML\support.html and so on...
         */
        int size = args.length - 3; // The first three parameters are not files.
        String[] files = new String[size];
        String taskId = args[1];
        String areaVpath = args[2].replaceAll("\\\\", "/");
        for (int i = 0; i < size; i++)
        {
            String file = args[i + 3].replaceAll("\\\\", "/");
            files[i] = areaVpath + "/" + file;
        }

        /**
         * first let's read the properties file and generate mappings that
         * contain information about the jobName and file profile name to use
         */
        try
        {
            AutomaticImporter ai = new AutomaticImporter();
            ai.getFileProfileAndJobNameInfo(areaVpath);
            ai.doAutomaticImport(taskId, files);
        }
        catch (Exception e)
        {
            s_logger.error("AutomaticImporter:: failed to import from workflow"
                    + e);
        }
    }

    /**
     * Removes the files from the Set that do not have the appropriate file
     * extension for the associated file profile
     * 
     * @param p_filesToImport
     * @param p_fileProfile
     *            file profile associated with directory
     * @exception Exception
     */
    private String[] removeInappropriateFiles(String[] p_filesToImport)
    {
        if (m_extensions == null)
        {
            s_logger.debug("No extensions mentioned so importing all files.");
            return p_filesToImport;
        }
        HashSet extSet = new HashSet();
        StringTokenizer st = new StringTokenizer(m_extensions);
        while (st.hasMoreTokens())
        {
            extSet.add(st.nextToken());
        }
        Vector filesToImport = new Vector();
        int count = 0;
        for (int r = 0; r < p_filesToImport.length; r++)
        {
            String fileName = (String) p_filesToImport[r];
            int idx = fileName.lastIndexOf(".");
            s_logger.info(" idx is " + idx);
            if (idx == -1) // This may be a DCR
            {
                idx = fileName.lastIndexOf("templatedata");
                if (idx > -1) // This is a DCR
                {
                    filesToImport.addElement(fileName);
                }
            }
            else
            {
                String ext = fileName.substring(idx + 1).toLowerCase();
                if (extSet.contains(ext.toLowerCase()) == false)
                {
                    //this file's extension is not in the extension set so remove the file
                    s_logger.warn("Ignoring file " + fileName
                            + " because extension ." + ext + " does not match");
                }
                else
                {
                    filesToImport.addElement(fileName);
                }
            }
        }
        String[] returnFilesToImport = new String[filesToImport.size()];
        for (int i = 0; i < filesToImport.size(); i++)
        {
            returnFilesToImport[i] = (String) filesToImport.elementAt(i);
        }
        return returnFilesToImport;
    }
}
