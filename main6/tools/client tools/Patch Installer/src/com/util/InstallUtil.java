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
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.config.properties.InstallValues;
//import org.apache.commons.codec.binary.Base64;
import com.demo.Hotfix;


public class InstallUtil
{
	private static Logger log = Logger.getLogger(InstallUtil.class);
	
    protected static final String VERSION_SPLIT = "\\.";
    protected static final boolean REINSTALL_ALLOW = false;
    protected static final String ROOT = "data";
    
    private static final String BACKSLASH = "\\";
    private static final String DOUBLEBACKSLASH = "\\\\";
    private static final String FORWARDSLASH = "/";
    private static final String DOUBLEBACKSLASH_REG = "\\\\\\\\";
    private static final String DOUBLEBACKSLASH_REP = "\\\\\\\\\\\\\\\\";

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
    
    private Properties getProperties()
    {        
        Properties properties = new Properties();
		try 
		{
			properties = getDefaultProperties();
		} 
		catch (IOException e) 
		{
			log.error(e);
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
            log.error(e);
            return;
        }

        log.info("Success to generate " + template.getName());
    }
}
