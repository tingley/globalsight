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
package com.globalsight.ling.jtidy;

/**
 * Interface for configuration property parser.
 * @author Fabrizio Giustina
 * @version $Revision $ ($Author $)
 */
public interface ParseProperty
{

    /**
     * Parse a configuration option.
     * @param value option value
     * @param option option name
     * @param configuration actual configuration instance
     * @return parsed configuration value
     */
    Object parse(String value, String option, Configuration configuration);

    /**
     * Returns the option type.
     * @return option type
     */
    String getType();

    /**
     * Returns the valid values.
     * @return valid values (text)
     */
    String getOptionValues();

    /**
     * Returns the "friendly name" for the passed value. Needed to print actual configuration setting.
     * @param option option name
     * @param value actual value
     * @param configuration actual configuration
     * @return "friendly" actual value
     */
    String getFriendlyName(String option, Object value, Configuration configuration);

}
