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
package com.globalsight.util;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.log4j.Logger;

import com.globalsight.log.ActivityLog;

/**
 * A ProcessInputStreamHandler runs in a separate thread to read
 * output (blocking) from an InputStream and write it to a
 * PrintStream.
 */
public class ProcessInputStreamHandler
    implements Runnable
{
    private static final Logger s_logger =
        Logger.getLogger(
            ProcessInputStreamHandler.class);

    private InputStream m_inputstream = null;
    private PrintStream m_out = null;
    private boolean m_writeOutput = true;

    public ProcessInputStreamHandler (InputStream p_inputstream,
        PrintStream p_out)
    {
        m_inputstream = p_inputstream;
        m_out = p_out;

        if (m_out == null)
        {
            m_writeOutput = false;
        }
    }

    public void run()
    {
        boolean keepReadingOutput = true;
        String outputLine = null;

        //run the command
        ActivityLog.Start activityStart = ActivityLog.start(
            ProcessInputStreamHandler.class, "run");
        try
        {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(m_inputstream));

            while (keepReadingOutput)
            {
                outputLine = in.readLine();

                if (outputLine == null)
                {
                    keepReadingOutput = false;
                }
                else
                {
                    if (m_writeOutput)
                    {
                        m_out.println(outputLine);
                    }
                }
            }
            in.close();

            if (m_writeOutput)
            {
                m_out.flush();
            }
        }
        catch (Throwable t)
        {
            s_logger.error("Problem processing input stream.",t);
        }
        finally
        {
            activityStart.end();
        }
    }
}

