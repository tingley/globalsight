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

package com.globalsight.everest.request;

// java core classes
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This is the concrete implementation of the Request interface.
 */
public class RequestImpl extends PersistentObject implements Request,
        Serializable
{
    private static final long serialVersionUID = -6730612010738702678L;

    // static class variables
    private static Logger c_logger = Logger.getLogger(RequestImpl.class
            .getName());

    // constants used for TOPLink queries - should match the values below
    public static final String ID = M_ID;
    public static final String PROFILE = "m_l10nProfile";
    public static final String SOURCE_PAGE = "m_sourcePage";
    public static final String BATCH_INFO = "m_batchInfo";
    public static final String COMPANY_ID = "m_companyId";

    private static HashMap TYPES = new HashMap();
    static
    {
        TYPES.put(new Integer(-2), "REQUEST_WITH_IMPORT_ERROR");
        TYPES.put(new Integer(-1), "REQUEST_WITH_CXE_ERROR");
        TYPES.put(new Integer(1), "EXTRACTED_LOCALIZATION_REQUEST");
        TYPES.put(new Integer(2), "UNEXTRACTED_LOCALIZATION_REQUEST");
    }

    // id of the company which this activity belong to
    private long m_companyId;

    //
    // private data members
    //

    // the type of request - see Request interface for const types
    private int m_type;

    // unique page identifier
    private String m_externalPageId;

    private L10nProfile m_l10nProfile = null;
    private String m_gxml;
    private String m_originalSourceEncoding;
    private String m_dataSourceType;
    // The unique id of the data source the request came from.
    private long m_dataSourceId;
    private String m_eventFlowXml;
    // is CXE set up to preview the page.
    private boolean m_isPageCxePreviewable = false;
    private GeneralException m_exception = null;
    private SourcePage m_sourcePage = null; // NOTE: This is not persisted - has
    // to be
    // set in the code to retrieve.
    private BatchInfo m_batchInfo = null;
    private String m_baseHref;
    // the Job the request is associated with
    private Job m_job = null;

    private String m_priority = null;

    private long m_sourcePageId = -1;

    // This hashtable holds any target locales this request/page
    // is already active in. This is used during importing when
    // the REIMPORT_WITH_NEW_TARGETS option is turned on.
    // Any target locales that this request's page is active in
    // will be stored in this hashtable and the information used
    // during importing.
    // The key is the target locale (as a GlobalSightLocale from the cache)
    // The value is the job id that the target is active in (as a Long).
    private Hashtable m_activeTargets = new Hashtable();
    private List m_unimportTargets = new ArrayList();

    /**
     * filename containing the original source file content this field is not
     * persisted
     */
    private String m_originalSourceFileContent = null;

    /**
     * Default constructor. It is intended to be used by TopLink
     */
    public RequestImpl()
    {
    }

    /**
     * Creates a valid localization request LOCALIZATION_REQUEST type. Package
     * specific - called by the RequestFactory.
     * 
     * @param p_l10nProfile
     *            - The localization profile that is associated with the
     *            request.
     * @param p_gxml
     *            The content to be translated
     * @param p_eventFlowXml
     *            The event flow to send back to CXE on export.
     */
    RequestImpl(L10nProfile p_l10nProfile, String p_gxml,
            String p_eventFlowXml, int p_type)
    {
        m_type = p_type;
        m_l10nProfile = p_l10nProfile;
        m_gxml = p_gxml;
        m_eventFlowXml = p_eventFlowXml;
        m_companyId = p_l10nProfile.getCompanyId();
    }

    /**
     * Creates a CXE error localization request REQUEST_WITH_CXE_ERROR type.
     * Package specific - called by the RequestFactory.
     * 
     * @param p_l10nProfile
     *            - The localization profile associated with this request.
     * @param p_gxml
     *            The content to be translated
     * @param p_eventFlowXml
     *            The event flow to send back to CXE on export.
     * @param p_exceptionId
     *            The exception Id associated with the error.
     */
    RequestImpl(L10nProfile p_l10nProfile, String p_gxml,
            String p_eventFlowXml, GeneralException p_exception)
    {
        m_type = REQUEST_WITH_CXE_ERROR;
        m_l10nProfile = p_l10nProfile;
        m_gxml = p_gxml;
        m_eventFlowXml = p_eventFlowXml;
        m_exception = p_exception;
        m_companyId = p_l10nProfile.getCompanyId();
    }

    /**
     * @see Request.getDataSourceId()
     */
    public long getDataSourceId()
    {
        return m_dataSourceId;
    }

    /**
     * @see Request.getDataSourceType()
     */
    public String getDataSourceType()
    {
        return m_dataSourceType;
    }

    /**
     * @see Request.getDataSourceName()
     */
    public String getDataSourceName() throws RequestHandlerException
    {
        // since the data source name can be changed - must query for
        // it can't store the name locally

        // remote calls shouldn't be done in the entity object pass of
        // to RequestHandler to do this. So it does possibly do one
        // remote call.
        RequestHandler rh = null;
        String dataSourceName = null;

        try
        {
            rh = ServerProxy.getRequestHandler();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the RequestHandler", ge);
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_FIND_REQUEST_HANDLER,
                    null, ge);
        }
        try
        {
            dataSourceName = rh.getDataSourceNameOfRequest(this);
        }
        catch (RemoteException re)
        {
            c_logger.error("Couldn't get the data source name.", re);
            String args[] = new String[2];
            args[0] = Long.toString(this.getDataSourceId());
            args[1] = Long.toString(this.getId());
            throw new RequestHandlerException(
                    RequestHandlerException.MSG_FAILED_TO_GET_DATA_SOURCE_NAME,
                    null, re);
        }

        return dataSourceName;
    }

    /**
     * @see Request.getEventFlowXml()
     */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /**
     * @see Request.getException()
     */
    public GeneralException getException()
    {
        return m_exception;
    }

    /**
     * @see Request.getExceptionAsString
     */
    public String getExceptionAsString()
    {
        String expString = new String();

        if (m_exception != null)
        {
            try
            {
                expString = m_exception.serialize();
            }
            catch (GeneralException ge)
            {
                c_logger.error("Failed to serialize the exception of request "
                        + this.getId(), ge);
            }
        }

        return expString;
    }

    /**
     * @see Request.setBaseHref(String)
     */
    public void setBaseHref(String p_baseHref)
    {
        m_baseHref = p_baseHref;
    }

    /**
     * @see Request.getBaseHref()
     */
    public String getBaseHref()
    {
        return m_baseHref;
    }

    /**
     * @see Request.getExternalPageId()
     */
    public String getExternalPageId()
    {
        return m_externalPageId;
    }

    /**
     * @see Request.getGxml()
     */
    public String getGxml()
    {
        return m_gxml;
    }

    /**
     * @see Request.getBatchInfo()
     */
    public BatchInfo getBatchInfo()
    {
        return m_batchInfo;
    }

    /**
     * @see Request.getL10nProfile()
     */
    public L10nProfile getL10nProfile()
    {
        return m_l10nProfile;
    }

    /**
     * @see Request.getSourceEncoding()
     */
    public String getSourceEncoding()
    {
        return m_originalSourceEncoding;
    }

    /**
     * @see Request.getPage()
     */
    public SourcePage getSourcePage()
    {
        return m_sourcePage;
    }

    /**
     * This is a hack to have the page id available when removing failed
     * requests and failed source pages. Do not rely on this method unless you
     * know what you do.
     */
    public long getSourcePageId()
    {
        return m_sourcePageId;
    }

    public Long getPageId()
    {
        Long id = null;

        if (m_sourcePageId != -1)
        {
            id = new Long(m_sourcePageId);
        }
        return id;
    }

    /**
     * This is a hack to let JobCreationQuery.getRequestListByJobId() fill in
     * the source page id.
     */
    public void setSourcePageId(long p_id)
    {
        m_sourcePageId = p_id;
    }

    public void setPageId(Long p_id)
    {
        setSourcePageId(p_id);
    }

    public void setSourcePageId(Long p_id)
    {
        if (p_id == null)
        {
            m_sourcePageId = -1;
        }
        else
        {
            m_sourcePageId = p_id.longValue();
        }
    }

    /**
     * @see Request.getType()
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * @see Request.setJob()
     */
    public void setJob(Job p_job)
    {
        m_job = p_job;
    }

    /**
     * Returns the job that this request is associated with. Could be null if it
     * hasn't been assigned to a job yet.
     */
    public Job getJob()
    {
        return m_job;
    }

    /**
     * @see Request.getWorkflowTemplateId()
     */
    public long getWorkflowTemplateId(GlobalSightLocale p_targetLocale)
            throws RequestHandlerException
    {
        // get the L10nProfile and find the workflow template id
        L10nProfile p = getL10nProfile();
        long templateId = p.getWorkflowTemplateInfo(p_targetLocale)
                .getWorkflowTemplateId();

        return templateId;
    }

    /**
     * @see Request.isPageCxePreviewable()
     */
    public boolean isPageCxePreviewable()
    {
        return m_isPageCxePreviewable;
    }

    public boolean getIsPageCxePreviewable()
    {
        return m_isPageCxePreviewable;
    }

    /**
     * Set the unique id of the data source this request came from.
     * 
     * @param p_id
     *            The unique id that maps to a particular datasource.
     */
    public void setDataSourceId(long p_id)
    {
        m_dataSourceId = p_id;
    }

    /**
     * @see Request.setDataSourceType(String)
     */
    public void setDataSourceType(String p_type) throws RequestHandlerException
    {
        if (m_sourcePage == null)
        {
            m_dataSourceType = p_type;
        }
        else
        {
            // if the types aren't equal - shouldn't allow to set
            if (!p_type.equals(m_sourcePage.getDataSourceType()))
            {
                // takes in three arguments - request id (may be 0),
                // external page id, attribute that can't be updated (as
                // string)
                String[] args = new String[3];
                args[0] = Long.toString(this.getId());
                args[1] = this.getExternalPageId();
                args[2] = p_type;
                throw new RequestHandlerException(
                        RequestHandlerException.MSG_PAGE_ATTRIBUTE_CAN_NOT_BE_UPDATED,
                        args, null);
            }
        }
    }

    /**
     * Set the batch information for this request
     * 
     * @param p_batchInfo
     *            Can set the batch information or null it out.
     */
    public void setBatchInfo(BatchInfo p_batchInfo)
    {
        m_batchInfo = p_batchInfo;
    }

    /**
     * Sets the Event Flow XML associated with this request. This information is
     * needed by CXE when the request is finished being localized and must be
     * exported to CXE.
     * 
     * @param p_eventFlow
     *            The EventFlowXML CXE needs.
     */
    public void setEventFlowXml(String p_eventFlow)
    {
        m_eventFlowXml = p_eventFlow;
    }

    /**
     * @see Request.setException(String)
     */
    public void setException(String p_exceptionXml)
            throws RequestHandlerException
    {
        if (p_exceptionXml != null && p_exceptionXml.length() > 0)
        {
            try
            {
                GeneralException exception = GeneralException
                        .deserialize(p_exceptionXml);

                setException(exception);
            }
            catch (GeneralException ge)
            {
                c_logger.error("Failed to deserialize the "
                        + "exception xml to add to request " + this.getId(), ge);
                String[] args = new String[3];
                args[0] = Long.toString(this.getId());
                args[1] = this.getExternalPageId();
                args[2] = p_exceptionXml;
                throw new RequestHandlerException(
                        RequestHandlerException.MSG_PAGE_ATTRIBUTE_CAN_NOT_BE_UPDATED,
                        args, ge);
            }
        }
    }

    public void setExceptionAsString(String p_exceptionXml)
            throws RequestHandlerException
    {
        setException(p_exceptionXml);
    }

    /**
     * @see Request.setException(GeneralException p_exception)
     */
    public void setException(GeneralException p_exception)
    {
        m_exception = p_exception;

        // if set to a good request -need to set to a bad one
        if (m_type == EXTRACTED_LOCALIZATION_REQUEST
                || m_type == UNEXTRACTED_LOCALIZATION_REQUEST)
        {
            m_type = REQUEST_WITH_IMPORT_ERROR;

            if (c_logger.isDebugEnabled())
            {
                c_logger.debug("Seting the request type to IMPORT ERROR.");
            }
        }
    }

    /**
     * @see Request.setExternalPageId(String)
     */
    public void setExternalPageId(String p_name) throws RequestHandlerException
    {
        if (m_sourcePage == null)
        {
            // this is the unique name of the page
            m_externalPageId = p_name;
        }
        else
        {
            // if the names are different
            if (!m_sourcePage.getExternalPageId().equals(p_name))
            {
                // takes in three arguments - request id (may be 0),
                // external page id, attribute that can't be updated (as
                // string)
                String[] args = new String[3];
                args[0] = Long.toString(this.getId());
                args[1] = this.getExternalPageId();
                args[2] = p_name;
                throw new RequestHandlerException(
                        RequestHandlerException.MSG_PAGE_ATTRIBUTE_CAN_NOT_BE_UPDATED,
                        args, null);
            }
        }
    }

    /**
     * Set the GXML. This is the content that is being requested to be
     * "localized". NOTE: If this method is called after the source page is
     * created, the source page will not be recreated based on the GXML value.
     * 
     * @param p_gxml
     *            The GXML or PRSXML to translate.
     */
    public void setGxml(String p_content)
    {
        m_gxml = p_content;
    }

    /**
     * Set it the page is previewable by CXE or not.
     * 
     * @param p_isPreviewable
     *            'true' - the page is previewable by CXE. 'false' - the page
     *            isn't previewable.
     * @exception RequestHandlerException
     *                Component exception
     */
    public void setPageCxePreviewable(boolean p_isPreviewable)
    {
        m_isPageCxePreviewable = p_isPreviewable;
    }

    public void setIsPageCxePreviewable(boolean p_isPreviewable)
    {
        m_isPageCxePreviewable = p_isPreviewable;
    }

    /**
     * Set the encoding of the request. If the page has already been created,
     * this can't be updated.
     * 
     * @param p_codeSet
     *            The source data's encoding.
     * 
     * @exception An
     *                exception is thrown if the page has already been created.
     *                This can't be set after the page has already been created.
     */
    public void setSourceEncoding(String p_charSet)
            throws RequestHandlerException
    {
        if (m_sourcePage == null)
        {
            m_originalSourceEncoding = p_charSet;
        }
        else
        {
            // if they aren't equal
            ExtractedSourceFile esf = getExtractedSourceFile(m_sourcePage);
            if (esf != null && (!esf.getOriginalCodeSet().equals(p_charSet)))
            {
                // takes in three arguments - request id (may be 0),
                // external page id, attribute that can't be updated (as
                // string)
                String[] args = new String[3];
                args[0] = Long.toString(this.getId());
                args[1] = this.getExternalPageId();
                args[2] = p_charSet;
                throw new RequestHandlerException(
                        RequestHandlerException.MSG_PAGE_ATTRIBUTE_CAN_NOT_BE_UPDATED,
                        args, null);
            }
        }
    }

    /**
     * @see Request.setSourcePage(SourcePage)
     */
    public void setSourcePage(SourcePage p_page)
    {
        m_sourcePage = p_page;
        // set the values that are stored in request that
        // are also in source page
        m_externalPageId = p_page.getExternalPageId();
        m_dataSourceType = p_page.getDataSourceType();
        if (p_page.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
        {
            m_originalSourceEncoding = getExtractedSourceFile(p_page)
                    .getOriginalCodeSet();
        }

        setSourcePageId(p_page.getIdAsLong());
    }

    /**
     * @see Request.setRequestType(int p_type)
     */
    public void setType(int p_type) throws RequestHandlerException
    {
        // verify it is a valid type
        switch (p_type)
        {
            case EXTRACTED_LOCALIZATION_REQUEST:
            case UNEXTRACTED_LOCALIZATION_REQUEST:
            case REQUEST_WITH_CXE_ERROR:
            case REQUEST_WITH_IMPORT_ERROR:
                m_type = p_type;
                break;
            default:
                // takes in three arguments - request id (may be 0),
                // external page id, attribute that can't be updated (as
                // string)
                String[] args = new String[3];
                args[0] = Long.toString(this.getId());
                args[1] = this.getExternalPageId();
                args[2] = Integer.toString(p_type);
                throw new RequestHandlerException(
                        RequestHandlerException.MSG_INVALID_REQUEST_TYPE, args,
                        null);
        }
    }

    /**
     * sets the original source file content name. this is a file that contains
     * the original source file content (temp file)
     * 
     * @param p_originalSourceFileContent
     *            This field may not be persisted.
     */
    public void setOriginalSourceFileContent(String p_originalSourceFileContent)
    {
        m_originalSourceFileContent = p_originalSourceFileContent;
    }

    /**
     * Gets the original source file content name. this is a file that contains
     * the original source file content (temp file)
     * 
     * This field may not be persisted.
     */
    public String getOriginalSourceFileContent()
    {
        return m_originalSourceFileContent;
    }

    /**
     * @see Request.getActiveTargets()
     */
    public Hashtable getActiveTargets()
    {
        return m_activeTargets;
    }

    /**
     * @see Request.addActiveTargets(GlobalSightLocale, Job)
     */
    public void addActiveTarget(GlobalSightLocale p_tLocale, Job p_job)
    {
        m_activeTargets.put(p_tLocale, p_job);
    }

    /**
     * @see Request.getInactiveTargetLocales()
     */
    public List getInactiveTargetLocales()
    {
        List targetLocales = new ArrayList(Arrays.asList(getL10nProfile()
                .getTargetLocales()));

        BatchInfo info = getBatchInfo();
        if (info != null)
        {
            String jobName = info.getBatchId();
            String hql = "from JobImpl j where j.jobName = :jobName";
            Map map = new HashMap();
            map.put("jobName", jobName);
            List jobs = HibernateUtil.search(hql, map);
            if (jobs.size() == 1)
            {
                JobImpl job = (JobImpl) jobs.get(0);
                for (Workflow w : job.getWorkflows())
                {
                    if (Workflow.READY_TO_BE_DISPATCHED.equals(w.getState())
                            || Workflow.DISPATCHED.equals(w.getState()))
                    {
                        GlobalSightLocale local = w.getTargetLocale();
                        if (!targetLocales.contains(local))
                        {
                            targetLocales.add(local);
                        }
                    }
                }
                job.getWorkflows();
            }
        }

        Set excludeTargetLocales = getActiveTargets().keySet();
        for (Iterator i = excludeTargetLocales.iterator(); i.hasNext();)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) i.next();
            targetLocales.remove(gsl);
        }
        return targetLocales;
    }

    /**
     * for debugging purposes
     */
    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(" m_type=");
        sb.append(m_type);
        sb.append(" m_dataSourceType=");
        sb.append((m_dataSourceType != null ? m_dataSourceType : "null"));
        sb.append(" m_dataSourceId=");
        sb.append(m_dataSourceId);
        sb.append(" m_externalPageId=");
        sb.append((m_externalPageId != null ? m_externalPageId : "null"));
        sb.append(" m_originalSourceEncoding=");
        sb.append((m_originalSourceEncoding != null ? m_originalSourceEncoding
                : "null"));
        sb.append(" L10nProfile id=");
        sb.append((m_l10nProfile != null ? Long.toString(m_l10nProfile.getId())
                : "no profile"));
        sb.append(" m_isPageCxePreviewable=");
        sb.append(m_isPageCxePreviewable);
        sb.append(" m_baseHref=");
        sb.append((m_baseHref != null ? m_baseHref : "null"));
        sb.append("\n");

        sb.append(" m_eventFlowXml=");
        sb.append((m_eventFlowXml != null ? m_eventFlowXml : "null"));
        sb.append(" job id=");
        sb.append((m_job != null ? Long.toString(m_job.getId()) : "no job"));
        sb.append(" m_sourcePageId=");
        sb.append(m_sourcePageId);
        sb.append("\n");

        sb.append(" m_batchInfo=");
        sb.append((m_batchInfo != null ? m_batchInfo.toString() : "null"));
        sb.append(" m_exception=");
        sb.append((m_exception != null ? m_exception.toString() : "null"));
        sb.append("\n");

        sb.append(" m_companyId=");
        sb.append(m_companyId);
        sb.append("\n");

        return sb.toString();
    }

    public String toString()
    {
        return getName() + getId();
    }

    /**
     * Returns the extracted source file or NULL if there isn't one.
     */
    private ExtractedSourceFile getExtractedSourceFile(SourcePage p_page)
    {
        ExtractedSourceFile esf = null;
        if (p_page.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
        {
            esf = (ExtractedSourceFile) p_page.getPrimaryFile();
        }
        return esf;
    }

    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * @see Request#addUnimportTargetLocale(GlobalSightLocale)
     */
    public void addUnimportTargetLocale(GlobalSightLocale p_locale)
    {
        m_unimportTargets.add(p_locale);
    }

    /**
     * @see Request#getUnimportTargetLocales()
     */
    public List getUnimportTargetLocales()
    {
        return m_unimportTargets;
    }

    /**
     * @see Request#getTargetLocalesToImport()
     */
    public GlobalSightLocale[] getTargetLocalesToImport()
    {
        List all = new ArrayList(Arrays.asList(getL10nProfile()
                .getTargetLocales()));
        List unimport = getUnimportTargetLocales();
        for (Iterator iter = unimport.iterator(); iter.hasNext();)
        {
            GlobalSightLocale g = (GlobalSightLocale) iter.next();
            all.remove(g);
        }

        GlobalSightLocale[] gs = new GlobalSightLocale[all.size()];
        for (int i = 0; i < gs.length; i++)
        {
            gs[i] = (GlobalSightLocale) all.get(i);
        }
        return gs;
    }

    public void setL10nProfile(L10nProfile profile)
    {
        m_l10nProfile = profile;
    }

    public String getTypeAsString()
    {
        return (String) TYPES.get(new Integer(m_type));
    }

    public void setTypeAsString(String type)
    {
        if (type != null)
        {
            Set keys = TYPES.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext())
            {
                Integer key = (Integer) iterator.next();
                String value = (String) TYPES.get(key);
                if (value.equalsIgnoreCase(type))
                {
                    m_type = key.intValue();
                    break;
                }
            }
        }
    }

    public String getPriority()
    {
        return m_priority;
    }

    public void setPriority(String mPriority)
    {
        m_priority = mPriority;
    }

    public long getFileProfileId()
    {
        String xml = getEventFlowXml();
        if (xml == null)
            return -1;

        String regex = "<fileProfileId>(\\d+)</fileProfileId>";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(xml);
        if (match.find())
        {
            return Long.parseLong(match.group(1));
        }

        regex = "dataSourceId=\"(\\d+)\"";
        pattern = Pattern.compile(regex);
        match = pattern.matcher(xml);
        if (match.find())
        {
            return Long.parseLong(match.group(1));
        }

        return -1;
    }
}
