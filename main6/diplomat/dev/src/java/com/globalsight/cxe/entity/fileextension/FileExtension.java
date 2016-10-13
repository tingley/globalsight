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
package com.globalsight.cxe.entity.fileextension;

/** Represents a CXE File Extension entity object. */

public interface FileExtension
{
    /**
     ** Return the id of the File Extension (cannot be set)
     ** 
     * @return id as a long
     **/
    public long getId();

    /**
     ** Return the name of the File Extension
     ** 
     * @return File Extension name
     **/
    public String getName();

    /**
     ** Sets the name of the File Extension
     **/
    public void setName(String p_name);

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId();

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId);

}
