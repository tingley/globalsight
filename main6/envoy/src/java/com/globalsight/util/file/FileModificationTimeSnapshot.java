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

import java.io.File;
import java.io.Serializable;
/**
* A FileModificationTimeSnapshot represents the modification time of a file
* at a particular moment in time.
*/
public class FileModificationTimeSnapshot implements Serializable
{
    
    //PRIVATE MEMBER VARIABLES
    private long m_timeLastModified;
    private String m_fileName;

    //CONSTRUCTORS
    /**
    * Constructs a FileModificationTimeSnapshot given a File
    * @param p_file -- a file to take a snapshot of
    */
    public FileModificationTimeSnapshot(File p_file)
    {
        m_timeLastModified = p_file.lastModified();
        m_fileName = p_file.getAbsolutePath();
    }

    /**
    * Constructs a FileModificationTimeSnapshot given a File
    * @param p_filename -- the name of a file to take a snapshot of
    */
    public FileModificationTimeSnapshot(String p_filename)
    {
        this(new File(p_filename));
    }

    //PUBLIC METHODS
    /**
    * Returns the hashCode of the filename
    * @return int
    */
    public long timeLastModified()
    {
        return m_timeLastModified;
    }


    /**
    * Returns the filename of the file represented by the snapshot
    * @return int
    */
    public String fileName()
    {
        return m_fileName;
    }

    //OVERRIDDEN PUBLIC METHODS
    /**
    * Returns the hashCode of the filename
    * @return int
    */
    public int hashCode()
    {
        return m_fileName.hashCode();
    }

    /**
    * Returns whether two FileModificationTime objects are equal.
    * They are equal if the filenames are the same
    * @return boolean
    */
    public boolean equals(Object p_other)
    {
        return m_fileName.equals(p_other);
    }
}

