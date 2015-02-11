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
import com.globalsight.diplomat.util.Logger;
import java.io.Serializable;

/**
* A DirectoryMapEntry holds a DirectoryMonitor, a collection of File
* Profiles, and a JobNamePrefix
*/
public class DirectoryMapEntry implements Serializable
{
    /****** PRIVATE SECTION ********/
    //PRIVATE STATIC VARIABLES
    private static final int INITIAL_JOBNAME_SEQUENCE_VALUE = 1;
    
    //PRIVATE MEMBER VARIABLES
    private DirectoryMonitor m_directoryMonitor;
    private String m_jobNamePrefix;
    private String[] m_fileProfileNames;
    private int m_jobPostFixSequence;

    //PRIVATE METHODS

    /** initializes the internal member data. used by the constructors*/
    private void initializeMembers(DirectoryMonitor p_directoryMonitor,
                                   String p_jobNamePrefix,
                                   String[] p_fileProfileNames)
    {
        m_directoryMonitor = p_directoryMonitor;
        m_jobNamePrefix = p_jobNamePrefix;
        m_fileProfileNames = p_fileProfileNames;
        m_jobPostFixSequence = 1;

        Logger.getLogger().println(
            Logger.DEBUG_D,
            "Creating new DirectoryMapEntry (" +
            m_directoryMonitor.getDirectoryName() + 
            "," + m_jobNamePrefix + "," + m_fileProfileNames[0] +
            ")");
    }
    

    /****** PUBLIC SECTION ********/

    //CONSTRUCTORS
    /**
    * Constructs a DirectoryMapEntry
    * 
    * @param p_directoryMonitor
    * @param p_jobNamePrefix
    * @param p_fileProfileName
    */
    public DirectoryMapEntry(DirectoryMonitor p_directoryMonitor,
                             String p_jobNamePrefix,
                             String p_fileProfileName)
    {
        String[] fileProfileNames = { p_fileProfileName };
        initializeMembers(p_directoryMonitor, p_jobNamePrefix, fileProfileNames);
    }

    /**
    * Constructs a DirectoryMapEntry
    * 
    * @param p_directoryMonitor
    * @param p_jobNamePrefix
    * @param p_fileProfileNames -- an array of file profiles
    */
    public DirectoryMapEntry(DirectoryMonitor p_directoryMonitor,
                             String p_jobNamePrefix,
                             String[] p_fileProfileNames)
    {
        initializeMembers(p_directoryMonitor, p_jobNamePrefix, p_fileProfileNames);
    }


    //PUBLIC METHODS
    /**
    * Gets the DirectoryMonitor
    * @return DirectoryMonitor
    */
    public DirectoryMonitor getDirectoryMonitor()
    {
        return m_directoryMonitor;
    }

    /**
    * Generates the next JobName based on the JobNamePrefix and
    * the internal sequence. This sequence is incremented after
    * a call to nextJobName()
    *
    * @return the next job name
    */
    public String nextJobName()
    {
        String nextJobName = m_jobNamePrefix + m_jobPostFixSequence;
        m_jobPostFixSequence++;
        return nextJobName;
    }

    //ACCESSORS/MUTATORS FOR ATTRIBUTES
    /**
    * Gets the JobNamePrefix
    * @return the job prefix name
    */
    public String getJobNamePrefix()
    {
        return m_jobNamePrefix;
    }

    /**
    * Gets the FileProfileNames
    * @return the file profile names as a String[]
    */
    public String[] getFileProfileNames()
    {
        return m_fileProfileNames;
    }

    /**
    * Sets the JobNamePrefix. If the new job prefix name is different from
    * the old one, then the internal jobname counter is reset to 1.
    */
    public void setJobNamePrefix(String p_newJobNamePrefix)
    {
        if (!m_jobNamePrefix.equals(p_newJobNamePrefix))
        {
            m_jobNamePrefix = p_newJobNamePrefix;
            m_jobPostFixSequence = INITIAL_JOBNAME_SEQUENCE_VALUE;
        }
     }

    /**
    * Sets the FileProfileNames
    */
    public void setFileProfileNames(String[] p_fileProfileNames)
    {
        m_fileProfileNames = p_fileProfileNames;
    }


    //OVERRIDDEN PUBLIC METHODS
    /**
    * Returns the hashCode of the DirectoryMonitor
    *
    * @return int
    */
    public int hashCode()
    {
        return m_directoryMonitor.hashCode();
    }

    /**
    * Returns whether two DirectoryMapEntry objects are equal.
    * They are equal if they have the same directory monitor
    *
    * @return boolean
    */
    public boolean equals(Object p_other)
    {
        return m_directoryMonitor.equals(p_other);
    }
}

