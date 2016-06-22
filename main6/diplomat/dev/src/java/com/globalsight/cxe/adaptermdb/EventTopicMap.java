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

public class EventTopicMap
{
    private static EventTopicMap s_instance = new EventTopicMap();

    private HashMap<Integer, BaseAdapterMDB> m_classMap = new HashMap<Integer, BaseAdapterMDB>(25);

    private EventTopicMap()
    {
        fillClassMap();
    }

    private void fillClassMap()
    {
        // events for the FileSystemSource Adapter
        m_classMap.put(CxeMessageType.FILE_SYSTEM_FILE_SELECTED_EVENT,
                new FileSystemSourceAdapterMDB());
        m_classMap.put(CxeMessageType.FILE_SYSTEM_EXPORT_EVENT, new FileSystemTargetAdapterMDB());

        // events for the extractor
        m_classMap.put(CxeMessageType.HTML_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.XML_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.PRSXML_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.RTF_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.XPTAG_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.JSON_IMPORTED_EVENT, new ExtractorMDB());

        // events for the merger
        m_classMap.put(CxeMessageType.HTML_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.XML_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.PRSXML_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.RTF_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.XPTAG_LOCALIZED_EVENT, new MergerMDB());
        m_classMap.put(CxeMessageType.JSON_LOCALIZED_EVENT, new MergerMDB());

        // CAP-CXE related
        m_classMap.put(CxeMessageType.GXML_CREATED_EVENT, new CapTargetAdapterMDB());
        m_classMap.put(CxeMessageType.GXML_LOCALIZED_EVENT, new CapSourceAdapterMDB());
        // m_classMap.put(CxeMessageType.CAP_EXPORT_STATUS_EVENT,
        // FOR_CAP_EXPORT_LISTENER);
        m_classMap.put(CxeMessageType.CXE_EXPORT_STATUS_EVENT, new CapTargetAdapterMDB());
        m_classMap.put(CxeMessageType.CXE_IMPORT_ERROR_EVENT, new CapTargetAdapterMDB());
        // m_classMap.put(CxeMessageType.CAP_IMPORT_ERROR_EVENT,
        // FOR_CAP_EXPORT_LISTENER);

        // unextracted goes directly to the cap target adapter or the cap source
        // adapter
        m_classMap.put(CxeMessageType.UNEXTRACTED_IMPORTED_EVENT, new CapTargetAdapterMDB());
        m_classMap.put(CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT, new CapSourceAdapterMDB());

        // handle STF creation event
        m_classMap.put(CxeMessageType.STF_CREATION_EVENT, new CapTargetAdapterMDB());

        // handle MS office events
        m_classMap.put(CxeMessageType.MSOFFICE_IMPORTED_EVENT, new MsOfficeSourceAdapterMDB());
        m_classMap.put(CxeMessageType.MSOFFICE_LOCALIZED_EVENT, new MsOfficeTargetAdapterMDB());

        // handle PDF events
        m_classMap.put(CxeMessageType.PDF_IMPORTED_EVENT, new PdfSourceAdapterMDB());
        m_classMap.put(CxeMessageType.PDF_LOCALIZED_EVENT, new PdfTargetAdapterMDB());

        // handle Quark and Frame events
        m_classMap.put(CxeMessageType.QUARK_IMPORTED_EVENT, new QuarkFrameSourceAdapterMDB());
        m_classMap.put(CxeMessageType.FRAME_IMPORTED_EVENT, new QuarkFrameSourceAdapterMDB());
        m_classMap.put(CxeMessageType.QUARK_LOCALIZED_EVENT, new QuarkFrameTargetAdapterMDB());
        m_classMap.put(CxeMessageType.FRAME_LOCALIZED_EVENT, new QuarkFrameTargetAdapterMDB());
        m_classMap.put(CxeMessageType.COPYFLOW_IMPORTED_EVENT, new CopyFlowSourceAdapterMDB());
        m_classMap.put(CxeMessageType.COPYFLOW_LOCALIZED_EVENT, new CopyFlowTargetAdapterMDB());

        // vignette
        m_classMap.put(CxeMessageType.VIGNETTE_FILE_SELECTED_EVENT, new VignetteSourceAdapterMDB());
        m_classMap.put(CxeMessageType.VIGNETTE_EXPORT_EVENT, new VignetteTargetAdapterMDB());

        // database
        m_classMap.put(CxeMessageType.DATABASE_EXPORT_EVENT, new DatabaseTargetAdapterMDB());

        // dynamic preview
        // m_classMap.put(CxeMessageType.DYNAMIC_PREVIEW_EVENT,
        // FOR_DYNAMIC_PREVIEW);

        // mediasurface
        m_classMap.put(CxeMessageType.MEDIASURFACE_FILE_SELECTED_EVENT,
                new MediasurfaceSourceAdapterMDB());
        m_classMap.put(CxeMessageType.MEDIASURFACE_EXPORT_EVENT,
                new MediasurfaceTargetAdapterMDB());

        // catalyst
        m_classMap.put(CxeMessageType.CATALYST_IMPORTED_EVENT, new CatalystSourceAdapterMDB());
        m_classMap.put(CxeMessageType.CATALYST_LOCALIZED_EVENT, new CatalystTargetAdapterMDB());

        // serviceware
        m_classMap.put(CxeMessageType.SERVICEWARE_FILE_SELECTED_EVENT,
                new ServiceWareSourceAdapterMDB());
        m_classMap.put(CxeMessageType.SERVICEWARE_EXPORT_EVENT, new ServiceWareTargetAdapterMDB());

        // documentum
        m_classMap.put(CxeMessageType.DOCUMENTUM_FILE_SELECTED_EVENT,
                new DocumentumSourceAdapterMDB());
        m_classMap.put(CxeMessageType.DOCUMENTUM_EXPORT_EVENT, new DocumentumTargetAdapterMDB());

        // adobe
        m_classMap.put(CxeMessageType.ADOBE_IMPORTED_EVENT, new AdobeSourceAdapterMDB());
        m_classMap.put(CxeMessageType.ADOBE_LOCALIZED_EVENT, new AdobeTargetAdapterMDB());

        // open office
        m_classMap.put(CxeMessageType.OPENOFFICE_IMPORTED_EVENT, new OpenOfficeSourceAdapterMDB());
        m_classMap.put(CxeMessageType.OPENOFFICE_LOCALIZED_EVENT, new OpenOfficeTargetAdapterMDB());

        // idml
        m_classMap.put(CxeMessageType.IDML_IMPORTED_EVENT, new IdmlSourceAdapterMDB());
        m_classMap.put(CxeMessageType.IDML_LOCALIZED_EVENT, new IdmlTargetAdapterMDB());

        // mif
        m_classMap.put(CxeMessageType.MIF_IMPORTED_EVENT, new ExtractorMDB());
        m_classMap.put(CxeMessageType.MIF_LOCALIZED_EVENT, new MergerMDB());

        // passolo
        m_classMap.put(CxeMessageType.PASSOLO_IMPORTED_EVENT, new PassoloSourceAdapterMDB());
        m_classMap.put(CxeMessageType.PASSOLO_LOCALIZED_EVENT, new PassoloTargetAdapterMDB());

        // WINDOWS PE
        m_classMap.put(CxeMessageType.WINPE_IMPORTED_EVENT, new WindowsPESourceAdapterMDB());
        m_classMap.put(CxeMessageType.WINPE_LOCALIZED_EVENT, new WindowsPETargetAdapterMDB());
    }

    public static BaseAdapterMDB getBaseAdapterMDB(CxeMessageType p_cxeMessageType)
    {
        BaseAdapterMDB mdb = s_instance.m_classMap.get(new Integer(p_cxeMessageType.getValue()));
        if (mdb == null)
        {
            throw new java.util.NoSuchElementException(
                    "There is no mapped base adapter MDB for event " + p_cxeMessageType.getName());
        }

        return mdb;
    }
}