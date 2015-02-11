/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

import java.util.List;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.text.MessageFormat;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.tuv.LeverageGroup;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tm.Tm;

import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.common.Text;

import com.globalsight.migration.system3.System3Segment;
import com.globalsight.migration.system4.*;

class StoreL10NedPages
{
    static private final String c_regex = "[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]";
    private AlignedPageBlocks m_pages = null;    
    private boolean m_saveModifiedOnly;
    private LeverageGroup m_leverageGroup;
    private Tm m_tm;
    private MigrationLocale m_locale;
    private Connection m_connection;
    private MigrationTu m_migrationTu;
    private MigrationTuv m_migrationTuv;
    private MigrationIndexer m_migrationIndexer;
    private String m_srcLocale;

    // Constructor
    public StoreL10NedPages(boolean p_saveModifiedOnly)        
    {             
        m_saveModifiedOnly = p_saveModifiedOnly;
    }
    
    public void createTM(
        Connection p_connection, 
        String p_tmName,
        String p_srcLocale)
        throws Exception
    {
        m_connection = p_connection;        
        //Project project = MigrationProject.get(MigrationUser.get());
        m_leverageGroup = MigrationLeverageGroup.create();
        m_tm = MigrationTm.get(p_tmName);
        m_locale = new MigrationLocale(m_connection);
        m_migrationTu = new MigrationTu(m_connection);
        m_migrationTuv = new MigrationTuv(m_connection);
        m_migrationIndexer = new MigrationIndexer();
        m_srcLocale = p_srcLocale;
        
        // delete old migration data
        MigrationTu.deleteAllTuTuvs(m_tm);
    }

    // store localized pages to DB
    public void storePages(AlignedPageBlocks pages)
        throws Exception
    {
        m_pages = pages;    
        PageBlocks blocks;
        LinkedList alignedPages = new LinkedList();                        
        
        // add all valid pages to alignedPages list
        Iterator it = m_pages.getPageBlocks();
        while(it.hasNext())
        {
            blocks = (PageBlocks)it.next();
            if(blocks.wasExtracted() && blocks.isAligned())
            {
                blocks.startIteration();
                alignedPages.add(blocks);
            }           
        }
               
        boolean hasBlocks = true;
        while(hasBlocks)
        {
            it = alignedPages.iterator();
            LinkedList alignedBlocks = new LinkedList();
            while(it.hasNext())
            {                
                blocks = (PageBlocks)it.next();
                Block block = blocks.getNext();
                if(block == null)
                {                    
                    hasBlocks = false;
                    break;
                }
                block.setLocale(blocks.getLocale());
                block.startIteration();                
                alignedBlocks.add(block);
            }
            storeAlignedBlocks(alignedBlocks);                
        }
    }
    
    private void storeAlignedBlocks(LinkedList p_alignedBlocks)
    throws Exception
    {     
        if (p_alignedBlocks.isEmpty())
        {
            return;
        }
        
        boolean hasSegments = true;
        while(hasSegments)
        {
            Iterator it = p_alignedBlocks.iterator();
            LinkedList segments = new LinkedList();
            while(it.hasNext())
            {
                Block block = (Block)it.next();
                if (block.areSegmentsAligned())
                {
                    Segment segment = block.getNext();
                    if (segment == null)
                    {
                        hasSegments = false;
                        break;
                    }           
                    segment.setLocale(block.getLocale());
                    segments.add(segment);
                }                                    
            }            
            storeAlignedSegments(segments);
        }
    }
    
    private void storeAlignedSegments(LinkedList p_alignedSegments)
    throws Exception
    {
        if (p_alignedSegments.isEmpty())
        {
            return;
        }
              
        String cleaned_segment;
        
        System3Segment sys3Seg = new System3Segment();
        
        Iterator it = p_alignedSegments.iterator();
        
        // first segment should be source
        Segment segment = (Segment)it.next();
        sys3Seg.setDataType(segment.getDataType());
        sys3Seg.setItemType(segment.getTuType());
        sys3Seg.setTranslatable(!segment.isLocalized());
            
        // should be source segment
        addText(sys3Seg, segment);
        
        // add remaining targets
        while(it.hasNext())
        {
            segment = (Segment)it.next();
            addText(sys3Seg, segment);
        }        
        
        // save TU (with TUVs) to System4 DB        
         Tu tu = m_migrationTu.create(sys3Seg, m_tm, m_leverageGroup);
         it = sys3Seg.iterator();
         while(it.hasNext())
         {
             // each locale tuv
             Map.Entry entry = (Map.Entry)it.next();
             Tuv tuv = m_migrationTuv.create((String)entry.getValue(),
                        sys3Seg.isTranslatable(),
                        m_locale.get((String)entry.getKey()), tu);
             
             // only index source TUVs as defined in config properties
             String locale = (String)entry.getKey();            
             if (locale.equalsIgnoreCase(m_srcLocale))
             {                
                m_migrationIndexer.index(tuv);               
             }
         }
    }   
    
    private void addText(System3Segment p_sys3Seg, Segment p_segment)
    throws Exception
    {
        // check to see if the utf length is > VARCHAR2 length limits
        if (Text.getUTF8Len(p_segment.getSegment()) <= 3990)
        {                
            // strip out control characters that would raise an error
            // with XML parser.        
            String cleaned_segment = RegEx.substituteAll(p_segment.getSegment(), c_regex, "");
            p_sys3Seg.addText(p_segment.getLocale(), cleaned_segment);                
        }   
        else
        {
             p_sys3Seg.addText(p_segment.getLocale(), "MISSING CLOB");       
        }
    }
}


