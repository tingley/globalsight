package com.globalsight.bo;

import com.globalsight.util.Constants;
import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class GetVersionBO
{
    public String query(String accessToken) throws Exception
    {
        String result = "";
        try
        {
            Ambassador abmassador = WebClientHelper.getAmbassador();
            result = abmassador.getGSVersion(accessToken);
        }
        catch (Exception e)
        {
            if ("No such operation 'getGSVersion'".equals(e.getMessage()))
            {
                // indicates this is using new version DI to connect old version
                // GS that does not have the new method getGSVersion(), then
                // return old version string for the version check.
                return Constants.APP_VERSION_OLD;
            }
            throw new Exception(e);
        }

        return result;
    }
}
