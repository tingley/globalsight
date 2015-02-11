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
            com.globalsight.everest.util.comparator.AttributeSetComparator,
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
  String title= bundle.getString("lb_attribute_groups");
  String helperText = bundle.getString("helper_text_attribute_groups");
  String confirmRemove = bundle.getString("msg_remove_define_attribute_group");
  String invalid = (String)request.getAttribute("invalid"); 
  boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
  boolean isSuperAttributeSet = false;
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
var guideNode = "attributeGroups";
var helpFile = "<%=bundle.getString("help_attribte_group_mian_screen")%>";

function submitForm(button)
{
    var isOk = true;
    if (button == "New")
    {
        attributeForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(attributeForm.selectAttributeSetIds);

        if (button == "Edit")
        {
            attributeForm.action = "<%=editURL%>" + "&id=" + value;
        }
        else if (button == "Remove")
        {
        	var referencedNames = "";
            var ids = document.getElementsByName("selectAttributeSetIds");
            
            for (var i = 0; i < ids.length; i++)
            {
                if (ids[i].checked)
                {
                    var info = document.getElementById("info".concat(ids[i].value));
                    if (info.value=="true")
                    {
                    	if (referencedNames.length > 0)
                        {
                        	referencedNames = referencedNames.concat(", ");
                        }
                        
                        referencedNames = referencedNames.concat(info.name);
                    }
                }
            }
            
            if (referencedNames.length > 0)
            {
            	isOk = confirm("<%=bundle.getString("msg_attribue_delete_used_attribute_group")%>\n".concat(referencedNames));
            }
            else
            {
                isOk = confirm('<%=confirmRemove%>');
            }
            
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
    var boxes = attributeForm.selectAttributeSetIds;
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

//for GBS-2599
function handleSelectAll() {
	if (attributeForm && attributeForm.selectAll) {
		if (attributeForm.selectAll.checked) {
			checkAllWithName('attributeForm', 'selectAttributeSetIds'); 
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
      <amb:tableNav bean="<%=AttributeConstant.ATTRIBUTE_GROUP_LIST%>" key="<%=AttributeConstant.ATTRIBUTE_GROUP_KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="<%=AttributeConstant.ATTRIBUTE_GROUP_LIST%>" id="attributeSet"
       key="<%=AttributeConstant.ATTRIBUTE_GROUP_KEY%>"
       dataClass="com.globalsight.cxe.entity.customAttribute.AttributeSet"
       pageUrl="self"
       emptyTableMsg="msg_attribute_group_none" >
        <amb:column label="checkbox">
        <%isSuperAttributeSet = 1 == attributeSet.getCompanyId();
          if (!(isSuperAttributeSet && !isSuperAdmin)){%>
          <input type="checkbox" name="selectAttributeSetIds" value="<%=attributeSet.getId()%>" 
              displayName="<%=attributeSet.getName()%>" 
              beenUsed="<%=attributeSet.getProjects().size() > 0%>" 
              onclick="setButtonState()">
          <input type="hidden" id="info<%=attributeSet.getId()%>" name="<%=attributeSet.getName()%>" value="<%=attributeSet.getProjects().size() > 0%>">
          <%}%>  
        </amb:column>
        <amb:column label="lb_name" sortBy="<%=AttributeSetComparator.NAME%>">
            <%
            if (isSuperAttributeSet) 
            {
                out.println("<div class=\"superAttribute\">");
                out.println(attributeSet.getName());
                out.println("</div>");
            }
            else
            {
	             String url = editURL + "&id=" + attributeSet.getId();
	             %>          
	             <amb:permission name="<%=Permission.ATTRIBUTE_GROUP_EDIT%>" >
	                <A name='nameLink' class='standardHREF' href="<%=url%>">
	             </amb:permission>
	            <%=attributeSet.getName()%>
	             <amb:permission name="<%=Permission.ATTRIBUTE_GROUP_EDIT%>" >
	                </A> 
	             </amb:permission>
            <%}%>  
        </amb:column>
        <amb:column label="lb_description" sortBy="<%=AttributeSetComparator.DESC%>"
         width="400px">
            <%if (isSuperAttributeSet) {
                out.print("<div class=\"superAttribute\">");
              } 
              out.print(attributeSet.getDescription() == null ? "" :attributeSet.getDescription());
              if (isSuperAttributeSet) {
                  out.print("</div>");
              }%>
        </amb:column>
        <%if(isSuperAdmin){ %>
        <amb:column label="lb_company_name" sortBy="<%=AttributeSetComparator.ASC_COMPANY%>">
              <%if (isSuperAttributeSet) {
                out.print("<div class=\"superAttribute\">");
              } 
              out.print(CompanyWrapper.getCompanyNameById(attributeSet.getCompanyId()));
              if (isSuperAttributeSet) {
                  out.print("</div>");
              }%>
        </amb:column>
        <%}%> 
      </amb:table>
      
      <!--for GBS-2599
	  DIV ID="CheckAllLayer" style="float: left; margin-left:10px;">
        <A CLASS="standardHREF"
           HREF="javascript:checkAllWithName('attributeForm', 'selectAttributeSetIds'); setButtonState()"><%=bundle.getString("lb_check_all")%></A> |
        <A CLASS="standardHREF"
           HREF="javascript:clearAll('attributeForm'); setButtonState();"><%=bundle.getString("lb_clear_all")%></A>
    </DIV-->
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.ATTRIBUTE_GROUP_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
      name="remBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.ATTRIBUTE_GROUP_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
      name="editBtn" disabled onclick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.ATTRIBUTE_GROUP_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
      onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</BODY>
</HTML>