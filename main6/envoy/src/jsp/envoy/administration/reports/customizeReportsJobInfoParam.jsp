<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator,
                  com.globalsight.everest.foundation.User,
                  com.globalsight.everest.projecthandler.Project,                  
                  com.globalsight.everest.util.comparator.JobComparator,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.jobhandler.JobSearchParameters,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.costing.Currency,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.util.List"
          session="true"
%>

<jsp:useBean id="done" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%

    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbdone = bundle.getString("lb_done");
    String lbprev = bundle.getString("lb_previous");
    String lbcollapse = bundle.getString("lb_collapse_all");
    String lbexpand = bundle.getString("lb_expand_all");

    String doneUrl = done.getPageURL() + "&action=" + WebAppConstants.ACTION_JOB_INFO;
    String cancelUrl = cancel.getPageURL() + "&action=" + WebAppConstants.ACTION_JOB_CANCEL;

    boolean edit = false;

    // Parameter xml
    String paramXml = (String)request.getAttribute(WebAppConstants.CUSTOMIZE_REPORTS_JOB_INFO_PARAM_XML);
%>


<html>
<!-- This is: envoy\administration\reports\customizeReportsJobInfoParam.jsp-->
<head>
<title><%=bundle.getString("customize_reports_paramater_configuration_web_form")%></title>

<script SRC="/globalsight/envoy/administration/reports/tree.js"></script>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<%@ include file="/envoy/common/warning.jspIncl" %>
<style type="text/css">
.root {
  padding: 0px 0px 2px 0px
}

.root div {
  padding: 0px 0px 0px 0px;
  display: none;
  margin-left: 2em;
}

a{
  outline:none;
}
</style>
<script>
var needWarning = true;
var xmlStr = '<%=paramXml.replace("\\r\\n","").replace("    ","")%>';
//
// Parse the xml file and read it into js objects and create
// a tree.
function loadTree()
{
	var xmlDocument,loaded;
	
	if(window.ActiveXObject)
    {
    	xmlDocument = new ActiveXObject('Microsoft.XMLDOM');
    	xmlDocument.async = false;

    	loaded = xmlDocument.loadXML(xmlStr);
    	if (!loaded)
    	{
        	alert("<%=bundle.getString("customize_reports_param_molformed")%>");
        	return;
    	}
    }
	else if(window.DOMParser)
    { 
	    var parser = new DOMParser();
	    xmlDocument = parser.parseFromString(xmlStr,"text/xml");
	    if (!xmlDocument)
	    {
	    	alert("<%=bundle.getString("customize_reports_param_molformed")%>");
	        return;
	    }
	}
	else
	{
	    alert("<%=bundle.getString("jsmsg_error_notParseXML")%>");
	}
	
    var objTree = new jsTree;
    objTree.createRoot("<%=bundle.getString("job_info_parameters")%>", "jobinfo", "<%=edit%>");

    readXmlFile(xmlDocument.documentElement, objTree.root);
    objLocalTree.root.writeNode(false);
    openNode(document.getElementById("root"));
}

// function to obtain node information and create tree
function readXmlFile(node, treeNode)
{
    if (node.childNodes == null) return;

    for (var i = 0; i < node.childNodes.length; i++)
    {
        var id = null;
        var text = null;
        var type = null;
        var set = false;
        child = node.childNodes[i];
        //alert(node.childNodes[i].nodeName);
        var attrs = node.childNodes[i].attributes;
        if (child.nodeName == "category")
        {
            for (var j=0; j<attrs.length; j++)
            {
                type = "cat";
                if (attrs[j].name == "id")
                    id = "cat." + attrs[j].value;
                else if (attrs[j].name == "label")
                    text = attrs[j].value;
            } 
        }
        else if (child.nodeName == "param")
        {
            for (var j = 0; j < attrs.length; j++)
            {
                type = "param";
                if (attrs[j].name == "id")
                    id = "param." + attrs[j].value;
                else if (attrs[j].name == "label")
                    text = attrs[j].value;
                else if (attrs[j].name == "set")
                    set = attrs[j].value;
            }
        }

        newChild = treeNode.addChild(text, id, type, set);
        readXmlFile(node.childNodes[i], newChild);
    }
}

function submitForm(formAction)
{
    if (formAction == "done")
    {
        paramForm.action = "<%=doneUrl%>";
    }  

    paramForm.submit();
}


</script>

</head>
<body id="idBody" leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">

<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("lb_customize_reports")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_select_values_done")%></SPAN>
</TD></TR></TABLE>

<form name="paramForm" method="post" action="">

<input type="button" name="expand" value="<%=lbexpand%>"
 onclick="objLocalTree.openAllNodes()">
<input type="button" name="collapse" value="<%=lbcollapse%>"
 onclick="objLocalTree.closeAllNodes()">

<p>
<script>loadTree();</script>
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr>
    <td class="standardText"><%=bundle.getString("lb_currency")%>:</td>
		<td><SELECT NAME="currency">
			<%
	        Collection<?> currencies = ServerProxy.getCostingEngine().getCurrencies();
	        Currency pivotCurrency = ServerProxy.getCostingEngine().getPivotCurrency();

	        ArrayList<String> labeledCurrencies = new ArrayList<String>();
	        ArrayList<String> valueCurrencies = new ArrayList<String>();
	        Iterator iter = currencies.iterator();

	        while ( iter.hasNext() ) 
	        {
	            Currency c = (Currency) iter.next();
	            if (!labeledCurrencies.contains(c.getDisplayName())) {
	            	labeledCurrencies.add(c.getDisplayName(uiLocale));
	            	valueCurrencies.add(c.getDisplayName());
	            }
	        }
			
	        for (int i = 0; i < labeledCurrencies.size(); i++)
	        {
	            String currencyLabel = labeledCurrencies.get(i);
	            String currencyText = valueCurrencies.get(i);
            %>
			<OPTION VALUE="<%=currencyText%>"><%=currencyLabel%> <%  }  %>
		</SELECT>
	</td>
  </tr>
  <tr><td></td></tr>
  <tr><td></td></tr>
  <tr>
    <td>
      <input type="button" name="cancel" value="<%=lbcancel%>" onclick="window.close()">
      <input type="button" name="done" value="<%=lbdone%>" onclick="submitForm('done')">
    </td>
  </tr>
</table>
</form>

<BODY>
</HTML>


