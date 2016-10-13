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
package com.globalsight.everest.persistence.vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.globalsight.everest.vendormanagement.Vendor;

/**
 * VendorQueryResultHandler provides functionality to convert a Collection
 * of ReportQueryResults into whatever is necessary for the specific
 * query.
 */
public class VendorQueryResultHandler
{
    // public constants

    public static final String COMPANY_NAME_METHOD = "handleCompanyResult";
    public static final String ALL_VENDORS = "handleAllVendors";
    public static final String VENDORS_IN_SAME_PROJECTS = 
        "handleVendorsInSameProjects";
    public static final String VENDOR_CUSTOM_ID_METHOD = "handleCustomIdResult";
    public static final String PSEUDONYM_METHOD = "handlePseudonymResult";
    public static final String VENDORS_USER_IDS_METHOD = "handleVendorsUserIdsResult";

    /**
     * Convert the given collection of ReportQueryResults into an array
     * of strings containing the vendor company names.
     *
     * @param p_collection the collection of ReportQueryResults that is created
     * when the original query is executed
     *
     * @return a list of Strings (company names)
     */
    public static Collection handleCompanyResult(Collection p_collection)
    {
        Collection coll = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Vendor result = (Vendor)it.next();
            coll.add(result.getCompanyName());
        }
        return coll;
    }

    /**
     * Convert the given collection of ReportQueryResults into an array
     * of strings containing the vendor pseudonyms.
     *
     * @param p_collection the collection of ReportQueryResults that is created
     * when the original query is executed
     *
     * @return a list of Strings (pseudonyms)
     */
    public static Collection handlePseudonymResult(Collection p_collection)
    {
        Collection coll = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Vendor result = (Vendor)it.next();
            coll.add(result.getPseudonym());
        }
        return coll;
    }

    public static Collection handleVendorsUserIdsResult(Collection p_collection)
    {
        Collection coll = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Vendor result = (Vendor)it.next();
            coll.add(result.getUserId());
        }
        return coll;
    }

    /**
     * Convert the given collection of ReportQueryResults into an array
     * of strings containing the vendor custom ids.
     *
     * @param p_collection the collection of ReportQueryResults that is created
     * when the original query is executed
     *
     * @return a list of Strings (vendor custom ids).
     */
    public static Collection handleCustomIdResult(Collection p_collection)
    {
        Collection coll = new ArrayList(p_collection.size());
        Iterator it = p_collection.iterator();
        while (it.hasNext())
        {
            Vendor result = (Vendor)it.next();
            coll.add(result.getCustomVendorId());
        }
        return coll;
    }
}
