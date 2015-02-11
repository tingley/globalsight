<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
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
            java.util.*"
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

boolean hasGsaTags = state.hasGsaTags();
boolean singlePage = layout.isSinglePage();

String pageFormat = state.getPageFormat();
boolean hasPreview = EditUtil.hasPreviewMode(pageFormat);
boolean isOOO = pageFormat.startsWith("openoffice") || pageFormat.equals(IFormatNames.FORMAT_OFFICE_XML);
boolean hasDynamicPreview = false;
boolean hasPDFPreview = EditUtil.hasPDFPreviewMode(state);
boolean isTeamsiteSource = false;
long pageId = state.getTargetPageId().longValue();
Object vpdfobj = sessionMgr.getAttribute("tgt_view_pdf");
boolean viewPdf = vpdfobj == null ? false : true;

String pageName = pageInfo.getPageName();
String pageNamePath = pageName.replaceAll("\\\\","/");
String dataSource = pageInfo.getDataSourceType();

if (dataSource.equals(ExportConstants.TEAMSITE))
{
    isTeamsiteSource = true;

    String formatType = pageInfo.getPageFormat();

    String externalBaseHref = pageInfo.getExternalBaseHref();
    String fileName = externalBaseHref.substring(
        externalBaseHref.lastIndexOf('/') + 1);
    int extension = fileName.lastIndexOf('.');
    String ext = fileName.substring(extension + 1);

    if (((ext.equals("dcr")) || (extension == -1)) &&
        (formatType.equals("xml")) )
    {
        // Is this a DCR? No extension to a file indicates presence of DCR.
        // Don't show static preview for a DCR.
        hasPreview = false;
        formatType = "dcr";
    }

    if (formatType.equals("html") || formatType.equals("asp") ||
        formatType.equals("dcr")  || formatType.equals("jsp") ||
        formatType.equals("word-html") || formatType.equals("excel-html") ||
        formatType.equals("powerpoint-html") || formatType.equals("pdf"))
    {
        hasDynamicPreview = true;
    }
}

if (dataSource.equals(ExportConstants.MEDIASURFACE))
{
    //all MEDIASURFACE content can be dynamically previewed
    hasDynamicPreview = true;
}

// LABELS
String lb_targetLocale  = bundle.getString("lb_target_locale");
String lb_preview = bundle.getString("lb_preview");
String lb_text = bundle.getString("lb_text");
String lb_list = bundle.getString("lb_list");
String lb_dynamic_preview = bundle.getString("lb_dynamic_preview");
StringBuffer str_targetLocale = new StringBuffer();
String targetLocale = "";
String lb_pdf_preview = bundle.getString("lb_pdf_preview");

if (state.isViewerMode() || (state.isReviewMode() && state.getUserIsPm()))
{
    str_targetLocale.append("<select name='tarLocales' onchange='switchTargetLocale(this[this.selectedIndex].value)' style='font-size: 8pt;'>");
      
    Vector targetLocales = state.getJobTargetLocales();
    for (int i = 0, max = targetLocales.size(); i < max; i++)
    {
        Locale trg = ((GlobalSightLocale)targetLocales.get(i)).getLocale();

        str_targetLocale.append("<option ");

        if (trg.equals(state.getTargetLocale().getLocale()))
        {
            str_targetLocale.append("selected ");
        }
        str_targetLocale.append("value='").append(trg.toString()).append("'>");
        str_targetLocale.append(trg.getDisplayName(uiLocale));
        str_targetLocale.append("</option>");
    }
    str_targetLocale.append("</select>");
}
else
{
    str_targetLocale.append(state.getTargetLocale().getDisplayName(uiLocale));
    targetLocale = state.getTargetLocale().toString();
}

//  String str_showPreviewButton;
//  if (EditUtil.hasPreviewMode(state.getPageFormat()))
//  {
//      str_showPreviewButton = "showPreviewButton()";
//  }
//  else
//  {
//      str_showPreviewButton = "";
//  }

//  String str_showDynamicPreviewButton;
//  if (EditUtil.hasDynamicPreviewMode(tgtPage.getDataSourceType()))
//  {
//    str_showDynamicPreviewButton = "showDynamicPreviewButton()";
//  }
//  else
//  {
//    str_showDynamicPreviewButton = "";
//  }

String str_currentViewId = null;
switch (layout.getTargetViewMode())
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

boolean isXMLPreview = false;
if (pageFormat.equals(IFormatNames.FORMAT_XML))
{
	try {
		long sourcePageId = state.getSourcePageId();
		SourcePage sp = ServerProxy.getPageManager().getSourcePage(sourcePageId);
		long fpId = sp.getRequest().getJob().getFileProfile().getId();
   	
		File xslFile = null;
	    StringBuffer xslPath = new StringBuffer(AmbFileStoragePathUtils.getXslDir().getPath())
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
	} catch (Exception ex) { }
}
%>
<HTML>
<HEAD>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<body>
<SCRIPT>
var g_isReviewMode = eval("<%=state.isReviewMode()%>");
var highlightedElement = null;

function findTargetFrame()
{
    return parent.content;
}

function cancelEvent()
{
    if (window.event != null)
    {
        window.event.returnValue = false;
        window.event.cancelBubble = true;
    }
}

function canCloseTarget()
{
    var fr_target = findTargetFrame();

    if (fr_target != null)
    {
       try
       {
          return fr_target.CanClose();
       }catch(e){}
    }

    return true;
}

function raiseSegmentEditor()
{
    var fr_target = findTargetFrame();

    if (fr_target != null)
    {
        fr_target.RaiseSegmentEditor();
    }
}

function highlight(element)
{
    if (highlightedElement != null)
    {
        highlightedElement.style.color =
            "<%=skin.getProperty("skin.editor.element.highlight.color")%>";
        highlightedElement = null;
    }
    element.style.color =
        "<%=skin.getProperty("skin.editor.element.color")%>";
    highlightedElement = element;
}

function showList()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        raiseSegmentEditor();
        return false;
    }
    else
    {
        parent.showList();
        highlight(idList);
    }
}

function showText()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        raiseSegmentEditor();
        return false;
    }
    else
    {
        parent.showText();
        highlight(idText);
    }
}

function showPreview()
{
    if (!canCloseTarget())
    {
        cancelEvent();
        raiseSegmentEditor();
        return false;
    }
    else
    {
    	<%if (!isOOO) {%>
    	parent.showPreview();
        <%} else { %>
        try
        {
             window.parent.showProgressBar();
        }
        catch(e)
        {
        }
        var srcPagePath = '<%=pageNamePath%>';
        var targetLocale = "";
        try
        {
           targetLocale = document.all.tarLocales[document.all.tarLocales.selectedIndex].value;
        }
        catch(e)
        {
            targetLocale = '<%=targetLocale%>';
        }
        var index = srcPagePath.indexOf('/');
        var targetPagePath = targetLocale + srcPagePath.substring(index);
        window.parent.showPreviewPage(targetPagePath);
        //window.parent.showPreviewPage('<%=pageNamePath%>');
        <% } %>
        
        highlight(idPreview);
    }
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
    if (!canCloseTarget())
    {
        cancelEvent();
        raiseSegmentEditor();
        return false;
    }
    else
    {
        var srcPagePath = '<%=pageNamePath%>';
        var targetLocale = "";
        try
        {
           targetLocale = document.all.tarLocales[document.all.tarLocales.selectedIndex].value;
        }
        catch(e)
        {
            targetLocale = '<%=targetLocale%>';
        }
        var index = srcPagePath.indexOf('/');
        var targetPagePath = targetLocale + srcPagePath.substring(index);
        window.parent.showPDFPreview(targetPagePath);
        highlight(idPDFPreview);
    }
}

function showXMLPreview()
{
  window.parent.showXMLPreview('<%=pageNamePath%>');
  highlight(idXMLPreview);
}

function exportForDynamicPreview()
{
      var url = "/globalsight/CapExportServlet?CxeRequestType=<%=ExportConstants.PREVIEW%>" +
      "&MessageId=<%=pageId%>" +
      "&UiLocale=<%=uiLocale%>" +
      "&DataSourceType=<%=dataSource%>";
      var config = "config='height=800,width=,toolbar=no,menubar=no," +
        "scrollbars=yes,resizable=yes,location=no,directories=no,status=yes'";

      preview_window = window.open(url,'',config);
      preview_window.screenX = 0;
      preview_window.screenY = 0;
}

function switchTargetLocale(p_locale)
{
    if (g_isReviewMode)
    {
        // reload editor including comment pane
        parent.top.SwitchTargetLocale(p_locale);
    }
    else if (eval("<%=hasGsaTags%> && (!<%=singlePage%>)"))
    {
        // reload both source & target
        parent.parent.SwitchTargetLocale(p_locale);
    }
    else
    {
       try
       {
         // reload target only
         parent.content.SwitchTargetLocale(p_locale);
       }
       catch(e)
       {
          alert("Cannot switch targetLocale in PDF preview mode!");
       }
    }
}

function doOnload()
{
    highlight(document.getElementById("<%=str_currentViewId%>"));

    <%if (viewPdf) {
        sessionMgr.removeElement("tgt_view_pdf");
    %>
    setTimeout(showPDFPreview, "3000");
    <% }%>
}
</SCRIPT>
</HEAD>

<BODY BGCOLOR="<%=skin.getProperty("skin.editor.bgColor")%>" onload="doOnload()">
<DIV ID="main" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; LEFT: 2px; RIGHT: 2px; TOP: 5px;">
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR CLASS="tableHeadingBasic">
    <TD><%=lb_targetLocale%>: <%=str_targetLocale.toString()%></TD>
    <TD ALIGN="RIGHT">
<%if(hasPDFPreview){ %>
 <A id="idPDFPreview" CLASS="HREFBoldWhite" HREF="javascript:showPDFPreview()"
      onfocus="this.blur();"><%=lb_pdf_preview%></A> |
<%}%>
<% if (hasPreview) { %>
      <A id="idPreview" CLASS="HREFBoldWhite" HREF="#"
      onclick="showPreview(); return false;"
      onfocus="this.blur();"><%=lb_preview%></A> |
<% } %>
<% if (hasDynamicPreview) { %>
      <A id="idDynPreview" CLASS="HREFBoldWhite" HREF="#"
      onclick="exportForDynamicPreview(); return false;"
      onfocus="this.blur();"><%=lb_dynamic_preview%></A> |
<% } %>
<% if (isXMLPreview) { %>
      <A id="idXMLPreview" CLASS="HREFBoldWhite" HREF="javascript:showXMLPreview()"
      onfocus="this.blur();"><%=lb_preview%></A> |
<% } %>
      <A id="idText" CLASS="HREFBoldWhite" HREF="#"
      onclick="showText(); return false;"
      onfocus="this.blur();"><%=lb_text%></A> |
      <A id="idList" CLASS="HREFBoldWhite" HREF="#"
      onclick="showList(); return false;"
      onfocus="this.blur();"><%=lb_list%></A>
    </TD>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
