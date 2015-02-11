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

package com.globalsight.everest.webapp.pagehandler.tm.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.globalsight.util.progress.TmProcessStatus;

public class FileUploadHelper implements Runnable
{
    private static final Logger CATEGORY = Logger
            .getLogger(FileUploadHelper.class.getName());

    private final static String REG_FILE_NAME = ".*[\\\\/]";

    private final static String IMPORT_OPTIONS = "importoptions";

    public final static String FILE_UPLOAD_DIR = "_Imports_";

    public static String DOCROOT = "/";

    private TmProcessStatus status = null;
    private File outFile = null;

    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();

            String root = sc
                    .getStringParameter(SystemConfigParamNames.WEB_SERVER_DOC_ROOT);

            if (!(root.endsWith("/") || root.endsWith("\\")))
            {
                root = root + "/";
            }

            DOCROOT = root;
        }
        catch (Throwable e)
        {
            CATEGORY.error("cannot create directory " + DOCROOT);
        }
    }

    private Hashtable<String, String> m_fields = new Hashtable<String, String>();

    private String m_contentType = null;

    private String m_filename = null;

    private String m_filepath = null;

    public String getFieldValue(String p_fieldName)
    {
        if (p_fieldName == null)
        {
            return null;
        }

        return m_fields.get(p_fieldName);
    }

    public String getFilename()
    {
        return m_filename;
    }

    public String getFilepath()
    {
        return m_filepath;
    }

    public String getContentType()
    {
        return m_contentType;
    }

    private String getImportOptions()
    {
        String value = getFieldValue(IMPORT_OPTIONS);

        if (value == null)
        {
            return "";
        }

        return value;
    }

    private void setFilename(String p_filenameLine)
    {

        m_filename = p_filenameLine;

    }

    private File saveTmpFile(List<FileItem> fileItems) throws Exception
    {
        File file = null;

        // Create a temporary file to store the contents in it for now. We might
        // not have additional information, such as TUV id for building the
        // complete file path. We will save the contents in this file for now
        // and finally rename it to correct file name.
        file = File.createTempFile("GSTMUpload", null);

        // Set overall request size constraint
        long uploadTotalSize = 0;
        for (FileItem item : fileItems)
        {
            if (!item.isFormField())
            {
                uploadTotalSize += item.getSize();
            }
        }
        status.setTotalSize(uploadTotalSize);

        for (FileItem item : fileItems)
        {
            if (!item.isFormField())
            {
                // If it's a ZIP archive, then expand it on the fly.
                // Disallow archives containing multiple files; let the
                // rest of the import/validation code figure out if the
                // contents is actually TMX or not.
                String fileName = getFileName(item.getName());
                if (fileName.toLowerCase().endsWith(".zip")) {
                    CATEGORY.info("Encountered zipped upload " + fileName);
                    ZipInputStream zis = 
                        new ZipInputStream(item.getInputStream());
                    boolean foundFile = false;
                    for (ZipEntry e = zis.getNextEntry(); e != null; 
                            e = zis.getNextEntry()) {
                        if (e.isDirectory()) {
                            continue;
                        }

                        if (foundFile) {
                            throw new IllegalArgumentException(
                                    "Uploaded zip archives should only " +
                                    "contain a single file.");
                        }
                        foundFile = true;

                        FileOutputStream os = new FileOutputStream(file);
                        int expandedSize = copyData(zis, os);
                        os.close();
                        // Update file name and size to reflect zip entry
                        setFilename(getFileName(e.getName()));
                        status.setTotalSize(expandedSize);
                        CATEGORY.info("Saved archive entry " + e.getName() +
                                " to tempfile " + file);
                    }
                }
                else {
                    item.write(file);
                    setFilename(fileName);
                    CATEGORY.info("Saving upload " + fileName + 
                                  " to tempfile " + file);
                }
            }
            else
            {
                m_fields.put(item.getFieldName(), item.getString());
            }
        }

        return file;
    }    

    /**
     * Renames a file from its originalName to a newName. NOTE:
     * java.io.File.renameTo() has some known bugs that apparently are still
     * unfixed in 1.3 so it cannot be used. <br>
     * This implementation simply copies originalName to newName and then
     * deletes the originalName file.
     * 
     * @param p_originalName --
     *            source file name
     * @param p_newName --
     *            destination file name
     */
    public static void renameFile(File p_originalName, File p_newName)
            throws IOException
    {
        FileInputStream fis = new FileInputStream(p_originalName);
        FileOutputStream fos = new FileOutputStream(p_newName);
        byte buffer[] = new byte[2056];
        boolean keepReading = true;
        int numBytesRead = -1;
        while (keepReading)
        {
            numBytesRead = fis.read(buffer);
            if (numBytesRead == -1)
                keepReading = false;
            else
                fos.write(buffer, 0, numBytesRead);
        }

        fis.close();
        fos.close();
        p_originalName.delete();
    }

    /**
     * Attaches an import event listener.
     */
    public void attachListener(TmProcessStatus p_listener)
    {
        status = p_listener;
    }

    /**
     * Gets the file name from the full file path format.
     * 
     * @param name
     * @return
     */
    public String getFileName(String name)
    {
        return name.replaceAll(REG_FILE_NAME, StringUtil.EMPTY_STRING);
    }

    public void uploadWithValidation(HttpServletRequest p_request)
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        
        if (status == null)
        {
            return;    
        }

        status.setBundle(bundle);
        status.setTotalSize(p_request.getContentLength());
        
        try
        {
            status.beginUpload();
            upload(p_request);

            status.beginValidation();
            validate();
            
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
    }
    
    private void upload(HttpServletRequest p_request) throws Exception
    {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024000);
        ServletFileUpload upload = new ServletFileUpload(factory);

        List<FileItem> fileItems = upload.parseRequest(p_request);

        outFile = saveTmpFile(fileItems);
    }
    
    private void validate()
    {
       MultiCompanySupportedThread thread = new MultiCompanySupportedThread(this);
       thread.start();
    }

    @Override
    public void run()
    {
     // By now, we should have all parameter values needed to
        // contruct a correct filepath to save the uploaded
        // images
        //
        if (outFile != null && outFile.exists())
        {
			String fsDirPath = AmbFileStoragePathUtils.getFileStorageDirPath();
            File savedDir = new File(fsDirPath,
                    AmbFileStoragePathUtils.TM_IMPORT_FILE_SUB_DIR);
			savedDir.mkdirs();
			File savedFile = new File(savedDir, getFilename());

            try
            {
                ImportUtil.createInstance().saveTmFileWithValidation(outFile,
                        savedFile, status);
                boolean success = outFile.delete();
                if (! success)
                {
                    CATEGORY.warn("Failed to delete temporary file " +
                            outFile);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage(), e);
            }
            finally
            {
                outFile.delete();
            }
            status.setSavedFilepath(savedFile.getAbsolutePath());
        }
        status.setImportOptions(getImportOptions());

        status.finished();        
    }

    private int copyData(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int r;
        int count = 0;
        while ((r = is.read(buffer)) != -1) {
            os.write(buffer, 0, r);
            count += r;
        }
        return count;
    }
}
