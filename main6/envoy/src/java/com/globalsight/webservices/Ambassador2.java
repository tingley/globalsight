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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.globalsight.cxe.engine.util.FileUtils;
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
                if (extension != null && extension.equalsIgnoreCase("zip") && bytes.length < MAX_SEND_SIZE)
                {
                    unzipFile(newFile);
                    try
                    {
                        fos.close();
                        newFile.delete();
                    }
                    catch (IOException e)
                    {

                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Could not copy uploaded file to specified directory.", e);
            String message = "Could not copy uploaded file to specified directory." + e.getMessage();
            message = makeErrorXml("uploadFile", message);
            throw new WebServiceException(message);
        }
        finally
        {
            try
            {
                if (fos != null)
                    fos.close();
            }
            catch (IOException e)
            {

            }
        }
    }
    
    private void unzipFile(File file)
    {
        String zipFileFullPath = file.getPath();// path contains file name
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(file.getName()));// path without file name
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(zipFileFullPath));
            ZipEntry zipEntry = null;
            byte[] buf = new byte[1024];
            
            while ((zipEntry = zin.getNextEntry()) != null)
            {
                String zipEntryName = zipEntry.getName();
                String newPath = zipFilePath
                        + File.separator
                        + file.getName().substring(0,
                                file.getName().lastIndexOf("."))
                        + File.separator
                        + zipEntryName;// original path + zipfile Name + entry name
                File outfile = new File(newPath);
                if (zipEntry.isDirectory())
                {
                    outfile.mkdirs();
                    continue;
                }
                else 
                {
                    if (!outfile.getParentFile().exists())
                    {
                        outfile.getParentFile().mkdirs();
                    }
                }
                
                OutputStream os = new BufferedOutputStream(
                        new FileOutputStream(outfile));
                int readLen = 0;
                try
                {
                    readLen = zin.read(buf, 0, 1024);
                }
                catch (IOException ioe)
                {
                    readLen = -1;
                }
                while (readLen != -1)
                {
                    os.write(buf, 0, readLen);
                    try
                    {
                        readLen = zin.read(buf, 0, 1024);
                    }
                    catch (IOException ioe)
                    {
                        readLen = -1;
                    }
                }
                os.close();
            }
        }
        catch (IOException e)
        {
            logger.error("unzip file error.", e);
        }
        finally
        {
            if (zin != null)
            {
                try
                {
                    zin.close();
                }
                catch (IOException e)
                {
                    logger.error("Error occurs.", e);
                }
            }
        }
    }
}
