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
package com.globalsight.cxe.adapter.pdf;

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
 * The PdfAdapter handles conversions of pdf to word using the separate
 * PdfConverter
 */
public class PdfAdapter extends BaseAdapter
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private static boolean s_isInstalled = false;

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
    public PdfAdapter(String p_loggingCategory) throws GeneralException
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
        String installKey = "PDF-" + "GS".hashCode() + "-" + "pdf".hashCode();
        s_isInstalled = isInstalled(SystemConfigParamNames.PDF_INSTALL_KEY,
                installKey);

        return s_isInstalled;
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
            case CxeMessageType.PDF_IMPORTED_EVENT:
                return convertFromPdf(p_cxeMessage);

            case CxeMessageType.PDF_LOCALIZED_EVENT:
                return passOnWithoutChange(p_cxeMessage);

            default:
                Exception e = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                String errorArgs[] = new String[1];
                errorArgs[0] = getLogger().getName();
                throw new PdfAdapterException("Unexpected", errorArgs, e);
        }
    }

    /**
     * Converts the content to Word and returns an AdapterResult.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type PDF_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertFromPdf(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            getLogger().info("in convertFromPdf...");
            PdfHelper helper = new PdfHelper(p_cxeMessage, getLogger());
            MessageData newContent = helper.convertPdfToWord();
            String newEventFlowXml = helper.getEventFlowXml();

            CxeMessageType type = helper.getPostNativeConversionEvent();
            getLogger().info("outgoing msg..." + type.getName());
            CxeMessage outgoingMsg = new CxeMessage(type);
            outgoingMsg.setParameters(p_cxeMessage.getParameters());
            outgoingMsg.setEventFlowXml(newEventFlowXml);
            outgoingMsg.setMessageData(newContent);
            AdapterResult results[] = new AdapterResult[1];
            results[0] = new AdapterResult(outgoingMsg);
            return results;
        }
        catch (PdfAdapterException msoe)
        {
            return this.makeImportError(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling Pdf Conversion.", e);
            PdfAdapterException pae = new PdfAdapterException("Unexpected",
                    null, e);
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
    private AdapterResult[] passOnWithoutChange(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            PdfHelper helper = new PdfHelper(p_cxeMessage, getLogger());
            MessageData newContent = helper.convertWordToPdf();
            String newEventFlowXml = helper.getEventFlowXml();

            CxeMessageType type = helper.getPostWordToPdfConversionEvent();
            CxeMessage outgoingMsg = new CxeMessage(type);
            outgoingMsg.setParameters(p_cxeMessage.getParameters());
            outgoingMsg.setEventFlowXml(newEventFlowXml);
            outgoingMsg.setMessageData(newContent);
            AdapterResult results[] = new AdapterResult[1];
            results[0] = new AdapterResult(outgoingMsg);
            return results;
        }
        catch (PdfAdapterException msoe)
        {
            return this.makeExportStatus(p_cxeMessage, msoe);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling MS Office Re-Conversion.", e);
            PdfAdapterException pae = new PdfAdapterException("Unexpected",
                    null, e);
            return this.makeExportStatus(p_cxeMessage, pae);
        }
    }
}
