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
package com.globalsight.everest.persistence.request;

/**
 * RequestDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Request descriptor.
 */
public class RequestDescriptorModifier
{
    private static final String JOB_ID_ARG = "jobId";
    public static final String REQUEST_LIST_BY_JOB_ID = "select r1.* "
            + "from job j1, request r1 where "
            + "j1.id = r1.job_id and j1.id = :" + JOB_ID_ARG;
}
