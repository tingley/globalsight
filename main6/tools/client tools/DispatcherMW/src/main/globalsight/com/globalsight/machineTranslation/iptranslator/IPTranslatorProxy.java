package com.globalsight.machineTranslation.iptranslator;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

public class IPTranslatorProxy extends AbstractTranslator implements
        MachineTranslator
{
    private static final Logger logger = Logger
            .getLogger(IPTranslatorProxy.class);

    public String getEngineName()
    {
        return ENGINE_IPTRANSLATOR;
    }

    @Override
    public boolean supportsLocalePair(Locale sourcelocale, Locale targetlocale)
    {
        return IPTranslatorUtil.supportsLocalePair(sourcelocale, targetlocale);
    }

    protected String[] doBatchTranslation(Locale sourceLocale,
            Locale targetLocale, String[] p_string)
            throws MachineTranslationException
    {
        Map paramMap = getMtParameterMap();
        String url = (String) paramMap.get(MTProfileConstants.MT_IP_URL);
        String key = (String) paramMap.get(MTProfileConstants.MT_IP_KEY);
        boolean containTags = false;
        if (null != paramMap.get(MachineTranslator.CONTAIN_TAGS))
        {
            containTags = (Boolean) paramMap
                .get(MachineTranslator.CONTAIN_TAGS);
        }

        return IPTranslatorUtil.doBatchTranslation(url, key,
                getPair(sourceLocale), getPair(targetLocale), p_string,
                containTags);
    }

    private String getPair(Locale sourceLocale)
    {
        String str = new String();
        str += Character.toUpperCase(sourceLocale.getLanguage().charAt(0));
        str += sourceLocale.getLanguage().charAt(1);
        return str;
    }

    @Override
    protected String doTranslation(Locale sourceLocale, Locale targetLocale,
            String p_string) throws MachineTranslationException
    {
        Map paramMap = getMtParameterMap();
        String url = (String) paramMap.get(MTProfileConstants.MT_IP_URL);
        String key = (String) paramMap.get(MTProfileConstants.MT_IP_KEY);
        boolean containTags = false;
        if(null!=paramMap
                .get(MachineTranslator.CONTAIN_TAGS))
        {
            containTags = (Boolean) paramMap
                .get(MachineTranslator.CONTAIN_TAGS);
        }
        String[] result = IPTranslatorUtil.doBatchTranslation(url, key,
                getPair(sourceLocale), getPair(targetLocale), new String[]
                { p_string },
                containTags);
        if (result != null && result.length > 0)
        {
            return result[0];
        }

        return null;
    }

}
