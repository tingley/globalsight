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

package galign.helpers.util;

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;

/**
 * A ProcessRunner executes a given command and handles the standard
 * output and standard error.
 */
public class ProcessRunner
    implements Runnable
{
    private static final Logger s_logger = Logger.getLogger(
        ProcessRunner.class.getName());

    private String m_command;
    private PrintStream m_out;
    private PrintStream m_err;

    /**
     * Constructs a Process Runner with the given command standard out
     * and standard error are not written out.
     */
    public ProcessRunner (String p_command)
    {
        m_command = p_command;
        m_out = null;
        m_err = null;
    }

    /**
     * Constructs a Process Runner with the given command standard out
     * and standard error are written out to the output streams.
     */
    public ProcessRunner (String p_command, PrintStream p_out,
        PrintStream p_err)
    {
        m_command = p_command;
        m_out = p_out;
        m_err = p_err;
    }

    /**
     * Constructs a Process Runner with the given command standard out
     * and standard error are written out to the files.
     */
    public ProcessRunner (String p_command, String p_outFileName,
        String p_errFileName, boolean p_append)
        throws FileNotFoundException
    {
        m_command = p_command;

        if (p_outFileName != null)
        {
            m_out = new PrintStream (
                new FileOutputStream(p_outFileName, p_append), true);
        }
        else
        {
            m_out = null;
        }

        if (p_errFileName != null)
        {
            m_err = new PrintStream (
                new FileOutputStream(p_errFileName,p_append), true);
        }
        else
        {
            m_err = null;
        }
    }


    public void run()
    {
        try
        {
            String threadName = Thread.currentThread().getName();

            Process p = Runtime.getRuntime().exec(m_command);

            ProcessInputStreamHandler outputHandler =
                new ProcessInputStreamHandler (p.getInputStream(), m_out);
            ProcessInputStreamHandler errorHandler =
                new ProcessInputStreamHandler (p.getErrorStream(), m_err);

            Thread outputThread = new Thread (outputHandler, threadName + "_out");
            Thread errorThread = new Thread (errorHandler, threadName + "_err");

            //start separate threads to handle output and error
            outputThread.start();
            errorThread.start();

            //wait for the process to die
            try
            {
                p.waitFor();
            }
            catch (InterruptedException ie)
            {}

            if (true)
            {
                //wait for the output thread to die
                try
                {
                    outputThread.join();
                }
                catch (InterruptedException ie)
                {}

                //wait for the error thread to die
                try
                {
                    errorThread.join();
                }
                catch (InterruptedException ie)
                {}
            }
        }
        catch (Throwable t)
        {
            s_logger.log(Level.SEVERE,
                "Problem running command '" + m_command + "'", t);
        }
    }
}

