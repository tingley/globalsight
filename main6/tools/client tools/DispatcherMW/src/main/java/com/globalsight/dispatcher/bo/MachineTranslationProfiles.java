/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.bo;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MachineTranslationProfiles")
public class MachineTranslationProfiles
{
    private AtomicInteger autoIncrement = new AtomicInteger(0);
    private Set<MachineTranslationProfile> mtProfiles = new CopyOnWriteArraySet<MachineTranslationProfile>();
    
    public MachineTranslationProfiles()
    {
        super();
    }

    public Set<MachineTranslationProfile> getMtProfiles()
    {
        return mtProfiles;
    }

    @XmlElement(name = "MachineTranslationProfile")
    public void setMtProfiles(Set<MachineTranslationProfile> mtProfiles)
    {
        this.mtProfiles = mtProfiles;
    }

    public int getAutoIncrement()
    {
        return autoIncrement.get();
    }

    @XmlAttribute(name="autoIncrement")
    public void setAutoIncrement(int newValue)
    {
        autoIncrement.set(newValue);
    }
    
    public int getAndIncrement()
    {
        return autoIncrement.getAndIncrement();
    }
}
