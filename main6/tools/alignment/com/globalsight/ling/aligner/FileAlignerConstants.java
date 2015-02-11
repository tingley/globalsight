package com.globalsight.ling.aligner;

/**
 * Insert the type's description here.
 */
public interface FileAlignerConstants
{
    public static final String SUMMARY_FNAME = "AlignmentSummary.html";
    public static final String SOURCE_LIST_FNAME = "SourceListing.txt";
    public static final String EXTRA_TRG_FNAME = "ExtraTargetFiles.txt";
    public static final String REPORT_SUBDIR = "AlignerErrors";

    // alignment levels
    public static final int UNKNOWN_MODE         = 0;
    public static final int DIR_MODE             = 1;
    public static final int ANALYSIS_MODE        = 2;
    public static final int UPDATE_MODE          = 3;    
    public static final int UPDATE_WRITE_CONFIG  = 4;    
}