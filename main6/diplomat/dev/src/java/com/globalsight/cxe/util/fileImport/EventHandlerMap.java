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
package com.globalsight.cxe.util.fileImport;

import java.util.HashMap;

import com.globalsight.cxe.adaptermdb.BaseAdapterMDB;
import com.globalsight.cxe.adaptermdb.adobe.AdobeSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.cap.CapTargetAdapterMDB;
import com.globalsight.cxe.adaptermdb.idml.IdmlSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.ling.ExtractorMDB;
import com.globalsight.cxe.adaptermdb.msoffice.MsOfficeSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.openoffice.OpenOfficeSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.passolo.PassoloSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.quarkframe.QuarkFrameSourceAdapterMDB;
import com.globalsight.cxe.adaptermdb.windowspe.WindowsPESourceAdapterMDB;

/**
 * The EventTopicMap contains knowledge of how CXE Event Names map to JMS
 * topics. This is used by the adapter message proxies (message driven beans).
 */
public class EventHandlerMap
{
    private static HashMap<String, BaseAdapterMDB> map = new HashMap<String, BaseAdapterMDB>();
    static
    {
        map.put("HTML_IMPORTED_EVENT", new ExtractorMDB());
        map.put("XML_IMPORTED_EVENT", new ExtractorMDB());
        map.put("MSOFFICE_IMPORTED_EVENT", new MsOfficeSourceAdapterMDB());
        map.put("UNEXTRACTED_IMPORTED_EVENT", new CapTargetAdapterMDB());
        map.put("ADOBE_IMPORTED_EVENT", new AdobeSourceAdapterMDB());
        map.put("OPENOFFICE_IMPORTED_EVENT", new OpenOfficeSourceAdapterMDB());
        map.put("IDML_IMPORTED_EVENT", new IdmlSourceAdapterMDB());
        map.put("MIF_IMPORTED_EVENT", new ExtractorMDB());
        map.put("FRAME_IMPORTED_EVENT", new QuarkFrameSourceAdapterMDB());
        map.put("PASSOLO_IMPORTED_EVENT", new PassoloSourceAdapterMDB());
        map.put("WINPE_IMPORTED_EVENT", new WindowsPESourceAdapterMDB());
    }

    public static BaseAdapterMDB getHandler(String name)
    {
        return map.get(name);
    }
}