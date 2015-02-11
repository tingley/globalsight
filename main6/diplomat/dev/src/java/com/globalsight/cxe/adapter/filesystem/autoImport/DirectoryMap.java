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

import com.globalsight.util.file.DirectoryMonitor;
import com.globalsight.util.file.DelimitedFileReader;
import java.io.Serializable;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.globalsight.diplomat.util.Logger;

/**
* A DirectoryMap holds DirectoryMapEntry objects
*/
public class DirectoryMap implements Serializable
{
    /****** PRIVATE SECTION ********/
    private static final long serialVersionUID = 4447318073443251115L;

    //PRIVATE STATIC
    private static final String MAP_FILE_DELIMITER = "|";
    private static final String MAP_FILE_COMMENT = "#";

    //PRIVATE MEMBER VARIABLES
    private String m_name;
    //relative path (from CLASSPATH) to where the map file is
    private String m_mapFileName;
    private String m_baseDirectory;
    private HashMap m_directoryMapEntries;
    private long m_mapFileLastModTime = 0;
    
    //the real map file path (not persisted)
    private transient String m_mapFilePath = null;

    private boolean m_assumeUnencounteredFilesAreNewOnFirstScan = true;


    //PRIVATE METHODS


    /**
    * Adds a new DirectoryMapEntry or updates an existing one if the directory
    * exists. Also removes directoryMapEntries if the directory does not
    * exist anymore.
    *@param p_directoryName -- name of the directory
    *@param p_jobPrefixName -- job prefix name
    *@param p_fileProfile -- the file profile to use
    */
    private void addOrModifyDirectoryMapEntries(String p_directoryName,
                                                String p_jobPrefixName,
                                                String p_fileProfile)
    {

        File directory = new File (p_directoryName);
        if (directory.exists() && directory.isDirectory())
        {
            DirectoryMapEntry mapEntry = 
            (DirectoryMapEntry) m_directoryMapEntries.get(p_directoryName);

            if (mapEntry == null)
            {
                DirectoryMonitor monitor = new DirectoryMonitor(p_directoryName);
                monitor.setAssumeUnencounteredFilesAreNewOnFirstScan(
                 m_assumeUnencounteredFilesAreNewOnFirstScan);
                
                mapEntry = new DirectoryMapEntry(monitor,
                                                 p_jobPrefixName,
                                                 p_fileProfile);

                m_directoryMapEntries.put(p_directoryName,mapEntry);
            }
            else
            {
                String[] fileProfileNames = {p_fileProfile};
                mapEntry.setJobNamePrefix(p_jobPrefixName);
                mapEntry.setFileProfileNames(fileProfileNames);
            }
        }
        else
        {
            Logger.getLogger().println(Logger.WARNING,
                                     "The directory " + p_directoryName +
                                     " does not exist and cannot be monitored.");
            m_directoryMapEntries.remove(p_directoryName);
        }
    }

    /**
    * Removes directories not listed in the map file from the DirectoryMap
    *
    * @param p_listedDirectories -- String array of directories in the map file
    */

    private void removeUnlistedDirectories(HashSet p_listedDirectories)
    {
        m_directoryMapEntries.keySet().retainAll(p_listedDirectories);
    }

    /****** PUBLIC SECTION ********/

    //CONSTRUCTORS
    /**
    * Constructs a DirectoryMap
    * 
    * @param p_name -- an ID for the DiretoryMap
    * @param p_mapFileName -- the name of the mapping file, relative to CLASSPATH
    * @param p_baseDirectory -- all directories in the mapping file are
    * viewed as being relative to this directory
    */
    public DirectoryMap(String p_name, String p_mapFileName, String p_baseDirectory)
    {
        m_name = p_name;
        m_mapFileName = p_mapFileName;
        m_baseDirectory = p_baseDirectory;
        m_directoryMapEntries = new HashMap();
    }

    //PUBLIC METHODS
    /**
    * Gets a list of the DirectoryMap Entries.
    * @return ArrayList of DirectoryMapEntry objects
    */
    public ArrayList getDirectoryMapEntries()
    {
        return(new ArrayList(m_directoryMapEntries.values()));
    }

    /**
    * Updates the directoryMap if the directory map file has been changed.
    *
    * Updates the DirectoryMap using the associated map file.
    * A DirectoryMonitor is created for each directory listed
    * in the mapfile if it does not already exist in the DirectoryMap.
    * If it does already exist, then it's other DirectoryMapEntry
    * attributes are updated. Also, DirectoryMapEntries that no longer
    * exist in the file are deleted from the DirectoryMap.
    *
    * @param assumeUnencounteredFilesAreNew -- true if when, on the first
    * scan of a directory, files should be treated as new
    *
    * NOTE: the assumeUnencounteredFilesAreNew flag only affects new
    * DirectoryMap entries -- it is irrelevant for existing directory map
    * entries
    *
    * @return boolean true if an update was needed
    */
    public boolean updateDirectoryMapIfNeeded(
        boolean p_assumeUnencounteredFilesAreNewOnFirstScan)
    throws Exception
    {
        boolean updateWasNeeded = false;
        m_assumeUnencounteredFilesAreNewOnFirstScan =
            p_assumeUnencounteredFilesAreNewOnFirstScan;

        if (m_mapFilePath == null)
        {
            //first check if the m_mapFileName includes path information.
            //as an older version of the DirectoryMap class did it that way
            int idx = m_mapFileName.indexOf("/properties/");
            if (idx != 0)
            {
                String newMapFileName = m_mapFileName.substring(idx);
                Logger.getLogger().println(
                    Logger.INFO,
                    "Directory map file name '" + m_mapFileName +
                    "' is pre Ambassador 6.4. Migrating to new format: " + newMapFileName);
                m_mapFileName = newMapFileName;
            }

            URL url = DirectoryMap.class.getResource(m_mapFileName);
            if (url == null)
                throw new FileNotFoundException("Map file " + m_mapFileName + " not found.");
            m_mapFilePath = url.toURI().getPath();
        }
        File mapFile = new File (m_mapFilePath);
        long currentMapFileLastModTime = mapFile.lastModified();

        if (currentMapFileLastModTime > m_mapFileLastModTime)
        {
            m_mapFileLastModTime = currentMapFileLastModTime;
            updateWasNeeded = true;
            updateDirectoryMap();
        }

        return updateWasNeeded;
    }

    /**
    * Performs the update described in updateDirectoryMapIfNeeded()
    */
    private void updateDirectoryMap()
    throws Exception
    {
        Logger logger = Logger.getLogger();
        logger.println(Logger.INFO, "Updating directory map");
        List lines = DelimitedFileReader.readLinesWithTokens(
                                                            m_mapFilePath, MAP_FILE_DELIMITER, MAP_FILE_COMMENT);
        String[] tokens;
        String directoryName;
        String fullPathDirectoryName;
        String jobPrefixName;
        String fileProfile;
        DirectoryMapEntry mapEntry;
        DirectoryMonitor monitor;
        HashSet listedDirectoryNames = new HashSet();
        int index = 0;

        for (int i=0; i < lines.size(); i++)
        {
            tokens = (String[])lines.get(i);
            index = 0;
            directoryName = tokens[index++];
            jobPrefixName = tokens[index++];
            fileProfile = tokens[index++];

            fullPathDirectoryName = m_baseDirectory + File.separator + directoryName;

            listedDirectoryNames.add(fullPathDirectoryName);
            addOrModifyDirectoryMapEntries(fullPathDirectoryName,
                                           jobPrefixName,
                                           fileProfile);

        }

        removeUnlistedDirectories(listedDirectoryNames);
    }

    //ACCESSORS/MUTATORS FOR ATTRIBUTES
    /**
    * Gets the name(ID) of the DirectoryMap
    * @return the directory map name
    */
    public String getDirectoryMapName()
    {
        return m_name;
    }

    /**
    * Gets the filname of the mapping file
    * @return the directory map file name
    */
    public String getDirectoryMapFileName()
    {
        return m_mapFileName;
    }

    //OVERRIDDEN PUBLIC METHODS
    /**
    * Returns the hashCode of the DirectoryMap name
    *
    * @return int
    */
    public int hashCode()
    {
        return m_name.hashCode();
    }

    /**
    * Returns whether two DirectoryMap objects are equal.
    * They are equal if they have the same name
    *
    * @return boolean
    */
    public boolean equals(Object p_other)
    {
        return m_name.equals(p_other);
    }
}

