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
package com.globalsight.everest.edit.offline.download;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.jobhandler.Job;

/**
 * A data class that specifies various download parameters.
 */
public class DownloadParams implements Serializable
{

	private static final long serialVersionUID = 7822307043878616032L;

	static private final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
			.getLogger(DownloadParams.class);

	//
	// Private Members
	//

	private File m_tmpDownloadFile = null;
	private String m_rootDir = null;
	private String m_urlPrefix = null;
	private String m_jobName = null;
	private String m_workflowID = null;
	private String m_taskID = null;
	private String m_encoding = null;
	private List m_PTF_Ids = null;
	private List m_PTF_Names = null;
	private List m_PTF_canUseUrl = null;
	private List m_PSF_Ids = null;
	private List m_STF_Ids = null;
	private int m_editorID = -1;
	private int m_platformID = -1;
	private String m_platformLineBreak = "\n";
	private int m_tagDisplayFormatID = -1;
	private String m_uiLocale = null;
	private GlobalSightLocale m_sourceLocale = null;
	private GlobalSightLocale m_targetLocale = null;
	private boolean m_createZip = false;
	private int m_fileFormat = -1;
	private Vector m_excludeTypeNames = null;
	private int m_downloadEditAll = AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED;
	private List m_supportFiles = null;
	private int m_resourceOption = AmbassadorDwUpConstants.MAKE_RES_ATNS;
	private boolean m_isPrimaryFiles = false;
	private boolean m_isSourceFiles = false;
	private boolean m_isSecondaryFiles = false;
	private boolean m_isSupportFiles = false;
	private boolean m_isReferencePageParameters = false;
	private boolean consolidateTmxFiles = false;
	private boolean consolidateTermFiles = false;
	private String termFormat = OfflineConstants.TERM_NONE;
	private User m_user = null;

	private String sessionId;
	private String displayExactMatch = "no";
	private JobPackageZipper zipper = null;
	
    public boolean forMailDownloadFlag = false;
    
    private Job m_job = null;
    private ArrayList autoActionNodeEmail = null;
    
    private boolean m_changeCreationIdForMTSegments = false;
    
    private boolean populate100 = true;
    private boolean populateFuzzy = true;
    
    private boolean needConsolidate = false;
    
    private String activityType = "";
    
	//
	// Constructors
	//

	public DownloadParams()
	{
		super();
	}

	/**
	 * DownloadParams constructor.
	 * 
	 * @param p_jobName -
	 *            the name of the imported job
	 * @param p_rootDir -
	 *            the root of the download/upload directory
	 * @param p_pageUrlPrefix -
	 *            URL to rendered page (minus page name)
	 * @param p_workflowID -
	 *            the WorkFlowID as a string
	 * @param p_taskID -
	 *            the task ID as a string
	 * @param p_PTF_Ids-
	 *            the IDs of any primary target files
	 * @param p_PTF_Names-
	 *            the literal names of the pages in the job
	 * @param p_PTF_canUseUrl-
	 *            the validity of the URL preview string for each page
	 * @param m_PSF_Ids -
	 *            the IDs of any primary source files
	 * @param p_STF_Ids -
	 *            the IDs of any secondary target fiels
	 * @param p_editorID -
	 *            the target editor (end-user)
	 * @param p_platformID -
	 *            the target OS (end-user)
	 * @param p_encoding -
	 *            the code page for download
	 * @param p_ptagDisplayMode -
	 *            compact or verbose
	 * @param p_uiLocale -
	 *            the Gui locale
	 * @param p_sourceLocale -
	 *            source language locale
	 * @param p_targetLocale -
	 *            target language locale
	 * @param p_createZip -
	 *            true requests a Job as a Zip package
	 * @param p_fileFormat -
	 *            indicates the file type (Text, RTF, TradosRtf)
	 * @param p_excludeTypes
	 *            a list of type to exclude as an array of strings
	 * @param p_downloadEditAll
	 *            the tri-state of the downloadEditAll button.
	 * @param p_supportFiles
	 *            a list of GlossaryFile objects.
	 * @param p_SupportFilesOnlyDownload
	 *            indicates whether pages are inlucded
	 * @param p_resourceOption
	 *            indicates how to present TM and Term resources. Pass null for
	 *            default resource linking behavior.
	 * @param p_user
	 *            A User object
	 */
	public DownloadParams(String p_jobName, String p_rootDir,
			String p_urlPrefix, String p_workflowID, String p_taskID,
			List p_PTF_Ids, List p_PTF_Names, List p_PTF_canUseUrl,
			List p_PSF_Ids, List p_STF_Ids, int p_editorID, int p_platformID,
			String p_encoding, int p_tagDisplayFormatID, String p_uiLocale,
			GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale,
			boolean p_createZip, int p_fileFormat, Vector p_excludeTypes,
			int p_downloadEditAll, List p_supportFiles, int p_resourceOption,
			User p_user)
	{
		super();

		m_jobName = p_jobName;
		m_rootDir = p_rootDir;
		m_urlPrefix = p_urlPrefix;
		m_workflowID = p_workflowID;
		m_taskID = p_taskID;
		m_PTF_Ids = p_PTF_Ids;
		m_PTF_Names = p_PTF_Names;
		m_PTF_canUseUrl = p_PTF_canUseUrl;
		m_PSF_Ids = p_PSF_Ids;
		m_STF_Ids = p_STF_Ids;
		m_editorID = p_editorID;
		m_platformID = p_platformID;

		// Set download encoding:
		//
		// NOTE: In system4 we now recieve Iana encoding names instead of Java
		// encoding names as we did in System3. So, to reduce impact on the
		// offline code we must insure here that the resulting codeset name is
		// converted back to a Java encoding name.
		//
		// NOTE: The jsp sends the words "defaultEncoding" when either
		// word for win or word for mac are selected. When the encoding is set
		// to default, we attempt to set the encoding based on the users
		// choice of editors.
		if (p_PTF_Ids != null)
		{
			if (p_encoding == null
					|| p_encoding.equals(OfflineConstants.ENCODING_DEFAULT))
			{
				if (m_editorID == AmbassadorDwUpConstants.EDITOR_WIN_WORD97
						|| m_editorID == AmbassadorDwUpConstants.EDITOR_WIN_WORD2000
						|| m_editorID == AmbassadorDwUpConstants.EDITOR_WIN_WORD2000_ANDABOVE
						|| m_editorID == AmbassadorDwUpConstants.EDITOR_XLIFF)
				{
					m_encoding = "UnicodeLittle";
				}
				else if (m_editorID == AmbassadorDwUpConstants.EDITOR_MAC_WORD98
						|| m_editorID == AmbassadorDwUpConstants.EDITOR_MAC_WORD2001)
				{
					m_encoding = "UnicodeBig";
				}
				else
				{
					m_encoding = CodesetMapper.getJavaEncoding(p_encoding);
				}
			}
			else
			{
				// when "other" is selected as the editor,
				// the encoding is user selectable.
				m_encoding = CodesetMapper.getJavaEncoding(p_encoding);
			}
		}

		if (CATEGORY.isDebugEnabled())
		{
			CATEGORY.debug("Download encoding = " + m_encoding);
		}

		m_tagDisplayFormatID = p_tagDisplayFormatID;
		m_uiLocale = p_uiLocale;
		m_sourceLocale = p_sourceLocale;
		m_targetLocale = p_targetLocale;
		m_createZip = p_createZip;
		m_fileFormat = p_fileFormat;
		m_downloadEditAll = p_downloadEditAll;
		m_excludeTypeNames = p_excludeTypes;
		m_supportFiles = p_supportFiles;
		m_resourceOption = p_resourceOption;
		m_user = p_user;
	}

	//
	// Public Methods
	//

	/**
	 * Verifies that the download parameters are acceptable.
	 */
	public void verify() throws AmbassadorDwUpException
	{
		// check global requirements
		if (!haveGlobalRequirements())
		{
			AmbassadorDwUpException ex = new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.DOWNLOAD_INVALID_PARAM,
					"Invalid (global) parameters: " + this.toString());
			CATEGORY.error(ex);
			throw ex;
		}

		// check primary target file requirements
		if (!havePrimaryFileRequirements())
		{
			AmbassadorDwUpException ex = new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.DOWNLOAD_INVALID_PARAM,
					"Invalid (Primary Target File) parameters: "
							+ this.toString());
			CATEGORY.error(ex);
			throw ex;
		}

		// check primary source file requirements
		if (!haveSourceFileRequirements())
		{
			AmbassadorDwUpException ex = new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.DOWNLOAD_INVALID_PARAM,
					"Invalid (Primary Source File) parameters: "
							+ this.toString());
			CATEGORY.error(ex);
			throw ex;
		}

		// check secondary target file requirements
		if (!haveSecondaryFileRequirements())
		{
			AmbassadorDwUpException ex = new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.DOWNLOAD_INVALID_PARAM,
					"Invalid (Secondary Target File) parameters: "
							+ this.toString());
			CATEGORY.error(ex);
			throw ex;
		}

		if (!haveSupportFileRequirements())
		{
			AmbassadorDwUpException ex = new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.DOWNLOAD_INVALID_PARAM,
					"Invalid (Support Files) parameters: " + this.toString());
			CATEGORY.error(ex);
			throw ex;
		}

		// If we have nothing, something is wrong
		if (m_PTF_Ids == null && m_PSF_Ids == null && m_STF_Ids == null
				&& m_supportFiles == null)
		{
			AmbassadorDwUpException ex = new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.DOWNLOAD_INVALID_PARAM,
					"Invalid download parameters (no PTF, PSF, STF or SupportFile ids) :"
							+ this.toString());
			CATEGORY.error(ex);
			throw ex;
		}
	}

	/**
	 * Returns the Editor ID.
	 */
	public int getEditorID()
	{
		return m_editorID;
	}

	/**
	 * Returns the Platform ID.
	 */
	public int getPlatformID()
	{
		return m_platformID;
	}

	/**
	 * Returns the platform's linefeed sequence. NOTE: Making a call to verify()
	 * first will confirm a valid platform has been set.
	 * 
	 * @return a String linebreak sequence
	 */
	public String getPlatformLineBreak()
	{
		switch (m_platformID)
		{
		case AmbassadorDwUpConstants.PLATFORM_WIN32:
			m_platformLineBreak = "\r\n";
			break;
		case AmbassadorDwUpConstants.PLATFORM_MAC:
			m_platformLineBreak = "\r";
			break;
		case AmbassadorDwUpConstants.PLATFORM_UNIX:
			m_platformLineBreak = "\n";
			break;
		default:
			m_platformLineBreak = "\r\n";
			break;
		}

		return m_platformLineBreak;
	}

	/**
	 * Creates an empty temporary download file (the download package file).
	 * 
	 * @return File
	 */
	public File createOutputFile() throws AmbassadorDwUpException
	{
		try
		{
			if (m_tmpDownloadFile != null)
			{
				m_tmpDownloadFile.deleteOnExit();
			}

			m_tmpDownloadFile = File.createTempFile("~GS", null);
		}
		catch (Exception ex)
		{
			throw new AmbassadorDwUpException(
					AmbassadorDwUpExceptionConstants.GENERAL_IO_WRITE_ERROR, ex);
		}

		return m_tmpDownloadFile;
	}

	/**
	 * Gets the temporary download file (the download package file).
	 */
	public File getOutputFile()
	{
		return m_tmpDownloadFile;
	}

	/**
	 * Returns the Tag display Format ID.
	 */
	public int getTagDisplayFormatID()
	{
		return m_tagDisplayFormatID;
	}

	/**
	 * Returns the PageList Iterator.
	 */
	public ListIterator getPageListIterator()
	{
		return m_PTF_Ids != null ? m_PTF_Ids.listIterator() : null;
	}

	/**
	 * Returns the Primary Source File ids.
	 */
	public List getPSFileIds()
	{
		return m_PSF_Ids;
	}

	/**
	 * Returns the Secondary Target File ids.
	 */
	public List getSTFileIds()
	{
		return m_STF_Ids;
	}

	/**
	 * Returns the PageList Iterator.
	 */
	public ListIterator getCanUseUrlListIterator()
	{
		return m_PTF_canUseUrl != null ? m_PTF_canUseUrl.listIterator() : null;
	}

	/**
	 * Returns the PageList Iterator.
	 */
	public ListIterator getPageNameListIterator()
	{
		return m_PTF_Names != null ? m_PTF_Names.listIterator() : null;
	}

	/**
	 * Returns the SupportFiles List.
	 */
	public Collection getSupportFilesList()
	{
		return m_supportFiles;
	}

	/**
	 * Returns the codeset for te download pages.
	 */
	public String getEncoding()
	{
		return m_encoding;
	}

	/**
	 * Returns the root of the download/upload directory.
	 */
	public String getUrlPrefix()
	{
		return m_urlPrefix;
	}

	/**
	 * Returns the full job name.
	 */
	public String getFullJobName()
	{
		return m_jobName;
	}

	/**
	 * Returns the job name truncated to ten characters. If the job name is not
	 * defined, the default name "NoJobName" is returned.
	 * 
	 * @return String
	 */
	public String getTruncatedJobName()
	{
		if (m_jobName == null || m_jobName.length() <= 0)
		{
			// If we want a default name, it should be set by the page handler.
			// See SendDownloadFileHandler.
			return "";
		}
		else
		{
			try
			{
				return m_jobName.substring(0,
						AmbassadorDwUpConstants.DOWNLOAD_MAX_FILE_PREFIX_LEN);
			}
			catch (Exception ex)
			{
				// Jobname is already less than 10 characters long.
				return m_jobName;
			}
		}
	}

	/**
	 * Returns the taskID for this workflow.
	 */
	public String getTaskID()
	{
		return m_taskID;
	}

	/**
	 * Returns the workflow ID.
	 */
	public String getWorkflowID()
	{
		return m_workflowID;
	}

	/**
	 * Returns the User Object.
	 */
	public User getUser()
	{
		return m_user;
	}

	/**
	 * Get the excludeTypes as an array of String's.
	 */
	public Vector getExcludedTypeNames()
	{
		return m_excludeTypeNames;
	}

	/**
	 * Returns the user locale.
	 */
	public String getUiLocale()
	{
		return m_uiLocale;
	}

	/**
	 * Returns the source locale.
	 */
	public GlobalSightLocale getSourceLocale()
	{
		return m_sourceLocale;
	}

	/**
	 * Returns the target locale.
	 */
	public GlobalSightLocale getTargetLocale()
	{
		return m_targetLocale;
	}

	/**
	 * Returns the ID of the requested format for the offline file.
	 * 
	 * See AmbassadorDwUpConstants: DOWNLOAD_FILE_FORMAT_LIST_START
	 * DOWNLOAD_FILE_FORMAT_TXT DOWNLOAD_FILE_FORMAT_RTF
	 * DOWNLOAD_FILE_FORMAT_TRADOSRTF DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE
	 * DOWNLOAD_FILE_FORMAT_LIST_END
	 * 
	 * @return - a file format id.
	 */
	public int getFileFormatId()
	{
		return m_fileFormat;
	}

	/**
	 * Get the downloadEditAll state.
	 * 
	 * @return a tri-state value indicating: Yes, No or Unauthorized.
	 * @see AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_*
	 */
	public int getDownloadEditAllState()
	{
		return m_downloadEditAll;
	}

	/**
	 * Get the resource insertion option
	 */
	public int getResInsOption()
	{
		return m_resourceOption;
	}

	/**
	 * This new method will be used to determine when we can use Trados
	 * segmentation markers.
	 * 
	 * @return true if we can use Trados segment markup, false if we cannot
	 */
	public boolean isDownloadForTrados()
	{
		return (m_fileFormat == AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF);
	}

	/**
	 * Returns true if a zip file is requested. Otherwise false.
	 */
	public boolean isCreateZip()
	{
		return m_createZip;
	}

	/**
	 * Returns true if unicode is required. Otherwise false.
	 */
	public boolean isUnicodeRTF()
	{
		if (m_editorID == AmbassadorDwUpConstants.EDITOR_WIN_WORD97
				|| m_editorID == AmbassadorDwUpConstants.EDITOR_WIN_WORD2000
				|| m_editorID == AmbassadorDwUpConstants.EDITOR_MAC_WORD98
				|| m_editorID == AmbassadorDwUpConstants.EDITOR_MAC_WORD2001)
		{
			// as our ordered list stands now, anything above this version
			// (windows or Mac) uses unicode.
			// See AmbassadorDwUpConstants....
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns true if there are no pages included in this download. Otherwise
	 * false.
	 */
	public boolean isSupportFilesOnlyDownload()
	{
		return (m_PTF_Ids == null && m_PSF_Ids == null && m_STF_Ids == null);
	}

	/**
	 * Sets the editor ID.
	 */
	public void setEditorID(int newEditorID)
	{
		m_editorID = newEditorID;
	}

	/**
	 * Sets the zip request flag. Setting it true indicates a request for a zip
	 * file.
	 */
	public void setCreateZip(boolean p_newCreateZip)
	{
		m_createZip = p_newCreateZip;
	}

	/**
	 * Sets the page ID list (primary files).
	 */
	public void setPageIDList(List p_newPageIDList)
	{
		m_PTF_Ids = p_newPageIDList;
	}

	/**
	 * Sets SecondaryTargetFile IDs.
	 * 
	 * @param p_pageIds
	 *            list of secondary target file ids
	 */
	public void setSTFileIds(List p_pageIds)
	{
		m_STF_Ids = p_pageIds;
	}

	/**
	 * Sets the platform ID.
	 */
	public void setPlatformID(int p_newPlatformID)
	{
		m_platformID = p_newPlatformID;
	}

	/**
	 * Sets the tag display format.
	 */
	public void setTagDisplayFormatID(int p_newTagDisplayFormatID)
	{
		m_tagDisplayFormatID = p_newTagDisplayFormatID;
	}

	/**
	 * Sets the codeset for the download pages.
	 */
	public void setEncoding(String p_newEncoding)
	{
		m_encoding = p_newEncoding;
	}

	/**
	 * Sets the page ID list.
	 */
	public void setPageNameList(List p_PTF_Names)
	{
		m_PTF_Names = p_PTF_Names;
	}

	/**
	 * Sets the supportFiles List.
	 * 
	 * @param p_supportFiles
	 *            list of support files.
	 */
	public void setSupportFilesList(List p_supportFiles)
	{
		m_supportFiles = p_supportFiles;
	}

	/**
	 * Sets the root directory for upload/download.
	 */
	public void setUrlPrefix(String p_urlPrefix)
	{
		m_urlPrefix = p_urlPrefix;
	}

	/**
	 * Sets the page ID list.
	 * 
	 * @param p_newPageIDList
	 *            List
	 */
	public void setCanUseUrlList(List p_PTF_canUseUrl)
	{
		m_PTF_canUseUrl = p_PTF_canUseUrl;
	}

	/**
	 * Sets the job name.
	 */
	public void setJobName(String p_jobName)
	{
		m_jobName = p_jobName;
	}

	/**
	 * Sets the task ID.
	 */
	public void setTaskID(String p_taskID)
	{
		m_taskID = p_taskID;
	}

	/**
	 * Sets the workflow ID.
	 */
	public void setWorkflowID(String p_workflowID)
	{
		m_workflowID = p_workflowID;
	}

	/**
	 * Sets the source locale.
	 */
	public void setSourceLocale(GlobalSightLocale p_locale)
	{
		m_sourceLocale = p_locale;
	}

	/**
	 * Sets the target locale.
	 */
	public void setTargetLocale(GlobalSightLocale p_locale)
	{
		m_targetLocale = p_locale;
	}

	/**
	 * Indicates that the page is requested for use as an error checker
	 * reference.
	 * 
	 * @param p_state
	 *            true if requesting a single reference page
	 */
	public void setIsReferencePageParameters()
	{
		m_isReferencePageParameters = true;
	}

	/**
	 * Indicates that the page is requested for use as an error checker
	 * reference.
	 */
	public boolean isReferencePageParameters()
	{
		return m_isReferencePageParameters;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append("JobName=" + m_jobName);
		sb.append(", RootDir=" + m_rootDir);
		sb.append(", URLPrefix=" + m_urlPrefix);
		sb.append(", WorkflowId=" + m_workflowID);
		sb.append(", TaskId=" + m_taskID);

		sb.append(", PageIdList="
				+ ((m_PTF_Ids == null) ? "null" : m_PTF_Ids.toString()));
		sb.append(", PageNameList="
				+ ((m_PTF_Names == null) ? "null" : m_PTF_Names.toString()));
		sb.append(", CanUseUrlList="
				+ ((m_PTF_canUseUrl == null) ? "null" : m_PTF_canUseUrl
						.toString()));

		sb.append(", EditorId=" + m_editorID);
		sb.append(", PlatforId=" + m_platformID);
		sb.append(", Encoding=" + m_encoding);
		sb.append(", TagDisplayformat=" + m_tagDisplayFormatID);
		sb.append(", UILocale="
				+ ((m_uiLocale == null) ? "null" : m_uiLocale.toString()));
		sb.append(", SrcLocale="
				+ ((m_sourceLocale == null) ? "null" : m_sourceLocale
						.toString()));
		sb.append(", TrgLocale="
				+ ((m_targetLocale == null) ? "null" : m_targetLocale
						.toString()));
		sb.append(", CreateZip=" + m_createZip);
		sb.append(", FileFormat=" + m_fileFormat);
		sb.append(", EditAll=" + m_downloadEditAll);
		sb.append(", ResourceLinking=" + m_resourceOption);
		sb.append(", ExcludedTypeNames=");
		if (m_excludeTypeNames == null)
		{
			sb.append("null");
		}
		else
		{
			for (int i = 0, max = m_excludeTypeNames.size(); i < max; i++)
			{
				sb.append(m_excludeTypeNames.get(i) + " ");
			}
		}

		sb.append(", SupportFilesList="
				+ ((m_supportFiles == null) ? "null" : m_supportFiles
						.toString()));

		return sb.toString();
	}

	/**
	 * Returns true if primary target files are included. Otherwise false. NOTE:
	 * Verify() must be called once before calling this method.
	 */
	public boolean hasPrimaryFiles()
	{
		return m_isPrimaryFiles;
	}

	/**
	 * Returns true if primary source files are included. Otherwise false. NOTE:
	 * Verify() must be called once before calling this method.
	 */
	public boolean hasSourceFiles()
	{
		return m_isSourceFiles;
	}

	/**
	 * Returns true if secondary target files are included. Otherwise false.
	 * NOTE: Verify() must be called once before calling this method.
	 */
	public boolean hasSecondaryFiles()
	{
		return m_isSecondaryFiles;
	}

	/**
	 * Returns true if support target files are included. Otherwise false. NOTE:
	 * Verify() must be called once before calling this method.
	 */
	public boolean hasSupportFiles()
	{
		return m_isSupportFiles;
	}

	//
	// Private Methods
	//

	private boolean haveGlobalRequirements()
	{
		boolean result = true;

		if ((m_jobName == null)
				|| (m_workflowID == null || m_workflowID.length() <= 0)
				|| (m_taskID == null || m_taskID.length() <= 0)
				|| (m_sourceLocale == null || m_sourceLocale.getDisplayName()
						.length() <= 0)
				|| (m_targetLocale == null || m_targetLocale.getDisplayName()
						.length() <= 0)
				|| ((m_createZip != true) && (m_createZip != false))
				)
		{
			result = false;
		}

		return result;
	}

	private boolean havePrimaryFileRequirements()
	{
		boolean result = true;

		if (m_PTF_Ids != null)
		{
			if (m_PTF_Ids.isEmpty()
					|| (m_PTF_Names == null || m_PTF_Names.isEmpty())
					|| (m_PTF_canUseUrl == null || m_PTF_canUseUrl.isEmpty())
					|| (m_urlPrefix == null)
					|| (m_encoding == null || m_encoding.length() <= 0)
					|| (m_editorID <= AmbassadorDwUpConstants.EDITOR_LIST_START)
					|| (m_editorID >= AmbassadorDwUpConstants.EDITOR_LIST_END)
					|| (m_resourceOption <= AmbassadorDwUpConstants.MAKE_RES_START)
					|| (m_resourceOption >= AmbassadorDwUpConstants.TOOL_RES_END)
					|| (m_platformID <= AmbassadorDwUpConstants.PLATFORM_LIST_START)
					|| (m_platformID >= AmbassadorDwUpConstants.PLATFORM_LIST_END)
					|| ((m_tagDisplayFormatID != PseudoConstants.PSEUDO_COMPACT) && (m_tagDisplayFormatID != PseudoConstants.PSEUDO_VERBOSE))
					|| ((m_fileFormat <= AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_LIST_START) || (m_fileFormat >= AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_LIST_END))
					|| ((m_downloadEditAll != AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_NO)
							&& (m_downloadEditAll != AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_YES) && (m_downloadEditAll != AmbassadorDwUpConstants.DOWNLOAD_EDITALL_STATE_UNAUTHORIZED)))
			{
				m_isPrimaryFiles = result = false;
			}
			else
			{
				m_isPrimaryFiles = result = true;
			}
		}

		return result;
	}

	private boolean haveSourceFileRequirements()
	{
		boolean result = true;

		if (m_PSF_Ids != null)
		{
			if (m_PSF_Ids.isEmpty())
			{
				result = false;
			}
			else
			{
				m_isSourceFiles = true;
			}
		}

		return result;
	}

	private boolean haveSecondaryFileRequirements()
	{
		boolean result = true;

		if (m_STF_Ids != null)
		{
			if (m_STF_Ids.isEmpty())
			{
				result = false;
			}
			else
			{
				m_isSecondaryFiles = true;
			}
		}

		return result;
	}

	private boolean haveSupportFileRequirements()
	{
		boolean result = true;

		if (m_supportFiles != null)
		{
			if (m_supportFiles.isEmpty())
			{
				result = false;
			}
			else
			{
				m_isSupportFiles = true;
			}
		}

		return result;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public void setSessionId(String sessionId)
	{
		this.sessionId = sessionId;
	}

    public boolean isConsolidateTmxFiles()
    {
        return consolidateTmxFiles;
    }

    public void setConsolidateTmxFiles(boolean consolidateTmxFiles)
    {
        this.consolidateTmxFiles = consolidateTmxFiles;
    }

	public String getDisplayExactMatch()
	{
		return displayExactMatch;
	}

	public void setDisplayExactMatch(String displayExactMatch)
	{
		this.displayExactMatch = displayExactMatch;
	}

	public JobPackageZipper getZipper() {
		return zipper;
	}

	public void setZipper(JobPackageZipper zipper) {
		this.zipper = zipper;
	}

    public boolean isConsolidateTermFiles()
    {
        return consolidateTermFiles;
    }

    public void setConsolidateTermFiles(boolean consolidateTermFiles)
    {
        this.consolidateTermFiles = consolidateTermFiles;
    }
    
    public boolean createTermFiles()
    {
        return !OfflineConstants.TERM_NONE.equals(termFormat);
    }

    public String getTermFormat()
    {
        return termFormat;
    }

    public void setTermFormat(String termFormat)
    {
        this.termFormat = termFormat;
    }
    
    public Job getJob()
    {
        return m_job;
    }

    public void setJob(Job p_job)
    {
        this.m_job = p_job;
    }
    
    public ArrayList getAutoActionNodeEmail() {
        return this.autoActionNodeEmail;
    }
    
    public void setAutoActionNodeEmail(ArrayList nodeEmail)
    {
        this.autoActionNodeEmail = nodeEmail;
    }
    
    public void setChangeCreationIdForMTSegments(boolean p_changeCreationIdForMTSegments)
    {
    	this.m_changeCreationIdForMTSegments = p_changeCreationIdForMTSegments;
    }
    
    public boolean getChangeCreationIdForMTSegments()
    {
    	return this.m_changeCreationIdForMTSegments;
    }

    public boolean isPopulate100()
    {
        return populate100;
    }

    public void setPopulate100(boolean populate100)
    {
        this.populate100 = populate100;
    }

    public boolean isPopulateFuzzy()
    {
        return populateFuzzy;
    }

    public void setPopulateFuzzy(boolean populateFuzzy)
    {
        this.populateFuzzy = populateFuzzy;
    }

	public boolean isNeedConsolidate() {
		return needConsolidate;
	}

	public void setNeedConsolidate(boolean needConsolidate) {
		if (m_fileFormat != AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF)
			this.needConsolidate = false;
		else
			this.needConsolidate = needConsolidate;
	}
    
    public String getActivityType() {
        return this.activityType;
    }
    
    public void setActivityType(String name) {
        this.activityType =  name;
    }
}
