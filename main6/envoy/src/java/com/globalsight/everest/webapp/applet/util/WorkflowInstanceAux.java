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
package com.globalsight.everest.webapp.applet.util;

import java.io.Serializable;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.util.GlobalSightLocale;

public class WorkflowInstanceAux implements Serializable {

    public static final int DO_NOTHING = 0;
    public static final int DO_DISPATCH = 1;
    public static final int DO_EXPORT = 2;
    public static final int DO_MODIFY = 3;

    private int m_action;
    private long m_workflowId;
    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;
    private String m_l10nProfileName;
    private String m_state;

    public WorkflowInstanceAux() {
    }

    public String toString() {
        Long id = new Long(m_workflowId);
        return id.toString();
    }

    public int getAction() {
        return m_action;
    }

    public void setAction(int p_action) {
        m_action = p_action;
    }

    public String getL10nProfileName() {
        return m_l10nProfileName;
    }

    public void setL10nProfileName(String p_l10nProfileName) {
        m_l10nProfileName = p_l10nProfileName;
    }

    public GlobalSightLocale getSourceLocale() {
        return m_sourceLocale;
    }

    public void setSourceLocale(GlobalSightLocale p_sourceLocale) {
        m_sourceLocale = p_sourceLocale;
    }

    public String getState() {
        return m_state;
    }

    public void setState(String p_state) {
        m_state = p_state;
    }

    public GlobalSightLocale getTargetLocale() {
        return m_targetLocale;
    }

    public void setTargetLocale(GlobalSightLocale p_targetLocale) {
        m_targetLocale = p_targetLocale;
    }

    public long getWorkflowId() {
        return m_workflowId;
    }

    public void setWorkflowId(long p_workflowId) {
        m_workflowId = p_workflowId;
    }

    public boolean equals(Object o) {
        if (o != null) {
            if (o instanceof WorkflowInstanceAux) {
                WorkflowInstanceAux tmp = (WorkflowInstanceAux) o;
                if (this.getWorkflowId() == tmp.getWorkflowId()) {
                    return true;
                }
            }
        }

        return false;
    }

}


