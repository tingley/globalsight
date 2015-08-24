<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
        java.util.*"
    session="true"
%>

<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_title = bundle.getString("lb_search_window");
String lb_help = bundle.getString("lb_help");
String lb_close = bundle.getString("lb_close");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
    WebAppConstants.SESSION_MANAGER);

ArrayList userList = (ArrayList)sessionMgr.getAttribute("userNameList");
ArrayList sidList = (ArrayList)sessionMgr.getAttribute("sidList");

String from = "";

if(sessionMgr.getAttribute("from") != null) {
    from = (String) sessionMgr.getAttribute("from");
}

String userArray = "var userArray = [";

if(userList != null && userList.size() > 0) {
    for(int i = 0; i < userList.size(); i++) {
        if(i != (userList.size() -1)) {
            userArray = userArray + "'" + (String)userList.get(i) + "',";
        }
        else {
            userArray = userArray + "'" + (String)userList.get(i) + "'";
        }
    }
}

userArray = userArray + "]";

String userNameArray = "var userNameArray = [";

if(userList != null && userList.size() > 0) {
    for(int i = 0; i < userList.size(); i++) {
        if(i != (userList.size() -1)) {
        	userNameArray = userNameArray + "'" + UserUtil.getUserNameById((String) userList.get(i)) + "',";
        }
        else {
        	userNameArray = userNameArray + "'" + UserUtil.getUserNameById((String) userList.get(i)) + "'";
        }
    }
}

userNameArray = userNameArray + "]";

String sidArray = "var sidArray = [";

if(sidList != null && sidList.size() > 0) {
    for(int i = 0; i < sidList.size(); i++) {
        if(i != (sidList.size() -1)) {
            sidArray = sidArray + "'" + (String)sidList.get(i) + "',";
        }
        else {
            sidArray = sidArray + "'" + (String)sidList.get(i) + "'";
        }
    }
}

sidArray = sidArray + "]";

%>
<TITLE><%=lb_title%></TITLE>
<META http-equiv="expires" CONTENT="0">
<STYLE TYPE="text/css">
@import url("/globalsight/includes/coolbutton2.css");
 BODY   { margin: 10px; background-color: white;
          font-family: Verdana; font-size: 10pt; 
        }
 BUTTON { width: 200px; font-family: Arial; font-size: 10pt; }
.title  { text-align: left; font-weight: bold; }
.text   { text-align: left; font-size: 10pt; }
.help   { cursor: hand; color: blue; }
.mainHeading {
    font-family:Arial, Helvetica, sans-serif;
    color:#0C1476;
    font-size:11pt;
    font-weight:bold;
}
</STYLE>

<SCRIPT>
<%=userArray%>;
<%=sidArray%>;
<%=userNameArray%>;

function selectType() {
    var type = document.all("searchType")[0].checked;
    var type1 = document.all("searchType")[1].checked;
    if(type) {
        document.getElementById("userTr").style.display = "block";
        document.getElementById("sidTr").style.display = "none";
    }
    else if(type1) {
        document.getElementById("userTr").style.display = "none";
        document.getElementById("sidTr").style.display = "block";
    }
}
    
function filtrateUser() {
    var searchText = document.getElementById("userFilter").value + "" ;
    var div = document.getElementById("userTr");
    var table = document.createElement("table");
    table.id = "userTable";
    table.border = 0;
    table.style.backgroundColor = "CFCECC";
    table.width = "80%";
    var colNum = 3;
    var row = table.insertRow(-1);
    var resultNum = 0;
    
    for(var i = 0; i < userArray.length; i++) {
        var arr = userArray[i];
        
        if(arr.indexOf(searchText) > -1 || searchText == "") {
            
            if((resultNum) % colNum == 0) {
                row = table.insertRow(-1);
            }
      
            var cell = row.insertCell(-1);
            cell.width = "10";
            cell.style.textAlign = "left";
            cell.style.fontSize = "10pt";
            cell.innerHTML='<INPUT TYPE="radio" name="userIdRadio"  ID="userIdRadio" value="' + arr + '">' + userNameArray[i];
            
            resultNum++;
       }
   } 
   
   if(resultNum == 0) {
       alert("<%=bundle.getString("msg_no_filterate_result")%>");
   }
   else {
       div.removeChild(document.getElementById("userTable"));
       div.appendChild(table); 
   } 
}

function filtrateSid() {
    var searchText = document.getElementById("sidFilter").value + "";
    var div = document.getElementById("sidTr");
    var table = document.createElement("table");
    table.id = "sidTable";
    table.border = 0;
    table.style.backgroundColor = "CFCECC";
    table.width = "80%";
    var colNum = 3;
    var row = table.insertRow(-1);
    var resultNum = 0;
    
    for(var i = 0; i < sidArray.length; i++) {
        var arr = sidArray[i];
        
        if(arr.indexOf(searchText) > -1 || searchText == "") {
            if(resultNum % colNum == 0) {
                row = table.insertRow(-1);
            }
      
            var cell = row.insertCell(-1);
            cell.width = "10";
            cell.style.textAlign = "left";
            cell.style.fontSize = "10pt";
            cell.innerHTML='<INPUT TYPE="radio" name="sidIdRadio"  ID="sidIdRadio" value="' + arr + '">' + arr;
            resultNum++;
       }
   } 
   
   if(resultNum == 0) {
       alert("<%=bundle.getString("msg_no_filterate_result")%>");
   }
   else {
       div.removeChild(document.getElementById("sidTable"));
       div.appendChild(table); 
   }
}

function Search() {
    var type = document.all("searchType")[0].checked;
    var type1 = document.all("searchType")[1].checked;
    
    if(type) {
        var elements = document.all("userIdRadio");
        var value;
        //if only one radio, the elements will not be a array
        if(elements) {
            if(elements.length) {
                for(i = 0; i < elements.length; i++)
                {
                    if(elements[i].checked) {
                        value = elements[i].value;
                        break;
                    }
                }
            }
            else {
                if(elements.checked) {
                    value = elements.value;
                }
            }
        }
        
        if(value == null || value=="") {
            alert("<%=bundle.getString("msg_user_selected_warning")%>");
            return;
        }
        
      <%if(from.equals("online")) {%>
        var urlTarget = window.opener.parent.content.target.content.location + "";

        if(urlTarget.indexOf("searchByUser=") > -1) {
            urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchByUser=")) + "&searchByUser=" + value;
        }
        else {
            urlTarget = urlTarget + "&searchByUser=" + value;
        }
        
        window.opener.parent.content.target.content.location = urlTarget;
    <% } else if(from.equals("online2")) {%>
        var urlTarget = window.opener.location + "";

        if(urlTarget.indexOf("searchByUser=") > -1) {
            urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchByUser=")) + "&searchByUser=" + value;
        }
        else {
            urlTarget = urlTarget + "&searchByUser=" + value;
        }
        
        window.opener.location = urlTarget;
        
    <%} else if(from.equals("online3")) {%>
	    var urlTarget = window.opener.location + "";
	
	    if(urlTarget.indexOf("searchByUser=") > -1) {
	        urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchByUser=")) + "&searchByUser=" + value;
	    }
	    else {
	        urlTarget = urlTarget + "&searchByUser=" + value;
	    }
	    
	    window.opener.location = urlTarget; 
	<%}%>
    }
    else if(type1) {
        var elements = document.all("sidIdRadio");
        var value;
        
        if(elements) {
            if(elements.length) {
                for(i = 0; i < elements.length; i++)
                {
                    if(elements[i].checked) {
                        value = elements[i].value;
                        break;
                    }
                } 
            }
            else {
                if(elements.checked) {
                    value = elements.value;
                }
            }
        }
        
        if(value == null || value=="") {
            alert("<%=bundle.getString("msg_sid_selected_warning")%>");
            return;
        }
        
        <%if(from.equals("online")) {%>
            var urlTarget = window.opener.parent.content.target.content.location + "";

            if(urlTarget.indexOf("searchBySid=") > -1) {
                urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchBySid=")) + "&searchBySid=" + value;
            }
            else {
                urlTarget = urlTarget + "&searchBySid=" + value;
            }

            window.opener.parent.content.target.content.location = urlTarget;
        
        <% } else if(from.equals("online2")) {%>

            var urlTarget = window.opener.location + "";
            
            if(urlTarget.indexOf("searchBySid=") > -1) {
                urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchBySid=")) + "&searchBySid=" + value;
            }
            else {
                urlTarget = urlTarget + "&searchBySid=" + value;
            }

            window.opener.location = urlTarget;
        <% } else if(from.equals("online3")) {%>

            var urlTarget = window.opener.location + "";
            
            if(urlTarget.indexOf("searchBySid=") > -1) {
                urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchBySid=")) + "&searchBySid=" + value;
            }
            else {
                urlTarget = urlTarget + "&searchBySid=" + value;
            }

            window.opener.location = urlTarget;
        <%}%> 
    }
}

function Reset() {
    <%if(from.equals("online")) {%>
        var urlTarget = window.opener.parent.content.target.content.location + "";
        
        if(urlTarget.indexOf("searchByUser=") > -1) {
            urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchByUser="));
        }
        
        if(urlTarget.indexOf("searchBySid=") > -1) {
            urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchBySid="));
        }
        urlTarget += "&setToNormal=true";
        window.opener.parent.content.target.content.location = urlTarget;
      <% } else if(from.equals("online2")) {%>
        var urlTarget = window.opener.location + "";
        
        if(urlTarget.indexOf("searchByUser=") > -1) {
            urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchByUser="));
        }
        
        if(urlTarget.indexOf("searchBySid=") > -1) {
            urlTarget = urlTarget.substring(0, urlTarget.indexOf("&searchBySid="));
        }
        
        window.opener.location = urlTarget;
      <%}%> 
}

function doKeyPress()
{
  var key = event.keyCode;

  if (key == 27) // ESC 
  {
    window.close();
  }
}

function showHelp()
{
    var helpWindow = window.open("/globalsight/help/en_US/My_Activities/Search_Window.htm", 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}
</SCRIPT>
</HEAD>

<BODY onload="selectType()" onbeforeunload="Reset()" onkeypress="doKeyPress()">
  <DIV class="mainHeading" style="margin-bottom: 6px"><%=bundle.getString("lb_search_window")%></DIV>
  <table border="0" cellspacing="0" width="100%" class="text">
    <tr>
        <td class="title" width="100px"><%=bundle.getString("lb_searched_by")%>:</td>
        <td ><INPUT type=radio name="searchType" id="searchType" value="0" CHECKED onClick="selectType();">User Name&nbsp;&nbsp;
             <INPUT type=radio name="searchType" id="searchType" value="1" onClick="selectType();">SID &nbsp;&nbsp;
             <input type="button" id="submit" value="<%=bundle.getString("lb_search")%>" onClick="Search()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
             <input type="button" id="submit" value="<%=bundle.getString("lb_reset_to_normal")%>" onClick="Reset()">
             
        </td>
        <td><span style="float:right;margin-right:5px;cursor:pointer" class="help" onclick="showHelp()"><%=bundle.getString("lb_help")%></span> </td>
    </tr>
    <tr>
        <td>&nbsp; </td><td> </td><td> </td>
    </tr>
    <tr>
        
    <td colspan=3 bgColor="EBE8E8">
        <div id="userTr">
        <div><%=bundle.getString("lb_filtrate")%>: <input type="text" id="userFilter"> 
            <input type="button" value="<%=bundle.getString("lb_ok")%>" onClick="filtrateUser()"></div>
        <br>
        <table id="userTable" border="0" style="background-color:CFCECC" width="90%" class="text">
            <%
            if(userList != null) {
                for(int i = 0; i < userList.size(); i++) {
                   if(i == 0) {
                        out.println("<tr>");
                    } 
            %>
                <td width="30%">
                    <INPUT TYPE="radio" name="userIdRadio"  ID="userIdRadio" value="<%=userList.get(i)%>">
                    <%=UserUtil.getUserNameById((String) userList.get(i))%>
                </td>
            <%
                    if((i + 1) % 3 == 0) {
                        out.println("</tr>");
                        out.println("<tr>");
                    }
                    
                    if(i == (sidList.size() - 1)) {
                      out.println("</tr>");
                    }
                }
            }
            %>
        </table>
        </div>
        
        <div id="sidTr">
        <div><%=bundle.getString("lb_filtrate")%>: <input type="text" id="sidFilter">  
            <input type="button" value="<%=bundle.getString("lb_ok")%>" onClick="filtrateSid()"></div>
        <br>
        <table id="sidTable" border="0" style="background-color:CFCECC" width="90%" class="text">
            <%
            if(sidList != null) {
                for(int i = 0; i < sidList.size(); i++) {
                    if(i == 0) {
                        out.println("<tr>");
                    } 
            %>
                <td width="30%">
                    <INPUT TYPE="radio" name="sidIdRadio"  ID="sidIdRadio" value="<%=sidList.get(i)%>">
                    <%=sidList.get(i)%>
                </td>
            <%
                    if((i + 1) % 3 == 0) {
                        out.println("</tr>");
                        out.println("<tr>");
                    }
                    
                    if(i == (sidList.size() - 1)) {
                      out.println("</tr>");
                    }
                }
            }
            %>
        </table>
        </div>
    </td>
    </tr>
    
    <tr>
        <td> </td><td>&nbsp;</td>
    </tr>
    <tr>
        <td> </td>
        <td>
            
        </td>
   <tr>
  </table>
</body>
