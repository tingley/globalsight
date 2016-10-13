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

import org.apache.log4j.Logger;

import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManager;
import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManagerWLRemote;
import com.globalsight.cxe.entity.exportlocation.ExportLocationImpl;
import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.util.GeneralException;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import com.globalsight.everest.servlet.EnvoyServletException;

// javax
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.naming.NamingException;

public class ModifyExportLocationPageHandler extends PageHandler
{

    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
                                  HttpServletRequest p_theRequest,
				  HttpServletResponse p_theResponse,
	                          ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        String action = p_theRequest.getParameter(EXPORT_LOCATION_ACTION);
        if ( action != null && action.intern() == EXPORT_LOCATION_ACTION_MODIFY)
        {
            String id = p_theRequest.getParameter(EXPORT_LOCATION_MODIFY_ID);
            p_theRequest.setAttribute(EXPORT_LOCATION_MODIFY_ID, id);
        }
        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
            p_theResponse,p_context);
    }
}
