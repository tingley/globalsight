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
package com.globalsight.everest.webapp.pagehandler.exportlocation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.cxe.entity.exportlocation.ExportLocationImpl;
import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

public class ExportLocationPageHandler extends PageHandler
{
    // color
    public static final String WHITE_BG = "#FFFFFF";
    public static final String GREY_BG = "#EEEEEE";

    // comparator
    protected ExportLocationComparator m_comparator = new ExportLocationComparator();

    public static final String SORT_PARAM = "sort";
    public static final String REMOVE_URL_PARAM = "removeURL";
    public static final String REMOVE_EXPORT_LOCATION_PARAM = "remove";
    public static final String MAKE_DEFAULT_URL_PARAM = "makeDefaultURL";
    public static final String MAKE_DEFAULT_EXPORT_LOCATION_PARAM = "makedefault";
    public static final String MODIFY_URL_PARAM = "modifyURL";
    public static final String MODIFY_EXPORT_LOCATION_PARAM = "modify";
    public static final String NEW_URL_PARAM = "newURL";
    public static final String NEW_EXPORT_LOCATION_PARAM = "new";
    public static final String EXPORT_LOCATION_SCRIPTLET = "exportLocation";
    public static final String EXPORT_LOCATIONS = "exportLocations";

    public static final int LOCATIONS_PER_PAGE = 20;

    // bean
    protected static final String REMOVE_BEAN = "remove";
    protected static final String MAKE_DEFAULT_BEAN = "makedefault";
    protected static final String MODIFY_BEAN = "modify";
    protected static final String NEW_BEAN = "newone";
    protected static final String ERROR_BEAN = "error";

    protected void processExportLocation(String p_operation, String p_name,
            String p_location, String p_description, int p_locId)
    {
        try
        {
            ExportLocationPersistenceManager mgr = ServerProxy
                    .getExportLocationPersistenceManager();

            if (p_operation.equals(EXPORT_LOCATION_ACTION_NEW))
            {
                ExportLocationImpl eLoc = new ExportLocationImpl(p_name,
                        p_description, p_location, null);

                mgr.createExportLocation(eLoc);
            }
            else
            {
                ExportLocation el = mgr.readExportLocation(p_locId);

                ExportLocation eLoc = mgr.getDefaultExportLocation();
                String defaultName = eLoc.getName();

                if (p_operation.equals(EXPORT_LOCATION_ACTION_REMOVE))
                {
                    // Do not ever allow to delete Export Location 1
                    // If the current default location is deleted
                    // revert back to old default location i.e. 1.
                    if (p_locId != 1)
                    {
                        mgr.deleteExportLocation(el);
                        if (defaultName.equals(el.getName()))
                        {
                            mgr.setDefaultExportLocation(1);
                        }
                    }
                }
                else if (p_operation.equals(EXPORT_LOCATION_ACTION_DEFAULT))
                {
                    mgr.setDefaultExportLocation(p_locId);
                }
                else if (p_operation.equals(EXPORT_LOCATION_ACTION_MODIFY))
                {
                    el.setName(p_name);
                    el.setLocation(p_location);
                    el.setDescription(p_description);

                    mgr.updateExportLocation(el);
                }
            }
        }
        catch (GeneralException ge)
        {
            System.out.println("General Exception " + ge.getMessage());
        }
        catch (RemoteException re)
        {
            System.out.println("Remote Exception " + re.getMessage());
        }
        catch (NamingException ne)
        {
            System.out.println("NamingException during set up.");
        }
    }

    protected String getLocationText(HttpServletRequest p_request)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            ExportLocationPersistenceManager mgr = ServerProxy
                    .getExportLocationPersistenceManager();

            Collection c = mgr.getAllExportLocations();
            sortLocations(p_request, (List) c);

            ExportLocation eLoc = mgr.getDefaultExportLocation();
            String defaultName = eLoc.getName();
            String defaultIndicator = "";
            Object[] locs = c.toArray();

            // need to store the array in the session manager for
            // checking the names during create or modify export
            // location (names are supposed to be unique).
            HttpSession session = p_request.getSession(false);
            SessionManager sm = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            sm.setAttribute(EXPORT_LOCATIONS, locs);

            int locationListStart = 0;

            // Note that these two integers, jobListStart and jobListEnd,
            // are the *indexes* of the list so for display we'll add 1 so it
            // will look good for the user. This is also why I subtract 1 from
            // jobListEnd below, so that jobListEnd will be an index value
            int locationListEnd = (locationListStart + LOCATIONS_PER_PAGE) > locs.length ? locs.length
                    : (locationListStart + LOCATIONS_PER_PAGE);
            locationListEnd = locationListEnd - 1;

            sb = new StringBuffer();

            for (int i = locationListStart; i <= locationListEnd; i++)
            {
                String bgColor = (i % 2 == 0) ? "#FFFFFF" : "#EEEEEE";

                sb.append("<TR BGCOLOR=");
                sb.append(bgColor);
                sb.append(" STYLE=\"padding-top: 5px; padding-bottom: 5px; height:30px;\" >");
                ExportLocation el = (ExportLocation) locs[i];
                // Radio button
                sb.append("<TD><INPUT TYPE=radio NAME=locId VALUE=\"");
                sb.append(el.getId());
                sb.append("\"></TD>\n");

                // Export Location Name
                sb.append("<TD><SPAN CLASS=\"standardText\">");
                sb.append(el.getName());
                sb.append("</SPAN></TD>\n");

                // Location
                sb.append("<TD><SPAN CLASS=\"standardText\" ");
                sb.append("STYLE=\"word-wrap: break-word;\">");
                sb.append(el.getLocation());
                sb.append("</SPAN></TD>\n");

                // Description
                sb.append("<TD><SPAN CLASS=\"standardText\">");
                sb.append(el.getDescription());
                sb.append("</SPAN></TD>\n");

                // Default
                if (defaultName.equals(el.getName()))
                {
                    defaultIndicator = "<IMG SRC=\"/globalsight/images/checkmark.gif\" "
                            + "HEIGHT=9 WIDTH=13 HSPACE=10 VSPACE=3>";
                }
                else
                {
                    defaultIndicator = "";
                }
                sb.append("<TD><SPAN CLASS=\"standardText\">");
                sb.append(defaultIndicator);
                sb.append("</SPAN></TD>\n");

                sb.append("</TR>\n");
            }
        }
        catch (GeneralException ge)
        {
            System.out.println("General Exception " + ge.getMessage());
        }
        catch (RemoteException re)
        {
            System.out.println("Remote Exception " + re.getMessage());
        }
        catch (NamingException ne)
        {
            System.out.println("NamingException during set up.");
        }

        return sb.toString();
    }

    protected void sortLocations(HttpServletRequest p_request, List p_locations)
    {
        String criteria = p_request.getParameter(SORT_PARAM);
        if (criteria != null)
        {
            int sortCriteria = Integer.parseInt(criteria);
            // from the same type to the same type.
            if (m_comparator.getComparisonCriteria() == sortCriteria)
            {
                SortUtil.sort(p_locations, m_comparator);
                if (m_comparator.getReverse())
                {
                    Collections.reverse(p_locations);
                    m_comparator.setReverse(false);
                }
                else
                {
                    m_comparator.setReverse(true);
                }
            }
            // from different sort type to another type.
            else
            {
                m_comparator.setComparisonCriteria(sortCriteria);
                SortUtil.sort(p_locations, m_comparator);
                m_comparator.setReverse(true);
            }
        }
    }

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        String action = p_request.getParameter(EXPORT_LOCATION_ACTION);
        if (action == null)
        {
            action = "";
        }

        if (action.equals(EXPORT_LOCATION_ACTION_REMOVE))
        {
            String id = p_request.getParameter(EXPORT_LOCATION_ACTION_REMOVE);
            int locId = Integer.parseInt(id);

            processExportLocation(EXPORT_LOCATION_ACTION_REMOVE, "", "", "",
                    locId);
        }
        else if (action.equals(EXPORT_LOCATION_ACTION_DEFAULT))
        {
            String id = p_request.getParameter(EXPORT_LOCATION_ACTION_DEFAULT);
            int locId = Integer.parseInt(id);

            processExportLocation(EXPORT_LOCATION_ACTION_DEFAULT, "", "", "",
                    locId);
        }
        else if (action.equals(EXPORT_LOCATION_ACTION_NEW))
        {
            String name = p_request.getParameter(EXPORT_LOCATION_NEW_NAME);
            String location = p_request
                    .getParameter(EXPORT_LOCATION_NEW_LOCATION);
            String description = p_request
                    .getParameter(EXPORT_LOCATION_NEW_DESCRIPTION);
            description = EditUtil.utf8ToUnicode(description);

            processExportLocation(EXPORT_LOCATION_ACTION_NEW, name, location,
                    description, -1);
        }
        else if (action.equals(EXPORT_LOCATION_ACTION_MODIFY))
        {
            String id = p_request.getParameter(EXPORT_LOCATION_MODIFY_ID);
            int locId = Integer.parseInt(id);
            String name = p_request.getParameter(EXPORT_LOCATION_MODIFY_NAME);
            String location = p_request
                    .getParameter(EXPORT_LOCATION_MODIFY_LOCATION);
            String description = p_request
                    .getParameter(EXPORT_LOCATION_MODIFY_DESCRIPTION);

            description = EditUtil.utf8ToUnicode(description);

            processExportLocation(EXPORT_LOCATION_ACTION_MODIFY, name,
                    location, description, locId);
        }

        String allLocations = getLocationText(p_request);

        p_request.setAttribute(EXPORT_LOCATION_SCRIPTLET, allLocations);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }
}
