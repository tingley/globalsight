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

import com.globalsight.cxe.adaptermdb.adobe.AdobeSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.adobe.AdobeTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.cap.CapSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.cap.CapTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.catalyst.CatalystSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.catalyst.CatalystTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.copyflow.CopyFlowSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.copyflow.CopyFlowTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.database.DatabaseTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.documentum.DocumentumSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.documentum.DocumentumTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.idml.IdmlSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.idml.IdmlTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.ling.ExtractorMDB;
import com.globalsight.cxe.adaptermdb.ling.MergerMDB;
import com.globalsight.cxe.adaptermdb.mediasurface.MediasurfaceSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.mediasurface.MediasurfaceTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.msoffice.MsOfficeSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.msoffice.MsOfficeTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.openoffice.OpenOfficeSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.openoffice.OpenOfficeTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.passolo.PassoloSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.passolo.PassoloTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.pdf.PdfSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.pdf.PdfTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.quarkframe.QuarkFrameSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.quarkframe.QuarkFrameTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.serviceware.ServiceWareSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.serviceware.ServiceWareTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.vignette.VignetteSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.vignette.VignetteTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.windowspe.WindowsPESourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.windowspe.WindowsPETargetAdapterMDB;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * The EventTopicMap contains knowledge of how CXE Event Names map to JMS
 * topics. This is used by the adapter message proxies (message driven beans).
 */
public class EventTopicMap
{
    public static final String JMS_PREFIX = "com.globalsight.cxe.jms.";
    public static final String FOR_FILE_SYSTEM_SOURCE_ADAPTER = "ForFileSystemSourceAdapter";
    public static final String FOR_FILE_SYSTEM_TARGET_ADAPTER = "ForFileSystemTargetAdapter";
    public static final String FOR_EXTRACTOR = "ForExtractor";
    public static final String FOR_MERGER = "ForMerger";
    public static final String FOR_CAP_TARGET_ADAPTER = "ForCapTargetAdapter";
    public static final String FOR_CAP_SOURCE_ADAPTER = "ForCapSourceAdapter";
    public static final String FOR_CAP_EXPORT_LISTENER = "ForCapExportListener";
    public static final String FOR_VIGNETTE_SOURCE_ADAPTER = "ForVignetteSourceAdapter";
    public static final String FOR_VIGNETTE_TARGET_ADAPTER = "ForVignetteTargetAdapter";
    public static final String FOR_DATABASE_TARGET_ADAPTER = "ForDatabaseTargetAdapter";
    public static final String FOR_MSOFFICE_SOURCE_ADAPTER = "ForMsOfficeSourceAdapter";
    public static final String FOR_MSOFFICE_TARGET_ADAPTER = "ForMsOfficeTargetAdapter";
    public static final String FOR_PDF_SOURCE_ADAPTER = "ForPdfSourceAdapter";
    public static final String FOR_PDF_TARGET_ADAPTER = "ForPdfTargetAdapter";
    public static final String FOR_QUARKFRAME_SOURCE_ADAPTER = "ForQuarkFrameSourceAdapter";
    public static final String FOR_QUARKFRAME_TARGET_ADAPTER = "ForQuarkFrameTargetAdapter";
    public static final String FOR_COPYFLOW_SOURCE_ADAPTER = "ForCopyFlowSourceAdapter";
    public static final String FOR_COPYFLOW_TARGET_ADAPTER = "ForCopyFlowTargetAdapter";
    public static final String FOR_DYNAMIC_PREVIEW = "ForDynamicPreview";
    public static final String FOR_TERM_AUDIT_LOG = "ForTermAuditLog";
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

    public static final String FOR_PASSOLO_SOURCE_ADAPTER = "ForPassoloSourceAdapter";
    public static final String FOR_PASSOLO_TARGET_ADAPTER = "ForPassoloTargetAdapter";

    public static final String FOR_WINPE_SOURCE_ADAPTER = "ForWindowsPESourceAdapter";
    public static final String FOR_WINPE_TARGET_ADAPTER = "ForWindowsPETargetAdapter";

    // ////////////////////////////////////
    // Private Members //
    // ////////////////////////////////////
    private static EventTopicMap s_instance = new EventTopicMap();
    private HashMap m_map = new HashMap(25);

    private HashMap m_classMap = new HashMap(25);

    /**
     * Constructor. Creates an EventTopicMap object and fills it with all the
     * known event-topic mappings.
     */
    private EventTopicMap()
    {
        // events for the FileSystemSource Adapter
        fillMap(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT,
                FOR_FILE_SYSTEM_SOURCE_ADAPTER);
        fillMap(CxeMessageType.FILE_SYSTEM_EXPORT_EVENT,
                FOR_FILE_SYSTEM_TARGET_ADAPTER);

        // events for the extractor
        fillMap(CxeMessageType.HTML_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.XML_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.PRSXML_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.RTF_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.XPTAG_IMPORTED_EVENT, FOR_EXTRACTOR);

        // events for the merger
        fillMap(CxeMessageType.HTML_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.XML_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.PRSXML_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.RTF_LOCALIZED_EVENT, FOR_MERGER);
        fillMap(CxeMessageType.XPTAG_LOCALIZED_EVENT, FOR_MERGER);

        // CAP-CXE related
        fillMap(CxeMessageType.GXML_CREATED_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.GXML_LOCALIZED_EVENT, FOR_CAP_SOURCE_ADAPTER);
        fillMap(CxeMessageType.CAP_EXPORT_STATUS_EVENT, FOR_CAP_EXPORT_LISTENER);
        fillMap(CxeMessageType.CXE_EXPORT_STATUS_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.CXE_IMPORT_ERROR_EVENT, FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.CAP_IMPORT_ERROR_EVENT, FOR_CAP_EXPORT_LISTENER);

        // unextracted goes directly to the cap target adapter or the cap source
        // adapter
        fillMap(CxeMessageType.UNEXTRACTED_IMPORTED_EVENT,
                FOR_CAP_TARGET_ADAPTER);
        fillMap(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT,
                FOR_CAP_SOURCE_ADAPTER);

        // handle STF creation event
        fillMap(CxeMessageType.STF_CREATION_EVENT, FOR_CAP_TARGET_ADAPTER);

        // handle MS office events
        fillMap(CxeMessageType.MSOFFICE_IMPORTED_EVENT,
                FOR_MSOFFICE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.MSOFFICE_LOCALIZED_EVENT,
                FOR_MSOFFICE_TARGET_ADAPTER);

        // handle PDF events
        fillMap(CxeMessageType.PDF_IMPORTED_EVENT, FOR_PDF_SOURCE_ADAPTER);
        fillMap(CxeMessageType.PDF_LOCALIZED_EVENT, FOR_PDF_TARGET_ADAPTER);

        // handle Quark and Frame events
        fillMap(CxeMessageType.QUARK_IMPORTED_EVENT,
                FOR_QUARKFRAME_SOURCE_ADAPTER);
        fillMap(CxeMessageType.FRAME_IMPORTED_EVENT,
                FOR_QUARKFRAME_SOURCE_ADAPTER);
        fillMap(CxeMessageType.QUARK_LOCALIZED_EVENT,
                FOR_QUARKFRAME_TARGET_ADAPTER);
        fillMap(CxeMessageType.FRAME_LOCALIZED_EVENT,
                FOR_QUARKFRAME_TARGET_ADAPTER);
        fillMap(CxeMessageType.COPYFLOW_IMPORTED_EVENT,
                FOR_COPYFLOW_SOURCE_ADAPTER);
        fillMap(CxeMessageType.COPYFLOW_LOCALIZED_EVENT,
                FOR_COPYFLOW_TARGET_ADAPTER);

        // vignette
        fillMap(CxeMessageType.VIGNETTE_FILE_SELECTED_EVENT,
                FOR_VIGNETTE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.VIGNETTE_EXPORT_EVENT,
                FOR_VIGNETTE_TARGET_ADAPTER);

        // database
        fillMap(CxeMessageType.DATABASE_EXPORT_EVENT,
                FOR_DATABASE_TARGET_ADAPTER);

        // dynamic preview
        fillMap(CxeMessageType.DYNAMIC_PREVIEW_EVENT, FOR_DYNAMIC_PREVIEW);

        // mediasurface
        fillMap(CxeMessageType.MEDIASURFACE_FILE_SELECTED_EVENT,
                FOR_MEDIASURFACE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.MEDIASURFACE_EXPORT_EVENT,
                FOR_MEDIASURFACE_TARGET_ADAPTER);

        // catalyst
        fillMap(CxeMessageType.CATALYST_IMPORTED_EVENT,
                FOR_CATALYST_SOURCE_ADAPTER);
        fillMap(CxeMessageType.CATALYST_LOCALIZED_EVENT,
                FOR_CATALYST_TARGET_ADAPTER);

        // serviceware
        fillMap(CxeMessageType.SERVICEWARE_FILE_SELECTED_EVENT,
                FOR_SERVICEWARE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.SERVICEWARE_EXPORT_EVENT,
                FOR_SERVICEWARE_TARGET_ADAPTER);

        // documentum
        fillMap(CxeMessageType.DOCUMENTUM_FILE_SELECTED_EVENT,
                FOR_DOCUMENTUM_SOURCE_ADAPTER);
        fillMap(CxeMessageType.DOCUMENTUM_EXPORT_EVENT,
                FOR_DOCUMENTUM_TARGET_ADAPTER);

        // adobe
        fillMap(CxeMessageType.ADOBE_IMPORTED_EVENT, FOR_ADOBE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.ADOBE_LOCALIZED_EVENT, FOR_ADOBE_TARGET_ADAPTER);

        // open office
        fillMap(CxeMessageType.OPENOFFICE_IMPORTED_EVENT,
                FOR_OPENOFFICE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.OPENOFFICE_LOCALIZED_EVENT,
                FOR_OPENOFFICE_TARGET_ADAPTER);

        // idml
        fillMap(CxeMessageType.IDML_IMPORTED_EVENT, FOR_IDML_SOURCE_ADAPTER);
        fillMap(CxeMessageType.IDML_LOCALIZED_EVENT, FOR_IDML_TARGET_ADAPTER);

        // mif
        fillMap(CxeMessageType.MIF_IMPORTED_EVENT, FOR_EXTRACTOR);
        fillMap(CxeMessageType.MIF_LOCALIZED_EVENT, FOR_MERGER);

        // passolo
        fillMap(CxeMessageType.PASSOLO_IMPORTED_EVENT,
                FOR_PASSOLO_SOURCE_ADAPTER);
        fillMap(CxeMessageType.PASSOLO_LOCALIZED_EVENT,
                FOR_PASSOLO_TARGET_ADAPTER);

        // WINDOWS PE
        fillMap(CxeMessageType.WINPE_IMPORTED_EVENT, FOR_WINPE_SOURCE_ADAPTER);
        fillMap(CxeMessageType.WINPE_LOCALIZED_EVENT, FOR_WINPE_TARGET_ADAPTER);

        fillClassMap();
    }

    /**
     * Constructor. Creates an EventTopicMap object and fills it with all the
     * known event-topic mappings MDB.
     */
    @SuppressWarnings("unchecked")
    private void fillClassMap()
    {
        // events for the FileSystemSource Adapter
        m_classMap.put(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT,
                new FileSystemSourceAdapterMDB());
        m_classMap.put(CxeMessageType.FILE_SYSTEM_EXPORT_EVENT,
                new FileSystemTargetAdapterMDB());

        // events for the extractor 
        m_classMap.put(CxeMessageType.HTML_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.XML_IMPORTED_EVENT,  new ExtractorMDB());
        m_classMap.put(CxeMessageType.PRSXML_IMPORTED_EVENT,  new ExtractorMDB());
        m_classMap.put(CxeMessageType.RTF_IMPORTED_EVENT,  new ExtractorMDB());
        m_classMap.put(CxeMessageType.XPTAG_IMPORTED_EVENT,  new ExtractorMDB());

        // events for the merger
        m_classMap.put(CxeMessageType.HTML_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.XML_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.PRSXML_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.RTF_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.XPTAG_LOCALIZED_EVENT, new MergerMDB());

        // CAP-CXE related
        m_classMap.put(CxeMessageType.GXML_CREATED_EVENT, new CapTargetAdapterMDB());
        m_classMap.put(CxeMessageType.GXML_LOCALIZED_EVENT, new CapSourceAdapterMDB());
//        m_classMap.put(CxeMessageType.CAP_EXPORT_STATUS_EVENT, FOR_CAP_EXPORT_LISTENER);
        m_classMap.put(CxeMessageType.CXE_EXPORT_STATUS_EVENT, new CapTargetAdapterMDB());
        m_classMap.put(CxeMessageType.CXE_IMPORT_ERROR_EVENT, new CapTargetAdapterMDB());
//        m_classMap.put(CxeMessageType.CAP_IMPORT_ERROR_EVENT, FOR_CAP_EXPORT_LISTENER);

        // unextracted goes directly to the cap target adapter or the cap source
        // adapter
        m_classMap.put(CxeMessageType.UNEXTRACTED_IMPORTED_EVENT,
                new CapTargetAdapterMDB());
        m_classMap.put(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT,
                new CapSourceAdapterMDB());

        // handle STF creation event
        m_classMap.put(CxeMessageType.STF_CREATION_EVENT, new CapTargetAdapterMDB());

        // handle MS office events
        m_classMap.put(CxeMessageType.MSOFFICE_IMPORTED_EVENT,
                new MsOfficeSourceAdapterMDB());
        m_classMap.put(CxeMessageType.MSOFFICE_LOCALIZED_EVENT,
                new MsOfficeTargetAdapterMDB());

        // handle PDF events
        m_classMap.put(CxeMessageType.PDF_IMPORTED_EVENT, new PdfSourceAdapterMDB());
        m_classMap.put(CxeMessageType.PDF_LOCALIZED_EVENT, new PdfTargetAdapterMDB());

        // handle Quark and Frame events
        m_classMap.put(CxeMessageType.QUARK_IMPORTED_EVENT,
                new QuarkFrameSourceAdapterMDB());
        m_classMap.put(CxeMessageType.FRAME_IMPORTED_EVENT,
                new QuarkFrameSourceAdapterMDB());
        m_classMap.put(CxeMessageType.QUARK_LOCALIZED_EVENT,
                new QuarkFrameTargetAdapterMDB());
        m_classMap.put(CxeMessageType.FRAME_LOCALIZED_EVENT,
                new QuarkFrameTargetAdapterMDB());
        m_classMap.put(CxeMessageType.COPYFLOW_IMPORTED_EVENT,
                new CopyFlowSourceAdapterMDB());
        m_classMap.put(CxeMessageType.COPYFLOW_LOCALIZED_EVENT,
                new CopyFlowTargetAdapterMDB());

        // vignette
        m_classMap.put(CxeMessageType.VIGNETTE_FILE_SELECTED_EVENT,
                new VignetteSourceAdapterMDB());
        m_classMap.put(CxeMessageType.VIGNETTE_EXPORT_EVENT,
                new VignetteTargetAdapterMDB());

        // database
        m_classMap.put(CxeMessageType.DATABASE_EXPORT_EVENT,
                new DatabaseTargetAdapterMDB());

        // dynamic preview
//        m_classMap.put(CxeMessageType.DYNAMIC_PREVIEW_EVENT, FOR_DYNAMIC_PREVIEW);

        // mediasurface
        m_classMap.put(CxeMessageType.MEDIASURFACE_FILE_SELECTED_EVENT,
                new MediasurfaceSourceAdapterMDB());
        m_classMap.put(CxeMessageType.MEDIASURFACE_EXPORT_EVENT,
                new MediasurfaceTargetAdapterMDB());

        // catalyst
        m_classMap.put(CxeMessageType.CATALYST_IMPORTED_EVENT,
                new CatalystSourceAdapterMDB());
        m_classMap.put(CxeMessageType.CATALYST_LOCALIZED_EVENT,
                new CatalystTargetAdapterMDB());

        // serviceware
        m_classMap.put(CxeMessageType.SERVICEWARE_FILE_SELECTED_EVENT,
                new ServiceWareSourceAdapterMDB());
        m_classMap.put(CxeMessageType.SERVICEWARE_EXPORT_EVENT,
                new ServiceWareTargetAdapterMDB());

        // documentum
        m_classMap.put(CxeMessageType.DOCUMENTUM_FILE_SELECTED_EVENT,
                new DocumentumSourceAdapterMDB());
        m_classMap.put(CxeMessageType.DOCUMENTUM_EXPORT_EVENT,
                new DocumentumTargetAdapterMDB());

        // adobe
        m_classMap.put(CxeMessageType.ADOBE_IMPORTED_EVENT, new AdobeSourceAdapterMDB());
        m_classMap.put(CxeMessageType.ADOBE_LOCALIZED_EVENT, new AdobeTargetAdapterMDB());

        // open office
        m_classMap.put(CxeMessageType.OPENOFFICE_IMPORTED_EVENT,
                new OpenOfficeSourceAdapterMDB());
        m_classMap.put(CxeMessageType.OPENOFFICE_LOCALIZED_EVENT,
                new OpenOfficeTargetAdapterMDB());

        // idml
        m_classMap.put(CxeMessageType.IDML_IMPORTED_EVENT, new IdmlSourceAdapterMDB());
        m_classMap.put(CxeMessageType.IDML_LOCALIZED_EVENT, new IdmlTargetAdapterMDB());

        // mif
        m_classMap.put(CxeMessageType.MIF_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.MIF_LOCALIZED_EVENT, new MergerMDB());

        // passolo
        m_classMap.put(CxeMessageType.PASSOLO_IMPORTED_EVENT,
                new PassoloSourceAdapterMDB());
        m_classMap.put(CxeMessageType.PASSOLO_LOCALIZED_EVENT,
                new PassoloTargetAdapterMDB());

        // WINDOWS PE
        m_classMap.put(CxeMessageType.WINPE_IMPORTED_EVENT, new WindowsPESourceAdapterMDB());
        m_classMap.put(CxeMessageType.WINPE_LOCALIZED_EVENT, new WindowsPETargetAdapterMDB());
    }

    /**
     * Fills the hashmap by mapping event values to JMS topic names.
     * 
     * @param p_eventName
     *            event name
     * @param p_topicName
     */
    private void fillMap(int p_eventValue, String p_topicName)
    {
        Integer eventValue = new Integer(CxeMessageType.getCxeMessageType(
                p_eventValue).getValue());
        String topicName = JMS_PREFIX + p_topicName;
        m_map.put(eventValue, topicName);
    }
    
    public static BaseAdapterMDB getBaseAdapterMDB(CxeMessageType p_cxeMessageType)
    {
        BaseAdapterMDB mdb = (BaseAdapterMDB) s_instance.m_classMap.get(new Integer(
                p_cxeMessageType.getValue()));

        if (mdb == null)
        {
            throw new java.util.NoSuchElementException(
                    "There is no mapped base adapter MDB for event "
                            + p_cxeMessageType.getName());
        }
        
         return mdb;
    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Returns the JMS topic to which this event should be sent.
     * 
     * @param p_cxeMessageType
     *            the CXE Message Type (Event Type)
     * @return JMS Topic Name as a String
     */
    public static String getJmsQueueName(CxeMessageType p_cxeMessageType)
    {
        String jmsName = (String) s_instance.m_map.get(new Integer(
                p_cxeMessageType.getValue()));

        if (jmsName == null)
        {
            throw new java.util.NoSuchElementException(
                    "There is no mapped topic for event "
                            + p_cxeMessageType.getName());
        }

        // This is JBOSS specified, when jboss bind the JMS destination(Queue or
        // Topic) in to naming context,
        // it will automatically add the prefix "topic/queue" ahead of the JNDI
        // name, such as:
        // "topic/com.globalsight.cxe.jms.ForExtractor".
        // So if the GlobalSight works on JBOSS, we need add the prefix manually
        // when lookup the JMS destination.
        AppServerWrapper s_appServerWrapper = AppServerWrapperFactory
                .getAppServerWrapper();
        if (s_appServerWrapper.getJ2EEServerName().equals(
                AppServerWrapperFactory.JBOSS))
        {
            jmsName = EventTopicMap.QUEUE_PREFIX_JBOSS + jmsName;
        }

        return jmsName;
    }
}