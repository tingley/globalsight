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

import com.globalsight.everest.company.CompanyWrapper;

/**
 * L10nProfileDescriptorModifier extends DescriptorModifier by providing
 * amendment methods unique to the L10nProfile descriptor.
 */
public class L10nProfileDescriptorModifier
{
    private static final String NO = "N";
    private static final String YES = "Y";
    private static final String JOB_ID_ARG = "jobId";
    private static final String WORKFLOW_TEMPLATE_ID = "workflowTemplateId";
    public static final String CURRENT_PROFILES_SQL = "SELECT * "
            + "FROM L10N_PROFILE L1 WHERE L1.ID = ( "
            + "SELECT MAX(L2.ID) FROM L10N_PROFILE L2 "
            + "WHERE L2.NAME = L1.NAME) AND L1.IS_ACTIVE = '" + YES + "'"
            + " AND L1.COMPANYID >= :" + CompanyWrapper.COPMANY_ID_START_ARG
            + " AND L1.COMPANYID <= :" + CompanyWrapper.COPMANY_ID_END_ARG;
    public static final String CURRENT_PROFILEINFOS_SQL = "SELECT ID, NAME, "
            + "DESCRIPTION, COMPANYID FROM L10N_PROFILE L1 "
            + "WHERE L1.ID = ( " + "SELECT MAX(L2.ID) FROM L10N_PROFILE L2 "
            + "WHERE L2.NAME = L1.NAME AND L2.COMPANYID = L1.COMPANYID) "
            + "AND L1.IS_ACTIVE = '" + YES + "'" + " AND L1.COMPANYID >= :"
            + CompanyWrapper.COPMANY_ID_START_ARG + " AND L1.COMPANYID <= :"
            + CompanyWrapper.COPMANY_ID_END_ARG + " order by L1.NAME";

    public static final String CURRENT_PROFILEINFOS_GUI_SQL = "SELECT L1.ID,"
            + "L1.NAME,L1.DESCRIPTION, L1.COMPANYID, "
            + "COUNT(L3.WF_TEMPLATE_ID) FROM "
            + " L10N_PROFILE L1, L10N_PROFILE_WFTEMPLATE_INFO L3 "
            + "WHERE L1.ID = L3.L10N_PROFILE_ID AND L3.IS_ACTIVE= '" + YES 
            + "' AND L1.ID = "
            + " (SELECT MAX(L2.ID) FROM L10N_PROFILE L2 WHERE L2.NAME = L1.NAME "
            + "AND L2.COMPANYID = L1.COMPANYID) AND L1.IS_ACTIVE= '" + YES
            + "'" + " AND L1.COMPANYID >= :"
            + CompanyWrapper.COPMANY_ID_START_ARG + " AND L1.COMPANYID <= :"
            + CompanyWrapper.COPMANY_ID_END_ARG
            + " GROUP BY L1.ID, L1.NAME, L1.DESCRIPTION, L1.COMPANYID"
            + " order by L1.NAME";

    public static final String DELETE_SQL = "update L10N_PROFILE set IS_ACTIVE='"
            + NO + "' where ID=#ID";
    public static final String L10N_PROFILE_FOR_JOB_ID = "select distinct l.* from"
            + " request r, l10n_profile l where l.id = r.l10n_profile_id"
            + " and r.job_id = :" + JOB_ID_ARG;
    public static final String L10N_PROFILE_BY_WORKFLOW_TEMPLATE_ID = 
    	      " SELECT DISTINCT L.* FROM L10N_PROFILE L, L10N_PROFILE_WFTEMPLATE_INFO LWFTI "
    	    + " WHERE L.IS_ACTIVE = \"Y\" "
    	    + " AND LWFTI.IS_ACTIVE = \"Y\" "
            + " AND L.ID = LWFTI.L10N_PROFILE_ID "
            + " AND LWFTI.WF_TEMPLATE_ID = :" + WORKFLOW_TEMPLATE_ID;
}
