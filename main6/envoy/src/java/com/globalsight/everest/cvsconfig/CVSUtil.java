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
package com.globalsight.everest.cvsconfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMapping;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingHelper;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingManagerLocal;
import com.globalsight.everest.cvsconfig.modulemapping.ModuleMappingRename;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandlerLocal;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;

public class CVSUtil
{
    private static final Logger logger = Logger.getLogger(CVSUtil.class);

    private static PrintWriter out = null;
    
    private static final String[] bashFileType = new String[]
    { "exe", "docx", "pptx", "xlsx", "doc", "xls", "ppt", "zip", "xlz", "jpeg",
            "gif", "ico", "jpg", "sql" };

    public static String getBaseDocRoot()
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();

        String root = sc.getStringParameter("cxe.docsDir");

        if (!(root.endsWith("/") || root.endsWith("\\")))
        {
            root = root + "/";
        }
        return root;
    }

    // fix for GBS-1368
    private static String getBaseDocRootByCompany(String p_companyId)
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();

        String root = sc.getStringParameter("cxe.docsDir", p_companyId);

        if (!(root.endsWith("/") || root.endsWith("\\")))
        {
            root = root + "/";
        }
        return root;
    }

    public static boolean createFolder(String p_serverName) throws IOException
    {
        File folder = null;

        try
        {
            folder = new File(getBaseDocRoot().concat(p_serverName));
            if (!folder.exists())
                folder.mkdirs();
            return true;
        }
        catch (Exception e)
        {
            logger.error("Can not create the CVS server folder correctly. "
                    .concat(p_serverName));
            throw new IOException(
                    "Can not create the CVS server folder correctly.");
        }
    }

    public static boolean removeFolder(String p_serverName) throws IOException
    {
        File folder = null;

        try
        {
            folder = new File(getBaseDocRoot().concat(p_serverName));
            if (folder.exists())
                folder.delete();
            return true;
        }
        catch (Exception e)
        {
            logger.error("Can not remove the CVS server folder correctly. "
                    .concat(p_serverName));
            throw new IOException(
                    "Can not remove the CVS server folder correctly.");
        }

    }

    public static boolean createFolder(File p_file)
    {
        try
        {
            if (p_file == null || p_file.isFile())
                return false;
            if (p_file.exists())
                return true;
            p_file.mkdirs();
            return true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public static String convertSeprator(String p_str)
    {
        if (p_str == null || p_str.trim().equals(""))
            return p_str;

        p_str.replace("\\", "/");

        return p_str;
    }

    public static boolean setCVSEnv(String env)
    {

        return false;
    }

    public static String exeCmd(String[] cmd, String workDirectory)
            throws IOException, InterruptedException
    {
        String result = "";
        String line = "";
        BufferedReader in = null;
        StringBuilder s = new StringBuilder();

        try
        {
            for (String t : cmd)
            {
                s.append(t).append(" ");
            }
            result = s.toString().trim();
            ProcessBuilder builder = new ProcessBuilder(cmd);
            // set working directory
            if (workDirectory != null)
            {
                builder.directory(new File(workDirectory));
            }
            // set error stream
            builder.redirectErrorStream(true);
            // start ProcessBuilder
            Process process = builder.start();

            in = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            while ((line = in.readLine()) != null)
            {
                result += "\r\n" + line;
            }
            // get exitValue
            try
            {
                int exitValue = process.waitFor();
                line = "\r\n***** Exited normally with code " + exitValue
                        + " *****\r\n";
            }
            catch (Exception e)
            {
                line = "\r\n***** Exited failure *****\r\n" + e.getMessage();
            }
            result += "\r\n" + line;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }

        return result.trim();
    }

    public static String[] exeCVSCmd(String[] cmd, String workDirectory)
            throws IOException, InterruptedException
    {
        String[] returnValue = new String[2];
        String result = "";
        String line = "";
        BufferedReader in = null;
        StringBuilder s = new StringBuilder("CVS Command ==== ");

        try
        {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            // set working directory
            if (workDirectory != null)
            {
                builder.directory(new File(workDirectory));
            }
            // set error stream
            builder.redirectErrorStream(true);
            // start ProcessBuilder
            Process process = builder.start();

            in = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            while ((line = in.readLine()) != null)
            {
                result += "\n" + line;
            }
            // process.getOutputStream()
            // get exitValue
            int exitValue = -1;
            try
            {
                exitValue = process.waitFor();
                line = "\n***** Exited normally with code " + exitValue
                        + " *****\n\n";
                returnValue[0] = String.valueOf(exitValue);
            }
            catch (Exception e)
            {
                line = "\n***** Exited failure *****\n" + e.getMessage();
                System.out.println(e.getMessage());
                returnValue[0] = "-1";
            }
            result += "\n" + line;
            if (exitValue == 0)
            {
                logger.info("*******************CVS Output*******************");
                for (String t : cmd)
                {
                    s.append(t).append(" ");
                }
                logger.info(s.toString().trim());
                logger.info(result);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
        returnValue[1] = result;
        return returnValue;
    }

    public static int copyFiles(String p_source, String p_target)
    {
        logger.info("copyFiles(). Source file==" + p_source + ", Target file=="
                + p_target);
        File sourceFile = null, targetFile = null;
        BufferedOutputStream fout = null;
        BufferedInputStream fin = null;
        try
        {
            sourceFile = new File(p_source);
            if (!sourceFile.exists())
                return -1;
            targetFile = new File(p_target);
            if (!targetFile.exists())
                targetFile.getParentFile().mkdirs();
            fout = new BufferedOutputStream(new FileOutputStream(targetFile));
            fin = new BufferedInputStream(new FileInputStream(sourceFile));
            byte[] buf = new byte[4096];
            int count = 0;
            while ((count = fin.read(buf)) >= 0)
            {
                fout.write(buf, 0, count);
            }
            return 0;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return -1;
        }
        finally
        {
            try
            {
                fin.close();
                fout.close();
            }
            catch (IOException ioe)
            {
            }
        }
    }

    private static String getUploadPath(String p_jobName,
            String p_sourceLocale, Date p_uploadDate)
    {
        // format the time with server's default time zone
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm");

        StringBuffer sb = new StringBuffer();
        // sb.append(getCXEBaseDir());
        sb.append(AmbFileStoragePathUtils.getCxeDocDir());
        sb.append(File.separator);
        sb.append(p_sourceLocale);
        sb.append(File.separator);
        sb.append(p_jobName);
        sb.append("_");
        sb.append(sdf.format(p_uploadDate));
        return sb.toString();
    }

    public static ArrayList<String> saveData(CVSModule p_module,
            String p_jobName, String p_srcLocale, String p_projectId,
            String p_projectName, String p_notes, String p_files, User p_user)
            throws EnvoyServletException, IOException
    {

        ArrayList returnValue = new ArrayList();
        ArrayList<String> results = new ArrayList<String>();
        HashSet<String> results1 = new HashSet<String>();
        long uploadDateInLong = System.currentTimeMillis();
        Date uploadDate = new Date(uploadDateInLong);
        String uploadPath = getUploadPath(p_jobName, p_srcLocale, uploadDate);
        returnValue.add(uploadPath.substring(getBaseDocRoot().length()));
        try
        {
            // copy files
            String[] files = p_files.split(",");
            String fileName = "", sFile = "";
            File file = null;
            String toPath = "";
            ArrayList<File> subFiles = new ArrayList<File>();
            if (files.length > 0)
            {
                for (int i = 0; i < files.length; i++)
                {
                    sFile = files[i];
                    file = new File(sFile);
                    if (!file.isFile())
                        subFiles.addAll(getSubFiles(file));
                    else
                        subFiles.add(file);
                }
                for (int j = 0; j < subFiles.size(); j++)
                {
                    file = subFiles.get(j);
                    if (file == null)
                        continue;
                    sFile = file.getAbsolutePath();
                    // results.add(sFile);
                    fileName = sFile.substring(sFile
                            .lastIndexOf(File.separator));
                    String baseDocDir = getBaseDocRoot();
                    int baseDocLen = baseDocDir.length() - 1;
                    String targetFileName = sFile.substring(baseDocLen);
                    toPath = uploadPath + targetFileName;
                    File toFolder = new File(toPath);
                    toFolder.getParentFile().mkdirs();
                    if (fileName != null && !fileName.equals(""))
                    {
                        copyFiles(sFile, toPath);
                        results.add(toPath);
                        results1.add(toPath.substring(baseDocDir.length()));
                    }
                }
                // now update the job name to include the timestamp
                String newJobName = uploadPath.substring(
                        uploadPath.lastIndexOf(File.separator) + 1,
                        uploadPath.length());

                saveJobNote(newJobName, uploadDateInLong, p_notes, p_user);

                saveFileInfo(newJobName, p_notes, subFiles, p_module);
                /**
                 * sendUploadCompletedEmail(files, sessionMgr, uploadDate, tz,
                 * (Locale) p_session .getAttribute(WebAppConstants.UILOCALE));
                 */
            }

            returnValue.add(results);
            returnValue.add(results1);
            return returnValue;
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    private static void saveFileInfo(String p_jobName, String p_notes,
            ArrayList<File> p_files, CVSModule p_module)
    {
        ConnectionPool pool = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        CVSServerManagerLocal serverManager = new CVSServerManagerLocal();
        try
        {
            conn = ConnectionPool.getConnection();
            conn.setAutoCommit(false);
            pstmt = conn
                    .prepareStatement("INSERT INTO CVS_SOURCE_FILES (JOB_NAME, FILENAME, STATUS, MODULE_ID, JOB_NOTES) VALUES (?, ?, ?, ?, ?)");
            CVSModule module = serverManager.getModule(p_module.getId());
            if (p_files != null)
            {
                for (int i = 0; i < p_files.size(); i++)
                {
                    pstmt.setString(1, p_jobName);
                    pstmt.setString(2, p_files.get(i).getAbsolutePath());
                    pstmt.setInt(3, 1);
                    pstmt.setLong(4, module.getServer().getId());
                    pstmt.setString(5, p_notes);

                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            conn.commit();
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                pstmt.close();
                ConnectionPool.returnConnection(conn);
            }
            catch (Exception ioe)
            {
            }
        }

    }

    private static ArrayList<File> getSubFiles(File p_file)
    {
        ArrayList<File> files = new ArrayList<File>();
        if (p_file == null)
            return files;
        File[] sfile = p_file.listFiles();
        for (int i = 0; i < sfile.length; i++)
        {
            if (sfile[i].getName().equalsIgnoreCase("CVS"))
                continue;
            if (sfile[i].isDirectory())
            {
                files.addAll(getSubFiles(sfile[i]));
            }
            else
                files.add(sfile[i]);
        }
        return files;
    }

    private static void saveJobNote(String p_newJobName,
            long p_uploadDateInLong, String p_notes, User p_user)
    {
        // save job note into <docs_folder>\<company_name>\<newJobName>.txt
        // read it in
        // com.globalsight.everest.jobhandler.jobcreation.JobAdditionEngine.createNewJob()
        // the content format is <userName>,<Date long type>,<note>
        try
        {
            String jobNote = p_notes;
            if (jobNote != null && jobNote.trim().length() != 0)
            {
                File jobnotesFile = new File(
                        AmbFileStoragePathUtils.getCxeDocDir(), p_newJobName
                                + ".txt");
                if (jobnotesFile.exists())
                {
                    jobnotesFile.delete();
                }
                if (jobnotesFile.createNewFile())
                {
                    char token = ',';
                    User user = p_user;
                    // save name for security
                    StringBuffer sb = new StringBuffer(user.getUserName());
                    sb.append(token);
                    sb.append(p_uploadDateInLong);
                    sb.append(token);
                    sb.append(jobNote);
                    FileUtils.write(jobnotesFile, sb.toString(), "utf-8");
                }
            }
        }
        catch (Exception e)
        {
            // do nothing but write log, because this exception
            // is not important
            logger.info(
                    "Error when save "
                            + p_newJobName
                            + " job's notes (added when uploading) into GlobalSight File System",
                    e);
        }
    }

    public static HashMap<String, String> seperateFileInfo(String p_file,
            String p_exportLocation)
    {
        HashMap<String, String> infos = new HashMap<String, String>();
        if (p_file == null || p_file.trim().equals(""))
            return null;
        String baseDocRoot = p_exportLocation;
        int rootLength = baseDocRoot.length();
        infos.put("baseDocRoot", p_exportLocation);
        infos.put("source", p_file);

        String tmp = p_file.substring(rootLength + 1);
        String targetLocale = tmp.substring(0, tmp.indexOf(File.separator));
        infos.put("targetLocale", targetLocale);
        tmp = tmp.substring(targetLocale.length());
        tmp = tmp.substring(1);
        String jobIdOrName = tmp.substring(0, tmp.indexOf(File.separator));
        
        //Because DI cannot create CVS job for now,
        //so if the job path contains 'webservice', it means that it is NOT
        //CVS job
        boolean isJobFromDI = false;
        if ("webservice".equalsIgnoreCase(jobIdOrName)) {
            isJobFromDI = true;
            tmp = tmp.substring(tmp.indexOf(File.separator) + 1);
            jobIdOrName = tmp.substring(0, tmp.indexOf(File.separator));
        }
        
        String jobId = "";
        String jobName = "";
        Job job = null;
        try
        {
            job = ServerProxy.getJobHandler().getJobById(Long.parseLong(jobIdOrName));
        }
        catch (Exception e)
        {
            if (!isJobFromDI)
                logger.info("Current job [" + jobIdOrName + "] was created by old version.");
            try
            {
                jobName = jobIdOrName;
                job = ServerProxy.getJobHandler().getJobByJobName(jobIdOrName);
            }
            catch (Exception e1)
            {
                logger.error("Cannot get job info.", e1);
            }
        }
        if (isJobFromDI) {
            jobId = "";
            if (logger.isDebugEnabled())
            {
                logger.debug("current job [" + jobName + "] is not a CVS job.");            	
            }
        } else if (job != null) {
            jobId = String.valueOf(job.getJobId());
            jobName = job.getJobName();
        }
        
        infos.put("jobId", jobId);
        infos.put("jobName", jobName);
        if (!StringUtil.isEmpty(jobId)) {
            String moduleName = "";
            if ((jobIdOrName.length() + 1) < tmp.lastIndexOf(File.separator))
            {
                moduleName = tmp.substring(jobIdOrName.length() + 1,
                        tmp.lastIndexOf(File.separator));
            }
            String fileName = tmp.substring(tmp.lastIndexOf(File.separator) + 1);
            infos.put("sourceModulePath", moduleName);
            infos.put("filename", fileName);
            infos.put("sourceModule", moduleName + File.separator + fileName);
        }
        
        return infos;
    }

    public static long getCVSJobModuleId(String jobId)
    {
        if (StringUtil.isEmpty(jobId))
            return -1L;
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = ConnectionPool.getConnection();
            String sql = "select * from cvs_source_files where status=2 and job_id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, jobId);
            rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getLong("module_id");
            else
                return -1L;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return -1L;
        }
        finally
        {
            try
            {
                pstmt.close();
                ConnectionPool.returnConnection(conn);
            }
            catch (Exception e)
            {
            }
        }
    }

    private static String getCVSJobNote(String jobId)
    {
        if (StringUtil.isEmpty(jobId))
            return "";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String jobNotes = "";
        try
        {
            conn = ConnectionPool.getConnection();
            String sql = "select * from cvs_source_files where status=2 and job_id=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, jobId);
            rs = pstmt.executeQuery();
            if (rs.next())
                jobNotes = rs.getString("JOB_NOTES");
            return jobNotes;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return "";
        }
        finally
        {
            try
            {
                pstmt.close();
                ConnectionPool.returnConnection(conn);
            }
            catch (Exception e)
            {
            }
        }
    }

    public static boolean isCVSJob(String jobId)
    {
        long result = getCVSJobModuleId(jobId);
        logger.debug("isCVSJob===" + result);
        return result > 0;
    }

    /**
     * For CVS job, when export, copy target files to "sandbox" then commit to
     * CVS server according to module mapping.
     * */
    public static void saveCVSFile(HashMap<String, String> infos,
            String p_srcLocale)
    {
        try
        {
            if (infos == null || p_srcLocale == null)
                return;

            if (p_srcLocale.indexOf("(") == 0) {
                p_srcLocale = p_srcLocale.substring(
                        p_srcLocale.indexOf(") ") + 2);
            }
            String sourceLocale = p_srcLocale;
            String targetLocale = infos.get("targetLocale");
            String jobId = infos.get("jobId");
            String jobName = infos.get("jobName");
            String sourceFile = infos.get("source");
            String sourceModule = infos.get("sourceModule");
            String sourceModulePath = infos.get("sourceModulePath");
            String filename = infos.get("filename");

            // GBS-1368, for super user
            String companyIdStr = null;
            long companyId;
            if (CompanyWrapper.getCurrentCompanyId().equals("1"))
            {
                JobHandlerLocal jobHandlerLocal = new JobHandlerLocal();
                Job job = jobHandlerLocal.getJobByJobName(jobName);
                companyId = job.getCompanyId();
                companyIdStr = String.valueOf(companyId);
            }
            else
            {
                companyId = CompanyWrapper.getCurrentCompanyIdAsLong();
            }

            long moduleId = getCVSJobModuleId(jobId);
            String jobNotes = getCVSJobNote(jobId);

            ModuleMappingHelper helper = new ModuleMappingHelper();
            HashMap result = helper.getTargetModuleMapping(companyId,
                    sourceLocale, targetLocale, moduleId, sourceModule);
            String targetFile = "";
            String workDir = "";

            // GBS-1368 for super user
            if (CompanyWrapper.getCurrentCompanyId().equals("1"))
            {
                targetFile = getBaseDocRootByCompany(companyIdStr);
            }
            else
            {
                targetFile = getBaseDocRoot();
            }

            boolean isNoMapping = true;
            if (result != null && result.get("type") != null)
            {
                // Exists defined module mapping
                String type = (String) result.get("type");
                ModuleMapping mm = (ModuleMapping) result.get("moduleMapping");
                if ("0".equals(type))
                {
                    targetFile = targetFile.concat(mm.getTargetModule());
                    File t = new File(targetFile);
                    if (t.isDirectory())
                        targetFile += File.separator
                                + getMappingRename(mm, filename, "0");
                }
                else
                {
                    targetFile = targetFile.concat(mm.getTargetModule());
                    File t = new File(targetFile);
                    if (t.isDirectory())
                        targetFile += File.separator
                                + getMappingRename(mm, filename,
                                        mm.getSubFolderMapped());
                    else
                    {
                        filename = getMappingRename(mm, filename, "1");
                        targetFile = targetFile.concat(mm.getTargetModule())
                                .concat(File.separator).concat(filename);
                    }
                }
                isNoMapping = false;
                filename = targetFile.substring(targetFile
                        .lastIndexOf(File.separator) + 1);
                if (CompanyWrapper.getCurrentCompanyId().equals("1"))
                {
                    workDir = getBaseDocRootByCompany(companyIdStr).concat(
                            mm.getTargetModule());
                }
                else
                {
                    workDir = getBaseDocRoot().concat(mm.getTargetModule());
                }
            }
            else
            {
                targetFile = targetFile.concat(sourceModulePath)
                        .concat(File.separator).concat(targetLocale)
                        .concat(File.separator).concat(filename);
                if (CompanyWrapper.getCurrentCompanyId().equals("1"))
                {
                    workDir = getBaseDocRootByCompany(companyIdStr).concat(
                            sourceModulePath);
                }
                else
                {
                    workDir = getBaseDocRoot().concat(sourceModulePath);
                }
            }
            copyFiles(sourceFile, targetFile);
            logger.info("Output file in CVS::" + targetFile);
            CVSServerManagerLocal manager = new CVSServerManagerLocal();
            CVSServer server = manager.getServer(moduleId);
            String cvsRoot = server.getCVSRootEnv();

            File workDirFile = new File(workDir);
            if (!workDirFile.isDirectory())
                workDir = workDir.substring(0,
                        workDir.lastIndexOf(File.separator));

            String[] cmd = null;
            synchronized (workDirFile)
            {
                // When commit back files, cvsroot can't be specified,otherwise
                // there will be
                // "'Update-to-date' check failed for 'xxx'" error.
                if (isNoMapping)
                {
                    // Need to add target locale folder first
                    logger.info("No module mapping found! workDir == "
                            + workDir);
                    cmd = new String[]
                    { "cvs", "-d", cvsRoot, "add", targetLocale };
                    exeCVSCmd(cmd, workDir);
                    cmd = new String[]
                    { "cvs", "ci", "-m", "\"" + jobNotes + "\"", targetLocale };
                    exeCVSCmd(cmd, workDir);
                    workDir += File.separator + targetLocale;

                    if (isBashFileType(filename))
                        cmd = new String[]
                        { "cvs", "-d", cvsRoot, "add", "-kb", filename };
                    else
                        cmd = new String[]
                        { "cvs", "-d", cvsRoot, "add", filename };
                    exeCVSCmd(cmd, workDir);
                    cmd = new String[]
                    { "cvs", "ci", "-m", "\"" + jobNotes + "\"", filename };
                    exeCVSCmd(cmd, workDir);
                }
                else
                {
                    workDir = targetFile.substring(0,
                            targetFile.lastIndexOf(File.separator));
                    logger.info("Found module mapping. WorkDir == " + workDir);
                    if (isBashFileType(filename))
                        cmd = new String[]
                        { "cvs", "-d", cvsRoot, "add", "-kb", filename };
                    else
                        cmd = new String[]
                        { "cvs", "-d", cvsRoot, "add", filename };
                    exeCVSCmd(cmd, workDir);
                    cmd = new String[]
                    { "cvs", "ci", "-m", "\"" + jobNotes + "\"", filename };
                    exeCVSCmd(cmd, workDir);
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }

    private static boolean isBashFileType(String filename) {
        if (StringUtil.isEmpty(filename))
            return false;
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if (StringUtil.isEmpty(extension))
            return false;
        
        for (String ext : bashFileType) {
            if (ext.equalsIgnoreCase(extension))
                return true;
        }
        return false;
    }
    
    private static String getMappingRename(ModuleMapping p_mm,
            String p_filename, String p_flag)
    {
        if (p_mm == null)
            return "";
        String result = "";
        try
        {
            ModuleMappingManagerLocal manager = new ModuleMappingManagerLocal();
            ModuleMapping kmm = manager.getModuleMapping(
                    p_mm.getSourceLocale(), p_mm.getTargetLocale(),
                    p_mm.getSourceModule(), p_mm.getTargetModule(),
                    String.valueOf(p_mm.getCompanyId()), p_flag);
            if (kmm != null)
            {
                Set<ModuleMappingRename> renames = kmm.getFileRenames();
                String source = "", target = "";
                for (ModuleMappingRename r : renames)
                {
                    source = r.getSourceName();
                    target = r.getTargetName();
                    if (source.indexOf("*") != -1 && target.indexOf("*") != -1)
                    {
                        result = rename(p_filename, source, target);
                        if (!result.equals(p_filename))
                            break;
                    }
                    else
                        result = p_filename.replaceAll(r.getSourceName(),
                                r.getTargetName());
                }
            }
            if ("".equals(result))
                return p_filename;
            else
                return result;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return "";
        }
    }

    private static String rename(String p_filename, String p_source,
            String p_target)
    {
        if (p_source.indexOf("\\*") != -1 || p_target.indexOf("\\*") != -1)
            return p_filename;
        try
        {
            String[] tmpA = p_source.split("\\*");
            String[] tmpB = p_target.split("\\*");
            String tmp = "";
            int l = 0, k = 0;
            for (int i = 0; i < tmpA.length; i++)
            {
                l = p_filename.indexOf(tmpA[i]);
                if (l != -1)
                {
                    tmp += p_filename.substring(k, l).concat(
                            tmpB[i].equals("") ? tmpA[i] : tmpB[i]);
                    l = l + tmpA[i].length();
                    k = l;
                }
                else
                {
                    tmp = p_filename;
                    break;
                }
            }
            return tmp;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return p_filename;
        }
    }

    public static boolean isCVSEnabled()
    {
        BufferedReader in = null;
        String workDirectory = ".";
        try
        {
            ProcessBuilder builder = new ProcessBuilder(new String[]
            { "cvs" });
            // set working directory
            if (workDirectory != null)
            {
                builder.directory(new File(workDirectory));
            }
            // set error stream
            builder.redirectErrorStream(true);
            // start ProcessBuilder
            Process process = builder.start();

            return true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return false;
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (Exception ie)
            {
            }
        }
    }

    public static ArrayList<String[]> getModuleList(String[] cmd,
            String workDirectory) throws IOException, InterruptedException
    {
        ArrayList<String[]> result = new ArrayList<String[]>();
        String[] tmp = null;
        String line = "";
        BufferedReader in = null;

        try
        {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            // set working directory
            if (workDirectory != null)
            {
                builder.directory(new File(workDirectory));
            }
            // set error stream
            builder.redirectErrorStream(true);
            // start ProcessBuilder
            Process process = builder.start();

            in = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            while ((line = in.readLine()) != null)
            {
                tmp = splitBySpace(line);
                if (tmp.length < 2 || tmp.length > 8)
                    continue;
                try
                {
                    if ("(directory)".equals(tmp[1].trim()))
                        result.add(tmp);
                    else
                    {
                        Float.parseFloat(tmp[1]);
                        result.add(tmp);
                    }
                }
                catch (Exception e)
                {
                }
            }
            // get exitValue
            try
            {
                int exitValue = process.waitFor();
                line = "\n***** Exited normally with code " + exitValue
                        + " *****\n\n";
            }
            catch (Exception e)
            {
                line = "\n***** Exited failure *****\n" + e.getMessage();
                logger.error(e.getMessage(), e);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }

        return result;
    }

    private static String[] splitBySpace(String p_str)
    {
        if (p_str == null || p_str.trim().equals(""))
            return new String[]
            { p_str };
        ArrayList<String> list = new ArrayList<String>();
        String[] tmp = p_str.split(" ");
        for (String t : tmp)
        {
            if ("".equals(t.trim()))
                continue;
            list.add(t);
        }
        tmp = new String[list.size()];
        list.toArray(tmp);
        return tmp;
    }

    public static boolean isCheckedOut(String p_serverName, String p_moduleName)
    {
        boolean result = false;
        try
        {
            File f = new File(getBaseDocRoot() + p_serverName + File.separator
                    + p_moduleName);
            if (f.list() != null && f.list().length > 0)
                result = true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        return result;
    }
}
