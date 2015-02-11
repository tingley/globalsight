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
package com.globalsight.cxe.adapter.filesystem;

import java.util.HashMap;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.util.GeneralException;

/**
 * The FileSystemAdapter class provides a means of interacting
 * with the filesystem in order to do import/export
 * (and possibly other) functions within CXE.
 */
public class FileSystemAdapter extends BaseAdapter
{
    //////////////////////////////////////
    // Constructor                      //
    //////////////////////////////////////
    /**
     * Creates a FileSystemAdapter object
     * 
     * @param p_loggingCategory
     *               the given logging category name (for example "FileSystemSourceAdapter" or "FileSystemTargetAdapter")
     *               
     * @exception GeneralException
     */
    public FileSystemAdapter(String p_loggingCategory) throws GeneralException
    { 
        super(p_loggingCategory);
    }

    /*** Public Methods ***/
    
    /**
     * <P>Performs the main function of the adapter based
     * on the CxeMessage. For the FileSystemAdapter this is
     * either "reading" the file in from the filesystem and
     * creating the necessary EventFlowXml as a part of import,
     * or "writing" the file back to the file system for export.
     *</P><P>
     * The adapter switches on the CxeMessage message type and
     * handles the following events:<br><ol>
     * <li>FILE_SYSTEM_FILE_SELECTED_EVENT </li>
     * <li>FILE_SYSTEM_EXPORT_EVENT </li>
     * </ol></P>
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
        case CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT:
            return handleSelectedFileEvent(p_cxeMessage);
        case CxeMessageType.FILE_SYSTEM_EXPORT_EVENT:
            return handleExportEvent(p_cxeMessage);
        default:
            Exception e = new Exception ("Unhandled message type: " + p_cxeMessage.getMessageType().getName());
            throw new FileSystemAdapterException("Unexpected", getLogger().getName(), e);
        }
    }

    /**
     * Handles the import case where a user has selected
     * a file and a "selected file event" comes in to the
     * file system adapter
     * 
     * @param p_cxeMessage
     *               CxeMessage of type FILE_SYSTEM_FILE_SELECTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleSelectedFileEvent(CxeMessage p_cxeMessage)
    throws GeneralException
    {
        AdapterResult[] results = null;
        String filename = null;
        try {
            HashMap params = p_cxeMessage.getParameters();
            
            filename = (String) params.get("Filename");
            boolean isAutomaticImport = ((Boolean)params.get("IsAutomaticImport")).booleanValue();
            Importer importer = new Importer(p_cxeMessage,this.getLogger());
            String newEventFlowXml = importer
                    .makeEventFlowXml(isAutomaticImport);
            p_cxeMessage.setEventFlowXml(newEventFlowXml);
            HashMap newParams = params;
            MessageData newMessageData = importer.readFile();
            CxeMessageType newMessageType = importer.getPreExtractEvent();
            BaseAdapter.preserveOriginalFileContent(newMessageData,newParams);
            
            p_cxeMessage.setMessageData(newMessageData);
            //If the script on import return value 1, GlobalSight will report an error on import.
            Object scriptOnImport = params.get("ScriptOnImport");
            if (scriptOnImport != null && ((Integer) scriptOnImport).intValue() == 1)
            {
            	String[] errorArgs = new String[2];
                errorArgs[0] = this.getLogger().getName();
                errorArgs[1] = filename;
                FileSystemAdapterException fsae = new FileSystemAdapterException(
                		"ScriptOnImport", errorArgs, new Exception());
                results= makeImportError(p_cxeMessage, fsae);
                
                return results;
            }
            results= makeSingleAdapterResult(newMessageType, newMessageData, newParams, newEventFlowXml);
        }
        catch (FileSystemAdapterException fsae)
        {
            results= makeImportError(p_cxeMessage, fsae);
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = this.getLogger().getName();
            errorArgs[1] = filename;
            FileSystemAdapterException fsae = new  FileSystemAdapterException("UnexpectedIm",errorArgs,e);
            results= makeImportError(p_cxeMessage, fsae);
        }
        
        return results;
    }

    /**
     * Handles the export case. Writes the given content out.
     * 
     * @param p_cxeMessage
     *               CxeMessage of type FILE_SYSTEM_EXPORT_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleExportEvent(CxeMessage p_cxeMessage)
    throws GeneralException
    {
        AdapterResult[] results = new AdapterResult[1];
        CxeMessage returnMessage = null;
        try {
            if (isStfCreationExport(p_cxeMessage))
            {
                returnMessage = makeStfCreationMsg(p_cxeMessage);
            }
            else
            {
                Exporter exporter = new Exporter(p_cxeMessage,this.getLogger());
                returnMessage = exporter.export();
            }
            
            results[0] = new AdapterResult(returnMessage);

        }
        catch (FileSystemAdapterException fsae)
        {
            results= makeExportStatus(p_cxeMessage,fsae);
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[1];
            errorArgs[0] = this.getLogger().getName();
            FileSystemAdapterException fsae = new  FileSystemAdapterException("UnexpectedIm",errorArgs,e);
            results= makeExportStatus(p_cxeMessage, fsae);
        }
        return results;
    }
}

