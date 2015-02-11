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
package com.globalsight.cxe.message;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * The MessageDataFactory is used to create different types of MessageData
 * objects
 */
public class MessageDataFactory
{
    private static Logger c_logger = Logger.getLogger(MessageDataFactory.class);

    /**
     * The system wide known location for file storage
     */
    // private static File s_tmpDir;
    // static
    // {
    // try {
    // SystemConfiguration sc = SystemConfiguration.getInstance();
    // String fileStorageDir =
    // sc.getStringParameter(SystemConfigParamNames.FILE_STORAGE_DIR);
    // fileStorageDir = fileStorageDir + File.separator + "GlobalSight" +
    // File.separator + "CXE";
    // s_tmpDir = new File(fileStorageDir);
    // if (s_tmpDir.exists()==false)
    // {
    // c_logger.info("Making " + fileStorageDir);
    // s_tmpDir.mkdirs();
    // }
    //
    // c_logger.info("Using " + fileStorageDir +
    // " as the CXE messaging directory.");
    // }
    // catch (Exception e)
    // {
    // c_logger.error("Could not set tmp directory for CXE messaging. Using C:\\TEMP",e);
    // s_tmpDir = new File("C:\\TEMP");
    // }
    // s_tmpDir.mkdirs();
    // }

    /**
     * Creates a FileMessageData object using a temporary file name.
     * 
     * @return FileMessageData
     * @exception IOException
     */
    public static FileMessageData createFileMessageData() throws IOException
    {
        File tmpFile = File.createTempFile("GSMessageData", null,
                AmbFileStoragePathUtils.getTempFileDir());
        return new FileMessageData(tmpFile.getAbsolutePath());
    }

    public static FileMessageData createFileMessageData(String ext)
            throws IOException
    {
        String suffix = "." + ext;
        File tmpFile = File.createTempFile("GSMessageData", suffix,
                AmbFileStoragePathUtils.getTempFileDir());
        return new FileMessageData(tmpFile.getAbsolutePath());
    }
}
