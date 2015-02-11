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

import com.globalsight.util.GeneralException;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.ling.common.Text;
import org.w3c.dom.Element;
import java.util.NoSuchElementException;

/**
 * The AlignerExtractorResult is a simple struct to
 * hold results from an aligner extraction.
 */
public class AlignerExtractorResult
{
    /**
     * The GXML (may be null if an error happens in which case
     * exception will not be null)
     */
    public String gxml = null;

    /**
     * The EventFlowXml (may be null if an error happens in which case
     * exception will not be null)
     */
    public String eventFlowXml = null;

    /**
     * Should be null for normal processing, but if an error
     * happens during extraction, this value will not be null.
     */
    public GeneralException exception = null;

    private EventFlowXmlParser m_eventFlowXmlParser = null;

    /**
     * Default constructor for AlignerExtractorResult.
     * All values are set to null.
     */
    public AlignerExtractorResult()
    {
    }


    /**
     * Creates an AlignerExtractorResult
     *
     * @param p_gxml GXML
     * @param p_eventFlowXml Event Flow XML
     * @param p_exception exception (may be null)
     */
    public AlignerExtractorResult(String p_gxml, String p_eventFlowXml,
        GeneralException p_exception)
    {
        gxml = p_gxml;
        eventFlowXml = p_eventFlowXml;
        exception = p_exception;
    }


    public String getDisplayName()
        throws Exception
    {
        parseEventFlowXmlIfNeeded();

        return m_eventFlowXmlParser.getDisplayName();
    }


    public String getFilePartName()
        throws Exception
    {
        parseEventFlowXmlIfNeeded();

        Element daElem = null;

        try
        {
            daElem = m_eventFlowXmlParser.getCategory(
                com.globalsight.cxe.adapter.msoffice
                .EventFlowXmlParser.EFXML_DA_CATEGORY_NAME);
        }
        catch (NoSuchElementException ex)
        {
            // not a Microsoft file
            return null;
        }

        String safeBaseName = m_eventFlowXmlParser.getCategoryDaValue(
            daElem, "safeBaseFileName")[0];

        // remove extension
        int idxExtension = safeBaseName.indexOf('.');
        if (idxExtension != -1)
        {
            safeBaseName = safeBaseName.substring(0, idxExtension);
        }

        // remove safe base name from rel safe name. The result is
        // something like: "\.html\header.html" for Word header part.
        String relSafeName = m_eventFlowXmlParser.getCategoryDaValue(
            daElem, "relSafeName")[0];

        return Text.replaceStringFirst(relSafeName, safeBaseName, "");
    }


    private void parseEventFlowXmlIfNeeded()
        throws Exception
    {
        if (m_eventFlowXmlParser == null)
        {
            m_eventFlowXmlParser = new EventFlowXmlParser();
            m_eventFlowXmlParser.parse(eventFlowXml);
        }
    }
}
