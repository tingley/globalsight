package com.globalsight.machineTranslation.iptranslator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MTHelper2;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.iptranslator.response.XliffTranslationResponse;

public class IPTranslatorUtil
{
    private static final Logger logger =
            Logger.getLogger(IPTranslatorUtil.class);

    // translate function api URL
    private static String TRANSLATE_XLIFF_URL = "/translate/xliff";
    private static String TRANSLATE_TEXT_URL = "/translate/text";
    private static String TRANSLATE_DOCUMENT_URL = "/translate/document";
    // monitor function api URL
    private static String MONITOR_URL = "/monitor";

    // client connection timeout and socket timeout
    private static final int INFINITE = 0;

    private static IPTRequestManager ipTranslatorBean = new IPTRequestManager();

    // Case sensitive !!!
    // "En" must be "from" or "to".
    public static final HashMap<String, String> supportLangs = new HashMap<String, String>();
    static
    {
        supportLangs.put("en", "En");
        supportLangs.put("fr", "Fr");
        supportLangs.put("de", "De");
        supportLangs.put("pt", "Pt");
        supportLangs.put("es", "Es");
        supportLangs.put("ja", "Ja");
        supportLangs.put("zhcn", "ZhCn");
        supportLangs.put("zhtw", "ZhTw");
    }

    /**
     * Test IPTranslator host, only init it.
     * 
     * @param ipUrl
     * @param ipKey
     * @param ipFrom
     * @param ipTo
     * @throws Exception
     */
    public static void testIPHost(String ipUrl, String ipKey, String ipFrom,
            String ipTo) throws Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, INFINITE);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                INFINITE);

        StringEntity monitorParams = ipTranslatorBean.checkMonitorParams(ipKey);

        // make request and receive response
        HttpResponse response = post(monitorParams, ipUrl + MONITOR_URL,
                httpClient);

        String theString = ipTranslatorBean.checkMonitorBack(response);
        if (StringUtils.isBlank(theString) || theString.contains("Error"))
        {
            Exception e = new Exception("IPTranslator Init call failed.");
            throw e;
        }
    }

    /**
     * check if the response code is 200
     */
    private static boolean checkResponse(HttpResponse response)
    {
        if (response.getStatusLine().getStatusCode() != 200)
        {
            return false;
        }
        return true;
    }

    private static HttpResponse post(StringEntity params, String url,
            DefaultHttpClient httpClient) throws IOException
    {
        HttpPost postRequest = new HttpPost(url);

        postRequest.setEntity(params);

        HttpResponse response = httpClient.execute(postRequest);

        return response;
    }

    private static String[] translate(String transXliffUrl, String key,
            String from, String to, String[] segments,
            DefaultHttpClient httpClient, boolean isXlf,
            String p_mtEngineWordCountKey) throws IOException
    {
        //IPTranslator uses XLF specification, replace "i" to "id".
        if (!isXlf)
        {
            for (int i = 0; i < segments.length; i++)
            {
                segments[i] = segments[i].replace(" i=", " id=");
            }
        }

        // Seems IPTranslator's "ZhCn" and "ZhTw" engines can not handle '@"
        // well, remove it when send to IPTranslator.
        // For "jp", it is fine.
        if (to.toLowerCase().startsWith("zh"))
        {
            for (int i = 0; i < segments.length; i++)
            {
                segments[i] = segments[i].replace("@", "");
            }
        }

        if (MTHelper.isLogDetailedInfo(IPTranslatorProxy.ENGINE_IPTRANSLATOR))
        {
            for (int j = 0; j < segments.length; j++)
            {
                logger.info("All source segments in xliff :: " + segments[j]);
            }
        }

        // Convert translationRequest to JSON string, then wrap it as a
        // StringEntity with encoding UTF-8, finally set the API accept type.
        StringEntity translateParams = ipTranslatorBean.checkTranslateParams(
                key, from, to, segments);

        // make request and receive response
        HttpResponse response = post(translateParams, transXliffUrl, httpClient);

        // initial translate response object
        XliffTranslationResponse translateResponse = null;

        // check translate response
        if (checkResponse(response))
        {
            // decode translation response
            translateResponse = IPTRequestManager.mapper.readValue(response
                    .getEntity().getContent(), XliffTranslationResponse.class);

            if (logger.isDebugEnabled())
            {
                logger.debug(IPTRequestManager.mapper
                        .writeValueAsString(translateResponse));
            }

            // consume used response
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            // print translation response
            if (translateResponse != null)
            {
                long wordCount = translateResponse.getWordCount();
                if (wordCount > 0 && p_mtEngineWordCountKey != null)
                {
                    MTHelper2.putValue(p_mtEngineWordCountKey, (int) wordCount);
                }
//                if (MTHelper.isLogDetailedInfo())
                {
                    logger.info("Word count from IPTranslator : " + wordCount);
                }
                String[] translatedXlf = translateResponse.getXliff();
                HashMap<Integer, HashMap<String, Integer>> xliff_status =
                        translateResponse.getXliff_status();
                if (isAllSegsTransSuccessful(xliff_status))
                {
                    if (!isXlf)
                    {
                        // Change the "id" back to "i".
                        for (int j = 0; j < translatedXlf.length; j++)
                        {
                            translatedXlf[j] = translatedXlf[j].replace(" id=",
                                    " i=").replace("trans-unit i=",
                                    "trans-unit id=");
                        }
                    }

                    if (MTHelper.isLogDetailedInfo(IPTranslatorProxy.ENGINE_IPTRANSLATOR))
                    {
                        for (int m = 0; m < translatedXlf.length; m++)
                        {
                            logger.info("All translated segments in xliff :: " + translatedXlf[m]);
                        }
                    }

                    return translatedXlf;
                }
                else
                {
                    logger.warn("Translation failed: "
                            + translateResponse.toString());
                }
            }
        }

        // error checking
        if (translateResponse == null)
        {
            logger.error("Translation failed.");
        }

        logger.info("Translation completed");
        return new String[0];
    }

    public static boolean supportsLocalePair(Locale sourcelocale,
            Locale targetlocale)
    {
        if (sourcelocale == null || targetlocale == null)
        {
            return false;
        }

        String srcLang = sourcelocale.getLanguage();
        String srcCountry = sourcelocale.getCountry();
        srcLang = checkLang(srcLang, srcCountry);

        String trgLang = targetlocale.getLanguage();
        String trgCountry = targetlocale.getCountry();
        trgLang = checkLang(trgLang, trgCountry);

        if (("en".equalsIgnoreCase(srcLang) && isLocaleSupported(trgLang))
                || ("en".equalsIgnoreCase(trgLang) && isLocaleSupported(srcLang)))
        {
            return true;
        }

        return false;
    }

    private static boolean isLocaleSupported(String lang)
    {
        if (supportLangs.keySet().contains(lang)
                || supportLangs.values().contains(lang))
            return true;

        return false;
    }

    public static String checkLang(String lang, String country)
    {
        String lowercaseLang = lang.toLowerCase();
        if ("zh".equalsIgnoreCase(lang))
        {
            if ("CN".equalsIgnoreCase(country)
                    || "SG".equalsIgnoreCase(country))
            {
                lowercaseLang = "zhcn";
            }
            else if ("TW".equalsIgnoreCase(country)
                    || "HK".equalsIgnoreCase(country)
                    || "MO".equalsIgnoreCase(country))
            {
                lowercaseLang = "zhtw";
            }
        }

        return supportLangs.get(lowercaseLang);
    }

    public static String[] doBatchTranslation(String url, String key,
            String from, String to, String[] p_string, boolean isXlf,
            String mtEngineWordCountKey) throws MachineTranslationException
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();

        // set client timeout
        int maxWaitTime = getMaxWaitTime() * 1000;
        httpClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, maxWaitTime/*INFINITE*/);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                maxWaitTime/*INFINITE*/);

        String transXliffUrl = url + TRANSLATE_XLIFF_URL;
        logger.info("Start Translation");
        String[] result = { "" };
        try
        {
            result = translate(transXliffUrl, key, from, to, p_string,
                    httpClient, isXlf, mtEngineWordCountKey);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage() + " from "
                    + from + " to " + to);
            throw new MachineTranslationException(e.getMessage());
        }

        return result;
    }

    /**
     * Check if all "trans-unit" are translated successfully.
     * @param xliff_status
     * @return
     */
    private static boolean isAllSegsTransSuccessful(
            HashMap<Integer, HashMap<String, Integer>> xliff_status)
    {
        // If no "xliff_status", we take it as successful.
        if (xliff_status == null)
            return true;

        // As we send all segments in ONE XLF file for translation, there will
        // be only one key "0" in "xliff_status".
        HashMap<String, Integer> statusMap = xliff_status.get(0);
        if (statusMap == null)
            return true;

        boolean flag = true;
        for (Integer status : statusMap.values())
        {
            // 0 means successful translation
            if (status != 0)
                flag = false;
            break;
        }

        return flag;
    }

    private static int getMaxWaitTime()
    {
        // Default 15 minutes(900 seconds).
        int maxWaitTime = 900;
        try
        {
            String param = MTHelper.getMTConfig("iptranslator.max.wait.timeout");
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
