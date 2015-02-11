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

import java.net.URL;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Locale;

import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistryException;
import com.globalsight.ling.docproc.DiplomatSegmenter;
import com.globalsight.ling.docproc.DiplomatSegmenterException;
import com.globalsight.ling.docproc.DiplomatWordCounter;
import com.globalsight.ling.docproc.DiplomatWordCounterException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.LocalizableElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.DiplomatAttribute;
import com.globalsight.ling.docproc.DiplomatWriter;
import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.ling.util.GeneralException;


/** Align GXML documents
 */
public class SegmentAligner
{       
    private SegmentTagsAligner m_tagAligner = null;   
    
    /**
     * SegmentAligner constructor comment.
     */
    public SegmentAligner()
    {
        super();
        m_tagAligner = new SegmentTagsAligner();
    }
    
    private Output extract(FileInfo p_file, String p_format)
    throws AlignerException
    {
        // codeset
        if (p_file.getCodeset() == null)
        {
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION);
        }
        
        // input path
        if (p_file.getPath() == null || p_file.getName() == null)
        {
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION);
        }
        
        // locale
        if (p_file.getLocale() == null)
        {
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION);
        }
        
        // construct an extractor
        String strClass = null;
        try
        {
            ExtractorRegistry registry = ExtractorRegistry.getObject();
            strClass =
            registry.getExtractorClasspath(registry.getFormatId(p_format));
        }
        catch(ExtractorRegistryException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION, e);
        }
        
        AbstractExtractor extractor;
        
        try
        {
            extractor = (AbstractExtractor)
            Class.forName(strClass).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            // The factory class was not found
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION, e);
        }
        catch (InstantiationException e)
        {
            // The factory class wasn't instantiated
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION, e);
        }
        catch (IllegalAccessException e)
        {
            // The factory class couldn't have been accessed
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION, e);
        }
        catch (ClassCastException e)
        {
            // The extractor class was not a AbstractExtractor class
            throw new AlignerException(
            AlignerExceptionConstants.EXTRACTOR_EXCEPTION, e);
            
        }
        
        String filePath = p_file.getPath() + "/" + p_file.getName();        
        EFInputData input = new EFInputData();
        input.setCodeset(p_file.getCodeset());
        input.setLocale(LocaleCreater.makeLocale(p_file.getLocale()));
        input.setURL(fileToUrl(filePath).toString());
        Output output = new Output ();
        
        // extract
        try
        {
            extractor.init(input, output);
            extractor.loadRules();
            extractor.extract();
        }
        catch(ExtractorException e)
        {
            System.err.println("Extractor error: " + e.toString());
            return null; // continue
        }
        
        // write out GXML file for debug 
        File f = new File(filePath+".xml");
        try
        {
            FileWriter fw = new FileWriter(f);
            fw.write(DiplomatWriter.WriteXML(output));
            fw.flush();
        }
        catch(IOException e)
        {
            System.err.println("UNEXPECTED ERROR: error writing GXML file");
        }
        
        // segment
        try
        {
            DiplomatSegmenter ds = new DiplomatSegmenter ();
            ds.segment(output);
            output = ds.getOutput();
        }
        catch(DiplomatSegmenterException e)
        {
            return null;
        }
        
        /* no need for word count
        // word count
        try
        {
            DiplomatWordCounter wc = new DiplomatWordCounter ();
            wc.countDiplomatDocument(output);
        }
        catch(DiplomatWordCounterException e)
        {
            throw new AlignerException(
                AlignerExceptionConstants.EXTRACTOR_EXCEPTION, e);
        }
         */
        return output;
    }
    
    /**
     * <p>Converts a filename to an absolute file:// URL.</p>
     */
    static private URL fileToUrl(String str_fileName)
    {
        File file = new File (str_fileName);
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        
        // System.err.println("absolute path: " + path);
        
        if (fSep != null && fSep.length() == 1)
        {
            path = path.replace(fSep.charAt(0), '/');
        }
        
        if (path.length() > 0 && path.charAt(0) != '/')
        {
            path = '/' + path;
        }
        
        // System.err.println("fixed path: " + path);
        
        try
        {
            return new URL ("file", null, path);
        }
        catch (java.net.MalformedURLException e)
        {
            // According to the spec this could only happen if the file
            // protocol were not recognized.
            
            // throw new Exception ("unexpected MalformedURLException: " +
            // e.toString());
            
            System.err.println("UNEXPECTED ERROR: file protocol unknown");
            return null;
        }
    }
    
    /** Given a list of aligned files, align blocks and segments in turn
     * return the alignments for each locale
     * @param p_files Aligned files source and all matching locales
     * @throws AlignerException
     * @return AlignedPageBlocks
    */    
    public AlignedPageBlocks align(AlignedFiles p_files)
    throws AlignerException
    {
        PageBlocks pageBlocks = null;
        
        // place are alignments here
        AlignedPageBlocks alignedPageBlocks = new AlignedPageBlocks();
        
        alignedPageBlocks.setRelativePath(p_files.getSrcRelativePath());
        alignedPageBlocks.setSourceLocale(p_files.getSrcLocale());
        alignedPageBlocks.setSourceFileName(p_files.getSrcFileName());
        
        FileInfo srcFileInfo = new FileInfo();
        srcFileInfo.setCodeset(p_files.getSrcCodeset());
        srcFileInfo.setLocale(p_files.getSrcLocale());
        srcFileInfo.setName(p_files.getSrcFileName());
        srcFileInfo.setPath(p_files.getSrcPath());
        
        Output srcOut = extract(srcFileInfo, p_files.getFormat());
        if (srcOut == null)
        {
            // extraction failure with the source - don't continue with this page            
            System.err.println("Extractor Failure on Source: " + p_files.getSrcFileName() + "\n");
            
            pageBlocks = new PageBlocks();
            pageBlocks.setAligned(false);
            pageBlocks.setFileName(srcFileInfo.getName());
            pageBlocks.setLocale(srcFileInfo.getLocale());
            pageBlocks.setPath(srcFileInfo.getPath());
            pageBlocks.setWasExtracted(false);
            alignedPageBlocks.addPage(pageBlocks);
            return alignedPageBlocks;
        }
        
        // add the source blocks to m_alignedPageBlocks
        pageBlocks = addSourcePage(srcOut);
        pageBlocks.setFileName(srcFileInfo.getName());
        pageBlocks.setLocale(srcFileInfo.getLocale());
        pageBlocks.setPath(srcFileInfo.getPath());
        alignedPageBlocks.addPage(pageBlocks);
        // finish source page
        //System.err.println("Current File:" + p_files.getSrcPath()+"/"+p_files.getSrcFileName());
         
        // align each target page with the source
        Iterator files = p_files.getTrgFileIterator();
        while(files.hasNext())
        {
            FileInfo trgFileInfo = (FileInfo)files.next();
            Output trgOut = extract(trgFileInfo, p_files.getFormat());
            
            if (trgOut == null)
            {
                // failure extracting target page                
                System.err.println("Extractor Failure on Target: " + trgFileInfo.getLocale() + ", " + p_files.getSrcFileName() + "\n");
                pageBlocks = new PageBlocks();
                pageBlocks.setWasExtracted(false);
                pageBlocks.setAligned(false);
                pageBlocks.setFileName(trgFileInfo.getName());
                pageBlocks.setLocale(trgFileInfo.getLocale());
                pageBlocks.setPath(trgFileInfo.getPath());            
                alignedPageBlocks.addPage(pageBlocks);
                continue;
            }
            
            if (srcOut.getSize() != trgOut.getSize())
            {
                // we are mislaigned at the block level
                // possibly added or deleted content                
                //System.err.println("Misaligned Block:" + p_files.getSrcFileName());
                pageBlocks = new PageBlocks();
                pageBlocks.setAligned(false);
                pageBlocks.setFileName(trgFileInfo.getName());
                pageBlocks.setLocale(trgFileInfo.getLocale());
                pageBlocks.setPath(trgFileInfo.getPath());            
                alignedPageBlocks.addPage(pageBlocks);
                continue;
            }
            
            // at least we align at the block level
            pageBlocks = alignBlocks(srcOut, trgOut);
            
            // add the blocks for this page to the aligned pages
            pageBlocks.setFileName(trgFileInfo.getName());
            pageBlocks.setLocale(trgFileInfo.getLocale());
            pageBlocks.setPath(trgFileInfo.getPath());
            
            alignedPageBlocks.addPage(pageBlocks);            
        }
        
        return alignedPageBlocks;
    }
    
    private PageBlocks alignBlocks(Output p_source, Output p_target)
    throws AlignerException
    {       
        Segment seg = null;       
        PageBlocks pageBlocks = new PageBlocks();
        
        Iterator source = p_source.documentElementIterator();
        Iterator target = p_target.documentElementIterator();
        while (source.hasNext())
        {
            DocumentElement src = (DocumentElement)source.next();
            DocumentElement trg = (DocumentElement)target.next();
            if (src.type() != trg.type())
            {
                pageBlocks.setAligned(false);
                //System.err.println("Block Type Mismatch");
                return pageBlocks;
            }
                        
            switch (src.type())
            {
                case DocumentElement.LOCALIZABLE:
                    // align tags
                    seg = alignTags(
                        ((LocalizableElement)src).getChunk(),
                        ((LocalizableElement)trg).getChunk());
                    Block block = new Block();
                    seg.setTuType(((LocalizableElement)trg).getType());
                    seg.setDataType(((LocalizableElement)trg).getDataType());
                    seg.setIsLocalized(true);
                    block.addSegment(seg);                   
                    pageBlocks.addBlock(block);
                    break;
                    
                case DocumentElement.TRANSLATABLE:
                     block = alignSegments((TranslatableElement)src,
                            (TranslatableElement)trg);
                    pageBlocks.addBlock(block);
                    break;
                    
                default:                    
                    break;
            }           
        }        
        return pageBlocks;
    }
    
    
    private PageBlocks addSourcePage(Output p_source)
    {
        Segment seg = null;
        Block block = null;
        PageBlocks pageBlocks = new PageBlocks();
        
        Iterator source = p_source.documentElementIterator();
        while (source.hasNext())
        {
            DocumentElement src = (DocumentElement)source.next();           
            switch (src.type())
            {
                case DocumentElement.LOCALIZABLE:
                    block = new Block();
                    seg = new Segment();
                    seg.setSegment(((LocalizableElement)src).getChunk());
                    seg.setTuType(((LocalizableElement)src).getType());
                    seg.setDataType(((LocalizableElement)src).getDataType());
                    seg.setIsLocalized(true);
                    block.addSegment(seg);
                    pageBlocks.addBlock(block);
                    break;
                    
                case DocumentElement.TRANSLATABLE:
                    block = new Block();
                    Iterator segments = ((TranslatableElement)src).getSegments();
                    while(segments.hasNext())
                    {
                        SegmentNode segNode = (SegmentNode)segments.next();
                        seg = new Segment();
                        seg.setSegment(segNode.getSegment());                        
                        seg.setTuType(((TranslatableElement)src).getType());
                        seg.setDataType(((TranslatableElement)src).getDataType());
                        seg.setIsLocalized(false);
                        block.addSegment(seg);                                                
                    }
                    pageBlocks.addBlock(block);
                    break;
                    
                default:                        
                   break;
            }            
        }        
        return pageBlocks;
    }
    
    private Block alignSegments(TranslatableElement p_source,
                    TranslatableElement p_target)
    throws AlignerException
    {
        Block block = new Block();
        
        // mismatch if we have different number of
        // source and target segments
        Iterator srcSegments = p_source.getSegments();
        Iterator trgSegments = p_target.getSegments();
        if (p_source.getSegmentCount() != p_target.getSegmentCount())
        {
            block.setSegmentsAligned(false);
            while(trgSegments.hasNext())
            {
                SegmentNode trgSegNode = (SegmentNode)trgSegments.next();
                Segment seg = new Segment(trgSegNode.getSegment());
                seg.setTuType(p_target.getType());
                seg.setDataType(p_target.getDataType());
                seg.setIsLocalized(false);
                block.addSegment(seg);                              
            }            
            //System.err.println("Block Segment Count Mismatch");
            return block;
        }
      
        // same number of segments in source and target
        //System.err.println(p_source.getSegmentCount());
        //System.err.println(p_target.getSegmentCount());
        while(trgSegments.hasNext())
        {
            SegmentNode srcSegNode = (SegmentNode)srcSegments.next();
            SegmentNode trgSegNode = (SegmentNode)trgSegments.next();
            Segment trgSeg = alignTags(srcSegNode.getSegment(),
                                       trgSegNode.getSegment());          
            trgSeg.setTuType(p_target.getType());
            trgSeg.setDataType(p_target.getDataType());
            trgSeg.setIsLocalized(false);
            block.addSegment(trgSeg);            
            //System.err.println("Trg Seg:" + trgSeg.getSegment());                     
        }        
        //System.err.println("-------------------");    
        return block;      
    }
    
    private Segment alignTags(String p_source, String p_target)
    throws AlignerException
    {
        String newTarget = null;
        Segment seg = new Segment();
        
        newTarget = m_tagAligner.alignTags(p_source, p_target);
        if (newTarget == null)
        {
            seg.setTagsAligned(false);
            seg.setSegment(p_target);
            //System.err.println("Mismatch Tags:\n" +  "Src:\n" + p_source + "\n\n" + "Trg:\n" + p_target);
        }
        else
        {
            seg.setSegment(newTarget);           
        }
        
        return seg;
    }
}