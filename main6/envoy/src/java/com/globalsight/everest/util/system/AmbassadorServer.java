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
package com.globalsight.everest.util.system;

// globalsight
import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.globalsight.config.SystemParameter;
import com.globalsight.config.SystemParameterEntityException;
import com.globalsight.config.SystemParameterPersistenceManagerLocal;
import com.globalsight.connector.git.GitConnectorPushThread;
import com.globalsight.cxe.adapter.database.DbAutoImporter;
import com.globalsight.cxe.adapter.filesystem.autoImport.AutomaticImportMonitor;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilter;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterMapping;
import com.globalsight.cxe.entity.filterconfiguration.BaseFilterParser;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.InternalItem;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.PropertiesInternalText;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.netegrity.Netegrity;
import com.globalsight.everest.util.system.migration.Migrate820InternalText;
import com.globalsight.everest.util.system.migration.Migrate852UpgradeLucene;
import com.globalsight.everest.util.system.migration.MigrateObj;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.webapp.pagehandler.tasks.AutoCompleteActivityThread;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;
import com.globalsight.ling.tm3.core.persistence.StatementBuilder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import com.globalsight.util.modules.Modules;

/**
 * Used to be EnvoyWLServer. This is the main class that starts up all the RMI
 * services for GlobalSight. But since this class is used by the app server to
 * do startup and shutdown, it cannot be vendor specific.
 */
public class AmbassadorServer
{
    /** The main output log */
    public static String SYSTEM_LOG = "/GlobalSight.log";
    public static final String SYSTEM_LOG_BASENAME = "GlobalSight.log";

    /**
     * Boolean value to tell whether GlobalSight is ready for access
     */
    private static Boolean s_isSystem4Accessible = Boolean.FALSE;
    private final static int MAX_NUM_OF_MESSAGES_PER_SESSION = 10;
    private final static long JDBCPOOL_ID = -1L;
    private static final Logger CATEGORY = Logger
            .getLogger(AmbassadorServer.class);

    private static DbAutoImporter s_dbAutoImporter = null;
    private static Timer s_dbTimer = null;

    private static PrintStream s_originalStdout = System.out;
    private static PrintStream s_originalStderr = System.err;

    /**
     * Use the same object for shutdown/startup
     */
    private static AmbassadorServer s_ambassadorServer = new AmbassadorServer();

    // flag to avoid multiple restarts
    private static boolean s_isStarted = false;
    
    public static final String LUCENE_LOCK_DIR = System.getProperty(
            "org.apache.lucene.lockDir", System.getProperty("java.io.tmpdir"));

    /**
     * Public way to get AmbassadorServer
     * 
     * @return AmbassadorServer
     */
    public static AmbassadorServer getAmbassadorServer()
    {
        return s_ambassadorServer;
    }

    /**
     * Default constructor.
     */
    private AmbassadorServer()
    {
        try
        {
            // set the log directory for the error files
            String logDirectory = SystemConfiguration.getInstance()
                    .getStringParameter(
                            SystemConfigParamNames.SYSTEM_LOGGING_DIRECTORY);
            // set the absolute path
            SYSTEM_LOG = logDirectory + SYSTEM_LOG;
        }
        catch (Exception e)
        {
            CATEGORY.warn(
                    "The log directory couldn't be found in the system configuration "
                            + " for CAP logging purposes.", e);
        }
    }

    /**
     * Tells whether System4 is ready for user access. This is false while the
     * system is starting up or shutting down.
     * 
     * @return true | false
     */
    public static boolean isSystem4Accessible()
    {
        return s_isSystem4Accessible.booleanValue();
    }

    /**
     * This method is called by the appserver when it is stopped and Envoy is
     * running.
     */
    public String shutdown(String p_name, Hashtable p_hashtable)
            throws SystemShutdownException
    {
        synchronized (s_isSystem4Accessible)
        {
            s_isSystem4Accessible = Boolean.FALSE;
        }
        CATEGORY.info("GlobalSight will no longer accept user logins because it is shutting down.");

        String result;

        try
        {
            stopCxeFileSystemAutomaticImport();
            stopCxeDatabaseAutomaticImport();
            ConnectionPool.terminate(JDBCPOOL_ID);

            CATEGORY.info("Stopping Envoy services.");
            Envoy envoy = new Envoy();
            envoy.shutdown();

            result = getClass().getName() + " shutdown successfully";
        }
        catch (SystemShutdownException sse)
        {
            result = getClass().getName() + " shutdown failed due to "
                    + sse.getLocalizedMessage();

            CATEGORY.error("shutdown error", sse);

            throw sse;
        }
        catch (Exception e)
        {
            result = getClass().getName() + " shutdown failed due to "
                    + e.getLocalizedMessage();

            CATEGORY.error("shutdown error", e);

            throw new SystemShutdownException(
                    SystemStartupException.EX_FAILEDTOINITSERVER, e);
        }
        finally
        {
            CATEGORY.info("GlobalSight shutdown complete.");
            resetStandardOutAndStandardError();
        }
        s_isStarted = false;
        return result;
    }

    private void setupTM3ConversionToStop()
    {
        Connection conn = null;
        Statement stmt = null;
        try
        {
            conn = ConnectionPool.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            stmt.execute("UPDATE project_tm SET status='Stopped' WHERE status='Converting'");
            CATEGORY.info("Set up STOP status to running TM3 conversion");
            conn.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("Cannot set up TM3 converting to STOP status.", e);
        }
        finally
        {
            try
            {
                if (stmt != null)
                    stmt.close();
                ConnectionPool.returnConnection(conn);
            }
            catch (Exception e2)
            {
            }

        }
    }

    /**
     * This method is called by the app server when it is started and Envoy
     * should be started.
     */
    public String startup(String p_name, Hashtable p_args)
            throws SystemStartupException
    {
        // avoid multiple calls to startup
        if (s_isStarted == true)
            return "Already started";

        String result = null;
        try
        {
            setStandardOutAndStandardError();
            synchronized (s_isSystem4Accessible)
            {
                s_isSystem4Accessible = Boolean.FALSE;
            }
            CATEGORY.info("GlobalSight is starting up. Disabling UI logins until System is ready.");

            // set the inetsoft sree.home
            String sree_home = SystemConfiguration.getInstance()
                    .getStringParameter("sree.home");
            System.setProperty("sree.home", sree_home);

            boolean isNetegrity = Netegrity.isNetegrityEnabled();
            if (isNetegrity)
                CATEGORY.info("Using Netegrity single sign-on.");

            // If need clean JMS messages when start up server, it should be
            // here.
            // cleanJmsMessages();

            StringBuffer propString = new StringBuffer(
                    "Java System Properties:\r\n");
            Properties props = System.getProperties();
            Enumeration e = props.propertyNames();
            while (e.hasMoreElements())
            {
                String n = (String) e.nextElement();
                String v = (String) props.get(n);
                propString.append(n).append("=").append(v).append("\r\n");
            }
            CATEGORY.info(propString.toString());
            CATEGORY.info("Max number of database connections: "
                    + ConnectionPool.getMaxConnections());
            Connection c = ConnectionPool.getConnection();
            DatabaseMetaData metaData = c.getMetaData();
            CATEGORY.info("JDBC driver version: " + metaData.getDriverVersion());
            CATEGORY.info("MySql Database version: "
                    + metaData.getDatabaseProductVersion());
            ConnectionPool.returnConnection(c);

            setupTM3ConversionToStop();

            // now initialize the permissions
            Permission.initialize();
            // from GBS-657
            CompanyRemoval.clearCompanyState();

            autoDeleteLuceneLockFile();

            fixWrongL10nProfileIdInJobTable();

            Envoy envoy = new Envoy();
            envoy.startup();
            result = getClass().getName() + " started successfully";

            startCxeFileSystemAutomaticImport();
            startCxeDatabaseAutomaticImport();
            
            WorkflowExportingHelper.cleanTable();
            
            GitConnectorPushThread runnable = new GitConnectorPushThread();
            Thread t = new MultiCompanySupportedThread(runnable);
            t.start();

            AutoCompleteActivityThread actrunnable = new AutoCompleteActivityThread();
            Thread thread = new MultiCompanySupportedThread(actrunnable);
            thread.start();
        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error", e);
            result = getClass().getName() + " startup failed due to "
                    + e.getMessage();
            throw new SystemStartupException(
                    SystemStartupException.EX_FAILEDTOINITSERVER, e);
        }
        catch (Throwable t)
        {
            CATEGORY.error("Startup Error", t);
            result = getClass().getName() + " startup failed due to "
                    + t.getMessage();
            throw new SystemStartupException(
                    SystemStartupException.EX_FAILEDTOINITSERVER,
                    t.getMessage());
        }

        try
        {
            // start migration
            doMigration();
        }
        catch (Exception e)
        {
            CATEGORY.error("Startup Error when doing migration", e);
            result = getClass().getName() + " startup failed due to "
                    + e.getMessage();
            throw new SystemStartupException(
                    SystemStartupException.EX_FAILEDTOINITSERVER, e);
        }

        synchronized (s_isSystem4Accessible)
        {
            s_isSystem4Accessible = Boolean.TRUE;
        }
        CATEGORY.info("GlobalSight is now ready to accept logins.");
        s_isStarted = true;

        return result;
    }

    /**
     * Do migration for GlobalSight upgrade
     * 
     * @throws Exception
     */
    private void doMigration() throws Exception
    {
        //MigrateObj[] objs = {new Migrate820InternalText(), new Migrate852UpgradeLucene()};
        MigrateObj[] objs = {new Migrate820InternalText()};
        
        for (int i = 0; i < objs.length; i++)
        {
            MigrateObj mobj = objs[i];
            if (mobj.checkIfDoMigration())
            {
                mobj.doMigration();
            }
        }
    }


    /**
     * Sets System.out and System.err to write to GlobalSight.log.
     */
    private void setStandardOutAndStandardError() throws Exception
    {
        // FileOutputStream fos = new FileOutputStream(SYSTEM_LOG, true);
        // PrintStream ps = new PrintStream(fos);
        //
        // System.setOut(ps);
        // System.setErr(ps);
        //
        // System.out.println("------------------Welocalize GlobalSight ("
        // + BuildVersion.BUILD_VERSION
        // + ") Log ------------------");
        // System.out.println("Standard out will be logged here.");
        // System.out.println("Standard error will be logged here.");
        AppServerWrapperFactory.getAppServerWrapper();
    }

    /**
     * Re-Sets System.out and System.err to the original destinations (This is
     * needed so the appserver can stay up, and you can delete GlobalSight.log)
     */
    private void resetStandardOutAndStandardError()
    {
        System.out.println("Re-setting Standard out and Standard err");
        System.out.close();
        System.err.close();
        System.setOut(s_originalStdout);
        System.setErr(s_originalStderr);
        System.out.println("Reset Standard out.");
        System.err.println("Reset Standard err.");
    }

    /**
     * Stops file system auto import
     * 
     * @throws SystemShutdownException
     */
    private void stopCxeFileSystemAutomaticImport()
            throws SystemShutdownException
    {
        CATEGORY.info("Stopping file system auto import.");
        try
        {
            boolean waitForThreadDeath = false;
            AutomaticImportMonitor.getInstance().shutdown(waitForThreadDeath);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to shutdown CXE auto import", e);
            throw new SystemShutdownException(
                    SystemShutdownException.EX_FAILEDTOINITSERVER, e);
        }
    }

    /**
     * Initializes and Starts Automatic Import in a separate thread
     * 
     * @throws SystemStartupException
     */
    private void startCxeFileSystemAutomaticImport()
            throws SystemStartupException
    {
        try
        {
            String docsDir = SystemConfiguration.getInstance()
                    .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR);
            AutomaticImportMonitor.initialize("FileSystem", docsDir);
            AutomaticImportMonitor.getInstance().startup();
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to startup CXE auto import", e);
            throw new SystemStartupException(
                    SystemStartupException.EX_FAILEDTOINITSERVER, e);
        }
    }

    /**
     * Shuts down the CXE db auto import thread.
     * 
     * @exception SystemShutdownException
     */
    private static void stopCxeDatabaseAutomaticImport()
            throws SystemShutdownException
    {
        CATEGORY.info("Stopping database auto import.");
        try
        {
            if (s_dbTimer != null)
            {
                CATEGORY.info("Stopping CXE database auto-import.");
                s_dbTimer.cancel();
            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to shutdown CXE database auto import", e);
            throw new SystemShutdownException(
                    SystemShutdownException.EX_FAILEDTOINITSERVER, e);
        }
    }

    /**
     * Initializes and Starts DB Automatic Import in a separate thread
     * 
     * @throws SystemStartupException
     */
    private static void startCxeDatabaseAutomaticImport()
            throws SystemStartupException
    {
        try
        {
            if (Modules.isDatabaseAdapterInstalled())
            {
                s_dbAutoImporter = new DbAutoImporter();
                s_dbTimer = new Timer(true); // isDaemon=true
                CATEGORY.info("Starting CXE database auto-import.");
                s_dbTimer.scheduleAtFixedRate(s_dbAutoImporter,
                        s_dbAutoImporter.getDelay(),
                        s_dbAutoImporter.getPeriod());

            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to startup CXE database auto import", e);
            throw new SystemStartupException(
                    SystemStartupException.EX_FAILEDTOINITSERVER, e);
        }
    }

    /**
     * Deletes the temp lock file which was created when importing Tm or Tm
     * index when the server starts up. Checks whether there is lucene lock file
     * for Tm index in temp directory.
     */
    private void autoDeleteLuceneLockFile()
    {
        File tempDir = new File(LUCENE_LOCK_DIR);
        if (!tempDir.exists())
        {
            return;
        }
        
        File[] tempFileList = tempDir.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isFile()
                        && FileUtils.getBaseName(pathname.getName()).endsWith(
                                ".lock");
            }
        });

        if (tempFileList != null)
        {
            File tempLockFile = null;
            for (int i = 0; i < tempFileList.length; i++)
            {
                tempLockFile = tempFileList[i];
                tempLockFile.delete();
            }
        }

    }

    /**
     * For case only: "L10N_PROFILE_ID" is -1 in "JOB" table, legacy issue.
     */
    private void fixWrongL10nProfileIdInJobTable()
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            StatementBuilder sb = new StatementBuilder();
            sb.append(
                    "UPDATE job j, (SELECT job_id, l10n_profile_id FROM request WHERE job_id IS NOT NULL GROUP BY job_id, l10n_profile_id) AS req ")
                    .append("SET j.L10N_PROFILE_ID = req.l10n_profile_id ")
                    .append("WHERE j.ID = req.job_id ")
                    .append("AND (j.L10N_PROFILE_ID <= 0 || j.L10N_PROFILE_ID IS NULL) ");
            SQLUtil.exec(conn, sb);
        }
        catch (Exception e)
        {
            CATEGORY.error(e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }
}
