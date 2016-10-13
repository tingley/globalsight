package com.globalsight.terminology.searchreplace;

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

public class SearchResult
{
    private long ConceptID;
    private long levelId;
    private String fieldContent;
    private String type;
    
    public void setConceptId(long cid) {
        this.ConceptID =  cid;
    }
    
    public long getConceptId() {
        return this.ConceptID;
    }
    
    public void setLevelId(long levelid) {
        this.levelId = levelid;
    }
    
    public long getLevelId() {
        return this.levelId;
    }
    
    public void setFiled(String field) {
        this.fieldContent = field;
    }
    
    public String getField() {
        return this.fieldContent;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return this.type;
    }
}
