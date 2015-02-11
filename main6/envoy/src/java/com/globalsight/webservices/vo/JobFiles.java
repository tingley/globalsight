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

package com.globalsight.webservices.vo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobFiles
{
//	private String jobName = null;
//	private long jobId;
    private String root = null;
    private List<String> paths = new ArrayList<String>();

    public List<String> getPaths()
    {
        return paths;
    }

    public void setPaths(List<String> paths)
    {
        this.paths = paths;
    }

    public void addPath(String path)
    {
        paths.add(path);
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

//	public void setJobName(String jobName) {
//		this.jobName = jobName;
//	}
//
//	public String getJobName() {
//		return jobName;
//	}
//
//	public void setJobId(long jobId) {
//		this.jobId = jobId;
//	}
//
//	public long getJobId() {
//		return jobId;
//	}
}
