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
package com.globalsight.ling.tm3.integration;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import com.globalsight.ling.tm3.core.TM3DataFactory;
import com.globalsight.ling.tm3.core.TM3FuzzyMatchScorer;
import com.globalsight.ling.tm3.core.TM3Locale;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class GSDataFactory implements TM3DataFactory<GSTuvData>
{

    @Override
    public GSTuvData fromSerializedForm(TM3Locale locale, String value)
    {
        return new GSTuvData(value, (GlobalSightLocale) locale);
    }

    @Override
    public TM3FuzzyMatchScorer<GSTuvData> getFuzzyMatchScorer()
    {
        return new GSFuzzyScorer();
    }

    @Override
    public TM3Locale getLocaleByCode(String code)
    {
        return localeFromCode(code);
    }

    public static GlobalSightLocale localeFromCode(String code)
    {
        Session session = HibernateUtil.getSession();
        String[] parts = code.split("_");
        if (parts.length != 2)
        {
            return null;
        }
        return (GlobalSightLocale) session
                .createCriteria(GlobalSightLocale.class)
                .add(Restrictions.eq("language", parts[0]))
                .add(Restrictions.eq("country", parts[1])).uniqueResult();
    }

    @Override
    public TM3Locale getLocaleById(long id)
    {
        return (TM3Locale) HibernateUtil
                .get(GlobalSightLocale.class, id, false);
    }

    @Override
    public Configuration extendConfiguration(Configuration cfg)
    {
        // Initialize the GlobalSightLocale mapping so this works with
        // tm3tool
        return cfg
                .addResource(
                        "com/globalsight/persistence/hibernate/xml/GlobalSightLocale.hbm.xml",
                        getClass().getClassLoader())
                .addResource(
                        "com/globalsight/persistence/hibernate/xml/ProjectTM.hbm.xml",
                        getClass().getClassLoader())
                .addResource(
                        "com/globalsight/persistence/hibernate/xml/TranslationMemoryProfile.hbm.xml",
                        getClass().getClassLoader())
                .addResource(
                        "com/globalsight/persistence/hibernate/xml/LeverageProjectTM.hbm.xml",
                        getClass().getClassLoader())
                .addResource(
                        "com/globalsight/persistence/hibernate/xml/TDATM.hbm.xml",
                        getClass().getClassLoader())
                .addResource(
                        "com/globalsight/persistence/hibernate/xml/Company.hbm.xml",
                        getClass().getClassLoader());
    }

}
