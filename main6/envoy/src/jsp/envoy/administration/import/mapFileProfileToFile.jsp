<%@page import="com.globalsight.everest.servlet.util.SessionManager"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.imp.MapFileProfileToFileHandler,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.SortUtil,
            java.util.Collections,
            java.util.Hashtable,
            java.util.Iterator,
            java.util.ResourceBundle,
            java.util.Set"
     session="true" 
%>
<%@page import="com.globalsight.everest.cvsconfig.CVSFileProfileManagerLocal"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.globalsight.everest.cvsconfig.CVSFileProfile"%>
<%@page import="com.globalsight.everest.cvsconfig.CVSModule"%>

<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobName" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="previous" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<jsp:useBean id="rssPrevious" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="rssCancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<jsp:useBean id="cvsPrevious" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cvsCancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    // get session manager from the http session
    SessionManager sessionMgr = (SessionManager)session.
               getAttribute(WebAppConstants.SESSION_MANAGER);
    
    String selfUrl = self.getPageURL();
    String jobNameUrl = jobName.getPageURL();
    String previousUrl = previous.getPageURL();
    String cancelUrl = cancel.getPageURL() + "&pageAction=" + WebAppConstants.CANCEL;
    
    //Added by Vincent Yan, 2010/04/21
    //To previous/cancel when the operation is RSS job
    String jobType = (String)sessionMgr.getAttribute("jobType");
    String otherPreviousUrl = null, otherCancelUrl = null;
    if (jobType != null && "rssJob".equals(jobType)) {
    	otherPreviousUrl = rssPrevious.getPageURL();
    	otherCancelUrl = rssCancel.getPageURL();
    } else if (jobType != null && "cvsJob".equals(jobType)) {
    	otherPreviousUrl = cvsPrevious.getPageURL();
    	otherCancelUrl = cvsCancel.getPageURL();
    }
    
    String lbCancel = bundle.getString("lb_cancel");
    String lbNext = bundle.getString("lb_next");
    String lbPrevious = bundle.getString("lb_previous");
    String lbClearAll = bundle.getString("lb_clear_all");
    String lbFiles = bundle.getString("lb_files_lowercase");
    String lbMap = bundle.getString("lb_map");

    //Get session data

    // get the selected files separated by extension
    Hashtable selectedFiles = 
       (Hashtable)sessionMgr.getAttribute(MapFileProfileToFileHandler.SELECTED_FILES);
    // get the file profiles that are allowed separated by extension
    Hashtable fileProfiles = 
       (Hashtable)sessionMgr.getAttribute(MapFileProfileToFileHandler.FILE_PROFILES);
    // get the existing mappings
    Hashtable mappings = 
        (Hashtable)sessionMgr.getAttribute(MapFileProfileToFileHandler.DISPLAY_MAPPINGS);
    // get the boolean that specifies if the mappings are complete or not (if complete
    // the user can move on to the NEXT page)
    boolean doneMapping = 
        ((Boolean)sessionMgr.getAttribute(MapFileProfileToFileHandler.DONE_MAPPING)).booleanValue();
    
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= bundle.getString("lb_map_file_to_file_profile") %></TITLE>
<%@ include file="/envoy/common/header.jspIncl" %>

<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>

<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/SelectableElements.js"></SCRIPT>

<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;  
    var guideNode = "";
    var helpFile = "<%=bundle.getString("help_import_map_file")%>";
   
    function init()
    {
        // Load the Guides
        loadGuides();
    }
   
    function submitForm(action) 
    {    
       if (action == '<%=WebAppConstants.NEXT%>') 
       {  
          // verify that all files are mapped
          if (!<%=doneMapping%>)
          {
            alert("<%=bundle.getString("jsmsg_map_file_to_file_profile")%>");
            return;
          }

          // otherwise move on to the job name screen the next step in
          // importing files
          mapFileProfileForm.action = "<%=jobNameUrl%>";
       }       
       else if (action == '<%=WebAppConstants.CANCEL%>')
       {
          // go to the first import screen
          // and clear out all contents
          mapFileProfileForm.action= "<%=otherCancelUrl == null ? cancelUrl : otherCancelUrl%>";
       }
       else if (action == '<%=WebAppConstants.PREVIOUS%>') 
       {
          mapFileProfileForm.action="<%=otherPreviousUrl == null ? previousUrl : otherPreviousUrl%>";
       }
       else if (action == 'clearAll')
       {
          mapFileProfileForm.action="<%=selfUrl%>" + "&pageAction=clearAll";
       }

       mapFileProfileForm.submit();
    }   

    function saveMappings(extension, fpList)
    {
      // variables to hold the info for the mapping
      var selectedFP = null;
      var fileString = null;
       
      // evaluates to the FP selection object
      var sel = eval('mapFileProfileForm.' + fpList); 
      var optionsFP = sel.options;

      // finds the selectedFP
      for (var i = 0; ( selectedFP == null) && (i < optionsFP.length) ; i++)
      {
        if (optionsFP[i].selected)
        {
          selectedFP = optionsFP[i].value;
        }
      }
      // if a file profile wasn't selected
      if (selectedFP == null)
      {
        alert("<%=bundle.getString("jsmsg_choose_file_profile")%>");
        return;
      }

      // evaluates to the file selection object set up with 
      // "div" using SelectableElements
      var files = eval('div' + extension); 
      
      var selFiles = files.getSelectedItems();
      var fileString = "";

      if (selFiles != null && selFiles.length > 0)
      {

        fileString += encodeURIComponent(selFiles[0].firstChild.data).replace(/%C2%A0/g, "%20");
        for (var idx=1 ; idx < selFiles.length; idx++)
        {
          var selfFile = encodeURIComponent(selFiles[idx].firstChild.data).replace(/%C2%A0/g, "%20");
          fileString += "," + selfFile;
        }
         
        // For the bug AMB177 that the name include some special chars, such as '&'.
        // replace Non-breaking space(&nbsp;) with normal space.
        //fileString = encodeURIComponent(fileString).replace(/%C2%A0/g, "%20");
      }
      else
      {
        alert("<%=bundle.getString("jsmsg_choose_file")%>");
        return;
      }
      
      // set the action value to store the new mapping
      mapFileProfileForm.action = "<%=selfUrl%>";

      $('<%=MapFileProfileToFileHandler.MAP_EXTENSION%>').value = extension;
      $('<%=MapFileProfileToFileHandler.MAP_FILE_PROFILE%>').value = selectedFP;
      $('<%=MapFileProfileToFileHandler.MAP_FILE_NAMES%>').value = fileString;

      mapFileProfileForm.submit();
    }

    function $(id)
    {
      return document.getElementById(id);
    }

    </SCRIPT>

<STYLE type="text/css">
.list {
    border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;   
    }
fieldset {
    padding:    5px;
    margin:     10px 5px;
    color:      #0C1476;
}         
 .div-list1 {
    background: Window;
    border:     1px solid ThreeDShadow;
    position: relative; 
    width: 400px; 
    height: 200px; 
    overflow: auto;
    }
    
 .div-list2 {
    background: Window;
    border:     1px solid ThreeDShadow;
    position: relative; 
    width: 440px; 
    height: 200px; 
    overflow: auto;
    }
    
 .div-list div {
    padding:    2px 5px;
    }
    
 .selected {
    background: Highlight;
    color:      HighlightText;
    border: solid 1px black;
  }
  
 .mapped {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 9pt;
    font-style: italic;
    color: gray;
    }
    
</STYLE>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="init();"  CLASS="standardText">


<FORM NAME="mapFileProfileForm" METHOD="POST">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading">
<%=bundle.getString("lb_map_file_to_file_profile")%>
</SPAN>
<P>
<SPAN CLASS="standardText">
<%= bundle.getString("helper_text_map_file_profile_to_file") %></SPAN>
<P>

<%
       CVSFileProfileManagerLocal cvsFPManager = new CVSFileProfileManagerLocal();
       CVSModule cvsModule = (CVSModule)sessionMgr.getAttribute("cvsmodule");
       String projectId = (String)sessionMgr.getAttribute(WebAppConstants.PROJECT_ID);
       boolean isCVSJob = false;
       if (cvsModule != null)
    	   isCVSJob = true;
       String cvsModuleId = "", srcLocale = "";
       if (isCVSJob) {
    	   cvsModuleId = String.valueOf(cvsModule.getId());
    	   srcLocale = (String)sessionMgr.getAttribute("srcLocale");
       }
       HashMap<String, String> params = null;
       long defaultFileProfile = 0L;
       
       Set keys = selectedFiles.keySet();
       for(Iterator i = keys.iterator() ; i.hasNext() ; )
       {
         String extension = (String)i.next();
         String fileProfileName = new String();
         String displayExtension = extension;
         if (extension.equals(MapFileProfileToFileHandler.ANY_EXTENSION))
         {
             displayExtension = bundle.getString("lb_extension_all");
         }
         if (isCVSJob) {
        	 params = new HashMap<String, String>();
        	 params.put("project", projectId);
        	 params.put("sourceLocale", srcLocale);
        	 params.put("module", cvsModuleId);
        	 params.put("fileExt", extension);
        	 ArrayList<CVSFileProfile> cvsfps = (ArrayList<CVSFileProfile>)cvsFPManager.getAllCVSFileProfiles(params);
        	 if (cvsfps != null && cvsfps.size()>0) {
        		 defaultFileProfile = cvsfps.get(0).getFileProfile().getId();
        	 }
         }
%>       
<fieldset>
<LEGEND><SPAN CLASS="standardTextBold"><%=displayExtension%> <%=lbFiles%></SPAN></LEGEND>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
    <TD><%=bundle.getString("lb_file_profiles")%>:
        <select name="FP<%=extension%>">
<%
            Set fps = (Set)fileProfiles.get(extension);
            boolean empty = true;
            if (fps != null && fps.size() > 0 ) 
            {
                empty = false;
                for (Iterator k = fps.iterator() ; k.hasNext() ; ) 
                {
                    FileProfile fpi = (FileProfile)k.next();
                    fileProfileName = fpi.getName();
                    if (fpi.getId() == defaultFileProfile)
                    	out.println("<option value='" + fpi + "' selected>" + fileProfileName + "</option>");
                    else
                    	out.println("<option value='" + fpi + "'>" + fileProfileName + "</option>");
                }
            }
%>
    </select>
    </TD>
    </TR>
<%
            if (empty)
            {
%>
            <TR>
                <TD>
                <SPAN CLASS="warningText"><%=bundle.getString("msg_no_import_file_profiles")%></SPAN>
                </TD>
            </TR>
<%
            }
%>
<TR>
   <TD>
      <%=bundle.getString("lb_files")%>:
   </TD>
   <TD>&nbsp;</TD>
   <TD>
        <SPAN CLASS="standardText"><%=bundle.getString("lb_mappings")%>:</SPAN>
    </TD>

</TR>
<TR>
   <TD>
        <div id = "File<%=extension%>" class="div-list1" onselectstart="return false">
 <%
               Set files = (Set)selectedFiles.get(extension);
               
               if (files != null)
               {
                    for (Iterator j = files.iterator() ; j.hasNext() ; )
                    {
                        StringBuffer text = new StringBuffer();
                        String fileName = (String)j.next();
                        Hashtable m = (Hashtable)mappings.get(extension);
                        if (m != null && m.containsKey(fileName))
                        {
%>
                            <DIV CLASS="mapped"><%=fileName.replaceAll(" ", "&nbsp;")%></DIV>
<%
                        }
                        else
                        {
%>
                            <DIV CLASS="standardText"><%=fileName.replaceAll(" ", "&nbsp;")%></DIV>
<%        
                        }
                    }
               }
               
%>
    </div>
    
<script language="JavaScript">
var div<%=extension%> = new SelectableElements(document.getElementById("File<%=extension%>"), true);
</script>
    
    
   </TD>
   <TD WIDTH='60' VALIGN="middle" aligh="center">
      <INPUT TYPE="BUTTON" NAME="MAP<%=extension%>" VALUE="<%=lbMap%>" 
             ONCLICK="saveMappings('<%=extension%>','FP<%=extension%>')">   
   </TD>
   <TD>
        <div id="Map<%=extension%>" class="div-list2" onselectstart="return false">
<%
        Hashtable mappedFiles= (Hashtable)mappings.get(extension);
        if (mappedFiles != null && mappedFiles.size() > 0)
        {
            Set fileNames = mappedFiles.keySet();
            ArrayList mappingText = new ArrayList();
            for (Iterator ifn = fileNames.iterator() ; ifn.hasNext() ; )
            {
                String filename = (String)ifn.next();
                String fpName = (String)mappedFiles.get(filename);
                StringBuffer displayString = new StringBuffer(filename);
                displayString.append(" ---> ");
                displayString.append(fpName);
                mappingText.add(displayString.toString());
            }

            SortUtil.sort(mappingText);
            for (int j = 0 ; j < mappingText.size() ; j++)
            {
                String dString = (String)mappingText.get(j);
%>
                <div CLASS="standardText"><%=dString.toString()%></div>
<%
            }
        }
%>
        </div>
    </TD>
</TR>
<TR>
    <TD COLSPAN=3>&nbsp;</TD>
</TR>
</TABLE>
</fieldset>
<%
    }
%>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
    <TD>
        <INPUT TYPE="BUTTON" NAME="<%=lbClearAll%>" VALUE="<%=lbClearAll%>" 
            ONCLICK="submitForm('<%=WebAppConstants.CLEAR_ALL%>')">
    </TD>
    <TD WIDTH=540>&nbsp;</TD>
    <TD ALIGN="RIGHT">
        <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
            ONCLICK="submitForm('<%=WebAppConstants.CANCEL%>')">   
        <INPUT TYPE="BUTTON" NAME="<%=lbPrevious%>" VALUE="<%=lbPrevious%>"
            ONCLICK="submitForm('<%=WebAppConstants.PREVIOUS%>')">
        <INPUT TYPE="BUTTON" NAME="<%=lbNext%>" VALUE="<%=lbNext%>"
            ONCLICK="submitForm('<%=WebAppConstants.NEXT%>')">   
          
   </TD> 
</TR>
</TABLE>
</DIV>

<div style="display:none">
  <input type="hidden" id="<%=MapFileProfileToFileHandler.MAP_EXTENSION%>" name="<%=MapFileProfileToFileHandler.MAP_EXTENSION%>"/>
  <input type="hidden" id="<%=MapFileProfileToFileHandler.MAP_FILE_PROFILE%>" name="<%=MapFileProfileToFileHandler.MAP_FILE_PROFILE%>"/>
  <input type="hidden" id="<%=MapFileProfileToFileHandler.MAP_FILE_NAMES%>" name="<%=MapFileProfileToFileHandler.MAP_FILE_NAMES%>"/>
</div>

</FORM>

</BODY>
</HTML>