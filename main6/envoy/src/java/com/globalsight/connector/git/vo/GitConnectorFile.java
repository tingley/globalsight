package com.globalsight.connector.git.vo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.util.StringUtil;

public class GitConnectorFile
{
	String id;
    String title;
    String path;
    boolean isFolder;
    boolean isLazy;
    boolean expand;
    List<GitConnectorFile> children;
    
    public GitConnectorFile(File p_file)
    { 
        this(p_file.getName(), p_file.getPath(), p_file.isDirectory(), p_file.isDirectory(), 
                false, null);
    }
    
    public GitConnectorFile(String p_title, String p_path, boolean p_isFolder, boolean p_isLazy, 
            boolean p_expand, List<GitConnectorFile> p_children)
    {
    	title = p_title;
        path = p_path;
        isFolder = p_isFolder;
        isLazy = p_isLazy;
        expand = p_expand;
        children = p_children;
        setId(CreateJobUtil.getFileId(path));
    }
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setName(String title)
    {
        this.title = title;
    }

    public String getPath()
    {
        return path;
    }
    
    public String getJSONPath()
    {
        return StringUtil.replace(path, "\\", "\\\\");
    }

    public void setPath(String path)
    {
        this.path = path;
        setId(CreateJobUtil.getFileId(path));
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

    public List<GitConnectorFile> getChildren()
    {
        return children;
    }

    public void setChildren(List<GitConnectorFile> children)
    {
        this.children = children;
    }
    
    public void addChildren(GitConnectorFile p_child)
    {
        if (children == null)
        {
            children = new ArrayList<GitConnectorFile>();
        }

        children.add(p_child);
    }
    
    public String toJSON()
    {
        StringBuilder result = new StringBuilder();
        if (title != null && title.trim().length() > 0)
        {
            result.append("{");
            result.append("\"id\":\"").append(this.getId()).append("\", ");
            result.append("\"title\":\"").append(this.getTitle()).append("\", ");
            result.append("\"path\":\"").append(this.getJSONPath()).append("\", ");
            result.append("\"isFolder\":").append(this.isFolder()).append(", ");
            result.append("\"isLazy\":").append(this.isLazy()).append(", ");
            result.append("\"expand\":").append(this.isExpand());

            if (children != null && children.size() > 0)
            {
                result.append(", \"children\":[");
                for (GitConnectorFile reportFile : children)
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
