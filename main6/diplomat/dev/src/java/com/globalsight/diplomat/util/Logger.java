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
package com.globalsight.diplomat.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import com.globalsight.cxe.message.MessageData;
import com.globalsight.everest.util.system.AmbassadorServer;

/**
 * Logs out messages to the "CXE" category.
 * 
 * @deprecated This class used to be a separate logger when each adapter was run
 *             in a separate JVM. Do not use this class anymore. Use the
 *             org.apache.log4j.Logger objects to do logging
 */
public class Logger
{
    // debug levels
    public static final int ERROR = 0;
    public static final int WARNING = 1;
    public static final int INFO = 2;
    public static final int DEBUG_A = 3;
    public static final int DEBUG_B = 4;
    public static final int DEBUG_C = 5;
    public static final int DEBUG_D = 6;

    // debug level names
    public static final String ERROR_NAME = "ERROR";
    public static final String WARNING_NAME = "WARNING";
    public static final String INFO_NAME = "INFO";
    public static final String DEBUG_A_NAME = "DEBUG_A";
    public static final String DEBUG_B_NAME = "DEBUG_B";
    public static final String DEBUG_C_NAME = "DEBUG_C";
    public static final String DEBUG_D_NAME = "DEBUG_D";

    private static final org.apache.log4j.Logger CATEGORY = org.apache.log4j.Logger
            .getLogger(Logger.class);

    private static Logger theLogger = new Logger();
    private int m_debugLevel = INFO;
    private FileWriter m_filewriter = null;
    private PrintWriter m_printwriter = null;
    private String m_logname = null;
    private String m_logFileDirectory = null;
    private String m_debugFileDirectory = null;
    private boolean m_writeDebugFiles = false;
    private int m_maxMessages = 10000; // a value of 0 means infinite log length
    private int m_numMessages = 0;

    /** Gets the singleton logger object */
    public static Logger getLogger()
    {
        return theLogger;
    }

    /**
     * Creates a file with the given filename in the debug file directory <br>
     * 
     * @param filename
     *            The basename of the file. A fullpath will be created.
     * @param content
     *            The file content to write out.
     */
    public static void writeDebugFile(String p_filename, String p_content)
    {
        if (!theLogger.m_writeDebugFiles)
            return;

        try
        {
            String fullname = theLogger.m_debugFileDirectory + File.separator
                    + p_filename;
            theLogger.println(Logger.DEBUG_B, "Writing debug file "
                    + p_filename + " in UTF8.");
            FileOutputStream fos = new FileOutputStream(fullname);
            // write out the file as UTF-8
            OutputStreamWriter os = new OutputStreamWriter(fos, "UTF8");
            os.write(p_content, 0, p_content.length());
            os.flush();
            os.close();
        }
        catch (FileNotFoundException fnfe)
        {
            theLogger.println(Logger.ERROR, "Error writing debug file "
                    + p_filename + ": " + fnfe);
        }
        catch (IOException e)
        {
            theLogger.println(Logger.ERROR, "Error writing debug file "
                    + p_filename + ": " + e);
        }
        catch (Throwable t)
        {
            theLogger.println(Logger.ERROR, "Error writing debug file "
                    + p_filename + ": " + t);
        }
    }

    /**
     * Creates a file with the given filename in the debug file directory <br>
     * 
     * @param filename
     *            The basename of the file. A fullpath will be created.
     * @param content
     *            The message data containing the content
     */
    public static void writeDebugFile(String p_filename, MessageData p_content)
    {
        if (!theLogger.m_writeDebugFiles)
            return;
        try
        {
            String fullname = theLogger.m_debugFileDirectory + File.separator
                    + p_filename;
            File f = new File(fullname);
            p_content.copyTo(f);
        }
        catch (Throwable t)
        {
            Logger.getLogger().println(Logger.ERROR,
                    "Error writing debug file " + p_filename + ": " + t);
        }
    }

    // ///////////////////////////////////////////////
    /**
     * Creates a file with the given filename in $HOME/debug <br>
     * 
     * @param filename
     *            The basename of the file. A fullpath will be created.
     * @param content
     *            The file content to write out.
     */
    public static void writeDebugFile(String filename, byte[] content)
    {
        if (!theLogger.m_writeDebugFiles)
            return;

        try
        {
            String file = theLogger.m_debugFileDirectory + File.separator
                    + filename;
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content);
            fos.close();
        }
        catch (IOException e)
        {
            theLogger.println(Logger.ERROR, "Error writing debug file "
                    + filename + ": " + e);
        }
        catch (Throwable t)
        {
            theLogger.println(Logger.ERROR, "Error writing debug file "
                    + filename + ": " + t);
        }
    }

    // public methods

    /**
     * sets the log filename <logname>.log for the Logger. Creates the file.
     * 
     * @throws IOException
     *             if the file cannot be created. Does nothing if setLogname()
     *             has already been called for this instance of the Logger.
     */
    public void setLogname(String p_logname) throws IOException
    {
    }

    /** gets the Logger's log file name */
    public String getLogname()
    {
        return AmbassadorServer.SYSTEM_LOG_BASENAME;
    }

    /**
     * gets the current debug level. The debug level is set in the properties
     * file.
     */
    public int getDebugLevel()
    {
        return m_debugLevel;
    }

    /**
     * sets the Logger's debug level
     ** 
     * @deprecated do not use this class
     */
    public synchronized void setDebugLevel(int p_debugLevel)
    {
        m_debugLevel = p_debugLevel;
    }

    /**
     * Writes the string to the log file if p_messageDebugLevel is less than or
     * equal to the current debug level
     ** 
     * @deprecated do not use this class
     */
    public void println(int p_messageDebugLevel, String p_msg)
    {
        switch (p_messageDebugLevel)
        {
            case ERROR:
                CATEGORY.error(p_msg);
                break;
            case WARNING:
                CATEGORY.warn(p_msg);
                break;
            case INFO:
                CATEGORY.info(p_msg);
                break;
            default:
                CATEGORY.debug(p_msg);
                break;
        }
    }

    /**
     * Writes the exception's stack trace to the log file if p_messageDebugLevel
     * is less than or equal to the current debug level
     ** 
     * @deprecated do not use this class
     */
    public void printStackTrace(int p_messageDebugLevel, String p_msg,
            Throwable p_throwable)
    {
        println(p_messageDebugLevel, p_msg);
        p_throwable.printStackTrace();
    }

    /* Creates a new Logger */
    public Logger()
    {
        try
        {
            Properties props = new Properties();
            InputStream is = Logger.class
                    .getResourceAsStream("/properties/Logger.properties");
            props.load(is);
            // get the directories
            m_debugFileDirectory = props.getProperty("DebugFileDirectory");

            String wdf = props.getProperty("WriteDebugFiles");
            Boolean b = Boolean.valueOf(wdf);
            m_writeDebugFiles = b.booleanValue();

            // create any necessary directories
            File debugdir = new File(m_debugFileDirectory);
            debugdir.mkdirs();
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not initialize Logger "
                    + e.getMessage());
        }
    }

    /**
     * Returns true if the Logger's writeDebugFiles property is true
     * 
     * @return true | false
     */
    public boolean isWritingDebugFiles()
    {
        return m_writeDebugFiles;
    }
}
