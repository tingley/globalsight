<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.everest.edit.online.PageInfo,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.page.ExtractedFile,
            com.globalsight.everest.page.TargetPage,
            com.globalsight.everest.page.pageexport.ExportConstants,
            com.globalsight.everest.persistence.PersistentObject,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.workflowmanager.Workflow,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.ling.docproc.IFormatNames,
            com.globalsight.everest.page.SourcePage,
            com.globalsight.util.AmbFileStoragePathUtils,
            com.globalsight.cxe.adapter.adobe.AdobeHelper,
            java.io.File,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="skin" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
EditorState.Layout layout = state.getLayout();

PageInfo pageInfo = state.getPageInfo();
long pageId = state.getTargetPageId().longValue();

String pageFormat = state.getPageFormat();
boolean hasPreview = EditUtil.hasPreviewMode(pageFormat);
boolean isOOO = pageFormat.startsWith("openoffice");
boolean isOfficeXml = pageFormat.equals(IFormatNames.FORMAT_OFFICE_XML);
boolean hasDynamicPreview = false;
boolean hasPDFPreview = EditUtil.hasPDFPreviewMode(state);
Object vpdfobj = sessionMgr.getAttribute("src_view_pdf");
boolean viewPdf = vpdfobj == null ? false : true;

String msgPreviewNotInstalled = "";
if (!hasPreview)
{
   String keyPreviewNotInstalled = EditUtil.warnPreviewNotInstalled(pageFormat);
   if (keyPreviewNotInstalled.length() > 0)
   {
       msgPreviewNotInstalled = bundle.getString(keyPreviewNotInstalled);
   }
}

String msgPdfPreviewNotInstalled = "";
if (!hasPDFPreview)
{
   String keyPdfPreviewNotInstalled = EditUtil.warnPdfPreviewNotInstalled(state);
   if (keyPdfPreviewNotInstalled.length() > 0)
   {
       msgPdfPreviewNotInstalled = bundle.getString(keyPdfPreviewNotInstalled);
   }
}

String dataSource = pageInfo.getDataSourceType();
String pageName = pageInfo.getPageName();
String pageNamePath = pageName.replaceAll("\\\\","/");

// LABELS
String lb_sourceLocale = bundle.getString("lb_source_locale");
String lb_preview = bundle.getString("lb_preview");
String lb_text = bundle.getString("lb_text");
String lb_list = bundle.getString("lb_list");
String lb_dynamic_preview = bundle.getString("lb_dynamic_preview");
String lb_pdf_preview = bundle.getString("lb_pdf_preview");
String str_sourceLocale = state.getSourceLocale().getDisplayName(uiLocale);

String str_currentViewId = null;
switch (layout.getSourceViewMode())
{
case EditorConstants.VIEWMODE_PREVIEW:
    str_currentViewId = "idPreview";
    break;
case EditorConstants.VIEWMODE_TEXT:
    str_currentViewId = "idText";
    break;
case EditorConstants.VIEWMODE_DETAIL:
    str_currentViewId = "idList";
    break;
}

long sourcePageId = state.getSourcePageId();
SourcePage sp = ServerProxy.getPageManager().getSourcePage(sourcePageId);
boolean hasZipPreviewFile = state.getSourcePreviewFile() != null;

boolean isXMLPreview = false;
if (pageFormat.equals(IFormatNames.FORMAT_XML))
{
	try {
		
		long fpId = sp.getRequest().getJob().getFileProfile().getId();
	   	
		File xslFile = null;
	    StringBuffer xslPath = new StringBuffer(AmbFileStoragePathUtils.getXslDir(fpId).getPath())
	                .append("/")
	                .append(fpId)
	        		.append("/");
	    File xslParent = new File(xslPath.toString());
	    if (xslParent.exists())
	    {
	        File[] files = xslParent.listFiles();
	        if (files.length > 0)
	        {
	            String fileName = files[0].getName();
				if (fileName.toLowerCase().endsWith("xsl")
					|| fileName.toLowerCase().endsWith("xml")
					|| fileName.toLowerCase().endsWith("xslt"))
				{
					xslFile = files[0];
				}
			}
	    }
	    if (xslFile != null && xslFile.exists())
	    {
	    	isXMLPreview = true;
	    }
	} catch (Exception ex) {
		//
	}

}

%>
<HTML>
<HEAD>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
var highlightedElement = null;

function highlight(element)
{
    if (highlightedElement != null)
    {
      highlightedElement.style.color =
        "<%=skin.getProperty("skin.editor.element.highlight.color")%>";
      highlightedElement = null;
    }

    element.style.color = "<%=skin.getProperty("skin.editor.element.color")%>";
    highlightedElement = element;
}

function warnPreviewNoInstalled(msg)
{   
    alert(msg);
}

function showList()
{
    window.parent.showList();
    highlight(idList);
}

function showText()
{
    window.parent.showText();
    highlight(idText);
}

function showPreview()
{
	<%if (!isOOO && !isOfficeXml) {%>
    window.parent.showPreview();
    <%} else { %>
    try
    {
         window.parent.showProgressBar();
    }
    catch(e)
    {
    }
    window.parent.showPreviewPage2('<%=pageNamePath%>', '<%=state.getSourceLocale()%>', '<%=state.getSourcePageId()%>');
    <% } %>
    highlight(idPreview);
}

function showPDFPreview()
{
  try
  {
       window.parent.showProgressBar();
  }
  catch(e)
  {
  }
  window.parent.showPDFPreview('<%=pageNamePath%>');
  highlight(idPDFPreview);
}

function showXMLPreview()
{
  window.parent.showXMLPreview('<%=pageNamePath%>');
  highlight(idXMLPreview);
}

// dead codes ?
function exportForPreview()
{
      var url = "/globalsight/CapExportServlet?" +
      "CxeRequestType=<%=ExportConstants.PREVIEW%>" +
      "&MessageId=<%=pageId%>" +
      "&UiLocale=<%=uiLocale%>" +
      "&DataSourceType=<%=dataSource%>";
      var config = "config='height=" + 800 + ",width=" + 800 +
                   ",toolbar=no,menubar=no," +
                   "scrollbars=yes,resizable=yes,location=no,directories=no,status=yes'";
      preview_window = window.open(url,'',config);
      preview_window.screenX = 0;
      preview_window.screenY = 0;
}

function doOnload()
{
    highlight(document.getElementById("<%=str_currentViewId%>"));

    <%if (viewPdf) {
        sessionMgr.removeElement("src_view_pdf");
        if (isOfficeXml) {%>
    setTimeout(showPreview, "3000");
    <% } else { %>
    setTimeout(showPDFPreview, "3000");
    <% } } %>
}
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY BGCOLOR="<%=skin.getProperty("skin.editor.bgColor")%>" onload="doOnload()">
<DIV ID="main" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; LEFT: 2px; RIGHT: 2px; TOP: 5px;">
<FORM>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
  <TR CLASS="tableHeadingBasic">
    <TD><%=lb_sourceLocale%>: <%=str_sourceLocale%></TD>
    <TD ALIGN="RIGHT">
<%if(hasPDFPreview && !isOfficeXml){ %>
 <A id="idPDFPreview" CLASS="HREFBoldWhite" HREF="javascript:showPDFPreview()"
      onfocus="this.blur();"><%=lb_pdf_preview%></A> |
<% } else if (msgPdfPreviewNotInstalled.length() > 0) {%>
     <A CLASS="HREFBoldWhite" HREF="javascript:warnPreviewNoInstalled('<%=msgPdfPreviewNotInstalled%>')"
      onfocus="this.blur();"><%=lb_pdf_preview%></A> |
 <% } %>
<% if (hasPreview) { %>
      <A id="idPreview" CLASS="HREFBoldWhite" HREF="javascript:showPreview()"
      onfocus="this.blur();"><%=lb_preview%></A> |
<% } else if (msgPreviewNotInstalled.length() > 0) {%>
     <A CLASS="HREFBoldWhite" HREF="javascript:warnPreviewNoInstalled('<%=msgPreviewNotInstalled%>')"
      onfocus="this.blur();"><%=lb_preview%></A> |
<% } %>
<% if (hasDynamicPreview) { %>
      <A id="idDynPreview" CLASS="HREFBoldWhite" HREF="javascript:exportForPreview();"
      onfocus="this.blur();"><%=lb_dynamic_preview%></A> |
<% } %>
<% if (isXMLPreview) { %>
      <A id="idXMLPreview" CLASS="HREFBoldWhite" HREF="javascript:showXMLPreview()"
      onfocus="this.blur();"><%=lb_preview%></A> |
<% } %>
<% if (hasZipPreviewFile) { %>
      <A id="idPreview" CLASS="HREFBoldWhite" HREF="javascript:showPreview()"
      onfocus="this.blur();"><%=lb_preview%></A> |
<% } %>

      <A id="idText" CLASS="HREFBoldWhite" HREF="javascript:showText();"
      onfocus="this.blur();"><%=lb_text%></A> |
      <A id="idList" CLASS="HREFBoldWhite" HREF="javascript:showList();"
      onfocus="this.blur();"><%=lb_list%></A>
    </TD>
  </TR>
</TABLE>
</FORM>

</DIV>
</BODY>
</HTML>
