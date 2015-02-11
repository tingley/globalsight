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
package com.globalsight.everest.coti;

// GlobalSight
import java.util.Date;

import com.globalsight.everest.coti.util.COTIConstants;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a COTI document
 * 
 */
public class COTIDocument extends PersistentObject
{
    private long projectId;
    private boolean isTranslation = true;
    private String fileRef;
    private String encoding;
    private String mimeType;
    private String fileType;
    private String creationDate;
    private String description;
    private boolean isExternal = false;

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public boolean getIsExternal()
    {
        return isExternal;
    }

    public void setIsExternal(boolean isExternal)
    {
        this.isExternal = isExternal;
    }

    public boolean getIsTranslation()
    {
        return isTranslation;
    }

    public void setIsTranslation(boolean isTranslation)
    {
        this.isTranslation = isTranslation;
    }

    public String getFileRef()
    {
        return fileRef;
    }

    public void setFileRef(String fileRef)
    {
        String temp = fileRef;
        if (fileRef.startsWith(COTIConstants.Dir_TranslationFiles_Name)
                || fileRef.startsWith(COTIConstants.Dir_ReferenceFiles_Name))
        {
            int index = fileRef.indexOf("\\");
            if (index == -1)
            {
                index = fileRef.indexOf("/");
            }

            while (fileRef.charAt(index) == '\\'
                    || fileRef.charAt(index) == '/')
            {
                index = index + 1;
            }

            temp = fileRef.substring(index);
        }

        this.fileRef = temp;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(String creationDate)
    {
        this.creationDate = creationDate;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
