package com.globalsight.ling.tm3.core;


public class TestLocale implements TM3Locale {

    private long id;
    private String m_language;
    private String m_country;
    private boolean m_isUiLocale = false;
    
    TestLocale() { }
    
    public TestLocale(long id) {
        this.id = id;
    }
    
    public long getId() {
        return id;
    }
    
    void setId(long id) {
        this.id = id;
    }
    
    /**
     * Constructor
     */
    public TestLocale(String p_language, String p_country,
        boolean p_isUiLocale)
    {
        m_language = p_language;
        m_country = p_country;
        m_isUiLocale = p_isUiLocale;
    }


    public boolean isUiLocale()
    {
        return m_isUiLocale;
    }

    public String getLanguageCode()
    {
        return m_language;
    }

    public String getCountryCode()
    {
        return m_country;
    }
    
    // 
    // Hibernate Accessors
    //
    
    public String getCountry()
    {
        return m_country;
    }

    public void setCountry(String m_country)
    {
        this.m_country = m_country;
    }

    public boolean isIsUiLocale()
    {
        return m_isUiLocale;
    }

    public void setIsUiLocale(boolean uiLocale)
    {
        m_isUiLocale = uiLocale;
    }

    public String getLanguage()
    {
        return m_language;
    }

    public void setLanguage(String m_language)
    {
        this.m_language = m_language;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof TestLocale)) {
            return false;
        }
        TestLocale l = (TestLocale)o;
        return l.getLanguageCode().equals(getLanguageCode()) &&
               l.getCountryCode().equals(getCountryCode());
    }
    
    @Override
    public int hashCode() {
        return getLanguageCode().hashCode() * 17 + getCountryCode().hashCode();
    }
    
    @Override
    public String toString() {
        return getLanguageCode() + "_" + getCountryCode();
    }

    @Override
    public String getLocaleCode() {
        return toString();
    }
}
