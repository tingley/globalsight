<%@ page
	import="java.util.*,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        java.io.IOException"
	session="true"%>

<%
    ResourceBundle m_bundle = PageHandler.getBundle(session);
    SessionManager m_sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);

    String lb_download_zip_file = m_bundle
            .getString("lb_download_zip_file");
    String lb_close = m_bundle.getString("lb_close");
    String lb_refresh = m_bundle.getString("lb_refresh");
    String lb_done = m_bundle.getString("lb_done");
    String lb_back = m_bundle.getString("lb_download_again");
    String lb_messages = m_bundle.getString("lb_messages");
    String lb_downloadFiles = m_bundle
            .getString("lb_preparing_download_file");
    String lb_please_wait = m_bundle.getString("lb_please_wait");
%>

<STYLE>
#idProgressContainerDownload {
	border: solid 1px<%=skin.getProperty("skin.list.borderColor")%>;
	z-index: 1;
	position: absolute;
	top: 62;
	left: 20;
	width: 400;
	height: 17px;
}

#idProgressBarDownload {
	background-color: #a6b8ce;
	z-index: 0;
	border: solid 1px<%=skin.getProperty("skin.list.borderColor")%>;
	position: absolute;
	top: 62;
	left: 20;
	width: 0;
	height: 17px;
}

#idProgressDownload {
	text-align: center;
	z-index: 2;
	font-weight: bold;
	height: 17px;
}

#idMessagesHeaderDownload {
	position: absolute;
	top: 92;
	left: 20;
	font-weight: bold;
}

#idMessagesDownload {
	position: absolute;
	overflow: auto;
	z-index: 0;
	top: 122;
	left: 20;
	height: 140;
	width: 400;
}

#idLinksDownload {
	position: absolute;
	left: 150;
	top: 325;
	z-index: 1;
}

#idHeadingDownload {
	position: absolute;
	left: 20;
	top: 22;
	z-index: 1;
}

#idPleaseWaitDownload {
	position: absolute;
	left: 20;
	top: 40;
	z-index: 1;
}
</STYLE>
<SCRIPT type="text/javascript">
<!--
var M_WIDTH = 400;
var o_intervalRefresh;

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerHTML = message;
    idMessagesDownload.appendChild(div);

    if (idMessagesDownload.style.pixelHeight < 80)
    {
      idMessagesDownload.style.pixelHeight = 80;
    }

    idMessagesDownload.style.pixelHeight += 40;

    if (idMessagesDownload.style.pixelHeight > 200)
    {
      idMessagesDownload.style.pixelHeight = 200;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
  if (percentage > 100)
    percentage = 100;
  document.getElementById("idProgressDownload").innerHTML = percentage.toString(10) + "%";

  document.getElementById("idProgressBarDownload").style.width = 
	  Math.round((percentage / 100) * M_WIDTH);
  if(window.navigator.userAgent.indexOf("Firefox")>0 || window.navigator.userAgent.indexOf("Chrome")>0) {
	  document.getElementById("idProgressBarDownload").style.height = '17px';
  }

  if (message != null && message != "")
  {
    showMessage(message);
  }
}

function doProgressCancel()
{
	document.getElementById("idProgressDivDownload").style.display = "none";
	if (o_intervalRefresh)
		window.clearInterval(o_intervalRefresh);
	idMessagesDownload.innerHTML = "";
}

function doProgressBack()
{
	document.getElementById("idProgressDivDownload").style.display = "none";
	if (o_intervalRefresh)
		window.clearInterval(o_intervalRefresh);
	idMessagesDownload.innerHTML = "";
}

function doProgressDone()
{
	document.getElementById("idProgressDivDownload").style.display = "none";
	if (o_intervalRefresh)
		window.clearInterval(o_intervalRefresh);
	idMessagesDownload.innerHTML = "";
}

function doProgressRefresh()
{
	idFrameDownload.document.location = document.getElementById("idFrameDownload").src;
}

function done(canDownload)
{
  idPleaseWaitDownload.style.visibility = 'hidden';

  idCancelOkDownload.value = "<%=lb_close%>";
  idCancelOkDownload.onclick = doProgressCancel;

  idRefreshResultDownload.value = "<%=lb_done%>";
  idRefreshResultDownload.onclick = doProgressDone;
  
  if (o_intervalRefresh)
    window.clearInterval(o_intervalRefresh);
}


	var m_lastScrollY = 0;
	function resetLocation(p_eid) {
		try {
			var m_elem = document.getElementById(p_eid);

			if (m_elem) {
				var diffY;
				if (document.documentElement
						&& document.documentElement.scrollTop)
					diffY = document.documentElement.scrollTop;
				else if (document.body)
					diffY = document.body.scrollTop
				else {/*Netscape stuff*/
				}
				percent = .1 * (diffY - m_lastScrollY);
				if (percent > 0)
					percent = Math.ceil(percent);
				else
					percent = Math.floor(percent);
				m_elem.style.top = parseInt(m_elem.style.top) + percent + "px";
				m_lastScrollY = m_lastScrollY + percent;
			}
		} catch (e) {
		}
	}
	window.setInterval("resetLocation(\"idProgressDivDownload\")", 1);
//-->
</SCRIPT>

<DIV ID="contentLayerDownload">
	<p>
		<BR>
	</p>
	<SPAN CLASS="mainHeading" id="idHeadingDownload"><%=lb_downloadFiles%></SPAN><BR>
	<SPAN CLASS="standardTextItalic" id="idPleaseWaitDownload"><%=lb_please_wait%></SPAN>

	<DIV id="idProgressContainerDownload">
		<DIV id="idProgressDownload">0%</DIV>
	</DIV>
	<DIV id="idProgressBarDownload"></DIV>

	<BR>
	<DIV id="idMessagesHeaderDownload" class="header"><%=lb_messages%></DIV>
	<DIV id="idMessagesDownload"></DIV>

	<DIV id="idLinksDownload" style="width: 500px">
		<INPUT TYPE="BUTTON" VALUE="<%=lb_close%>" id="idCancelOkDownload"
			onclick="doProgressCancel()" /> &nbsp; <INPUT TYPE="BUTTON"
			VALUE="<%=lb_refresh%>" id="idRefreshResultDownload"
			onclick="doProgressRefresh()" />
	</DIV>

	<IFRAME id="idFrameDownload" name="idFrameDownload"
		src="envoy/edit/offline/download/getstatus.jsp" style="display: none"></IFRAME>

</DIV>
