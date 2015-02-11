package com.globalsight.ling.tm3.tools;

import org.apache.commons.cli.CommandLine;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Tm;

abstract class CreateTmCommand extends TM3Command {

    protected abstract TM3Tm<?> createTm(Session session, CommandLine command) 
                        throws Exception;

    @Override
    public void handle(Session session, CommandLine command) throws Exception {
        TM3Tm<?> tm = createTm(session, command);
        System.out.println("Created TM " + tm.getId());
    }
}
