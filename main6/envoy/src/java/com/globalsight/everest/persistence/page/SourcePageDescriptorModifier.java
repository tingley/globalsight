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
package com.globalsight.everest.persistence.page;

// globalsight
import com.globalsight.everest.page.PageState;

/**
 * SourcePageDescriptorModifier extends PageDescriptorModifier by providing
 * amendment methods unique to the SourcePage descriptor.
 */
public class SourcePageDescriptorModifier extends PageDescriptorModifier
{
    private static final String JOB_ID_ARG = "jobId";

    public static final String SOURCE_PAGE_BY_JOB_ID = "select sp.* "
            + "from source_page sp, request r "
            + "where sp.id = r.page_id and r.job_id = :" + JOB_ID_ARG;

    public static final String UNEXTRACTED_SOURCE_PAGES_BY_JOB_ID_SQL = SOURCE_PAGE_BY_JOB_ID
            + " and storage_path is not null";

    public static final String EXTRACTED_SOURCE_PAGES_BY_JOB_ID_SQL = SOURCE_PAGE_BY_JOB_ID
            + " and original_encoding is not null";

    public static final String SOURCE_PAGES_STILL_IMPORTING_SQL = "select * "
            + "from source_page where (state = '" + PageState.IMPORTING
            + "' or state = '" + PageState.IMPORT_SUCCESS + "' or state = '"
            + PageState.IMPORT_FAIL + "') and id in "
            + "(select page_id from request where job_id is null "
            + "and page_id is not null)";
}
