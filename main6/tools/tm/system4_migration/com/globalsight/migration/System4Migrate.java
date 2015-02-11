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

package com.globalsight.migration;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.globalsight.migration.system3.*;
import com.globalsight.migration.system4.*;

import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.LeverageGroupImpl;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.tm.TmImpl;
import com.globalsight.ling.tm.Indexer;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.persistence.TopLinkPersistence;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.diplomat.util.database.ConnectionPool;

import TOPLink.Public.PublicInterface.UnitOfWork;
import TOPLink.Public.PublicInterface.Session;
import TOPLink.Public.PublicInterface.Descriptor;

/**
 * Migration Tool (System 3 -> System 4)
 */
public class System4Migrate
{
    public static void main(String[] args)
        throws Exception
    {
        // command line check
        if(args.length < 1)
        {
            System.err.println("USAGE java System4Migrate config_file");
            System.exit(1);
        }
        
        // read a config file
        InputStream in = new FileInputStream(args[0]);
        ResourceBundle resource
            = new PropertyResourceBundle(new BufferedInputStream(in));

        // locale string error check
        String sourceLocale = resource.getString("system3.sourceLocale");
        if(sourceLocale.length() != 5
           || !sourceLocale.substring(2, 3).equals("_"))
        {
            throw new Exception("Source locale name in config file is incorrect: " + sourceLocale);
        }

        String dataFromItems = resource.getString("system3.dataFromItems");
        if(dataFromItems == null
            || !(dataFromItems.equals("yes") || dataFromItems.equals("no")))
        {
            throw new Exception("Invalid configuration: system3.dataFromItems must be \"yes\" or \"no\" and case sensitive.");
        }
        
        // prompt for confirmation
        System.out.println("Do you want to proceed the System 3 migration using the following setting?");
        System.out.println("  System 3 database: " + resource.getString("system3.dburl"));
        System.out.println("  System 4 running on: " + InetAddress.getLocalHost().getHostName());
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
        

        // connect to the System 3 database
        Connection connection = System3DbConnect.connect(resource);

        // create System 4 tm
        String tmName = resource.getString("system4.tmName");
        if(tmName == null)
        {
            throw new Exception("TM name must be specified in config.properties file.");
        }
        
        Tm tm = MigrationTm.get(tmName);
        
        // delete old migration data including fuzzy_index
        MigrationTu.deleteAllTuTuvs(tm);
        

        // retrieve data from System 3
        DataTable dataTable = null;
        if(dataFromItems.equals("yes"))
        {
            dataTable = new Items(connection, sourceLocale);
        }
        else
        {
            dataTable = new ExactTm(connection,
                resource.getString("system3.dataTypeInExacttm"));
        }
        
        dataTable.query();

        Session session
            = PersistenceService.getInstance().acquireClientSession();
        session.getDescriptor(TuImpl.class).useNoIdentityMap();
        session.getDescriptor(TuvImpl.class).useNoIdentityMap();

        Connection system4Connection = ConnectionPool.getConnection(-1L);
        MigrationTu tuCreator = new MigrationTu(system4Connection);
        MigrationTuv tuvCreator = new MigrationTuv(system4Connection);
        
        MigrationLocale locale = new MigrationLocale(system4Connection);
        System3Segment sys3Seg = null;
        LeverageGroup leverageGroup = MigrationLeverageGroup.create();
        int processedItemNum = 0;
        // store each segment to System 4
        System.err.println("Moving Tuvs.");
        while((sys3Seg = dataTable.nextSegment()) != null)
        {
            Tu tu = tuCreator.create(sys3Seg, tm, leverageGroup);
            Iterator it = sys3Seg.iterator();
            while(it.hasNext())
            {
                // each locale tuv
                Map.Entry entry = (Map.Entry)it.next();
                Tuv tuv = tuvCreator.create((String)entry.getValue(),
                    sys3Seg.isTranslatable(),
                    locale.get((String)entry.getKey()), tu);

                // index a source locale tuv
                if(sourceLocale.equals((String)entry.getKey()))
                {
                    MigrationIndexer.index(tuv);
                }

                // A little feedback of process
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
        }
        System.err.println("\nDone moving Tuvs.");
        
        // run the analyze stored procedure
        System.err.print("Running analyze stored procedure... ");
        String userName = system4Connection.getMetaData().getUserName();
        CallableStatement storedProc
            = system4Connection.prepareCall("{call analyze_sys4(?)}");
        storedProc.setString(1, userName.toUpperCase());
        storedProc.execute();
        System.err.println("Done.");

        // return System 4 connection to ConnectionPool
        ConnectionPool.returnConnection(system4Connection);

        // disconnect from System 3 database
        connection.close();
        
//          // index the tm data in System 4
//          Map processedLocales = locale.getLocaleCache();
//          Collection values = processedLocales.values();
//          Iterator it = values.iterator();
//          while(it.hasNext())
//          {
//              GlobalSightLocale indexLocale = (GlobalSightLocale)it.next();
//              System.err.println("Creating index for "
//                  + indexLocale.toString() + "...");
//              MigrationIndexer.index(indexLocale, tm.getId());
//          }

        System.out.println("" + processedItemNum
            + " segments have been migrated to System 4.");
    }
    
}

