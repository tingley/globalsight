/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.workflow;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.util.Entry;

/**
 * The vo object to store the data the skip activity operation.
 * 
 */
public class SkipActivityVo
{
    /* id of the workflow */
    private long workflowId;

    private String targetLocale;

    private List<Entry> list = null;

    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    public long getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(long workflowId)
    {
        this.workflowId = workflowId;
    }

    public List<Entry> getList()
    {
        return list;
    }

    public void setList(List<Entry> list)
    {
        this.list = list;
    }

}
