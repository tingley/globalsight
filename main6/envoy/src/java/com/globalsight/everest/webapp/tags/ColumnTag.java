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
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.util.StringUtil;

/**
 * Writes a column in a table.
 */
public class ColumnTag extends TagSupport implements TableConstants
{
    private static final long serialVersionUID = 6797674181578276319L;

    private int sortBy;
    private String  label;
    private boolean wrap = true;        //default is header will wrap
    private boolean wrapData = true;    //default is that data will wrap
    private String  width;
    private String  align;              //default is left
    private String style;
    private String filter;
    private String filterSelect;
    private String filterValue;

	public ColumnTag()
    {
        init();
    }

    /**
     * Set horizontal alignment.  Default is left.
     */
    public void setAlign(String align)
    {
        this.align = align;
    }
    /**
     * This is an integer that is usually defined in a Comparator file.  Each
     * column that can be sorted should be defined in a Comparator file.
     */
    public void setSortBy(int sortBy)
    {
        this.sortBy = sortBy;
    }

    /**
     * This is a value in the properties file.  It is the label for the column.
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * If set to true, allows the column header to wrap.  Default is true.
     */
    public void setWrap(boolean wrap)
    {
        this.wrap = wrap;
    }

    /**
     * If set to true, allows the column data to wrap.  Default is true.
     */
    public void setWrapData(boolean wrapData)
    {
        this.wrapData = wrapData;
    }

    /**
     * Set the width of the column.  Default is html default.
     */
    public void setWidth(String width)
    {
        this.width = width;
    }

    public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

    public String getAlign()
    {
        return align;
    }

    public int getSortBy()
    {
        return sortBy;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean getWrap()
    {
        return wrap;
    }

    public boolean getWrapData()
    {
        return wrapData;
    }

    public String getWidth()
    {
        return width;
    }

    public int doEndTag() throws JspException
    {

        JspWriter    out    = pageContext.getOut();

        // get helper bean
        TableTag table = (TableTag) getParent();
        if (!table.skip())
        {
            try
            {
                out.println("    </td>");
            } catch (IOException e) {
                throw new JspTagException("I/O exception " + e.getMessage());
            }
        }
        return EVAL_PAGE;
    }

    public void release()
    {
        init();
    }

    public void init()
    {
        sortBy = -1;
        label = null;
        wrap = true;
        wrapData = true;
        width = null;
        align = "left";
        style = null;
        filter = null;
        filterSelect = null;
        filterValue = null;
    }

    public int doStartTag() throws JspException
    {
        JspWriter    out    = pageContext.getOut();
        TableTag table = (TableTag) getParent();
        if (table == null) {
            throw new JspTagException("amb:column must be embedded within a amb:table tag.");
        }
        ResourceBundle bundle = PageHandler.getBundle(pageContext.getSession());
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String key = table.getKey();

        try
        {
            if (table.showHeader())
            {
                String pageUrl = table.getPageUrl();
                int pageNum = ((Integer)request.getAttribute(key+PAGE_NUM)).intValue();

                //for gbs-2599
                HttpSession session = request.getSession(false);
                PermissionSet userPerms = (PermissionSet) session.getAttribute(
                        WebAppConstants.PERMISSIONS);

                String filterContent = (String) pageContext.getAttribute("filterContent");

                // print column header
                out.print("    <td ");
                if (wrap == false)
                {
                    out.print(" nowrap");
                }
                out.println(">");
                if (label.equals(""))
                {
                    out.println("&nbsp;");
                }
                else if (label.equals("checkbox"))
                {
                    out.println("<input type=\"checkbox\" name=\"selectAll\" id=\"selectAll\" onclick=\"handleSelectAll()\" />");
                }

                //for gbs-2599
                else if (label.equals("multiCheckbox_1"))
                {
                    if (userPerms.getPermissionFor(Permission.ACTIVITIES_JOB_COMMENTS_DOWNLOAD)) {
                        out.println("<input type=\"checkbox\" name=\"multiSelectAll_1\" id=\"multiSelectAll_1\" onclick=\"handleMultiSelectAll_1()\" />");
                    }
                }
                else if (label.equals("multiCheckbox_2"))
                {
                    if (userPerms.getPermissionFor(Permission.ACTIVITIES_COMMENTS_DOWNLOAD)) {
                        out.println("<input type=\"checkbox\" name=\"multiSelectAll_2\" id=\"multiSelectAll_2\" onclick=\"handleMultiSelectAll_2()\" />");
                    }
                }

                else if (sortBy != -1)
                {
                    out.print("        <a class=\"sortHREFWhite\" href=\"" + pageUrl +
                        "&" + key + PAGE_NUM + "=" + pageNum +
                        "&" + key + SORTING + "=" + sortBy +
                        "&" + key + "doSort=true");

                    String taskListStart = table.getTaskListStart();
                    if (taskListStart != null)
                    {
                        out.print("&" + WebAppConstants.TASK_LIST_START + "=" + taskListStart);
                    }

                    String filterSelection = table.getFilterSelection();
                    if (filterSelection != null)
                        out.print("&" + key + FILTER + "=" + filterSelection);
                    out.print("\">" + bundle.getString(label));
                    SessionManager sessionMgr = (SessionManager)pageContext.getSession()
                        .getAttribute(WebAppConstants.SESSION_MANAGER);
                    Integer sortChoice = (Integer)sessionMgr.getAttribute(key + SORTING);
                    if (sortChoice.intValue() == sortBy)
                    {
                        Boolean reverseSort = (Boolean)sessionMgr.getAttribute(
                                                                key + REVERSE_SORT);
                        if (reverseSort.booleanValue() == false)
                            out.print("<img src=\"/globalsight/images/sort-up.gif\" width=7 height=4 hspace=1 border=0>");
                        else
                            out.print("<img src=\"/globalsight/images/sort-down.gif\" width=7 height=4 hspace=1 border=0>");
                    }
                    out.println("</a>");
                }
                else
                {
                    out.println(bundle.getString(label));
                }
                if (table.isHasFilter()) {
                    if (StringUtil.isEmpty(filterContent))
                        filterContent = "";

                    if (StringUtil.isNotEmpty(filter)) {
						if (filterValue != null) {
                    		filterValue = filterValue.replace("'", "&#39;");
                    	}
                        filterContent += "<td>";
                        filterContent += "<input type='text' id='" + filter + "' name='" + filter + "' value='" + filterValue + "' onkeydown='filterItems(event);' class='standardText' title='Input the filter, press Enter to filter'/>";
                        filterContent += "</td>";
                    }
                    else if (StringUtil.isEmpty(filter) && StringUtil.isNotEmpty(filterSelect)) {
                        filterContent += "<td>";
                        filterContent += "<select id='" + filterSelect + "' name='" + filterSelect + "' onkeydown='filterSelectItems(event);' class='standardText' title='Select the filter,press Enter to filter'></select>";
                        filterContent += "</td>";
                    }
                    else {
                        filterContent += "<td>&nbsp;</td>";
                    }
                    pageContext.setAttribute("filterContent", filterContent);
                }
            }
            else if (!table.skip())
            {
                // print td for data
                out.print("    <td style=\"padding-bottom: 5px; padding-top: 5px;\" class=\"standardText\" align=" + align);
                if (width != null)
                {
                    out.print(" width=\"" + width + "\"");
                }
                if (wrapData == false)
                {
                                    out.print(" nowrap");
                }
                if (style != null) {
                	out.print(" style=\"" + style + "\"");
                }
                out.println(">");
            }
        }
        catch (IOException e)
        {
            throw new JspTagException("columnTag:I/O exception " + e.getMessage());
        }
        if (table.showHeader() || table.skip())
            return SKIP_BODY;
        else
            return EVAL_PAGE;
    }

    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public String getFilterSelect()
    {
        return filterSelect;
    }

    public void setFilterSelect(String filterSelect)
    {
        this.filterSelect = filterSelect;
    }

    public String getFilterValue()
    {
        return filterValue;
    }

    public void setFilterValue(String filterValue)
    {
        this.filterValue = filterValue;
    }
}
