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
package com.globalsight.everest.persistence.snippet;

// globalsight
import java.util.Collection;
import java.util.List;

import com.globalsight.everest.snippet.SnippetImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * SnippetUnnamedQueries.  This class contains and builds
 * the queries that can't be be built ahead with a name.
 *
 */
public class SnippetUnnamedQueries
{
    // query strings
    private static final String GENERIC_SNIPPETS_BY_NAMES_SQL =
        "select * from snippet where locale_id is null and name in ";
    private static final String SNIPPETS_BY_LOCALE_SQL =
        "select * from snippet where (locale_id is null or locale_id = ";
    private static final String ADD_NAME_SQL = ") and name in ";

    //
    // PUBLIC STATIC METHODS
    //

    /**
     * Build a query to find all snippets with a particular locale and
     * whose name is in the vector passed in.
     *
     * This is NOT a named query, but can be called by any component.
     * The nature of the IN clause and binding the parameters doesn't
     * work as a named query.
     *
     * @param p_names A vector containing a list of snippets names to
     * find.
     * @param p_localeId The id of the locale the snippet(s) are in.
     * @return The ReadQuery created to look for snippets according to
     * names and locale.
     */
    public static List getSnippetsByNamesAndLocaleQuery(
        Collection p_names, Long p_localeId)
    {        
        StringBuffer sqlString = new StringBuffer(SNIPPETS_BY_LOCALE_SQL);
        sqlString.append(p_localeId);
        sqlString.append(ADD_NAME_SQL);
        sqlString.append(buildNameInClause(p_names));
        
        String sql = sqlString.toString();     
        return HibernateUtil.searchWithSql(sql, null, SnippetImpl.class);
    }

    /**
     * Build a query to find all generic snippets whose name is in the
     * vector passed in.  This is NOT a named query, but can be called
     * by any component.  The natrue of the IN clause and binding the
     * parameters doesn't work as a named query.
     *
     * @param p_names A vector containing a list of snippet names to
     * find.
     * @return The ReadQuery created to look for generic snippets
     * according to name.
     */
    public static List getGenericSnippetsByNames(Collection p_names)
    {
        StringBuffer sqlString = new StringBuffer(GENERIC_SNIPPETS_BY_NAMES_SQL);
        sqlString.append(buildNameInClause(p_names));
        
        String sql = sqlString.toString();     
        return HibernateUtil.searchWithSql(sql, null, SnippetImpl.class);
    }

    /**
     * Creates the grouping of names for an in clause.
     * Assumes there is atleast one name in the collection.
     */
    private static String buildNameInClause(Collection p_names)
    {
        Object[] names = p_names.toArray();
        StringBuffer sqlString = new StringBuffer("(");
        sqlString.append("'");
        sqlString.append((String)names[0]);
        sqlString.append("'");
        for (int i = 1 ; i < names.length ; i++)
        {
            sqlString.append(", '");
            sqlString.append((String)names[i]);
            sqlString.append("'");
        }
        sqlString.append(")");
        return sqlString.toString();
    }
}
