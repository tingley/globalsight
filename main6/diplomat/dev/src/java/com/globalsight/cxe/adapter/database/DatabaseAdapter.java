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
package com.globalsight.cxe.adapter.database;

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

/**
 * The DatabaseAdapter class provides a means of interacting with the SQL
 * databases in order to do import/export (and possibly other) functions within
 * CXE.
 */
public class DatabaseAdapter extends BaseAdapter
{
    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private static boolean s_isInstalled = false;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    /**
     * Creates a DatabaseAdapter object
     * 
     * @param p_loggingCategory
     *            the given logging category name (for example
     *            "DatabaseTargetAdapter")
     * 
     * @exception GeneralException
     */
    public DatabaseAdapter(String p_loggingCategory) throws GeneralException
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
        String installKey = "DB-" + "GS".hashCode() + "-" + "dbase".hashCode();
        s_isInstalled = isInstalled(
                SystemConfigParamNames.DATABASE_INSTALL_KEY, installKey);

        return s_isInstalled;
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage. For
     * the DatabaseAdapter this is only writing content back to the DB since
     * auto-import handles polling the DB for changes.
     * </P>
     * <P>
     * The adapter switches on the CxeMessage message type and handles the
     * following events:<br>
     * <ol>
     * <li>DATABASE_EXPORT_EVENT</li>
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
            case CxeMessageType.DATABASE_EXPORT_EVENT:
                return handleExportEvent(p_cxeMessage);
            default:
                Exception e = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                String[] errorArgs = new String[1];
                errorArgs[0] = getLogger().getName();
                throw new DatabaseAdapterException("Unexpected", errorArgs, e);
        }
    }

    /**
     * Handles the export case. Writes the given content out to the database.
     * NOTE: There is no Secondary Target File creation for Database Jobs
     * 
     * @param p_cxeMessage
     *            CxeMessage of type DATABASE_EXPORT_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleExportEvent(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        AdapterResult[] results = null;
        try
        {
            Logger
                    .writeDebugFile("dbTarget.xml", p_cxeMessage
                            .getMessageData());
            ExportDatabaseOperationHelper helper = new ExportDatabaseOperationHelper(
                    p_cxeMessage);
            String prsXml = helper.getPrsXml();
            helper.writeToDatabase();
            HashMap params = p_cxeMessage.getParameters();
            params.put("PreviewUrlXml", helper.findPreviewUrlXml());
            params.put("ExportedTime", new Long(System.currentTimeMillis()));
            params.put("Exception", null);
            p_cxeMessage.setDeleteMessageData(true);
            results = this.makeExportStatus(p_cxeMessage, null);
        }
        catch (Exception e)
        {
            getLogger().error("Failed to export to the customer database.", e);
            String[] errorArgs = new String[1];
            errorArgs[0] = this.getLogger().getName();
            DatabaseAdapterException fsae = new DatabaseAdapterException(
                    "UnexpectedIm", errorArgs, e);
            results = makeExportStatus(p_cxeMessage, fsae);
        }
        return results;
    }
}
