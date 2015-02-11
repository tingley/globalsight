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

import com.globalsight.everest.edit.offline.download.DownloadHelper;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.util.progress.ProcessStatus;
import com.globalsight.util.progress.IProcessStatusListener;

import com.globalsight.everest.glossaries.GlossaryFile;
import java.util.Iterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.io.File;

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


    private int findNumberOfFiles(DownloadParams p_params)
    {
        int count = 0;

        int fileNumber = 0;
        if (p_params.hasPrimaryFiles())
        {
            ListIterator primaryFiles = p_params.getPageListIterator();
            while (primaryFiles.hasNext())
            {
                Long srcPageId = (Long) primaryFiles.next();
                fileNumber++;
            }
            count += fileNumber;
        }
        
        //GBS-1157, Added by Vincent Yan, 2010-7-6
        if (p_params.isNeedConsolidate())
        	count = 1;

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
                count += fileNumber;
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
                count += fileNumber;
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
}
