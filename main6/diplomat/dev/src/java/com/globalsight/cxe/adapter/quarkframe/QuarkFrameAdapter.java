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
package com.globalsight.cxe.adapter.quarkframe;

import com.globalsight.cxe.adapter.quarkframe.DesktopAppHelper;
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
 * The QuarkFrameAdapter handles conversions of QuarkFrame to word using the
 * separate QuarkFrameConverter
 */
public class QuarkFrameAdapter extends BaseAdapter
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private String m_workingDir;

    private static boolean s_isQuarkInstalled = false;

    private static boolean s_isFrameInstalled = false;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    /**
     * Creates an MsOfficeAdapter
     * 
     * @param p_loggingCategory
     *            the given logging category name (for example
     *            "MsOfficeAdapter")
     * 
     * @exception GeneralException
     */
    public QuarkFrameAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
        isQuarkInstalled();
        isFrameInstalled();
        m_workingDir = SystemConfiguration.getInstance().getStringParameter(
                SystemConfigParamNames.CXE_NTCS_DIR);
    }

    /** * Public Methods ** */

    /**
     * Returns true if the given adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    public static boolean isQuarkInstalled()
    {
        String installKey = "QF-" + "GS".hashCode() + "-" + "Quark".hashCode();
        s_isQuarkInstalled = isInstalled(
                SystemConfigParamNames.QUARK_INSTALL_KEY, installKey);

        return s_isQuarkInstalled;
    }

    /**
     * Returns true if the given adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    public static boolean isFrameInstalled()
    {
        String installKey = "FR-" + "GS".hashCode() + "-" + "Frame".hashCode();
        s_isFrameInstalled = isInstalled(
                SystemConfigParamNames.FRAME_INSTALL_KEY, installKey);

        return s_isFrameInstalled;
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
            case CxeMessageType.QUARK_IMPORTED_EVENT:
                return convertFromQuarkFrame(p_cxeMessage, "quark");

            case CxeMessageType.FRAME_IMPORTED_EVENT:
                return convertFromQuarkFrame(p_cxeMessage, "frame");

            case CxeMessageType.QUARK_LOCALIZED_EVENT:
                return convertToQuarkFrame(p_cxeMessage, "quark");

            case CxeMessageType.FRAME_LOCALIZED_EVENT:
                return convertToQuarkFrame(p_cxeMessage, "frame");

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
     * Converts the content to XML/Mif and returns an AdapterResult.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type QUARK_IMPORTED_EVENT or
     *            FRAME_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertFromQuarkFrame(CxeMessage p_cxeMessage,
            String p_formatType) throws GeneralException
    {
        try
        {
            if ("frame".equals(p_formatType) && isFrameInstalled() == false)
            {
                DesktopApplicationAdapterException dae = new DesktopApplicationAdapterException(
                        "FrameNotInstalled", null, null);
                return this.makeImportError(p_cxeMessage, dae);
            }
            else if ("quark".equals(p_formatType)
                    && isQuarkInstalled() == false)
            {
                DesktopApplicationAdapterException dae = new DesktopApplicationAdapterException(
                        "QuarkNotInstalled", null, null);
                return this.makeImportError(p_cxeMessage, dae);
            }

            DesktopAppHelper helper = DesktopAppHelperFactory
                    .createDesktopAppHelper(m_workingDir, p_cxeMessage,
                            getLogger(), p_formatType);
            MessageData newContent = helper.convert();
            String newEventFlowXml = helper.getEventFlowXml();
            CxeMessageType type = helper.getPostConversionEvent();
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
            getLogger().error("Problem handling QuarkFrame Conversion.", msoe);
            return this.makeImportError(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling QuarkFrame Conversion.", e);
            DesktopApplicationAdapterException d = new DesktopApplicationAdapterException(
                    "Unexpected", null, e);
            return this.makeImportError(p_cxeMessage, d);
        }
    }

    /**
     * Handles the export case. Converts from XML/Mif to Quark/Frame
     * 
     * @param p_cxeMessage
     *            CxeMessage of type QUARK_LOCALIZED_EVENT or
     *            FRAME_LOCALIZED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertToQuarkFrame(CxeMessage p_cxeMessage,
            String p_formatType) throws GeneralException
    {
        try
        {
            DesktopAppHelper helper = DesktopAppHelperFactory
                    .createDesktopAppHelper(m_workingDir, p_cxeMessage,
                            getLogger(), p_formatType);
            MessageData newContent = helper.convertBack();
            String newEventFlowXml = helper.getEventFlowXml();
            CxeMessageType type = helper.getPostConversionBackEvent();
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
            getLogger().error("Problem handling QuarkFrame conversion back.", e);
            DesktopApplicationAdapterException pae = new DesktopApplicationAdapterException(
                    "Unexpected", null, e);
            return this.makeExportStatus(p_cxeMessage, pae);
        }
    }
}
