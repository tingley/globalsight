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
package com.globalsight.util.resourcebundle;

import java.util.Locale;

/**
 * This interface contains all of the resource bundle related constants, which
 * seem to be the locales into which the System 4 UI has been localized.
 */
public interface ResourceBundleConstants
{

    /**
     * The location where all the message resource bundles are located.
     */
    public static final String BUNDLE_LOCATION = "com/globalsight/resources/messages/";
    public static final String LOCALE_RESOURCE_NAME = BUNDLE_LOCATION
            + "LocaleResource";
    public static final String EMAIL_RESOURCE_NAME = BUNDLE_LOCATION
            + "EmailMessageResource";
    public static final String EXCEPTION_RESOURCE_NAME = BUNDLE_LOCATION
            + "ExceptionResource";
    
    public static final String UPLOAD_BUNDLE_LOCATION = "com/globalsight/everest/edit/offline/upload/";

    public static final String UPLOADAPI_BUNDLE_LOCATION = UPLOAD_BUNDLE_LOCATION
            + "UploadApi";

    public static final int INITIAL_MAP_SIZE = 15;
    // English
    public static final Locale EN = new Locale("en", "US", "");
    // French
    public static final Locale FR = new Locale("fr", "FR", "");
    // German
    public static final Locale DE = new Locale("de", "DE", "");
    // Japanese
    public static final Locale JA = new Locale("ja", "JP", "");
    // Spanish
    public static final Locale ES = new Locale("es", "ES", "");

    public static final Locale[] SUPPORTED_LOCALES =
    { EN, FR, DE, JA, ES };
}
