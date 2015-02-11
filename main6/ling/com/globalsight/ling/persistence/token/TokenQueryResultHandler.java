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
package com.globalsight.ling.persistence.token;

import java.util.Collection;

/**
 * TokenQueryResultHandler provides functionality to convert a Collection of
 * ReportQueryResults into a Collection of Token objects.
 */
public class TokenQueryResultHandler
{
    /**
     * Convert the given collection of ReportQueryResults into a collection of
     * Token.
     * 
     * @param p_collection
     *            the collection of ReportQueryResults that is created when the
     *            original query is executed
     * 
     * @return a collection of Token objects that were created from the contents
     *         the original collection.
     */
    public static void handleResult(Collection p_collection)
    {

        // Collection result = new ArrayList();
        // Iterator it = p_collection.iterator();
        // while (it.hasNext())
        // {
        // ReportQueryResult rqr = (ReportQueryResult)it.next();
        // long tuvId = ((Long)rqr.getByIndex(0)).longValue();
        // double score = ((Double)rqr.getByIndex(1)).doubleValue();
        // short roundedScore = (short)Math.round((score*100.0));
        // result.add(new FuzzyCandidate(tuvId, roundedScore));
        // }
        // return result;
    }
}
