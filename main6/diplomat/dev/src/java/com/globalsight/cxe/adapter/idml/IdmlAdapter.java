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

package com.globalsight.cxe.adapter.idml;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.util.GeneralException;

/**
 * The IdmlAdapter handles conversions of open office file to xml
 */
public class IdmlAdapter extends BaseAdapter
{
    public IdmlAdapter(String p_loggingCategory) throws GeneralException
    {
        super(p_loggingCategory);
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage.
     * 
     * This adapter handles events like Idml_IMPORTED_EVENT and
     * Idml_LOCALIZED_EVENT
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
            case CxeMessageType.IDML_IMPORTED_EVENT:
                return convertFromIdml(p_cxeMessage);

            case CxeMessageType.IDML_LOCALIZED_EVENT:
                return convertToIdml(p_cxeMessage);

            default:
                throw createExcpetionForUnknownMessageType(p_cxeMessage
                        .getMessageType());
        }
    }

    private IdmlAdapterException createExcpetionForUnknownMessageType(
            CxeMessageType type)
    {
        Exception e = new Exception("Unhandled message type: " + type.getName());
        String errorArgs[] = new String[1];
        errorArgs[0] = type.getName();
        return new IdmlAdapterException("Unexpected", errorArgs, e);
    }

    /**
     * converts the open office file to xml.
     * 
     * @param message
     *            the CxeMessage object.
     * @return AdapterResult[]
     */
    private AdapterResult[] convertFromIdml(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        try
        {
            IdmlHelper helper = new IdmlHelper(p_cxeMessage);

            return adapterCxeMessages(helper.performConversion());
        }
        catch (IdmlAdapterException aae)
        {
            return this.makeImportError(p_cxeMessage, aae);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling Idml Conversion. ", e);
            IdmlAdapterException ie = new IdmlAdapterException("Unexpected",
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

    private AdapterResult[] convertToIdml(CxeMessage p_cxeMessage)
    {
        try
        {
            IdmlHelper helper = new IdmlHelper(p_cxeMessage);

            return adapterCxeMessages(helper.performConversionBack());
        }
        catch (IdmlAdapterException aae)
        {
            return this.makeExportStatus(p_cxeMessage, aae);
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling Idml Conversion.", e);
            IdmlAdapterException ie = new IdmlAdapterException("Unexpected",
                    null, e);
            return this.makeExportStatus(p_cxeMessage, ie);
        }
    }

}