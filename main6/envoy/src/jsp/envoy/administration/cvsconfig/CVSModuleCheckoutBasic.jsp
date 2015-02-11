<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants,
                 com.globalsight.everest.cvsconfig.*,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.GeneralException,
                 java.text.MessageFormat,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="update" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields 

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_done");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=";
        title = bundle.getString("lb_edit") + " " + 
                bundle.getString("lb_cvsmodule");
    }
    else
    {
        saveURL +=  "&action=";
        title = bundle.getString("lb_new") + " " + 
                bundle.getString("lb_cvsmodule");
    }
    
    String updateURL = 
        update.getPageURL() + "&action=update";

    // Data
    CVSModule module = (CVSModule)sessionMgr.getAttribute(CVSConfigConstants.CVS_MODULE_KEY);
    CVSServer server = module.getServer();
    String serverName = server.getSandbox();
    String moduleName = module.getName();
    session.setAttribute("moduleId", String.valueOf(module.getId()));
    
    File f = new File(module.getRealPath());
    if (!f.exists())
    	f.mkdirs();
    
    boolean isCheckedOut = CVSUtil.isCheckedOut(serverName, moduleName);
%>

<%@page import="java.io.*"%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_cvsmodule")%>";
var guideNode="cvsmodule";
var helpFile = "<%=bundle.getString("helper_text_cvsmodule")%>";
function submitForm(formAction)
{
    if (formAction == "update")
    {
        var files = tree2.getAllChecked();
        if (files == "") {
            alert("<%=bundle.getString("jsmsg_cvs_module_select_file") %>");
            return false;
        }
        cvsserverForm.files.value = files;
    	cvsserverForm.action = "<%=updateURL%>";
    	cvsserverForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
        	cvsserverForm.action = "<%=saveURL%>";
        	cvsserverForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate activity name.
//
function confirmForm()
{
    return true;
}

function doOnload()
{
    loadGuides();
}
</script>

	<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/tree/css/dhtmlXTree.css">
	<script  src="/globalsight/includes/tree/js/dhtmlXCommon.js"></script>
	<script  src="/globalsight/includes/tree/js/dhtmlXTree.js"></script>		
	<script  src="/globalsight/includes/tree/js/dhtmlXTreeExtend.js"></script>

</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=bundle.getString("lb_cvs_checkout_title")%></span>
<br>
<br>
<form name="cvsserverForm" method="post" action="">
<input type="hidden" name="files"/>
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td nowrap><b><%=bundle.getString("lb_cvs_server") %></b></td>
          <td colspan="2"><%=server.getName() %></td>
        </tr>
        <tr>
          <td nowrap><b><%=bundle.getString("lb_cvs_sandbox") %></b></td>
          <td colspan="2"><%=server.getSandbox() %></td>
        </tr>
        <tr>
        	<td nowrap><b><%=bundle.getString("lb_cvs_repository") %></b></td>
        	<td colspan="2"><%=server.getRepository() %></td>
        </tr>
        <tr>
            <td nowrap><b><%=bundle.getString("lb_cvs_repository_url") %></b></td>
            <td colspan="2"><%=server.getCVSRootEnv() %></td>
        </tr>
        <tr>
        	<td nowrap><b><%=bundle.getString("lb_cvs_module") %></b></td>
        	<td colspan="2"><%=module.getName() %></td>
        </tr>
        <tr>
            <td nowrap><b><%=bundle.getString("lb_cvs_module_storeage") %></b></td>
            <td colspan="2"><%=module.getRealPath() %></td>
        </tr>
        <tr>
        	<td nowrap><b><%=bundle.getString("lb_cvs_module_output")%></b></td>
        	<td>
	        	<textarea name="cvsout" cols="70" rows="20" style="height:400;">
			        <%
			        String workDir = "";
			        String[] cmd = null;
			        String result = "";
                    if (!isCheckedOut) {
                        String moduleNames = module.getModulename();
                        String[] names = moduleNames.split(",");
				        for (int i=0;i<names.length;i++) {
				        	out.println("");
					        workDir = module.getRealPath();
                            String btr = module.getBranch();
                            if (btr != null && "HEAD".equalsIgnoreCase(btr)) {
                            	cmd = new String[]{"cvs", "-d", server.getCVSRootEnv(), "-q", "co", "-P", names[i]};
                            } else {
                            	cmd = new String[]{"cvs", "-d", server.getCVSRootEnv(), "-q", "co", "-P", "-r", module.getBranch(), names[i]};
                            }
					        result = CVSUtil.exeCmd(cmd, workDir);
					        out.println(result);
					        
					        workDir += File.separator + names[i];
					        //if is "HEAD", add "-A" parameter to remove sticky tag
							if (btr != null && "HEAD".equalsIgnoreCase(btr)) {
						        cmd = new String[]{"cvs", "-d", server.getCVSRootEnv(), "-q", "update", "-P", "-d", "-A"};
							} else {
						        cmd = new String[]{"cvs", "-d", server.getCVSRootEnv(), "-q", "update", "-P", "-d"};
							}
					        CVSUtil.exeCmd(cmd, workDir);
				        }
			        } else {
			        	String files = request.getParameter("files");
			        	if (files != null && files.length() > 0) {
			        		String baseDocRoot = CVSUtil.getBaseDocRoot();
			        		if (CompanyWrapper.getCurrentCompanyId() == null)
			        		    baseDocRoot += (String)session.getAttribute("companyName") + File.separator;
			        	    workDir = baseDocRoot;
			        	    String[] names = files.split(",");
			        	    int nameLen = names.length - 1;
			        	    StringBuilder sb = new StringBuilder();
			        	    for (int i=0;i<nameLen; i++) {
			        	    	if (names[i].trim().equals(""))
			        	    		continue;
			        	    	sb.append("\"").append(names[i]).append("\" ");
			        	    }
			        	    sb.append("\"").append(names[nameLen]).append("\"");
			        	    cmd = new String[]{"cvs","-d", server.getCVSRootEnv(), "-q", "update", "-P", "-d", sb.toString()};
			        	    result = CVSUtil.exeCmd(cmd, workDir);
			        	    out.println("");
			        	    out.println(result);
			        	}
			        }
			        %>
	        	</textarea>
        	</td>
        	<td>
				<div id="treeboxbox_tree2" style="width:450; height:400;background-color:#f5f5f5;border :1px solid Silver;; overflow:auto;"></div>
        	</td>
        </tr>
        <tr><td>&nbsp;</td></tr>
		<tr>
		  <td colspan="3">
		    <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
		    onclick="submitForm('save')">
            <input type="button" name="updateBtn" value="<%=bundle.getString("lb_cvs_update_action") %>"
            onclick="submitForm('update')">
		  </td>
		</tr>
      </table>
    </td>
  </tr>
</table>
</form>
        	 	<script>
					tree2=new dhtmlXTreeObject("treeboxbox_tree2","100%","100%",0);
					tree2.setImagePath("/globalsight/includes/tree/imgs/");
                    tree2.enableCheckBoxes(1);
                    tree2.enableThreeStateCheckboxes(true);
                    tree2.setXMLAutoLoading("/globalsight/envoy/administration/cvsconfig/getFileList.jsp");	
					tree2.loadXML("/globalsight/envoy/administration/cvsconfig/getFileList.jsp?id=");
				</script>

</div>
</body>
</html>