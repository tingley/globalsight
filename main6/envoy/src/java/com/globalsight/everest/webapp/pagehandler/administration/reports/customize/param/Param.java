/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param;

public interface Param {
    
    /**
     * Dummy param, root of the param tree.
     */
    static public final String ROOT = "jobinfo";

    /**
     * Static JobInfo Parameter definitions -- see below for mapping
     */
    static public final String JOB_ID = "jobinfo.jobid";
    static public final String JOB_DETAIL = "jobinfo.jobdetail";
    static public final String JOB_NAME = "jobinfo.jobdetail.jobname";
    static public final String PROJECT_DESCRIPTION = "jobinfo.jobdetail.projectdescription";
    static public final String JOB_CREATIONDATE = "jobinfo.jobdetail.jobcreationdate";
    static public final String PAGE_COUNT = "jobinfo.jobdetail.pagecount";
    static public final String JOB_PRIORITY = "jobinfo.jobdetail.jobpriority";
    static public final String JOB_PROJECT_NAME = "jobinfo.jobdetail.jobprojectname";
    static public final String JOB_PROJECT_MANAGER = "jobinfo.jobdetail.jobprojectmanager";
    static public final String JOB_SOURCE_LOCALE = "jobinfo.jobdetail.jobsourcelocale";
    static public final String JOB_TARGET_LOCALE = "jobinfo.jobdetail.jobtargetlocale";
    static public final String ESTIMATED_COST = "jobinfo.jobdetail.estimatedcost";
    static public final String ESTIMATED_BILLING_CHARGES = "jobinfo.jobdetail.estimatedbillingcharges";
    static public final String LOCALIZATION_PROFILE = "jobinfo.jobdetail.localizationprofile";
    static public final String FILE_PROFILE = "jobinfo.jobdetail.fileprofile";
    static public final String STATUS = "jobinfo.status";
    static public final String JOB_STATUS = "jobinfo.status.jobstatus";
    static public final String WORKFLOW_STATUS = "jobinfo.status.workflowstatus";
    static public final String ESTIMATED_JOB_COMPLETION = "jobinfo.status.estimatedjobcompletion";
    static public final String ACTUAL_JOB_COMPLETION = "jobinfo.status.actualjobcompletion";
    static public final String TM_MATCHES = "jobinfo.tmmatches";
    static public final String TM_MATCHES_WORD_COUNTS = "jobinfo.tmmatches.wordcounts";
    static public final String TM_MATCHES_WORD_COUNTS_INTERNAL_REPS = "jobinfo.tmmatches.wordcounts.internalreps";
    static public final String TM_MATCHES_WORD_COUNTS_EXACT_MATCHES = "jobinfo.tmmatches.wordcounts.exactmatches";
    static public final String TM_MATCHES_WORD_COUNTS_IN_CONTEXT_MATCHES = "jobinfo.tmmatches.wordcounts.incontextmatches";
    static public final String TM_MATCHES_WORD_COUNTS_FUZZY_MATCHES = "jobinfo.tmmatches.wordcounts.fuzzymatches";
    static public final String TM_MATCHES_WORD_COUNTS_NEW_WORDS = "jobinfo.tmmatches.wordcounts.newwords";
    static public final String TM_MATCHES_WORD_COUNTS_TOTAL = "jobinfo.tmmatches.wordcounts.total";
    static public final String TM_MATCHES_INVOICE = "jobinfo.tmmatches.invoice";
    static public final String TM_MATCHES_INVOICE_INTERNAL_REPS = "jobinfo.tmmatches.invoice.internalreps";
    static public final String TM_MATCHES_INVOICE_EXACT_MATCHES = "jobinfo.tmmatches.invoice.exactmatches";
    static public final String TM_MATCHES_INVOICE_IN_CONTEXT_MATCHES = "jobinfo.tmmatches.invoice.incontextmatches";
    static public final String TM_MATCHES_INVOICE_FUZZY_MATCHES = "jobinfo.tmmatches.invoice.fuzzymatches";
    static public final String TM_MATCHES_INVOICE_NEW_WORDS = "jobinfo.tmmatches.invoice.newwords";
    static public final String TM_MATCHES_INVOICE_JOB_TOTAL = "jobinfo.tmmatches.invoice.jobtotal";
    static public final String TRADOS_MATCHES = "jobinfo.tradosmatches";
    static public final String TRADOS_MATCHES_WORD_COUNTS = "jobinfo.tradosmatches.wordcounts";
    static public final String TRADOS_MATCHES_WORD_COUNTS_PER100 = "jobinfo.tradosmatches.wordcounts.per100matches";
    static public final String TRADOS_MATCHES_WORD_COUNTS_PERINCONTEXT = "jobinfo.tradosmatches.wordcounts.perincontextmatches";
    static public final String TRADOS_MATCHES_WORD_COUNTS_PER95 = "jobinfo.tradosmatches.wordcounts.per95matches";
    static public final String TRADOS_MATCHES_WORD_COUNTS_PER85 = "jobinfo.tradosmatches.wordcounts.per85matches";
    static public final String TRADOS_MATCHES_WORD_COUNTS_PER75 = "jobinfo.tradosmatches.wordcounts.per75matches";
    static public final String TRADOS_MATCHES_WORD_COUNTS_PER50 = "jobinfo.tradosmatches.wordcounts.per50matches";
    static public final String TRADOS_MATCHES_WORD_COUNTS_NOMATCH = "jobinfo.tradosmatches.wordcounts.nomatch";
    static public final String TRADOS_MATCHES_WORD_COUNTS_REPETITION = "jobinfo.tradosmatches.wordcounts.repetition";
    static public final String TRADOS_MATCHES_WORD_COUNTS_TOTAL = "jobinfo.tradosmatches.wordcounts.total";
    static public final String TRADOS_MATCHES_INVOICE = "jobinfo.tradosmatches.invoice";
    static public final String TRADOS_MATCHES_INVOICE_PER100 = "jobinfo.tradosmatches.invoice.per100matches";
    static public final String TRADOS_MATCHES_INVOICE_PERINCONTEXT = "jobinfo.tradosmatches.invoice.perincontextmatches";
    static public final String TRADOS_MATCHES_INVOICE_PER95 = "jobinfo.tradosmatches.invoice.per95matches";
    static public final String TRADOS_MATCHES_INVOICE_PER85 = "jobinfo.tradosmatches.invoice.per85matches";
    static public final String TRADOS_MATCHES_INVOICE_PER75 = "jobinfo.tradosmatches.invoice.per75matches";
    static public final String TRADOS_MATCHES_INVOICE_PER50 = "jobinfo.tradosmatches.invoice.per50matches";
    static public final String TRADOS_MATCHES_INVOICE_NOMATCH = "jobinfo.tradosmatches.invoice.nomatch";
    static public final String TRADOS_MATCHES_INVOICE_REPETITION = "jobinfo.tradosmatches.invoice.repetition";
    static public final String TRADOS_MATCHES_INVOICE_JOB_TOTAL = "jobinfo.tradosmatches.invoice.jobtotal";
    static public final String SEGMENT_COMMENTS = "jobinfo.segmentcomments";
    static public final String SEGMENT_NUMBER = "jobinfo.segmentcomments.segmentnumber";
    static public final String BY_WHO = "jobinfo.segmentcomments.bywho";
    static public final String ON_DATE = "jobinfo.segmentcomments.ondate";
    static public final String COMMENT_HEADER = "jobinfo.segmentcomments.commentheader";
    static public final String COMMENT_BODY = "jobinfo.segmentcomments.commentbody";
    static public final String LINK = "jobinfo.segmentcomments.link";
    
    static public final String[] WHOLE_PARAMS = {
        JOB_ID,
        JOB_DETAIL,
        JOB_NAME,
        PROJECT_DESCRIPTION,
        JOB_CREATIONDATE,
        PAGE_COUNT,
        JOB_PRIORITY,
        JOB_PROJECT_NAME,
        JOB_PROJECT_MANAGER,
        JOB_SOURCE_LOCALE,
        JOB_TARGET_LOCALE,
        ESTIMATED_COST,
        ESTIMATED_BILLING_CHARGES,
        LOCALIZATION_PROFILE,
        FILE_PROFILE,
        STATUS,
        JOB_STATUS,
        WORKFLOW_STATUS,
        ESTIMATED_JOB_COMPLETION,
        ACTUAL_JOB_COMPLETION,
        TM_MATCHES,
        TM_MATCHES_WORD_COUNTS,
        TM_MATCHES_WORD_COUNTS_INTERNAL_REPS,
        TM_MATCHES_WORD_COUNTS_EXACT_MATCHES,
        TM_MATCHES_WORD_COUNTS_IN_CONTEXT_MATCHES,
        TM_MATCHES_WORD_COUNTS_FUZZY_MATCHES,
        TM_MATCHES_WORD_COUNTS_NEW_WORDS,
        TM_MATCHES_WORD_COUNTS_TOTAL,
        TM_MATCHES_INVOICE,
        TM_MATCHES_INVOICE_INTERNAL_REPS,
        TM_MATCHES_INVOICE_EXACT_MATCHES,
        TM_MATCHES_INVOICE_IN_CONTEXT_MATCHES,
        TM_MATCHES_INVOICE_FUZZY_MATCHES,
        TM_MATCHES_INVOICE_NEW_WORDS,
        TM_MATCHES_INVOICE_JOB_TOTAL,
        TRADOS_MATCHES,
        TRADOS_MATCHES_WORD_COUNTS,
        TRADOS_MATCHES_WORD_COUNTS_PER100,
        TRADOS_MATCHES_WORD_COUNTS_PER95,
        TRADOS_MATCHES_WORD_COUNTS_PER85,
        TRADOS_MATCHES_WORD_COUNTS_PER75,
        TRADOS_MATCHES_WORD_COUNTS_NOMATCH,
        TRADOS_MATCHES_WORD_COUNTS_REPETITION,
        TRADOS_MATCHES_WORD_COUNTS_PERINCONTEXT,
        TRADOS_MATCHES_WORD_COUNTS_TOTAL,
        TRADOS_MATCHES_INVOICE,
        TRADOS_MATCHES_INVOICE_PER100,
        TRADOS_MATCHES_INVOICE_PER95,
        TRADOS_MATCHES_INVOICE_PER85,
        TRADOS_MATCHES_INVOICE_PER75,
        TRADOS_MATCHES_INVOICE_NOMATCH,
        TRADOS_MATCHES_INVOICE_REPETITION,
        TRADOS_MATCHES_INVOICE_PERINCONTEXT,
        TRADOS_MATCHES_INVOICE_JOB_TOTAL,
        SEGMENT_COMMENTS,
        SEGMENT_NUMBER,
        BY_WHO,
        ON_DATE,
        COMMENT_HEADER,
        COMMENT_BODY,
        LINK
    };
    
    public String getName();
    
    public String getCompletedName();
    
    public boolean getValue();
    
    public boolean hasChildren();
    
    public Param[] getChildParams();
    
    public int childrenSize();
    
    public boolean hasSelectedChildren();
    
    public int selectedChildrenSize();
    
    public Param[] getSelectedChildren();
    
    public boolean equals(Param theOtherParam);
}
