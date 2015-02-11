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

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * ProjectDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Project descriptor.
 */
public class ProjectDescriptorModifier
{

    protected static final String ID_ARG = "id";
    // public because it's used in TmManager's query
    public static final String ID_ATTR = PersistentObject.M_ID;
    protected static final String NAME_ARG = "name";

    protected static final String COMPANY_ARG = "companyid";

    public static final String VENDOR_ID_ARG = "vendorId";
    public static final String USER_ID_ARG = "userId";

    protected static final String DESCRIPTION_ARG = "description";

    protected static final String TERMBASE_NAME_ARG = "termbaseName";
    
    // returns all the projects that a particular user is a part of (assigned
    // to)
    public static final String PROJECTS_BY_USER_ID_SQL = "select p.* "
            + " from project p, project_user pu "
            + " where p.project_seq = pu.project_id "
            + " and p.is_active = \"Y\" "
            + " and pu.user_id = :" + USER_ID_ARG 
            + " and p.companyid >= :" + CompanyWrapper.COPMANY_ID_START_ARG 
            + " and p.companyid <= :" + CompanyWrapper.COPMANY_ID_END_ARG;
    // returns all the projects that a particular vendor is a part of (assigned
    // to).
    public static final String PROJECTS_BY_VENDOR_ID_SQL = "select p.* "
            + " from project p, project_vendor pv "
            + " where p.project_seq = pv.project_id " 
            + " and p.is_active = \"Y\" "
            + " and pv.vendor_id = :" + VENDOR_ID_ARG 
            + " and p.companyid >= :" + CompanyWrapper.COPMANY_ID_START_ARG 
            + " and p.companyid <= :" + CompanyWrapper.COPMANY_ID_END_ARG;
    // return specific project info for a particular user
    public static final String PROJECT_INFO_BY_USER_ID_SQL = 
    		" select p.* "
            + " from project p, project_user pu "
            + " where p.project_seq = pu.project_id " 
            + " and p.is_active = \"Y\" "
            + " and pu.user_id = :" + USER_ID_ARG 
            + " and p.companyid >= :" + CompanyWrapper.COPMANY_ID_START_ARG 
            + " and p.companyid <= :" + CompanyWrapper.COPMANY_ID_END_ARG 
            + " order by p.PROJECT_NAME";

}
