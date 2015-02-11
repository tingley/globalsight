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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.globalsight.util.ProcessRunner;

/**
 * The FileMessageData class represents a MessageData object that is tied to
 * particular file on the file system. The data for the message is stored in
 * that file.
 */
public class FileMessageData implements MessageData
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    static private Logger s_logger = Logger.getLogger(FileMessageData.class);

    // The file that contains the message data.
    private File m_file;
    // The name of the file.
    private String m_name;

    // ////////////////////////////////////
    // Constructors //
    // ////////////////////////////////////
    /**
     * Creates a new FileMessageData object
     */
    public FileMessageData(String p_filename)
    {
        m_name = p_filename;
        m_file = new File(m_name);
    }

    // /////////////////////////////////////////
    // Interface Implementation: MessageData //
    // /////////////////////////////////////////

    /**
     * Returns the name of the MessageData object. This could be a filename,
     * primary key, etc.
     * 
     * @return name as String
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Gets a new inputstream associated with this MessageData object that can
     * be used to read the message data.
     * 
     * @return InputStream
     * @exception IOException
     */
    public InputStream getInputStream() throws IOException
    {
        return new FileInputStream(m_file);
    }

    /**
     * Gets an outputstream associated with this MessageData object that can be
     * used to write the message data.
     * 
     * @return OutputStream
     * @exception IOException
     */
    public OutputStream getOutputStream() throws IOException
    {
        return new FileOutputStream(m_file);
    }

    /**
     * Deletes the underlying message data. After deletion other methods on this
     * MessageData object may throw exceptions
     * 
     * @exception IOException
     */
    public void delete() throws IOException
    {
        boolean value = m_file.delete();

        if (value == false)
        {
            s_logger.warn("Failed to delete message data file " + m_name);
        }
    }

    /**
     * Returns the byte size of the data represented by this MessageData object.
     * 
     * @return size in bytes
     * @exception IOException
     */
    public long getSize() throws IOException
    {
        return m_file.length();
    }

    public void operatingSystemSafeCopyTo(File p_file) throws IOException
    {
        if (isWindows())
        {
            String command = "cmd /c copy \"" + m_name + "\" " + "\""
                    + p_file.getAbsolutePath() + "\"";
            exec(command);
        }
        else
        {
            copyTo(p_file);
        }
    }

    public void operatingSystemSafeCopyFrom(File p_file) throws IOException
    {
        if (isWindows())
        {
            String command = "cmd /c copy \"" + p_file.getAbsolutePath()
                    + "\" " + " \"" + m_name + "\"";
            exec(command);
        }
        else
        {
            copyFrom(p_file);
        }
    }

    private boolean isWindows()
    {
        String os = System.getProperty("os.name");
        return os.startsWith("Win");
    }

    private void exec(String p_command)
    {
        s_logger.debug("Executing command: " + p_command);

        ProcessRunner pr = new ProcessRunner(p_command, System.out, System.err);
        Thread t = new Thread(pr, "FileMessageData_exec");
        t.start();

        try
        {
            t.join();
        }
        catch (InterruptedException ie)
        {
        }
    }

    /**
     * Copies the message data to the given File.
     * 
     * @param p_file
     *            a file to write to
     * @exception IOException
     */
    public void copyTo(File p_file) throws IOException
    {
        doCopy(this.getInputStream(), new FileOutputStream(p_file));
    }

    /**
     * Fills the messageData with content from the given File.
     * 
     * @param p_file
     *            a source file
     * @exception IOException
     */
    public void copyFrom(File p_file) throws IOException
    {
        doCopy(new FileInputStream(p_file), this.getOutputStream());
    }

    // add for indd
    // Note: should not close Inputstream <code>in</code>.
    public void copyFrom(InputStream in) throws IOException
    {
        OutputStream out = this.getOutputStream();
        try
        {
            doCopyNotClose(in, out);
        }
        finally
        {
            out.close();
        }
    }

    /**
     * Fills this MessageData with content from the other MessageData.
     * 
     * @param p_other
     *            -- another message data
     * @exception IOException
     */
    public void copyFrom(MessageData p_other) throws IOException
    {
        doCopy(p_other.getInputStream(), this.getOutputStream());
    }

    // //////////////////
    // Public Methods //
    // //////////////////

    /**
     * Returns the underlying file object.
     */
    public File getFile()
    {
        return m_file;
    }

    // //////////////////////
    // Private Methods //
    // //////////////////////

    /**
     * Copies chunks of bytes from the InputStream to the OutputStream.
     * 
     * @param p_from
     *            source InputStream
     * @param p_to
     *            target OutputStream
     * @exception IOException
     */
    private void doCopy(InputStream p_from, OutputStream p_to)
            throws IOException
    {
        // Modify for indd
        /*
         * BufferedInputStream bis = new BufferedInputStream(p_from);
         * BufferedOutputStream bos = new BufferedOutputStream(p_to); byte[] buf
         * = new byte[10240]; int count = 0; while ((count = bis.read(buf)) !=
         * -1) { bos.write(buf, 0, count); } bis.close(); bos.close();
         */
        doCopyNotClose(p_from, p_to);
        // these two statetments should be enclosed in finally statement.
        p_from.close();
        p_to.close();
    }

    private void doCopyNotClose(InputStream p_from, OutputStream p_to)
            throws IOException
    {
        BufferedInputStream bis = new BufferedInputStream(p_from);
        BufferedOutputStream bos = new BufferedOutputStream(p_to);
        byte[] buf = new byte[10240];
        int count = 0;
        while ((count = bis.read(buf)) != -1)
        {
            bos.write(buf, 0, count);
        }
        bos.flush(); // Don't forget to flush
    }
}
