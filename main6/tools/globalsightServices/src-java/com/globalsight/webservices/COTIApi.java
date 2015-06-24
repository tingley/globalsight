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
package com.globalsight.webservices;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisError;
import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.coti.util.COTIConstants;
import com.globalsight.everest.coti.util.COTIDbUtil;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.coti.util.COTIXmlBase;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.ServerUtil;
import com.globalsight.webservices.coti.util.COTISession;
import com.globalsight.webservices.coti.util.COTIUtil;

/**
 * API class for COTI
 * 
 * @author Wayzou
 * 
 */
public class COTIApi
{
    private static final Logger CATEGORY = Logger.getLogger(COTIApi.class
            .getName());
    private static String session_id = "session_id";

    public void init(ServiceContext serviceContext)
    {

    }

    public void destroy(ServiceContext serviceContext)
    {

    }

    public String HelloWorld()
    {
        return "Hello World from Welocalize GlobalSight COTIApi";
    }

    /**
     * SessionService Login
     * 
     * @return
     */
    public void Logout()
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIUtil.logoutSession(msgCtx);
    }

    /**
     * Checks, if the TMS is running and returns meta information about the TMS.
     * 
     * @return
     */
    public VersionInfo GetVersionInfo()
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        String v = ServerUtil.getVersion();
        String cotiVersion = "1.0";
        String cotiLevel = "4";

        VersionInfo vi = new VersionInfo(v, cotiVersion, cotiLevel);

        return vi;
    }

    /**
     * Retrieves meta information about the meta properties the TMS supports.
     * 
     * @return
     */
    public MetadataInfo GetMetadataInfo()
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTISession s = COTIUtil.getSession(msgCtx);
        String companyName = s.getCompanyName();
        Company c = CompanyWrapper.getCompanyByName(companyName);

        // TODO add details after discussion
        MetadataInfo mi = new MetadataInfo();
        
        //Name http://meta.DERCOM.de/COTI/name 
        //Subject http://meta.DERCOM.de/COTI/subject 
        //Source language http://meta.DERCOM.de/COTI/source_language 
        //Target language http://meta.DERCOM.de/COTI/target_language 
        //Workflow http://meta.DERCOM.de/COTI/workflow 
        //Report type http://meta.DERCOM.de/COTI/report_type
        //Translator http://meta.DERCOM.de/COTI/translator 
        //Project manager http://meta.DERCOM.de/COTI/manager
        MetaProperty[] metaProperties = new MetaProperty[5];
        MetaProperty mp;
        Entry entry;
        Entry[] entries;
        
        mp = new MetaProperty();
        mp.setUri("http://meta.DERCOM.de/COTI/source_language");
        mp.setType("string");
        mp.setLabel("source language");
        mp.setMandatory(true);
        entries = new Entry[3];
        entry = new Entry();
        entry.setKey("de-DE");
        entry.setLabel("German (Germany)");
        entries[0] = entry;
        entry = new Entry();
        entry.setKey("en-US");
        entry.setLabel("English (United States)");
        entries[1] = entry;
        entry = new Entry();
        entry.setKey("it-IT");
        entry.setLabel(" Italian (Italy)");
        entries[2] = entry;
        mp.setDomain(entries);
        metaProperties[0] = mp;
        
        mp = new MetaProperty();
        mp.setUri("http://meta.DERCOM.de/COTI/target_language");
        mp.setType("string");
        mp.setLabel("target language");
        mp.setMandatory(true);
        entries = new Entry[3];
        entry = new Entry();
        entry.setKey("ru-RU");
        entry.setLabel("Russian (Russia)");
        entries[0] = entry;
        entry = new Entry();
        entry.setKey("en-US");
        entry.setLabel("English (United States)");
        entries[1] = entry;
        entry = new Entry();
        entry.setKey("it-IT");
        entry.setLabel(" Italian (Italy)");
        entries[2] = entry;
        mp.setDomain(entries);
        metaProperties[1] = mp;
        
        mp = new MetaProperty();
        mp.setUri("http://meta.DERCOM.de/COTI/workflow");
        mp.setType("string");
        mp.setLabel("workflow");
        mp.setMandatory(false);
        entries = new Entry[2];
        entry = new Entry();
        entry.setKey("translation");
        entry.setLabel("translation");
        entries[0] = entry;
        entry = new Entry();
        entry.setKey("pretranslation");
        entry.setLabel("pretranslation");
        entries[1] = entry;
        mp.setDomain(entries);
        metaProperties[2] = mp;
        
        mp = new MetaProperty();
        mp.setUri("http://meta.DERCOM.de/COTI/report_type");
        mp.setType("string");
        mp.setLabel("Report-Typ");
        mp.setMandatory(false);
        entries = new Entry[2];
        entry = new Entry();
        entry.setKey("translationStatus");
        entry.setLabel("translationStatus");
        entries[0] = entry;
        entry = new Entry();
        entry.setKey("costEstimation");
        entry.setLabel("costEstimation");
        entries[1] = entry;
        mp.setDomain(entries);
        metaProperties[3] = mp;
        
        mp = new MetaProperty();
        mp.setUri("http://meta.DERCOM.de/COTI/translator");
        mp.setType("string");
        mp.setLabel("translator");
        mp.setMandatory(false);
        entries = new Entry[2];
        entry = new Entry();
        entry.setKey("kim");
        entry.setLabel("Kim");
        entries[0] = entry;
        entry = new Entry();
        entry.setKey("peter");
        entry.setLabel("Peter");
        entries[1] = entry;
        mp.setDomain(entries);
        metaProperties[4] = mp;

        mi.setMetaProperties(metaProperties);
        return mi;
    }

    /**
     * Retrieves the current status of a translation project or a single
     * document.
     * 
     * @param objType
     * @param objId
     * @return
     */
    public StatusInfo GetStatusInfo(String objType, String objId)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        StatusInfo si = new StatusInfo();
        String status = "In-Progress";

        // get document status
        if (objType.equals(COTIConstants.ObjType_Document))
        {
            COTIProject cproject = null;
            COTIDocument cdoc = null;

            try
            {
                long id = Long.parseLong(objId);
                cdoc = COTIDbUtil.getCOTIDocument(id);
                cproject = COTIDbUtil.getCOTIProject(cdoc.getProjectId());
            }
            catch (Exception e)
            {
                String msg = "Cannot get project/package infor by projectId "
                        + objId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            TargetPage tp = COTIUtilEnvoy.getTargetPageByCOTIDocument(cproject,
                    cdoc);

            if (tp == null)
            {
                status = cproject.getStatus();
            }
            else
            {
                status = tp.getWorkflowInstance().getState();
            }
        }
        // get project status
        else if (objType.equals(COTIConstants.ObjType_Project))
        {
            COTIProject cproject = null;
            COTIPackage cpackage = null;

            try
            {
                long pid = Long.parseLong(objId);
                cproject = COTIDbUtil.getCOTIProject(pid);
                cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
            }
            catch (Exception e)
            {
                String msg = "Cannot get project/package infor by projectId "
                        + objId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            Job gsjob = COTIDbUtil.getGlobalSightJob(cproject);

            if (gsjob == null)
            {
                status = cproject.getStatus();
            }
            else
            {
                status = gsjob.getState();
            }
        }

        si.setStatusValue(status);

        return si;
    }

    /***
     * Retrieves the current translation progress for a translation project or a
     * single document.
     * 
     * @param objType
     * @param objId
     * @return
     */
    public StatusInfo GetProgressInfo(String objType, String objId)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        StatusInfo si = new StatusInfo();
        String status = "95.785";

        // get document status
        if (objType.equals(COTIConstants.ObjType_Document))
        {
            COTIProject cproject = null;
            COTIDocument cdoc = null;

            try
            {
                long id = Long.parseLong(objId);
                cdoc = COTIDbUtil.getCOTIDocument(id);
                cproject = COTIDbUtil.getCOTIProject(cdoc.getProjectId());
            }
            catch (Exception e)
            {
                String msg = "Cannot get project/package infor by projectId "
                        + objId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            TargetPage tp = COTIUtilEnvoy.getTargetPageByCOTIDocument(cproject,
                    cdoc);

            if (tp == null)
            {
                status = "0";
            }
            else
            {
                status = ""
                        + tp.getWorkflowInstance().getPercentageCompletion();
            }
        }
        // get project status
        else if (objType.equals(COTIConstants.ObjType_Project))
        {
            COTIProject cproject = null;

            try
            {
                long pid = Long.parseLong(objId);
                cproject = COTIDbUtil.getCOTIProject(pid);
            }
            catch (Exception e)
            {
                String msg = "Cannot get project/package infor by projectId "
                        + objId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            Job gsjob = COTIDbUtil.getGlobalSightJob(cproject);

            // Currently one COTI job contain only one workflow
            if (gsjob == null)
            {
                status = "0";
            }
            else
            {
                Workflow wf = gsjob.getWorkflows().iterator().next();
                status = "" + wf.getPercentageCompletion();
            }
        }

        si.setStatusValue(status);

        return si;
    }

    /**
     * Triggers the generation of a report for an existing translation project
     * or document. The report generation process is executed asynchronously and
     * the caller will have to w ait until she receives a ReceiveReport message
     * with the report id that has been returned by this method.
     * 
     * @param objType
     * @param objId
     * @param reportType
     * @return
     */
    public String RequestReport(String objType, String objId, String reportType)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        // get document status
        if (objType.equals(COTIConstants.ObjType_Document))
        {
            COTIProject cproject = null;
            COTIDocument cdoc = null;

            try
            {
                long id = Long.parseLong(objId);
                cdoc = COTIDbUtil.getCOTIDocument(id);
                cproject = COTIDbUtil.getCOTIProject(cdoc.getProjectId());
            }
            catch (Exception e)
            {
                String msg = "Cannot get project/package infor by document id "
                        + objId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            TargetPage tp = COTIUtilEnvoy.getTargetPageByCOTIDocument(cproject,
                    cdoc);
            if (tp == null)
            {
                return "N/A";
            }
            else if (reportType.equals(COTIConstants.reportType_costEstimation))
            {
                if (tp.getWorkflowInstance().getJob().hasSetCostCenter())
                {
                    Job gsjob = tp.getWorkflowInstance().getJob();

                    Currency oCurrency;
                    try
                    {
                        oCurrency = ServerProxy.getCostingEngine()
                                .getPivotCurrency();
                        Cost cost = ServerProxy.getCostingEngine()
                                .calculateCost(gsjob, oCurrency, true,
                                        Cost.EXPENSE);

                        return cost.getEstimatedCost().getFormattedAmount();
                    }
                    catch (Exception e)
                    {
                        String msg = "Cannot get costing information by document id "
                                + objId;
                        CATEGORY.error(msg, e);
                        throw new AxisError(msg, e);
                    }
                }
                else
                {
                    return "N/A";
                }
            }
            else if (reportType
                    .equals(COTIConstants.reportType_translationStatus))
            {
                Workflow wf = tp.getWorkflowInstance();
                return "" + wf.getPercentageCompletion();
            }
        }
        // get project status
        else if (objType.equals(COTIConstants.ObjType_Project))
        {
            COTIProject cproject = null;

            try
            {
                long pid = Long.parseLong(objId);
                cproject = COTIDbUtil.getCOTIProject(pid);
            }
            catch (Exception e)
            {
                String msg = "Cannot get project/package infor by projectId "
                        + objId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            Job gsjob = COTIDbUtil.getGlobalSightJob(cproject);

            if (gsjob == null)
            {
                return "N/A";
            }
            else if (reportType.equals(COTIConstants.reportType_costEstimation))
            {
                if (gsjob.hasSetCostCenter())
                {
                    try
                    {
                        Currency oCurrency = ServerProxy.getCostingEngine()
                                .getPivotCurrency();
                        Cost cost = ServerProxy.getCostingEngine()
                                .calculateCost(gsjob, oCurrency, true,
                                        Cost.EXPENSE);

                        return cost.getEstimatedCost().getFormattedAmount();
                    }
                    catch (Exception e)
                    {
                        String msg = "Cannot get costing information by project id "
                                + objId;
                        CATEGORY.error(msg, e);
                        throw new AxisError(msg, e);
                    }
                }
                else
                {
                    return "N/A";
                }
            }
            else if (reportType
                    .equals(COTIConstants.reportType_translationStatus))
            {
                Workflow wf = gsjob.getWorkflows().iterator().next();
                return "" + wf.getPercentageCompletion();
            }
        }

        return null;
    }

    /**
     * Create a new translation project using the information provided in the
     * given COTI.xml file.
     * 
     * @param cotiFile
     * @return
     */
    // translation-COTI-specification_2009-11-13T10:39:35Z.coti
    // /4711_de-DE_en-GB/
    // /COTI.xml
    // /translation files/
    // /reference files/
    // /4711_de-DE_ru-RU/
    // /COTI.xml
    // /translation files/
    // /reference files/
    public String CreateProject(byte[] cotiFile)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTISession s = COTIUtil.getSession(msgCtx);
        String companyName = s.getCompanyName();
        Company c = CompanyWrapper.getCompanyByName(companyName);
        String cotiXml = null;

        try
        {
            cotiXml = COTIUtilEnvoy.readOutCotiXml(cotiFile);
        }
        catch (Exception e)
        {
            CATEGORY.error("Cannot create temp file for COTI.xml", e);
            throw new AxisError("Cannot create temp file for COTI.xml", e);
        }

        COTIProject cproject = null;
        try
        {
            cproject = COTIUtilEnvoy.createCOTIProject(c, cotiXml);
        }
        catch (Exception e)
        {
            CATEGORY.error("Cannot save information from COTI.xml", e);
            throw new AxisError("Cannot save information from COTI.xml", e);
        }

        try
        {
            HibernateUtil.getSession().close();
        }
        catch (Exception ex)
        {
            // ignore this
        }

        return "" + cproject.getId();
    }

    /**
     * Update the definition of an existing translation project using the
     * information provided in the given COTI.xml file. This operation is only
     * available for translation projects in status <b> created or finished
     * </b>.
     * 
     * @param projectId
     * @param cotiFile
     */
    public void UpdateProject(String projectId, byte[] cotiFile)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long pid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(pid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        if (cproject != null && cpackage != null)
        {
            String cotiXml = null;

            try
            {
                cotiXml = COTIUtilEnvoy.readOutCotiXml(cotiFile);
            }
            catch (Exception e)
            {
                CATEGORY.error("Cannot create temp file for COTI.xml", e);
                throw new AxisError("Cannot create temp file for COTI.xml", e);
            }

            try
            {
                long cid = cpackage.getCompanyId();
                COTIXmlBase cx = COTIXmlBase.getInstance(cotiXml);
                cx.setCompanyId(cid);

                // create coti package & project
                COTIPackage newcpa = cx.createPackage();
                COTIProject newcproject = cx.createProject();

                cpackage.setCreationDate(new Date());
                cpackage.setFileName(newcpa.getFileName());
                cpackage.setCotiProjectName(newcpa.getCotiProjectName());
                cpackage.setCotiProjectTimestamp(newcpa
                        .getCotiProjectTimestamp());

                cproject.setDirName(newcproject.getDirName());
                cproject.setCotiProjectId(newcproject.getCotiProjectId());
                cproject.setCotiProjectName(newcproject.getCotiProjectName());
                cproject.setSourceLang(newcproject.getSourceLang());
                cproject.setTargetLang(newcproject.getTargetLang());

                COTIDbUtil.update(cpackage);
                COTIDbUtil.update(cproject);

                COTIUtilEnvoy.saveCotiXml(cid, cpackage, cproject, cotiXml);
            }
            catch (Exception e)
            {
                CATEGORY.error("Cannot update information from COTI.xml", e);
                throw new AxisError("Cannot update information from COTI.xml",
                        e);
            }
        }
    }

    /**
     * Retrieve the COTI.xml file of an existing translation project.
     * 
     * @param projectId
     * @return
     */
    public byte[] GetProject(String projectId)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long pid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(pid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        if (cproject != null && cpackage != null)
        {
            try
            {
                String cotiXmlPath = COTIUtilEnvoy.getCotiXmlPath(
                        cpackage.getCompanyId(), cpackage, cproject);
                File f = new File(cotiXmlPath);
                byte[] result = COTIUtilEnvoy.zipAndReadData(f);

                return result;
            }
            catch (IOException e)
            {
                String msg = "Cannot get project/package infor by projectId "
                        + projectId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }
        }
        else
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg);
            throw new AxisError(msg);
        }
    }

    /**
     * Cancel an existing translation project and set its status to <b>
     * cancelled </b>.
     * 
     * @param projectId
     */
    public void CancelProject(String projectId)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long pid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(pid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        cproject.setStatus(COTIConstants.project_status_cancelled);
        try
        {
            COTIDbUtil.update(cproject);
        }
        catch (Exception e)
        {
            String msg = "Update status failed information by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }
        // cancel project in globalsight
        Job gsjob = COTIDbUtil.getGlobalSightJob(cproject);
        try
        {
            if (gsjob != null)
            {
                JobHandler jh = COTIUtilEnvoy.getJobHandler();
                if (jh != null)
                {
                    jh.cancelJob(gsjob);
                }
            }
        }
        catch (Exception e)
        {
            String msg = "Cancel job with exception, job : " + gsjob.toString();
            CATEGORY.error(msg, e);
        }
    }

    /**
     * Start the execution of an existing translation. This operation signals
     * the TMS, that the project definition is complete (i.e. all documents
     * listed in the COTI file have been uploaded) and that the execution of the
     * given workflow may be triggered by the TMS. This method can also be used
     * to start a subsequent workflow, when the execution of a workflow has
     * finished (e.g. start translation process after the pre-translation
     * workflow). The status of the project must be either <b> created or
     * finished </b>. Upon successful completion of this operation, the status
     * value of the translation project is set to started.
     * 
     * @param projectId
     * @param workflow
     */
    public void StartProject(String projectId, String workflow)
    {
        // mark this project as finished to tell GlobalSight start this project
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long pid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(pid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        // check project status
        String oriStatus = cproject.getStatus();
        if (COTIConstants.project_status_created.equals(oriStatus)
                || COTIConstants.project_status_finished.equals(oriStatus))
        {
            cproject.setStatus(COTIConstants.project_status_started);
            try
            {
                COTIDbUtil.update(cproject);
            }
            catch (Exception e)
            {
                String msg = "Update status failed information by projectId "
                        + projectId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            // user create GlobalSight job in Create COTI job UI
        }
        else
        {
            throw new AxisError(
                    "The status of the project must be either created or finished");
        }
    }

    /**
     * This operation signals the TMS, that an existing translation project is
     * no longer required by the caller, i.e. no further API calls regarding
     * this project will be made. The status of the project must be either <b>
     * created or finished </b>. It is up to the TMS, if the project data is
     * archived, deleted or if the project is only marked as being closed.
     * 
     * @param projectId
     */
    public void CloseProject(String projectId)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long pid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(pid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        // check project status
        String oriStatus = cproject.getStatus();
        if (COTIConstants.project_status_created.equals(oriStatus)
                || COTIConstants.project_status_finished.equals(oriStatus))
        {
            cproject.setStatus(COTIConstants.project_status_closed);
            try
            {
                COTIDbUtil.update(cproject);
            }
            catch (Exception e)
            {
                String msg = "Update status failed information by projectId "
                        + projectId;
                CATEGORY.error(msg, e);
                throw new AxisError(msg, e);
            }

            // close project in GlobalSight
            Job gsjob = COTIDbUtil.getGlobalSightJob(cproject);
            try
            {
                if (gsjob != null)
                {
                    JobHandler jh = COTIUtilEnvoy.getJobHandler();
                    if (jh != null)
                    {
                        jh.archiveJob(gsjob);
                    }
                }
            }
            catch (Exception e)
            {
                String msg = "Cancel job with exception, job : "
                        + gsjob.toString();
                CATEGORY.error(msg, e);
            }
        }
        else
        {
            throw new AxisError(
                    "The status of the project must be either created or finished");
        }
    }

    /**
     * Uploads the content of a document as part of a translation project. The
     * project specified by its id must be an already existing project in status
     * created. The value of the fileRef parameter must correspond to one of the
     * file definitions (i.e. the value of its file-ref attribute) specified in
     * the COTI.xml of the project. The method returns a document id that can be
     * used for querying the status or translation progress of this document.
     * This method can also be used to update a previously uploaded document, as
     * long as the associated project is still in status created. In this case,
     * this method will return the same document id as the initial upload of the
     * document. For the latter scenario an additional variant of this method is
     * provided that accepts the id of the document to be replaced.
     * 
     * @param projectId
     * @param fileType
     * @param fileRef
     * @param content
     * @return
     */
    public String UploadDocument(String projectId, String fileType,
            String fileRef, byte[] content)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long pid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(pid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get project/package infor by projectId "
                    + projectId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        // check project status
        String oriStatus = cproject.getStatus();
        if (!COTIConstants.project_status_created.equals(oriStatus))
        {
            throw new AxisError(
                    "The status of the project must be created for UploadDocument");
        }

        String path = COTIUtilEnvoy.getCotiDocumentPath(
                cpackage.getCompanyId(), cpackage, cproject, fileType, fileRef);
        try
        {
            File ff = COTIUtilEnvoy.unzipAndGetFirstFile(content);
            File dst = new File(path);

            if (dst.exists())
            {
                dst.delete();
            }

            FileUtil.copyFile(ff, dst);
        }
        catch (Exception e)
        {
            String msg = "Write document to harddist with error.";
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        return COTIDbUtil.getDocumentId(projectId, fileType, fileRef);
    }

    /**
     * This method can also be used to update a previously uploaded document, as
     * long as the associated project is still in status created. In this case,
     * this method will return the same document id as the initial upload of the
     * document.
     * 
     * @param documentId
     * @param content
     * @return
     */
    public String UploadDocumentById(String documentId, byte[] content)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIDocument cdoc = null;
        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long did = Long.parseLong(documentId);
            cdoc = COTIDbUtil.getCOTIDocument(did);
            cproject = COTIDbUtil.getCOTIProject(cdoc.getProjectId());
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get document infor by documentId "
                    + documentId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        // check project status
        String oriStatus = cproject.getStatus();
        if (!COTIConstants.project_status_created.equals(oriStatus))
        {
            throw new AxisError(
                    "The status of the project must be created for UploadDocument");
        }

        String path = COTIUtilEnvoy.getCotiDocumentPath(
                cpackage.getCompanyId(), cpackage, cproject, cdoc);
        try
        {
            File ff = COTIUtilEnvoy.unzipAndGetFirstFile(content);
            File dst = new File(path);

            if (dst.exists())
            {
                dst.delete();
            }

            FileUtil.copyFile(ff, dst);
        }
        catch (Exception e)
        {
            String msg = "Write document to harddist with error.";
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        return documentId;
    }

    /**
     * Retrieve the content of a document specified by its id.
     * 
     * @param documentId
     * @return
     */
    public byte[] DownloadDocument(String documentId)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        COTIDocument cdoc = null;
        COTIProject cproject = null;
        COTIPackage cpackage = null;

        try
        {
            long did = Long.parseLong(documentId);
            cdoc = COTIDbUtil.getCOTIDocument(did);
            cproject = COTIDbUtil.getCOTIProject(cdoc.getProjectId());
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
        }
        catch (Exception e)
        {
            String msg = "Cannot get document infor by documentId "
                    + documentId;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        byte[] result = null;
        File f = COTIUtilEnvoy.getExportedDocument(cpackage, cproject, cdoc);
        try
        {
            result = COTIUtilEnvoy.zipAndReadData(f);
        }
        catch (IOException e)
        {
            String msg = "Cannot read document data by path " + f;
            CATEGORY.error(msg, e);
            throw new AxisError(msg, e);
        }

        return result;
    }

    /**
     * Get the document id by its information
     * 
     * @param projectId
     * @param fileType
     * @param fileRef
     * @return
     */
    public String GetDocumentId(String projectId, String fileType,
            String fileRef)
    {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        COTIUtil.checkSession(msgCtx);

        return COTIDbUtil.getDocumentId(projectId, fileType, fileRef);
    }

}
