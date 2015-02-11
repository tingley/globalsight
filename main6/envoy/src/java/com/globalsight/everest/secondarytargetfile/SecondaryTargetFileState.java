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

package com.globalsight.everest.secondarytargetfile;

import com.globalsight.everest.page.PageState;
import com.globalsight.everest.jobhandler.Job;

/**
 * This class defines the states for a Secondary Target File object.
 */
public class SecondaryTargetFileState 
{    
    /**
     * Constant used for identifying a secondary target file as 
     * an out of date file.
     */
    public final static String OUT_OF_DATE = PageState.OUT_OF_DATE;
    
    /**
     * Constant used for identifying a secondary target file that's 
     * part of an active job.
     */
    public final static String ACTIVE_JOB = PageState.ACTIVE_JOB;
    
    /**
     * Constant used for identifying a secondary target file that is part
     * of a cancelled workflow.
     */
    public static final String CANCELLED = Job.CANCELLED;

    /**
     * Constant used for identifying a secondary target file that has 
     * been localized.
     */
    public final static String LOCALIZED = PageState.LOCALIZED;
    
    /**
     * Constant used for identifying a secondary target file that has 
     * been exported.
     */
    public final static String EXPORTED = PageState.EXPORTED;

    /**
     * Constant used for identifying a secondary target file in the 
     * process of being exported.
     */
    public final static String EXPORT_IN_PROGRESS = PageState.EXPORT_IN_PROGRESS;

    /**
     * Constant used for identifying a secondary target file that failed
     * to be exported.
     */
    public final static String EXPORT_FAIL = PageState.EXPORT_FAIL;    
}
