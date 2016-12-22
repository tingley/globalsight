/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.vo;

/**
 * <code>PerplexityLanguageModulePair</code> is used in <code>PerplexityScoreHelper</code>
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityLanguageModulePair
{
    private long id;
    private String sourceLanguage;
    private String targetLanguage;
    private String domain;
    private long accountId;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getSourceLanguage()
    {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage)
    {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage()
    {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage)
    {
        this.targetLanguage = targetLanguage;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(long accountId)
    {
        this.accountId = accountId;
    }
}
