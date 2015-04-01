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
package com.globalsight.everest.webapp.pagehandler.administration.logs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.globalsight.util.StringUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Utility to download system log files
 *
 * @author Vincent Yan
 * @date 2012/04/24
 * @since 8.2.3
 */
public class ViewLogsHelper
{
    private static Logger logger = Logger.getLogger(ViewLogsHelper.class);
    private HttpServletResponse response;
    public static final String ZIP_FILENAME = "GSSystemLogs.zip";

    public ViewLogsHelper(HttpServletRequest request,
            HttpServletResponse response)
    {
        this.response = response;
    }

    /**
     * Download full log files
     *
     * @param systemLogDir
     *            Path of system log directory
     * @param logs
     *            Types of log
     */
    public void doDownloadFullLogs(String systemLogDir, ArrayList<String> logs)
    {
        File zipFile = generateZip4FullLogs(systemLogDir, logs, null);
        doDownload(zipFile);
    }

    /**
     * Download full log files since days
     *
     * @param systemLogDir
     *            Path of system log directory
     * @param logs
     *            Types of log
     * @param days
     *            Since days
     */
    public void doDownloadFullLogs(String systemLogDir, ArrayList<String> logs,
            String days)
    {
        File zipFile = generateZip4FullLogs(systemLogDir, logs, days);
        doDownload(zipFile);
    }

    /**
     * Download the package file of log files through HttpServletResponse
     *
     * @param zipFile
     *            Package file of log files
     */
    public void doDownload(File zipFile)
    {
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;
        byte[] buffer = new byte[4096];
        int size = -1;

        if (zipFile != null)
        {
            try
            {
                fin = new BufferedInputStream(new FileInputStream(zipFile));
                fout = new BufferedOutputStream(response.getOutputStream());
                while ((size = fin.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, size);
                }
                fout.flush();
                fout.close();
                fin.close();

                zipFile.delete();
            }
            catch (IOException e)
            {
                logger.error("Cannot download full logs.", e);
            }
        }
    }

    /**
     * Download log files with specified time scope
     *
     * @param systemLogDir
     *            Path of system log directory
     * @param logs
     *            Types of log
     * @param start
     *            Start time
     * @param end
     *            End time
     */
    public void doDownloadPartLogs(String systemLogDir, ArrayList<String> logs,
            String start, String end)
    {
        File zipFile = generateZip4PartLogs(systemLogDir, logs, start, end);

        doDownload(zipFile);
    }

    /**
     * Generate package file which contains all full system log files
     *
     * @param systemLogDir
     *            Path of system log directory
     * @param logs
     *            Types of log
     * @param days
     *            Since days
     * @return File Package file contains all full system log files
     */
    public File generateZip4FullLogs(String systemLogDir,
            ArrayList<String> logs, String days)
    {
        File[] logFiles = null;

        File zipFile = null;
        ArrayList<File> files = new ArrayList<File>();

        Calendar startDay = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        boolean isDaysDownload = !StringUtil.isEmpty(days);
        int daysRange = -1;
        String startDate = "";
        String filename = "";
        try
        {
            if (isDaysDownload)
            {
                daysRange = Integer.parseInt(days) - 1;
                if (daysRange < 0)
                    daysRange = 0;
                startDay.add(Calendar.DATE, (0 - daysRange));
                startDate = sdf.format(startDay.getTime());
            }

            for (String log : logs)
            {
                logFiles = getFileList(log, systemLogDir);
                if (logFiles == null || logFiles.length == 0)
                    continue;

                for (File file : logFiles)
                {
                    filename = file.getName();
                    if (isDaysDownload)
                    {
                        if (filename.compareTo(log + "." + startDate) >= 0 || filename.equals(log)) {
                            if ("JBoss_GlobalSight.log".equals(filename))
                                continue;
                            else {
                                files.add(file);
                            }
                        }
                    }
                    else
                        files.add(file);
                }
            }

            if (files.size() > 0)
            {
                logFiles = convertToArray(files);
            }
            zipFile = new File(systemLogDir + ZIP_FILENAME);
            ZipIt.addEntriesToZipFile(zipFile, logFiles, true,
                    "Package of all system logs");
        }
        catch (Exception e)
        {
            logger.error("Cannot download full logs.", e);
        }

        return zipFile;
    }

    /**
     * Generate package file which contains all system log files according with
     * specified time scope
     *
     * @param systemLogDir
     *            Path of system log directory
     * @param logs
     *            Types of log
     * @param start
     *            Start time
     * @param end
     *            End time
     * @return File Package file contains system log files
     */
    private File generateZip4PartLogs(String systemLogDir,
            ArrayList<String> logs, String start, String end)
    {
        // Init
        String tmpLogDirectory = systemLogDir + "tmp" + File.separator;

        File[] logFiles = null;

        File zipFile = null;
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String todayString = sdf.format(today.getTime());

        try
        {
            int index = -1;
            index = start.indexOf(" ");
            String startDate = start.substring(0, index).replace("-", "");

            File tmpLogDir = new File(tmpLogDirectory);
            tmpLogDir.mkdirs();

            ArrayList<File> files = null;

            boolean isToday = todayString.equals(startDate);
            if (isToday)
                files = getTodayLogs(systemLogDir, tmpLogDirectory, start, end,
                        logs);
            else
            {
                files = getDateRangeLogs(systemLogDir, tmpLogDirectory, start,
                        end, logs);
            }

            if(files.size() > 0)
            {
            	logFiles = convertToArray(files);
            }
            zipFile = new File(systemLogDir + ZIP_FILENAME);
            ZipIt.addEntriesToZipFile(zipFile, logFiles, true,
                    "Package of all system log files");

            FileUtils.deleteDirectory(tmpLogDir);
        }
        catch (Exception e)
        {
            logger.error("Cannot download full logs.", e);
        }

        return zipFile;
    }

    private ArrayList<File> getDateRangeLogs(String systemLogDirectory,
            String tmpLogDirectory, String start, String end,
            ArrayList<String> logs)
    {
        ArrayList<File> files = new ArrayList<File>();
        File srcFile = null, destFile = null, tmpFile = null;
        String filename = null;

        int index = start.indexOf(" ");
        String startDate = start.substring(0, index).replace("-", "");
        index = end.indexOf(" ");
        String endDate = end.substring(0, index).replace("-", "");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String today = sdf.format(calendar.getTime());

        String tmpStartLogFile = "", tmpEndLogFile = "";
        File[] logFiles = null;

        for (String logType : logs)
        {
            try
            {
                logFiles = getFileList(logType, systemLogDirectory);
                if (logFiles == null || logFiles.length == 0)
                    continue;

                tmpStartLogFile = logType + "." + startDate;
                tmpEndLogFile = logType + "." + endDate;

                for (int i = 0; i < logFiles.length; i++)
                {
                    tmpFile = logFiles[i];
                    filename = tmpFile.getName();

                    int beforeStart = filename.compareTo(tmpStartLogFile);
                    int afterEnd = filename.compareTo(tmpEndLogFile);
                    if (beforeStart < 0 || afterEnd > 0)
                        continue;

                    if (beforeStart > 0 && afterEnd < 0)
                    {
                        // Copy log files to tmp log directory
                        srcFile = tmpFile;
                        destFile = new File(tmpLogDirectory + filename);
                        FileUtils.copyFile(srcFile, destFile);

                        files.add(destFile);
                    }
                    else if (beforeStart == 0 || afterEnd == 0)
                    {
                        // Logs in same date
                        srcFile = tmpFile;
                        destFile = new File(srcFile.getAbsolutePath() + ".tmp");
                        FileUtils.copyFile(srcFile, destFile);

                        File destLogFile = new File(tmpLogDirectory + filename);
                        getPartLogs(destFile, destLogFile, start, end, logType);
                        files.add(destLogFile);
                    }
                }

                if (today.equals(startDate) || today.equals(endDate))
                {
                    ArrayList<String> tmpLogs = new ArrayList<String>();
                    tmpLogs.add(logType);
                    
                    ArrayList<File> todayLogs = getTodayLogs(
                            systemLogDirectory, tmpLogDirectory, start, end,
                            tmpLogs);
                    files.addAll(todayLogs);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return files;
    }

    private void getPartLogs(File srcFile, File destFile, String start,
            String end, String logType) throws UnsupportedEncodingException,
            FileNotFoundException, IOException
    {
        BufferedReader fin = null;
        BufferedWriter fout = null;
        String line = null;
        String dateString = null;
        String startDate = start.substring(0, start.indexOf(" "));
        String endDate = end.substring(0, end.indexOf(" "));
        boolean hasContent = false;
        fin = new BufferedReader(new InputStreamReader(new FileInputStream(
                srcFile), "UTF-8"));
        fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                destFile), "UTF-8"));
        boolean isJbossGlobalSightLog = logType.equals("JBoss_GlobalSight.log");

        if (!isJbossGlobalSightLog)
        {
            while ((line = fin.readLine()) != null)
            {
            	if (!line.startsWith(startDate)
                        && !line.startsWith(endDate)
                        && !line.startsWith("<"+startDate)
                        && !line.startsWith("<"+endDate))
                {
                    if (hasContent)
                        fout.write(line + "\n");
                }
                else
                {
                	if("operation.log".equalsIgnoreCase(logType))
                	{
                		dateString = line.substring(1, 17);
                	}
                	else
                	{
                		dateString = line.substring(0, 16);
                	}
                    if (dateString.compareTo(start) >= 0
                            && dateString.compareTo(end) < 0)
                    {
                        fout.write(line + "\n");
                        hasContent = true;
                    }
                    else
                        hasContent = false;
                }
            }
        }
        else
        {
            startDate = startDate.replace("-", "/");
            endDate = endDate.replace("-", "/");
            start = start.replace("-", "/");
            end = end.replace("-", "/");

            while ((line = fin.readLine()) != null)
            {
                String[] params = line.split("\\|");

                if (params == null || params.length < 3)
                {
                    if (hasContent)
                        fout.write(line + "\n");
                }
                else
                {
                    dateString = params[2].trim();
                    dateString = dateString.substring(0, dateString.lastIndexOf(":"));
                    if (dateString.compareTo(start) >= 0
                            && dateString.compareTo(end) < 0)
                    {
                        fout.write(line + "\n");
                        hasContent = true;
                    }
                    else
                        hasContent = false;
                }
            }
        }
        fout.flush();
        fout.close();

        fin.close();

        srcFile.delete();
    }

    private File[] convertToArray(ArrayList<File> files)
    {
        File[] list = null;
        if (files != null)
        {
            int size = files.size();
            if (size > 0)
            {
                list = new File[size];
                for (int i = 0; i < size; i++)
                    list[i] = files.get(i);
            }
        }

        return list;
    }

    /**
     * Get all logs in current day with time range
     *
     * @param systemLogDirectory
     *            System log directory
     * @param start
     *            Start date and time
     * @param end
     *            End date and time
     * @param logs
     *            Including logs
     */
    private ArrayList<File> getTodayLogs(String systemLogDirectory,
            String tmpLogDirectory, String start, String end,
            ArrayList<String> logs)
    {
        ArrayList<File> files = new ArrayList<File>();
        File srcFile = null, destFile = null, tmpLogFile = null;
        BufferedWriter fout = null;
        BufferedReader fin = null;
        String filename = null;
        String line = "", dateString = "";
        String startDate = start.substring(0, start.indexOf(" "));
        String endDate = end.substring(0, end.indexOf(" "));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar now = Calendar.getInstance();
        String today = sdf.format(now.getTime());
        boolean isJBossWrapperLog = false;

        for (String log : logs)
        {
            isJBossWrapperLog = log.equals("JBoss_GlobalSight.log");
            if (isJBossWrapperLog)
                filename = systemLogDirectory + log + "." + today;
            else
                filename = systemLogDirectory + log;

            try
            {
                // Copy current log file to a tmp file
                srcFile = new File(filename);
                destFile = new File(filename + ".tmp");
                FileUtils.copyFile(srcFile, destFile);

                if (isJBossWrapperLog)
                    tmpLogFile = new File(tmpLogDirectory + log + "." + today);
                else
                    tmpLogFile = new File(tmpLogDirectory + log);
                fout = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(tmpLogFile), "UTF-8"));
                fin = new BufferedReader(new InputStreamReader(
                        new FileInputStream(destFile), "UTF-8"));
                boolean hasContent = false;
                if (!isJBossWrapperLog)
                {
                    while ((line = fin.readLine()) != null)
                    {
                        if (!line.startsWith(startDate)
                                && !line.startsWith(endDate)
                                && !line.startsWith("<"+startDate)
                                && !line.startsWith("<"+endDate))
                        {
                            if (hasContent)
                                fout.write(line + "\n");
                        }
                        else
                        {
                        	if("operation.log".equalsIgnoreCase(log))
                        	{
                        		dateString = line.substring(1, 17);
                        	}
                        	else
                        	{
                        		dateString = line.substring(0, 16);
                        	}
                            if (dateString.compareTo(start) >= 0
                                    && dateString.compareTo(end) < 0)
                            {
                                fout.write(line + "\n");
                                hasContent = true;
                            }
                            else
                            {
                                hasContent = false;
                            }
                        }
                    }
                }
                else
                {
                    startDate = startDate.replace("-", "/");
                    endDate = endDate.replace("-", "/");
                    start = start.replace("-", "/");
                    end = end.replace("-", "/");

                    while ((line = fin.readLine()) != null)
                    {
                        String[] params = line.split("\\|");

                        if (params == null || params.length < 3)
                        {
                            if (hasContent)
                                fout.write(line + "\n");
                        }
                        else
                        {
                            dateString = params[2].trim();
                            dateString = dateString.substring(0, dateString.lastIndexOf(":"));
                            if (dateString.compareTo(start) >= 0
                                    && dateString.compareTo(end) < 0)
                            {
                                fout.write(line + "\n");
                                hasContent = true;
                            }
                            else
                                hasContent = false;
                        }
                    }
                }

                fout.flush();
                fout.close();

                files.add(tmpLogFile);

                fin.close();

                destFile.delete();
            }
            catch (Exception e)
            {
                logger.error("File [" + filename + "] not found!");
            }
        }

        return files;
    }

    private File[] getFileList(String logType, String directory)
    {
        FileFilter filter = null;
        if ("GlobalSight.log".equals(logType))
            filter = new GlobalSightLogFilter();
        else if ("activity.log".equals(logType))
            filter = new ActivityLogFilter();
        else if ("operation.log".equals(logType))
            filter = new OperationLogFilter();
        else if ("webservices.log".equals(logType))
            filter = new WebservicesLogFilter();
        else if ("JBoss_GlobalSight.log".equals(logType))
            filter = new JBossLogFilter();

        if (filter == null)
            return null;
        else
            return new File(directory).listFiles(filter);
    }

    class GlobalSightLogFilter implements FileFilter
    {

        @Override
        public boolean accept(File pathname)
        {
            if (pathname.getName().startsWith("GlobalSight.log"))
                return true;
            else
                return false;
        }
    }

    class ActivityLogFilter implements FileFilter
    {

        @Override
        public boolean accept(File pathname)
        {
            if (pathname.getName().startsWith("activity.log"))
                return true;
            else
                return false;
        }
    }

    class OperationLogFilter implements FileFilter
    {

        @Override
        public boolean accept(File pathname)
        {
            if (pathname.getName().startsWith("operation.log"))
                return true;
            else
                return false;
        }
    }

    class WebservicesLogFilter implements FileFilter
    {

        @Override
        public boolean accept(File pathname)
        {
            if (pathname.getName().startsWith("webservices.log"))
                return true;
            else
                return false;
        }
    }

    class JBossLogFilter implements FileFilter
    {

        @Override
        public boolean accept(File pathname)
        {
            if (pathname.getName().startsWith("JBoss_GlobalSight"))
                return true;
            else
                return false;
        }
    }

    public void packageLogs(String systemLogDir, ArrayList<String> logs,
            String days)
    {
        String zipLogsDirectory = systemLogDir + File.separator + "backup"
                + File.separator;
        int iDays = 0;
        try
        {
            File zipLogsDir = new File(zipLogsDirectory);
            if (!zipLogsDir.exists() || zipLogsDir.isFile())
                zipLogsDir.mkdirs();

            iDays = Integer.parseInt(days);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar day = Calendar.getInstance();
            String today = sdf.format(day.getTime());

            day.add(Calendar.DATE, 0 - iDays);
            String daysBefore = sdf.format(day.getTime());

            File[] logFiles = null;
            FileFilter fileFilter = null;

            File zipFile = null;
            String filename = "";
            String tmp = "";

            for (String log : logs)
            {
                ArrayList<File> files = new ArrayList<File>();
                if ("GlobalSight.log".equals(log) || "activity.log".equals(log))
                {
                    if ("GlobalSight.log".equals(log))
                        fileFilter = new GlobalSightLogFilter();
                    else
                        fileFilter = new ActivityLogFilter();

                    tmp = log + "." + daysBefore;
                    logFiles = new File(systemLogDir).listFiles(fileFilter);

                    if (logFiles != null && logFiles.length > 0)
                    {
                        for (File file : logFiles)
                        {
                            filename = file.getName();
                            if (!filename.equals(log)
                                    && filename.compareTo(tmp) <= 0)
                                files.add(file);
                        }
                    }
                    if (files.size() > 0)
                    {
                        logFiles = convertToArray(files);
                        zipFile = new File(zipLogsDirectory + log + "." + today
                                + ".zip");
                        ZipIt.addEntriesToZipFile(zipFile, logFiles, true,
                                "Package of log file" + log + " at " + today);

                        for (File file : files)
                        {
                            file.delete();
                        }
                    }
                }
                logger.info("Package old log file " + log + " successfully.");
            }
        }
        catch (Exception e)
        {
            logger.error("Error found in packageLogs().", e);
        }
    }

}
