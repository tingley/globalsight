/* Copyright (c) 2004-2005, GlobalSight Corporation.  All rights reserved.
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
package com.documentum.ambassador.webservice;

import java.io.IOException;
import java.util.Properties;

/**
 * Configure all information related with GlobalSight web service And Documentum
 * content server.
 */
public class AmbassadorConfiguration {

    public static final String AMB_HOSTNAME = "amb_hostname";

    public static final String AMB_HTTP_PORT = "amb_http_port";

    public static final String AMB_HTTPS_PORT = "amb_https_port";

    public static final String AMB_WEBSERVICE_URL = "amb_webservice_url";

    public static final String AMB_USERNAME = "amb_username";
    
    public static final String AMB_PASSWORD = "amb_password";

    public static final String DCTM_REPOSITORY = "dctm_repository";

    public static final String DCTM_USERNAME = "dctm_username";

    public static final String DCTM_PASSWORD = "dctm_password";
    
    private static final String FILEPATH = "/properties/DocumentumAdapter.properties";
      
    public final static String DM_OBJECT_TRANSLATION_STATE = "field_translation_state";
       
    public final static String DM_OBJECT_TRANSLATION_JOB = "field_translation_jobid";
    
    public final static String DM_OBJECT_TARGET_LANGUANGES = "field_target_languages";
    
    public final static String TABLE_COUNTRY_LIST = "table_country_list";
    
    public final static String FIELD_LOCALE = "field_locale";

    public final static String FIELD_COUNTRY_LANGUANGE = "field_country_language";

    public final static String TABLE_PRESELECT_LANGUAGE = "table_translation_preselect"; 
    
    public final static String FIELD_COUNTRY_LOCALES = "field_country_locales";
    
    public final static String FIELD_LOCALE_SET_CODE = "field_locale_set_code";

    private static AmbassadorConfiguration instance = null;
    
    private Properties properties = null;

    /**
     * private constructor.
     */
    private AmbassadorConfiguration() {
        init();
    }
    
    /**
     * @return AmbassadorConfiguration object.
     */
    public static AmbassadorConfiguration getInstance() {
        if (instance == null) {
            instance = new AmbassadorConfiguration();
        }
        
        return instance;
    }
    
    /**
     * Get the corresponding value.
     * 
     * @param key
     * @return
     */
    public String getPropertyValue(String key) {
        return properties.getProperty(key).trim();
    }

    public void reloadConfiguration() {
        init();
    }
    
    private void init() {
        properties = new Properties();

        try {
            properties.load(getClass().getResourceAsStream(FILEPATH));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
