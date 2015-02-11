package com.globalsight.machineTranslation.safaba;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.TextNode;

public class SafabaProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger logger = Logger.getLogger(SafabaProxy.class);

    /**
     * There are 2 kinds of translations.
     * If this flag is true, all segments are sent to Safaba with tags directly.
     * If this flag is false, only plain texts in segments are sent to Safaba.
     */
    public static boolean DIRECT_TRANSLATE = true;
    
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
        if (DIRECT_TRANSLATE)
        {
            return translateWithTags(sourceLocale, targetLocale, segments);
        }
        else
        {
            return transatePureText(sourceLocale, targetLocale, segments);
        }
    }

    /**
     * Send segments with tags to MT engine.
     */
    private String[] translateWithTags(Locale sourceLocale,
            Locale targetLocale, String[] segments)
    {
        boolean isXlf = MTHelper.isXlf(this.getMtParameterMap());

        String[] results = new String[segments.length];
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

            List<String> allSegments = new ArrayList<String>();
            for (int i = 0; i < segments.length; i++)
            {
                GxmlElement gxmlRoot = MTHelper.getGxmlElement("<segment>" + segments[i] + "</segment>");
                String gxmlWithId = segments[i];
                if (!isXlf)
                {
                    gxmlWithId = segments[i].replace(" i=", " id=");
                }
                boolean isAllTagsHaveId = MTHelper.isAllTagsHaveIdAttr(gxmlWithId);
                List subFlowList = gxmlRoot.getDescendantElements(GxmlElement.SUB_TYPE);
                // If segment gxml has NO subs, send whole gxml to MT.
                if (isAllTagsHaveId
                        && (subFlowList == null || subFlowList.size() == 0))
                {
                    allSegments.add(gxmlWithId);
                }
                else
                {
                    // If segment gxml HAS subs, send texts to MT then compose
                    // back again. This is because Safaba can not handle segment
                    // with sub segments well.
                    List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);
                    for (int subIndex = 0; subIndex < items.size(); subIndex++)
                    {
                        TextNode textNode = (TextNode) items.get(subIndex);
                        allSegments.add(textNode.toGxml());
                    }
                }
            }

            String[] allSegmentsArr = new String[allSegments.size()];
            for (int k = 0; k < allSegments.size(); k++)
            {
                allSegmentsArr[k] = allSegments.get(k);
            }

            String[] translatedSegs = SafabaTranslateUtil.batchTranslate(host,
                    Integer.parseInt(port), companyName, password, client,
                    langPair, allSegmentsArr, maxWaitingTimeInSeconds);

            if (translatedSegs == null
                    || translatedSegs.length != allSegmentsArr.length)
                return null;

            int counter = 0;
            for (int mainIndex = 0; mainIndex < segments.length; mainIndex++)
            {
                GxmlElement gxmlRoot = MTHelper.getGxmlElement("<segment>" + segments[mainIndex] + "</segment>");
                String gxmlWithId = segments[mainIndex];
                if (!isXlf)
                {
                    gxmlWithId = segments[mainIndex].replace(" i=", " id=");
                }
                boolean isAllTagsHaveId = MTHelper.isAllTagsHaveIdAttr(gxmlWithId);
                List subFlowList = gxmlRoot.getDescendantElements(GxmlElement.SUB_TYPE);
                // If segment gxml has NO subs, send whole gxml to MT.
                if (isAllTagsHaveId
                        && (subFlowList == null || subFlowList.size() == 0))
                {
                    results[mainIndex] = translatedSegs[counter];
                    if (!isXlf)
                    {
                        results[mainIndex] = results[mainIndex].replace(" id=", " i=");
                    }
                    counter++;
                }
                else
                {
                    // If segment gxml HAS subs, send texts to MT then compose
                    // back again. This is because Safaba can not handle segment
                    // with sub segments well.
                    List items = MTHelper.getImmediateAndSubImmediateTextNodes(gxmlRoot);
                    if (items != null && items.size() > 0)
                    {
                        for (int subIndex = 0; subIndex < items.size(); subIndex++)
                        {
                            TextNode textNode = (TextNode) items.get(subIndex);
                            textNode.setTextBuffer(new StringBuffer(translatedSegs[counter]));
                            counter++;
                        }
                        results[mainIndex] = GxmlUtil.stripRootTag(gxmlRoot.toGxml());
                    }
                }
            }

            return results;
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

    /**
     * Send pure texts to MT engine for translation.
     */
    private String[] transatePureText(Locale sourceLocale,
            Locale targetLocale, String[] segments)
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
