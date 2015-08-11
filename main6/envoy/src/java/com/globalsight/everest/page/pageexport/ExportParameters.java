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
package com.globalsight.everest.page.pageexport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.StringUtil;

/**
 * ExportParameters class contains a few Workflow level info that are used
 * during export process. Since the selected pages for export are within the
 * same workflow, we need to gather the generic info required for export and
 * link management prior to page retrieval.
 */
public class ExportParameters implements java.io.Serializable, Cloneable 
{
    private static final long serialVersionUID = 219733514107887560L;

    static private final Logger s_category = Logger
            .getLogger(ExportParameters.class);

    private String m_exportCodeset = null;
    private String m_targetURL = null;
    private String m_exportType = null;
    private String m_exportLocation = null;
    private String m_localeSubDir = null;
    private int m_bomType = 0;
    private int m_xlfSrcAsTrg = -1;

    // For documentum Job
    private long m_workflowId = 0;
    private String m_newObjId = null;
    private boolean m_isJobDone = false;
    private boolean m_isFinalExport = false;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Constructor used for dynamic preview. This default constructor will only
     * populate information required during preview.
     */
    public ExportParameters(TargetPage p_targetPage) throws PageException
    {
        this(p_targetPage.getWorkflowInstance(), null, null, null,
                ExportConstants.NOT_SELECTED, ExportConstants.PREVIEW);
    }

    /**
     * Constructor used for automatic export.
     * 
     * @param p_workflow
     *            - The workflow which the selected pages to be exported belong
     *            to.
     */
    public ExportParameters(Workflow p_workflow) throws PageException
    {
        this(p_workflow, null, null, null, ExportConstants.NOT_SELECTED,
                ExportConstants.AUTOMATIC_EXPORT);
    }

    /**
     * Constructor used for manual export since a target charset and export
     * directory could be defined.
     * 
     * @param p_workflow
     *            - The workflow where the selected pages to be exported belong
     *            to.
     * @param p_exportCodeset
     *            - The target locale's charset.
     * @param p_exportLocation
     *            - The export location for the target pages
     * @param p_localeSubDir
     *            - locale or language
     * @param p_bomType
     *            - UTF-8 Byte Order Mark
     */
    public ExportParameters(Workflow p_workflow, String p_exportCodeset,
            String p_exportLocation, String p_localeSubDir, int p_bomType)
            throws PageException
    {
        this(p_workflow, p_exportCodeset, p_exportLocation, p_localeSubDir,
                p_bomType, ExportConstants.MANUAL_EXPORT);
    }

    // internal constructor used for setting the export parameters
    public ExportParameters(Workflow p_workflow, String p_exportCodeset,
            String p_exportLocation, String p_localeSubDir, int p_bomType,
            String p_type) throws PageException
    {
        try
        {
            setExportParameters(p_workflow, p_exportCodeset, p_exportLocation,
                    p_localeSubDir, p_bomType, p_type);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_workflow.getId()) };

            s_category.debug(
                    "ExportParameters :: setExportParameters -- "
                            + e.getMessage(), e);

            throw new PageException(
                    PageException.MSG_FAILED_TO_SET_EXPORT_PARAMETER, args, e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the code set for export.
     * 
     * @return The code set based on the target locale.
     */
    public String getExportCodeset()
    {
        return m_exportCodeset;
    }

    /**
     * Returns true if this export occurs at the completion of a workflow. This
     * is not interim export or preview.
     * 
     * @return true | false
     */
    public boolean getIsFinalExport()
    {
        return m_isFinalExport;
    }

    /**
     * Get the URL to CXE for sending the export request.
     * 
     * @return The CXE's URL.
     */
    public String getTargetURL()
    {
        return m_targetURL;
    }

    /**
     * Get the export location.
     * 
     * @return The export location directory for export.
     */
    public String getExportLocation()
    {
        return m_exportLocation;
    }

    public void setExportLocation(String p_exportLocation)
    {
        this.m_exportLocation = p_exportLocation;
    }

    /**
     * Get the locale sub dir
     * 
     * @return The locale subdir for export.
     */
    public String getLocaleSubDir()
    {
        return m_localeSubDir;
    }

    /**
     * Get the request type for the export (automatic vs. manual)
     * 
     * @return The export type.
     */
    public String getExportType()
    {
        return m_exportType;
    }

    /**
     * Set the new documentum object id.
     */
    public void setNewObjectId(String newObjId)
    {
        m_newObjId = newObjId;
    }

    /**
     * Get the new documentum object id.
     */
    public String getNewObjectId()
    {
        return m_newObjId;
    }

    /**
     * Get the workflow id for the export (just for documentum job).
     */
    public long getWorkflowId()
    {
        return m_workflowId;
    }

    /**
     * Get the job state for the export (just for documentum job).
     */
    public boolean isJobDone()
    {
        return m_isJobDone;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Helper Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    // prepare the export parameters by setting values required for export.
    private void setExportParameters(Workflow p_workflow,
            String p_exportCodeset, String p_exportLocation,
            String p_localeSubDir, int p_bomType, String p_exportType)
            throws Exception
    {
        m_targetURL = getCxeServletUrl();
        m_exportType = p_exportType;
        m_exportCodeset = p_exportCodeset;
        String companyId = CompanyWrapper.getCurrentCompanyId();

        if (p_workflow == null)
        {
            if (p_exportCodeset == null)
            {
                m_exportCodeset = ExportConstants.UTF8;
            }
            else
            {
                m_exportCodeset = p_exportCodeset;
            }
        }
        else
        {
            m_exportCodeset = p_exportCodeset == null ? getExportCodeset(p_workflow)
                    : p_exportCodeset;

            // export of a localized workflow is a final export
            if (p_workflow.getState().equals(Workflow.LOCALIZED))
            {
                m_isFinalExport = true;
            }

            // These two variables just for Documentum job
            m_workflowId = p_workflow.getId();
            String jobState = p_workflow.getJob().getState();
            if (jobState.equals(Job.LOCALIZED) || jobState.equals(Job.EXPORTED))
            {
                m_isJobDone = true;
            }
            companyId = String.valueOf(p_workflow.getJob().getCompanyId());
        }

        m_localeSubDir = p_localeSubDir == null ? getLocaleSubDirType(p_workflow
                .getTargetLocale().toString()) : p_localeSubDir;
        m_exportLocation = p_exportLocation == null ? lookupDefaultExportLocation(companyId)
                : p_exportLocation;
        m_bomType = p_bomType;
    }

    // get the codeset of the given workflow's target locale.
    private String getExportCodeset(Workflow p_workflow) throws Exception
    {
        long lpId = p_workflow.getJob().getL10nProfileId();
        L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(lpId);
        return lp.getWorkflowTemplateInfo(p_workflow.getTargetLocale())
                .getCodeSet();
    }

    // Get the rule type form property file for determining
    // the locale specific sub directory for export
    private String getLocaleSubDirType(String p_targetLocale) throws Exception
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        String ruleType = sc
                .getStringParameter(SystemConfiguration.DIRECTORY_RULE_TYPE);
        String subDir = p_targetLocale;

        if (ruleType != null)
        {
            if (ruleType.toUpperCase().equals(
                    ExportConstants.LANGUAGE_DIRECTORY))
            {
                int index = p_targetLocale.indexOf("_");
                subDir = p_targetLocale.substring(0, index);
            }
            else if (ruleType.toUpperCase().equals(
                    ExportConstants.EXPORT_DIRECTORY))
            {
                subDir = "export";
            }
            else
                subDir = p_targetLocale;
        }
        return subDir;
    }

    // Get the URL to CXE's export servlet from property file...
    private String getCxeServletUrl() throws Exception
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        StringBuffer sb = new StringBuffer();

        sb.append("http://");
        sb.append(sc.getStringParameter(SystemConfiguration.SERVER_HOST));
        sb.append(":");
        sb.append(sc.getStringParameter(SystemConfiguration.SERVER_PORT));
        sb.append(sc.getStringParameter(SystemConfiguration.CXE_SERVLET_URL));

        return sb.toString();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString()
    {
        return super.toString() + " m_exportCodeset=" + m_exportCodeset
                + " m_targetURL=" + m_targetURL + " m_exportLocation="
                + m_exportLocation + " m_localeSubDir=" + m_localeSubDir;
    }

    /**
     * Looks up the default export location.
     * 
     * @return the default export location (may be CXE docs dir)
     */
    private String lookupDefaultExportLocation(String companyId)
            throws Exception
    {
        // Modify this code to return the user's default preference
        // just return the CXE docs dir for now
        if (StringUtil.isEmpty(companyId))
            return ServerProxy.getExportLocationPersistenceManager()
                    .getDefaultExportLocation().getLocation();
        else
            return ServerProxy.getExportLocationPersistenceManager()
                    .getDefaultExportLocation(companyId).getLocation();
    }

    public void setExportCodeset(String codeset)
    {
        m_exportCodeset = codeset;
    }

    /**
     * @param m_bomType
     *            the m_bomType to set
     */
    public void setBOMType(int m_bomType)
    {
        this.m_bomType = m_bomType;
    }

    /**
     * @return the m_bomType
     */
    public int getBOMType()
    {
        return m_bomType;
    }

    public void setXlfSrcAsTrg(int p_value)
    {
        this.m_xlfSrcAsTrg = p_value;
    }

    public int getXlfSrcAsTrg()
    {
        return this.m_xlfSrcAsTrg;
    }
    
    /**
     * Needs deep clone.
     */
    public ExportParameters clone() throws CloneNotSupportedException
    {
        ByteArrayOutputStream  byteOut = new ByteArrayOutputStream();             
        ObjectOutputStream out;
        try
        {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(this);                    
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());             
            ObjectInputStream in = new ObjectInputStream(byteIn);  
            
            return (ExportParameters) in.readObject();
        } 
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }             
        
        return (ExportParameters) super.clone();
    }
}
