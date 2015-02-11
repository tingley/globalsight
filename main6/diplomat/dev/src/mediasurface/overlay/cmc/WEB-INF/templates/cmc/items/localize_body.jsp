<%--
 * Copyright (c) 2004, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF 
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
--%>

<%@ page import="java.io.FileInputStream,
                 java.io.File, 
                 java.io.InputStream, 
                 java.net.URL"%>
<%!
   // The property file will only be loaded once.
    static Properties connectionProp = null;   
            
%>
<%
   try
   {
      if (connectionProp == null)
      {
         InputStream is = 
         getServletContext().getResourceAsStream("GlobalSightHost.properties");
         connectionProp = new Properties();
         connectionProp.load(is);
         is.close(); 
      }
   }
   catch (Exception ex)
   {
      // failed to read the property file (no GlobalSight host found).
   }
   
   Enumeration names = connectionProp.propertyNames();   
   String gsId = (String)session.getAttribute("gsUsername");
   String height = "300";
   if (gsId == null)
   {
      gsId = "";
      height = "430";
   }
%>
<%@ include file="../../include/pagesetup.inc" %>
<%@ page errorPage="../error.jsp" %>
<html>
<head>
<script language="JavaScript" type="text/javascript" src="<%=ctxPath%>/resources/javascriptmsg.jsp"></script>
<script language="JavaScript" src="<%=ctxPath%>/resources/javascript.js"></script>

<SCRIPT LANGUAGE="JavaScript">
var urlPrefix = "http://";
var urlSuffix = "/globalsight/envoy/mediasurface/gsMediaSurfaceImport.jsp";    
var globalsightUrl = "";
function buildUrl(selectedOption) {	
    if ((selectedOption.selectedIndex-1) != -1)
    {
       globalsightUrl = urlPrefix + selectedOption.value + urlSuffix;
	}
}

function checkAll(form)
{    
   form = eval("document." + form);
   for (var i = 0; i < form.elements.length; i++)
   {
	if (form.elements[i].type == "radio" &&
	    !form.elements[i].disabled)
      {
         form.elements[i].checked = true;
         checkItems(form.elements[i]);
         return;
      }
   }
}

function clearAll(form)
{    
   form = eval("document." + form);
   for (var i = 0; i < form.elements.length; i++)
   {
      if (form.elements[i].type == "radio" || 
          form.elements[i].type == "checkbox")
      {
         form.elements[i].checked = false;
         form.elements[i].disabled = false;
      }
   }
}

function checkItems(formatTypeCheckbox)
{
   // "formatTypeCheckbox" is the checkbox object
   form = document.localizeForm;
   var primary = formatTypeCheckbox.value;
   
   for (var i = 0; i < form.elements.length; i++)
   {
      // If it's a checkbox or radio button
      if (form.elements[i].type == "checkbox")
      {
         if (form.elements[i].name.indexOf(primary) != -1)
         {
            // Set the checkbox equal to the parent workflow
            // checkbox state
            form.elements[i].checked = formatTypeCheckbox.checked;
            form.elements[i].disabled = false;
         }
         else
         {
            form.elements[i].checked = false;
            if (form.elements[i].type == "checkbox")
            {
               form.elements[i].disabled = true;
            }            
         }
      }
   }
}

function localize(form)
{
   form = eval("document." + form);
   var selectionName = null;
   var selectedIds = "";   
   for (var i = 0; i < form.elements.length; i++)
   {      
      if ((form.elements[i].name == "globalsightHost" && 
           form.elements[i].selectedIndex-1) == -1)
      {
         alert('<fmt:message bundle="${applicationScope.CMCMessages}" key="selectGlobalSightHost"/>');
         return;
      }

      if (form.elements[i].type == "radio" && 
          form.elements[i].checked)
      {
         selectionName = form.elements[i].value;         
      }
      else if (selectionName != null && 
               form.elements[i].type == "checkbox" && 
               form.elements[i].checked) 
      {
         selectedIds = selectedIds + form.elements[i].value + ',';         
      }
   }

   if (selectedIds != "") 
   {      
      window.open(globalsightUrl+'?gsId='+"<%= gsId%>"+'&itemKey='+selectedIds, 'MSImport','HEIGHT='+"<%= height%>"+',WIDTH=500,scrollbars');
      return false;
   }   
   else 
   {
      alert('<fmt:message bundle="${applicationScope.CMCMessages}" key="selectMediaType"/>');
   }
}
</SCRIPT>          

<link rel="STYLESHEET" type="text/css" href="<%=Utils.GetStylesheetName(request)%>">
</head>
<body class="foregroundLight">
<FORM NAME="localizeForm" ACTION="" METHOD="post" >
<table border="0" cellspacing="0" cellpadding="0"><tr><td height="8" width="16"><img border="0" src="<%=ctxPath%>/resources/images/spacer16.gif"></td><td width="100%"/></tr><tr><td></td><td>

<%
	Integer parentItem = (Integer)request.getAttribute(IAttributeNames.ITEM_SYSTEMID);
	boolean listAltColor=true;
%>
<ms:getItem id="theItem" itemId="<%= parentItem %>"/>

<%
   HashMap map = new HashMap();
   // First add the selected item (parent item)
   List l = new ArrayList();
   l.add(theItem);
   map.put(theItem.getMediaType(), l);   
   
   // now go thru the bound items and group them by media type
   ILink[] children = theItem.getBoundItems(null, false, null, null, null);
   for (int i=0; i < children.length; i++)
   {
      IItem child = children[i].getChildItem();
      List itemList = (List)map.get(child.getMediaType());
      if (itemList == null)
      {
         itemList = new ArrayList();
         map.put(child.getMediaType(), itemList);
      }  
      itemList.add(child);    
   }
%>
<table width="100%" border="0" cellspacing="0" height="23">
<%
  IItem storedItem = (IItem) pageContext.getAttribute(Utils.CMC_EDITITEM,PageContext.SESSION_SCOPE);
  String onclickEvent = "";
  if ((storedItem != null) && (theItem != null) && (theItem.equals(storedItem)))
  {
    onclickEvent = "window.alert(\"" + Dispatcher.GetTextMessage("cannotRevertLockedJS")  + "\");return false;";
%>
    <tr>
      <td class="pageWarningText">
        <br/><fmt:message bundle="${applicationScope.CMCMessages}" key="cannotRevertLocked">
				<fmt:param><%=theItem.getFullName()%></fmt:param>
			 </fmt:message> <br/>
        <br/>
      </td>
    </tr>
<%
  }
%>
  <tr>
    <td><b><fmt:message bundle="${applicationScope.CMCMessages}" key="localizeItem"><fmt:param><%=theItem.getFullName()%></fmt:param></fmt:message></b></td>
  </tr>
</table><BR>
<%
if (connectionProp.size() > 1)
{ %> 
    <SPAN CLASS="header2"><fmt:message bundle="${applicationScope.CMCMessages}" key="globalsightHost"/>:</SPAN>
    <SELECT NAME=globalsightHost onChange="buildUrl(this);">
        <OPTION VALUE=-1><fmt:message bundle="${applicationScope.CMCMessages}" key="pleaseSelect"/></OPTION>
        <%        
        while(names.hasMoreElements())    
        {        
            String host = (String)names.nextElement();
            String ip = connectionProp.getProperty(host);
        %>
            <OPTION VALUE="<%= ip %>" > <%= host %> </OPTION>
        <%
        }
        %>
    </SELECT><BR><BR>
<%
}
else
{
    String ip = connectionProp.getProperty((String)names.nextElement());
%>
<SCRIPT LANGUAGE="JavaScript1.2">
globalsightUrl = urlPrefix + "<%=ip%>" + urlSuffix;
</script>
<%}%>


<table width="40%" border="0" cellspacing="0" cellpadding="0">
  <tr class="columnHeaderLowLight" height="23">    
    <td align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <b><fmt:message bundle="${applicationScope.CMCMessages}" key="mediaType"/></b></td>
  </tr>  
    <!-- The format type -->
    <%
      int size = map.size();
      Object[] mapKeys = map.keySet().toArray();
      for (int i = 0; i < size; i++)
      {
         String mapKey = (String)mapKeys[i];
      %>
    <tr>
    <TD NOWRAP><INPUT TYPE="radio" NAME="formatType" VALUE="<%=mapKey %>" ONCLICK="checkItems(this);">
    <%=mapKey %></td>
    </tr>
    <%
         List items = (List)map.get(mapKey);
         int listSize = items.size();
         for (int j = 0; j < listSize; j++)
         {
            IItem itm = (IItem)items.get(j);
    %>
    <!-- members of that type-->
    <tr BGCOLOR="#FFFFFF">
    <TD>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <INPUT TYPE="checkbox" NAME="<%=mapKey %>" VALUE="<%=itm.getKey().getKey() %>">
    <%=itm.getFullName() %>
    </TD>    
    </tr>
    <%
         }
      }%>
</table><BR>

<!-- Links and buttons -->
<table>
<tr>
<TD>
    <A CLASS="standardHREF" HREF="javascript: checkAll('localizeForm');"><fmt:message bundle="${applicationScope.CMCMessages}" key="checkAll"/></A> | 
    <A CLASS="standardHREF" HREF="javascript: clearAll('localizeForm');"><fmt:message bundle="${applicationScope.CMCMessages}" key="clearAll"/></A>
</TD>
<td width="170">&nbsp;</td>
<TD CLASS="header2" HEIGHT="20" ALIGN="RIGHT">
<button ONCLICK="localize('localizeForm')" name="lb">
<fmt:message bundle="${applicationScope.CMCMessages}" key="localize"/>
</button>
</TD>
</tr>
</table>
</FORM>
  </body>
</html>     
