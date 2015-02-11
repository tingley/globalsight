package com.globalsight.ling.tm3.tools;

import java.io.PrintStream;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Tm;

class CreateSharedTmCommand extends CreateTmCommand {

    @Override
    protected TM3Tm<?> createTm(Session session, CommandLine command)
            throws Exception {   
        String s = command.getOptionValue(STORAGE);
        int i = Integer.valueOf(s);
        return getManager().createMultilingualSharedTm(
                session, null, Collections.EMPTY_SET, i); 
                   // (Integer)command.getParsedOptionValue(STORAGE));
    }

    static final String STORAGE = "storage";
    @SuppressWarnings("static-access")
    static final Option STORAGE_OPT = OptionBuilder.withArgName("id")
                        .hasArg()
                        .withDescription("using shared storage pool <id>")
                        .isRequired()
                        .withType(Integer.class)
                        .create(STORAGE);
    
    @Override
    public Options getOptions() {
        Options opts = getDefaultOptions();
        opts.addOption(STORAGE_OPT);
        return opts;
    }
    
    @Override
    public String getDescription() {
        return "create a multilingual TM with shared storage";
    }

    @Override
    public String getName() {
        return "create-shared";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " [options] -" + STORAGE + " storageId";
    }
    
    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Creates a new multilingual TM using a shared storage pool.");
    }

}
