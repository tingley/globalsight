package com.globalsight.machineTranslation.mstranslator;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileConstants;
import com.microsofttranslator.api.V2.adm.AdmAccessToken;

public class MSMTUtil implements TMProfileConstants
{
    private static final Logger logger = Logger.getLogger(MSMTUtil.class);
    
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
                HttpPost post = new HttpPost(MT_MS_GET_ACCESS_TOKEN_URL);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("grant_type", MT_MS_GRANT_TYPE));
                params.add(new BasicNameValuePair("client_id", clientId));
                params.add(new BasicNameValuePair("client_secret", clientSecret));
                params.add(new BasicNameValuePair("scope", MT_MS_SCOPE));
                
                post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                HttpResponse httpResponse = new DefaultHttpClient().execute(post);
                
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
}
