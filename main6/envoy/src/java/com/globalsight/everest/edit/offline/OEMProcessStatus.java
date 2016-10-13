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

package com.globalsight.everest.edit.offline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.upload.CheckResult;
import com.globalsight.everest.glossaries.GlossaryFile;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.util.progress.IProcessStatusListener;
import com.globalsight.util.progress.ProcessStatus;

/**
 * Extends ProcessStatus to create a specialized status object for offline.
 */
public class OEMProcessStatus
    extends ProcessStatus
    implements IProcessStatusListener
{
    private boolean m_abort = false;
    private int m_totalFiles = 0;
    private DownloadParams m_dldParams;
    private StringBuffer p_results_sb = new StringBuffer();
    
    private boolean useProcess = false;
    private int process = 0;
    private Boolean isContinue = null;
    private CheckResult checkResult = null;
    private CheckResult checkResultCopy = null;
    private Set<Long> taskIds = new HashSet<Long>();
    private List<Long> taskIdList = new ArrayList<Long>();
    public void addTaskId(Long taskId)
    {
    	taskIds.add(taskId);
    }
    
    public Set<Long> getTaskIds()
    {
    	return taskIds;
    }

    public List<Long> getTaskIdList() {
		return taskIdList;
	}

	public void setTaskIdList(List<Long> taskIdList) {
		this.taskIdList = taskIdList;
	}

	/** Creates a new instance of OEMProcessStatus. */
    public OEMProcessStatus()
    {
        // default for upload
        m_totalFiles = 1;
    }

    /** Creates a new instance of OEMProcessStatus. */
    public OEMProcessStatus(DownloadParams p_params)
    {
        m_dldParams = p_params;
        m_totalFiles = findNumberOfFiles(p_params);
    }

    /**
     * Sets the abort flag, which prevents the download package from
     * being delivered to the user.
     */
    public void setAbort()
    {
        m_abort = true;
    }

    /**
     * Gets the abort flag.
     * @return true if downlaod should be aborted, false if not.
     */
    public boolean isAborted()
    {
        return m_abort;
    }

    /** Get the total number of files being processed. */
    public int getTotalFiles()
    {
        return m_totalFiles;
    }

    /**
     * Notifies the event listener of the current import status.
     * The message will appear in orange.
     *
     * @param p_message the warning text
     * @param p_msgCaption an optional caption appearing above the message.
     */
    public void warn(String p_message, String p_msgCaption)
        throws IOException
    {
        listenHTML(getCounter(), getPercentage(), p_message,
            p_msgCaption, "STYLE='color: orange;'");
    }

    /**
     * Notifies the event listener of the current import status.
     * The message will appear in red.
     */
    public void speakRed(int p_entryCount, int p_percentage,
        String p_message, String p_msgCaption)
        throws IOException
    {
        if (p_entryCount >= m_totalFiles)
        {
            p_entryCount = m_totalFiles - 1;
        }
        
        listenHTML(p_entryCount, p_percentage, p_message,
            p_msgCaption, "STYLE='color: red;'");
    }

    /**
     * Notifies the event listener of the current import status.
     * The message will appear in red.
     */
    public void speakRed(int p_entryCount, String p_message,
        String p_msgCaption)
        throws IOException
    {
        int percentage =
            (int)(((p_entryCount * 1.0)/(m_totalFiles * 1.0)) * 100.0);
        
        if (p_entryCount >= m_totalFiles)
        {
            p_entryCount = m_totalFiles - 1;
        }
        
        speakRed(p_entryCount, percentage, p_message, p_msgCaption);
    }

    /**
     * Notifies the event listener of the current import status.
     * The message will appear in green.
     */
    public void speakGreen(int p_entryCount, int p_percentage,
        String p_message, String p_msgCaption)
        throws IOException
    {
        if (p_entryCount >= m_totalFiles)
        {
            p_entryCount = m_totalFiles - 1;
        }
        
        listenHTML(p_entryCount, p_percentage, p_message,
            p_msgCaption, "STYLE='color: green;'");
    }

    /** Notifies the event listener of the current import status. */
    public void speakGreen(int p_entryCount, String p_message,
        String p_msgCaption)
        throws IOException
    {
        int percentage =
            (int)(((p_entryCount * 1.0)/(m_totalFiles * 1.0)) * 100.0);
        
        if (p_entryCount >= m_totalFiles)
        {
            p_entryCount = m_totalFiles - 1;
        }
        speakGreen(p_entryCount, percentage, p_message, p_msgCaption);
    }

    /** Notifies the event listener of the current import status. */
    public void speak(int p_entryCount, String p_message)
        throws IOException
    {
        int percentage =
            (int)(((p_entryCount * 1.0)/(m_totalFiles * 1.0)) * 100.0);
        
        if (p_entryCount > m_totalFiles)
        {
            p_entryCount = m_totalFiles - 1;
        }
        
        speak(p_entryCount, percentage, p_message);
    }

    /** Notifies the event listener of the current import status. */
    public void speak(int p_entryCount, int p_percentage, String p_message)
        throws IOException
    {
        if (p_entryCount > m_totalFiles)
        {
            p_entryCount = m_totalFiles - 1;
        }
        
        listen(p_entryCount, p_percentage, p_message);
    }
    
    /**Override Set the results. */
    public void setResults(Object p_results)
    {
    	if(p_results instanceof File)
    	{
    		super.setResults(p_results);
    	}
    	else if(p_results != null)   
       {
                p_results_sb.append(p_results);
       }
    }

    /**Override Get the results. */
    public Object getResults()
    {
    	Object obj = super.getResults();
    	if(obj instanceof File)
    	{
    		return obj;
    	}
    	else
    	{
    		return (p_results_sb == null || p_results_sb.length() == 0)? null:p_results_sb.toString();
    	}
    	
    }


    //
    // Private methods
    //


    public static int findNumberOfFiles(DownloadParams p_params)
    {
        int count = 0;

        int[] fileNumbers = getFileNumbers(p_params);
        int extractFileNumber = fileNumbers[0];
        int unextractFileNumber = fileNumbers[1];

        //GBS-1157, Added by Vincent Yan, 2010-7-6
        if (p_params.getConsolidateFileType() != null 
        		&& p_params.getConsolidateFileType().equals("consolidate"))
        {
            count = 1 + unextractFileNumber;
        }
        else
        {
            count = extractFileNumber + unextractFileNumber;
        }

        if (p_params.hasSecondaryFiles())
        {
            List stfIds = p_params.getSTFileIds();
            Iterator secondaryFiles = stfIds.iterator();
            while (secondaryFiles.hasNext())
            {
                Long stfId = (Long)secondaryFiles.next();
                count++;
            }
        }

        if (p_params.hasSupportFiles())
        {
            Iterator supportFiles = p_params.getSupportFilesList().iterator();
            while (supportFiles.hasNext())
            {
                GlossaryFile gf = (GlossaryFile)supportFiles.next();
                count++;
            }
        }
        
        int omegaT = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_OMEGAT;
        boolean isOmegaT = (omegaT == p_params.getFileFormatId()); 
        int resMode = p_params.getResInsOption();
        if (resMode == AmbassadorDwUpConstants.MAKE_RES_TMX_14B
                || resMode == AmbassadorDwUpConstants.MAKE_RES_TMX_PLAIN
                || resMode == AmbassadorDwUpConstants.MAKE_RES_TMX_BOTH)
        {
            if (p_params.isConsolidateTmxFiles())
            {
                count += 1;
            }
            else
            {
                count += extractFileNumber;
            }

            if (isOmegaT)
            {
                // mt files, tmx penalty files, 
                if (p_params.isConsolidateTmxFiles())
                {
                    count += (1 * 3);
                }
                else
                {
                    count += (extractFileNumber * 3);
                }
            }
        }

        String termFormat = p_params.getTermFormat();
        if (!OfflineConstants.TERM_NONE.equals(termFormat))
        {
            if (p_params.isConsolidateTermFiles())
            {
                count += 1;
            }
            else
            {
                count += extractFileNumber;
            }
        }
        
        if (p_params.isIncludeRepetitions())
        {
            count += 1;
        }

        // An additional 1 is added so that additional files and
        // parameters can be downloaded while the percentage shows 99%,
        // and 100% when all done.
        count += 1;

        return count;
    }

    private static int[] getFileNumbers(DownloadParams p_params)
    {
        int extractFileNumber = 0;
        int unextractFileNumber = 0;
        if (p_params != null && p_params.hasPrimaryFiles())
        {
            ListIterator primaryFiles = p_params.getPageListIterator();
            TargetPage trgPage = null;
            while (primaryFiles.hasNext())
            {
                Long srcPageId = (Long) primaryFiles.next();
                try
                {
                    trgPage = ServerProxy.getPageManager().getTargetPage(
                            srcPageId.longValue(),
                            p_params.getTargetLocale().getId());
                    if (trgPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE)
                    {
                        extractFileNumber++;
                    }
                    else
                    {
                        unextractFileNumber++;
                    }
                }
                catch (Exception ex)
                {
                    extractFileNumber++;
                }
            }
        }

        int[] results = {extractFileNumber, unextractFileNumber};
        return results;
    }

    public void setTotalFiles(int files) {
		m_totalFiles = files;
	}
	
	public void updateProcess(int n)
	{
		if (!useProcess)
			return;
		
		process = n;
	}

	public boolean isUseProcess() 
	{
		return useProcess;
	}

	public void setUseProcess(boolean useProcess) 
	{
		this.useProcess = useProcess;
	}

	public int getProcess() 
	{
		return process;
	}

	public void setProcess(int process) 
	{
		this.process = process;
	}

	public Boolean getIsContinue() {
		return isContinue;
	}

	public void setIsContinue(Boolean isContinue) {
		this.isContinue = isContinue;
	}

	public CheckResult getCheckResult() {
		return checkResult;
	}

	public void setCheckResult(CheckResult checkResult) {
		this.checkResult = checkResult;
	}

    public CheckResult getCheckResultCopy()
    {
        return checkResultCopy;
    }

    public void setCheckResultCopy(CheckResult checkResultCopy)
    {
        this.checkResultCopy = checkResultCopy;
    }
}
