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

package com.globalsight.everest.webapp.pagehandler.administration.jobAttribute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;

/**
 * Provides some methods to help management job attribute files.
 */
public class JobAttributeFileManager
{
    private static final Logger logger = Logger
            .getLogger(JobAttributeFileManager.class);

    /**
     * Gets all xml dtd files included in the xml dtd.
     * <p>
     * The xml dtd is specified by the parameter<code>id</code>.
     * 
     * @param id
     *            the id of the xml dtd.
     * @return
     */
    public static List<File> getAllFiles(String id)
    {
        List<File> files = new ArrayList<File>();
        File root = new File(getStorePath(id));
        if (root.exists())
        {
            files.addAll(FileUtil.getAllFiles(root));
        }

        return files;
    }

    public static List<File> getAllFiles(long id)
    {
        return getAllFiles(Long.toString(id));
    }

    public static List<String> getAllFilesAsString(long id)
    {
        return getAllFilesAsString(Long.toString(id));
    }

    public static List<String> getAllFilesAsString(String id)
    {
        List<String> files = new ArrayList<String>();
        File root = new File(getStorePath(id));
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root);
            for (File f : fs)
            {
                files.add(getDisplayPath(id, f));
            }
        }

        SortUtil.sort(files);
        return files;
    }

    /**
     * Get the path for display.
     * 
     * @param id
     * @param file
     * @return
     */
    public static String getDisplayPath(String id, File file)
    {
        Assert.assertFileExist(file);
        String path = getPath(file.getPath());
        String StorePath = getPath(getStorePath(id));
        return path.replace(StorePath + "/", "");
    }

    public static String getDisplayPath(long id, File file)
    {
        return getDisplayPath(Long.toString(id), file);
    }

    public static String getDisplayPath2(String id, File file, String companyId)
    {
        Assert.assertFileExist(file);
        String path = getPath(file.getPath());
        String StorePath = getPath(getStorePath2(id, companyId));
        return path.replace(StorePath + "/", "");
    }

    private static String getPath(String path)
    {
        return path.replace("\\", "/");
    }

    /**
     * Gets the store path of the xml dtd according to the xml dtd id.
     * <p>
     * Will throw exception if the xml dtd with the id can not be found in
     * database.
     * 
     * @param id
     *            The id of the xml dtd.
     * @return
     */
    public static String getStorePath(String id)
    {
        return AmbFileStoragePathUtils.getJobAttributeDir().getPath() + "/"
                + id;
    }

    public static String getStorePath(long id)
    {
        return getStorePath(Long.toString(id));
    }

    public static String getStorePath2(String id, String companyId)
    {
        return AmbFileStoragePathUtils.getJobAttributeDir2(companyId).getPath()
                + "/" + id;
    }

    public static List<String> getAllFilesAsString2(long id, String companyId)
    {
        List<String> files = new ArrayList<String>();
        File root = new File(getStorePath2(Long.toString(id), companyId));
        if (root.exists())
        {
            List<File> fs = FileUtil.getAllFiles(root);
            for (File f : fs)
            {
                files.add(getDisplayPath2(Long.toString(id), f, companyId));
            }
        }

        SortUtil.sort(files);
        return files;
    }
}
