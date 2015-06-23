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
package com.globalsight.cxe.adapter.cap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.util.GeneralException;
/**
 * The CapAdapter provides functionality for receiving messages
 * from CAP and processing them, as well as preparing messages
 * for sending to CAP.
 */
public class CapAdapter extends BaseAdapter
{
    /**
     * Creates a CapAdapter
     * 
     * @param p_loggingCategory
     *               the given logging category name (for example "CapAdapter")
     *               
     * @exception GeneralException
     */
    public CapAdapter(String p_loggingCategory) throws GeneralException
    { 
        super(p_loggingCategory);
    }

    /*** Public Methods ***/
    
    /**
     * <P>Performs the main function of the adapter based
     * on the CxeMessage. This involves receiving messages from CAP
     * for export, secondary target file creation, etc. And it involves
     * sending messages to CAP for import.
     *
     * This adapter handles events like GXML_CREATED_EVENT, GXML_LOCALIZED_EVENT,etc.
     *
     * @param p_cxeMessage
     *               a CxeMessage object containing EventFlowXml, content,
     *               and possibly parameters
     * @return AdapterResult[]
     * @exception GeneralException
     */
    public AdapterResult[] handleMessage (CxeMessage p_cxeMessage)
    throws GeneralException
    {
        switch (p_cxeMessage.getMessageType().getValue())
        {
            //add all extraction events here
        case CxeMessageType.GXML_CREATED_EVENT:
            return performImport(p_cxeMessage);
        
	case CxeMessageType.UNEXTRACTED_IMPORTED_EVENT:
            return performImport(p_cxeMessage);

            //add all merging events here
        case CxeMessageType.GXML_LOCALIZED_EVENT:
            return performExport(p_cxeMessage, Mapper.PRE_MERGE_EVENT);

	case CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT:
            return performExport(p_cxeMessage, Mapper.POST_MERGE_EVENT);

            //add all errors here
        case CxeMessageType.CXE_IMPORT_ERROR_EVENT:
            return performImport(p_cxeMessage);

        case CxeMessageType.CXE_EXPORT_STATUS_EVENT:
            return performExportStatusHandling(p_cxeMessage);

            //stf creation is special
        case CxeMessageType.STF_CREATION_EVENT:
            return performStfCreationNotification(p_cxeMessage);

        default:
            Exception e = new Exception ("Unhandled message type: " + p_cxeMessage.getMessageType().getName());
            String errorArgs[] = new String[1];
            errorArgs[0] = getLogger().getName();
            throw new CapAdapterException("Unexpected", errorArgs, e);
        }
    }
    
    /**
     * <P>Performs the main function of the adapter based
     * on the CxeMessage. This involves receiving messages from CAP
     * for export, secondary target file creation, etc. And it involves
     * sending messages to CAP for import.
     *
     * This adapter handles events like GXML_CREATED_EVENT, GXML_LOCALIZED_EVENT,etc.
     *
     * @param p_cxeMessage
     *               a CxeMessage object containing EventFlowXml, content,
     *               and possibly parameters
     * @return AdapterResult[]
     * @exception GeneralException
     */
    public AdapterResult[] handleMessage (AdapterResult ars) throws Exception
    {
    	List<CxeMessage> msgs = ars.getMsgs();
    	ArrayList<HashMap> hms = new ArrayList<HashMap>();
    	for (CxeMessage msg : msgs)
    	{
    		int requestType = Request.EXTRACTED_LOCALIZATION_REQUEST;
    		 CapImporter capImporter = new CapImporter(msg, getLogger(), requestType);
    		 HashMap hm = capImporter.getContent();
    		 hms.add(hm);
    	}
    	
    	if (hms.size() > 0)
    	{
    		JmsHelper.sendMessageToQueue((Serializable) hms,
                    JmsHelper.JMS_IMPORTING_QUEUE);
    	}
    	
    	return null;
    }

    /**
     * Prepares the content for sending to CAP, and then sends it.
     * This involves creating the l10nxml as well.
     * This method will return null as there is no result from this
     * operation.
     * 
     * @param p_cxeMessage
     *               CxeMessage of type XXX_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] performImport(CxeMessage p_cxeMessage)
    throws GeneralException
    {
        try {
            int requestType = Request.EXTRACTED_LOCALIZATION_REQUEST;
            if (p_cxeMessage.getMessageType().getValue() != CxeMessageType.GXML_CREATED_EVENT)
                requestType = Request.UNEXTRACTED_LOCALIZATION_REQUEST;
            if (p_cxeMessage.getParameters().get("Exception") != null)
                requestType = Request.REQUEST_WITH_CXE_ERROR;
            CapImporter capImporter = new CapImporter(p_cxeMessage, getLogger(), requestType);
            capImporter.sendContent();
        }
        catch (Exception e)
        {
            getLogger().error("Could not send content to CAP!",e);
        }
        return null; //no result from this adapter
    }

    /**
     * Handles the export case. If the content is extracted, then the next
     * event is the "pre-merge" event so the content should get sent to the
     * merger. If the content is unextracted, then the next event will
     * be the "post-merge" event to send the content directly to the target
     * adapter
     *
     *NOTE: This is the only case that may return an export error CxeMessage
     *object because it was not in the act of sending something to CAP. The other
     *methods send something to CAP, so returning an error message that could not
     *be sent to CAP wouldn't help.
     * 
     * @param p_cxeMessage
     *               CxeMessage of type XXX_LOCALIZED_EVENT
     * @param p_eventType Mapper.PRE_MERGE_EVENT or Mapper.POST_MERGE_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] performExport(CxeMessage p_cxeMessage, int p_eventType)
    throws GeneralException
    {
        AdapterResult results[] = null;
        try {
            Mapper mapper = new Mapper(p_cxeMessage,getLogger());
            CxeMessage msg = mapper.map(p_eventType);
            results = makeSingleAdapterResult(msg);
        }
        catch (CapAdapterException cae)
        {
            results= makeExportStatus(p_cxeMessage,cae);
        }
        catch (Exception e)
        {
            getLogger().error("Could not initiate export.",e);
            String[] errorArgs = new String[1];
            errorArgs[0] = this.getLogger().getName();
            CapAdapterException cae = new  CapAdapterException("UnexpectedIm",errorArgs,e);
            results= makeExportStatus(p_cxeMessage,cae);
        }
        return results;
    }

    /**
     * Handles notifying CAP of export status (errors and successes)
     * This also handles the case of Secondary Target File Creation
     * 
     * @param p_cxeMessage
     *               CxeMessage of type CXE_EXPORT_STATUS_EVENT
     * @return AdapterResult[] -- (null or contains results if for dynamic preview)
     * @exception GeneralException
     */
    private AdapterResult[] performExportStatusHandling(CxeMessage p_cxeMessage)
    throws GeneralException
    {
        AdapterResult[] results = null;
        try {
            Uploader uploader = new Uploader(p_cxeMessage, getLogger());
            String requestType = (String) p_cxeMessage.getParameters().get("CxeRequestType");
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("CXE request type is: " + requestType);                
            }
            if (ExportConstants.PREVIEW.equals(requestType))
                uploader.uploadPreviewStatus();
            else
                uploader.uploadExportStatus();
        }
        catch (Exception e)
        {
            getLogger().error("Could not send content to CAP!",e);
        }
        
        return results;
    }

    /**
     * Calls the appropriate methods in CAP for
     * Secondary Target File Creation. Also notifies of successful export completion.
     * 
     * @param p_cxeMessage
     *               incoming CXE message that an STF file has been created
     * @return null -- no output event from this adapter
     * @exception GeneralException
     */
    private AdapterResult[] performStfCreationNotification(CxeMessage p_cxeMessage)
    throws GeneralException
    {
        try {
            Uploader uploader = new Uploader(p_cxeMessage, getLogger());
            uploader.performStfCreationNotification();
            uploader.uploadExportStatus();
        }
        catch (Exception e)
        {
            getLogger().error("Could not perform STF Creation Notification.",e);
        }
        return null; //no result from this adapter
    }
}
