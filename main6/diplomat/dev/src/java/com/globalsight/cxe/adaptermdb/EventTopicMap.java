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
package com.globalsight.cxe.adaptermdb;

import java.util.HashMap;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * The EventTopicMap contains knowledge of how CXE Event Names map to
 * JMS topics. This is used by the adapter message proxies (message
 * driven beans).
 */
public class EventTopicMap
{
    //////////////////////////////////////
    // Public Constants                 //
    //////////////////////////////////////

    //JMS Topic JNDI Names (one for each adapter)
    //an example topic name is com.globalsight.cxe.jms.ForFileSystemSourceAdapter
    public static final String JMS_PREFIX = "com.globalsight.cxe.jms.";
    public static final String FOR_FILE_SYSTEM_SOURCE_ADAPTER = "ForFileSystemSourceAdapter";
    public static final String FOR_FILE_SYSTEM_TARGET_ADAPTER = "ForFileSystemTargetAdapter";
    public static final String FOR_EXTRACTOR = "ForExtractor";
    public static final String FOR_MERGER = "ForMerger";
    public static final String FOR_CAP_TARGET_ADAPTER = "ForCapTargetAdapter";
    public static final String FOR_CAP_SOURCE_ADAPTER = "ForCapSourceAdapter";
    public static final String FOR_CAP_EXPORT_LISTENER = "ForCapExportListener";
    public static final String FOR_TEAMSITE_SOURCE_ADAPTER = "ForTeamSiteSourceAdapter";
    public static final String FOR_TEAMSITE_TARGET_ADAPTER = "ForTeamSiteTargetAdapter";
    public static final String FOR_VIGNETTE_SOURCE_ADAPTER = "ForVignetteSourceAdapter";
    public static final String FOR_VIGNETTE_TARGET_ADAPTER = "ForVignetteTargetAdapter";
    public static final String FOR_DATABASE_TARGET_ADAPTER = "ForDatabaseTargetAdapter"; //do not need one for DB Src Adapter because of auto-polling
    public static final String FOR_MSOFFICE_SOURCE_ADAPTER = "ForMsOfficeSourceAdapter";
    public static final String FOR_MSOFFICE_TARGET_ADAPTER = "ForMsOfficeTargetAdapter";
    public static final String FOR_PDF_SOURCE_ADAPTER = "ForPdfSourceAdapter";
    public static final String FOR_PDF_TARGET_ADAPTER = "ForPdfTargetAdapter";
    public static final String FOR_QUARKFRAME_SOURCE_ADAPTER = "ForQuarkFrameSourceAdapter";
    public static final String FOR_QUARKFRAME_TARGET_ADAPTER = "ForQuarkFrameTargetAdapter";
    public static final String FOR_COPYFLOW_SOURCE_ADAPTER = "ForCopyFlowSourceAdapter";
    public static final String FOR_COPYFLOW_TARGET_ADAPTER = "ForCopyFlowTargetAdapter";
    public static final String FOR_DYNAMIC_PREVIEW = "ForDynamicPreview";
    public static final String FOR_MEDIASURFACE_SOURCE_ADAPTER = "ForMediasurfaceSourceAdapter";
    public static final String FOR_MEDIASURFACE_TARGET_ADAPTER = "ForMediasurfaceTargetAdapter";

    public static final String FOR_CATALYST_SOURCE_ADAPTER = "ForCatalystSourceAdapter";
    public static final String FOR_CATALYST_TARGET_ADAPTER = "ForCatalystTargetAdapter";

    public static final String FOR_SERVICEWARE_SOURCE_ADAPTER = "ForServiceWareSourceAdapter";
    public static final String FOR_SERVICEWARE_TARGET_ADAPTER = "ForServiceWareTargetAdapter";

    public static final String FOR_DOCUMENTUM_SOURCE_ADAPTER = "ForDocumentumSourceAdapter";
    public static final String FOR_DOCUMENTUM_TARGET_ADAPTER = "ForDocumentumTargetAdapter";

    public static final String TOPIC_PREFIX_JBOSS = "topic/";
    public static final String QUEUE_PREFIX_JBOSS = "queue/";

    public static final String FOR_ADOBE_SOURCE_ADAPTER = "ForAdobeSourceAdapter";
    public static final String FOR_ADOBE_TARGET_ADAPTER = "ForAdobeTargetAdapter";
    
    public static final String FOR_OPENOFFICE_SOURCE_ADAPTER = "ForOpenOfficeSourceAdapter";
    public static final String FOR_OPENOFFICE_TARGET_ADAPTER = "ForOpenOfficeTargetAdapter";
    
    public static final String FOR_IDML_SOURCE_ADAPTER = "ForIdmlSourceAdapter";
    public static final String FOR_IDML_TARGET_ADAPTER = "ForIdmlTargetAdapter";

    //////////////////////////////////////
    // Private Members                  //
    //////////////////////////////////////
    private static EventTopicMap s_instance = new EventTopicMap();
    private HashMap m_map = new HashMap(25);

    /**
     * Constructor. Creates an EventTopicMap object and fills it with all the known
     * event-topic mappings.
     */
    private EventTopicMap()
    {
        //events for the FileSystemSource Adapter
        fillMap(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT, FOR_FILE_SYSTEM_SOURCE_ADAPTER);
        fillMap(CxeMessageType.FILE_SYSTEM_EXPORT_EVENT, FOR_FILE_SYSTEM_TARGET_ADAPTER);

        //events for the extractor
        fillMap(CxeMessageType.HTML_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.XML_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.PRSXML_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.RTF_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.XPTAG_IMPORTED_EVENT, FOR_EXTRACTOR);


        //events for the merger
        fillMap(CxeMessageType.HTML_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.XML_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.PRSXML_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.RTF_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.XPTAG_LOCALIZED_EVENT, FOR_MERGER);

        //CAP-CXE related
        fillMap(CxeMessageType.GXML_CREATED_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.GXML_LOCALIZED_EVENT, FOR_CAP_SOURCE_ADAPTER);
        fillMap(CxeMessageType.CAP_EXPORT_STATUS_EVENT, FOR_CAP_EXPORT_LISTENER);
        fillMap(CxeMessageType.CXE_EXPORT_STATUS_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.CXE_IMPORT_ERROR_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.CAP_IMPORT_ERROR_EVENT, FOR_CAP_EXPORT_LISTENER);

        //unextracted goes directly to the cap target adapter or the cap source adapter
        fillMap(CxeMessageType.UNEXTRACTED_IMPORTED_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT, FOR_CAP_SOURCE_ADAPTER);

        //handle STF creation event
        fillMap(CxeMessageType.STF_CREATION_EVENT, FOR_CAP_TARGET_ADAPTER);

        //handle MS office events
        fillMap(CxeMessageType.MSOFFICE_IMPORTED_EVENT, FOR_MSOFFICE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.MSOFFICE_LOCALIZED_EVENT, FOR_MSOFFICE_TARGET_ADAPTER);

        //handle PDF events
        fillMap(CxeMessageType.PDF_IMPORTED_EVENT, FOR_PDF_SOURCE_ADAPTER);
        fillMap(CxeMessageType.PDF_LOCALIZED_EVENT, FOR_PDF_TARGET_ADAPTER);

        //handle Quark and Frame events
        fillMap(CxeMessageType.QUARK_IMPORTED_EVENT, FOR_QUARKFRAME_SOURCE_ADAPTER);
        fillMap(CxeMessageType.FRAME_IMPORTED_EVENT, FOR_QUARKFRAME_SOURCE_ADAPTER);
        fillMap(CxeMessageType.QUARK_LOCALIZED_EVENT, FOR_QUARKFRAME_TARGET_ADAPTER);
        fillMap(CxeMessageType.FRAME_LOCALIZED_EVENT, FOR_QUARKFRAME_TARGET_ADAPTER);
        fillMap(CxeMessageType.COPYFLOW_IMPORTED_EVENT, FOR_COPYFLOW_SOURCE_ADAPTER);
        fillMap(CxeMessageType.COPYFLOW_LOCALIZED_EVENT, FOR_COPYFLOW_TARGET_ADAPTER);

        //teamsite events
        fillMap(CxeMessageType.TEAMSITE_FILE_SELECTED_EVENT, FOR_TEAMSITE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.TEAMSITE_JOB_STATUS_EVENT, FOR_TEAMSITE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.TEAMSITE_IGNORE_EVENT, FOR_TEAMSITE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.TEAMSITE_EXPORT_EVENT, FOR_TEAMSITE_TARGET_ADAPTER);

        //vignette
        fillMap(CxeMessageType.VIGNETTE_FILE_SELECTED_EVENT, FOR_VIGNETTE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.VIGNETTE_EXPORT_EVENT, FOR_VIGNETTE_TARGET_ADAPTER);

        //database
        fillMap(CxeMessageType.DATABASE_EXPORT_EVENT, FOR_DATABASE_TARGET_ADAPTER);

        //dynamic preview
        fillMap(CxeMessageType.DYNAMIC_PREVIEW_EVENT, FOR_DYNAMIC_PREVIEW);

        //mediasurface
        fillMap(CxeMessageType.MEDIASURFACE_FILE_SELECTED_EVENT, FOR_MEDIASURFACE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.MEDIASURFACE_EXPORT_EVENT, FOR_MEDIASURFACE_TARGET_ADAPTER);

        //catalyst
        fillMap(CxeMessageType.CATALYST_IMPORTED_EVENT, FOR_CATALYST_SOURCE_ADAPTER);
        fillMap(CxeMessageType.CATALYST_LOCALIZED_EVENT, FOR_CATALYST_TARGET_ADAPTER);

        //serviceware
        fillMap(CxeMessageType.SERVICEWARE_FILE_SELECTED_EVENT, FOR_SERVICEWARE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.SERVICEWARE_EXPORT_EVENT, FOR_SERVICEWARE_TARGET_ADAPTER);

        //documentum
        fillMap(CxeMessageType.DOCUMENTUM_FILE_SELECTED_EVENT, FOR_DOCUMENTUM_SOURCE_ADAPTER);
        fillMap(CxeMessageType.DOCUMENTUM_EXPORT_EVENT, FOR_DOCUMENTUM_TARGET_ADAPTER);

        //adobe
        fillMap(CxeMessageType.ADOBE_IMPORTED_EVENT, FOR_ADOBE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.ADOBE_LOCALIZED_EVENT, FOR_ADOBE_TARGET_ADAPTER);
        
        //open office
        fillMap(CxeMessageType.OPENOFFICE_IMPORTED_EVENT, FOR_OPENOFFICE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.OPENOFFICE_LOCALIZED_EVENT, FOR_OPENOFFICE_TARGET_ADAPTER);
        
        //idml
        fillMap(CxeMessageType.IDML_IMPORTED_EVENT, FOR_IDML_SOURCE_ADAPTER);
        fillMap(CxeMessageType.IDML_LOCALIZED_EVENT, FOR_IDML_TARGET_ADAPTER);
    }

    /**
     * Fills the hashmap by mapping event values to JMS topic names.
     *
     * @param p_eventName event name
     * @param p_topicName
     */
    private void fillMap(int p_eventValue, String p_topicName)
    {
        Integer eventValue = new Integer(
            CxeMessageType.getCxeMessageType(p_eventValue).getValue());
        String topicName = JMS_PREFIX + p_topicName;
        m_map.put(eventValue,topicName);
    }


    //////////////////////////////////////
    // Public Methods                    //
    //////////////////////////////////////

    /**
     * Returns the JMS topic to which this event should be sent.
     *
     * @param p_cxeMessageType the CXE Message Type (Event Type)
     * @return JMS Topic Name as a String
     */
    public static String getJmsTopicName(CxeMessageType p_cxeMessageType)
    {
        String topicName = (String)s_instance.m_map.get(
            new Integer(p_cxeMessageType.getValue()));

        if (topicName == null)
        {
            throw new java.util.NoSuchElementException(
                "There is no mapped topic for event " +
                p_cxeMessageType.getName());
        }
        
        // This is JBOSS specified, when jboss bind the JMS destination(Queue or Topic) in to naming context,
        // it will automatically add the prefix "topic/queue" ahead of the JNDI name, such as: 
        // "topic/com.globalsight.cxe.jms.ForExtractor".  
        // So if the GlobalSight works on JBOSS, we need add the prefix manually when lookup the JMS destination.
        AppServerWrapper s_appServerWrapper = AppServerWrapperFactory.getAppServerWrapper();
        if (s_appServerWrapper.getJ2EEServerName().equals(AppServerWrapperFactory.JBOSS)) 
        {
            topicName = EventTopicMap.TOPIC_PREFIX_JBOSS + topicName;
        }

        return topicName;
    }
}