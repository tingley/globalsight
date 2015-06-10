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
package com.globalsight.connector.eloqua.models.view;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class TextBoxViewHandler implements ViewHandler
{
    private static Pattern P = Pattern.compile("<eloquaTxt>([\\d\\D]*?)</eloquaTxt>");
    
    List<String> values = new ArrayList<String>();
    
    @Override
    public String getTyle()
    {
        return "CoreOrion.TextBoxView.design";
    }

    @Override
    public void handContent(String content) throws JSONException
    {
        int start = content.indexOf("},value: \"");
        if (start < 0)
            return;
        
        start += "},value: \"".length();
        int end = content.indexOf("\",inlineStyles:");
        
        values.add(content.substring(start, end));
    }

    public List<String> getValues()
    {
        return values;
    }

    public void setValues(List<String> values)
    {
        this.values = values;
    }

    @Override
    public void readContentFromFile(String content)
    {
        Matcher m = P.matcher(content);
        int start = 0;
        while (m.find(start))
        {
            values.add(m.group(1));
            start = m.end();
        }
    }

    @Override
    public String updateContentFromFile(String content) throws JSONException
    {
        int start = content.indexOf("},value: \"");
        if (start < 0)
            return content;
        
        start += "},value: \"".length();
        int end = content.indexOf("\",inlineStyles:");
        String value = values.remove(0);
        value = value.replace("&amp;gt;", "&gt;");
        
        return content.substring(0, start) + value + content.substring(end);
    }
}
