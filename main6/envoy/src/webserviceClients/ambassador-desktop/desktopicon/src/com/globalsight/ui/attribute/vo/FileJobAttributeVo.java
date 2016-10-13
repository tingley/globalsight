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

package com.globalsight.ui.attribute.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FileJobAttributeVo extends JobAttributeVo
{
    List<String> files = new ArrayList<String>();
    
    public void addFile(String file)
    {
        files.add(file);
    }

    public List<String> getFiles()
    {
        return files;
    }

    public void setFiles(List<String> files)
    {
        this.files = files;
    }
    
    private String getName(String file)
    {
        String f = file.replace("\\", "/");
        int index = f.lastIndexOf("/");
        return file.substring(index + 1);
    }
    
    public String getLabel()
    {
        StringBuffer label = new StringBuffer();
        if (files != null)
        {
            for (String value : files)
            {
                if (label.length() > 0)
                {
                    label.append(", ");
                }
                
                label.append(getName(value));
            }
        }
        
        return label.toString();
    }
    
    public boolean isSetted()
    {
        return files != null && files.size() > 0;
    }
}
