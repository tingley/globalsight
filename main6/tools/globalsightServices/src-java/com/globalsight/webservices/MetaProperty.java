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

public class MetaProperty implements java.io.Serializable
{
    private Entry[] domain;
    private String uri;
    private String type;
    private String label;
    private boolean mandatory;
    

    public MetaProperty()
    {

    }


    public Entry[] getDomain()
    {
        return domain;
    }


    public void setDomain(Entry[] domain)
    {
        this.domain = domain;
    }


    public String getUri()
    {
        return uri;
    }


    public void setUri(String uri)
    {
        this.uri = uri;
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getLabel()
    {
        return label;
    }


    public void setLabel(String label)
    {
        this.label = label;
    }


    public boolean isMandatory()
    {
        return mandatory;
    }


    public void setMandatory(boolean mandatory)
    {
        this.mandatory = mandatory;
    }
}
