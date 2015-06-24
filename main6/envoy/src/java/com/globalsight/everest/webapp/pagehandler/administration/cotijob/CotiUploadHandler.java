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
package com.globalsight.everest.webapp.pagehandler.administration.cotijob;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.coti.util.COTIConstants;
import com.globalsight.everest.coti.util.COTIDbUtil;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReader;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FileUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Handler class for COTI job details page
 * 
 * @author Wayzou
 * 
 */
public class CotiUploadHandler extends PageHandler
{
    private static final Logger logger = Logger
            .getLogger(CotiJobDetailHandler.class);

    private static final String DOT = ".";
    private static final String DISABLED = "disabled";
    private static final String SELECTED = "selected";
    private Map<String, List<FileProfileImpl>> extensionToFileProfileMap;

    private static final int SUCCEED = 0;
    private static final int FAIL = 1;
    private static final String transDirName = COTIConstants.Dir_TranslationFiles_Name;

    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        // permission check
        HttpSession session = request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.COTI_JOB))
        {
            logger.error("User doesn't have the permission to create COTI jobs via this page.");
            response.sendRedirect("/globalsight/ControlServlet?");
            return;
        }
        // get the operator
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
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

        String cid = CompanyWrapper.getCurrentCompanyId();
        Company c = CompanyWrapper.getCompanyById(cid);
        String action = (String) request.getParameter(UPLOAD_ACTION);

        if ("startUpload".equals(action))
        {
            String errorResult = null;
            String successResult = null;
            List<String> projectIds = new ArrayList<String>();
            MultipartFormDataReader reader = new MultipartFormDataReader();
            try
            {
                File uploadFile = reader.uploadToTempFile(request);
                String fileName = reader.getFilename();
                List<File> projectDirs = COTIUtilEnvoy
                        .unzipAndGetProjectDir(uploadFile);
                List<File> cotiFiles = new ArrayList<File>();
                boolean needUnzip = false;

                // check if these files are coti package
                if (projectDirs == null || projectDirs.size() == 0)
                {
                    String tempDir = uploadFile.getPath() + ".unzip";
                    List<String> unzipFilepaths = ZipIt.unpackZipPackage(
                            uploadFile.getPath(), tempDir);
                    needUnzip = true;

                    for (int i = 0; i < unzipFilepaths.size(); i++)
                    {
                        String fpath = unzipFilepaths.get(i);

                        cotiFiles.add(new File(tempDir, fpath));
                    }
                }
                else
                {
                    cotiFiles.add(uploadFile);
                }

                boolean error = false;
                for (File cotiFile : cotiFiles)
                {
                    if (needUnzip)
                    {
                        projectDirs = COTIUtilEnvoy
                                .unzipAndGetProjectDir(cotiFile);
                    }
                    
                    if (projectDirs == null || projectDirs.size() == 0)
                    {
                        errorResult = "Cannot find COTI project from package: "
                                + cotiFile;
                        errorMsg(errorResult);
                        break;
                    }

                    // create coti project with one coti package
                    File cotiXmlFile = null;
                    List<File> transFiles = new ArrayList<File>();
                    List<String> transDocuments = new ArrayList<String>();
                    List<File> refFiles = new ArrayList<File>();
                    Map<File, String> fileRefs = new HashMap<File, String>();

                    for (int i = 0; i < projectDirs.size(); i++)
                    {
                        File projectDir = projectDirs.get(i);
                        String fpath = projectDir.getPath();
                        List<File> subFiles = FileUtil.getAllFiles(projectDir);

                        for (int j = 0; j < subFiles.size(); j++)
                        {
                            File subfile = subFiles.get(j);
                            String subPath = subfile.getPath();
                            String fileRef = subPath
                                    .substring(fpath.length() + 1);

                            if ("COTI.xml".equalsIgnoreCase(fileRef))
                            {
                                cotiXmlFile = subfile;
                            }
                            else if (fileRef.startsWith("reference files"))
                            {
                                refFiles.add(subfile);

                                fileRefs.put(subfile,
                                        fileRef.substring("reference files"
                                                .length() + 1));
                            }
                            else if (fileRef.startsWith("translation files"))
                            {
                                transFiles.add(subfile);
                                fileRefs.put(subfile,
                                        fileRef.substring("translation files"
                                                .length() + 1));
                            }
                        }

                        if (cotiXmlFile == null || !cotiXmlFile.exists())
                        {
                            errorResult = "Cannot find COTI.xml from package: "
                                    + cotiFile;
                            errorMsg(errorResult);
                            error = true;
                            break;
                        }

                        if (transFiles.size() == 0)
                        {
                            errorResult = "Cannot find translation files from package: "
                                    + cotiFile;
                            errorMsg(errorResult);
                            error = true;
                            break;
                        }

                        // create project
                        String cotiXmlContent = FileUtil.readFile(cotiXmlFile,
                                "UTF-8");
                        COTIProject cproject = COTIUtilEnvoy.createCOTIProject(
                                c, cotiXmlContent);
                        COTIPackage cpackage = COTIDbUtil
                                .getCOTIPackage(cproject.getPackageId());
                        long projectId = cproject.getId();
                        projectIds.add("" + projectId);
                        logMsg("Create one COTI job, coti job id :" + projectId);

                        // upload documents
                        for (int j = 0; j < transFiles.size(); j++)
                        {
                            File cotif = transFiles.get(j);
                            String fileRef = fileRefs.get(cotif);
                            String fileType = COTIConstants.fileType_translation;

                            String docid = COTIUtilEnvoy.saveDocumentFile(
                                    cpackage, cproject, cotif, fileRef,
                                    fileType);
                            logMsg("Upload translation document: " + cotif
                                    + " id " + docid);
                        }

                        for (int j = 0; j < refFiles.size(); j++)
                        {
                            File cotif = refFiles.get(j);
                            String fileRef = fileRefs.get(cotif);
                            String fileType = COTIConstants.fileType_reference;

                            COTIUtilEnvoy.saveDocumentFile(cpackage, cproject,
                                    cotif, fileRef, fileType);
                            logMsg("Upload reference document: " + cotif);
                        }

                        COTIUtilEnvoy.startCOTIProject(cproject);
                        logMsg("Start coti job " + projectId);

                        cotiXmlFile = null;
                        transFiles.clear();
                        refFiles.clear();
                        fileRefs.clear();
                        transDocuments.clear();
                    }
                    
                    if (error)
                    {
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                errorResult = (e instanceof NullPointerException) ? "NullPointerException"
                        : e.getMessage();
                logger.error(errorResult, e);
            }

            request.setAttribute("COTI_successResult",
                    errorResult == null ? "Upload Successfully. COTI Job(s) id: " + projectIds : "");
            request.setAttribute("COTI_errorResult", errorResult == null ? ""
                    : "Upload Failed: " + errorResult);
        }

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private void logMsg(String msg)
    {
        logger.info(msg);
    }

    private void errorMsg(String msg)
    {
        logger.error(msg);
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
            return path.replace("\\", File.separator).replace("/",
                    File.separator);
        }
        else
        {
            return "";
        }
    }

    private String replaceSpecialCharacters(String input)
    {
        return input.replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

}
