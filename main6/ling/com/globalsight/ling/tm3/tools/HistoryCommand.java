package com.globalsight.ling.tm3.tools;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.hibernate.Session;

import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Handle;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3EventLog;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;

@SuppressWarnings("static-access")
public class HistoryCommand extends TM3Command {

    @Override
    public String getDescription() {
        return "show history for a TM";
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    protected String getUsageLine() {
        return getName() + " [options] -" + TM + " tmid";
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
    protected boolean requiresDataFactory() {
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        TM3Tm tm = getTm(session, command.getOptionValue(TM));
        if (tm == null) {
            die("Not a valid TM id: '" + command.getOptionValue(TM) + "'");
        }
        
        Formatter f = new Formatter(System.out);
        
        // If there are no segments listed, just show segment history.
        if (command.getArgs().length == 0) {
            printEvents(tm.getEventLog(), "TM " + tm.getId(), f);
        }
    }

    protected void printEvents(TM3EventLog events, String label,
                               Formatter f) {
        f.format("== History for %s ==\n", label);
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        f.format("%-22s%-12s%-6s%s\n", "Date", "User", "Type", "Argument");
        for (TM3Event e : events.getEvents()) {
            String user = e.getUsername();
            if (user.length() > 10) user = user.substring(0, 10);
            f.format("%-22s%-12s%-6d%s\n", df.format(e.getTimestamp()), 
                     user, e.getType(), e.getArgument());
        }
    }
}
