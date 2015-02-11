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

import com.globalsight.everest.page.GenericPage;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * ExportingPage holds information about a page that's in the process of getting
 * exported. This information is stored in the system and used by
 * ExportBatchEvent object.
 */
public class ExportingPage extends PersistentObject
{
	// /////////////////
	// public constants
	// /////////////////

	private static final long serialVersionUID = -6044952583446069328L;

	/* The valid exporting page states */
	public final static String EXPORTED = "EXPORTED";
	public final static String EXPORT_IN_PROGRESS = "EXPORT_IN_PROGRESS";
	public final static String EXPORT_FAIL = "EXPORT_FAIL";

	private ExportBatchEvent m_exportBatchEvent = null;
	private long m_endTime = 0;
	private GenericPage m_page = null;
	private String m_errorMsg = null;
	private String m_pageType = null;
	private String m_state = null;
	private String m_exportPath = null;
	private char isComponentPage = 'N';

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructors
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Default constructor.
	 */
	public ExportingPage()
	{
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Constructors
	// ////////////////////////////////////////////////////////////////////

	// ////////////////////////////////////////////////////////////////////
	// Begin: Public Methods
	// ////////////////////////////////////////////////////////////////////
	/**
	 * Get the time in milliseconds where the export process for this page was
	 * completed.
	 * 
	 * @return The end time of the export process for this page in milliseconds.
	 */
	public long getEndTime()
	{
		return m_endTime;
	}

	/**
	 * Set the end time of the exporting page to be the specified value.
	 * 
	 * @param p_endTime The end time in milliseconds.
	 */
	public void setEndTime(long p_endTime)
	{
		m_endTime = p_endTime;
	}

	/**
	 * Get the error message generated during the export process of this
	 * particular exporting page. An error message is set only when the export
	 * process has failed for this particular page.
	 * 
	 * @return The error message, if there's one. Otherwise, return null.
	 */
	public String getErrorMessage()
	{
		return m_errorMsg;
	}

	/**
	 * Set the error message of the exporting page to be the specified value.
	 * 
	 * @param p_endDate The error message for this exporting page.
	 */
	public void setErrorMessage(String p_errorMsg)
	{
		m_errorMsg = p_errorMsg;
	}

	/**
	 * Get the export batch event where this exporting page belongs to.
	 * 
	 * @return The export batch event for this exporting page.
	 */
	public ExportBatchEvent getExportBatchEvent()
	{
		return m_exportBatchEvent;
	}

	/**
	 * Set the export batch event where this exporting page belongs to.
	 * 
	 * @param p_endDate The error message for this exporting page.
	 */
	public void setExportBatchEvent(ExportBatchEvent p_exportBatchEvent)
	{
		m_exportBatchEvent = p_exportBatchEvent;
	}

	/**
	 * Get the page object based on this exporting page.
	 * 
	 * @return The page object which this exporting page points to.
	 */
	public GenericPage getPage()
	{
		return m_page;
	}

	/**
	 * Set the page which this exporting page points to.
	 * 
	 * @param p_page The exported page.
	 */
	public void setPage(GenericPage p_page)
	{
		m_page = p_page;
	}

	/**
	 * Get the page type for this export batch event. The valid types are
	 * 'SOURCE', 'PRIMARY_TARGET', and 'SECONDARY_TARGET'. Note that no multiple
	 * page types can be part of an export batch event.
	 * 
	 * @return The type of the pages that were exported.
	 */
	public String getPageType()
	{
		return m_pageType;
	}

	public void setPageType(String pageType)
	{
		m_pageType = pageType;
	}

	/**
	 * Get the export state for this exporting page. The state could be
	 * 'EXPORTED','EXPORT_IN_PROGRESS', or 'EXPORT_FAIL'.
	 * 
	 * @return The state of this exporting page.
	 */
	public String getState()
	{
		return m_state;
	}

	/**
	 * Set the state of this exporting page.
	 * 
	 * @param p_state The state of this exporting page. Note that only
	 * 'EXPORTED','EXPORT_IN_PROGRESS', and 'EXPORT_FAIL' are valid states.
	 */
	public void setState(String p_state)
	{
		m_state = p_state;
	}

	/**
	 * Get the export path for this exporting page.
	 * 
	 * @return The path that the file was exported to.
	 */
	public String getExportPath()
	{
		return m_exportPath;
	}

	/**
	 * Set the export path for this exporting page.
	 * 
	 * @param p_exportPath
	 *            The path that the file was exported to.
	 */
	public void setExportPath(String p_exportPath)
	{
		m_exportPath = p_exportPath;
	}

	/**
	 * Gets the isComponent flag. (See setIsComponent() )
	 * 
	 * The component state is obtained from the CXE response.
	 * 
	 * The primary use of this flag is aid with presentation and layout of
	 * export results.
	 * 
	 * @return True if the exporting page is a component of a larger export file
	 *         and False if it is either the main component or a normal
	 *         standalone file.
	 * 
	 */
	public boolean isComponentPage()
	{
		return isComponentPage == 'Y' ? true : false;
	}

	/**
	 * Sets the isComponent flag. The flag should be set to true if the
	 * exporting page is a component that was merged into a larger main export
	 * file (like for office formats) and False if it was either the main
	 * component file (the file that other components are merged into) or a
	 * simply mormal single file.
	 * 
	 * The component state is obtained from the CXE export response.
	 * 
	 * The primary use of this flag is aid with presentation and layout of
	 * export results.
	 * 
	 * @param p_state
	 *            set 'Y' if the exporting page is a component of a larger
	 *            export file and 'N' if it is either the main component or a
	 *            normal standlaone file.
	 */
	public void setComponentPage(char p_yesNo)
	{
		isComponentPage = p_yesNo;
	}

	// ////////////////////////////////////////////////////////////////////
	// End: Public Methods
	// ////////////////////////////////////////////////////////////////////
}
