/**
 *  Copyright 2011 Welocalize, Inc. 
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParameterPersistenceManagerLocal;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskThread;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.util.zip.ZipIt;
//import jxl.write.NumberFormat;
//import jxl.write.WritableCellFormat;
//import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;

public class ReportHelper
{
    public static List<String> allJobStatusList;

    /**
     * Get the job info which report need. Because maybe there are a lot of
     * jobs, so here use the projective query, that can get a good performance.
     */
    public static Map<String, ReportJobInfo> getJobInfo(
            ArrayList<String> stateList)
    {
        Map<String, ReportJobInfo> reportJobInfosMap = new HashMap<String, ReportJobInfo>();

        StringBuffer hql = new StringBuffer();
        hql.append("select distinct j.id, j.jobName, j.state, b.id, b.project.id,w.targetLocale.id");
        hql.append(" from JobImpl j , WorkflowImpl w , RequestImpl r , BasicL10nProfile b");
        hql.append(" where w.job.id = j.id and r.job.id = j.id ");
        hql.append(" and r.l10nProfile.id = b.id and j.state ");
        hql.append(addClause(stateList));

        String currentId = CompanyThreadLocal.getInstance().getValue();

        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql.append(" and j.companyId = ");
            hql.append(Long.parseLong(currentId));
        }

        Iterator it = HibernateUtil.search(hql.toString()).iterator();

        while (it.hasNext())
        {
            Object[] row = (Object[]) it.next();
            Long jobId = (Long) row[0];
            String name = (String) row[1];
            String state = (String) row[2];
            Long locprofileId = (Long) row[3];
            Long projectId = (Long) row[4];
            Long targetId = (Long) row[5];

            if (reportJobInfosMap.get(jobId.toString()) == null)
            {
                List<String> targetLocaleIdList = new ArrayList<String>();
                if (targetId != null)
                    targetLocaleIdList.add(targetId.toString());

                ReportJobInfo rj = new ReportJobInfo(jobId.toString(), name,
                        state, locprofileId.toString(), projectId.toString(),
                        targetLocaleIdList);
                reportJobInfosMap.put(jobId.toString(), rj);
            }
            else
            {
                if (targetId != null)
                    reportJobInfosMap.get(jobId.toString()).addTargetLocale(
                            targetId.toString());
            }
        }

        return reportJobInfosMap;
    }

    private static String addClause(ArrayList p_list)
    {
        StringBuffer p_sb = new StringBuffer();
        p_sb.append(" in (");

        for (int i = 0; i < p_list.size(); i++)
        {
            p_sb.append("'");
            p_sb.append(p_list.get(i));
            p_sb.append("'");

            if (i < p_list.size() - 1)
            {
                p_sb.append(", ");
            }
        }

        p_sb.append(")");

        return p_sb.toString();
    }

    /**
     * Splits the string by comma, return List<Long>.
     */
    public static List<Long> getListOfLong(String p_str)
    {
        if (p_str == null || p_str.trim().length() == 0)
            return null;

        return getListOfLong(p_str, ",");
    }

    /**
     * Splits this string around matches of the given regular expression
     * 
     * @param p_str
     *            string
     * @param p_regex
     *            regular expression
     * @return List<Long>
     */
    public static List<Long> getListOfLong(String p_str, String p_regex)
    {
        if (p_str == null || p_str.trim().length() == 0)
            return null;

        List<Long> result = new ArrayList<Long>();
        String[] array = p_str.split(p_regex);
        for (String str : array)
        {
            if (str != null && str.trim().length() > 0)
            {
                long id = Long.valueOf(str.trim());
                if (!result.contains(id))
                {
                    result.add(id);
                }
            }
        }
        return result;
    }

    /**
     * Splits this string around matches of the given regular expression
     * 
     * @param p_str
     *            string
     * @param p_regex
     *            regular expression
     * @return List<String>
     */
    public static List<String> getListOfStr(String p_str, String p_regex)
    {
        if (p_str == null || p_str.trim().length() == 0)
            return null;

        List<String> result = new ArrayList<String>();
        for (String str : p_str.split(p_regex))
        {
            if (str != null && str.trim().length() > 0)
            {
                result.add(str);
            }
        }
        return result;
    }

    /**
     * Gets Sorted Target Locale list from String array, and sorted by ISO_CODE.
     * 
     * @param p_array
     *            target language string array
     * @param p_comparator
     *            GlobalSightLocale Comparator which is used for sort the
     *            resturn list.
     * @return
     * @throws Exception
     */
    public static List<GlobalSightLocale> getTargetLocaleList(String[] p_array,
            GlobalSightLocaleComparator p_comparator) throws Exception
    {
        if (p_array == null)
        {
            return null;
        }
        else if (p_array.length == 1)
        {
            p_array = p_array[0].split(",");
        }

        List<GlobalSightLocale> result = new ArrayList<GlobalSightLocale>();
        for (String str : p_array)
        {
            if ("*".equals(str))
            {
                result = new ArrayList<GlobalSightLocale>(ServerProxy
                        .getLocaleManager().getAllTargetLocales());
                break;
            }
            else if (str != null && str.trim().length() > 0)
            {
                String localeString = str.trim();
                GlobalSightLocale tl;
                if (localeString.contains("_"))
                {
                    tl = ServerProxy.getLocaleManager().getLocaleByString(
                            localeString);
                }
                else
                {
                    tl = ServerProxy.getLocaleManager().getLocaleById(
                            Long.valueOf(str.trim()));
                }
                if (!result.contains(tl))
                {
                    result.add(tl);
                }
            }
        }

        if (p_comparator != null)
        {
            SortUtil.sort(result, p_comparator);
        }

        return result;
    }

    // public static Locale getLocale(Locale p_locale)
    // {
    // return p_locale == null ? Locale.US : p_locale;
    // }

    /**
     * Gets the Excel Report File.
     */
    public static File getXLSReportFile(String p_reportType, Job p_job)
    {
        return getReportFile(p_reportType, p_job, ReportConstants.EXTENSION_XLSX, null);
    }

    /**
     * Gets the Report File, such as
     * {FileStorage}/{CompanyName}/Reports/
     * {ReportType}-(jobName)(jobId)-{langInfo}-{Timestamp}.xlsx
     * 
     * @param p_reportType
     *            Report Type
     * @param p_job
     * @param p_extension
     *            Report File Extension, such as .xlsx/.csv
     * @param p_langInfo
     *            Report Language Info, such as en_US_de_DE
     * @return
     */
    public static File getReportFile(String p_reportType, Job p_job,
            String p_extension, String p_langInfo)
    {
        StringBuffer result = new StringBuffer();
        result.append(AmbFileStoragePathUtils.getFileStorageDirPath(1))
                .append(File.separator);
        result.append(ReportConstants.REPORTS_SUB_DIR).append(File.separator);
        new File(result.toString()).mkdirs();
        // Report Name Part 1: Report Type
        result.append(p_reportType);
        // Report Name Part 2: Job Info
        if (p_job != null)
        {
            String jobName = p_job.getJobName();
            result.append("-(").append(jobName).append(")(")
                  .append(p_job.getJobId()).append(")");
        }
        // Report Name Part 3: Language Info
        if(p_langInfo != null)
        {
            result.append("-").append(p_langInfo);
        }
        // Report Name Part 4: Timestamp
        result.append("-").append(new SimpleDateFormat("yyyyMMdd HHmmss").format(new Date()));
        // Report Name Part 5: File Extension
        result.append(p_extension);

        return new File(result.toString());
    }
    
    /**
     * Move the report to the user folder for download, the path will be 
     * {FileStorage}/Reports/{userID}/{ReportType}/{date}/{ReportName.xlsx}.
     * 
     * @param p_fileList
     *            The temp report file list.
     * @param p_userID
     *            The operator user id.
     */
    public static File[] moveReports(List<File> p_fileList, String p_userID)
 {
        if (p_fileList == null || p_fileList.size() == 0)
            return new File[0];

        String value = "yes";
		File reports[] = new File[p_fileList.size()];
		for (int i = 0; i < reports.length; i++)
		{
			File srcReport = p_fileList.get(i);
			String name = srcReport.getName();
			String srcPath = srcReport.getPath();
			srcPath = srcPath.substring(0,
					srcPath.lastIndexOf(File.separator) + 1);
			String destPath = p_userID + File.separator;
			destPath += name.substring(0, name.indexOf("-")) + File.separator;
			destPath += new SimpleDateFormat("yyyyMMdd").format(new Date())
					+ File.separator;
			destPath = srcPath + destPath;
			File destFolder = new File(destPath);
			destFolder.mkdirs();

			String fullReportName = name.substring(0, name.indexOf("-"));
            if ("yes".equals(value)
                    && ReportConstants.reportNameMap
                            .containsKey(fullReportName))
			{
                name = ReportConstants.reportNameMap.get(fullReportName)
                        + name.substring(name.indexOf("-"), name.length());
			}

			File report = new File(destPath + name);
			if (report.exists())
				report.delete();
			srcReport.renameTo(report);
			reports[i] = report;
		}

		return reports;
	}

    /**
     * Send File to Client
     * 
     * @param p_file
     *            file
     * @param p_fileName
     *            attached file name. If NULL, use p_file.getName().
     * @param p_contentType
     *            response content type
     * @param p_response
     *            response
     * @param p_isDelete
     *            whether delete the input file(p_file).
     * @throws IOException
     * @throws ServletException
     */
    protected static void sendFile(File p_file, String p_attachFileName, HttpServletResponse p_response, 
            boolean p_isDelete) 
            throws IOException, ServletException
    {
        BufferedInputStream buf = null;
        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            String attachFileName = p_attachFileName;
            if(attachFileName == null || attachFileName.trim().length() == 0)
                attachFileName = p_file.getName();
            
            p_response.setContentType(getMIMEType(p_file));
            p_response.setHeader("Content-Disposition", "attachment; filename=\"" + attachFileName + "\"");
            p_response.setHeader("Expires", "0");
            p_response.setHeader("Cache-Control", "must-revalidate, post-check=0,pre-check=0");
            p_response.setHeader("Pragma", "public");
            p_response.setContentLength((int) p_file.length());
            FileInputStream fis = new FileInputStream(p_file);
            buf = new BufferedInputStream(fis);
            int readBytes = 0;
            while ((readBytes = buf.read()) != -1)
            {
                out.write(readBytes);
            }
        }
        catch (IOException ioe)
        {
            throw new ServletException(ioe.getMessage());
        }
        finally
        {
            if (buf != null)
                buf.close();
            if (out != null)
                out.close();
            if (p_isDelete)
                p_file.delete();
        }
    }

    public static void sendFiles(List<File> p_reports, String p_zipFileName,
            HttpServletResponse p_response) throws FileNotFoundException,
            IOException, ServletException
    {
        File[] files = p_reports.toArray(new File[p_reports.size()]);
        sendFiles(files, p_zipFileName, p_response);
    }
    
    public static void sendFiles(File[] p_files, String p_zipFileName,
            HttpServletResponse p_response) throws FileNotFoundException,
            IOException, ServletException
    {
        sendFiles(p_files, p_zipFileName, p_response, false);
    }

    /**
     * Sends Files to Client.
     * 
     * @param p_files
     *            The Files will send to Client's Browser.
     * @param p_fileName
     *            File Name.
     * @param p_response
     *            Servlet Response
     * @param p_isDelete
     *            Whether delete input files(p_files)
     */
    public static void sendFiles(File[] p_files, String p_fileName, 
            HttpServletResponse p_response, boolean p_isDelete) 
            throws FileNotFoundException, IOException, ServletException
    {
        File file = null;
        // Whether delete the final file(Delete ZIP file).
        boolean isDeleteFinalFile = true;
        if (p_files == null || p_files.length == 0)
        {
            return;
        }
        else if (p_files.length == 1)
        {
            file = p_files[0];
            isDeleteFinalFile = p_isDelete;
        }
        else
        {
            if (p_fileName == null || p_fileName.trim().length() == 0)
            {
                p_fileName = ReportConstants.REPORTS_NAME + ".zip";
            }
            if (!p_fileName.endsWith(".zip"))
            {
                p_fileName = p_fileName + ".zip";
            }
            file = new File(p_fileName);
            ZipIt.addEntriesToZipFile(file, p_files, true, "");
            if (p_isDelete)
                ReportHelper.deleteFiles(p_files);
        }

        sendFile(file, p_fileName, p_response, isDeleteFinalFile);
    }
    
    public static void sendFiles(Set<File> p_files, String p_fileName, 
            HttpServletResponse p_response, boolean p_isDelete) 
            throws FileNotFoundException, IOException, ServletException
    {
        File file = null;
        // Whether delete the final file(Delete ZIP file).
        boolean isDeleteFinalFile = true;
        if (p_files == null || p_files.size() == 0)
        {
            return;
        }
        else if (p_files.size() == 1)
        {
            file = p_files.iterator().next();
            isDeleteFinalFile = p_isDelete;
        }
        else
        {
            if (p_fileName == null || p_fileName.trim().length() == 0)
            {
                p_fileName = ReportConstants.REPORTS_NAME;
            }
            file = new File(p_fileName + ".zip");
            ZipIt.addEntriesToZipFile(file, p_files, true, "");
            if (p_isDelete)
                ReportHelper.deleteFiles(p_files);
        }

        sendFile(file, p_fileName, p_response, isDeleteFinalFile);
    }

    /**
     * Get Internet Media Type.
     * Wiki: http://en.wikipedia.org/wiki/Internet_media_type
     */
    public static String getMIMEType(File file)
    {
        if (file == null)
            return "";

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".zip"))
        {
            return "application/zip";
        }
        else if(fileName.endsWith(".csv"))
        {
            return "application/csv";
        }
        else if (fileName.endsWith(".xls"))
        {
            return "application/msexcel";
        }
        else if (fileName.endsWith(".ppt"))
        {
            return "application/mspowerpoint";
        }
        else if (fileName.endsWith(".doc"))
        {
            return "application/msword";
        }
        else if (fileName.endsWith(".xlsx"))
        {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        else if (fileName.endsWith(".pptx"))
        {
            return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        }
        else if (fileName.endsWith(".docx"))
        {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        else if (fileName.endsWith(".pdf"))
        {
            return "application/pdf";
        }

        return "";
    }

    /**
     * Gets the Job List by jobID List.
     * 
     * @param p_jobIDS
     *            jobID List
     */
    public static ArrayList<Job> getJobListByIDS(List<Long> p_jobIDS)
            throws JobException, RemoteException, GeneralException,
            NamingException
    {
        ArrayList<Job> jobs = new ArrayList<Job>();
        for (long jobId : p_jobIDS)
        {
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            if (job != null)
            {
                jobs.add(job);
            }
        }
        return jobs;
    }

    /**
     * Adds p_files to File List.
     * 
     * @param p_reports
     *            File List
     * @param p_files
     *            File Array
     */
    public static void addFiles(List<File> p_reports, File[] p_files)
    {
        if(p_reports == null || p_files == null)
            return;
        
        for (File file : p_files)
        {
            p_reports.add(file);
        }
    }

    public static List<GlobalSightLocale> getTargetLocals(Job p_job)
    {
        List<GlobalSightLocale> list = new ArrayList<GlobalSightLocale>();
        Iterator<Workflow> it = p_job.getWorkflows().iterator();
        while (it.hasNext())
        {
            Workflow wf = it.next();
            GlobalSightLocale wfTargetLocale = wf.getTargetLocale();
            String wfStatus = wf.getState();
            if (!Workflow.CANCELLED.equals(wfStatus))
                list.add(wfTargetLocale);
        }
        return list;
    }

    // Get all target Locale, and sorted by ISO_CODE.
    public static List<GlobalSightLocale> getAllTargetLocales()
            throws EnvoyServletException
    {
        return getAllTargetLocales(Locale.US,
                GlobalSightLocaleComparator.ISO_CODE);
    }

    // Gets all sorted target locale.
    public static List<GlobalSightLocale> getAllTargetLocales(Locale p_locale,
            int p_sortedType) throws EnvoyServletException
    {
        List<GlobalSightLocale> locales = new ArrayList<GlobalSightLocale>();

        try
        {
            locales = ServerProxy.getLocaleManager().getAllTargetLocales();
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge.getExceptionId(), ge);
        }

        if (p_locale != null && p_sortedType >= 0)
        {
            SortUtil.sort(locales, new GlobalSightLocaleComparator(
                    p_sortedType, p_locale));
        }

        return locales;
    }

    public static ArrayList<String> getAllJobStatusList()
    {
        if (allJobStatusList == null || allJobStatusList.size() == 0)
        {
            allJobStatusList = new ArrayList<String>();
            allJobStatusList.add(Job.PENDING);
            allJobStatusList.add(Job.READY_TO_BE_DISPATCHED);
            allJobStatusList.add(Job.DISPATCHED);
            allJobStatusList.add(Job.LOCALIZED);
            allJobStatusList.add(Job.EXPORTED);
            allJobStatusList.add(Job.EXPORT_FAIL);
            allJobStatusList.add(Job.ARCHIVED);
        }

        return (ArrayList<String>) ((ArrayList<String>) allJobStatusList)
                .clone();
    }

//    public static WritableCellFormat getMoneyFormat(String p_currencyName)
//    {
//        String symbol = ReportUtil.getCurrencySymbol(p_currencyName);
//        NumberFormat moneyFormat = new NumberFormat(symbol + "###,###,##0.000",
//                NumberFormat.COMPLEX_FORMAT);
//        return new WritableCellFormat(moneyFormat);
//    }

    public static String getJobStatusDisplayName(String p_jobState)
    {
        return getJobStatusDisplayName(p_jobState, Locale.US);
    }

    public static String getJobStatusDisplayName(String p_jobState,
            Locale p_userLocale)
    {
        String propertyKey = "";
        String defaultName = "";
        if ((p_jobState.equals(Job.PENDING))
                || (p_jobState.equals(Job.BATCHRESERVED))
                || (p_jobState.equals(Job.IMPORTFAILED)))
        {
            defaultName = "Pending";
            propertyKey = "lb_pending";
        }
        else if (p_jobState.equals(Job.READY_TO_BE_DISPATCHED))
        {
            defaultName = "Ready";
            propertyKey = "lb_ready";
        }
        else if (p_jobState.equals(Job.ADD_FILE))
        {
            defaultName = "Adding Files";
            propertyKey = "lb_addfiles";
        }
        else if (p_jobState.equals(Job.DISPATCHED))
        {
            defaultName = "In Progress";
            propertyKey = "lb_inprogress";
        }
        else if (p_jobState.equals(Job.LOCALIZED))
        {
            defaultName = "Localized";
            propertyKey = "lb_localized";
        }
        else if (p_jobState.equals(Job.DTPINPROGRESS))
        {
            defaultName = "DTP In Progress";
            propertyKey = "lb_dtpinprogress";
        }
        else if (p_jobState.equals(Job.EXPORTING))
        {
            defaultName = "Exporting";
            propertyKey = "lb_state_exporting";
        }
        else if (p_jobState.equals(Job.EXPORTED))
        {
            defaultName = "Exported";
            propertyKey = "lb_exported";
        }
        else if(p_jobState.equals(Job.EXPORT_FAIL))
        {
        	defaultName = "Export Failed";
            propertyKey = "lb_exported_failed";
        }
        else if (p_jobState.equals(Job.UPLOADING))
        {
            defaultName = "Uploading";
            propertyKey = "lb_state_uploading";
        }
        else if (p_jobState.equals(Job.IN_QUEUE))
        {
            defaultName = "In Queue";
            propertyKey = "lb_state_inqueue";
        }
        else if (p_jobState.equals(Job.EXTRACTING))
        {
            defaultName = "Extracting";
            propertyKey = "lb_state_extracting";
        }
        else if (p_jobState.equals(Job.LEVERAGING))
        {
            defaultName = "Leveraging";
            propertyKey = "lb_state_leveraging";
        }
        else if (p_jobState.equals(Job.CALCULATING_WORD_COUNTS))
        {
            defaultName = "Calculating Word Counts";
            propertyKey = "lb_state_CalculatingWordCounts";
        }
        /*
         * else if (p_jobState.equals(Job.PROCESSING)) { long fileCount = 0;
         * Collection<Request> requests = getRequestList(); Set<Long>
         * pageNumbers = new HashSet<Long>(requests.size()); for (Request r :
         * requests) { if (fileCount == 0) { fileCount =
         * r.getBatchInfo().getPageCount(); } long pageNumber =
         * r.getBatchInfo().getPageNumber(); if
         * (!pageNumbers.contains(pageNumber)) { pageNumbers.add(pageNumber); }
         * } int fileNumber = pageNumbers.size(); defaultName = "Processing (" +
         * fileNumber + " of " + fileCount + ")"; }
         */
        else if (p_jobState.equals(Job.SKIPPING))
        {
            defaultName = "Skipping";
            propertyKey = "lb_state_skipping";
        }
        else
        {
            defaultName = "Archived";
            propertyKey = "lb_archived";
        }

        // get value from resource bundle
        if (p_userLocale == null)
        {
            p_userLocale = Locale.US;
        }
        SystemResourceBundle srb = SystemResourceBundle.getInstance();
        ResourceBundle rb = srb.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_userLocale);
        String result = null;
        try
        {
            result = rb.getString(propertyKey);
        }
        catch (MissingResourceException e)
        {
            result = defaultName;
        }

        return result;
    }

    /**
     * Store the Reports Data into database, such as Report Status and percent.
     */
    public static void setReportsData(String p_userId, List<Long> p_reportJobIDS, List<String> p_reportTypeList,
            double p_percent, String p_status) throws UserManagerException, RemoteException, GeneralException
    {
        if (ReportsData.STATUS_FINISHED.equals(p_status))
        {
            ReportDBUtil.delReportsData(p_userId, p_reportJobIDS, p_reportTypeList);
        }
        else
        {
            ReportDBUtil.saveOrUpdateReportsData(p_userId, p_reportJobIDS, p_reportTypeList, p_status, p_percent);
        }
    }
    
    public static void setReportsData(String p_userId, List<Long> p_reportJobIDS, String p_reportType,
            double p_percent, String p_status) throws UserManagerException, RemoteException, GeneralException
    {
        List<String> reportTypeList = new ArrayList<String>();
        reportTypeList.add(p_reportType);
        setReportsData(p_userId, p_reportJobIDS, reportTypeList, p_percent, p_status);
    }

    /**
     * Check whether the Reports is generating.
     * The ReportData is stored by method setReportsData().
     * 
     * @throws GeneralException
     * @throws RemoteException
     * @throws UserManagerException
     */
    public static boolean checkReportsDataInProgressStatus(String p_userId, List<Long> p_reportJobIDS, List<String> p_reportTypeList)
            throws UserManagerException, RemoteException, GeneralException
    {
        ReportsData reportsData = ReportDBUtil.getReportsData(p_userId, p_reportJobIDS, p_reportTypeList);
        if (reportsData == null)
            return false;
        else
            return reportsData.isInProgress();
    }
    
    public static boolean checkReportsDataInProgressStatus(String p_userId, List<Long> p_reportJobIDS, String p_reportType)
            throws UserManagerException, RemoteException, GeneralException
    {
        List<String> reportTypeList = new ArrayList<String>();
        reportTypeList.add(p_reportType);
        return checkReportsDataInProgressStatus(p_userId, p_reportJobIDS, reportTypeList);
    }

    /**
     * Gets key of the Map.
     */
    public static String getKey(String p_userId, List<Long> p_reportJobIDS,
            String p_reportType)
    {
        StringBuffer result = new StringBuffer();
        if (p_userId != null)
            result.append(p_userId);

        if (p_reportJobIDS != null && p_reportJobIDS.size() > 0)
        {
            List<Long> reportJobIDS = new ArrayList<Long>(p_reportJobIDS);
            SortUtil.sort(reportJobIDS);
            result.append(reportJobIDS);
        }

        if (p_reportType != null && p_reportType.trim().length() > 0)
        {
            if (p_reportType.startsWith("["))
                result.append(p_reportType);
            else
                result.append("[").append(p_reportType).append("]");
        }

        return result.toString();
    }

    public static String getKey(String p_userId, List<Long> p_reportJobIDS,
            List<String> p_reportTypeList)
    {
        String reportType = null;
        if (p_reportTypeList != null)
            reportType = p_reportTypeList.toString();

        return getKey(p_userId, p_reportJobIDS, reportType);
    }

    public static List<Long> getJobIDS(List<Job> p_jobs)
    {
        List<Long> result = new ArrayList<Long>();
        for (Job job : p_jobs)
        {
            result.add(job.getJobId());
        }
        return result;
    }
    
    public static void deleteFiles(File[] files)
    {
        for (File file : files)
        {
            file.delete();
        }
    }
    
    public static void deleteFiles(Set<File> files)
    {
        for (File file : files)
        {
            file.delete();
        }
    }
}
