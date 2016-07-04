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
package com.globalsight.cxe.util.aligner;

import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.aligner.AlignerExtractor;
import com.globalsight.everest.aligner.AlignerExtractorResult;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.page.pageimport.ExtractedFileImporter;
import com.globalsight.everest.request.CxeToCapRequest;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * Class {@code AlignerUtil} is used for handling aligner request without using
 * JMS.
 * 
 * @since GBS-4400
 */
public class AlignerUtil
{
    static private final Logger logger = Logger.getLogger(AlignerUtil.class);

    /**
     * Handles aligner request asynchronously with thread instead of JMS.
     */
    static public void handleAlignerWithThread(Map<String, Object> data)
    {
        AlignerRunnable runnable = new AlignerRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Handles aligner request synchronously.
     */
    static public void handleAligner(Map<String, Object> p_data)
    {
        try
        {
            String contentFileName = (String) p_data.get(CxeToCapRequest.CONTENT);
            String eventFlowXml = (String) p_data.get(CxeToCapRequest.EVENT_FLOW_XML);
            GeneralException exception = (GeneralException) p_data.get(CxeToCapRequest.EXCEPTION);

            String alignerExtractorName = (String) p_data.get("AlignerExtractor");
            String gxml = null;

            if (exception == null)
            {
                logger.info("Aligner received GXML in file: " + contentFileName);
            }
            else
            {
                logger.info("Aligner received an aligner-import failure."
                        + exception.getLocalizedMessage());
            }

            if (contentFileName != null)
            {
                gxml = ExtractedFileImporter.readXmlFromFile(contentFileName);
            }

            AlignerExtractorResult aeResult = new AlignerExtractorResult(gxml, eventFlowXml,
                    exception);
            AlignerExtractor ae = AlignerExtractor.getAlignerExtractor(alignerExtractorName);

            ae.addToResults(aeResult, (String) p_data.get("PageCount"),
                    (String) p_data.get("PageNumber"), (String) p_data.get("DocPageCount"),
                    (String) p_data.get("DocPageNumber"));
        }
        catch (Exception e)
        {
            logger.error("Failed to handle aligner request.", e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
