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
package com.globalsight.everest.persistence.l10nprofile;

/**
 * Specifies the names of all the named queries for L10nProfile.
 */
public interface L10nProfileQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all available localization profiles.
     * <p>
     * Arguments: None.
     */
    public static String ALL_PROFILES = "getAllProfiles";

    /**
     * A named query to return all current localization profiles.
     * <p>
     * Arguments: None.
     */
    public static String ALL_CURRENT_PROFILES = "getAllCurrentProfiles";

    /**
     * A named query to return a hashtable where the key is the L10nProfileId
     * and the value is the profile name.
     * <p>
     * Arguments: None.
     */
    public static String ALL_PROFILE_NAMES = "getProfileNames";

    /**
     * A named query to return a L10n Profile based on its id
     * <p>
     * Arguments: 1: L10nProfile Id.
     */
    public static String PROFILE_BY_ID = "getProfileById";

    /**
     * A named query to return a projection of all available localization 
     * profiles. This projection only contains the profile id, profile name,
     * and profile description. It is mainly used by GUI only.
     * <p>
     * Arguments: None.
     */
    public static String ALL_PROFILES_FOR_GUI = "getAllProfilesForGUI";

    /**
     * A named query to return all active/current profiles that contain
     * the specified source and target locale.  So the profile(s) have
     * the specified source locale and one of its workflow(s) has the
     * target locale specified.
     * <p>
     * Arguments: 1: Source locale id   
     *            2: Target Locale id
     *            3: company id
     */
    public static String PROFILES_BY_SOURCE_AND_TARGET_LOCALE_ID = 
        "getProfilesBySourceTargetLocaleIds";  

    /**
    * A named query to obtain a l10n profile given a job id 
    * Argument:1 : Job id
    */
    public static String L10N_PROFILE_BY_JOB_ID = "getProfileByJobId";

    /**
    * A named query to obtain a l10n profile given a Workflow Template id 
    * Argument:1 : Workflow Template id
    */
    public static String L10N_PROFILE_BY_WORKFLOW_TEMPLATE_ID = "getProfileByWftId";
}
