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

package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.planview;

import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;

public class NodeModel
{
    public static final int OR_NODE = 1;
    public static final int AND_NODE = 0;
    public static final int COND_NODE = 2;
    private int m_nNodeType;
    private boolean m_bNotification;
    private String m_strDesc;
    private String m_strName;
    private String m_strScript;
    private String m_strAddress;
    private WorkflowTask  m_plNode;

    /**
       @roseuid 372F8CB90116
     */
    public NodeModel(int p_nNodeType, WorkflowTask  p_plNode)
    {
        m_nNodeType = p_nNodeType;
        m_plNode = p_plNode;
        initValues();

    }

    /**
       @roseuid 372F8CB90121
     */
    private void initValues()
    {
        // retrieve initial values
        if (m_plNode != null)
        {
            m_strName = m_plNode.getActivityName();
            /*if (m_plNode.getSequence() > -1 )
                m_strDesc = new Integer(m_plNode.getSequence()).toString();
            else
                m_strDesc="";  */
            m_strDesc = m_plNode.getDesc();
            try
            {
                m_strScript = m_plNode.getEpilogueScript();
            }
            catch (Exception mie)
            {
            }
            return;
        }
        // properties
        m_strName = new String("");
        m_strDesc = new String("");
        // scripting
        m_strScript = new String("");
        // no email support in server yet
        m_bNotification = false;
        m_strAddress = new String("");

    }

    /**
       @roseuid 372F8CB90122
     */
    public void saveChanges() throws Exception {
        if (m_plNode != null)
        {
            m_plNode.setName( m_strName );
            m_plNode.setDesc( m_strDesc );
            m_plNode.setEpilogueScript(m_strScript);

            // no support for email yet
        }

    }

    /**
       @roseuid 372F8CB90123
     */
    public boolean getNotification()
    {
        return m_bNotification;

    }

    /**
       @roseuid 372F8CB90124
     */
    public void setNotification(boolean p_bNotification)
    {
        m_bNotification = p_bNotification;

    }

    /**
       @roseuid 372F8CB90126
     */
    public String getScript()
    {
        return m_strScript;

    }

    /**
       @roseuid 372F8CB90127
     */
    public void setScript(String p_strScript)
    {
        m_strScript = p_strScript;

    }

    /**
       @roseuid 372F8CB90129
     */
    public String getAddress()
    {
        return m_strAddress;

    }

    /**
       @roseuid 372F8CB9012A
     */
    public void setAddress(String p_strAddr)
    {
        m_strAddress = p_strAddr;

    }

    /**
       @roseuid 372F8CB9012C
     */
    public String getPropDesc()
    {
        return m_strDesc;

    }

    /**
       @roseuid 372F8CB9012D
     */
    public String getPropName()
    {
        return m_strName;

    }

    /**
       @roseuid 372F8CB9012E
     */
    public void setPropDesc(String p_strDesc)
    {
        m_strDesc = p_strDesc;

    }

    /**
       @roseuid 372F8CB90130
     */
    public void setPropName(String p_strName)
    {
        m_strName = p_strName;

    }
}
