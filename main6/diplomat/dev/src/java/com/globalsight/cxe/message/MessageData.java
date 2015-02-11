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
package com.globalsight.cxe.message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * The MessageData interface represents the data for
 * a CxeMessage object
 */
public interface MessageData extends Serializable
{
    /**
     * Returns the name of the MessageData object.
     * This could be a filename, primary key, etc.
     * 
     * @return name as String
     */
    public String getName();


    /**
     * Gets an inputstream associated with this MessageData object
     * that can be used to read the message data.
     * 
     * @return InputStream
     * @exception IOException
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Gets an outputstream associated with this MessageData object
     * that can be used to write the message data.
     * 
     * @return OutputStream
     * @exception IOException     
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Deletes the underlying message data. After deletion
     * other methods on this MessageData object may throw
     * exceptions
     * 
     * @exception IOException
     */
    public void delete() throws IOException;


    /**
     * Returns the byte size of the data represented by this
     * MessageData objects.
     * 
     * @return size in bytes
     * @exception IOException
     */
    public long getSize() throws IOException;

    /**
     * Copies the message data to the given File.
     * 
     * @param p_file a file to write to
     * @exception IOException
     */
    public void copyTo(File p_file) throws IOException;

    /**
     * Fills the messageData with content from the given File.
     * 
     * @param p_file a source file
     * @exception IOException     
     */
    public void copyFrom(File p_file) throws IOException;

    /**
     * Fills this MessageData with content from the other MessageData
     * 
     * @param p_other -- another message data
     * @exception IOException     
     */
    public void copyFrom(MessageData p_other) throws IOException;
}

