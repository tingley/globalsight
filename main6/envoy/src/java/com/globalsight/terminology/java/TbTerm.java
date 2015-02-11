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

import java.io.Serializable;
import java.sql.Timestamp;

import com.globalsight.everest.persistence.PersistentObject;

public class TbTerm implements Serializable
{
    /**
     * 
     */
    public static final long INITIAL_ID = -1L;
    private static final long serialVersionUID = -7763585302517076081L;

    private long id;
    private long tbid;
//    private long cid;
    private TbLanguage tblanguage;
    private TbConcept tbconcept;
    private String language;
    private String term_content;
    private String type;
    private String status;
    private String sort_key;
    private String xml;
    private Timestamp creation_date;
    private String created_by;
    private Timestamp modify_date;
    private String modify_by;
    
    //
    // PUBLIC METHODS
    //
    public void setId(long p_id)
    {
        id = p_id;
    }

    /**
     * Return the persistent object's id.
     *
     * @return the unique identifier.
     */
    public long getId()
    {
        return id;
    }
    
    public long getTbid() {
        return this.tbid;
    }
    
    public void setTbid(long id) {
        this.tbid = id;
    }
    
    public TbConcept getTbConcept() {
        return this.tbconcept;
    }
    
    public void setTbConcept(TbConcept concept) {
        this.tbconcept = concept;
    }
    
    public TbLanguage getTbLanguage() {
        return this.tblanguage;
    }
    
    public void setTbLanguage(TbLanguage tl) {
        this.tblanguage = tl;
    }
    
    public String getTermContent() {
        return term_content;
    }
    
    public void setTermContent(String content) {
        this.term_content = content;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSortKey() {
        return sort_key;
    }
    
    public void setSortKey(String sort_key) {
        this.sort_key = sort_key;
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
}
