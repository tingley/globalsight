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

package com.globalsight.tools.normalizetuv;

import java.sql.Connection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.ling.common.Text;

/**
 * White space normalize tuv table.
 *
 * Whitespace normalization is performed only on HTML segments with a
 * specified locale (typically source locale). The procedure is as
 * follows.
 *
 *   1. Take a string from either segment_string or segment_clob column.
 *   2. Whitespace normalize it using c.g.l.common.Text#normalizeWhiteSpaces().
 *   3. If the result of the normalized string is different from the
 *      original, it is written back to the database. exact_match_key is
 *      also recalculated and updated.
 *   4. If the segment is translatable, delete its indices from
 *      fuzzy_index table and reindex it.
 */
public class WsNormalizeMigrate
{
    public static void main(String[] args)
        throws Exception
    {
        // command line check
        if(args.length < 1)
        {
            System.err.println("USAGE java WsNormalizeMigrate [-q] locale");
            System.exit(1);
        }
        
        // parse the arguments
        ParseArgs parsedArgs = new ParseArgs();
        parsedArgs.parse(args);

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
        
            System.err.println("Normalizing Tuvs.");
            long iteratedItemNum = 0;
            long processedItemNum = 0;

            // get source GlobalSightLocale
            LocaleManagerLocal localeManager = new LocaleManagerLocal();
            GlobalSightLocale srcLocale = localeManager.getLocaleByString(
                parsedArgs.getSourceLocale());

            // autocommit off
            autoCommit = system4Connection.getAutoCommit();
            system4Connection.setAutoCommit(false);
            
            TuvIndexer tuvIndexer
                = new TuvIndexer(system4Connection, srcLocale);
            TuvQuery tuvQuery = new TuvQuery(system4Connection, srcLocale);
            tuvQuery.query();
            TuvRep tuv = null;
        
            while((tuv = tuvQuery.next()) != null)
            {
                // normalize segment string
                String normalizedString
                    = Text.normalizeWhiteSpaces(tuv.getGxml());
                if(!normalizedString.equals(tuv.getGxml()))
                {
                    // normalized string is different from the original
                    processedItemNum++;

                    tuv.updateSegment(normalizedString);

                    // persist the changes
                    system4Connection.commit();

                    if(!tuv.isLocalizable())
                    {
                        tuvIndexer.index(tuv.getFuzzyMatchFormat(),
                            tuv.getId(), tuv.getTmId());
                    }
                }
            
                // feedback of process
                iteratedItemNum++;
                if((iteratedItemNum % 10000) == 0)
                {
                    System.err.print(":");
                }
                else if((iteratedItemNum % 1000) == 0)
                {
                    System.err.print(".");
                }
            }

            // persist the changes
            system4Connection.commit();

            System.err.println("\nDone normalizing Tuvs.");
            System.out.println("" + processedItemNum
                + " segments have been whitespace normalized.");
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
        System.out.println("Do you want to whitespace normalize the segment data in System 4 table from the following database?");
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

