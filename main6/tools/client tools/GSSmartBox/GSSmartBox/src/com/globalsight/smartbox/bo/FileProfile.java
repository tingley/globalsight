/**
 *  Copyright 2009, 2011 Welocalize, Inc. 
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
package com.globalsight.smartbox.bo;

import java.util.HashSet;
import java.util.Set;

/**
 * File Profile from GlobalSight
 * 
 * @author leon
 * 
 */
public class FileProfile
{

    private String id = "";
    private String name = "";
    private String sourceLocale = "";
    private Set<String> fileExtensions = null;
    private Set<String> targetLocales = null;

    public FileProfile()
    {
        fileExtensions = new HashSet<String>();
        targetLocales = new HashSet<String>();
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void setSourceLocale(String sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    public String getSourceLocale()
    {
        return sourceLocale;
    }

    public void addFileExtension(String fileExtension)
    {
        fileExtensions.add(fileExtension);
    }

    public Set<String> getFileExtensions()
    {
        return this.fileExtensions;
    }

    public void addTargetLocale(String targetLocale)
    {
        targetLocales.add(targetLocale);
    }

    public Set<String> getTargetLocale()
    {
        return this.targetLocales;
    }
}
