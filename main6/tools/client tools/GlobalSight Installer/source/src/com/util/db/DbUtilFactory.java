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
package com.util.db;

import com.util.ServerUtil;

/**
 * A factory class, provide a faculty to get a <code>DbUtil</code>.<br>
 * Which <class>DbUtil</class> implement will be return according to system is
 * windows or linux.
 * <p>
 * More information can be get from <code>DbUtil</code> and
 * <code>ServerUtil.isInLinux()</code>
 */
public class DbUtilFactory
{
    private static DbUtil DB_UTIL;

    /**
     * Gets a subclass of <code>DbUtil</code> according to system is linux or
     * not.
     * 
     * @return The subclass of <code>DbUtil</code>.
     * @see com.util.db.DbUtil
     */
    public static DbUtil getDbUtil()
    {
        if (DB_UTIL == null)
        {
            DB_UTIL = ServerUtil.isInLinux() ? new DbUtilInLinux()
                    : new DbUtilInWindows();
        }

        return DB_UTIL;
    }
}
