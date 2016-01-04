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
package com.globalsight.everest.servlet.util;


import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.calendar.CalendarManagerWLRemote;
import com.globalsight.config.SystemParameterPersistenceManager;
import com.globalsight.config.UserParameterPersistenceManager;
import com.globalsight.cxe.persistence.databasecolumn.DatabaseColumnPersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.dbconnection.DBConnectionPersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.documentum.DocumentumPersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.previewurl.PreviewUrlPersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.xmlrulefile.XmlRuleFilePersistenceManagerWLRemote;
import com.globalsight.cxe.persistence.segmentationrulefile.SegmentationRuleFilePersistenceManagerWLRemote;
import com.globalsight.everest.aligner.AlignerManagerWLRemote;
import com.globalsight.everest.comment.CommentManagerWLRemote;
import com.globalsight.everest.corpus.CorpusManagerWLRemote;
import com.globalsight.everest.costing.CostingEngineWLRemote;
import com.globalsight.everest.edit.SynchronizationManager;
import com.globalsight.everest.edit.offline.OfflineEditManagerWLRemote;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.online.OnlineEditorManager;
import com.globalsight.everest.edit.online.OnlineEditorManagerWLRemote;
import com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapPersistenceManagerWLRemote;
import com.globalsight.everest.glossaries.GlossaryManagerWLRemote;
import com.globalsight.everest.jobhandler.JobEventObserverWLRemote;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.jobhandler.JobHandlerWLRemote;
import com.globalsight.everest.jobhandler.jobcreation.JobCreatorWLRemote;
import com.globalsight.everest.jobhandler.jobmanagement.JobDispatchEngineWLRemote;
import com.globalsight.everest.jobreportingmgr.JobReportingManagerWLRemote;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.nativefilestore.NativeFileManagerWLRemote;
import com.globalsight.everest.page.PageEventObserverWLRemote;
import com.globalsight.everest.page.PageManagerWLRemote;
import com.globalsight.everest.page.TemplateManagerWLRemote;
import com.globalsight.everest.page.pageexport.ExportEventObserverWLRemote;
import com.globalsight.everest.projecthandler.ProjectEventObserverWLRemote;
import com.globalsight.everest.projecthandler.ProjectHandlerWLRemote;
import com.globalsight.everest.request.RequestHandlerWLRemote;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileMgrWLRemote;
import com.globalsight.everest.securitymgr.SecurityManagerWLRemote;
import com.globalsight.everest.snippet.SnippetLibraryWLRemote;
import com.globalsight.everest.taskmanager.TaskManagerWLRemote;
import com.globalsight.everest.tm.TmManager;
import com.globalsight.everest.tuv.TuvEventObserverWLRemote;
import com.globalsight.everest.tuv.TuvManagerWLRemote;
import com.globalsight.everest.usermgr.UserManagerWLRemote;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.vendormanagement.VendorManagementWLRemote;
import com.globalsight.everest.webapp.pagehandler.rss.RSSPersistenceManagerWLRemote;
import com.globalsight.everest.workflow.WorkflowServerWLRemote;
import com.globalsight.everest.workflowmanager.WorkflowEventObserverWLRemote;
import com.globalsight.everest.workflowmanager.WorkflowManagerWLRemote;
import com.globalsight.mediasurface.CmsUserManagerWLRemote;
import com.globalsight.scheduling.EventSchedulerWLRemote;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.scheduler.ITermbaseScheduler;
import com.globalsight.terminology.termleverager.TermLeverageManagerWLRemote;


import com.globalsight.everest.util.server.RegistryLocator;
import com.globalsight.everest.util.server.ServerRegistry;


import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;
import com.globalsight.util.mail.MailerWLRemote;
import com.globalsight.webservices.remoteaccess.RemoteAccessManagerWLRemote;

import java.rmi.RemoteException;

import java.util.HashMap;


/**
 * Please add documentation for this class.
 */
public class ServerProxy
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            ServerProxy.class.getName());

    // lazy instantiation variables.

    private static AlignerManagerWLRemote m_alignerManager = null;
    private static CalendarManagerWLRemote m_calendarManager = null;
    private static CmsUserManagerWLRemote m_cmsUserManager = null;
    private static CommentManagerWLRemote m_commentReferenceManager = null;
    private static CorpusManagerWLRemote m_corpusManager = null;
    private static CostingEngineWLRemote m_costingEngine = null;
    private static DBConnectionPersistenceManagerWLRemote m_dbconnectionManager = null;
    private static DatabaseColumnPersistenceManagerWLRemote m_dbColumnManager = null;
    private static DatabaseProfilePersistenceManagerWLRemote m_dbProfileManager = null;
    private static EventSchedulerWLRemote m_eventScheduler = null;
    private static ExportEventObserverWLRemote m_exportEventObserver = null;
    private static ExportLocationPersistenceManagerWLRemote m_exportLocationManager = null;
    private static FileProfilePersistenceManagerWLRemote m_fileprofileManager = null;
    private static GlossaryManagerWLRemote m_glossaryManager = null;
    private static ITermbaseManager m_termbaseManager = null;
    private static ITermbaseScheduler m_termbaseScheduler = null;
    private static ImageReplaceFileMapPersistenceManagerWLRemote m_imageReplaceMapManager = null;
    private static JobCreatorWLRemote m_jobCreator = null;
    private static JobDispatchEngineWLRemote m_jobDispatchEngine = null;
    private static JobEventObserverWLRemote m_jobEventObserver = null;
    private static JobHandlerWLRemote m_jobHandler = null;
    private static JobReportingManagerWLRemote m_jobReportingManager = null;
    private static LocaleManagerWLRemote m_localeManager = null;
    private static MailerWLRemote m_mailer = null;
    private static NativeFileManagerWLRemote m_nativeFileManager = null;
    private static SynchronizationManager m_synchronizationManager = null;
    private static OfflineEditManagerWLRemote m_offlineEditManager = null;
    private static OnlineEditorManagerWLRemote m_onlineEditorManager = null;
    private static PageEventObserverWLRemote m_pageEventObserver = null;
    private static PageManagerWLRemote m_pageManager = null;
    private static PreviewUrlPersistenceManagerWLRemote m_previewUrlManager = null;
    private static ProjectEventObserverWLRemote m_projectEventObserver = null;
    private static ProjectHandlerWLRemote m_projectHandler = null;
    private static RequestHandlerWLRemote m_requestHandler = null;
    private static SecondaryTargetFileMgrWLRemote m_stfManager = null;
    private static SecurityManagerWLRemote m_securityManager = null;
    private static ServerRegistry SERVER_REGISTRY = null;
    private static SnippetLibraryWLRemote m_snippetLibrary = null;
    private static SystemParameterPersistenceManager m_sysParamManager = null;
    private static TaskManagerWLRemote m_taskManager = null;
    private static TemplateManagerWLRemote m_templateManager = null;
    private static TermLeverageManagerWLRemote m_termLeverageManager = null;
    private static TmManager m_tmManager = null;
    private static TuvEventObserverWLRemote m_tuvEventObserver = null;
    private static TuvManagerWLRemote m_tuvManager = null;
    private static UserManagerWLRemote m_userManager = null;
    private static UserParameterPersistenceManager m_usrParamManager = null;
    private static VendorManagementWLRemote m_vendorManagement = null;
    private static WorkflowEventObserverWLRemote m_workflowEventObserver = null;
    private static WorkflowManagerWLRemote m_workflowManager = null;
    private static WorkflowServerWLRemote m_workflowServer = null;
    private static XmlRuleFilePersistenceManagerWLRemote m_xmlruleManager = null;
    private static SegmentationRuleFilePersistenceManagerWLRemote m_segmentationruleManager = null;
    private static DocumentumPersistenceManagerWLRemote m_documentumPersistenceManager = null;
    private static RSSPersistenceManagerWLRemote m_rssPersistenceManager = null;
    private static RemoteAccessManagerWLRemote m_remoteAccessManager = null;

    static
    {
        try
        {
            SERVER_REGISTRY = RegistryLocator.getRegistry();
        }
        catch (GeneralException ge)
        {
            CATEGORY.error("cannot access registry", ge);
        }
    }


    private static HashMap m_services = new HashMap(30);

    /////////////////////////////////////////////////////////////////////////
    //////////////////  BEGIN: ServerProxy  interface methods  //////////////
    /////////////////////////////////////////////////////////////////////////
    public static ExportLocationPersistenceManagerWLRemote getExportLocationPersistenceManager()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_exportLocationManager == null)
        {
            m_exportLocationManager = (ExportLocationPersistenceManagerWLRemote)
                SERVER_REGISTRY.lookup(
                    ExportLocationPersistenceManagerWLRemote.SERVICE_NAME);
        }

        return m_exportLocationManager;
    }

    public static CostingEngineWLRemote getCostingEngine()
       throws GeneralException
   {
       if (m_costingEngine == null)
       {
           try
           {
               m_costingEngine = (CostingEngineWLRemote)
                   SERVER_REGISTRY.lookup(
                       CostingEngineWLRemote.SERVICE_NAME);
           }
           catch (NamingException ne)
           {
               throwException(ne);
           }
       }
        return m_costingEngine;
   }



    public static JobHandlerWLRemote getJobHandler()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_jobHandler == null)
        {
            m_jobHandler = (JobHandlerWLRemote)SERVER_REGISTRY.lookup(
                JobHandler.SERVICE_NAME);
        }

        return m_jobHandler;
    }

    public static JobCreatorWLRemote getJobCreator()
        throws GeneralException
    {
        if (m_jobCreator == null)
        {
            try
            {
                m_jobCreator = (JobCreatorWLRemote)
                    SERVER_REGISTRY.lookup(
                        JobCreatorWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_jobCreator;
    }

    public static JobDispatchEngineWLRemote getJobDispatchEngine()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_jobDispatchEngine == null)
        {
            m_jobDispatchEngine = (JobDispatchEngineWLRemote)
                SERVER_REGISTRY.lookup(
                    JobDispatchEngineWLRemote.SERVICE_NAME);
        }

        return m_jobDispatchEngine;
    }

    public static JobEventObserverWLRemote getJobEventObserver()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_jobEventObserver == null)
        {
            m_jobEventObserver = (JobEventObserverWLRemote)
                SERVER_REGISTRY.lookup(
                    JobEventObserverWLRemote.SERVICE_NAME);
        }

        return m_jobEventObserver;
    }

    public static JobReportingManagerWLRemote getJobReportingManager()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_jobReportingManager == null)
        {
            m_jobReportingManager = (JobReportingManagerWLRemote)
                SERVER_REGISTRY.lookup(
                    JobReportingManagerWLRemote.SERVICE_NAME);
        }

        return m_jobReportingManager;
    }

    public static RequestHandlerWLRemote getRequestHandler()
        throws GeneralException
    {
        if (m_requestHandler == null)
        {
            try
            {
                m_requestHandler = (RequestHandlerWLRemote)
                    SERVER_REGISTRY.lookup(
                        RequestHandlerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_requestHandler;
    }

    public static LocaleManagerWLRemote getLocaleManager()
        throws GeneralException
    {
        if (m_localeManager == null)
        {
            try
            {
                m_localeManager = (LocaleManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        LocaleManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_localeManager;
    }

    public static ProjectHandlerWLRemote getProjectHandler()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_projectHandler == null)
        {
            m_projectHandler = (ProjectHandlerWLRemote)
                SERVER_REGISTRY.lookup(
                    ProjectHandlerWLRemote.SERVICE_NAME);
        }

        return m_projectHandler;
    }

    public static ProjectEventObserverWLRemote getProjectEventObserver()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_projectEventObserver == null)
        {
            m_projectEventObserver = (ProjectEventObserverWLRemote)
                SERVER_REGISTRY.lookup(
                    ProjectEventObserverWLRemote.SERVICE_NAME);
        }

        return m_projectEventObserver;
    }

    public static UserManagerWLRemote getUserManager()
        throws GeneralException
    {
        if (m_userManager == null)
        {
            try
            {
                m_userManager = (UserManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        UserManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_userManager;
    }

    public static SecurityManagerWLRemote getSecurityManager()
        throws GeneralException
    {
        if (m_securityManager == null)
        {
            try
            {
                m_securityManager = (SecurityManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        SecurityManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_securityManager;
    }

    public static CorpusManagerWLRemote getCorpusManager()
        throws GeneralException
    {
        if (m_corpusManager == null)
        {
            try
            {
                m_corpusManager = (CorpusManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        CorpusManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_corpusManager;
    }

    public static SnippetLibraryWLRemote getSnippetLibrary()
        throws GeneralException
    {
        if (m_snippetLibrary == null)
        {
            try
            {
                m_snippetLibrary = (SnippetLibraryWLRemote)
                    SERVER_REGISTRY.lookup(
                        SnippetLibraryWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_snippetLibrary;
    }

    public static TemplateManagerWLRemote getTemplateManager()
        throws GeneralException
    {
        if (m_templateManager == null)
        {
            try
            {
               m_templateManager = (TemplateManagerWLRemote)
                   SERVER_REGISTRY.lookup(
                       TemplateManagerWLRemote.SERVICE_NAME);
           }
           catch (NamingException ne)
           {
               throwException(ne);
           }
       }

       return m_templateManager;
    }


    public static WorkflowServerWLRemote getWorkflowServer()
        throws GeneralException
    {
        if (m_workflowServer == null)
        {
            try
            {
                m_workflowServer = (WorkflowServerWLRemote)
                    SERVER_REGISTRY.lookup(
                        WorkflowServerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_workflowServer;
    }

    public static DBConnectionPersistenceManagerWLRemote getDBConnectionPersistenceManager()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_dbconnectionManager == null)
        {
            m_dbconnectionManager = (DBConnectionPersistenceManagerWLRemote)
                SERVER_REGISTRY.lookup(
                    DBConnectionPersistenceManagerWLRemote.SERVICE_NAME);
        }

        return m_dbconnectionManager;
    }

    public static XmlRuleFilePersistenceManagerWLRemote getXmlRuleFilePersistenceManager()
			throws GeneralException, NamingException, RemoteException
	{
		if (m_xmlruleManager == null)
		{
			m_xmlruleManager = (XmlRuleFilePersistenceManagerWLRemote) SERVER_REGISTRY
					.lookup(XmlRuleFilePersistenceManagerWLRemote.SERVICE_NAME);
		}

		return m_xmlruleManager;
	}

    public static SegmentationRuleFilePersistenceManagerWLRemote getSegmentationRuleFilePersistenceManager()
			throws GeneralException, NamingException, RemoteException
	{
		if (m_segmentationruleManager == null)
		{
			m_segmentationruleManager = (SegmentationRuleFilePersistenceManagerWLRemote) SERVER_REGISTRY
					.lookup(SegmentationRuleFilePersistenceManagerWLRemote.SERVICE_NAME);
		}

		return m_segmentationruleManager;
	}

    public static FileProfilePersistenceManagerWLRemote getFileProfilePersistenceManager()
        throws GeneralException,
               NamingException,
               RemoteException
    {
        if (m_fileprofileManager == null)
        {
            m_fileprofileManager =
                (FileProfilePersistenceManagerWLRemote)
                SERVER_REGISTRY.lookup(
                    FileProfilePersistenceManagerWLRemote.SERVICE_NAME);
        }

        return m_fileprofileManager;
    }

    public static PageManagerWLRemote getPageManager()
        throws GeneralException
    {
        if (m_pageManager == null)
        {
            try
            {
                m_pageManager = (PageManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        PageManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_pageManager;
    }

    public static TaskManagerWLRemote getTaskManager()
        throws GeneralException
    {
        if (m_taskManager == null)
        {
            try
            {
                m_taskManager = (TaskManagerWLRemote)SERVER_REGISTRY.
                    lookup(TaskManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_taskManager;
    }

    public static PageEventObserverWLRemote getPageEventObserver()
        throws GeneralException
    {
        if (m_pageEventObserver == null)
        {
            try
            {
                m_pageEventObserver = (PageEventObserverWLRemote)
                    SERVER_REGISTRY.lookup(
                        PageEventObserverWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_pageEventObserver;
    }

    public static TuvEventObserverWLRemote getTuvEventObserver()
        throws GeneralException
    {
        if (m_tuvEventObserver == null)
        {
            try
            {
                m_tuvEventObserver = (TuvEventObserverWLRemote)
                    SERVER_REGISTRY.lookup(
                        TuvEventObserverWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_tuvEventObserver;
    }

    public static TuvManagerWLRemote getTuvManager()
        throws GeneralException
    {
        if (m_tuvManager == null)
        {
            try
            {
                m_tuvManager = (TuvManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        RemoteServer.getServiceName(
                            TuvManagerWLRemote.class));
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_tuvManager;
    }

    /*public static IndexerWLRemote getIndexer()
      throws GeneralException
      {
      if (m_indexer == null)
      {
      try
      {
      ServerRegistry serverRegistry = RegistryLocator.getRegistry();
      m_indexer = (IndexerWLRemote) serverRegistry.lookup(
      IndexerWLRemote.SERVICE_NAME);
      }
      catch (NamingException ne)
      {
      throw new GeneralException(GeneralExceptionConstants.COMP_SYSUTIL,
      GeneralExceptionConstants.EX_NAMING, ne);
      }
      }
      return m_indexer;
      }*/

    /*public static LingManagerWLRemote getLingManagerWLRemote()
      throws GeneralException
      {
      if (m_lingManagerWLRemote == null)
      {
      try
      {
      ServerRegistry serverRegistry = RegistryLocator.getRegistry();
      m_lingManagerWLRemote = (LingManagerWLRemote)serverRegistry.lookup(
      LingManagerWLRemote.SERVICE_NAME);
      }
      catch (NamingException ne)
      {
      throw new GeneralException(GeneralExceptionConstants.COMP_SYSUTIL,
      GeneralExceptionConstants.EX_NAMING, ne);
      }
      }
      return m_lingManagerWLRemote;
      }*/

    public static TmManager getTmManager()
        throws GeneralException
    {
        if (m_tmManager == null)
        {
            try
            {
                m_tmManager = (TmManager)
                    SERVER_REGISTRY.lookup(RemoteServer.getServiceName(
                        TmManager.class));
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_tmManager;
    }

    public static SynchronizationManager getSynchronizationManager()
        throws GeneralException
    {
        if (m_synchronizationManager == null)
        {
            try
            {
                m_synchronizationManager = (SynchronizationManager)
                    SERVER_REGISTRY.lookup(RemoteServer.getServiceName(
                        SynchronizationManager.class));
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_synchronizationManager;
    }

    public static OnlineEditorManager getOnlineEditorManagerWLRemote()
        throws GeneralException
    {
        if (m_onlineEditorManager == null)
        {
            try
            {
                m_onlineEditorManager = (OnlineEditorManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        OnlineEditorManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        // dummy code -- use Aswin's factory
        try
        {
            return m_onlineEditorManager.newInstance();
        }
        catch (RemoteException e)
        {
            throw new GeneralException(GeneralExceptionConstants.COMP_SYSUTIL,
                GeneralExceptionConstants.EX_NAMING, e);
        }
    }

    public static WorkflowEventObserverWLRemote getWorkflowEventObserver()
        throws GeneralException
    {
        if (m_workflowEventObserver == null)
        {
            try
            {
                m_workflowEventObserver =
                    (WorkflowEventObserverWLRemote)
                    SERVER_REGISTRY.lookup(
                        WorkflowEventObserverWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_workflowEventObserver;
    }

    public static WorkflowManagerWLRemote getWorkflowManager()
        throws GeneralException
    {
        if (m_workflowManager == null)
        {
            try
            {
                m_workflowManager = (WorkflowManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        WorkflowManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_workflowManager;
    }

    public static DatabaseProfilePersistenceManagerWLRemote getDatabaseProfilePersistenceManager()
        throws GeneralException
    {
        if (m_dbProfileManager == null)
        {
            try
            {
                m_dbProfileManager = (DatabaseProfilePersistenceManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        DatabaseProfilePersistenceManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_dbProfileManager;
    }

    public static DatabaseColumnPersistenceManagerWLRemote getDatabaseColumnPersistenceManager()
        throws GeneralException
    {
        if (m_dbColumnManager == null)
        {
            try
            {
                m_dbColumnManager = (DatabaseColumnPersistenceManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        DatabaseColumnPersistenceManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_dbColumnManager;
    }

    public static PreviewUrlPersistenceManagerWLRemote getPreviewUrlPersistenceManager()
        throws GeneralException
    {
        if (m_previewUrlManager == null)
        {
            try
            {
                m_previewUrlManager = (PreviewUrlPersistenceManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        PreviewUrlPersistenceManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_previewUrlManager;
    }

    public static OfflineEditManager getOfflineEditManager()
        throws GeneralException,
               NamingException
    {
        if (m_offlineEditManager == null)
        {
            m_offlineEditManager = (OfflineEditManagerWLRemote)
                SERVER_REGISTRY.lookup(
                    OfflineEditManagerWLRemote.SERVICE_NAME);
        }
        try
        {
            return m_offlineEditManager.newInstance();
        }
        catch (RemoteException e)
        {
            throw new GeneralException(GeneralExceptionConstants.COMP_SYSUTIL,
                GeneralExceptionConstants.EX_NAMING, e);
        }
    }

    public static SystemParameterPersistenceManager getSystemParameterPersistenceManager()
        throws GeneralException
    {
        if (m_sysParamManager == null)
        {
            try
            {
                m_sysParamManager = (SystemParameterPersistenceManager)
                    SERVER_REGISTRY.lookup(
                        SystemParameterPersistenceManager.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_sysParamManager;
    }

    public static UserParameterPersistenceManager getUserParameterManager()
        throws GeneralException
    {
        if (m_usrParamManager == null)
        {
            try
            {
                m_usrParamManager = (UserParameterPersistenceManager)
                    SERVER_REGISTRY.lookup(
                        UserParameterPersistenceManager.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_usrParamManager;
    }

    public static ImageReplaceFileMapPersistenceManagerWLRemote getImageReplaceFileMapPersistenceManager()
        throws GeneralException
    {
        if (m_imageReplaceMapManager == null)
        {
            try
            {
                m_imageReplaceMapManager = (ImageReplaceFileMapPersistenceManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        ImageReplaceFileMapPersistenceManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_imageReplaceMapManager;
    }

    public static GlossaryManagerWLRemote getGlossaryManager()
        throws GeneralException
    {
        if (m_glossaryManager == null)
        {
            try
            {
                m_glossaryManager = (GlossaryManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        GlossaryManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_glossaryManager;
    }

    public static CommentManagerWLRemote getCommentManager()
        throws GeneralException
    {
        if (m_commentReferenceManager == null)
        {
            try
            {
                m_commentReferenceManager = (CommentManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        CommentManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_commentReferenceManager;
    }

    public static ITermbaseManager getTermbaseManager()
        throws GeneralException
    {
        if (m_termbaseManager == null)
        {
            try
            {
                m_termbaseManager = (ITermbaseManager)
                    SERVER_REGISTRY.lookup(RemoteServer.getServiceName(
                        ITermbaseManager.class));
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_termbaseManager;
    }


    public static ITermbaseScheduler getTermbaseScheduler()
        throws GeneralException
    {
        if (m_termbaseScheduler == null)
        {
            try
            {
                m_termbaseScheduler = (ITermbaseScheduler)
                    SERVER_REGISTRY.lookup(RemoteServer.getServiceName(
                        ITermbaseScheduler.class));
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_termbaseScheduler;
    }


    public static TermLeverageManagerWLRemote getTermLeverageManager()
        throws GeneralException
    {
        if (m_termLeverageManager == null)
        {
            try
            {
                m_termLeverageManager = (TermLeverageManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        TermLeverageManagerWLRemote.SERVICE_NAME);
            }
            catch(NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_termLeverageManager;
    }

    public static EventSchedulerWLRemote getEventScheduler()
        throws GeneralException
    {
        if (m_eventScheduler == null)
        {
            try
            {
                m_eventScheduler = (EventSchedulerWLRemote)
                    SERVER_REGISTRY.lookup(
                        EventSchedulerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }
        return m_eventScheduler;
    }


    public static MailerWLRemote getMailer()
        throws GeneralException
    {
        if (m_mailer == null)
        {
            try
            {
                m_mailer = (MailerWLRemote)
                    SERVER_REGISTRY.lookup(
                        MailerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }
        return m_mailer;
    }

    public static ExportEventObserverWLRemote getExportEventObserver()
        throws GeneralException
    {
        if (m_exportEventObserver == null)
        {
            try
            {
                m_exportEventObserver = (ExportEventObserverWLRemote)
                    SERVER_REGISTRY.lookup(
                        ExportEventObserverWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }
        return m_exportEventObserver;
    }

    public static VendorManagementWLRemote getVendorManagement()
        throws GeneralException
    {
        if (m_vendorManagement == null)
        {
            try
            {
                m_vendorManagement = (VendorManagementWLRemote)
                    SERVER_REGISTRY.lookup(
                        VendorManagementWLRemote.SERVICE_NAME);
          }
          catch (NamingException ne)
          {
              throwException(ne);
          }
      }
       return m_vendorManagement;
    }

    public static NativeFileManagerWLRemote getNativeFileManager()
        throws GeneralException,
               NamingException
    {
        if (m_nativeFileManager == null)
        {
            m_nativeFileManager = (NativeFileManagerWLRemote)
                SERVER_REGISTRY.lookup(
                    NativeFileManagerWLRemote.SERVICE_NAME);
        }

        return m_nativeFileManager;
    }

    /**
     *
     */
    public static SecondaryTargetFileMgrWLRemote getSecondaryTargetFileManager()
        throws GeneralException
    {
        if (m_stfManager == null)
        {
            try
            {
                m_stfManager = (SecondaryTargetFileMgrWLRemote)
                    SERVER_REGISTRY.lookup(
                        SecondaryTargetFileMgrWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_stfManager;
    }

    public static CalendarManagerWLRemote getCalendarManager()
        throws GeneralException
    {
        if (m_calendarManager == null)
        {
            try
            {
                m_calendarManager = (CalendarManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        CalendarManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_calendarManager;
    }

    public static CmsUserManagerWLRemote getCmsUserManager()
        throws GeneralException
    {
        if (m_cmsUserManager == null)
        {
            try
            {
                m_cmsUserManager = (CmsUserManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        CmsUserManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_cmsUserManager;
    }


    public static AlignerManagerWLRemote getAlignerManager()
        throws GeneralException
    {
        if (m_alignerManager == null)
        {
            try
            {
                m_alignerManager = (AlignerManagerWLRemote)
                    SERVER_REGISTRY.lookup(
                        AlignerManagerWLRemote.SERVICE_NAME);
            }
            catch (NamingException ne)
            {
                throwException(ne);
            }
        }

        return m_alignerManager;
    }

    public static DocumentumPersistenceManagerWLRemote getDocumentumPersistenceManager()
    throws GeneralException,
           NamingException,
           RemoteException
    {
        if (m_documentumPersistenceManager == null)
        {
            m_documentumPersistenceManager = (DocumentumPersistenceManagerWLRemote)
                SERVER_REGISTRY.lookup(
                        DocumentumPersistenceManagerWLRemote.SERVICE_NAME);
        }
    
        return m_documentumPersistenceManager;
    }

    public static RSSPersistenceManagerWLRemote getRSSPersistenceManager()
    throws GeneralException
    {
        if (m_rssPersistenceManager == null)
        {
           	try {
				m_rssPersistenceManager = (RSSPersistenceManagerWLRemote)
					SERVER_REGISTRY.lookup(
						RSSPersistenceManagerWLRemote.SERVICE_NAME);
			} catch (NamingException ne) {
				throwException(ne);
			}
        }
    
        return m_rssPersistenceManager;
    }
    
    public static RemoteAccessManagerWLRemote getRemoteAccessManager()
    	throws GeneralException
    {
        if (m_remoteAccessManager == null)
        {
           	try {
           		m_remoteAccessManager = (RemoteAccessManagerWLRemote)
					SERVER_REGISTRY.lookup(
							RemoteAccessManagerWLRemote.SERVICE_NAME);
			} catch (NamingException ne) {
				throwException(ne);
			}        		
        }
    
        return m_remoteAccessManager;
    }

    /////////////////////////////////////////////////////////////////////////
    ///////////////////  END: ServerProxy  interface methods  ///////////////
    /////////////////////////////////////////////////////////////////////////

    /**
     * Get the remote service by the interface class name.<p>
     *
     * @return remote service
     */
    public static Object getService(Class p_class)
        throws GeneralException,
               RemoteException
    {
        Object service = m_services.get(p_class);
        if (service == null)
        {
            try
            {
                service = SERVER_REGISTRY.lookup(
                    RemoteServer.getServiceName(p_class));
                m_services.put(p_class, service);
            }
            catch (NamingException ne)
            {
                throw new GeneralException(GeneralExceptionConstants.COMP_SYSUTIL,
                    GeneralExceptionConstants.EX_NAMING, p_class.toString(), ne);
            }
        }

        return service;
    }

    // PRIVATE METHODS
    private static void throwException(NamingException p_exception)
        throws GeneralException
    {
        CATEGORY.error("Naming exception:",p_exception);
        throw new GeneralException(
                    GeneralExceptionConstants.COMP_SYSUTIL,
                    GeneralExceptionConstants.EX_NAMING, p_exception);
    }
}
