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

package com.globalsight.everest.util.applet;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AddFileVo implements Serializable
{
    private static final long serialVersionUID = -5287400150055362027L;
    private long jobId;
    private List<Long> fileProfileIds;
    private List<String> filePaths;
    private String locale;

    public long getJobId()
    {
        return jobId;
    }

    public void setJobId(long jobId)
    {
        this.jobId = jobId;
    }

    public List<Long> getFileProfileIds()
    {
        return fileProfileIds;
    }

    public void setFileProfileIds(List<Long> fileProfileIds)
    {
        this.fileProfileIds = fileProfileIds;
    }

    public List<String> getFilePaths()
    {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths)
    {
        this.filePaths = filePaths;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

}
