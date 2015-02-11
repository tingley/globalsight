<%@ page contentType="application/vnd.ms-excel"
	errorPage="/envoy/common/error.jsp"
	import="jxl.*,jxl.write.*,jxl.format.*,com.globalsight.everest.servlet.util.ServerProxy,com.globalsight.everest.jobhandler.*,com.globalsight.everest.foundation.SearchCriteriaParameters,com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,com.globalsight.everest.workflowmanager.Workflow,com.globalsight.everest.jobhandler.Job,com.globalsight.everest.projecthandler.Project,com.globalsight.util.IntHolder,com.globalsight.everest.util.comparator.JobComparator,com.globalsight.everest.company.CompanyWrapper,com.globalsight.everest.page.TargetPage,java.text.SimpleDateFormat,jxl.write.Number,java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,java.util.ArrayList,java.util.Iterator,com.globalsight.log.GlobalSightCategory,com.globalsight.everest.company.CompanyThreadLocal,com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,com.globalsight.everest.usermgr.UserLdapHelper,com.globalsight.everest.webapp.WebAppConstants"
	session="true"%><%!private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger("Reports");
    private WritableWorkbook m_workbook = null;

    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yy hh:mm:ss a z");

    private boolean useInContext = false;
    private boolean useDefaultContext = false;

    private int wordCountCol = 0;

    private RequestData data = new RequestData();

    /**
     * Generates the Excel report and spits it to the outputstream
     * The report consists of all in progress workflows that are
     * currently at a reviewOnly stage.
     * 
     * @return File
     * @exception Exception
     */
    private void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(p_response.getOutputStream(),
                settings);
        initRequest(p_request);
        addJobs(p_request);
        addCriteriaSheet(p_request);
        m_workbook.write();
        m_workbook.close();
    }

    private void initRequest(HttpServletRequest p_request)
    {
        setProjectIdList(p_request);
        setTargetLocales(p_request);
        setStates(p_request);
        String dateFormatString = p_request.getParameter("dateFormat");
        if (dateFormatString != null && !dateFormatString.equals(""))
        {
            dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    private void setProjectIdList(HttpServletRequest p_request)
    {
        data.projectIdList.clear();
        data.wantsAllProjects = false;
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectId");

        for (int i = 0; i < projectIds.length; i++)
        {
            String id = projectIds[i];
            if (id.equals("*") == false)
            {
                data.projectIdList.add(new Long(id));
            }
            else
            {
                data.wantsAllProjects = true;
                break;
            }
        }
    }

    private void setTargetLocales(HttpServletRequest p_request)
    {
        data.trgLocaleList.clear();
        data.wantsAllLocales = false;
        String[] paramTrgLocales = p_request
                .getParameterValues("targetLocalesList");
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    data.wantsAllLocales = true;
                    break;
                }
                else
                {
                    data.trgLocaleList.add(paramTrgLocales[i]);
                }
            }
        }
    }

    private void setStates(HttpServletRequest p_request)
    {
        data.jobStateList.clear();
        data.workflowStateList.clear();
        String[] states = p_request.getParameterValues("states");

        List lst = new ArrayList();

        if (states != null)
        {
            lst = Arrays.asList(states);
        }

        if (lst.contains("ready"))
        {
            data.jobStateList.add(Job.READY_TO_BE_DISPATCHED);
            data.workflowStateList.add(Workflow.READY_TO_BE_DISPATCHED);
        }
        if (lst.contains("pending"))
        {
            data.jobStateList.add(Job.PENDING);
            data.workflowStateList.add(Workflow.PENDING);
        }

        if (lst.contains("progress"))
        {
            data.jobStateList.add(Job.DISPATCHED);
            data.workflowStateList.add(Workflow.DISPATCHED);
        }

        if (lst.contains("localized"))
        {
            data.jobStateList.add(Job.LOCALIZED);
            data.workflowStateList.add(Workflow.LOCALIZED);
        }

        if (lst.contains("exported"))
        {
            data.jobStateList.add(Job.EXPORTED);
            data.jobStateList.add(Job.EXPORT_FAIL);
            data.workflowStateList.add(Workflow.EXPORTED);
            data.workflowStateList.add(Workflow.EXPORT_FAILED);
        }

        if (lst.contains("archived"))
        {
            data.jobStateList.add(Job.ARCHIVED);
            data.workflowStateList.add(Workflow.ARCHIVED);
        }

    }

    private class RequestData
    {
        public boolean wantsAllProjects = false;

        boolean wantsAllLocales = false;

        Set<String> trgLocaleList = new HashSet<String>();

        List<Long> projectIdList = new ArrayList<Long>();

        List<String> jobStateList = new ArrayList<String>();

        List<String> workflowStateList = new ArrayList<String>();

    };

    private void addCriteriaSheet(HttpServletRequest p_request)
            throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        WritableSheet paramsSheet = m_workbook.createSheet(bundle
                .getString("lb_criteria"), 1);
        paramsSheet.addCell(new Label(0, 0, bundle
                .getString("lb_report_criteria")));
        paramsSheet.setColumnView(0, 50);

        if (data.wantsAllProjects)
        {
            paramsSheet.addCell(new Label(0, 1, bundle
                    .getString("lb_selected_projects")
                    + " " + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(0, 1, bundle
                    .getString("lb_selected_projects")));
            Iterator<Long> iter = data.projectIdList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                Long pid = (Long) iter.next();
                String projectName = "??";
                try
                {
                    Project p = ServerProxy.getProjectHandler().getProjectById(
                            pid.longValue());
                    projectName = p.getName();
                }
                catch (Exception e)
                {
                }
                paramsSheet.addCell(new Label(0, r, projectName));
                paramsSheet.addCell(new Label(1, r, bundle.getString("lb_id")
                        + "=" + pid.toString()));
                r++;
            }
        }

        String from = "";
        String to = "";
        String startCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String startOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if ("-1".equals(startOpts) == false)
        {
            from = startCount + " " + translateOpts(startOpts, bundle);
        }

        String endCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String endOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);

        if ("-1".equals(endOpts) == false)
        {
            to = endCount + " " + translateOpts(endOpts, bundle);
        }

        paramsSheet.addCell(new Label(2, 1, bundle.getString("lb_from") + ":"));
        paramsSheet.addCell(new Label(2, 2, from));
        paramsSheet
                .addCell(new Label(3, 1, bundle.getString("lb_until") + ":"));
        paramsSheet.addCell(new Label(3, 2, to));

        if (data.wantsAllLocales)
        {
            paramsSheet.addCell(new Label(4, 1, bundle
                    .getString("lb_selected_langs")
                    + " " + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(4, 1, bundle
                    .getString("lb_selected_langs")));
            Iterator<String> iter = data.trgLocaleList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String locale = (String) iter.next();
                paramsSheet.addCell(new Label(4, r, locale));
                r++;
            }
        }

    }

    private String translateOpts(String opt, ResourceBundle bundle)
    {
        String optLabel = "";
        if (SearchCriteriaParameters.NOW.equals(opt))
        {
            optLabel = bundle.getString("lb_now");
        }
        else if (SearchCriteriaParameters.HOURS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_hours_ago");
        }
        else if (SearchCriteriaParameters.DAYS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_days_ago");
        }
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_weeks_ago");
        }
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_months_ago");
        }

        return optLabel;
    }

    /**
     * Returns search params used to find the in progress
     * jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
            throws Exception
    {

        JobSearchParameters sp = new JobSearchParameters();

        sp.setJobState(data.jobStateList);

        if (data.wantsAllProjects == false)
        {
            sp.setProjectId(data.projectIdList);
        }

        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if ("-1".equals(paramCreateDateStartOpts) == false)
        {
            sp.setCreationStart(new Integer(paramCreateDateStartCount));
            sp.setCreationStartCondition(paramCreateDateStartOpts);
        }

        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String paramCreateDateEndOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
        if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new java.util.Date());
        }
        else if ("-1".equals(paramCreateDateEndOpts) == false)
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        return sp;
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet p_sheet, ResourceBundle bundle)
            throws Exception
    {
        // title font is black bold on white
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        p_sheet.addCell(new Label(0, 0, EMEA + " "
                + bundle.getString("lb_file_list"), titleFormat));
        p_sheet.addCell(new Label(0, 1, bundle.getString("lb_desp_file_list")));
        p_sheet.setColumnView(0, 20);

        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        WritableCellFormat langFormat = new WritableCellFormat(headerFont);
        langFormat.setWrap(true);
        langFormat.setBackground(jxl.format.Colour.GRAY_25);
        langFormat.setShrinkToFit(false);
        langFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountFormat = new WritableCellFormat(headerFont);
        wordCountFormat.setWrap(true);
        wordCountFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountFormat.setShrinkToFit(false);
        wordCountFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountValueFormat = new WritableCellFormat(
                headerFont);
        wordCountValueFormat.setWrap(true);
        wordCountValueFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueFormat.setShrinkToFit(false);
        wordCountValueFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        WritableCellFormat wordCountValueRightFormat = new WritableCellFormat(
                headerFont);
        wordCountValueRightFormat.setWrap(true);
        wordCountValueRightFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueRightFormat.setShrinkToFit(false);
        wordCountValueRightFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueRightFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueRightFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        int col = 0;

        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_job_id"),
                headerFormat));
        p_sheet.setColumnView(col, 15);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_job_name"),
                headerFormat));
        p_sheet.setColumnView(col, 30);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_file_path"),
                headerFormat));
        p_sheet.setColumnView(col, 55);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_file_name"),
                headerFormat));
        p_sheet.setColumnView(col, 25);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("reportDesc"),
                headerFormat));
        p_sheet.setColumnView(col, 30);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_creation_date"),
                headerFormat));
        p_sheet.setColumnView(col, 20);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_lang"),
                langFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_word_counts"),
                wordCountFormat));
        wordCountCol = col;
        int span = 6;
        if (useDefaultContext && useInContext)
        {
            span += 2;
        }
        else if (useDefaultContext || useInContext)
        {
            span += 1;
        }

        p_sheet.mergeCells(col, 2, col + span, 2);

        if (useDefaultContext)
        {
            p_sheet.addCell(new Label(col, 3,
                    bundle.getString("lb_context_tm_report"), wordCountValueFormat));
        }

        if (useInContext)
        {
            if (useDefaultContext)
                col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }

        if (useDefaultContext || useInContext)
            col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("jobinfo.tradosmatches.wordcounts.per100matches"),
                wordCountValueFormat));
        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_95_99"),
                wordCountValueFormat));
        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_75_94"),
                wordCountValueFormat));
        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_1_74"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_no_match"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("lb_repetition_word_cnt"), wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));

    }

    /**
     * Gets the jobs and outputs workflow information.
     * @exception Exception
     */
    private void addJobs(HttpServletRequest p_request) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        //print out the request parameters
        HttpSession session = p_request.getSession();
        WritableSheet sheet = m_workbook.createSheet(bundle
                .getString("jobinfo.tradosmatches"), 0);

        ArrayList jobs = new ArrayList();

        //do a search based on the params
        JobSearchParameters searchParams = getSearchParams(p_request);
        jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
        //sort jobs by job name
        Collections.sort(jobs, new JobComparator(Locale.US));

        getUseInContextInfos(jobs, data.wantsAllLocales, data.trgLocaleList);

        addHeader(sheet, bundle);

        Iterator jobIter = jobs.iterator();
        IntHolder row = new IntHolder(4);
        while (jobIter.hasNext())
        {
            Job j = (Job) jobIter.next();
            Collection c = j.getWorkflows();
            Iterator wfIter = c.iterator();
            while (wfIter.hasNext())
            {
                Workflow w = (Workflow) wfIter.next();
                String state = w.getState();
                //skip certain workflow whose target locale is not selected
                String trgLocale = w.getTargetLocale().toString();
                if (!data.wantsAllLocales
                        && !data.trgLocaleList.contains(trgLocale))
                {
                    continue;
                }
                //if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    addWorkflow(p_request, sheet, j, w, row);
                }
            }
        }

        row.inc();
        addTotals(sheet, row, bundle);

    }

    private void addTotals(WritableSheet p_sheet, IntHolder p_row,
            ResourceBundle bundle) throws Exception
    {
        int row = p_row.getValue() + 1; // skip a row
        WritableFont subTotalFont = new WritableFont(WritableFont.ARIAL, 10,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);

        WritableCellFormat subTotalFormat = new WritableCellFormat(subTotalFont);
        subTotalFormat.setWrap(true);
        subTotalFormat.setShrinkToFit(false);
        subTotalFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        subTotalFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        String title = bundle.getString("lb_totals");
        subTotalFormat.setBackground(jxl.format.Colour.GRAY_25);

        // For "Add Job Id into online job report" issue
        char sumStartCellCol = 'H';
        int c = 7;

        p_sheet.addCell(new Label(0, row, title, subTotalFormat));
        p_sheet.mergeCells(0, row, sumStartCellCol - 'B', row);
        int lastRow = p_row.getValue() - 1;

        // add in word count totals
        // word counts
        if (useDefaultContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                    + "5:" + sumStartCellCol + lastRow + ")", subTotalFormat));
            sumStartCellCol++;
        }

        if (useInContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                    + "5:" + sumStartCellCol + lastRow + ")", subTotalFormat));
            sumStartCellCol++;
        }

        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //100% Matches
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //95-99%
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //	75-94%
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //	1-74%*
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //	No Match
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //	Repetitions
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); //	Total
        sumStartCellCol++;

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

                //if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    for (TargetPage tp : wf.getTargetPages())
                    {
                        boolean isInContextMatch = PageHandler
                                .isInContextMatch(tp.getSourcePage()
                                        .getRequest().getJob());
                        boolean isDefaultContextMatch = PageHandler
                                .isDefaultContextMatch(tp);
                        if (isInContextMatch)
                        {
                            useInContext = true;
                        }
                        if (isDefaultContextMatch)
                        {
                            useDefaultContext = true;
                        }
                    }
                }
            }
        }
    }

    private String getProjectDesc(Job p_job)
    {
        Project p = p_job.getL10nProfile().getProject();
        String d = p.getDescription();
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + ": " + d;
        return desc;
    }

    /**
     * Gets the task for the workflow and outputs page information.
     *@exception Exception
     */
    private void addWorkflow(HttpServletRequest p_request,
            WritableSheet p_sheet, Job p_job, Workflow p_workflow,
            IntHolder p_row) throws Exception
    {
        //write job id, job name, description, creation date, creation time, language
        p_sheet.addCell(new Label(0, p_row.value, "" + p_job.getId())); //job id
        p_sheet.addCell(new Label(1, p_row.value, p_job.getJobName())); //job name
        p_sheet.addCell(new Label(4, p_row.value, getProjectDesc(p_job))); //description
        p_sheet.addCell(new Label(5, p_row.value, dateFormat.format(p_job
                .getCreateDate()))); //creation date
        p_sheet.addCell(new Label(6, p_row.value, p_workflow.getTargetLocale()
                .toString())); //language

        //write word count and file info
        for (TargetPage tg : p_workflow.getTargetPages())
        {
            try
            {
                addWordCount(tg, p_row, p_sheet);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            p_row.inc();
        }

    }

    public int addWordCount(TargetPage tg, IntHolder p_row,
            WritableSheet p_sheet) throws Exception
    {

        boolean isInContextMatch = PageHandler.isInContextMatch(tg
                .getSourcePage().getRequest().getJob());
        boolean isDefaultContextMatch = PageHandler.isDefaultContextMatch(tg
                .getSourcePage().getRequest().getJob());
        int segmentTmWordCount = 0;// 100% match
        int inContextWordCount = 0;// in context word match
        boolean isUseDefaultContextMatch = PageHandler
                .isDefaultContextMatch(tg);
        int lb_context_tm = tg.getWordCount().getContextMatchWordCount();
        if (isInContextMatch)
        {
            segmentTmWordCount = tg.getWordCount().getSegmentTmWordCount();// 100% match
            inContextWordCount = tg.getWordCount().getInContextWordCount();// in context word match
            lb_context_tm = 0;
        }
        else
        {
            if (isUseDefaultContextMatch)
            {
                segmentTmWordCount = tg.getWordCount()
                        .getNoUseExactMatchWordCount()
                        - lb_context_tm;
            }
            else
            {
                segmentTmWordCount = tg.getWordCount()
                        .getNoUseExactMatchWordCount();// 100% match
                lb_context_tm = 0;
            }
        }
        int hiFuzzyWordCount = tg.getWordCount().getHiFuzzyWordCount();// 95% match
        int medHiFuzzyWordCount = tg.getWordCount().getMedHiFuzzyWordCount();// 85% match
        int medFuzzyWordCount = tg.getWordCount().getMedFuzzyWordCount();// 75% match
        int lowFuzzyWordCount = tg.getWordCount().getLowFuzzyWordCount();// 50% match
        int unmatchedWordCount = tg.getWordCount().getUnmatchedWordCount();// no match
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
            totalWords += lb_context_tm;
        }

        String fileFullName = tg.getSourcePage().getExternalPageId();
        String filePath = fileFullName;
        String fileName = " ";
        if (fileFullName.indexOf("/") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("/") + 1, fileFullName.length());
            filePath = fileFullName.substring(0, fileFullName.lastIndexOf("/"));
        }
        else if (fileFullName.indexOf("\\") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("\\") + 1, fileFullName.length());
            filePath = fileFullName
                    .substring(0, fileFullName.lastIndexOf("\\"));
        }

        p_sheet.addCell(new Label(2, p_row.value, filePath));
        p_sheet.addCell(new Label(3, p_row.value, fileName));

        // write the information of word count
        int cursor = wordCountCol;
        if (useDefaultContext)
        {
            if (isDefaultContextMatch)
            {
                p_sheet.addCell(new Number(cursor, p_row.value, lb_context_tm));
            }
            else
            {
                p_sheet.addCell(new Number(cursor, p_row.value, 0));
            }
        }

        if (useInContext)
        {
            if (useDefaultContext)
                cursor++;

            if (isInContextMatch)
            {
                p_sheet.addCell(new Number(cursor, p_row.value,
                        inContextWordCount));
            }
            else
            {
                p_sheet.addCell(new Number(cursor, p_row.value, 0));
            }
        }

        if (useDefaultContext || useInContext)
            cursor++;
        p_sheet.addCell(new Number(cursor, p_row.value, segmentTmWordCount)); //100%
        p_sheet.addCell(new Number(++cursor, p_row.value, hiFuzzyWordCount)); //95-99%
        p_sheet.addCell(new Number(++cursor, p_row.value, medHiFuzzyWordCount
                + medFuzzyWordCount)); //75-94%
        //p_sheet.addCell(new Number(++cursor, p_row.value, medFuzzyWordCount)); 
        p_sheet.addCell(new Number(++cursor, p_row.value, lowFuzzyWordCount)); //1-74%
        p_sheet.addCell(new Number(++cursor, p_row.value, unmatchedWordCount)); //no match
        p_sheet.addCell(new Number(++cursor, p_row.value,
                lb_repetition_word_cnt));
        p_sheet.addCell(new Number(++cursor, p_row.value, totalWords));

        return totalWords;

    }%>
<%
    //Multi-Company: get current user's company from the session
    HttpSession userSession = request.getSession(false);
    String companyName = (String) userSession
            .getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
    if (UserUtil.isBlank(companyName))
    {
        companyName = (String) userSession
                .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
    }
    if (companyName != null)
    {
        CompanyThreadLocal.getInstance().setValue(companyName);
    }

    response.setHeader("Content-Disposition",
            "attachment; filename=VendorFileList.xls");
    response.setHeader("Expires", "0");
    response.setHeader("Cache-Control",
            "must-revalidate, post-check=0,pre-check=0");
    response.setHeader("Pragma", "public");
    generateReport(request, response);
%>