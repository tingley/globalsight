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

import com.globalsight.cxe.entity.eloqua.EloquaConnector;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.util.FileUtil;

public abstract class EloquaObject
{
    static private final Logger logger = Logger.getLogger(EloquaObject.class);

    protected String id;
    protected String name;
    protected String html;
    protected String createBy;
    protected JSONObject json;
    protected String createdAt;

    // Save connection information for export.
    private EloquaConnector connect;

    public String getId()
    {
        return id;
    }

    public String getDisplayName()
    {
    	if (name == null)
    		return "--";
    	
    	HtmlEntities e = new HtmlEntities();
    	return e.encodeStringBasic(name);
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
        if (json != null)
        {
            try
            {
                json.put("name", name);
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }

    public JSONObject getJson()
    {
        return json;
    }

    public void setJson(JSONObject json, boolean fromList)
    {
        this.json = json;
        this.id = getString("id");
        this.name = getString("name");

        if (!fromList)
        {
            JSONObject content;
            try
            {
                content = json.getJSONObject("htmlContent");
                String type = content.getString("type");

                if ("RawHtmlContent".equalsIgnoreCase(type))
                {
                    html = content.getString("html");
                }
                else
                {
                	if (content.has("systemHeader"))
                	{
                		html = "<head>" + content.getString("systemHeader") + "</head>" + content.getString("htmlBody") ;
                	}
                	else
                	{
                		html = content.getString("htmlBody");
                	}                    
                }
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }

    public EloquaConnector getConnect()
    {
        return connect;
    }

    public void setConnect(EloquaConnector connect)
    {
        this.connect = connect;
    }

    public void setId(String id)
    {
        this.id = id;
        if (json != null)
        {
            try
            {
                json.put("id", id);
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }

    public String getString(String name)
    {
        try
        {
            if (this.json.has(name))
                return this.json.getString(name);
            else
                return "--";
        }
        catch (JSONException e)
        {
            logger.error(e);
        }

        return "";
    }

    public abstract String getDisplayId();

    public void saveJsonToFile(File f)
    {
        try
        {
            json.put("conn_company", connect.getCompany());
            json.put("conn_username", connect.getUsername());
            json.put("conn_password", connect.getPassword());
            json.put("conn_url", connect.getUrl());

            FileUtil.writeFile(f, json.toString(1), "utf-8");
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public void loadFromFile(File f)
    {
        try
        {
            String content = FileUtil.readFile(f, "utf-8");
            JSONObject json = new JSONObject(content);
            setJson(json, false);

            EloquaConnector connect = new EloquaConnector();
            connect.setCompany(json.getString("conn_company"));
            connect.setUsername(json.getString("conn_username"));
            connect.setPassword(json.getString("conn_password"));
            connect.setUrl(json.getString("conn_url"));
            setConnect(connect);
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    public String getHtml()
    {
        return html;
    }

    public void setHtml(String html)
    {
        this.html = html;

        if (json != null)
        {
            try
            {
                JSONObject content = new JSONObject();
                content.put("type", "RawHtmlContent");
                content.put("html", html);
                json.put("htmlContent", content);
            }
            catch (JSONException e)
            {
                logger.error(e);
            }
        }
    }

    public String getCreateBy()
    {
        return createBy;
    }

    public void setCreateBy(String createBy)
    {
        this.createBy = createBy;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public String getStatus()
    {
        String s = getString("currentStatus");
        s = s == null ? "--" : s;
        return s;
    }
    
    public boolean isStructuredHtmlContent()
    {
        if (json == null)
            return false;
        
        try
        {
            JSONObject cont = json.getJSONObject("htmlContent");
            return cont != null && "editor".equalsIgnoreCase((String) cont.get("contentSource"));
        }
        catch (JSONException e)
        {
            logger.error(e);
        }
        
        return false;
    }
    
}
