package com.globalsight.ling.tm3.tools;

import java.util.Arrays;
import java.util.Formatter;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.MissingOptionException;

public class TM3Tool {

    private SortedMap<String, Class<? extends TM3Command>> commands =
        new TreeMap<String, Class<? extends TM3Command>>();
       
    public static void main(String[] args) {
        new TM3Tool().run(args);
    }
    
    protected void run(String[] args) {
        registerDefaultCommands(commands);
        registerCustomCommands(commands);
        if (args.length == 0) {
            help();
        }
        String cmd = args[0].toLowerCase();
        if (cmd.equals("help") && args.length == 2) {
            help(args[1]);
        }
        TM3Command command = getCommand(commands.get(cmd));
        if (command == null) {
            help();
        }
        if (args.length > 1) {
            args = Arrays.asList(args).subList(1, args.length)
                                .toArray(new String[args.length - 1]);
        }
        else {
            args = new String[0];
        }
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cl = parser.parse(command.getOptions(), args);
            command.execute(cl, getDefaultProperties());
        }
        catch (MissingOptionException e) {
            System.err.println(e.getMessage());
            command.usage();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void registerDefaultCommands(
                Map<String, Class<? extends TM3Command>> commands) {
        commands.put("show", ShowTmCommand.class);
        commands.put("delete", DeleteTmCommand.class);
        commands.put("delete-attr", DeleteAttributeCommand.class);
        commands.put("add-attr", AddAttributeCommand.class);
        commands.put("create-shared", CreateSharedTmCommand.class);
        commands.put("history", HistoryCommand.class);
    }
    
    /**
     * Subclasses can override this to pre-populate any of the properties
     * needed for session initialization.
     */
    protected Properties getDefaultProperties() {
        return new Properties();
    }
    
    /**
     * Subclasses can override this to add their own commands to the command map,
     * or manipulate the map in some other way.
     * @param commands
     */
    protected void registerCustomCommands(
            Map<String, Class<? extends TM3Command>> commands) {    
    }

    private static TM3Command getCommand(Class<? extends TM3Command> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void help() {
        Formatter f = new Formatter(System.err);
        f.format("Available commands:\n");
        for (Map.Entry<String, Class<? extends TM3Command>> e : 
                                            commands.entrySet()) {
            TM3Command c = getCommand(e.getValue());
            f.format("%-20s%s\n", e.getKey(), c.getDescription());
        }
        f.flush();
        f.close();
    }
    
    private void help(String cmd) {
        TM3Command command = getCommand(commands.get(cmd));
        if (command == null) {
            help();
        }
        command.usage();
    }
}
