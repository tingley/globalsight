<%@ page 
        errorPage="/envoy/common/error.jsp"
        contentType="text/html; charset=UTF-8"
        import="java.util.*,java.util.ResourceBundle,        
                com.globalsight.everest.webapp.pagehandler.PageHandler"
        session="true" 
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html xmlns:gs>
<head>
<title><%=bundle.getString("lb_currency")%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/projects/workflows/Currency.js"> </SCRIPT>
<SCRIPT language="Javascript">
var isModify = false;
var bLanguageExists = false;
var args;

function doClose(ok)
{
  if (ok == true)
  {
    window.returnValue = idCurrency.options[idCurrency.selectedIndex].value;
  }
  else
  {
    window.returnValue = null;
  }
  window.close();
}

function setCurrency(curr)
{
  for (i = 0; i < idCurrency.options.length; ++i)
  {
    if (idCurrency.options[i].value == curr)
    {
      idCurrency.selectedIndex = i;
      break;
    }
  }
}

function initCurrency(aCurrency)
{
  var coption;
  var cname;
  var cvalue;
  
  for (i = 0; i < aCurrency.length; ++i)
  {
    cname = aCurrency[i].name;
    cvalue = aCurrency[i].value;
    
    coption = document.createElement("OPTION");
   
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
      coption.text = cname;
      coption.value = cvalue;
      idCurrency.add(coption);
    } 
    else 
    {
      coption.text = cname.data;
      coption.value = cvalue.data;
      var clist = document.getElementById("idCurrency");
      clist.appendChild(coption);
    } 
  }
  
}

function doLoad()
{
  args = window.dialogArguments;
  initCurrency(args.currencyOptions);
  setCurrency(args.currentCurrency);
}
</script>
</head>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="doLoad()">

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 20; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading"><%=bundle.getString("lb_select_currency")%></SPAN>
<P>
<SPAN CLASS="standardText">
<%=bundle.getString("lb_currency")%>: <SELECT id="idCurrency"></SELECT>
</SPAN>

<P>

<button TABINDEX="4" onclick="doClose(false);"><%=bundle.getString("lb_cancel")%></button>
&nbsp;
<button TABINDEX="3" onclick="doClose(true);"><%=bundle.getString("lb_ok")%></button>

</DIV>
</body>
</html>
