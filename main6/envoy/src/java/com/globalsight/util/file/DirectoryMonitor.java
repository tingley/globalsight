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
package com.globalsight.util.file;

import com.globalsight.util.CollectionHelper;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import com.globalsight.diplomat.util.Logger;

/**
* A DirectoryMonitor can detect new, deleted, and modified files
* in a directory. It polls the files' last modification time.
*/
public class DirectoryMonitor implements Serializable
{
    //private static final long serialVersionUID=1L;
    private static final long serialVersionUID=981320241581273528L;

    /****** PRIVATE SECTION ********/
    //PRIVATE MEMBER VARIABLES
    private String m_directoryName;
    private boolean m_assumeUnencounteredFilesAreNewOnFirstScan;

    //current files in the directory. This HashMap contains FileModificationTimeSnapshot objects
    private HashMap m_currentFiles;

    //state to record the results of change detection
    private boolean m_changesWereDetected;

    //state to record the fact that the initial scan happened
    private boolean m_onFirstScan = true;

    //These sets contain only the fileNames of the files, not FileModificationTimeSnapshot
    private HashSet m_newFiles;
    private HashSet m_deletedFiles;
    private HashSet m_modifiedFiles;
    private HashSet m_newAndModifiedFiles;

    //PRIVATE METHODS
    
    /**
    * clears out the new, deleted, modified, and
    * newAndModified file sets
    */
    private void clearStateFromLastCheck()
    {
        m_newFiles.clear();
        m_deletedFiles.clear();
        m_modifiedFiles.clear();
        m_newAndModifiedFiles.clear();
        m_changesWereDetected = false;
    }

    /**
    * reads the directory for its list of files
    * and sets m_currentFiles. Ignores if not a directory
    */
    private HashMap readCurrentFiles()
    throws IOException
    {
        Logger theLogger = Logger.getLogger();
        File dir = new File(m_directoryName);
        HashMap currentFiles = new HashMap();
        FileModificationTimeSnapshot snapshot = null;
        if (dir.isDirectory())
        {
            File[] files = dir.listFiles();
            File aFile;
            for (int i=0; i < files.length; i++)
            {
                aFile = files[i];
                if (aFile.isFile())
                {
                    snapshot = new FileModificationTimeSnapshot(aFile);
                    currentFiles.put(snapshot.fileName(),snapshot);
                }
            }
        }
        return currentFiles;
    }


    /**
    * Adds all the current files to the new and newAndModified file sets
    */
    private void addCurrentFilesAsNew()
    throws IOException
    {
        Object[] values = m_currentFiles.values().toArray();
        FileModificationTimeSnapshot snapshot;

        for (int i=0; i < values.length; i++)
        {
            snapshot = (FileModificationTimeSnapshot) values[i];
            m_newFiles.add(snapshot.fileName());
        }

        if (m_newFiles.size() > 0)
        {
            m_changesWereDetected = true;
            m_newAndModifiedFiles.addAll(m_newFiles);
        }
    }

    /**
     * Actually performs the change detection algorithm of comparing the last
     * and current file sets from the previous poll and this poll
     */
    private void compareLastAndCurrentFiles()
    throws IOException
    {
        int oldSize = m_currentFiles.size();
        HashMap nextCurrentFiles = readCurrentFiles();
        int newSize = nextCurrentFiles.size();
        Logger.getLogger().println(Logger.DEBUG_D, "Directory " + m_directoryName + ": before scan " +
                                   oldSize + " files, afterScan " + newSize + " files.");

        Iterator currentFilesIterator = m_currentFiles.values().iterator();
        FileModificationTimeSnapshot lastSnapshot = null;
        FileModificationTimeSnapshot newSnapshot = null;

        while (currentFilesIterator.hasNext())
        {
            lastSnapshot = (FileModificationTimeSnapshot) currentFilesIterator.next();

            if (nextCurrentFiles.containsKey(lastSnapshot.fileName()))
            {
                newSnapshot = (FileModificationTimeSnapshot) nextCurrentFiles.get(
                    lastSnapshot.fileName());

                if (newSnapshot.timeLastModified() != lastSnapshot.timeLastModified())
                {
                    m_modifiedFiles.add(newSnapshot.fileName());
                }
            }
            else
            {
                m_deletedFiles.add(lastSnapshot.fileName());
            }
        }

        determineNewFiles(nextCurrentFiles);

        m_newAndModifiedFiles.addAll(m_newFiles);
        m_newAndModifiedFiles.addAll(m_modifiedFiles);

        if (m_newAndModifiedFiles.size() > 0 ||
            m_deletedFiles.size() > 0)
        {
            m_changesWereDetected = true;
        }
        
        m_currentFiles = nextCurrentFiles;
    }

    /**
    * Determines the files that are in the nextCurrentFiles
    * but not in the current set of files
    */
    private void determineNewFiles (HashMap nextCurrentFiles)
    {
        HashMap difference = CollectionHelper.hashMapDifference(
            m_currentFiles, nextCurrentFiles);
        Object[] values = difference.values().toArray();
        FileModificationTimeSnapshot snapshot;

        for (int i=0; i < values.length; i++)
        {
            snapshot = (FileModificationTimeSnapshot) values[i];
            m_newFiles.add(snapshot.fileName());
        }
    }


    /****** PUBLIC SECTION ********/

    //CONSTRUCTORS
    /**
    * Constructs a DirectoryMonitor for the given directory
    * The default is to assume that unencountered files are new
    * on the first scan.
    * @param p_directoryName -- the directory to monitor
    */
    public DirectoryMonitor(String p_directoryName)
    {
        m_currentFiles = new HashMap();
        m_changesWereDetected = false;
        m_directoryName = p_directoryName;
        m_assumeUnencounteredFilesAreNewOnFirstScan = true;

        m_newFiles = new HashSet();
        m_deletedFiles = new HashSet();
        m_modifiedFiles = new HashSet();
        m_newAndModifiedFiles = new HashSet();
    }

    /**
    * Constructs a DirectoryMonitor for the given directory
    * @param p_directory -- the directory to monitor
    */
    public DirectoryMonitor(File p_directory)
    {
        this(p_directory.getAbsolutePath());
    }


    //PUBLIC METHODS

    /**
    * Returns whether unencountered files will be treated as new
    * on the first time scan of the directory
    *
    * @return boolean
    */
    public boolean getAssumeUnencounteredFilesAreNewOnFirstScan()
    {
        return m_assumeUnencounteredFilesAreNewOnFirstScan;
    }

    /**
    * Sets whether unencountered files will be treated as new
    * on the first time scan of the directory
    *    
    * NOTE: this is only useful if called before detectChanges()
    */
    public void setAssumeUnencounteredFilesAreNewOnFirstScan(boolean p_bool)
    {
        m_assumeUnencounteredFilesAreNewOnFirstScan = p_bool;
    }


    /**
    * Performs the change detection
    */
    public synchronized void detectChanges()
    throws IOException
    {
        clearStateFromLastCheck();
       
        if(m_currentFiles.size() == 0)
        {
            int oldSize = m_currentFiles.size();
            m_currentFiles = readCurrentFiles();
            int newSize = m_currentFiles.size();
            Logger.getLogger().println(Logger.DEBUG_D, "Directory " + m_directoryName + ": before scan " +
                                       oldSize + " files, afterScan " + newSize + " files.");
            if (newSize > 0)
            {
                if (m_assumeUnencounteredFilesAreNewOnFirstScan && m_onFirstScan)
                {
                    Logger.getLogger().println(Logger.DEBUG_D, "Assuming all files in directory " + m_directoryName + 
                                               " are new...");
                    addCurrentFilesAsNew();
                }
                else if (!m_onFirstScan)
                {
                    addCurrentFilesAsNew();
                }
                else
                {
                    //otherwise we're on the first scan and are ignoring new files
                    StringBuffer sb = new StringBuffer("There are ");
                    sb.append(newSize).append(" files in ").append(m_directoryName);
                    sb.append(" but they are being ignored because this is the first scan, and the value of ");
                    sb.append(" assumeUnencounteredFilesAreNew is false.");
                    Logger.getLogger().println(Logger.INFO, sb.toString());
                }
            }
        }
        else
        {
            compareLastAndCurrentFiles();
        }

        m_onFirstScan = false; //the first scan is completed
    }

    /**
    * Returns true if any changes were detected during the last polling
    * @return boolean
    */
    public boolean changesWereDetected()
    {
        return m_changesWereDetected;
    }

    /**
    * Returns an unmodifable set of the files that were added to
    * the directory.
    *
    * @return unmodifable Set
    */
    public Set newFiles()
    {
        return Collections.unmodifiableSet(m_newFiles);
    }

    /**
    * Returns an unmodifiable set of files that were deleted from the
    * directory
    *
    * @return unmodifiable Set
    */
    public Set deletedFiles()
    {
        return Collections.unmodifiableSet(m_deletedFiles);
    }

    /**
    * Returns an unmodifiable set of files that were modified
    *
    * @return unmodifiable Set
    */
    public Set modifiedFiles()
    {
        return Collections.unmodifiableSet(m_modifiedFiles);
    }

    /**
    * Returns an unmodifiable set of files that were modified
    * or added to the directory
    *
    * @return unmodifiable Set
    */
    public Set newAndModifiedFiles()
    {
        return Collections.unmodifiableSet(m_newAndModifiedFiles);
    }

    /**
    * Returns the directory name associated with the DirectoryMonitor
    * @return directory name
    */
    public String getDirectoryName()
    {
        return m_directoryName;
    }

    //OVERRIDDEN PUBLIC METHODS
    /**
    * Returns the hashCode of the DirectoryMonitor
    * @return int
    */
    public int hashCode()
    {
        return m_directoryName.hashCode();
    }

    /**
    * Returns whether two DirectoryMonitor objects are equal.
    * They are equal if they monitor the same directory
    * @return boolean
    */
    public boolean equals(Object p_other)
    {
        return m_directoryName.equals(p_other);
    }

    /**
    * Returns a hashmap of the current files for this directory monitor.
    * <br>
    * @return HashMap
    */
    public HashMap getCurrentFiles()
    {
        return m_currentFiles;
    }
}

