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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.IntHolder;

public class WordCountProcessor implements ReportsProcessor
{
    private static Logger s_logger = Logger
            .getLogger(REPORTS);

    private WritableWorkbook m_workbook = null;

    private File tempFile = null;

    private OutputStream os = null;

    private boolean useInContext = false;

    private boolean useDefaultContext = false;

    private int colInc = 0;

    public WordCountProcessor()
    {
        s_logger.debug("Word Count Report..");
        initXlsFile();
    }

    /*
     * @see ReportsProcessor#generateReport(HttpServletRequest,
     *      HttpServletResponse)
     */
    public void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (!UserUtil.isBlank(companyName))
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        WorkbookSettings settings = new WorkbookSettings();
        // settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(tempFile, settings);
        addJobs(p_request);
        m_workbook.write();
        m_workbook.close();

        initOs(p_response);
        transferXls2Csv();
        release();
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @throws Exception
     */
    private void addJobs(HttpServletRequest p_request) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        WritableSheet sheet = m_workbook.createSheet(bundle.getString("lb_job_status"), 0);

        String[] jobIds = p_request.getParameterValues(PARAM_JOB_ID);
        List<Job> jobList = new ArrayList<Job>();

        if (jobIds != null && PARAM_SELECTED_ALL.equals(jobIds[0]))
        {
            // search jobs based on the params
            jobList.addAll(ServerProxy.getJobHandler().getJobs(
                    getSearchParams(p_request)));
            // sort jobs by job name
            Collections.sort(jobList, new JobComparator(Locale.US));
        }
        else
        {
            // just get the chosen jobs
            for (int i = 0; i < jobIds.length; i++)
            {
                Job j = ServerProxy.getJobHandler().getJobById(
                        Long.parseLong(jobIds[i]));
                jobList.add(j);

            }
        }

        String[] trgLocales = p_request
                .getParameterValues(PARAM_TARGET_LOCALES_LIST);
        boolean wantsAllLocales = false;
        Set<String> trgLocaleList = new HashSet<String>();
        if (trgLocales != null && !PARAM_SELECTED_ALL.equals(trgLocales[0]))
        {
            for (int i = 0; i < trgLocales.length; i++)
            {
                trgLocaleList.add(trgLocales[i]);
            }
        }
        else
        {
            wantsAllLocales = true;
        }

        getUseInContextInfos(jobList, wantsAllLocales, trgLocaleList);

        addHeader(sheet, bundle);

        IntHolder row = new IntHolder(2);
        for (Job j : jobList)
        {
            for (Workflow w : j.getWorkflows())
            {
                String state = w.getState();

                // skip certain workflow whose target locale is not selected
                String trgLocale = w.getTargetLocale().toString();
                if (!wantsAllLocales && !trgLocaleList.contains(trgLocale))
                {
                    continue;
                }
                if (Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state))
                {
                    addWorkflow(p_request, sheet, j, w, row);
                }
            }
        }
    }

    private void getUseInContextInfos(List<Job> jobs, boolean wantsAllLocales,
            Set<String> trgLocales)
    {
        for (Job j : jobs)
        {
            for (Workflow wf : j.getWorkflows())
            {
                String state = wf.getState();

                // skip certain workflow whose target locale is not selected
                String trgLocale = wf.getTargetLocale().toString();
                if (!wantsAllLocales && !trgLocales.contains(trgLocale))
                {
                    continue;
                }
                if (Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state))
                {
                    for (TargetPage tp : wf.getTargetPages())
                    {
                        // boolean isUseInContext = tp.getSourcePage()
                        // .getRequest().getJob().getL10nProfile()
                        // .getTranslationMemoryProfile()
                        // .getIsContextMatchLeveraging();
                        boolean isInContextMatch = PageHandler
                                .isInContextMatch(tp.getSourcePage()
                                        .getRequest().getJob());
                        boolean isDefaultContextMatch = PageHandler
                                .isDefaultContextMatch(tp);
                        if (isInContextMatch)
                        {
                            useInContext = true;
                            colInc = 4;
                            // return;
                        }
                        if (isDefaultContextMatch)
                        {
                            useDefaultContext = true;
                            colInc = 4;
                        }
                        if (useInContext && useDefaultContext)
                        {
                            colInc = 4;
                        }
                        if(!useInContext && !useDefaultContext)
                        {
                            colInc = 4;
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the task for the workflow and outputs page information.
     * 
     * @param p_request
     * @param p_sheet
     * @param p_job
     * @param p_workflow
     * @param p_row
     * 
     * @throws Exception
     */
    private void addWorkflow(HttpServletRequest p_request,
            WritableSheet p_sheet, Job p_job, Workflow p_workflow,
            IntHolder p_row) throws Exception
    {
        for (TargetPage tg : p_workflow.getTargetPages())
        {
            int allWords = 0;
            int allChars = 0;
            try
            {
                initEveryRow(p_sheet, p_row);
                allWords = addWordCountAndPercent(tg, p_row, p_sheet);
                allChars = addCharacters(tg, p_row, p_sheet);
                addCharDivWord(tg, p_row, p_sheet, allWords, allChars);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            p_row.inc();
        }

    }

    public int addCharacters(TargetPage tg, IntHolder p_row,
            WritableSheet p_sheet) throws Exception
    {
        List targetTuvs = getPageTuvs(tg);
        Tuv targetTuv = null;
        int allCharacters = 0;
        String targetSegmentString = "";

        for (int j = 0; j < targetTuvs.size(); j++)
        {
            targetTuv = (Tuv) targetTuvs.get(j);
            targetSegmentString = targetTuv.getGxmlElement().getTextValue();
            allCharacters += targetSegmentString.length();
        }
        int offset = 0;
        if(! useInContext && ! useDefaultContext)
        {
            offset = -8;
        }
        else if(! useInContext || ! useDefaultContext)
        {
            offset = -4;
        }
        p_sheet.addCell(new Label(ExcelColRow.AM + colInc + offset, p_row.value, ""
                + allCharacters));

        return allCharacters;
    }

    public void addCharDivWord(TargetPage tg, IntHolder p_row,
            WritableSheet p_sheet, int AllWords, int AllChars) throws Exception
    {
        double perc = 0.00;
        if (AllWords != 0)
        {
            perc = (AllChars * 100 / AllWords) / 100.0;
        }
        p_sheet.addCell(new Label(ExcelColRow.C, p_row.value, "" + perc));
    }

    public int addWordCountAndPercent(TargetPage tg, IntHolder p_row,
            WritableSheet p_sheet) throws Exception
    {
        // boolean rowUseInContext = tg.getSourcePage().getRequest().getJob()
        // .getL10nProfile().getTranslationMemoryProfile()
        // .getIsContextMatchLeveraging();
        boolean isInContextMatch = PageHandler.isInContextMatch(tg
                .getSourcePage().getRequest().getJob());
        boolean isDefaultContextMatch = PageHandler.isDefaultContextMatch(tg
                .getSourcePage().getRequest().getJob());
        int segmentTmWordCount = 0;// 100% match
        int inContextWordCount = 0;// in context word match
        boolean isUseDefaultContextMatch = PageHandler
                .isDefaultContextMatch(tg);
        int contextMatchWC = tg.getWordCount().getContextMatchWordCount();
        if (isInContextMatch)
        {
            // 100% match
            segmentTmWordCount = tg.getWordCount().getSegmentTmWordCount();
            // in context word match
            inContextWordCount = tg.getWordCount().getInContextWordCount();
            contextMatchWC = 0;
        }
        else
        {
            if (isUseDefaultContextMatch)
            {
                segmentTmWordCount = tg.getWordCount()
                        .getTotalExactMatchWordCount() - contextMatchWC;
            }
            else
            {
                // 100% match
                segmentTmWordCount = 
                        tg.getWordCount().getTotalExactMatchWordCount();
                contextMatchWC = 0;
            }
        }
        int hiFuzzyWordCount = tg.getWordCount().getHiFuzzyWordCount();// 95%
                                                                        // match
        int medHiFuzzyWordCount = tg.getWordCount().getMedHiFuzzyWordCount();// 85%
                                                                                // match
        int medFuzzyWordCount = tg.getWordCount().getMedFuzzyWordCount();// 75%
                                                                            // match
        int lowFuzzyWordCount = tg.getWordCount().getLowFuzzyWordCount();// 50%
                                                                            // match
        int unmatchedWordCount = tg.getWordCount().getUnmatchedWordCount();// no
                                                                            // match
        int lb_repetition_word_cnt = tg.getWordCount().getRepetitionWordCount();

        int totalWords = segmentTmWordCount + hiFuzzyWordCount
                + medHiFuzzyWordCount + medFuzzyWordCount + lowFuzzyWordCount
                + unmatchedWordCount + lb_repetition_word_cnt;

        if (isInContextMatch)
        {
            totalWords += inContextWordCount;
        }
        if (isDefaultContextMatch)
        {
            totalWords += contextMatchWC;
        }

        int pc_segmentTmWordCount = (int) ((segmentTmWordCount * 100.0) / totalWords); // 100%
                                                                                        // match
                                                                                        // percentage
        int pc_inContextWordCount = 0;
        if (isInContextMatch)
        {
            pc_inContextWordCount = (int) ((inContextWordCount * 100.0) / totalWords); // in
                                                                                        // context
                                                                                        // word
                                                                                        // percentage
        }
        int pc_hiFuzzyWordCount = (int) ((hiFuzzyWordCount * 100.0) / totalWords); // 95%
                                                                                    // match
                                                                                    // percentage
        int pc_medHiFuzzyWordCount = (int) ((medHiFuzzyWordCount * 100.0) / totalWords);// 85%
                                                                                        // match
                                                                                        // percentage
        int pc_medFuzzyWordCount = (int) ((medFuzzyWordCount * 100.0) / totalWords);// 75%
                                                                                    // match
                                                                                    // percentage
        int pc_lowFuzzyWordCount = (int) ((lowFuzzyWordCount * 100.0) / totalWords);// 50%
                                                                                    // match
                                                                                    // percentage
        int pc_lb_context_tm = 0;
        if (isDefaultContextMatch)
        {
            pc_lb_context_tm = (int) ((contextMatchWC * 100.0) / totalWords);// no
                                                                            // match
                                                                            // percentage
        }

        int pc_lb_repetition_word_cnt = (int) ((lb_repetition_word_cnt * 100.0) / totalWords);
        int pc_unmatchedWordCount = 100 - pc_segmentTmWordCount
                - pc_hiFuzzyWordCount - pc_medHiFuzzyWordCount
                - pc_medFuzzyWordCount - pc_lowFuzzyWordCount
                - pc_lb_repetition_word_cnt;

        if (isInContextMatch)
        {
            pc_unmatchedWordCount -= pc_inContextWordCount;
        }
        if (isDefaultContextMatch)
        {
            pc_unmatchedWordCount -= pc_lb_context_tm;
        }
        String fileName = tg.getSourcePage().getExternalPageId();

        // write the information of word count
        p_sheet.addCell(new Label(ExcelColRow.A, p_row.value, fileName));
        int offset = 0;
        if (useDefaultContext)
        {
            if (isDefaultContextMatch)
            {
                p_sheet.addCell(new Label(ExcelColRow.E, p_row.value, ""
                        + contextMatchWC));
            }
            else
            {
                p_sheet.addCell(new Label(ExcelColRow.D, p_row.value, "N/A"));
                p_sheet.addCell(new Label(ExcelColRow.E, p_row.value, "N/A"));
            }
        }
        else
        {
            offset = -4;
        }

        p_sheet.addCell(new Label(ExcelColRow.I + offset, p_row.value, ""
                + lb_repetition_word_cnt));
        p_sheet.addCell(new Label(ExcelColRow.M + offset, p_row.value, ""
                + segmentTmWordCount));
        p_sheet.addCell(new Label(ExcelColRow.K + offset, p_row.value, ""
                + pc_lb_repetition_word_cnt));
        p_sheet.addCell(new Label(ExcelColRow.O + offset, p_row.value, ""
                + pc_segmentTmWordCount));

        if (useInContext)
        {
            if (isInContextMatch)
            {
                p_sheet.addCell(new Label(ExcelColRow.M + colInc + offset, p_row.value,
                        "" + inContextWordCount));
            }
            else
            {
                p_sheet.addCell(new Label(ExcelColRow.L + colInc + offset, p_row.value,
                        "N/A"));
                p_sheet.addCell(new Label(ExcelColRow.M + colInc + offset, p_row.value,
                        "N/A"));
            }
        }
        else
        {
            offset -= 4;
        }
        p_sheet.addCell(new Label(ExcelColRow.Q + colInc + offset, p_row.value, ""
                + hiFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.U + colInc + offset, p_row.value, ""
                + medHiFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.Y + colInc + offset, p_row.value, ""
                + medFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.AC + colInc + offset, p_row.value, ""
                + lowFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.AG + colInc + offset, p_row.value, ""
                + unmatchedWordCount));
        p_sheet.addCell(new Label(ExcelColRow.AK + colInc + offset, p_row.value, ""
                + totalWords));

        // write the infomation of word count percentage
        if (useDefaultContext)
        {
            if (isDefaultContextMatch)
            {
                p_sheet.addCell(new Label(ExcelColRow.G, p_row.value, ""
                        + pc_lb_context_tm));
            }
            else
            {
                p_sheet.addCell(new Label(ExcelColRow.F, p_row.value, "N/A"));
                p_sheet.addCell(new Label(ExcelColRow.G, p_row.value, "N/A"));
            }
        }

        if (useInContext)
        {
            if (isInContextMatch)
            {
                p_sheet.addCell(new Label(ExcelColRow.O + colInc + offset, p_row.value,
                        "" + pc_inContextWordCount));
            }
            else
            {
                p_sheet.addCell(new Label(ExcelColRow.N + colInc + offset, p_row.value,
                        "N/A"));
                p_sheet.addCell(new Label(ExcelColRow.O + colInc + offset, p_row.value,
                        "N/A"));
            }
        }
        p_sheet.addCell(new Label(ExcelColRow.S + colInc + offset, p_row.value, ""
                + pc_hiFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.W + colInc + offset, p_row.value, ""
                + pc_medHiFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.AA + colInc + offset, p_row.value, ""
                + pc_medFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.AE + colInc + offset, p_row.value, ""
                + pc_lowFuzzyWordCount));
        p_sheet.addCell(new Label(ExcelColRow.AI + colInc + offset, p_row.value, ""
                + pc_unmatchedWordCount));

        return totalWords;

    }

    /**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
    {
        JobSearchParameters sp = new JobSearchParameters();

        String[] status = p_request.getParameterValues(PARAM_STATUS);
        // search by statusList
        List statusList = new ArrayList();
        if (status != null && !PARAM_SELECTED_ALL.equals(status[0]))
        {
            for (int i = 0; i < status.length; i++)
            {
                statusList.add(status[i]);
            }
        }
        else
        {
            // just do a query for all ready, in progress, localized, exported,
            // archived and export failed jobs.
            statusList.add(Job.READY_TO_BE_DISPATCHED);
            statusList.add(Job.DISPATCHED);
            statusList.add(Job.LOCALIZED);
            statusList.add(Job.EXPORTED);
            statusList.add(Job.EXPORT_FAIL);
            statusList.add(Job.ARCHIVED);
        }
        sp.setJobState(statusList);

        String[] projectIds = p_request.getParameterValues(PARAM_PROJECT_ID);
        // search by project
        List projectIdList = new ArrayList();

        if (projectIds != null && !PARAM_SELECTED_ALL.equals(projectIds[0]))
        {
            for (int i = 0; i < projectIds.length; i++)
            {
                projectIdList.add(new Long(projectIds[i]));
            }
            sp.setProjectId(projectIdList);
        }

        String createDateStartCount = p_request
                .getParameter(PARAM_CREATION_START);
        String createDateStartOpts = p_request
                .getParameter(PARAM_CREATION_START_OPTIONS);

        if (!PARAM_SELECTED_NONE.equals(createDateStartOpts))
        {
            sp.setCreationStart(new Integer(createDateStartCount));
            sp.setCreationStartCondition(createDateStartOpts);
        }

        String createDateEndCount = p_request.getParameter(PARAM_CREATION_END);
        String createDateEndOpts = p_request
                .getParameter(PARAM_CREATION_END_OPTIONS);

        if (SearchCriteriaParameters.NOW.equals(createDateEndOpts))
        {
            sp.setCreationEnd(new Date());
        }
        else if (!PARAM_SELECTED_NONE.equals(createDateEndOpts))
        {
            sp.setCreationEnd(new Integer(createDateEndCount));
            sp.setCreationEndCondition(createDateEndOpts);
        }

        return sp;
    }

    private void initEveryRow(WritableSheet p_sheet, IntHolder p_row)
            throws Exception
    {
        int endCol = ExcelColRow.AM ;
//        + (useInContext ? 4 : 0);
        if(! useInContext && ! useDefaultContext)
        {
            endCol = ExcelColRow.AM - 4;
        }
        for (int i = ExcelColRow.A; i <= endCol; i++)
        {
            p_sheet.addCell(new Label(i, p_row.value, "0"));
        }
    }

    private void addHeader(WritableSheet p_sheet, ResourceBundle bundle) throws Exception
    {
        int offset = 0;
        if (useDefaultContext)
        {
            p_sheet.addCell(new Label(ExcelColRow.D, 0, bundle.getString("lb_context_tm")));
        }
        else
        {
            offset = -4;
        }
        p_sheet.addCell(new Label(ExcelColRow.H + offset, 0, bundle.getString("lb_repetition_word_cnt")));
        p_sheet.addCell(new Label(ExcelColRow.L + offset, 0, bundle.getString("lb_100_match")));
        if (useInContext)
        {
            p_sheet.addCell(new Label(ExcelColRow.L + colInc + offset, 0,
                    bundle.getString("lb_in_context_match")));
        }
        else
        {
            offset -= 4;
        }
        p_sheet
                .addCell(new Label(ExcelColRow.P + colInc + offset, 0,
                        bundle.getString("lb_95")));
        p_sheet
                .addCell(new Label(ExcelColRow.T + colInc + offset, 0,
                        bundle.getString("lb_85")));
        p_sheet
                .addCell(new Label(ExcelColRow.X + colInc + offset, 0,
                        bundle.getString("lb_75")));
        p_sheet.addCell(new Label(ExcelColRow.AB + colInc + offset, 0,
                bundle.getString("lb_50")));
        p_sheet.addCell(new Label(ExcelColRow.AF + colInc + offset, 0,
                bundle.getString("lb_no_match")));
        p_sheet.addCell(new Label(ExcelColRow.AJ + colInc + offset, 0, bundle
                .getString("lb_total")));

        p_sheet.addCell(new Label(ExcelColRow.A, 1, bundle.getString("lb_file")));
        p_sheet.addCell(new Label(ExcelColRow.B, 1, bundle.getString("lb_tagging_errors")));
        p_sheet.addCell(new Label(ExcelColRow.C, 1, bundle.getString("lb_chars_word")));

        int rowNumber = ExcelColRow.D;
        int count = 10;
        if(useDefaultContext && useInContext)
        {
            count = 10;
        }
        else if(useDefaultContext || useInContext)
        {
            count = 9;
        }
        else
        {
            count = 8;
        }
        for(int i = 0; i < count; i++)
        {
            p_sheet.addCell(new Label(ExcelColRow.D + i * 4, 1, bundle.getString("lb_segments")));
            p_sheet.addCell(new Label(ExcelColRow.E + i * 4, 1, bundle.getString("lb_words")));
            p_sheet.addCell(new Label(ExcelColRow.F + i * 4, 1, bundle.getString("lb_placeables")));
            if(i == count - 1)
            {
                p_sheet.addCell(new Label(ExcelColRow.G + i * 4, 1, bundle.getString("lb_characters")));
            }
            else
            {
                p_sheet.addCell(new Label(ExcelColRow.G + i * 4, 1, bundle.getString("lb_percent")));
            }
        }
    }

    private void initXlsFile()
    {
        tempFile = new File("temp" + System.currentTimeMillis() + ".xls");
        if (tempFile.exists())
        {
            tempFile.delete();
        }
        else
        {
            try
            {
                tempFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void initOs(HttpServletResponse p_response)
    {
        try
        {
            os = p_response.getOutputStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void release()
    {
        if (tempFile.exists())
        {
            tempFile.delete();
        }
    }

    private List getPageTuvs(TargetPage p_targetPage) throws Exception
    {
        return new ArrayList(ServerProxy.getTuvManager()
                .getTargetTuvsForStatistics(p_targetPage));
    }

    private void transferXls2Csv()
    {
        try
        {
            String encoding = "UTF8";
            OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
            BufferedWriter bw = new BufferedWriter(osw);

            WorkbookSettings ws = new WorkbookSettings();
            Workbook w = Workbook.getWorkbook(tempFile, ws);

            // Gets the sheets from workbook
            for (int sheet = 0; sheet < w.getNumberOfSheets(); sheet++)
            {
                Sheet s = w.getSheet(sheet);

                Cell[] row = null;

                // Gets the cells from sheet
                for (int i = 0; i < s.getRows(); i++)
                {
                    row = s.getRow(i);

                    if (row.length > 0)
                    {
                        bw.write(row[0].getContents());
                        for (int j = 1; j < row.length; j++)
                        {
                            bw.write(',');
                            bw.write(row[j].getContents());
                        }
                    }
                    bw.newLine();
                }
            }
            bw.flush();
            bw.close();

        }
        catch (UnsupportedEncodingException ue)
        {
            System.err.println(ue.toString());
        }
        catch (IOException ioe)
        {
            System.err.println(ioe.toString());
        }
        catch (Exception e)
        {
            System.err.println(e.toString());
        }

    }

    static class ExcelColRow
    {
        private static int i = 0;

        static final int A = i++;
        static final int B = i++;
        static final int C = i++;
        static final int D = i++;
        static final int E = i++;
        static final int F = i++;
        static final int G = i++;
        static final int H = i++;
        static final int I = i++;
        static final int J = i++;
        static final int K = i++;
        static final int L = i++;
        static final int M = i++;
        static final int N = i++;
        static final int O = i++;
        static final int P = i++;
        static final int Q = i++;
        static final int R = i++;
        static final int S = i++;
        static final int T = i++;
        static final int U = i++;
        static final int V = i++;
        static final int W = i++;
        static final int X = i++;
        static final int Y = i++;
        static final int Z = i++;
        static final int AA = i++;
        static final int AB = i++;
        static final int AC = i++;
        static final int AD = i++;
        static final int AE = i++;
        static final int AF = i++;
        static final int AG = i++;
        static final int AH = i++;
        static final int AI = i++;
        static final int AJ = i++;
        static final int AK = i++;
        static final int AL = i++;
        static final int AM = i++;
        static final int AN = i++;
        static final int AO = i++;
        static final int AP = i++;
        static final int AQ = i++;
        static final int AR = i++;
        static final int AS = i++;
        static final int AT = i++;
        static final int AU = i++;
        static final int AV = i++;
        static final int AW = i++;
        static final int AX = i++;
        static final int AY = i++;
        static final int AZ = i++;
    }
}
