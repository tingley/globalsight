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

package com.globalsight.util;

import java.io.File;
import java.text.NumberFormat;

import javazoom.upload.UploadFile;
import javazoom.upload.UploadListener;
import javazoom.upload.UploadParameters;

public class ProcessStatus implements UploadListener
{
    private static String[] SIZE =
    { " B", " KB", " MB", " GB" };

    private long size;
    private long totalSize;
    private boolean done = false;

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

    public void beginUpload()
    {
        size = 0;
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
