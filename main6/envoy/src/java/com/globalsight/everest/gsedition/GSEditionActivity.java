package com.globalsight.everest.gsedition;

import com.globalsight.everest.persistence.PersistentObject;

public class GSEditionActivity extends PersistentObject{

    /**
     * 
     */
    private static final long serialVersionUID = -8786870153057698537L;
    
    private String name = "";
    private long fileprofile;
    private String fileProfileName;
    private int sourceFileReference = 0;
    private String description = "";
    private GSEdition gsEdition;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public long getFileProfile() {
        return fileprofile;
    }
    public void setFileProfile(long fileprofile) {
        this.fileprofile = fileprofile;
    }
    
    public String getFileProfileName() {
        return fileProfileName;
    }
    public void setFileProfileName(String fileProfileName) {
        this.fileProfileName = fileProfileName;
    }
    
    public int getSourceFileReference() {
        return sourceFileReference;
    }
    
    public void setSourceFileReference(int sourceFileReference) {
        this.sourceFileReference = sourceFileReference;
    }
    
    public String getDescription() {
        
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public GSEdition getGsEdition() {
        
        return gsEdition;
    }
    
    public void setGsEdition(GSEdition gsEdition) {
        this.gsEdition = gsEdition;
    }
}
