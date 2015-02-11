/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.edit.offline.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import jxl.Sheet;
import jxl.Workbook;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.PageData;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileMgr;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReviewerLisaQAXlsReportHelper;
import com.globalsight.everest.webapp.pagehandler.edit.EditCommonHelper;
import com.globalsight.ling.rtf.RtfAPI;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * Contains methods responsible for error-checking and saving uploaded content.
 */
public class UploadApi implements AmbassadorDwUpConstants
{
	static private final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
	        .getLogger(UploadApi.class);

	private PageData m_referencePageData = null;
	
	private ArrayList<PageData> m_referencePageDatas = null;
	
	private OfflinePageData m_uploadPageData = null;

	private PtagErrorPageWriter m_errWriter = null;

	private OfflinePtagErrorChecker m_errChecker = null;

	private ResourceBundle m_messages = null;

	private GlobalSightLocale m_uiLocale = null;

	private UploadPageSaver m_uploadPageSaver = null;

	private String m_unextractedFilenameHead = null;

	private String m_unextractedFileId = null;

	private String m_unextractedFileType = null;

	private String m_unextractedFileTaskId = null;

	private String m_unextractedFilenameTail = null;

	private String m_unextractedFilenameExt = null;

	private String m_normalizedLB = null;

	private String SECONDARY_TYPE = "S";

	// For uploading report
	private Map segId2RequiredTranslation = null;

	private Map segId2Comment = null;

	private Map segId2PageId = null;

	private Map segId2FailureType = null;

	private long reportTargetLocaleId = -1;

	public static final int REPORT_COMMENT_UPLOAD = 1;

	public static final int REPORT_TRANSLATION_UPLOAD = 2;

	static private int RE_UNEXTRACTED_FILE_HEAD = 1; // Regex position

	static private int RE_UNEXTRACTED_FILE_ID = 2; // Regex position

	static private int RE_UNEXTRACTED_FILE_TYPE = 3; // Regex position

	static private int RE_UNEXTRACTED_FILE_TASK_ID = 4; // Regex position

	static private int RE_UNEXTRACTED_FILE_TAIL = 5; // Regex position

	static private int RE_UNEXTRACTED_FILE_EXT = 2; // Regex position

	static private final REProgram RE_UNEXTRACTED_FILE_FNAME = createProgram("^(.*?)"
	        + // (head)
	        AmbassadorDwUpConstants.FILE_NAME_BREAK + "([0-9]+)([S||s||P||p])" + // (pageid)(type)
	        AmbassadorDwUpConstants.FILE_NAME_BREAK + "([0-9]+)" + // (taskid)
	        "(.*?)$"); // optional (tail)

	static private final REProgram RE_UNEXTRACTED_FILE_FNAME_EXT = createProgram("(.*)\\.(.*)$");;

	static private REProgram createProgram(String p_pattern)
	{
		REProgram pattern = null;

		try
		{
			RECompiler compiler = new RECompiler();
			pattern = compiler.compile(p_pattern);
		}
		catch (RESyntaxException ex)
		{
			// Pattern syntax error. Stop the application.
			throw new RuntimeException(ex.getMessage());
		}

		return pattern;
	}

	//
	// Constructors
	//

	/**
     * Default constructor.
     */
	public UploadApi() throws AmbassadorDwUpException
	{
		super();

		m_errWriter = new PtagErrorPageWriter();

		try
		{
			m_uploadPageSaver = new UploadPageSaver();
		}
		catch (Exception ex)
		{
			throw new AmbassadorDwUpException(ex);
		}
	}

	//
	// Public Methods
	//

	/**
     * Processes a single upload page (list view text file).
     * 
     * If the page contains errors, an HTML error page is returned as a string.
     * If no errors occur, segmemts are converted back to GXML and written to
     * database.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_inputStream
     *            a stream opened on the input file..
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId -
     *            the task Id that the upload page must match.
     * @param p_fileName -
     *            The name of the file to be uploaded. Used for email
     *            notification.
     * @param p_jmsQueueDestination -
     *            The JMS destination queue for performing the save and indexing
     *            process in the background.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
	public String processPage(Reader p_reader, String p_sessionId, User p_user,
	        long p_ownerTaskId, String p_fileName,
	        Collection p_excludedItemTypes, String p_jmsQueueDestination)
	{
		//m_referencePageData = new PageData();
		// so getPage() will be cleared if errors occur
		m_uploadPageData = new OfflinePageData();
		
		m_referencePageDatas = new ArrayList<PageData>();

		String errPage;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user, p_reader)) != null)
		{
			return errPage;
		}

		// load the upload file
		if ((errPage = loadListViewTextFile(p_reader, p_fileName, false)) != null)
		{
			CATEGORY.error("UploadApi.processPage(): "
			        + "Unable to load the upload-file.");

			return errPage;
		}
		
		//Verify if upload file is a consolated file
		if (m_uploadPageData.getPageId().indexOf(",") > 0)
			m_uploadPageData.setConsolated(true);

		if ((errPage = postLoadInit(p_ownerTaskId,
		        m_uploadPageData.getTaskId(), p_excludedItemTypes,
		        DOWNLOAD_FILE_FORMAT_TXT)) != null)
		{
			return errPage;
		}

		// Check uploaded page for errors. If there are no errors - save it
		if ((errPage = checkPage(p_user)) != null)
		{
			return errPage;
		}

		if ((errPage = save(m_uploadPageData, m_referencePageDatas, p_jmsQueueDestination, p_user,
		        p_fileName)) != null)
		{
			return errPage;
		}

		return null;
	}

	/**
     * Processes a single upload page (RTF paragraph view).
     * 
     * If the page contains errors, an HTML error page is returned as a string.
     * If no errors occur, segmemts are converted back to GXML and written to
     * database.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_rtfDoc
     *            an RTF DOM representing the uploaded content.
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId -
     *            the task Id that the upload page must match.
     * @param p_fileName -
     *            The name of the file to be uploaded. Used for email
     *            notification.
     * @param p_jmsQueueDestination -
     *            The JMS queue used for performing the save and indexing
     *            process in the background.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
	public String process_GS_PARAVIEW_1(RtfDocument p_rtfDoc,
	        String p_sessionId, User p_user, long p_ownerTaskId,
	        String p_fileName, Collection p_excludedItemTypes,
	        String p_jmsQueueDestination)
	{
		//m_referencePageData = new PageData();
		// so getPage() will be cleared if errors occur
		m_uploadPageData = new OfflinePageData();

		String errPage = null;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user, p_rtfDoc)) != null)
		{
			return errPage;
		}

		// load the upload file
		if ((errPage = load_GS_PARAVIEW_1_File(p_rtfDoc, p_fileName)) != null)
		{
			CATEGORY.error("UploadApi.process_GS_PARAVIEW_1(): "
			        + "Unable to load the upload-file.");

			return errPage;
		}

		if ((errPage = postLoadInit(p_ownerTaskId,
		        m_uploadPageData.getTaskId(), p_excludedItemTypes,
		        DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)) != null)
		{
			return errPage;
		}

		// Check uploaded page for errors. If there are no errors - save it
		if ((errPage = checkPage(p_user)) != null)
		{
			return errPage;
		}

		// Now we're ready to save.
		if ((errPage = save(m_uploadPageData, m_referencePageDatas, p_jmsQueueDestination, p_user,
		        p_fileName)) != null)
		{
			return errPage;
		}

		return null;
	}

	public String processReport(File p_tempFile, String p_sessionId,
	        User p_user, long p_ownerTaskId, String p_fileName,
	        String p_jmsQueueDestination, String p_reportName) throws Exception
	{
		String errPage = null;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user)) != null)
		{
			return errPage;
		}

		// load the upload file
		if ((errPage = loadReportData(p_tempFile, p_fileName, p_ownerTaskId,
		        p_reportName)) != null)
		{
			CATEGORY.error("UploadApi.loadReportData(): "
			        + "Unable to load the upload-file.");

			return errPage;
		}

		if (p_reportName.equals(WebAppConstants.TRANSLATION_EDIT))
		{
			if ((errPage = createErrorChecker()) != null)
			{
				return errPage;
			}

			// Check uploaded page for errors. If there are no errors - save it
			if ((errPage = checkReportPage(p_user)) != null)
			{
				return errPage;
			}
		}
		else if (p_reportName.equals(WebAppConstants.LANGUAGE_SIGN_OFF))
		{
			uploadComments(p_user.getUserId());
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private String loadReportData(File p_tempFile, String p_fileName,
	        long p_taskId, String p_reportName)
	{

		try
		{
			m_errWriter.setFileName(p_fileName);
			String fileSuff = p_fileName.substring(p_fileName.lastIndexOf("."));

			if (!fileSuff.equalsIgnoreCase(".xls"))
			{
				m_errWriter
				        .addFileErrorMsg("The file you are trying to upload is not an excel (xls format)."
				                + "\r\nPlease make sure it is correct and upload again.");
				return m_errWriter.buildReportErroPage().toString();
			}
			InputStream is = new FileInputStream(p_tempFile);

			Workbook readWorkbook = Workbook.getWorkbook(is);
			Sheet sheet = readWorkbook.getSheet(0);

			int languageInfoRow = ReviewerLisaQAXlsReportHelper.LANGUAGE_INFO_ROW;
			int segmentHeaderRow = ReviewerLisaQAXlsReportHelper.SEGMENT_HEADER_ROW;
			int segmentStartRow = ReviewerLisaQAXlsReportHelper.SEGMENT_START_ROW;
			String targetLanguage = sheet.getCell(1, languageInfoRow)
			        .getContents();
			if (targetLanguage == null || targetLanguage.equals(""))
			{
				m_errWriter
				        .addFileErrorMsg("No language information detected.");
				return m_errWriter.buildReportErroPage().toString();
			}
			else if (targetLanguage.indexOf('[') < 0
			        || targetLanguage.indexOf(']') < 0)
			{
				m_errWriter
				        .addFileErrorMsg("Target language format is not correct.\r\nIt should "
				                + "contain a portion which is a locale code encolsed by [ ] such as [zh_CN]");
				return m_errWriter.buildReportErroPage().toString();
			}
			reportTargetLocaleId = getLocaleId(targetLanguage);

			Task task = ServerProxy.getTaskManager().getTask(p_taskId);
			long jobId = task.getJobId();
			Set<String> jobIds = new HashSet<String>();

			if (p_reportName.equals(WebAppConstants.TRANSLATION_EDIT))
			{
				if (!sheet.getCell(0, segmentHeaderRow).getContents()
				        .equalsIgnoreCase("Job id")
				        || !sheet.getCell(1, segmentHeaderRow).getContents()
				                .equalsIgnoreCase("Segment id")
				        || !sheet.getCell(2, segmentHeaderRow).getContents()
				                .equalsIgnoreCase("TargetPage id")
				        || !sheet.getCell(7, segmentHeaderRow).getContents()
				                .startsWith("Required translation"))
				{
					m_errWriter
					        .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
					                + "\r\nMaybe you have changed some column header signatures or orders."
					                + "\r\nThe following column header signatures and orders should keep the source report's format."
					                + "\r\nJob id, Segment id, TargetPage id, Required translation."
					                + "\r\nPlease make sure they are correct and upload again.");
					return m_errWriter.buildReportErroPage().toString();
				}
				else
				{
					segId2RequiredTranslation = new HashMap();
					String segmentId = null;
					String updatedText = null;
					String jobIdText = null;
					boolean hasSegmentIdErro = false;
					for (int j = segmentStartRow, row = sheet.getRows(); j < row; j++)
					{
						segmentId = sheet.getCell(1, j).getContents();
						updatedText = sheet.getCell(7, j).getContents();
						jobIdText = sheet.getCell(0, j).getContents();
						jobIds.add(jobIdText);

						if (updatedText != null && !updatedText.equals(""))
						{
							if (segmentId != null && !segmentId.equals(""))
							{
								segId2RequiredTranslation.put(new Long(Long
								        .parseLong(segmentId)), updatedText);
							}
							else
							{
								m_errWriter
								        .addFileErrorMsg("Segment id is lost in row "
								                + (j + 1) + "\r\n");
								hasSegmentIdErro = true;
							}

						}

					}
					if (hasSegmentIdErro)
					{
						return m_errWriter.buildReportErroPage().toString();
					}

					if (jobIds.size() > 1)
					{
						m_errWriter
						        .addFileErrorMsg("The job id is not consistent, you may hava changed some of them."
						                + "\r\nPlease make sure they are correct and upload again.");
						return m_errWriter.buildReportErroPage().toString();
					}
					else if ((jobIds.size() == 1)
					        && !jobIds.contains(String.valueOf(jobId)))
					{
						m_errWriter
						        .addFileErrorMsg("The file you are uploading does not belong to this job."
						                + "\r\nPlease make sure they are correct and upload again.");
						return m_errWriter.buildReportErroPage().toString();
					}
					else if (jobIds.size() == 0)
					{
						m_errWriter
						        .addFileErrorMsg("No job id detected."
						                + "\r\nPlease make sure they are correct and upload again.");
						return m_errWriter.buildReportErroPage().toString();
					}
				}

			}

			else if (p_reportName.equals(WebAppConstants.LANGUAGE_SIGN_OFF))
			{

				if (!sheet.getCell(0, segmentHeaderRow).getContents()
				        .equalsIgnoreCase("Job id")
				        || !sheet.getCell(1, segmentHeaderRow).getContents()
				                .equalsIgnoreCase("Segment id")
				        || !sheet.getCell(2, segmentHeaderRow).getContents()
				                .equalsIgnoreCase("Page name")
				        || !sheet.getCell(6, segmentHeaderRow).getContents()
				                .startsWith("Comment"))
				{
					m_errWriter
					        .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
					                + "\r\nMaybe you have changed some column header signature or orders."
					                + "\r\nThe following column header signatrues and orders should keep the source report's format."
					                + "\r\nJob id, Segment id, Page name, Comment(free hand your comments)."
					                + "\r\nPlease make sure they are correct and upload again.");
					return m_errWriter.buildReportErroPage().toString();
				}
				else
				{
					segId2Comment = new HashMap();
					segId2PageId = new HashMap();
					segId2FailureType = new HashMap();
					String segmentId = null;
					String pageId = null;
					String comment = null;
					Long segIdLong = null;
					String jobIdText = null;
					String failureType = null;
					boolean hasIdErro = false;
					for (int k = segmentStartRow, row = sheet.getRows(); k < row; k++)
					{
						segmentId = sheet.getCell(1, k).getContents();
						Tu tu = ServerProxy.getTuvManager()
								.getTuForSegmentEditor(
										Long.parseLong(segmentId));
						TuImpl tuImpl = (TuImpl) tu;
						Tuv tuv = tuImpl.getCurrentTuv(reportTargetLocaleId);
						TuvImpl tuvImpl = (TuvImpl) tuv;
						TargetPage targetPage = tuvImpl.getTargetPage();
						pageId = new String(String.valueOf(targetPage.getId()));
						comment = sheet.getCell(6, k).getContents();
						failureType = sheet.getCell(7, k).getContents();

						jobIdText = sheet.getCell(0, k).getContents();

						jobIds.add(jobIdText);
						segIdLong = new Long(Long.parseLong(segmentId));
						segId2FailureType.put(segIdLong, failureType);

						if (comment != null && !comment.equals(""))
						{
							if (segmentId != null && !segmentId.equals("")
							        && pageId != null && !pageId.equals(""))
							{

								segId2PageId.put(segIdLong, new Long(Long
								        .parseLong(pageId)));
								segId2Comment.put(segIdLong, comment);
							}
							else
							{
								m_errWriter
								        .addFileErrorMsg("Segment or Page id is lost in row "
								                + (k + 1) + "\r\n");
								hasIdErro = true;
							}

						}

					}
					if (hasIdErro)
					{
						return m_errWriter.buildReportErroPage().toString();
					}

					if (jobIds.size() > 1)
					{
						m_errWriter
						        .addFileErrorMsg("The job id is not consistent, you may change some of them."
						                + "\r\nPlease make sure they are correct and upload again.");
						return m_errWriter.buildReportErroPage().toString();
					}
					else if ((jobIds.size() == 1)
					        && !jobIds.contains(String.valueOf(jobId)))
					{
						m_errWriter
						        .addFileErrorMsg("The file you are uploading does not belong to this job."
						                + "\r\nPlease make sure they are correct and upload again.");
						return m_errWriter.buildReportErroPage().toString();
					}
					else if (jobIds.size() == 0)
					{
						m_errWriter
						        .addFileErrorMsg("No job id detected."
						                + "\r\nPlease make sure they are correct and upload again.");
						return m_errWriter.buildReportErroPage().toString();
					}
				}

			}
			else
			{
				m_errWriter
				        .addFileErrorMsg("The report type is not correct."
				                + "\r\nPlease make sure the report type is correct and upload again.");
				return m_errWriter.buildReportErroPage().toString();
			}

		}
		catch (Throwable ex)
		{
			String args[] = { EditUtil.encodeHtmlEntities(ex.getMessage()) };
			String errMsg = MessageFormat.format(m_messages
			        .getString("FormatTwoLoadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildReportErroPage().toString();
		}

		return null;

	}

	private void uploadComments(String p_userId) throws Exception
	{
		TuvManager tuvManager = ServerProxy.getTuvManager();
		CommentManager commentManager = ServerProxy.getCommentManager();

		Set tuIds = segId2Comment.keySet();
		Iterator tuIdIterator = tuIds.iterator();
		long tuId = -1;
		long tuvId = -1;
		Long tuIdLong = null;
		String comment = null;
		String failureType = null;
		long targetPageId = -1;
		List existIssues = null;
		while (tuIdIterator.hasNext())
		{
			tuIdLong = (Long) tuIdIterator.next();
			tuId = tuIdLong.longValue();
			Tuv tuv = tuvManager.getTuvForSegmentEditor(tuId,
			        reportTargetLocaleId);
			tuvId = tuv.getId();
			comment = (String) segId2Comment.get(tuIdLong);
			targetPageId = ((Long) segId2PageId.get(tuIdLong)).longValue();
			failureType = (String) segId2FailureType.get(tuIdLong);
			if (comment != null && comment != "")
			{
				existIssues = commentManager.getIssues(Issue.TYPE_SEGMENT,
				        CommentHelper.makeLogicalKey(targetPageId, tuId, tuvId,
				                ""));

				if (existIssues == null)
				{
					if (failureType != null && !failureType.equals(""))
					{
						if (Issue.CATEGORY_SPELLING.replaceAll(",", "")
						        .equalsIgnoreCase(failureType.trim()))
						{
							failureType = Issue.CATEGORY_SPELLING;
						}
						commentManager.addIssue(Issue.TYPE_SEGMENT, tuvId,
						        "Comment by LSO", Issue.PRI_MEDIUM,
						        Issue.STATUS_OPEN, failureType.trim(),
						        p_userId, comment, CommentHelper
						                .makeLogicalKey(targetPageId, tuId,
						                        tuvId, 0));
					}
					else
					{
						commentManager.addIssue(Issue.TYPE_SEGMENT, tuvId,
						        "Comment by LSO", Issue.PRI_MEDIUM,
						        Issue.STATUS_OPEN, Issue.CATEGORY_TYPE01,
						        p_userId, comment, CommentHelper
						                .makeLogicalKey(targetPageId, tuId,
						                        tuvId, 0));
					}
					Thread.sleep(1000);

				}

				else
				{
					if (existIssues.size() > 0)
					{
						List histories = null;
						// boolean edited = false;
						for (int i = 0; i < existIssues.size(); i++)
						{
							Issue issue = (Issue) existIssues.get(i);
							histories = issue.getHistory();

							if (histories != null && histories.size() > 0)
							{
								IssueHistory history = (IssueHistory) histories
								        .get(0);
								if (history.reportedBy().equals(p_userId))
								{

									if (failureType != null
									        && !failureType.equals(""))
									{
										if (Issue.CATEGORY_SPELLING.replaceAll(
										        ",", "").equalsIgnoreCase(
										        failureType.trim()))
										{
											failureType = Issue.CATEGORY_SPELLING;
										}
										commentManager.editIssue(issue.getId(),
										        issue.getTitle(), issue
										                .getPriority(), issue
										                .getStatus(),
										        failureType.trim(), p_userId,
										        comment);
									}
									else
									{
										commentManager.editIssue(issue.getId(),
										        issue.getTitle(), issue
										                .getPriority(), issue
										                .getStatus(), issue
										                .getCategory(),
										        p_userId, comment);
									}
									Thread.sleep(1000);

									// edited = true;
								}
								else
								{
									// commentManager.replyToIssue(issue.getId(),
									// "Comment by LSO",
									// Issue.PRI_MEDIUM, Issue.STATUS_OPEN,
									// Issue.CATEGORY_TYPE01, p_userId,
									// comment);

									if (failureType != null
									        && !failureType.equals(""))
									{
										if (Issue.CATEGORY_SPELLING.replaceAll(
										        ",", "").equalsIgnoreCase(
										        failureType.trim()))
										{
											failureType = Issue.CATEGORY_SPELLING;
										}
										commentManager.replyToIssue(issue
										        .getId(), issue.getTitle(),
										        issue.getPriority(), issue
										                .getStatus(),
										        failureType.trim(), p_userId,
										        comment);
									}
									else
									{
										commentManager.replyToIssue(issue
										        .getId(), issue.getTitle(),
										        issue.getPriority(), issue
										                .getStatus(), issue
										                .getCategory(),
										        p_userId, comment);
									}
									Thread.sleep(1000);
								}
							}

						}

					}

					else
					{
					    if (failureType != null
                                && !failureType.equals(""))
                        {
                            if (Issue.CATEGORY_SPELLING.replaceAll(
                                    ",", "").equalsIgnoreCase(
                                    failureType.trim()))
                            {
                                failureType = Issue.CATEGORY_SPELLING;
                            }
                        }
					    else
					    {
					        failureType = Issue.CATEGORY_TYPE01;
					    }
						commentManager.addIssue(Issue.TYPE_SEGMENT, tuvId,
						        "Comment by LSO", Issue.PRI_MEDIUM,
						        Issue.STATUS_OPEN, failureType,
						        p_userId, comment, CommentHelper
						                .makeLogicalKey(targetPageId, tuId,
						                        tuvId, 0));
						Thread.sleep(1000);
					}

				}

			}
		}
	}

	private long getLocaleId(String language) throws Exception
	{
		String locale = null;
		long localeId = -1;
		int startIndex = language.indexOf("[");
		int endIndex = language.indexOf("]");
		if (startIndex != -1 && endIndex != -1)
		{
			locale = language.substring(startIndex + 1, endIndex);
			localeId = ServerProxy.getLocaleManager().getLocaleByString(locale)
			        .getId();
		}
		return localeId;
	}

	/**
     * Processes a single upload page (Unicode text file, RTF list view).
     * 
     * If the page contains errors, an HTML error page is returned as a string.
     * If no errors occur, segmemts are converted back to GXML and written to
     * database.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_rtfDoc
     *            an RTF DOM representing the uploaded content.
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId -
     *            the task Id that the upload page must match.
     * @param p_fileName -
     *            The name of the file to be uploaded. Used for email
     *            notification.
     * @param p_jmsQueueDestination -
     *            The JMS queue used for performing the save and indexing
     *            process in the background.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
	public String process_GS_WRAPPED_UNICODE_TEXT(RtfDocument p_rtfDoc,
	        String p_sessionId, User p_user, long p_ownerTaskId,
	        String p_fileName, Collection p_excludedItemTypes,
	        String p_jmsQueueDestination)
	{
		//m_referencePageData = new OfflinePageData();
		// so getPage() will be cleared if errors occur
		m_uploadPageData = new OfflinePageData();

		String errPage = null;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user, p_rtfDoc)) != null)
		{
			return errPage;
		}

		// load the upload file
		if ((errPage = load_GS_WRAPPED_UNICODE_TEXT_File(p_rtfDoc, p_fileName)) != null)
		{
			CATEGORY.error("process_GS_WRAPPED_UNICODE_TEXT: "
			        + "Unable to load the upload-file.");

			return errPage;
		}

		if ((errPage = postLoadInit(p_ownerTaskId,
		        m_uploadPageData.getTaskId(), p_excludedItemTypes,
		        DOWNLOAD_FILE_FORMAT_TXT)) != null)
		{
			return errPage;
		}

		// Check uploaded page for errors. If there are no errors - save it
		if ((errPage = checkPage(p_user)) != null)
		{
			return errPage;
		}

		if ((errPage = save(m_uploadPageData, m_referencePageDatas, p_jmsQueueDestination, p_user,
		        p_fileName)) != null)
		{
			return errPage;
		}

		return null;
	}

	/**
     * Processes an unextracted file.
     * 
     * The name of an Unextracted file must adhere to the following syntax:
     * 
     * [FILENAME]_[FILEID[FILETYPE]]_[TASKID][EXTENSION]
     * 
     * Where: FILENAME = optional (user defined) filename '_' = a mandatory dash
     * FILEID = the System4 generated id for this unextracted file FILETYPE =
     * the target type: 'P' == Primary, 'S' == Secondary '_' = a mandatory dash
     * TASKID = the task Id that this file was downloaded for EXTENSION =
     * optional (user defined) extension
     * 
     * Example: MyFileName_1001P_298.html
     * 
     * If the filename contains errors, an HTML error page is returned. If no
     * errors occur, the file is sent to the NativeFileManager to be saved.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_tmpFile
     *            the upload temp file.
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId -
     *            the task Id that the upload page must match.
     * @param p_fileName -
     *            The name of the file to be uploaded. Used for email
     *            notification.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
	public String doUnextractedFileUpload(File p_tmpFile, String p_sessionId,
	        User p_user, long p_ownerTaskId, String p_fileName)
	{
		String errPage = null;
		GlobalSightLocale sourceLocale;
		GlobalSightLocale targetLocale;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user,
		        p_tmpFile)) != null)
		{
			return errPage;
		}

		// Load:
		// NOTE: There is no formal load into an OfflinePageData since this is
		// am unextracted file. The load consists of verify the filename
		// syntax and extracting the routing ids from the filename.
		if (!parseUnextractedFilename(p_fileName))
		{
			String[] args = { p_fileName };
			String errMsg = MessageFormat.format(m_messages
			        .getString("InvalidUnextractedFilename"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(EditUtil.encodeHtmlEntities(errMsg));
			return m_errWriter.buildPage().toString();
		}

		// Perform post load checks specific to unextracted files:

		// verify the filename taskID is the same as the ownerTaskId
		if ((errPage = confirmValidFileTaskId(p_ownerTaskId,
		        m_unextractedFileTaskId)) != null)
		{
			return errPage;
		}

		// verify that an stf or primary target file by this id exists
		// in this workflow
		if (!isValidUnextractedFileId(m_unextractedFileTaskId,
		        m_unextractedFileId, m_unextractedFileType))
		{
			String args[] = { m_unextractedFileId };
			String errMsg = MessageFormat.format(m_messages
			        .getString("InvalidUnextractedFileId"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		// Save file:
		// Handles an un-extracted primary target or a secondary target file
		try
		{
			if (m_unextractedFileType.equalsIgnoreCase(SECONDARY_TYPE))
			{
				SecondaryTargetFileMgr stfMgr = ServerProxy
				        .getSecondaryTargetFileManager();
				SecondaryTargetFile stf = stfMgr.getSecondaryTargetFile(Long
				        .parseLong(m_unextractedFileId));
				sourceLocale = stf.getWorkflow().getJob().getSourceLocale();
				targetLocale = stf.getWorkflow().getTargetLocale();

				// re-assemble upload filename (without IDs)
				StringBuffer sb = new StringBuffer();
				sb.append(m_unextractedFilenameHead);
				sb.append(m_unextractedFilenameTail);

				ServerProxy.getNativeFileManager().save(stf, p_tmpFile, p_user,
				        sb.toString());
			}
			else
			// if m_unextractedFileType.equalsIgnoreCase(PRIMARY_TYPE)
			{
				PageManager pm = ServerProxy.getPageManager();
				TargetPage tp = pm.getTargetPage(Long
				        .parseLong(m_unextractedFileId));
				// assumes that this file contains an un-extraced file -
				// otherwise
				// this place in the code wouldn't have been reached
				UnextractedFile uf = (UnextractedFile) tp.getPrimaryFile();

				sourceLocale = tp.getWorkflowInstance().getJob()
				        .getSourceLocale();
				targetLocale = tp.getWorkflowInstance().getTargetLocale();

				// verify the upload extension
				if (!compareExtension(uf.getStoragePath(),
				        m_unextractedFilenameExt))
				{
					String[] args = { uf.getStoragePath().toLowerCase(),
					        m_unextractedFilenameExt.toLowerCase() };
					String errMsg = MessageFormat.format(m_messages
					        .getString("ContentMismatch"), (Object[]) args);
					m_errWriter.addSystemErrorMsg(errMsg);
					return m_errWriter.buildPage().toString();
				}
				else
				{
					ServerProxy.getNativeFileManager().save(uf, p_tmpFile,
					        p_user);
					// Persist the updated unextracted file info.
					// The user, modify date and file length was updated in the
					// "save" method.
					pm.updateUnextractedFileInfo(tp);
				}
			}

			// Send success e-mail:
			// We only need to do this to be consistant with extracted uploads.
			// For an extracted upload, we need the user to wait till the Tuvs
			// are saved and indexed (via JMS) before finishing the task.
			// Since we do not use JMS here, there is need to send a failure
			// e-mail, they will get the Error page in the UI right away.
			String localePair = OfflineEditHelper.localePair(sourceLocale,
			        targetLocale, m_uiLocale);
			OfflineEditHelper.notifyUser(p_user, p_fileName, localePair,
			        OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
			        OfflineEditHelper.UPLOAD_SUCCESSFUL_MESSAGE);
		}
		catch (Exception ex)
		{
			CATEGORY.error("Unable to save un-extracted file:", ex);
			m_errWriter.addSystemErrorMsg(ex.toString());
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	//
	// Private methods
	//

	/**
     * Loads the upload file into an OfflinePageData object.
     * 
     * @param p_reader
     *            a stream opened on the upload file.
     * @param p_keepIssues
     *            when an OfflinePageData object is called *twice* to load data,
     *            this parameter allows to keep issues read in the first run
     *            (the second run normally clears the entire object). This is
     *            necessary for RTF list view which first parses the RTF, then
     *            loads the textual content as list view text file.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error report page is returned.
     */
	private String loadListViewTextFile(Reader p_reader, String p_fileName,
	        boolean p_keepIssues)
	{

		try
		{
			p_reader.mark(0);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}

		String errPage = null;

		// Set the linefeed normalization sequence.
		if ((errPage = getLFNormalizationSequence()) != null)
		{
			return errPage;
		}

		// Read the upload file into an OfflinePageData object.
		try
		{
			m_errWriter.setFileName(p_fileName);
			m_uploadPageData.setLoadConversionLineBreak(m_normalizedLB);
			m_uploadPageData.loadOfflineTextFile(p_reader, false);

			// set err writer's page, task and job ids
			m_errWriter.processOfflinePageData(m_uploadPageData);
		}
		catch (Throwable ex)
		{
			String args[] = { EditUtil.encodeHtmlEntities(ex.getMessage()) };

			try
			{
				p_reader.reset();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			bindErrMsg(args, p_reader);

			String errMsg = MessageFormat.format(m_messages
			        .getString("FormatTwoLoadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	/**
     * Loads the upload file into an OfflinePageData object.
     * 
     * @param p_rtfDoc
     *            the rtf DOM.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error report page is returned.
     */
	private String load_GS_PARAVIEW_1_File(RtfDocument p_rtfDoc,
	        String p_fileName)
	{
		String errPage = null;

		// Set the linefeed normalization sequence.
		if ((errPage = getLFNormalizationSequence()) != null)
		{
			return errPage;
		}

		// Load file into OfflinePageData.
		try
		{
			m_errWriter.setFileName(p_fileName);
			m_uploadPageData.setLoadConversionLineBreak(m_normalizedLB);
			m_uploadPageData.loadParaViewOneWorkFile(p_rtfDoc);

			// set err writer's page, task and job ids
			m_errWriter.processOfflinePageData(m_uploadPageData);
		}
		catch (Throwable ex)
		{
			String msg = "";

			if (ex instanceof GeneralException)
			{
				msg = ((GeneralException) ex)
				        .getMessage(m_uiLocale.getLocale());
			}
			else
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				msg = sw.toString();
			}

			String args[] = { EditUtil.encodeHtmlEntities(msg) };
			String errMsg = MessageFormat.format(m_messages
			        .getString("FormatParaViewOneLoadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	/**
     * Loads the wrapped text file into an OfflinePageData object. Loads RTF
     * list view files.
     * 
     * @param p_rtfDoc
     *            the RTF DOM.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error report page is returned.
     */
	private String load_GS_WRAPPED_UNICODE_TEXT_File(RtfDocument p_rtfDoc,
	        String p_fileName)
	{
		// ---------------------------------------------------------------
		// NOTE:
		// ---------------------------------------------------------------
		// We are half way to direct RTF reading for list-view. For
		// now we still get segments the old way (by getting a plain
		// text dump from the Rtf reader and passing that to the
		// original plain text parser). However, we do now read/load
		// segment annotations with the new RTF parser.
		// ---------------------------------------------------------------

		// -----------------------------------
		// Load comments (eventually loadListViewOneWorkFile should
		// load the entire file)
		// -----------------------------------
		try
		{
			m_errWriter.setFileName(p_fileName);
			m_uploadPageData.setLoadConversionLineBreak(m_normalizedLB);
			m_uploadPageData.loadListViewOneWorkFile(p_rtfDoc);

			// set err writer's page, task and job ids
			m_errWriter.processOfflinePageData(m_uploadPageData);
		}
		catch (Throwable ex)
		{
			String msg = "";

			if (ex instanceof GeneralException)
			{
				msg = ((GeneralException) ex)
				        .getMessage(m_uiLocale.getLocale());
			}
			else
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				msg = sw.toString();
			}

			String args[] = { EditUtil.encodeHtmlEntities(msg) };
			String errMsg = MessageFormat.format(m_messages
			        .getString("FormatTwoLoadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		// -----------------------------------
		// Now load segments the old way.
		// Eventually loadListViewOneWorkFile (above)
		// should load the entire file.
		// -----------------------------------

		StringReader p_reader = new StringReader(RtfAPI.getText(p_rtfDoc));

		return loadListViewTextFile(p_reader, p_fileName, true);
	}

	private boolean isValidUnextractedFileId(String p_unextractedTaskId,
	        String p_unextractedFileId, String p_unextractedFileType)
	{
		boolean found = false;

		try
		{
			TaskManager taskMgr = ServerProxy.getTaskManager();
			Task task = taskMgr.getTask(Long.parseLong(p_unextractedTaskId));

			if (p_unextractedFileType.equalsIgnoreCase(SECONDARY_TYPE))
			{
				List workflowStfs = task.getWorkflow()
				        .getSecondaryTargetFiles();

				for (int i = 0; !found && i < workflowStfs.size(); i++)
				{
					SecondaryTargetFile stf = (SecondaryTargetFile) workflowStfs
					        .get(i);
					found = p_unextractedFileId.equals(stf.getIdAsLong()
					        .toString());
				}
			}
			else
			// assume that it is a primary target file type - PRIMARY_TYPE
			{
				List unextractedTargets = task.getWorkflow().getTargetPages(
				        UnextractedFile.UNEXTRACTED_FILE);

				for (int i = 0; !found && i < unextractedTargets.size(); i++)
				{
					TargetPage tp = (TargetPage) unextractedTargets.get(i);
					found = p_unextractedFileId.equals(tp.getIdAsLong()
					        .toString());
				}
			}
		}
		catch (Exception ex)
		{
			CATEGORY.error("Problem validating un-extracted file id", ex);
		}

		return found;
	}

	private boolean parseUnextractedFilename(String p_name)
	{
		boolean found = false;

		RE fname = new RE(RE_UNEXTRACTED_FILE_FNAME);
		RE fnameExt = new RE(RE_UNEXTRACTED_FILE_FNAME_EXT);

		if (fname.match(p_name))
		{
			found = true;

			m_unextractedFilenameHead = fname
			        .getParen(RE_UNEXTRACTED_FILE_HEAD);
			m_unextractedFileId = fname.getParen(RE_UNEXTRACTED_FILE_ID);
			m_unextractedFileType = fname.getParen(RE_UNEXTRACTED_FILE_TYPE);
			m_unextractedFileTaskId = fname
			        .getParen(RE_UNEXTRACTED_FILE_TASK_ID);
			m_unextractedFilenameTail = fname
			        .getParen(RE_UNEXTRACTED_FILE_TAIL);

			if (fnameExt.match(m_unextractedFilenameTail))
			{
				m_unextractedFilenameExt = fnameExt
				        .getParen(RE_UNEXTRACTED_FILE_EXT);
			}
		}

		return found;
	}

	private boolean compareExtension(String p_path, String p_newExt)
	{
		// get current extension from current path
		int idx = p_path.lastIndexOf(".");
		String extension = idx >= 0 ? p_path.substring(idx + 1) : "";

		String newExt = p_newExt != null ? p_newExt : "";

		// validate extension
		// Note: this does not prevent anyone from renaming files to get
		// around this. For instance, you could rename a_100S_200.doc
		// to a_100S_200.html and then upload over the html file.
		// We do not try to detect the actual file format (by content)
		return extension.toLowerCase().equals(newExt.toLowerCase());
	}

	private boolean getAdjustWhitespaceParam(String p_user)
	{
		UserParameter param = getUserParameter(p_user,
		        UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE);

		if (param != null && !param.getValue().equals("0"))
		{
			return true;
		}

		return false;
	}

	private UserParameter getUserParameter(String p_user, String p_param)
	{
		try
		{
			return ServerProxy.getUserParameterManager().getUserParameter(
			        p_user, p_param);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private String setLocale(User p_user)
	{
		// set report locale
		try
		{
			m_uiLocale = ServerProxy.getLocaleManager().getLocaleByString(
			        p_user.getDefaultUILocale());
			m_errWriter.setLocale(m_uiLocale.getLocale());
			m_messages = ResourceBundle.getBundle(getClass().getName(),
			        m_uiLocale.getLocale());
		}
		catch (Exception ex)
		{
			CATEGORY.error("Upload error, unable to set report locale to "
			        + p_user.getDefaultUILocale(), ex);

			m_errWriter.addFileErrorMsg(ex.toString());
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	private String confirmReader(Reader p_reader)
	{
		// confirm we have a stream
		if (p_reader == null)
		{
			String args[] = { "Null reference to the upload-input stream." };

			String errMsg = MessageFormat.format(m_messages
			        .getString("IOReadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	/**
     * Confirm we have some content.
     * 
     * Note: submitting an invalid path for the browser to read from (in the UI)
     * results in zero bytes being set as content in the request.
     */
	private String confirmInputBytes(File p_tmpFile)
	{
		if (p_tmpFile == null || !p_tmpFile.exists() || p_tmpFile.length() == 0)
		{
			String errMsg = MessageFormat.format(m_messages
			        .getString("NoFileContent"), (Object[]) null);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	private String confirmRtfDoc(RtfDocument p_rtfDoc)
	{
		// confirm we have a stream
		if (p_rtfDoc == null)
		{
			String args[] = { "Null reference to the upload-input stream." };

			String errMsg = MessageFormat.format(m_messages
			        .getString("IOReadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	private String confirmValidUserTaskId(String p_sessionId, User p_user,
	        long p_ownerTaskId)
	{
		try
		{
			EditCommonHelper.verifyTask(p_sessionId, p_user.getUserId(), Long
			        .toString(p_ownerTaskId));
		}
		catch (EnvoyServletException ex)
		{
			String errMsg = m_messages.getString("TaskDeactivated");

			CATEGORY.error(errMsg, ex);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	/**
     * The upload file TaskId is extracted from our file header or (as in the
     * case of unextracted files) from the file name.
     */
	private String confirmValidFileTaskId(long p_ownerTaskId,
	        String p_fileTaskId)
	{
		String p_ownerTaskIdAsString = Long.toString(p_ownerTaskId);

		if (!p_ownerTaskIdAsString.equals(p_fileTaskId))
		{
			String args[] = { p_fileTaskId, p_ownerTaskIdAsString };
			String errMsg = MessageFormat.format(m_messages
			        .getString("TaskIdMatchError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	/**
     * Loads the reference page by unmerging it and then remerging according to
     * the merge directives contained in m_uploadPageData.
     */
	private String loadReferencePageData(long p_ownerTaskId,
	        Collection p_excludedItemTypes, int p_uploadFileFormat)
	{
		// load reference data from the DB
		try
		{
			if (!m_uploadPageData.isConsolated()) {
				m_referencePageData = m_uploadPageSaver
				        .initializeAndGetReferencePage(m_uploadPageData,
				                p_excludedItemTypes, p_uploadFileFormat);
				m_referencePageDatas = new ArrayList<PageData>();
				m_referencePageDatas.add(m_referencePageData);
			} else {
				m_referencePageDatas = m_uploadPageSaver.initializeAndGetReferencePages(m_uploadPageData, p_excludedItemTypes, p_uploadFileFormat);
			}
		}
		catch (UploadPageSaverException ex)
		{
			CATEGORY.error("Unable to load extracted file reference data:", ex);

			String args[] = { AmbassadorDwUpConstants.LABEL_PAGEID,
			        AmbassadorDwUpConstants.LABEL_SRCLOCALE,
			        AmbassadorDwUpConstants.LABEL_TRGLOCALE,
			        ex.getStackTraceString() };

			String errMsg = MessageFormat.format(m_messages
			        .getString("ReferencePageLoadError"), (Object[]) args);

			m_errWriter.addFileErrorMsg(errMsg);
			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	private String createErrorChecker()
	{
		// Create offline error checker
		try
		{
			m_errChecker = new OfflinePtagErrorChecker(m_errWriter);
		}
		catch (AmbassadorDwUpException ex)
		{
			CATEGORY.error("Unable to instantiate OfflinePtagErrorChecker", ex);

			m_errWriter
			        .addSystemErrorMsg("Unable to instantiate OfflinePtagErrorChecker\n"
			                + ex.getStackTraceString());

			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	/** Initialize using a Reader. */
	private String preLoadInit(String p_sessionId, long p_ownerTaskId,
	        User p_user, Reader p_reader)
	{
		String errPage = null;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user)) != null)
		{
			return errPage;
		}
		else if ((errPage = confirmReader(p_reader)) != null)
		{
			return errPage;
		}

		return null;
	}

	/** Initialize using a File. */
	private String preLoadInit(String p_sessionId, long p_ownerTaskId,
	        User p_user, File p_tmpFile)
	{
		String errPage = null;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user)) != null)
		{
			return errPage;
		}
		else if ((errPage = confirmInputBytes(p_tmpFile)) != null)
		{
			return errPage;
		}

		return null;
	}

	/** Initialize using a RtfDocument. */
	private String preLoadInit(String p_sessionId, long p_ownerTaskId,
	        User p_user, RtfDocument p_rtfDoc)
	{
		String errPage = null;

		if ((errPage = preLoadInit(p_sessionId, p_ownerTaskId, p_user)) != null)
		{
			return errPage;
		}
		else if ((errPage = confirmRtfDoc(p_rtfDoc)) != null)
		{
			return errPage;
		}

		return null;
	}

	/** Common initialization checks. */
	private String preLoadInit(String p_sessionId, long p_ownerTaskId,
	        User p_user)
	{
		String errPage = null;

		if ((errPage = setLocale(p_user)) != null)
		{
			return errPage;
		}
		else if ((errPage = confirmValidUserTaskId(p_sessionId, p_user,
		        p_ownerTaskId)) != null)
		{
			return errPage;
		}

		return null;
	}

	private String postLoadInit(long p_ownerTaskId, String p_fileTaskId,
	        Collection p_excludedItemTypes, int p_uploadFileFormat)
	{
		String errPage = null;

		if ((errPage = confirmValidFileTaskId(p_ownerTaskId, m_uploadPageData
		        .getTaskId())) != null)
		{
			return errPage;
		}
		else if ((errPage = loadReferencePageData(p_ownerTaskId,
		        p_excludedItemTypes, p_uploadFileFormat)) != null)
		{
			return errPage;
		}
		else if ((errPage = createErrorChecker()) != null)
		{
			return errPage;
		}

		return null;
	}

	private String getLFNormalizationSequence()
	{
		String errMsg = null;
		String rslt = null;

		// Get the linefeed normalization sequence
		// NOTE1: We must normalize during parsing so the parser can remove
		// newlines we added for formating.
		// NOTE: processPage() takes care of container-format linefeeds.
		try
		{
			ResourceBundle res = ResourceBundle
			        .getBundle(AmbassadorDwUpConstants.OFFLINE_CONFIG_PROPERTY);
			m_normalizedLB = res
			        .getString(AmbassadorDwUpConstants.OFFLINE_CONFIG_KEY_LB_NORMALIZATION);
		}
		catch (Throwable ex)
		{
			String args[] = { AmbassadorDwUpConstants.OFFLINE_CONFIG_PROPERTY };
			errMsg = MessageFormat.format(m_messages
			        .getString("ResourceFileLoadError"), (Object[]) args);

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			rslt = m_errWriter.buildPage().toString();
		}

		// Validate the linebreak normalization sequence
		if (!m_normalizedLB.equals("\n") && !m_normalizedLB.equals("\r")
		        && !m_normalizedLB.equals("\r\n"))
		{
			errMsg = m_messages.getString("LineBreakNormalizationError");

			CATEGORY.error(errMsg);

			m_errWriter.addFileErrorMsg(errMsg);
			rslt = m_errWriter.buildPage().toString();
		}

		return rslt;
	}

	private String save(OfflinePageData m_uploadPageData, ArrayList<PageData> m_refPageDatas, 
	        String p_jmsQueueDestination, User p_user, String p_fileName)
	{
		try
		{
			m_uploadPageSaver.savePageToDb(m_uploadPageData, m_refPageDatas, 
			        p_jmsQueueDestination, p_user, p_fileName);
		}
		catch (UploadPageSaverException ex)
		{
			CATEGORY.error("Unable to save page", ex);

			m_errWriter.addSystemErrorMsg("Unable to save page:\n"
			        + ex.getStackTraceString());

			return m_errWriter.buildPage().toString();
		}
		catch (Throwable ex)
		{
			CATEGORY.error("Unable to save page", ex);

			m_errWriter.addSystemErrorMsg("Unable to save page:\n"
			        + ex.toString());

			return m_errWriter.buildPage().toString();
		}

		return null;
	}

	private String checkPage(User p_user)
	{
		String errPage = null;

		boolean adjustWS = getAdjustWhitespaceParam(p_user.getUserId());
		if (!m_uploadPageData.isConsolated()) {
			ArrayList<PageData> pageDatas = new ArrayList<PageData>();
			pageDatas.add(m_referencePageData);
			errPage = m_errChecker.check(pageDatas, m_uploadPageData, adjustWS);
		} else
			errPage = m_errChecker.check(m_referencePageDatas, m_uploadPageData, adjustWS);
		
		if (errPage != null)
		{
			if (CATEGORY.isDebugEnabled())
			{
				CATEGORY
				        .debug("Page failed error checking. Returning error results");
			}

			return errPage;
		}

		return null;
	}

	private String checkReportPage(User p_user) throws Exception
	{
		String errPage = null;

		boolean adjustWS = getAdjustWhitespaceParam(p_user.getUserId());
		if ((errPage = m_errChecker.checkAndSave(segId2RequiredTranslation,
		        adjustWS, reportTargetLocaleId, p_user.getUserId())) != null)
		{
			if (CATEGORY.isDebugEnabled())
			{
				CATEGORY
				        .debug("Page failed error checking. Returning error results");
			}

			return errPage;
		}

		return null;
	}

	private String addLinesErrMsg(Reader p_reader, int current_line)
	{

		StringBuffer string_buffer = new StringBuffer();
		String lines_err_msg = "";
		// the line_count is between start_line and end_line,the base is on the
		// current_line
		int line_count = 1;
		int start_line = (current_line - 5) >= 0 ? current_line - 5 : 0;
		int end_line = current_line + 5;
		int tempchar = 0;

		try
		{
			while ((tempchar = p_reader.read()) != -1)
			{
				char c_tempchar = (char) tempchar;

				if (c_tempchar == '\r')
				{
					continue;
				}

				if (c_tempchar == '\n')
				{
					line_count++;
				}

				if ((line_count >= start_line) && (line_count <= end_line))
				{
					if (line_count == current_line + 1)
					{
						string_buffer.append("</b>");
					}
					string_buffer.append(c_tempchar);
					if (c_tempchar == '\n')
					{
						if (line_count == current_line)
						{
							string_buffer.append("<b>");
						}

						string_buffer.append(line_count);
						string_buffer.append('\t');
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		lines_err_msg = string_buffer.toString();

		return lines_err_msg;
	}

	private int getErrLine(String head_err_msg)
	{
		int line = 0;
		int start = ("The Problem was caused at line ").length();
		String err_line = "";

		for (int i = start; i < head_err_msg.length(); i++)
		{
			String temp = "" + head_err_msg.charAt(i);
			if (temp.matches("\\d"))
			{
				err_line += temp;
			}
		}

		line = Integer.parseInt(err_line);
		return line;

	}

	private void bindErrMsg(String[] args, Reader p_reader)
	{
		String head_err_msg = args[0];
		int current_line = getErrLine(head_err_msg);
		String lines_err_msg = addLinesErrMsg(p_reader, current_line);
		if (!("".equals(lines_err_msg.trim())))
		{
			args[0] += "LineNumber  -  Text\n";
			args[0] += lines_err_msg;
		}
	}
	
    public OfflinePageData getUploadPageData()
    {
        return m_uploadPageData;
    }
}
