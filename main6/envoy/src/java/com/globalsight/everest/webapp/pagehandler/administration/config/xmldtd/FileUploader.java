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

import javazoom.upload.parsing.CfuFileItem;
import javazoom.upload.parsing.CfuFileItemFactory;

import org.apache.commons.fileupload.FileUpload;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.ProcessStatus;
import com.globalsight.util.StringUtil;

public class FileUploader
{
    private static final GlobalSightCategory log = (GlobalSightCategory) GlobalSightCategory
            .getLogger(FileUploader.class.getName());

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
        log.info("Uploading files...");

        CfuFileItemFactory factory = new CfuFileItemFactory();
        factory.setListeners(listeners);
        
        FileUpload upload = new FileUpload(factory);
        List<CfuFileItem> fileItems = upload.parseRequest(request);
        outFile = saveTmpFile(fileItems);

        log.info("Uploading files finished.");

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

    private File saveTmpFile(List<CfuFileItem> fileItems) throws Exception
    {

        File file = File.createTempFile("~GS", null);

        // Set overall request size constraint
        long uploadTotalSize = 0;
        for (CfuFileItem item : fileItems)
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

        for (CfuFileItem item : fileItems)
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
