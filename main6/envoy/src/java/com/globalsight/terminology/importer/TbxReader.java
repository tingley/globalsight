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

package com.globalsight.terminology.importer;

import java.io.File;

import org.apache.log4j.Logger;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.importer.IReader;
import com.globalsight.importer.ImportOptions;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.util.FileUtil;
import com.globalsight.util.ReaderResult;
import com.globalsight.util.ReaderResultQueue;

public class TbxReader implements IReader, TermbaseExceptionMessages {
	
	private static final Logger CATEGORY =
        Logger.getLogger(
        		TbxReader.class);
	
	//
    // Private Member Variables
    //
    private Termbase m_termbase;
    private ImportOptions m_options;
    private int m_entryCount;

    private TbxReaderThread m_thread = null;
    private ReaderResultQueue m_results = null;
    private ReaderResult m_result;
    
    private static final String DTD_FILE_NAME = "TBXcdv04.dtd";
	
	public TbxReader (ImportOptions p_options, Termbase p_termbase)
    {
        m_termbase = p_termbase;
        setImportOptions(p_options);
    }

	public ImportOptions analyze() {
		m_entryCount = 0;

        try
        {
            /*
             * in order to support tbx files from WorldServer,
             * when importing tbx files, the dtd file from WorldServer
             * should be copied to _Imports_ directory in advance
             */
            SystemConfiguration config = SystemConfiguration.getInstance();
            String s_webserverDocroot = config
                    .getStringParameter(SystemConfigParamNames.WEB_SERVER_DOC_ROOT);
            s_webserverDocroot = 
                s_webserverDocroot.replace("\\", "/").replace("/", File.separator);
            File tmp = (new File(s_webserverDocroot)).getParentFile();
            String dtdDir = tmp.getPath()
                    + "\\lib\\classes\\resources\\" + DTD_FILE_NAME;
            dtdDir = dtdDir.replace("\\", "/").replace("/", File.separator);
            String importDir = s_webserverDocroot + File.separator + "_Imports_"
                    + File.separator + DTD_FILE_NAME;
            if (!new File(importDir).exists()) {
                FileUtil.copyFile(new File(dtdDir), new File(importDir));
            }
            
            if (!m_options.getStatus().equals(ImportOptions.ANALYZED))
            {
                // We check if the file is a valid XML file.
                analyzeTbx(m_options.getFileName());

                m_options.setStatus(ImportOptions.ANALYZED);
                m_options.setExpectedEntryCount(m_entryCount);
            }
        }
        catch (Exception ex)
        {
            m_options.setError(ex.getMessage());
        }

        return m_options;
	}

	private void analyzeTbx(String p_url) throws Exception {
		SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");

        CATEGORY.debug("Analyzing document: " + p_url);
        // enable element complete notifications to conserve memory
        
        reader.addHandler("/martif/text/body/termEntry",
                new ElementHandler ()
                    {
                        public void onStart(ElementPath path)
                        {
                            ++m_entryCount;
                        }

                        public void onEnd(ElementPath path)
                        {
                            Element element = path.getCurrent();

                            // prune the current element to reduce memory
                            element.detach();
                        }
                    }
                );

            Document document = reader.read(p_url);
	}

	public boolean hasNext() {
		// Ensure the thread is running
        startThread();

        m_result = m_results.get();

        if (m_result != null)
        {
            return true;
        }

        // No more results, clean up
        stopThread();
        return false;
	}

	public ReaderResult next() {
		return m_result;
	}

	public void setImportOptions(ImportOptions options) {
		m_options = options;
	}
	
	//
    // Private Methods
    //
    private void startThread()
    {
        if (m_thread == null)
        {
            m_results = new ReaderResultQueue (100);
            m_thread = new TbxReaderThread(m_results, m_options, m_termbase);
            m_thread.start();
        }
    }

    private void stopThread()
    {
        if (m_thread != null)
        {
            m_results.consumerDone();
            m_results = null;
            m_thread = null;
        }
    }

}
