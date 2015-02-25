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
package com.globalsight.cxe.adapter.msoffice;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.CxeProcessor;
import com.globalsight.cxe.adapter.IConverterHelper2;
import com.globalsight.cxe.adapter.msoffice.MsOfficeAdapterException;
import com.globalsight.cxe.adapter.msoffice.MicrosoftWordHelper;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.util.GeneralException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Properties;

import java.io.FileInputStream;

import org.xml.sax.SAXException;

import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;

/**
 * The MsOfficeAdapter handles conversions of word, excel, ppt by using the
 * separate MsOfficeConverter
 */
public class MsOfficeAdapter extends BaseAdapter
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private MsOfficeConfiguration m_msOfficeConfig = MsOfficeConfiguration
            .getInstance();

    static private boolean s_isWordInstalled = false;

    static private boolean s_isExcelInstalled = false;

    static private boolean s_isPowerPointInstalled = false;

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
    public MsOfficeAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
    }

    //
    // Public Methods
    //

    /**
     * Returns true if the word adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    public static boolean isWordInstalled()
    {
        String installKey = "DOC-" + "GS".hashCode() + "-" + "Word".hashCode();
        s_isWordInstalled = isInstalled(
                SystemConfigParamNames.WORD_INSTALL_KEY, installKey);

        return s_isWordInstalled;
    }

    /**
     * Returns true if the excel adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    public static boolean isExcelInstalled()
    {
        String installKey = "XLS-" + "GS".hashCode() + "-" + "Excel".hashCode();
        s_isExcelInstalled = isInstalled(
                SystemConfigParamNames.EXCEL_INSTALL_KEY, installKey);

        return s_isExcelInstalled;
    }

    /**
     * Returns true if the powerpoint adapter is installed. This method may
     * check installation key validity.
     * 
     * @return true | false
     */
    public static boolean isPowerPointInstalled()
    {
        String installKey = "PPT-" + "GS".hashCode() + "-" + "ppt".hashCode();
        s_isPowerPointInstalled = isInstalled(
                SystemConfigParamNames.POWERPOINT_INSTALL_KEY, installKey);

        return s_isPowerPointInstalled;
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
            case CxeMessageType.MSOFFICE_IMPORTED_EVENT:
                return convertFromMsOffice(p_cxeMessage);

            case CxeMessageType.MSOFFICE_LOCALIZED_EVENT:
                return convertToMsOffice(p_cxeMessage);

            default:
                Exception ex = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                String errorArgs[] = new String[1];
                errorArgs[0] = getLogger().getName();
                throw new MsOfficeAdapterException("Unexpected", errorArgs, ex);
        }
    }

    /**
     * Converts the content to HTML and returns an AdapterResult.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type XXX_IMPORTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertFromMsOffice(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            Properties props = m_msOfficeConfig.loadProperties();

            IConverterHelper2 helper = initHelper(p_cxeMessage, props);

            AdapterResult results[] = helper.performConversion();
            return results;
        }
        catch (MsOfficeAdapterException msoe)
        {
            return this.makeImportError(p_cxeMessage, msoe);
        }
        catch (Exception ex)
        {
            getLogger().error("Problem handling MS Office Conversion.", ex);
            MsOfficeAdapterException msoe = new MsOfficeAdapterException(
                    "Unexpected", null, ex);
            return this.makeImportError(p_cxeMessage, msoe);
        }
    }

    private IConverterHelper2 initHelper(CxeMessage p_cxeMessage, Properties props) throws SAXException, IOException
    {
        IConverterHelper2 helper = null;
        if (IFormatNames.FORMAT_OFFICE_XML.equals(p_cxeMessage.getEventFlowObject().getSource().getFormatType()))
        {
            helper = new OfficeXmlHelper(p_cxeMessage, getLogger(), props);
        }
        else
        {
            helper = new MicrosoftWordHelper(p_cxeMessage, getLogger(), props);
        }
        return helper;
    }

    /**
     * Handles the export case. Converts the HTML back to the right MS Office
     * format.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type XXX_LOCALIZED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] convertToMsOffice(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            Properties props = m_msOfficeConfig.loadProperties();

            IConverterHelper2 helper = initHelper(p_cxeMessage, props);

            CxeMessage msg = helper.performConversionBack();

            AdapterResult[] results = new AdapterResult[1];
            results[0] = new AdapterResult(msg);

            return results;
        }
        catch (MsOfficeAdapterException msoe)
        {
            return this.makeExportStatus(p_cxeMessage, msoe);
        }
        catch (Exception ex)
        {
            getLogger().error("Problem handling MS Office Re-Conversion.", ex);
            MsOfficeAdapterException msoe = new MsOfficeAdapterException(
                    "Unexpected", null, ex);
            return this.makeExportStatus(p_cxeMessage, msoe);
        }
    }
}
