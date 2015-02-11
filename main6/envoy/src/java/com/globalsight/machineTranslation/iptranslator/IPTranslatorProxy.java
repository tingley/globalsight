package com.globalsight.machineTranslation.iptranslator;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
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

    @Override
    protected String doTranslation(Locale sourceLocale, Locale targetLocale,
            String p_string) throws MachineTranslationException
    {
        String[] result = doBatchTranslation(sourceLocale, targetLocale,
                new String[] { p_string });

        if (result != null && result.length > 0)
        {
            return result[0];
        }

        return null;
    }

    protected String[] doBatchTranslation(Locale sourceLocale,
            Locale targetLocale, String[] p_strings)
            throws MachineTranslationException
    {
        @SuppressWarnings("rawtypes")
        Map paramMap = getMtParameterMap();
        String url = (String) paramMap.get(MTProfileConstants.MT_IP_URL);
        String key = (String) paramMap.get(MTProfileConstants.MT_IP_KEY);
        boolean isXlf = MTHelper.isXlf(paramMap);
        String mtEngineWordCountKey = getKeyForMtWordCount();

        return IPTranslatorUtil.doBatchTranslation(url, key,
                getPair(sourceLocale), getPair(targetLocale), p_strings, isXlf,
                mtEngineWordCountKey);
    }

    private String getPair(Locale locale)
    {
        String lang = locale.getLanguage();
        String country = locale.getCountry();
        return IPTranslatorUtil.checkLang(lang, country);
    }

    /**
     * Use source page Id and target locale Id as key to record the MT engine
     * word count in cache for later usage. e.g. "sourcePageId_targetLocaleId".
     */
    private String getKeyForMtWordCount()
    {
        String key = null;
        Map paramMap = getMtParameterMap();
        Object spIdObj = paramMap.get(MachineTranslator.SOURCE_PAGE_ID);
        Object trgLocaleObj = paramMap.get(MachineTranslator.TARGET_LOCALE_ID);
        if (spIdObj != null && trgLocaleObj != null)
        {
            key = String.valueOf((Long) spIdObj) + "_"
                    + String.valueOf((Long) trgLocaleObj);
        }
        return key;
    }
}
