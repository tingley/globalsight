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
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.Main;

public class CvsUtil
{
    private static List<String> FOLDER = new ArrayList<String>();
    private static Set<String> FILE = new HashSet<String>();
    private static String PATH = null;
    private static Logger log = Logger.getLogger(CvsUtil.class);
    private static String ENTRIES_PATH = "/CVS/Entries";
    private static String ENTRIES_PATH_LOG = "/CVS/Entries.Log";
    private static SimpleDateFormat FORMAT = new SimpleDateFormat(
            "EEE MMM dd kk:mm:ss yyyy", Locale.ENGLISH);

    private static Date FROM_DATE = null;

    private static String CLASS_ROOT = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/lib/classes/";
    
    private static String SERVICE_CLASS_ROOT = BuildUtil.ROOT
            + "/main6/tools/globalsightServices/globalsightServices/";

    public static String SERVER_NAME = "GlobalSight";

    private static String DEPLOY_PATH = SERVER_NAME
            + "/jboss/server/standalone/deployments/";
    private static String NEW_EAR_PATH = DEPLOY_PATH + "globalsight.ear";
//    private static String NEW_JMX_CONSOLE_CLASS = DEPLOY_PATH + "jmx-console.war/WEB-INF/classes";
    private static String NEW_CLASS_PATH = NEW_EAR_PATH + "/lib/classes/";
    private static String NEW_SERVICE_CLASS_ROOT = NEW_EAR_PATH + "/globalsightServices.war/";

    private static String SERVER_BIN_PATH = SERVER_NAME
            + "/jboss/server/bin/";
    public static String NEW_WAR_PATH = NEW_EAR_PATH + "/globalsight-web.war/";
    private static String NEW_INSTALL_DATA_PATH = SERVER_NAME
            + "/install/data/";

    private static String NEW_PROPERTIES_ROOT = NEW_CLASS_PATH + "/properties/";

    private static String OLD_GLOBALSIGHT_JAR = BuildUtil.ROOT
            + "/main6/tools/build/capclasses/globalsight.ear/globalsight.jar";
    private static String NEW_GLOBALSIGHT_JAR = NEW_EAR_PATH
            + "/globalsight.jar";

//    private static String OLD_WRAPPER_FILE_PATH = BuildUtil.ROOT
//            + "/main6/tools/install/JavaServiceWrapper/conf/wrapper.conf.template";
//    private static String WRAPPER_FILE_PATH = SERVER_NAME
//            + "/install/JavaServiceWrapper/conf/wrapper.conf.template";

//    private static Boolean COPY_GRAPHICAL_WF_JAR = false;

    private static Boolean COPY_BIN_FILES = false;

//    private static Boolean COPY_WRAPPER_FILE = false;

    private static Map<String, String> SPECIAL_FILES = new HashMap<String, String>();
    static
    {
        SPECIAL_FILES.put("/web.xml.template", NEW_EAR_PATH
                + "/globalsight-web.war/WEB-INF/web.xml.template");
        SPECIAL_FILES.put("/globalsight.tld", NEW_EAR_PATH
                + "/globalsight-web.war/WEB-INF/tlds/globalsight.tld");
        SPECIAL_FILES
                .put("/conf/globalsight_ori.keystore",
                        SERVER_NAME
                                + "/jboss/util/globalsight_ori.keystore");
        SPECIAL_FILES.put("/default/conf/standardjboss.xml", SERVER_NAME
                + "/jboss/server/standalone/configuration/standardjboss.xml");
        SPECIAL_FILES.put("/envoy/src/web.xml.template", NEW_EAR_PATH
                + "/globalsight-web.war/WEB-INF/web.xml.template");
        SPECIAL_FILES.put("/server/default/conf/OpenSSL_Sign.txt", SERVER_NAME
                + "/jboss/util/OpenSSL_Sign.txt");
        SPECIAL_FILES
                .put("/terminology/importer/TBXcdv04.dtd",
                        SERVER_NAME
                                + "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/resources/TBXcdv04.dtd");
        SPECIAL_FILES
                .put("/SRX2.0.xsd.template",
                        SERVER_NAME
                                + "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/SRX2.0.xsd.template");
        SPECIAL_FILES
                .put("/extractor/xml/xml.xsd",
                        SERVER_NAME
                                + "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/xml.xsd");
        SPECIAL_FILES
                .put("/server-config.wsdd",
                        SERVER_NAME
                                + "/jboss/server/standalone/deployments/globalsight.ear/globalsight-web.war/WEB-INF/server-config.wsdd");
		SPECIAL_FILES
				.put("/db_connection.properties.template",
						SERVER_NAME
								+ "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/db_connection.properties.template");
    }

    private static Map<String, String> BIN_FILES = new HashMap<String, String>();
    static
    {
        BIN_FILES.put("/j2eeVendor/jboss/jboss_server/bin/run.bat", "run.bat");
        BIN_FILES
                .put("/j2eeVendor/jboss/jboss_server/bin/run.conf", "run.conf");
    }

    private static List<String> PROPERTIES_FILES = new ArrayList<String>();
    static
    {
        PROPERTIES_FILES.add("SRX2.0.xsd");
        PROPERTIES_FILES.add("schemarules.rng");
        PROPERTIES_FILES.add("LCID.properties");
        PROPERTIES_FILES.add("Tags.properties");
        PROPERTIES_FILES.add("Wordcounter.properties");
        PROPERTIES_FILES.add("WordExtractor.properties");
        PROPERTIES_FILES.add("MSXlsxXmlRule.properties");
        PROPERTIES_FILES.add("MSPptxXmlRule.properties");
        PROPERTIES_FILES.add("MSHeaderXmlRule.properties");
        PROPERTIES_FILES.add("MSDocxXmlRule.properties");
        PROPERTIES_FILES.add("MSXlsxSheetXmlRule.properties");
        PROPERTIES_FILES.add("MSCommentXmlRule.properties");
        PROPERTIES_FILES.add("envoy.properties");
        PROPERTIES_FILES.add("ResxRule.properties");
        PROPERTIES_FILES.add("IdmlAdapter.properties");
        PROPERTIES_FILES.add("idmlrule.properties");
        PROPERTIES_FILES.add("AdobeAdapter.properties");
        PROPERTIES_FILES.add("Passolo.properties");
        PROPERTIES_FILES.add("PassoloAdapter.properties");
        PROPERTIES_FILES.add("WindowsPEAdapter.properties");
        PROPERTIES_FILES.add("WhitespaceForExport.properties");
    }

    private static List<String> UNDER_WAR_FILTER = new ArrayList<String>();
    static
    {
        UNDER_WAR_FILTER.add("/src/jsp/");
        UNDER_WAR_FILTER.add("/envoy/src/includes/");
        UNDER_WAR_FILTER.add("/src/help/");
        UNDER_WAR_FILTER.add("/src/images/");
        UNDER_WAR_FILTER.add("/reports/messages/");
        UNDER_WAR_FILTER.add("/envoy/src/dojo/");
        UNDER_WAR_FILTER.add("/envoy/src/dijit/");
        UNDER_WAR_FILTER.add("/envoy/src/dojox/");
        UNDER_WAR_FILTER.add("/envoy/src/jquery/");
        UNDER_WAR_FILTER.add("/envoy/src/helptb/");
        UNDER_WAR_FILTER.add("/envoy/src/FCKeditor/");
    }

    private static List<String> INSTALL_DATA_FILTER = new ArrayList<String>();
    static
    {
        INSTALL_DATA_FILTER.add("/envoy/schema/");
        INSTALL_DATA_FILTER.add("/install/data/");
    }

    public static List<String> IGNORE_INSTALL_DATA_FILTER = new ArrayList<String>();
    static
    {
        IGNORE_INSTALL_DATA_FILTER.add("/installAmbassador.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installDefaultValues.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installDefaultValuesNoUI.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installDisplay_en_US.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installDisplay.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installNoUI.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installOrder.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installOrderUI.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installUI.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/installValueTypes.properties");
        IGNORE_INSTALL_DATA_FILTER.add("/mergeProperties.properties");
    }

    private static List<String> IGNORE_FILTER = new ArrayList<String>();
    static
    {
        IGNORE_FILTER.add("/.gitignore");
        IGNORE_FILTER.add("/visual-basic/");
        IGNORE_FILTER.add("/QA_Automation/");
        IGNORE_FILTER.add("GlobalSight Installer");
        IGNORE_FILTER.add("main6/docs/");
        IGNORE_FILTER.add("main6/test/");
        IGNORE_FILTER.add("/envoy/hibernatetest/");
        IGNORE_FILTER.add("(from");
        IGNORE_FILTER.add("/ambassador-desktop/");
        IGNORE_FILTER.add("/tools/client tools/");
        IGNORE_FILTER.add("/src/webserviceClients/");
        IGNORE_FILTER.add("/tools/build/");
        IGNORE_FILTER.add("/webtop-plugin/");
        IGNORE_FILTER.add("/AdobeXmpRule.properties");
        IGNORE_FILTER.add("/inddrule.properties");
        IGNORE_FILTER.add("/dist/conv_adobe_indesign_cs4.zip");
        IGNORE_FILTER.add("/build/buildnum.txt");
        IGNORE_FILTER.add("/diplomat/dev/doc/");
        IGNORE_FILTER.add("build.xml");
        IGNORE_FILTER.add("main6/diplomat/dev/src/c-sharp/");
        IGNORE_FILTER.add(".review");
        IGNORE_FILTER.add(".jupiter");
        IGNORE_FILTER.add("main6/tools/sso/");
        IGNORE_FILTER.add("/ling/test/");
        IGNORE_FILTER.add("/WEB-INF/lib/xerces.jar");
        IGNORE_FILTER.add("/tm3tool.cmd");
        IGNORE_FILTER.add("/tm3tool.sh");
        IGNORE_FILTER.add("main6/tools/converters/build/dist/");
        IGNORE_FILTER.add("/tools/gsjava/");
        IGNORE_FILTER.add("/tools/lingtools/");
        IGNORE_FILTER.add("/dev/src/web-doc/");
    }

    private static List<String> NO_PRINT_IGNORE = new ArrayList<String>();
    static
    {
        NO_PRINT_IGNORE.add("/QA_Automation/");
    }

    private static List<String> DEPLOY_FILTER = new ArrayList<String>();
    static
    {
        DEPLOY_FILTER.add("/default/deploy/");
    }
    
    private static Set<String> ERROR_PATHS = new HashSet<String>();

    private static Set<String> getChangedFiles()
    {
        if (PATH == null)
        {
            PATH = BuildUtil.ROOT;
            FOLDER.add(PATH);
        }

        while (FOLDER.size() > 0)
        {
            PATH = FOLDER.remove(0);
            if (PATH != null)
            {
                File file = new File(PATH);
                if (!file.exists())
                    log.info(file.getPath() + " is not exist");
                else
                {
                    File entriesFile = new File(PATH + ENTRIES_PATH);
                    if (!entriesFile.exists())
                        log.info(entriesFile.getPath() + " is not exist");
                    else
                        parseEntriesFile(entriesFile);

                    File entriesLogFile = new File(PATH + ENTRIES_PATH_LOG);

                    if (entriesLogFile.exists())
                        parseEntriesFile(entriesLogFile);
                }
            }
        }
        return FILE;
    }

    private static void parseEntriesFile(File file)
    {
        BufferedReader in;
        try
        {
            in = new BufferedReader(new FileReader(file));
            String line = in.readLine();

            while (line != null)
            {
                String[] contents = line.split("/");

                if (line.endsWith("////"))
                    // is folder
                    FOLDER.add(PATH + "/" + contents[1]);
                else if (contents.length > 3)
                {
                    if (!"0".equals(contents[2])
                            && !"Result of merge".equals(contents[3])
                            && validateChangedTime(contents[3]))
                        FILE.add(PATH + "/" + contents[1]);
                }

                line = in.readLine();
            }
        }
        catch (Exception e)
        {
        	log.error("File Path: " + file.getAbsolutePath());
            log.error(e.getMessage(), e);
        }
    }

    private static boolean validateChangedTime(String time) throws Exception
    {
        Date date = FORMAT.parse(time);
        return date.after(getFromDate());
    }

    private static Date getFromDate() throws Exception
    {
        if (FROM_DATE == null)
        {
            String previousVersion = PropertyUtil.get(new File(
                    "release.properties"), "previousVersion");
            String time = PropertyUtil.get(new File("releaseDate.properties"),
                    previousVersion);

            if (time == null)
                throw new Exception("Can not get the release time of version: "
                        + previousVersion
                        + ", please add it to releaseDate.properties");

            SimpleDateFormat format = new SimpleDateFormat(
                    "MM-dd-yyyy kk:mm:ss", Locale.ENGLISH);
            FROM_DATE = format.parse(time);
        }

        return FROM_DATE;
    }

    public static void buildGlobalSight()
    {
        Set<String> javas = new HashSet<String>();
        Set<String> underClassFiles = new HashSet<String>();
        Set<String> underJbossFiles = new HashSet<String>();
        Set<String> underWarFiles = new HashSet<String>();
        Set<String> others = new HashSet<String>();
        Set<String> ignores = new HashSet<String>();
        Set<String> installData = new HashSet<String>();
        Set<String> specialFiles = new HashSet<String>();
        Set<String> deployFiles = new HashSet<String>();
        Set<String> propertiesFiles = new HashSet<String>();
        Set<String> binFiles = new HashSet<String>();
        Set<String> commonJars = new HashSet<String>();
        Set<String> ssoJars = new HashSet<String>();
        Set<String> gsService = new HashSet<String>();
//        Set<String> jmxConsoles = new HashSet<String>();

        Set<String> files = CvsUtil.getChangedFiles();
        
        PatchConfig config = new PatchConfig();
        config.run();

        for (String f : files)
        {
            boolean copyJar = JarManager.accept(f);

            if (inInclude(f, SPECIAL_FILES.keySet()))
            {
                specialFiles.add(f);
            }
//            else if (f.indexOf("/jmx-console.war/WEB-INF/classes") > 0)
//            {
//            	jmxConsoles.add(f);
//            }
            else if (inInclude(f, NO_PRINT_IGNORE))
            {
                // do nothing
            }
            else if (inInclude(f, IGNORE_FILTER))
            {
                ignores.add(f);
            }
            else if (inInclude(f, BIN_FILES.keySet()))
            {
                binFiles.add(f);
            }

            else if (inInclude(f, PROPERTIES_FILES))
            {
                propertiesFiles.add(f);
            }
            else if (f.indexOf("/globalsightServices/src-java/") > 0)
            {
            	gsService.add(f);
            }
            else if (f.indexOf("com/globalsight") > 0)
            {
                if (f.indexOf("ambassador-desktop") > 0)
                {
                    ignores.add(f);
                }
                else if (f.indexOf(".java") > 0)
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
            else if (inInclude(f, UNDER_WAR_FILTER))
            {
                underWarFiles.add(f);
            }
            else if (inInclude(f, INSTALL_DATA_FILTER))
            {
                if (inInclude(f, IGNORE_INSTALL_DATA_FILTER))
                {
                    ignores.add(f);
                }
                else
                {
                    installData.add(f);
                }
            }
            else if (inInclude(f, DEPLOY_FILTER))
            {
                deployFiles.add(f);
            }
            else if (f.indexOf("/tools/lib/common/") > 0)
            {
                commonJars.add(f);
            }
            else if (f.indexOf("/tools/lib/sso/") > 0)
            {
                ssoJars.add(f);
            }
            else if (f.indexOf("/j2eeVendor/jboss/v7.1.1.Final/") > 0)
            {
            	underJbossFiles.add(f);
            }
            else if (!copyJar)
            {
                others.add(f);
            }

//            if (f.indexOf("/gui/planview/") > 0)
//            {
//                COPY_GRAPHICAL_WF_JAR = true;
//            }

            if (f.indexOf("/jboss/jboss_server/bin/") > 0)
            {
                COPY_BIN_FILES = true;
            }

//            if (f.indexOf("/install/JavaServiceWrapper/conf/") > 0
//                    && !"8.0".equals(BuildUtil.VERSION))
//            {
//                COPY_WRAPPER_FILE = true;
//            }
        }

        print(specialFiles, "Special files:");
//        print(jmxConsoles, "Jmx consoles files:");
        print(propertiesFiles, "Property files without company:");
        print(javas, "Java files:");
        print(underJbossFiles, "Under jboss files:");
        print(underClassFiles, "Under class root files:");
        JarManager.printAddedJars();
        print(underWarFiles, "Under war root files:");
        print(installData, "Install data files:");
        print(deployFiles, "Under deploy files:");
        print(commonJars, "Common jars:");
        print(ssoJars, "Sso jars:");
        print(ignores, "Ignore files:");
        print(others, "Other files:");

        log("");
        log("Special files:" + specialFiles.size());
//        log("Jmx consoles files:" + jmxConsoles.size());
        log("Property files:" + propertiesFiles.size());
        log("Java files:" + javas.size());
        log("Under jboss files:" + underJbossFiles.size());
        log("Under class root files:" + underClassFiles.size());
        log("Jars:" + JarManager.getAddedJars().size());
        log("Under deploy files:" + deployFiles.size());
        log("Jsp files:" + underWarFiles.size());
        log("Install data files:" + installData.size());
        log("Common jars:" + commonJars.size());
        log("Sso jars:" + ssoJars.size());
        log("Ignore files:" + ignores.size());
        log("Other files:" + others.size());

        log("Copy classes:" + Main.IS_PATCH);

        copySpecialFiles(specialFiles);
//        copyJmxConsoles(jmxConsoles);
        copyPropertyFiles(propertiesFiles);
        copyUnderJbossFiles(underJbossFiles);
        copyClass(javas);
        JarManager.addJars();
        copyBinFiles(binFiles);
//        copyWrapperFile();
        copyDeployFiles(deployFiles);
        copyUnderClassFiles(underClassFiles);
        copyUnderWarFiles(underWarFiles);
        copyCommonJars(commonJars);
        copySsoJars(ssoJars);
        copyInstallDataFiles(installData);
        copyGsServiceClass(gsService);
        
        print(ERROR_PATHS, "Error files:");
    }

//    private static void copyWrapperFile()
//    {
//        if (COPY_WRAPPER_FILE)
//        {
//            try
//            {
//                FileUtil.copyFile(new File(OLD_WRAPPER_FILE_PATH), new File(
//                        WRAPPER_FILE_PATH));
//                log("Add: "
//                        + "wrapper.conf.template. After apply this patch, please go to server to re-install windows server.");
//            }
//            catch (Exception e)
//            {
//                log.error(e.getMessage(), e);
//            }
//        }
//    }

    private static void copyBinFiles(Set<String> binFiles)
    {
        if (COPY_BIN_FILES)
        {
            for (String f : binFiles)
            {
                File src = new File(f);

                for (String name : BIN_FILES.keySet())
                {
                    if (f.indexOf(name) > 0)
                    {
                        String path = BIN_FILES.get(name);
                        File dst = new File(SERVER_BIN_PATH + path);

                        try
                        {
                            FileUtil.copyFile(src, dst);
                            log("Add bin files: " + path);
                        }
                        catch (Exception e)
                        {
                            log.error(e.getMessage(), e);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static boolean inInclude(String f, Collection<String> filters)
    {
        for (String filter : filters)
        {
            if (f.indexOf(filter) > 0)
                return true;
        }

        return false;
    }
    
    private static void copyGsServiceClass(String f)
    {
        String name = f.substring(f.lastIndexOf("/") + 1, f.lastIndexOf("."));
        String parent = f.substring(0, f.lastIndexOf("/"));
        File file = new File(SERVICE_CLASS_ROOT + parent);
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
                        FileUtil.copyFile(f1, new File(NEW_SERVICE_CLASS_ROOT + parent
                                + "/" + f1.getName()));
                        log("Add: " + parent + "/" + f1.getName());
                    }
                    catch (Exception e)
                    {
                        log.error(e.getMessage(), e);
                    }
                }
            }

            if (i == 0)
                log("Can not find " + CLASS_ROOT + parent + "/" + name
                        + ".class");
        }
        else
            log("Can not find " + CLASS_ROOT + parent);
    }
    
    private static void copyGsServiceClass(Set<String> files)
    {
        for (String f : files)
        {
        	copyGsServiceClass(f);
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
                        log.error(e.getMessage(), e);
                    }
                }
            }

            if (i == 0)
                log("Can not find " + CLASS_ROOT + parent + "/" + name
                        + ".class");
        }
        else
            log("Can not find " + CLASS_ROOT + parent);
    }

    private static void copyClass(Set<String> files)
    {
        if (!BuildUtil.VERSION.endsWith(".0") && !"8.1.1".equals(BuildUtil.VERSION))
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

    private static void copySpecialFiles(Set<String> files)
    {
        for (String f : files)
        {
            File src = new File(f);

            for (String name : SPECIAL_FILES.keySet())
            {
                if (f.indexOf(name) > 0)
                {
                    String path = SPECIAL_FILES.get(name);
                    File dst = new File(path);

                    try
                    {
                        FileUtil.copyFile(src, dst);
                        log("Add: " + path);
                    }
                    catch (Exception e)
                    {
                        log.error(e.getMessage(), e);
                    }
                    break;
                }
            }
        }

    }

    private static void copyPropertyFiles(Set<String> files)
    {
        for (String f : files)
        {
            File src = new File(f);

            for (String name : PROPERTIES_FILES)
            {
                if (f.indexOf(name) > 0)
                {

                    File dst = new File(NEW_PROPERTIES_ROOT + name);

                    try
                    {
                        FileUtil.copyFile(src, dst);
                        log("Add: /properties/" + name);
                    }
                    catch (Exception e)
                    {
                        log.error(e.getMessage(), e);
                    }
                    break;
                }
            }
        }

    }
    
    private static void copyUnderJbossFiles(Set<String> files)
    {
    	for (String f : files)
        {
            String path = f.substring(f.indexOf("/main6"));
            String realPath = f
                    .substring(f.indexOf("/j2eeVendor/jboss/v7.1.1.Final/") + "/j2eeVendor/jboss/v7.1.1.Final/".length());

            File src = new File(BuildUtil.ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(SERVER_NAME + "/jboss/" + realPath));
                log("Add: " + realPath);
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

            String path = f.substring(f.indexOf("/main6"));

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

            if (path.endsWith(".jj") && Main.IS_PATCH)
            {
                String parent = path.substring(0, path.lastIndexOf("/"));
                File src = new File(BuildUtil.ROOT + parent);
                if (src.exists())
                {
                    File[] fs = src.listFiles();
                    for (File cf : fs)
                    {
                        if (cf.getName().endsWith("java"))
                        {
                            String fPath = cf.getPath();
                            fPath = fPath.replace("\\", "/");
                            String rPath = fPath.substring(fPath
                                    .indexOf("/com/") + 1);
                           
                            copyClass(rPath);
                        }
                    }
                }
            }
            
            if (realPath == null)
            {
                ERROR_PATHS.add(path);
                continue;
            }

            File src = new File(BuildUtil.ROOT + path);
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
            String path = f.substring(f.indexOf("/main6"));
            String realPath = f
                    .substring(f.indexOf("/src/") + "/src/".length());
            if (f.indexOf("jsp/") >= 0)
                realPath = f.substring(f.indexOf("jsp/") + "jsp/".length());

            File src = new File(BuildUtil.ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(NEW_WAR_PATH + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static void copyDeployFiles(Set<String> files)
    {
        for (String f : files)
        {
            String path = f.substring(f.indexOf("/main6"));
            String realPath = f.substring(f.indexOf("/default/deploy/")
                    + "/default/deploy/".length());

            File src = new File(BuildUtil.ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(DEPLOY_PATH + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private static void copySsoJars(Set<String> files)
    {
        for (String f : files)
        {
            String path = f.substring(f.indexOf("/main6"));
            String realPath = f.substring(f.indexOf("/tools/lib/sso/")
                        + "/tools/lib/sso/".length());

            File src = new File(BuildUtil.ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(NEW_EAR_PATH + "/lib/sso/" + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    private static void copyCommonJars(Set<String> files)
    {
        for (String f : files)
        {
            String path = f.substring(f.indexOf("/main6"));
            String realPath = f.substring(f.indexOf("/tools/lib/sso/")
                        + "/tools/lib/sso/".length());

            File src = new File(BuildUtil.ROOT + path);
            try
            {
                FileUtil.copyFile(src, new File(NEW_EAR_PATH + "/lib/sso/" + realPath));
                log("Add: " + realPath);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
//    private static void copyJmxConsoles(Set<String> files)
//    {
//        for (String f : files)
//        {
//            String path = f.substring(f.indexOf("/main6"));
//            String realPath = f.substring(f.indexOf("/jmx-console.war/WEB-INF/classes")
//                        + "/jmx-console.war/WEB-INF/classes".length());
//
//            File src = new File(BuildUtil.ROOT + path);
//            try
//            {
//                FileUtil.copyFile(src, new File(NEW_JMX_CONSOLE_CLASS + realPath));
//                log("Add: " + realPath);
//            }
//            catch (Exception e)
//            {
//                log.error(e.getMessage(), e);
//            }
//        }
//    }

    private static void copyInstallDataFiles(Set<String> files)
    {
        for (String f : files)
        {
            String path = f.substring(f.indexOf("/main6"));
            String realPath = null;
            if (f.indexOf("/envoy/schema/") > 0)
                realPath = f.substring(f.indexOf("/envoy/schema/")
                        + "/envoy/schema/".length());
            else if (f.indexOf("/install/data/") > 0)
                realPath = f.substring(f.indexOf("/install/data/")
                        + "/install/data/".length());
            else
            {
                log.error("Don't know how to copy " + f);
                throw new IllegalArgumentException("Don't know how to copy "
                        + f);
            }

            File src = new File(BuildUtil.ROOT + path);
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

    private static void print(Set<String> files, String message)
    {
        if (files.size() > 0)
        {
            log("");
            log(message);
            for (String f : files)
            {
                log.info(f);
                System.out.println(f);
            }
        }
    }

    private static void log(String msg)
    {
        System.out.println(msg);
        log.info(msg);
    }
}
