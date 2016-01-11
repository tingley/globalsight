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
package com.globalsight.everest.page;


// globalsight
import java.io.File;

import org.apache.log4j.Logger;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LazyInitializationException;

import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * TargetPage is the object containing information about a page with a
 * target locale.
 */
public class TargetPage
    extends Page
{
    private static final long serialVersionUID = -1230516788376282211L;

    //  static class variables
    private static Logger s_logger =
        Logger.getLogger(
            TargetPage.class.getName());

    /**
     * Constant.
     */
    public static final String SOURCE_PAGE = "m_sourcePage";
    public static final String WORKFLOW = "m_workflowInstance";

    private SourcePage m_sourcePage = null;
    private Workflow m_workflowInstance = null;
    private PageWordCounts m_wordCount = null;
    private GeneralException m_error = null;        // stores an error on IMPORT or EXPORT
    private String m_exportSubDir = null;
    private String m_CVSTargetModule = null;
    private String m_CVSTargetFilename = null;
    private long m_companyId;

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
    * Constructor.
    */
    public TargetPage()
    {
        super();
        // only for TopLink
        m_wordCount = new PageWordCounts();
    }

    /**
    * Constructor.
    * @param p_globalSightLocale - The locale of the target page.
    * @param p_sourcePage - The source page (distinguished by locale)
    * of this page.
    */
    public TargetPage(GlobalSightLocale p_locale, SourcePage p_sourcePage)
    {
        super(p_sourcePage.getPrimaryFileType());
        m_globalSightLocale = p_locale;
        m_sourcePage = p_sourcePage;
        m_wordCount = new PageWordCounts();
        m_companyId = p_sourcePage.getCompanyId();

        // copy the base href's for the target page to start out with
        if (p_sourcePage.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
        {
            ExtractedFile sEf = (ExtractedFile)p_sourcePage.getPrimaryFile();
            ExtractedFile tEf = (ExtractedFile)this.getPrimaryFile();
            tEf.setInternalBaseHref(sEf.getInternalBaseHref());
            tEf.setExternalBaseHref(sEf.getExternalBaseHref());
        }
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Abstract Methods Implementation
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the locale id of the target page.
     * @return the Locale id of the page.
     */
    public long getLocaleId()
    {
        return getGlobalSightLocale().getId();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Abstract Methods Implementation
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the source page of this target page.
     * @return The source page where this target page was created
     * from.
     */
    public SourcePage getSourcePage()
    {
        return m_sourcePage;
    }

    /**
     * Set the word counts for the Target Page.
     * @param p_wordCount The word count object containing all the
     * word count information for this page.
     */
    public void setWordCount(PageWordCounts p_wordCount)
    {
        m_wordCount = p_wordCount;
    }

    /**
     * Get the word counts for this Target Page.
     * @return PageWordCounts object containing all word count
     * information for this page.
     */
    public PageWordCounts getWordCount()
    {
        return m_wordCount;
    }

    /**
     * Get the workflow instance that this page belongs to.
     * @return The workflow instance that this page belongs to.
     */
    public Workflow getWorkflowInstance()
    {
        return m_workflowInstance;
    }

    /**
     * Set the workflow instance that this target page belongs to.
     * This is used when workflow is adding target pages to itself
     * (need the back pointer for TopLink)
     * @param p_workflowInstance - The workflow instance to be set.
     */
    public void setWorkflowInstance(Workflow p_workflowInstance)
    {
        m_workflowInstance = p_workflowInstance;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: override methods
    //////////////////////////////////////////////////////////////////////
    /**
    * Get the locale of this page.
    * @return The locale of this page.
    */
    public GlobalSightLocale getGlobalSightLocale()
    {
        if (m_globalSightLocale == null)
        {
            m_globalSightLocale = m_workflowInstance.getTargetLocale();
        }

        return m_globalSightLocale;
    }

    /**
     * Returns the unique page identifier for the page external to the
     * system.
     * @return the external page identifier.
     */
    public String getExternalPageId()
    {
    	try 
    	{
			return m_sourcePage.getExternalPageId();
		} 
    	catch (LazyInitializationException e) 
    	{
			m_sourcePage = HibernateUtil.get(SourcePage.class, m_sourcePage
					.getId());
			return m_sourcePage.getExternalPageId();
		}
    }

    /**
	 * @return the data source type.
	 */
    public String getDataSourceType()
    {
        return m_sourcePage.getDataSourceType();
    }

    /**
     * Return a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString()); 
        sb.append("\nm_sourcePage=");
        sb.append((m_sourcePage!=null?m_sourcePage.toString():"null"));
        sb.append("\nWorkflow=");
        sb.append((m_workflowInstance!=null?
                ((WorkflowImpl)m_workflowInstance).
                toStringNoTargetPages():"null")); 
        sb.append("\nm_wordCount=");
        sb.append((m_wordCount!=null?m_wordCount.toString():"null"));
        sb.append("\n");
        return sb.toString();
    } 


    //////////////////////////////////////////////////////////////////////
    //  End: override methods
    ////////////////////////////////////////////////////////////////////// 

    //////////////////////////////////////////////////////////////////////
    //  Begin: Methods related to Export Errors        
    //////////////////////////////////////////////////////////////////////

    /**
     * Gets the associated export error or returns null if there is no error
     * @return GeneralException or null
     */
    public GeneralException getExportError()
    {
        if (getPageState().equals(PageState.EXPORT_FAIL))
        {
            return m_error;
        }
        else
            return null;
    }

    /**
     * Gets the associated import error or returns null if there is no error.
     */
    public GeneralException getImportError()
    {
        if (getPageState().equals(PageState.IMPORT_FAIL)) 
        {
            return m_error;
        }
        else
            return null;
    }

    /**
     * Used by TOPLink to write the serialized error to the database
     * @return String of Exception XML
     */
    public String getErrorAsString()
    {
        String errString = null;

        if (m_error != null)
        {
            try
            {
                errString = m_error.serialize();
            }
            catch (GeneralException ge)
            {
                s_logger.error("Failed to serialize the error exception xml "
                    + this.getId(), ge);
            }
        }
        return errString;
    }

    /**
     * Set the error (for import or export) using a serialized exception
     *
     * @param p_exceptionXml The exception as an xml string.
     */
    public void setErrorAsString(String p_exceptionXml)
    {
        if (p_exceptionXml != null && p_exceptionXml.length() > 0)
        {
            try
            {
                GeneralException exception =
                    GeneralException.deserialize(p_exceptionXml);

                setError(exception);
            }
            catch (GeneralException ge)
            {
                s_logger.error("Failed to deserialize the " +
                    "exception xml to add to target page " + this.getId(), ge);
                setError(null);
            }
        }
        else
        {
            setError(null);
        }
    }

    /**
     * Set the error (for export or import) using a GeneralException
     *
     * @param p_exception The GeneralException
     */
    public void setError(GeneralException p_exception)
    {
        m_error = p_exception;
    }

    /**
     * Create the correct type of primary file that 
     * this target page is associated with.
     */
    protected void createPrimaryFile(int p_type)
    {
        switch (p_type)
        {
            case PrimaryFile.UNEXTRACTED_FILE:
                m_unextractedFile = new UnextractedFile();
                break;
            case PrimaryFile.EXTRACTED_FILE:
                // fall through to default
            default:    //assume extracted file
                m_extractedFile = new ExtractedTargetFile();
                break;
        }
    }
    
    public String getExportSubDir()
    {
        //For old data, this field maybe empty. So we still use the target 
        //locale as the export sub dir default value. Remove it in a pure new
        // version
        if (StringUtils.isEmpty(this.m_exportSubDir))
        {
            s_logger.debug("No value for \"localeSubDir\"(In export page), use target locale as default value.");
            return File.separator + this.getGlobalSightLocale().toString();
        }
        else
        {
            return this.m_exportSubDir;
        }
    }
    
    public void setExportSubDir(String p_exportSubDir)
    {
        this.m_exportSubDir = p_exportSubDir;
    }

    public void setSourcePage(SourcePage page)
    {
        m_sourcePage = page;
    }
    
    public void setCVSTargetModule(String p_targetModule) {
    	this.m_CVSTargetModule = p_targetModule;
    }
    
    public String getCVSTargetModule() {
    	return m_CVSTargetModule;
    }
    
    public void setCVSTargetFilename(String p_f) {
    	this.m_CVSTargetFilename = p_f;
    }
    
    public String getCVSTargetFilename() {
    	return m_CVSTargetFilename == null ? "" : m_CVSTargetFilename;
    }

    public long getCompanyId()
    {
        return this.m_companyId;
    }

    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }
}
