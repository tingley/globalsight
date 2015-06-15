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

package com.globalsight.webservices;

public class VersionInfo implements java.io.Serializable
{
    private String version;
    private String cotiVersion;
    private String cotiLevel;

    public VersionInfo()
    {

    }

    public VersionInfo(String version, String cotiVersion, String cotiLevel)
    {
        super();
        this.version = version;
        this.cotiVersion = cotiVersion;
        this.cotiLevel = cotiLevel;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getCotiVersion()
    {
        return cotiVersion;
    }

    public void setCotiVersion(String cotiVersion)
    {
        this.cotiVersion = cotiVersion;
    }

    public String getCotiLevel()
    {
        return cotiLevel;
    }

    public void setCotiLevel(String cotiLevel)
    {
        this.cotiLevel = cotiLevel;
    }

}
