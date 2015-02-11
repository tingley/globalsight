package com.globalsight.ling.tm3.integration;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

import com.globalsight.ling.tm3.core.TM3DataFactory;
import com.globalsight.ling.tm3.core.TM3FuzzyMatchScorer;
import com.globalsight.ling.tm3.core.TM3Locale;
import com.globalsight.util.GlobalSightLocale;

public class GSDataFactory implements TM3DataFactory<GSTuvData> {

    @Override
    public GSTuvData fromSerializedForm(TM3Locale locale, String value) {
        return new GSTuvData(value, (GlobalSightLocale)locale);
    }

    @Override
    public TM3FuzzyMatchScorer<GSTuvData> getFuzzyMatchScorer() {
        return new GSFuzzyScorer();
    }

    @Override
    public TM3Locale getLocaleByCode(Session session, String code) {
        return localeFromCode(session, code);
    }

    public static GlobalSightLocale localeFromCode(Session session, String code) {
        String[] parts = code.split("_");
        if (parts.length != 2) {
            return null;
        }
        return (GlobalSightLocale)session
            .createCriteria(GlobalSightLocale.class)
            .add(Restrictions.eq("language", parts[0]))
            .add(Restrictions.eq("country", parts[1]))
            .uniqueResult();
    }

    @Override
    public TM3Locale getLocaleById(Session session, long id) {
        return 
            (TM3Locale)session.get(GlobalSightLocale.class, id);
    }

    @Override
    public Configuration extendConfiguration(Configuration cfg) {
        // Initialize the GlobalSightLocale mapping so this works with
        // tm3tool
        return cfg.addResource("com/globalsight/persistence/hibernate/xml/GlobalSightLocale.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/ProjectTM.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/TranslationMemoryProfile.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/LeverageProjectTM.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/ProMTInfo.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/AsiaOnlineLP2DomainCombination.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/TDATM.hbm.xml",
                               getClass().getClassLoader())
                  .addResource("com/globalsight/persistence/hibernate/xml/Company.hbm.xml",
                               getClass().getClassLoader());
    }

}
