package com.globalsight.ling.tm3.integration;

import java.util.Map;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

import com.globalsight.ling.tm3.tools.TM3Command;
import com.globalsight.ling.tm3.tools.TM3Tool;

/**
 * GSTmTool extends TM3Tool with custom commands to
 * convert legacy TMs.
 */
public class GSTmTool extends TM3Tool {

    public static void main(String[] args) {
        new GSTmTool().run(args);
        System.exit(0);
    }
    
    /**
     * Add custom commands for GlobalSight.
     */
    @Override
    protected void registerCustomCommands(
            Map<String, Class<? extends TM3Command>> commands) {    
        commands.put("migrate-tm", MigrateTmCommand.class);
        commands.put("enable-tm3", EnableTm3Command.class);
        commands.put("tokenize", TokenizeCommand.class);
    }

    static final String ENVOY_PROPERTIES = 
                "properties/envoy_generated.properties";
    
    /**
     * Hardcode the data factory class; pull connection params from
     * nearby places.
     */
    @Override
    protected Properties getDefaultProperties() {
        Properties props = new Properties();
        props.setProperty(TM3Command.TM3_DATAFACTORY_PROPERTY, 
                          GSDataFactory.class.getName());
        try {
            InputStream is = getClass().getClassLoader()
                .getResourceAsStream(ENVOY_PROPERTIES);
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                props.setProperty(TM3Command.TM3_CONNECTION_PROPERTY, 
                                p.getProperty("db.connection"));
                props.setProperty(TM3Command.TM3_USER_PROPERTY, 
                        p.getProperty("db.username"));
                props.setProperty(TM3Command.TM3_PASSWORD_PROPERTY, 
                        p.getProperty("db.password"));
            }
            else {
                System.out.println("Couldn't open " + ENVOY_PROPERTIES);
            }
        }
        catch (IOException e) {
            System.out.println("Error opening " + ENVOY_PROPERTIES + ":" +
                               e.getMessage());
        }

        return props;
    }
}
