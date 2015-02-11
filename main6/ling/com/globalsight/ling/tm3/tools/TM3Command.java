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
package com.globalsight.ling.tm3.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3DataFactory;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.persistence.HibernateConfig;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

@SuppressWarnings("static-access")
public abstract class TM3Command
{

    private TM3Manager manager;
    private Properties properties = new Properties();
    private ClassLoader classLoader;
    private TM3DataFactory<?> factory;

    public abstract String getName();

    public abstract String getDescription();

    public Options getOptions()
    {
        return getDefaultOptions();
    }

    public void execute(CommandLine command, Properties defaultProperties)
            throws Exception
    {
        Transaction tx = HibernateUtil.getTransaction();

        properties.putAll(defaultProperties);

        // TODO: allow subclass a chance to examine command line before
        // wasting time starting hibernate -- for instance, let delete
        // bail if there is no argument

        // disable log4j spam
        Logger.getLogger("org.hibernate").setLevel(Level.INFO);

        if (command.hasOption(DEBUG))
        {
            SQLUtil.getLogger().setLevel(Level.DEBUG);
        }
        manager = DefaultManager.create();
        handle(command);
        HibernateUtil.commit(tx);
    }

    protected abstract void handle(CommandLine command) throws Exception;

    /**
     * Subclasses should override this to indicate that they need a non-null
     * TM3DataFactory to be passed to any TM3Tm instances.
     * 
     * @return
     */
    protected boolean requiresDataFactory()
    {
        return false;
    }

    protected TM3Manager getManager()
    {
        return manager;
    }

    @SuppressWarnings("unchecked")
    protected TM3Tm getTm(String arg)
    {
        TM3Tm tm = null;
        try
        {
            long id = Long.valueOf(arg);
            tm = getManager().getTm(getDataFactory(), id);
        }
        catch (NumberFormatException e)
        {
        }
        return tm;
    }

    static final String PROPERTIES = "properties";
    static final Option PROPERTIES_OPT = OptionBuilder
            .withArgName("propertiesFile").hasArg()
            .withDescription("database properties file").create(PROPERTIES);
    static final String DEBUG = "debug";
    static final Option DEBUG_OPT = OptionBuilder.withDescription(
            "enable sql debugging").create(DEBUG);
    static final String PROPERTY = "D";
    static final Option PROPERTY_OPT = OptionBuilder
            .withArgName("property=value").hasArgs(2).withValueSeparator()
            .withDescription("use value for given property").create(PROPERTY);

    protected Options getDefaultOptions()
    {
        Options opts = new Options();
        opts.addOption(PROPERTIES_OPT);
        opts.addOption(PROPERTY_OPT);
        opts.addOption(DEBUG_OPT);
        return opts;
    }

    protected Properties getProperties()
    {
        return properties;
    }

    public static final String TM3_USER_PROPERTY = "tm3.user";
    public static final String TM3_PASSWORD_PROPERTY = "tm3.password";
    public static final String TM3_CONNECTION_PROPERTY = "tm3.connection";
    public static final String TM3_DATAFACTORY_PROPERTY = "tm3.datafactory";
    public static final String TM3_CLASSPATH_PROPERTY = "tm3.classpath";

    // This handles the DB-related default options to initialize
    // hibernate
    protected SessionFactory getSessionFactory(CommandLine command)
    {
        String username = null, password = null, connStr = null;
        // Order of operations:
        // - start with defaults
        // - load the file specified with -properties (if set), or the
        // default properties file.
        // - Look for command line options to override.
        if (command.hasOption(PROPERTIES))
        {
            loadPropertiesFromFile(properties,
                    command.getOptionValue(PROPERTIES), true);
        }
        else
        {
            loadDefaultProperties(properties);
        }
        // Override any file properties with things passed on the command line
        Properties overrideProps = command.getOptionProperties(PROPERTY);
        for (Map.Entry<Object, Object> e : overrideProps.entrySet())
        {
            properties.setProperty((String) e.getKey(), (String) e.getValue());
        }

        username = properties.getProperty(TM3_USER_PROPERTY);
        password = properties.getProperty(TM3_PASSWORD_PROPERTY);
        connStr = properties.getProperty(TM3_CONNECTION_PROPERTY);
        if (username == null || password == null || connStr == null)
        {
            usage("Must specify " + TM3_USER_PROPERTY + ", "
                    + TM3_PASSWORD_PROPERTY + ", and "
                    + TM3_CONNECTION_PROPERTY
                    + " with -Dprop=val or via properties file");
        }

        Properties hibernateProps = new Properties(properties);
        hibernateProps.put("hibernate.dialect",
                "org.hibernate.dialect.MySQLInnoDBDialect");
        hibernateProps.put("hibernate.connection.driver_class",
                "com.mysql.jdbc.Driver");
        hibernateProps.put("hibernate.connection.url", connStr);
        hibernateProps.put("hibernate.connection.username", username);
        hibernateProps.put("hibernate.connection.password", password);
        hibernateProps.put("hibernate.cglib.use_reflection_optimizer", "false"); // this
                                                                                 // is
                                                                                 // default
                                                                                 // in
                                                                                 // hibernate
                                                                                 // 3.2
        hibernateProps.put("hibernate.show_sql", "false");
        hibernateProps.put("hibernate.format_sql", "false");
        hibernateProps.put("hibernate.connection.pool_size", "1");
        hibernateProps.put("hibernate.cache.provider_class",
                "org.hibernate.cache.HashtableCacheProvider");
        // A little sketchy. Due to some undocumented (?) Hibernate
        // behavior, it will pick up hibernate cfg properties from elsewhere
        // in the classpath, even though we've specified them by hand.
        // As a result, this may end up unintentionally running c3p0
        // connection pooling, which will release connections on commit.
        // This causes problems for multi-transaction commands (like
        // GlobalSight's migrate-tm), because it will kill the connection
        // in mid-operation. As a result, we force the release mode to
        // keep the connection open until we're done.
        hibernateProps.put("hibernate.connection.release_mode", "on_close");
        Configuration cfg = new Configuration().addProperties(hibernateProps);
        cfg = HibernateConfig.extendConfiguration(cfg);
        if (requiresDataFactory())
        {
            cfg = getDataFactory().extendConfiguration(cfg);
        }

        return cfg.buildSessionFactory();
    }

    private void loadPropertiesFromFile(Properties props, String filename,
            boolean warn)
    {
        File f = new File(filename);
        if (!f.exists())
        {
            if (warn)
            {
                System.err.println("File does not exist: " + filename);
            }
            return;
        }
        try
        {
            props.load(new FileInputStream(f));
        }
        catch (IOException e)
        {
            System.err.println("Could not load " + filename + ": "
                    + e.getMessage());
        }
    }

    // Load properties from ~/.tm3.properties
    private void loadDefaultProperties(Properties props)
    {
        // To find the home dir, first look in the env $HOME, then fall back
        // to the system user.home
        String homeDir = System.getenv("HOME");
        if (homeDir == null)
        {
            homeDir = System.getProperty("user.home");
            if (homeDir == null)
            {
                return;
            }
        }
        String filename = homeDir + File.separator + ".tm3.properties";
        loadPropertiesFromFile(props, filename, false);
    }

    private ClassLoader getClassLoader(Properties props)
    {
        String classpath = props.getProperty(TM3_CLASSPATH_PROPERTY);
        if (classpath == null)
        {
            return getClass().getClassLoader();
        }
        List<URL> urls = new ArrayList<URL>();
        String cpSep = System.getProperty("path.separator");
        try
        {
            String[] cps = classpath.split(cpSep);
            for (String cp : cps)
            {
                File f = new File(cp);
                if (!f.exists())
                {
                    System.err
                            .println("Skipping non-existent classpath element "
                                    + f);
                    continue;
                }
                if (cp.toLowerCase().endsWith(".jar"))
                {
                    StringBuilder sb = new StringBuilder("jar:").append(
                            f.toURI().toString()).append("!/");
                    urls.add(new URL(sb.toString()));
                }
                else if (f.isDirectory())
                {
                    urls.add(f.toURI().toURL());
                }
                else
                {
                    System.err.println("Not a directory or JAR file: " + f);
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("Could not load from " + classpath + ": "
                    + e.getMessage());
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]));
    }

    @SuppressWarnings("unchecked")
    protected TM3DataFactory getDataFactory()
    {
        if (!requiresDataFactory())
        {
            return null;
        }
        if (factory != null)
        {
            return factory;
        }
        if (classLoader == null)
        {
            classLoader = getClassLoader(properties);
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        String factoryClass = properties.getProperty(TM3_DATAFACTORY_PROPERTY);
        if (factoryClass == null)
        {
            die(TM3_DATAFACTORY_PROPERTY + " was not set");
        }
        try
        {
            Class clazz = classLoader.loadClass(factoryClass);
            Object o = clazz.newInstance();
            if (!(o instanceof TM3DataFactory))
            {
                die(factoryClass
                        + " is not a valid TM3DataFactory implementation");
            }
            factory = (TM3DataFactory) o;
        }
        catch (Exception e)
        {
            die("Error loading data factory " + factoryClass + ": "
                    + e.getMessage());
        }
        return factory;
    }

    // Print usage for this command to stderr and die
    protected void usage()
    {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("TM3Tool " + getUsageLine(), getOptions());
        printExtraHelp(System.err);
        System.exit(1);
    }

    protected String getUsageLine()
    {
        return getName() + " [options]";
    }

    protected void printExtraHelp(PrintStream out)
    {

    }

    // Prints usage info as well as an additional message, then die
    protected void usage(String message)
    {
        System.err.println(message);
        usage();
    }

    protected void die(String message)
    {
        System.err.println(message);
        System.exit(1);
    }
}
