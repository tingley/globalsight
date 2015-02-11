/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.migration.system4;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.locale.LocaleQueryNames;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

/**
 * This class is responsible for getting/creating a GlobalSightLocale
 */
public class MigrationLocale
{
    private Map m_localeCache = null;

    private static final String SEQUENCE_QUERY
        = "SELECT count FROM sequence WHERE name = 'LOCALE_SEQ'";
    private static final String LANGUAGE_QUERY
        = "SELECT iso_lang_code FROM language WHERE iso_lang_code = ?";
    private static final String COUNTRY_QUERY
        = "SELECT iso_country_code FROM country WHERE iso_country_code = ?";
    private static final String LOCALE_INSERT
        = "INSERT INTO locale VALUES (?, ?, ?, 'N')";
    
    private PreparedStatement m_sequenceStatement = null;
    private PreparedStatement m_localeStatement = null;
    private PreparedStatement m_languageStatement = null;
    private PreparedStatement m_countryStatement = null;
    
    public MigrationLocale(Connection p_connection)
        throws Exception
    {
        m_sequenceStatement = p_connection.prepareStatement(SEQUENCE_QUERY,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        m_languageStatement = p_connection.prepareStatement(LANGUAGE_QUERY,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        m_countryStatement = p_connection.prepareStatement(COUNTRY_QUERY,
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        m_localeStatement = p_connection.prepareStatement(LOCALE_INSERT);

        m_localeCache = new HashMap();
    }

    
    /**
     * get a GlobalSightLocale. If it doesn't exist yet, the method
     * creates a new one.
     * @param five character locale name e.g. en_US
     * @return GlobalSightLocale object
     */
    public GlobalSightLocale get(String p_localeName)
        throws Exception
    {
        GlobalSightLocale locale
            = (GlobalSightLocale)m_localeCache.get(p_localeName);
        if(locale == null)
        {
            locale = getLocaleByString(p_localeName);
            if(locale == null)
            {
                locale = createGlobalSightLocale(p_localeName);
                long id = storeGlobalSightLocaleToDatabase(locale);
                locale.setId(id);
            }
            m_localeCache.put(p_localeName, locale);
        }

        return locale;
    }


    /**
     * returns Locale cache
     * @return map of locale name - GlobalSightLocale pair
     */
    public Map getLocaleCache()
    {
        return m_localeCache;
    }
    
    // create a GlobalSightLocale using a given locale string in the
    // form of "en_US"
    private GlobalSightLocale createGlobalSightLocale(String p_localeName)
        throws Exception
    {
        if(p_localeName.length() != 5
           || !p_localeName.substring(2, 3).equals("_"))
        {
            throw new Exception("Locale name is incorrect: " + p_localeName);
        }
        
        String language = p_localeName.substring(0, 2);
        String country = p_localeName.substring(3, 5);
        return new GlobalSightLocale(language, country, false);
    }
    

    private GlobalSightLocale getLocaleByString(String p_localeName)
        throws Exception
    {
        if(p_localeName.length() != 5
           || !p_localeName.substring(2, 3).equals("_"))
        {
            throw new Exception("Locale name is incorrect: " + p_localeName);
        }
        
        String language = p_localeName.substring(0, 2);
        String country = p_localeName.substring(3, 5);

        Vector queryArgs = new Vector();
        queryArgs.add(language);
        queryArgs.add(country);
        
        PersistenceService ps = PersistenceService.getInstance();
        Iterator it = ps.executeNamedQuery(
            LocaleQueryNames.LOCALE_BY_LANGUAGE_AND_COUNTRY,
            queryArgs, false).iterator();
        return it.hasNext() ? (GlobalSightLocale)it.next() : null;
    }
    


    private long storeGlobalSightLocaleToDatabase(GlobalSightLocale p_locale)
        throws Exception
    {
        // increase LOCALE_SEQ by one
        ResultSet rs = m_sequenceStatement.executeQuery();
        rs.next();
        long id = rs.getLong("count");
        rs.updateLong("count", ++id);
        rs.updateRow();
        rs.close();

        // check existence of the language code in language table
        m_languageStatement.setString(1, p_locale.getLanguageCode());
        rs = m_languageStatement.executeQuery();
        if(!rs.next())
        {
           rs.moveToInsertRow();
           rs.updateString(1, p_locale.getLanguageCode());
           rs.insertRow();
        }
        rs.close();
        
        // check existence of the country code in country table
        m_countryStatement.setString(1, p_locale.getCountryCode());
        rs = m_countryStatement.executeQuery();
        if(!rs.next())
        {
           rs.moveToInsertRow();
           rs.updateString(1, p_locale.getCountryCode());
           rs.insertRow();
        }
        rs.close();

        // insert locale
        m_localeStatement.setLong(1, id);
        m_localeStatement.setString(2, p_locale.getLanguageCode());
        m_localeStatement.setString(3, p_locale.getCountryCode());
        m_localeStatement.executeUpdate();
        return id;
    }
    
}
