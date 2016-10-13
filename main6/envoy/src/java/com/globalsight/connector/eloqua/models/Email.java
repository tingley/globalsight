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
package com.globalsight.connector.eloqua.models;

import java.io.File;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.connector.eloqua.models.view.ViewUtil;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.util.FileUtil;


public class Email extends EloquaObject
{
    static private final Logger logger = Logger.getLogger(Email.class);
    private String subject;
    
    @Override
    public String getDisplayId()
    {
        return "e" + getId();
    }
   
    public void setSubject(String subject)
    {
        this.subject = subject;
        
        if (json != null)
        {
            try
            {
                json.put("subject", subject);
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }

    public String getSubject()
    {
        return subject;
    }
    
    public void saveToFile(File f)
    {
        try
        {
            StringBuffer sb = new StringBuffer();
            if (subject == null)
                sb.append("<titel/><hr>");
            else
                sb.append("<titel>").append(subject).append("</titel><hr>");
            sb.append(html);
            
            if (isStructuredHtmlContent())
            {
                // just for preview
                String path = f.getAbsolutePath() + ".preview.html";
                FileUtil.writeFile(new File(path), sb.toString(), "utf-8");
                
                JSONObject js = getJson();
                JSONObject cont = js.getJSONObject("htmlContent");
                String root = cont.getString("root");
                
                EloquaHelper eh = new EloquaHelper(getConnect());
                ViewUtil util = new ViewUtil(root, eh);
                String html = util.generateHtml();
                FileUtil.writeFile(f, html, "utf-8");
            }
            else
            {
                FileUtil.writeFile(f, sb.toString(), "utf-8");
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }
    
    /**
     * Update the translated name, subject and html body
     * @param f
     */
    public boolean updateFromFile(File f, boolean uploaded, String targetLocale)
    {
        String content;
        try
        {
            content = FileUtil.readFile(f, "utf-8");
            
            int htmlIndex = 0;
            if (content.startsWith("<titel>"))
            {
            	int i1 = content.indexOf("<titel>") + "<titel>".length();
                int i2 = content.indexOf("</titel>", i1);
                setSubject(content.substring(i1, i2));
                htmlIndex = i2 + "</titel><hr>".length();
                
                setHtml(content.substring(htmlIndex));
            }
            else if (content.startsWith("<titel/><hr>"))// start with <titel/><hr>. no title
            {
            	htmlIndex = "<titel/><hr>".length();
            	
            	setHtml(content.substring(htmlIndex));
            }
            else if (content.startsWith("<body>")) // from editor.
            {
                JSONObject js = getJson();
                JSONObject cont = js.getJSONObject("htmlContent");
                String root = cont.getString("root");
                
                EloquaHelper eh = new EloquaHelper(getConnect());
                ViewUtil util = new ViewUtil(root, eh);
                String newRoot = util.updateFromFile(content, uploaded, targetLocale);
                cont.put("root", newRoot);
                cont.remove("htmlBody");
                
                return false;
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        
        return true;
    }

    @Override
    public void setJson(JSONObject json, boolean fromList)
    {
        super.setJson(json, fromList);
        
        if (!fromList)
        {
            try
            {
            	if (json.has("subject"))
            	{
            		this.subject = json.getString("subject");
            	}                
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }
}
