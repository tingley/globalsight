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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.HibernateException;

import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.CompanyComparator;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.ling.tm2.TmVersion;
import com.globalsight.persistence.dependencychecking.CompanyDependencyChecker;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

public class CompanyMainHandler extends PageHandler implements CompanyConstants {
    private static final Vector<String> FILTER_NAMES = new Vector<String>();
    private static final Vector<String> KNOWNFORMATIDS = new Vector<String>();
    private static final Vector<String> FILTER_TABLE_NAMES = new Vector<String>();
    private static final Vector<String> FILTER_DESCRIPTION = new Vector<String>();

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
    }
    private File tagsProperties;
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

		// gbs-1389: restrict direct access to company page without view
		// company permission.
		PermissionSet userPerms = (PermissionSet) session
				.getAttribute(WebAppConstants.PERMISSIONS);
		if (!userPerms.getPermissionFor(Permission.COMPANY_VIEW)) 
		{
			p_response.sendRedirect(p_request.getContextPath());
			return;
		}
        try
        {
            if (CompanyConstants.CANCEL.equals(action))
            {
                clearSessionExceptTableInfo(session, CompanyConstants.COMPANY_KEY);
            }
            else if (CompanyConstants.CREATE.equals(action))
            {
				// gbs-1389: restrict direct access to create company without
				// create company permission.
            	if (!userPerms.getPermissionFor(Permission.COMPANY_NEW)) 
        		{
            		if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            		{
            			p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
            		}
            		else
            		{
            			p_response.sendRedirect(p_request.getContextPath());
            		}
        			return;
        		}
                Company company = createCompany(p_request);
                if (company != null)
                {
                    initialFilterConfigurations(company.getId());
                    initialHTMLFilter(company.getId());
                	SessionManager sessionMgr =
                        (SessionManager) session.getAttribute(SESSION_MANAGER);
                	User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
                	ArrayList msg = new ArrayList();
                	msg.add(company.getName());
                	msg.add(Long.toString(company.getId()));
                	msg.add(user);
                	JmsHelper.sendMessageToQueue(msg, JmsHelper.JMS_NEW_COMPANY_QUEUE);
                }
            }
            else if (CompanyConstants.EDIT.equals(action))
            {
            	// gbs-1389: restrict direct access to edit company without
				// edit company permission.
            	if (!userPerms.getPermissionFor(Permission.COMPANY_EDIT)) 
        		{
            		if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            		{
            			p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
            		}
            		else
            		{
            			p_response.sendRedirect(p_request.getContextPath());
            		}
        			return;
        		}
                modifyCompany(session, p_request);
                clearSessionExceptTableInfo(session, CompanyConstants.COMPANY_KEY);
            }
            else if (CompanyConstants.REMOVE.equals(action))
            {
            	// gbs-1389: restrict direct access to remove company without
				// remove company permission.
            	if (!userPerms.getPermissionFor(Permission.COMPANY_REMOVE)) 
        		{
            		if (userPerms.getPermissionFor(Permission.COMPANY_VIEW))
            		{
            			p_response.sendRedirect("/globalsight/ControlServlet?activityName=companies");
            		}
            		else
            		{
            			p_response.sendRedirect(p_request.getContextPath());
            		}
        			return;
        		}
                removeCompany(session, p_request);
            }
            dataForTable(p_request, session);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        catch (JMSException je)
        {
        	throw new EnvoyServletException(je);
        }
        catch (HibernateException e)
        {
            throw new EnvoyServletException(e);
        }
        catch (SQLException e)
        {
            throw new EnvoyServletException(e);
        }
        
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    private void initialHTMLFilter(long companyId)
    {
        URL url = SystemConfiguration.class.getResource("/");
        String prefixPath = "";
        String postPath = "lib/classes/properties/Tags.properties";
        String filePath = null;
        if(url != null)
        {
            try
            {
                prefixPath = url.toURI().getPath(); 
                filePath = prefixPath + postPath;
                tagsProperties = new File(filePath);
                insert(companyId);
            }
            catch (URISyntaxException e)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
            catch (IOException e)
            {
                throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, e);
            }
        }

    }

    public void insert(Long companyId) throws IOException
    {
        String filterName = "HTML_Filter(Default)";
        String filterDescription = "The default html filter.";
        String placeHolderTrim = "embeddable_tags";
        String jsFunctionText = "l10n";
        String embeddableTags = this.getValueByKey("InlineTag_html");
        String defaultEmbeddableTags = embeddableTags;
        String pairedTags = this.getValueByKey("PairedTag_html");
        String defaultPairedTags = pairedTags;
        String unpairedTags = this.getValueByKey("UnpairedTag_html");
        String defaultUnpairedTags = unpairedTags;
        String switchTagMap = getValueByKey("SwitchTagMap_html");
        String defaultSwitchTagMap = switchTagMap;
        String whitePreservingTag = getValueByKey("WhitePreservingTag_html");
        String defaultWhitePreservingTag = whitePreservingTag;
        String nonTranslatableMetaAttribute = getValueByKey("NonTranslatableMetaAttribute_html");
        String defaultNonTranslatableMetaAttribute = nonTranslatableMetaAttribute;
        String translatableAttribute = getValueByKey("TranslatableAttribute_html");
        String defaultTranslatableAttribute = translatableAttribute;
        String localizableAttributeMap = getValueByKey("LocalizableAttributeMap_html");
        String defaultLocalizableAttributeMap = localizableAttributeMap;
        String chEntry = getValueByKey("convertHtmlEntity");
//        String defaultLocalizableInlineAtrributes = "href";
//        String localizableInlineAttributes = "";
        boolean convertHtmlEntity = (chEntry == null || "".equals(chEntry) ? false : Boolean.parseBoolean(chEntry));
        String IIHtmlTags = getValueByKey("IgnoreInvalidHtmlTags");
        boolean ignoreInvalidHtmlTags = ("".equals(IIHtmlTags) ? true : Boolean.parseBoolean(IIHtmlTags));
        HtmlFilter filter = new HtmlFilter(filterName, filterDescription,
                defaultEmbeddableTags, embeddableTags,
                placeHolderTrim, companyId, convertHtmlEntity,
                ignoreInvalidHtmlTags, jsFunctionText,
                defaultPairedTags, pairedTags,
                defaultUnpairedTags, unpairedTags,
                defaultSwitchTagMap, switchTagMap,
                defaultWhitePreservingTag, whitePreservingTag,
                defaultNonTranslatableMetaAttribute,
                nonTranslatableMetaAttribute,
                defaultTranslatableAttribute,
                translatableAttribute
//                defaultLocalizableInlineAtrributes,
//                localizableInlineAttributes
                );
        if(!checkExist(FilterConstants.HTML_TABLENAME, filterName, companyId))
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
    
    private String getValueByKey(String key) throws IOException
    {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(tagsProperties);
        properties.load(fis);
        return properties.getProperty(key);
    }
    
    private void initialFilterConfigurations(long companyId) throws HibernateException, SQLException
    {
        StringBuilder insertSql = new StringBuilder(
                "insert into filter_configuration(name, known_format_id, filter_table_name, filter_description, company_id) values ");
        for (int j = 0; j < FILTER_NAMES.size(); j++)
        {
            insertSql.append("(");
            insertSql.append("'").append(FILTER_NAMES.get(j)).append("',");
            insertSql.append("'").append(KNOWNFORMATIDS.get(j)).append("',");
            insertSql.append("'").append(FILTER_TABLE_NAMES.get(j)).append("',");
            insertSql.append("'").append(FILTER_DESCRIPTION.get(j)).append("',");
            insertSql.append(companyId);
            insertSql.append(")");
            insertSql.append(",");
        }
        insertSql = insertSql.deleteCharAt(insertSql.length() - 1);
        HibernateUtil.executeSql(insertSql.toString());
    }
    
    /**
     * insert resx rule for new companies
     */
    private void initialXMLRule(long companyId) throws HibernateException, SQLException
    {
        StringBuffer insertSql = new StringBuffer("insert into xml_rule " +
        		"(NAME, COMPANY_ID, RULE_TEXT) values ('resx rules',").append(companyId)
        		.append(", '<?xml version=\"1.0\"?><schemarules>")
        		.append("<ruleset schema=\"root\"><dont-translate path=\"/root/xsd:schema\"/>")
        		.append("<dont-translate path=\"/root/xsd:schema//*\"/>")
        		.append("<dont-translate path=\"/root/resheader\"/>")
        		.append("<dont-translate path=\"/root/resheader//*\"/>")
        		.append("<translate path=\"/root/data/value\"/></ruleset></schemarules>')");
        HibernateUtil.executeSql(insertSql.toString());
    }


    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Vector companies = getAllCompanies();
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, companies,
                       new CompanyComparator(uiLocale),
                       10,
                       COMPANY_LIST, COMPANY_KEY);
    }

//    /** 
//     * This method should be in a transacton to make sure each step is successful.
//     * Create an Company.
//     */
//    private Company createCompany(HttpServletRequest p_request)
//        throws RemoteException, NamingException, GeneralException
//    {
//        // create the company.
//        Company company = new Company();
//        company.setName(p_request.getParameter(CompanyConstants.NAME));
//        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
//        company = ServerProxy.getJobHandler().createCompany(company);
//        
//        String companyId = company.getIdAsLong().toString();
//        
//        //Create and insert a pivot currency for each new company.
//        ServerProxy.getCostingEngine().addOrModifyPivotCurrency(companyId);
//
//        //Create and insert a default calendar for each new company.
//        ServerProxy.getCalendarManager().createDefCal(companyId);
//        
//        //Create and insert a list of default file extensions for each new company.
//        ServerProxy.getFileProfilePersistenceManager().createDefaultFileExtension(companyId);
//        
//        //Create and insert a list of default permission group for each new company.
//        Permission.getPermissionManager().createDefaultPermissionGroup(companyId);
//        
//        //Creates default termbase for each new company.
//        String userId = PageHandler.getUser(p_request.getSession()).getUserId();
//        String definitionXml = "<definition><name>Sample</name><description>Sample Termbase</description><languages><language><name>English</name><locale>en</locale><hasterms>true</hasterms></language><language><name>French</name><locale>fr</locale><hasterms>true</hasterms></language><language><name>Spanish</name><locale>es</locale><hasterms>true</hasterms></language><language><name>German</name><locale>de</locale><hasterms>true</hasterms></language></languages><fields></fields></definition>";
//        ServerProxy.getTermbaseManager().create(userId, "", definitionXml, companyId);
//        
//        return company;
//    }
    
    /** 
     * This method should be in a transacton to make sure each step is successful.
     * Create an Company.
     */
    private Company createCompany(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        // create the company.
        Company company = new Company();
        company.setName(p_request.getParameter(CompanyConstants.NAME).trim());
        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
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
        String userId = PageHandler.getUser(p_request.getSession()).getUserId();

        company = ServerProxy.getJobHandler().createCompany(company, userId);
        return company;
    }
    
    private Vector getAllCompanies()
        throws RemoteException, NamingException, GeneralException
    {
        return vectorizedCollection(ServerProxy.getJobHandler().getAllCompanies());
    }

    private void modifyCompany(HttpSession p_session, HttpServletRequest p_request)
         throws RemoteException, NamingException, GeneralException
    {
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Company company = (Company) sessionMgr
                .getAttribute(CompanyConstants.COMPANY);
        company.setDescription(p_request.getParameter(CompanyConstants.DESC));
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
        ServerProxy.getJobHandler().modifyCompany(company);
    }

    /**
     * Remove an Company if there are no dependencies.
     */
    private void removeCompany(HttpSession p_session, HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        String name = (String)p_request.getParameter("name");
        Company company = (Company)ServerProxy.getJobHandler().getCompany(name);
        String deps = checkForDependencies(company, p_session);
        if (deps == null)
        {
            ServerProxy.getJobHandler().removeCompany(company);
        }
        else
        {
            SessionManager sessionMgr = (SessionManager)
                p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
            sessionMgr.setAttribute(DEPENDENCIES, deps);
        }
    }

    /**
     * Check if any objects have dependencies on this company.
     * This should be called BEFORE attempting to remove a company. In fact it's
     * not allowed to remove a company by now
     * <p>
     * 
     * @param p_company
     * @param session
     * @return 
     * @exception EnvoyServletException
     *                   Failed to look for dependencies for the profile.
     *                   The cause is indicated by the exception message.
     * @exception RemoteException
     * @exception GeneralException
     */
    private String checkForDependencies(Company p_company, HttpSession session)
        throws RemoteException, GeneralException
    {
        ResourceBundle bundle = PageHandler.getBundle(session);
        CompanyDependencyChecker depChecker = new CompanyDependencyChecker(); 
        Hashtable catDeps = depChecker.categorizeDependencies(p_company);

        //now convert the hashtable into a Vector of Strings
        StringBuffer deps = new StringBuffer();
        if (catDeps.size() == 0)
            return null;

        deps.append("<span class=\"errorMsg\">");
        Object[] args = {bundle.getString("lb_company")};
        deps.append(MessageFormat.format(bundle.getString("msg_dependency"), args));

        for (Enumeration e = catDeps.keys(); e.hasMoreElements() ;)
        {
            String key = (String)e.nextElement();
            deps.append("<p>*** " + bundle.getString(key) + " ***<br>");
            Vector values = (Vector)catDeps.get(key);
            for (int i = 0 ; i < values.size() ; i++)
            {
                deps.append((String)values.get(i));
                deps.append("<br>");
            }
        }
        deps.append("</span>");
        return deps.toString();
    }
}
