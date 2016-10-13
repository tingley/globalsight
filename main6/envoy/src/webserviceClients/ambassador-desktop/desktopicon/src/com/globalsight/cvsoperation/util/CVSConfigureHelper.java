package com.globalsight.cvsoperation.util;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cvsoperation.entity.Module;
import com.globalsight.cvsoperation.entity.Repository;
import com.globalsight.util.Constants;

/**
 * save CVS related configuration information to properties file
 * 
 * @author York.Jin
 * 
 */
public class CVSConfigureHelper
{

	static Logger log = Logger.getLogger(CVSConfigureHelper.class.getName());

	static Properties m_properties = new Properties();

	static long lasttime = 0l;

	static StringBuffer header;
	
	static int repositoryNum = 0;
	
	static List repositoryList = new ArrayList();
	
	static int maxRepositoryIndex = -1;
	
	static
	{
		header = new StringBuffer();
		header.append("####################################################");

		/*
         * Load properties and check file (cvs_configure.properties) exist or not. 
         * If configure file does not exist, then create it. If occur exceptions,
         * log them and exit application
         */
		checkProperties();
		
		//Set the repository number, List and max index.
		setRepositoryInfo();
		
		//Set 
	}

	public static String getProperty(String key)
	{
		checkProperties();
		String result = m_properties.getProperty(key);
		return (result == null) ? null : result.trim();
	}

	public static void setProperty(String key, String value)
	{
		m_properties.setProperty(key, value.trim());
		try
		{
			File file = new File(Constants.CVS_CONFIGURE_FILE);
			FileOutputStream out = new FileOutputStream(file);
			m_properties.store(out, header.toString());
			out.close();
			lasttime = file.lastModified();
		}
		catch (FileNotFoundException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
	}
	
	public static void removeProperty(String key)
	{
		m_properties.remove(key);
		try
		{
			File file = new File(Constants.CVS_CONFIGURE_FILE);
			FileOutputStream out = new FileOutputStream(file);
			m_properties.store(out, header.toString());
			out.close();
			lasttime = file.lastModified();
		}
		catch (FileNotFoundException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	public static int getRepositoryNum()
	{
		if ( getRepositoryList() != null )
		{
			return getRepositoryList().size();			
		}
		else
		{
			return 0;
		}

	}
	
	public static Object[] getRepositoryListForDisplay()
	{
		Object[] objs = null;
		List repList = CVSConfigureHelper.getRepositoryList();
		if ( repList != null && repList.size() > 0 ) {
			objs = new Object[repList.size()];
			for (int i=0; i<repList.size(); i++){
				Repository reps = (Repository) repList.get(i);
				objs[i] = reps.getCvsRoot() + " on '" + reps.getCvsServer() + "' " +
				"(in directory:" + reps.getCvsSandbox() + ")";
			}
		}
		
		return objs;
	}
	
	public static List getRepositoryList()
	{
		setRepositoryInfo();
		return repositoryList;
	}
	
	public static int getMaxRepositoryIndex()
	{
		setRepositoryInfo();
		return maxRepositoryIndex;
	}
	
	private static void setRepositoryInfo()
	{
		repositoryList.clear();
		maxRepositoryIndex = -1;

		for (int i=0; i<1000; i++ )
		{
			String cvs_repository = CVSConfigureHelper.getProperty(Constants.CVS_REPOSITORY + "_" + i);
			if ( cvs_repository != null && !"".equals(cvs_repository) )
			{
				try
				{
					Repository rep = getRepository(cvs_repository);
					repositoryList.add(rep);
					if ( maxRepositoryIndex < i )
					{
						maxRepositoryIndex = i;
					}
				}
				catch (Exception ex)
				{
					//do nothing
				}
			}
		}
	}
	
	private static Repository getRepository(String repositoryDes)
	{
		Repository rep = null;
		
		int index = 0;
		String cvs_server = null;
		int cvs_server_port = -1;
		String cvs_root = null;
		String cvs_local_bin = null;
		String cvs_sandbox = null;
		
		String[] reps = repositoryDes.split(";");
		for ( int i=0; i<reps.length; i++)
		{
			if ( i == 0 )//index
			{
				try {
					index = (new Integer(reps[i])).intValue();					
				} catch (Exception ex) {
					index = 99;
				}

			}
			if ( i == 1 )//cvs server
			{
				cvs_server = reps[i];
			}
			if ( i == 2 )//cvs server port
			{
				try {
					cvs_server_port = (new Integer(reps[i])).intValue();
				} catch (Exception ex) {
					cvs_server_port = -1;
				}
			}
			if ( i == 3 )//cvs root
			{
				cvs_root = reps[i];
			}
			if ( i == 4 )// local cvs bin path
			{
				cvs_local_bin = reps[i];
			}
			if ( i == 5 )// cvs sandbox
			{
				cvs_sandbox = reps[i];
			}
		}
		if ( cvs_server_port == -1)
		{
			rep = new Repository(index, cvs_server, cvs_root, cvs_local_bin, cvs_sandbox);			
		} else {
			rep = new Repository(index, cvs_server, cvs_server_port, cvs_root, cvs_local_bin, cvs_sandbox);
		}
		
		return rep;
	}
	
	private static void checkProperties()
	{
		File file = new File(Constants.CVS_CONFIGURE_FILE);
		long time = file.lastModified();
		if (lasttime < time)
		{
			try
			{
				FileInputStream in = new FileInputStream(file);
				m_properties.load(in);
				in.close();
				lasttime = time;
			}
			catch (Exception e)
			{
				log.error("error when loading " + Constants.CVS_CONFIGURE_FILE,	e);
				System.exit(1);
			}
		}
	}
	
	public static List getAllModules()
	{
		List allModuleList = new ArrayList();
		
		List allRepList = getRepositoryList();
		if ( allRepList != null && allRepList.size() > 0 )
		{
			for (int i=0; i< allRepList.size(); i++ )
			{
				int repIndex = ((Repository) allRepList.get(i)).getIndex();
				List tmpList = getModulesByRepositoryIndex(repIndex);
				if (tmpList != null && tmpList.size() > 0)
				{
					allModuleList.add(tmpList);
				}
			}			
		}
		
		return allModuleList;
	}
	/**
	 * Get 'Module' list by repository index
	 * @param repositoryIndex
	 * @return List <Module>
	 */
	public static List getModulesByRepositoryIndex(int repositoryIndex)
	{
		List moduleList = new ArrayList();
		
		List moduleNames = getModuleNames(repositoryIndex);
		if (moduleNames != null && moduleNames.size() > 0 )
		{
			for (int i=0; i<moduleNames.size(); i++)
			{
				Module module = new Module();
				//
				module.setRepositoryIndex(repositoryIndex);
				//
				String moduleName = (String) moduleNames.get(i);
				module.setModuleName(moduleName);
				//
				Vector modulePaths = getModulePath(repositoryIndex, moduleName);
				if (modulePaths != null && modulePaths.size() > 0 )
				{
					module.setModulePath(modulePaths);
				}
				//
				List moduleBTRs = getModuleBTR(repositoryIndex, moduleName);
				if ( moduleBTRs != null && moduleBTRs.size() > 0 )
				{
					module.setBranchTagRevision((String) moduleBTRs.get(0));
				}
				//
				List moduleProjects = getModuleProject(repositoryIndex, moduleName);
				if ( moduleProjects != null && moduleProjects.size() > 0 )
				{
					module.setProject((String) moduleProjects.get(0));
				}
				
				moduleList.add(module);
			}
		}
		
		return moduleList;
	}
	
	/**
	 * Get module names by repository index
	 * @param repositoryIndex
	 * @return List module names
	 */
	public static List<String> getModuleNames(int repositoryIndex)
	{
		List moduleNamesList = new ArrayList();
		String moduleNames = getProperty(Constants.CVS_MODULE_NAME);
		if ( moduleNames != null && !"".equals(moduleNames.trim()) ) 
		{
			String[] mns = moduleNames.split(";");
			for (int i=0; i<mns.length; i++)
			{
				String[] mn = mns[i].split("<->");
				if ( mn[0].equals(String.valueOf(repositoryIndex)) ) 
				{
					moduleNamesList.add(mn[1]);
				}
			}
		}
		
		return moduleNamesList;
	}
	
	/**
	 * remove a module
	 * @param module <Module>
	 */
	public static void removeModule(Module module)
	{
		if (module != null)
		{
			removeModuleName(module.getRepositoryIndex(), module.getModuleName());
			removeModulePath(module.getRepositoryIndex(), module.getModuleName());
			removeModuleBTR(module.getRepositoryIndex(), module.getModuleName());
			removeModuleProject(module.getRepositoryIndex(), module.getModuleName());
		}
	}
	
	private static void removeModuleName(int repIndex, String moduleName)
	{
		StringBuffer sb = new StringBuffer();
		
		String moduleNames = getProperty(Constants.CVS_MODULE_NAME);
		if ( moduleNames != null && !"".equals(moduleNames.trim()))
		{
			String[] mNames = moduleNames.split(";");
			for (int i=0; i<mNames.length; i++) 
			{
				String[] mn = mNames[i].split("<->");
				if ( !mn[0].equals(String.valueOf(repIndex)) ||
					 !mn[1].equals(moduleName) )
				{
					if ( sb.length()==0 )
					{
						sb.append(mNames[i]);
					}
					else
					{
						sb.append(";" + mNames[i]);
					}
				}
			}
		}

		CVSConfigureHelper.setProperty(Constants.CVS_MODULE_NAME, sb.toString());
	}
	
	/**
	 * Get module paths by repository index and module name
	 * @param repositoryIndex
	 * @param moduleName
	 * @return List module paths
	 */
	public static Vector<String> getModulePath(int repositoryIndex, String moduleName)
	{
		Vector modulePathVector = new Vector();
		
		String modulePath = getProperty(Constants.CVS_MODULE_PATH);
		if ( modulePath != null && !"".equals(modulePath.trim())) 
		{
			String[] modulePaths = modulePath.split(";");
			for (int i=0; i<modulePaths.length; i++) 
			{
				String[] mp = modulePaths[i].split("<->");
				
				if ( mp[0].equals(String.valueOf(repositoryIndex)) &&
					 mp[1].equals(moduleName) )
				{
					modulePathVector.add(mp[2]);
				}
			}
		}
		
		return modulePathVector;
	}
	
	public static void removeModulePath(int repositoryIndex, String moduleName)
	{
		StringBuffer sb = new StringBuffer();
		
		String modulePath = getProperty(Constants.CVS_MODULE_PATH);
		if ( modulePath != null && !"".equals(modulePath.trim()))
		{
			String[] modulePath2 = modulePath.split(";");
			for (int i=0; i<modulePath2.length; i++) 
			{
				String[] mp = modulePath2[i].split("<->");
				if ( !mp[0].equals(String.valueOf(repositoryIndex)) ||
					 !mp[1].equals(moduleName) )
				{
					if ( sb.length()==0 )
					{
						sb.append(modulePath2[i]);
					}
					else
					{
						sb.append(";" + modulePath2[i]);
					}
				}
			}
		}

		CVSConfigureHelper.setProperty(Constants.CVS_MODULE_PATH, sb.toString());
	}
	
	/**
	 * Get module branch|tag|revision name
	 * @param repositoryIndex
	 * @param moduleName
	 * @return List
	 */
	public static List<String> getModuleBTR(int repositoryIndex, String moduleName)
	{
		List moduleBTRList = new ArrayList();
		
		String moduleBTR = getProperty(Constants.CVS_BRANCH_TAG_REVISION);
		if ( moduleBTR != null && !"".equals(moduleBTR.trim())) 
		{
			String[] moduleBTRs = moduleBTR.split(";");
			for (int i=0; i<moduleBTRs.length; i++) 
			{
				String[] mbtr = moduleBTRs[i].split("<->");
				
				if ( mbtr[0].equals(String.valueOf(repositoryIndex)) &&
						mbtr[1].equals(moduleName) )
				{
					moduleBTRList.add(mbtr[2]);
				}
			}
		}
		
		return moduleBTRList;
	}
	
	public static void removeModuleBTR(int repositoryIndex, String moduleName)
	{
		StringBuffer sb = new StringBuffer();
		String moduleBTR = getProperty(Constants.CVS_BRANCH_TAG_REVISION);
		
		if ( moduleBTR != null && !"".equals(moduleBTR.trim())) 
		{
			String[] moduleBTRs = moduleBTR.split(";");
			for (int i=0; i<moduleBTRs.length; i++) 
			{
				String[] mbtr = moduleBTRs[i].split("<->");
				
				if ( !mbtr[0].equals(String.valueOf(repositoryIndex)) ||
					 !mbtr[1].equals(moduleName) )
				{
					if (sb.length()==0)
					{
						sb.append(moduleBTRs[i]);
					}
					else
					{
						sb.append(";" + moduleBTRs[i]);
					}
				}
			}
		}
		
		setProperty(Constants.CVS_BRANCH_TAG_REVISION, sb.toString());
	}
	
	/**
	 * Get all module project names
	 * @return List
	 */
	public static List<String> getAllModuleProjects()
	{
		List moduleProjectsList = new ArrayList();
		
		String moduleProject = getProperty(Constants.CVS_PROJECT);
		if ( moduleProject != null && !"".equals(moduleProject.trim())) 
		{
			String[] moduleProjects = moduleProject.split(";");
			for (int i=0; i<moduleProjects.length; i++) 
			{
				String[] mpro = moduleProjects[i].split("<->");

				if ( !moduleProjectsList.contains(mpro[2]) )
				{
					moduleProjectsList.add(mpro[2]);
				}
			}
		}
		
		return moduleProjectsList;
	}
	
	/**
	 * Get project names by repository index and module name
	 * Commonly, the returned list only contains ONE project name
	 * @param repositoryIndex
	 * @param moduleName
	 * @return List
	 */
	public static List<String> getModuleProject(int repositoryIndex, String moduleName)
	{
		List moduleProjectsList = new ArrayList();
		
		String moduleProject = getProperty(Constants.CVS_PROJECT);
		if ( moduleProject != null && !"".equals(moduleProject.trim())) 
		{
			String[] moduleProjects = moduleProject.split(";");
			for (int i=0; i<moduleProjects.length; i++) 
			{
				String[] mpro = moduleProjects[i].split("<->");
				
				if ( mpro[0].equals(String.valueOf(repositoryIndex)) 
					 &&	mpro[1].equals(moduleName) 
					 && !moduleProjectsList.contains(mpro[2]) )
				{
					moduleProjectsList.add(mpro[2]);
				}
			}
		}
		
		return moduleProjectsList;
	}
	
	public static void removeModuleProject(int repositoryIndex, String moduleName)
	{
		StringBuffer sb = new StringBuffer();
		String moduleProjects = getProperty(Constants.CVS_PROJECT);
		
		if ( moduleProjects != null && !"".equals(moduleProjects.trim())) 
		{
			String[] modulePros = moduleProjects.split(";");
			for (int i=0; i<modulePros.length; i++) 
			{
				String[] mpros = modulePros[i].split("<->");
				
				if ( !mpros[0].equals(String.valueOf(repositoryIndex)) ||
					 !mpros[1].equals(moduleName) )
				{
					if (sb.length()==0)
					{
						sb.append(modulePros[i]);
					}
					else
					{
						sb.append(";" + modulePros[i]);
					}
				}
			}
		}
		
		setProperty(Constants.CVS_PROJECT, sb.toString());
	}
	
	private static void logError(String p_msg)
	{
		log.error("Can't " + p_msg + " to cvs_configure.properties.");
	}
}
