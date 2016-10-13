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
//
// Copyright (c) 2005 GlobalSight Corporation. All rights reserved.
//
// THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
// GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
// IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
// OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
// AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
//
// THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
// SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
// UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
// BY LAW.
//

package com.globalsight.everest.page.pageupdate;

import com.globalsight.everest.page.SourcePage;

import com.globalsight.util.gxml.GxmlRootElement;

import com.globalsight.ling.jtidy.TidyMessage;
import com.globalsight.ling.jtidy.TidyMessageListener;

import java.util.ArrayList;
import java.util.Collection;

public class UpdateState
    implements TidyMessageListener
{
    //
    // Members
    //

    private SourcePage m_sourcePage;
    private ArrayList m_targetPages;
    private ArrayList m_targetLocales;

    private String m_gxml;
    private String m_dataformat;
    private ArrayList m_validationMessages;
    private boolean m_validated = false;

    private int m_fileLength = 0;

    private GxmlRootElement m_gxmlRoot;

    private boolean m_hasGsTags = false;

    private ArrayList m_originalTus;
    private ArrayList m_newTus;
    private ArrayList m_unmodifiedTus;
    private ArrayList m_modifiedTus;

    private ArrayList m_persistenceCommands;

    //
    // Constructor
    //

    public UpdateState(SourcePage p_sourcePage, String p_gxml)
    {
        m_sourcePage = p_sourcePage;
        m_gxml = p_gxml;
    }

    //
    // Public Methods
    //

    public String getGxml()
    {
        return m_gxml;
    }

    public void setGxml(String p_arg)
    {
        m_gxml = p_arg;
    }

    public String getDataFormat()
    {
        return m_dataformat;
    }

    public void setDataFormat(String p_arg)
    {
        m_dataformat = p_arg;
    }

    public boolean getValidated()
    {
        return m_validated;
    }

    public void setValidated(boolean p_arg)
    {
        m_validated = p_arg;
    }

    public ArrayList getValidationMessages()
    {
        return m_validationMessages;
    }

    public void clearValidationMessages()
    {
        m_validationMessages = null;
    }

    public void addValidationMessage(String p_arg)
    {
        if (m_validationMessages == null)
        {
            m_validationMessages = new ArrayList();
        }

        m_validationMessages.add(p_arg);
    }

    /**
     * Called by JTidy when a warning or error occurs.
     * @param message Tidy message
     */
    public void messageReceived(TidyMessage p_message)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_message.getLine());
        sb.append(";");
        sb.append(p_message.getColumn());
        sb.append(";");
        sb.append(p_message.getLevel());
        sb.append(";");
        sb.append(p_message.getMessage());

        addValidationMessage(sb.toString());
    }

    public int getFileLength()
    {
        return m_fileLength;
    }

    public void setFileLength(int p_arg)
    {
        m_fileLength = p_arg;
    }

    public GxmlRootElement getGxmlRoot()
    {
        return m_gxmlRoot;
    }

    public void setGxmlRoot(GxmlRootElement p_arg)
    {
        m_gxmlRoot = p_arg;
    }

    public boolean getHasGsTags()
    {
        return m_hasGsTags;
    }

    public void setHasGsTags(boolean p_arg)
    {
        m_hasGsTags = p_arg;
    }

    public ArrayList getOriginalTus()
    {
        return m_originalTus;
    }

    public void setOriginalTus(ArrayList p_arg)
    {
        m_originalTus = p_arg;
    }

    public ArrayList getNewTus()
    {
        return m_newTus;
    }

    public void setNewTus(ArrayList p_arg)
    {
        m_newTus = p_arg;
    }

    public ArrayList getUnmodifiedTus()
    {
        if (m_unmodifiedTus == null)
        {
            m_unmodifiedTus = new ArrayList();
        }

        return m_unmodifiedTus;
    }

    public void setUnmodifiedTus(ArrayList p_arg)
    {
        m_unmodifiedTus = p_arg;
    }

    public void addUnmodifiedTu(Object p_arg)
    {
        if (m_unmodifiedTus == null)
        {
            m_unmodifiedTus = new ArrayList();
        }

        m_unmodifiedTus.add(p_arg);
    }

    public ArrayList getModifiedTus()
    {
        if (m_modifiedTus == null)
        {
            m_modifiedTus = new ArrayList();
        }

        return m_modifiedTus;
    }

    public void setModifiedTus(ArrayList p_arg)
    {
        m_modifiedTus = p_arg;
    }

    public void addModifiedTu(Object p_arg)
    {
        if (m_modifiedTus == null)
        {
            m_modifiedTus = new ArrayList();
        }

        m_modifiedTus.add(p_arg);
    }

    public SourcePage getSourcePage()
    {
        return m_sourcePage;
    }

    public void setSourcePage(SourcePage p_arg)
    {
        m_sourcePage = p_arg;
    }

    public ArrayList getTargetPages()
    {
        return m_targetPages;
    }

    public void setTargetPages(Collection p_arg)
    {
        m_targetPages = new ArrayList(p_arg);
    }

    public ArrayList getTargetLocales()
    {
        return m_targetLocales;
    }

    public void setTargetLocales(ArrayList p_arg)
    {
        m_targetLocales = p_arg;
    }

    public ArrayList getPersistenceCommands()
    {
        if (m_persistenceCommands == null)
        {
            m_persistenceCommands = new ArrayList();
        }

        return m_persistenceCommands;
    }

    public void addPersistenceCommand(Object p_arg)
    {
        if (m_persistenceCommands == null)
        {
            m_persistenceCommands = new ArrayList();
        }

        m_persistenceCommands.add(p_arg);
    }
}
