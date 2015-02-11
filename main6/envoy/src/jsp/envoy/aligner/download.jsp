<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.everest.aligner.AlignmentStatus,
        com.globalsight.util.edit.EditUtil,
        java.util.ArrayList,
        java.util.List,
        java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="showerrors" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

List gapPackages =
  (List)sessionMgr.getAttribute(WebAppConstants.GAP_PACKAGES);

String urlShowErrors = showerrors.getPageURL();
String urlRemove = remove.getPageURL();
String urlOk = ok.getPageURL();

String lb_title = bundle.getString("lb_aligner_package_download");
String lb_helptext = bundle.getString("helper_text_aligner_package_download");
String lb_name = bundle.getString("lb_name");
String lb_description = bundle.getString("lb_description");
String lb_status = bundle.getString("lb_status");

String error = (String)sessionMgr.getAttribute(WebAppConstants.GAP_ERROR);
sessionMgr.removeElement(WebAppConstants.GAP_ERROR);
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
TD {
    font: 10pt arial;
}

FORM { display: inline; }

.clickable { cursor: hand; }

#idPackages {
    background-color: #ffffff;
    table-layout: fixed;
    behavior: url(/globalsight/includes/sort.htc)
              url(/globalsight/includes/rowover.htc);
}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "aligner";
var helpFile = "<%=bundle.getString("help_align_downloadPackage")%>";

var name = "";

function findSelectedPackageName()
{
   var name;

   if (packageForm.packageId)
   {
       // If more than one radio button is displayed, the length
       // attribute of the radio button array will be non-zero, so
       // find which one is checked
       if (packageForm.packageId.length)
       {
           for (i = 0; i < packageForm.packageId.length; i++)
           {
               if (packageForm.packageId[i].checked == true)
               {
                   name = packageForm.packageId[i].getAttribute("pname");
                   break;
               }
           }
       }
       else
       {
           // If only one is displayed, there is no radio button
           // array, so just check if the single radio button is
           // checked
           if (packageForm.packageId.checked == true)
           {
               name = packageForm.packageId.getAttribute("pname");
           }
       }
   }

   return name;
}

function findSelectedPackage()
{
   var result;
   if (packageForm.packageId)
   {
       // If more than one radio button is displayed, the length
       // attribute of the radio button array will be non-zero, so
       // find which one is checked
       if (packageForm.packageId.length)
       {
           for (i = 0; i < packageForm.packageId.length; i++)
           {
               if (packageForm.packageId[i].checked == true)
               {
                   result = packageForm.packageId[i].value;
                   break;
               }
           }
       }
       else
       {
           // If only one is displayed, there is no radio button
           // array, so just check if the single radio button is
           // checked
           if (packageForm.packageId.checked == true)
           {
               result = packageForm.packageId.value;
           }
       }
   }

   return result;
}


function doOk()
{
  window.location.href = '<%=urlOk%>';
}

function downloadPackage()
{
  var filename = findSelectedPackage();
  if (!filename)
  {
    alert("<%=bundle.getString("jsmsg_aligner_package_download_select") %>");
  }
  else
  {
    document.getElementById("idDownload").src =
      '/globalsight/alignerPackages?file=' + encodeURIComponent(filename) + '&zip=false';
  }
}

function removePackage()
{
  var name = findSelectedPackageName();
  if (!name)
  {
    alert("<%=bundle.getString("jsmsg_aligner_package_download_select") %>");
  }
  else
  {
    document.frmRemove.<%=WebAppConstants.GAP_PACKAGE%>.value = name;    
    document.frmRemove.submit();
  }
}

function showErrors(p_packageName)
{
<%--
    window.open('<%=urlShowErrors +
       "&" + WebAppConstants.GAP_ACTION +
       "=" + WebAppConstants.GAP_ACTION_SHOWERRORS +
       "&" + WebAppConstants.GAP_PACKAGE + "=" %>' + p_packageName, '_blank',
       'height=200,width=400,status=no,toolbar=no,' +
       'menubar=no,location=no,resizable=yes',
       true);
--%>
    window.showModalDialog('<%=urlShowErrors +
      "&" + WebAppConstants.GAP_ACTION +
      "=" + WebAppConstants.GAP_ACTION_SHOWERRORS +
      "&" + WebAppConstants.GAP_PACKAGE + "=" %>' + p_packageName, null,
      'dialogHeight=250px;dialogWidth=400px;status=no;resizable=yes;');
}

function selectRow()
{
  var index = event.srcRow.rowIndex;

  if (index > 0)
  {
    var radios = packageForm.packageId;

    if (radios.length)
    {
      radios[index-1].checked = true;
    }
    else
    {
      radios.checked = true;
    }
  }
}

function doLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();

  idPackages.Format();

  // Sort list alphabetically by name.
  idNameColumn.click();
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538><%=lb_helptext%></TD>
  </TR>
</TABLE>

<BR>

<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="2">
  <TR>
    <TD><SPAN CLASS="standardText"><B><%=bundle.getString("lb_aligner_packages") %></B></SPAN></TD>
  </TR>
  <TR>
    <!-- Left side: buttons -->
    <!-- Right side: package table -->
    <TD valign="top" WIDTH="400">

    <!-- Border table -->
    <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="1" WIDTH="100%">
    <TR>
    <TD BGCOLOR="#0C1476" ALIGN="CENTER">

      <FORM NAME=packageForm id=packageFormID>
      <!-- Data table -->
      <TABLE id="idPackages" BORDER="0" CELLSPACING="0" CELLPADDING="2"
        STRIPED="true" SELECTABLE="true" onrowselect="selectRow()">
        <THEAD>
          <TR>
            <TD WIDTH="25" class="tableHeadingBasic">&nbsp;</TD>
            <TD WIDTH="325" class="tableHeadingBasic"
	      id="idNameColumn"><%=lb_name%></TD>
<%-- we don't allow descriptions yet, and we dont' generate them
            <TD WIDTH="500" class="tableHeadingBasic"><%=lb_description%></TD>
--%>
            <TD WIDTH="70" class="tableHeadingBasic"><%=lb_status%></TD>
          </TR>
        </THEAD>
        <TBODY id="idTBody">
          <%
          for (int i = 0, max = gapPackages.size(); i < max; i++)
          {
          AlignmentStatus s = (AlignmentStatus)gapPackages.get(i);

          out.println("<tr><td>");
          out.print("<input type=radio name=packageId value='");
          out.print(s.getPackageUrl());
          out.print("' pname='");
          out.print(s.getPackageName());
          out.print("'>");
          out.println("</td><td>");
          out.print(s.getPackageName());
          //out.println("</td><td>");
          //out.print(s.getPackageDescription());
          out.println("</td><td>");
          if (s.isError())
            {
            out.print("<span style='background-color: red' ");
            out.print("class=clickable onclick=\"showErrors('");
            out.print(EditUtil.toJavascript(s.getPackageName()));
            out.print("')\">");
            out.print("&nbsp;" + bundle.getString("lb_error") + "&nbsp;</span>");
            }
          else if (s.isWarning())
            {
            out.print("<span style='background-color: yellow' ");
            out.print("class=clickable onclick=\"showErrors('");
            out.print(EditUtil.toJavascript(s.getPackageName()));
            out.print("')\">");
            out.print("&nbsp;" + bundle.getString("lb_warning") + "&nbsp;</span>");
            }
          else
            {
            out.print("<span style='background-color: green' ");
            out.print(">&nbsp;" + bundle.getString("lb_ok") + "&nbsp;</span>");
            }
          out.println("</td></tr>");
          }
          %>
        </TBODY>
      </TABLE>
      <!-- End Data table -->
      </FORM>
    </TD>
    </TR>
    </TABLE>
    <!-- End Border table -->

    <BR>

    <DIV ALIGN="LEFT">
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_download") %>"
     onclick="downloadPackage()">
    &nbsp;
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove") %>"
     onclick="removePackage()">
    &nbsp;
    <INPUT CLASS="standardText" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_done") %>"
     ID="idOk" onclick="doOk()">
    </DIV>
    </TD>
  </TR>
</TABLE>

<FORM name="frmRemove" id="frmRemoveID" method="post" action="<%=urlRemove%>">
<INPUT type="hidden" name="<%=WebAppConstants.GAP_ACTION%>"
 value="<%=WebAppConstants.GAP_ACTION_REMOVEPACKAGE%>">
<INPUT type="hidden" name="<%=WebAppConstants.GAP_PACKAGE%>" 
		id="<%=WebAppConstants.GAP_PACKAGE%>" value="">
</FORM>

<IFRAME id="idDownload" NAME='download' SRC='about:blank' WIDTH='0' HEIGHT='0'></IFRAME>

</DIV>
</BODY>
</HTML>
