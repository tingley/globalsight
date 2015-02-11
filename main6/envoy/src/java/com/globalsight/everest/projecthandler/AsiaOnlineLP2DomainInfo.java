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
package com.globalsight.everest.projecthandler;

import com.globalsight.everest.persistence.PersistentObject;

public class AsiaOnlineLP2DomainInfo extends PersistentObject
{
    private static final long serialVersionUID = -8374213774228137327L;

    private TranslationMemoryProfile tmProfile = null;
    private long languagePairCode = -1;
    private String languagePairName = null;
    private long domainCombinationCode = -1;

    public TranslationMemoryProfile getTmProfile()
    {
        return tmProfile;
    }

    public void setTmProfile(TranslationMemoryProfile tmProfile)
    {
        this.tmProfile = tmProfile;
    }

    public long getLanguagePairCode()
    {
        return languagePairCode;
    }

    public void setLanguagePairCode(long languagePairCode)
    {
        this.languagePairCode = languagePairCode;
    }
    
    public String getLanguagePairName()
    {
        return languagePairName;
    }
    
    public void setLanguagePairName(String languagePairName)
    {
        this.languagePairName = languagePairName;
    }

    public long getDomainCombinationCode()
    {
        return domainCombinationCode;
    }

    public void setDomainCombinationCode(long domainCombinationCode)
    {
        this.domainCombinationCode = domainCombinationCode;
    }

}
