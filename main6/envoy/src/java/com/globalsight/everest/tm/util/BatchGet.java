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

package com.globalsight.everest.tm.util;

import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmImpl;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tm.TmManagerLocal;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;

import java.util.*;

/**
 *
 */
public class BatchGet
{
    private String m_tmName;
    private String m_tuid;

    private Tm m_tm = null;

    private long m_start;
    private long m_stop;

    //
    // Main Routine
    //
    static public void main(String[] args)
    {
        new BatchGet().run(args);
    }

    //
    // Constructor
    //
    public BatchGet()
    {
    }

    //
    // Public Methods
    //
    public void run(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println(
                "Usage: BatchGet <TM-NAME> <TU-ID>\n" +
                "\tPrints a TU.");
            return;
        }

        m_tmName = args[0];
        m_tuid   = args[1];

        try
        {
            m_start = System.currentTimeMillis();

            String tu = TmManagerLocal.getProjectTmTu(
                m_tmName, Long.parseLong(m_tuid));

            System.out.println(tu);

            m_stop = System.currentTimeMillis();

            long duration = m_stop - m_start;
            if (duration < 1000)
            {
                // millisecond range
                System.err.println(duration + " ms.");
            }
            else if (duration < 60*1000)
            {
                // second range less than a minute
                System.err.println(
                    (duration/1000) + ":" + (duration % 1000) + " sec.");
            }
            else
            {
                // minute range
                System.err.println(
                    (duration/60000) + "." +
                    ((duration-duration/60000)/1000) + ":" +
                    (duration % 1000) + " min.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            m_tm = null;
        }
    }
}
