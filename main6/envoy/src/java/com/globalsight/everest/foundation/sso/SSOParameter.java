package com.globalsight.everest.foundation.sso;

import java.util.HashMap;

public class SSOParameter
{
    public static String COMPANY_NAME = "COMPANY_NAME";
    public static String SSO_USER_NAME = "SSO_USER_NAME";
    
    private HashMap<String, String> store;
    
    public SSOParameter()
    {
        store = new HashMap<String, String>();
    }
    
    public String setParameter(String key, String value)
    {
        return store.put(key, value);
    }
    
    public String getParameter(String key)
    {
        return store.get(key);
    }
}
