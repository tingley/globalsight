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
package com.globalsight.machineTranslation.asiaOnline;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * This represents a domain combination object from Asia Online.
 * 
 * @author York
 * @since 2010-10-20
 */
public class DomainCombination extends PersistentObject
{
    private static final long serialVersionUID = -8664792920755627238L;

    // Take all the attributes of domain combination object as "String".
    private String code = null;
    private String languagePairCode = null;
    private String sourceLanguageCode = null;
    private String sourceLanguage = null;
    private String sourceAbbreviation = null;
    private String targetLanguageCode = null;
    private String targetLanguage = null;
    private String targetAbbreviation = null;
    private String languagePair = null;
    private String domainCount = null;
    private String domainCombinationName = null;
    private String domainCombinationDesc = null;
    private String domainCombination = null;
    private String domainComName = null;
    private String privateAtt = null;
    private String disabled = null;
    private String deleted = null;
    private String capitalize = null;
    private String defaultAtt = null;
    private String trueCase = null;

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLanguagePairCode()
    {
        return languagePairCode;
    }

    public void setLanguagePairCode(String languagePairCode)
    {
        this.languagePairCode = languagePairCode;
    }

    public String getSourceLanguageCode()
    {
        return sourceLanguageCode;
    }

    public void setSourceLanguageCode(String sourceLanguageCode)
    {
        this.sourceLanguageCode = sourceLanguageCode;
    }

    public String getSourceLanguage()
    {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage)
    {
        this.sourceLanguage = sourceLanguage;
    }

    public String getSourceAbbreviation()
    {
        return sourceAbbreviation;
    }

    public void setSourceAbbreviation(String sourceAbbreviation)
    {
        this.sourceAbbreviation = sourceAbbreviation;
    }

    public String getTargetLanguageCode()
    {
        return targetLanguageCode;
    }

    public void setTargetLanguageCode(String targetLanguageCode)
    {
        this.targetLanguageCode = targetLanguageCode;
    }

    public String getTargetLanguage()
    {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage)
    {
        this.targetLanguage = targetLanguage;
    }

    public String getTargetAbbreviation()
    {
        return targetAbbreviation;
    }

    public void setTargetAbbreviation(String targetAbbreviation)
    {
        this.targetAbbreviation = targetAbbreviation;
    }

    public String getLanguagePair()
    {
        return languagePair;
    }

    public void setLanguagePair(String languagePair)
    {
        this.languagePair = languagePair;
    }

    public String getDomainCount()
    {
        return domainCount;
    }

    public void setDomainCount(String domainCount)
    {
        this.domainCount = domainCount;
    }

    public String getDomainCombinationName()
    {
        return domainCombinationName;
    }

    public void setDomainCombinationName(String domainCombinationName)
    {
        this.domainCombinationName = domainCombinationName;
    }

    public String getDomainCombinationDesc()
    {
        return domainCombinationDesc;
    }

    public void setDomainCombinationDesc(String domainCombinationDesc)
    {
        this.domainCombinationDesc = domainCombinationDesc;
    }

    public String getDomainCombination()
    {
        return domainCombination;
    }

    public void setDomainCombination(String domainCombination)
    {
        this.domainCombination = domainCombination;
    }

    public String getDomainComName()
    {
        return domainComName;
    }

    public void setDomainComName(String domainComName)
    {
        this.domainComName = domainComName;
    }

    public String getPrivate()
    {
        return privateAtt;
    }

    public void setPrivate(String privateAtt)
    {
        this.privateAtt = privateAtt;
    }

    public String getDisabled()
    {
        return disabled;
    }

    public void setDisabled(String disabled)
    {
        this.disabled = disabled;
    }

    public String getDeleted()
    {
        return deleted;
    }

    public void setDeleted(String deleted)
    {
        this.deleted = deleted;
    }

    public String getCapitalize()
    {
        return capitalize;
    }

    public void setCapitalize(String capitalize)
    {
        this.capitalize = capitalize;
    }

    public String getDefault()
    {
        return defaultAtt;
    }

    public void setDefault(String defaultAtt)
    {
        this.defaultAtt = defaultAtt;
    }

    public String getTrueCase()
    {
        return trueCase;
    }

    public void setTrueCase(String trueCase)
    {
        this.trueCase = trueCase;
    }

}
