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
package com.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.Main;
import com.model.SystemInfo;

public class BuildUtil
{
    private static Logger log = Logger.getLogger(BuildUtil.class);

    public static String VERSION = (PropertyUtil.get(new File(
            "release.properties"), "newVersion")).trim();
    public static String ROOT = getRoot();
    private static String SOURCE_PATH = ROOT
            + "/main6/tools/client tools/GlobalSight Installer/source";
    private static String DOC_FILE = ROOT + "/main6/docs/OpenSource_GlobalSight/Installation Guides/GlobalSight_Upgrade_Installer.pdf";
    private static String DIST = ROOT + "/main6/tools/build/dist";

    private static String BUILD_FILE_PATH = "script/windows/build.bat";

    private static String SYSTE_INFO_PATH = getGlobalSightRoot()
            + "/GlobalSight/install/data/system.xml";

    private static void changeToLinux(File file)
    {
        if (file.exists())
        {
            try
            {
                BufferedReader in = new BufferedReader(new FileReader(file));
                StringBuilder content = new StringBuilder();
                String s = in.readLine();
                while (s != null)
                {
                    if (content.length() > 0)
                    {
                        content.append("\n");
                    }
                    content.append(s);
                    s = in.readLine();
                }
                
                BufferedWriter out = 
                    new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(
                                    file), "UTF-8"));
                out.write(content.toString());
                out.flush();
                out.close();
                in.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private static void compile()
    {
        changeToLinux(new File(SOURCE_PATH + "/run/build/run.sh"));
        changeToLinux(new File(SOURCE_PATH + "/run/patch/run.sh"));
        
        if (Main.isInLinux())
        {
            try
            {
                FileWriter out = new FileWriter(new File(
                        "./script/linux/build.sh"));
                out.write("cd \"" + SOURCE_PATH + "\"" + FileUtil.lineSeparator);
                out.write("ant buildInstaller");
                out.flush();
                out.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
            
            String[] cmd =
            { "sh", "./script/linux/build.sh"};
            try
            {
                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        else
        {
            String[] cmd =
            { BUILD_FILE_PATH };
            try
            {
                BufferedWriter out = 
                    new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(
                                    BUILD_FILE_PATH), "UTF-8"));  
                out.write("cd /D " + SOURCE_PATH + "\r\n");
                out.write("ant buildInstaller");
                out.flush();
                out.close();

                CmdUtil.run(cmd);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void saveSystemInfo()
    {
        SystemInfo info = new SystemInfo();
        info.setPreviousVersion(PropertyUtil.get(
                new File("release.properties"), "previousVersion"));
        info.setVersion(VERSION);
        File file = new File(SYSTE_INFO_PATH);
        file.getParentFile().mkdirs();
        XmlUtil.save(info, SYSTE_INFO_PATH);
    }

    private static void copyGlobalSight() throws Exception
    {
        File file = new File(GitUtil.SERVER_NAME);
        if (file.exists())
        {
            file.delete();
        }

        if (Main.IS_PATCH)
        {
            GitUtil.buildGlobalSight();
            file = new File(GitUtil.SERVER_NAME);
        }
        else
        {
            file = new File(DIST + "/" + GitUtil.SERVER_NAME);
            if (file.exists())
            {
                file.delete();
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String date = format.format(new Date());
            String fileName = "GlobalSight_" + date + ".zip";

            FileUtil.unzip(DIST, fileName);

            file = new File(DIST + "/" + GitUtil.SERVER_NAME);
        }

        File newFile = new File(getGlobalSightRoot() + GitUtil.SERVER_NAME);
        newFile.getParentFile().mkdirs();
        boolean copyed = file.renameTo(new File(getGlobalSightRoot()
                + GitUtil.SERVER_NAME));
        if (!copyed)
        {
            log.error("Failed to copy GlobalSight");
        }
        
        AboutModifier.modifyAboutJsp(getGlobalSightRoot());
    }

    private static String getGlobalSightRoot()
    {
        return Main.IS_PATCH ? SOURCE_PATH + "/installer_root/installer/data/"
                : SOURCE_PATH + "/installer_root/server/";
    }

    private static void zip()
    {
        File file = new File(getInstallerName());
        if (file.exists())
        {
            file.delete();
        }
        FileUtil.zip(SOURCE_PATH + "/installer_root", getInstallerName());
        File newFile = new File(SOURCE_PATH + "/installer_root",
                getInstallerName());
        file = new File(getInstallerName());
        newFile.renameTo(file);
    }

    private static void copyRun()
    {
        String path = Main.IS_PATCH ? "/run/patch" : "/run/build";
        path = SOURCE_PATH + path;
        try
        {
            FileUtil.copyFile(new File(path + "/run.sh"), new File(SOURCE_PATH
                    + "/installer_root/run.sh"));
            FileUtil.copyFile(new File(path + "/run.bat"), new File(SOURCE_PATH
                    + "/installer_root/run.bat"));
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private static void copyDoc()
    {
        try
        {
            FileUtil.copyFile(new File(DOC_FILE), new File(SOURCE_PATH
                    + "/installer_root/GlobalSight_Upgrade_Installer.pdf"));
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }

    public static void build() throws Exception
    {
        log("Compiling src...");
        compile();

        log("Copying GlobalSight to installer...");
        copyGlobalSight();

        log("Saving system info file...");
        saveSystemInfo();

        log("Copying run files...");
        copyRun();
        
        log("Copying installer document...");
        copyDoc();

        log("Zip...");
        zip();

        log("Finished, press Enter key to continue");
        System.in.read();
    }

    private static String getInstallerName()
    {
        if (isReleasePatch())
        {
            return "GlobalSight_Installer_" + VERSION + "(patch).zip";
        }

        return "GlobalSight_Installer_" + VERSION + ".zip";
    }

    private static boolean isReleasePatch()
    {
        if (Main.IS_PATCH)
        {
            try
            {
                String[] ints = VERSION.split("\\.");
                String last = ints[ints.length - 1];
                if ("0".equalsIgnoreCase(last))
                {
                    return true;
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    private static void log(String msg)
    {
        log.info(msg);
        System.out.println(msg);
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(PropertyUtil.get(new File("release.properties"),
                "root"));
        System.out.println(getRoot());
        copyDoc();
    }

    public static String getRoot()
    {
        String root = PropertyUtil.get(new File("release.properties"), "root");
        root = root.replace("\\", "/");
        root = root.replace(":/", ":\\\\");
        return root;
    }
}
