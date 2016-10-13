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

//JDK
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

//globalsight
import com.globalsight.diplomat.util.Logger;

/**
* The FileWaiter class can be used to wait for a file to be written.
* Normally, if you're accessing a file over NFS, then you'll see different behavior
* whether you're on Solaris on NT in terms of being able to read the file while it's
* still being written. This class should help get around that problem.
*/
public class FileWaiter
{

    //////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS                                                  //
    //////////////////////////////////////////////////////////////////////
    private long m_sleepTime;
    private long m_maxTimeToWait;
    private long m_timeSpentSoFar;
    private String m_fileName;
    private Logger m_logger;


    //////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS                                                   //
    //////////////////////////////////////////////////////////////////////
    /**
    * Creates a FileWaiter.
    * @param p_sleepTime -- the amount of time to sleep between detection attempts
    * @param p_maxTimeToWait -- the total amount of time to wait for this file
    * @param p_fileName -- the file to wait for
    */
    public FileWaiter(long p_sleepTime, long p_maxTimeToWait, String p_fileName)
    {
        this(p_sleepTime, p_maxTimeToWait, p_fileName, false);
    }
    
    /**
     * Creates a FileWaiter.
     * @param p_sleepTime -- the amount of time to sleep between detection attempts
     * @param p_maxTimeToWait -- the total amount of time to wait for this file
     * @param p_fileName -- the file to wait for
     * @param noLogger -- true for do not init Logger in this method
     */
    public FileWaiter(long p_sleepTime, long p_maxTimeToWait, String p_fileName, boolean noLogger)
    {
        m_sleepTime = p_sleepTime;
        m_maxTimeToWait = p_maxTimeToWait;
        m_timeSpentSoFar = 0L;
        m_fileName = p_fileName;
        m_logger = noLogger ? null : Logger.getLogger();
    }

    /**
    * Will attempt to detect the file. If the file fails to appear and be
    * completely written before the maxTimeToWait is up, then  this will
    * throw IOExceptions. It may also throw any IOExceptions resulting from
    * trying to detect the file.
    * If the file appears and is completely written out, then the method
    * returns normally.
    * @throws IOException
    */
    public void waitForFile() throws IOException
    {
	if (waitForFileToAppear())
	{
	    if (waitForFileToBeWritten())
		return;
	    else
		throw new IOException("Timeout when waiting for file completion for file " + m_fileName);
	}
	else
	    throw new IOException("Timeout when looking for file " + m_fileName);
    }

    //////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS                                                  //
    //////////////////////////////////////////////////////////////////////
    
    private void logMessage(int level, String msg)
    {
        if (m_logger != null)
        {
            m_logger.println(level, msg);
        }
        else
        {
            System.out.println(msg);
        }
    }

    /**
    * Waits for the file to appear
    * Returns true if the file appeared, or false if it failed
    * to appear in the right time (timeout)
    */
    private boolean waitForFileToAppear()
    {
	File file = new File (m_fileName);
        while (m_timeSpentSoFar < m_maxTimeToWait)
        {
	    m_timeSpentSoFar += m_sleepTime;
            logMessage(Logger.DEBUG_D, "Waiting for file " + m_fileName + " to appear.");
            if (file.exists())
            {
                logMessage(Logger.DEBUG_D, "File " + m_fileName + " appeared.");
                return true;
            }
            else
            {
                sleep();
            }
        }
	return false;
    }

    /**
    * The file may have appeared, but may be incomplete. This method waits
    * until all writing on the file has stopped.
    * @throws IOException
    */
    private boolean waitForFileToBeWritten() throws IOException
    {
	byte[] buffer = new byte[32];
	boolean writingStarted = false;
	while (m_timeSpentSoFar < m_maxTimeToWait)
        {
	    m_timeSpentSoFar += m_sleepTime;
            try
            {
                logMessage(Logger.DEBUG_D, "Waiting for file " + m_fileName + " to be written.");
                FileInputStream fis = new FileInputStream(m_fileName);
                int bytesRead = fis.read(buffer);
                fis.close();
                if (bytesRead == -1)
                {
                    logMessage(Logger.DEBUG_A, "No data yet in file: " + m_fileName);
                }
                else
		{
		    writingStarted = true;
                    logMessage(Logger.DEBUG_A, "Conversion started for file: " + m_fileName);
                }
            }
            catch (FileNotFoundException fnfe)
            {
                //since the file exists, such an exception is only thrown
                //if the some process is still writing the file out on NT
                logMessage(Logger.DEBUG_D, "(NT?) Permission denied: " + fnfe.getMessage());
            }
            catch (IOException ioe)
            {
                //since the file exists, such an exception is only thrown
                //if the conversion server is still writing the file out on UNIX
                if (ioe.getMessage().indexOf("Permission denied") > -1)
                {
                    //do nothing because the file is still being written (we're on UNIX
                    //looking at a file on NT over NFS
                    logMessage(Logger.DEBUG_D, "(Solaris?) Permission denied: " + ioe.getMessage());
                }
                else
                {
		    //some real IOException
                    throw ioe;
                }
            }

	    if (writingStarted)
	    {
		waitForWritingToStop();
		return true;
	    }
	    else
		sleep();
        }
	return false;
    }


    /**
    * Since the conversion server apparently writes the file out in chunks, reading
    * from an InputStream might give the EOF value before the file is really done.
    * This causes only part of the file to be read in. This method will check the
    * file size periodically to determine when the file has stopped increasing in size.
    * <br>
    */
    private void waitForWritingToStop() throws IOException
    {
	try 
	{
	    File file = new File(m_fileName);
	    long lastFileSize= file.length();
	    boolean stillGrowing = true;
	    while (stillGrowing)
		{
		sleep(); 		// wait for the file to grow some

		long currentFileSize = file.length();
		if (lastFileSize == currentFileSize)
		    {
		    stillGrowing = false;
		    logMessage(Logger.DEBUG_D,"Done Writing: " + m_fileName + ", size: " + currentFileSize);
		}
		else
		    {
		    lastFileSize = currentFileSize;
		}
	    }
	}
	catch (Exception e)
	{
	    throw new IOException("Problem when waiting for file " + m_fileName + " to stop being written: " + e.getMessage());
	}
    }

    /**
    * Sleeps
    */
    private void sleep()
    {
	try {
	    Thread.sleep(m_sleepTime);
	}
	catch (Exception e)
	{
	}
    }
}

