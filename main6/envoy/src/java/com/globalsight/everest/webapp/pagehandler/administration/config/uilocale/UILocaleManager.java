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

package com.globalsight.everest.webapp.pagehandler.administration.config.uilocale;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.globalsight.config.SystemParameter;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * A manager who manage system ui locale. The user can add/remove ui locale from
 * system.
 * 
 * The ui locale information stored in <tt>SYSTEM_PARAMETER</tt> table, and the
 * persistent object is <tt>UILocale</tt>.
 */
public class UILocaleManager
{
    private static final Logger s_logger = Logger
            .getLogger(UILocaleManager.class);

    private static String m_proFileRoot = null;
    private static String m_gsWarRoot = null;
    private static String m_gsWarReportPropertiesRoot = null;
    private static String m_native2ascii = null;
    private static String m_javahome = null;
    private static final int BUFSIZE = 4096;
    private static String m_defaultLocaleResourceFile = "/lib/classes/com/globalsight/resources/messages/LocaleResource.properties";

    /**
     * Get one system parameter
     * @param p_name
     * @return
     * @throws EnvoyServletException
     */
    public static SystemParameter getSystemParameter(String p_name)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getSystemParameterPersistenceManager()
                    .getSystemParameter(p_name);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(
                    GeneralExceptionConstants.EX_REMOTE, re);
        }
    }

    /**
     * Update the given system parameter
     * @param p_systemParameter
     * @return
     * @throws EnvoyServletException
     */
    public static SystemParameter updateSystemParameter(
            SystemParameter p_systemParameter) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getSystemParameterPersistenceManager()
                    .updateSystemParameter(p_systemParameter);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(
                    GeneralExceptionConstants.EX_REMOTE, re);
        }
    }

    /**
     * Get the ui locales of system
     * @return
     */
    public static List<String> getSystemUILocaleStrings()
    {
        List<String> strings = new ArrayList<String>();
        try
        {
            SystemParameter spUILocales = getSystemParameter(SystemConfigParamNames.UI_LOCALES);
            String locales = spUILocales.getValue();
            StringTokenizer tokenizer = new StringTokenizer(locales,
                    SystemConfiguration.DEFAULT_DELIMITER);
            int cnt = tokenizer.countTokens();
            if (cnt > 0)
            {
                for (int i = 0; i < cnt; i++)
                {
                    strings.add(tokenizer.nextToken());
                }
            }
        }
        catch (Exception e)
        {
            strings.clear();
        }

        return strings;
    }

    /**
     * Get the default ui locale (String) of system 
     * @return
     */
    public static String getSystemUILocaleDefaultString()
    {
        SystemParameter spUILocaleDefault = getSystemParameter(SystemConfigParamNames.DEFAULT_UI_LOCALE);
        String defaultLocale = spUILocaleDefault.getValue();
        return defaultLocale;
    }

    /**
     * Get all available locales
     * @return
     * @throws RemoteException
     */
    public static Vector getAvailableLocales() throws RemoteException
    {
        LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
        Vector sources = localeMgr.getAvailableLocales();
        return sources;
    }

    /**
     * Get the root path of properties files in ear lib
     * @return
     */
    public static String getPropertiesFilesRootEarLib()
    {
        if (null == m_proFileRoot)
        {
			try 
			{
				File defaultProFile = new File(m_defaultLocaleResourceFile);
				m_proFileRoot = defaultProFile.getParent().toString();
			} 
			catch (Exception e) 
			{
				s_logger.error("Error in getPropertiesFilesRoot", e);
			}
        }

        return m_proFileRoot;
    }
    
    /**
     * Get the root path of properties files in war report
     * @return
     */
    public static String getPropertiesFilesRootWarReport()
    {
        if (null == m_gsWarReportPropertiesRoot)
        {
            String warRoot = getGlobalSightWarRoot();
            String earLib = getPropertiesFilesRootEarLib();
            File proRoot = new File(warRoot, earLib);
            m_gsWarReportPropertiesRoot = proRoot.getPath();
        }
        
        return m_gsWarReportPropertiesRoot;
    }
    
    /**
     * Get the GlobalSight web root
     * @return
     */
    public static String getGlobalSightWarRoot()
    {
        if (null == m_gsWarRoot)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            String root = sc.getStringParameter(SystemConfiguration.WEB_SERVER_DOC_ROOT);
            File msgRootFile = new File(root);
            File earRoot = msgRootFile.getParentFile();
            m_gsWarRoot = earRoot.getPath();
        }
        
        return m_gsWarRoot;
    }

    /**
     * Get the usable properties files in war report for one locale
     * @param uilocale
     * @return
     */
    public static File[] getPropertiesFilesListOfAll(String uilocale)
    {
        File[] proFilesEarLib = getPropertiesFilesListEarLib(uilocale);
        File[] proFilesWarReport = getPropertiesFilesListWarReport(uilocale);

        int sizeOfEar = (proFilesEarLib == null) ? 0 : proFilesEarLib.length;
        int sizeOfWar = (proFilesWarReport == null) ? 0 : proFilesWarReport.length;
        
        File[] result = new File[sizeOfEar + sizeOfWar];

        for(int i = 0; i < sizeOfEar; i++)
        {
            result[i] = proFilesEarLib[i];
        }
        
        for(int i = 0; i < sizeOfWar; i++)
        {
            result[sizeOfEar + i] = proFilesWarReport[i];
        }
        
        return result;
    }
    
    /**
     * Get the usable properties files in ear lib for one locale
     * @param uilocale
     * @return
     */
    public static File[] getPropertiesFilesListEarLib(String uilocale)
    {
        File[] proFiles = new File[UILocaleConstant.PROPERTIES_FILE_NAMES_EARLIB.length];

        for (int i = 0; i < proFiles.length; i++)
        {
            String fname = UILocaleConstant.PROPERTIES_FILE_NAMES_EARLIB[i] + "_"
                    + uilocale + ".properties";
            proFiles[i] = new File(getPropertiesFilesRootWarReport(), fname);
        }

        return proFiles;
    }
    
    /**
     * Get the usable properties files in war report for one locale
     * @param uilocale
     * @return
     */
    public static File[] getPropertiesFilesListWarReport(String uilocale)
    {
        File[] proFiles = new File[UILocaleConstant.PROPERTIES_FILE_NAMES_WARREPORT.length];

        for (int i = 0; i < proFiles.length; i++)
        {
            String fname = UILocaleConstant.PROPERTIES_FILE_NAMES_WARREPORT[i] + "_"
                    + uilocale + ".properties";
            proFiles[i] = new File(getPropertiesFilesRootWarReport(), fname);
        }

        return proFiles;
    }
    
    public static String getPropertiesFileParentPath(String filename)
    {
        if (isInArray(UILocaleConstant.PROPERTIES_FILE_NAMES_EARLIB, filename))
        {
            return getPropertiesFilesRootWarReport();
        }
        
        if (isInArray(UILocaleConstant.PROPERTIES_FILE_NAMES_WARREPORT, filename))
        {
            return getPropertiesFilesRootWarReport();
        }
        
        return null;
    }

    private static boolean isInArray(String[] filenames,
            String filename)
    {
        if (filenames == null || filename == null)
        {
            return false;
        }
        
        for (String fn : filenames)
        {
            if (filename.startsWith(fn + "_"))
                return true;
        }
        
        return false;
    }

    /**
     * Get the default properties file path by the locale and its properties file path
     * @param proFile
     * @param uilocale
     * @return
     */
    public static File getPropertiesFileDefault(File proFile, String uilocale)
    {
        File parent = proFile.getParentFile();
        String toBeRemoved = "_" + uilocale;
        StringBuilder filename = new StringBuilder(proFile.getName());
        int indexOfLocale = filename.lastIndexOf(toBeRemoved);
        String defaultName = filename.delete(indexOfLocale,
                indexOfLocale + toBeRemoved.length()).toString();

        return new File(parent, defaultName);
    }
    
    /**
     * Get the jdk home, from JAVA_HOME first and then get it from System_Parameter
     * @return
     */
    public static String getJdkHome()
    {
    	if (m_javahome == null)
    	{
    	    File jvHome = new File(System.getProperty("java.home"));
    	    String jvName = jvHome.getName().toLowerCase();
    	    
    	    if (jvName.startsWith("jre"))
    	    {
    	        m_javahome = jvHome.getParent();
    	    }
    	    else
    	    {
    	        m_javahome = jvHome.getPath();
    	    }
    	}
    	
    	return m_javahome;
    }
    
    /**
     * Get the jdk home
     * @return
     */
    public static String getJdkNative2Ascii()
    {
        if (m_native2ascii == null || m_javahome == null)
        {
            File native2ascii = new File(getJdkHome(), "bin" + File.separatorChar + "native2ascii");
            m_native2ascii = native2ascii.getPath();
        }
    	return m_native2ascii;
    }

    /**
     * Writes out the specified file to the responses output stream.
     */
    public static void writeOutFile(File p_file, HttpServletResponse p_response)
            throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                p_file));
        OutputStream out = p_response.getOutputStream();

        byte[] buf = new byte[BUFSIZE];
        int readLen = 0;

        while ((readLen = in.read(buf, 0, BUFSIZE)) != -1)
        {
            out.write(buf, 0, readLen);
        }
        in.close();
        out.close();
    }

    /**
     * Copy images and other files for new locale
     * @param locale
     * @throws IOException 
     */
    public static void CopyOtherFilesForLocale(String locale) throws IOException
    {
        String warRoot = getGlobalSightWarRoot();
		String imgRoot = warRoot + File.separatorChar + "globalsight-web.war"
				+ File.separatorChar + "images";
        String enUSImgRoot = imgRoot + File.separatorChar + "en_US";
        String tgtLocaleImgRoot = imgRoot + File.separatorChar + locale;
        Stack<File> srcFiles = new Stack<File>();
        Stack<File> tgtFiles = new Stack<File>();
        
        srcFiles.add(new File(enUSImgRoot, "guide_logo_database.gif"));
        srcFiles.add(new File(enUSImgRoot, "guide_logo_fileSystem.gif"));
        
        tgtFiles.add(new File(tgtLocaleImgRoot, "guide_logo_database.gif"));
        tgtFiles.add(new File(tgtLocaleImgRoot, "guide_logo_fileSystem.gif"));
        
        CopyFiles(srcFiles, tgtFiles, false);
    }

    private static void CopyFiles(Stack<File> srcFiles, Stack<File> tgtFiles, boolean overwrite) throws IOException
    {
        if (srcFiles == null || tgtFiles == null)
        {
            return;
        }
        
        while(!srcFiles.empty() && !tgtFiles.empty())
        {
            File tgtFile = tgtFiles.pop();
            File srcFile = srcFiles.pop();
            
            if (!overwrite && tgtFile.exists())
            {
                continue;
            }
            
            com.globalsight.util.FileUtil.copyFile(srcFile, tgtFile);
        }
    }
}
