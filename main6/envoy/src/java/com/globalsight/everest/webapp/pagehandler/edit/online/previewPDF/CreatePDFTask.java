/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.everest.webapp.pagehandler.edit.online.previewPDF;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.webapp.pagehandler.edit.online.EditorState;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * The Thread Task for creating PDF File, and the thread can be canceled.
 */
public class CreatePDFTask implements Callable<File>, PreviewPDFConstants
{
    private static final Logger LOGGER = Logger.getLogger(CreatePDFTask.class);
    private TargetPage m_targetPage;
    private String m_userId;
    private int m_fileVersionType;
    private EditorState m_editState;

    public CreatePDFTask(TargetPage p_targetPage, String p_userId, int p_fileVersionType)
    {
        m_targetPage = p_targetPage;
        m_userId = p_userId;
        m_fileVersionType = p_fileVersionType;
    }

    @Override
    public File call() throws Exception
    {
        File pdfFile = null;
        PreviewPDFHelper helper = new PreviewPDFHelper();
        try
        {
            switch (m_fileVersionType)
            {
                case ADOBE_FM9:
                    pdfFile = helper.createPDF4FM9(m_targetPage, m_userId, m_editState);
                    break;
                case ADOBE_TYPE_IDML:
                    pdfFile = helper.createPDF4IDML(m_targetPage, m_userId);
                    break;
                default:
                    pdfFile = helper.createPDF4INDDAndInx(m_targetPage, m_userId);
            }
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();
            message.append("Create PDF Error, for TargetPage:")
                   .append(m_targetPage.getExternalPageId());
            message.append(", By User:").append(m_userId).append(". \r\n");
            LOGGER.error(message, e);
        }
        finally
        {
            // Close Session
            HibernateUtil.closeSession();
        }     
        
        return pdfFile;
    }

    public void setEditState(EditorState p_state)
    {
        m_editState = p_state;
    }
    
}
