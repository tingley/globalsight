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
package com.globalsight.everest.webapp.applet.util;

// Standard Java packages

/**
 * This class is a subclass of AbstractSearchCriteria which implements the
 * getSqlStatement abstract method.
 * 
 * @deprecated
 */

public class WorkflowInstanceSearchCriteria
{
    //
    //
    // //////////////////////////////////////////////////////////////////////////////
    // // Begin: Constructor
    // //////////////////////////////////////////////////////////////////////////////
    // /**
    // * Constructor.
    // */
    // public WorkflowInstanceSearchCriteria()
    // {
    // super();
    // }
    // //////////////////////////////////////////////////////////////////////////////
    // // End: Constructor
    // //////////////////////////////////////////////////////////////////////////////
    //
    //
    // //////////////////////////////////////////////////////////////////////////////
    // // Begin: Abstract Method Implementation
    // //////////////////////////////////////////////////////////////////////////////
    // /**
    // * Get the search expression based on the specified search criteria
    // object.
    // * @param p_searchCriteriaParams - The search criteria object.
    // */
    // public Expression getSearchExpression(SearchCriteriaParameters
    // p_searchCriteriaParams)
    // {
    //
    // Map criteria = p_searchCriteriaParams.getParameters();
    //
    // // get the keys of the map
    // Object[] keys = criteria.keySet().toArray();
    //
    // int mapSize = keys.length;
    //
    // // loop throught the parameters to create the sql statement.
    // for (int i = 0; i < mapSize; i++)
    // {
    // switch (((Integer)(keys[i])).intValue())
    // {
    // // Job id range
    // case WorkflowInstanceSearchParameters.ID_RANGE:
    // Object[] ids = (Object[]) criteria.get(keys[i]);
    // prepareRangeExpression(WorkflowInstanceDbConstants.COL_INSTANCE_ID,
    // ids[0], ids[1]);
    // break;
    //
    // // target locale
    // case WorkflowInstanceSearchParameters.TARGET_LOCALE:
    // prepareExpression(WorkflowInstanceDbConstants.COL_INSTANCE_TARGET_LOCALE,
    // criteria.get(keys[i]));
    // break;
    //
    // // Start date range
    // case WorkflowInstanceSearchParameters.START_DATE_RANGE:
    // Object[] startDates = (Object[]) criteria.get(keys[i]);
    // prepareRangeExpression(WorkflowInstanceDbConstants.COL_INSTANCE_START_DATE,
    // startDates[0], startDates[1]);
    // break;
    //
    // // Start date range
    // case WorkflowInstanceSearchParameters.DUE_DATE_RANGE:
    // Object[] dueDates = (Object[]) criteria.get(keys[i]);
    // prepareRangeExpression(WorkflowInstanceDbConstants.COL_INSTANCE_DUE_DATE,
    // dueDates[0], dueDates[1]);
    // break;
    //
    // default:
    // //System.out.println("Unknown criterion type " + criteria.get(keys[i]));
    // break;
    // }
    // }
    // return getExpression();
    // }
    // ////////////////////////////////////////////////////////////////////////////
    // End: Abstract Method Implementation
    // ////////////////////////////////////////////////////////////////////////////
}
