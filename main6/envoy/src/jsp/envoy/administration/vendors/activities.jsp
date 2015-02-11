<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.vendormanagement.VendorRole,
                com.globalsight.everest.workflow.Activity,
                com.globalsight.everest.costing.Rate,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>


<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   
    
    //Labels
    String lbNoRate = bundle.getString("lb_no_rate");

    // Data
    boolean isCostingEnabled = false;
    try
    {
       SystemConfiguration sc = SystemConfiguration.getInstance();
       isCostingEnabled = sc.getBooleanParameter(
           SystemConfigParamNames.COSTING_ENABLED);
    }
    catch (Exception e )
    {
    }
    HashMap activitiesHash = (HashMap)request.getAttribute("activities");
    Set activities = activitiesHash.keySet();
    HashMap selectedActivitiesHash = (HashMap)request.getAttribute("selectedActivities");
        
%>

<script language="JavaScript">
// Return true if at least one activity is set
function confirmActivities(theForm)
{
    for (i = 0; i < theForm.elements.length; i++)
    {
        if ((theForm.elements[i].type == "checkbox") &&
          (theForm.elements[i].checked == true))
        {
            return true;
        }
    }
    alert("<%=bundle.getString("jsmsg_users_activities")%>");
    return false;
}

</script>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
      <TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="list">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_activity_types")%>:
          </td>
          <% if (isCostingEnabled) { %>
          <td class="standardText">
            <%=bundle.getString("lb_expense")%>:
          </td>
          <% } %>
        </tr>
<%      Iterator iterator = activities.iterator();
        while (iterator.hasNext())
        {
            Activity activity = (Activity)iterator.next();
            List rates = (List)activitiesHash.get(activity);
            Long selected = null;
            if (selectedActivitiesHash != null)
            {
                selected = (Long)selectedActivitiesHash.get(activity.getName());
            }
%>
            <tr>
                <td class="standardText">
                    <input type="checkbox"
                         name="<%=activity.getActivityName()%>" value="true"
<%
                    if (selected != null)
                        out.print(" checked ");
%>
                    >
                    <%= activity.getActivityName()%>
                </td>
          <% if(isCostingEnabled) { %>
                <td class="standardText">
                    <select name="<%=activity.getActivityName()%>_expense">
                        <option value="-1">
                            <%=lbNoRate%>
                        </option>
<%
                    if (rates != null)
                    {
                        for (int i = 0; i < rates.size(); i++)
                        {
                            Rate rate = (Rate)rates.get(i);
%>
                            <option value=<%=rate.getId()%>
<%
                            if (selected != null && selected.longValue() == rate.getId())
                                out.println(" selected ");
%>
                            >
                                <%=rate.getName()%>
                            </option>
<%
                        }
                    }
%>
                    </select>
                </td>
          <% } %>
            </tr>
<%        } %>
      </TABLE>
