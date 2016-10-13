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
package com.globalsight.everest.webapp.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.reports.Constants;

public class WorkflowStatusTag extends TagSupport
{
    private String type;
    private String param;

    private static final String DISPATCHED = "DISPATCHED";
    public static final String PENDING = "PENDING";
    public static final String EXPORTED = "EXPORTED";
    public static final String READY = "READY_TO_BE_DISPATCHED";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String LOCALIZED = "LOCALIZED";
    public static final String QUOTE = "\"";

    public WorkflowStatusTag()
    {
        type = "select";
        param = "wfstatus";
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setParam(String param)
    {
        this.param = param;
    }

    public int doStartTag() throws JspTagException
    {
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspTagException
    {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();

        JspWriter out = pageContext.getOut();
        try
        {
            if (type.equals("select"))
            {
                if (param.equals("wfstatus"))
                {
                    HashMap wfStatesHashMap;
                    wfStatesHashMap = (HashMap) req
                            .getAttribute(Constants.WFSTATUS_ARRAY);
                    if (wfStatesHashMap != null)
                    {
                        Iterator keySet = wfStatesHashMap.keySet().iterator();
                        out.write("<select name=wfstatus>");
                        while (keySet.hasNext())
                        {
                            String key = (String) keySet.next();
                            String value = (String) wfStatesHashMap.get(key);
                            if (key.equals(DISPATCHED))
                            {
                                out.write("<option value=" + QUOTE + key
                                        + QUOTE + " selected>" + value
                                        + "</option>");
                            }
                            else
                            {
                                out.write("<option value=" + QUOTE + key
                                        + QUOTE + ">" + value + "</option>");
                            }
                        }
                        out.write("</select>");
                    }
                }
                else if (param.equals("projectmgr"))
                {
                    ArrayList projectManagers;
                    projectManagers = (ArrayList) req
                            .getAttribute(Constants.PROJECT_MGR_ARRAY);
                    String defaultValue = (String) req
                            .getAttribute(Constants.PROJECT_MGR_DEFVALUE);
                    defaultValue = UserUtil.getUserNameById(defaultValue);
                    if (projectManagers != null && defaultValue != null)
                    {
                        out.write("<select name=projectMgr>");
                        for (int i = 0; i < projectManagers.size(); i++)
                        {
                            String value = (String) projectManagers.get(i);
                            value = UserUtil.getUserNameById(value);
                            if (value.equals(defaultValue))
                            {
                                out.write("<option value=" + QUOTE + value
                                        + QUOTE + " selected>" + value
                                        + "</option>");
                            }
                            else
                            {
                                out.write("<option value=" + QUOTE + value
                                        + QUOTE + ">" + value + "</option>");
                            }
                        }
                        out.write("</select>");
                    }
                }
                else if (param.equals("currency"))
                {
                    ArrayList currencys = (ArrayList) req
                            .getAttribute(Constants.CURRENCY_ARRAY);
                    ArrayList currencyLabels = (ArrayList) req
                            .getAttribute(Constants.CURRENCY_ARRAY_LABEL);
                    String defaultValue = (String) req
                            .getAttribute(Constants.PIVOT_CURRENCY_PM_DEFVALUE);
                    if (currencys != null && defaultValue != null)
                    {
                        out.write("<select name=currency>");
                        for (int i = 0; i < currencys.size(); i++)
                        {
                            String value = (String) currencys.get(i);
                            String lable = (String) currencyLabels.get(i);
                            if (value.equals(defaultValue))
                            {
                                out.write("<option value=" + QUOTE + value
                                        + QUOTE + " selected>" + lable
                                        + "</option>");
                            }
                            else
                            {
                                out.write("<option value=" + QUOTE + value
                                        + QUOTE + ">" + lable + "</option>");
                            }
                        }
                        out.write("</select>");
                    }
                }
            }
            else if (type.equals("label"))
            {
                String label = null;
                if (param.equals("wfstatuslabel"))
                {
                    label = (String) req.getAttribute(Constants.WFSTATUS_LABEL);
                }
                else if (param.equals("projectmgrlabel"))
                {
                    label = (String) req
                            .getAttribute(Constants.PROJECT_MGR_LABEL);
                }
                else if (param.equals("currencylabel"))
                {
                    label = (String) req
                            .getAttribute(Constants.CURRENCY_DISPLAYNAME_LABEL);
                }

                if (label != null)
                {
                    out.write(label);
                }
            }
        }
        catch (IOException ex)
        {
            throw new JspTagException(
                    "Fatal error:custom tag could not write to JSP out");
        }

        return EVAL_PAGE;
    }
}
