<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.vendormanagement.VendorRole,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.util.GlobalSightLocale,
                java.util.Enumeration,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="rates" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   
    String ratesURL = rates.getPageURL() + "&action=rates";
    
    //Labels
    String lbChoose = bundle.getString("lb_choose");
    String lbSourceLocale = bundle.getString("lb_source_locale");
    String lbTargetLocale = bundle.getString("lb_target_locale");

    // Data
    Hashtable locales = (Hashtable)sessionMgr.getAttribute("remainingLocales");
    GlobalSightLocale sourceLocale = (GlobalSightLocale)
            sessionMgr.getAttribute("sourceLocale");
    GlobalSightLocale targetLocale = (GlobalSightLocale)
        sessionMgr.getAttribute("targetLocale");
    int srcSelected = 0;
%>

<script language="JavaScript">
<%

    // Set up source and target arrays
    Enumeration srcs = locales.keys();
    int cnt = 1;
    while (srcs.hasMoreElements())
    {
        GlobalSightLocale source = (GlobalSightLocale)srcs.nextElement();
        Vector targets = (Vector)locales.get(source);
        out.print("var targetArrayText" + cnt + " = new Array(\"" +
                    lbChoose + "\"");
        for (int j=0; j < targets.size(); j++)
        {
            out.print(",\"");
            GlobalSightLocale target = (GlobalSightLocale)targets.get(j);
            out.print(target.getDisplayName() + "\"");
        }
        out.print(");\n");
        out.print("var targetArrayValue" + cnt + " = new Array(\"-1\"");
        for (int j=0; j < targets.size(); j++)
        {
            out.print(",\"");
            GlobalSightLocale target = (GlobalSightLocale)targets.get(j);
            out.print(target.getId() + "\"");
        }
        out.print(");\n");
        cnt++;
    }
%>

// make sure both source and target locales are set
function confirmLocales()
{
    if (vendorForm.srcLocales.options[0].selected)
    {
        alert("<%=bundle.getString("jsmsg_users_source_locale")%>");
        return false;
    }
    if (vendorForm.targLocales.options[0].selected)
    {
        alert("<%=bundle.getString("jsmsg_users_target_locale")%>");
        return false;
    }
}

// Update the target locale drop down
function getTargets(selected)
{
    if (selected == 0) return;
    text = eval("targetArrayText" + selected);
    value = eval("targetArrayValue" + selected);
    // remove all current options
    for (i=0; i < vendorForm.targLocales.length; i++)
    {
        vendorForm.targLocales.options[i] = null;
    }
    // create new options
    for (i=0; i < (eval("targetArrayText" + selected)).length; i++)
    {
        vendorForm.targLocales.options[i] = new Option(text[i], value[i]);
        <% if (targetLocale != null) { %>
            if (value[i] == <%=targetLocale.getId()%>)
            {
                vendorForm.targLocales.options[i].selected = true;
            }
        <% } %>
    }
}

// Get the rates for the selected locale pair
function getRates(selected)
{
    if (selected == 0) return;
    vendorForm.action = "<%=ratesURL%>";
    vendorForm.submit();
}

</script>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>

    <tr>
      <td class="standardText">
        <%=lbSourceLocale%>:
      </td>
      <td class="standardText">
        <%=lbTargetLocale%>:
      </td>
    </tr>
    <tr>
      <td class="standardText">
        <select name="srcLocales" onchange="getTargets(selectedIndex)">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            Enumeration keys = locales.keys();
            int i = 1;
            while (keys.hasMoreElements())
            {
                GlobalSightLocale source = (GlobalSightLocale)keys.nextElement();
                out.println("<option value=\"" + source.getId() + "\"");
                if (sourceLocale != null) {
                    if (source.getId() == sourceLocale.getId())
                    {
                        out.print(" selected ");
                        srcSelected = i;
                    }
                }
                out.println(">" + source.getDisplayName() + "</option>");
                i++;
            }
%>
        </select>
      </td>
      <td class="standardText">
        <select name="targLocales" onchange="getRates(selectedIndex)">
            <option>&lt;--<%=bundle.getString("lb_sel_source_first")%></option>
        </select>
      </td>
    </tr>

<script>
getTargets(<%=srcSelected%>);
</script>
