package com.globalsight.everest.util.system;

import java.util.Map;

import com.globalsight.util.system.ConfigException;

/**
 * EnvoySystemConfiguration is for
 * "envoy.properties","envoy_generated.properties" files and "system_parameter"
 * DB store, this subclass is to mock that for unit test purpose.
 * 
 * @author York
 * 
 */
public class MockEnvoySystemConfiguration extends EnvoySystemConfiguration
{
    private Map<String, String> params;

    public MockEnvoySystemConfiguration(Map<String, String> params)
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
        return params.get(pParamName);
    }

    @Override
    public String toString()
    {
        return "MockEnvoySystemConfiguration";
    }
}
