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

package com.globalsight.cxe.adapter.adobe;

import java.util.Properties;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.util.GeneralException;

/**
 * The AdobeAdapter handles conversions of adobe file to xml using the separate
 * Adobe Converters
 */
public class AdobeAdapter extends BaseAdapter
{

    private AdobeConfiguration m_adobeConfig = AdobeConfiguration.getInstance();

    private static boolean s_isInddInstalled = false;

    private static boolean s_isIllustratorInstalled = false;

    public AdobeAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
    }

    public static boolean isInddInstalled()
    {
        String installKey = "INDD-" + "GS".hashCode() + "-" + "indd".hashCode();
        s_isInddInstalled = isInstalled(
                SystemConfigParamNames.INDD_INSTALL_KEY, installKey);

        return s_isInddInstalled;
    }

    public static boolean isIllustratorInstalled()
    {
        String installKey = "AI-" + "GS".hashCode() + "-"
                + "illustrator".hashCode();
        s_isIllustratorInstalled = isInstalled(
                SystemConfigParamNames.ILLUSTRATOR_INSTALL_KEY, installKey);

        return s_isIllustratorInstalled;
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage.
     * 
     * This adapter handles events like ADOBE_IMPORTED_EVENT and
     * ADOBE_LOCALIZED_EVENT
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
            case CxeMessageType.ADOBE_IMPORTED_EVENT:
                return convertFromAdobe(p_cxeMessage);

            case CxeMessageType.ADOBE_LOCALIZED_EVENT:
                return convertToAdobe(p_cxeMessage);

            default:
                throw createExcpetionForUnknownMessageType(p_cxeMessage
                        .getMessageType());
        }
    }

    private AdobeAdapterException createExcpetionForUnknownMessageType(
            CxeMessageType type)
    {
        Exception e = new Exception("Unhandled message type: " + type.getName());
        String errorArgs[] = new String[1];
        errorArgs[0] = type.getName();
        return new AdobeAdapterException("Unexpected", errorArgs, e);
    }

    /**
     * converts the adobe file to xml.
     * 
     * @param message
     *            the CxeMessage object.
     * @return AdapterResult[]
     */
    private AdapterResult[] convertFromAdobe(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            Properties props = m_adobeConfig.loadProperties();
            AdobeHelper helper = new AdobeHelper(p_cxeMessage, props);

            return adapterCxeMessages(helper.performConversion());
        }
        catch (AdobeAdapterException aae)
        {
            return this.makeImportError(p_cxeMessage, aae);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling Adobe Conversion. ", e);
            AdobeAdapterException ie = new AdobeAdapterException("Unexpected",
                    null, e);
            return this.makeImportError(p_cxeMessage, ie);
        }
    }

    private AdapterResult[] adapterCxeMessages(CxeMessage[] msgs)
    {
        AdapterResult results[] = new AdapterResult[msgs.length];
        for (int i = 0; i < msgs.length; i++)
        {
            results[i] = new AdapterResult(msgs[i]);
        }
        return results;
    }

    private AdapterResult[] convertToAdobe(CxeMessage p_cxeMessage)
    {
        try
        {
            Properties props = m_adobeConfig.loadProperties();
            AdobeHelper helper = new AdobeHelper(p_cxeMessage, props);

            return adapterCxeMessages(helper.performConversionBack());
        }
        catch (AdobeAdapterException aae)
        {
            return this.makeExportStatus(p_cxeMessage, aae);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling Adobe Conversion.", e);
            AdobeAdapterException ie = new AdobeAdapterException("Unexpected",
                    null, e);
            return this.makeExportStatus(p_cxeMessage, ie);
        }
    }

}