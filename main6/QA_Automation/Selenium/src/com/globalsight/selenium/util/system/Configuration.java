/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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

package com.globalsight.selenium.util.system;

import java.util.Hashtable;

/**
 * @author Vincent
 *
 */
public class Configuration
{
    //GlobalSight server
    public static String GS_SERVER_HOST = "localhost";
    public static boolean GS_SERVER_HTTPS = false;
    public static String GS_SERVER_URL = "//globalsight";
    
    //Database server
    public static String DATABASE_HOST = "localhost";
    public static String DATABASE_PORT = "3036";
    public static String DATABASE_SCHEMA = "globalsight";
    public static String DATABASE_EXTRA = "useUnicode=true&characterEncoding=UTF-8";
    public static String DATABASE_USERNAME = "globalsight";
    public static String DATABASE_PASSWORD = "password";
    
    //Selenium server
    public static String SELENIUM_SERVER_HOST = "localhost";
    public static String SELENIUM_SERVER_PORT = "4444";
    
    //CVS server
    public static String CVS_SERVER_HOST = "localhost";
    public static String CVS_LOGIN_USER = "cvsadmin";
    public static String CVS_LOGIN_PASSWORD = "password";
    
    //Base path
    public static String BASE_FILE_INPUT_PATH = null;
    public static String BASE_FILE_OUTPUT_PATH = null;
    
    //Browser
    public static String BROWSER = "*firefox";
    
    //Wait time
    public static String SHORT_WAIT = "10000";
    public static String MIDDLE_WAIT = "60000";
    public static String LONG_WAIT = "120000";
    public static String BETWEEN_OPERATION_WAIT = "2000";
    
    public static Hashtable<String, Company> COMPANYS = new Hashtable<String, Company>();
}
