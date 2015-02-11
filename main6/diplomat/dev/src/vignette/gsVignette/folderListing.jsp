<%@ page contentType="text/html; charset=UTF-8"
        errorPage="error.jsp"
		import="com.vignette.cms.client.beans.*, java.net.*, java.util.*"
        session="true"%>
<jsp:useBean
		id="cms" class="com.vignette.cms.client.beans.CMS"
		scope = "session" />
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<HTML>
<HEAD>
<TITLE></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js"></SCRIPT>
</SCRIPT>
</HEAD>
<%!
	static String myTemplateName = "folderListing";
	String getParent( Asset asst ) throws NullObjectException
	{
		if( asst.getProject() != null )
			return asst.getProject().getManagementId();
		else
			return null;
	}

	String getState( ContentItem ci ) throws NullObjectException
	{
		String theState = "Not Set";
		if( ci.getState().isExpired() ) theState = "Expired";
		if( ci.getState().isLive() ) theState = "Live";
		if( ci.getState().isReadyForFinalReview() ) theState = "Ready for Final Review";
		if( ci.getState().isReadyForInternalUse() ) theState = "Ready for Internal Review";
		if( ci.getState().isReadyToLaunch() ) theState = "Ready to Launch";
		if( ci.getState().isUnknown() ) theState = "Unknown";
		if( ci.getState().isWorking() ) theState = "Working";

		return theState;
	}

	String getTemplateType( Template tmpl ) throws NullObjectException
	{
		String theType = "Not Set";

		if( tmpl.getTemplateType().isASP() ) theType = "ASP";
		if( tmpl.getTemplateType().isJSP() ) theType = "JSP";
		if( tmpl.getTemplateType().isTCL() ) theType = "TCL";

		return theType;
	}

	String getTemplateCategory( Template tmpl ) throws NullObjectException, RequestException
	{
		String theCategory = "Not Set";

		if( tmpl.getCategory().isComponent() ) theCategory = "Component";
		if( tmpl.getCategory().isContentEntry()  ) theCategory = "Content Entry";
		if( tmpl.getCategory().isIndex()  ) theCategory = "Index";
		if( tmpl.getCategory().isItem()  ) theCategory = "Item";
		if( tmpl.getCategory().isLibrary()  ) theCategory = "Library";
		if( tmpl.getCategory().isOther()  ) theCategory = "Other";
		if( tmpl.getCategory().isTransaction()  ) theCategory = "Trasaction";

		return theCategory;
	}
%>
<BODY BGCOLOR="#FFFFFF">
<%--
##################################################################################
  Gather all files and directories under the root project and display them.
##################################################################################
--%>
<%
     	Properties connectionProp = (Properties)session.getAttribute("properties");
        String startingProject = connectionProp.getProperty("ui_base_project").replace('/','>');
        CMSObject startingProjectObj = (CMSObject) cms.findProjectByPath(startingProject);


        String srcBaseProject = connectionProp.getProperty("source_base_project").replace('/','>');
        CMSObject srcBaseProjectObj = (CMSObject) cms.findProjectByPath(srcBaseProject);
        session.setAttribute("srcBaseProjectMid",srcBaseProjectObj.getManagementId());

        String trgBaseProject = connectionProp.getProperty("target_base_project").replace('/','>');
        CMSObject trgBaseProjectObj = (CMSObject) cms.findProjectByPath(trgBaseProject);
        session.setAttribute("trgBaseProjectMid",trgBaseProjectObj.getManagementId());
        
		String targetObject = null;
        CMSObject obj = null;
		if( request.getParameter("objId") != null )
		{
			targetObject = request.getParameter("objId");
            obj = cms.findByManagementId(targetObject);            
		}
        else
        {
            targetObject = startingProject;
            obj = startingProjectObj;
        }
        
        //set the value in the session to reverse the order for next time
		String parentId = null;

	    //=========================================================================
		// Object is a Project
	    //=========================================================================
		if ( obj instanceof Project )
		{
			Project targetProject = ( Project ) obj;
            if(obj.getManagementId().equals(startingProjectObj.getManagementId()))
            {
   			   parentId = null;
            }
            else
            {
               parentId = getParent( targetProject );
            }

			Project[] projectArray = targetProject.getProjects();
			Template[] templateArray = targetProject.getTemplates();
			StaticFile[] fileArray = targetProject.getFiles();
			Record[] recordArray = targetProject.getRecords();

            //now fix up the sorting
            int x=0;            
            TreeMap projects = new TreeMap();
            for(x=0; x<projectArray.length; x++){
               projects.put(projectArray[x].getName(),projectArray[x]);
            }
            
            TreeMap templates = new TreeMap();
            for(x=0; x<templateArray.length; x++){
               templates.put(templateArray[x].getName(),templateArray[x]);
            }
            TreeMap files = new TreeMap();
            for(x=0; x<fileArray.length; x++){
               files.put(fileArray[x].getName(),fileArray[x]);
            }
            
            TreeMap records = new TreeMap();
            for(x=0; x<recordArray.length; x++){
               records.put(recordArray[x].getName(),recordArray[x]);
            }
%>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 20px; LEFT: 20px;">
<div class="tabBox" style="clear:both;">
<TABLE CLASS="tableVbrowser" WIDTH="100%" CELLSPACING="0" CELLPADDING="0" BORDER="0">
<TR>
    <TD WIDTH="100%" VALIGN="TOP">
        <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="0" >
        <TR>
            <TD WIDTH="100%" VALIGN="TOP">

                <TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="2" BORDER="0">
                <TR>
                    <TD CLASS="tableHeadingBasic" NOWRAP><IMG SRC="Images/openProj.gif" ALIGN="LEFT">Project:&nbsp;<%=targetProject.getName()%></TD>
<% if (parentId != null) { %>
                    <TD  CLASS="tableHeadingBasic" ALIGN="RIGHT" NOWRAP>
<A href="folderListing.jsp?objId=<%= parentId %>"><IMG BORDER=0 SRC="Images/upProject.gif" BORDER="0"></A>
                    </TD>
<% } %>					
                </TR>
                </TABLE>

			<TABLE BGCOLOR=WHITE CELLSPACING=0 CELLPADDING=3 BORDER=0 WIDTH=100%>
			<%	
               Iterator iter = projects.values().iterator();
               while (iter.hasNext())
				{
					Project temp = (Project) iter.next();
			%>
			<TR>
                        <TD WIDTH="65%" NOWRAP><A CLASS="standardHREF" border=0 href="folderListing.jsp?objId=<%= temp.getManagementId() %>"><IMG BORDER=0 SRC="Images/Icon-Project.gif"></a> <%= temp.getName() %></TD>
						<TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						<TD WIDTH="15%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						<TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
                        <TD WIDTH="15%" NOWRAP ALIGN="CENTER">
						<%-- <A CLASS="standardHREF" HREF="createAddContentsURL" TARGET="fileList">Add Contents</A> --%>
						</TD>
                        </TR>
			<%	} %>
			<%--	for( int j = 0; j < templates.length; j++ )
				{
				//	Template tmp = templates[j];
			<TR><TD WIDTH="65%" NOWRAP><A CLASS="standardHREF" HREF="#" onClick="javascript:detail1Window = window.open('folderListing.jsp?objId=<%= tmp.getManagementId() %>','detail1Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);><IMG BORDER=0 SRC="Images/Icon-Template.gif"></a> <%= tmp.getName() %></TD>
                         <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						 <TD WIDTH="15%" NOWRAP ALIGN="RIGHT"><A CLASS="standardHREF" HREF="#" onClick="javascript:detail1Window = window.open('folderListing.jsp?objId=<%= tmp.getManagementId() %>','detail1Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);>Details</A></TD>
						 <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">|</SPAN></TD>
						 <TD WIDTH="15%" NOWRAP ALIGN="LEFT"><A CLASS="standardHREF" HREF="tabFrame.jsp?type=t&name=<%=tmp.getName()%>&tmplid=<%=tmp.getTemplateId()%>&tmpltype=<%=getTemplateType(tmp)%>&ext=<%=temp1.getExtension()%>&table=<%=((temp1.getTable() == "") ? "Not Set" : temp1.getTable()) %>&mid=<%=temp1.getManagementId()%>&status=<%=getState( temp1 )%>" TARGET="fileList">Add File</A></TD>
			</TR>
			// }
			--%>
			<%--	for( int m = 0; m < records.length; m++ )
				{
					Record temp3 = records[m];
			<TR><TD WIDTH="65%" NOWRAP><A CLASS="standardHREF" HREF="#" onClick="javascript:detail3Window = window.open('folderListing.jsp?objId=<%= temp3.getManagementId() %>','detail3Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);><IMG BORDER=0 SRC="Images/Icon-Record.gif"></a> <%= temp3.getName() %></TD>
                         <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						 <TD WIDTH="15%" NOWRAP ALIGN="RIGHT"><A CLASS="standardHREF" HREF="#" onClick="javascript:detail3Window = window.open('folderListing.jsp?objId=<%= temp3.getManagementId() %>','detail3Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);>Details</A></TD>
						 <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">|</SPAN></TD>
<TD WIDTH="15%" NOWRAP ALIGN="LEFT">
<A CLASS="standardHREF" HREF="tabFrame.jsp?type=r&server=<%=temp3.getServer()%>&database=<%=temp3.getDatabase()%>&table=<%=temp3.getTable()%>&keyid=<%=temp3.getRecordId()%>&key=<%=temp3.getPrimaryKey()%>&name=<%=temp3.getName()%>&mid=<%=temp3.getManagementId()%>&status=<%=getState( temp3 )%>" TARGET="fileList">Add File</A></TD>


			</TR>
			}
			--%>

			<%	
               iter = files.values().iterator();
               while (iter.hasNext())
				{
					StaticFile sf = (StaticFile) iter.next();
			%>

			<TR><TD WIDTH="65%" NOWRAP><A CLASS="standardHREF" HREF="#" onClick="javascript:detail2Window = window.open('folderListing.jsp?objId=<%= sf.getManagementId() %>','detail2Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);><IMG BORDER=0 SRC="Images/Icon-File.gif" ALIGN="LEFT"></a><%= sf.getName() %></TD>
                         <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						 <TD WIDTH="15%" NOWRAP ALIGN="RIGHT"><A CLASS="standardHREF" HREF="#" onClick="javascript:detail2Window = window.open('folderListing.jsp?objId=<%= sf.getManagementId() %>','detail2Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);>Details</A></TD>
						 <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">|</SPAN></TD>
	                    <TD WIDTH="15%" NOWRAP ALIGN="LEFT"><A CLASS="standardHREF" HREF="tabFrame.jsp?type=f&path=<%=URLEncoder.encode(sf.getPath())%>&mid=<%=URLEncoder.encode(sf.getManagementId())%>&status=<%=URLEncoder.encode(getState(sf))%>" TARGET="fileList">Add File</A></TD>

						</TR>
			<%	} %>
			</TABLE>

			</TD></TR></TABLE>
			
<br><br><a border=0 href="folderListing.jsp"><IMG BORDER=0 SRC="Images/goHome.gif"></a> Return to <B><%=startingProject%></B>


<%
		}
		else
		{
	    	//=========================================================================
			// Object is a Template
	    	//=========================================================================
			if ( obj instanceof Template )
			{
				Template targetTemplate = ( Template ) obj;
				parentId = getParent( targetTemplate );
				String[] paths = targetTemplate.getPaths();
%>
				<I>Properties of Template</I> <B><%=targetTemplate.getName()%></B><BR>
				<TABLE CELLSPACING=0 CELLPADDING=0 BORDER=1><TR><TD bgcolor=white>
				<TABLE CELLSPACING=0 CELLPADDING=3 BORDER=0>
					<TR><TD><B>Template ID</B></TD><TD><%=targetTemplate.getTemplateId()%></TD></TR>
					<TR><TD><B>Paths</B></TD><TD>
					<%
						for( int n = 0; n < paths.length; n++ )
						{
					%>
							<%=paths[n]%><BR>
					<%
						}
					%>
					</TD></TR>
					<TR><TD><B>Cached</B></TD><TD><%=targetTemplate.isCached()%></TD></TR>
					<TR><TD><B>Extension</B></TD><TD><%=targetTemplate.getExtension()%></TD></TR>
					<TR><TD><B>Table</B></TD><TD><%=((targetTemplate.getTable() == "") ? "Not Set" : targetTemplate.getTable()) %></TD></TR>
					<TR><TD><B>Type<B></TD><TD><%=getTemplateType( targetTemplate )%></TD></TR>
					<TR><TD><B>Category<B></TD><TD><%=getTemplateCategory( targetTemplate )%></TD></TR>
					<TR><TD><B>State<B></TD><TD><%=getState( targetTemplate )%></TD></TR>
					<TR>
						<TD bgcolor=#c6c6c6>
					<%
						// Make sure the template has a path to view (i.e. Library will not have a path)
                    	if ( paths.length != 0 )
						{
					%>
							<A HREF="#" onClick="viewTemplate('<%= targetTemplate.getPaths()[0] %>')"><IMG BORDER=no SRC="Images/Button-Preview.gif"></A>View
					<%
						}
						else
						{
					%>
						&nbsp;
					<%  } %>
						</TD>
						<TD bgcolor=#c6c6c6>
							<A HREF="#" onClick="viewTemplate('view_source.jsp?templateId=<%= targetTemplate.getManagementId() %>')"><IMG BORDER=no SRC="Images/Button-TemplateEditor.gif"></A>View Source
						</TD>
					</TR>
				</TABLE>
				</TD></TR></TABLE>
		<%
			}
			else
			{
		    	//=========================================================================
				// Object is a Record
	    		//=========================================================================
				if( obj instanceof Record )
				{
					Record targetRecord = ( Record ) obj;
					parentId = getParent( targetRecord );
			%>
					<I>Properties of Record</I> <B><%=targetRecord.getName()%></B><BR>
					<TABLE CELLSPACING=0 CELLPADDING=0 BORDER=1><TR><TD bgcolor=white>
					<TABLE CELLSPACING=0 CELLPADDING=3 BORDER=0>
						<TR><TD><B>Record ID</B></TD><TD><%=targetRecord.getRecordId()%></TD></TR>
						<TR><TD><B>Server</B></TD><TD><%=targetRecord.getServer()%></TD></TR>
						<TR><TD><B>Database</B></TD><TD><%=targetRecord.getDatabase()%></TD></TR>
						<TR><TD><B>Table</B></TD><TD><%=targetRecord.getTable()%></TD></TR>
						<TR><TD><B>Primary Key</B></TD><TD><%=targetRecord.getPrimaryKey()%></TD></TR>
						<TR><TD><B>State</B></TD><TD><%=getState( targetRecord )%></TD></TR>
					</TABLE>
					</TD></TR></TABLE>
			<%
				}
				else
				{
	    			//=========================================================================
					// Object is a Static File
	    			//=========================================================================
					if( obj instanceof StaticFile )
					{
						StaticFile targetFile = ( StaticFile ) obj;
						parentId = getParent( targetFile );
				%>
						<I>Properties of File</I> <B><%=targetFile.getName()%></B><BR>
						<TABLE CELLSPACING=0 CELLPADDING=0 BORDER=1><TR><TD bgcolor=white>
						<TABLE CELLSPACING=0 CELLPADDING=3 BORDER=0>
							<TR><TD><B>Path</B></TD><TD><%=targetFile.getPath()%></TD></TR>
							<TR><TD><B>State</B></TD><TD><%=getState( targetFile )%></TD></TR>
<%--                            
				<%
						if ( targetFile.getName().endsWith(".gif") || targetFile.getName().endsWith(".bmp") || targetFile.getName().endsWith(".jpg"))
						{
				%>
							<TR><TD><B>Value</B></TD><TD><IMG BORDER=0 SRC="<%=targetFile.getPath()%>"></TD></TR>
					<%
						}
					%>
--%>                    
						</TABLE>
					</TD></TR></TABLE>
				<%
					}
				}
			}
		}
%>
<br><br>


    </TABLE>
            </TD>
        </TR>
        </TABLE>
    </TD>
</TR>
</TABLE>
</DIV>

</BODY>
</HTML>
