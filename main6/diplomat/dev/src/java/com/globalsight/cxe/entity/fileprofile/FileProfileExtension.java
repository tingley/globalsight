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

package com.globalsight.cxe.entity.fileprofile;

import java.io.Serializable;

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;

public class FileProfileExtension implements Serializable
{
    private static final long serialVersionUID = -2670282219002761081L;

    FileExtensionImpl extension;
    FileProfileImpl fileProfile;

    public FileExtensionImpl getExtension()
    {
        return extension;
    }

    public void setExtension(FileExtensionImpl extension)
    {
        this.extension = extension;
    }

    public FileProfileImpl getFileProfile()
    {
        return fileProfile;
    }

    public void setFileProfile(FileProfileImpl fileProfile)
    {
        this.fileProfile = fileProfile;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object paramObject)
    {
        return super.equals(paramObject);
    }
}
