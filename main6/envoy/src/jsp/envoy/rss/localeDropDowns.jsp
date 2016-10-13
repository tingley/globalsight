<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.util.GlobalSightLocale,
                com.globalsight.util.SortUtil,
                com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                java.util.ResourceBundle" 
        session="true" 
%>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />

<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   
    
    //Labels
    String lbChoose = bundle.getString("lb_choose");
    String lbSourceLocale = bundle.getString("lb_source_locale");
    String lbTargetLocale = bundle.getString("lb_target_locales");

    // Data
    Hashtable locales = (Hashtable)sessionMgr.getAttribute("remainingLocales");
    GlobalSightLocale targetLocale = (GlobalSightLocale)
        sessionMgr.getAttribute("targetLocale");

    int srcSelected = 0;
    String srcLocale = (String)sessionMgr.getAttribute("srcLocale");

    Hashtable targHash = new Hashtable();
    String targLocales = (String)sessionMgr.getAttribute("targLocales");

%>

<script language="JavaScript">

// Create array of selected targ locales
selectedTargLocales = new Array();
<%
    if (targLocales != null)
    {
        StringTokenizer st = new StringTokenizer(targLocales, ",");
        while (st.hasMoreTokens())
        {
            out.println("selectedTargLocales[\"" + st.nextToken() + "\"] = 1;");
        }
    }

    // Set up source and target arrays
    ArrayList srcs = Collections.list(locales.keys());
    SortUtil.sort(srcs, new GlobalSightLocaleComparator(uiLocale));

    int cnt = 1;
    for (int i =0; i < srcs.size(); i++)
    {
        GlobalSightLocale source = (GlobalSightLocale)srcs.get(i);
        Vector targets = (Vector)locales.get(source);
        SortUtil.sort(targets, new GlobalSightLocaleComparator(uiLocale));
        out.print("var targetArrayText" + cnt + " = new Array(");
        for (int j=0; j < targets.size(); j++)
        {
            if (j != 0)
                out.print(",");
            GlobalSightLocale target = (GlobalSightLocale)targets.get(j);
            out.print("\"" + target.getDisplayName(Locale.US) + "\"");
        }
        out.print(");\n");
        out.print("var targetArrayValue" + cnt + " = new Array(");
        for (int j=0; j < targets.size(); j++)
        {
            if (j != 0)
                out.print(",");
            GlobalSightLocale target = (GlobalSightLocale)targets.get(j);
            out.print("\"" + target.toString() + "\"");
        }
        out.print(");\n");
        cnt++;
    }
%>

// make sure both source and target locales are set
function confirmLocales()
{
    if (customerForm.srcLocales.options[0].selected)
    {
        alert("<%=bundle.getString("jsmsg_users_source_locale")%>");
        return false;
    }
    if (customerForm.targLocales.options[0].selected)
    {
        alert("<%=bundle.getString("jsmsg_users_target_locale")%>");
        return false;
    }
}

</script>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>

    <tr>
      <td class="standardText">
        <%=lbSourceLocale%><span class="asterisk">*</span>:
      </td>
      <td class="standardText">
        <select name="srcLocales">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
<%
            cnt = 1;
            for (int i =0; i < srcs.size(); i++)
            {
                GlobalSightLocale source = (GlobalSightLocale)srcs.get(i);
                out.println("<option value=\"" + source.toString() + "\"");
                if (source.toString().toLowerCase().equals(srcLocale.toLowerCase()))
                {
                    out.print(" selected ");
                    srcSelected = cnt;
                }
                out.println(">" + source.getDisplayName(Locale.US) + "</option>");
                cnt++;
            }
%>
        </select>
      </td>
    </tr>
