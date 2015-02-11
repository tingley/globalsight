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
 * DelayedImportRequestDescriptorModifier extends DescriptorModifier by
 * providing amendment methods unique to the DelayedImportRequest descriptor.
 */
public class DelayedImportRequestDescriptorModifier
{
    private static final String REQ_ID_ARG = "delayedImportRequestId";
    public static final String DELAYED_IMPORT_REQ_BY_REQ_ID_SQL = "select "
            + "dir.* from delayed_import_request dir where " + "dir.id = :"
            + REQ_ID_ARG;
}
