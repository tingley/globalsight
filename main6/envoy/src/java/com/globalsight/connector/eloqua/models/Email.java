package com.globalsight.connector.eloqua.models;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

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
            FileUtil.writeFile(f, sb.toString(), "utf-8");
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
    public void updateFromFile(File f)
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
            }
            else // start with <titel/><hr>. no title
            {
            	htmlIndex = "<titel/><hr>".length();
            }
            
            setHtml(content.substring(htmlIndex));
        }
        catch (IOException e)
        {
            logger.error(e);
        }        
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
