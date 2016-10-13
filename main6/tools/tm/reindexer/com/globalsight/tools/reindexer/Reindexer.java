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
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.lucene.LuceneIndexWriter;


/**
 * Reindex TM data in GlobalSight 5.2.1
 */
public class Reindexer
{
    private static final String GET_SOURCE_LOCALES
        = "SELECT distinct SOURCE_LOCALE_ID from project_tm_tu_t";

    private static final String GET_SOURCE_SEGMENTS
        = "SELECT tu.id tu_id, tu.tm_id tm_id, tu.format format, "
        + "tu.type type, tuv.id tuv_id, tuv.segment_string segment_string, "
        + "tuv.segment_clob segment_clob from project_tm_tu_t tu, "
        + "project_tm_tuv_t tuv, REINDEX_PROGRESS rec "
        + "where tu.id = tuv.tu_id and tu.id = rec.tu_id and "
        + "tuv.tu_id = rec.tu_id and tu.SOURCE_LOCALE_ID = ? "
        + "and tuv.locale_id = tu.SOURCE_LOCALE_ID and rec.migrated = 0";
    

    private static final boolean SEGMENT_TM = false;
    private static final boolean TRANSLATABLE_IDX = true;

    private Connection m_connection;
    private boolean m_verbose;
    private PreparedStatement m_stmt;
    private ResultSet m_rs;
    private GlobalSightLocale m_sourceLocale;
    
    
    private Reindexer(Connection p_connection, boolean p_verbose)
        throws Exception
    {
        m_verbose = p_verbose;
        m_connection = p_connection;
        m_stmt = m_connection.prepareStatement(GET_SOURCE_SEGMENTS);
    }


    public List getSourceLocales()
        throws Exception
    {
        LocaleManager localeManager = new LocaleManager(m_connection);
        List sourceLocales = new ArrayList();
        Statement st = null;
        
        try
        {
            st = m_connection.createStatement();
            ResultSet rs = st.executeQuery(GET_SOURCE_LOCALES);
            while(rs.next())
            {
                long localeId = rs.getLong("SOURCE_LOCALE_ID");
                sourceLocales.add(
                    localeManager.getGlobalSightLocale(localeId));
            }
        }
        finally
        {
            if(st != null)
            {
                st.close();
            }
        }

        return sourceLocales;
    }

    
    public synchronized void saveTokens(
        List p_tokens, List p_tuIds, GlobalSightLocale p_sourceLocale,
        int p_tuCount, long p_tuToBeIndexedCount)
        throws Exception
    {
        outputDebugNewLine();
        outputDebugString("Saving indexes...");

        m_indexPersistence.saveTokens(p_tokens, SEGMENT_TM,
            p_sourceLocale, TRANSLATABLE_IDX);
                        
        m_progressRecorder.setTusMigrated(p_tuIds);
        m_analyzeIndexTable.analyze();
                        
        p_tokens.clear();
        p_tuIds.clear();

        outputDebugString("Done saving indexes for "
            + p_tuCount + "/" + p_tuToBeIndexedCount + " TUs.");
    }
    

    public void querySourceSegment(GlobalSightLocale p_sourceLocale)
        throws Exception
    {
        m_sourceLocale = p_sourceLocale;

        m_stmt.setLong(1, p_sourceLocale.getId());
        m_rs = m_stmt.executeQuery();
    }
    

    public SegmentTmTuv nextSegment()
        throws Exception
    {
        SegmentTmTuv tuv = null;
        
        if(m_rs.next())
        {
            String segment = m_rs.getString("segment_string");
            if(segment == null)
            {
                segment = DbUtil.readClob(m_rs, "segment_clob");
            }
            
            tuv = new SegmentTmTuv(m_rs.getLong("tuv_id"),
                segment, m_sourceLocale);

            SegmentTmTu tu = new SegmentTmTu(m_rs.getLong("tu_id"),
                m_rs.getLong("tm_id"), m_rs.getString("format"),
                m_rs.getString("type"), true, m_sourceLocale);

            tu.addTuv(tuv);
        }
        
        return tuv;
    }
    

    private void outputDebugString(String p_message)
    {
        if(m_verbose)
        {
            Date now = new Date();
            System.err.println(now.toString() + " " + p_message);
        }
    }
    
    private void outputDebugNewLine()
    {
        if(m_verbose)
        {
            System.err.println();
        }
    }
    
    
    public static void main(String[] args)
        throws Throwable
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
        
        
        Connection ambassadorConnection = null;
        
        try
        {
            ambassadorConnection = DbUtil.getConnection();

            if(!parsedArgs.isQuiet())
            {
                // prompt for confirmation
                confirm(parsedArgs, ambassadorConnection);
            }
        
            Reindexer reindexer
                = new Reindexer(ambassadorConnection, parsedArgs.isDebug());
            
            long allTuCount = reindexer.getAllTuCount();
            System.err.println("Indexing " + allTuCount + " TU(s)...");
            
            reindexer.outputDebugString("Starting migration process...");
            
            int tuCount = 0;
            List sourceLocales = reindexer.getSourceLocales();
            
            Iterator itSourceLocales = sourceLocales.iterator();
            while(itSourceLocales.hasNext())
            {
                List tokens = new ArrayList();
                List tuIds = new ArrayList();
                
                GlobalSightLocale sourceLocale
                    = (GlobalSightLocale)itSourceLocales.next();

                reindexer.outputDebugString("Starting reindexing "
                    + sourceLocale.toString() + " source segments...");
                
                reindexer.querySourceSegment(sourceLocale);
                SegmentTmTuv tuv = null;
                while((tuv = reindexer.nextSegment()) != null)
                {
                    tuCount++;
                    
                    // record TU id
                    tuIds.add(new Long(tuv.getTu().getId()));
                    
                    // create tokens of the segment
                    List tuvTokens
                        = tokenizer.tokenize(tuv.getFuzzyIndexFormat(),
                            tuv.getId(), tuv.getTu().getId(),
                            tuv.getTu().getTmId(), tuv.getLocale(), true);

                    tokens.addAll(tuvTokens);

                    if(tuCount % 100 == 0)
                    {
                        System.err.print(tuCount % 1000 == 0 ? ":" : ".");
                    }
                    
                    // save tokens to the database
                    if(tokens.size() > parsedArgs.tokenBufferSize())
                    {
                        reindexer.saveTokens(tokens, tuIds,
                            sourceLocale, tuCount, tuToBeIndexedCount);

                        Thread.currentThread().sleep(1);
                    }
                }
                
                // save left over tokens
                if(tokens.size() > 0)
                {
                    reindexer.saveTokens(tokens, tuIds,
                        sourceLocale, tuCount, tuToBeIndexedCount);

                    Thread.currentThread().sleep(1);
                }
            }

            System.err.println("\nDone reindexing Tm.");
        }
        catch(Throwable e)
        {
            e.printStackTrace();

            ambassadorConnection.rollback();
            
            throw e;
        }
        finally
        {
            // return connection 
            DbUtil.returnConnection(ambassadorConnection);
        }
    }



    static private void confirm(ParseArgs p_args, Connection p_connection)
        throws Exception
    {
        System.out.println("Do you want to reindex the Ambassador Tm using the following database?");

        System.out.println("  Ambassador database: "
            + p_connection.getMetaData().getURL());
        
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
        System.err.println("Usage: java com.globalsight.tools.reindexer.Reindexer [OPTION]");
        System.err.println("  -q,    Suppress the confirmation message.");
        System.err.println("  -v,    Verbose output.");
        System.err.println("  -t,    Number of segments to be indexed at once. Default is 10,000");
    }

}
