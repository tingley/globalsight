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
package com.globalsight.util.modules;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.database.DatabaseAdapter;
import com.globalsight.cxe.adapter.mediasurface.MediasurfaceAdapter;
import com.globalsight.cxe.adapter.msoffice.MsOfficeAdapter;
import com.globalsight.cxe.adapter.pdf.PdfAdapter;
import com.globalsight.cxe.adapter.quarkframe.QuarkFrameAdapter;
import com.globalsight.cxe.adapter.vignette.VignetteAdapter;
import com.globalsight.cxe.adapter.serviceware.ServiceWareAdapter;
import com.globalsight.cxe.adapter.documentum.DocumentumAdapter;
import com.globalsight.everest.corpus.CorpusTm;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.vendormanagement.VendorManagementLocal;
import com.globalsight.calendar.CalendarManagerLocal;
import com.globalsight.webservices.Ambassador;
import com.globalsight.cxe.adapter.catalyst.CatalystAdapter;
import com.globalsight.everest.aligner.AlignerManagerLocal;

/**
 * The Modules class allows clients to check whether a particular
 * module/component is installed (has valid key).
 */
public class Modules
{
    private static Logger s_logger = Logger
            .getLogger(Modules.class);

    // Keeps track of whether certain modules are installed
    private static boolean s_cms, s_db, s_vignette, s_serviceware = false;

    private static boolean s_documentum = false;

    private static boolean s_quark, s_frame, s_pdf = false;

    private static boolean s_ms_doc, s_ms_ppt, s_ms_xls = false;

    private static boolean s_costing, s_reports, s_snippets = false;

    private static boolean s_l10nWebService, s_vmWebService = false;

    private static boolean s_vendorMgmt, s_corpus = false;

    private static boolean s_calendar = false;

    private static boolean s_catalyst = false;

    private static boolean s_corpusAligner = false;

    private static boolean s_customerAccessGroup = false;

    // determines up front what modules are installed. Throws an
    // IllegalStateException if we can't do this for some reason
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_cms = MediasurfaceAdapter.isInstalled();
            s_db = DatabaseAdapter.isInstalled();
            s_vignette = VignetteAdapter.isInstalled();
            s_pdf = PdfAdapter.isInstalled();
            s_quark = QuarkFrameAdapter.isQuarkInstalled();
            s_frame = QuarkFrameAdapter.isFrameInstalled();
            s_ms_doc = MsOfficeAdapter.isWordInstalled();
            s_ms_ppt = MsOfficeAdapter.isPowerPointInstalled();
            s_ms_xls = MsOfficeAdapter.isExcelInstalled();
            s_costing = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
            s_reports = sc
                    .getBooleanParameter(SystemConfigParamNames.REPORTS_ENABLED);
            s_snippets = sc
                    .getBooleanParameter(SystemConfigParamNames.ADD_DELETE_ENABLED);
            s_vendorMgmt = VendorManagementLocal.isInstalled();
            s_l10nWebService = Ambassador.isInstalled();
            s_vmWebService = s_vendorMgmt && s_l10nWebService;
            s_corpus = CorpusTm.isInstalled();
            s_calendar = CalendarManagerLocal.isInstalled();
            s_catalyst = CatalystAdapter.isInstalled();
            s_corpusAligner = AlignerManagerLocal.isInstalled();
            s_serviceware = ServiceWareAdapter.isInstalled();
            s_documentum = DocumentumAdapter.isInstalled();
            checkCustomerAccessGroup();
        }
        catch (Exception e)
        {
            String msg = "Failed to determine installed modules.";
            s_logger.error(msg, e);
            throw new IllegalStateException(msg);
        }
    }

    /**
     * Returns true if the CMS Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isCmsAdapterInstalled()
    {
        return s_cms;
    }

    /**
     * Returns true if the Database Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isDatabaseAdapterInstalled()
    {
        return s_db;
    }

    /**
     * Returns true if the Pdf Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isVignetteAdapterInstalled()
    {
        return VignetteAdapter.isInstalled();
    }

    /**
     * Returns true if the Pdf Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isPdfAdapterInstalled()
    {
        return s_pdf;
    }

    /**
     * Returns true if the Quark Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isQuarkInstalled()
    {
        return s_quark;
    }

    /**
     * Returns true if the Frame Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isFrameInstalled()
    {
        return s_frame;
    }

    /**
     * Returns true if the Word Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isWordAdapterInstalled()
    {
        return s_ms_doc;
    }

    /**
     * Returns true if the Excel Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isExcelAdapterInstalled()
    {
        return s_ms_xls;
    }

    /**
     * Returns true if the PowerPoint Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isPowerPointAdapterInstalled()
    {
        return s_ms_ppt;
    }

    /**
     * Returns true if Costing is installed
     * 
     * @return true | false
     */
    public static boolean isCostingInstalled()
    {
        return s_costing;
    }

    /**
     * Returns true if Reports is installed
     * 
     * @return true | false
     */
    public static boolean isReportsInstalled()
    {
        return s_reports;
    }

    /**
     * Returns true if Snippets (Add/Delete) is installed
     * 
     * @return true | false
     */
    public static boolean isSnippetsInstalled()
    {
        return s_snippets;
    }

    /**
     * Returns true if Corpus TM is installed
     * 
     * @return true | false
     */
    public static boolean isCorpusInstalled()
    {
        return s_corpus;
    }

    /**
     * Returns true if Calendaring is installed
     * 
     * @return true | false
     */
    public static boolean isCalendaringInstalled()
    {
        return s_calendar;
    }

    /**
     * Returns true if VendorMgmt is installed
     * 
     * @return true | false
     */
    public static boolean isVendorManagementInstalled()
    {
        return s_vendorMgmt;
    }

    /**
     * Returns true if the Catalyst Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isCatalystAdapterInstalled()
    {
        return s_catalyst;
    }

    /**
     * Returns true if the Corpus Aligner is installed
     * 
     * @return true | false
     */
    public static boolean isCorpusAlignerInstalled()
    {
        return s_corpusAligner;
    }

    /**
     * Returns true if the ServiceWare Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isServiceWareAdapterInstalled()
    {
        return s_serviceware;
    }

    /**
     * Returns true if the Documentum Adapter is installed
     * 
     * @return true | false
     */
    public static boolean isDocumentumAdapterInstalled()
    {
        return s_documentum;
    }

    /**
     * Returns true if the customer access group is enabled.
     */
    public static boolean isCustomerAccessGroupInstalled()
    {
        return s_customerAccessGroup;
    }

    // 
    // Private Methods
    //

    /*
     * Determine whether the customer access group install key is valid.
     */
    private static void checkCustomerAccessGroup()
    {
        String realKey = "CAG-" + "GS".hashCode() + "-" + "customer".hashCode();
        s_customerAccessGroup = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.CUSTOMER_INSTALL_KEY);
    }

}
