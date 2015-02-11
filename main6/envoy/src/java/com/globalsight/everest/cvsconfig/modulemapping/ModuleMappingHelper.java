package com.globalsight.everest.cvsconfig.modulemapping;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.cvsconfig.CVSUtil;
import java.util.*;
import java.sql.*;
import java.io.*;


public class ModuleMappingHelper {
	private static final Logger logger =
        Logger.getLogger(ModuleMappingHelper.class.getName());

	/**
	 * Get all subfolders under the special path
	 * @param p_path
	 * @return
	 */
	public ArrayList<File> getAllSubFolders(File p_path) {
		ArrayList<File> subFolders = new ArrayList<File>();
		if (p_path == null || !p_path.isDirectory())
			return subFolders;
		File[] lists = null;
		try {
			lists = p_path.listFiles();
			for (File file : lists) {
				if (file.isDirectory()) {
					subFolders.add(file);
					subFolders.addAll(getAllSubFolders(file));
				}
			}
			return subFolders;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return subFolders;
		}
	}
	
	/**
	 * Create target modules according to source module folder structure
	 * @param p_mm
	 * @return
	 */
	public ArrayList<ModuleMapping> createSubModuleMapping(ModuleMapping p_mm) {
		ArrayList<ModuleMapping> mms = new ArrayList<ModuleMapping>();
		if (p_mm == null)
			return mms;
		if ("".equals(p_mm.getSourceModule()) || "".equals(p_mm.getTargetModule()))
			return mms;
		mms.add(p_mm);
		try {
			String basePath = CVSUtil.getBaseDocRoot();
			int srcPathLength = 0, tarPathLength = 0;
			String sourceModule = basePath.concat(p_mm.getSourceModule());
			srcPathLength = sourceModule.length();
			String targetModule = basePath.concat(p_mm.getTargetModule());
			tarPathLength = targetModule.length();
			File source = null, target = null;
			source = new File(sourceModule);
			target = new File(targetModule);
			if (!source.isDirectory() || !target.isDirectory()) {
				logger.error("Source module[" + sourceModule + "] or target module[" + targetModule + "] is NOT folder.");
				return mms;
			}
			ArrayList<File> subs = getAllSubFolders(source);
			String tmp = "";
			File tmpFolder = null;
			ModuleMapping mm = null;
			for (File sub : subs) {
				tmp = sub.getAbsolutePath().substring(srcPathLength);
				tmp = targetModule.concat(tmp);
				tmpFolder = new File(tmp);
				if (tmpFolder.exists() || tmpFolder.isFile())
					continue;
				tmpFolder.mkdirs();
				
				mm = new ModuleMapping();
				//mm.setCompanyId(p_mm.getCompanyId());
				mm.setSourceLocale(p_mm.getSourceLocale());
				mm.setSourceLocaleLong(p_mm.getSourceLocaleLong());
				mm.setSourceModule(sub.getAbsolutePath().substring(basePath.length()));
				mm.setTargetLocale(p_mm.getTargetLocale());
				mm.setTargetLocaleLong(p_mm.getTargetLocaleLong());
				mm.setTargetModule(tmp.substring(basePath.length()));
				mm.setModuleId(p_mm.getModuleId());
				mms.add(mm);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return mms;
	}
	
	public HashMap getTargetModuleMapping(long p_companyId, String p_srcLocale, String p_tarLocale, long p_moduleId, String p_module) {
		ModuleMapping mm = null;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		StringBuilder sql = null;
		boolean isFile = true;
		HashMap result = new HashMap();
		
		if (p_srcLocale == null || p_module == null)
			return null;
		
		try {
			conn = ConnectionPool.getConnection();
			stmt = conn.createStatement();
			ModuleMappingManagerLocal manager = new ModuleMappingManagerLocal();
			String baseDocDir = CVSUtil.getBaseDocRoot();
			String realSourceModulePath = baseDocDir.concat(p_module);
			File srcFolder = new File(realSourceModulePath);
			//If source module is a file, then get its parent path as a parentPath to use to match if 
			String parentPath = "";
			parentPath = srcFolder.getParent();
			if (parentPath.length() > baseDocDir.length())
			    parentPath = parentPath.substring(baseDocDir.length());
			else
			    parentPath = "";
				
			sql = new StringBuilder();
			sql.append("select * from module_mapping where is_active='Y' and company_id=").append(p_companyId).append(" and module_id=").append(p_moduleId);
			sql.append(" and source_locale='").append(p_srcLocale).append("' and target_locale='").append(p_tarLocale).append("'");
			sql.append(" and source_module='").append(replaceToSQL(p_module)).append("'");
			//logger.info("To get module mapping ==== " + sql.toString());
			rs = stmt.executeQuery(sql.toString());
			if (rs.next()) {
				//Get corresponding module mapping match
				long mmId = rs.getLong("ID");
				//logger.info("Get corresponding module mapping. Id == " + mmId);
				mm = manager.getModuleMapping(mmId);
				result.put("type", "0");
				result.put("moduleMapping", mm);
			} else {
				//NOT get corresponding module mapping match
//				if (isFile) {
					//logger.info("Get file's parent path module mapping");
					sql = new StringBuilder();
					sql.append("select * from module_mapping where is_active='Y' and company_id=").append(p_companyId).append(" and module_id=").append(p_moduleId);
					sql.append(" and source_locale='").append(p_srcLocale).append("' and target_locale='").append(p_tarLocale).append("'");
					sql.append(" and source_module='").append(replaceToSQL(parentPath)).append("'");
					rs = stmt.executeQuery(sql.toString());
					if (rs.next()) {
						long mmId = rs.getLong("ID");
						//logger.info("File :: Get corresponding module mapping. Id == " + mmId);
						mm = manager.getModuleMapping(mmId);
						result.put("type", "1");
						result.put("moduleMapping", mm);
					}
//				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				stmt.close();
				ConnectionPool.returnConnection(conn);
			} catch (Exception ie) {}
		}
		return result;
	}
	
	public String getTargetFileName(ModuleMappingRename p_mmr, String p_srcFile) {
		if (p_mmr == null || p_srcFile == null || p_srcFile.trim().equals(""))
			return p_srcFile;
		if (p_srcFile.equalsIgnoreCase(p_mmr.getSourceName()))
			return p_mmr.getTargetName();
		else
			return p_srcFile;
	}
	
	private String replaceToSQL(String p_str) {
		return p_str.replaceAll("\\\\", "\\\\\\\\");
	}
	public void verifyGetAllSubFolders(File p_path) {
		ArrayList<File> folders = getAllSubFolders(p_path);
		System.out.println("Base folder ====" + p_path);
		for (File folder : folders) {
			System.out.println("Folder====" + folder.getAbsolutePath());
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ModuleMappingHelper helper = new ModuleMappingHelper();
		//helper.verifyGetAllSubFolders(new File("d:\\GS_Update"));
		ModuleMapping mm = new ModuleMapping();
		mm.setSourceModule("installer\\script");
		mm.setTargetModule("tmp");
		ArrayList<ModuleMapping> tmp = helper.createSubModuleMapping(mm);
		for (ModuleMapping m : tmp) {
			System.out.println("SourceModule====" + m.getSourceModule());
			System.out.println("TargetModule====" + m.getTargetModule()+ "\r\n");
		}
	}
}
