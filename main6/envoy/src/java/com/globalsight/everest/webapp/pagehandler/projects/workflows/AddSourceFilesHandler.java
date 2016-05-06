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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.config.UserParameter;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.glossaries.GlossaryException;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;

import de.innosystec.unrar.rarfile.FileHeader;

public class AddSourceFilesHandler extends PageHandler
{
    public static final String TMP_FOLDER_NAME = "addFiles_tmp";

    private static final Logger logger = Logger.getLogger(AddSourceFilesHandler.class);
    private static final String DOT = ".";
    private static final String DISABLED = "disabled";
    private static final String SELECTED = "selected";
    private static final String SELECTED_FOLDER = "selected_folder_path_in_create_job";
    private final static int MAX_LINE_LENGTH = 4096;
    private Map<String, List<FileProfileImpl>> extensionToFileProfileMap;
    
    @Override
    public void invokePageHandler(WebPageDescriptor pageDescriptor, HttpServletRequest request,
            HttpServletResponse response, ServletContext context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        // get the operator
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }
        ResourceBundle bundle = PageHandler.getBundle(session);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        String action = request.getParameter(UPLOAD_ACTION);
        if (action != null)
        {
          if (action.equals("queryFileProfile"))
            {
                this.setPageParameter(request, bundle, user, session, currentCompanyId);
                this.queryFileProfile(request, response, currentCompanyId, user);
                return;
            }
            else if (action.equals("deleteFile"))
            {
                this.deleteFile(request);
                return;
            }
            else if (action.equals("uploadSelectedFile"))
            {
                String tempFolder = request.getParameter("tempFolder");
                List<File> uploadedFiles = new ArrayList<File>();
                try
                {
                    uploadedFiles = uploadSelectedFile(request, tempFolder);
                    for (File uploadedFile : uploadedFiles)
                    {
                        StringBuffer ret = new StringBuffer("[");
                        response.setContentType("text/html;charset=UTF-8");
                        if (isSupportedZipFileFormat(uploadedFile) && isUnCompress(uploadedFile))
                        {
                            if (CreateJobUtil.isZipFile(uploadedFile))
                            {
                                ret.append(addZipFile(uploadedFile));
                            }
                            else if (CreateJobUtil.isRarFile(uploadedFile))
                            {
                                ret.append(addRarFile(uploadedFile));
                            }
                            else if (CreateJobUtil.is7zFile(uploadedFile))
                            {
                                ret.append(addZip7zFile(uploadedFile));
                            }
                            else
                            {
                                ret.append(addCommonFile(uploadedFile));
                            }

                            ret.append("]");
                            PrintWriter writer = response.getWriter();
                            writer.write("<script type='text/javascript'>window.parent.addDivForNewFile("
                                    + ret.toString() + ")</script>;");
                            if (CreateJobUtil.isZipFile(uploadedFile)
                                    || CreateJobUtil.isRarFile(uploadedFile)
                                    || (CreateJobUtil.is7zFile(uploadedFile)))
                            {
                                uploadedFile.delete();
                            }
                        }
                        else
                        {
                            ret.append(addCommonFile(uploadedFile));
                            ret.append("]");
                            PrintWriter writer = response.getWriter();
                            writer.write("<script type='text/javascript'>window.parent.addDivForNewFile("
                                    + ret.toString() + ")</script>;");
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }
            else if (action.equals("addFile"))
            {
                addFiles(request, currentCompanyId,user);
            }
        }
        else
        {
            try
            {
                String jobId = (String) request.getParameter("jobId");
                Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(jobId));
                request.setAttribute("Job", job);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
          
        }
        super.invokePageHandler(pageDescriptor, request, response, context);
    }
    
    
    private void addFiles(HttpServletRequest request, String currentCompanyId, User user)
    {
        try
        {
            Vector<FileProfileImpl> fileProfiles = new Vector<FileProfileImpl>();
            Vector<String> locales = new Vector<String>();
            Vector<File> files = new Vector<File>();
            long jobId = Long.valueOf(request.getParameter("jobId"));
            String tmpFolderName = request.getParameter("tmpFolderName");
            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
            HibernateUtil.getSession().refresh(job);
            request.setAttribute("Job", job);
            CompanyThreadLocal.getInstance().setIdValue(currentCompanyId);
            StringBuffer allLocales = new StringBuffer();
            for (Workflow wf : job.getWorkflows())
            {
                if (allLocales.length() > 0)
                {
                    allLocales.append(",");
                }
                allLocales.append(wf.getTargetLocale().toString());
            }
            String[] l10nAndfileProfiles = request.getParameterValues("fileProfile");
            List<Long> fpIds = new ArrayList<Long>();
            for (int i = 0; i < l10nAndfileProfiles.length; i++)
            {
                String profileValue = l10nAndfileProfiles[i];
                String[] l10nAndFp = profileValue.split(",");
                long fpId = Long.parseLong(l10nAndFp[1]);
                fpIds.add(fpId);
            }

            for (Long id : fpIds)
            {
                fileProfiles.add(HibernateUtil.get(FileProfileImpl.class, id, false));
                locales.add(allLocales.toString());
            }
            String locale = job.getSourceLocale().toString();
            Boolean fromDi = null;
            File root = AmbFileStoragePathUtils.getCxeDocDir();
            String[] filePaths = request.getParameterValues("jobFilePath");
            for (String filePath : filePaths)
            {
                filePath = convertFilePath(filePath);
                if (filePath.contains(tmpFolderName))
                {
                    filePath = filePath.substring(filePath.indexOf(tmpFolderName) + tmpFolderName.length());
                }
                String currentLocation = root + File.separator + TMP_FOLDER_NAME + File.separator
                        + tmpFolderName + File.separator + filePath;
                String destinationLocation = locale + File.separator + job.getId() + File.separator
                        + filePath;
                
                if (currentLocation.contains(TMP_FOLDER_NAME))
                {
                    File sourceFile = new File(currentLocation);
                    File descFile = new File(root + File.separator + destinationLocation);
                    try
                    {
                        FileUtil.copyFile(sourceFile, descFile);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                File targetFile = new File(root + File.separator + destinationLocation);
                if (!targetFile.exists())
                {
                    String newPath = new StringBuffer(locale).append(File.separator)
                            .append(job.getName()).append(File.separator).append(destinationLocation).toString();
                    targetFile = new File(root, newPath);
                }
                else if (fromDi == null)
                {
                    destinationLocation = destinationLocation.replace("\\", "/");
                    String[] nodes = destinationLocation.split("/");
                    fromDi = nodes.length > 1 && "webservice".equals(nodes[1]);
                }
                files.add(targetFile);
            }
            String username = job.getCreateUser().getUserName();
            String jobName = job.getName();

            Vector result = FileSystemUtil.execScript(files, fileProfiles, locales, jobId, jobName);
            Vector sFiles = (Vector) result.get(0);
            Vector exitValues = (Vector) result.get(3);

            int addedCount = job.getRequestSet().size();
            int pageCount = sFiles.size();

            List<Request> requests = new ArrayList<Request>();
            requests.addAll(job.getRequestSet());
            SortUtil.sort(requests, new Comparator<Request>()
            {
                @Override
                public int compare(Request o1, Request o2)
                {
                    BatchInfo info1 = o1.getBatchInfo();
                    BatchInfo info2 = o2.getBatchInfo();

                    if (info1 != null && info2 != null)
                    {
                        return info1.getPageNumber() > info2.getPageNumber() ? 1 : -1;
                    }

                    return 0;
                }
            });

            for (int i = 0; i < requests.size(); i++)
            {
                Request p_request = requests.get(i);
                BatchInfo info = p_request.getBatchInfo();

                info.setPageCount(addedCount + pageCount);
                info.setPageNumber(i + 1);
                info.setDocPageCount(1);
                info.setDocPageNumber(1);
            }

            String orgState = job.getState();
            job.setState(Job.ADD_FILE);
            job.setOrgState(orgState);
            HibernateUtil.update(job);

            for (int i = 0; i < pageCount; i++)
            {
                File realFile = (File) sFiles.get(i);
                String path = realFile.getPath();
                String relativeName = path.substring(AmbFileStoragePathUtils.getCxeDocDir()
                        .getPath().length() + 1);

                try
                {
                    String key = jobName + relativeName + (addedCount + i + 1);
                    CxeProxy.setTargetLocales(key, locales.get(i));
                    logger.info("Publishing import request to CXE for file " + relativeName);
                    CxeProxy.importFromFileSystem(relativeName, job.getId(), jobName,
                            job.getUuid(), jobName, "" + fileProfiles.get(i).getId(), new Integer(
                                    addedCount + pageCount), new Integer(addedCount + i + 1),
                            new Integer(1), new Integer(1), fromDi, Boolean.FALSE,
                            CxeProxy.IMPORT_TYPE_L10N, username, (Integer) exitValues.get(i), ""
                                    + job.getPriority());
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }

            // after all steps, delete files that are used to create job
            for (String tmpPath : filePaths)
            {
                File tmpFile = new File(tmpPath);
                FileUtil.deleteFile(tmpFile);
            }
            // delete the upload directory
            File uploads = new File(AmbFileStoragePathUtils.getCxeDocDir() + File.separator
                    + TMP_FOLDER_NAME + File.separator + tmpFolderName);
            if (uploads != null && uploads.exists())
            {
                FileUtil.deleteFile(uploads);
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
    
    /**
     * Called by ajax, to search the file profiles for files, and init all file
     * profile selects on the jsp.
     * 
     * @param request
     * @param response
     * @param currentCompanyId
     * @param user
     * @throws IOException
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private void queryFileProfile(HttpServletRequest request, HttpServletResponse response,
            String currentCompanyId, User user) throws IOException
    {
        try
        {
            String fileName = request.getParameter("fileName");
            String l10nIdStr = request.getParameter("l10Nid");
            long l10nId = 0;
            if (!StringUtils.isEmpty(l10nIdStr))
            {
                l10nId = Long.parseLong(l10nIdStr);
            }

            if (fileName != null && fileName.contains(DOT))
            {
                String fileExtension = fileName.substring(fileName.lastIndexOf(DOT) + 1);
                List<FileProfileImpl> fileProfileListOfUser;

                if (extensionToFileProfileMap.get(fileExtension) != null)
                {
                    fileProfileListOfUser = extensionToFileProfileMap.get(fileExtension);
                }
                else
                {
                    fileProfileListOfUser = new ArrayList<FileProfileImpl>();
                    List<String> extensionList = new ArrayList<String>();
                    extensionList.add(fileExtension);
                    List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                            .getFileProfilePersistenceManager().getFileProfilesByExtension(
                                    extensionList, Long.valueOf(currentCompanyId));
                    SortUtil.sort(fileProfileListOfCompany, new Comparator<Object>()
                    {
                        public int compare(Object arg0, Object arg1)
                        {
                            FileProfileImpl a0 = (FileProfileImpl) arg0;
                            FileProfileImpl a1 = (FileProfileImpl) arg1;
                            return a0.getName().compareToIgnoreCase(a1.getName());
                        }
                    });

                    List projectsOfCurrentUser = ServerProxy.getProjectHandler()
                            .getProjectsByUser(user.getUserId());

                    for (FileProfileImpl fp : fileProfileListOfCompany)
                    {
                        Project fpProj = getProject(fp);
                        // get the project and check if it is in the group of
                        // user's projects
                        if (projectsOfCurrentUser.contains(fpProj)&&fp.getL10nProfileId()==l10nId)
                        {
                            fileProfileListOfUser.add(fp);
                        }
                    }
                    extensionToFileProfileMap.put(fileExtension, fileProfileListOfUser);
                }

                if (fileProfileListOfUser.size() > 0)
                {
                    // the return value should be in the pattern of html
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    this.initFileProfileSelect(fileProfileListOfUser, writer, l10nId);
                    writer.close();
                }
            }
            else
            {
                logger.warn("The file " + fileName + " doesn't have a extension.");
            }
        }
        catch (Exception e)
        {
            logger.error("Query fileprofile error.", e);
        }
    }

    /**
     * Set useful parameters onto the jsp
     * 
     * @param request
     * @param bundle
     * @param user
     * @param session
     * @param currentCompanyId
     */
    private void setPageParameter(HttpServletRequest request, ResourceBundle bundle, User user,
            HttpSession session, String currentCompanyId)
    {
        request.setAttribute("rand", session.getAttribute("UID_" + session.getId()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String tmpFolderName = sdf.format(new Date()) + "-" + getRandomNumber();
        if (user != null)
        {
            request.setAttribute("lastSelectedFolder",
                    convertFilePath(getLastSelectedFolder(user.getUserId(), SELECTED_FOLDER))
                            .replace("\\", "\\\\"));
        }
        else
        {
            request.setAttribute("lastSelectedFolder", "");
        }

        request.setAttribute("baseTmpFolder",
                convertFilePath(
                        AmbFileStoragePathUtils.getCxeDocDir() + File.separator + TMP_FOLDER_NAME)
                                .replace("\\", "\\\\"));
        request.setAttribute("baseStorageFolder", tmpFolderName + "," + currentCompanyId);

        if (request.getParameter("currentFolderName") != null)
        {
            request.setAttribute("tmpFolderName",
                    convertFilePath(request.getParameter("currentFolderName")));
        }
        else
        {
            request.setAttribute("tmpFolderName", tmpFolderName);
            extensionToFileProfileMap = new HashMap<String, List<FileProfileImpl>>();
        }
        SystemConfiguration sysConfig = SystemConfiguration.getInstance();
        boolean useSSL = sysConfig.getBooleanParameter(SystemConfigParamNames.USE_SSL);
        if (useSSL)
        {
            request.setAttribute("httpProtocolToUse", WebAppConstants.PROTOCOL_HTTPS);
        }
        else
        {
            request.setAttribute("httpProtocolToUse", WebAppConstants.PROTOCOL_HTTP);
        }
    }

    /**
     * Try to decompress "zip", "rar" or "7z" file to see if it can be
     * decompressed successfully.
     */
    private boolean isUnCompress(File uploadedFile) throws Exception
    {
        boolean result = false;
        if (CreateJobUtil.isZipFile(uploadedFile))
        {
            result = CreateJobUtil.unzipFile(uploadedFile);
        }
        else if (CreateJobUtil.isRarFile(uploadedFile))
        {
            result = CreateJobUtil.unrarFile(uploadedFile);
        }
        else if (CreateJobUtil.is7zFile(uploadedFile))
        {
            result = CreateJobUtil.un7zFile(uploadedFile);
        }

        return result;
    }

    private boolean isSupportedZipFileFormat(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if ("rar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)
                || "7z".equalsIgnoreCase(extension))
        {
            return true;
        }
        return false;
    }

    private List<File> uploadSelectedFile(HttpServletRequest request, String tempFolder) throws GlossaryException, IOException
    {
        File parentFile = null;
        List<String> fileNames = new ArrayList<String>();
        List<File> uploadedFiles = new ArrayList<File>();
        File saveDir = AmbFileStoragePathUtils.getCxeDocDir();
        String baseTmpDir = saveDir + File.separator + TMP_FOLDER_NAME;
        parentFile = new File(baseTmpDir + File.separator + tempFolder);
        parentFile.mkdirs();

        fileNames = uploadFile(request, parentFile);
        for (String fileName : fileNames)
        {
            File uploadedFile = new File(fileName);
            uploadedFiles.add(uploadedFile);
        }
        return uploadedFiles;
    }
    
    private List<String> uploadFile(HttpServletRequest p_request, File parentFile)
            throws GlossaryException, IOException
    {
        byte[] inBuf = new byte[MAX_LINE_LENGTH];
        int bytesRead;
        ServletInputStream in;
        String contentType;
        String boundary;
        String filePath = "";
        String path = parentFile.getPath() + File.separator;
        List<String> filePaths = new ArrayList<String>();
        File file = new File(path);
        Set<String> uploadedFileNames = new HashSet<String>();
        for (File f : file.listFiles())
        {
            uploadedFileNames.add(f.getName());
        }

        // Let's make sure that we have the right type of content
        //
        contentType = p_request.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            String[] arg =
            { "form did not use ENCTYPE=multipart/form-data but `" + contentType + "'" };

            throw new GlossaryException(GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
        }

        // Extract the boundary string in this request. The
        // boundary string is part of the content type string
        //
        int bi = contentType.indexOf("boundary=");
        if (bi == -1)
        {
            String[] arg =
            { "no boundary string found in request" };

            throw new GlossaryException(GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
        }
        else
        {
            // 9 := len("boundary=")
            boundary = contentType.substring(bi + 9);

            // The real boundary has additional two dashes in
            // front
            //
            boundary = "--" + boundary;
        }

        in = p_request.getInputStream();
        bytesRead = in.readLine(inBuf, 0, inBuf.length);

        if (bytesRead < 3)
        {
            String[] arg =
            { "incomplete request (not enough data)" };

            // Not enough content was send as part of the post
            throw new GlossaryException(GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
        }

        while (bytesRead != -1)
        {
            String lineRead = new String(inBuf, 0, bytesRead, "utf-8");
            if (lineRead.startsWith("Content-Disposition: form-data; name=\""))
            {
                if (lineRead.indexOf("filename=\"") != -1)
                {
                    // Get file name
                    String fileName = getFilename(lineRead.substring(0, lineRead.length() - 2));

                    // Get content type line
                    bytesRead = in.readLine(inBuf, 0, inBuf.length);
                    lineRead = new String(inBuf, 0, bytesRead - 2, "utf-8");

                    // Read and ignore the blank line
                    bytesRead = in.readLine(inBuf, 0, inBuf.length);

                    // Create a temporary file to store the
                    // contents in it for now. We might not have
                    // additional information, such as TUV id for
                    // building the complete file path. We will
                    // save the contents in this file for now and
                    // finally rename it to correct file name.
                    //

                    // if a file with same name has been uploaded, ignore this
                    if (uploadedFileNames.contains(fileName))
                    {
                        continue;
                    }

                    filePath = path + fileName;
                    filePaths.add(filePath);
                    File m_tempFile = new File(filePath);
                    FileOutputStream fos = new FileOutputStream(m_tempFile);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, MAX_LINE_LENGTH * 4);

                    // Read through the file contents and write
                    // it out to a local temp file.
                    boolean writeRN = false;
                    while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
                    {
                        // Let's first check if we are already on
                        // boundary line
                        if (bytesRead > 2 && inBuf[0] == '-' && inBuf[1] == '-')
                        {
                            lineRead = new String(inBuf, 0, bytesRead, "utf-8");
                            if (lineRead.startsWith(boundary))
                            {
                                break;
                            }
                        }

                        // Write out carriage-return, new-line
                        // pair which might have been left over
                        // from last write.
                        //
                        if (writeRN)
                        {
                            bos.write(new byte[]
                            { (byte) '\r', (byte) '\n' });
                            writeRN = false;
                        }

                        // The ServletInputStream.readline() adds
                        // "\r\n" bytes for the last line of the
                        // file contents. If we find these pair
                        // as the last bytes we need to delay
                        // writing it until the next go, since it
                        // could very well be the last line of
                        // file content.
                        //
                        // since GBS-3830, do not write \r\n in the last line
                        if (bytesRead >= 2 && inBuf[bytesRead - 2] == '\r'
                                && inBuf[bytesRead - 1] == '\n')
                        {
                            bos.write(inBuf, 0, bytesRead - 2);
                            writeRN = true;
                        }
                        else
                        {
                            bos.write(inBuf, 0, bytesRead);
                        }
                    }

                    bos.flush();
                    bos.close();
                    fos.close();
                }
                else
                {
                    // This is the field part

                    // First get the field name

                    // int start = lineRead.indexOf("name=\"");
                    // int end = lineRead.indexOf("\"", start + 7);
                    // String fieldName = lineRead.substring(start + 6, end);

                    // Read and ignore the blank line
                    bytesRead = in.readLine(inBuf, 0, inBuf.length);

                    // String Buffer to keep the field value
                    //
                    StringBuffer fieldValue = new StringBuffer();

                    boolean writeRN = false;
                    while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
                    {
                        lineRead = new String(inBuf, 0, bytesRead, "utf-8");

                        // Let's first check if we are already on
                        // boundary line
                        //
                        if (bytesRead > 2 && inBuf[0] == '-' && inBuf[1] == '-')
                        {
                            if (lineRead.startsWith(boundary))
                            {
                                break;
                            }
                        }

                        // Write out carriage-return, new-line
                        // pair which might have been left over
                        // from last write.
                        //
                        if (writeRN)
                        {
                            fieldValue.append("\r\n");
                            writeRN = false;
                        }

                        // The ServletInputStream.readline() adds
                        // "\r\n" bytes for the last line of the
                        // field value. If we find these pair as
                        // the last bytes we need to delay
                        // writing it until the next go, since it
                        // could very well be the last line of
                        // field value.
                        //
                        if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
                                && inBuf[bytesRead - 1] == '\n')
                        {
                            fieldValue.append(lineRead.substring(0, lineRead.length() - 2));
                            writeRN = true;
                        }
                        else
                        {
                            fieldValue.append(lineRead);
                        }
                    }
                }
            }

            bytesRead = in.readLine(inBuf, 0, inBuf.length);
        }
        return filePaths;
    }

    private String getFilename(String p_filenameLine)
    {
        int start = 0;

        if (p_filenameLine != null && (start = p_filenameLine.indexOf("filename=\"")) != -1)
        {
            String filepath = p_filenameLine.substring(start + 10, p_filenameLine.length() - 1);

            // Handle Windows v/s Unix file path
            if ((start = filepath.lastIndexOf('\\')) > -1)
            {
                return filepath.substring(start + 1);
            }
            else if ((start = filepath.lastIndexOf('/')) > -1)
            {
                return filepath.substring(start + 1);
            }
            else
            {
                return filepath;
            }
        }
        return null;
    }
    
    /**
     * Add a progress bar for each files within a zip file.
     * 
     * @param file
     * @throws Exception
     */
    private String addZipFile(File file) throws Exception
    {
        String zipFileFullPath = file.getPath();
        String zipFilePath = zipFileFullPath.substring(0, zipFileFullPath.indexOf(file.getName()));

        List<net.lingala.zip4j.model.FileHeader> entriesInZip = CreateJobUtil
                .getFilesInZipFile(file);

        StringBuffer ret = new StringBuffer("");
        for (net.lingala.zip4j.model.FileHeader entry : entriesInZip)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zipEntryName = entry.getFileName();
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zipFilePath
                    + file.getName().substring(0, file.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(file) + File.separator + zipEntryName;
            // if zip file contains subfolders, entry name will contains "/" or
            // "\"
            if (zipEntryName.indexOf("/") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);
            }
            else if (zipEntryName.indexOf("\\") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName.lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'").append(id).append("',zipName:'")
                    .append(file.getName().replace("'", "\\'")).append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(zipEntryName.replace("'", "\\'")).append("',size:'")
                    .append(entry.getUncompressedSize()).append("'}");
        }
        return ret.toString();
    }

    /**
     * Add a progress bar for each files within a rar file.
     * 
     * @param file
     */
    private String addRarFile(File file) throws Exception
    {
        String rarEntryName = null;
        String rarFileFullPath = file.getPath();
        String rarFilePath = rarFileFullPath.substring(0, rarFileFullPath.indexOf(file.getName()));

        List<FileHeader> entriesInRar = CreateJobUtil.getFilesInRarFile(file);

        StringBuffer ret = new StringBuffer("");
        for (FileHeader header : entriesInRar)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            if (header.isUnicode())
            {
                rarEntryName = header.getFileNameW();
            }
            else
            {
                rarEntryName = header.getFileNameString();
            }
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = rarFilePath
                    + file.getName().substring(0, file.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(file) + File.separator + rarEntryName;
            // if zip file contains subfolders, entry name will contains "/" or
            // "\"
            if (rarEntryName.indexOf("/") != -1)
            {
                rarEntryName = rarEntryName.substring(rarEntryName.lastIndexOf("/") + 1);
            }
            else if (rarEntryName.indexOf("\\") != -1)
            {
                rarEntryName = rarEntryName.substring(rarEntryName.lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'").append(id).append("',zipName:'")
                    .append(file.getName().replace("'", "\\'")).append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(rarEntryName.replace("'", "\\'")).append("',size:'")
                    .append(header.getDataSize()).append("'}");
        }
        return ret.toString();
    }

    /**
     * Add a progress bar for each files within a 7z file.
     * 
     * @param file
     * @throws Exception
     */
    private String addZip7zFile(File file) throws Exception
    {
        String zip7zFileFullPath = file.getPath();
        String zip7zFilePath = zip7zFileFullPath.substring(0,
                zip7zFileFullPath.indexOf(file.getName()));

        List<SevenZArchiveEntry> entriesInZip7z = CreateJobUtil.getFilesIn7zFile(file);

        StringBuffer ret = new StringBuffer("");
        for (SevenZArchiveEntry item : entriesInZip7z)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zip7zEntryName = item.getName();
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zip7zFilePath
                    + file.getName().substring(0, file.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(file) + File.separator + zip7zEntryName;
            // if zip file contains subf,olders, entry name will contains "/" or
            // "\"
            if (zip7zEntryName.indexOf("/") != -1)
            {
                zip7zEntryName = zip7zEntryName.substring(zip7zEntryName.lastIndexOf("/") + 1);
            }
            else if (zip7zEntryName.indexOf("\\") != -1)
            {
                zip7zEntryName = zip7zEntryName.substring(zip7zEntryName.lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'").append(id).append("',zipName:'")
                    .append(file.getName().replace("'", "\\'")).append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(zip7zEntryName.replace("'", "\\'"))
                    .append("',size:'").append(item.getSize()).append("'}");
        }
        return ret.toString();
    }

    /**
     * Add a progress bar for a common file.
     * 
     * @param file
     */
    private String addCommonFile(File file)
    {
        String id = CreateJobUtil.getFileId(file.getPath());
        StringBuffer ret = new StringBuffer("");
        ret.append("{id:'").append(id).append("',zipName:'")
                .append(file.getName().replace("'", "\\'")).append("',path:'")
                .append(file.getPath().replace("\\", "\\\\").replace("'", "\\'")).append("',name:'")
                .append(file.getName().replace("'", "\\'")).append("',size:'").append(file.length())
                .append("'}");
        return ret.toString();
    }
    
    /**
     * Get the project that the file profile is associated with.
     * 
     * @param p_fp
     *            File profile information
     * @return Project Project information which is associated with specified
     *         file profile
     */
    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error("Failed to get the project that file profile " + p_fp.toString()
                    + " is associated with.", e);
        }
        return p;
    }
    
    /**
     * Init the select box of each files. Files in a same job must map the
     * target locales in the same localization profiles. All the other file
     * profiles under other localizations must be disabled.
     * 
     * @param fileProfileList
     * @param writer
     * @throws IOException
     */
    private void initFileProfileSelect(List<?> fileProfileList, PrintWriter writer, long l10nId)
            throws IOException
    {
        if (fileProfileList.size() == 1)
        {
            FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(0);
            if (l10nId == 0)
            {
                l10nId = fp.getL10nProfileId();
            }
            if (fp.getL10nProfileId() == l10nId)
            {
                // if file profile has the same localization profile, select it.
                writer.write(initFileProfileOption(fp, SELECTED));
            }
            else
            {
                // if file profile has different l10n, it should be disabled.
                writer.write(initFileProfileOption(fp, DISABLED));
            }
        }
        else
        {
            int enableCount = countAvailableOptionCounts(fileProfileList, l10nId);
            for (int i = 0; i < fileProfileList.size(); i++)
            {
                FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(i);
                if (l10nId == 0)
                {
                    // just add the options, don't select.
                    writer.write(initFileProfileOption(fp, null));
                }
                else if (fp.getL10nProfileId() == l10nId)
                {
                    if (enableCount == 1)
                    {
                        // select the only 1 proper file profile
                        writer.write(initFileProfileOption(fp, SELECTED));
                    }
                    else
                    {
                        // just add the options, don't select.
                        writer.write(initFileProfileOption(fp, null));
                    }
                }
                else
                {
                    // disable other file profiles under other localization
                    // profiles
                    writer.write(initFileProfileOption(fp, DISABLED));
                }
            }
        }
    }
    
    /**
     * Init a single file profile option for the file profile select.
     * 
     * @param fp
     * @param flag
     * @return
     */
    private String initFileProfileOption(FileProfileImpl fp, String flag)
    {
        StringBuffer option = new StringBuffer();
        option.append("<option title=\"");
        if (StringUtils.isEmpty(fp.getDescription()))
        {
            option.append(fp.getName());
        }
        else
        {
            option.append(replaceSpecialCharacters(fp.getDescription()));
        }
        option.append("\" value=\"").append(fp.getL10nProfileId()).append(",").append(fp.getId())
                .append("\"");
        if (flag != null)
        {
            option.append(" ").append(flag).append("=\"true\"");
        }
        option.append(">").append(fp.getName()).append("</option>");
        return option.toString();
    }
    
    private String replaceSpecialCharacters(String input)
    {
        return input.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
    
    /**
     * Calculate how many file profiles are fit for the localization profile
     * 
     * @param fileProfileList
     * @param l10nId
     * @return
     */
    private int countAvailableOptionCounts(List<?> fileProfileList, long l10nId)
    {
        int enableCount = 0;
        for (int i = 0; i < fileProfileList.size(); i++)
        {
            FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(i);
            if (l10nId != 0 && fp.getL10nProfileId() == l10nId)
            {
                enableCount++;
            }
        }
        return enableCount;
    }
    
    /**
     * Replace "\" and "/" to file separator
     * 
     * @param path
     * @return
     */
    private String convertFilePath(String path)
    {
        if (path != null)
        {
            return path.replace("\\", File.separator).replace("/", File.separator);
        }
        else
        {
            return "";
        }
    }
    
    /**
     * Generate a random number
     * 
     * @return
     */
    private long getRandomNumber()
    {
        return (long) (Math.random() * 1000000000);
    }
    
    /**
     * Get last selected folder, so that user won't have to choose again.
     * 
     * @param userId
     * @param name
     * @return folderPath
     */
    private String getLastSelectedFolder(String userId, String parameterName)
    {
        try
        {
            if (userId != null)
            {
                UserParameter up = ServerProxy.getUserParameterManager().getUserParameter(userId,
                        parameterName);
                if (up != null)
                {
                    return up.getValue();
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to get last selected folder information.", e);
            return "";
        }

        return "";
    }
    
    /**
     * When deleting a file on page, delete the file from server too. If all
     * files are removed on page, delete the whole folder.
     * 
     * @param request
     */
    private void deleteFile(HttpServletRequest request)
    {
        try
        {
            String filePath = request.getParameter("filePath");
            String folder = request.getParameter("folder");
            String uploadPath = AmbFileStoragePathUtils.getCxeDocDir() + File.separator
                    + TMP_FOLDER_NAME + File.separator + folder;
            if (filePath == null)
            {
                FileUtil.deleteFile(new File(uploadPath));
            }
            else if (filePath.contains(folder))
            {
                File file = new File(filePath);
                file.delete();
            }
            else
            {
                filePath = convertFilePath(filePath);
                if (filePath.contains(":"))
                {
                    filePath = filePath.substring(filePath.indexOf(":") + 1);
                }
                String storagePath = uploadPath + File.separator + filePath;
                File fileToDelete = new File(storagePath);
                fileToDelete.delete();
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to delete file.", e);
        }
    }
}
