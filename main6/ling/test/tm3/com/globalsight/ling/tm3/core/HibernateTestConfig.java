package com.globalsight.ling.tm3.core;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.globalsight.ling.tm3.core.persistence.HibernateConfig;

public class HibernateTestConfig {

    public static SessionFactory createSessionFactory(int poolsize) {
        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
        props.put("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        props.put("hibernate.connection.url", 
                  "jdbc:mysql://localhost:3306/tm3test?useUnicode=true&characterEncoding=UTF-8");
        props.put("hibernate.connection.username", "tm3test");
        props.put("hibernate.connection.password", "password");
        props.put("hibernate.cglib.use_reflection_optimizer", "false"); // this is default in hibernate 3.2
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.connection.pool_size", "" + poolsize);
        Configuration cfg = new Configuration()
            .addResource("com/globalsight/ling/tm3/core/TestLocale.hbm.xml")
            .addProperties(props);
        return HibernateConfig.extendConfiguration(cfg).buildSessionFactory();
    }

}
