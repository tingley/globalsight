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

package com.globalsight.util.progress;

import java.io.File;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

import javazoom.upload.UploadFile;
import javazoom.upload.UploadListener;
import javazoom.upload.UploadParameters;

import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tm.management.FileUploadHelper;

/**
 * Stores information while uploading and validating tm files.
 */
public class TmProcessStatus implements UploadListener
{
    private static String[] SIZE =
    { " B", " KB", " MB", " GB" };
    private static final String MSG_KEY_UPLOAD = "msg_upload_tm_file";
    private static final String MSG_KEY_VALIDATE = "msg_validate_tm_file";

    private long size;
    private String message;
    private long totalSize;
    private String msgkey = MSG_KEY_UPLOAD;
    private boolean done = false;
    private ResourceBundle bundle = null;
    private boolean canceled = false;

    private String totalTus = "0";
    private String errorTus = "0";

    private String savedFilepath = null;
    private String importOptions = null;

    private final static float K = 1024.0f;

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public void addSize(long size)
    {
        this.size += size;
    }

    public String getMessage()
    {
        if (message == null)
        {
            if (bundle == null)
            {
                bundle = PageHandler.getBundle(null);
            }

            message = MessageFormat.format(bundle.getString(msgkey),
                    getDisplayValue(totalSize, 2));

        }

        return message;
    }

    public void beginUpload()
    {
        size = 0;
        msgkey = MSG_KEY_UPLOAD;
        message = null;
    }

    public void finished()
    {
        size = totalSize;
        done = true;
    }

    public boolean isFinished()
    {
        return done;
    }

    public void beginValidation()
    {
        size = 0;
        msgkey = MSG_KEY_VALIDATE;
        message = null;
    }

    public int getPercentage()
    {
        if (totalSize == 0)
        {
            return 0;
        }
        
        int percentage = (int) (size * 100 / totalSize);
        if (percentage > 100)
        {
            percentage = 100;
        }
        if (percentage < 0)
        {
            percentage = 0;
        }

        return percentage;
    }

    public String getDisplaySize()
    {
        return getDisplayValue(size, 2);
    }

    private String getDisplayValue(float number, int fraction)
    {
        float p = number;

        int i = 0;
        while (p > K && i < SIZE.length)
        {
            p /= K;
            i++;
        }

        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(fraction);
        return format.format(p) + SIZE[i];
    }

    public long getTotalSize()
    {
        return totalSize;
    }

    public void setTotalSize(long totalSize)
    {
        this.totalSize = totalSize;
    }

    public void setBundle(ResourceBundle bundle)
    {
        this.bundle = bundle;
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void setCanceled(boolean canceled)
    {
        this.canceled = canceled;
    }

    public String getTotalTus()
    {
        return totalTus;
    }

    public void setTotalTus(String totalTus)
    {
        this.totalTus = totalTus;
    }

    public String getErrorTus()
    {
        return errorTus;
    }

    public void setErrorTus(String errorTus)
    {
        this.errorTus = errorTus;
    }

    public String getSavedFilepath()
    {
        return savedFilepath;
    }

    public void setSavedFilepath(String filepath)
    {
        savedFilepath = filepath;
    }

    public String getSavedFileUrl()
    {
        String path = getSavedFilepath();
        int index = path.indexOf(FileUploadHelper.FILE_UPLOAD_DIR);
        if (index > 0)
        {
            path = path.substring(index);
        }

        path = path.replace('\\', '/');

        return path;
    }

    public String getLogUrl()
    {
        return getSavedFileUrl() + ImportUtil.LOG_FILE_SUFFIX;
    }

    public String getErrorUrl()
    {
        return getSavedFileUrl() + ImportUtil.ERROR_FILE_SUFFIX;
    }

    public String getImportOptions()
    {
        return importOptions;
    }

    public void setImportOptions(String importOptions)
    {
        this.importOptions = importOptions;
    }

    @Override
    public void dataRead(int read)
    {
        size += read;
    }

    @Override
    public void fileUploadStarted(File tmpFile, int contentlength,
            String contenttype)
    {
        if (contentlength > 0)
        {
            totalSize = contentlength;
        }
    }

    @Override
    public void fileUploaded(UploadParameters up, UploadFile file)
    {
        //Do nothing.
    }
}
