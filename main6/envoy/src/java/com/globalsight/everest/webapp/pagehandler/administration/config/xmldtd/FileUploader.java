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

package com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.globalsight.util.ProcessStatus;
import com.globalsight.util.StringUtil;

public class FileUploader
{
    private static final Logger log = Logger.getLogger(FileUploader.class
            .getName());

    private final static String REG_FILE_NAME = ".*[\\\\/]";
    private Hashtable<String, String> fields = new Hashtable<String, String>();
    private File outFile = null;
    private String path;
    private String name = null;
    private Vector<ProcessStatus> listeners = new Vector<ProcessStatus>();

    public File getOutFile()
    {
        return outFile;
    }

    public void setOutFile(File outFile)
    {
        this.outFile = outFile;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public void addListener(ProcessStatus listener)
    {
        listeners.add(listener);
    }

    public File upload(HttpServletRequest request) throws Exception
    {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(1024000);
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> fileItems = upload.parseRequest(request);
        outFile = saveTmpFile(fileItems);

        return outFile;
    }

    public String getFieldValue(String p_fieldName)
    {
        if (p_fieldName == null)
        {
            return null;
        }

        return fields.get(p_fieldName);
    }

    private File saveTmpFile(List<FileItem> fileItems) throws Exception
    {

        File file = File.createTempFile("GSDTDUpload", null);

        // Set overall request size constraint
        long uploadTotalSize = 0;
        for (FileItem item : fileItems)
        {
            if (!item.isFormField())
            {
                uploadTotalSize += item.getSize();
            }
        }

        for (ProcessStatus status : listeners)
        {
            status.setTotalSize(uploadTotalSize);
        }

        log.debug("File size: " + uploadTotalSize);

        for (FileItem item : fileItems)
        {
            if (!item.isFormField())
            {
                item.write(file);
                setName(getFileName(item.getName()));
            }
            else
            {
                fields.put(item.getFieldName(), item.getString("utf-8"));
            }
        }

        for (ProcessStatus status : listeners)
        {
            status.finished();
        }

        return file;
    }

    /**
     * Gets the file name from the full file path format.
     * 
     * @param name
     * @return
     */
    private String getFileName(String name)
    {
        return name.replaceAll(REG_FILE_NAME, StringUtil.EMPTY_STRING);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
