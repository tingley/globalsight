/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.tools.exactmatchkey;

import java.sql.Connection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.ling.common.Text;

/**
 * Store exact match key for target locales in TUV table.
 *
 * Until System 4 4.4.2, exact match keys are stored only for source
 * TUV segments. Since 4.4.4, exact match keys are necessary for
 * targets as well to prevent the leverage query from returning lots
 * of duplicates.
 */

public class ExactMatchKeyMigration
{
    public static void main(String[] args)
        throws Exception
    {
        // parse the arguments
        ParseArgs parsedArgs = new ParseArgs();
        if(!parsedArgs.parse(args))
        {
            System.err.println("Unrecognized command parameter.");
            System.err.println("USAGE java ExactMatchKeyMigration [-q]");
            System.exit(1);
        }
        

        Connection system4Connection = null;
        boolean autoCommit = false;
        
        try
        {
            system4Connection = ConnectionPool.getConnection(-1L);

            if(parsedArgs.askProceed())
            {
                // prompt for confirmation
                confirm(system4Connection.getMetaData().getURL());
            }
        
            System.err.println("Migrating exact match keys.");
            long processedItemNum = 0;

            // autocommit off
            autoCommit = system4Connection.getAutoCommit();
            system4Connection.setAutoCommit(false);
            
            TuvQuery tuvQuery = new TuvQuery(system4Connection);
            tuvQuery.query();
            TuvRep tuv = null;
        
            while((tuv = tuvQuery.next()) != null)
            {
                // calculate and store the exact match key
                tuv.updateExactMatchKey();

                // feedback of process
                processedItemNum++;
                if((processedItemNum % 10000) == 0)
                {
                    System.err.print(":");
                }
                else if((processedItemNum % 1000) == 0)
                {
                    System.err.print(".");
                }
            }

            // persist the changes
            system4Connection.commit();

            System.err.println("\nDone exact match key migration.");
            System.out.println("" + processedItemNum
                + " segments have been migrated.");
        }
        finally
        {
            system4Connection.setAutoCommit(autoCommit);
            // return System 4 connection to ConnectionPool
            ConnectionPool.returnConnection(system4Connection);
        }
    }


    static private void confirm(String p_dbUrl)
        throws Exception
    {
        System.out.println("Do you want to migrate exact match key in System 4 table from the following database?");
        System.out.println("  System 4 database: " + p_dbUrl);
        System.out.print("Proceed? (yes/no)");
        BufferedReader br
            = new BufferedReader(new InputStreamReader(System.in));
        while(true)
        {
            String reply = br.readLine();
            if(reply.equals("yes"))
            {
                break;
            }
            else if(reply.equals("no"))
            {
                System.exit(0);
            }
            else
            {
                System.out.print("Reply yes or no. Proceed? (yes/no)");
            }
        }
    }
        
    
}

