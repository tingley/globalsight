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

package com.globalsight.terminology;

import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.util.*;

/**
 * This class reads multiple concept entries from a file and adds them
 * to a termbase.
 */
public class BatchLoad
    implements ElementHandler
{
    private String m_pruningPath = "/entries/conceptGrp";
    private String m_userName;
    private String m_password;
    private String m_termbaseName;

    private ITermbase m_termbase = null;

    private int m_countTotal = 0;
    private int m_countErrors = 0;
    private ArrayList m_errorIds = new ArrayList();

    private long m_start;
    private long m_stop;

    //
    // Main Routine
    //
    static public void main(String[] args)
    {
        new BatchLoad().run(args);
    }

    //
    // Constructor
    //
    public BatchLoad()
    {
    }

    //
    // Public Methods
    //
    public void run(String[] args)
    {
        if (args.length < 4)
        {
            System.err.println(
                "Usage: BatchLoad <TERMBASE> <USER> <PASSWORD> <XML-FILE>");
            return;
        }

        m_termbaseName = args[0];
        m_userName = args[1];
        m_password = args[2];
        String xmlFile = args[3];

        try
        {
            System.out.print("Connecting to termbase " + m_termbaseName +
                " as user " + m_userName);

            connectToTermbase();

            System.out.println("... succeeded.");
        }
        catch (Exception e)
        {
            System.err.println("\nError: " + e);
            System.err.println("Aborted.");
            return;
        }

        try
        {
            m_start = System.currentTimeMillis();

            Document document = parse(xmlFile);

            m_stop = System.currentTimeMillis();
            long duration = m_stop - m_start;
            long durationPerEntry = duration / m_countTotal;

            System.err.println("Imported " + m_countTotal + " entries, " +
                m_countErrors + " had errors.");

            System.err.println("Duration: " + getTimeString(duration) +
                ", " + getTimeString(durationPerEntry) + " per entry.");

            if (m_countErrors > 0)
            {
                System.err.println("");
                System.err.println("Error IDs: " + m_errorIds);
            }
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
        finally
        {
            m_termbase = null;
        }
    }

    public void connectToTermbase()
        throws Exception
    {
        ITermbaseManager m_manager = ServerProxy.getTermbaseManager();
        m_termbase = m_manager.connect(m_termbaseName, m_userName, m_password);
    }


    //
    // ElementHandler interface
    //
    public void onStart(ElementPath path)
    {
        Element element = path.getCurrent();

        ++m_countTotal;
        if (m_countTotal % 10 == 0)
        {
            System.out.println("Entry " + m_countTotal);
        }
    }

    public void onEnd(ElementPath path)
    {
        Element element = path.getCurrent();

        try
        {
            // Save to termbase
            m_termbase.addEntry(element.asXML());
        }
        catch (Exception e)
        {
            System.out.println("Entry " + m_countTotal + ": " +
                e.getMessage());
            e.printStackTrace();

            m_countErrors++;
            m_errorIds.add(new Integer(m_countTotal));
        }

        // now prune the current element to reduce memory
        element.detach();
    }

    protected Document parse(String url)
        throws Exception
    {
        SAXReader reader = new SAXReader();

        System.out.println("Parsing document:   " + url);

        // enable pruning to call me back as each Element is complete
        reader.addHandler(m_pruningPath, this);

        Document document = reader.read(url);

        // the document will be complete but have the prunePath
        // elements pruned

        return document;
    }

    static public String getTimeString(long duration)
    {
        if (duration < 1000L)
        {
            // millisecond range
            return String.valueOf(duration) + "ms";
        }
        else if (duration < 60L*1000L)
        {
            // second range less than a minute
            return String.valueOf(duration/1000L) + "s " +
                String.valueOf(duration % 1000L) + "ms";
        }
        else
        {
            // minute range, don't show ms.
            long seconds, minutes, hours;

            seconds = duration / 1000;
            hours = seconds / 3600;
            seconds = seconds % 3600;
            minutes = seconds / 60;
            seconds = seconds % 60;

            return String.valueOf(hours) + "h " +
                String.valueOf(minutes) + "m " +
                String.valueOf(seconds) + "s";
        }
    }
}
