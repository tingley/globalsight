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

public class TranslationMemoryProfileDescriptorModifier
{

    public static final String ALL_TM_PROFILES_SQL = new StringBuffer().append(
            "select * from tm_profile ").append(
            "where project_tm_id_for_save in (select project_tm.id from ")
            .append("project_tm where project_tm.company_id >= :").append(
                    CompanyWrapper.COPMANY_ID_START_ARG).append(
                    " and project_tm.company_id <= :").append(
                    CompanyWrapper.COPMANY_ID_END_ARG).append(")").toString();

}
