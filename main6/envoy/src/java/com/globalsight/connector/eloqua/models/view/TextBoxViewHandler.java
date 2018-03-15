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
        
        String txt = content.substring(start, end);
        txt = replaceEloquaTags(txt);
        values.add(txt);
    }

    private String replaceEloquaTag(String txt)
    {
        int n = txt.indexOf("<eloqua ");
        if (n > 0)
        {
            int m = txt.indexOf(">", n + 1);
            if (m > 0)
            {
                txt = txt.substring(0, n) + "&lt;gs" + txt.substring(n + 1, m) + "&gt;" + txt.substring(m + 1);
            }
        }
        
        return txt;
    }
    
    /**
     * For GBS-4820. eloqua content returned broken
     * @param txt
     * @return
     */
    private String replaceEloquaTags(String txt)
    {
        String txt2 = replaceEloquaTag(txt);
        while (!txt2.equals(txt))
        {
            txt = txt2;
            txt2 = replaceEloquaTag(txt);
        }
        
        return txt2;
    }
    
    private String replaceEloquaTagBack(String txt)
    {
        int n = txt.indexOf("&lt;gseloqua ");
        if (n > 0)
        {
            int m = txt.indexOf("&gt;", n + 1);
            if (m > 0)
            {
                txt = txt.substring(0, n) + "<" + txt.substring(n + 6, m) + ">" + txt.substring(m + 4);
            }
        }
        
        return txt;
    }
    
    private String replaceEloquaTagsBack(String txt)
    {
        String txt2 = replaceEloquaTagBack(txt);
        while (!txt2.equals(txt))
        {
            txt = txt2;
            txt2 = replaceEloquaTagBack(txt);
        }
        
        return txt2;
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
            String txt = m.group(1);
            txt = replaceEloquaTagsBack(txt);
            values.add(txt);
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
