package com.globalsight.ling.tm3.integration;

import org.apache.commons.cli.CommandLine;
import java.util.*;
import java.io.*;
import org.hibernate.Session;

import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm3.tools.TM3Command;
import com.globalsight.util.GlobalSightLocale;

public class TokenizeCommand extends TM3Command {

    @Override
    public String getDescription() {
        return "tokenizes";
    }

    @Override
    public String getName() {
        return "tokenize";
    }

    @Override
    public boolean requiresDataFactory() {
        return true;
    }

    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        GlobalSightLocale locale = (GlobalSightLocale)getDataFactory().getLocaleById(session, 32);
        if (locale == null) {
            System.out.println("No such locale");
            System.exit(1);
        }
        List<String> args = command.getArgList();
        if (args.size() == 0) {
            System.out.println("no filename");
            System.exit(1);
        }

        File f = new File(args.get(0));
        if (!f.exists()) {
            System.out.println("File '" + f + "' does not exist");
            System.exit(1);
        }
        
        BufferedReader r = new BufferedReader(
            new InputStreamReader(new FileInputStream(f), "UTF-8"));
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            System.out.println("Args: " + line);
            GSTuvData data = new GSTuvData(line, locale);
            for (String s : data.getTokens()) {
                System.out.print("[" + s + "]");
                if (s.length() == 1) {
                    System.out.print(" (" + 
                        (int)s.charAt(0) + ")");
                }
                System.out.println("");
            }
        }

    }

}
