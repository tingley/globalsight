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
 * File Profile Business Object
 * 
 * @author leon
 * @author Joey
 *      Adds fields aliasFPName/gsUnExtractedFPName for "Usecase01".
 */
public class FileProfile
{
    private String id = "";
    private String name = "";
    private String sourceLocale = "";
    private Set<String> fileExtensions = null;
    private Set<String> targetLocales = null;
    private String aliasFPName;                 // Adds for alias File Profile Name
    private String gsUnExtractedFPName;         // Adds for UnExtracted GS File Profile Name

    public FileProfile()
    {
        fileExtensions = new HashSet<String>();
        targetLocales = new HashSet<String>();
    }
    
    public FileProfile(String p_gsFPName, String p_aliasFPName,
            String p_gsUnExtractedFPName)
    {
        name = p_gsFPName;
        aliasFPName = p_aliasFPName;
        gsUnExtractedFPName = p_gsUnExtractedFPName;
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

    public String getAliasFPName()
    {
        return aliasFPName;
    }

    public void setAliasFPName(String aliasFPName)
    {
        this.aliasFPName = aliasFPName;
    }

    public String getGsUnExtractedFPName()
    {
        return gsUnExtractedFPName;
    }

    public void setGsUnExtractedFPName(String gsUnExtractedFPName)
    {
        this.gsUnExtractedFPName = gsUnExtractedFPName;
    }
}
