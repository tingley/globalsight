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
package com.globalsight.cxe.adapter.documentum;

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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashMap;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.ling.common.URLDecoder;
import java.net.URL;
import java.util.Properties;
import java.io.FileNotFoundException;

/**
 * The Documentum adapter class provides a means of interacting with the
 * Documentum KMS in order to do import/export (and possibly other) functions
 * within CXE.
 * 
 * 
 * NOTE: This adapter is currently at demo-level functionality
 */
public class DocumentumAdapter extends BaseAdapter
{
    private static final org.apache.log4j.Logger s_logger = org.apache.log4j.Logger
            .getLogger(DocumentumAdapter.class.getName());

    // ////////////////////////////////////
    // Public static members //
    // ////////////////////////////////////
    public static String s_username = null;

    public static String s_password = null;

    public static String s_docbase = null;

    static
    {
        // load the documentum connection info
        try
        {
            URL url = DocumentumAdapter.class
                    .getResource("/properties/DocumentumAdapter.properties");
            if (url == null)
                throw new FileNotFoundException(
                        "Property file DocumentumAdapter.properties not found");
            String s = URLDecoder.decode(url.toURI().getPath(), "UTF-8");
            FileInputStream fis = new FileInputStream(s);
            Properties props = new Properties();
            props.load(fis);
            fis.close();
            s_username = props.getProperty("username");
            s_password = props.getProperty("password");
            s_docbase = props.getProperty("docbase");
            s_logger.debug("Using username '" + s_username
                    + "' to connect to docbase '" + s_docbase
                    + "' in Documentum.");
        }
        catch (Exception e)
        {
            s_logger.error("Unable to load Documentum properties.", e);
        }
    }

    // Public static members

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private static boolean s_isInstalled = false;

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    /**
     * Creates a DocumentumAdapter object
     * 
     * @param p_loggingCategory
     *            the given logging category name (for example
     *            "FileSystemSourceAdapter" or "FileSystemTargetAdapter")
     * 
     * @exception GeneralException
     */
    public DocumentumAdapter(String p_loggingCategory) throws GeneralException
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
        // key is DCTM-2284-100539
        String installKey = "DCTM-" + "GS".hashCode() + "-" + "emc".hashCode();
        s_isInstalled = isInstalled(
                SystemConfigParamNames.DOCUMENTUM_INSTALL_KEY, installKey);

        return s_isInstalled;
    }

    /**
     * <P>
     * Performs the main function of the adapter based on the CxeMessage. For
     * the DocumentumAdapter this is either "reading" the file in from the
     * filesystem and creating the necessary EventFlowXml as a part of import,
     * or "writing" the file back to the file system for export.
     * </P>
     * <P>
     * The adapter switches on the CxeMessage message type and handles the
     * following events:<br>
     * <ol>
     * <li>DOCUMENTUM_FILE_SELECTED_EVENT </li>
     * <li>DOCUMENTUM_EXPORT_EVENT </li>
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
            case CxeMessageType.DOCUMENTUM_FILE_SELECTED_EVENT:
                return handleSelectedFileEvent(p_cxeMessage);
            case CxeMessageType.DOCUMENTUM_EXPORT_EVENT:
                return handleExportEvent(p_cxeMessage);
            default:
                Exception e = new Exception("Unhandled message type: "
                        + p_cxeMessage.getMessageType().getName());
                throw new DocumentumAdapterException("Unexpected", getLogger()
                        .getName(), e);
        }
    }

    /**
     * Handles the import case where a user has selected a file and a "selected
     * file event" comes in to the file system adapter
     * 
     * @param p_cxeMessage
     *            CxeMessage of type FILE_SYSTEM_FILE_SELECTED_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleSelectedFileEvent(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        System.out.println("DocumentumAdapter.handleSelectedFileEvent()");
        AdapterResult[] results = null;
        String filename = null;
        try
        {
            Importer importer = new Importer(p_cxeMessage, getLogger());
            MessageData newMessageData = importer.readContent();
            String newEventFlowXml = importer.makeEventFlowXml();
            HashMap newParams = p_cxeMessage.getParameters();
            BaseAdapter.preserveOriginalFileContent(newMessageData, newParams);
            results = makeSingleAdapterResult(importer.getPreExtractEvent(),
                    newMessageData, newParams, newEventFlowXml);
        }
        catch (DocumentumAdapterException fsae)
        {
            results = makeImportError(p_cxeMessage, fsae);
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = this.getLogger().getName();
            errorArgs[1] = filename;
            DocumentumAdapterException fsae = new DocumentumAdapterException(
                    "UnexpectedIm", errorArgs, e);
            results = makeImportError(p_cxeMessage, fsae);
        }

        return results;
    }

    /**
     * Handles the export case. Writes the given content out.
     * 
     * @param p_cxeMessage
     *            CxeMessage of type FILE_SYSTEM_EXPORT_EVENT
     * @return AdapterResult[]
     * @exception GeneralException
     */
    private AdapterResult[] handleExportEvent(CxeMessage p_cxeMessage)
            throws GeneralException
    {
        AdapterResult[] results = new AdapterResult[1];
        CxeMessage returnMessage = null;
        try
        {
            if (isStfCreationExport(p_cxeMessage))
            {
                returnMessage = makeStfCreationMsg(p_cxeMessage);
            }
            else
            {
                Exporter exporter = new Exporter(p_cxeMessage, this.getLogger());
                returnMessage = exporter.export();
            }

            results = makeExportStatus(returnMessage, null);
        }
        catch (DocumentumAdapterException fsae)
        {
            results = makeExportStatus(p_cxeMessage, fsae);
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[1];
            errorArgs[0] = this.getLogger().getName();
            DocumentumAdapterException fsae = new DocumentumAdapterException(
                    "UnexpectedIm", errorArgs, e);
            results = makeExportStatus(p_cxeMessage, fsae);
        }
        return results;
    }
}
