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
