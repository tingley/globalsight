package com.globalsight.ling.tm3.core.persistence;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateConfig {
    
    public static SessionFactory createSessionFactory(Properties properties) {
        Configuration cfg = extendConfiguration(new Configuration())
            .addProperties(properties);
        return cfg.buildSessionFactory();
    }

    public static Configuration extendConfiguration(Configuration config) {
        return config
            .addResource("com/globalsight/ling/tm3/core/persistence/xml/BaseTm.hbm.xml")
            .addResource("com/globalsight/ling/tm3/core/persistence/xml/TM3Attribute.hbm.xml")
            .addResource("com/globalsight/ling/tm3/core/persistence/xml/TM3Event.hbm.xml");
    }
}
