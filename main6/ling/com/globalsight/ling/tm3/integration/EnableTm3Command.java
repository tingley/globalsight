/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.tm3.integration;

import java.io.PrintStream;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.tools.TM3Command;
import com.globalsight.util.progress.ProgressReporter;

public class EnableTm3Command extends TM3Command implements ProgressReporter
{

    @Override
    public String getDescription()
    {
        return "Enables TM3 for a company and creates TM storage tables.";
    }

    @Override
    public String getName()
    {
        return "enable-tm3";
    }

    @Override
    protected String getUsageLine()
    {
        return super.getUsageLine() + " <companyId>";
    }

    @Override
    protected void printExtraHelp(PrintStream out)
    {
        out.println("Enable the tm3 storage engine for a given company.");
        out.println("This does two things: create storage tables in the DB for");
        out.println("the company, and modify the TM_VERSION column for the");
        out.println("company in the COMPANY table.  Running this command");
        out.println("multiple times for a single company will have no effect.");
    }

    // XXX Sort of a bug. We must return true here in order to get
    // the custom Hibernate bindings to work.
    @Override
    protected boolean requiresDataFactory()
    {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handle(CommandLine command) throws Exception
    {

        List<String> args = command.getArgList();
        if (args.size() != 1)
        {
            usage();
        }
        long companyId = 0;
        try
        {
            companyId = Long.valueOf(args.get(0));
        }
        catch (NumberFormatException e)
        {
            die("Not a company id: " + args.get(0));
        }

        // Verify it's a valid company.
        Company company = CompanyWrapper.getCompanyById(companyId);
        if (company == null)
        {
            die("Non-existent company: " + companyId);
        }

        Connection conn = DbUtil.getConnection();
        conn.setAutoCommit(false);
        try
        {
            new Tm3Enabler(conn, company).enable(this);
            conn.commit();
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    @Override
    public void setMessageKey(String messageKey, String defaultMessage)
    {
        System.out.println(defaultMessage);
    }

    @Override
    public void setPercentage(int percentage)
    {
    }

}
