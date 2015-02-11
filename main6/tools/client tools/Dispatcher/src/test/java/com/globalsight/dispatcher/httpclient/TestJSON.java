package com.globalsight.dispatcher.httpclient;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class TestJSON
{
    public static void main(String[] args){
        String jsonText = "{\"srcLang\":\"en_US\",\"errorMsg\":null,\"trgLang\":\"zh_CN\",\"src\":\"China\",\"trg\":\"中国\",\"status\":\"success\"}";
        try
        {
            JSONObject jsObj = new JSONObject(jsonText);
            Iterator<String> it = jsObj.keys();
            while(it.hasNext()) {
                String key = it.next();
                System.out.println(key + ":\t" + jsObj.getString(key));
            }            
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
