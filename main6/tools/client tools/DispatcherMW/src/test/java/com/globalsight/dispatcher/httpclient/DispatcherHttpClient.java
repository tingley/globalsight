package com.globalsight.dispatcher.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.globalsight.dispatcher.bo.AppConstants;

public class DispatcherHttpClient implements AppConstants
{
    String BASIC_URL = "http://joey-pc:8080/dispatcher/translate/?";

    public static void main(String[] args)
    {
        DispatcherHttpClient client = new DispatcherHttpClient();
        client.doGetTranslate1("en_US", "zh_CN", "China");// Welcome to China

    }

    public String doGetTranslate(String p_srcLang, String p_trgLang, String src)
    {
        String result = "No Result!";
        try
        {
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("q", "httpclient"));
            qparams.add(new BasicNameValuePair("btnG", "Google Search"));
            qparams.add(new BasicNameValuePair("aq", "f"));
            qparams.add(new BasicNameValuePair("oq", null));
            URI uri = URIUtils.createURI("http", "www.google.com", -1, "/search",
                                         URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet httpget = new HttpGet(uri);
            System.out.println(httpget.getURI());
            //http://www.google.com/search?q=httpclient&btnG=Google+Search&aq=f&oq=
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public String doGetTranslate1(String p_srcLang, String p_trgLang, String src)
    {

        String result = "No Result!";
        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            StringBuilder url = new StringBuilder(BASIC_URL);
            url.append(JSONPN_SOURCE_LANGUAGE).append("=").append(p_srcLang).append("&");
            url.append(JSONPN_TARGET_LANGUAGE).append("=").append(p_trgLang).append("&");
            url.append(JSONPN_SOURCE_TEXT).append("=").append(src);
            HttpGet getRequest = new HttpGet(url.toString());
            getRequest.addHeader("accept", "application/json");
            getRequest.addHeader("Content-Type", "text/html; charset=UTF-8");

            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != 200)
            {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

//            BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()), "UTF-8"));
//
//            String output;
//            System.out.println("Output from Server .... \n");
//            while ((output = br.readLine()) != null)
//            {
//                System.out.println(output);
//                result = output;
//            }
            
            /* 5 处理 HTTP 响应内容 */  
            // HTTP响应头部信息，这里简单打印  
            Header[] headers = getRequest.getAllHeaders();  
            for (Header h : headers)  
                System.out.println(h.getName() + "------------ " + h.getValue());  
              
            // 读取 HTTP 响应内容，这里简单打印网页内容  
//            byte[] responseBody = response.getResponseBody();// 读取为字节数组  
//            response = new String(responseBody, charset);  
//            System.out.println("----------response:"+response);  
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // A Simple JSON Response Read
                // InputStream instream = entity.getContent();
                // String jsonText = convertStreamToString(instream);

                String jsonText = EntityUtils.toString(entity, HTTP.UTF_8);
                System.out.println("jsonText:\t" + jsonText);
                // ... toast code here
            }

            httpClient.getConnectionManager().shutdown();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
