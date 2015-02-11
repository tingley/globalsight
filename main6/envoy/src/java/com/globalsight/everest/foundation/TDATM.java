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

package com.globalsight.everest.foundation;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;

public class TDATM extends PersistentObject
{
    private static final long serialVersionUID = 8874587622069814431L;

    private int enable = 0;
    private String host_name = "";
    private String userName = "";
    private String password = "";
    private String description;
    private TranslationMemoryProfile profile;

    public int getEnable()
    {
        return enable;
    }

    public void setEnable(int enable)
    {
        this.enable = enable;
    }

    public String getHostName()
    {
        return host_name;
    }

    public void setHostName(String host_name)
    {
        this.host_name = host_name;
    }

    public String getUserName()
    {

        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPassword()
    {

        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDescription()
    {

        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public TranslationMemoryProfile getTranslationMemoryProfile()
    {

        return profile;
    }

    public void setTranslationMemoryProfile(TranslationMemoryProfile profile)
    {
        this.profile = profile;
    }
}
