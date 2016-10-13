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
package com.globalsight.everest.comment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.util.GeneralException;
import com.sun.jndi.toolkit.url.UrlUtil;

public class CommentFilesDownLoad
{
    private static final Logger CATEGORY = Logger
            .getLogger(CommentFilesDownLoad.class.getName());

    private JobPackageZipper m_zipper = null;

    private static final String UNDERLINE = "_";

    private static final String COLON = ":";

    private static final String ZERO = "0";

    private static final int LENGTH_OF_HAS_LOCALE = 4;

    private static final int LENGTH_OF_NO_LOCALE = 3;

    /**
     * Downloads the files in comment page.
     */
    public void downloadCommentFiles(String[] commentIds,
            HttpServletRequest request, HttpServletResponse response)
            throws JobException, GeneralException, NamingException, IOException
    {
        long jobId = Long.parseLong(request
                .getParameter(JobManagementHandler.JOB_ID));
        Job job = ServerProxy.getJobHandler().getJobById(jobId);
        String jobName = job.getJobName();
        String zipFileName = URLEncoder.encode(jobName + "_comments" + ".zip");
        String companyId = String.valueOf(job.getCompanyId());
        File tmpFile = File.createTempFile("GSCommentsDownload", ".zip");
        m_zipper = new JobPackageZipper();
        m_zipper.createZipFile(tmpFile);
        addCommentsFiles(zipFileName, commentIds, request, companyId);
        m_zipper.closeZipFile();
        sendFileToClient(request, response, zipFileName, tmpFile);
    }

    public void sendFileToClient(HttpServletRequest request,
            HttpServletResponse response, String zipFileName, File tmpFile)
    {
        if (request.isSecure())
        {
            PageHandler.setHeaderForHTTPSDownload(response);
        }
        FileInputStream fis = null;
        try
        {
            response.setContentType("application/zip");
            String attachment = "attachment; filename=\""
                    + UrlUtil.encode(zipFileName, "utf-8") + "\";";
            response.setHeader("Content-Disposition", attachment);
            response.setContentLength((int) tmpFile.length());
            byte[] inBuff = new byte[4096];
            fis = new FileInputStream(tmpFile);
            int bytesRead = 0;
            while ((bytesRead = fis.read(inBuff)) != -1)
            {
                response.getOutputStream().write(inBuff, 0, bytesRead);
            }

            if (bytesRead > 0)
            {
                response.getOutputStream().write(inBuff, 0, bytesRead);
            }

            fis.close();
        }
        catch (IOException e)
        {
            CATEGORY.error("Could not download the comment files.");
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    CATEGORY.error("Could not close the fileinputstream.");
                }
            }
            if (tmpFile != null)
            {
                tmpFile.deleteOnExit();
            }
        }

    }

    public String[] mergeCommentIds(String[] jobComments,
            String[] activityComments)
    {
        String[] totalCommentIds = new String[(jobComments == null ? 0
                : jobComments.length)
                + (activityComments == null ? 0 : activityComments.length)];
        if (jobComments == null && activityComments == null)
        {
            return totalCommentIds;
        }
        if (jobComments != null)
        {
            for (int i = 0; i < jobComments.length; i++)
            {
                totalCommentIds[i] = jobComments[i];
            }
        }
        if (activityComments != null)
        {
            for (int j = activityComments.length - 1; j >= 0; j--)
            {
                totalCommentIds[totalCommentIds.length - j - 1] = activityComments[j];
            }
        }
        return totalCommentIds;
    }

    private void addCommentsFiles(String zipFileName, String[] commentIds,
            HttpServletRequest request, String companyId)
            throws CommentException, RemoteException
    {
        PermissionSet perms = (PermissionSet) request.getSession()
                .getAttribute(WebAppConstants.PERMISSIONS);
        CommentManager commentManager = ServerProxy.getCommentManager();

        String access = "";

        if (perms.getPermissionFor(Permission.COMMENT_ACCESS_RESTRICTED))
        {
            access = WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS;
        }
        else
        {
            access = WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS;
        }
        Vector<String> fileList = new Vector<String>();
        Vector<String> fileNameList = new Vector<String>();
        Vector<String> commentIdList = new Vector<String>();
        for (int i = 0; i < commentIds.length; i++)
        {
            String commentId_Access_FileName = commentIds[i];
            if (ZERO.equals(commentId_Access_FileName))
            {
                continue;
            }
            String[] commentId_Access_FileNameArray = commentId_Access_FileName
                    .split(COLON);
            String commentId = commentId_Access_FileNameArray[0];
            String fileAccess = commentId_Access_FileNameArray[1];
            String fileName = commentId_Access_FileNameArray[2];
            ArrayList<CommentFile> commentReferences = commentManager
                    .getCommentReferences(commentId, access, companyId);
            L: for (int j = 0; j < commentReferences.size(); j++)
            {
                CommentFile file = commentReferences.get(j);
                if (!fileAccess.equals(file.getFileAccess()))
                {
                    continue L;
                }
                if (!fileName.equals(file.getFilename()))
                {
                    continue L;
                }
                String commentFile = file.getAbsolutePath();
                fileList.add(commentFile);
                fileNameList.add(file.getFilename());
                commentIdList.add(commentId);
            }
        }
        Vector<String> newFileList = addCommentIdToSameFileName(fileList,
                fileNameList, commentIdList);
        Vector<String> newFileNameList = generateNewFileNameList(newFileList);
        newFileList = addAccessToSameFileName(newFileList, newFileNameList);
        addCommentFiles(zipFileName, newFileList, fileList);
    }

    private Vector<String> generateNewFileNameList(Vector<String> newFileList)
    {
        Vector<String> newFileNameList = new Vector<String>();
        for (int i = 0; i < newFileList.size(); i++)
        {
            String newFile = newFileList.get(i);
            String fileName = newFile.substring(newFile
                    .lastIndexOf(File.separatorChar) + 1);
            newFileNameList.add(fileName);
        }
        return newFileNameList;
    }

    private void addCommentFiles(String zipFileName,
            Vector<String> newFileList, Vector<String> fileList)
    {
        for (int i = 0; i < fileList.size(); i++)
        {
            File file = new File(fileList.get(i));
            writeOneFile(
                    zipFileName,
                    file,
                    newFileList.get(i)
                            .substring(
                                    newFileList.get(i).lastIndexOf(
                                            File.separatorChar) + 1));
        }
    }

    private void writeOneFile(String zipFileName, File file, String fileName)
    {

        FileInputStream input = null;
        try
        {
            input = new FileInputStream(file);

            if (input == null)
            {
                CATEGORY.warn("Could not locate comment file: " + file);
            }
            else
            {
                if (zipFileName.indexOf(".") != -1)
                {
                    m_zipper.writePath(zipFileName.substring(0,
                            zipFileName.indexOf("."))
                            + "/comments/" + fileName);
                }
                else
                {
                    m_zipper.writePath(zipFileName + "/comments/" + fileName);
                }

                m_zipper.writeFile(input);
            }
            input.close();
        }
        catch (IOException ex)
        {
            CATEGORY.warn("cannot write comment file: " + file
                    + " to zip stream.", ex);
        }
        finally
        {
            try
            {
                if (input != null)
                {
                    input.close();
                }
            }
            catch (IOException e)
            {
                CATEGORY.warn("cannot close comment file: " + file
                        + " to zip stream.", e);
            }
        }

    }

    private Vector<String> addCommentIdToSameFileName(Vector<String> fileList,
            Vector<String> fileNameList, Vector<String> commentIdList)
    {
        Vector<String> newFileList = new Vector<String>();
        Vector<Integer> sameFileNameIndex = getSameFileNameIndexToAddCommentId(fileNameList);
        for (int i = 0; i < fileList.size(); i++)
        {
            if (sameFileNameIndex.contains(i))
            {
                // The i-th element have the same filename with the others
                String oldFile = fileList.get(i);
                String newFileName = oldFile.substring(0,
                        oldFile.lastIndexOf('.'))
                        + UNDERLINE
                        + commentIdList.get(i)
                        + oldFile.substring(oldFile.lastIndexOf('.'));
                newFileList.add(newFileName);
            }
            else
            {
                newFileList.add(fileList.get(i));
            }
        }
        return newFileList;
    }

    private Vector<String> addAccessToSameFileName(Vector<String> fileList,
            Vector<String> fileNameList)
    {
        Vector<String> newFileList = new Vector<String>();
        Vector<String> sameFileNameIndexPair = getSameFileNameIndexToAddAccess(fileNameList);
        for (int i = 0; i < fileList.size(); i++)
        {
            if (isIndexInSameFileNameIndexPair(i, sameFileNameIndexPair))
            {
                // The i-th element have the same filename with the others
                String oldFile = fileList.get(i);
                String access = getAccess(oldFile);
                String newFileName = oldFile.substring(0,
                        oldFile.lastIndexOf('.'))
                        + "("
                        + access
                        + ")"
                        + oldFile.substring(oldFile.lastIndexOf('.'));
                newFileList.add(newFileName);
            }
            else
            {
                newFileList.add(fileList.get(i));
            }
        }
        return newFileList;
    }

    private String getAccess(String oldFile)
    {
        String access = "";
        if (oldFile.indexOf(File.separator + "Restricted" + File.separator) != -1)
        {
            access = "Restricted";
        }
        else
        {
            access = "General";
        }
        return access;
    }

    private boolean isIndexInSameFileNameIndexPair(int index,
            Vector<String> sameFileNameIndexPair)
    {
        boolean flag = false;
        for (int i = 0; i < sameFileNameIndexPair.size(); i++)
        {
            String pair = sameFileNameIndexPair.get(i);
            String[] pairs = pair.split(UNDERLINE);

            for (int j = 0; j < pairs.length; j++)
            {
                if (Integer.parseInt(pairs[j]) == index)
                {
                    flag = true;
                }
            }
        }
        return flag;
    }

    private Vector<String> getSameFileNameIndexToAddAccess(
            Vector<String> fileNameList)
    {
        Vector<String> sameFileNameIndexPair = new Vector<String>();
        for (int i = 0; i < fileNameList.size(); i++)
        {
            for (int j = i + 1; j < fileNameList.size(); j++)
            {
                if (fileNameList.get(i).equals(fileNameList.get(j)))
                {
                    sameFileNameIndexPair.add(i + UNDERLINE + j);
                }
            }
        }
        return sameFileNameIndexPair;
    }

    private Vector<Integer> getSameFileNameIndexToAddCommentId(
            Vector<String> fileNameList)
    {
        Vector<Integer> sameFileNameIndex = new Vector<Integer>();
        for (int i = 0; i < fileNameList.size(); i++)
        {
            for (int j = i + 1; j < fileNameList.size(); j++)
            {
                if (fileNameList.get(i).equals(fileNameList.get(j)))
                {
                    sameFileNameIndex.add(i);
                    sameFileNameIndex.add(j);
                }
            }
        }
        return sameFileNameIndex;
    }

    public String[] removeUnrelatedIds(String[] activityComments,
            String selectedLocale)
    {
        if (activityComments == null)
        {
            return activityComments;
        }
        if ("allLocales".equals(selectedLocale))
        {
            for (int i = 0; i < activityComments.length; i++)
            {
                String commentId_Access_FileName = activityComments[i];
                String[] commentId_Access_FileNameArray = commentId_Access_FileName
                        .split(COLON);
                if (commentId_Access_FileNameArray.length == LENGTH_OF_HAS_LOCALE)
                {
                    activityComments[i] = ZERO;
                }
            }
        }
        else
        {
            for (int i = 0; i < activityComments.length; i++)
            {
                String commentId_Access_FileName = activityComments[i];
                String[] commentId_Access_FileNameArray = commentId_Access_FileName
                        .split(COLON);
                if (commentId_Access_FileNameArray.length == LENGTH_OF_NO_LOCALE)
                {
                    activityComments[i] = commentId_Access_FileName;
                }
                else
                {
                    if (!commentId_Access_FileNameArray[LENGTH_OF_NO_LOCALE]
                            .equals(selectedLocale))
                    {
                        activityComments[i] = ZERO;
                    }
                }
            }
        }
        return activityComments;
    }

}
