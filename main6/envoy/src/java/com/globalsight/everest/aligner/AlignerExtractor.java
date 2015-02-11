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
package com.globalsight.everest.aligner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.xmlrulefile.XmlRuleFile;
import com.globalsight.cxe.util.CxeProxy;

/**
 * The AlignerExtractor provides methods to do the import for Aligner
 * for aligner import requests.
 */
public class AlignerExtractor
    implements Serializable
{
    private static final long serialVersionUID = 2726147319103398617L;

    static private Logger s_logger =
        Logger.getLogger(
            AlignerExtractor.class);

    static private final Integer ONE = new Integer(1);

    private boolean m_keepLooping = true;
    // Aligner object name
    private String m_name = null;

    // List of aligner results for one file
    private AlignerExtractorResult[] m_results = null;
    private Integer m_docPageCount = null;
    private ArrayList m_docPageNums = new ArrayList();
    private String m_filename = null;
    private KnownFormatType m_knownFormatType=null;
    private String m_locale = null;
    private String m_encoding = null;
    private XmlRuleFile m_xmlRule = null;

    //this map holds all AlignerExtractor objects that are currently looping
    private static HashMap s_currentAligners = new HashMap();

    //
    // Constructor
    //

    /**
     * The constructor is private. The public usage of this class is static.
     */
    private AlignerExtractor()
    {
        // Make a unique name by concatenating thread id and time.
        m_name = Thread.currentThread().getName() + "_" +
            System.currentTimeMillis();
    }

    //
    // Public Methods
    //

    /**
     * This should be called to aligner-import (extract) exactly one file.
     * This method will return when the extraction is done.
     * Even though the processing through CXE happens asynchronously, this
     * method will wait until processing is done.
     *
     * @param p_timeoutInMinutes amount of time to wait for an
     * alignment to happen before giving up. An exception gets thrown
     * in that case.
     * @param p_filename path name of file (relative to docs directory
     * for now)
     * @param p_format   format
     * @param p_locale   locale
     * @param p_encoding IANA encoding
     * @param p_xmlRule  xml rule file (if applicable, may be null)
     * @return a List of AlignerExtractorResult
     */
    static public List extract(int p_timeoutInMinutes,
        String p_filename, KnownFormatType p_format, String p_locale,
        String p_encoding, XmlRuleFile p_xmlRule)
        throws Exception
    {
        AlignerExtractor ae = new AlignerExtractor();

        ae.m_filename = p_filename;
        ae.m_knownFormatType = p_format;
        ae.m_locale = p_locale;
        ae.m_encoding = p_encoding;
        ae.m_xmlRule = p_xmlRule;

        long stopTime = (long)p_timeoutInMinutes * 60 * 1000;
        stopTime += System.currentTimeMillis();

        s_currentAligners.put(ae.m_name, ae);

        return ae.doLoop(stopTime);
    }

    static public AlignerExtractor getAlignerExtractor(String p_name)
    {
        return (AlignerExtractor)s_currentAligners.get(p_name);
    }

    private ArrayList doLoop(long p_stopTime)
        throws Exception
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Starting aligner extraction of file " + m_filename);
        }

        // Start the asynchronous extraction.
        CxeProxy.importFromFileSystemForAligner(this);

        while (keepLooping())
        {
            // wait for the processing to finish
            try
            {
                Thread.sleep(1000L);
            }
            catch (InterruptedException e)
            {}

            // check the timeout
            if (System.currentTimeMillis() > p_stopTime)
            {
                s_logger.error("TIMEOUT for Aligner extraction of file " +
                    m_filename);

                throw new Exception("Timeout");
            }
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Extraction for file " + m_filename + " complete.");
        }

        ArrayList results = new ArrayList();
        if (m_results != null)
        {
            for (int i = 0; i < m_results.length; i++)
            {
                results.add(m_results[i]);
            }
        }

        return results;
    }

    public void addToResults(AlignerExtractorResult p_result,
        String p_pageCount, String p_pageNum,
        String p_docPageCount, String p_docPageNum)
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("New results to add to AlignerExtractor." +
                " PageCount=" + p_pageCount + " PageNumber=" + p_pageNum +
                " DocPageCount=" + p_docPageCount + " DocPageNumber=" +
                p_docPageNum);
        }

        // Now check the page count to see if we're done with this batch.
        if (m_docPageCount == null)
        {
            m_docPageCount = new Integer(Integer.parseInt(p_docPageCount));
        }

        Integer docPageNum = new Integer(Integer.parseInt(p_docPageNum));
        m_docPageNums.add(docPageNum);

        // Add the result to the array of results
        if (m_results == null)
        {
            m_results = new AlignerExtractorResult[m_docPageCount.intValue()];
        }
        m_results[docPageNum.intValue() - 1] = p_result;

        if (m_docPageNums.size() == m_docPageCount.intValue())
        {
            stopLooping();

            s_currentAligners.remove(m_name);

            s_logger.debug("Finished waiting for file to extract" +
                " (alignerName=" + getName() + ").");
        }
    }

    public String getFilename()
    {
        return m_filename;
    }

    public KnownFormatType getFormat()
    {
        return m_knownFormatType;
    }

    public String getLocale()
    {
        return m_locale;
    }

    public String getEncoding()
    {
        return m_encoding;
    }

    public XmlRuleFile getXmlRule()
    {
        return m_xmlRule;
    }

    public String getName()
    {
        return m_name;
    }

    private synchronized boolean keepLooping()
    {
        return m_keepLooping;
    }

    private synchronized void stopLooping()
    {
        m_keepLooping = false;
    }
}

