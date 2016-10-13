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
package com.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SystemInfo
{
    private String version = "7.1.0.0";
    private String previousVersion = "7.1.0.0";
    
    public static final String XML_PATH = "install/data/system.xml";

    public String getPreviousVersion()
    {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion)
    {
        this.previousVersion = previousVersion;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
