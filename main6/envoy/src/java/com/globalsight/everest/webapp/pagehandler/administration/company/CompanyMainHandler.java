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
package com.globalsight.everest.webapp.pagehandler.administration.company;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.everest.company.Category;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CompanyComparator;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobDataMigration;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;

public class CompanyMainHandler extends PageActionHandler implements
        CompanyConstants
{
    private static Logger logger = Logger.getLogger(CompanyMainHandler.class);

    private static final Vector<String> FILTER_NAMES = new Vector<String>();
    private static final Vector<String> KNOWNFORMATIDS = new Vector<String>();
    private static final Vector<String> FILTER_TABLE_NAMES = new Vector<String>();
    private static final Vector<String> FILTER_DESCRIPTION = new Vector<String>();

    private static final String PROPERTIES_TAGS = "/properties/Tags.properties";
    private Properties tagsProperties = new Properties();

    HttpSession session = null;
    PermissionSet userPerms = null;

    static
    {
        FILTER_NAMES.add("Java Properties Filter");
        FILTER_NAMES.add("Java Script Filter");
        FILTER_NAMES.add("MS Office Doc Filter");
        FILTER_NAMES.add("XML Filter");
        FILTER_NAMES.add("HTML Filter");
        FILTER_NAMES.add("JSP Filter");
        FILTER_NAMES.add("MS Office Excel Filter");
        FILTER_NAMES.add("InDesign Filter");
        FILTER_NAMES.add("OpenOffice Filter");
        FILTER_NAMES.add("MS Office PowerPoint Filter");
        FILTER_NAMES.add("MS Office 2010 Filter");
        FILTER_NAMES.add("Portable Object Filter");
        FILTER_NAMES.add("Internal Text Filter");

        KNOWNFORMATIDS.add("|4|10|11|");
        KNOWNFORMATIDS.add("|5|");
        KNOWNFORMATIDS.add("|14|33|");
        KNOWNFORMATIDS.add("|7|15|16|17|25|45|");
        KNOWNFORMATIDS.add("|1|");
        KNOWNFORMATIDS.add("|13|");
        KNOWNFORMATIDS.add("|19|34|");
        KNOWNFORMATIDS.add("|31|36|37|38|40|");
        KNOWNFORMATIDS.add("|41|");
        KNOWNFORMATIDS.add("|20|35|");
        KNOWNFORMATIDS.add("|43|");
        KNOWNFORMATIDS.add("|42|");
        KNOWNFORMATIDS.add("|6|");

        FILTER_TABLE_NAMES.add("java_properties_filter");
        FILTER_TABLE_NAMES.add("java_script_filter");
        FILTER_TABLE_NAMES.add("ms_office_doc_filter");
        FILTER_TABLE_NAMES.add("xml_rule_filter");
        FILTER_TABLE_NAMES.add("html_filter");
        FILTER_TABLE_NAMES.add("jsp_filter");
        FILTER_TABLE_NAMES.add("ms_office_excel_filter");
        FILTER_TABLE_NAMES.add("indd_filter");
        FILTER_TABLE_NAMES.add("openoffice_filter");
        FILTER_TABLE_NAMES.add("ms_office_ppt_filter");
        FILTER_TABLE_NAMES.add("office2010_filter");
        FILTER_TABLE_NAMES.add("po_filter");
        FILTER_TABLE_NAMES.add("base_filter");

        FILTER_DESCRIPTION.add("The filter for java properties files.");
        FILTER_DESCRIPTION.add("The filter for java script files.");
        FILTER_DESCRIPTION.add("The filter for MS office doc files.");
        FILTER_DESCRIPTION.add("The filter for XML files.");
        FILTER_DESCRIPTION.add("The filter for HTML files.");
        FILTER_DESCRIPTION.add("The filter for JSP files.");
        FILTER_DESCRIPTION.add("The filter for MS excel files.");
        FILTER_DESCRIPTION.add("The filter for InDesign files.");
        FILTER_DESCRIPTION.add("The filter for OpenOffice files.");
        FILTER_DESCRIPTION.add("The filter for MS PowerPoint files.");
        FILTER_DESCRIPTION.add("The filter for MS Office 2010 files.");
        FILTER_DESCRIPTION.add("The filter for Portable Object files.");
        FILTER_DESCRIPTION.add("The filter to handle internal text.");
    }

    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        session = request.getSession(false);

        // gbs-1389: restrict direct access to company page without view
        // company permission.
        userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.COMPANY_VIEW))
        {
            try
            {
                response.sendRedirect(request.getContextPath());
                return;
            }
            catch (IOException ioEx)
            {
                logger.error(ioEx);
            }
        }
    }

    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
        try
        {
            dataForTable(request, session);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Create a new company.
     */
    @SuppressWarnings("unchecked")
    @ActionHandler(action = CompanyConstants.CREATE, formClass = "")
    public void create(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        // GBS-1389: restrict direct access to create company without
        // create company permission.
        if (!userPerms.getPermissionFor(Permission.COMPANY_NEW))
        {
            if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=companies");
            }
            else
            {
                p_response.sendRedirect(p_request.getContextPath());
            }
            return;
        }

        Company company = storeCompanyInfo(p_request);
        String userId = PageHandler.getUser(p_request.getSession()).getUserId();
        company = ServerProxy.getJobHandler().createCompany(company, userId);

        if (company != null)
        {
            String[] categories = p_request.getParameterValues("to");
            createCategory(categories, company.getId());

            initialFilterConfigurations(company.getId());
            initialHTMLFilter(company.getId());
            // Create TU/TUV/LeverageMatch tables for current company
            CompanyMigration.createLMTableForCompany(company.getId());
            CompanyMigration.createTuTableForCompany(company.getId());
            CompanyMigration.createTuvTableForCompany(company.getId());
            // Create archive tables for current company.
            JobDataMigration.checkArchiveTables(company.getId());

            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);
            User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
            ArrayList msg = new ArrayList();
            msg.add(company.getName());
            msg.add(Long.toString(company.getId()));
            msg.add(user);
            JmsHelper.sendMessageToQueue(msg, JmsHelper.JMS_NEW_COMPANY_QUEUE);
        }
    }

    /**
     * Edit company.
     */
    @ActionHandler(action = CompanyConstants.EDIT, formClass = "")
    public void edit(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        // GBS-1389: restrict direct access to edit company without edit company
        // permission.
        if (!userPerms.getPermissionFor(Permission.COMPANY_EDIT))
        {
            if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=companies");
            }
            else
            {
                p_response.sendRedirect(p_request.getContextPath());
            }
            return;
        }

        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        Company company = (Company) sessionMgr
                .getAttribute(CompanyConstants.COMPANY);
        modifyCompany(company, p_request);
        ServerProxy.getJobHandler().modifyCompany(company);
        String[] categories = p_request.getParameterValues("to");
        // delete categories first
        deleteCategory(company.getId());
        createCategory(categories, company.getId());
        clearSessionExceptTableInfo(session, CompanyConstants.COMPANY_KEY);
    }

    /**
     * Remove company.
     */
    @ActionHandler(action = CompanyConstants.REMOVE, formClass = "")
    public void remove(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        // gbs-1389: restrict direct access to remove company without
        // remove company permission.
        if (!userPerms.getPermissionFor(Permission.COMPANY_REMOVE))
        {
            if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=companies");
            }
            else
            {
                p_response.sendRedirect(p_request.getContextPath());
            }
            return;
        }

        removeCompany(p_request);
    }

    /**
     * Cancel "new" or "edit" company.
     */
    @ActionHandler(action = CompanyConstants.CANCEL, formClass = "")
    public void cancel(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        clearSessionExceptTableInfo(session, CompanyConstants.COMPANY_KEY);
    }

    /**
     * Migrate company to use separated tables.
     */
    @ActionHandler(action = CompanyConstants.CONVERT, formClass = "")
    public void migrate(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        if (!userPerms.getPermissionFor(Permission.COMPANY_MIGRATE))
        {
            if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            {
                p_response
                        .sendRedirect("/globalsight/ControlServlet?activityName=companies");
            }
            else
            {
                p_response.sendRedirect(p_request.getContextPath());
            }
            return;
        }

        String companyId = p_request.getParameter("id");
        migrateToSeparatedTables(companyId);
    }

    /**
     * Get company migration processing percentage.
     */
    @ActionHandler(action = CompanyConstants.GET_MIGRATE_PROCESSING, formClass = "")
    public void getMigrateProcessing(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String comId = p_request.getParameter("companyId");
        int percentage = HibernateUtil.get(Company.class,
                Integer.parseInt(comId)).getMigrateProcessing();

        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            p_response.setContentType("text/plain");
            out = p_response.getOutputStream();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"migrateProcessing\":");
            sb.append(percentage).append("}");
            out.write(sb.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            out.close();
            pageReturn();
        }
    }

    /**
     * Search company by name.
     */
    @ActionHandler(action = CompanyConstants.SEARCH, formClass = "")
    public void search(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        p_request
                .setAttribute(CompanyConstants.SEARCH, CompanyConstants.SEARCH);
    }

    /**
     * Delete all categories of a company
     * 
     * @param companyId
     */
    private boolean deleteCategory(long companyId)
    {
        try
        {
            String hql = "from Category as c where c.companyId = " + companyId;
            List<String> containedCategoryList = (List<String>) HibernateUtil
                    .search(hql);
            HibernateUtil.delete(containedCategoryList);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Create new categories for a company
     * 
     * @param p_request
     * @param companyId
     * @throws JobException
     */
    private boolean createCategory(String[] categories, long companyId)
            throws JobException
    {
        try
        {
            for (int i = 0; i < categories.length; i++)
            {
                String categoryString = categories[i];
                Category category = new Category();
                category.setCategory(categoryString);
                category.setCompanyId(companyId);
                ServerProxy.getJobHandler().createCategory(category);
                // HibernateUtil.save(category);
            }
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private void initialHTMLFilter(long companyId)
    {
        try
        {
            tagsProperties
                    .load(getClass().getResourceAsStream(PROPERTIES_TAGS));
            insert(companyId);
        }
        catch (IOException e)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
        }
    }

    public void insert(Long companyId) throws IOException
    {
        String filterName = "HTML_Filter(Default)";
        String filterDescription = "The default html filter.";
        String placeHolderTrim = "embeddable_tags";
        String jsFunctionText = "l10n";
        String embeddableTags = tagsProperties.getProperty("InlineTag_html");
        String defaultEmbeddableTags = embeddableTags;
        String pairedTags = tagsProperties.getProperty("PairedTag_html");
        String defaultPairedTags = pairedTags;
        String unpairedTags = tagsProperties.getProperty("UnpairedTag_html");
        String defaultUnpairedTags = unpairedTags;
        String switchTagMap = tagsProperties.getProperty("SwitchTagMap_html");
        String defaultSwitchTagMap = switchTagMap;
        String whitePreservingTag = tagsProperties
                .getProperty("WhitePreservingTag_html");
        String defaultWhitePreservingTag = whitePreservingTag;
        String nonTranslatableMetaAttribute = tagsProperties
                .getProperty("NonTranslatableMetaAttribute_html");
        String defaultNonTranslatableMetaAttribute = nonTranslatableMetaAttribute;
        String translatableAttribute = tagsProperties
                .getProperty("TranslatableAttribute_html");
        String defaultTranslatableAttribute = translatableAttribute;
        String localizableAttributeMap = tagsProperties
                .getProperty("LocalizableAttributeMap_html");
        String defaultLocalizableAttributeMap = localizableAttributeMap;
        String chEntry = tagsProperties.getProperty("convertHtmlEntity");
        // String defaultLocalizableInlineAtrributes = "href";
        // String localizableInlineAttributes = "";
        boolean convertHtmlEntity = (chEntry == null || "".equals(chEntry) ? false
                : Boolean.parseBoolean(chEntry));
        String IIHtmlTags = tagsProperties.getProperty("IgnoreInvalidHtmlTags");
        boolean ignoreInvalidHtmlTags = ("".equals(IIHtmlTags) ? true : Boolean
                .parseBoolean(IIHtmlTags));
        HtmlFilter filter = new HtmlFilter(filterName, filterDescription,
                defaultEmbeddableTags, embeddableTags, placeHolderTrim,
                companyId, convertHtmlEntity, ignoreInvalidHtmlTags,
                jsFunctionText, defaultPairedTags, pairedTags,
                defaultUnpairedTags, unpairedTags, defaultSwitchTagMap,
                switchTagMap, defaultWhitePreservingTag, whitePreservingTag,
                defaultNonTranslatableMetaAttribute,
                nonTranslatableMetaAttribute, defaultTranslatableAttribute,
                translatableAttribute
        // defaultLocalizableInlineAtrributes,
        // localizableInlineAttributes
        );
        if (!checkExist(FilterConstants.HTML_TABLENAME, filterName, companyId))
        {
            HibernateUtil.saveOrUpdate(filter);
        }
    }

    public boolean checkExist(String tableName, String filterName,
            long companyId)
    {
        String checkSql = null;
        if ("filter_configuration".equals(tableName))
        {
            checkSql = "select id from " + tableName + " where name='"
                    + filterName + "' and company_id=" + companyId;
        }
        else
        {
            checkSql = "select id from " + tableName + " where filter_name='"
                    + filterName + "' and company_id=" + companyId;
        }
        return HibernateUtil.searchWithSql(checkSql, null).size() > 0;
    }

    private void initialFilterConfigurations(long companyId)
            throws HibernateException, SQLException
    {
        StringBuilder insertSql = new StringBuilder(
                "insert into filter_configuration(name, known_format_id, filter_table_name, filter_description, company_id) values ");
        for (int j = 0; j < FILTER_NAMES.size(); j++)
        {
            insertSql.append("(");
            insertSql.append("'").append(FILTER_NAMES.get(j)).append("',");
            insertSql.append("'").append(KNOWNFORMATIDS.get(j)).append("',");
            insertSql.append("'").append(FILTER_TABLE_NAMES.get(j))
                    .append("',");
            insertSql.append("'").append(FILTER_DESCRIPTION.get(j))
                    .append("',");
            insertSql.append(companyId);
            insertSql.append(")");
            insertSql.append(",");
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        HibernateUtil.executeSql(insertSql.toString());
    }

    private void dataForTable(HttpServletRequest p_request,
            HttpSession p_session) throws RemoteException, NamingException,
            GeneralException
    {
        Vector companies = getAllCompanies();
        // Filter companies by company name filter
        filterCompaniesByCompanyName(p_request, p_session, companies);
        // Get the number per page
        int numPerPage = getNumPerPage(p_request, p_session);
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, companies,
                new CompanyComparator(uiLocale), numPerPage, COMPANY_LIST,
                COMPANY_KEY);
    }

    private void filterCompaniesByCompanyName(HttpServletRequest p_request,
            HttpSession p_session, Vector p_companies)
    {
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String searchingForName = p_request
                .getParameter(CompanyConstants.FILTER_NAME);
        if (searchingForName == null)
        {
            searchingForName = (String) sessionManager
                    .getAttribute(CompanyConstants.FILTER_NAME);
        }
        if (searchingForName == null)
        {
            searchingForName = "";
        }
        sessionManager.setAttribute(CompanyConstants.FILTER_NAME,
                searchingForName.trim());

        if (!StringUtil.isEmpty(searchingForName))
        {
            for (Iterator comIt = p_companies.iterator(); comIt.hasNext();)
            {
                Company com = (Company) comIt.next();
                String comName = com.getCompanyName().toLowerCase();
                if (comName.indexOf(searchingForName.trim().toLowerCase()) == -1)
                {
                    comIt.remove();
                }
            }
        }
    }
    
    private int getNumPerPage(HttpServletRequest p_request,
            HttpSession p_session)
    {
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        int numPerPage = 10;
        String companyNumPerPage = p_request.getParameter("numOfPageSize");
        if (StringUtil.isEmpty(companyNumPerPage))
        {
            companyNumPerPage = (String) sessionManager.getAttribute("companyNumPerpage");
        }

        if (companyNumPerPage != null)
        {
            sessionManager.setAttribute("companyNumPerpage", companyNumPerPage.trim());
            if ("all".equalsIgnoreCase(companyNumPerPage))
            {
                numPerPage = Integer.MAX_VALUE;
            }
            else
            {
                try
                {
                    numPerPage = Integer.parseInt(companyNumPerPage);
                }
                catch (NumberFormatException ignore)
                {
                    numPerPage = 10;
                }
            }
        }
        
        return numPerPage;
    }

    private Vector getAllCompanies() throws RemoteException, NamingException,
            GeneralException
    {
        return vectorizedCollection(ServerProxy.getJobHandler()
                .getAllCompanies());
    }

    /**
     * Removes a Company.
     */
    private void removeCompany(HttpServletRequest p_request)
    {
        String companyIds = p_request.getParameter("id");
        String[] companyIdsInArray = companyIds.split(",");
        for (int i = 0; i < companyIdsInArray.length; i++)
        {
            String companyId = companyIdsInArray[i];
            Company company = null;
            try
            {
                company = ServerProxy.getJobHandler().getCompanyById(
                        Long.parseLong(companyId));
            }
            catch (Exception e)
            {
                logger.error("Failed to get company with id: " + companyId, e);
            }
            if (company != null
                    && !Company.STATE_DELETING.equals(company.getState()))
            {
                CompanyRemoval removal = new CompanyRemoval(companyId);
                removal.removeCompany();
            }
        }
    }

    private void migrateToSeparatedTables(String p_companyId)
    {
        long companyId = Long.parseLong(p_companyId);

        if (CompanyMigration.canMigrateCompany(companyId))
        {
            // Check LM/TU/TUV tables to ensure they are created
            CompanyMigration.checkLeverageMatchTable(companyId);
            CompanyMigration.checkTuTable(companyId);
            CompanyMigration.checkTuvTable(companyId);

            // Begin converting
            try
            {
                CompanyMigration.migrateToSeparatedTables(companyId);
            }
            catch (Exception e)
            {
                logger.error(e);
            }
        }
    }

    public void clearSessionExceptTableInfo(HttpSession p_session, String p_key)
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(SESSION_MANAGER);
        String companyNameFilterValue = (String) sessionMgr
                .getAttribute(CompanyConstants.FILTER_NAME);
        String companyNumPerpageValue = (String) sessionMgr
                .getAttribute("companyNumPerpage");

        super.clearSessionExceptTableInfo(p_session, p_key);

        if (companyNameFilterValue != null)
        {
            sessionMgr.setAttribute(CompanyConstants.FILTER_NAME,
                    companyNameFilterValue);
        }
        if (companyNumPerpageValue != null)
        {
            sessionMgr
                    .setAttribute("companyNumPerpage", companyNumPerpageValue);
        }
    }

    private void modifyCompany(Company company, HttpServletRequest p_request)
    {
        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
        company.setEmail(p_request.getParameter(CompanyConstants.EMAIL));
        company.setSessionTime(p_request
                .getParameter(CompanyConstants.SESSIONTIME));
        String enableIPFilter = p_request
                .getParameter(CompanyConstants.ENABLE_IP_FILTER);
        String enableTMAccessControl = p_request
                .getParameter(CompanyConstants.ENABLE_TM_ACCESS_CONTROL);
        String enableTBAccessControl = p_request
                .getParameter(CompanyConstants.ENABLE_TB_ACCESS_CONTROL);
        if (enableIPFilter != null && enableIPFilter.equalsIgnoreCase("on"))
        {
            company.setEnableIPFilter(true);
        }
        else
        {
            company.setEnableIPFilter(false);
        }
        if (enableTMAccessControl != null
                && enableTMAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTMAccessControl(true);
        }
        else
        {
            company.setEnableTMAccessControl(false);
        }
        if (enableTBAccessControl != null
                && enableTBAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTBAccessControl(true);
        }
        else
        {
            company.setEnableTBAccessControl(false);
        }
        String enableSso = p_request
                .getParameter(CompanyConstants.ENABLE_SSO_LOGON);
        company.setEnableSSOLogin(enableSso != null
                && enableSso.equalsIgnoreCase("on"));
        String ssoIdpUrl = p_request.getParameter(CompanyConstants.SSO_IDP_URL);
        company.setSsoIdpUrl(ssoIdpUrl);
        int TM3Version = 2;
        try
        {
            TM3Version = Integer.parseInt(p_request
                    .getParameter(CompanyConstants.TM3_VERSION));
        }
        catch (NumberFormatException nfe)
        {
            TM3Version = 2;
        }
        company.setTmVersion(TmVersion.fromValue(TM3Version));
    }

    /**
     * This method should be in a transacton to make sure each step is
     * successful. Store a company info.
     */
    private Company storeCompanyInfo(HttpServletRequest p_request)
            throws RemoteException, NamingException, GeneralException
    {
        // create the company.
        Company company = new Company();
        company.setName(p_request.getParameter(CompanyConstants.NAME).trim());
        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
        company.setEmail(p_request.getParameter(CompanyConstants.EMAIL));
        company.setSessionTime(p_request
                .getParameter(CompanyConstants.SESSIONTIME));
        String enableIPFilter = p_request
                .getParameter(CompanyConstants.ENABLE_IP_FILTER);
        String enableTMAccessControl = p_request
                .getParameter(CompanyConstants.ENABLE_TM_ACCESS_CONTROL);
        String enableTBAccessControl = p_request
                .getParameter(CompanyConstants.ENABLE_TB_ACCESS_CONTROL);
        if (enableIPFilter != null && enableIPFilter.equalsIgnoreCase("on"))
        {
            company.setEnableIPFilter(true);
        }
        else
        {
            company.setEnableIPFilter(false);
        }
        if (enableTMAccessControl != null
                && enableTMAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTMAccessControl(true);
        }
        else
        {
            company.setEnableTMAccessControl(false);
        }
        if (enableTBAccessControl != null
                && enableTBAccessControl.equalsIgnoreCase("on"))
        {
            company.setEnableTBAccessControl(true);
        }
        else
        {
            company.setEnableTBAccessControl(false);
        }

        String enableSso = p_request
                .getParameter(CompanyConstants.ENABLE_SSO_LOGON);
        company.setEnableSSOLogin(enableSso != null
                && enableSso.equalsIgnoreCase("on"));
        String ssoIdpUrl = p_request.getParameter(CompanyConstants.SSO_IDP_URL);
        company.setSsoIdpUrl(ssoIdpUrl);
        int TM3Version = 2;
        try
        {
            TM3Version = Integer.parseInt(p_request
                    .getParameter(CompanyConstants.TM3_VERSION));
        }
        catch (NumberFormatException nfe)
        {
            TM3Version = 2;
        }
        company.setTmVersion(TmVersion.fromValue(TM3Version));

        // When new a company, this is always 1.
        company.setSeparateTmTuTuvTables(1);

        return company;
    }
}
