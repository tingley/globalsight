package com.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A util class, let operate file more easy.
 * 
 */
public class FileUtil
{
    /**
     * Copies one file to a specified file.
     * 
     * @param src
     * @param dst
     * @throws Exception
     */
    public static void copyFile(File src, File dst) throws Exception
    {
        File parent = dst.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try
        {
            int len = 0;
            byte[] buf = new byte[1024];
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dst);
            while ((len = fis.read(buf)) != -1)
            {
                fos.write(buf, 0, len);
            }
            fos.flush();
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }

            if (fos != null)
            {
                fos.close();
            }
        }
    }

    /**
     * Gets all files under the specified fold.
     * 
     * @param root
     *            the specified fold
     * @return A list includes all files under the specified fold.
     */
    public static List<File> getAllFiles(File root)
    {
        return getAllFiles(root, null);
    }

    /**
     * Gets all files under the specified fold.
     * 
     * @param root
     *            the specified fold
     * @return A list includes all files under the specified fold.
     */
    public static List<File> getAllFiles(File root, FileFilter filter)
    {
        List<File> files = new ArrayList<File>();
        if (root.isFile())
        {
            if (filter == null || filter.accept(root))
            {
                files.add(root);
            }
        }
        else
        {
            File[] fs = root.listFiles();
            for (File f : fs)
            {
                files.addAll(getAllFiles(f, filter));
            }
        }

        return files;
    }
}
