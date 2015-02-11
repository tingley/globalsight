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
package com.globalsight.cxe.adapter.vignette;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.CxeProcessor;
import com.globalsight.cxe.adapter.vignette.VignetteImporter;
import com.globalsight.cxe.adapter.vignette.VignetteExporter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.GeneralException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import com.globalsight.vignette.VignetteConnection;

/**
 * The VignetteAdapter class provides a means of interacting with the Vignette
 * in order to do import/export (and possibly other) functions within CXE.
 */
public class VignetteAdapter extends BaseAdapter
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private static boolean s_isInstalled = false;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    /**
     * Creates a VignetteAdapter object
     * 
     * @param p_loggingCategory
     *            the given logging category name (for example
     *            "VignetteSourceAdapter" or "VignetteTargetAdapter")
     * 
     * @exception GeneralException
     */
    public VignetteAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
        if (!isInstalled())
            throw new GeneralException(
                    "This adapter is not installed properly.");
    }

    /** * Public Methods ** */

    /**
     * Returns true if the given adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    public static boolean isInstalled()
    {
        String installKey = "VGN-" + "GS".hashCode() + "-"
                + "vignette".hashCode();
        s_isInstalled = isInstalled(
                SystemConfigParamNames.VIGNETTE_INSTALL_KEY, installKey);

        return s_isInstalled;
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage. For
     * the VignetteAdapter this is either "reading" the file in from the
     * Vignette and creating the necessary EventFlowXml as a part of import, or
     * "writing" the file back to the file system for export.
     * </P>
     * <P>
     * The adapter switches on the CxeMessage message type and handles the
     * following events:<br>
     * <ol>
     * <li>Vignette_FILE_SELECTED_EVENT </li>
     * <li>Vignette_EXPORT_EVENT </li>
     * </ol>
     * </P>
     * 
     * @param p_cxeMessage
     *            a CxeMessage object containing EventFlowXml, content, and
     *            possibly parameters
     * @return AdapterResult[]
     * @exception GeneralException
     */
    public AdapterResult[] handleMessage(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        switch (p_cxeMessage.getMessageType().getValue())
        {
            case CxeMessageType.VIGNETTE_FILE_SELECTED_EVENT:
                return handleSelectedFileEvent(p_cxeMessage);

            case CxeMessageType.VIGNETTE_EXPORT_EVENT:
                return handleExportEvent(p_cxeMessage);
            default:
                Exception e = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                String[] errorArgs = new String[1];
                errorArgs[0] = getLogger().getName();
                throw new VignetteAdapterException("Unexpected", errorArgs, e);
        }
    }

    /**
     * Handles the import case where a user has selected a file and a "selected
     * file event" comes in to the file system adapter
     * 
     * @param p_cxeMessage
     *            CxeMessage of type VIGNETTE_FILE_SELECTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleSelectedFileEvent(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        AdapterResult[] results = null;
        String filename = null;
        try
        {
            VignetteConnection conn = new VignetteConnection();
            conn.connect();

            VignetteImporter importer = new VignetteImporter(p_cxeMessage,
                    getLogger(), conn);
            String newEventFlowXml = importer.makeEventFlowXml();
            MessageData newContent = importer.readContent();
            conn.disconnect();
            CxeMessage msg = new CxeMessage(importer.getPreExtractEvent());
            msg.setEventFlowXml(newEventFlowXml);
            msg.setMessageData(newContent);
            msg.setParameters(new HashMap());
            results = new AdapterResult[1];
            results[0] = new AdapterResult(msg);
            getLogger().info(
                    "Publishing " + msg.getMessageType().getName()
                            + " of size " + msg.getMessageData().getSize());
        }
        catch (VignetteAdapterException fsae)
        {
            results = makeImportError(p_cxeMessage, fsae);
        }
        catch (Exception e)
        {
            getLogger().error("Failed to import from Vignette.", e);
            getLogger().error("Vignette import error.", e);
            String[] errorArgs = new String[2];
            errorArgs[0] = this.getLogger().getName();
            errorArgs[1] = filename;
            VignetteAdapterException fsae = new VignetteAdapterException(
                    "UnexpectedIm", errorArgs, e);
            results = makeImportError(p_cxeMessage, fsae);
        }

        return results;
    }

    /**
     * Handles the export case. Writes the given content out.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type Vignette_EXPORT_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleExportEvent(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        AdapterResult[] results = null;
        try
        {
            if (isStfCreationExport(p_cxeMessage))
            {
                results = new AdapterResult[1];
                results[0] = new AdapterResult(makeStfCreationMsg(p_cxeMessage));
            }
            else
            {
                VignetteConnection conn = new VignetteConnection();
                conn.connect();
                VignetteExporter exporter = new VignetteExporter(p_cxeMessage,
                        getLogger(), conn);
                exporter.export();
                conn.disconnect();
                results = this.makeExportStatus(p_cxeMessage, null);
            }
        }
        catch (Exception e)
        {
            getLogger().error("Failed to export to Vignette.", e);
            String[] errorArgs = new String[1];
            errorArgs[0] = this.getLogger().getName();
            VignetteAdapterException fsae = new VignetteAdapterException(
                    "UnexpectedIm", errorArgs, e);
            results = makeExportStatus(p_cxeMessage, fsae);
        }
        return results;
    }
}
