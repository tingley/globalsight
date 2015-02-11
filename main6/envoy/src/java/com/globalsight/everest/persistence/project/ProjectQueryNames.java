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
package com.globalsight.everest.persistence.project;

/**
 * Specifies the names of all the named queries for Project.
 */
public interface ProjectQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all available projects
     * <p>
     * Arguments: None.
     */
    public static String ALL_PROJECTS = "getAllProjects";

    /**
     * A named query to return a hashtable where the key is the project id
     * and the value is the project name.
     * <p>
     * Arguments: None.
     */
    public static String ALL_PROJECT_NAMES = "getAllProjectNames";

    /**
     * A named query to return a project based on its id
     * <p>
     * Arguments: 1: Project Id.
     */
    public static String PROJECT_BY_ID = "getProjectByID";

    /**
     * A named query to return a project based on its name
     * <p>
     * Arguments: 1: Project name
     */
    public static String PROJECT_BY_NAME = "getProjectByName";

    /**
     * A named query to return all projects based on the given
     * user id
     * <p>
     * Arguments: 1: User id
     */
    public static String PROJECTS_BY_PM_USER_ID = "getProjectsByPmUserId";

    /**
     * A named query to return the informabion about all projects based on
     * a given project manager's id.
     */
    public static String PROJECT_INFOS_BY_PM_USER_ID = "getProjectInfosByPmUserId";

    /**
     * A named query to return a projection of all available projects.
     * This projection only contains the project id, project name,
     * and project description. It is mainly used by GUI only.
     * <p>
     * Arguments: None.
     */
    public static String ALL_PROJECTS_FOR_GUI = "getAllProjectsForGUI";

    /**
     * A named query to return project information (name, id, description,
     * project manager id) for all projects that a particular
     * user is associated with.
     * <p>
     * Arguments: User Id
     */
    public static String ALL_PROJECT_INFOS_BY_USER_ID = "getProjectInfosByUserId";

    /**
     * A named query to return all projects that a particular user is
     * part of.
     * <p>
     * Arguments: User id
     */
    public static String ALL_PROJECTS_BY_USER_ID = "getProjectsByUserId";

    /**
     * A named query to return all projects that a particular vendor is
     * part of.
     * <p>
     * Arguments: id of the vendor
     */
    public static String ALL_PROJECTS_BY_VENDOR_ID = "getProjectByVendorId";

    /**
     * A named query to return a project based on its termbase
     * <p>
     * Arguments: 1: Termbase.
     */
    public static String ALL_PROJECTS_BY_TERMBASE = "getProjectsByTermbase";

}
