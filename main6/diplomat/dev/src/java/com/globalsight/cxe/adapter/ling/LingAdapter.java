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
package com.globalsight.cxe.adapter.ling;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.util.GeneralException;

/**
 * The LingAdapter class provides a means of interacting with the
 * LING API to do extraction and merging.
 */
public class LingAdapter extends BaseAdapter
{
    /**
     * Creates a LingAdapter
     *
     * @param p_loggingCategory the given logging category name (for
     * example "LingAdapter")
     *
     * @exception GeneralException
     */
    public LingAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
    }

    /*** Public Methods ***/

    /**
     * <P>Performs the main function of the adapter based on the
     * CxeMessage.  For the LingAdapter this is either extracting the
     * content to convert it to GXML, or merging the content to remove
     * GXML markup.
     *
     * This adapter handles events like XXX_IMPORTED_EVENT and
     * XXX_LOCALIZED_EVENT
     *
     * @param p_cxeMessage a CxeMessage object containing
     * EventFlowXml, content, and possibly parameters
     * @return AdapterResult[]
     * @exception GeneralException
     */
    public AdapterResult[] handleMessage (CxeMessage p_cxeMessage)
        throws GeneralException
    {
        switch (p_cxeMessage.getMessageType().getValue())
        {
            //add all extraction events here
        case CxeMessageType.HTML_IMPORTED_EVENT:
        case CxeMessageType.XML_IMPORTED_EVENT:
        case CxeMessageType.PRSXML_IMPORTED_EVENT:
        case CxeMessageType.RTF_IMPORTED_EVENT:
        case CxeMessageType.XPTAG_IMPORTED_EVENT:
        case CxeMessageType.MIF_IMPORTED_EVENT:
        case CxeMessageType.PASSOLO_IMPORTED_EVENT:
        case CxeMessageType.WINPE_IMPORTED_EVENT:
            return performExtraction(p_cxeMessage);

            //add all merging events here
        case CxeMessageType.HTML_LOCALIZED_EVENT:
        case CxeMessageType.XML_LOCALIZED_EVENT:
        case CxeMessageType.PRSXML_LOCALIZED_EVENT:
        case CxeMessageType.RTF_LOCALIZED_EVENT:
        case CxeMessageType.XPTAG_LOCALIZED_EVENT:
        case CxeMessageType.MIF_LOCALIZED_EVENT:
            return performMerging(p_cxeMessage);

        default:
            Exception e = new Exception ("Unhandled message type: " +
                p_cxeMessage.getMessageType().getName());

            String errorArgs[] = new String[1];
            errorArgs[0] = getLogger().getName();
            throw new LingAdapterException("Unexpected", errorArgs, e);
        }
    }

    /**
     * Converts the content to GXML and returns an AdapterResult.
     *
     * @param p_cxeMessage CxeMessage of type XXX_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] performExtraction(CxeMessage p_cxeMessage)
        throws GeneralException
    {
        AdapterResult[] results = null;
        try
        {
            StandardExtractor se =
                new StandardExtractor(getLogger(), p_cxeMessage);

            MessageData newMessageData = se.extract();
            CxeMessageType type = CxeMessageType.getCxeMessageType(
                CxeMessageType.GXML_CREATED_EVENT);

            results= makeSingleAdapterResult(type, newMessageData,
                p_cxeMessage.getParameters(), p_cxeMessage.getEventFlowObject());
        }
        catch (LingAdapterException lae)
        {
            results= makeImportError(p_cxeMessage,lae);
        }
        catch (Exception e)
        {
            getLogger().error("Unexpected problem extracting",e);
            String errorArgs[] = new String[1];
            errorArgs[0] = getLogger().getName();
            LingAdapterException lae = new LingAdapterException(
                "Unexpected", errorArgs, e);
            results = makeImportError(p_cxeMessage, lae);
        }

        return results;
    }


    /**
     * Handles the export case. Merges the content to remove the GXML tags.
     *
     * @param p_cxeMessage CxeMessage of type XXX_LOCALIZED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] performMerging(CxeMessage p_cxeMessage)
        throws GeneralException
    {
        AdapterResult[] results = null;
        try
        {
            StandardMerger sm = new StandardMerger(p_cxeMessage,
						   this.getConfig());
            MessageData newMessageData = sm.merge();
            CxeMessageType type = sm.getPostMergeEvent();
            results= makeSingleAdapterResult(type, newMessageData,
                p_cxeMessage.getParameters(), p_cxeMessage.getEventFlowObject().clone());
        }
        catch (LingAdapterException lae)
        {
            results= makeExportStatus(p_cxeMessage,lae);
        }
        catch (Exception e)
        {
            getLogger().error("Unexpected problem merging",e);
            String errorArgs[] = new String[1];
            errorArgs[0] = getLogger().getName();
            LingAdapterException lae= new LingAdapterException(
                "UnexpectedEx",errorArgs,e);
            results = makeExportStatus(p_cxeMessage,lae);
        }

        return results;
    }
}

