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
package com.globalsight.config;

/**
 * System Parameter Class Definition
 */
public interface SystemParameter
{
    /**
     * Return the id of System parameter.
     *
     * <p>Note: The id is set by TopLink persistence.
     *
     * @return id as a long
     */
    public long getId();
    /**
     * Return the company_id of System parameter.
     *
     * @return id as a long
     */
    public long getCompanyId();
    
    public void setCompanyId(long p_id);
    /**
     * Return the system parameter key name
     *
     * @return system parameter key name
     */
    public String getName();

    /**
     * Return the system parameter value
     *
     * @return system parameter value
     */
    public String getValue();

    /**
     * Set the system parameter value
     *
     * @param p_value system parameter value
     */
    public void setValue(String p_value);

    /**
     * Convert int to String and set system parameter value
     *
     * @param p_intValue int system parameter value
     */
    public void setValue(int p_intValue);


    /**
     * Convert boolean to String and set system parameter value - true
     * is set as "1" and false is set as "0"
     *
     * @param p_booleanValue int system parameter value
     */
    public void setValue(boolean p_booleanValue);
}
