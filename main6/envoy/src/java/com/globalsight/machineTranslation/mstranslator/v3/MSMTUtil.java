package com.globalsight.machineTranslation.mstranslator.v3;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;

public class MSMTUtil
{
    //https://docs.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-translate?tabs=curl
    private static final Logger logger = Logger.getLogger(MSMTUtil.class);
    private static HashMap<String, RecordedToken> RECORDED_TOKENS = new HashMap<>();

    private static String getRecordedToken(String subscriptionKey)
    {
        RecordedToken recordedToken = RECORDED_TOKENS.get(subscriptionKey);
        if (recordedToken == null)
            return null;
        
        // When expired, token will become null.
        return recordedToken.getToken();
    }
    
    private static void addRecordedToken(String subscriptionKey, String accessToken, String tokenUrl)
    {
        RecordedToken recordedToken = new RecordedToken();
        recordedToken.setToken(accessToken);
        RECORDED_TOKENS.put(subscriptionKey + tokenUrl, recordedToken);
    }
    
    public static String getAccessToken(String subscriptionKey, String tokenUrl)
    {
        String token = getRecordedToken(subscriptionKey + tokenUrl);
        if (token != null)
        {
            return token;
        }
        
        
        String accessToken = null;
        int count = 0;
        boolean gotten = false;

        while (!gotten && count < 3)
        {
            count++;
            CloseableHttpClient httpclient = null;
            try
            {
                HttpPost post = new HttpPost(tokenUrl);
                post.setHeader(MTProfileConstants.MT_MS_SUBSCRIPTION_KEY_HEADER, subscriptionKey);

                httpclient = HttpClients.custom().build();
                HttpResponse httpResponse = httpclient.execute(post);

                if (httpResponse.getStatusLine().getStatusCode() == 200)
                {
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    accessToken = "Bearer " + strResult;
                    gotten = true;
                    
                    // record token with time
                    addRecordedToken(subscriptionKey, accessToken, tokenUrl);
                }
            }
            catch (Exception e)
            {
                logger.error("Failed to get access token from MS Translator with error: "
                        + e.getMessage());
                if (logger.isDebugEnabled())
                {
                    logger.error(e);
                }
            }
            finally
            {
                shutdownHttpClient(httpclient);
            }
        }
        return accessToken;
    }

    
    private static void shutdownHttpClient(CloseableHttpClient httpclient)
    {
        if (httpclient == null)
            return;

        try
        {
            httpclient.close();
            httpclient = null;
        }
        catch (IOException e)
        {
            logger.warn(e);
        }
    }
   
    
    public static boolean isEmpty(String s)
    {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}

