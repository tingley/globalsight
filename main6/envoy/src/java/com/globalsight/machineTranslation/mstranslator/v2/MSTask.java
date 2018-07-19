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
package com.globalsight.machineTranslation.mstranslator.v2;

import java.util.concurrent.Callable;

import org.datacontract.schemas._2004._07.Microsoft_MT_Web_Service_V2.TranslateArrayResponse;

public class MSTask implements Callable<TranslateArrayResponse[]>
{
    private MSTranslateConfig config;
    private String[] segments;

    public MSTask(MSTranslateConfig config, String[] segments)
    {
        this.config = config;
        this.segments = segments;
    }

    @Override
    public TranslateArrayResponse[] call() throws Exception
    {
        return config.getService().translateArray(config.getAccessToken(), segments,
                config.getSourceLang(), config.getTargetLang(), config.getOptions());
    }

    public MSTranslateConfig getConfig()
    {
        return config;
    }

    public void setConfig(MSTranslateConfig config)
    {
        this.config = config;
    }
}
