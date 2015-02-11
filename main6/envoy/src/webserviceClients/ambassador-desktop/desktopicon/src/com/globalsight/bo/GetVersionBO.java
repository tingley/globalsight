package com.globalsight.bo;

import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class GetVersionBO {

    public String query(String accessToken) throws Exception
    {
        String result = "";

        Ambassador abmassador = WebClientHelper.getAmbassador();
        result = abmassador.getVersion(accessToken);

        return result;
    }
}
