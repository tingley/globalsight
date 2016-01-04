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

import java.util.Hashtable;
import java.util.List;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This interface defines the operations that can be performed on a request from
 * CXE to CAP.
 */
public interface Request
{
    //
    // Constant Definitions
    //

    /**
     * The localization request types that are valid. They are greater than 0.
     */
    public static final int EXTRACTED_LOCALIZATION_REQUEST = 1;
    public static final int UNEXTRACTED_LOCALIZATION_REQUEST = 2;

    /**
     * The error requests are negative so they are easier to find. Can just
     * check if the type is < 0 to determine if it is an error.
     */
    public static final int REQUEST_WITH_CXE_ERROR = -1;
    public static final int REQUEST_WITH_IMPORT_ERROR = -2;

    //
    // Public Method Interfaces
    //

    /**
     * Set compnay id in request. It is used to create job, workflow, taskinfo.
     */
    public void setCompanyId(long p_companyId);

    /**
     * Returns the company id stored in request.
     * 
     * @return
     */
    public long getCompanyId();

    /**
     * Returns the ID of this request.
     * 
     * @return long The primary key of request object
     */
    public long getId();

    /**
     * Returns the unique id of the data source this request came from.
     * 
     * @return long The unique id that maps to a particular datasource.
     */
    public long getDataSourceId();

    /**
     * Returns the Event Flow XML associated with this request. This information
     * is needed by CXE when the request is finished being localized and must be
     * exported to CXE.
     * 
     * @return String The EventFlowXML CXE needs.
     */
    public String getEventFlowXml();

    /**
     * Returns the external page id (unique page name).
     * 
     * @return String The unique name for the page to be associated/or already
     *         associated with this request.
     */
    public String getExternalPageId();

    /**
     * Sets the external page id (unique name for the page).
     * 
     * @param p_externalPageId
     *            The unique name to give the page.
     * @exception RequestHandlerException
     *                Returns an exception if the page has already been set
     *                within the request and the names are different.
     */
    public void setExternalPageId(String p_pageName)
            throws RequestHandlerException;

    /**
     * Returns the GXML or PRSXML associated with this request. This XML is
     * created in CXE and passed to CAP NOTE: this could either be a temp
     * filename or a string of GXML. This field is not persisted.
     * 
     * @return String The GXML (content to be translated).
     */
    public String getGxml();

    /**
     * Sets the GXML or PRSXML associated with this request. NOTE: this could
     * either be a temp filename or a string of GXML/PRSXML. This field is not
     * persisted.
     */
    public void setGxml(String p_gxml);

    /**
     * Returns the batch information.
     * 
     * @return BatchInfo Returns the batch information for this request or null
     *         if this request isn't part of a batch.
     */
    public BatchInfo getBatchInfo();

    /**
     * Returns the localization profile associated with this request.
     * 
     * @return com.globalsight.everest.foundation.L10nProfile
     */
    public L10nProfile getL10nProfile();

    /**
     * Gets the encoding (code/char set) of the page (GXML) to be or already
     * associated with this request.
     */
    public String getSourceEncoding();

    /**
     * Gets the exception associated with this request.
     * 
     * @return The exception associated with this request. Will be null if there
     *         isn't an exception.
     */
    public GeneralException getException();

    /**
     * Gets the deserialized exception as a string.
     * 
     * @return The exception represented as a string.
     */
    public String getExceptionAsString();

    /**
     * Sets the exception associated with processing this request.
     * 
     * @param p_exceptionXml
     *            The exception as an xml string.
     * @exception RequestHandlerException
     *                - couldn't deserialize the exception
     */
    public void setException(GeneralException p_exception);

    /**
     * Checks if the page is previewable by CXE.
     * 
     * @return 'true' or 'false'
     */
    public boolean isPageCxePreviewable();

    /**
     * Sets the base href specified by CXE.
     */
    public void setBaseHref(String p_baseHref);

    /**
     * Returns the base href specified by CXE. This may be an empty string if
     * none was specified.
     */
    public String getBaseHref();

    /**
     * Returns the id of the workflow template that is associated with this
     * request and with the target locale specified. The id is the unique
     * identifier for the template.
     * 
     * @param GlobalSightLocale
     *            The locale to find the template for.
     * @return long The id of the workflow template.
     * 
     * @exception An
     *                exception is thrown if the template can't be found for the
     *                locale specified.
     */
    public long getWorkflowTemplateId(GlobalSightLocale p_targetLocale)
            throws RequestHandlerException;

    /**
     * Gets the page that was created from the GXML in this request.
     * 
     * @return SourcePage The page that was created from parsing the GXML in
     *         this request. This can be null if the page hasn't been
     *         created/set yet.
     */
    public SourcePage getSourcePage();

    /**
     * Sets the source page. This is not set on a query for a request, so must
     * be set by the code.
     */
    void setSourcePage(SourcePage p_page);

    /**
     * Returns the type of data source this request is for.
     * 
     * @return String The value that maps to a particular datasource
     *         DataSourceType (database, filesystem, other content
     *         management systems)
     */
    public String getDataSourceType();

    /**
     * Sets the type of data source this request is for.
     * 
     * @param p_type
     *            The value that maps to a particular datasource (database,
     *            filesystem, content management system)
     * @exception RequestHandlerException
     *                An exception is thrown if the page has already been set.
     *                At this point the datasource type can't be changed.
     */
    public void setDataSourceType(String p_type) throws RequestHandlerException;

    /**
     * Returns the name of data source this request is for.
     * 
     * @return String The name of the data source
     * @exception Returns
     *                an exception if it couldn't retrieve the data source name.
     */
    public String getDataSourceName() throws RequestHandlerException;

    /**
     * Returns the type of the request.
     */
    public int getType();

    /**
     * Sets the Job that the Request is associated with
     */
    public void setJob(Job p_job);

    public Job getJob();

    /**
     * Sets the request type. The type can change from one without error to one
     * with error if the GXML fails parsing (import).
     * 
     * @param p_type
     *            The type to set the request to.
     * 
     * @exception Returns
     *                an error if the type specified isn't a valid type.
     */
    public void setType(int p_type) throws RequestHandlerException;

    /**
     * Sets the original source file content name. This is a file that contains
     * the original source file content (temp file).
     * 
     * This field may not be persisted.
     */
    public void setOriginalSourceFileContent(String p_originalSourceFileContent);

    /**
     * Gets the original source file content name. This is a file that contains
     * the original source file content (temp file).
     * 
     * This field may not be persisted.
     */
    public String getOriginalSourceFileContent();

    /**
     * Returns the list of targets that are already active with the same page
     * that this request is associated with.
     * 
     * The list of targets also contains the various jobs they are active in.
     * 
     * This list is populated and used during importing. It is not persisted in
     * the database.
     * 
     * @return A hashtable where the target locale is the key and the job id is
     *         the value.
     */
    public Hashtable getActiveTargets();

    /**
     * Adds a target locale to the request that this page is already actively
     * being worked on and the job it is active in.
     * 
     * This list is persisted only in memory not the database.
     * 
     * @param p_tLocale
     *            The target locale the page is active in (as a
     *            GlobalSightLocale object from the cache).
     * @param p_job
     *            The job that the target page is active in.
     */
    public void addActiveTarget(GlobalSightLocale p_tLocale, Job p_job);

    /**
     * Returns a set of target locales (GlobalSightLocale) that this particuarl
     * page of the request is NOT active in - so it can be imported.
     * 
     * Basically all the target locales from the L10nProfile minus the active
     * target locales.
     */
    public List getInactiveTargetLocales();

    /**
     * add GlobalSightLocale to here to make this workflow unimported when
     * creating job
     * 
     * @param p_tLocale
     */
    public void addUnimportTargetLocale(GlobalSightLocale p_tLocale);

    /**
     * get which GlobalSightLocales do not need to import
     * 
     * @return a list of GlobalSightLocales
     */
    public List getUnimportTargetLocales();

    /**
     * get which target locales should be used to import
     */
    public GlobalSightLocale[] getTargetLocalesToImport();

    public String getPriority();

    public void setPriority(String p_priority);

    public long getFileProfileId();
}
