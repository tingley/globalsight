package com.globalsight.ling.tm3.integration;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3SaveMode;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.segmenttm.EventType;
import com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute;
import com.globalsight.ling.tm3.integration.segmenttm.TM3Util;
import com.globalsight.ling.tm3.tools.TM3Command;
import com.globalsight.util.progress.ProgressReporter;

// TODO: move to integration.segmenttm?
public class MigrateTmCommand extends TM3Command 
        implements ProgressReporter {

    @Override
    public String getDescription() {
        return "migrates a legacy segment TM to use TM3";
    }

    @Override
    public String getName() {
        return "migrate-tm";
    }

    @Override
    protected String getUsageLine() {
        return super.getUsageLine() + " <tmId>";
    }

    @Override
    protected void printExtraHelp(PrintStream out) {
        out.println("Migrate a legacy (tm2) Project TM to tm3.  This will create");
        out.println("a new Project TM to store the migrated data and leave the");
        out.println("original TM intact.  Note that tm3 must be enabled for the");
        out.println("company in order for this operation to succeed.");
        out.println("Migrating a TM more than once will produce multiple copies");
        out.println("Of the migrated TM.");
    }
    
    @Override
    protected boolean requiresDataFactory() {
        return true;
    }
    
    @Override
    protected void handle(Session session, CommandLine command)
            throws Exception {
        List<String> args = command.getArgList();
        if (args.size() != 1) {
            usage();
        }
        long tm2Id = 0;
        try {
            tm2Id = Long.valueOf(args.get(0));
        }
        catch (NumberFormatException e) {
            die("Not a TM id: " + args.get(0));
        }
        
        // Alternate approach is to use raw db access....
        // Note that going through ProjectHandlerLocal here will actually
        // reinitialize hibernate globally using the standard GS config.
        // (This is not what I want to do.)
        //ProjectTM oldTm = new ProjectHandlerLocal().getProjectTMById(tm2Id, false);
        ProjectTM oldTm = (ProjectTM)session.get(ProjectTM.class, tm2Id);
        if (oldTm == null) {
            die("Not a valid TM id: " + tm2Id);
        }
        if (oldTm.getTm3Id() != null) {
            die("Tm " + tm2Id + " is already using TM3");
        }
        if (oldTm.getIsRemoteTm()) {
            die("Tm " + tm2Id + " is a remote TM and can not be migrated");
        }

        long companyId = 0;
        try {
            companyId = Long.valueOf(oldTm.getCompanyId());
        }
        catch (NumberFormatException e) {
            die("TM " + tm2Id + " belongs to unknown company '" + oldTm.getCompanyId() + "'");
        }

        ProjectTM newTm = new Tm3Migrator(session, companyId, oldTm)
            .migrate(this, new TransactionWrapper());
    }

    @Override
    public void setMessageKey(String messageKey, String defaultMessage) {
        System.out.println(defaultMessage);
    }

    @Override
    public void setPercentage(int percentage) {
        if (percentage > 0 && percentage % 10 == 0) {
            System.out.println("" + percentage + "%");
        }
    }
    
    class TransactionWrapper implements Tm3Migrator.TransactionControl {
        @Override
        public void commitAndRestartTransaction() {
            MigrateTmCommand.this.commitAndRestartTransaction();
        }
    }
}
