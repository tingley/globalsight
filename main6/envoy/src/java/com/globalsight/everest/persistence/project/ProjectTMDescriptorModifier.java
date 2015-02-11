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

public class ProjectTMDescriptorModifier
{
    public static String ALL_PROJECT_TMS_SQL = new StringBuffer().append(
            "select * from project_tm where company_id >= :").append(
            CompanyWrapper.COPMANY_ID_START_ARG).append(" and company_id <= :")
            .append(CompanyWrapper.COPMANY_ID_END_ARG).toString();
    private static final String ID_ARG = "idArg";
    public static String PROJECT_TM_BY_ID = "select * from project_tm where id = :"
            + ID_ARG;
    public static final String NAME_ARG = "nameArg";

    public static String PROJECT_TM_BY_NAME = new StringBuffer().append(
            "select * from project_tm where name = :").append(NAME_ARG)
            .toString();
}
