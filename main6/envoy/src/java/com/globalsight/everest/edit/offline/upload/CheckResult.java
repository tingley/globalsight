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
package com.globalsight.everest.edit.offline.upload;

import java.text.MessageFormat;
import java.util.List;

public class CheckResult
{
    private String errMsg;
    private String fileName;
    private List<List<String>> errorInternalList;
    private String pageId;
    private String workflowId;
    private String taskId;

    private String format = "<tr BGCOLOR={0}><td style=\"padding:5px;\">{1}</td><td style=\"padding:5px;\">{2}</td></tr>";

    public String getMessage(boolean withHeight)
    {
        if (errorInternalList == null || errorInternalList.size() == 0)
            return null;

        StringBuffer sb = new StringBuffer();
        sb.append("<div CLASS=\"standardText\" style=\"");
        if (withHeight)
        {
            sb.append("height: 200px;");
        }
        sb.append(" padding-bottom: 6px; overflow: auto;\">");
        if (fileName != null)
        {
            sb.append("File: ");
            sb.append(fileName);
        }
        sb.append("<br>");
        sb.append("<table border=\"0\"  cellpadding=0 cellspacing=0 style=\"width: 100%;  \"  CLASS=\"standardText\">"
                + "<tr><td style=\"width: 33% \">");
        if (pageId != null)
        {
            sb.append("Page ID: " + pageId);
        }
        sb.append("</td><td style=\"width: 33% \" align=\"center\">");
        if (workflowId != null)
        {
            sb.append("Workflow ID:" + workflowId);
        }
        sb.append("</td><td align=\"right\">");
        if (taskId != null)
        {
            sb.append("Task ID:" + taskId);
        }
        sb.append("</td></tr>");
        sb.append("<table border=\"0\"  cellpadding=0 cellspacing=0 style=\"width: 100% \" class=\"list, standardText\">");
        sb.append("<tr class=\"tableHeadingBasic\"><td style=\"padding:5px;\" >Segment ID</td><td style=\"padding:5px;\">Internal Tag</td></tr>");

        int i = 0;
        for (List<String> ss : errorInternalList)
        {
            i++;

            String style = "#EEEEEE";
            if (i % 2 == 0)
            {
                style = "#FFFFFF";
            }

            sb.append(MessageFormat.format(format, style, ss.get(0), ss.get(1)));
        }

        sb.append("</table></div>");
        return sb.toString();
    }

    public String getErrMsg()
    {
        return errMsg;
    }

    public void setErrMsg(String errMsg)
    {
        this.errMsg = errMsg;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public List<List<String>> getErrorInternalList()
    {
        return errorInternalList;
    }

    public void setErrorInternalList(List<List<String>> errorInternalList)
    {
        this.errorInternalList = errorInternalList;
    }

    public String getPageId()
    {
        return pageId;
    }

    public void setPageId(String pageId)
    {
        this.pageId = pageId;
    }

    public String getWorkflowId()
    {
        return workflowId;
    }

    public void setWorkflowId(String workflowId)
    {
        this.workflowId = workflowId;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }
}
