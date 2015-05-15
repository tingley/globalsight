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

import com.globalsight.connector.eloqua.util.EloquaHelper;

/**
 * 
 * Used to handler Form view. But we can not create a form with eloqua api.
 *
 */
public class FormViewHandler implements ViewHandler
{
    private EloquaHelper eh;
    private boolean uploaded;
    private String targetLocale;
    private static Pattern P = Pattern.compile("<eloquaForm>([\\d\\D]*?)</eloquaForm>");
    
    List<String> values = new ArrayList<String>();
    
    @Override
    public String getTyle()
    {
        return "CoreOrion.FormView.design";
    }

    @Override
    public void handContent(String content) throws JSONException
    {
        JSONObject tt = new JSONObject(content);
        values.add(tt.getString("contentId"));
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
        int start = content.indexOf("},value: \"") + "},value: \"".length();
        int end = content.indexOf("\",inlineStyles:");
        
        return content.substring(0, start - 1) + JSONObject.quote(values.remove(0)) + content.substring(end + 1);
    }

    public EloquaHelper getEh()
    {
        return eh;
    }

    public void setEh(EloquaHelper eh)
    {
        this.eh = eh;
    }

    public boolean isUploaded()
    {
        return uploaded;
    }

    public void setUploaded(boolean uploaded)
    {
        this.uploaded = uploaded;
    }

    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }
}
