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
package com.globalsight.everest.localemgr;

import java.io.Serializable;

/** Represents a CAP Code set entity object.
*/
public interface CodeSet
{

    /**
     * Return the code_set of the code set
     * @return code set code_set
     */
    public String getCodeSet();


    /**
     * Set the code-set of the code set
     */
    public void setCodeSet(String p_code_set);

}

