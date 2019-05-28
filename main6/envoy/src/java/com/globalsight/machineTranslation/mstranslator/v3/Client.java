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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.globalsight.connector.eloqua.util.Response;
import com.globalsight.machineTranslation.globalese.MyMethod;

public class Client 
{
    private static final Logger logger = Logger.getLogger(Client.class);
    
    private String _authToken;
    private String _baseUrl;
    private String tokenUrl;
    private String msSubscriptionKey;
    private int error = 0;
    
    public Client(String authToken, String url, String tokenUrl) 
    {
        _baseUrl = url;
        _authToken = authToken;
        this.tokenUrl = tokenUrl;
    }
    
    public Client(String authToken, String msSubscriptionKey, String url, String tokenUrl) 
    {
        _baseUrl = url;
        this.msSubscriptionKey = msSubscriptionKey;
        _authToken = authToken;
        this.tokenUrl = tokenUrl;
    }
    
    public void setAuthToken (String authToken) 
    {
        _authToken = authToken;
    }
    
    public Response get(String uri)
    {
        return this.execute(uri, MyMethod.GET, null);
    }
    
    public Response post(String uri, String body)
    {
        return this.execute(uri, MyMethod.POST, body);
    }
    
    public Response post2(String uri, String body)
    {
        return this.execute2(uri, MyMethod.POST, body);
    }
    
    public Response put(String uri, String body)
    {
        return this.execute(uri, MyMethod.PUT, body);
    }
    
    public void delete(String uri)
    {
        this.execute(uri, MyMethod.DELETE, null);
    }
    
    public Response execute(String uri, int method, String body) 
    {
        Response response = new Response();
        
        String methodString = "";
        switch (method) {
        case 1:  methodString = "GET";
                break;
        case 2:  methodString = "POST";
                break;
        case 3:  methodString = "PUT";
                break;
        case 4:  methodString = "DELETE";
                break;
        }
        
        try 
        {
            URL url = new URL(_baseUrl + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setInstanceFollowRedirects(false); 
            //conn.setRequestMethod(method.toString());
            conn.setRequestMethod(methodString);
            conn.setRequestProperty("Content-Type", "application/json"); 
            conn.setRequestProperty("Content-Length", body.length() + "");
            //conn.setRequestProperty("Ocp-Apim-Subscription-Key", _authToken);
            conn.setRequestProperty("Authorization", _authToken);
            //conn.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
            
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            
            if (method == MyMethod.POST || method == MyMethod.PUT) 
            {
                conn.setDoOutput(true); 
                final OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("utf-8"));
                os.flush();
                os.close();
            }
            
            try
            {
                int statusCode = conn.getResponseCode();
                if (statusCode >= 400)
                {
                    InputStream errIs = conn.getErrorStream();
                    if (errIs != null)
                    {
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(errIs,
                                "utf-8"));
                        String line1;
                        while ((line1 = errReader.readLine()) != null)
                        {
                            if (line1.indexOf("The Text field is required.") > 0)
                            {
                                break;
                            }
                            
                            if ((line1.indexOf("\"code\":401000,") > 0 || line1.indexOf("\"code\":503000,") > 0) && error < 4)
                            {
                                _authToken = MSMTUtil.getAccessToken(msSubscriptionKey, tokenUrl);
                                errReader.close();
                                conn.disconnect(); 
                                error++;
                                System.out.println(error);
                                return execute(uri, method, body);
                            }
                            
                            logger.error(line1);
                        }
                        errReader.close();
                    }
                }
                else
                {
                    error = 0;
                }
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            
            InputStream is = conn.getInputStream(); 
            BufferedReader rd = new BufferedReader(new InputStreamReader( is, "utf-8"));

            String line;
            while ((line = rd.readLine()) != null) 
            {
                response.body += line;
            }           
            rd.close();

            response.statusCode = conn.getResponseCode(); 
            conn.disconnect(); 
        } 
        catch (Exception e) 
        {
            response.e = e;
            response.exception = e.getMessage();
        }
        return response;
    }
    
    public Response execute2(String uri, int method, String body) 
    {
        Response response = new Response();
        
        String methodString = "";
        switch (method) {
        case 1:  methodString = "GET";
                break;
        case 2:  methodString = "POST";
                break;
        case 3:  methodString = "PUT";
                break;
        case 4:  methodString = "DELETE";
                break;
        }
        int i;
        try 
        {
            URL url = new URL(_baseUrl + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setInstanceFollowRedirects(false); 
            //conn.setRequestMethod(method.toString());
            conn.setRequestMethod(methodString);
            conn.setRequestProperty("Content-Type", "application/json"); 
            conn.setRequestProperty("Content-Length", body.length() + "");
            conn.setRequestProperty("Ocp-Apim-Subscription-Key", _authToken);
            //conn.setRequestProperty("Authorization", _authToken);
            conn.setRequestProperty("X-ClientTraceId", java.util.UUID.randomUUID().toString());
            
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            
            if (method == MyMethod.POST || method == MyMethod.PUT) 
            {
                conn.setDoOutput(true); 
                final OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("utf-8"));
                os.flush();
                os.close();
            }
            
            try
            {
                int statusCode = conn.getResponseCode();
                if (statusCode >= 400)
                {
                    InputStream errIs = conn.getErrorStream();
                    if (errIs != null)
                    {
                        BufferedReader errReader = new BufferedReader(new InputStreamReader(errIs,
                                "utf-8"));
                        String line1;
                        while ((line1 = errReader.readLine()) != null)
                        {
                            logger.error(line1);
                            if (response.exception == null)
                                response.exception = "";
                            
                            response.exception += line1;
                        }
                        errReader.close();
                    }
                }
            }
            catch (Exception e)
            {
                logger.error(e);
            }
            
            InputStream is = conn.getInputStream(); 
            BufferedReader rd = new BufferedReader(new InputStreamReader( is, "utf-8"));

            String line;
            while ((line = rd.readLine()) != null) 
            {
                response.body += line;
            }           
            rd.close();

            response.statusCode = conn.getResponseCode(); 
            conn.disconnect(); 
        } 
        catch (Exception e) 
        {
            response.e = e;
            if (!(response.exception != null && response.exception.indexOf("The category parameter") > 0))
            {
                response.exception = e.getMessage();
            }
        }
        return response;
    }

    public String getAuthToken()
    {
        return _authToken;
    }

    public String getTokenUrl()
    {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl)
    {
        this.tokenUrl = tokenUrl;
    }
}