package com.globalsight.util;

import java.io.IOException;

import org.apache.log4j.RollingFileAppender;

public class MyRollingFileAppender extends RollingFileAppender
{
    @Override
    public synchronized void setFile(String fileName, boolean append,
            boolean bufferedIO, int bufferSize) throws IOException
    {
        // Set log file path for Windows Vista.
        if (UsefulTools.isWindowsVista())
        {
            fileName = Constants.LOG_FILE;
        }
            
        super.setFile(fileName, append, bufferedIO, bufferSize);
    }
}
