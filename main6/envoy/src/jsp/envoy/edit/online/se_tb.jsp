<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.util.GlobalSightLocale,
	    com.globalsight.terminology.Hitlist,
	    com.globalsight.terminology.ITermbase,
	    com.globalsight.terminology.termleverager.TermLeverageMatchResult,
	    com.globalsight.everest.edit.online.SegmentView,
	    com.globalsight.everest.edit.online.SegmentMatchResult,
	    com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.webapp.WebAppConstants,
	    com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorHelper,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.Collection,
            java.util.Iterator,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper,
            java.io.File,
            java.text.NumberFormat"
    session="true"
%><%

ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

NumberFormat percent = NumberFormat.getPercentInstance(
  (Locale)session.getAttribute(WebAppConstants.UILOCALE));
percent.setMinimumFractionDigits(2);

boolean b_rtl = EditUtil.isRTLLocale(state.getTargetViewLocale());

String lb_matchResults = bundle.getString("lb_tb_match_results");
String lb_clickToCopy  = bundle.getString("action_click_copy");
String lb_noTerms      = bundle.getString("lb_tb_no_match_results");
String lb_clickToOpenTb = bundle.getString("lb_click_to_open_tb");
String lb_explanation = bundle.getString("lb_target_terms_tip");
String lb_matches_from_tb = bundle.getString("lb_matches_from_tb");

String str_defaultTermbaseName = state.getDefaultTermbaseName();
long l_defaultTermbaseId = state.getDefaultTermbaseId();
boolean b_haveTermbase = (str_defaultTermbaseName != null);
boolean canAccessTB = state.isCanAccessTB();

Collection tbMatches = view.getTbMatchResults();

StringBuffer term_segments = new StringBuffer();

if (tbMatches != null && tbMatches.size() > 0){
    int i = 0;
 
    for (Iterator it = tbMatches.iterator(); it.hasNext(); ) {
        TermLeverageMatchResult p = (TermLeverageMatchResult)it.next();
        Hitlist.Hit sourceHit = p.getSourceHit();
        
        String str_target;
        long l_conceptId;
        long l_termId;
    
        for (Iterator it1 = p.getTargetHitIterator(); it1.hasNext(); )
        {
            Hitlist.Hit hit = (Hitlist.Hit)it1.next();
    
            str_target = hit.getTerm();
    
            if (b_rtl)
            {
                str_target = "<SPAN DIR=rtl>" + str_target + "</SPAN>";
            }
    
            l_conceptId = hit.getConceptId();
            l_termId = hit.getTermId();
            // i_score = hit.getScore();
          
            //for show terminology image
            String termImgLink = "";
          
            String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
            File parentFilePath = new File(termImgPath.toString());
            File[] files = parentFilePath.listFiles();
                    
            if (files != null && files.length > 0) {
                for (int j = 0; j<files.length; j++) {
                    File file = files[j];
                    String fileName = file.getName();
                        
                    if(fileName.lastIndexOf(".") > 0) {
                        String tempName= fileName.substring(0, fileName.lastIndexOf("."));

                        if(tempName.equals("tb_"+Long.toString(l_termId))) {
                           termImgLink = "<a href=\"#\" onClick=\"termImgShow('" + fileName + "');\">" 
                               + " <img src=images\\image.gif border=0 width=20 height=25 align=\"absmiddle\">" + "</a>";
                        }
                   }
                }
             }

            term_segments.append("a_TermSegments[");
            term_segments.append(i++);
            term_segments.append("] = { data: \"");
            term_segments.append(EditUtil.toJavascript(hit.getTerm()));
            term_segments.append("\", text: \"");
            term_segments.append(sourceHit.getTerm() + "<br><div class='targetTerm' cid='" + l_conceptId +"' tid='"+ l_termId+"'><b>&nbsp;&nbsp;" + str_target + "</b>&nbsp;&nbsp;"+ EditUtil.toJavascript(termImgLink ) +"<div>");
            term_segments.append("\", label: \"");
            term_segments.append("");
            
            term_segments.append("\" };\n");
         }
     }
}

String str_termSegments = term_segments.toString();
%>
<HTML>
<HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<script src="/globalsight/includes/menu/js/menu4.js"></script>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>
<link type="text/css" rel="StyleSheet" href="/globalsight/includes/menu/skins/winclassic.css">
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none; }
.link  { color: blue; cursor: hand;cursor:pointer; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript">
var g_termIndex = 0;
var g_selectedTerm = null;
var g_defaultTermbaseId = "<%=l_defaultTermbaseId%>";

function fnInit() {
    if (document.recalc) {
		   sourceBoxTitle.style.setExpression("pixelWidth", "document.body.clientWidth");
		   sourceBoxTitle.style.setExpression("pixelHeight", "document.body.clientHeight - 35");
	  }
	  
	  initSegmentTerm();
	  ContextMenu.intializeContextMenu();
}

window.onload = fnInit;

var a_TermSegments = new Array();
<%=str_termSegments%>

// Menus

Menu.prototype.cssFile = "/globalsight/includes/menu/skins/winclassic.css";
Menu.prototype.mouseHoverDisabled = false;
Menu.prototype.showTimeout = 5;
Menu.prototype.closeTimeout = 5;
MenuButton.prototype.subMenuDirection = "horizontal";
// Menu.keyboardAccelKey = -1;
// Menu.keyboardAccelProperty = "";
var termMenu = new Menu();
termMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_in_editor") %>", copyTermToEditor));
termMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_browse_term") %>", browseTerm));
MenuButton.prototype.subMenuDirection = "horizontal";

function copyTermToEditor()
{
    parent.parent.InsertTerm(a_TermSegments[g_termIndex].data);
}

function browseTerm(event)
{
    if (!g_selectedTerm) return;
    var conceptid = g_selectedTerm.getAttribute("cid");
    var termid = g_selectedTerm.getAttribute("tid");

    ShowTermbaseConceptTerm(g_defaultTermbaseId, conceptid, termid, event);
}

function showContextMenu(event)
{
    var el;
    var selection;

    if(document.recalc) {
	      event = window.event;
	      el = event.srcElement;
	      event.returnValue=false;
    }
    else {
	      //event = getEvent();
	      el = event.target;
    }

    while (el != null && el.tagName != "DIV") { el = el.parentNode; };
    var showTbMenu = (el != null && el.className == 'targetTerm');
    
    if(document.all) {
        if (showTbMenu ) {
            g_selectedTerm = el;
            // find left and top
            var left = event.screenX || event.pageX;
            var top  = event.screenY || event.pageY;
    
            termMenu.invalidate();
            termMenu.show(left, top);
        }
        else {
            return;
        }
    }
    else {   
        event.preventDefault();
        event.stopPropagation(); 
    	  
    	  if (showTbMenu)
        {
            g_selectedTerm = el;
        }
        else {
            return;
        }
         	
		    var popupoptions = [
			      new ContextItem("<%=bundle.getString("lb_insert_in_editor") %>",
							function(){ copyTermToEditor();}),
			      new ContextItem("<%=bundle.getString("lb_browse_term") %>",
							function(){ browseTerm(event);})
    	  ];    	
    }

    ContextMenu.display(popupoptions, event); 

}

function showTermSegmentData(index)
{

  var o = a_TermSegments[index];
  idTermSegmentText.innerHTML  = o.text;
  
  
  if(document.recalc)
  {
  	  idTermSegmentLabel.innerText = o.label;
  }
  else
  {
  	  idTermSegmentLabel.textContent = o.label;
  }
  
  if (index == 0)
  {
    idTermPrev.style.visibility = 'hidden';
  }
  else
  {
    idTermPrev.style.visibility = 'visible';
  }
  
  if (index == a_TermSegments.length - 1)
  {
    idTermNext.style.visibility = 'hidden';
  }
  else
  {
    idTermNext.style.visibility = 'visible';
  }
}

function goLeftTerm()
{
  if (a_TermSegments.length > 0)
  {
    if (g_termIndex == 0)
    {
      g_termIndex = a_TermSegments.length - 1;
    }
    else
    {
      --g_termIndex;
    }
  }
  
  showTermSegmentData(g_termIndex);
}

function goRightTerm()
{
  if (a_TermSegments.length > 0)
  {
    if (g_termIndex == (a_TermSegments.length - 1))
    {
      g_termIndex = 0;
    }
    else
    {
      ++g_termIndex;
    }
  }
  
  showTermSegmentData(g_termIndex);
}

function initSegmentTerm()
{
  if (a_TermSegments.length > 0)
  {
    showTermSegmentData(0);
  }
}

function showTermSegmentData(index)
{

  var o = a_TermSegments[index];
  idTermSegmentText.innerHTML  = o.text;
  
  
  if(document.recalc)
  {
  	  idTermSegmentLabel.innerText = o.label;
  }
  else
  {
  	  idTermSegmentLabel.textContent = o.label;
  }
  
  if (index == 0)
  {
    idTermPrev.style.visibility = 'hidden';
  }
  else
  {
    idTermPrev.style.visibility = 'visible';
  }
  
  if (index == a_TermSegments.length - 1)
  {
    idTermNext.style.visibility = 'hidden';
  }
  else
  {
    idTermNext.style.visibility = 'visible';
  }
}

function isIE(){
	if(navigator.appName.indexOf("Microsoft")!=-1)
        if (navigator.userAgent.indexOf('Opera') == -1)
    		return true;
	return false;
}

//for terminology image show or hide.
function termImgShow(divName) {
    window.open ('terminologyImg/'+ divName,'newwindow','top=0,left=0,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no') ;
}
</SCRIPT>
</HEAD>
<BODY>
<HR COLOR="#0C1476" WIDTH="95%">
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
  <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="5" HEIGHT="1"></TD>
  <TD WIDTH="100%">
    <SPAN CLASS="standardTextBold">
    <% if (b_haveTermbase)
    {
     if(canAccessTB)
     {%>
       <%= lb_matches_from_tb %>    
       <SPAN ID="idTermbase" CLASS="link" TITLE="<%=lb_clickToOpenTb%>"
       onclick="ShowTermbase('<%=l_defaultTermbaseId%>')">
       <%=str_defaultTermbaseName%></SPAN>.
     <%}
     else
     {%>
       <%= lb_matches_from_tb %>    
       <SPAN ID="idTermbase" TITLE="<%=lb_clickToOpenTb%>">
       <%=str_defaultTermbaseName%></SPAN>.
   <%}
    }
    else
    {
    %>
    <%= lb_matchResults %>
  <%}
    %>
    </SPAN>
    <SPAN CLASS="standardTextItalic"><%=bundle.getString("lb_right_click_for_actions") %></SPAN>
  </TD>
</TR>
</TABLE>
<DIV ID="sourceBoxTitle"
     STYLE="position: absolute; top: 35px; left: 0px; overflow: auto;" >
<TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">

<%
    if (tbMatches != null && tbMatches.size() > 0)
    {
%>
  <TR>
       <TD VALIGN="TOP" width=6><a href="#">
          <IMG SRC="/globalsight/images/previousMatchArrow.gif" height=12 width=6
            id="idTermPrev" border=0
            class="clickable" onclick="goLeftTerm(); return false;"></a>
      </TD>
      <TD VALIGN="TOP" width=6>
         <SPAN id="idTermSegmentLabel" class="standardTextBold"></SPAN>
      </TD>
      <TD VALIGN="TOP" id="idTermSegmentText" class="editorStandardText"  oncontextmenu="showContextMenu(event);" width=400 noWrap=false></TD>
      <TD VALIGN="TOP" width=6><a href="#">
        <IMG SRC="/globalsight/images/nextMatchArrow.gif" height=12 width=6
          id="idTermNext" border=0
          class="clickable" onclick="goRightTerm(); return false;"> </a>
      </TD>
    </TR>
<%
    }
    else
    {
%>  
  <TR>  
    <TD VALIGN="TOP" WIDTH="100%" class="standardTextItalic">
      <%=lb_noTerms%>
    </TD>
  </TR>
<%
    }
%>
</TABLE>
</DIV>
</BODY>
</HTML>
