/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.webservices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;


/**
 * Webservice APIs that are not intended to public released are put here, only
 * for internal usage by GlobalSight itself.
 * 
 * @author YorkJin
 * @since 8.3.1
 */
public class Ambassador2 extends Ambassador
{
    private static final Logger logger = Logger.getLogger("WebService2");
    
    private static int MAX_SEND_SIZE = 5 * 1000 * 1024; // 5M

    /**
     * Logs into the WebService. Returns an access token and company name
     * 
     * The format of returning string is 'AccessToken+_+CompanyName'.
     * 
     * @param p_username
     *            Username used to log in
     * 
     * @param p_password
     *            Password used to log in
     * 
     * @return java.lang.String Access token and company name which user works
     *         for
     * 
     * @exception WebServiceException
     */
    public String dummyLogin(String p_username, String p_password)
            throws WebServiceException
    {
        return super.login(p_username, p_password);
    }

    /**
     * Upload files to server with specified path.
     * 
     * @param p_accessToken
     * @param p_companyId
     *            -- the company Id file should be uploaded to.
     * @param p_basePathType
     *            -- File's real saving path is composed of base path and sub
     *            path. 1: upload source files on web DI, files will be saved in
     *            CXE_DOCS/createJob_tmp folder. 2: upload job comment file on
     *            web DI, file will be save in
     *            CXE_FILESTORAGE/GlobalSight/CommentReference/tmp folder. 3.
     *            upload source files for "Add Files" on job details UI, files
     *            will be saved in CXE_DOCS/addFiles_tmp folder.
     * @param p_path
     *            -- sub path
     * @param bytes
     *            -- file content in bytes.
     * @throws WebServiceException
     */
    public void uploadFiles(String p_accessToken, String p_companyId,
            int p_basePathType, String p_path, byte[] bytes)
            throws WebServiceException
    {
        try
        {
            Assert.assertNotEmpty(p_accessToken, "Access token");
            Assert.assertNotEmpty(p_path, "Upload path");
            p_path = p_path.replace("\\", "/");
            if (Integer.parseInt(p_companyId) < 1)
            {
                throw new Exception("Invalid 'p_companyId' parameter: "
                        + p_companyId);
            }
            if (p_basePathType < 1)
            {
                throw new Exception("Invalid 'p_basePathType' parameter: "
                        + p_basePathType);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            throw new WebServiceException(e.getMessage());
        }

        // Check access token
        checkAccess(p_accessToken, "uploadFiles");

        File newFile = null;
        if (p_basePathType == 1 || p_basePathType == 3)
        {
            newFile = new File(
                    AmbFileStoragePathUtils.getCxeDocDir(p_companyId), p_path);            
        }
        else if (p_basePathType == 2)
        {
            newFile = new File(
                    AmbFileStoragePathUtils.getFileStorageDirPath(p_companyId),
                    p_path);
        }
        newFile.getParentFile().mkdirs();

        FileOutputStream fos = null;
        boolean decompressSuccess = false;
        try
        {
            fos = new FileOutputStream(newFile, true);
            fos.write(bytes);

            // Web DI supports zip file.
            if (p_basePathType == 1)
            {
                String extension = FileUtils.getFileExtension(newFile);
                // When upload file, every time 5M. For large zip file, we must
                // wait to unzip it after the whole file uploading is done.If
                // the bytes size is less 5M, unzip it and delete the zip file.
                // This is not 100% reliable, but harmless.
                Set<String> formats = new HashSet<String>();
                formats.add("zip");
                formats.add("rar");
                formats.add("7z");
                if (formats.contains(extension) && bytes.length < MAX_SEND_SIZE)
                {
                    if ("zip".equalsIgnoreCase(extension))
                    {
                        decompressSuccess = CreateJobUtil.unzipFile(newFile);                        
                    }
                    else if ("rar".equalsIgnoreCase(extension))
                    {
                        decompressSuccess = CreateJobUtil.unrarFile(newFile);                        
                    }
                    else if ("7z".equalsIgnoreCase(extension))
                    {
                        decompressSuccess = CreateJobUtil.un7zFile(newFile);
                    }
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Could not copy uploaded file to specified directory.";
            logger.error(msg, e);
            String message = msg + e.getMessage();
            message = makeErrorXml("uploadFile", message);
            throw new WebServiceException(message);
        }
        finally
        {
            try
            {
                if (fos != null)
                    fos.close();
                if (decompressSuccess)
                {
                    newFile.delete();
                }
            }
            catch (IOException e)
            {
                logger.error(e);
            }
        }
    }
    
}
