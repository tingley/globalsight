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

package com.globalsight.everest.webapp.applet.admin.customer.download; 

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import com.globalsight.everest.webapp.applet.common.EnvoyJApplet;
import com.globalsight.everest.webapp.applet.util.SwingWorker;
import com.globalsight.util.zip.ZipIt;

/* 
 * The DownloadTask object gives the Progress Bar something to query as
 * the download is done in the background using SwingWorker.
 */
public class DownloadTask
{
    private URL m_url;
    private String m_saveToDir;
    private long m_bytesToDownload =0;
    private int m_current = 0;
    private boolean m_done = false;
    private boolean m_canceled = false;
    private String m_statMessage; //status message
    private JProgressBar m_progressBar;
    private JTextArea m_outputArea;
    private File m_tmpFile = null;
    private Map<String, String> fileLastModifiedTimes = new HashMap<String, String>();

    private String[] jobNames;
    private String locale;
    /**
     * Creates a DownloadTask which will download and report
     * progress on downloading the given URL based on its size.
     * 
     * @param p_applet download applet
     * @param p_progressBar
     *                 progress bar to update
     * @param p_outputArea
     *                 message area to write messages to
     */
    public DownloadTask(EnvoyJApplet p_applet,
                        JProgressBar p_progressBar,
                        JTextArea p_outputArea,
                        String p_saveToDir)
    {
        m_bytesToDownload = Long.parseLong(p_applet.getParameter("zipFileSize"));
        try {
            m_url = new URL(p_applet.getDocumentBase(), p_applet.getParameter("zipUrl"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        jobNames = p_applet.getParameter("jobNames").split(",");
		locale = p_applet.getParameter("locale");
		String lastModifiedTimes = p_applet.getParameter("lastModifiedTimes");
		String fileNames = p_applet.getParameter("fileNames");
		if (lastModifiedTimes != null && fileNames != null) {
			String[] fileNamesStr = fileNames.split(",");
			String[] lastModifiedTimesStr = lastModifiedTimes.split(",");
			for (int i = 0; i < fileNamesStr.length;i ++) {
				try
                {
                    fileLastModifiedTimes.put(URLDecoder.decode(fileNamesStr[i], "UTF-8"), lastModifiedTimesStr[i]);
                }
                catch (UnsupportedEncodingException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
			}
		}
		m_saveToDir = p_saveToDir;
		m_progressBar = p_progressBar;
		m_outputArea = p_outputArea;
	}

    /**
	 * Called to start the Download
	 */
    public void startDownload()
    {
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                m_current = 0;
                m_done = false;
                m_canceled = false;
                m_statMessage = null;
                return new ActualTask();
            }
        };
        worker.start();
    }

    /**
     * Called to find out the percentage of work that has been done.
     */
    public int getCurrent() {
        return m_current;
    }

    /**
     * Stops the download
     */
    public void stop() {
        m_canceled = true;
        m_statMessage = null;
    }

    /**
     * Called to find out if the task has completed.
     */
    public boolean isDone() {
        return m_done;
    }

    /**
     * Returns the most recent status message, or null
     * if there is no current status message.
     */
    public String getMessage() {
        return m_statMessage;
    }

    private void setCurrent(int p_value)
    {
        m_current = p_value;
        m_progressBar.setValue(m_current);
    }

    private void setMessage(String p_msg)
    {
        m_statMessage = p_msg;
        m_outputArea.append(m_statMessage);
        m_outputArea.append("\r\n");
    }


    /**
     * The actual long running task.  This runs in a SwingWorker thread.
     */
    class ActualTask
    {
        ActualTask()
        {
            try
            {
                download();
                extract();
            }
            catch (Exception e)
            {
                m_done = true;
                m_statMessage = "Error!";
                e.printStackTrace();
            }
        }


        /**
         * Downloads the tmp zip file from the server
         * and reports progress to the progress bar and message
         * box
         * 
         * @exception Exception
         */
        private void download() throws Exception
        {
            InputStream is = null;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;
            try
            {
                setCurrent(0);
                setMessage("Downloading");

                //first try to create the dir if it does not exist
                File dir = new File (m_saveToDir);
                if (dir.exists() == false)
                {
                    setMessage("Creating directory " + m_saveToDir);
                    boolean made = dir.mkdirs();
                    if (!made)
                    {
                        setMessage("ERROR: Failed to create directory.");
                        setCurrent(0);
                        return;
                    }
                }

                m_tmpFile = File.createTempFile("GSDownload",".zip", dir);
                is = m_url.openStream();

                bis = new BufferedInputStream(is);
                byte buffer[] = new byte[2048]; //buffer
                int read = 0;
                fos = new FileOutputStream(m_tmpFile);
                long totalRead = 0;
                while ((read=bis.read(buffer,0,2048)) != -1)
                {
                    fos.write(buffer,0,read);
                    totalRead += read;
                    int percentDone = (int) (((double) totalRead / (double)m_bytesToDownload) * (double)100.0);
                    setCurrent(percentDone / 2); //downloading is only half the battle
                }
                fos.flush();
                setCurrent(50);
                m_done = true;
                setMessage("Finished downloading");                
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try {
                    if (fos != null) fos.close();
                } catch (Exception ignore) {
//                    ignore.printStackTrace();
                }

                // If there is special character in file path or name,
                // "bis.close()" will fail. As this runs in applet, we can
                // ignore this failure. 
                try {
                    if (bis != null) bis.close();
                } catch (Exception ignore) {
//                    ignore.printStackTrace();
                }
            }
        }

        /**
         * Extracts the tmp zip file, reports progress to the progress bar and
         * output area and deletes the tmp file afterwards.
         */
        private void extract()
        {
            try
            {
                setMessage("Unzipping files to " + m_saveToDir);
                //zipit doesn't take a process status listener yet, so unzip first
                //and then update the progress bar just for show
                ArrayList<String> fileList = ZipIt
                        .unpackZipPackageForDownloadExport(
                                m_tmpFile.getAbsolutePath(),
                                fileLastModifiedTimes, jobNames, locale);
                int numFiles = fileList.size();
                int numFilesSoFar = 0;
                for (String entryName : fileList)
                {
                    numFilesSoFar++;
                    setMessage("Saving " + entryName);
                    float percentDone = (float) numFilesSoFar / (float) numFiles;
                    setCurrent((int) percentDone / 2);
                }
                m_tmpFile.delete();
                setMessage("Done");
                setCurrent(100);
            }
            catch (Exception e)
            {
                setMessage("ERROR: " + e.getMessage());
                setMessage("Failed to save files to local system.");
                e.printStackTrace();
            }
        }
    }
}

