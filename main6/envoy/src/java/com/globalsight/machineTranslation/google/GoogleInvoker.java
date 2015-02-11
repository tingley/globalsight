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
package com.globalsight.machineTranslation.google;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.tecnick.htmlutils.htmlentities.HTMLEntities;

import org.json.JSONObject;

/**
 * @deprecated
 * @since GlobalSight 7.1.7.1
 * @author york
 *
 */
public class GoogleInvoker 
{
    private static final String ENCODING = "UTF-8";
    private static final String URL_STRING = "http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=";
    private static final String TEXT_VAR = "&q=";

    public GoogleInvoker()
    {
    }

    public static String translate(String text, String from, String to)
    	throws Exception
   	{
    	return retrieveTranslation(text, from, to);
   	}

    private static String retrieveTranslation(String text, String from, String to)
    	throws Exception
    {
//    	if(!Language.isValidLanguage(from) || !Language.isValidLanguage(to) || "".equals(to))
//    	{
//            throw new IllegalArgumentException("You must use a valid language code to translate to and from.");    		
//    	}

    	try	
    	{
            HttpURLConnection uc = null;
            URL url = null;
            String s = null;
            
            StringBuilder tempUrl = new StringBuilder();
            tempUrl.append("http://ajax.googleapis.com/ajax/services/language/translate?v=1.0&langpair=").append(from).append("%7C").append(to);
            tempUrl.append("&q=").append(URLEncoder.encode(text, "UTF-8"));

            url = new URL(tempUrl.toString());
            uc = (HttpURLConnection) url.openConnection();
            String result = toString(uc.getInputStream());
            JSONObject json = new JSONObject(result);
            String translatedText = ((JSONObject)json.get("responseData")).getString("translatedText");
            s = HTMLEntities.unhtmlentities(translatedText);
            
            if (uc.getInputStream() != null) 
            {
            	uc.getInputStream().close();
            }

            if(uc.getErrorStream() != null)
            {
            	uc.getErrorStream().close();	
            }
            
            if (uc != null) 
            {
            	uc.disconnect();
            }
            
            return s;
    	}
    	catch (Exception ex) 
    	{
	        throw new Exception("Error retrieving translation.", ex);    		
    	}

    }

	    private static String toString(InputStream inputStream)
	        throws Exception
	    {
	        StringBuilder outputBuilder = new StringBuilder();
	        
	        try
	        {
	            if(inputStream != null)
	            {
	                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	                String string;
	                while((string = reader.readLine()) != null) 
	                {
	                    outputBuilder.append(string).append('\n');	                	
	                }
	            }
	        }
	        catch(Exception ex)
	        {
	            throw new Exception("[google-api-translate-java] Error reading translation stream.", ex);
	        }
	        
	        return outputBuilder.toString();
	    }

}
