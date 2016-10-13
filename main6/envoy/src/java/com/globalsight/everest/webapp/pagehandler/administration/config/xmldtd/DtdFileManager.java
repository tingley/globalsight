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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;

/**
 * Provides some methods to help management xml dtd files.
 */
public class DtdFileManager
{
    private static final Logger logger = Logger
            .getLogger(DtdFileManager.class);

    private static final String DTD_REGEX = "<!DOCTYPE[^>]*?\"([^>\"]*?\\.dtd)\"[^>]*?>";

    /**
     * Gets all xml dtd files included in the xml dtd.
     * <p>
     * The xml dtd is specified by the parameter<code>id</code>.
     * 
     * @param id
     *            the id of the xml dtd.
     * @return
     */
    public static List<File> getAllFiles(long id)
    {
        List<File> files = new ArrayList<File>();
        File root = new File(getStorePath(id));
        if (root.exists())
        {
            files.addAll(FileUtil.getAllFiles(root, getDtdFileter()));
        }

        return files;
    }

    /**
     * A file filter that only pick up dtd files.
     * 
     * @return
     */
    private static FileFilter getDtdFileter()
    {
        return new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.getName().endsWith(".dtd");
            }
        };
    }

    /**
     * Searchs the dtd file with xml dtd id and the xml dtd file path.
     * <p>
     * Return null if the dtd file can not be found.
     * 
     * @param id
     *            The id of xml dtd.
     * @param path
     *            The path of xml dtd file.
     * @return
     */
    public static File getFile(long id, String path)
    {
        if (StringUtil.isEmpty(path))
        {
            return null;
        }

        path = getPath(path);
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        File file = new File(getStorePath(id) + "/" + path);
        if (!file.exists())
        {
            file = null;
        }

        return file;
    }

    /**
     * Searchs the dtd file with xml dtd id and the xml file. The dtd file is
     * definded in the xml file.
     * <p>
     * Return null if the dtd file can not be found.
     * 
     * @param id
     * @param xmlFile
     * @return
     */
    public static File getDtdFile(long id, File xmlFile)
    {
        Assert.assertFileExist(xmlFile);

        String path = getDtdFilePath(xmlFile);
        if (logger.isDebugEnabled())
        {
            logger.debug("XML file name: " + xmlFile.getName());
            logger.debug("Defined dtd path: " + path);
        }

        if (StringUtil.isEmpty(path))
        {
            return null;
        }

        path = getPath(path);
        while (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        File file = null;

        while (file == null &&  path != null && path.length() > 0)
        {
            file = new File(getStorePath(id) + "/" + path);
            if (!file.exists())
            {
                file = null;
                int index = path.indexOf("/");
                if (index > 0)
                {
                    path = path.substring(index + 1);
                }
                else
                {
                    path = null;
                }
            }
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Found dtd file path: " + (file == null ? "null" : file
                    .getPath()));            
        }

        return file;
    }

    /**
     * Gets the definded dtd file path.
     * 
     * @param file
     *            An xml file, maybe defind a dtd validation file.
     * @return the path of the definded dtd file. Maybe null.
     */
    private static String getDtdFilePath(File file)
    {
        String dtd = null;
        String encoding;
        try
        {
            encoding = FileUtil.getEncodingOfXml(file);
            byte[] bs = FileUtil.readFile(file, 300);
            Pattern p = Pattern.compile(DTD_REGEX);
            Matcher m = p.matcher(new String(bs, encoding));
            if (m.find())
            {
                dtd = m.group(1);
            }
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }

        return dtd;
    }

    /**
     * Get the path for display.
     * 
     * @param id
     * @param file
     * @return
     */
    public static String getDisplayPath(long id, File file)
    {
        Assert.assertFileExist(file);
        String path = getPath(file.getPath());
        String StorePath = getPath(getStorePath(id));
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
    public static String getStorePath(long id)
    {
        return AmbFileStoragePathUtils.getDtdDir().getPath() + "/" + id;
    }
}
