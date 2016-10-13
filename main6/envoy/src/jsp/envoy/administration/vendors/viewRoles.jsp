<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.securitymgr.VendorSecureFields,
         com.globalsight.everest.vendormanagement.Vendor,
         com.globalsight.everest.vendormanagement.VendorRole,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
         com.globalsight.everest.util.comparator.VendorRoleComparator,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.costing.Rate,
         java.text.MessageFormat,
         java.util.Set,
         java.util.Iterator,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="roles" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String selfUrl = self.getPageURL();

    // Field level security
    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.ROLES);

    String lbRoles = bundle.getString("lb_roles");
%>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <p>
<table cellspacing="0" cellpadding="1" border="0" borderColor="red" class="detailText" width="100%">
  <tr>
     <td bgcolor="D6CFB2">
        <table cellpadding=4 cellspacing=0 border=0
                    class=detailText bgcolor="WHITE" width="100%">
        <tr valign="TOP">
            <td style="background:D6CFB2; font-weight:bold; font-size:larger" colspan=2><%=lbRoles%></td>
        </tr>
        <tr>
            <td>
<%
    if (access.equals("hidden"))
    {
%>
        </td></tr><tr><td>&nbsp;
        <div class="standardTextBold">
         <span class="confidential">[<%=bundle.getString("lb_confidential")%>]
         </span> <p>
        </div>
<%
    }
    else
    {
%>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="roles" key="<%=VendorConstants.ROLE_KEY%>"
                     pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
          <amb:table bean="roles" id="role" key="<%=VendorConstants.ROLE_KEY%>"
             dataClass="com.globalsight.everest.vendormanagement.VendorRole" pageUrl="self"
             emptyTableMsg="msg_no_roles" >
            <amb:column label="lb_activity" sortBy="<%=VendorRoleComparator.ACTIVITY%>">
                <%=role.getActivity().getName()%>
            </amb:column>
            <amb:column label="lb_source_locale" sortBy="<%=VendorRoleComparator.SRC%>">
                <%=role.getLocalePair().getSource().getDisplayName(uiLocale)%>
            </amb:column>
            <amb:column label="lb_target_locale" sortBy="<%=VendorRoleComparator.TARG%>">
                <%=role.getLocalePair().getTarget().getDisplayName(uiLocale)%>
            </amb:column>
            <amb:column label="lb_currency" sortBy="<%=VendorRoleComparator.CURRENCY%>">
                <%
                    Rate rate = role.getRate();
                    if (rate != null) out.print(rate.getCurrency().getDisplayName());
                 %>
            </amb:column>
            <amb:column label="lb_rate_type" sortBy="<%=VendorRoleComparator.RATE_TYPE%>">
                <%
                    Rate rate = role.getRate();
                    if (rate != null)
                    {
                        Integer type = rate.getRateType();
                        String typeStr = "";
                        switch (type.intValue())
                        {
                            case 1:
                                typeStr = "Fixed";
                                break;
                            case 2:
                                typeStr = "Hourly";
                                break;
                            case 3:
                                typeStr = "Page";
                                break;
                            case 4:
                                typeStr = "Word Count";
                            break;
                        }
                        out.print(typeStr);
                     } %>
            </amb:column>
            <amb:column label="lb_amount">
                <%
                   Rate rate = role.getRate();
                   if (rate != null && !rate.getRateType().equals(Rate.UnitOfWork.WORD_COUNT))
                   {
                        out.println(rate.getUnitRate());
                   }
                   else if (rate != null && rate.getRateType().equals(Rate.UnitOfWork.WORD_COUNT))
                   {
                        out.print(bundle.getString("lb_exact") + ":" + rate.getSegmentTmRate() + ", ");
                        out.print(bundle.getString("lb_fuzzy") + ":" + rate.getHiFuzzyMatchRate() + ", ");
                        out.print(bundle.getString("lb_no_match") + ":" + rate.getNoMatchRate() + ", ");
                        out.print(bundle.getString("lb_no_match_repetition") + ":" + rate.getRepetitionRate());
                   }
                %>
            </amb:column>
          </amb:table>
        </td>
      </tr>
<%
    } // end if access
%>
    </table>
  </td>
 </tr>
</table>
</td>
</tr>
</table>

