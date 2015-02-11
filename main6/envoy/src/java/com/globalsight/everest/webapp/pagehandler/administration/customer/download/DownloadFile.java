/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.administration.customer.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.util.StringUtil;

public class DownloadFile
{
    String title;
    String key;
    boolean isFolder;
    boolean isLazy;
    boolean expand;
    List<DownloadFile> children;
    
    public DownloadFile(File p_file)
    { 
        this(p_file.getName(), p_file.getPath().replaceAll("\\\\", "/"), p_file.isDirectory(), p_file.isDirectory(), 
                false, null);
    }
    
    public DownloadFile(String p_title, String p_key, boolean p_isFolder, boolean p_isLazy, 
            boolean p_expand, List<DownloadFile> p_children)
    {
        title = p_title;
        key = p_key;
        isFolder = p_isFolder;
        isLazy = p_isLazy;
        expand = p_expand;
        children = p_children;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getKey()
    {
        return key;
    }
    
    public String getJSONKey()
    {
        return StringUtil.replace(key, "\\", "\\\\");
    }

    public void setKey(String key)
    {
        this.key = key;
    }
    
    public boolean isFolder()
    {
        return isFolder;
    }

    public void setFolder(boolean isFolder)
    {
        this.isFolder = isFolder;
    }
    
    public boolean isLazy()
    {
        return isLazy;
    }

    public void setLazy(boolean isLazy)
    {
        this.isLazy = isLazy;
    }

    public boolean isExpand()
    {
        return expand;
    }

    public void setExpand(boolean expand)
    {
        this.expand = expand;
    }

    public List<DownloadFile> getChildren()
    {
        return children;
    }

    public void setChildren(List<DownloadFile> children)
    {
        this.children = children;
    }
    
    public void addChildren(DownloadFile p_child)
    {
        if (children == null)
        {
            children = new ArrayList<DownloadFile>();
        }

        children.add(p_child);
    }
    
    public String toJSON()
    {
        StringBuilder result = new StringBuilder();
        if (title != null && title.trim().length() > 0)
        {
            result.append("{");
            result.append("\"title\":\"").append(this.getTitle()).append("\", ");
            result.append("\"key\":\"").append(this.getJSONKey()).append("\", ");
            result.append("\"isFolder\":").append(this.isFolder()).append(", ");
            result.append("\"isLazy\":").append(this.isLazy()).append(", ");
            result.append("\"expand\":").append(this.isExpand());

            if (children != null && children.size() > 0)
            {
                result.append(", \"children\":[");
                for (DownloadFile reportFile : children)
                {
                    result.append(reportFile.toJSON()).append(", ");
                }
                result.delete(result.length() - 2, result.length());
                result.append("]");
            }

            result.append("}");
        }

        return result.toString();
    }

}
