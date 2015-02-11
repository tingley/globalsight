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
package com.globalsight.cxe.persistence.cms.teamsite.server;

/**
 * Specifies the names of all the named queries for TeamSiteServer.
 */
public interface TeamSiteServerQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all TeamSite Servers.
     * <p>
     * Arguments: none.
     */
    public static String ALL_TEAMSITE_SERVERS = "getAllTeamSiteServers";

    /**
     * A named query to return the TeamSite Server specified by the given id.
     * <p>
     * Arguments: 1: TeamSite Server id.
     */
    public static String TEAMSITE_SERVER_BY_ID = "getTeamSiteServerById";
    
     /**
     * A named query to return the TeamSite Server ID specified by the given name.
     * <p>
     * Arguments: 1: TeamSite Server name.
     */
    public static String TEAMSITE_SERVER_ID_BY_NAME = "getTeamSiteServerIdByName";

     /**
     * A named query to return the TeamSite Server specified by the given name.
     * <p>
     * Arguments: 1: TeamSite Server name.
     */
    public static String TEAMSITE_SERVER_BY_NAME = "getTeamSiteServerByName";
}
