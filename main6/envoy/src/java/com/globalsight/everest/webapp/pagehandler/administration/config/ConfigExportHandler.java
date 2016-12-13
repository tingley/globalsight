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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jdom.Document;
import org.jdom.Element;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConfiguration;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.SpecialFilterToExport;
import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocalePairComparator;
import com.globalsight.everest.util.comparator.MTProfileComparator;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.everest.webapp.pagehandler.administration.costing.currency.CurrencyHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.costing.rate.RateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionHelper;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowStatePostHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.terminology.TermbaseInfo;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.SortUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * handler for export system configuration file
 *
 */
public class ConfigExportHandler extends PageHandler
{
    static public final String ZIP_FILE_NAME = "DownloadAllConfigFiles.zip";

    /**
     * @param pageDescriptor
     *            the description of the page to be produced
     * @param request
     *            the original request sent from the browser
     * @param response
     *            original response object
     * @param context
     *            the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        String currentId = CompanyThreadLocal.getInstance().getValue();
        long companyId = Long.parseLong(currentId);
        String action = p_request.getParameter("action");
        if ("export".equals(action))
        {
            String ids = p_request.getParameter("id");
            File downLoadFile = getZip(user, ids, companyId);
            CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
            commentFilesDownload.sendFileToClient(p_request, p_response, ZIP_FILE_NAME,
                    downLoadFile);
            downLoadFile.delete();
            return;
        }
        else
        {
            // 1. Attributes
            getAttributes(p_request);

            // 2. Attribute Groups
            getAttributeGroup(p_request);

            // 3. Locale Pairs
            getLocalePairs(p_request, uiLocale);

            // 4. Activity Types
            getActivityTypes(p_request, uiLocale);

            // 5. Currency
            getCurrencies(p_request);

            // 6. Rates
            getRates(p_request);

            // 7. Permission Groups
            getPermissionGroups(p_request);

            // 8. Users
            getUsers(p_request, uiLocale);

            // 9. Translation Memories
            getTMs(p_request);

            // 10. Segmentation Rules
            getSRXRules(p_request);

            // 11. Translation Memory Profiles
            getTMProfiles(p_request);

            // 12. Machine Translation profiles
            getMTProfiles(p_request, uiLocale);

            // 13. Terminology
            getTermbases(p_request, uiLocale);

            // 14. Projects
            getProjects(p_request, uiLocale);

            // 15. Workflows
            getWorkflows(p_request, uiLocale);

            // 16. Workflow State Post Profile
            getWfStatePostProfiles(p_request);

            // 17. Localization Profiles
            getL10nProfiles(p_request);

            // 18. XML Rules
            getXMLRules(p_request);

            // 19. Filter Configuration
            getFilters(p_request);

            // 20. File Profiles
            getFileProfiles(p_request);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    // get zip file
    private File getZip(User user, String ids, long companyId)
    {
        File downLoadFile = null;
        Set<File> entryFiles = new HashSet<File>();
        try
        {
            String[] idsArr = null;
            File localePropertyFile = LocalePairExportHelper.createPropertyFile(user.getUserName());

            File userPropertyFile = UserExportHelper.createPropertyfile();

            File mtPropertyFile = MTExportHelper.createPropertyfile(user.getUserName());

            File filterPropertyFile = FilterExportHelper.createPropertyfile(user.getUserName(),
                    companyId);

            Element root = new Element("UserInfo");
            Document Doc = new Document(root);
            if (ids != null && !ids.equals(""))
            {
                idsArr = ids.split(",");
                if (idsArr != null)
                {
                    for (int n = 0; n < idsArr.length; n++)
                    {
                        String[] idArr = idsArr[n].split("-");
                        if ("localePair".equals(idArr[0]))
                        {
                            // get locale pair property file
                            LocalePairExportHelper.propertiesInputLocalePair(localePropertyFile,
                                    idArr[1]);
                        }
                        else if ("user".equals(idArr[0]))
                        {
                            // get user property file
                            userPropertyFile = UserExportHelper.exportUsers(userPropertyFile, root,
                                    Doc, user, idArr[1]);
                        }
                        else if ("mt".equals(idArr[0]))
                        {
                            MachineTranslationProfile mtp = MTProfileHandlerHelper
                                    .getMTProfileById(idArr[1]);
                            if (mtp != null)
                            {
                                // get mt profile property file
                                mtPropertyFile = MTExportHelper.propertiesInputMTP(mtPropertyFile,
                                        mtp);
                            }
                        }
                        else if ("filter".equals(idArr[0]))
                        {
                            // get filter property file
                            SpecialFilterToExport specialFilterToExport = new SpecialFilterToExport(
                                    Long.parseLong(idArr[2]), idArr[1]);
                            filterPropertyFile = FilterExportHelper.exportFilters(
                                    filterPropertyFile, specialFilterToExport, companyId);
                        }
                    }
                }
            }
            if (userPropertyFile.length() > 0)
            {
                entryFiles.add(userPropertyFile);
            }
            if (localePropertyFile.length() > 0)
            {
                entryFiles.add(localePropertyFile);
            }
            if (mtPropertyFile.length() > 0)
            {
                entryFiles.add(mtPropertyFile);
            }
            if (filterPropertyFile.length() > 0)
            {
                entryFiles.add(filterPropertyFile);
            }
            downLoadFile = new File(ZIP_FILE_NAME);
            String configPath = getQAReportWorkflowPath();
            if (File.separator.equals("\\"))
            {
                configPath = configPath.replace("/", File.separator);
            }
            ZipIt.addEntriesToZipFile(downLoadFile, entryFiles, configPath, "");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return downLoadFile;
    }

    private String getQAReportWorkflowPath()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(AmbFileStoragePathUtils.getFileStorageDirPath());
        sb.append(File.separator);
        sb.append("GlobalSight");
        sb.append(File.separator);
        sb.append("config");
        sb.append(File.separator);

        return sb.toString();
    }

    private void getFileProfiles(HttpServletRequest p_request)
    {
        try
        {
            List<FileProfile> fileProfiles = (List<FileProfile>) ServerProxy
                    .getFileProfilePersistenceManager().getAllFileProfiles();
            p_request.setAttribute("fileProfiles", fileProfiles);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getFilters(HttpServletRequest p_request)
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        long companyId = Long.parseLong(currentId);
        ArrayList<FilterConfiguration> filterConfigurations = FilterHelper
                .getAllFilterConfiguration(companyId);
        Map<String, ArrayList<Filter>> map = new HashMap<String, ArrayList<Filter>>();
        for (FilterConfiguration filterConfig : filterConfigurations)
        {
            String filterName = filterConfig.getName();
            if ("JavaScript Object Notation Filter ".equals(filterName))
            {
                filterName = "JSON Filter";
            }
            ArrayList<Filter> specialFilters = filterConfig.getSpecialFilters();
            if (specialFilters.size() > 0)
            {
                map.put(filterName, specialFilters);
            }
        }

        p_request.setAttribute("filters", map);
    }

    private void getXMLRules(HttpServletRequest p_request)
    {
        try
        {
            List xmlrulefiles = (List) ServerProxy.getXmlRuleFilePersistenceManager()
                    .getAllXmlRuleFiles();
            p_request.setAttribute("xmlruleFiles", xmlrulefiles);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getL10nProfiles(HttpServletRequest p_request)
    {
        List locProfiles = LocProfileHandlerHelper.getAllL10nProfiles();
        p_request.setAttribute("locProfiles", locProfiles);
    }

    private void getWfStatePostProfiles(HttpServletRequest p_request)
    {
        List wfStatePostProfiles = WorkflowStatePostHandlerHelper.getAllWfStatePost();
        p_request.setAttribute("wfstatePostProfiles", wfStatePostProfiles);
    }

    private void getWorkflows(HttpServletRequest p_request, Locale uiLocale)
    {
        List<WorkflowTemplateInfo> wfTemplates = WorkflowTemplateHandlerHelper
                .getAllWorkflowTemplateInfos();
        WorkflowTemplateInfoComparator workflowComparator = new WorkflowTemplateInfoComparator(
                uiLocale);
        SortUtil.sort(wfTemplates, workflowComparator);
        p_request.setAttribute("wfTemplates", wfTemplates);
    }

    private void getTermbases(HttpServletRequest p_request, Locale uiLocale)
    {
        try
        {
            List<TermbaseInfo> termBases = ServerProxy.getTermbaseManager().getTermbaseList(
                    uiLocale);
            p_request.setAttribute("termBases", termBases);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getMTProfiles(HttpServletRequest p_request, Locale uiLocale)
    {
        try
        {
            List<MachineTranslationProfile> mtProfiles = (List<MachineTranslationProfile>) MTProfileHandlerHelper
                    .getAllMTProfiles("");
            MTProfileComparator MTProfileComparator = new MTProfileComparator(uiLocale);
            SortUtil.sort(mtProfiles, MTProfileComparator);
            p_request.setAttribute("mtProfiles", mtProfiles);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getTMProfiles(HttpServletRequest p_request)
    {
        List tmProfiles = TMProfileHandlerHelper.getAllTMProfiles();
        p_request.setAttribute("tmProfiles", tmProfiles);
    }

    private void getSRXRules(HttpServletRequest p_request)
    {
        try
        {
            List segmentationrulefiles = (List) ServerProxy
                    .getSegmentationRuleFilePersistenceManager().getAllSegmentationRuleFiles();
            p_request.setAttribute("segRules", segmentationrulefiles);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getTMs(HttpServletRequest p_request)
    {
        List projectTMs = (List) TMProfileHandlerHelper.getProjectTMs();
        p_request.setAttribute("projectTMs", projectTMs);
    }

    private void getUsers(HttpServletRequest p_request, Locale uiLocale)
    {
        List users = UserHandlerHelper.getUsersForCurrentCompany();
        UserComparator userComparator = new UserComparator(uiLocale);
        SortUtil.sort(users, userComparator);
        p_request.setAttribute("users", users);
    }

    private void getPermissionGroups(HttpServletRequest p_request)
    {
        ArrayList permissionGroups = (ArrayList) PermissionHelper.getAllPermissionGroups();
        p_request.setAttribute("permissionGroups", permissionGroups);
    }

    private void getRates(HttpServletRequest p_request)
    {
        ArrayList allRates = (ArrayList) RateHandlerHelper.getAllRates();
        p_request.setAttribute("rates", allRates);
    }

    private void getCurrencies(HttpServletRequest p_request)
    {
        ArrayList currencies = (ArrayList) CurrencyHandlerHelper.getAllCurrencies();
        p_request.setAttribute("currencies", currencies);
    }

    private void getActivityTypes(HttpServletRequest p_request, Locale uiLocale)
    {
        List<Activity> activities = UserHandlerHelper.getAllActivities(uiLocale);
        p_request.setAttribute("activities", activities);
    }

    private void getProjects(HttpServletRequest p_request, Locale uiLocale)
    {
        List<Project> projectInfos = (List<Project>) ProjectHandlerHelper.getAllProjects();
        p_request.setAttribute("projects", projectInfos);
    }

    private void getLocalePairs(HttpServletRequest p_request, Locale uiLocale)
    {
        try
        {
            Vector<LocalePair> al = ServerProxy.getLocaleManager().getSourceTargetLocalePairs();
            LocalePairComparator comp = new LocalePairComparator(uiLocale);
            SortUtil.sort(al, comp);
            p_request.setAttribute("localPairs", al);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getAttributeGroup(HttpServletRequest p_request)
    {
        List<AttributeSet> allAttributeSets = (List<AttributeSet>) AttributeManager
                .getAllAttributeSets();
        p_request.setAttribute("allAttributeSets", allAttributeSets);
    }

    private void getAttributes(HttpServletRequest p_request)
    {
        List<Attribute> allAttributes = (List<Attribute>) AttributeManager.getAllAttributes();
        Comparator<Attribute> comparator = getComparator();
        SortUtil.sort(allAttributes, comparator);
        p_request.setAttribute("allAttributes", allAttributes);
    }

    private Comparator<Attribute> getComparator()
    {
        return new Comparator<Attribute>()
        {
            @Override
            public int compare(Attribute o1, Attribute o2)
            {
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        };
    }
}
