/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.restful;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class RestfulApiTestHelper
{
    public final static int MAX_SEND_SIZE = 5 * 1024 * 1024;//5M

    public final static String ACCESS_TOKEN = "accessToken";

    private CloseableHttpClient httpClient = null;

    protected HttpPost getHttpPost(String url, String accessToken)
    {
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader(ACCESS_TOKEN, accessToken);
        return httppost;
    }

    protected HttpGet getHttpGet(String url, String accessToken)
    {
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(ACCESS_TOKEN, accessToken);
        return httpget;
    }

    protected HttpPut getHttpPut(String url, String accessToken)
    {
        HttpPut httpput = new HttpPut(url);
        httpput.setHeader(ACCESS_TOKEN, accessToken);
        return httpput;
    }

    protected HttpDelete getHttpDelete(String url, String accessToken)
    {
        HttpDelete httpdelete = new HttpDelete(url);
        httpdelete.setHeader(ACCESS_TOKEN, accessToken);
        return httpdelete;
    }

    CookieSpecProvider easySpecProvider = new CookieSpecProvider()
    {
        public CookieSpec create(HttpContext context)
        {
            return new BrowserCompatSpec()
            {
                @Override
                public void validate(Cookie cookie, CookieOrigin origin)
                        throws MalformedCookieException {
                    // Oh, I am easy
                }
            };
        }
    };

    Registry<CookieSpecProvider> reg = RegistryBuilder
            .<CookieSpecProvider> create()
            .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
            .register(CookieSpecs.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory())
            .register("mySpec", easySpecProvider)
            .build();

    RequestConfig requestConfig = RequestConfig.custom()
            .setCookieSpec("mySpec").setConnectTimeout(600000)
            .setSocketTimeout(600000).build();

    protected CloseableHttpClient getHttpClient()
    {
        if (httpClient == null)
        {
            httpClient = HttpClients.custom().setDefaultCookieSpecRegistry(reg)
                    .setDefaultRequestConfig(requestConfig).build();
        }

        return httpClient;
    }

    protected void consumeQuietly(HttpResponse httpResponse)
    {
        if (httpResponse != null)
        {
            try
            {
                EntityUtils.consumeQuietly(httpResponse.getEntity());                
            }
            catch (Exception ignore)
            {
                
            }
        }
    }

    protected void shutdownHttpClient()
    {
        if (httpClient == null)
            return;

        try
        {
            httpClient.close();
            httpClient = null;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected final static String authorizationHeader(String username, String password)
    {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);

        return authHeader;
    }

    protected String printHttpResponse(HttpResponse httpResponse) throws ParseException,
            IOException
    {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        System.out.println("Status code: " + statusCode + "\r\n");

        String res = httpResponse.getStatusLine().toString();
        System.out.println("Status line: " + res + "\r\n");

        String entityContent = EntityUtils.toString(httpResponse.getEntity());
        System.out.println(entityContent);

        return entityContent;
    }
}
