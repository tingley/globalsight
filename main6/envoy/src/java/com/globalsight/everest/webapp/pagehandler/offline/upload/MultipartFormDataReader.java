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

package com.globalsight.everest.webapp.pagehandler.offline.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.globalsight.io.UnicodeReader;

/**
 * MultipartFormDataReader reads multipart/form-data type HTTP request
 * (typically used for file upload).
 * 
 * NOTE: This is no longer the O'reily class we started with.
 * 
 */
public class MultipartFormDataReader
{
    private static final Logger CATEGORY = Logger
            .getLogger(MultipartFormDataReader.class.getName());

    private final static int MAX_LINE_LENGTH = 4096;

    private Hashtable<String, String> m_fields = new Hashtable<String, String>();

    @SuppressWarnings("unused")
    private String m_contentType = null;

    private String m_filename = null;
    private String m_filepath = null;

    /**
     * Uploads the file to a temporary location and returns a File object. The
     * File object is passed over to OfflineEditManager who re-opens and
     * processes the files contents and then removes the temporary file.
     */
    public File uploadToTempFile(HttpServletRequest p_request)
            throws MultipartFormDataReaderException
    {
        File outFile = null;
        BufferedOutputStream bos = null;

        try
        {
            byte[] inBuf = new byte[MAX_LINE_LENGTH];
            int bytesRead;
            ServletInputStream in;
            String contentType;
            String boundary;

            // Let's make sure that we have the right type of content
            contentType = p_request.getContentType();
            if (contentType == null
                    || !contentType.toLowerCase().startsWith(
                            "multipart/form-data"))
            {
                String[] arg = { "form did not use ENCTYPE=multipart/form-data but `"
                        + contentType + "'" };

                throw new MultipartFormDataReaderException(
                        MultipartFormDataReaderException.MSG_FAILED_TO_UPLOAD_FILE,
                        arg, null);
            }

            // Extract the boundary string in this request. The
            // boundary string is part of the content type string
            int bi = contentType.indexOf("boundary=");
            if (bi == -1)
            {
                String[] arg = { "no boundary string found in request" };

                throw new MultipartFormDataReaderException(
                        MultipartFormDataReaderException.MSG_FAILED_TO_UPLOAD_FILE,
                        arg, null);
            }
            else
            {
                // 9 := len("boundary=")
                boundary = contentType.substring(bi + 9);

                // The real boundary has additional two dashes in
                // front
                boundary = "--" + boundary;
            }

            in = p_request.getInputStream();
            bytesRead = in.readLine(inBuf, 0, inBuf.length);

            if (bytesRead < 3)
            {
                String[] arg = { "incomplete request (not enough data)" };

                // Not enough content was send as part of the post
                throw new MultipartFormDataReaderException(
                        MultipartFormDataReaderException.MSG_FAILED_TO_UPLOAD_FILE,
                        arg, null);
            }

            while (bytesRead != -1)
            {
                String lineRead = new String(inBuf, 0, bytesRead, "utf-8");
                if (lineRead
                        .startsWith("Content-Disposition: form-data; name=\""))
                {
                    if (lineRead.indexOf("filename=\"") != -1)
                    {
                        // This is a file part
                        // Get file name
                        setFilename(lineRead
                                .substring(0, lineRead.length() - 2));

                        // Get content type line
                        bytesRead = in.readLine(inBuf, 0, inBuf.length);
                        lineRead = new String(inBuf, 0, bytesRead - 2, "utf-8");
                        setContentType(lineRead);

                        // Read and ignore the blank line
                        bytesRead = in.readLine(inBuf, 0, inBuf.length);

                        // Create a temporary file to store the
                        // contents in it for now. We might not have
                        // additional information, such as TUV id for
                        // building the complete file path. We will
                        // save the contents in this file for now and
                        // finally rename it to correct file name.
                        //
                        outFile = File.createTempFile("GSOfflineUpload", null);
                        bos = new BufferedOutputStream(new FileOutputStream(
                                outFile), MAX_LINE_LENGTH * 4);

                        // Read through the file contents and write
                        // it out to a local temp file.
                        boolean writeRN = false;
                        while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
                        {
                            // Let's first check if we are already on
                            // boundary line
                            //
                            if (bytesRead > 2 && inBuf[0] == '-'
                                    && inBuf[1] == '-')
                            {
                                lineRead = new String(inBuf, 0, bytesRead,
                                        "utf-8");
                                if (lineRead.startsWith(boundary))
                                    break;
                            }

                            // Write out carriage-return, new-line
                            // pair which might have been left over
                            // from last write.
                            //
                            if (writeRN)
                            {
                                bos
                                        .write(new byte[] { (byte) '\r',
                                                (byte) '\n' });
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
                            if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
                                    && inBuf[bytesRead - 1] == '\n')
                            {
                                bos.write(inBuf, 0, bytesRead - 2);
                                writeRN = true;
                            }
                            else
                            {
                                bos.write(inBuf, 0, bytesRead);
                            }
                            bos.flush();
                        }
                        bos.close();
                    }
                    else
                    {
                        // This is the field part
                        // First get the field name
                        int start = lineRead.indexOf("name=\"");
                        int end = lineRead.indexOf("\"", start + 7);
                        String fieldName = lineRead.substring(start + 6, end);

                        // Read and ignore the blank line
                        bytesRead = in.readLine(inBuf, 0, inBuf.length);

                        // String Buffer to keep the field value
                        StringBuffer fieldValue = new StringBuffer();

                        boolean writeRN = false;
                        while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
                        {
                            lineRead = new String(inBuf, 0, bytesRead, "utf-8");

                            // Let's first check if we are already on
                            // boundary line
                            if (bytesRead > 2 && inBuf[0] == '-'
                                    && inBuf[1] == '-')
                            {
                                if (lineRead.startsWith(boundary))
                                    break;
                            }

                            // Write out carriage-return, new-line
                            // pair which might have been left over
                            // from last write.
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
                            if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
                                    && inBuf[bytesRead - 1] == '\n')
                            {
                                fieldValue.append(lineRead.substring(0,
                                        lineRead.length() - 2));
                                writeRN = true;
                            }
                            else
                            {
                                fieldValue.append(lineRead);
                            }
                        }

                        // Add field to collection of field
                        m_fields.put(fieldName, fieldValue.toString());
                    }
                }
                bytesRead = in.readLine(inBuf, 0, inBuf.length);
            }

            // skip BOM (Byte Order Mark) in unicode files
            // outFile = UnicodeReader.skipBom(outFile);
        }
        catch (MultipartFormDataReaderException se)
        {
            CATEGORY.info(se);
            throw new MultipartFormDataReaderException(se);
        }
        catch (IOException e)
        {
            CATEGORY.info(e);
            throw new MultipartFormDataReaderException(e);
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException ex)
                {
                    CATEGORY.info(ex);
                    throw new MultipartFormDataReaderException(ex);
                }
            }
        }

        return outFile;

    }

    public String getFilename()
    {
        return m_filename;
    }

    private void setFilename(String p_filenameLine)
    {
        int start = 0;

        if (p_filenameLine != null
                && (start = p_filenameLine.indexOf("filename=\"")) != -1)
        {
            m_filepath = p_filenameLine.substring(start + 10, p_filenameLine
                    .length() - 1);

            // Handle Windows v/s Unix file path
            if ((start = m_filepath.lastIndexOf('\\')) > -1)
            {
                m_filename = m_filepath.substring(start + 1);
            }
            else if ((start = m_filepath.lastIndexOf('/')) > -1)
            {
                m_filename = m_filepath.substring(start + 1);
            }
            else
            {
                m_filename = m_filepath;
            }
        }
    }

    private void setContentType(String p_contentLine)
    {
        int start = 0;

        if (p_contentLine != null
                && (start = p_contentLine.indexOf(": ")) != -1)
        {
            m_contentType = p_contentLine.substring(start + 2);
        }
        else
        {
            m_contentType = "application/octet-stream";
        }
    }
}
