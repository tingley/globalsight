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
package com.globalsight.machineTranslation.globalese;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import com.globalsight.connector.eloqua.util.Response;

public class Client 
{
    private static final Logger logger = Logger.getLogger(Client.class);
    
	private String _authToken;
	private String _baseUrl;
	
	public Client(String user, String password, String url) 
	{
		_baseUrl = url;		

		String authString = user + ":" + password;
		_authToken = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(authString.getBytes());	        
	}
	
	public Response get(String uri)
	{
		return this.execute(uri, MyMethod.GET, null);
	}
	
	public Response post(String uri, String body)
	{
		return this.execute(uri, MyMethod.POST, body);
	}
	
	public Response put(String uri, String body)
	{
		return this.execute(uri, MyMethod.PUT, body);
	}
	
	public void delete(String uri)
	{
		this.execute(uri, MyMethod.DELETE, null);
	}
	
	public static void acceptAllCerts() throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                    String authType)
            {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                    String authType)
            {
            }
        } };

        // Let's create the factory where we can set some parameters for the
        // connection
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
	
	public Response execute(String uri, int method, String body) 
	{
	    System.out.println(uri);
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
			acceptAllCerts();
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setInstanceFollowRedirects(false); 
			conn.setRequestMethod(methodString);
			conn.setRequestProperty("Content-Type", "application/json"); 
	        conn.setRequestProperty("Authorization", _authToken);
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
	                        if (line1.contains("File not found"))
	                            continue;
	                        
	                        System.out.println(line1);
	                        logger.error(line1);
	                    }
	                    errReader.close();
	                }
	            }
            }
            catch (Exception e)
            {
                e.printStackTrace();
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
}