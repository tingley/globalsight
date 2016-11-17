package com.globalsight.machineTranslation.mstranslator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.util.StringUtil;
import com.microsofttranslator.api.V2.adm.AdmAccessToken;

public class MSMTUtil implements MTProfileConstants
{
    private static final Logger logger = Logger.getLogger(MSMTUtil.class);

    public static String getMsAccessToken(String clientId, String clientSecret,
            String subscriptionKey)
    {
        String accessToken = null;
        // Old way, to be abandoned.
        if (StringUtil.isNotEmpty(clientId) && StringUtil.isNotEmpty(clientSecret))
        {
            accessToken = getAccessToken(clientId, clientSecret);
        }
        // New way
        else if (StringUtil.isNotEmpty(subscriptionKey))
        {
            accessToken = getAccessToken(subscriptionKey);
        }

        return accessToken;
    }

    /**
     * To be abandoned.
     * 
     * @param clientId
     * @param clientSecret
     * @return String
     * 
     */
    public static String getAccessToken(String clientId, String clientSecret)
    {
        String accessToken = null;
        int count = 0;
        boolean gotten = false;

        while (!gotten && count < 3)
        {
            count++;
            try
            {
                HttpPost post = new HttpPost(MT_MS_GET_ACCESS_TOKEN_URL_OLD);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("grant_type", MT_MS_GRANT_TYPE));
                params.add(new BasicNameValuePair("client_id", clientId));
                params.add(new BasicNameValuePair("client_secret", clientSecret));
                params.add(new BasicNameValuePair("scope", MT_MS_SCOPE));

                post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                HttpResponse httpResponse = HttpClients.custom().build().execute(post);

                if (httpResponse.getStatusLine().getStatusCode() == 200)
                {
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    ObjectMapper mapper = new ObjectMapper();
                    AdmAccessToken adm = mapper.readValue(strResult, AdmAccessToken.class);
                    accessToken = "Bearer " + adm.getAccess_token();
                    gotten = true;
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
        }
        return accessToken;
    }

    public static String getAccessToken(String subscriptionKey)
    {
        String accessToken = null;
        int count = 0;
        boolean gotten = false;

        while (!gotten && count < 3)
        {
            count++;
            CloseableHttpClient httpclient = null;
            try
            {
                HttpPost post = new HttpPost(MT_MS_GET_ACCESS_TOKEN_URL);
                post.setHeader(MT_MS_SUBSCRIPTION_KEY_HEADER, subscriptionKey);

                httpclient = HttpClients.custom().build();
                HttpResponse httpResponse = httpclient.execute(post);

                if (httpResponse.getStatusLine().getStatusCode() == 200)
                {
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
                    accessToken = "Bearer " + strResult;
                    gotten = true;
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
}
