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
package com.globalsight.cxe.adapter.catalyst;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.CxeProcessor;
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
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.cxe.adapter.catalyst.CatalystHelper;

/**
 * The Catalyst adapter interacts with the Alchemy Catalyst command line
 * interface
 */
public class CatalystAdapter extends BaseAdapter
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private static boolean s_isInstalled = false;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    /**
     * Creates a CatalystAdapter object
     * 
     * @param p_loggingCategory
     *            the given logging category name (for example
     *            "FileSystemSourceAdapter" or "FileSystemTargetAdapter")
     * 
     * @exception GeneralException
     */
    public CatalystAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
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
        String installKey = "CAT-" + "GS".hashCode() + "-"
                + "Alchemy".hashCode();
        s_isInstalled = isInstalled(
                SystemConfigParamNames.CATALYST_INSTALL_KEY, installKey);

        return s_isInstalled;
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage. This
     * adapter handles events like CATALYST_IMPORTED_EVENT and
     * CATALYST_LOCALIZED_EVENT
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
            case CxeMessageType.CATALYST_IMPORTED_EVENT:
                return compileTranslatorToolKit(p_cxeMessage);
            case CxeMessageType.CATALYST_LOCALIZED_EVENT:
                return decompileTranslatorToolKit(p_cxeMessage);
            default:
                Exception e = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                String errorArgs[] = new String[1];
                errorArgs[0] = getLogger().getName();
                throw new CatalystAdapterException("Unexpected", errorArgs, e);
        }
    }

    /**
     * Constructs or appends to a Translator's Tool Kit (TTK) file, and
     * ultimately returns a single AdapterResult corresponding to that TTK. If
     * the TTK is not ready, then no adapter result is returned.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type PDF_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] compileTranslatorToolKit(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            if (!isInstalled())
                throw new Exception("Catalyst integration is not installed.");

            CatalystHelper helper = new CatalystHelper(p_cxeMessage,
                    getLogger(), this.getConfig());
            MessageData newContent = helper.createTTKs();
            if (newContent == null) return null;

            String newEventFlowXml = helper.getEventFlowXml();
            CxeMessageType type = CxeMessageType
                    .getCxeMessageType(CxeMessageType.UNEXTRACTED_IMPORTED_EVENT);
            getLogger().info("outgoing msg..." + type.getName());
            CxeMessage outgoingMsg = new CxeMessage(type);
            outgoingMsg.setParameters(p_cxeMessage.getParameters());
            outgoingMsg.setEventFlowXml(newEventFlowXml);
            outgoingMsg.setMessageData(newContent);
            AdapterResult results[] = new AdapterResult[1];
            results[0] = new AdapterResult(outgoingMsg);
            return results;
        }
        catch (CatalystAdapterException msoe)
        {
            return this.makeImportError(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling Catalyst Conversion.", e);
            String errorArgs[] = null;
            CatalystAdapterException pae = new CatalystAdapterException(
                    "Unexpected", errorArgs, e);
            return this.makeImportError(p_cxeMessage, pae);
        }
    }

    /**
     * Handles the export case. Leaves in Word format.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type PDF_LOCALIZED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] decompileTranslatorToolKit(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            if (!isInstalled())
                throw new Exception("Catalyst integration is not installed.");

            CatalystHelper helper = new CatalystHelper(p_cxeMessage,
                    getLogger(), this.getConfig());
            return helper.extractTTK();
        }
        catch (CatalystAdapterException msoe)
        {
            return this.makeExportStatus(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling MS Office Re-Conversion.", e);
            String errorArgs[] = null;
            CatalystAdapterException pae = new CatalystAdapterException(
                    "Unexpected", errorArgs, e);
            return this.makeExportStatus(p_cxeMessage, pae);
        }
    }
}
