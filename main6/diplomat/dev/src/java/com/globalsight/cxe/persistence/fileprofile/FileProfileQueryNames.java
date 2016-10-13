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
package com.globalsight.cxe.persistence.fileprofile;

/**
 * Specifies the names of all the named queries for FileProfile.
 */
public interface FileProfileQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all file profiles.
     * <p>
     * Arguments: none.
     */
    public static String ALL_FILE_PROFILES = "getAllFileProfiles";

    /**
     * A named query to return the file profile specified by the given id.
     * <p>
     * Arguments: 1: File Profile id.
     */
    public static String FILE_PROFILE_BY_ID = "getFileProfileById";
    
    /**
     * A named query to return all the file profiles associated with
     * the specified localization profile id.
     * <p>
     * Arguments: 1 - L10nProfile id
     */
     public static String FILE_PROFILES_BY_L10NPROFILE_ID = "getFileProfilesByL10nProfileId";

     /**
     * A named query to return the file profile ID specified by the given name.
     * <p>
     * Arguments: 1: File Profile name.
     */
    public static String FILE_PROFILE_ID_BY_NAME = "getFileProfileIdByName";

     /**
     * A named query to return the file profile specified by the given name.
     * <p>
     * Arguments: 1: File Profile name.
     */
    public static String FILE_PROFILE_BY_NAME = "getFileProfileByName";
}

