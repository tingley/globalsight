/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.util;

import org.apache.log4j.Logger;

public class LogUtil
{
    // main log file
    public static Logger GSSMARTBOXLOG = Logger.getLogger("GSSmartBox");
    // failed log, all failed, exception...
    public static Logger FAILEDLOG = Logger.getLogger("failed");
    // jobs log, job create and download, failed or successful
    public static Logger JOBSLOG = Logger.getLogger("jobs");

    public static void fail(String message, Exception e)
    {
        String msg = message + "\r\n" + ExceptionStackTrace.getStackTrace(e);
        FAILEDLOG.error(msg);
    }

    public static void info(String message)
    {
        GSSMARTBOXLOG.info(message);
    }
}
