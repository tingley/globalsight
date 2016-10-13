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
package com.globalsight.cxe.adapter;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GeneralException;

/**
 * The BaseAdapter is a class that all adapters extend.<br>
 * It provides common methods for the adapters.
 */
public abstract class BaseAdapter
{

    public static final String PARAM_ORIGINAL_FILE_CONTENT = "originalFileContent";

    // ////////////////////////////////////
    // Private Member Variables //
    // ////////////////////////////////////
    private CxeProcessor m_preProcessor = null;

    private CxeProcessor m_postProcessor = null;

    private EventFlowXmlParser m_parser;

    /**
     * Holds adapter specific configuration loaded from a property file with the
     * name <adapter>.properties
     */
    private SystemConfiguration m_adapterConfig = null;

    private String m_adapterBaseName = null; // for example MsOfficeAdapter

    private Logger m_logger = null;

    private String m_loggingCategory = null;

    // ////////////////////////////////////
    // Constructors //
    // ////////////////////////////////////

    /**
     * Creates a BaseAdapter using the given logging category name.
     * 
     * @param p_loggingCategory
     *            the name of the logging category to use
     * @exception GeneralException
     */
    protected BaseAdapter(String p_loggingCategory) throws GeneralException
    {
        /* all your BaseAdapter are belong to us */

        String className = this.getClass().getName();
        m_adapterBaseName = className.substring(className.lastIndexOf(".") + 1);
        m_loggingCategory = p_loggingCategory;
        m_parser = new EventFlowXmlParser();
        m_logger = Logger.getLogger(p_loggingCategory);
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Performs the main function of the adapter based on the CxeMessage.
     * 
     * @param p_cxeMessage
     *            a CxeMessage object containing EventFlowXml, content, and
     *            possibly parameters
     * @return AdapterResult[]
     * @exception GeneralException
     */
    public abstract AdapterResult[] handleMessage(CxeMessage p_cxeMessage)
            throws GeneralException;

    /**
     * Runs the adapter's preprocessor on the CxeMessage
     * 
     * @param p_cxeMessage
     *            cxe message
     * @return CxeMessage
     */
    public CxeMessage runPreProcessor(CxeMessage p_cxeMessage)
    {
        try
        {
            CxeProcessor preProcessor = getPreProcessor();
            if (preProcessor != null && m_logger.isDebugEnabled())
                m_logger.debug("Running pre processor: "
                        + preProcessor.getClass().getName());
            return runProcessor(preProcessor, p_cxeMessage);
        }
        catch (Throwable t)
        {
            m_logger.error("Could not run pre processor.", t);
            return p_cxeMessage;
        }
    }

    /**
     * Runs the adapter's postprocessor on the CxeMessage
     * 
     * @param p_cxeMessage
     *            cxe message
     * @return CxeMessage
     */
    public CxeMessage runPostProcessor(CxeMessage p_cxeMessage)
    {
        try
        {
            CxeProcessor postProcessor = getPostProcessor();
            if (postProcessor != null && m_logger.isDebugEnabled())
                m_logger.debug("Running post processor: "
                        + postProcessor.getClass().getName());
            return runProcessor(postProcessor, p_cxeMessage);
        }
        catch (Throwable t)
        {
            m_logger.error("Could not run post processor.", t);
            return p_cxeMessage;
        }
    }

    /**
     * Returns true if the given adapter is installed. This method may check
     * installation key validity.
     * 
     * @return true | false
     */
    static public boolean isInstalled()
    {
        return true;
    }

    // ////////////////////////////////////
    // Protected Methods //
    // ////////////////////////////////////
    /**
     * Gets the adapter's EventFlowXmlParser n *
     * 
     * @return EventFlowXmlParser
     */
    protected EventFlowXmlParser getEventFlowXmlParser()
    {
        return m_parser;
    }

    /**
     * Gets the pre processor. This may be null if there is none.
     * 
     * @return CxeProcessor
     */
    protected CxeProcessor getPreProcessor()
    {
        return m_preProcessor;
    }

    /**
     * Gets the post processor. This may be null if there is none.
     * 
     * @return CxeProcessor
     */
    protected CxeProcessor getPostProcessor()
    {
        return m_postProcessor;
    }

    /**
     * Loads the pre and post processors that are used by the adapter. The
     * processors are loaded based on fully qualified java class names in the
     * <adapterName>.properties file based on the adapter name.<br>
     * For example:<br>
     * MsOfficeAdapter.preProcessor=com.globalsight.pso.hp.FerryPre
     * MsOfficeAdapter.postProcessor=com.globalsight.pso.hp.FerryPost
     */
    public void loadProcessors()
    {
        String name = null;
        try
        {
        	if (m_adapterConfig != null)
        	{
                // preprocessor
                name = m_adapterConfig.getStringParameter(m_loggingCategory
                        + ".preProcessor");
                m_preProcessor = loadProcessorClass(name);
                // postprocessor
                name = m_adapterConfig.getStringParameter(m_loggingCategory
                        + ".postProcessor");
                m_postProcessor = loadProcessorClass(name);
        	}
        	else
        	{
        		if (m_logger.isDebugEnabled())
        		{
            		m_logger.warn("m_adapterConfig is null");        			
        		}
        	}
        }
        catch (Exception e)
        {
            m_logger.error("Could not load pre or post processor: " + name, e);
        }
    }

    /**
     * Dynamically loads the given processor class.
     * 
     * @param p_className
     *            full java class name of processor
     * @return CxeProcessor
     * @exception Exception
     */
    protected CxeProcessor loadProcessorClass(String p_className)
            throws Exception
    {
        if (p_className != null && p_className.length() > 0)
        {
            m_logger.info("Loading processor class " + p_className);
            return (CxeProcessor) Class.forName(p_className).newInstance();
        }
        else
            return null;
    }

    /**
     * Loads the adapter specific configuration. This loads properties from the
     * <adapterName>.properties file.
     * 
     * @exception GeneralException
     */
    public void loadConfiguration() throws GeneralException
    {

        String propertyFile = "/properties/" + m_adapterBaseName
                + ".properties";
        m_adapterConfig = SystemConfiguration.getInstance(propertyFile);
    }

    /**
     * Gets the property file configuration for this adapter
     * 
     * @return SystemConfiguration
     */
    protected SystemConfiguration getConfig()
    {
        return m_adapterConfig;
    }

    /**
     * Gets the correct logger to use
     * 
     * @return Logger logger
     */
    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Runs the given CxeProcessor with the given CxeMessage and returns the
     * results in a CxeMessage
     * 
     * @param p_processor
     *            a CxeProcessor (possibly null)
     * @param p_cxeMessage
     *            CxeMessage (cannot be null)
     * @return CxeMessage (possibly the same as what was passed in)
     */
    protected CxeMessage runProcessor(CxeProcessor p_processor,
            CxeMessage p_cxeMessage)
    {
        if (p_processor != null)
        {
            return p_processor.process(p_cxeMessage);
        }
        else
            return p_cxeMessage;
    }

    /**
     * Convenience method to create an AdapterResult array based on a newly
     * created CxeMessage object. The created array only has one AdapterResult
     * in it.
     * 
     * @param p_msgType
     *            the type of the CxeMessage
     * @param p_messageData
     *            MessageData
     * @param p_params
     *            HashMap of parameters
     * @param p_eventFlowXml
     *            String of EventFlowXml
     * @return AdapterResult[]
     */
    protected static AdapterResult[] makeSingleAdapterResult(
            CxeMessageType p_msgType, MessageData p_messageData,
            HashMap p_params, String p_eventFlowXml) throws IOException
    {
        CxeMessage outputMessage = new CxeMessage(p_msgType);
        outputMessage.setMessageData(p_messageData);
        outputMessage.setParameters(p_params);
        outputMessage.setEventFlowXml(p_eventFlowXml);
        return makeSingleAdapterResult(outputMessage);
    }
    
    /**
     * Convenience method to create an AdapterResult array based on a newly
     * created CxeMessage object. The created array only has one AdapterResult
     * in it.
     * 
     * @param p_msgType
     *            the type of the CxeMessage
     * @param p_messageData
     *            MessageData
     * @param p_params
     *            HashMap of parameters
     * @param p_eventFlowXml
     *            Object of EventFlowXml
     * @return AdapterResult[]
     */
    protected static AdapterResult[] makeSingleAdapterResult(
            CxeMessageType p_msgType, MessageData p_messageData,
            HashMap p_params, EventFlowXml p_eventFlowXml) throws IOException
    {
        CxeMessage outputMessage = new CxeMessage(p_msgType);
        outputMessage.setMessageData(p_messageData);
        outputMessage.setParameters(p_params);
        outputMessage.setEventFlowObject(p_eventFlowXml);
        return makeSingleAdapterResult(outputMessage);
    }

    /**
     * Convenience method to create an AdapterResult array based on a newly
     * created CxeMessage object. The created array only has one AdapterResult
     * in it.
     * 
     * @param p_cxeMessage
     *            the output cxe message
     * @return AdapterResult[]
     */
    protected static AdapterResult[] makeSingleAdapterResult(
            CxeMessage p_cxeMessage)
    {
        AdapterResult[] results = new AdapterResult[1];

        results[0] = new AdapterResult(p_cxeMessage);
        return results;
    }

    /**
     * Convenience method to quickly make an AdapterResult array containing a
     * single import error event
     * 
     * @param p_cxeMessage
     *            a cxe message
     * @return AdapterResult[] of one item
     */
    protected AdapterResult[] makeImportError(CxeMessage p_cxeMessage,
            GeneralException p_exception)
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_IMPORT_ERROR_EVENT);
        CxeMessage errorMsg = new CxeMessage(type);
        errorMsg.setEventFlowXml(p_cxeMessage.getEventFlowXml());
        try
        {
            errorMsg.setMessageData(p_cxeMessage.getMessageData());
        }
        catch (IOException ioe)
        {
            getLogger()
                    .error("Could not create message data in import error event.",
                            ioe);
        }
        errorMsg.setParameters(p_cxeMessage.getParameters());
        p_exception.setLogger(null);
        errorMsg.getParameters().put("Exception", p_exception);
        AdapterResult[] results = new AdapterResult[1];
        results[0] = new AdapterResult(errorMsg);
        return results;
    }

    /**
     * Convenience method to quickly make an AdapterResult array containing an
     * export status event, and a dynamic preview event if the export is for
     * preview.
     * 
     * @param p_cxeMessage
     *            a cxe message
     * @return AdapterResult[] of one item
     */
    protected AdapterResult[] makeExportStatus(CxeMessage p_cxeMessage,
            GeneralException p_exception)
    {

        AdapterResult[] results = null;
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.CXE_EXPORT_STATUS_EVENT);
        CxeMessage errorMsg = new CxeMessage(type);
        errorMsg.setEventFlowXml(p_cxeMessage.getEventFlowXml());
        errorMsg.setParameters(p_cxeMessage.getParameters());
        errorMsg.getParameters().put("Exception", p_exception);
        try
        {
            errorMsg.setMessageData(p_cxeMessage.getMessageData());
        }
        catch (IOException ioe)
        {
            getLogger()
                    .error("Could not create message data in import error event.",
                            ioe);
        }

        CxeMessage dynPrevMsg = null;
        String requestType = (String) p_cxeMessage.getParameters().get(
                "CxeRequestType");
        if (ExportConstants.PREVIEW.equals(requestType))
        {
            CxeMessageType dtype = CxeMessageType
                    .getCxeMessageType(CxeMessageType.DYNAMIC_PREVIEW_EVENT);
            dynPrevMsg = new CxeMessage(dtype);
            dynPrevMsg.setParameters(p_cxeMessage.getParameters());
            dynPrevMsg.setEventFlowXml(p_cxeMessage.getEventFlowXml());
            dynPrevMsg.getParameters().put("Exception", p_exception);
        }

        if (dynPrevMsg != null)
        {
            results = new AdapterResult[2];
            results[1] = new AdapterResult(dynPrevMsg);
        }
        else
            results = new AdapterResult[1];

        results[0] = new AdapterResult(errorMsg);
        return results;
    }

    /**
     * Returns true if this is an STF creation export
     * 
     * @param p_cxeMessage
     *            cxe message
     * @return boolean
     */
    protected boolean isStfCreationExport(CxeMessage p_cxeMessage)
    {
        String exportType = (String) p_cxeMessage.getParameters().get(
                "CxeRequestType");
        if (ExportConstants.EXPORT_FOR_STF_CREATION.equals(exportType))
            return true;
        else
            return false;
    }

    /**
     * Makes a message to be used for STF creation. This message contains a
     * MessageData related to the "almost exported" version of the content
     * 
     * @param p_cxeMessage
     *            incoming Cxe Message
     * @return STF Creation Msg
     */
    protected CxeMessage makeStfCreationMsg(CxeMessage p_cxeMessage)
            throws IOException
    {
        CxeMessageType type = CxeMessageType
                .getCxeMessageType(CxeMessageType.STF_CREATION_EVENT);
        CxeMessage newCxeMessage = new CxeMessage(type);
        newCxeMessage.setEventFlowXml(p_cxeMessage.getEventFlowXml());
        newCxeMessage.setMessageData(p_cxeMessage.getMessageData());
        newCxeMessage.setParameters(p_cxeMessage.getParameters());
        return newCxeMessage;
    }

    /**
     * Makes a copy of the original file and adds it to the parameters as
     * "originalFileContent"
     * 
     * @param p_messageData
     *            source/target message data content
     * @param p_params
     *            hashmap of params
     */
    public static void preserveOriginalFileContent(MessageData p_messageData,
            HashMap p_params) throws java.io.IOException
    {
        FileMessageData copy = MessageDataFactory.createFileMessageData();
        copy.copyFrom(p_messageData);
        p_params.put(PARAM_ORIGINAL_FILE_CONTENT, copy.getFile()
                .getAbsolutePath());
    }

    /**
     * Looks up the system parameter with the given name and compares its value
     * to the given key. Returns false if any exceptions are encountered
     * 
     * @param p_keyParameter
     *            system parameter name for an install key
     * @param p_keyValue
     *            an actual install key value
     * @return TRUE | FALSE
     */
    static protected boolean isInstalled(String p_keyParameter,
            String p_keyValue)
    {
        return SystemConfiguration.isKeyValid(p_keyParameter);
    }
}
