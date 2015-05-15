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

import com.globalsight.ling.common.HtmlEntities;

public class ImageViewHandler implements ViewHandler
{
    private static Pattern P = Pattern.compile("<eloquaImg>([\\d\\D]*?)</eloquaImg>");
    
    List<String> values = new ArrayList<String>();
    
    
    
    @Override
    public String getTyle()
    {
        return "CoreOrion.ImageView.design";
    }

    @Override
    public void handContent(String content) throws JSONException
    {
        JSONObject tt = new JSONObject(content);
        
        if (tt.has("toolTip"))
        {
            HtmlEntities en = new HtmlEntities();
            String hyperlinkHoverValue = tt.getString("toolTip");
            if (hyperlinkHoverValue != null && hyperlinkHoverValue.length() > 0 && !"null".equals(hyperlinkHoverValue))
            {
                hyperlinkHoverValue = en.encodeStringBasic(hyperlinkHoverValue);
                values.add(hyperlinkHoverValue);
            }
        }
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
        
        int start = content.indexOf("toolTip: \"");
        if (start == -1)
            return content;
        
        start += "toolTip: \"".length();
        int end = content.indexOf("\",naturalSize:");
        String value = values.remove(0);
        HtmlEntities en = new HtmlEntities();
        value = en.decodeStringBasic(value);
        
        value = JSONObject.quote(value);
        value = value.replace("\\\\/", "\\/");
        String all = content.substring(0, start - 1) + value + content.substring(end + 1);
        return all;
    }

}
