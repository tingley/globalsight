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
package com.globalsight.connector.eloqua.util.config;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AutoMappingObj
{
    private String globalSightLanguage;
    private String eloquaLanguage;
    
    public String getEloquaLanguage()
    {
        return eloquaLanguage;
    }
    
    public void setEloquaLanguage(String eloquaLanguage)
    {
        this.eloquaLanguage = eloquaLanguage;
    }

    public String getGlobalSightLanguage()
    {
        return globalSightLanguage;
    }

    public void setGlobalSightLanguage(String globalSightLanguage)
    {
        this.globalSightLanguage = globalSightLanguage;
    }
    
    
}
