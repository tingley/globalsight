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
package com.globalsight.machineTranslation.mstranslator;

import java.util.Map;

import org.apache.log4j.Logger;
import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateOptions;
import org.tempuri.SoapService;
import org.tempuri.SoapServiceLocator;

import com.globalsight.machineTranslation.MachineTranslator;
import com.microsofttranslator.api.V2.LanguageService;

public class MSTranslateConfig
{
    private static final Logger CATEGORY = Logger.getLogger(MSTranslateConfig.class);
    
    private String sourceLang;
    private String targetLang;
    private String endpoint;
    private String msCategory;
    private String msClientId;
    private String msClientSecret;
    private String msSubscriptionKey;
    private LanguageService service;
    private String accessToken;
    
    private TranslateOptions options = null;
    
    @SuppressWarnings("rawtypes")
    public void init(Map paramMap)
    {
        endpoint = (String) paramMap.get(MachineTranslator.MSMT_ENDPOINT);
        msCategory = (String) paramMap.get(MachineTranslator.MSMT_CATEGORY);
        msClientId = (String) paramMap.get(MachineTranslator.MSMT_CLIENTID);
        msClientSecret = (String) paramMap.get(MachineTranslator.MSMT_CLIENT_SECRET);
        msSubscriptionKey = (String) paramMap.get(MachineTranslator.MSMT_SUBSCRIPTION_KEY);
        accessToken = MSMTUtil.getMsAccessToken(this);
    }
    
    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getMsCategory()
    {
        return msCategory;
    }

    public void setMsCategory(String msCategory)
    {
        this.msCategory = msCategory;
    }

    public String getMsClientId()
    {
        return msClientId;
    }

    public void setMsClientId(String msClientId)
    {
        this.msClientId = msClientId;
    }

    public String getMsClientSecret()
    {
        return msClientSecret;
    }

    public void setMsClientSecret(String msClientSecret)
    {
        this.msClientSecret = msClientSecret;
    }

    public String getMsSubscriptionKey()
    {
        return msSubscriptionKey;
    }

    public void setMsSubscriptionKey(String msSubscriptionKey)
    {
        this.msSubscriptionKey = msSubscriptionKey;
    }

    public String getSourceLang()
    {
        return sourceLang;
    }

    public void setSourceLang(String sourceLang)
    {
        this.sourceLang = sourceLang;
    }

    public String getTargetLang()
    {
        return targetLang;
    }

    public void setTargetLang(String targetLang)
    {
        this.targetLang = targetLang;
    }

    public TranslateOptions getOptions()
    {
        if (options == null)
        {
            options = new TranslateOptions();
            options.setCategory(msCategory);
            options.setContentType(MachineTranslator.MSMT_CONTENT_TYPE);
        }
        
        return options;
    }

    public LanguageService getService()
    {
        if (service == null)
        {
            try
            {
                SoapService soap = new SoapServiceLocator(getEndpoint());
                service = soap.getBasicHttpBinding_LanguageService();
            }
            catch (Exception ex)
            {
                CATEGORY.error(ex);
            }
        }
        
        return service;
    }

    /**
     * Updates the access token to a new one and set the service to null(Force it to create a new one)
     */
    public void updateAccessToken()
    {
        accessToken = MSMTUtil.getMsAccessToken(this);
        service = null;
    }
    
    public String getAccessToken()
    {
        return accessToken;
    }
}
