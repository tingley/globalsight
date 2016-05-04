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

package com.globalsight.restful.tmprofile;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.globalsight.restful.RestfulApiTestHelper;

public class TmProfileResourceTester extends RestfulApiTestHelper
{
    private String userName = null;
    private String password = null;

    public TmProfileResourceTester(String userName, String password)
    {
        this.userName = userName;
        this.password = password;
    }

    /**
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tmprofiles/{id}
     */
    public void testGetTMProfile()
    {
        String url = "http://localhost:8080/globalsight/restfulServices/companies/York/tmprofiles/1";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpGet httpGet = getHttpGet(url, userName, password);

            httpResponse = httpClient.execute(httpGet);

            printHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

    }

    /**
     * http://localhost:8080/globalsight/restfulServices/companies/{companyName}/tmprofiles
     */
    public void testGetAllTmProfiles()
    {
        String url = "http://localhost:8080/globalsight/restfulServices/companies/York/tmprofiles";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpGet httpGet = getHttpGet(url, userName, password);

            httpResponse = httpClient.execute(httpGet);

            printHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            consumeQuietly(httpResponse);
        }
    }

    public static void main(String[] args)
    {
        TmProfileResourceTester tester = new TmProfileResourceTester("york", "password");

        try
        {
            tester.testGetTMProfile();

            tester.testGetAllTmProfiles();
        }
        finally
        {
            tester.shutdownHttpClient();
        }
    }
}
