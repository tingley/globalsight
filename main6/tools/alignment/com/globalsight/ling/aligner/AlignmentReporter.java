package com.globalsight.ling.aligner;
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

import com.globalsight.ling.aligner.AlignedPageBlocks;
import java.util.ResourceBundle;
import java.util.ListIterator;
import java.util.Vector;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.File;
import java.util.MissingResourceException;
import java.util.Locale;
import java.text.MessageFormat;
import java.util.Iterator;
import java.io.PrintWriter;

public class AlignmentReporter
{
    
    private static final String TEMPLATE_SUMMARY_PAGE_START = "SummaryPageStart";
    private static final String TEMPLATE_SUMMARY_PAGE_END = "SummaryPageEnd";
    private static final String TEMPLATE_PAGE_START = "PageStart";
    private static final String TEMPLATE_PAGE_END = "PageEnd";
    private static final String TEMPLATE_GENERIC_ERROR = "GenericErrorMessage";
    private static final String TEMPLATE_GENERIC_WARN = "GenericWarningMessage";
    private static final String TEMPLATE_NOTIFICATION = "GenericNotificationMessage";
    private static final String TEMPLATE_FILE_ERROR = "FileError";
    private static final String TEMPLATE_SYSTEM_ERROR = "SystemError";
    
    private static final String MSG_SUMMARY_TITLE = "SummaryReportTitle";
    private static final String MSG_REPORT_TITLE = "ReportTitle";
    private static final String MSG_QUICKHELP_TITLE = "QuickHelpTitle";
    private static final String MSG_QUICKHELP = "QuickHelp";
    private static final String MSG_REPORT_END = "EndOfReport";
    private static final String MSG_MISSING_FILES = "MissingFiles";
    private static final String MSG_EXTRA_FILES = "ExtraTrgFiles";
    private static final String MSG_SRC_ALIGNED_FILES = "SrcAlignedFiles";
    private static final String MSG_FILE_ALIGN_SUCCESS = "FileAlignmentOK";
    
    private static final int SEG_PARAM_COUNT = 4;
    
    private static final String RES_TEMPLATE_FILE =
    "com.globalsight.ling.aligner.ReportTemplate";
    private static final String RES_MSG_FILE =
    "com.globalsight.ling.aligner.Resources";
    
    private ResourceBundle m_template = null;
    private ResourceBundle m_msg = null;
    private StringBuffer m_pageMsgs = new StringBuffer();
    private Locale m_locale = null;
    private Locale m_reportLocale = null;
    private boolean m_verbose = true;
    
    private static AlignmentReporter m_instance = null;
    private FileWriter m_summaryWriter = null;
    private String m_rootDir = "";
    
    private int m_reportTotalSegmentCount = 0;
    private int m_reportTotalMismatches = 0;
    private int m_errFileIdx = 1;
    private int m_srcFileIdx = 1;

    /**
     * Singleton constructor
     */
    private AlignmentReporter()
    {
        super();
    }

    /**
     * Get Singleton instance
     */
    public static AlignmentReporter instance() throws AlignerException
    {
        
        if (m_instance == null)
        {
            m_instance = new AlignmentReporter();
            m_instance.setReportLocale(new Locale("en", "US"));
        }
        return m_instance;
    }

    /**
     * Sets new locale and re-loads resources
     */
    public void setReportLocale(Locale p_locale) throws AlignerException
    {
        try
        {
            m_reportLocale = p_locale;
            m_template = loadProperties(RES_TEMPLATE_FILE, p_locale);
            m_msg = loadProperties(RES_MSG_FILE, p_locale);
        }
        catch (MissingResourceException e)
        {
            throw new AlignerException(AlignerExceptionConstants.REPORT_WRITER_ERROR, e);
        }
        
    }

    /**
     * Sets verbose mode
     */
    public void enableVerbose()
    {
        m_verbose = true;
    }

    /**
     * Sets normal mode
     */
    public void disableVerbose()
    {
        m_verbose = false;
    }

    /**
     * Returns writers locale.
     *
     */
    public Locale getReportLocale()
    {
        return m_locale;
    }

    /**
     *
     *
     * @param p_propertyFileName java.lang.String
     * @exception com.globalsight.ling.tw.offline.AmbassadorDwUpException The exception description.
     */
    protected ResourceBundle loadProperties( String p_propertyFileName,    Locale p_locale)
    throws AlignerException
    {
        return ResourceBundle.getBundle(p_propertyFileName, p_locale);
    }

    /**
     *
     *
     * @param p_template java.lang.String
     * @param p_params java.lang.String[]
     */
    protected String formatString(String p_template, String[] p_params)
    {
        return MessageFormat.format(p_template, p_params);
    }

    /**
     *
     *
     * @param p_template java.lang.String
     * @param p_params java.lang.String[]
     */
    protected String formatString(String p_template, String p_param)
    {
        String[] param = new String[2];
        param[1] = p_param;
        return MessageFormat.format(p_template, param);
    }

    /**
     * Assembles and returns the complete HTML error page.
     *
     */
    private String buildPageReport() throws AlignerException
    {
        
        if (m_pageMsgs.length() <= 0)
        {
            return null;
        }
        
        StringBuffer page = new StringBuffer("");
        java.util.Date date = new java.util.Date();
        
        // Top portion - page header and quick help
        String[] paramsTop = new String[5];
        paramsTop[1] = m_msg.getString(MSG_REPORT_TITLE);
        paramsTop[2] = m_msg.getString(MSG_QUICKHELP_TITLE);
        paramsTop[3] = m_msg.getString(MSG_QUICKHELP);
        paramsTop[4] = date.toString();
        page.append(formatString(m_template.getString(TEMPLATE_PAGE_START), paramsTop));
        
        // messages
        page.append(m_pageMsgs);
        m_pageMsgs = new StringBuffer(); //clear messages
        
        // end page
        page.append(
        formatString(
        m_template.getString(TEMPLATE_PAGE_END),
        m_msg.getString(MSG_REPORT_END)));
        
        return prepForHtml(page.toString());
    }

    /**
     * Write a page report
     *
     */
    public void writePageReport(String m_path) throws AlignerException
    {
        try
        {
            String report = buildPageReport();
            if (report != null)
            {
                FileWriter fw = new FileWriter(new File(m_path));
                fw.write(report);
                fw.close();
            }
            else
            {
                File oldReport = new File(m_path);
                oldReport.delete();
            }
        }
        catch (Exception e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/18/2001 9:24:27 PM)
     */
    public void openSummary(String p_path) throws AlignerException
    {
        m_rootDir = p_path;
        try
        {
            StringBuffer page = new StringBuffer("");
            java.util.Date date = new java.util.Date();
            String headBegin = "<p>";
            String headEnd = "</p>";
            
            // Top portion - page header and quick help
            String[] paramsTop = new String[5];
            paramsTop[1] = m_msg.getString(MSG_SUMMARY_TITLE);
            paramsTop[2] = m_msg.getString(MSG_QUICKHELP_TITLE);
            paramsTop[3] = m_msg.getString(MSG_QUICKHELP);
            paramsTop[4] = date.toString();
            page.append(
            formatString(m_template.getString(TEMPLATE_SUMMARY_PAGE_START), paramsTop));
            
            m_summaryWriter =
            new FileWriter(
            new File(p_path + File.separator + FileAlignerConstants.SUMMARY_FNAME));
            m_summaryWriter.write(page.toString());
            m_summaryWriter.flush();
            
            //remove previous report
            File f = new File(p_path + File.separator + FileAlignerConstants.REPORT_SUBDIR);
            if (f.exists())
            {
                File list[] = f.listFiles();
                for (int i = 0; i < list.length; i++)
                {
                    list[i].delete();
                }
                f.delete();
            }
            f.mkdirs();
            
        }
        catch (Exception e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            "Unable to open the alignment report in the following directory: " + p_path );
        }
    }

    /**
     * Replaces all linefeeds with <BR>
     * and all tabs with three non-breaking spaces (&nbsp;).
     * @param p_input - string to prepare.
     * @return java.lang.String[]
     */
    private String prepForHtml(String p_input)
    {
        StringBuffer buff = new StringBuffer();
        int i = 0, len = p_input.length();
        char c;
        
        for (i = 0; i < len; i++)
        {
            c = p_input.charAt(i);
            if ((c == '\n') || (c == '\r'))
            {
                buff.append("<BR>");
            }
            else if (c == '\t')
            {
                buff.append("&nbsp;&nbsp;&nbsp;");
            }
            else
            {
                buff.append(c);
            }
        }
        
        return buff.toString();
        
    }

    /**
     * Adds the File aligment stats message to the page.
     */
    public void addSummaryFileAlignmentStats(FileAligner p_aligner)
    throws AlignerException
    {
        StringBuffer s = null;
        try
        {
            // config file
            m_summaryWriter.write( prepForHtml(
            "Configuration file: "
            + " <a href=\""    + p_aligner.getConfigPathName()
            + "\">" + p_aligner.getConfigPathName() + "</a>\n\n"));
            
            // source
            FileWriter fw =
            new FileWriter(
            new File(m_rootDir + File.separator + FileAlignerConstants.SOURCE_LIST_FNAME));
            PrintWriter pw = new PrintWriter(fw);
            
            int i = p_aligner.m_sourceFiles.size();
            Iterator srcIterator = p_aligner.m_sourceFiles.iterator();
            pw.print("Source files. Total: " + i + "\n\n");
            while (srcIterator.hasNext())
            {
                pw.println(srcIterator.next());
            }
            pw.close();
            m_summaryWriter.write(prepForHtml(
            "A total of "
            + i
            + " <a href=\""
            + FileAlignerConstants.SOURCE_LIST_FNAME
            + "\">source files</a> were located.\n"));
            
            // extra targets
            File f =
            new File(m_rootDir + File.separator + FileAlignerConstants.EXTRA_TRG_FNAME);
            if ((i = p_aligner.m_extraTrgFiles.size()) == 0)
            {
                // remove old file
                f.delete();
            }
            else
            {
                pw = new PrintWriter(new FileWriter(f));
                s =
                new StringBuffer(
                "\n\n" + m_msg.getString(MSG_EXTRA_FILES) + "Total: " + i + "\n\n");
                Iterator trgIterator = p_aligner.m_extraTrgFiles.iterator();
                while (trgIterator.hasNext())
                {
                    s.append(trgIterator.next() + "\n");
                }
                pw.print(s.toString());
                pw.close();
                m_summaryWriter.write(
                prepForHtml(
                "There were "
                + i
                + " <a href=\""
                + FileAlignerConstants.EXTRA_TRG_FNAME
                + "\">extra target files</a> that have no corresponding source file.\n"));
            }
            
            // missing targets
            Iterator trgIterator = p_aligner.m_missingTrgFiles.iterator();
            if (trgIterator.hasNext())
            {
                s =
                new StringBuffer(
                "\n<font color=\"red\">" + m_msg.getString(MSG_MISSING_FILES) + "\n");
                
                while (trgIterator.hasNext())
                {
                    s.append("<small>" + trgIterator.next() + "</small>\n");
                }
                s.append("</font>");
            }
            else
            {
                s = new StringBuffer("\n" + m_msg.getString(MSG_FILE_ALIGN_SUCCESS) + "\n");
            }
            m_summaryWriter.write(prepForHtml(s.toString()));
            
            m_summaryWriter.flush();
        }
        catch (IOException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Adds a System error message to the page.
     * @param p_errMsg - the error message
     */
    public void addSummarySysErr(String p_msg) throws AlignerException
    {
        writeSysError(p_msg, m_summaryWriter);
    }

    /**
     * Adds a Segment warning message to the page.
     */
    public void addSummaryWarningMsg(String p_msg) throws AlignerException
    {
        
    }

    /**
     * Writes an error message to the summary page.
     */
    public void addSummaryErrMsg(String p_msg) throws AlignerException
    {
        writeErrorMsg(p_msg, m_summaryWriter);
    }

    /**
     * Adds a Segment stats to the summary page
     */
    public void addSummaryGroupStats(AlignedPageBlocks p_pageBlocks)
    throws AlignerException
    {
        writePageStats(p_pageBlocks, m_summaryWriter);
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/18/2001 10:56:22 PM)
     */
    public void writeErrorMsg(String p_msg, FileWriter p_stream)
    throws AlignerException
    {
        String s;
        try
        {
            s = formatString(m_template.getString(TEMPLATE_GENERIC_ERROR), p_msg.trim());
            
            p_stream.write(prepForHtml(s));
            p_stream.flush();
        }
        catch (IOException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Adds a Segment stats message to the page.
     */
    public void writePageStats(AlignedPageBlocks p_pageBlocks, FileWriter p_writer)
    throws AlignerException
    {
        String s;
        PageBlocks trgPage = null;
        boolean hasError = true;
        int pageTotalSegmentCount = 0;
        int pageTotalMismatches = 0; // of any kind
        
        String grpBegin = "<spacer><small><b>";
        String grpEnd = "</small></b>";
        String errorBegin = "<small><font color=\"red\">";
        String errorEnd = "</font></small>";
        String srcBegin = "<small>";
        String srcEnd = "</small>";
        String trgBegin = "<small><font color=\"green\">";
        String trgEnd = "</font></small>";
        
        String srcSegBegin = "<small><pre>SOURCE: ";
        String srcSegEnd = "</small></pre>";
        String trgSegBegin = "<small><pre>TARGET: ";
        String trgSegEnd = "</small></pre>";
        
        
        if (p_pageBlocks == null)
        {
            return;
        }
        
        try
        {
            // get source page
            Iterator pageList = p_pageBlocks.getPageBlocks();
            PageBlocks srcPage = (PageBlocks) pageList.next();
            p_writer.write(
            prepForHtml(
            grpBegin
            + "\nAlignment #"
            + m_srcFileIdx++
            + "  -------------------------------------------------------------------------------------\n"
            + grpEnd));
            
            if (srcPage.wasExtracted())
            {
                String path = srcPage.getPath()    + File.separator + srcPage.getFileName();
                p_writer.write(
                prepForHtml(
                srcBegin
                + "Source File:\t"
                + "<a href=\"" + path + "\" TARGET=\"_blank\" >"
                + path
                + "</a>"
                + "\n"
                + srcEnd));
                p_writer.flush();
            }
            else
            {
                p_writer.write(
                prepForHtml(errorBegin + "Fatal: SOURCE file extraction error.\n" + errorEnd));
                p_writer.flush();
                return;
            }
            
            // Is there at least one target file ?
            if (!pageList.hasNext())
            {
                p_writer.write(
                prepForHtml(errorBegin + "Error: Zero target files.\n" + errorEnd));
                p_writer.flush();
                return;
            }
            
            // iterate over remaining target files
            while (pageList.hasNext())
            {
                trgPage = (PageBlocks) pageList.next();
                String pageReportRelativePath =
                FileAlignerConstants.REPORT_SUBDIR
                + File.separator
                + m_errFileIdx++
                + trgPage.getFileName();
                String pageReportPath = m_rootDir + File.separator + pageReportRelativePath;
            
                String path = trgPage.getPath()    + File.separator + trgPage.getFileName();    
                p_writer.write(
                prepForHtml(
                trgBegin
                + "Target File:\t"
                + "<a href=\"" + path + "\"" + " TARGET=\"_blank\" >"
                + path
                + "</a>"    + "\n"
                + trgEnd));
                
                if (!trgPage.wasExtracted())
                {
                    p_writer.write(
                    prepForHtml(
                    errorBegin + "Fatal: TARGET file extraction error.\n\n" + errorEnd));
                    continue;
                }
                
                // Page Percentages ============================
                boolean    makeLink = true;
                pageTotalSegmentCount = trgPage.getSegmentCount();
                pageTotalMismatches =
                trgPage.getMismatchedSegments() + trgPage.getSegmentsWithMismatchedTags();
                float pagePercentMismatch =
                ((float) pageTotalMismatches / (float) pageTotalSegmentCount) * (float) 100;
                
                if (Float.isNaN(pagePercentMismatch) || (pagePercentMismatch > (float) 0.0) )
                {
                    if(Float.isNaN(pagePercentMismatch))
                    {
                        pagePercentMismatch = (float) 100.0;
                        makeLink = false;
                        // NaN is 100% mismatch - since we do not have trg numbers use src numbers
                        m_reportTotalSegmentCount += srcPage.getSegmentCount();
                        m_reportTotalMismatches += srcPage.getSegmentCount();;
                    }
                    else
                    {
                        m_reportTotalSegmentCount += pageTotalSegmentCount;
                        m_reportTotalMismatches += pageTotalMismatches;
                    }
                    
                    s =    errorBegin
                    + "Percent Mismatch: "
                    + (makeLink ? "<a href=\"" : "")
                    + (makeLink ? pageReportRelativePath : "")
                    + (makeLink ? "\">" : "")
                    + pagePercentMismatch
                    + errorEnd
                    + (makeLink ? "</a>" : "" )
                    + "\n\n";
                }
                else
                {
                    s = trgBegin + "Percent Mismatch: " + pagePercentMismatch + "\n\n" + trgEnd;
                    hasError = false;
                }
                p_writer.write(prepForHtml(s));
                
                //==============================================
                if (!hasError) // no need to continue
                {
                    hasError = true;
                    continue;
                }
                
                
                // check at the block level
                if (!trgPage.isAligned())
                {
                    p_writer.write(
                    prepForHtml(
                    errorBegin
                    + "Entire page failed alignment (error at the &lt;localizable&gt; or &lt;translatable&gt; block level).\n\n"
                    + errorEnd));
                }
                else
                {
                    // check segment level
                    Iterator trgBlockList = trgPage.getBlockListIterator();
                    Iterator srcBlockList = srcPage.getBlockListIterator();
                    FileWriter fw = new FileWriter(new File(pageReportPath));
                    fw.write("<html>");
                    fw.write(
                    prepForHtml(
                    trgPage.getPath() + File.separator + trgPage.getFileName() + "\n\n"));
                    
                    while (trgBlockList.hasNext())
                    {
                        Block trgBlock = (Block) trgBlockList.next();
                        Block srcBlock = (Block) srcBlockList.next();
                        
                        if (!trgBlock.areSegmentsAligned())
                        {
                            s =
                            errorBegin
                            + "<spacer>Block failure (segments misaligned within &lt;localizable&gt; or &lt;translatable&gt; block).\n"
                            + "Block seg-count: "
                            + trgBlock.getSegmentCount()
                            + "\n"
                            + "Block seg-mismatches: "
                            + trgBlock.getSegmentMismatches()
                            + "\n"
                            + errorEnd;
                            fw.write(prepForHtml(s));
                            
                            // dump segments to report page
                            Iterator srcSegList = srcBlock.getSegmentListIterator();
                            while (srcSegList.hasNext())
                            {
                                Segment srcSeg = (Segment) srcSegList.next();
                                fw.write(srcSegBegin + srcSeg.getSegment() + srcSegEnd);
                            }
                            Iterator trgSegList = trgBlock.getSegmentListIterator();
                            while (trgSegList.hasNext())
                            {
                                Segment trgSeg = (Segment) trgSegList.next();
                                fw.write(trgSegBegin + trgSeg.getSegment() + trgSegEnd);
                            }
                        }
                        else
                        {
                            // check tag level
                            Iterator trgSegList = trgBlock.getSegmentListIterator();
                            Iterator srcSegList = srcBlock.getSegmentListIterator();
                            
                            while (trgSegList.hasNext())
                            {
                                Segment trgSeg = (Segment) trgSegList.next();
                                Segment srcSeg = (Segment) srcSegList.next();
                                
                                if (!trgSeg.areTagsAligned())
                                {
                                    s =
                                    errorBegin
                                    + "<spacer>Segment failure (at the tag level within a segment).\n"
                                    + errorEnd
                                    + srcSegBegin
                                    + srcSeg.getSegment()
                                    + "\n"
                                    + srcSegEnd
                                    + trgSegBegin
                                    + trgSeg.getSegment()
                                    + "\n"
                                    + trgSegEnd;
                                    fw.write(prepForHtml(s));
                                }
                                else
                                {
                                    // no-op
                                    
                                }
                            }
                            
                        }
                    }
                    fw.write("</html>");
                    fw.flush();
                    fw.close();
                }
                
                p_writer.flush();
            }
        }
        catch (IOException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
        
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/18/2001 11:14:21 PM)
     */
    public void writeSysError(String p_msg, FileWriter p_stream)
    throws AlignerException
    {
        String s;
        
        try
        {
            s = formatString(m_template.getString(TEMPLATE_SYSTEM_ERROR), p_msg.trim());
            
            m_summaryWriter.write(prepForHtml(s));
            m_summaryWriter.flush();
        }
        catch (IOException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/18/2001 11:32:10 PM)
     */
    public void writeWarningMsg(String p_msg, FileWriter p_stream)
    throws AlignerException
    {
        String s;
        
        try
        {
            s = formatString(m_template.getString(TEMPLATE_GENERIC_WARN), p_msg.trim());
            
            p_stream.write(prepForHtml(s));
            p_stream.flush();
        }
        catch (IOException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Close/Write summary page
     *
     */
    public void closeSummary() throws AlignerException
    {
        try
        {
            
            // Page Percentages ============================
            
            float pagePercentMismatch =
            (float) m_reportTotalMismatches / (float) m_reportTotalSegmentCount;
            m_summaryWriter.write(
            prepForHtml(
            "\n\n==============================\n"
            + "Total Percent Mismatch: "
            + (pagePercentMismatch * (float) 100)
            + "\n"
            + "==============================\n\n"));
            //==============================================
            
            // end page
            m_summaryWriter.write(
            formatString(
            m_template.getString(TEMPLATE_SUMMARY_PAGE_END),
            m_msg.getString(MSG_REPORT_END)));
            m_summaryWriter.close();
        }
        catch (Exception e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Close/Write summary page
     *
     */
    public void closeAnalysisSummary() throws AlignerException
    {
        try
        {
            // end page
            m_summaryWriter.write(
            formatString(
            m_template.getString(TEMPLATE_SUMMARY_PAGE_END),
            m_msg.getString(MSG_REPORT_END)));
            m_summaryWriter.close();
        }
        catch (Exception e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }

    /**
     * Writes an notificaation message to the summary page.
     */
    public void addSummaryNotificationMsg(String p_msg) throws AlignerException
    {
        writeNotificationMsg(p_msg, m_summaryWriter);
    }

    /**
     * Insert the method's description here.
     * Creation date: (2/18/2001 11:32:10 PM)
     */
    public void writeNotificationMsg(String p_msg, FileWriter p_stream)
    throws AlignerException
    {
        String s;
        
        try
        {
            s = formatString(m_template.getString(TEMPLATE_NOTIFICATION), p_msg.trim());
            
            p_stream.write(prepForHtml(s));
            p_stream.flush();
        }
        catch (IOException e)
        {
            throw new AlignerException(
            AlignerExceptionConstants.REPORT_WRITER_ERROR,
            e.toString());
        }
    }
}