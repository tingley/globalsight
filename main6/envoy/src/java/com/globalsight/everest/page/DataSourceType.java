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

package com.globalsight.everest.page;


/**
 * This interface contains a list of supported data source types used by
 * the system.  The supported data sources match with the types send by
 * CXE.
 */
public interface DataSourceType 
{

    /**
     * Constant used for identifying a "file system" data source type.
     */
    public final static String FILE_SYSTEM = "fs";

    /**
     * Constant used for identifying a "auto import file system" data source type.
     */
    public final static String FILE_SYSTEM_AUTO_IMPORT = "fsAutoImport";
    
    /**
     * Constant used for identifying a "cvs import file system" data source type.
     */
    public final static String FILE_SYSTEM_CVS_IMPORT = "fsCvsImport";

    /**
     * Constant used for identifying a "data base" data source type.
     */
    public final static String DATABASE = "db";
    
    /**
     * Constant used for identifying a "Documentum CMS" data source type
     */
    public final static String DOCUMENTUM = "documentum";

    /**
     * Constant used for identifying a "Vignette CMS" data source type.
     */
    public final static String VIGNETTE = "vignette";    

    /**
     * Constant used for identifying a "Mediasurface CMS" data source type.
     */
    public final static String MEDIASURFACE = "mediasurface";    

}
