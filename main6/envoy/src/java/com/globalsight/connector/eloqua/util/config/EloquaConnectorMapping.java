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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EloquaConnectorMapping
{
    private AutoMapping autoMapping = new AutoMapping();
    private List<Mapping> headers = new ArrayList<>();
    private List<Mapping> footers = new ArrayList<>();
    
    public List<Mapping> getHeaders()
    {
        return headers;
    }
    public void setHeaders(List<Mapping> headers)
    {
        this.headers = headers;
    }
    public List<Mapping> getFooters()
    {
        return footers;
    }
    public void setFooters(List<Mapping> footers)
    {
        this.footers = footers;
    }
    public AutoMapping getAutoMapping()
    {
        return autoMapping;
    }
    public void setAutoMapping(AutoMapping autoMapping)
    {
        this.autoMapping = autoMapping;
    }
    
    
}
