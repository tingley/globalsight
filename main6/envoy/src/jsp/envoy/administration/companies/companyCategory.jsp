<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<html>
<head>
<title><c:out value="${title}"/></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script>
var guideNode = "companies";
var helpFile = "<c:out value='${helpFile}'/>";

function isNumeric(str)
{
	if (str.startsWith("0"))
		return false;
	return /^(-|\+)?\d+(\.\d+)?$/.test(str);
}

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        companyForm.action = "<%=cancel.getPageURL()%>" + "&action=cancel";
        companyForm.submit();
    }
    else if (formAction == "save")
    {
		var tbox = document.getElementById("to");
		if (tbox.options.length == 0)
		{
			alert("<c:out value='${alert}'/>");
			return false;
		}
		for(var i=0;i<tbox.options.length;i++)
		{
			tbox.options[i].selected=true;
		}
        
    	companyForm.action = "<%=save.getPageURL()%>" + "&action=<c:out value='${action}'/>";
        companyForm.submit();
    }
    else if (formAction == "previous")
    {
    	companyForm.action = "<%=previous.getPageURL()%>" + "&action=<c:out value='${action}'/>" + "&name=<c:out value='${tmpcompanyInfo.name}'/>";
        companyForm.submit();
    }
}

function move(f,t) {
	var fbox = document.getElementById(f);
	var tbox = document.getElementById(t);
	for(var i=0; i<fbox.options.length; i++) {
		if(fbox.options[i].selected && fbox.options[i].value != "") {
			var no = new Option();
			no.value = fbox.options[i].value;
			no.text = fbox.options[i].text;
			no.title = fbox.options[i].title;
			tbox.options[tbox.options.length] = no;
			fbox.options[i].value = "";
			fbox.options[i].text = "";
			fbox.options[i].title = "";
   		}
	}
	BumpUp(fbox);
	SortD(tbox);
}

function BumpUp(box)  {
	for(var i=0; i<box.options.length; i++) {
		if(box.options[i].value == "")  {
			for(var j=i; j<box.options.length-1; j++)  {
				box.options[j].value = box.options[j+1].value;
				box.options[j].text = box.options[j+1].text;
				box.options[j].title = box.options[j+1].title;
			}
			var ln = i;
			break;
		}
	}
	if(ln < box.options.length)  {
		box.options.length -= 1;
		BumpUp(box);
   	}
}

function SortD(box){
	var temp_opts = new Array();
	var temp = new Object();
	for(var i=0; i<box.options.length; i++){
		temp_opts[i] = box.options[i];
	}

	for(var x=0; x<temp_opts.length-1; x++){
		for(var y=(x+1); y<temp_opts.length; y++){
			if(temp_opts[x].text.toLowerCase() > temp_opts[y].text.toLowerCase()){
				temp = temp_opts[x].text;
				temp_opts[x].text = temp_opts[y].text;
	      		temp_opts[y].text = temp;
	      		
	      		temp = temp_opts[x].value;
	      		temp_opts[x].value = temp_opts[y].value;
	      		temp_opts[y].value = temp;

	      		temp = temp_opts[x].title;
	      		temp_opts[x].title = temp_opts[y].title;
	      		temp_opts[y].title = temp;
	      	}
	   	}
	}

	for(var j=0; j<box.options.length; j++){
		box.options[j].value = temp_opts[j].value;
		box.options[j].text = temp_opts[j].text;
		box.options[j].title = temp_opts[j].title;
	}
}

function isLetterAndNumber(str){
	var reg = new RegExp("^[A-Za-z0-9 _,.-]+$");
	return (reg.test(str));
}

function isChinese(str){
	return str.match(/[\u4e00-\u9fa5]/g);
}

function addTo()
{
	var txt = document.getElementById("newCategory").value;
	if(Trim(txt) != "")
	{
		txt = Trim(txt);
		if (!isLetterAndNumber(txt) && !isChinese(txt))
		{
			alert("<c:out value='${alert_illegal}' escapeXml='false'/>");
			return false;
		}
		
		var toBox = document.getElementById("to");
		var fromBox = document.getElementById("from");
		for (var i=0;i<toBox.options.length;i++)
		{
			if(toBox.options[i].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		for (var j=0;j<fromBox.options.length;j++)
		{
			if(fromBox.options[j].text.toLowerCase()==txt.toLowerCase())
			{
				alert("<c:out value='${alert_same}'/>");
				return false;
			}
		}
		var op = new Option();
		op.value = txt;
		op.text = txt;
		op.title = txt;
		toBox.options[toBox.options.length] = op;
		document.getElementById("newCategory").value = "";

		SortD(toBox);
	}
}

function Trim(str)
{
	if(str=="") return str;
	var newStr = ""+str;
	RegularExp = /^\s+|\s+$/gi;
	return newStr.replace( RegularExp,"" );
}
</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><c:out value="${title}"/></span>
<br>
<p>
<div class="standardText"><c:out value="${helpMsg}"/>:</div>
<br>
<form name="companyForm" method="post" action="">
<input type="hidden" name="companyInfo" value="<c:out value='${companyInfo}'/>">
<input type="hidden" name="categoryInfo" value="">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
      	<tr>
      		<td>
      			<span><c:out value="${labelForLeftTable}"/>
      		</td>
      		<td>&nbsp;</td>
      		<td>
      			<span><c:out value="${labelForRightTable}"/>
      		</td>
      	</tr>
        <tr>
        	<td>
        		<select id="from" name="from" multiple class="standardText" size="15" style="width:300">
        			<c:forEach var="op" items="${fromList}">
	      				<option title="${op.value}" value="${op.key}">${op.value}</option>
	    			</c:forEach>
        		</select>
        	</td>
        	<td>
        		<table>
					<tr>
		              <td>
		                <input type="button" name="addButton" value=" >> "
		                    onclick="move('from','to')"><br>
		              </td>
		            </tr>
		            <tr><td>&nbsp;</td></tr>
		            <tr>
		                <td>
		                <input type="button" name="removedButton" value=" << "
		                    onclick="move('to','from')">
						</td>
					</tr>
				</table>
        	</td>
        	<td>
        		<select id="to" name="to" multiple class="standardText" size="15" style="width:300">
        			<c:forEach var="op" items="${toList}">
	      				<option title="${op.value}" value="${op.key}">${op.value}</option>
	    			</c:forEach>
        		</select>
        	</td>
        </tr>
		</table>
		<table border="0" class="standardText" cellpadding="2">
        <tr>
        	<td>
	        	<span><c:out value="${label}"/></span> :
        	</td>
        	<td>
        		<input id="newCategory" size="40" maxlength="100">
        		<input style="display:none">
        	</td>
        	<td>
        		<input type="button" name="add" value="<c:out value='${addButton}'/>" onclick="addTo()">
        	</td>
        </tr>
        
        <tr><td colspan="3">&nbsp;</td></tr>
        <tr>
            <td colspan="3">
                <input type="button" name="cancel" value="<c:out value='${cancelButton}'/>" onclick="submitForm('cancel')">
                <input type="button" name="previous" value="<c:out value='${previousButton}'/>" onclick="submitForm('previous')">
                <input type="button" name="save" value="<c:out value='${saveButton}'/>" onclick="submitForm('save')">
            </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</form>
</div>
</body>
</html>
