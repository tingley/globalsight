package com.globalsight.machineTranslation.safaba;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

public class SafabaProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger logger = Logger.getLogger(SafabaProxy.class);

    /**
     * There are 2 kinds of trsnaltions.
     * If this flag is true, all segments are sent to Safaba with tags directly.
     * If this flag is false, only plain texts in segments are sent to Safaba.
     */
    public static boolean DIRECT_TRANSLATE = false;
    
    public String getEngineName()
    {
        return ENGINE_SAFABA;
    }

    public boolean supportsLocalePair(Locale sourceLocale,
            Locale targetLocale) throws MachineTranslationException
    {
        Map paramMap = getMtParameterMap();
        String host = (String) paramMap.get(TMProfileConstants.MT_SAFA_HOST);
        String port = (String) paramMap.get(TMProfileConstants.MT_SAFA_PORT);
        String companyName = (String) paramMap.get(TMProfileConstants.MT_SAFA_COMPANY_NAME);
        String password = (String) paramMap.get(TMProfileConstants.MT_SAFA_PASSWORD);
        String client = (String) paramMap.get(TMProfileConstants.MT_SAFA_CLIENT);

        String langPair = SafabaTranslateUtil.getLocalePairs(sourceLocale, targetLocale);
        try
        {
            int maxWaitingTimeInSeconds = 60;
            String translated = SafabaTranslateUtil.translate(host,
                    Integer.parseInt(port), companyName, password, client,
                    langPair, "This is a test.", maxWaitingTimeInSeconds);
            if (StringUtils.isNotEmpty(translated))
            {
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            logger.warn("Failed to find out whether Safaba support this language pair: "
                    + e.getMessage());
            return false;
        }
    }

    protected String doTranslation(Locale sourceLocale,
            Locale targetLocale, String text)
            throws MachineTranslationException
    {
        int maxWaitingTimeInSeconds = 60;//getMaxWaitTime();
        try
        {
            Map paramMap = getMtParameterMap();
            String host = (String) paramMap.get(TMProfileConstants.MT_SAFA_HOST);
            String port = (String) paramMap.get(TMProfileConstants.MT_SAFA_PORT);
            String companyName = (String) paramMap.get(TMProfileConstants.MT_SAFA_COMPANY_NAME);
            String password = (String) paramMap.get(TMProfileConstants.MT_SAFA_PASSWORD);
            String client = (String) paramMap.get(TMProfileConstants.MT_SAFA_CLIENT);
            
            String langPair = SafabaTranslateUtil.getLocalePairs(sourceLocale, targetLocale);
            
            return SafabaTranslateUtil.translate(host, Integer.parseInt(port),
                    companyName, password, client, langPair, text,
                    maxWaitingTimeInSeconds);
        }
        catch (Exception e)
        {
            logger.error("Failed to translate via Safaba after waiting "
                    + maxWaitingTimeInSeconds + " seconds : " + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error(e);
            }
            return "";
        }
    }

    protected String[] doBatchTranslation(Locale sourceLocale,
            Locale targetLocale, String[] segments)
            throws MachineTranslationException
    {
        int maxWaitingTimeInSeconds = getMaxWaitTime();
        try
        {
            Map paramMap = getMtParameterMap();
            String host = (String) paramMap.get(TMProfileConstants.MT_SAFA_HOST);
            String port = (String) paramMap.get(TMProfileConstants.MT_SAFA_PORT);
            String companyName = (String) paramMap.get(TMProfileConstants.MT_SAFA_COMPANY_NAME);
            String password = (String) paramMap.get(TMProfileConstants.MT_SAFA_PASSWORD);
            String client = (String) paramMap.get(TMProfileConstants.MT_SAFA_CLIENT);
            String langPair = SafabaTranslateUtil.getLocalePairs(sourceLocale, targetLocale);
            
            return SafabaTranslateUtil.batchTranslate(host,
                    Integer.parseInt(port), companyName, password, client,
                    langPair, segments, maxWaitingTimeInSeconds);
        }
        catch (Exception e)
        {
            logger.error("Failed to do batch translation via Safaba after waiting "
                    + maxWaitingTimeInSeconds + " seconds : " + e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.error(e);
            }
            return null;
        }
    }

    private static int getMaxWaitTime()
    {
        int maxWaitTime = 1500;
        try
        {
            String param = MTHelper.getMTConfig("safaba.max.wait.timeout");
            if (param != null)
            {
                maxWaitTime = Integer.parseInt(param);
            }
        }
        catch (Exception ignore)
        {

        }
        return maxWaitTime;
    }
}
