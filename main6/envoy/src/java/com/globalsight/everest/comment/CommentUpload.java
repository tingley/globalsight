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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.util.AmbFileStoragePathUtils;

public class CommentUpload
{
    //
    // Public Member variables
    //

    // Attribute names expected in the http request. These correspond
    // to the field names in the html form used to post the data.

    /** Temporary directory to upload the files */
    public final static String KEY_TMP_DIR = "tmpdir";

    /** One or more target locales of the uploaded file */
    public final static String KEY_TARGET_LOCALE = "trg";

//    /** The directory to which files are uploaded. Needs trailing "/". */
//    public final static String UPLOAD_DIRECTORY = "GlobalSight/CommentReference/";

    /** the job id to which the workflow comment reference belongs */
    public final static String KEY_JOB_ID = "jobid";

    /** the access restrictions for the comment reference file */
    public final static String KEY_ACCESS_TYPE = "access";
//
//    /* The directory to which temp files are created. */
//    private static File UPLOAD_TMP_DIRECTORY = null;

    /** the restricted field which determines files access */
    public final static String RESTRICTED_FIELD = "restricted";

    /** the restricted file directory */ 
    public final static String RESTRICTED = "Restricted";

    /** the general file directory */ 
    public final static String GENERAL = "General";

//    public static String UPLOAD_BASE_DIRECTORY = "/";
    
    
//    static {
//        try
//        {
//            SystemConfiguration sc = SystemConfiguration.getInstance();
//
//            UPLOAD_BASE_DIRECTORY = sc.getStringParameter(
//                SystemConfiguration.FILE_STORAGE_DIR);
//
//            if (!(UPLOAD_BASE_DIRECTORY.endsWith("/") ||
//                  UPLOAD_BASE_DIRECTORY.endsWith("\\")))
//            {
//                UPLOAD_BASE_DIRECTORY = UPLOAD_BASE_DIRECTORY + "/";
//            }
//
//            UPLOAD_TMP_DIRECTORY
//                = new File(UPLOAD_BASE_DIRECTORY + UPLOAD_DIRECTORY + "tmp/");
//            UPLOAD_TMP_DIRECTORY.mkdirs();
//        }
//        catch (Throwable e)
//        {
//            // well, what do we say to that?
//        }
//    }

    //
    // Private Member variables
    //

    private final static int MAX_LINE_LENGTH = 4096;

    private Hashtable m_fields = new Hashtable();
    private String    m_contentType = null;
    private String    m_filename = null;
    private String    m_savedFilepath = null;
    private File      m_tempFile = null;

    //
    // Methods
    //

    /**
     * Main method: reads an uploaded file from a http request (result
     * of a form post) and saves it to the file system.
     */
    public void doUpload (HttpServletRequest p_request)
        throws CommentException
    {
        try
        {
            // read request and save uploaded file into a temp file
            readRequest(p_request);
            // verify that the upload contained full information;
            // throws exception if not
            verifyUpload();
            // rename the temp file to the first real file
            boolean fromUI =  true;
            renameFile(fromUI, p_request, null, -1, null);
            // if requested, copy file to other src/trg locations
        }
        catch (SecurityException se)
        {
            throw new CommentException(se);
        }
        catch (IOException ioe)
        {
            throw new CommentException(ioe);
        }
    }

    /**
     * Main method: reads file 
     * and saves it to the file system.
     */
    public void doUpload (File p_file, String p_restrict, long p_taskId, String p_userId)
        throws CommentException
    {
        try
        {
            // read file and save uploaded file into a temp file
            readFile(p_file);
            // verify that the upload contained full information;
            // throws exception if not
            verifyUpload();
            // rename the temp file to the first real file
            boolean fromUI = false;
            renameFile(fromUI, null, p_restrict, p_taskId, p_userId);
        }
        catch (SecurityException se)
        {
            throw new CommentException(se);
        }
        catch (IOException ioe)
        {
            throw new CommentException(ioe);
        }
    }

    /**
     * Parses a HttpRequest into parameters and file data and saves
     * the file data into a temporary file.
     */
    private void readRequest (HttpServletRequest p_request)
        throws CommentException,
               IOException
    {
        byte[]              inBuf = new byte[MAX_LINE_LENGTH];
        int                 bytesRead;
        ServletInputStream  in;
        String              contentType;
        String              boundary;

        //  Let's make sure that we have the right type of content
        //
        contentType = p_request.getContentType();
        if (contentType == null ||
            !contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            String[] arg = {
               "form did not use ENCTYPE=multipart/form-data but `" +
                contentType + "'"
            };

            throw new CommentException(
                CommentException.MSG_FAILED_TO_UPLOAD_FILE,
                arg, null);
        }

        //  Extract the boundary string in this request. The
        //  boundary string is part of the content type string
        //
        int bi = contentType.indexOf("boundary=");
        if (bi == -1)
        {
            String[] arg = {
                "no boundary string found in request"
            };

            throw new CommentException(
                CommentException.MSG_FAILED_TO_UPLOAD_FILE,
                arg, null);
        }
        else
        {
            // 9 := len("boundary=")
            boundary = contentType.substring(bi + 9);

            //  The real boundary has additional two dashes in
            //  front
            //
            boundary = "--" + boundary;
        }

        in = p_request.getInputStream();
        bytesRead = in.readLine(inBuf, 0, inBuf.length);

        if (bytesRead < 3)
        {
            String[] arg = {
            "incomplete request (not enough data)"
            };

            //  Not enough content was send as part of the post
            //
            throw new CommentException(
                CommentException.MSG_FAILED_TO_UPLOAD_FILE,
                arg, null);
        }

        while (bytesRead != -1)
        {
            String lineRead = new String(inBuf, 0, bytesRead, "utf-8");
            if (lineRead.startsWith("Content-Disposition: form-data; name=\""))
            {
                if (lineRead.indexOf("filename=\"") != -1)
                {
                    //  This is a file part

                    //  Get file name
                    setFilename(lineRead.substring(0, lineRead.length() - 2));

                    //  Get content type line
                    bytesRead = in.readLine(inBuf, 0, inBuf.length);
                    lineRead = new String(inBuf, 0, bytesRead - 2, "utf-8");
                    setContentType(lineRead);

                    //  Read and ignore the blank line
                    bytesRead = in.readLine(inBuf, 0, inBuf.length);

                    //  Create a temporary file to store the
                    //  contents in it for now.
                    //  Save the contents in this file for now and
                    //  finally rename it to correct file name.
                    //
                    m_tempFile  = File.createTempFile("GSCommentsUpload", null, 
                            AmbFileStoragePathUtils.getCommentReferenceTempDir());

                    FileOutputStream fos = new FileOutputStream(m_tempFile);
                    BufferedOutputStream bos =
                        new BufferedOutputStream(fos, MAX_LINE_LENGTH * 4);

                    //  Read through the file contents and write
                    //  it out to a local temp file.
                    //
                    boolean writeRN = false;
                    while ((bytesRead =
                        in.readLine(inBuf, 0, inBuf.length)) != -1)
                    {
                        //  Let's first check if we are already on
                        //  boundary line
                        //
                        if (bytesRead > 2 &&
                            inBuf[0] == '-' &&
                            inBuf[1] == '-')
                        {
                            lineRead = new String(inBuf, 0, bytesRead, "utf-8");
                            if (lineRead.startsWith(boundary))
                            {
                                break;
                            }
                        }

                        //  Write out carriage-return, new-line
                        //  pair which might have been left over
                        //  from last write.
                        //
                        if (writeRN)
                        {
                            bos.write(new byte[] {(byte)'\r', (byte)'\n'});
                            writeRN = false;
                        }

                        //  The ServletInputStream.readline() adds
                        //  "\r\n" bytes for the last line of the
                        //  file contents.  If we find these pair
                        //  as the last bytes we need to delay
                        //  writing it until the next go, since it
                        //  could very well be the last line of
                        //  file content.
                        //
                        if (bytesRead > 2 &&
                            inBuf[bytesRead - 2] == '\r' &&
                            inBuf[bytesRead - 1] == '\n')
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
                    //  This is the field part

                    //  First get the field name
                    //
                    int     start = lineRead.indexOf("name=\"");
                    int     end = lineRead.indexOf("\"", start + 7);
                    String  fieldName = lineRead.substring(start + 6, end);

                    //  Read and ignore the blank line
                    bytesRead = in.readLine(inBuf, 0, inBuf.length);

                    //  String Buffer to keep the field value
                    //
                    StringBuffer fieldValue = new StringBuffer();

                    boolean writeRN = false;
                    while ((bytesRead =
                        in.readLine(inBuf, 0, inBuf.length)) != -1)
                    {
                        lineRead = new String(inBuf, 0, bytesRead, "utf-8");

                        //  Let's first check if we are already on
                        //  boundary line
                        //
                        if (bytesRead > 2 &&
                            inBuf[0] == '-' &&
                            inBuf[1] == '-')
                        {
                            if (lineRead.startsWith(boundary))
                                break;
                        }

                        //  Write out carriage-return, new-line
                        //  pair which might have been left over
                        //  from last write.
                        //
                        if (writeRN)
                        {
                            fieldValue.append("\r\n");
                            writeRN = false;
                        }

                        //  The ServletInputStream.readline() adds
                        //  "\r\n" bytes for the last line of the
                        //  field value.  If we find these pair as
                        //  the last bytes we need to delay
                        //  writing it until the next go, since it
                        //  could very well be the last line of
                        //  field value.
                        //
                        if (bytesRead > 2 &&
                            inBuf[bytesRead - 2] == '\r' &&
                            inBuf[bytesRead - 1] == '\n')
                        {
                            fieldValue.append(lineRead.substring(
                                0, lineRead.length() - 2));
                            writeRN = true;
                        }
                        else
                        {
                            fieldValue.append(lineRead);
                        }
                    }

                    // Add field to collection of field
                    //
                    setFieldValue(fieldName, fieldValue.toString());
                }
            }

            bytesRead = in.readLine(inBuf, 0, inBuf.length);
        }
    }

    /**
     * Parses the file and saves
     * the file data into a temporary file.
     */
    private void readFile (File p_file)
        throws CommentException,
               IOException
    {
        DataInputStream in = null; 

        //  Get file name
        setFilename(p_file);
        //  Create a temporary file to store the
        //  contents in it for now. 
        m_tempFile = File.createTempFile("GSCommentsFile", null, 
                AmbFileStoragePathUtils.getCommentReferenceTempDir());

        //  Read through the file contents and write
        //  it out to a local temp file.
        //
        FileInputStream fis = new FileInputStream(p_file); 
        BufferedInputStream bis = new BufferedInputStream(fis); 
        in = new DataInputStream(bis);  
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(m_tempFile)));
        String  line;
        while ((line = in.readLine()) != null) 
        {
             out.println(line);
        }
        out.close();
        fis.close();
        bis.close();
        in.close();
    }

    /**
     * Verifies that the uploaded information was complete
     * temporary file
     * must have been successfully created from the uploaded file. If
     * not, we throw an exception.
     */
    private void verifyUpload()
        throws CommentException
    {

        if (m_tempFile == null ||
            (m_tempFile != null && !m_tempFile.exists()))
        {
            String[] arg = {
               "temporary file could not be created"
            };

            throw new CommentException(
                CommentException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
        }
    }

    /**
     * Renames the temporary file to the real name; if that fails, the
     * temporary file is deleted.
     */
    private void renameFile(boolean p_fromUI,
                            HttpServletRequest p_request, 
                            String p_restrict, 
                            long p_taskId, 
                            String p_userId)
        throws CommentException,
               SecurityException
    {
        String restrictedValue = null;
        String tempDir = null;
        if(p_request != null)
        {
            //  We should have all parameter values needed to construct a
            //  correct filepath to save the uploaded file.
            //
            restrictedValue = p_request.getParameter("restricted");
            if (restrictedValue != null) 
            {
                restrictedValue = restrictedValue.trim();
                if (restrictedValue.length() == 0 || restrictedValue.equals("false"))
                {
                    restrictedValue = null;
                }
            }
        }
        else if(p_restrict.equals("true"))
        {
            restrictedValue = "true";
        }
        else
        {
            restrictedValue = null;
        }
        if (p_fromUI) 
        {
            tempDir = getTmpDir();
        }
        else
        {
            tempDir = WebAppConstants.COMMENT_REFERENCE_TEMP_DIR 
                + p_taskId 
                + p_userId;
        }
        boolean restricted = (restrictedValue != null);

        String access = "";
        if(restricted)
        {
            access = RESTRICTED;
        }
        else
        {
            access = GENERAL;
        }
        if (m_tempFile != null && m_tempFile.exists())
        {
            try
            {
                // First, define and create upload 
                // directory structure,
                // if not already done so
                setSavedFilepath(
                        AmbFileStoragePathUtils.getCommentReferenceDirPath()
                        + "/" + tempDir + "/" +  access + "/");

                File savedDir = new File(getSavedFilepath());
                savedDir.mkdirs();

                //  Create a destination file and 
                //  rename/move the file
                //  from temporary location to upload directory
                //
                File finalFile = new File(savedDir, getFilename());
                m_tempFile.renameTo(finalFile);
            }
            catch (SecurityException ex)
            {
                try
                {
                    m_tempFile.delete();
                }
                catch (Exception e)
                {
                }

                throw ex;
            }
        }
    }


    private void setFieldValue (String p_fieldName, String p_value)
    {
        m_fields.put(p_fieldName, p_value);
    }

    private String getFieldValue (String p_fieldName)
    {
        if (p_fieldName == null)
        {
            return null;
        }
        else
        {
            return (String) m_fields.get(p_fieldName);
        }
    }

    private Enumeration getFields ()
    {
        return m_fields.keys();
    }

    private void setSavedFilepath(String p_filePath)
    {
        m_savedFilepath = p_filePath;
    }

    private String getSavedFilepath()
    {
        return m_savedFilepath;
    }

    private String getTargetLocale()
    {
        return getFieldValue(KEY_TARGET_LOCALE);
    }

    private String getJobId()
    {
        return getFieldValue(KEY_JOB_ID);
    }

    private String getFileAccessType()
    {
        return getFieldValue(KEY_ACCESS_TYPE);
    }

    private String getTmpDir()
    {
        return getFieldValue(KEY_TMP_DIR);
    }
    private void setFilename (File p_file)
    {
        m_filename = p_file.getName();
    }
    private void setFilename (String p_filenameLine)
    {
        int start = 0;

        if (p_filenameLine != null &&
            (start = p_filenameLine.indexOf("filename=\"")) != -1)
        {
            String filepath = p_filenameLine.substring(start + 10,
                p_filenameLine.length() - 1);

            //  Handle Windows v/s Unix file path
            //
            if ((start = filepath.lastIndexOf('\\')) > -1)
            {
                m_filename = filepath.substring(start + 1);
            }
            else if ((start = filepath.lastIndexOf('/')) > -1)
            {
                m_filename = filepath.substring(start + 1);
            }
            else
            {
                m_filename = filepath;
            }
        }
    }

    public String getFilename ()
    {
        return m_filename;
    }

    private void setContentType (String p_contentLine)
    {
        int start = 0;

        if (p_contentLine != null &&
            (start = p_contentLine.indexOf(": ")) != -1)
        {
            m_contentType = p_contentLine.substring(start + 2);
        }
        else
        {
            m_contentType = "application/octet-stream";
        }
    }

    private String getContentType ()
    {
        return m_contentType;
    }
}
