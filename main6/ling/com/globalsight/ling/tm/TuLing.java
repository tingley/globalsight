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
package com.globalsight.ling.tm;

/**
 * @deprecated as "Tu" interface will not implement this any more.
 */
public interface TuLing
{

    /**
     * test if it's localizable
     * @return true if localizable, false if translatable
     */
    boolean isLocalizable();
    

    /**
     * Get the id.
     * @return The id of this Tu.
     */
    long getId();

    /**
     * Get the Tm id.
     * @return The id of this Tu's tm.
     */
    long getTmId();

    /**
     * @see com.globalsight.everest.persistent.PersistentObject#getIdAsLong()
     */
    Long getIdAsLong();

    /**
     * Get Leverage group id.
     * @return leverage group id
     */
    long getLeverageGroupId();
    
    /**
     * Set the order of this Tu - with regards to the page.
     */
    void setOrder(long p_order);
        
    long getOrder(); 
    
}
