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

package com.globalsight.restful.login;

import javax.ws.rs.core.HttpHeaders;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.globalsight.restful.RestfulApiTestHelper;

public class LoginResourceTester extends RestfulApiTestHelper
{
    /**
     * http://localhost:8080/globalsight/restfulServices/companies/{companyID}/login-helper
     */
    public String testLogin(String userName, String password)
    {
        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            String url = "http://localhost:8080/globalsight/restfulServices/companies/1000/login-helper";

            HttpGet httpGet = getHttpGet(url, userName, password);

            httpResponse = httpClient.execute(httpGet);

            return printHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            consumeQuietly(httpResponse);
        }
        return null;
    }

    private HttpGet getHttpGet(String url, String userName, String password)
    {
        HttpGet httpget = new HttpGet(url);
        httpget.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader(userName, password));
        return httpget;
    }

    public static void main(String[] args)
    {
        LoginResourceTester tester = new LoginResourceTester();

        try
        {
            tester.testLogin("york", "password");
        }
        finally
        {
            tester.shutdownHttpClient();
        }
    }
}
