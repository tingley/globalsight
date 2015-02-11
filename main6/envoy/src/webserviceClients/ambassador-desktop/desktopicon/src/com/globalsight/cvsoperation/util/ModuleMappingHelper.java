package com.globalsight.cvsoperation.util;

import java.sql.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.globalsight.action.QueryAction;
import com.globalsight.cvsoperation.entity.FileRename;
import com.globalsight.cvsoperation.entity.ModuleMapping;
import com.globalsight.cvsoperation.entity.SourceModuleMapping;
import com.globalsight.cvsoperation.entity.TargetModuleMapping;
import com.globalsight.entity.User;
import com.globalsight.util2.CacheUtil;
import com.mysql.jdbc.Driver;

public class ModuleMappingHelper {
	private static Connection conn = null;
	static Logger log = Logger.getLogger(ModuleMappingHelper.class.getName());
	private String url = "jdbc:mysql://localhost:3306/globalsight";
	private String user = "globalsight";
	private String password = "password";
	
	public void add(SourceModuleMapping sourceModule) {
		this.add(sourceModule, null);
	}
	
	public void add(SourceModuleMapping sourceModule, Connection conn) {
		try {
			if (conn == null)
				conn = getDBConnection();
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			PreparedStatement pstmt = conn.prepareStatement("insert into module_rename_di (source_name, target_name, module_mapping_id) values (?, ?, ?)");
			ResultSet rs = null;
			String newID = "1";
			String sourceLocale = sourceModule.getLocale();
			String fullSourceLocale = sourceModule.getFullLocale();
			String module = sourceModule.getModule();
			StringBuffer sql = new StringBuffer();
			ArrayList<TargetModuleMapping> targets = sourceModule.getTargetModules();
			TargetModuleMapping target = null;
			User user = CacheUtil.getInstance().getCurrentUser();
			for (int i=0;i<targets.size();i++) {
				sql = new StringBuffer();
				target = targets.get(i);
				sql.append("insert into module_mapping_di (SOURCE_LOCALE, SOURCE_LOCALE_LONG, SOURCE_MODULE, TARGET_LOCALE, TARGET_LOCALE_LONG, TARGET_MODULE, USER_ID, IS_ACTIVE) values ('");
				sql.append(sourceLocale).append("','").append(fullSourceLocale).append("','");
				sql.append(replace(module)).append("', '").append(target.getLocale()).append("', '").append(target.getFullLocale()).append("', '");
				sql.append(replace(target.getModule())).append("', '");
				sql.append(user.getName()).append("', '1')");
				stmt.executeUpdate(sql.toString(), Statement.RETURN_GENERATED_KEYS);
				rs = stmt.getGeneratedKeys();
				if (rs.next())
					newID = rs.getString(1);
				ArrayList<FileRename> fileRenames = target.getFileRenames();
				if (fileRenames != null && fileRenames.size()>0) {
					for (int j=0;j<fileRenames.size();j++) {
						pstmt.setString(1, fileRenames.get(j).getSourceFilename());
						pstmt.setString(2, fileRenames.get(j).getTargetFilename());
						pstmt.setString(3, newID);
						pstmt.addBatch();
					}
					pstmt.executeBatch();
				}
			}
			conn.commit();
		} catch (Exception e) {
			log.error("Add new module mapping error! ".concat(e.toString()));
			try {
				conn.rollback();
			} catch (Exception e2) {}
		}
	}
	
	public void update(SourceModuleMapping sourceModule) {
		try {
			String id = sourceModule.getID();
			ModuleMapping mm = getModuleMapping(id);

			conn = getDBConnection();
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();

			User user = CacheUtil.getInstance().getCurrentUser();
			StringBuffer sql = new StringBuffer();
			//Delete all module_rename
			sql.append("delete from module_rename_di where module_mapping_id in (select id from module_mapping_di where source_locale='");
			sql.append(mm.getSourceLocale()).append("' and source_module='").append(replace(mm.getSourceModule())).append("'");
			sql.append(" and user_id='").append(user.getName()).append("')");
			stmt.executeUpdate(sql.toString());
			
			sql = new StringBuffer();
			sql.append("delete from module_mapping_di where source_locale='").append(mm.getSourceLocale()).append("'");
			sql.append("and source_module='").append(replace(mm.getSourceModule())).append("'");
			sql.append(" and user_id='").append(user.getName()).append("'");
			stmt.executeUpdate(sql.toString());
			
			add(sourceModule, conn);
		} catch (Exception e) {
			log.error("Update module mapping error! ".concat(e.toString()));
		}
	}

	public boolean isExist(String sourceLocale, String sourceModule, String targetLocale, String targetModule, boolean full) {
		boolean exist = false;
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			
			User user = CacheUtil.getInstance().getCurrentUser();
			StringBuffer sql = new StringBuffer();
			sql.append("select * from module_mapping_di where is_active=1");
			if (sourceLocale != null && !"".equals(sourceLocale)) {
				if (full)
					sql.append(" and source_locale_long='");
				else
					sql.append(" and source_locale='");
				sql.append(sourceLocale).append("'");
			}
			if (targetLocale != null && !"".equals(targetLocale)) {
				if (full)
					sql.append(" and target_locale_long='");
				else
					sql.append(" and target_locale='");
				sql.append(targetLocale).append("'");
			}
			if (sourceModule != null && !sourceModule.equals(""))
				sql.append(" and source_module='").append(sourceModule).append("'");
			if (targetModule != null && !"".equals(targetModule))
				sql.append(" and target_module='").append(targetModule).append("'");
			sql.append(" and user_id='").append(user.getName()).append("'");
			rs = stmt.executeQuery(sql.toString());
			if (rs.next())
				exist = true;
			else
				exist = false;
			stmt.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return exist;
	}

	public void delete(String id) {
		try {
			conn = getDBConnection();
			conn.setAutoCommit(false);
			Statement stmt = conn.createStatement();
			
			stmt.executeUpdate("delete from module_rename_di where module_mapping_id=" + id);
			stmt.executeUpdate("delete from module_mapping_di where id=" + id);
			
			conn.commit();
		} catch (Exception e) {
			log.error("Delete module mapping error! ".concat(e.toString()));
			try {
				conn.rollback();
			} catch (Exception e2) {}
		}
	}
	
	public void deleteFilename(String p_id) {
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("delete from module_rename_di where id=" + p_id);
			conn.commit();
		} catch (Exception e) {
			log.error("Delete module mapping error! ".concat(e.toString()));
		}
	}
	
	public ArrayList<ModuleMapping> getAllModuleMappings(String p_user) {
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			ArrayList<ModuleMapping> mappings = new ArrayList<ModuleMapping>();
			ModuleMapping module = null;
			
			rs = stmt.executeQuery("select * from module_mapping_di where is_active=1 and user_id='" + p_user + "' order by source_locale, source_module");
			while (rs.next()) {
				module = new ModuleMapping();
				module.setId(rs.getString("ID"));
				module.setSourceLocale(rs.getString("Source_Locale"));
				module.setFullSourceLocale(rs.getString("Source_Locale_Long"));
				module.setSourceModule(rs.getString("Source_Module"));
				module.setTargetLocale(rs.getString("Target_Locale"));
				module.setFullTargetLocale(rs.getString("Target_Locale_Long"));
				module.setTargetModule(rs.getString("Target_Module"));
				module.setUserID(rs.getString("User_ID"));
				
				mappings.add(module);
			}
			return mappings;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public SourceModuleMapping getSourceModuleMapping(String locale, String module, String user) {
		SourceModuleMapping smp = new SourceModuleMapping();
		if (locale == null || locale.trim().equals(""))
			return smp;
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			Statement stmt1 = conn.createStatement();
			ResultSet rs = null, rs1 = null;
			FileRename fr = null;
			ArrayList<FileRename> fileRenames = new ArrayList<FileRename>();
			
			ArrayList<TargetModuleMapping> targets = new ArrayList<TargetModuleMapping>();
			TargetModuleMapping target = null;
			
			rs = stmt.executeQuery("select * from module_mapping_di where is_active=1 and source_locale='" + locale + "' and source_module='" + replace(module) + "' and user_id='" + user + "' order by target_locale, target_module");
			smp.setLocale(locale);
			smp.setModule(module);
			while (rs.next()) {
				smp.setFullLocale(rs.getString("Source_Locale_Long"));
				target = new TargetModuleMapping();
				target.setID(rs.getString("ID"));
				target.setLocale(rs.getString("Target_Locale"));
				target.setFullLocale(rs.getString("Target_Locale_Long"));
				target.setModule(rs.getString("Target_Module"));
				
				fileRenames = new ArrayList<FileRename>();
				rs1 = stmt1.executeQuery("select * from module_rename_di where module_mapping_id=" + target.getID());
				while (rs1.next()) {
					fr = new FileRename();
					fr.setID(rs1.getString("ID"));
					fr.setSourceFilename(rs1.getString("Source_Name"));
					fr.setTargetFilename(rs1.getString("Target_Name"));
					
					fileRenames.add(fr);
				}
				target.setFileRenames(fileRenames);
	
				targets.add(target);
			}
			smp.setTargetModules(targets);
			
			return smp;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public TargetModuleMapping getTargetModuleMapping(String id) {
		TargetModuleMapping t = new TargetModuleMapping();
		if (id == null || "".equals(id))
			return t;
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			
			ArrayList<FileRename> fileRenames = new ArrayList<FileRename>();
			FileRename fr = null;
			
			rs = stmt.executeQuery("select * from module_mapping_di where id=" + id);
			if (rs.next()) {
				t.setID(id);
				t.setLocale(rs.getString("Target_Locale"));
				t.setFullLocale(rs.getString("Target_Locale_Long"));
				t.setModule(rs.getString("Target_Module"));
			}
			rs = stmt.executeQuery("select * from module_rename_di where module_mapping_id=" + id);
			while (rs.next()) {
				fr = new FileRename();
				fr.setID(rs.getString("ID"));
				fr.setSourceFilename(rs.getString("Source_Name"));
				fr.setTargetFilename(rs.getString("Target_Name"));
				
				fileRenames.add(fr);
			}
			t.setFileRenames(fileRenames);
			
			return t;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public ModuleMapping getModuleMapping(String id) {
		if (id == null || "".equals(id))
			return null;
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from module_mapping_di where id=" + id);
			ModuleMapping mm = new ModuleMapping();
			if (rs.next()) {
				mm.setId(rs.getString("ID"));
				mm.setSourceLocale(rs.getString("Source_Locale"));
				mm.setFullSourceLocale(rs.getString("Source_Locale_Long"));
				mm.setSourceModule(rs.getString("Source_Module"));
				mm.setTargetLocale(rs.getString("Target_Locale"));
				mm.setFullTargetLocale(rs.getString("Target_Locale_Long"));
				mm.setTargetModule(rs.getString("Target_Module"));
				mm.setUserID(rs.getString("User_ID"));
			}
			return mm;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	
	public ModuleMapping getModuleMapping(String p_sourceLocale, String p_sourceModule, String p_targetLocale, String p_targetModule) {
		ModuleMapping mm = new ModuleMapping();
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			
			User user = CacheUtil.getInstance().getCurrentUser();
			StringBuffer sql = new StringBuffer();
			sql.append("select * from module_mapping_di where is_active=1 and source_locale='").append(p_sourceLocale).append("' ");
			if (p_sourceModule != null && !p_sourceModule.equals(""))
				sql.append("and source_module='").append(replace(p_sourceModule)).append("' ");
			if (p_targetLocale != null && !"".equals(p_targetLocale))
				sql.append("and target_locale='").append(p_targetLocale).append("' ");
			if (p_targetModule != null && !"".equals(p_targetModule))
				sql.append("and target_module='").append(replace(p_targetModule)).append("'");
			sql.append(" and user_id='").append(user.getName()).append("'");
			
			rs = stmt.executeQuery(sql.toString());
			if (rs.next()) {
				mm.setId(rs.getString("ID"));
				mm.setSourceLocale(rs.getString("Source_Locale"));
				mm.setFullSourceLocale(rs.getString("Source_Locale_Long"));
				mm.setSourceModule(rs.getString("Source_Module"));
				mm.setTargetLocale(rs.getString("Target_Locale"));
				mm.setFullTargetLocale(rs.getString("Target_Locale_Long"));
				mm.setTargetModule(rs.getString("Target_Module"));
				mm.setUserID(rs.getString("User_ID"));
				return mm;
			} else
				return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ModuleMapping getModuleMapping(String p_sourceLocale, String p_sourceModule, String p_targetLocale, String p_targetModule, String p_user, boolean isUpper) {
		ModuleMapping mm = new ModuleMapping();
		try {
			conn = getDBConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			
			StringBuffer sql = new StringBuffer();
			sql.append("select * from module_mapping_di where is_active=1 and UPPER(source_locale)='").append(p_sourceLocale).append("' ");
			if (p_sourceModule != null && !p_sourceModule.equals(""))
				sql.append("and UPPER(source_module)='").append(replace(p_sourceModule)).append("' ");
			if (p_targetLocale != null && !"".equals(p_targetLocale))
				sql.append("and UPPER(target_locale)='").append(p_targetLocale).append("' ");
			if (p_targetModule != null && !"".equals(p_targetModule))
				sql.append("and UPPER(target_module)='").append(replace(p_targetModule)).append("'");
			if (p_user != null && !"".equals(p_user))
				sql.append("and UPPER(user_id)='").append(p_user).append("'");
			rs = stmt.executeQuery(sql.toString());
			if (rs.next()) {
				mm.setId(rs.getString("ID"));
				mm.setSourceLocale(rs.getString("Source_Locale"));
				mm.setFullSourceLocale(rs.getString("Source_Locale_Long"));
				mm.setSourceModule(rs.getString("Source_Module"));
				mm.setTargetLocale(rs.getString("Target_Locale"));
				mm.setFullTargetLocale(rs.getString("Target_Locale_Long"));
				mm.setTargetModule(rs.getString("Target_Module"));
				mm.setUserID(rs.getString("User_ID"));
				return mm;
			} else
				return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Connection getDBConnection() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.setAutoCommit(true);
				return conn;
			} 
			QueryAction query = new QueryAction();
			String connStr = query.execute(new String[]{QueryAction.q_getConnection});
			if (connStr != null) {
				String[] params = connStr.split(",");
				url = params[0];
				user = params[1];
				password = params[2];
				Class.forName("com.mysql.jdbc.Driver");
				if (url.indexOf("localhost") != -1) {
					User u = CacheUtil.getInstance().getCurrentUser();
					String userUrl = u.getHost().getName();
					url = url.replaceAll("localhost", userUrl);
				}
				log.info("DBUrl==".concat(url));
				conn = DriverManager.getConnection(url, user, password);
			}
		} catch (Exception e) {
			log.error("Can NOT get database connection correctly. " + e.toString());
		}
		return conn;
	}

	private String replace(String s) {
		if (s != null && !"".equals(s))
			return s.replaceAll("\\\\", "\\\\\\\\");
		return "";
	}
	
	public void closeDBConnection() {
		try {
			log.info("Close conn == " + conn);
			conn.close();
		} catch (SQLException e) {
			
		}
	}
}
