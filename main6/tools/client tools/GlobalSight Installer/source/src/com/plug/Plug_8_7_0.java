package com.plug;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.install.Upgrade;
import com.util.FileUtil;
import com.util.ServerUtil;
import com.util.db.DbUtil;
import com.util.db.DbUtilFactory;

public class Plug_8_7_0 implements Plug
{
    private static Logger log = Logger.getLogger(Plug_8_7_0.class);

    public DbUtil dbUtil = DbUtilFactory.getDbUtil();
    private String propertiesPath = ServerUtil.getPath() + "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/properties/";

    // use "blaise-translation-supplier-api-example-1.3.0-jar-with-dependencies.jar" to replace this one
    private static final String BLAISE_OLD_JAR_FILE = "/jboss/server/standalone/deployments/globalsight.ear/lib/blaise-translation-supplier-api-example-1.2.1.jar";

    @Override
	public void run()
	{
		executeCreateIndexSql();
		updateThreadProperties();
		installGlobalSightService();

        // Delete old Blaise jar file
        deleteFiles(ServerUtil.getPath() + BLAISE_OLD_JAR_FILE);
	}

	private void executeCreateIndexSql()
	{
		List<String> sqlList = getSqlList();
		for (String sql : sqlList)
		{
			try
			{
				dbUtil.update(sql);
			}
			catch (SQLException e)
			{
			    // ignore "duplicate key name" case, log others
			    if (e.getMessage().indexOf("Duplicate key name") == -1)
			    {
			        log.error("Failed to execute sql: " + sql, e);
			    }
			}
		}
	}

	private List<String> getSqlList()
	{
		List<String> sqlList = new ArrayList<String>();
		sqlList.add("ALTER TABLE SOURCE_PAGE MODIFY COLUMN EXTERNAL_PAGE_ID VARCHAR(750);");
		sqlList.add("ALTER TABLE UPDATED_SOURCE_PAGE MODIFY COLUMN EXTERNAL_PAGE_ID VARCHAR(750);");
		sqlList.add("ALTER TABLE ADDING_SOURCE_PAGE MODIFY COLUMN EXTERNAL_PAGE_ID VARCHAR(750);");
		sqlList.add("ALTER TABLE DELAYED_IMPORT_REQUEST MODIFY COLUMN EXTERNAL_PAGE_ID VARCHAR(750);");
		sqlList.add("CREATE INDEX INDEX_ID_TS_EPID ON SOURCE_PAGE(ID, TIMESTAMP, EXTERNAL_PAGE_ID);");
		sqlList.add("CREATE INDEX INDEX_EPID ON SOURCE_PAGE(EXTERNAL_PAGE_ID);");
		sqlList.add("CREATE INDEX INDEX_TUV_ID ON XLIFF_ALT(TUV_ID);");
		sqlList.add("CREATE INDEX INDEX_ID_STATE ON JOB(ID, STATE);");
		sqlList.add("CREATE INDEX INDEX_JOB_NAME ON JOB(NAME);");
		sqlList.add("CREATE INDEX INDEX_STATE_TIMESTAMP ON JOB(STATE, TIMESTAMP);");
		sqlList.add("CREATE INDEX INDEX_WFIDSTATE ON WORKFLOW(IFLOW_INSTANCE_ID, STATE);");
		sqlList.add("CREATE INDEX INDEX_WFIDTLID ON WORKFLOW (IFLOW_INSTANCE_ID, TARGET_LOCALE_ID);");
		sqlList.add("CREATE INDEX INDEX_WFID ON WORKFLOW_OWNER(WORKFLOW_ID);");
		sqlList.add("CREATE INDEX INDEX_ID_USERID ON PROJECT(PROJECT_SEQ, MANAGER_USER_ID);");
		sqlList.add("CREATE INDEX INDEX_ID_BATCHID ON REQUEST(ID,BATCH_ID);");
		sqlList.add("CREATE INDEX INDEX_ID_PAGEID ON REQUEST(ID, PAGE_ID);");
		sqlList.add("CREATE INDEX INDEX_REQUEST_PAGEID ON REQUEST(PAGE_ID);");
		sqlList.add("CREATE INDEX INDEX_REQUEST_JOBID ON REQUEST(JOB_ID);");
		sqlList.add("CREATE INDEX INDEX_REQUEST_BATCHID ON REQUEST(BATCH_ID);");
		sqlList.add("CREATE INDEX INDEX_IS_UI_LOCALE ON LOCALE(IS_UI_LOCALE);");
		sqlList.add("CREATE INDEX INDEX_ISO_COUNTRY_LANG ON LOCALE(ISO_COUNTRY_CODE,ISO_LANG_CODE);");
		sqlList.add("CREATE INDEX INDEX_SL_ACTIVE ON LOCALE_PAIR (IS_ACTIVE, SOURCE_LOCALE_ID);");
		sqlList.add("CREATE INDEX INDEX_CTUVID ON TASK_TUV(CURRENT_TUV_ID);");
		sqlList.add("CREATE INDEX IDX_TASK_TUV_PREV_TUVID ON TASK_TUV( PREVIOUS_TUV_ID );");
		sqlList.add("CREATE INDEX INDEX_SLID ON L10N_PROFILE(SOURCE_LOCALE_ID);");
		sqlList.add("CREATE INDEX INDEX_IDNAME ON L10N_PROFILE(ID, NAME);");
		sqlList.add("CREATE INDEX INDEX_LKEY_OTYPE ON ISSUE(LOGICAL_KEY, ISSUE_OBJECT_TYPE);");
		sqlList.add("CREATE INDEX INDEX_STATUS_OTYPE ON ISSUE(STATUS, ISSUE_OBJECT_TYPE);");
		sqlList.add("CREATE INDEX INDEX_OID_OTYPE ON ISSUE(ISSUE_OBJECT_ID, ISSUE_OBJECT_TYPE);");
		sqlList.add("CREATE INDEX INDEX_TARGET_PAGE_ID ON ISSUE(TARGET_PAGE_ID);");
		sqlList.add("CREATE INDEX INDEX_IID ON ISSUE_HISTORY(ISSUE_ID);");
		sqlList.add("CREATE INDEX INDEX_TPID_TUVID_SUBID ON IMAGE_REPLACE_FILE_MAP(TARGET_PAGE_ID, TUV_ID, SUB_ID);");
		sqlList.add("CREATE INDEX INDEX_IMAGEMAP_TPID ON IMAGE_REPLACE_FILE_MAP(TARGET_PAGE_ID);");
		sqlList.add("CREATE INDEX IDX_IMAGE_REP_FM_TUVID ON IMAGE_REPLACE_FILE_MAP(TUV_ID);");
		sqlList.add("CREATE INDEX IDX_RESERVED_TIME ON RESERVED_TIME (USER_CALENDAR_ID, TASK_ID, START_TIME, END_TIME);");
		sqlList.add("CREATE INDEX IDX_L10N_PROJID_ID ON L10N_PROFILE (PROJECT_ID, ID);");
		sqlList.add("CREATE INDEX IDX_REQUEST_L10NID_JOB ON REQUEST(L10N_PROFILE_ID, JOB_ID);");
		sqlList.add("CREATE INDEX IDX_WORKFLOW_JOB ON WORKFLOW(JOB_ID);");
		sqlList.add("CREATE INDEX IDX_WORKFLOW_STATE_JOB ON WORKFLOW(STATE, JOB_ID);");
		sqlList.add("CREATE INDEX IDX_TASK_INFO_WORKFLOW_ID ON TASK_INFO (WORKFLOW_ID);");
		sqlList.add("CREATE INDEX IDX_COST_BY_WORD_COUNT_COST_ID ON COST_BY_WORD_COUNT(COST_ID);");

		return sqlList;
	}
	
	private void updateThreadProperties()
	{
		for(String p : Upgrade.COPY_UNCOVER)
		{
			updateThreadProperty(p);
		}
	}
	
	private void updateThreadProperty(String name)
	{
	    File f = new File(propertiesPath + name);
	    String content = FileUtil.readFile(f);
	    int index = content.indexOf("# Set thread number");
	    if (index > 0)
	    {
	        content = content.substring(index);
	        try 
	        {
				FileUtil.writeFile(f, content);
			}
	        catch (IOException e) 
	        {
				log.error("Failed to update file: " + name, e);
			}
	    }
	}
	
	private void installGlobalSightService()
	{
		try 
		{
			URL url = new URL("file:" + ServerUtil.getPath() + "/install/installer.jar");
			URLClassLoader loader = new URLClassLoader( new URL[]{ url } );
			Class<?> install = loader.loadClass("Install");
			Object instance = install.newInstance();
			Field f = install.getField("JBOSS_UTIL_BIN");
			f.set(instance, ServerUtil.getPath() + "/jboss/util/bin");
			Method m = install.getMethod("determineOperatingSystem");
			m.invoke(instance);
			m = install.getMethod("installGlobalSightService");
			m.invoke(instance);
			loader.close();
		} 
		catch (Exception e) 
		{
			log.error(e);
		}
	}

    private void deleteFiles(String path)
    {
        try
        {
            File f = new File(path);
            if (f.exists())
                FileUtil.deleteFile(f);
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }
}
