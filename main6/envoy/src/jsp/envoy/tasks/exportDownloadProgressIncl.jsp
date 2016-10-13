<%@ page
	import="java.util.*,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        java.io.IOException"
	session="true"%>

<STYLE>
#idExportDownloadProgressContainerDownload {
	border: solid 1px<%=skin.getProperty("skin.list.borderColor")%>;
	z-index: 1;
	position: absolute;
	top: 62;
	left: 20;
	width: 400;
	height: 17px;
}

#idExportDownloadProgressBarDownload {
	background-color: #a6b8ce;
	z-index: 0;
	border: solid 1px<%=skin.getProperty("skin.list.borderColor")%>;
	position: absolute;
	top: 62;
	left: 20;
	width: 0;
	height: 17px;
}

#idExportDownloadProgressDownload {
	text-align: center;
	z-index: 2;
	font-weight: bold;
	height: 17px;
}

#idExportDownloadMessagesHeaderDownload {
	position: absolute;
	top: 92;
	left: 20;
	font-weight: bold;
}

#idExportDownloadMessagesDownload {
	position: absolute;
	overflow: auto;
	z-index: 0;
	top: 122;
	left: 20;
	height: 140;
	width: 400;
}

#idExportDownloadLinksDownload {
	position: absolute;
	left: 200;
	top: 325;
	z-index: 1;
}

#idExportDownloadHeadingDownload {
	position: absolute;
	left: 20;
	top: 22;
	z-index: 1;
}

#idExportDownloadPleaseWaitDownload {
	position: absolute;
	left: 20;
	top: 40;
	z-index: 1;
}
</STYLE>
<SCRIPT type="text/javascript">
<!--
var M_WIDTH = 400;

function showExportDownloadMessage(message)
{
    var div = document.createElement("DIV");
    div.innerHTML = message;
    idExportDownloadMessagesDownload.appendChild(div);

    if (idExportDownloadMessagesDownload.style.pixelHeight < 80)
    {
      idExportDownloadMessagesDownload.style.pixelHeight = 80;
    }

    idExportDownloadMessagesDownload.style.pixelHeight += 40;

    if (idExportDownloadMessagesDownload.style.pixelHeight > 200)
    {
      idExportDownloadMessagesDownload.style.pixelHeight = 200;
    }

    div.scrollIntoView(false);
}

function showExportDownloadProgress(entryCount, percentage, message)
{
  if (percentage > 100)
    percentage = 100;
  document.getElementById("idExportDownloadProgressDownload").innerHTML = percentage.toString(10) + "%";

  document.getElementById("idExportDownloadProgressBarDownload").style.width = 
	  Math.round((percentage / 100) * M_WIDTH);
  if(window.navigator.userAgent.indexOf("Firefox")>0 || window.navigator.userAgent.indexOf("Chrome")>0) {
	  document.getElementById("idExportDownloadProgressBarDownload").style.height = '17px';
  }

  if (message != null && message != "")
  {
    showExportDownloadMessage(message);
  }
}

function doExportDownloadProgressCancel()
{
	document.getElementById("idExportDownloadProgressDivDownload").style.display = "none";
	if (o_intervalRefresh)
		window.clearInterval(o_intervalRefresh);
	idExportDownloadMessagesDownload.innerHTML = "";
}
//-->
</SCRIPT>

<DIV ID="contentLayerDownload">
	<p>
		<BR>
	</p>
	<SPAN CLASS="mainHeading" id="idExportDownloadHeadingDownload">Export & Download</SPAN>

	<DIV id="idExportDownloadProgressContainerDownload">
		<DIV id="idExportDownloadProgressDownload">0%</DIV>
	</DIV>
	<DIV id="idExportDownloadProgressBarDownload"></DIV>

	<BR>
	<DIV id="idExportDownloadMessagesHeaderDownload" class="header"></DIV>
	<DIV id="idExportDownloadMessagesDownload"></DIV>

	<DIV id="idExportDownloadLinksDownload" style="width: 500px">
		<INPUT TYPE="BUTTON" VALUE="Close" id="idExportDownloadCancelOkDownload"
			onclick="doExportDownloadProgressCancel()" />
	</DIV>

</DIV>
