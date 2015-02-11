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

package com.globalsight.everest.nativefilestore;

// globalsight
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.NativeEnDecoderException;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

/**
 * <p>
 * This is the front-end manager for Native-file storage and retrieval.
 * <p>
 * 
 * <p>
 * Eventualy, all processes needing to store and/or retrieve bulk files (in
 * thier native formats) should use this manager. This will enable us to
 * eventually redirect the file store to either the file system or to a DB. All
 * from one location.
 * </p>
 */
public class NativeFileManagerLocal implements NativeFileManager
{
    private static final Logger CATEGORY = Logger
            .getLogger(NativeFileManagerLocal.class.getName());

    // *************************************************
    // File-System storage params
    // *************************************************

    // private String m_absolutePath_doc = null;

    /**
     * Constants used for a sub-directory relative to a system-wide file storage
     * defined during installation process.
     */
    // // for STF files
    // public final static String STF_SUB_DIRECTORY =
    // "Globalsight/SecondaryTargetFiles";
    // // for Un-extracted files
    // public final static String UNEXTRACTED_SUB_DIRECTORY =
    // "Globalsight/UnextractedFiles";
    //
    // /** The FULL path to which files are uploaded.*/
    // private File m_stfParentDir = null;
    // private File m_unextractedParentDir = null;
    // *************************************************
    // Constructors
    // *************************************************
    public NativeFileManagerLocal() throws NativeFileManagerException
    {
        // try
        // {
        // SystemConfiguration sc = SystemConfiguration.getInstance();
        //
        // m_absolutePath_doc = sc.getStringParameter(
        // SystemConfiguration.FILE_STORAGE_DIR);
        //
        // if (!(m_absolutePath_doc.endsWith("/") ||
        // m_absolutePath_doc.endsWith("\\")))
        // {
        // m_absolutePath_doc = m_absolutePath_doc + "/";
        // }
        //
        // m_stfParentDir
        // = new File(m_absolutePath_doc + STF_SUB_DIRECTORY + "/");
        // m_stfParentDir.mkdirs();
        //
        // m_unextractedParentDir =
        // new File(m_absolutePath_doc + UNEXTRACTED_SUB_DIRECTORY + "/");
        // m_unextractedParentDir.mkdirs();
        // }
        // catch (Exception e)
        // {
        // throw new NativeFileManagerException(
        // NativeFileManagerException.MSG_FAILED_TO_INIT_FILESYSTEM_STORAGE,
        // null, e);
        // }
    }

    // *************************************************
    // Begin Public methods
    // *************************************************

    /**
     * See NativeFileManager interface for documentation
     */
    public void save(SecondaryTargetFile p_stf, File p_tmpFile, User p_user,
            String p_newFilename) throws NativeFileManagerException
    {
        NativeFileManagerException nfme = null;
        boolean isRename = false;
        File newFile = null;
        File originalAbsouluteStoragePath = new File(
                AmbFileStoragePathUtils.getStfParentDir(),
                p_stf.getStoragePath());
        String storagePath = p_stf.getStoragePath();

        // if we are renaming the file, check for (and resolve) name collisions
        if (p_newFilename != null && p_newFilename.length() > 0
                && !getFileName(storagePath).equals(p_newFilename))
        {
            isRename = true;
            storagePath = resolveStoragePathCollision(storagePath, p_stf
                    .getIdAsLong().toString(), p_newFilename);
        }

        try
        {
            newFile = save(AmbFileStoragePathUtils.getStfParentDir(),
                    storagePath, p_tmpFile);

            p_stf.setModifierUserId(p_user != null ? p_user.getUserId()
                    : "Unknown");
            p_stf.setFileSize(newFile.length());
            p_stf.setLastUpdatedTime(newFile.lastModified());
            if (isRename)
            {
                p_stf.setStoragePath(storagePath);
            }
            ServerProxy.getSecondaryTargetFileManager()
                    .updateSecondaryTargetFile(p_stf);

            // if we renamed the file, remove the old file
            if (isRename && originalAbsouluteStoragePath.exists())
            {
                if (!originalAbsouluteStoragePath.delete())
                {
                    CATEGORY.warn("Save/Rename was unable to remove the old file: "
                            + originalAbsouluteStoragePath.toString());
                }
            }
        }
        catch (Exception e)
        {
            // hold over for finally
            String[] args =
            { newFile.toString() };
            nfme = new NativeFileManagerException(
                    NativeFileManagerException.MSG_FAILED_TO_SAVE_FILE, args, e);
        }
        finally
        {
            if (nfme != null)
                throw nfme;
        }
    }

    /**
     * @see NativeFileManager
     */
    public void save(UnextractedFile p_unextractedFile, File p_tmpFile,
            User p_user) throws NativeFileManagerException
    {
        NativeFileManagerException nfme = null;
        File newFile = null;
        try
        {
            newFile = save(AmbFileStoragePathUtils.getUnextractedParentDir(),
                    p_unextractedFile.getStoragePath(), p_tmpFile);

            // update the UnextractedFile Info
            p_unextractedFile.setLength(newFile.length());
            p_unextractedFile.setLastModifiedDate(newFile.lastModified());
            p_unextractedFile.setLastModifiedBy(p_user != null ? p_user
                    .getUserId() : "Unknown");
        }
        catch (Exception e)
        {
            // hold over for finally
            String[] args =
            { newFile.toString() };
            nfme = new NativeFileManagerException(
                    NativeFileManagerException.MSG_FAILED_TO_SAVE_FILE, args, e);
        }
        finally
        {
            if (nfme != null)
                throw nfme;
        }
    }

    /**
     * Saves the given file content to the path relative to the file storage
     * dir, in the specified encoding. It will overwrite the file if it already
     * exists.
     * 
     * @param p_fileContent
     *            file content as a String
     * @param p_encoding
     *            Java char encoding, for example "UTF8"
     * @param p_relPath
     *            path relative to the file storage dir, for example
     *            "corpus/blah/foo/bar.doc"
     * @param returns
     *            the full path of the file just saved
     */
    public String save(String p_fileContent, String p_encoding, String p_relPath)
            throws NativeFileManagerException
    {
        // fullPath is <filestoragedir>/<relPath>
        String fullPath = makeFullPath(p_relPath);
        try
        {
            File f = new File(fullPath);
            if (f.exists())
                f.delete();
            else
            {
                // seems to be an odd threading situation with IO and directory
                // making on NT
                synchronized (this)
                {
                    boolean b = f.getParentFile().mkdirs();
                    CATEGORY.debug("Creating directories for: " + fullPath
                            + " == " + b);
                }
            }

            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter os = new OutputStreamWriter(fos, p_encoding);
            os.write(p_fileContent, 0, p_fileContent.length());
            os.close();
            return fullPath.toString();
        }
        catch (Exception e)
        {
            String[] args =
            { fullPath };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_FAILED_TO_SAVE_FILE, args, e);
        }
    }

    /**
     * Returns the file named by the path relative to the file storage dir
     * 
     * @param p_relPath
     *            relative path
     * @return File
     */
    public File getFile(String p_relPath) throws NativeFileManagerException
    {
        String fullPath = makeFullPath(p_relPath);
        return new File(fullPath);
    }

    /**
     * See NativeFileManager interface for documentation
     */
    public File getFile(SecondaryTargetFile p_stf, String companyId)
            throws NativeFileManagerException
    {
        if (p_stf != null)
        {
            // return getBytes(m_stfParentDir, p_stf.getStoragePath());
            return new File(AmbFileStoragePathUtils.getStfParentDir(companyId),
                    p_stf.getStoragePath());
        }
        else
        {
            String[] args =
            { "p_stf=null" };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }
    }

    /**
     * See NativeFileManager interface for documentation
     */
    public File getFile(SecondaryTargetFile p_stf)
            throws NativeFileManagerException
    {
        if (p_stf != null)
        {
            // return getBytes(m_stfParentDir, p_stf.getStoragePath());
            return new File(AmbFileStoragePathUtils.getStfParentDir(),
                    p_stf.getStoragePath());
        }
        else
        {
            String[] args =
            { "p_stf=null" };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }
    }

    /**
     * @see NativeFileManager interface for documentation
     */
    public File getFile(UnextractedFile p_unextractedFile)
            throws NativeFileManagerException
    {
        if (p_unextractedFile != null)
        {
            // return getBytes(m_unextractedParentDir,
            // p_unextractedFile.getStoragePath());
            return new File(AmbFileStoragePathUtils.getUnextractedParentDir(),
                    p_unextractedFile.getStoragePath());
        }
        else
        {
            String[] args =
            { "p_unextractedFile=null" };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }
    }

    /**
     * @see NativeFileManager.getBytes(String)
     */
    public byte[] getBytes(String p_relPath) throws NativeFileManagerException
    {
        // return this.getBytes(new File(m_absolutePath_doc), p_relPath);
        return this.getBytes(AmbFileStoragePathUtils.getFileStorageDir(),
                p_relPath);
    }

    /**
     * @see NativeFileManager.getString
     */
    public String getString(String p_relPath, String encoding)
            throws NativeFileManagerException
    {
        try
        {
            return this.getString(AmbFileStoragePathUtils.getFileStorageDir(),
                    p_relPath, encoding);
        }
        catch (NativeEnDecoderException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * See NativeFileManager interface for documentation
     */
    public SecondaryTargetFile moveFileToStorage(String p_absolutePath,
            SecondaryTargetFile p_stf, int p_sourcePageBomType)
            throws NativeFileManagerException
    {
        FileInputStream fis = null;
        File inputFile = null;
        Workflow wf = p_stf.getWorkflow();

        if (p_absolutePath != null && p_stf != null)
        {
            try
            {
                // Modify for super user.
                String companyId;
                if (wf != null && wf.getCompanyId() != -1)
                {
                    companyId = String.valueOf(wf.getCompanyId());
                }
                else
                {
                    companyId = CompanyThreadLocal.getInstance().getValue();
                }

                File sftParentDir = AmbFileStoragePathUtils
                        .getStfParentDir(companyId);
                // Make directories
                makeAdditionalDirs(sftParentDir, p_stf.getStoragePath());

                // move the file (using rename)
                File newFile = new File(sftParentDir, p_stf.getStoragePath());
                inputFile = new File(p_absolutePath);
                if (inputFile.exists())
                {
                    // must delete target file first (if exists)
                    // otherwise renameTo() will fail
                    if (newFile.exists())
                    {
                        newFile.delete();
                    }

                    if (p_sourcePageBomType != ExportConstants.NO_UTF_BOM)
                    {
                        fis = new FileInputStream(inputFile);
                        byte[] bytes = new byte[fis.available()];
                        fis.read(bytes);
                        String content = new String(bytes, 0, bytes.length);
                        FileUtil.writeFileWithBom(newFile, content,
                                FileUtil.getUTFFormat(p_sourcePageBomType));
                    }
                    else
                    {

                        // NOTE (do not remove this comment):
                        // In checking Suns bug database, there seems to be a
                        // lot
                        // of chatter about renameTo() and various problems
                        // related to different behavior on Unix vs Windoze and
                        // NFS
                        // mounted voulumes(search the bug db for "renameTo").
                        // If
                        // renameTo fails we make an attempt to copy the file
                        // instead.
                        if (!inputFile.renameTo(newFile))
                        {
                            CATEGORY.info("renameTo() Failed: Attempting a "
                                    + "copy from " + inputFile + " to "
                                    + newFile);
                            fis = new FileInputStream(inputFile);
                            writeFile(fis, newFile);
                            CATEGORY.info("Copy "
                                    + (newFile.exists() ? "successful"
                                            : "also failed!!"));
                        }
                    }

                    // update the SecondaryFile Info
                    // note: it is the responsiblity of the caller to persist
                    // the stf. Here we just update it and return it
                    p_stf.setFileSize(newFile.length());
                    p_stf.setLastUpdatedTime(newFile.lastModified());

                    inputFile.delete();
                }
                else
                {
                    String[] args =
                    { inputFile.getAbsolutePath() };
                    throw new NativeFileManagerException(
                            NativeFileManagerException.MSG_INVALID_FILENAME,
                            args, null);
                }
            }
            catch (Exception e)
            {
                throw new NativeFileManagerException(
                        NativeFileManagerException.MSG_FAILED_TO_MOVE_FILE,
                        null, e);
            }

        }
        else
        {
            String[] args =
            { ((p_absolutePath == null ? "p_absolutePath=null" : "") + (p_stf == null ? " p_stf=null"
                    : "")) };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }

        return p_stf;
    }

    /**
     * @see NativeFileManager.copyFileToStorage(String, UnextractedFile,
     *      boolean)
     */
    public UnextractedFile copyFileToStorage(String p_absolutePath,
            UnextractedFile p_unextractedFile, boolean p_removeOriginal)
            throws NativeFileManagerException
    {
        FileInputStream fis = null;
        File inputFile = null;

        if (p_absolutePath != null && p_unextractedFile != null)
        {
            try
            {
                // Make directories
                makeAdditionalDirs(
                        AmbFileStoragePathUtils.getUnextractedParentDir(),
                        p_unextractedFile.getStoragePath());

                // copy the file
                File newFile = new File(
                        AmbFileStoragePathUtils.getUnextractedParentDir(),
                        p_unextractedFile.getStoragePath());
                inputFile = new File(p_absolutePath);
                if (inputFile.exists())
                {
                    // must delete target file first (if exists)
                    // otherwise renameTo() will fail
                    if (newFile.exists())
                    {
                        newFile.delete();
                    }

                    fis = new FileInputStream(inputFile);
                    writeFile(fis, newFile);
                    CATEGORY.info("Copy "
                            + (newFile.exists() ? "successful"
                                    : "also failed!!"));

                    // update the SecondaryFile Info
                    p_unextractedFile.setLength(newFile.length());
                    p_unextractedFile.setLastModifiedDate(newFile
                            .lastModified());

                    // if set delete the original file
                    // What is the purpose?
                    if (p_removeOriginal)
                    {
                        // inputFile.delete();
                    }
                }
                else
                {
                    String[] args =
                    { inputFile.getAbsolutePath() };
                    throw new NativeFileManagerException(
                            NativeFileManagerException.MSG_INVALID_FILENAME,
                            args, null);
                }
            }
            catch (Exception e)
            {
                String[] args =
                {
                        p_unextractedFile.getName(),
                        p_absolutePath,
                        AmbFileStoragePathUtils.getUnextractedParentDir()
                                + p_unextractedFile.getStoragePath() };
                throw new NativeFileManagerException(
                        NativeFileManagerException.MSG_FAILED_TO_COPY_FILE,
                        args, e);
            }
        }
        else
        {
            String[] args =
            { ((p_absolutePath == null ? "p_absolutePath=null" : "") + (p_unextractedFile == null ? " p_unextractedFile=null"
                    : "")) };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }
        return p_unextractedFile;
    }

    /**
     * Copies a file into the native file store. The new file will be
     * overwritten if it already exists.
     * 
     * @param p_absolutePathToOriginalFile
     *            absolute path to the original file to copy
     * @param p_relPathToNewFile
     *            relative path to the new file
     * @param p_removeOriginal
     *            if true, then the original file is deleted
     * @return the absolute path of the new (copied) file
     * @exception NativeFileManagerException
     */
    public String copyFileToStorage(String p_absolutePathToOriginalFile,
            String p_relPathToNewFile, boolean p_removeOriginal)
            throws NativeFileManagerException
    {
        String fullPath = makeFullPath(p_relPathToNewFile);
        try
        {
            File fullPathFile = makeAdditionalDirs(fullPath);
            File originalFile = new File(p_absolutePathToOriginalFile);
            FileInputStream fis = new FileInputStream(originalFile);
            writeFile(fis, fullPathFile);
            if (p_removeOriginal)
            {
                originalFile.delete();
                if (originalFile.exists())
                {
                    CATEGORY.debug("Deleting file failed: setting deleteOnExit(): "
                            + p_absolutePathToOriginalFile);
                    originalFile.deleteOnExit();
                }
            }

            return fullPathFile.getAbsolutePath();
        }
        catch (Exception e)
        {
            String[] args =
            { fullPath };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_FAILED_TO_SAVE_FILE, args, e);
        }
    }

    // *************************************************
    // End public methods
    // *************************************************
    // *************************************************
    // Begin private methods
    // *************************************************

    /**
     * Returns the fullpath of a file given the relative path from the file
     * storage directory.
     * 
     * @param p_relPathToNewFile
     *            relative path
     * @return full path
     */
    private String makeFullPath(String p_relPathToNewFile)
    {
        // StringBuffer fullPath = new StringBuffer(m_absolutePath_doc);
        StringBuffer fullPath = new StringBuffer(
                AmbFileStoragePathUtils.getFileStorageDirPath());
        fullPath.append("/").append(p_relPathToNewFile);
        return fullPath.toString();
    }

    private File save(File p_parentDirectory, String p_storagePath, File p_file)
            throws NativeFileManagerException
    {
        File finalFile = null;
        if (p_file != null && p_file.exists())
        {
            try
            {
                // Make directories
                makeAdditionalDirs(p_parentDirectory, p_storagePath);

                // Write bytes to the file system
                // - writeFile() closes both streams
                finalFile = new File(p_parentDirectory, p_storagePath);
                writeFile(new FileInputStream(p_file), finalFile);
            }
            catch (Exception e)
            {
                String[] args =
                { finalFile.toString() };
                throw new NativeFileManagerException(
                        NativeFileManagerException.MSG_FAILED_TO_SAVE_FILE,
                        args, e);
            }
        }
        else
        {
            String[] args =
            { p_file == null ? " p_file=null" : p_file.getAbsolutePath()
                    + " does not exist!!" };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }
        return finalFile;
    }

    private byte[] getBytes(File p_parentDirectory, String p_storagePath)
            throws NativeFileManagerException
    {
        NativeFileManagerException ne = null;
        FileInputStream fis = null;
        byte[] contents = null;
        File fileToReadFrom = null;
        try
        {
            // Construct full path
            fileToReadFrom = new File(p_parentDirectory, p_storagePath);

            Long length = new Long(fileToReadFrom.length());
            contents = new byte[length.intValue()];// oh no...
            fis = new FileInputStream(fileToReadFrom);
            int i_read;
            while ((i_read = fis.read(contents, 0, length.intValue())) != -1)
            {
                // load contents in one read - bad
                // (need memory mapped file in java 1.4!!! or servlet)
                // cannot return a stream over rmi.

                if (i_read < 1)
                {
                    // We read 0 bytes!! This can happen for extremely long
                    // filenames, or deep paths, at which point even Windows
                    // breaks down (Windows Explorer for example). Java thinks
                    // it has successfully created this file, maybe even written
                    // out to it using FileOutputStream, but FileInputStream
                    // can't
                    // handle reading it because it doesn't really exist.
                    StringBuffer msg = new StringBuffer(
                            "Failed to read bytes from file ");
                    msg.append(fileToReadFrom.getAbsolutePath());
                    msg.append("\r\nThe filename may be much too long, or may not even exist.");
                    throw new Exception(msg.toString());
                }
            }
        }
        catch (Exception ex)
        {
            // hold over for finally
            String[] args =
            { fileToReadFrom.toString() };
            ne = new NativeFileManagerException(
                    NativeFileManagerException.MSG_FAILED_TO_GET_FILE, args, ex);
        }
        finally
        {
            if (fis != null)
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                }
            if (ne != null)
                throw ne;
        }

        return contents;
    }

    private String getString(File p_parentDirectory, String p_storagePath,
            String p_encoding) throws NativeEnDecoderException
    {
        NativeFileManagerException ne = null;
        FileInputStream fis = null;
        BufferedReader fisr = null;
        StringBuffer contents = new StringBuffer();
        File fileToReadFrom = null;
        try
        {
            // Construct full path
            fileToReadFrom = new File(p_parentDirectory, p_storagePath);
            char[] buffer = new char[1024 * 16];

            fis = new FileInputStream(fileToReadFrom);
            fisr = new BufferedReader(new InputStreamReader(fis, p_encoding));
            int i_read;
            while ((i_read = fisr.read(buffer)) != -1)
            {
                if (i_read < 1)
                {
                    // We read 0 bytes!! This can happen for extremely long
                    // filenames, or deep paths, at which point even Windows
                    // breaks down (Windows Explorer for example). Java thinks
                    // it has successfully created this file, maybe even written
                    // out to it using FileOutputStream, but FileInputStream
                    // can't
                    // handle reading it because it doesn't really exist.
                    StringBuffer msg = new StringBuffer(
                            "Failed to read bytes from file ");
                    msg.append(fileToReadFrom.getAbsolutePath());
                    msg.append("\r\nThe filename may be much too long, or may not even exist.");
                    throw new Exception(msg.toString());
                }
                else
                {
                    contents.append(buffer, 0, i_read);
                }
            }
        }
        catch (Exception ex)
        {
            // hold over for finally
            String[] args =
            { fileToReadFrom.toString() };
            ne = new NativeFileManagerException(
                    NativeFileManagerException.MSG_FAILED_TO_GET_FILE, args, ex);
        }
        finally
        {
            try
            {
                if (fisr != null)
                    fisr.close();
                if (fis != null)
                    fis.close();
            }
            catch (IOException e)
            {
            }
            if (ne != null)
                throw ne;
        }

        return contents.toString();
    }

    // write input stream to output file location and closes both streams
    private void writeFile(InputStream p_fis, File p_outputFile)
            throws Exception
    {
        Exception ee = null;
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(p_outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead = 0;
            while ((bytesRead = p_fis.read(buffer)) != -1)
            {
                fos.write(buffer, 0, bytesRead);
            }
            p_fis.close();
            fos.close();
        }
        catch (Exception e)
        {
            ee = e; // hold over for finally
        }
        finally
        {
            if (p_fis != null)
                try
                {
                    p_fis.close();
                }
                catch (IOException e)
                {
                }
            if (fos != null)
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                }
            if (ee != null)
                throw ee;
        }
    }

    // Just builds directories
    private synchronized void makeAdditionalDirs(File p_parentDir,
            String p_absolutePath)
    {
        File path = new File(p_parentDir, p_absolutePath);
        path.getParentFile().mkdirs();
    }

    private synchronized File makeAdditionalDirs(String p_absolutePath)
    {
        File f = new File(p_absolutePath);
        f.getParentFile().mkdirs();
        return f;
    }

    // Gets only the file name
    private String getFileName(String p_filepath)
    {
        int start = 0;

        // Handle Windows v/s Unix file path
        if ((start = p_filepath.lastIndexOf('\\')) > -1)
        {
            return p_filepath.substring(start + 1);
        }
        else if ((start = p_filepath.lastIndexOf('/')) > -1)
        {
            return p_filepath.substring(start + 1);
        }
        else
        {
            return p_filepath;
        }
    }

    private String resolveStoragePathCollision(String p_storagePath,
            String p_prefix, String p_newFilename)
            throws NativeFileManagerException
    {
        if (p_prefix == null || p_prefix.length() <= 0)
        {
            String[] args =
            { "adjustStoragePath(p_prefix is null)" };
            throw new NativeFileManagerException(
                    NativeFileManagerException.MSG_INVALID_ARGS, args, null);
        }

        File currentAbsoulutePath = new File(
                AmbFileStoragePathUtils.getStfParentDir(), p_storagePath);
        File newStoragePath = new File(currentAbsoulutePath.getParent(),
                p_newFilename);
        String origParent = new File(p_storagePath).getParent();
        // append stfId only when needed to avoid collision
        String newAdjustedFilename = newStoragePath.exists() ? p_prefix
                + p_newFilename : p_newFilename;
        return new File(origParent, newAdjustedFilename).toString();
    }
}