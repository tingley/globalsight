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
package com.globalsight.connector.eloqua.util;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;

public class Client 
{
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
		return this.execute(uri, Method.GET, null);
	}
	
	public Response post(String uri, String body)
	{
		return this.execute(uri, Method.POST, body);
	}
	
	public Response put(String uri, String body)
	{
		return this.execute(uri, Method.PUT, body);
	}
	
	public void delete(String uri)
	{
		this.execute(uri, Method.DELETE, null);
	}
	
	public Response execute(String uri, Method method, String body) 
	{
		Response response = new Response();

		try 
		{
			URL url = new URL(_baseUrl + uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setInstanceFollowRedirects(false); 
			conn.setRequestMethod(method.toString()); 
			conn.setRequestProperty("Content-Type", "application/json"); 
	        conn.setRequestProperty("Authorization", _authToken);
	        conn.setRequestProperty("Accept-Charset", "utf-8");
	        conn.setRequestProperty("contentType", "utf-8");
	        
	        if (method == Method.POST || method == Method.PUT) 
	        {
				conn.setDoOutput(true); 
	        	final OutputStream os = conn.getOutputStream();
                os.write(body.getBytes("utf-8"));
                os.flush();
                os.close();
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