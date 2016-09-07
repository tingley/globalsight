package com.globalsight.machineTranslation.mstranslator;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateArrayResponse;
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsofttranslator.api.V2.LanguageService;
import com.microsofttranslator.api.V2.adm.AdmAccessToken;

public class TestMSMT
{

    private static final String uriAPI = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
//    private static final String appId = "0E79EE1C580587CD7084B8E5CD763A907B18057E";
    private static final String clientId = "wallywei";
    private static final String clientSecret = "DyihDAU9kyZw2+lPB70d6lt7NpuFAY+iS9YwCG6pVhU=";
    private static final String category = "general";
    private static String accessToken;

    private LanguageService service;

    @Before
    public void setUp()
    {
        try
        {
            HttpPost post = new HttpPost(uriAPI);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("grant_type",
                    "client_credentials"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("scope",
                    "http://api.microsofttranslator.com"));

            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(post);
            if (httpResponse.getStatusLine().getStatusCode() == 200)
            {
                String strResult = EntityUtils.toString(httpResponse
                        .getEntity());
                ObjectMapper mapper = new ObjectMapper();
                AdmAccessToken adm = mapper.readValue(strResult,
                        AdmAccessToken.class);
                accessToken = "Bearer " + adm.getAccess_token();
            }
        }
        catch (MalformedURLException e)
        {
            fail(e.getMessage());
        }
        catch (UnsupportedEncodingException e)
        {
            fail(e.getMessage());
        }
        catch (ClientProtocolException e)
        {
            fail(e.getMessage());
        }
        catch (IOException e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSingleMSMT()
    {
        try
        {
            String text = "Hello world";
            String result1 = service.translate(accessToken, text, "en", "fr",
                    "text/plain", category);
            Assert.assertNotNull(result1);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testMutiMSMT()
    {
        try
        {
            TranslateOptions options = new TranslateOptions();
            options.setCategory("general");
            options.setContentType("text/plain");
            String[] texts =
            {
                    "Press [<span style='font-weight: bold;'>Enter</span>]",
                    "press [<bpt type=\"x-span\" i=\"1\" x=\"1\">&lt;span style=&quot;font-weight: bold;&quot;&gt;</bpt>enter<ept i=\"1\">&lt;/span&gt;</ept>]",
                    "The Cards List window should be displayed." };
            TranslateArrayResponse[] result = service.translateArray(
                    accessToken, texts, "en", "fr", options);
            Assert.assertTrue(result.length > 0);
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }
}
