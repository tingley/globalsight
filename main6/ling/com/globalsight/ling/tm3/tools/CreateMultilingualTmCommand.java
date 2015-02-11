package com.globalsight.ling.tm3.tools;

import java.io.PrintStream;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Tm;

public class CreateMultilingualTmCommand extends CreateTmCommand {

    @Override
    protected TM3Tm<?> createTm(Session session, CommandLine command)
            throws Exception {
        return getManager().createMultilingualTm(
                session, null, Collections.EMPTY_SET);
    }

    @Override
    public String getDescription() {
        return "create a multilingual TM with dedicated storage";
    }

    @Override
    public String getName() {
        return "create-multilingual";
    }
    
    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Creates a new multilingual TM with dedicated storage.");
    }

}
