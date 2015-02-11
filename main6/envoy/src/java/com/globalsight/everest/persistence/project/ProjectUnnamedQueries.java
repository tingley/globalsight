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

import java.util.List;

/**
 * ProjectUnnamedQueries. This class contains and builds the queries that can't
 * be be built ahead with a name.
 */
public class ProjectUnnamedQueries
{
    //
    // PRIVATE STATIC VARIABLES
    //

    public static String PROJECTS_WITH_USERS_SQL =
        "select * from project p, project_user pu where p.project_seq = pu.project_id " +
        " and pu.user_id in ";

    private static String USERS_IN_PROJECTS_PREFIX = "select count(*) from project_user where project_id = ";

    private static String USERS_IN_PROJECTS_SUFFIX = " and user_id in ";

    //
    // PUBLIC STATIC METHODS
    // 

    /**
     * Build a query to get all projects that have one or more of the specified
     * user id(s) within it. This is NOT a named query, but can be called by any
     * component.
     * 
     * @param p_userIds
     *            A List containing Strings of user ids.
     * 
     * @return The ReadQuery created to look for projects according to user ids.
     */
    // public static ReadQuery getProjectsByUserIdsQuery(List p_userIds)
    // {
    // ReadAllQuery query = new ReadAllQuery(s_entityClass);
    // StringBuffer sqlString =
    // new StringBuffer(PROJECTS_WITH_USERS_SQL);
    // sqlString.append(buildUserIdsInClause(p_userIds));
    // query.setSQLString(sqlString.toString());
    // query.bindAllParameters();
    // return query;
    // }
    /**
     * Determine whether the given list of usernames are associated with the
     * project specified by the given id.
     * 
     * @param p_userIds -
     *            A list of user ids.
     * @param p_projectId -
     *            The id of the project.
     * @return A report query used to count the number users (from the given
     *         list) associated with the project.
     */
    public static String countUsersInProject(List p_userIds, long p_projectId)
    {
        StringBuffer sql = new StringBuffer();
        sql.append(USERS_IN_PROJECTS_PREFIX);
        sql.append(p_projectId);
        sql.append(USERS_IN_PROJECTS_SUFFIX);
        sql.append(buildUserIdsInClause(p_userIds));
        return sql.toString();
    }

    /**
     * Creates the grouping of user ids for an in clause. Assumes there is
     * atleast one userid in the collection.
     */
    public static String buildUserIdsInClause(List p_userIds)
    {
        Object[] userIds = p_userIds.toArray();
        StringBuffer sqlString = new StringBuffer("(");
        sqlString.append("'");
        sqlString.append((String) userIds[0]);
        sqlString.append("'");
        for (int i = 1; i < userIds.length; i++)
        {
            sqlString.append(", '");
            sqlString.append((String) userIds[i]);
            sqlString.append("'");
        }
        sqlString.append(")");
        return sqlString.toString();
    }
}
