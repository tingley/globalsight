<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.permission.Permission, 
            com.globalsight.everest.servlet.util.SessionManager,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeConstant,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.util.comparator.DefinedAttributeComparator,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.company.CompanyWrapper,java.util.*"
    session="true"
%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>

<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="dup" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rem" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
  ResourceBundle bundle = PageHandler.getBundle(session);
  String newURL = new1.getPageURL() + "&action=" + AttributeConstant.NEW;
  String editURL = edit.getPageURL() + "&action=" + AttributeConstant.EDIT;
  String remURL = rem.getPageURL() + "&action=" + AttributeConstant.REMOVE;
  String title= bundle.getString("lb_define_attribute");
  String helperText = bundle.getString("helper_text_define_attribute");
  String confirmRemove = bundle.getString("msg_remove_define_attribute");
  String invalid = (String)request.getAttribute("invalid"); 
  
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
  boolean isSuperAttribute = false;
%>
<HTML>
<!-- attributeMain.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode="attributes";
var helpFile = "<%=bundle.getString("help_attribte_main_screen")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        attributeForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(attributeForm.selectAttributeIds);

        if (button == "Edit")
        {
            attributeForm.action = "<%=editURL%>" + "&id=" + value;
        }
        else if (button == "Remove")
        {
            var referencedNames = "";
            var cannotRemoveNames = "";
            var ids = document.getElementsByName("selectAttributeIds");           
            
            for (var i = 0; i < ids.length; i++)
            {
                if (ids[i].checked)
                {
                    var info = eval("info".concat(ids[i].value));
                    if (info.removeable=="false")
                    {
                    	if (cannotRemoveNames.length > 0)
                        {
                    		cannotRemoveNames = cannotRemoveNames.concat(", ");
                        }
                    	cannotRemoveNames = cannotRemoveNames.concat(info.displayName);
                    }
                }
            }

            if (cannotRemoveNames.length > 0)
            {
            	alert('<%=bundle.getString("msg_attribue_delete_protect_attribute")%>\n'.concat(cannotRemoveNames));
            	return;
            }
            
            isOk = confirm('<%=confirmRemove%>');
            
            attributeForm.action = "<%=remURL%>";
        }
    }

    if (isOk)
    {
        attributeForm.submit();
    }
}

function enableButtons()
{
    if (attributeForm.editBtn)
        attributeForm.editBtn.disabled = false;
    if (attributeForm.dupBtn)
        attributeForm.dupBtn.disabled = false;
    if (attributeForm.remBtn)
        attributeForm.remBtn.disabled = false;    
}


function setButtonState()
{
    var selectedIndex = new Array();
    var boxes = attributeForm.selectAttributeIds;
    if (boxes != null) 
    {
        if (boxes.length) 
        {
            for (var i = 0; i < boxes.length; i++) 
            {
                var checkbox = boxes[i];
                if (checkbox.checked) 
                {
                    selectedIndex.push(i);
                }
            }
        } 
        else 
        {
            if (boxes.checked) 
            {
                selectedIndex.push(0);
            }
        }
    }
    
    if (attributeForm.editBtn)
    {
        if (selectedIndex.length != 1)
        {
            attributeForm.editBtn.disabled = true;
        }
        else
        {
            attributeForm.editBtn.disabled = false;
        }
    }
    
    if (attributeForm.remBtn)
    {
        if (selectedIndex.length > 0)
        {
            attributeForm.remBtn.disabled = false;
        }
        else
        {
            attributeForm.remBtn.disabled = true;
        }
    }
}

//for GBS-2599,by fan
function handleSelectAll() {
	if (attributeForm && attributeForm.selectAll) {
		if (attributeForm.selectAll.checked) {
			checkAllWithName('attributeForm', 'selectAttributeIds'); 
			setButtonState();
	    }
	    else {
			clearAll('attributeForm'); 
			setButtonState();
	    }
	}
}
</SCRIPT>

<style type="text/css">
@import url(/globalsight/includes/attribute.css);
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
      onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<span class=errorMsg><%
    if (invalid != null && invalid.length() > 0) out.print(invalid);
%></span>

<form name="attributeForm" method="post">
<table cellpadding=0 cellspacing=0 border=0 class="standardText">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="<%=AttributeConstant.ATTRIBUTE_DEFINE_LIST%>" key="<%=AttributeConstant.ATTRIBUTE_DEFINE_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="<%=AttributeConstant.ATTRIBUTE_DEFINE_LIST%>" id="attribute"
       key="<%=AttributeConstant.ATTRIBUTE_DEFINE_KEY%>"
       dataClass="com.globalsight.cxe.entity.customAttribute.Attribute"
       pageUrl="self"
       emptyTableMsg="msg_attribute_none" >
        <amb:column label="checkbox">
         <%isSuperAttribute = 1 == attribute.getCompanyId();
           if (!(isSuperAttribute && !isSuperAdmin)){%>
          <input type="checkbox" name="selectAttributeIds" value="<%=attribute.getId()%>" onclick="setButtonState()">
          <%} %>
          <script>
          var info<%=attribute.getId()%> = new Object();
          info<%=attribute.getId()%>.displayName="<%=attribute.getDisplayName()%>";
          info<%=attribute.getId()%>.removeable="<%=attribute.removeable()%>"
          </script>
        </amb:column>
        <amb:column label="lb_name" sortBy="<%=DefinedAttributeComparator.NAME%>">
           <%          
           if (isSuperAttribute) 
           {
               out.println("<div class=\"superAttribute\">");
               out.println(attribute.getDisplayName());
               out.println("</div>");
           }
           else
           {
               String url = editURL + "&id=" + attribute.getId();
           %>
               <amb:permission name="<%=Permission.ATTRIBUTE_EDIT%>" >
                    <A name='nameLink' class='standardHREF' href="<%=url%>">
               </amb:permission>
                    <%=attribute.getDisplayName()%>
               <amb:permission name="<%=Permission.ATTRIBUTE_EDIT%>" >
                    </A> 
               </amb:permission>   
          <%}%>  
        </amb:column>
        
        <amb:column label="lb_internal_name" sortBy="<%=DefinedAttributeComparator.INTERNAL_NAME%>">
            <%
            if (isSuperAttribute) {%>
            <div class="superAttribute">
            <%} %>
            <%=attribute.getName()%>
            <%if (isSuperAttribute) {%>
            </div>
            <%} %>
             
        </amb:column>

        <amb:column label="lb_type" sortBy="<%=DefinedAttributeComparator.TYPE%>">
            <%
            if (isSuperAttribute) {%>
            <div class="superAttribute">
            <%} %>
            <%=bundle.getString("lb_attribute_type_" + attribute.getType())%>
            <%if (isSuperAttribute) {%>
            </div>
            <%} %>
             
        </amb:column>
        
        <amb:column label="lb_description" sortBy="<%=DefinedAttributeComparator.DESC%>"
         width="400px">
            <%
            if (isSuperAttribute) {%>
                <div class="superAttribute">
            <%} 
              out.print(attribute.getDescription() == null ? "" :attribute.getDescription());
              if (isSuperAttribute) {%>
             </div>
            <%} %>
        </amb:column>
 
        <%if(isSuperAdmin){ %>
        <amb:column label="lb_company_name" sortBy="<%=DefinedAttributeComparator.ASC_COMPANY%>">
            <%
            if (isSuperAttribute) {%>
            <div class="superAttribute">
            <%} %>
            <%=CompanyWrapper.getCompanyNameById(attribute.getCompanyId())%>
            <%if (isSuperAttribute) {%>
            </div>
            <%} %>
        </amb:column>
        <%} %>
      </amb:table>
      
      <!--for gbs-2599
	  DIV ID="CheckAllLayer" style="float: left; margin-left:10px;">
        <A CLASS="standardHREF"
           HREF="javascript:checkAllWithName('attributeForm', 'selectAttributeIds'); setButtonState()"><%=bundle.getString("lb_check_all")%></A> |
        <A CLASS="standardHREF"
           HREF="javascript:clearAll('attributeForm'); setButtonState();"><%=bundle.getString("lb_clear_all")%></A>
    </DIV-->
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.ATTRIBUTE_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
      name="remBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.ATTRIBUTE_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
      name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.ATTRIBUTE_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</BODY>
</HTML>