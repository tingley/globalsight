package com.globalsight.action;

import com.globalsight.entity.FileProfile;
import com.globalsight.util.WebClientHelper;
import com.globalsight.www.webservices.Ambassador;

public class GetAttributeAction extends Action
{

    @Override
    public String execute(String[] args) throws Exception
    {
        // long projectId = args[0];
        return null;
    }

    public static String getAttributesByProjectId(long p_projectId)
            throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        return abmassador.getAttributesByProjectId(accessToken, p_projectId);
    }

    public static long getProjectIdByFileProfile(FileProfile profile)
            throws Exception
    {
        Ambassador abmassador = WebClientHelper.getAmbassador();
        return abmassador.getProjectIdByFileProfileId(accessToken, Long
                .parseLong(profile.getId()));
    }
}
