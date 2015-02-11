package com.globalsight.everest.webapp.pagehandler.terminology.maintenance;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.comparator.TermEntryComparator;
import com.globalsight.everest.util.comparator.TermbaseInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Hitlist;
import com.globalsight.terminology.Hitlist.Hit;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseInfo;
import com.globalsight.terminology.TermbaseList;
import com.globalsight.terminology.java.TbConcept;
import com.globalsight.terminology.java.TbLanguage;
import com.globalsight.terminology.java.TbTerm;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.webservices.WebServiceException;

public class TermSearchHandlerHelper
{
    private static final Logger logger = Logger
            .getLogger(TermSearchHandlerHelper.class);

    /**
     * Set label on the page
     * 
     * @param request
     * @param bundle
     */
    public static void setLabel(HttpServletRequest request,
            ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_termbase_search_title");
        setLableToJsp(request, bundle, "help_termbase_search_term");
        setLableToJsp(request, bundle, "jsmsg_tb_maintenance_search_tm_empty");

        setLableToJsp(request, bundle, "lb_match_type");
        setLableToJsp(request, bundle, "lb_fuzzy_match");
        setLableToJsp(request, bundle, "lb_exact_match");

        setLableToJsp(request, bundle, "lb_search_for");
        setLableToJsp(request, bundle, "lb_termbase_select_tms");
        setLableToJsp(request, bundle, "lb_source_locale");
        setLableToJsp(request, bundle, "lb_target_locale");

        setLableToJsp(request, bundle, "lb_first");
        setLableToJsp(request, bundle, "lb_previous");
        setLableToJsp(request, bundle, "lb_next");
        setLableToJsp(request, bundle, "lb_last");
        setLableToJsp(request, bundle, "lb_termbase");
        setLableToJsp(request, bundle, "lb_no_termbase_data_matches");
        setLableToJsp(request, bundle, "lb_termbases");
        setLableToJsp(request, bundle, "lb_ok");
        setLableToJsp(request, bundle, "lb_company");
        setLableToJsp(request, bundle, "lb_terminology_search_entries");
        setLableToJsp(request, bundle, "lb_tm_search");

        setLableToJsp(request, bundle,
                "jsmsg_tb_maintenance_search_srclocale_empty");
        setLableToJsp(request, bundle,
                "jsmsg_tb_maintenance_search_tgtlocale_empty");
        setLableToJsp(request, bundle, "jsmsg_tb_maintenance_search_tm_empty");
        setLableToJsp(request, bundle,
                "jsmsg_tb_maintenance_search_string_empty");

    }

    /**
     * Set locales
     * 
     * @param request
     * @throws LocaleManagerException
     * @throws RemoteException
     */
    public static void setLocales(HttpServletRequest request)
            throws LocaleManagerException, RemoteException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector locales = localeMgr.getAvailableLocales();
        SortUtil.sort(locales,
                new GlobalSightLocaleComparator(Locale.getDefault()));
        request.setAttribute(LocalePairConstants.LOCALES,
                JsonUtil.toJson(locales));
    }

    /**
     * Set TBs
     * 
     * @param request
     * @throws LocaleManagerException
     * @throws RemoteException
     */
    public static void setTBs(HttpServletRequest request, String userId,
            Locale uiLocale) throws LocaleManagerException, RemoteException
    {
        List<TermbaseInfo> tbList = new ArrayList<TermbaseInfo>();
        List<String> companies = null;
        ITermbaseManager manager = ServerProxy.getTermbaseManager();
        List<TermbaseInfo> allTBs = new ArrayList<TermbaseInfo>();
        try
        {
            allTBs.addAll(manager.getTermbaseList(uiLocale));
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        Company currentCompany = CompanyWrapper
                .getCompanyById(currentCompanyId);
        boolean enableTBAccessControl = currentCompany
                .getEnableTBAccessControl();

        boolean isAdmin = UserUtil.isInPermissionGroup(userId, "Administrator");
        boolean isSuperPM = UserUtil.isSuperPM(userId);
        boolean isSuperLP = UserUtil.isSuperLP(userId);

        if ("1".equals(currentCompanyId))
        {
            companies = new ArrayList<String>();
            if (isSuperLP)
            {
                // Get all the companies the super translator worked for
                List projectList = null;
                try
                {
                    projectList = ServerProxy.getProjectHandler()
                            .getProjectsByUser(userId);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (projectList.size() < 1)
                {
                    allTBs = new ArrayList<TermbaseInfo>();
                }
                for (Iterator it = projectList.iterator(); it.hasNext();)
                {
                    Project pj = (Project) it.next();
                    String companyName = CompanyWrapper.getCompanyNameById(pj
                            .getCompanyId());
                    if (!companies.contains(companyName))
                    {
                        companies.add(companyName);
                    }
                }
            }
            else
            {
                // Super admin
                String[] companyNames = CompanyWrapper.getAllCompanyNames();
                for (String name : companyNames)
                {
                    companies.add(name);
                }

            }
            SortUtil.sort(companies, new StringComparator(Locale.getDefault()));
            tbList = allTBs;
        }
        else
        {
            if (enableTBAccessControl && !isAdmin)
            {

                ArrayList<Long> tbListOfUser = new ArrayList<Long>();
                ProjectTMTBUsers projectTMTBUsers = new ProjectTMTBUsers();
                List tbIdList = projectTMTBUsers.getTList(userId, "TB");
                Iterator it = tbIdList.iterator();
                while (it.hasNext())
                {
                    long id = ((BigInteger) it.next()).longValue();
                    Termbase tb = TermbaseList.get(id);

                    if (isSuperPM)
                    {
                        if (tb != null
                                && tb.getCompanyId().equals(currentCompanyId))
                        {
                            tbListOfUser.add(id);
                        }
                    }
                    else
                    {
                        if (tb != null)
                        {
                            tbListOfUser.add(id);
                        }
                    }
                }

                Iterator<TermbaseInfo> itAllTBs = allTBs.iterator();
                while (itAllTBs.hasNext())
                {
                    TermbaseInfo tbi = (TermbaseInfo) itAllTBs.next();
                    if (tbListOfUser.contains(tbi.getTermbaseId()))
                    {
                        tbList.add(tbi);
                    }
                }
            }
            else
            {
                tbList = allTBs;
            }
        }
        SortUtil.sort(tbList, new TermbaseInfoComparator(0, uiLocale));
        List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        for (Iterator<TermbaseInfo> it = tbList.iterator(); it.hasNext();)
        {
            TermbaseInfo tb = it.next();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", tb.getName());
            map.put("company",
                    CompanyWrapper.getCompanyNameById(tb.getCompanyId()));
            list.add(map);
        }
        request.setAttribute("tbs", JsonUtil.toJson(list));
        request.setAttribute("companies", JsonUtil.toJson(companies));
    }

    /**
     * Search in TB
     * 
     * @param termbaseNames
     * @param searchString
     * @param sourceLocale
     * @param targetLocale
     * @param matchType
     * @return
     * @throws WebServiceException
     */
    public static String search(HttpServletRequest request, Locale uiLocale,
            String[] termbaseNames, String searchString, String sourceLocale,
            String targetLocale, String searchType) throws Exception
    {
        List<Map<String, String>> searchResult = new ArrayList<Map<String, String>>();
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            for (String termbaseName : termbaseNames)
            {
                Termbase searchTB = null;
                try
                {
                    searchTB = TermbaseList.get(termbaseName);
                }
                catch (Exception e)
                {
                    continue;
                }

                if (searchTB == null)
                {
                    continue;
                }
                // Valid source language name list
                List<String> validSrcLangNameList = new ArrayList<String>();
                validSrcLangNameList = getValidLangNameList(sourceLocale,
                        searchTB, searchType, connection);

                // Valid target language name list
                List<String> validTrgLangNameList = new ArrayList<String>();
                validTrgLangNameList = getValidLangNameList(targetLocale,
                        searchTB, searchType, connection);

                // Search in term base

                if (validSrcLangNameList.size() > 0)
                {
                    Iterator<String> srcLangIter = validSrcLangNameList
                            .iterator();
                    while (srcLangIter.hasNext())
                    {
                        String sourceLang = srcLangIter.next();
                        String targetLang = null;

                        if (validTrgLangNameList.size() > 0)
                        {
                            Iterator<String> trgLangIter = validTrgLangNameList
                                    .iterator();
                            while (trgLangIter.hasNext())
                            {
                                targetLang = trgLangIter.next();
                                searchResult.addAll(searchHitlist(sourceLocale,
                                        targetLocale, sourceLang, targetLang,
                                        searchString, searchType, searchTB));
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            if (connection != null)
                ConnectionPool.returnConnection(connection);
        }

        if (searchResult.size() == 0)
        {
            return "";
        }

        // Default ordered by TBNAME ASC
        SortUtil.sort(searchResult, new TermEntryComparator(
                TermEntryComparator.SRC_TERM_ASC, uiLocale));
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        sessionMgr.setAttribute("searchResult", searchResult);

        // Get the first 10 records
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        int max = searchResult.size() < 11 ? searchResult.size() : 10;
        for (int i = 0; i < max; i++)
        {
            list.add(searchResult.get(i));
        }
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("result", list);
        temp.put("totalNum", searchResult.size());

        return JsonUtil.toJson(temp);
    }

    /**
     * First, Next, Previous, Last...paging
     * 
     * @return
     */
    public static String refreshPage(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        List result = (List) sessionMgr.getAttribute("searchResult");
        String page = (String) request.getParameter("page");

        List<Object> list = new ArrayList<Object>();
        if (result != null)
        {
            int i = (Integer.parseInt(page) - 1) * 10;
            int max = i + 9 < result.size() ? i + 10 : result.size();
            for (; i < max; i++)
            {
                list.add(result.get(i));
            }
        }
        else
        {
            return "";
        }

        return JsonUtil.toJson(list);
    }

    /**
     * Refresh Order
     * 
     * @return
     */
    public static String refreshOrder(HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        List<Map<String, String>> searchResult = (List<Map<String, String>>) sessionMgr
                .getAttribute("searchResult");
        String orderBy = (String) request.getParameter("orderBy");

        SortUtil.sort(searchResult,
                new TermEntryComparator(Integer.parseInt(orderBy), uiLocale));
        sessionMgr.setAttribute("searchResult", null);
        sessionMgr.setAttribute("searchResult", searchResult);
        // Get the first 10 records
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        int max = searchResult.size() < 11 ? searchResult.size() : 10;
        for (int i = 0; i < max; i++)
        {
            list.add(searchResult.get(i));
        }
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("result", list);
        temp.put("totalNum", searchResult.size());

        return JsonUtil.toJson(list);
    }

    private static List<Map<String, String>> searchHitlist(String sourceLocale,
            String targetLoacle, String srcLang, String trgLang,
            String searchString, String searchType, Termbase searchTB)
    {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        String tbName = searchTB.getName();
        Hitlist hitList = searchTB.searchHitlist(srcLang, trgLang,
                searchString, searchType, 300, 0);
        Iterator<Hit> hitIter = hitList.getHits().iterator();
        while (hitIter.hasNext())
        {
            Hit hit = hitIter.next();
            TbConcept concept = HibernateUtil.get(TbConcept.class,
                    hit.getConceptId());
            Iterator<TbLanguage> langIter = concept.getLanguages().iterator();

            String srcTerm = "";
            List<String> targetTerms = new ArrayList<String>();

            while (langIter.hasNext())
            {
                TbLanguage tl = (TbLanguage) langIter.next();
                Iterator<TbTerm> termsIter = tl.getTerms().iterator();
                while (termsIter.hasNext())
                {
                    TbTerm term = termsIter.next();
                    String termContent = term.getTermContent();
                    String language = term.getLanguage();
                    String locale = term.getTbLanguage().getLocal();
                    // Some locale is en-US, need to convert to en_US to
                    // compare
                    locale = locale.replace("-", "_");
                    if (language.equals(srcLang)
                            && (locale.equals(sourceLocale) || sourceLocale
                                    .startsWith(locale)))
                    {
                        srcTerm = termContent;
                    }

                    if (language.equalsIgnoreCase(trgLang)
                            && (locale.equals(targetLoacle) || targetLoacle
                                    .startsWith(locale)))
                    {
                        targetTerms.add(termContent);
                    }
                }
            }
            for (String targetTerm : targetTerms)
            {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("tbname", tbName);
                map.put("src_term", srcTerm);
                map.put("target_term", targetTerm);
                result.add(map);
            }
        }

        return result;
    }

    private static List<String> getValidLangNameList(String langName,
            Termbase searchTB, String searchType, Connection connection)
    {
        List<String> result = new ArrayList<String>();

        List<String> candidateLangNameList = getLangNameByLocale(langName,
                searchTB, connection);
        if (candidateLangNameList.size() > 0)
        {
            // If exact search, don't check if "locale" is in the definition
            // of TB(Exact search does not need indexing)
            if ("exact".equals(searchType))
            {
                result = candidateLangNameList;
            }
            else
            {
                Iterator<String> iter = candidateLangNameList.iterator();
                while (iter.hasNext())
                {
                    String lName = iter.next();
                    String locale = searchTB.getLocaleByLanguage(lName);
                    // Current TB has defined this language name.
                    if (locale != null)
                    {
                        result.add(lName);
                    }
                }
            }
        }

        result = filterLangNameList(langName, result);

        return result;
    }

    private static List<String> getLangNameByLocale(String queryLocale,
            Termbase tb, Connection connection)
    {
        List<String> langList = new ArrayList<String>();
        String lang_country = ImportUtil.normalizeLocale(queryLocale);
        String lang = "";
        try
        {
            lang = lang_country.substring(0, 2);
        }
        catch (Exception ex)
        {
            String msg = "invalid locale : " + queryLocale;
            logger.error(msg, ex);
        }

        PreparedStatement query = null;
        ResultSet results = null;

        try
        {
            StringBuffer sbSQL = new StringBuffer();
            sbSQL.append("SELECT DISTINCT NAME FROM TB_LANGUAGE "
                    + " WHERE LOCALE LIKE '%" + lang + "%' ");
            sbSQL.append(" AND TBID = " + tb.getId());
            query = connection.prepareStatement(sbSQL.toString());
            results = query.executeQuery();
            while (results.next())
            {
                langList.add(results.getString("NAME"));
            }
        }
        catch (Exception e)
        {
            String message = "Fail to get lang_name for " + queryLocale;
            logger.error(message, e);
        }
        finally
        {
            try
            {
                if (results != null)
                {
                    results.close();
                }
                if (query != null)
                {
                    query.close();
                }
            }
            catch (Exception e)
            {
                logger.error("Exception when close connection", e);
            }
        }

        return langList;
    }

    private static List<String> filterLangNameList(String locale,
            List<String> langNameList)
    {
        List<String> result = new ArrayList<String>();

        String lang = "";
        String country = "";
        locale = ImportUtil.normalizeLocale(locale);
        int index = locale.indexOf("_");
        if (index == -1)
        {
            index = locale.indexOf("-");
        }
        if (index > -1)
        {
            lang = locale.substring(0, index);
            country = locale.substring(index + 1, locale.length());
        }

        Iterator<String> it = langNameList.iterator();
        while (it.hasNext())
        {
            String langName = it.next();

            boolean flag = true;
            // "zh_CN" && "zh_TW"
            if ("zh".equals(lang) && "CN".equalsIgnoreCase(country)
                    && langName.length() >= 5
                    && "zh".equalsIgnoreCase(langName.substring(0, 2))
                    && "TW".equalsIgnoreCase(langName.substring(3, 5)))
            {
                flag = false;
            }
            if ("zh".equals(lang) && "TW".equalsIgnoreCase(country)
                    && langName.length() >= 5
                    && "zh".equalsIgnoreCase(langName.substring(0, 2))
                    && "CN".equalsIgnoreCase(langName.substring(3, 5)))
            {
                flag = false;
            }

            if (flag)
            {
                result.add(langName);
            }
        }

        return result;
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private static void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }
}
