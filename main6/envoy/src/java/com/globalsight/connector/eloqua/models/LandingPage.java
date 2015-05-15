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
import org.json.JSONObject;

import com.globalsight.connector.eloqua.models.view.ViewUtil;
import com.globalsight.connector.eloqua.util.EloquaHelper;
import com.globalsight.util.FileUtil;


public class LandingPage extends EloquaObject
{
    static private final Logger logger = Logger.getLogger(LandingPage.class);
    
    @Override
    public String getDisplayId()
    {
        return "p" + getId();
    }
   
    public void saveToFile(File f)
    {
        try
        {
            if (isStructuredHtmlContent())
            {
                String path = f.getAbsolutePath() + ".preview.html";
                FileUtil.writeFile(new File(path), html, "utf-8");
                
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
                FileUtil.writeFile(f, html, "utf-8");
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
            if (isStructuredHtmlContent()) // from editor.
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
            else 
            {
                setHtml(content);
            }
            
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        
        return true;
    }
}