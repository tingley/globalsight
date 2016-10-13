<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.securitymgr.VendorSecureFields,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>


<% 
    ResourceBundle bundle = PageHandler.getBundle(session);        
   
    String pagetitle= bundle.getString("lb_view_vendor") + " - " +
                    bundle.getString("lb_cv_resume");
    
    //Labels
    String lbCV = bundle.getString("lb_cv_resume");


    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Field level security
    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.ROLES);

    // Get the data
    Vendor vendor = (Vendor) sessionMgr.getAttribute(VendorConstants.VENDOR);

    String cv = vendor.getResume();
    String cvFilename = vendor.getResumePath();
    String baseName = null;

    if ((cv == null || cv.equals("")) && (cvFilename == null || cvFilename.equals(""))) {
        cv = bundle.getString("jsmsg_no_resume");
    }
    else if (cvFilename != null)
    {
        baseName = vendor.getResumeFilename();
    }
%>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<p>
<table cellspacing="0" cellpadding="1" border="0" class="detailText" width="100%">
  <tr>
     <td bgcolor="D6CFB2">
        <table cellpadding=4 cellspacing=0 border=0
                    class=detailText bgcolor="WHITE" width="100%">
        <tr valign="TOP">
            <td style="background:D6CFB2; font-weight:bold; font-size:larger" colspan=2><%=bundle.getString("lb_cv_resume")%></td>
        </tr>

        <tr>
<%
    if (access.equals("hidden"))
    {
%>
        <td>&nbsp;
        <div class="standardTextBold">
         <span class="confidential">[<%=bundle.getString("lb_confidential")%>]
         </span> <p>
        </div>
<%
    }
    else
    {
%>
            <td class="standardText">
              <%
                if (baseName != null)
                    out.println("<a class='standardHREF' target='_blank' href=" + cvFilename + ">" + baseName + "</a>");
                else
                    out.println(cv);
              %>
<%
    } // end if access
%>
            </td>
        </tr>
    </table>
    </td>
  </tr>
</table>
