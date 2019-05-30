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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.machineTranslation.mstranslator.v2.MSTranslatorProxyV2;
import com.globalsight.machineTranslation.mstranslator.v3.MSTranslatorProxyV3;

/**
 * Acts as a proxy to the translation Machine Translation Service: MS
 * Translator.
 */
public class MSTranslatorProxy extends AbstractTranslator implements MachineTranslator
{
    private static final Logger CATEGORY = Logger.getLogger(MSTranslatorProxy.class);

    private int version = 0;
    private Class proxyClass = null;
    private Object proxy = null;

    public MSTranslatorProxy() throws MachineTranslationException
    {
    }

    public String getEngineName()
    {
        return ENGINE_MSTRANSLATOR;
    }

    private int getVersion()
    {
        if (version == 0)
        {
            HashMap paramMap = getMtParameterMap();
            MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap.get(MachineTranslator.MT_PROFILE);

            if (!mtProfile.isV3())
                version = 2;
            else
                version = 3;
        }

        return version;
    }

    private Class getProxyClass()
    {
        if (proxyClass == null)
        {
            if (getVersion() == 2)
            {
                proxyClass = MSTranslatorProxyV2.class;
                proxy = new MSTranslatorProxyV2();
                setProxyMtParameterMap();

            }
            else
            {
                proxyClass = MSTranslatorProxyV3.class;
                proxy = new MSTranslatorProxyV3();
                setProxyMtParameterMap();
            }
        }

        return proxyClass;
    }

    private void setProxyMtParameterMap()
    {
        try
        {
            Method m = getProxyClass().getMethod("setMtParameterMap", HashMap.class);
            m.setAccessible(true);
            m.invoke(getProxy(), getMtParameterMap());
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }
    }

    private Object getProxy()
    {
        return proxy;
    }

    /**
     * Returns true if the given locale pair is supported for MT.
     */
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale)
            throws MachineTranslationException
    {
        try
        {
            Method m = getProxyClass().getDeclaredMethod("supportsLocalePair", Locale.class,
                    Locale.class);
            m.setAccessible(true);
            return (boolean) m.invoke(getProxy(), p_sourceLocale, p_targetLocale);
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }

        return false;
    }

    public String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
            throws MachineTranslationException
    {
        try
        {
            Method m = getProxyClass().getDeclaredMethod("doTranslation", Locale.class,
                    Locale.class, String.class);
            m.setAccessible(true);
            return (String) m.invoke(getProxy(), p_sourceLocale, p_targetLocale, p_string);
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }

        return p_string;
    }

    protected String[] doBatchTranslation(Locale p_sourceLocale, Locale p_targetLocale,
            String[] segments) throws MachineTranslationException
    {
        try
        {
            Method m = getProxyClass().getDeclaredMethod("doBatchTranslation", Locale.class,
                    Locale.class, String[].class);
            m.setAccessible(true);
            return (String[]) m.invoke(getProxy(), p_sourceLocale, p_targetLocale, segments);
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }

        return segments;
    }
}
