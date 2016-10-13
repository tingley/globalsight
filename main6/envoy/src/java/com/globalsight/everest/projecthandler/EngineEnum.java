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

import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.MachineTranslator;

public enum EngineEnum
{
    // the String array key is Relation the machinetranslator.java
    Asia_Online
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", "aoMtUrl", "aoMtPort", "aoMtUsername",
                    "aoMtPassword", "CATEGORY", "aoMtAccountNumber" };
        }

        String getClassName()
        {
            return "com.globalsight.machineTranslation.asiaOnline.AsiaOnlineProxy";

        }
    },
    DoMT
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", MTProfileConstants.MT_DOMT_URL,
                    "PORT", "USERNAME", "PASSWORD",
                    MTProfileConstants.MT_DOMT_ENGINE_NAME, "ACCOUNT_INFO" };
        }

        String getClassName()
        {
            return "com.globalsight.machineTranslation.domt.DoMTProxy";
        }
    },
    IPTranslator
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", MTProfileConstants.MT_IP_URL, "PORT", "username",
                    MTProfileConstants.MT_IP_KEY,
                    "CATEGORY", "ACCOUNT_NUMBER" };
        }

        String getClassName()
        {
            return "com.globalsight.machineTranslation.iptranslator.IPTranslatorProxy";

        }
    },
    MS_Translator
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", "msMtEndpoint", "Port", "msMtClientID",
                    "msMtClientSecret", "msMtCategory", "msMtAppID" };
        }

        String getClassName()
        {
            return "com.globalsight.machineTranslation.mstranslator.MSTranslatorProxy";

        }
    },
    ProMT
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", "ptsUrl", "PORT", "username", "password",
                    "CATEGORY", "ACCOUNT_NUMBER" };
        }

        String getClassName()
        {
            return "com.globalsight.machineTranslation.promt.ProMTProxy";

        }
    },
    Safaba
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", "safa_mt_host", "safa_mt_port",
                    "safa_mt_company_name", "safa_mt_password", "msMtCategory",
                    "safaba_client" };
        }

        String getClassName()
        {
            return "com.globalsight.machineTranslation.safaba.SafabaProxy";

        }
    },
    Google_Translate
    {
        String[] getInfo()
        {
            return new String[]
            { "mtProfileID", "url", "port", "clientName", "password",
                    "CATEGORY", MTProfileConstants.MT_GOOGLE_API_KEY };
        }
    	
    	String getClassName(){
    		return "com.globalsight.machineTranslation.google.GoogleProxy";
    	}
    };

    public static EngineEnum getEngine(String name)
    {
        return valueOf(name);
    }

    public MachineTranslator getProxy() throws Exception
    {
        MachineTranslator mt = (MachineTranslator) Class
                .forName(getClassName()).newInstance();
        return mt;
    }

    abstract String[] getInfo();

    abstract String getClassName();

}
