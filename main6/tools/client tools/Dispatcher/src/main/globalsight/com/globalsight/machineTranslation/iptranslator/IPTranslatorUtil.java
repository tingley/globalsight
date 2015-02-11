package com.globalsight.machineTranslation.iptranslator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.iptranslator.response.TranslateResponse;

public class IPTranslatorUtil
{
    // private static final TestLogger logger = new TestLogger();
    private static final Logger logger = Logger
            .getLogger(IPTranslatorUtil.class);
    private static final String domain = "https://sadfapi.iptranslator.com/ipt/v2";

    // translate function api URL
    private static String TRANSLATE_URL = "/translate";
    // monitor function api URL
    private static String MONITOR_URL = "/monitor";
    // terminate function api URL
    // private static final String TERMINATE_URL = "/terminate";
    public static final List<String> supportLangs = Arrays.asList("fr", "de",
            "pt", "es", "ja");
    // a simple client
    // private static DefaultHttpClient httpClient = new DefaultHttpClient();;
    // client connection timeout and socket timeout
    private static final int INFINITE = 0;
    // request encoding

    // maximum waiting time for translation engine to start
    private static final long MAXIMUM_WAIT = 1000 * 60 * 10;
    // sleep time between each translation engine status check
    private static final long SLEEP_DURATION = 1000 * 10;
    // expected status code for running translation engine
    private static final Integer RUNNING_STATUS = 3;
    // testing machine client testing key.
    private static final String key = "ryHUToaQ2zPnLLCHMT8w";
    // source language of requested translation engine
    // whether the translation engine usage should be optimized
    // private static final boolean optimized = false;
    // whether the translation engine usage should be optimized
    // private static HttpResponse response;
    private static IPTRequestManager ipTranslatorBean = new IPTRequestManager();


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
        if (isBlank(theString) || theString.contains("Error"))
        {
            Exception e = new Exception("IPTranslator Init call failed.");
            throw e;
        }
    }
    
    /**
     * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
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

    /**
     * post params to url
     */
    private static HttpResponse post(StringEntity params, String url,
            DefaultHttpClient httpClient)
            throws IOException
    {
        HttpPost postRequest = new HttpPost(url);

        postRequest.setEntity(params);

        HttpResponse response = httpClient.execute(postRequest);

        return response;
    }


    private static int checkOutStatus(String moniUrl,
            String engineId, StringEntity monitorParams,
            DefaultHttpClient httpClient) throws ClientProtocolException,
            IOException, JsonParseException, JsonMappingException,
            JsonGenerationException
    {
        int status = -1;
        // make request and receive response
        HttpResponse response = post(monitorParams, moniUrl, httpClient);

        // check response
        if (checkResponse(response))
        {

            // decode response
            HashMap<String, Integer> statusMap = IPTRequestManager.mapper
                    .readValue(response.getEntity().getContent(), HashMap.class);
            logger.info(IPTRequestManager.mapper.writeValueAsString(statusMap));
            // extract translation engine status
            if (statusMap.get(engineId) != null)
            {
                status = statusMap.get(engineId);
            }
        }

        // consume used response
        HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);

        return status;
    }

    private static String[] translat(String tranUrl, String key, String from,
            String to, boolean flag, String[] string,
            DefaultHttpClient httpClient) throws IOException
    {

        // convert translationRequest to JSON string, then wrap it as a
        // StringEntity
        // with encoding utf-8, finally set the api accept type
        StringEntity translateParams = ipTranslatorBean.checkTranslateParams(
                key, from, to, flag, string);

        // make request and receive response
        HttpResponse response = post(translateParams, tranUrl, httpClient);

        // initial translate response object
        TranslateResponse translateResponse = null;

        // check translate response
        if (checkResponse(response))
        {

            // decode translation response
            translateResponse = IPTRequestManager.mapper.readValue(response
                    .getEntity().getContent(), TranslateResponse.class);
            logger.debug(IPTRequestManager.mapper
                    .writeValueAsString(translateResponse));
            // consume used response
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            // print translation response
            if (translateResponse != null)
            {

                int[] xliff_status = translateResponse.getXliff_status();
                String[] str = flag ? translateResponse.getXliff() : translateResponse
                        .getText();
                if (null == xliff_status || xliff_status[0] != -1)
                {
                    return str;
                }
                else
                {
                    logger.warn("Translation failed: " + str[0] + ", status: "
                            + xliff_status[0]);
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
        String trgLang = targetlocale.getLanguage();
        if (srcLang.equals("zh"))
        {
            String srcCountry = sourcelocale.getCountry();
            if (trgLang.equals("en")
                    && (srcCountry.equalsIgnoreCase("CN") || srcCountry
                            .equalsIgnoreCase("SG")))
            {
                return true;
            }
        }
        if (trgLang.equals("zh"))
        {
            String trgCountry = targetlocale.getCountry();
            if (srcLang.equals("en")
                    && (trgCountry.equalsIgnoreCase("CN") || trgCountry
                            .equalsIgnoreCase("SG")))
            {
                return true;
            }
        }
        if ((srcLang.equals("en") && supportLangs.contains(trgLang))
                || (supportLangs.contains(srcLang) && trgLang.equals("en")))
        {

            return true;
        }
        return false;
    }

    public static String[] doBatchTranslation(String url, String key,
            String from, String to, String[] p_string, boolean containTags)
            throws MachineTranslationException
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // set client timeout
        httpClient.getParams().setParameter(
                CoreConnectionPNames.CONNECTION_TIMEOUT, INFINITE);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
                INFINITE);
        String tranUrl = url + TRANSLATE_URL;
        logger.info("Start Translation");

        /*************************************/
        /** Step 1 initialise engine **/
        /*************************************/

        // create an init request using key, language pair and configuration
        // parameters


        String[] result =
        { "" };
        try
        {
            result = translat(tranUrl, key, from, to, containTags, p_string,
                    httpClient);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage() + " from"
                    + from + " to" + to);
            throw new MachineTranslationException(e.getMessage());
        }
        return result;
        /***************************************/
        /** end of translation **/
        /***************************************/

        /***************************************/
        /** Step 4 mannual switch off engine **/
        /***************************************/

        // mannually switch off engine, otherwise, engine will be switched after
        // maxIdleTime

        // terminate(engineId, translateParams);
    }

    public static void main(String[] args)
    {
        String[] source =
        { "asd" };
        try
        {
            testIPHost(domain, key, null, null);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        try
        {
            for (int i = 0; i < 2; i++)
            {
                String[] result = doBatchTranslation(
                        "https://api.iptranslator.com/ipt/v2",
                        "ryHUToaQ2zPnLLCHMT8w", "En", "Fr", source, false);
                if (null == result || null == result[0])
                {
                    System.out.print(i);
                }
                System.out.print(result[0]);
            }
        }
        catch (MachineTranslationException e)
        {
            e.printStackTrace();
        }
    }
}
