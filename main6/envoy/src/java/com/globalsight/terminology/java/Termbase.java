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

package com.globalsight.terminology.java;

import java.util.HashSet;
import java.util.Set;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.persistence.PersistentObject;

public class Termbase extends PersistentObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 5300454145070353965L;
    private String name;
    private String description;
    private String defination;
    private Company company;
    private Set concepts = new HashSet();
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDefination() {
        return defination;
    }
    
    public void setDefination(String defination) {
        this.defination = defination;
    }
    
    public Company getCompany() {
        return company;
    }
    
    public void setCompany(Company company) {
        this.company = company;
    }
    
    public Set getConcepts() {
        return this.concepts;
    }
    
    public void setConcepts(Set concepts) {
        this.concepts = concepts;
    }

}
