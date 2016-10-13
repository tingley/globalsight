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
 * L10nProfileRuntimeQueries.  This class contains and builds the
 * queries that can't be be built ahead with a name.
 */
public class L10nProfileUnnamedQueries
{
    /**
     * Build a query to get all profiles that have one or more of the
     * specified workflow template id(s) within it.  This is NOT a
     * named query, but can be called by any component.
     *
     * @param p_workflowTemplateIds A Vector containing Longs of
     * workflow template ids.
     * @return The ReadQuery created to look for profiles according to a role.
     */

    
// public static ReadQuery getProfilesByTemplateIdsQuery(
    // Vector p_workflowTemplateIds)
    // {
    // ReadAllQuery query = new ReadAllQuery(s_entityClass);
    // ExpressionBuilder builder = query.getExpressionBuilder();
    // Expression expr = builder.getField(L10N_PROFILE_ACTIVE).notEqual(NO);
    // Expression expr1 =
    // builder.anyOf(BasicL10nProfile.WORKFLOW_TEMPLATES).
    // get(WorkflowTemplateInfo.TEMPLATE_ID).
    // in(p_workflowTemplateIds);
    // query.setSelectionCriteria(expr.and(expr1));
    // query.bindAllParameters();
    //        return query;
    //    }
    
}
