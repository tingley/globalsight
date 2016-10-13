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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;

public class TbConcept extends PersistentObject
{
    private static final long serialVersionUID = -3493380753081119055L;
    public static final long INITIAL_ID = -1L;
    
    private long m_id;
    private Termbase termbase;
    private String domain;
    private String status;
    private String project;
    private String xml;
    private Timestamp creation_date;
    private String created_by;
    private Timestamp modify_date;
    private String modify_by;
    private Set<TbLanguage> languages = new HashSet<TbLanguage>();
    
    public void setId(long p_id)
    {

        m_id = p_id;

    }

    /**
     * Return the persistent object's id.
     *
     * @return the unique identifier.
     */
    public long getId()
    {
        return m_id;
    }
    
    public Termbase getTermbase(){
        return this.termbase;
    }
    
    public void setTermbase(Termbase termbase) {
        this.termbase = termbase;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getProject() {
        return project;
    }
    
    public void setProject(String project) {
        this.project = project;
    }
    
    public String getXml() {
        return xml;
    }
    
    public void setXml(String xml) {
        this.xml = xml;
    }
    
    public Timestamp getCreationDate() {
        return creation_date;
    }
    
    public void setCreationDate(Timestamp creation_date) {
        this.creation_date = creation_date;
    }
    
    public String getCreationBy() {
        return this.created_by;
    }
    
    public void setCreationBy(String created_by) {
        this.created_by = created_by;
    }
    
    public Timestamp getModifyDate() {
        return this.modify_date;
    }
    
    public void setModifyDate(Timestamp modify_date) {
        this.modify_date = modify_date;
    }
    
    public String getModifyBy() {
        return this.modify_by;
    }
    
    public void setModifyBy(String modify_by) {
        this.modify_by = modify_by;
    }
    
    public Set<TbLanguage> getLanguages() {
        return this.languages;
    }
    
    public void setLanguages(Set<TbLanguage> languages) {
        this.languages = languages;
    }
}
