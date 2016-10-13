package util;

import jodd.util.StringUtil;

import java.io.File;

/**
 * Created by Vincent Yan on 13-11-26.
 */
public class FileUtil
{
    public static String getCurrentPath() {
        String currentPath = "";
        File dir = new File(".");
        currentPath = dir.getAbsolutePath();
        currentPath = currentPath.substring(0, currentPath.lastIndexOf(File.separator));
        currentPath = StringUtil.replace(currentPath, File.separator, "/");
        if (!currentPath.endsWith("/"))
            currentPath += "/";

        return currentPath;
    }
}
