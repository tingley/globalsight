package com.globalsight.ling.tm3.integration;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.hibernate.Session;

import java.io.PrintStream;
import java.sql.Connection;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;
import com.globalsight.ling.tm3.tools.TM3Command;
import com.globalsight.util.progress.ProgressReporter;

public class EnableTm3Command extends TM3Command implements ProgressReporter {

    @Override
    public String getDescription() {
        return "Enables TM3 for a company and creates TM storage tables.";
    }

    @Override
    public String getName() {
        return "enable-tm3";
    }

    @Override
    protected String getUsageLine() {
        return super.getUsageLine() + " <companyId>";
    }

    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Enable the tm3 storage engine for a given company.");
        out.println("This does two things: create storage tables in the DB for");
        out.println("the company, and modify the TM_VERSION column for the");
        out.println("company in the COMPANY table.  Running this command");
        out.println("multiple times for a single company will have no effect.");
    }
        
    // XXX Sort of a bug.  We must return true here in order to get
    // the custom Hibernate bindings to work.
    @Override
    protected boolean requiresDataFactory() {
        return true;
    }
        
    @SuppressWarnings("unchecked")
    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        
        List<String> args = command.getArgList();
        if (args.size() != 1) {
            usage();
        }
        long companyId = 0;
        try {
            companyId = Long.valueOf(args.get(0));
        }
        catch (NumberFormatException e) {
            die("Not a company id: " + args.get(0));
        }

        // Verify it's a valid company.  
        Company company = (Company)session.get(Company.class, companyId);
        if (company == null) {
            die("Non-existent company: " + companyId);
        }
        
        new Tm3Enabler(session.connection(), company).enable(this);
    }

    @Override
    public void setMessageKey(String messageKey, String defaultMessage) {
        System.out.println(defaultMessage);
    }

    @Override
    public void setPercentage(int percentage) {
    }

}
