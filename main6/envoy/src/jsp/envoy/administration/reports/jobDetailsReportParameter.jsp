<%@ page contentType="text/html; charset=UTF-8" errorPage="/envoy/common/error.jsp"
         import="java.sql.*,
                 java.util.ArrayList,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator,
                 java.util.ResourceBundle,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.reports.Constants,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.util.system.SystemConfiguration,
                 com.globalsight.everest.util.system.SystemConfigParamNames" session="true"
%>
<!--
<%@ taglib uri="/WEB-INF/tlds/workflowStatusTag.tld" prefix="wfsTag"%>
-->
    <%
    ResourceBundle bundle = PageHandler.getBundle(session);
    %>
<html>
    <head>
        <title><%=bundle.getString("job_details_report_parameters")%></title>
    </head>
    <body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <%
        String jobStatusLabel = (String)request.getAttribute(Constants.JOB_STATUS_LABEL);
        if(jobStatusLabel  == null)
        {
            jobStatusLabel = "";
        }
        HashMap jobRadioLabelMap = (HashMap)request.getAttribute(Constants.JOB_RADIO_LABEL_MAP);
        if(jobRadioLabelMap  == null)
        {
            jobRadioLabelMap = new HashMap();
        }
        HashMap jobSelectLabelMap = (HashMap)request.getAttribute(Constants.JOB_SELECT_LABEL_MAP);
        HashMap jobSelectNameMap = (HashMap)request.getAttribute(Constants.JOB_SELECT_NAME_MAP);

        ArrayList dispatchedJobidArray = (ArrayList)request.getAttribute(Constants.DISPATCHED_JOBID_ARRAY);
        ArrayList localizedJobidArray = (ArrayList)request.getAttribute(Constants.LOCALIZED_JOBID_ARRAY);
        ArrayList exportedJobidArray = (ArrayList)request.getAttribute(Constants.EXPORTED_JOBID_ARRAY);        
        ArrayList archivedJobidArray = (ArrayList)request.getAttribute(Constants.ARCHIVED_JOBID_ARRAY);
        
        String currencyLabel = null;
        ArrayList currencyArray = null;
        ArrayList currencyArraylabel = null;

        if(SystemConfiguration.getInstance().getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED)) {
            currencyLabel = (String)request.getAttribute(Constants.CURRENCY_DISPLAYNAME_LABEL);
            currencyArray = (ArrayList)request.getAttribute(Constants.CURRENCY_ARRAY);
            currencyArraylabel = (ArrayList)request.getAttribute(Constants.CURRENCY_ARRAY_LABEL);
        }
        
        SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        HashMap map = (HashMap)sessionManager.getReportAttribute(Constants.JOBDETAILS_REPORT_KEY);
        HashMap jobListMap = (HashMap) map.get(Constants.JOB_LIST_MAP);
                
    %>    
        <table border="0" cellspacing="0" cellpadding="5" height="543" width="100%">
            <tr bgcolor="#ABB0D3" valign="top"> 
                <td height="40" colspan="2">
                    <b><font face="Verdana, Arial, Helvetica, sans-serif"><%=bundle.getString("report")%></font></b>: 
                    <font face="Verdana, Arial, Helvetica, sans-serif"><%=bundle.getString("job_details")%></font>
                </td>
            </tr>
            <tr bgcolor="ccffff"> 
                <td height="6" colspan="2"></td>
            </tr>
            <tr> 
                <td height="452" valign="top" align="left" width="30%" background="/globalsight/images/parambar.jpg">
                    <font face="Verdana, Arial, Helvetica, sans-serif" size="2"></font>
                </td>
                <td height="452" valign="top" align="left" width="70%" bgcolor="#E9E9E9">
                    <font face="Verdana, Arial, Helvetica, sans-serif" size="3">
                        <form name=request action="/globalsight/TranswareReports?reportPageName=JobDetails&act=Create" method=POST>
                            <center>
                                <table border=0 cellspacing=10>
                                    <tr>
                                        <td align=left colspan=2><%=jobStatusLabel%></td>
                                    </tr>
                                    <tr>
                                        <td colspan=2>
                                            <TABLE border=0 width='100%'>
                                              <tr>
                                              <% 
                                                    Collection keyCollection = (Collection) jobRadioLabelMap.keySet();
                                                    String jobRadioLabel = "";
                                                    ArrayList currentArrayList = null;
                                                    String jobRadioValue = "";
                                                    boolean isChecked = true;
                                                    for(Iterator iter = keyCollection.iterator();iter.hasNext();)
                                                    {
                                                        jobRadioValue = (String)iter.next();
                                                        jobRadioLabel = (String)jobRadioLabelMap.get(jobRadioValue);
                                              %>
                                                  <td>
                                                    <%
                                                                       if(isChecked)
                                       {
                                    %>
                                                    <INPUT TYPE=RADIO NAME="jobstatus" checked VALUE="<%=jobRadioValue%>"><%=jobRadioLabel%>
                                                    <%
                                                        isChecked = false;
                                                       }
                                                       else
                                                       {
                                                    %>
                                                    <INPUT TYPE=RADIO NAME="jobstatus" VALUE="<%=jobRadioValue%>"><%=jobRadioLabel%>
                                                    <% }   %>
                                                  </td>
                                                  <%  }   %>
                                              </tr> 
                                            </TABLE>
                                        </td>
                                    </tr>
                                    <% 
                                        Collection keyCollection2 = (Collection) jobRadioLabelMap.keySet();
                                        String jobRadioValue2 = "";
                                        String jobSelectLabel = "";
                                        String jobSelectName = "";
                                        for(Iterator iter2 = keyCollection2.iterator();iter2.hasNext();)
                                        {
                                            jobRadioValue2 = (String)iter2.next();
                                            jobSelectLabel = (String)jobSelectLabelMap.get(jobRadioValue2);
                                            jobSelectName = (String)jobSelectNameMap.get(jobRadioValue2);


                                            
                                            if(jobRadioValue2.equals("DISPATCHED"))
                                            {
                                                currentArrayList = dispatchedJobidArray;
                                            }
                                            else if(jobRadioValue2.equals("LOCALIZED"))
                                            {
                                                currentArrayList = localizedJobidArray;
                                            }
                                            else if(jobRadioValue2.equals("EXPORTED"))
                                            {
                                                currentArrayList = exportedJobidArray;
                                            }
                                            else if(jobRadioValue2.equals("ARCHIVED"))
                                            {
                                                currentArrayList = archivedJobidArray;
                                            }
                                    %>   
                                    <tr>
                                        <td align=right><%=jobSelectLabel%></td>
                                        <td >
                                            <SELECT NAME=<%=jobSelectName%> style="width:300px">
                                            <%
                                                String currentJobid = null;
                                                boolean isSelected = true;
                                                for(Iterator iterJob = currentArrayList.iterator();iterJob.hasNext();)
                                                {
                                                    currentJobid = (String)iterJob.next();

                                                    if(isSelected) 
                                                    {
                                            %>    
                                                    <OPTION title="<%=jobListMap.get(currentJobid)%>" VALUE="<%=currentJobid%>" selected><%=jobListMap.get(currentJobid)%></OPTION>
                                            <%
                                                      isSelected = false;
                                                    }
                                                    else
                                                    {
                                            %>
                                                    <OPTION title="<%=jobListMap.get(currentJobid)%>" VALUE="<%=currentJobid%>" ><%=jobListMap.get(currentJobid)%></OPTION>
                                                            <%      }
                                                                }  %>
                                            </SELECT>
                                        </td>
                                    </tr>
                                    <%  }  

                                        if (currencyLabel != null) {
                                    %>
                                    <tr>
                                        <td align=right><%=currencyLabel%></td>
                                        <td>
                                            <SELECT NAME="currency">
                                            <%
                                            for (int i = 0; i < currencyArray.size(); i++)
                                            {
                                                String currencyText = (String) currencyArray.get(i);
                                                String currencyDisplay = (String) currencyArraylabel.get(i);
                                            %>
                                                    <OPTION VALUE="<%=currencyText%>" ><%=currencyDisplay%>
                                            <%  }  %>
                                            </SELECT>        
                                        </td>
                                    </tr>
                                    <% } %>
                                    <tr>
                                        <td colspan=2>
                                            <center>
                                                <input type=Submit value="<%=bundle.getString("lb_shutdownSubmit")%>"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                                                <input type=button value="<%=bundle.getString("lb_cancel")%>" onclick="window.close()"/>
                                            </center>
                                        </td>
                                    </tr>
                                </table>
                            </center>
                        </form>
                    </font>
                </td>
            </tr>
            <tr>
                <td width="27%">&nbsp;</td>
                <td width="73%">&nbsp;</td>
            </tr>
        </table>
    </body>
</html>

