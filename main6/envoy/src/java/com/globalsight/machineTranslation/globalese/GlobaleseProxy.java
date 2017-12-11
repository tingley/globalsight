package com.globalsight.machineTranslation.globalese;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

public class GlobaleseProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY = Logger.getLogger(GlobaleseProxy.class);

    @Override
    public String getEngineName()
    {
        return ENGINE_GLOBALESE;
    }

    @Override
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale)
            throws MachineTranslationException
    {
        return true;
    }

    private String getGlobaleseLocale(Locale locale)
    {
        String l = locale.toString();
        l = l.toLowerCase().replace("_", "-");
        return l;
    }

    @Override
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
            throws MachineTranslationException
    {
        HashMap paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMTProfileById((String) paramMap.get(MachineTranslator.MT_PROFILE_ID));
        Client globClient = new Client(mtProfile.getUsername(), mtProfile.getAccountinfo(),
                mtProfile.getUrl());
        Long groupId = Long.parseLong(mtProfile.getJsonValue("groupId"));
        Long engineId = Long.parseLong(mtProfile.getJsonValue("engineId"));
        String trans = GlobaleseMTUtil.translate(globClient, groupId, engineId,
                getGlobaleseLocale(p_sourceLocale), getGlobaleseLocale(p_targetLocale), p_string,
                15);

        return trans;
    }

    @Override
    protected String[] doBatchTranslation(Locale p_sourceLocale, Locale p_targetLocale,
            String[] p_segments) throws MachineTranslationException
    {
        HashMap paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                .getMTProfileById((String) paramMap.get(MachineTranslator.MT_PROFILE_ID));
        Client globClient = new Client(mtProfile.getUsername(), mtProfile.getAccountinfo(),
                mtProfile.getUrl());
        Long groupId = Long.parseLong(mtProfile.getJsonValue("groupId"));
        Long engineId = Long.parseLong(mtProfile.getJsonValue("engineId"));
        if (mtProfile.isLogDebugInfo())
        {
            for (int i = 0; i < p_segments.length; i++)
            {
                CATEGORY.info("Source segment[" + i + "]:" + p_segments[i]);
            }
        }
        List<String> result = GlobaleseMTUtil.batchTranslate(globClient, groupId, engineId,
                "", "", p_segments, 20);
        if (!result.isEmpty())
        {
            String[] results = new String[result.size()];
            for (int i = 0; i < results.length; i++)
            {
                results[i] = result.get(i);
            }
                
            if (mtProfile.isLogDebugInfo())
            {
                for (int i = 0; i < results.length; i++)
                {
                    CATEGORY.info("Translated segment[" + i + "]:" + results[i]);
                }
            }
            return results;
        }
        return null;
    }
}
