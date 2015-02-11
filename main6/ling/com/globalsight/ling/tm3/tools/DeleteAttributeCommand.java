package com.globalsight.ling.tm3.tools;

import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Tm;

@SuppressWarnings("static-access")
class DeleteAttributeCommand extends TM3Command {

    @Override
    public String getDescription() {
        return "remove attributes from a TM";
    }

    @Override
    public String getName() {
        return "delete-attr";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " [options] -" + TM + " tmid attr-name1 ... attr-nameN";
    }
    
    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Deletes one or more attributes from a given TM");
    }
    
    static final String TM = "tm";
    static final Option TM_OPT = OptionBuilder.withArgName("id")
                        .hasArg()
                        .withDescription("TM id")
                        .isRequired()
                        .create(TM);
    
    @Override
    public Options getOptions() {
        return getDefaultOptions().addOption(TM_OPT);
    }
    
    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        TM3Tm<?> tm = getTm(session, command.getOptionValue(TM));
        if (tm == null) {
            die("Not a valid TM id: '" + command.getOptionValue(TM) + "'");
        }
        for (String name : command.getArgs()) {
            TM3Attribute attr = tm.getAttributeByName(name);
            if (attr == null) {
                System.out.println("Not an attribute: '" + name + "'");
                continue;
            }
            tm.removeAttribute(attr);
            System.out.println("Removed '" + attr + "'");
        }
    }

}
