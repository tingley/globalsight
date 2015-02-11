package com.globalsight.everest.util.system;

import java.util.Map;

import com.globalsight.util.system.ConfigException;

/**
 * DynamicPropertiesSystemConfiguration is for any properties files except
 * "envoy.properties" and "envoy_generated.properties" files. This subclass is
 * to mock that for unit test purpose.
 * 
 * @author York
 * 
 */
public class MockDynamicPropertiesSystemConfiguration extends
        DynamicPropertiesSystemConfiguration
{
    private Map<String, String> params;

    public MockDynamicPropertiesSystemConfiguration(Map<String, String> params)
    {
        super();
        this.params = params;
    }
    
    @Override
    public String getStringParameter(String pParamName) throws ConfigException
    {
        return params.get(pParamName);
    }

    @Override
    public String getStringParameter(String pParamName, String pCompanyId)
            throws ConfigException
    {
        // XXX This is incorrect; when tests eventually need this we will need
        // to implement it for real
        return params.get(pParamName);
    }

    @Override
    public String toString()
    {
        return "MockDynamicPropertiesSystemConfiguration";
    }
}
