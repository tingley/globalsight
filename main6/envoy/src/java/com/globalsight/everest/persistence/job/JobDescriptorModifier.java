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
package com.globalsight.everest.persistence.job;

/**
 * JobDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Job descriptor.
 */
public class JobDescriptorModifier
{
    private static final String SOURCE_LOCALE_ID_ARG = "sourceLocaleId";
    private static final String TARGET_LOCALE_ID_ARG = "targetLocaleId";
    // Add the comany id into the query parameter to fix multi company issue
    private static final String COMPANY_ID_ARG = "companyId";
    // retrieve all ACTIVE jobs that contain a workflow with the specfied source
    // and target locale
    // Add the comany id into the query parameter to fix multi company issue
    public static final String ACTIVE_JOBS_BY_SOURCE_AND_TARGET_SQL = "select * "
            + "from job j where state in (\'PENDING\', \'BATCH_RESERVED\', \'DISPATCHED\', "
            + "\'LOCALIZED\',\'IMPORT_FAILED\',\'READY_TO_BE_DISPATCHED\') "
            + " and Exists (select job_id from workflow w where j.id = w.job_id "
            + "and target_locale_id = :"
            + TARGET_LOCALE_ID_ARG
            + " and Exists (select job_id  from request r where r.job_id = j.id and Exists "
            + "(select id from l10n_profile lp where lp.id = r.l10n_profile_id "
            + "and source_locale_id = :"
            + SOURCE_LOCALE_ID_ARG
            + ")))"
            + " and j.COMPANY_ID = :" + COMPANY_ID_ARG;

}