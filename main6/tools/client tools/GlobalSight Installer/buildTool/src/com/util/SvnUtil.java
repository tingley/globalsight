package com.util;

import java.util.HashSet;
import java.util.Set;

import com.Main;

public class SvnUtil
{
    public static int getVersion()
    {
        String version = null;
        try
        {
            Set<String> versions = run("script/linux/version.sh");
            version = versions.iterator().next().trim();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Integer.parseInt(version);
    }

    public static Set<String> getChangedFiles(int version)
    {
        Set<String> files = new HashSet<String>();
        String[] cmd =
        { "sh", "script/linux/changedFiles.sh", String.valueOf(version), Main.ROOT_PATH };
        try
        {
            files.addAll(CmdUtil.run(cmd));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return files;
    }

    public static Set<String> getChangedFilesFrom(int version)
    {
        Set<String> files = new HashSet<String>();
        int newVersion = getVersion();
        for (int i = version; i <= newVersion; i++)
        {
            files.addAll(getChangedFiles(i));
        }

        return files;
    }

    public static Set<String> run(String path)
    {
        String[] cmd =
        { "sh", path, Main.ROOT_PATH};
        Set<String> files = new HashSet<String>();
        try
        {
            files.addAll(CmdUtil.run(cmd));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return files;
    }
}
