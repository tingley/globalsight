package com;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.util.CmdUtil;
import com.util.FileUtil;
import com.util.PropertyUtil;
import com.util.SvnUtil;

public class Main
{
    private static Logger log = Logger.getLogger(Main.class);

    private static String ROOT = PropertyUtil.get(
            new File("release.properties"), "root");
    private static String CLASS_ROOT = ROOT
            + "/Welocalize/main6/tools/build/capclasses/globalsight.ear/lib/classes/";

    private static String SERVER_NAME = PropertyUtil.get(new File(
            "release.properties"), "server.name");
    private static String NEW_CLASS_PATH = SERVER_NAME
            + "/jboss/jboss_server/server/default/deploy/globalsight.ear/lib/classes/";
    private static String NEW_JSP_PATH = SERVER_NAME
            + "/jboss/jboss_server/server/default/deploy/globalsight.ear/globalsight-web.war/";
    private static String NEW_INSTALL_DATA_PATH = SERVER_NAME
            + "/install/data/";

    private static String OLD_GLOBALSIGHT_JAR = ROOT
            + "/Welocalize/main6/tools/build/capclasses/globalsight.ear/lib/globalsight.jar";
    private static String NEW_GLOBALSIGHT_JAR = SERVER_NAME
            + "/jboss/jboss_server/server/default/deploy/globalsight.ear/lib/globalsight.jar";

    private static String INSTALL_JAR_PATH = ROOT
            + "/Welocalize/main6/tools/build/installclasses/installer.jar";
    private static String INSTALL_JAR_NEW_PATH = SERVER_NAME
            + "/install/installer.jar";

    public static String BASE_VERSION = null;
    public static String ROOT_PATH = "/home/max/svn/main-2-23/Welocalize/main6";

    private static Boolean COPY_iNSTALL_JAR = false;
    private static boolean IS_PATCH = true;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        log("args:");
        for (String s : args)
        {
            log(s);
        }
        log("");
        
        if (args.length < 1)
        {
            log("Please set the base version");
            return;
        }

        if (args.length > 1)
        {            
            if (args[1].indexOf("r") > -1)
            {
                IS_PATCH = false;
            }
        }

        BASE_VERSION = args[0];

        log.info("\nStart\n");
        log.info("Version: " + BASE_VERSION);
        int version = Integer.parseInt(BASE_VERSION);
        Set<String> files = SvnUtil.getChangedFilesFrom(version);
        Set<String> javas = new HashSet<String>();
        Set<String> underClassFiles = new HashSet<String>();
        Set<String> underWarFiles = new HashSet<String>();
        Set<String> others = new HashSet<String>();
        Set<String> ignores = new HashSet<String>();
        Set<String> installData = new HashSet<String>();

        for (String f : files)
        {
            if (f.indexOf("com/globalsight") > 0)
            {
                if (f.indexOf(".java") > 0)
                {
                    javas.add(f.substring(f.indexOf("com/globalsight")));
                }
                else
                {
                    underClassFiles.add(f);
                }
            }
            else if (f.indexOf("/src/java/") > 0)
            {
                if (f.indexOf(".java") > 0)
                {
                    javas.add(f.substring(f.indexOf("/src/java/")
                            + "/src/java/".length()));
                }
                else
                {
                    underClassFiles.add(f);
                }
            }
            else if (f.indexOf("/src/jsp/") > 0)
            {
                underWarFiles.add(f);
            }
            else if (f.indexOf("/src/help/") > 0)
            {
                underWarFiles.add(f);
            }
            else if (f.indexOf("/src/images/") > 0)
            {
                underWarFiles.add(f);
            }
            else if (f.indexOf("/envoy/schema/") > 0)
            {
                installData.add(f);
            }
            else if (f.indexOf("/install/data/") > 0)
            {
                installData.add(f);
            }
            else if (f.indexOf("GlobalSight Installer") > 0)
            {
                ignores.add(f);
            }
            else if (f.indexOf("/main6/docs/") > 0)
            {
                ignores.add(f);
            }
            else if (f.indexOf("(from") > 0)
            {
                ignores.add(f);
            }
            else if (f.indexOf("/tools/install/") > 0)
            {
                COPY_iNSTALL_JAR = true;
            }
            else
            {
                others.add(f);
            }
        }

        log("\n\nJava files:");
        print(javas);

        log("\n\nUnder class root files:");
        print(underClassFiles);

        log("\n\nUnder war root files:");
        print(underWarFiles);

        log("\n\nInstall data files:");
        print(installData);

        log("\n\nIgnore files:");
        print(ignores);

        log("\n\nOther files:");
        print(others);

        log("\n\nJava files:" + javas.size());
        log("\nUnder class root files:" + underClassFiles.size());
        log("Jsp files:" + underWarFiles.size());
        log("Install data files:" + installData.size());

        log("Ignore files:" + ignores.size());
        log("Other files:" + others.size());

        log("Copy install.jar:" + COPY_iNSTALL_JAR);
        log("Copy classes:" + IS_PATCH);

        copyClass(javas);
        copyInstallJar();
        copyUnderClassFiles(underClassFiles);
        copyUnderWarFiles(underWarFiles);
        copyInstallDataFiles(installData);
    }

    private static void copyInstallJar()
    {
        if (COPY_iNSTALL_JAR)
        {
            try
            {
                FileUtil.copyFile(new File(INSTALL_JAR_PATH), new File(
                        INSTALL_JAR_NEW_PATH));
                log("Add: " + "/install/installer.jar");
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void copyClass(String f)
    {
        String name = f.substring(f.lastIndexOf("/") + 1, f.lastIndexOf("."));
        String parent = f.substring(0, f.lastIndexOf("/"));
        File file = new File(CLASS_ROOT + parent);
        if (file.exists())
        {
            File[] fs = file.listFiles();
            int i = 0;
            for (File f1 : fs)
            {
                if (f1.getName().equals(name + ".class")
                        || f1.getName().startsWith(name + "$"))
                {
                    i++;
                    try
                    {
                        FileUtil.copyFile(f1, new File(NEW_CLASS_PATH + parent
                                + "/" + f1.getName()));
                        log("Add: " + parent + "/" + f1.getName());
                    }
                    catch (Exception e)
                    {
                        System.out.println(new File(NEW_CLASS_PATH + parent
                                + f1.getName()).getAbsolutePath());
                        log.error(e.getMessage(), e);
                    }
                }
            }

            if (i == 0)
            {
                log("Can not find " + CLASS_ROOT + parent + name + ".class");
            }
        }
        else
        {
            log("Can not find " + CLASS_ROOT + parent);
        }
    }

    private static void copyClass(Set<String> files)
    {
        if (IS_PATCH)
        {
            for (String f : files)
            {
                copyClass(f);
            }
        }
        else
        {
            try
            {
                FileUtil.copyFile(new File(OLD_GLOBALSIGHT_JAR), new File(
                        NEW_GLOBALSIGHT_JAR));
                log("Add: " + NEW_GLOBALSIGHT_JAR);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void copyUnderClassFiles(Set<String> files)
    {
        for (String f : files)
        {

            String path = f.substring(f.indexOf("/Welocalize"));

            String realPath = null;
            if (f.indexOf("/src/java/") > 0)
            {
                realPath = f.substring(f.indexOf("/src/java/")
                        + "/src/java/".length());
            }
            else if (f.indexOf("/main6/ling/") > 0)
            {
                realPath = f.substring(f.indexOf("/main6/ling/")
                        + "/main6/ling/".length());
            }

            if (path.endsWith(".jj") && IS_PATCH)
            {
                String parent = path.substring(0, path.lastIndexOf("/"));
                File src = new File(ROOT + parent);
                if (src.exists())
                {
                    File[] fs = src.listFiles();
                    for (File cf : fs)
                    {
                        if (cf.getName().endsWith("java"))
                        {
                            String fPath = cf.getPath();
                            String rPath = fPath.substring(fPath
                                    .indexOf("/com/") + 1);
                            System.out.println(rPath);
                            copyClass(rPath);
                        }
                    }
                }
            }

            File src = new File(ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(NEW_CLASS_PATH + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void copyUnderWarFiles(Set<String> files)
    {
        for (String f : files)
        {
            String path = f.substring(f.indexOf("/Welocalize"));
            String realPath = f
                    .substring(f.indexOf("/src/") + "/src/".length());
            if (f.indexOf("jsp/") >= 0)
            {
                realPath = f.substring(f.indexOf("jsp/") + "jsp/".length());
            }

            File src = new File(ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(NEW_JSP_PATH + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void copyInstallDataFiles(Set<String> files)
    {
        for (String f : files)
        {
            String path = f.substring(f.indexOf("/Welocalize"));
            String realPath = null;
            if (f.indexOf("/envoy/schema/") > 0)
            {
                realPath = f.substring(f.indexOf("/envoy/schema/")
                        + "/envoy/schema/".length());
            }
            else if (f.indexOf("/install/data/") > 0)
            {
                realPath = f.substring(f.indexOf("/install/data/")
                        + "/install/data/".length());
            }
            else
            {
                log.error("Don't know how to copy " + f);
                throw new IllegalArgumentException("Don't know how to copy "
                        + f);
            }

            File src = new File(ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(NEW_INSTALL_DATA_PATH
                        + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void print(Set<String> files)
    {
        for (String f : files)
        {
            log.info(f);
            System.out.println(f);
        }
    }

    private static void log(String msg)
    {
        System.out.println(msg);
        log.info(msg);
    }
}
