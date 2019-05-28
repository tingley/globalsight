/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.machineTranslation.mstranslator.v2;

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
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateArrayResponse;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.util.StringUtil;
import com.microsofttranslator.api.V2.adm.AdmAccessToken;

public class MSMTUtil implements MTProfileConstants
{
    private static final Logger logger = Logger.getLogger(MSMTUtil.class);

    public static String getMsAccessToken(String clientId, String clientSecret,
            String subscriptionKey, String tokenUrl)
    {
        String accessToken = null;
        // Old way, to be abandoned.
        if (StringUtil.isNotEmpty(clientId) && StringUtil.isNotEmpty(clientSecret))
        {
            accessToken = getAccessTokenOld(clientId, clientSecret);
        }
        // New way
        else if (StringUtil.isNotEmpty(subscriptionKey))
        {
            accessToken = getAccessToken(subscriptionKey, tokenUrl);
        }

        return accessToken;
    }
    
    public static String getMsAccessToken(MSTranslateConfig config)
    {
        return getMsAccessToken(config.getMsClientId(), config.getMsClientSecret(),
                config.getMsSubscriptionKey(), config.getMsTokenUrl());
    }
    
    public static String[] toArray(TranslateArrayResponse[] result)
    {
        if (result != null)
        {
            String[] results = new String[result.length];
            for (int i = 0; i < result.length; i++)
            {
                results[i] = result[i].getTranslatedText();
            }
            return results;
        }
        
        return null;
    }

    /**
     * To be abandoned.
     * 
     * @param clientId
     * @param clientSecret
     * @return String
     * 
     */
    public static String getAccessTokenOld(String clientId, String clientSecret)
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

    public static String getAccessToken(String subscriptionKey, String tokenUrl)
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
                HttpPost post = new HttpPost(tokenUrl);
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
