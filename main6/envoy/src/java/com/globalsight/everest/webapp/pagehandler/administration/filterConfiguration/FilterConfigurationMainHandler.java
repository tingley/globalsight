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
package com.globalsight.everest.webapp.pagehandler.administration.filterConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.filterconfiguration.ExportFilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.FilterConfiguration;
import com.globalsight.cxe.entity.filterconfiguration.SpecialFilterToExport;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class FilterConfigurationMainHandler extends PageHandler
{
    /**
     * Invokes this PageHandler
     * 
     * @param pageDescriptor
     *            the page descriptor
     * @param request
     *            the original request sent from the browser
     * @param response
     *            the original response object
     * @param context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

        String[] filters =
        { "MS Office Doc Filter", "XML Filter", "HTML Filter", "JSP Filter",
                "MS Office Excel Filter", "Java Properties Filter",
                "Java Script Filter", "InDesign Filter", "OpenOffice Filter",
                "MS Office PowerPoint Filter", "MS Office 2010 Filter",
                "Portable Object Filter", "Base Text Filter",
                "Frame Maker 9 Filter", "Plain Text Filter", "QA Filter" };

        String hql2 = "select f.name from FilterConfiguration f where f.companyId=:companyId";
        String currentId = CompanyThreadLocal.getInstance().getValue();
        long companyId = Long.parseLong(currentId);
        Map map = new HashMap();
        map.put("companyId", companyId);
        List itList = HibernateUtil.search(hql2, map);
        String action = p_request.getParameter("action");

        ArrayList exCol = new ArrayList();

        for (int i = 0; i < filters.length; i++)
        {
            if (!itList.contains(filters[i]))
            {
                FilterConfiguration fc = new FilterConfiguration();
                fc.setCompanyId(companyId);
                fc.setName(filters[i]);

                if (i == 0)
                {
                    fc.setKnownFormatId("|14|33|");
                    fc.setFilterTableName("ms_office_doc_filter");
                    fc.setFilterDescription("The filter for MS office doc files.");
                }
                else if (i == 1)
                {
                    fc.setKnownFormatId("|7|15|16|17|25|");
                    fc.setFilterTableName("xml_rule_filter");
                    fc.setFilterDescription("The filter for XML files.");
                }
                else if (i == 2)
                {
                    fc.setKnownFormatId("|1|");
                    fc.setFilterTableName("html_filter");
                    fc.setFilterDescription("The filter for HTML files.");
                }
                else if (i == 3)
                {
                    fc.setKnownFormatId("|13|");
                    fc.setFilterTableName("jsp_filter");
                    fc.setFilterDescription("The filter for JSP files.");
                }
                else if (i == 4)
                {
                    fc.setKnownFormatId("|19|34|");
                    fc.setFilterTableName("ms_office_excel_filter");
                    fc.setFilterDescription("The filter for MS excel files.");
                }
                else if (i == 5)
                {
                    fc.setKnownFormatId("|4|10|11|");
                    fc.setFilterTableName("java_properties_filter");
                    fc.setFilterDescription("The filter for java properties files.");
                }
                else if (i == 6)
                {
                    fc.setKnownFormatId("|5|");
                    fc.setFilterTableName("java_script_filter");
                    fc.setFilterDescription("The filter for java script files.");
                }
                else if (i == 7)
                {
                    fc.setKnownFormatId("|31|36|37|38|40|");
                    fc.setFilterTableName("indd_filter");
                    fc.setFilterDescription("The filter for InDesign files.");
                }
                else if (i == 8)
                {
                    fc.setKnownFormatId("|41|");
                    fc.setFilterTableName("openoffice_filter");
                    fc.setFilterDescription("The filter for OpenOffice files.");
                }
                else if (i == 9)
                {
                    fc.setKnownFormatId("|20|35|");
                    fc.setFilterTableName("ms_office_ppt_filter");
                    fc.setFilterDescription("The filter for MS PowerPoint files.");
                }
                else if (i == 10)
                {
                    fc.setKnownFormatId("|43|");
                    fc.setFilterTableName("office2010_filter");
                    fc.setFilterDescription("The filter for MS Office 2010 files.");
                }
                else if (i == 11)
                {
                    fc.setKnownFormatId("|42|");
                    fc.setFilterTableName("po_filter");
                    fc.setFilterDescription("The filter for Portable Object files.");
                }
                else if (i == 12)
                {
                    fc.setKnownFormatId("|0|");
                    fc.setFilterTableName("base_filter");
                    fc.setFilterDescription("The filter to handle extracted text.");
                }
                else if (i == 13)
                {
                    fc.setKnownFormatId("|49|50|");
                    fc.setFilterTableName("frame_maker_filter");
                    fc.setFilterDescription("The filter to handle Frame Maker 9 files.");
                }
                else if (i == 14)
                {
                    fc.setKnownFormatId("|6|");
                    fc.setFilterTableName("plain_text_filter");
                    fc.setFilterDescription("The filter for Plain Text files.");
                }
                else if (i == 15)
                {
                    fc.setKnownFormatId("|0|");
                    fc.setFilterTableName("qa_filter");
                    fc.setFilterDescription("The filter to handle QA checks.");
                }

                exCol.add(fc);

                try
                {
                    HibernateUtil.save(exCol);
                }
                catch (Exception e)
                {
                }
            }
        }

        if (action != null && "export".equals(action))
        {
            String checkedSpecialFilters = p_request.getParameter("param");
            String[] specialFilters = checkedSpecialFilters.split(":");
            List<SpecialFilterToExport> specialFilterToExportList = buildSpecialFilterToExport(specialFilters);
            ExportFilterHelper.exportFilters(user.getUserName(), p_response,
                    specialFilterToExportList, companyId);
            return;
        }
        // GBS-3697
        Vector locales = ServerProxy.getLocaleManager().getAvailableLocales();
        SortUtil.sort(locales,
                new GlobalSightLocaleComparator(Locale.getDefault()));
        p_request.setAttribute(LocalePairConstants.LOCALES, locales);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private ArrayList<SpecialFilterToExport> buildSpecialFilterToExport(
            String[] specialFilters)
    {
        ArrayList<SpecialFilterToExport> specialFilterToExportList = new ArrayList<SpecialFilterToExport>();
        for (int i = 0; i < specialFilters.length; i++)
        {
            String specialFilter = specialFilters[i];
            if (!"".equals(specialFilter))
            {
                String[] filtersArray = specialFilter.split(",");
                SpecialFilterToExport specialFilterToExport = new SpecialFilterToExport(
                        Long.parseLong(filtersArray[1]), filtersArray[2]);
                specialFilterToExportList.add(specialFilterToExport);
            }
        }
        return specialFilterToExportList;
    }
}
