package com.globalsight.everest.util.system;

import java.util.Map;

import com.globalsight.util.system.ConfigException;

// In-memory System Configuration that returns whatever params you give it.
// Does not currently enforce per-company parameters.
public class MockSystemConfiguration extends SystemConfiguration {

    private Map<String, String> params;
    
    public MockSystemConfiguration(Map<String, String> params) {
        this.params = params;
    }
    
    @Override
    public String getStringParameter(String pParamName) throws ConfigException {
        return params.get(pParamName);
    }

    @Override
    public String getStringParameter(String pParamName, String pCompanyId)
            throws ConfigException {
        // XXX This is incorrect; when tests eventually need this we will need
        // to implement it for real
        return params.get(pParamName);
    }

    @Override
    public String toString() {
        return "MockSystemConfiguration";
    }

}
