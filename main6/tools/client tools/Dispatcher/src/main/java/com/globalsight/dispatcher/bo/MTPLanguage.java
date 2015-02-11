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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import com.globalsight.dispatcher.dao.MTProfilesDAO;

/**
 * The Business Object for MT Profile and Source/Target Language.
 */
public class MTPLanguage
{
    private long id;                                // MTPLanguage ID
    private String name;                            // MTPLanguage Name
    private GlobalSightLocale srcLocale;            // Source Locale
    private GlobalSightLocale trgLocale;            // Target Locale
    private MachineTranslationProfile mtProfile;    // MT Profile, only store id in xml

    public MTPLanguage()
    {
        id = -1;
    }
    
    public MTPLanguage(String p_name, GlobalSightLocale p_srcLocale, GlobalSightLocale p_trgLocale, MachineTranslationProfile p_mtProfile)
    {
        id = -1;
        name = p_name; 
        srcLocale = p_srcLocale;
        trgLocale = p_trgLocale;
        mtProfile = p_mtProfile;
    }

    public long getId()
    {
        return id;
    }

    @XmlAttribute(name="ID")
    public void setId(long id)
    {
        this.id = id;
    }
    
    public GlobalSightLocale getSrcLocale()
    {
        return srcLocale;
    }

    public void setSrcLocale(GlobalSightLocale srcLocale)
    {
        this.srcLocale = srcLocale;
    }

    public GlobalSightLocale getTrgLocale()
    {
        return trgLocale;
    }

    public void setTrgLocale(GlobalSightLocale trgLocale)
    {
        this.trgLocale = trgLocale;
    }

    public long getMtProfileId()
    {
        return mtProfile.getId();
    }

    public void setMtProfileId(long p_mtProfileId)
    {
        mtProfile = new MTProfilesDAO().getMTProfile(p_mtProfileId);
    }

    public MachineTranslationProfile getMtProfile()
    {
        return mtProfile;
    }

    @XmlTransient
    public void setMtProfile(MachineTranslationProfile mtProfile)
    {
        this.mtProfile = mtProfile;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
