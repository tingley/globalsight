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

// Standard Java packages
import java.io.Serializable;
import java.util.Vector;
import java.util.Date;
import java.util.Map;
import java.util.Hashtable;

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.util.GlobalSightLocale;

/**
* This class is a subclass of SearchCriteriaParameters where has
* bunch of setters for setting the search criteria value for the
* WorkflowInstance search.
*/
public class WorkflowInstanceSearchParameters extends SearchCriteriaParameters {

    public transient static final int WORKFLOW_STATE = 0;
    public transient static final int START_DATE_RANGE = 1;
    public transient static final int DUE_DATE_RANGE = 2;
    public transient static final int ID_RANGE = 3;
    public transient static final int SOURCE_LOCALE = 4;
    public transient static final int TARGET_LOCALE = 5;
    public transient static final int CURRENT_TASK = 6;
    public transient static final int PROJECT_MANAGER = 7;
    public transient static final int WORKFLOW_PARTICIPANT = 8;

    /**
     *  Default constructor.
     */
    public WorkflowInstanceSearchParameters() {
        super();
    }

    public void setWorkflowState(String state) {
        addElement(WORKFLOW_STATE, state);
    }

    public void setStartDateRange(Date[] startDateRange) {
        addElement(START_DATE_RANGE, startDateRange);
    }

    public void setDueDateRange(Date[] dueDateRange) {
        addElement(DUE_DATE_RANGE, dueDateRange);
    }

    public void setIdRange(Long[] idRange) {
        addElement(ID_RANGE, idRange);
    }

    public void setSourceLocale(GlobalSightLocale sourceLocale) {
        addElement(SOURCE_LOCALE, sourceLocale);
    }

    public void setTargetLocale(GlobalSightLocale targetLocale) {
        addElement(TARGET_LOCALE, targetLocale);
    }

    public void setCurrentTask(String curTask) {
        addElement(CURRENT_TASK, curTask);
    }

    public void setProjectManager(String projectManager) {
        addElement(PROJECT_MANAGER, projectManager);
    }

    public void setWorkflowParticipant(String workflowParticipant) {
        addElement(WORKFLOW_PARTICIPANT, workflowParticipant);
    }

}

