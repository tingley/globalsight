package com.globalsight.ling.tm3.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Tm;

// XXX This probably needs a safeguard
// Also it needs a way to specify 
@SuppressWarnings("static-access")
class DeleteTmCommand extends TM3Command {

    @Override
    public String getDescription() {
        return "deletes a tm";
    }

    @Override
    public String getName() {
        return "delete";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " [options] tmId";
    }
    
    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Deletes a TM, including all TM data.");
        out.println("Use with caution -- this is irreversible!");
    }

    static final String FORCE = "f";
    static final Option FORCE_OPT = OptionBuilder
        .withDescription("force delete (do not prompt for confirmation)")
        .create(FORCE);
    
    @Override
    public Options getOptions() {
        return getDefaultOptions().addOption(FORCE_OPT);
    }
    
    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        
        boolean doDelete = command.hasOption(FORCE);
        
        String arg = command.getArgs()[0];
        TM3Tm<?> tm = getTm(session, arg);
        if (tm == null) {
            System.out.println("Ignoring '" + arg + "' - not a valid id");
        }
        if (!doDelete) {
            System.out.println("WARNING: This will irreversibly delete TM " 
                               + tm.getId());
            System.out.print("Are you sure you wish to continue? [y/n] ");
            BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
            String s = r.readLine().toLowerCase();
            if (s.equals("y") || s.equals("yes")) {
                doDelete = true;
            }
        }
        if (doDelete) {
            getManager().removeTm(session, tm);
            System.out.println("Removed TM " + arg);
        }
    }

}
