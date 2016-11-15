package com.globalsight.machineTranslation.mstranslator;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;

public class MSMTUtil implements MTProfileConstants
{
    private static final Logger logger = Logger.getLogger(MSMTUtil.class);
    
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
