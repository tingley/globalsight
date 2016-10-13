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

package com.globalsight.util.progress;

import java.io.IOException;

/**
 * ProcessStatus2 is a generic object for tracking progress of a time
 * consuming background process.
 *
 * <p>This listener provides 2 progress bars: one for top-level
 * objects (file to be processed, indexes to be created), and one for
 * progress within one of the objects (a single file, a single
 * index).</p>
 *
 * The object holds the data for the top-level object's progress bar:
 * description and completion percentage. Error and warning messages,
 * result object, data for the 1st progress bar, and the method to
 * interrupt the background process are inherited.
 *
 * @see ProcessStatus
 */
public class ProcessStatus2
    extends ProcessStatus
    implements IProcessStatusListener,
               IProcessStatusListener2
{
    private boolean m_done = false;
    // Total number of embedded records processed.
    private String m_description2 = "";
    // Embedded percentage complete (0-100)
    private int m_percentage2 = 0;

    //
    // Constructor
    //

    /** Initializes the ProcessStatus object. */
    public ProcessStatus2()
    {
        super();
    }

    //
    // Public Methods
    //

    public void setDone()
    {
        m_done = true;
    }

    public boolean getDone()
    {
        return m_done;
    }

    /**
     * Method for setting the description of the current object being
     * processed (entries, files, etc...).
     */
    public void setDescription2(String p_arg)
    {
        m_description2 = p_arg;
    }

    /** Method for getting description value. */
    public String getDescription2()
    {
        return m_description2;
    }

    /** Method for setting percentage complete information. */
    public void setPercentage2(int p_arg)
    {
        m_percentage2 = p_arg;
    }

    /** Method for getting percentage complete information. */
    public int getPercentage2()
    {
        return m_percentage2;
    }

    /**
     * For the background process: updates the second progress bar
     * with a new "percentage complete" and an optional status
     * message.
     *
     * @param p_message a message string or null for no message.
     *
     * @throws IOException when the background process should be
     * interrupted.
     */
    public void listen2(String p_desc, int p_percentage, String p_message)
        throws IOException
    {
        m_description2 = p_desc;
        m_percentage2 = p_percentage;

        addMessage(p_message);

        if (m_interruped == true)
        {
            // stop backend processing.
            throw new ClientInterruptException();
        }
    }
}
