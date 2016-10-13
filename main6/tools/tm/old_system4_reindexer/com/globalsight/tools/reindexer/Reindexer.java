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

package com.globalsight.tools.reindexer;

import java.sql.Connection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.localemgr.LocaleManagerLocal;

/**
 * Reindex source segments.
 *
 * This tools is used to re-index source segments in System 4 tables.
 * The procedure of reindexing is as follows.
 *
 *   1. Drop fuzzy_index table and create it again just to delete all
 *      data in it.
 *   2. Query Tuvs that is specified source locale and is_index flag
 *      is set to 'Y'.
 *   3. Index the segment.
 */
public class Reindexer
{
    public static void main(String[] args)
        throws Exception
    {
        // command line check
        if(args.length < 1)
        {
            System.err.println("USAGE java Reindexer [-qd] locale");
            System.exit(1);
        }
        
        // parse the arguments
        ParseArgs parsedArgs = new ParseArgs();
        parsedArgs.parse(args);

        Connection system4Connection = ConnectionPool.getConnection(-1L);

        if(parsedArgs.askProceed())
        {
            // prompt for confirmation
            confirm(system4Connection.getMetaData().getURL());
        }
        
        // get source GlobalSightLocale
        LocaleManagerLocal localeManager = new LocaleManagerLocal();
        GlobalSightLocale srcLocale
            = localeManager.getLocaleByString(parsedArgs.getSourceLocale());

        TuvIndexer indexer = new TuvIndexer(system4Connection,
            srcLocale, parsedArgs.dropFuzzyIndex());

        if(parsedArgs.dropFuzzyIndex())
        {
            // drop fuzzy_index table
            indexer.recreateFuzzyIndexTable();
        }

        TuvQuery tuvQuery = new TuvQuery(system4Connection, srcLocale);
        tuvQuery.query();
        System.err.println("Reindexing...");
        long processedItemNum = 0;
        TuvRep tuv = null;
        while((tuv = tuvQuery.next()) != null)
        {
            // reindex the segment
            indexer.index(tuv.getFuzzyMatchFormat(),
                tuv.getId(), tuv.getTmId());
            
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
        system4Connection.commit();
        
        System.err.println("\nDone reindexing.");
        
        // return System 4 connection to ConnectionPool
        ConnectionPool.returnConnection(system4Connection);
        
        System.out.println("" + processedItemNum
            + " segments have been reindexed.");
    }


    static private void confirm(String p_dbUrl)
        throws Exception
    {
        System.out.println("Do you want to reindex the segment data in System 4 table from the following database?");
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

