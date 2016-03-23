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
package com.globalsight.everest.localemgr;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * <p>
 * LocaleManagerLocal
 * </p>
 * 
 * <p>
 * Manages all the locales available for the application and for the users to
 * translate from/to
 * </p>
 * .
 */
public class LocaleManagerLocal implements LocaleManager
{
    private static final Logger CATEGORY = Logger
            .getLogger(LocaleManagerLocal.class.getName());

    private static String SEPARATOR = "_";

    // IANA names for the encodings we most often see
    private static final String UTF8 = "UTF-8".intern();
    private static final String WINDOWS_1252 = "Windows-1252".intern();

    /**
     * Persist a GlobalSightLocale
     * 
     * @param GlobalSightLocale
     * @return GlobalSightLocale that is persisted
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public GlobalSightLocale addLocale(GlobalSightLocale p_locale)
            throws LocaleManagerException, RemoteException
    {
        String language = p_locale.getLanguage();
        String country = p_locale.getCountry();
        addLanguage(language);
        addCountry(country);
        Session session = null;
        Transaction transaction = null;

        GlobalSightLocale locale = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            String hql = "from GlobalSightLocale l where l.language = :lang "
                    + "and l.country = :coun";
            Map<String, String> map = new HashMap<String, String>();
            map.put("lang", language);
            map.put("coun", country);
            List result = HibernateUtil.search(hql, map);
            if (result != null && result.size() > 0)
            {
                locale = (GlobalSightLocale) result.get(0);
            }
            else
            {
				locale = new GlobalSightLocale(p_locale.getLanguage(),
						p_locale.getCountry(), false);
            }

            session.saveOrUpdate(locale);
            // only commit if all roles added successfully
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            CATEGORY.error("Failed to add locale " + p_locale.toString(), e);
            String args[] =
            { p_locale.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_ADD_LOCALE, args, e);
        }
        finally
        {
            if (session != null)
            {
                // //session.close();
            }
        }
        return locale;
    }

    /**
     * Add a country to database if it doesn't exist.
     * 
     * @param country
     *            the country name to be added.
     */
    private void addCountry(String country)
    {
        Connection c = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement("select * from country where ISO_COUNTRY_CODE = ?");
            ps.setString(1, country);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                ps = c.prepareStatement("insert into country values (?)");
                ps.setString(1, country);
                ps.executeUpdate();
            }

        }
        catch (Exception ex)
        {
            throw new LocaleManagerException(
                    "Failed to add country " + country, null, ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }

    }

    /**
     * Add a language to database if it doesn't exist.
     * 
     * @param language
     */
    private void addLanguage(String language)
    {
        Connection c = null;
        ResultSet rs = null;
        PreparedStatement ps = null;

        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement("select * from language where ISO_LANG_CODE = ?");
            ps.setString(1, language);
            rs = ps.executeQuery();
            if (!rs.next())
            {
                ps = c.prepareStatement("insert into language values (?)");
                ps.setString(1, language);
                ps.executeUpdate();
            }

        }
        catch (Exception ex)
        {
            throw new LocaleManagerException("Failed to add language "
                    + language, null, ex);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Get all the available locales supported by the system.
     * <p>
     * 
     * @return Vector A vector of all the GlobalSightLocales that the system can
     *         support.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAvailableLocales() throws LocaleManagerException,
            RemoteException
    {
        try
        {
            String hql = "from GlobalSightLocale";
            return new Vector(HibernateUtil.search(hql, null));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all available locales", pe);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALES, null, pe);
        }
    }

    /**
     * Get all the available locales supported by the system.
     * <p>
     * 
     * @return Vector A vector of all the locales (GlobalSightLocales) that is
     *         supported by the UI.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getSupportedLocalesForUi() throws LocaleManagerException,
            RemoteException
    {
        // The supported UI locales will remain the same as long as
        // the server is up and running.
        String[] supportedLocales = null;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            supportedLocales = sc.getStrings(SystemConfigParamNames.UI_LOCALES);
        }
        catch (Exception e)
        {
            supportedLocales = new String[1];
            supportedLocales[0] = "en_US";
        }

        try
        {
            List supportedLocalesList = Arrays.asList(supportedLocales);
            return vectorizedCollection(supportedLocalesList);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get all supported locales for UI.", e);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALES, null, e);
        }
    }

    /**
     * Get a particular locale based on the id.
     * <p>
     * 
     * @return A locale based on the specified id.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public GlobalSightLocale getLocaleById(long p_id)
            throws LocaleManagerException, RemoteException
    {
        try
        {
            return (GlobalSightLocale) HibernateUtil.get(
                    GlobalSightLocale.class, p_id, false);
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get Locale by Id " + p_id, pe);
            String messageArgs[] =
            { String.valueOf(p_id) };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE,
                    messageArgs, pe);
        }
    }

    /**
     * Get a particular locale based on the string "language code"_"country
     * code"
     * <p>
     * 
     * @return A locale based on the specified string or NULL if it couldn't be
     *         found.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public GlobalSightLocale getLocaleByString(String p_localeString)
            throws LocaleManagerException, RemoteException
    {
        GlobalSightLocale locale = null;

        if (p_localeString == null || p_localeString.length() <= 1)
        {
            return null;
        }
        if (p_localeString.equalsIgnoreCase("iw_IL"))
        {
            p_localeString = "he_IL";
        }
        StringTokenizer tokenizer = new StringTokenizer(p_localeString,
                SEPARATOR);

        // should at least be 2 (ll_cc) or at most 3 (ll_cc_vv)
        String language = tokenizer.nextToken();
        String country = "";

        if (p_localeString.length() > 3)
        {
            country = tokenizer.nextToken();
        }

        try
        {
            String hql = "from GlobalSightLocale l "
                    + "where l.language = :language and l.country = :country";
            HashMap map = new HashMap();
            map.put("language", language);
            map.put("country", country);

            List locales = HibernateUtil.search(hql, map);
            if (locales != null && locales.size() > 0)
            {
                locale = (GlobalSightLocale) locales.get(0);
            }
            else
            {
                // In the database, the special new locale (he, yi, id) can be
                // storied as old locale (iw, ji, in).
                map.put("language", handleSpecialLocales(language));

                locales = HibernateUtil.search(hql, map);
                if (locales != null && locales.size() > 0)
                {
                    locale = (GlobalSightLocale) locales.get(0);
                }
            }
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get locale by string " + p_localeString,
                    pe);
            String messageArgs[] =
            { p_localeString };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE,
                    messageArgs, pe);
        }

        return locale;
    }

    /**
     * Returns a string representation of the object. Converts any old iso
     * language codes and the new codes.
     * 
     * @return a string representation of the object.
     */
    private String handleSpecialLocales(String language)
    {
        if ("he".equals(language))
        {
            language = "iw";
            return language;
        }
        if ("iw".equals(language))
        {
            language = "he";
            return language;
        }
        if ("yi".equals(language))
        {
            language = "ji";
            return language;
        }
        if ("ji".equals(language))
        {
            language = "yi";
            return language;
        }
        if ("id".equals(language))
        {
            language = "in";
            return language;
        }
        if ("in".equals(language))
        {
            language = "id";
            return language;
        }
        return language;
    }

    /**
     * Add a source/target locales pair. After this, the source/target locale
     * pair becomes valid for a localization profile.
     * <p>
     * 
     * @param p_source
     *            The source locale
     * @param p_target
     *            The target locale
     * @param companyId
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair addSourceTargetLocalePair(GlobalSightLocale p_source,
            GlobalSightLocale p_target, long companyId)
            throws LocaleManagerException, RemoteException
    {
        Session session = null;
        Transaction transaction = null;
        LocalePair lp = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            String hql = "from LocalePair l where l.source.id = :sId "
                    + "and l.target.id = :tId and l.companyId = :cId";
            Map map = new HashMap();
            map.put("sId", p_source.getIdAsLong());
            map.put("tId", p_target.getIdAsLong());
            map.put("cId", companyId);
            List result = HibernateUtil.search(hql, map);
            if (result != null && result.size() > 0)
            {
                lp = (LocalePair) result.get(0);
                lp.setIsActive(true);
            }
            else
            {
                lp = new LocalePair(p_source, p_target, companyId);
            }

            session.saveOrUpdate(lp);
            // add all the roles
            addRoles(p_source, p_target);
            // only commit if all roles added successfully
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            CATEGORY.error("Failed to add a locale pair" + p_source.toString()
                    + " : " + p_target.toString(), e);
            String args[] =
            { p_source.toString(), p_target.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_ADD_LOCALE_PAIR, args,
                    e);
        }
        finally
        {
            if (session != null)
            {
                // //session.close();
            }
        }

        return lp;
    }

    /**
     * Remove a source/target locales pair
     * <p>
     * 
     * @param LocalePair
     *            The locales Pair
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public void removeSourceTargetLocalePair(LocalePair p_thePair)
            throws LocaleManagerException, RemoteException
    {
        try
        {
            // When modify Role, this method need to be modified.
            removeRoles(p_thePair);
            HibernateUtil.delete(p_thePair);
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Failed to remove locale pair " + p_thePair.toString(), e);
            String args[] =
            { p_thePair.getSource().toString(),
                    p_thePair.getTarget().toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_REMOVE_LOCALE_PAIR,
                    args, e);
        }
    }

    /**
     * Get all the locales which are the source locale in all the source/target
     * locale pairs with specified company id
     * <p>
     * 
     * @return A vector all the GlobalSightLocales that are specified as sources
     *         in LocalePairs.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllSourceLocalesByCompanyId(String p_companyId)
            throws LocaleManagerException, RemoteException
    {
        if (p_companyId == null || p_companyId.trim().length() == 0)
        {
            return new Vector();
        }

        try
        {
            String hql = "select distinct lp.source from LocalePair lp"
                    + " where lp.isActive = 'Y' and lp.companyId = :companyId";
            HashMap map = new HashMap();
            map.put("companyId", Long.parseLong(p_companyId));

            return new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all source locales", pe);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_SOURCE_LOCALES,
                    null, pe);
        }
    }

    /**
     * Get all the locales which are the source locale in all the source/target
     * locale pairs
     * <p>
     * 
     * @return A vector all the GlobalSightLocales that are specified as sources
     *         in LocalePairs.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllSourceLocales() throws LocaleManagerException,
            RemoteException
    {
        try
        {
            String hql = "select distinct lp.source from LocalePair lp where lp.isActive = 'Y'";

            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and lp.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            return new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all source locales", pe);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_SOURCE_LOCALES,
                    null, pe);
        }
    }

    /**
     * Get all the distinct locales which are the targets locale in all the
     * source/target locale pairs
     * 
     * @return A vector of all the GlobalSightLocales that are specified as
     *         sources in LocalePairs.
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllTargetLocales() throws LocaleManagerException,
            RemoteException
    {
        try
        {
            String hql = "select distinct lp.target from LocalePair lp where lp.isActive = 'Y'";

            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and lp.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            return new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all target locales.", pe);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_TARGET_LOCALES,
                    null, pe);
        }
    }

    /**
     * Get all the source/target locales pairs
     * <p>
     * 
     * @return A list of LocalePair's (the source/target locale pairs)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getSourceTargetLocalePairs() throws LocaleManagerException,
            RemoteException
    {
        try
        {
            String hql = "from LocalePair lp where lp.isActive = 'Y'";

            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and lp.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            return new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all locale pairs.", pe);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE_PAIRS,
                    null, pe);
        }
    }

    /**
     * Get a source/target locales pair based on the given id.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairById(long p_localePairId)
            throws LocaleManagerException, RemoteException
    {
        try
        {
            return (LocalePair) HibernateUtil.get(LocalePair.class,
                    p_localePairId);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_localePairId);

            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE_PAIR_BY_ID,
                    args, e);
        }
    }

    /**
     * Get a locale pair object based on the given source and target ids.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairBySourceTargetIds(long p_sourceLocaleId,
            long p_targetLocaleId) throws LocaleManagerException,
            RemoteException
    {
        LocalePair localePair = null;

        try
        {
            String hql = "from LocalePair lp where lp.isActive = 'Y' "
                    + "and lp.source.id = :sId and lp.target.id = :tId";

            HashMap map = new HashMap();
            map.put("sId", new Long(p_sourceLocaleId));
            map.put("tId", new Long(p_targetLocaleId));

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and lp.companyId = :companyId";
                map.put("companyId", Long.parseLong(currentId));
            }

            List localePairs = HibernateUtil.search(hql, map);
            if (localePairs != null && localePairs.size() > 0)
            {
                localePair = (LocalePair) localePairs.get(0);
            }
        }
        catch (Exception e)
        {
            String[] args = new String[2];
            args[0] = Long.toString(p_sourceLocaleId);
            args[1] = Long.toString(p_targetLocaleId);

            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE_PAIR_BY_SRC_TRGT_IDs,
                    args, e);
        }

        return localePair;
    }

    /**
     * Get a locale pair object based on the given source, target string (ie.
     * en_US, fr_FR_), and a company ID.
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairBySourceTargetAndCompanyStrings(
            String p_sourceLocaleString, String p_targetLocaleString,
            long companyId) throws LocaleManagerException, RemoteException
    {
        if (p_sourceLocaleString == null || p_sourceLocaleString.length() <= 1
                || p_targetLocaleString == null
                || p_targetLocaleString.length() <= 1)
        {
            return null;
        }
        LocalePair localePair = null;

        // source
        StringTokenizer srcTokenizer = new StringTokenizer(
                p_sourceLocaleString, SEPARATOR);

        // should at least be 2 (ll_cc) or at most 3 (ll_cc_vv)
        String language = srcTokenizer.nextToken();
        String country = "";

        if (p_sourceLocaleString.length() > 3)
        {
            country = srcTokenizer.nextToken();
        }

        HashMap map = new HashMap();
        map.put("sLanguage", language);
        map.put("sCountry", country);

        // target
        StringTokenizer trgTokenizer = new StringTokenizer(
                p_targetLocaleString, SEPARATOR);
        language = trgTokenizer.nextToken();
        country = "";

        if (p_targetLocaleString.length() > 3)
        {
            country = trgTokenizer.nextToken();
        }

        map.put("tLanguage", language);
        map.put("tCountry", country);

        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("from LocalePair lp where lp.isActive = 'Y'")
                    .append(" and lp.source.language = :sLanguage ")
                    .append(" and lp.source.country = :sCountry ")
                    .append(" and lp.target.language = :tLanguage ")
                    .append(" and lp.target.country = :tCountry");

            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                hql.append(" and lp.companyId = :companyId");
                map.put("companyId", companyId);
            }

            List localePairs = HibernateUtil.search(hql.toString(), map);
            if (localePairs != null && localePairs.size() > 0)
            {
                localePair = (LocalePair) localePairs.get(0);
            }
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = p_sourceLocaleString;
            errorArgs[1] = p_targetLocaleString;

            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE_PAIR_BY_SRC_TRGT_STRINGS,
                    errorArgs, e);
        }

        return localePair;
    }

    /**
     * Get a locale pair object based on the given source and target string (ie.
     * en_US, fr_FR_)
     * 
     * @return A LocalePair object (the source/target locale pair)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public LocalePair getLocalePairBySourceTargetStrings(
            String p_sourceLocaleString, String p_targetLocaleString)
            throws LocaleManagerException, RemoteException
    {
        if (p_sourceLocaleString == null || p_sourceLocaleString.length() <= 1
                || p_targetLocaleString == null
                || p_targetLocaleString.length() <= 1)
        {
            return null;
        }

        LocalePair localePair = null;

        // source
        StringTokenizer srcTokenizer = new StringTokenizer(
                p_sourceLocaleString, SEPARATOR);

        // should at least be 2 (ll_cc) or at most 3 (ll_cc_vv)
        String language = srcTokenizer.nextToken();
        String country = "";

        if (p_sourceLocaleString.length() > 3)
        {
            country = srcTokenizer.nextToken();
        }

        HashMap map = new HashMap();
        map.put("sLanguage", language);
        map.put("sCountry", country);

        // target
        StringTokenizer trgTokenizer = new StringTokenizer(
                p_targetLocaleString, SEPARATOR);
        language = trgTokenizer.nextToken();
        country = "";

        if (p_targetLocaleString.length() > 3)
        {
            country = trgTokenizer.nextToken();
        }

        map.put("tLanguage", language);
        map.put("tCountry", country);

        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("from LocalePair lp where lp.isActive = 'Y'")
                    .append(" and lp.source.language = :sLanguage ")
                    .append(" and lp.source.country = :sCountry ")
                    .append(" and lp.target.language = :tLanguage ")
                    .append(" and lp.target.country = :tCountry");

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql.append(" and lp.companyId = :companyId");
                map.put("companyId", Long.parseLong(currentId));
            }

            List localePairs = HibernateUtil.search(hql.toString(), map);
            if (localePairs != null && localePairs.size() > 0)
            {
                localePair = (LocalePair) localePairs.get(0);
            }
        }
        catch (Exception e)
        {
            String[] errorArgs = new String[2];
            errorArgs[0] = p_sourceLocaleString;
            errorArgs[1] = p_targetLocaleString;

            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_LOCALE_PAIR_BY_SRC_TRGT_STRINGS,
                    errorArgs, e);
        }

        return localePair;
    }

    /**
     * For a given source locale, get the list of target locales that it is
     * paired with and specified company id.
     * <p>
     * 
     * @param GlobalSightLocale
     *            The source locale
     * @return A list of GlobalSightLocale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getTargetLocalesByCompanyId(GlobalSightLocale p_sourceLocale,
            String p_companyId) throws LocaleManagerException, RemoteException
    {
        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("select distinct lp.target from LocalePair lp ")
                    .append(" where lp.isActive = 'Y' and lp.source.id = :sId")
                    .append(" and lp.companyId = :companyId");

            HashMap map = new HashMap();
            map.put("sId", p_sourceLocale.getIdAsLong());
            map.put("companyId", Long.parseLong(p_companyId));

            return new Vector(HibernateUtil.search(hql.toString(), map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all target locales associated with "
                    + "source locale " + p_sourceLocale.toString(), pe);
            String args[] =
            { p_sourceLocale.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_TARGET_LOCALES_BY_SOURCE,
                    args, pe);
        }
    }

    /**
     * For a given source locale, get the list of target locales that it is
     * paired with.
     * <p>
     * 
     * @param GlobalSightLocale
     *            The source locale
     * @return A list of GlobalSightLocale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getTargetLocales(GlobalSightLocale p_sourceLocale)
            throws LocaleManagerException, RemoteException
    {
        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append("select distinct lp.target from LocalePair lp ").append(
                    " where lp.isActive = 'Y' and lp.source.id = :sId");

            HashMap map = new HashMap();
            map.put("sId", p_sourceLocale.getIdAsLong());

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql.append(" and lp.companyId = :companyId");
                map.put("companyId", Long.parseLong(currentId));
            }

            return new Vector(HibernateUtil.search(hql.toString(), map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all target locales associated with "
                    + "source locale " + p_sourceLocale.toString(), pe);
            String args[] =
            { p_sourceLocale.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_TARGET_LOCALES_BY_SOURCE,
                    args, pe);
        }
    }

    /**
     * For a given source locale, get the list of target locales that it is
     * paired with.
     * <p>
     * 
     * @param GlobalSightLocale
     *            The source locale
     * @return A list of GlobalSightLocale's (the target locales)
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getTargetLocalesByProject(GlobalSightLocale p_sourceLocale,
            String p_project) throws LocaleManagerException, RemoteException
    {
        try
        {
            StringBuffer hql = new StringBuffer();
            hql.append(
                    "select distinct wf.targetLocale from WorkflowTemplateInfo wf")
                    .append(" where wf.project.id = :pId and wf.isActive='Y' and wf.sourceLocale.id = :sId");

            HashMap map = new HashMap();
            map.put("sId", p_sourceLocale.getIdAsLong());
            map.put("pId", Long.parseLong(p_project));

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql.append(" and wf.companyId = :companyId");
                map.put("companyId", Long.parseLong(currentId));
            }

            return new Vector(HibernateUtil.search(hql.toString(), map));
        }
        catch (Exception pe)
        {
            CATEGORY.error("Failed to get all target locales associated with "
                    + "source locale " + p_sourceLocale.toString(), pe);
            String args[] =
            { p_sourceLocale.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_TARGET_LOCALES_BY_SOURCE,
                    args, pe);
        }
    }

    private Vector vectorizedCollection(Collection p_collection)
    {
        return new Vector(p_collection);
    }

    /**
     * Retrieve all codesets in the datastore
     * 
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllCodeSets() throws LocaleManagerException,
            RemoteException
    {
        Vector codeSets = null;
        try
        {
            String hql = "from CodeSetImpl";
            codeSets = new Vector(HibernateUtil.search(hql, null));
        }
        catch (Exception ex)
        {
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_CODE_SETS, null,
                    ex);
        }

        reOrderCodeSets(codeSets);
        return codeSets;
    }

    /**
     * Reorders the codesets such that utf8 and windows1252 are at the front of
     * the list in sorted order
     */
    private void reOrderCodeSets(Vector p_codeSets)
    {
        int size = p_codeSets.size();
        CodeSet utf8 = null;
        CodeSet win1252 = null;

        for (int i = 0; i < size; i++)
        {
            CodeSet cs = (CodeSet) p_codeSets.get(i);
            String codeSet = cs.getCodeSet().intern();
            if (UTF8 == codeSet)
            {
                utf8 = cs;
            }
            else
            {
                if (WINDOWS_1252 == codeSet)
                {
                    win1252 = cs;
                }
            }

            if (utf8 != null && win1252 != null)
                break;
        }

        if (utf8 != null && win1252 != null)
        {
            p_codeSets.remove(utf8);
            p_codeSets.remove(win1252);
            p_codeSets.insertElementAt(utf8, 0);
            p_codeSets.insertElementAt(win1252, 1);
        }
    }

    /**
     * Retrieve all codesets associated with the specific locale id.
     * 
     * @exception LocaleManagerException
     *                Specifies the error, probably persistence exception
     * @exception RemoteException
     *                System or network related exception
     */
    public Vector getAllCodeSets(long p_localeId)
            throws LocaleManagerException, RemoteException
    {
        Vector codeSets = null;

        try
        {
            String hql = "select distinct lSet.codeSet from LocaleCodeSet lSet where lSet.local.id = :lId";
            HashMap map = new HashMap();
            map.put("lId", new Long(p_localeId));
            codeSets = new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception ex)
        {
            String args[] =
            { String.valueOf(p_localeId) };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_CODE_SETS_BY_LOCALE,
                    args, ex);
        }

        return codeSets;
    }

    /**
     * Adds container roles for the source and target locale pair combined with
     * the available activities.
     */
    private void addRoles(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale) throws LocaleManagerException
    {
        Role role = null;
        try
        {
            JobHandler jh = ServerProxy.getJobHandler();
            UserManager um = ServerProxy.getUserManager();

            Collection cActivities = jh.getAllActivities();
            Object activities[] = cActivities.toArray();
            for (int i = 0; i < activities.length; i++)
            {
                Activity curActivity = (Activity) activities[i];

                // create all the container roles for this
                // source/target locale pair combination with activity
                role = um.createContainerRole();
                role.setActivity(curActivity);
                role.setSourceLocale(p_sourceLocale.toString());
                role.setTargetLocale(p_targetLocale.toString());
                um.addRole(role);
            }
        }
        catch (JobException je)
        {
            CATEGORY.error(
                    "Failed to get all the activities for creating roles for locale pair "
                            + p_sourceLocale + " to " + p_targetLocale, je);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_GET_ACTIVITES, null,
                    je);
        }
        catch (UserManagerException ume)
        {
            CATEGORY.error("Failed to add role " + role.toString(), ume);
            String args[] =
            { role.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_ADD_ROLE, args, ume);

        }
        catch (Exception ge)
        {
            CATEGORY.error("Failed to get or bind to another component "
                    + "needed for adding roles.", ge);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_BIND_TO_COMPONENT,
                    null, ge);
        }
    }

    /**
     * Removes all roles (container and user roles) associated with this locale
     * pair.
     */
    private void removeRoles(LocalePair p_thePair) throws LocaleManagerException
    {
        Role curRole = null;
        GlobalSightLocale sourceLocale = p_thePair.getSource();
        GlobalSightLocale targetLocale = p_thePair.getTarget();
        try
        {
            JobHandler jh = ServerProxy.getJobHandler();
            UserManager um = ServerProxy.getUserManager();
            CostingEngine ce = ServerProxy.getCostingEngine();

            long lpCompnayId = p_thePair.getCompanyId();
            Collection cActivities = jh.getAllActivities();
            Object activities[] = cActivities.toArray();
            for (int i = 0; i < activities.length; i++)
            {
                Activity curActivity = (Activity) activities[i];
                if (curActivity.getCompanyId() != lpCompnayId)
                {
                    continue;
                }
                String curActivityName = curActivity.getActivityName();

                // remove all the container roles
                Collection cRoles = um.getContainerRoles(curActivityName,
                        sourceLocale.toString(), targetLocale.toString());
                if (cRoles != null && cRoles.size() > 0)
                {
                    Object roles[] = cRoles.toArray();
                    for (int k = 0; k < roles.length; k++)
                    {
                        curRole = (Role) roles[k];
                        // remove the rates (deactive) that are associated with
                        // the role
                        // and then remove the roles.
                        // The rates are stored in database and the roles in
                        // LDAP,
                        // so the
                        // dependancy between then must be kept in the code and
                        // not in the DB.
                        ce.deleteRatesOnRole(curRole);
                        um.removeRole(curRole);
                    }
                }

                // remove all the user roles
                Collection uRoles = um.getUserRoles(curActivityName,
                        sourceLocale.toString(), targetLocale.toString());
                if (uRoles != null && uRoles.size() > 0)
                {
                    Object roles[] = uRoles.toArray();
                    for (int j = 0; j < roles.length; j++)
                    {
                        curRole = (Role) roles[j];
                        um.removeRole(curRole);
                    }
                }
            }
        }
        catch (JobException je)
        {
            CATEGORY.error("Failed to get all the activities for removing roles for locale pair "
                    + sourceLocale + " to " + targetLocale, je);
            throw new LocaleManagerException(LocaleManagerException.MSG_FAILED_TO_GET_ACTIVITES,
                    null, je);
        }
        catch (UserManagerException ume)
        {
            CATEGORY.error("Failed to remove role " + curRole.toString(), ume);
            String args[] =
            { curRole.toString() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_REMOVE_ROLE, args, ume);
        }
        catch (CostingException ce)
        {
            CATEGORY.error("Failed to remove the rates for role "
                    + curRole.getName());
            // tbd
            String msgArgs[] =
            { curRole.getName() };
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_DELETE_RATES, msgArgs,
                    ce);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to get or bind to another component "
                    + "needed for removing roles.", e);
            throw new LocaleManagerException(
                    LocaleManagerException.MSG_FAILED_TO_BIND_TO_COMPONENT,
                    null, e);

        }
    }
}
