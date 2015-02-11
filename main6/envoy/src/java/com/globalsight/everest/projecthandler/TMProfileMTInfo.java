package com.globalsight.everest.projecthandler;


public class TMProfileMTInfo
{

    private long id;

    private long tmProfileID;

    private String mtEngine;

    private String mtKey;

    private String mtValue;

    private TranslationMemoryProfile tmProfile;
    
    public TMProfileMTInfo()
    {
        super();
    }
    
    public TMProfileMTInfo(String mtEngine, String key, String value)
    {
        this.mtEngine = mtEngine;
        this.mtKey = key;
        this.mtValue = value;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getTmProfileID()
    {
        return tmProfileID;
    }

    public void setTmProfileID(long tmProfileID)
    {
        this.tmProfileID = tmProfileID;
    }

    public String getMtEngine()
    {
        return mtEngine;
    }

    public void setMtEngine(String mtEngine)
    {
        this.mtEngine = mtEngine;
    }

    public String getMtKey()
    {
        return mtKey;
    }

    public void setMtKey(String mtKey)
    {
        this.mtKey = mtKey;
    }

    public String getMtValue()
    {
        return mtValue;
    }

    public void setMtValue(String mtValue)
    {
        this.mtValue = mtValue;
    }

    public TranslationMemoryProfile getTmProfile()
    {
        return tmProfile;
    }

    public void setTmProfile(TranslationMemoryProfile tmProfile)
    {
        this.tmProfile = tmProfile;
        if (tmProfile != null)
        {
            this.tmProfileID = tmProfile.getId();
        }
    }
    
}
