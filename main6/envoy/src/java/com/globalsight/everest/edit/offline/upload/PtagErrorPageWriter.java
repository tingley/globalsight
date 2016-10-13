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



package com.globalsight.everest.edit.offline.upload;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.DownloadWriter;
import com.globalsight.everest.edit.offline.download.HTMLResourcePages.DownloadWriterInterface;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;

public class PtagErrorPageWriter
    extends DownloadWriter
    implements DownloadWriterInterface
{
    static private final String ERRORPAGE_START = "ErrorPageStart";
    static private final String ERRORPAGE_HEADER = "ErrorPageHeader";
    static private final String ERROR_MSG = "ErrorHeaderAndText";
    static private final String ERRORPAGE_END = "ErrorPageEnd";
    static private final String FILE_ERROR_MSG = "FileErrorHeaderAndText";
    static private final String SYSTEM_ERROR_MSG = "SystemErrorHeaderAndText";
    static private final String SYSTEM_ERROR2_MSG = "SystemErrorHeaderAndText2";

    static private final String LABEL_PAGE_TITLE = "Title";
    static private final String LABEL_PAGE_ID = "Page";
    static private final String LABEL_JOB_ID = "Job";
    static private final String LABEL_STAGE_ID = "Stage";
    static private final String LABEL_FILE_ERR_MSG = "FileErrorLabel";
    static private final String LABEL_SYSTEM_ERR_MSG = "SystemErrorLabel";
    static private final String LABEL_PAGE_END = "PageEndMsg";
    static private final String LABEL_SEGMENTID = "SegmentIDLabel";
    static private final String LABEL_QUICKHELP_TITLE = "QuickHelpLabel";
    static private final String LABEL_QUICKHELP = "QuickHelp";

    static private final int SEG_PARAM_COUNT = 4;

    static private final String ERRORPAGE_TEMPLATE_FILE =
        "com.globalsight.everest.edit.offline.upload.ErrorPageWriter";
    static private final String ERRORPAGE_LABEL_FILE =
        "com.globalsight.everest.edit.offline.upload.ErrorPageWriterLabel";

    private ResourceBundle m_template = null;
    private ResourceBundle m_labels = null;
    private StringBuffer m_segments = new StringBuffer();
    private String m_pageId = "---";
    private String m_workflowId = "---";
    private String m_taskId = "---";
    private Locale m_locale = null;
    private String m_fileName = "---";

    /**
     * PtagErrorWriter constructor.
     * @param p_locale - Sets/loads resources.
     */
    public PtagErrorPageWriter()
        throws AmbassadorDwUpException
    {
        super();

        // Set default locale and load resouces
        setLocale(Locale.US);
    }

    public void setFileName(String p_fileName)
    {
        m_fileName = p_fileName;
    }
    
    public String getFileName()
    {
    	return m_fileName;
    }

    public void setPageId(String p_pageId)
    {
        m_pageId = p_pageId;
    }

    public void setWorkflowId(String p_workflowId)
    {
        m_workflowId = p_workflowId;
    }

    public void setTaskId(String p_taskId)
    {
        m_taskId = p_taskId;
    }

    /**
     * Returns writer's locale.
     */
    public Locale getLocale()
    {
        return m_locale;
    }

    /**
     * Sets new locale and re-loads resources.
     */
    public void setLocale(Locale p_locale)
        throws AmbassadorDwUpException
    {
        try
        {
            m_locale = p_locale;
            m_template = loadProperties(ERRORPAGE_TEMPLATE_FILE, m_locale);
            m_labels = loadProperties(ERRORPAGE_LABEL_FILE, m_locale);
        }
        catch (MissingResourceException ex)
        {
            throw new AmbassadorDwUpException(
                AmbassadorDwUpExceptionConstants.WRITER_RESOURCE_NOT_FOUND, ex);
        }
    }

    /**
     * Assembles and returns the complete HTML error page.
     */
   
    public StringBuffer buildPage()
    {
        StringBuffer page = new StringBuffer();

        // Top portion - page header and quick help
        //String[] paramsTop = new String[3];
        //paramsTop[1] = m_labels.getString(LABEL_QUICKHELP_TITLE);
        //paramsTop[2] = m_labels.getString(LABEL_QUICKHELP);
        String s = m_template.getString(ERRORPAGE_START);
        //page.append(formatString(s, paramsTop));
        page.append(s);

        // report header
        String[] params = new String[8];
        params[1] = m_fileName;
        params[2] = m_labels.getString(LABEL_PAGE_ID);
        StringBuffer sbf = new StringBuffer();
    	if (m_pageId.length() > 0)
		{
    		int count =0;
			String[] pageIds = m_pageId.split(",");
			for (int i = 0; i < pageIds.length; i++)
			{
				count++;
				if (i != pageIds.length - 1)
				{
					sbf.append(pageIds[i]);
					sbf.append(",");
				}
				else
				{
					sbf.append(pageIds[i]);
				}
				if (count == 40)
				{
					sbf.append("<br>");
					count = 0;
				}
			}
		}
        params[3] = " " + sbf.toString();
        params[4] = m_labels.getString(LABEL_JOB_ID);
        params[5] = " " + m_workflowId;
        params[6] = m_labels.getString(LABEL_STAGE_ID);
        params[7] = " " + m_taskId;
        page.append(formatString(m_template.getString(ERRORPAGE_HEADER), params));

        // error messages
        page.append(m_segments);

        // end HTML page
        page.append(formatString(m_template.getString(ERRORPAGE_END),
            m_labels.getString(LABEL_PAGE_END)));

        // clear the messages
        m_segments = new StringBuffer();

        return page;
    }
    
    /**
     * When task is not accepted or acceptor of this task is not right,
     * Assembles and returns the complete HTML error page.
     */
    public StringBuffer buildPageForTaskError()
    {
        StringBuffer page = new StringBuffer();

        String s = m_template.getString(ERRORPAGE_START);
        page.append(s);

        // error messages
        page.append(m_segments);

        // end HTML page
        page.append(formatString(m_template.getString(ERRORPAGE_END),
            m_labels.getString(LABEL_PAGE_END)));

        // clear the messages
        m_segments = new StringBuffer();

        return page;
    }
    
    public StringBuffer buildReportErroPage()
    {
    	 StringBuffer page = new StringBuffer();

         // Top portion - page header and quick help
         //String[] paramsTop = new String[3];
         //paramsTop[1] = m_labels.getString(LABEL_QUICKHELP_TITLE);
         //paramsTop[2] = m_labels.getString(LABEL_QUICKHELP);
         String s = m_template.getString(ERRORPAGE_START);
         //page.append(formatString(s, paramsTop));
         page.append(s);

         // report header
         // error messages
         page.append(m_segments);

         // end HTML page
         page.append(formatString(m_template.getString(ERRORPAGE_END),
             m_labels.getString(LABEL_PAGE_END)));

         // clear the messages
         m_segments = new StringBuffer();

         return page;
    }

    /**
     * Adds a File error message to the page.
     */
    public void addFileErrorMsg(String p_errMsg)
    {
        String s = formatString(m_template.getString(FILE_ERROR_MSG),
            makeFileErrorParamList(p_errMsg));

        m_segments.append(prepForHtmlDisplay(s));
    }

    /**
     * Adds a System error message to the page.
     */
    public void addSystemErrorMsg(String p_errMsg)
    {
        String s = formatString(m_template.getString(SYSTEM_ERROR_MSG),
            makeSystemErrorParamList(p_errMsg));

        m_segments.append(prepForHtmlDisplay(s));
    }

    /**
     * Adds a System error message and segment id to the page.
     */
    public void addSystemErrorMsg(OfflineSegmentData p_segmentData,
        String p_errMsg)
    {
        String s = formatString(m_template.getString(SYSTEM_ERROR2_MSG),
            makeSystemErrorParamList(p_segmentData, p_errMsg));

        m_segments.append(prepForHtmlDisplay(s));
    }

    /**
     * Pulls initial information from the page to initialize the writer.
     *
     * @param p_page com.globalsight.everest.edit.offline.OfflinePageData
     */
    public void processOfflinePageData(OfflinePageData p_page)
        throws AmbassadorDwUpException
    {
        m_pageId = " " + p_page.getPageId();
        m_workflowId = " " + p_page.getWorkflowId();
        m_taskId = " " + p_page.getTaskId();
    }

    /**
     * Adds a Segment error message to the page.
     */
    public void addSegmentErrorMsg(OfflineSegmentData p_segmentData,
        String p_errMsg)
    {
        String s = formatString(m_template.getString(ERROR_MSG),
            makeParamList(p_segmentData, p_errMsg.trim()));

        m_segments.append(prepForHtmlDisplay(s));
    }

    private String[] makeParamList(OfflineSegmentData p_segment,
        String p_errMsg)
    {
        String[] params = new String[SEG_PARAM_COUNT];

        params[1] = m_labels.getString(LABEL_SEGMENTID);
        params[2] = p_segment.getDisplaySegmentID();
        params[3] = p_errMsg;

        return params;
    }
    
    public void addSegmentErrorMsg(String p_segID,
            String p_errMsg)
    {
        String s = formatString(m_template.getString(ERROR_MSG),
            makeParamList(p_segID, p_errMsg.trim()));

        m_segments.append(prepForHtmlDisplay(s));
    }

    private String[] makeParamList(String p_segId,
        String p_errMsg)
    {
        String[] params = new String[SEG_PARAM_COUNT];

        params[1] = m_labels.getString(LABEL_SEGMENTID);
        params[2] = p_segId;
        params[3] = p_errMsg;

        return params;
    }

    private String[] makeFileErrorParamList(String p_errMsg)
    {
        String[] params = new String[4];

        params[1] = m_labels.getString(LABEL_FILE_ERR_MSG);
        params[2] = p_errMsg;

        return params;
    }

    private String[] makeSystemErrorParamList(String p_errMsg)
    {
        String[] params = new String[3];

        params[1] = m_labels.getString(LABEL_SYSTEM_ERR_MSG);
        params[2] = p_errMsg;

        return params;
    }

    private String[] makeSystemErrorParamList(OfflineSegmentData p_segment,
        String p_errMsg)
    {
        StringBuffer sb = new StringBuffer();

        String[] params = new String[4];

        params[1] = m_labels.getString(LABEL_SYSTEM_ERR_MSG);
        sb.append(m_labels.getString(LABEL_SEGMENTID));
        sb.append(" ");
        sb.append(p_segment.getDisplaySegmentID());
        params[2] = sb.toString();
        params[3] = p_errMsg;

        return params;
    }

    private String prepForHtmlDisplay(String p_input)
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 0, max = p_input.length(); i < max; i++)
        {
            char c = p_input.charAt(i);

            if (c == '\n' || c == '\r')
            {
                sb.append("<BR>");
            }
            else if (c == '\t')
            {
                sb.append("&nbsp;&nbsp;&nbsp;");
            }
            else
            {
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
