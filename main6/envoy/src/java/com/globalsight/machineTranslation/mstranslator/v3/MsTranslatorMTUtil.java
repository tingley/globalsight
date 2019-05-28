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

package com.globalsight.machineTranslation.mstranslator.v3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.connector.eloqua.util.Response;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.StringUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class MsTranslatorMTUtil
{
    private static final Logger logger = Logger.getLogger(MsTranslatorMTUtil.class);

    private String accessToken = null;
    private static final String MS_MT_EXPIRE_ERROR = "The incoming token has expired";
    public static final String MSMT_CONTENT_TYPE = "text/plain";

    // New
    public static final String MS_MT_BASE_URL_V3 = "https://api.cognitive.microsofttranslator.com";
    public static final String MS_MT_TRANSLATE_V3 = "/translate?api-version=3.0";

    /**
     * Run a get request to test the Globalese connection
     * 
     * @param globClient
     * @return boolean
     */

    public static class RequestBody
    {
        String Text;

        public RequestBody(String text)
        {
            this.Text = text;
        }
    }

    public static boolean testMsTranslatorHost(Client msTransClient) throws Exception
    {
        int count = 0;
        boolean gotten = false;

        try
        {
            while (!gotten && count < 3)
            {
                count++;
                Response response = msTransClient.get("");
                if (response.statusCode == 200)
                    gotten = true;
            }
        }
        catch (Exception e)
        {
            String msg = "Invalid MS Translato URL or API key.";
            logger.info(msg);
            logger.info("Exception message is : " + e.getMessage());

            throw new Exception(msg);
        }
        return gotten;
    }

    /*
     * ============== Rules for text and array for translation
     * ===================================== The array can have at most 25
     * elements. The text value of an array element cannot exceed 1,000
     * characters including spaces. The entire text included in the request
     * cannot exceed 5,000 characters including spaces.
     * 
     */

    public String Translate(String sourceLang, String targetLang, String segment, HashMap paramMap)
            throws Exception
    {
        if (StringUtil.isEmpty(segment))
        {
            return segment;
        }

        String msCategory = (String) paramMap.get(MachineTranslator.MSMT_CATEGORY);
        String params = "";

        // optional
        if (sourceLang.length() > 0)
        {
            params = "&from=" + sourceLang;
        }

        // required
        params = params + "&to=" + targetLang;

        // optional
        if (msCategory.length() > 0)
        {
            params = params + "&category=" + msCategory;
        }
        
        if (segment.indexOf("<") > -1)
        {
            params += "&textType=html";
        }

        List<RequestBody> objList = new ArrayList<RequestBody>();
        objList.add(new RequestBody(segment));
        String content = new Gson().toJson(objList);

        return doTranslation(params, content, paramMap);
    }

    protected String doTranslation(String params, String content, HashMap paramMap) throws Exception
    {
        String result = "";

        String endpoint = (String) paramMap.get(MachineTranslator.MSMT_ENDPOINT);
        String msSubscriptionKey = (String) paramMap.get(MachineTranslator.MSMT_SUBSCRIPTION_KEY);
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap.get(MachineTranslator.MT_PROFILE);
        // uri = /translate?api-version=3.0&from=en&to=de&category=generalNN
        String uri = MS_MT_TRANSLATE_V3 + params;

        try
        {
            if (accessToken == null)
            {
                accessToken = MSMTUtil.getAccessToken(msSubscriptionKey, mtProfile.getMsTokenUrl());
            }

            Client msTransClient = new Client(accessToken, msSubscriptionKey, endpoint, mtProfile.getMsTokenUrl());

            boolean needTranslateAgain = true;
            int count = 0;
            // try at most 3 times
            while (accessToken != null && needTranslateAgain && count < 3)
            {
                count++;
                Response response = msTransClient.post(uri, content);
                needTranslateAgain = false;
                
                // The token maybe changed.
                accessToken = msTransClient.getAuthToken();
                
                result = getTransFromResponse(response);
            }
        }
        catch (Exception ex)
        {
            if (ex.getMessage().contains(MS_MT_EXPIRE_ERROR)
                    || ex.getMessage().toLowerCase().contains("connection timed out"))
            {
                try
                {
                    accessToken = MSMTUtil.getAccessToken(msSubscriptionKey, mtProfile.getMsTokenUrl());
                    Client msTransClient = new Client(accessToken, endpoint, mtProfile.getMsTokenUrl());
                    accessToken = msTransClient.getAuthToken();
                    boolean needTranslateAgain = true;
                    int count = 0;
                    // try at most 3 times
                    while (accessToken != null && needTranslateAgain && count < 3)
                    {
                        count++;
                        Response response = msTransClient.post(uri, content);
                        needTranslateAgain = false;
                        result = getTransFromResponse(response);
                    }
                }
                catch (Exception e)
                {
                    // CATEGORY.error(e.getMessage());
                }
            }

            if (result == null || "".equals(result))
            {
                result = "";
            }
            else
            {
                // CATEGORY.info(exceptionMsg);
            }
        }
        return result;
    }

    public static Response getResponse(Client msTransClient, String msCategory, String sourceLang,
            String targetLang, String segment)
    {
        String params = "";
        String uri = "";

        // optional
        if (sourceLang.length() > 0)
        {
            params = "&from=" + sourceLang;
        }

        // required
        params = params + "&to=" + targetLang;

        // optional
        if (msCategory.length() > 0)
        {
            params = params + "&category=" + msCategory;
        }

        uri = MS_MT_TRANSLATE_V3 + params;
        if (segment.indexOf("<") > -1)
        {
            uri += "&textType=html";
        }

        List<RequestBody> objList = new ArrayList<RequestBody>();
        objList.add(new RequestBody(segment));
        String content = new Gson().toJson(objList);
        
        return msTransClient.post(uri, content);
    }
    
    public static String Translate2(Client msTransClient, String msCategory, String sourceLang,
            String targetLang, String segment) throws Exception
    {
        Response response = getResponse(msTransClient, msCategory, sourceLang, targetLang, segment);
        String target = getTransFromResponse(response);

        // return response.body;
        return target;
    }

    public static String getTransFromResponse(Response response)
    {
        String target = "";

        int statusCode = response.statusCode;
        if (statusCode == 200 || statusCode == 201)
        {
            String strTransList = response.body;
            if (strTransList.charAt(0) == '[')
            {
                strTransList = strTransList.substring(1, strTransList.length() - 1);
            }

            // read the response JSON
            Gson gson = new Gson();
            MsTranslations trans = gson.fromJson(strTransList, MsTranslations.class);
            List<MsTranslation> translations = trans.getTranslations();

            for (int i = 0; i < translations.size(); i++)
            {
                target = translations.get(i).getText();
            }
        }
        else
        {
            // FAILED
        }

        return target;

    }

    public static String prettify(String json_text)
    {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }
}
