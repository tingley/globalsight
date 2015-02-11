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

package com.globalsight.terminology;

import com.globalsight.everest.persistence.PersistentObject;

public class TuConceptRelation extends PersistentObject{

    /**
     * 
     */
    private static final long serialVersionUID = -6282280000129669619L;
    
    private long tuId;
    private long conceptId;
    private String addedLanguage;
    
    public long getTuId() {
        
        return tuId;
    }
    
    public void setTuId(long id) {
        this.tuId = id;
    }
    
    public long getConceptId() {   
        return conceptId;
    }
    
    public void setConceptId(long id) {
        this.conceptId = id;
    }
    
    public String getAddedLanguage() {
        return addedLanguage;
    }
    
    public void setAddedLanguage(String lan) {
        this.addedLanguage = lan;
    }
}
