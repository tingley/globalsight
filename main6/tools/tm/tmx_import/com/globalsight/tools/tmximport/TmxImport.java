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

package com.globalsight.tools.tmximport;

import java.sql.Connection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;


import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.GeneralException;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.ling.common.Text;
import com.globalsight.everest.tm.Tm;

/**
 * Import TMX files.
 */
public class TmxImport
{
    public static void main(String[] args)
        throws Exception
    {
        // parse the arguments
        ParseArgs parsedArgs = new ParseArgs();
        parsedArgs.parse(args);

        // check the commandline syntax
        if(parsedArgs.hasError())
        {
            System.err.println(parsedArgs.getErrorMessage());
            
            printUsage();
            System.exit(1);
        }
        
        Connection system4Connection = null;
        try
        {
            system4Connection = ConnectionPool.getConnection(-1L);
            // invalid auto commit
            system4Connection.setAutoCommit(false);

            if(!parsedArgs.isQuiet())
            {
                // prompt for confirmation
                confirm(system4Connection.getMetaData().getURL(), parsedArgs);
            }
        
            Tm tm = TmTmx.get(parsedArgs.getTmName());
            if(parsedArgs.deletesData())
            {
                System.err.println("Deleting previous data...");
                DeleteData.deleteAllTuTuvs(tm);
            }
            
            System.err.println("Importing TMX...");

            TmxParser parser
                = new TmxParser(system4Connection, tm,
                    LeverageGroupTmx.create());
        
            // parse TMX files and save data
            Iterator it = parsedArgs.getTmxFileNames().iterator();
            while(it.hasNext())
            {
                String tmxFile = (String)it.next();
                System.out.println("Processing " + tmxFile + "...");
                parser.parse(tmxFile);
            }

            // persist the changes
            system4Connection.commit();

            System.out.println("Done importing TMXs.");
        }
        catch(GeneralException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // return System 4 connection to ConnectionPool
            ConnectionPool.returnConnection(system4Connection);
        }
    }


    static private void confirm(String p_dbUrl, ParseArgs p_args)
        throws Exception
    {
        System.out.println("Do you want to import TMX files into System 4 using the following parameters?");
        System.out.println("  System 4 database: " + p_dbUrl);
        System.out.println("  TM name          : " + p_args.getTmName());
        System.out.println("  TM data          : "
            + (p_args.deletesData() ? "delete" : "keep"));
        System.out.println("  TMX files        : ");
        Iterator it = p_args.getTmxFileNames().iterator();
        while(it.hasNext())
        {
            System.out.println("  " + (String)it.next());
        }
        
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
        

    static private void printUsage()
    {
        System.err.println("Usage: java TmxImport [OPTOIN] FILE...");
        System.err.println("  FILE,  A list of files to import separated by a space.");
        System.err.println("         Wildcard can be used.");
        System.err.println("  -t,    TM name to store data. The default is \"TMX_import\"");
        System.err.println("  -d,    Delete the existing data from the specified TM by -t option.");
        System.err.println("  -q,    Suppress the confirmation message.");
    }
    
}

