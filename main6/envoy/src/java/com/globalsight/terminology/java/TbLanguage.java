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
import java.util.HashSet;
import java.util.Set;

public class TbLanguage implements Serializable
{

    /**
     * 
     */
    public static final long INITIAL_ID = -1L;
    private static final long serialVersionUID = -2990951246059676638L;
    
    private long m_id;
    private long tbid;
    private TbConcept concept;
    private String name;
    private String xml;
    private String local;
    private Set<TbTerm> terms = new HashSet<TbTerm>();
    
    /**
     * Return the persistent object's id.
     *
     * @return the unique identifier.
     */
    public long getId()
    {
        return m_id;
    }
    
    public void setId(long p_id)
    {
        m_id = p_id;
    }
    
    public long getTbid()
    {
        return tbid;
    }
    
    public void setTbid(long tbid)
    {
        this.tbid = tbid;
    }
    
    public TbConcept getConcept() {
        return this.concept;
    }
    
    public void setConcept(TbConcept concept) {
        this.concept = concept;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getXml() {
        return xml;
    }
    
    public void setXml(String xml) {
        this.xml = xml;
    }
    
    public String getLocal() {
        return local;
    }
    
    public void setLocal(String local) {
        this.local = local;
    }
    
    public Set<TbTerm> getTerms() {
        return terms;
    }
    
    public void setTerms(Set<TbTerm> terms) {
        this.terms = terms;
    }
}
