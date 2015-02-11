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
package com.globalsight.everest.persistence.comment;

import java.util.List;

/**
 * IssueUnnamedQueries. This class contains and builds the queries that can't be
 * be built ahead with a name.
 * 
 */
public class IssueUnnamedQueries
{
    public static final String TYPE_ARG = "objectType";

    // sql string to get issues by type
	private static String ISSUES_BY_TYPE_SQL =
			"select * from issue where issue_object_type = :" + TYPE_ARG;

    // sql string to get the count of issues by type and logical key
    private static String ISSUE_COUNT_BY_TYPE_STATUS_AND_KEY_SQL = 
    		"select count(*) from issue where issue_object_type = :" + TYPE_ARG;
    
    private static String ISSUE_COUNT_BY_TYPE_STATUS_PER_TARGET_PAGE =
    		"select target_page_id, COUNT(*) AS num from issue " +
    		"where issue_object_type = :" + TYPE_ARG;
    		
    /**
     * Creates the read all query that returns the issues that are associated
     * with the object type and the object ids specified. If not ids are
     * specified then it returns all issues of the particular object type.
     * 
     * @param p_objectIds
     *            The list of object ids that the issues are associated with.
     *            This list should not be contain more sthan 1000 ids. If there
     *            is more this query will fail since it uses an "IN" clause
     *            which can only hold up to 1000 items in its list.
     */
    public static String getIssuesByTypeAndObjectId(List p_objectIds)
    {
        StringBuffer sql = new StringBuffer(ISSUES_BY_TYPE_SQL);

        // build a list of ids if they are specified
        if (p_objectIds != null && p_objectIds.size() > 0)
        {
            sql.append(" and issue_object_id in (");
            sql.append((Long) p_objectIds.get(0));
            for (int i = 1; i < p_objectIds.size(); i++)
            {
                sql.append(", ");
                sql.append((Long) p_objectIds.get(i));
            }
            sql.append(")");
        }

        return sql.toString();
    }

    /**
     * Creates the read all query that returns the issues that are associated
     * with the object type and the object ids specified. If not ids are
     * specified then it returns all issues of the particular object type.
     * 
     * @param p_objectIds
     *            The list of object ids that the issues are associated with.
     *            This list should not be contain more sthan 1000 ids. If there
     *            is more this query will fail since it uses an "IN" clause
     *            which can only hold up to 1000 items in its list.
     */
	public static String getIssueCountByTypeStatusAndPageId(
			List<String> p_states, List<Long> p_pageIds)
    {
        StringBuffer sql = new StringBuffer(
                ISSUE_COUNT_BY_TYPE_STATUS_AND_KEY_SQL);

        sql.append(appendStateAndPageIds(p_states, p_pageIds));

        return sql.toString();
    }
	
	public static String getIssueCountByTypeStatusPerPageId(
			List<String> p_states, List<Long> p_pageIds)
	{
        StringBuffer sql = new StringBuffer(
        		ISSUE_COUNT_BY_TYPE_STATUS_PER_TARGET_PAGE);

        sql.append(appendStateAndPageIds(p_states, p_pageIds));
        sql.append(" group by target_page_id ");
        
        return sql.toString();
	}

	private static String appendStateAndPageIds(List<String> p_states,
			List<Long> p_pageIds)
	{
		StringBuffer sql = new StringBuffer("");

		// build a list of states if they are specified
        if (p_states != null && p_states.size() > 0)
        {
            sql.append(" and status in ('");
            sql.append((String) p_states.get(0));
            for (int i = 1; i < p_states.size(); i++)
            {
                sql.append("','");
                sql.append((String) p_states.get(i));
            }
            sql.append("')");
        }

        // build a list of ids if they are specified
        if (p_pageIds != null && p_pageIds.size() > 0)
        {
        	StringBuilder tpIdsBuffer = new StringBuilder();
        	for (int i=0; i< p_pageIds.size(); i++) {
        		tpIdsBuffer.append(p_pageIds.get(i)).append(",");
        	}
        	String tpIds = tpIdsBuffer.toString();
        	tpIds = tpIds.substring(0, tpIds.length() - 1);

        	sql.append(" and target_page_id in (").append(tpIds).append(")");
        }

        return sql.toString();
	}
}
