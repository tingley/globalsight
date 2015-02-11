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
package com.install;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.config.properties.EnvoyConfig;
import com.config.properties.Resource;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.Assert;
import com.util.CmdUtil;
import com.util.FileUtil;
import com.util.PatchUtil;
import com.util.ServerUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

/**
 * The main class that manage update a patch to server.
 * <p>
 * 
 * You can get more information from doUpdate() method.
 * 
 * @see #doUpdate()
 */
public class Update
{
    private static Logger log = Logger.getLogger(Update.class);
    private static final String EAR_PATH = "/jboss/server/standalone/deployments/globalsight.ear";
    private static final String LIB_PATH = EAR_PATH + "/lib";
    private static final String GLOBLASIGHT_JAR = EAR_PATH + "/globalsight.jar";
    private static final String CLASS_PATH = LIB_PATH + "/classes";
//    private static final String PATCH_SQL_DIR = "/install/data/ERs";
    private static final String BACKUP_FILE = "backup";
    private static final String VALIEDATE = "validate";
    private static final String STATISTIC = "statistic";
    private static final String BACKUP = "backup";
    private static final String COPY = "copy";
    private static final String UPDATE_JAR = "jar";
    private static final String DATABASE = "database";

    private static Map<String, Integer> RATES = new HashMap<String, Integer>();
    static
    {
        RATES.put(VALIEDATE, 100000);
        RATES.put(STATISTIC, 100000);
        RATES.put(BACKUP, 200000);
        RATES.put(COPY, 100000);
        RATES.put(UPDATE_JAR, 100000);
        RATES.put(DATABASE, 200000);
    }

    private List<File> filesNeedCheck = new ArrayList<File>();
    private List<File> allFiles = new ArrayList<File>();
    private List<String> ignoreFiles = new ArrayList<String>();

    private UI ui = UIFactory.getUI();
    private PatchUtil updateUtil = new PatchUtil();
    
    /**
     * Init ignoreFiles.
     */
    public Update()
    {
        ignoreFiles.add("system.xml");
    }

    /**
     * Backups original files to backup fold.
     * <p>
     * Note: if backup fold has exist, will not backup again.
     * 
     * @throws Exception
     */
    public void backup() throws Exception
    {
        log.info("Backuping");

        String serverName = ServerUtil.getServerName();
        String backupName = serverName + "(" + ServerUtil.getVersion() + ")";
        File root = new File(BACKUP_FILE + File.separator + backupName);
        String rootPath = root.getCanonicalPath().replace("\\", "/");
        if (root.exists())
        {
            log.info("The folder (" + rootPath + ") already exist");
            if (!ui.confirmRewrite(rootPath))
            {
                ui.addProgress(RATES.get(BACKUP), "");
                return;
            }
        }

        File jar = new File(updateUtil.getPath() + GLOBLASIGHT_JAR);
        allFiles.add(jar);

        int size = allFiles.size();
        log.info("File size: " + size);
        int processTotle = getProgress(BACKUP);
        if (size == 0)
        {
            ui.addProgress(processTotle, "");
            return;
        }

        int rate = processTotle / size;
        int lose = processTotle - rate * size;

        for (int i = 0; i < size; i++)
        {
            File f = allFiles.get(i);
            ui.addProgress(0, MessageFormat.format(Resource
                    .get("process.backup"), f.getName(), i + 1, size));

            if (EnvoyConfig.needCopyToAllCompany(f.getName()))
            {
                for (File companyFile : getCompanyFiles(f))
                {
                    backupFile(companyFile, backupName);
                }
            }
            else
            {
                backupFile(f, backupName);
            }

            ui.addProgress(rate, MessageFormat.format(Resource
                    .get("process.backup"), f.getName(), i + 1, size));
        }
        allFiles.remove(jar);

        ui.addProgress(lose, "");
        log.info("Backuping finished");
    }

    private void backupFile(File file, String rootName) throws Exception
    {
        String path = file.getPath().replace("\\", "/");
        path = path.replace(updateUtil.getPath().replace("\\", "/"), "");
        path = path.replace(ServerUtil.getPath().replace("\\", "/"), "");
        File sf = new File(ServerUtil.getPath() + path);
        if (sf.exists())
        {
            File bf = new File(BACKUP_FILE + File.separator + rootName + path);
            FileUtil.copyFile(sf, bf);
            log.debug(path + " has been backuped");
        }
        else if (!sf.getName().endsWith(".class"))
        {
            log.debug(sf.getPath() + " does not exist");
        }
    }

    /**
     * Copies files from patch to server.
     * <p>
     * Note: if a file will not copy to server if the name has been included in
     * <code>ignoreFiles</<code>, 
     * @throws Exception 
     * 
     * @throws Exception A exception will be throw out if copy file failed.
     */
    public void copy() throws Exception
    {
        log.info("Start copying files");

        List<File> copyFiles = new ArrayList<File>();
        for (File file : allFiles)
        {
            if (!file.getName().endsWith(".class"))
            {
                copyFiles.add(file);
            }
        }

        int size = copyFiles.size();
        log.info("File size: " + size);

        int processTotle = getProgress(COPY);
        if (size == 0)
        {
            ui.addProgress(processTotle, "");
            return;
        }
        int rate = processTotle / size;
        int lose = processTotle - rate * size;

        for (int i = 0; i < size; i++)
        {
            File f = copyFiles.get(i);
            ui.addProgress(0, MessageFormat.format(
                    Resource.get("process.copy"), f.getName(), i + 1, size));
            copyFile(f);
            ui.addProgress(rate, MessageFormat.format(Resource
                    .get("process.copy"), f.getName(), i + 1, size));
        }

        ui.addProgress(lose, "");
        log.info("Copying files finished");
    }
    
    private void copyFile(File file) throws Exception
    {
        String path = file.getCanonicalPath().replace("\\", "/");
        
        path = path.replace(updateUtil.getPath().replace("\\", "/"), "");
        File targetFile = new File(ServerUtil.getPath(), path);
            
        FileUtil.copyFile(file, targetFile);
        log.debug(targetFile.getPath() + " has been updated");
    }

    private List<File> getCompanyFiles(File file)
    {
        List<File> files = new ArrayList<File>();
        String name = file.getName();
        if (EnvoyConfig.needCopyToAllCompany(name))
        {
            String path = ServerUtil.getPath() + EnvoyConfig.RESOURCE_PARENT
                    + "/" + name;
            files.add(new File(path));
            for (String company : EnvoyConfig.getCompanyNames())
            {
                String realPath = ServerUtil.getPath()
                        + EnvoyConfig.RESOURCE_PARENT + "/" + company + "/"
                        + name;
                files.add(new File(realPath));
            }
        }

        return files;
    }

    private void updateJar() throws Exception
    {
        log.info("Updating jar");
        ui.addProgress(0, "Updating jar");
        if (ServerUtil.isInLinux())
        {
            String[] cmd =
            { "sh", "script/linux/updateJar.sh", ServerUtil.getPath(),
                    updateUtil.getPath() + CLASS_PATH };
            CmdUtil.run(cmd, false);
        }
        else
        {
            String[] cmd =
            { "cmd", "/c", "jar", "uvf",
                    ServerUtil.getPath() + GLOBLASIGHT_JAR, "-C",
                    updateUtil.getPath() + CLASS_PATH, "com" };
            CmdUtil.run(cmd);
        }
        ui.addProgress(getProgress(UPDATE_JAR), "Updating jar");
        log.info("Updating jar finished");
    }

    /**
     * Update a patch to server.
     */
    public void doUpdate()
    {
        log.info("\n\n == Start updating ==================================\n");
        try
        {
            ui.showWelcomePage();
            validate();
            updateUtil.removeHotfix();
            ListAllFiles();
            backup();
            copy();
            updateJar();
            updateUtil.parseAllTemplates();
            updateUtil.updateAxis2();
            updateUtil.upgradeVerion(getProgress(DATABASE));
            updateUtil.saveSystemInfo();
            DbUtil util = DbUtilFactory.getDbUtil();
            util.closeExistConn();
            
//            ui.finish();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement msg : trace)
            {
                log.error("\tat " + msg);
            }

            ui.error(e.getMessage());
        }
    }

    private Integer getProgress(String key)
    {
        Assert.assertNotNull(key, "Press key");

        return RATES.get(key);
    }

    private boolean isIgnoreFile(File f)
    {
        return ignoreFiles.contains(f.getName());
    }

    /**
     * Lists all files included in patch, and find sql files.
     */
    public void ListAllFiles()
    {
        log.info("Listing all files in patch");
        ui.addProgress(0, Resource.get("process.count"));

        filesNeedCheck.add(new File(updateUtil.getPath()));
        allFiles = new ArrayList<File>();

        while (!filesNeedCheck.isEmpty())
        {
            File f = filesNeedCheck.remove(0);
            for (File cf : f.listFiles())
            {
                if (isIgnoreFile(cf))
                {
                    continue;
                }

                if (cf.isDirectory())
                {
                    filesNeedCheck.add(cf);
                }
                else
                {
                    allFiles.add(cf);
                }
            }
        }

        log.debug("All files:");
        for (File f : allFiles)
        {
            log.debug(f.getPath());
        }

        ui.addProgress(getProgress(STATISTIC), Resource.get("process.count"));
        log.info("Listing file finished");
    }

    /**
     * Does some simple validations before do update.
     * <p>
     * 1. version.<br>
     * 2. datebase connection.
     * 
     * @throws Exception
     *             A exception will be throw out if validation failed.
     */
    private void validate() throws Exception
    {
        int rate = getProgress(VALIEDATE);
        log.debug("Checking database connection");

        try
        {
            ui.addProgress(0, Resource.get("process.validateVersion"));
            updateUtil.validateVersion();
            ui.addProgress(rate / 2, Resource.get("process.validateVersion"));
        }
        catch (Exception e)
        {
            ui.infoError(e.getMessage());
        }

        ui.addProgress(0, Resource.get("process.validateDatabase"));
        DbUtilFactory.getDbUtil().testConnection();
        ui.addProgress(rate - rate / 2, Resource
                .get("process.validateDatabase"));
    }
}
