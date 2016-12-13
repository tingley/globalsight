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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;

import de.innosystec.unrar.rarfile.FileHeader;

/**
 * handler for import system configuration file
 *
 */
public class ConfigImportHandler extends PageHandler implements ConfigConstants
{
    private static final Logger logger = Logger.getLogger(ConfigImportHandler.class);

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException,
            IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String sessionId = session.getId();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        String companyId = CompanyThreadLocal.getInstance().getValue();
        String action = p_request.getParameter("action");
        List<File> uploadedFiles = new ArrayList<File>();
        if ("startUpload".equals(action))
        {
            try
            {
                uploadedFiles = getUploadFiles(p_request);
                session.setAttribute("uploading_filter", uploadedFiles);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if ("doImport".equals(action))
        {
            int count = 0;
            if (sessionMgr.getAttribute("count") != null)
            {
                count = (Integer) sessionMgr.getAttribute("count");
                if (count == 1)
                {
                    count++;
                    sessionMgr.setAttribute("count", count);
                }
            }
            else
            {
                count++;
                sessionMgr.setAttribute("count", count);
            }
            if (session.getAttribute("uploading_filter") != null)
            {
                config_error_map.clear();// .remove(sessionId);
                List<File> uploadFiles = (List<File>) session.getAttribute("uploading_filter");

                session.removeAttribute("importToCompId");
                session.removeAttribute("uploading_filter");
                Map<String, File> fileInfo = new HashMap<String, File>();
                for (File file : uploadFiles)
                {
                    String fileName = file.getName();
                    fileName = fileName.substring(0, fileName.indexOf("_") + 1);
                    fileInfo.put(fileName, file);
                }
                if (fileInfo.containsKey(LOCALEPAIR_FILE_NAME))
                {
                    // uploads locale pair property file
                    File file = fileInfo.get(LOCALEPAIR_FILE_NAME);
                    LocalePairImport imp = new LocalePairImport(sessionId, file, companyId);
                    imp.start();
                }
                if (fileInfo.containsKey(USER_FILE_NAME))
                {
                    // uploads user property file
                    File file = fileInfo.get(USER_FILE_NAME);
                    UserImport imp = new UserImport(sessionId, file, user, companyId);
                    imp.start();
                }
                if (fileInfo.containsKey(MT_FILE_NAME))
                {
                    // uploads mt profile property file
                    File file = fileInfo.get(MT_FILE_NAME);
                    MTProfileImport imp = new MTProfileImport(sessionId, file, companyId);
                    imp.start();
                }
                if (fileInfo.containsKey(FILTER_FILE_NAME))
                {
                    // uploads filter property file
                    File file = fileInfo.get(FILTER_FILE_NAME);
                    FilterConfigImport imp = new FilterConfigImport(sessionId, file,
                            user.getUserId(), companyId);
                    imp.start();
                }
            }
            else
            {
                logger.error("No uploaded config file.");
            }
        }
        else if ("refreshProgress".equals(action))
        {
            this.refreshProgress(p_request, p_response, sessionId);
            return;
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    private void refreshProgress(HttpServletRequest request, HttpServletResponse response,
            String sessionId)
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        int count = 0;
        if (sessionMgr.getAttribute("count") != null)
        {
            count = (Integer) sessionMgr.getAttribute("count");
        }
        else
        {
            count++;
            sessionMgr.setAttribute("count", count);
        }
        try
        {
            String msg;
            if (config_error_map.get(sessionId) == null)
            {
                msg = "";
            }
            else
            {
                if (count == 1)
                {
                    msg = "";
                }
                else
                {
                    msg = config_error_map.get(sessionId);
                }
            }
            count++;
            sessionMgr.setAttribute("count", count);

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(String.valueOf(msg));
            writer.close();
        }
        catch (Exception e)
        {
            logger.error("Refresh failed.", e);
        }
    }

    // get all upload files
    private List<File> getUploadFiles(HttpServletRequest p_request) throws Exception
    {
        ArrayList<File> uploadFiles = new ArrayList<File>();
        File uploadedFile = this.uploadFile(p_request);
        if (isSupportedZipFileFormat(uploadedFile) && isUnCompress(uploadedFile))
        {
            if (CreateJobUtil.isZipFile(uploadedFile))
            {
                uploadFiles = getFilesInZipFile(uploadedFile);
            }
            else if (CreateJobUtil.isRarFile(uploadedFile))
            {
                uploadFiles = getFilesInRarFile(uploadedFile);
            }
            else if (CreateJobUtil.is7zFile(uploadedFile))
            {
                uploadFiles = getFilesIn7ZFile(uploadedFile);
            }
            else
            {
                uploadFiles.add(uploadedFile);
            }
            if (CreateJobUtil.isZipFile(uploadedFile) || CreateJobUtil.isRarFile(uploadedFile)
                    || (CreateJobUtil.is7zFile(uploadedFile)))
            {
                uploadedFile.delete();
            }
        }
        else
        {
            uploadFiles.add(uploadedFile);
        }
        return uploadFiles;
    }

    // get files in 7z compressed package
    private ArrayList<File> getFilesIn7ZFile(File uploadedFile) throws Exception
    {
        ArrayList<File> files = new ArrayList<File>();
        String zip7zFileFullPath = uploadedFile.getPath();
        String zip7zFilePath = zip7zFileFullPath.substring(0,
                zip7zFileFullPath.indexOf(uploadedFile.getName()));

        List<SevenZArchiveEntry> entriesInZip7z = CreateJobUtil.getFilesIn7zFile(uploadedFile);
        for (SevenZArchiveEntry entry : entriesInZip7z)
        {
            String zip7zEntryName = entry.getName();
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zip7zFilePath
                    + uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."))
                    + "_" + CreateJobUtil.getFileExtension(uploadedFile) + File.separator
                    + zip7zEntryName;
            File file = new File(unzippedFileFullPath);
            files.add(file);
        }
        return files;
    }

    // get files in rar compressed package
    private ArrayList<File> getFilesInRarFile(File uploadedFile) throws Exception
    {
        ArrayList<File> files = new ArrayList<File>();
        String rarEntryName = null;
        String rarFileFullPath = uploadedFile.getPath();
        String rarFilePath = rarFileFullPath.substring(0,
                rarFileFullPath.indexOf(uploadedFile.getName()));

        List<FileHeader> entriesInRar = CreateJobUtil.getFilesInRarFile(uploadedFile);
        for (FileHeader fileHeader : entriesInRar)
        {
            if (fileHeader.isUnicode())
            {
                rarEntryName = fileHeader.getFileNameW();
            }
            else
            {
                rarEntryName = fileHeader.getFileNameString();
            }
            String unzippedFileFullPath = rarFilePath
                    + uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."))
                    + "_" + CreateJobUtil.getFileExtension(uploadedFile) + File.separator
                    + rarEntryName;
            File file = new File(unzippedFileFullPath);
            files.add(file);
        }
        return files;
    }

    // get files in zip compressed package
    private ArrayList<File> getFilesInZipFile(File uploadedFile) throws Exception
    {
        ArrayList<File> files = new ArrayList<File>();
        String zipFileFullPath = uploadedFile.getPath();
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(uploadedFile.getName()));

        List<net.lingala.zip4j.model.FileHeader> entriesInZip = CreateJobUtil
                .getFilesInZipFile(uploadedFile);
        for (net.lingala.zip4j.model.FileHeader entry : entriesInZip)
        {
            String zipEntryName = entry.getFileName();
            String unzippedFileFullPath = zipFilePath
                    + uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."))
                    + "_" + CreateJobUtil.getFileExtension(uploadedFile) + File.separator
                    + zipEntryName;
            File file = new File(unzippedFileFullPath);
            files.add(file);
        }

        return files;
    }

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

    private boolean isSupportedZipFileFormat(File uploadedFile)
    {
        String extension = CreateJobUtil.getFileExtension(uploadedFile);
        if ("rar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)
                || "7z".equalsIgnoreCase(extension))
        {
            return true;
        }
        return false;
    }

    /**
     * uploads configuration files
     * 
     * @param p_request
     */
    private File uploadFile(HttpServletRequest p_request)
    {
        File f = null;
        try
        {
            String tmpDir = AmbFileStoragePathUtils.getFileStorageDirPath() + File.separator
                    + "GlobalSight" + File.separator + "config" + File.separator + "import";
            boolean isMultiPart = ServletFileUpload.isMultipartContent(p_request);
            if (isMultiPart)
            {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1024000);
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<?> items = upload.parseRequest(p_request);
                for (int i = 0; i < items.size(); i++)
                {
                    FileItem item = (FileItem) items.get(i);
                    if (!item.isFormField())
                    {
                        String filePath = item.getName();
                        if (filePath.contains(":"))
                        {
                            filePath = filePath.substring(filePath.indexOf(":") + 1);
                        }
                        String originalFilePath = filePath.replace("\\", File.separator).replace(
                                "/", File.separator);
                        String fileName = tmpDir + File.separator + originalFilePath;
                        f = new File(fileName);
                        f.getParentFile().mkdirs();
                        item.write(f);
                    }
                }
            }
            return f;
        }
        catch (Exception e)
        {
            logger.error("File upload failed.", e);
            return null;
        }
    }

}
