package com.globalsight.bo;

import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class IsDownloadableBO {

    public String query(String accessToken, String msg) throws Exception
    {
        String result = "";

        Ambassador abmassador = WebClientHelper.getAmbassador();
        result = abmassador.getDownloadableJobs(accessToken, msg);

        return result;
    }
}
