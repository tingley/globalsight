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
package com.globalsight.log;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * Implement  {@link LoggerFactory} interface to create new instances
 * of {@link GlobalSightCategory}.
 */
public class GlobalSightCategoryFactory
    implements LoggerFactory
{
    /**
     * The constructor should be public as it will be called by
     * configurators in different packages.
     */
    public GlobalSightCategoryFactory()
    {
    }

    /**
     * Make a new instance of {@link GlobalSightCategory}.
     * @param p_name Name of {@link Logger}
     * @return instance of GlobalSightCategory.
     */
    public Logger makeNewLoggerInstance(String p_name)
    {
        return new GlobalSightCategory(p_name);
    }
}  

