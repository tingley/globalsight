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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.config.properties.InstallValues;
import com.config.properties.Resource;
import com.config.xml.model.SystemInfo;
import com.plug.Plug;
import com.plug.PlugManager;
import com.plug.PrePlug;
import com.ui.UI;
import com.ui.UIFactory;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

public abstract class InstallUtil
{
    private static Logger log = Logger.getLogger(InstallUtil.class);
    protected static final String VERSION_SPLIT = "\\.";
    protected static final boolean REINSTALL_ALLOW = false;
    protected static final String ROOT = "data";
    private static final String PATCH_SQL_DIR = "/install/data/ERs";

    protected static SystemInfo SYSTEM_INFO;
    private static String version = null;
    private static String previousVersion = null;

    private List<String> plugClass = null;
    private List<File> sqlFile = null;
    DbUtil util = DbUtilFactory.getDbUtil();
    private UI ui = UIFactory.getUI();
    
    private static final String BACKSLASH = "\\";
    private static final String DOUBLEBACKSLASH = "\\\\";
    private static final String FORWARDSLASH = "/";
    private static final String DOUBLEBACKSLASH_REG = "\\\\\\\\";
    private static final String DOUBLEBACKSLASH_REP = "\\\\\\\\\\\\\\\\";

    /**
     * Load <code>systemInfo</code> from the system.xml in patch.
     * 
     * @see com.config.xml.model.SystemInfo.
     * @see com.util.XmlUtil#load(Class, String)
     * @return The loaded <code>systemInfo</code>.
     */
    public SystemInfo getSystemInfo()
    {
        if (SYSTEM_INFO == null)
        {
            String path = getPath() + File.separator + SystemInfo.XML_PATH;
            Assert.assertFileExist(path);
            SYSTEM_INFO = XmlUtil.load(SystemInfo.class, path);
        }

        return SYSTEM_INFO;
    }

    /**
     * Save <code>system.xml</code> to server.
     * <p>
     * It means the upgrade has been finished successful.
     * 
     * @see com.config.xml.model.SystemInfo
     */
    public void saveSystemInfo()
    {
        String path = ServerUtil.getPath() + File.separator
                + SystemInfo.XML_PATH;
        XmlUtil.save(getSystemInfo(), path);
    }

    public void updateAxis2() throws IOException
    {
    	boolean enableSSL = "true".equalsIgnoreCase(InstallValues.get("server_ssl_enable"));
    	
    	String axis2config = "axis2.http.xml";
        if (enableSSL)
        {
            axis2config = "axis2.https.xml";
        }

        String root = ServerUtil.getPath() + "/jboss/server/standalone/deployments/globalsight.ear/globalsightServices.war/WEB-INF/conf";
        File src = new File(root, axis2config);
        File dst = new File(root, "axis2.xml");

        FileUtil.copyFile(src, dst);
    }
    
    public abstract String getPath();

    protected String getRequestedVersion()
    {
        if (previousVersion == null)
        {
            previousVersion = getSystemInfo().getPreviousVersion();
            log.info("Oldest version supported: " + previousVersion);
        }

        return previousVersion;
    }

    public String getVersion()
    {
        if (version == null)
        {
            version = getSystemInfo().getVersion();
            log.info("Patch version: " + version);
        }
        
        return version;
    }
    
	private String getRealVersion(String version)
	{
		while (version.endsWith(".0"))
			version = version.substring(0, version.length() - 2);
		return version;
	}

    public int compare(String version1, String version2)
    {
		version1 = getRealVersion(version1);
		version2 = getRealVersion(version2);
		
        String[] v1 = version1.split(VERSION_SPLIT);
        String[] v2 = version2.split(VERSION_SPLIT);

        for (int i = 0; i < v1.length; i++)
        {
            if (v2.length <= i)
            {
                return 1;
            }

            if (Integer.parseInt(v1[i]) == Integer.parseInt(v2[i]))
            {
                continue;
            }

            return Integer.parseInt(v1[i]) - Integer.parseInt(v2[i]);
        }

        return v1.length - v2.length;
    }
    
    private int compareVersion(String f1, String f2)
    {
        return compare(f1, f2);
    }

    private Comparator<String> getPlugComparator()
    {
        return new Comparator<String>()
        {
            @Override
            public int compare(String f1, String f2)
            {
                return compareVersion(f1, f1);
            }
        };
    }

    private List<String> getPlugClasses()
    {
        if (plugClass == null)
        {
            List<String> versions = new ArrayList<String>();
            plugClass = new ArrayList<String>();
            versions.addAll(PlugManager.plugClasses);
            Collections.sort(versions, getPlugComparator());
            
            String version = ServerUtil.getVersion();
            for (String plug : versions)
            {
                if (compare(plug, version) > 0)
                {
                    plugClass.add(plug);
                }
            }
        }

        return plugClass;
    }

    private List<File> getSqlFiles()
    {
        if (sqlFile == null)
        {
            sqlFile = new ArrayList<File>();
            sqlFile.addAll(FileUtil.getPatchSqlFiles(new File(getPath()
                    + PATCH_SQL_DIR)));
        }
        return sqlFile;
    }
    
    public void runPrePlug() throws Exception
    {
    	List<String> PlugClasses = getPlugClasses();
    	while (PlugClasses.size() > 0)
    	{
    		runPrePlugClass(PlugClasses);
    	}
    	
    	plugClass = null;
    }

    public void upgradeVerion(int totalProgress) throws Exception
    {
        log.info("Updating database");
        List<File> sqlFiles = getSqlFiles();
        List<String> PlugClasses = getPlugClasses();
        
        ui.addProgress(0, Resource.get("process.database"));

        int size = sqlFiles.size();
        
        int rate = 0;
        int lost = totalProgress;
        
        if (size == 0)
        {
            ui.addProgress(totalProgress, "");
        }
        else
        {
            rate = totalProgress / size;
            lost = totalProgress - rate * size;
        }
        
        while (sqlFiles.size() > 0 || PlugClasses.size() > 0)
        {
            String sqlVersion = null;
            String plugVersion = null;

            if (sqlFiles.size() > 0)
            {
                sqlVersion = FileUtil.getSqlVersion(sqlFiles.get(0));
            }

            if (PlugClasses.size() > 0)
            {
                plugVersion = PlugClasses.get(0);
            }

            if (sqlVersion != null && plugVersion != null)
            {
                if (compare(sqlVersion, plugVersion) > 0)
                {
                    runPlugClass(PlugClasses);
                }
                else
                {
                    updateDb(sqlFiles, rate);
                }
            }
            else if (sqlVersion == null)
            {
                while (PlugClasses.size() > 0)
                {
                    runPlugClass(PlugClasses);
                }
            }
            else if (plugVersion == null)
            {
                while (sqlFiles.size() > 0)
                {
                    updateDb(sqlFiles, rate);
                }
            }
        }
        
        ui.addProgress(lost, "");
        log.info("Updating database finished");
    }

    private void runPlugClass(List<String> PlugClasses) throws Exception
    {
        String name = "Plug_" + PlugClasses.get(0).replace(".", "_");
        Plug plug = null;
        try
        {
            plug = (Plug) Class.forName("com.plug." + name).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            // Ignore it.
        }

        if (plug != null)
        {
            plug.run();
        }

        PlugClasses.remove(0);
    }
    
    private void runPrePlugClass(List<String> PlugClasses) throws Exception
    {
        String name = "Plug_" + PlugClasses.get(0).replace(".", "_");
        Plug plug = null;
        try
        {
            plug = (Plug) Class.forName("com.plug." + name).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            // Ignore it.
        }

        if (plug != null && plug instanceof PrePlug)
        {
        	PrePlug prePlug = (PrePlug) plug;
        	prePlug.preRun();
        }

        PlugClasses.remove(0);
    }

    private void updateDb(List<File> sqlFiles, int rate) throws IOException
    {
        String version = null;
        while (sqlFiles.size() > 0)
        {
            File file = sqlFiles.get(0);

            String sqlVersion = FileUtil.getSqlVersion(sqlFiles.get(0));
            if (version == null)
            {
                version = sqlVersion;
            }

            if (sqlVersion.equals(version))
            {
                try
                {
                    ui.addProgress(0, MessageFormat.format(Resource.get("process.sql"),
                            file.getName()));
                    
                    util.execSqlFile(file);
                    log.info("   " + file.getName() + " : successful");
                    
                    ui.addProgress(rate, MessageFormat.format(Resource
                            .get("process.sql"), file.getName()));
                }
                catch (Exception e)
                {
                    log.info("   " + file.getName() + " : failed");
                    log.debug(e);
                    ui.confirmContinue(MessageFormat.format(Resource
                            .get("msg.executeSql"), file.getCanonicalPath(), e
                            .getMessage()));
                }

                sqlFiles.remove(0);
            }
            else
            {
                break;
            }
        }
    }
    
    private Properties getDefaultProperties() throws IOException
    {
    	String os = System.getProperty("os.name");
    	
		String jar = ServerUtil.getPath() + "/install/installer.jar";
		String fileName="data/installDefaultValues.properties";
		
		if (os.startsWith("Linux"))
		{
			fileName="data/installDefaultValuesNoUI.properties";
		}
		
        JarFile jarFile = new JarFile(jar);
        JarEntry entry = jarFile.getJarEntry(fileName);       
        InputStream input = jarFile.getInputStream(entry);
     
        Properties p = new Properties();
        p.load(input);
     
        jarFile.close();
        return p;
    }
    
    private Properties getProperties()
    {        
        Properties properties = new Properties();
		try 
		{
			properties = getDefaultProperties();
		} 
		catch (IOException e) 
		{
			log.error(e.getMessage(), e);
		}
		
		properties.putAll(InstallValues.getProperties());

        properties.put("Jboss_JNDI_prefix", "topic/");
        properties.put("ldap_user_password", encodeMD5(properties.getProperty("ldap_password")));
        properties.put("super_admin_password", encodeMD5(properties.getProperty("system4_admin_password")));
        
        String pop3Server = "";
        String mailAddress = properties.getProperty("admin_email");
        if (mailAddress != null && mailAddress.contains("@"))
            pop3Server = "pop3."
                    + mailAddress.substring(mailAddress.indexOf("@") + 1);
        else
            pop3Server = properties.getProperty("mailserver");
        
        properties.put("mailserver_pop3", pop3Server);

        boolean useSSLMail = "true"
                .equalsIgnoreCase(properties.getProperty("mailserver_use_ssl"));
        if (useSSLMail)
            properties.put("mail_transport_protocol", "smtps");
        else
            properties.put("mail_transport_protocol", "smtp");
        
        boolean enableSSL = "true".equalsIgnoreCase(properties.getProperty("server_ssl_enable", "false"));
        if (enableSSL)
        {
            properties.setProperty("ssl_comments_end", "-->");
            properties.setProperty("ssl_comments_start", "<!--");
            String kspwd = properties.getProperty("server_ssl_ks_pwd");
            if (kspwd == null || "".equals(kspwd.trim()))
                properties.setProperty("server_ssl_ks_pwd", "changeit");
        }
        else
        {
            properties.setProperty("ssl_comments_end", "");
            properties.setProperty("ssl_comments_start", "");
            properties.setProperty("server_ssl_port", "443");
            properties.setProperty("server_ssl_ks_pwd", "changeit");
        }
        
        boolean enableEmailServer = "true".equalsIgnoreCase(properties.getProperty("system_notification_enabled", "false"));
        properties.setProperty("mail_smtp_start", enableEmailServer ? "" : "<!--");
        properties.setProperty("mail_smtp_end", enableEmailServer ? "" : "-->");
        
        boolean enableEmailAuthentication = "true".equalsIgnoreCase(properties.getProperty("email_authentication_enabled", "false"));
        if (!enableEmailServer)
        {
        	enableEmailAuthentication = true;
        }
        properties.setProperty("mail_authentication_start", enableEmailAuthentication ? "" : "<!--");
        properties.setProperty("mail_authentication_end", enableEmailAuthentication ? "" : "-->");
        
        return properties;
    }
    
    private static String encodeMD5(String msg)
    {
        try
        {
            byte[] md5Msg = MessageDigest.getInstance("MD5").digest(
                    msg.getBytes());
            return "{MD5}" + new String(new Base64().encode(md5Msg));
        }
        catch (NoSuchAlgorithmException e)
        {
            System.err.println("Can not find the MD5 ALGORITHM.");
            return null;
        }
    }
    
    public void parseTemplates(List<File> files)
    {
    	InstallValues.addAdditionalInstallValues();
    	
        URL url = InstallUtil.class.getClassLoader().getResource("generateFiles.properties");
        Properties installValues = getProperties();
        for (File file : files)
        {
            String path = file.getPath();
            path = path.replace("\\", "/");
            path = path.replace(ServerUtil.getPath().replace("\\", "/") + "/", "");
            
            String trg = PropertyUtil.get(url, path);
            if (trg == null)
            {
                log.error("Can not find " + path + " in generateFiles.properties");
                continue;
            }
            
            processFile(file, new File(ServerUtil.getPath() + "/" + trg), installValues);
        }
    }
    
    public void removeHotfix()
    {
    	String path = ServerUtil.getPath() + "/hotfix";
    	File f = new File(path);
    	if (f.exists())
    	{
    		FileUtil.deleteFile(new File(path));
    	}
    }
    
    /**
     * @throws IOException
     */
    public void parseAllTemplates() throws IOException
    {
    	InstallValues.addAdditionalInstallValues();
    	
        URL url = InstallUtil.class.getClassLoader().getResource("generateFiles.properties");
        Properties properties = new Properties();
        InputStream in = url.openStream();
        properties.load(in);
        Properties installValues = getProperties();
        
        for (Object ob : properties.keySet())
        {
        	String path = (String) ob;
        	File f1 = new File(ServerUtil.getPath() + "/" + path);
        	File f2 = new File(ServerUtil.getPath() + "/" + properties.getProperty(path));
        	
        	try 
        	{
        		processFile(f1, f2, installValues);
			} 
        	catch (Exception e) 
        	{
        		log.error("Failed to parse " + f1.getPath() + " to " + f2.getPath());
				log.error(e);
				continue;
			}
        }
    }
    
    private String replacePathSlash(String str)
    {
        if (str.startsWith("{MD5}"))
            return str;
        
        if (str.startsWith(BACKSLASH))
        {
            return (DOUBLEBACKSLASH + str.substring(2).replaceAll(
                    DOUBLEBACKSLASH, FORWARDSLASH)).replaceAll(
                    DOUBLEBACKSLASH_REG, DOUBLEBACKSLASH_REP);
        }
        else
        {
            return str.replaceAll(DOUBLEBACKSLASH, FORWARDSLASH);
        }
    }
    
    private void processFile(File template, File destFile,
            Properties properties)
    {
        template.getParentFile().mkdirs();

        String content = FileUtil.readFile(template);
        for (Object k : properties.keySet())
        {
            String key = (String) k;
            String value = properties.getProperty(key);
            value = replacePathSlash(value);
            content = content.replace("%%" + key + "%%", value);
        }

        try
        {
            FileUtil.writeFile(destFile, content);
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
            UI ui = UIFactory.getUI();
            ui.confirmContinue("Failed to generate mail-service.xml.");
            return;
        }

        log.info("Success to generate " + template.getName());
    }
}
