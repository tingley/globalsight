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
/**
 * @author Fan
 */
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.MachineTranslateAdapter;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.util.comparator.TMProfileComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowStatePostHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class LocProfileNewAndEditHandler extends PageHandler implements
        LocProfileStateConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(LocProfileNewAndEditHandler.class.getName());
    private MachineTranslateAdapter machineTranslateAdapter = new MachineTranslateAdapter();

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        String action = p_request.getParameter("action");
        if ("ajax".equals(action))
        {
            String jsonStr = getJSONWorkflows(p_request);
            p_response.setCharacterEncoding("utf-8");
            p_response.getWriter().write(jsonStr);
            return;
        }
        if ("edit".equals(action))
        {
            String id = p_request.getParameter("radioBtn");
            if (id == null
                    || p_request.getMethod().equalsIgnoreCase(
                            REQUEST_METHOD_GET))
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
                return;
            }
            BasicL10nProfile editLocprofile = (BasicL10nProfile) LocProfileHandlerHelper
                    .getL10nProfile(Long.parseLong(id));
            setEditDataToRequest(p_request, editLocprofile);
        }

        setDataToRequestForPageComboBoxes(p_request);

        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    private SessionManager getSessionMgrByRequest(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        return sessionMgr;
    }

    private void setDataToRequestForPageComboBoxes(HttpServletRequest p_request)
            throws ServletException, IOException, EnvoyServletException
    {
        // L10n profiles (for dup name checking
        p_request.setAttribute("names",
                LocProfileHandlerHelper.getL10nProfileNames());

        // Source Locales
        HttpSession session = p_request.getSession();
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        List sourceLocales = LocProfileHandlerHelper
                .getAllSourceLocales(uiLocale);
        p_request.setAttribute("srcLocales", sourceLocales);

        // TmProfiles
        List tmProfiles = TMProfileHandlerHelper.getAllTMProfiles();
        TMProfileComparator comp = new TMProfileComparator(
                TMProfileComparator.NAME, uiLocale);
        SortUtil.sort(tmProfiles, comp);
        p_request.setAttribute("tmProfiles", tmProfiles);

        // Projects
        SessionManager sessionMgr = getSessionMgrByRequest(p_request);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        List projectInfos = LocProfileHandlerHelper
                .getAllProjectNamesForManagedUser(user);
        SortUtil.sort(projectInfos, new ProjectComparator(uiLocale));
        p_request.setAttribute("projects", projectInfos);

        // Priorities
        int maxPriority;
        int defaultPriority;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            maxPriority = sc
                    .getIntParameter(SystemConfigParamNames.MAX_PRIORITY);
            defaultPriority = sc
                    .getIntParameter(SystemConfigParamNames.DEFAULT_PRIORITY);
        }
        catch (Throwable e)
        {
            // Default to some know default values
            maxPriority = 5;
            defaultPriority = 3;
        }
        p_request.setAttribute("maxPriority", new Integer(maxPriority));
        p_request.setAttribute("defaultPriority", new Integer(defaultPriority));
        
        //workflow state post profile
        
        List wfStatePost = WorkflowStatePostHandlerHelper.getAllWfStatePost();
        p_request.setAttribute("wfStatePost", wfStatePost);
    }

    private String getJSONWorkflows(HttpServletRequest p_request)
    {
        String locProfileProjectId = p_request
                .getParameter("locProfileProjectId");
        String sourceLocaleId = p_request.getParameter("sourceLocaleId");
        HttpSession session = p_request.getSession();
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        Hashtable targetlocale_pairs = getTargetLocalesByProject(uiLocale,
                Long.parseLong(sourceLocaleId), locProfileProjectId);
        Locale sourcelocale = (Locale) targetlocale_pairs.get("source");
        Vector targetLocalesId = (Vector) targetlocale_pairs.get("object");
        Vector targetLocalesDisplayNames = (Vector) targetlocale_pairs
                .get("display");
        JSONObject jsonObj = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        List<MachineTranslationProfile> mtSupport = (List<MachineTranslationProfile>) MTProfileHandlerHelper
                .getSupportMTProfiles();
        List<MachineTranslationProfile> mtNotSure = (List<MachineTranslationProfile>) MTProfileHandlerHelper
                .getNotSureMTProfiles(mtSupport);
        try
        {
            for (int i = 0; i < targetLocalesId.size(); i++)
            {
                long targetLocaleIdLong = Long
                        .parseLong((String) targetLocalesId.elementAt(i));
                Locale targetlocale = (Locale) targetlocale_pairs
                        .get(targetLocaleIdLong);
                long sourceLocaleIdLong = Long.parseLong(sourceLocaleId);
                long locProfileProjectIdLong = Long
                        .parseLong(locProfileProjectId);
                JSONObject jsonTargetLocaleObj = new JSONObject();
                jsonTargetLocaleObj.put("targetLocaleId",
                        targetLocalesId.elementAt(i));
                jsonTargetLocaleObj.put("targetLocaleDisplayName",
                        targetLocalesDisplayNames.elementAt(i));
                jsonTargetLocaleObj.put(
                        "mtProfiles",
                        getJSONEachMt(sourcelocale, targetlocale, mtSupport,
                                mtNotSure));
                JSONArray trgLocaleWfs = getJSONEachWorkflow(targetLocaleIdLong,
                        sourceLocaleIdLong, locProfileProjectIdLong);
                if (trgLocaleWfs != null)
                {
                    jsonTargetLocaleObj.put("targetLocaleWorkflows", trgLocaleWfs);
                }
                jsonArr.put(jsonTargetLocaleObj);
            }
            jsonObj.put("targetLocalesWorkflows", jsonArr);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return jsonObj.toString();
    }

    private JSONArray getJSONEachMt(Locale sourcelocale, Locale targetlocale,
            List<MachineTranslationProfile> mtSupport,
            List<MachineTranslationProfile> mtNotSure) throws JSONException
    {
        JSONArray jsonWorkflowsArr = new JSONArray();
        for (int i = 0; i < mtNotSure.size(); i++)
        {
            MachineTranslationProfile mt = (MachineTranslationProfile) mtNotSure
                    .get(i);
            if (machineTranslateAdapter.isSupportsLocalePair(mt, sourcelocale,
                    targetlocale))
            {
                JSONObject jsonWorkflowsObj = new JSONObject();
                jsonWorkflowsObj.put("mtId", mt.getId());
                jsonWorkflowsObj.put("mtName", mt.getMtProfileName());
                jsonWorkflowsArr.put(jsonWorkflowsObj);
                continue;

            }
        }
        for (int i = 0; i < mtSupport.size(); i++)
        {
            MachineTranslationProfile mt = (MachineTranslationProfile) mtSupport
                    .get(i);
            JSONObject jsonWorkflowsObj = new JSONObject();
            jsonWorkflowsObj.put("mtId", mt.getId());
            jsonWorkflowsObj.put("mtName", mt.getMtProfileName());
            jsonWorkflowsArr.put(jsonWorkflowsObj);
        }

        return jsonWorkflowsArr;
    }

    private JSONArray getJSONEachWorkflow(long targetLocaleId,
            long sourceLocaleId, long locProfileProjectId) throws JSONException
    {
        Collection<WorkflowTemplateInfo> targetLocaleWorkflows = null;
        JSONArray jsonWorkflowsArr = new JSONArray();
        try
        {
            targetLocaleWorkflows = ServerProxy.getProjectHandler()
                    .getAllWorkflowTemplateInfosByParameters(sourceLocaleId,
                            targetLocaleId, locProfileProjectId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        if (targetLocaleWorkflows != null && targetLocaleWorkflows.size() > 0)
        {
            for (WorkflowTemplateInfo wft : targetLocaleWorkflows)
            {
                JSONObject jsonWorkflowsObj = new JSONObject();
                jsonWorkflowsObj.put("workflowId", wft.getId());
                jsonWorkflowsObj.put("workflowName", wft.getName());
                jsonWorkflowsArr.put(jsonWorkflowsObj);
            }
            return jsonWorkflowsArr;
        }

        return null;
    }

    // create hashtable of target locale names and ids according project
    private Hashtable getTargetLocalesByProject(Locale p_uiLocale,
            long p_sourceLocaleId, String p_project)
            throws EnvoyServletException
    {
        // gather target locale name id pairs for user to choose
        GlobalSightLocale source = (GlobalSightLocale) LocProfileHandlerHelper
                .getLocaleById(p_sourceLocaleId);
        Vector targetlocales = LocProfileHandlerHelper
                .getTargetLocalesByProject(source, p_project);
        // TomyD - use this code for sorting based on getDisplayName(uiLocale)
        // by passing 2
        SortUtil.sort(targetlocales, new LocaleComparator(2, p_uiLocale));
        Hashtable targetLocale_pairs = new Hashtable();
        targetLocale_pairs.put("source", source.getLocale());
        // for each target locale, get language to display
        long id = 0;
        GlobalSightLocale locale;
        GlobalSightLocale localeToBeRemoved = null;
        Vector displayNames = new Vector();
        Vector targetIds = new Vector();
        for (int i = 0; i < targetlocales.size(); i++)
        {
            locale = (GlobalSightLocale) targetlocales.elementAt(i);
            id = locale.getId();
            if (id == p_sourceLocaleId)
            {
                // remove this locale from target
                localeToBeRemoved = locale;
            }
            else
            {
                displayNames.add(locale.getDisplayName(p_uiLocale));
                targetIds.add((new Long(id)).toString());
                targetLocale_pairs.put(id, locale.getLocale());
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("LocProfileNewAndEditHandler: locale_id " + id
                        + " lang " + locale.getDisplayLanguage() + " country "
                        + locale.getDisplayCountry());
            }
        }

        if (localeToBeRemoved != null)
        {
            targetlocales.removeElement(localeToBeRemoved);
        }
        targetLocale_pairs.put("object", targetIds);
        targetLocale_pairs.put("display", displayNames);
        return targetLocale_pairs;
    }

    private void setEditDataToRequest(HttpServletRequest p_request,
            BasicL10nProfile editLocprofile)
    {
        p_request.setAttribute("edit", true);
        p_request.setAttribute("radioBtn", p_request.getParameter("radioBtn"));
        p_request.setAttribute("LocProfileName", editLocprofile.getName());
        p_request.setAttribute("LocProfileDescription",
                editLocprofile.getDescription());

        // sql script
        String chosenSQLScript;
        if (editLocprofile.runScriptAtJobCreation())
        {
            chosenSQLScript = editLocprofile.getNameOfJobCreationScript();
        }
        else
        {
            chosenSQLScript = "";
        }
        p_request.setAttribute("LocProfileSQLScript", chosenSQLScript);

        // tmProfile id
        Iterator it = editLocprofile.getTmProfiles().iterator();
        if (it.hasNext())
        {
            TranslationMemoryProfile tmProfile = (TranslationMemoryProfile) it
                    .next();
            p_request.setAttribute("locTMProfileId", tmProfile.getId() + "");
        }

        //workflow state post profile
        p_request.setAttribute("wfStatePostProfileId",
                editLocprofile.getWfStatePostId() + "");
        
        p_request.setAttribute("LocProfileProjectId",
                editLocprofile.getProjectId() + "");
        p_request
                .setAttribute("JobPriority", editLocprofile.getPriority() + "");
        GlobalSightLocale locale = (GlobalSightLocale) editLocprofile
                .getSourceLocale();
        Integer Itmp = new Integer((int) locale.getId());
        p_request.setAttribute("SourceLocaleId", Itmp.toString());

        // tm usage id
        int TMEditType = editLocprofile.getTMEditType();
        p_request.setAttribute("LocProfileTMUsageId", TMEditType);

        p_request.setAttribute("AutomaticDispatch",
                editLocprofile.dispatchIsAutomatic() + "");

        GlobalSightLocale[] targets = editLocprofile.getTargetLocales();
        long[] workflowTemplateId = new long[targets.length];
        long[] mtIds = new long[targets.length];
        for (int i = 0; i < targets.length; i++)
        {
            WorkflowTemplateInfo workflowTemplateInfo = (WorkflowTemplateInfo) editLocprofile
                    .getWorkflowTemplateInfo((GlobalSightLocale) targets[i]);
            workflowTemplateId[i] = workflowTemplateInfo.getId();
            mtIds[i] = MTProfileHandlerHelper.getMTProfileIdByRelation(
                    editLocprofile.getId(), workflowTemplateId[i]);
        }
        p_request.setAttribute("WorkflowTemplateIdArr", workflowTemplateId);
        p_request.setAttribute("mtIdArr", mtIds);
    }
}
