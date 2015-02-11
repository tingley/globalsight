package com.globalsight.ling.tm3.tools;

import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Tm;

@SuppressWarnings("static-access")
class AddAttributeCommand extends TM3Command {

    @Override
    public String getDescription() {
        return "add attributes to a TM";
    }

    @Override
    public String getName() {
        return "add-attr";
    }
    
    @Override
    protected String getUsageLine() {
        return getName() + " [options] -" + TM + " tmid attr-name1 ... attr-nameN";
    }
    
    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Adds one or more attributes to a given TM");
    }

    static final String TM = "tm";
    static final Option TM_OPT = OptionBuilder.withArgName("id")
                        .hasArg()
                        .withDescription("TM id")
                        .isRequired()
                        .create(TM);
    
    @Override
    public Options getOptions() {
        Options opts = getDefaultOptions();
        opts.addOption(TM_OPT);
        return opts;
    }
    
    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        TM3Tm<?> tm = getTm(session, command.getOptionValue(TM));
        if (tm == null) {
            die("Not a valid TM id: '" + command.getOptionValue(TM) + "'");
        }
        for (String attr : command.getArgs()) {
            tm.addAttribute(attr);
            System.out.println("Added '" + attr + "'");
        }
    }

}
