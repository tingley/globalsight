package com.globalsight.selenium.functions.DownloadFileRead;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.globalsight.selenium.properties.ConfigUtil;

/**
 * Read File from the basePathResult directory, because selenium can not get the
 * downloaded file name with Firefox. As we deployed, Firefox will download the
 * file to "\\server ip\GlobalSight Automation Files\ResultFiles\".
 * 
 * 1. If you know the file name, get the file name with the file 2. If you don't
 * know the file name, get the file from the directory, when we verified the
 * file, moved the file to the subfile folder.
 * 
 * @author leon
 * 
 */
public class FileRead
{
    private String basePathResult = null;

    /**
     * Init basePathResult. The base file path is the directory that firefox
     * used to download. "Base_Path_Result" property in ConfigData.properties
     * file.
     */
    public FileRead()
    {
        basePathResult = ConfigUtil.getConfigData("Base_Path_Result");
    }

    /**
     * Get the file by file name
     * 
     * @param fileName
     * @return
     */
    public File getFile(String fileName)
    {
        File file = new File(basePathResult + fileName);
        return file;
    }

    /**
     * Get the newest file in the directory
     * 
     * @return
     */
    public File getNewestFile()
    {
        File dir = new File(basePathResult);
        if (dir.exists())
        {
            File files[] = dir.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (!files[i].isDirectory())
                {
                    return files[i];
                }
            }
        }
        return null;
    }

    /**
     * Move the file to the subfile directory. Make sure there are only the
     * newest file in the basePathResult.
     * 
     * @param file
     */
    public void moveFile(File file)
    {
        String fileName = file.getName();
        File newFile = new File(basePathResult + "files\\" + fileName);
        file.renameTo(newFile);
    }

    /**
     * Read the content with read line
     * 
     * @return content
     */
    public String getFileContent(File file)
    {
        String result = null;
        String temp = null;
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF-16"));
            while ((temp = br.readLine()) != null)
            {
                result = result + temp;
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Delete directory
     * 
     * @param path
     */
    public void deleteDirectory(String path)
    {
        File f = new File(path);
        if (!f.exists())
        {
            System.out.println("not exists");
            return;
        }
        if (f.delete())
        {
            System.out.println("delete directory : " + f.getAbsolutePath());
        }
        else
        {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++)
            {
                if (fs[i].isDirectory())
                {
                    if (!fs[i].delete())
                        deleteDirectory(fs[i].getAbsolutePath());
                    else
                        System.out.println("delete directory : "
                                + fs[i].getAbsolutePath());
                }
                else
                {
                    fs[i].delete();
                    System.out.println("delete file : "
                            + fs[i].getAbsolutePath());
                }
            }
            f.delete();
            System.out.println("delete directory : " + f.getAbsolutePath());
        }
    }
}
