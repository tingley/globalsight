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
package com.globalsight.cxe.adapter.copyflow;

import com.globalsight.cxe.adapter.copyflow.DesktopAppHelper;

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

import java.io.FileInputStream;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;

/**
 * The CopyFlowAdapter handles conversions of Quark to XPTags using the separate
 * CopyFlow Converter.
 */
public class CopyFlowAdapter extends BaseAdapter
{
    //
    // Private Members
    //
    private static boolean s_isCopyFlowInstalled = false;

    //
    // Constructor
    //

    /**
     * Creates an CopyFlowAdapter
     * 
     * @param p_loggingCategory
     *            the given logging category name (for example
     *            "MsOfficeAdapter")
     * 
     * @exception GeneralException
     */
    public CopyFlowAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
        isCopyFlowInstalled();
    }

    //
    // Public Methods
    //

    /**
     * Returns true if the given adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    public static boolean isCopyFlowInstalled()
    {
        // CF-2284-441727389
        String installKey = "CF-" + "GS".hashCode() + "CopyFlow".hashCode();

        s_isCopyFlowInstalled = isInstalled(
                SystemConfigParamNames.COPYFLOW_INSTALL_KEY, installKey);

        return s_isCopyFlowInstalled;
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage. For
     * the LingAdapter this is either extracting the content to convert it to
     * GXML, or merging the content to remove GXML markup.
     * 
     * This adapter handles events like XXX_IMPORTED_EVENT and
     * XXX_LOCALIZED_EVENT
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
            case CxeMessageType.COPYFLOW_IMPORTED_EVENT:
                return convertFromQuark(p_cxeMessage);

            case CxeMessageType.COPYFLOW_LOCALIZED_EVENT:
                return convertToQuark(p_cxeMessage);

            default:
                Exception e = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                String errorArgs[] = new String[1];
                errorArgs[0] = getLogger().getName();
                throw new DesktopApplicationAdapterException("Unexpected",
                        errorArgs, e);
        }
    }

    /**
     * Converts the content to Xml and returns an AdapterResult.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type QUARK_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertFromQuark(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            if (!isCopyFlowInstalled())
            {
                DesktopApplicationAdapterException dae = new DesktopApplicationAdapterException(
                        "GlobalSightXT Adapter for Quark(MAC) Not Installed",
                        null, null);

                return this.makeImportError(p_cxeMessage, dae);
            }

            DesktopAppHelper helper = new DesktopAppHelper(p_cxeMessage,
                    getLogger());

            MessageData newContent = helper.convertNativeToXml();
            String newEventFlowXml = helper.getEventFlowXml();

            CxeMessageType type = helper.getPostNativeToXmlConversionEvent();
            CxeMessage outgoingMsg = new CxeMessage(type);
            outgoingMsg.setParameters(p_cxeMessage.getParameters());
            outgoingMsg.setEventFlowXml(newEventFlowXml);
            outgoingMsg.setMessageData(newContent);

            AdapterResult results[] = new AdapterResult[1];
            results[0] = new AdapterResult(outgoingMsg);

            return results;
        }
        catch (DesktopApplicationAdapterException msoe)
        {
            getLogger().error("Problem handling CopyFlow Conversion.", msoe);
            return this.makeImportError(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling CopyFlow Conversion.", e);
            DesktopApplicationAdapterException d = new DesktopApplicationAdapterException(
                    "Unexpected", null, e);
            return this.makeImportError(p_cxeMessage, d);
        }
    }

    /**
     * Handles the export case. Converts from XML to Quark
     * 
     * @param p_cxeMessage
     *            CxeMessage of type QUARK_LOCALIZED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertToQuark(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            DesktopAppHelper helper = new DesktopAppHelper(p_cxeMessage,
                    getLogger());

            MessageData newContent = helper.convertXmlToNative();
            String newEventFlowXml = helper.getEventFlowXml();

            CxeMessageType type = helper.getPostXmlToNativeConversionEvent();
            CxeMessage outgoingMsg = new CxeMessage(type);
            outgoingMsg.setParameters(p_cxeMessage.getParameters());
            outgoingMsg.setEventFlowXml(newEventFlowXml);
            outgoingMsg.setMessageData(newContent);

            AdapterResult results[] = new AdapterResult[1];
            results[0] = new AdapterResult(outgoingMsg);

            return results;
        }
        catch (DesktopApplicationAdapterException msoe)
        {
            return this.makeExportStatus(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling CopyFlow Re-Conversion.", e);
            DesktopApplicationAdapterException pae = new DesktopApplicationAdapterException(
                    "Unexpected", null, e);
            return this.makeExportStatus(p_cxeMessage, pae);
        }
    }
}
