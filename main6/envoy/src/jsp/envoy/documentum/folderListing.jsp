<%@ page contentType="text/html; charset=UTF-8"
		import="java.net.*, java.util.*,com.documentum.com.*,com.documentum.fc.client.*,com.documentum.fc.common.*"
        import="com.globalsight.cxe.adapter.documentum.DocumentumAdapter"
        session="true" %>
<%
IDfSessionManager sMgr= null;
IDfSession dsession= null;
IDfLoginInfo loginInfoObj = null;
IDfClientX clientx = null;
IDfClient client = null;

try {        
%>
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<HTML>
<HEAD>
<TITLE>Documentum UI</TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="Includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<%!
	static String myTemplateName = "folderListing";
	String getParent(Object asst ) throws Exception
	{
			return null;
	}
%>
<%
        //log into Documentum
        dsession= (IDfSession) session.getAttribute("dsession");
        if (dsession==null)
        {
            System.out.println("**** Logging into Documentum");

            clientx = new DfClientX();
            client = clientx.getLocalClient();
            
            //create a Session Manager object
             sMgr = client.newSessionManager();

            //create an IDfLoginInfo object named loginInfoObj
            loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser(DocumentumAdapter.s_username);
            loginInfoObj.setPassword(DocumentumAdapter.s_password);
            loginInfoObj.setDomain(null);
        
            //bind the Session Manager to the login info
            sMgr.setIdentity(DocumentumAdapter.s_docbase, loginInfoObj);
            System.out.println("Getting session to docbase.");
            dsession = sMgr.getSession(DocumentumAdapter.s_docbase);
            System.out.println("Setting dsession in session.");
            session.setAttribute("dsession",dsession);
            session.setAttribute("loginInfoObj",loginInfoObj);
            session.setAttribute("dsmgr",sMgr);
            session.setAttribute("client",client);
            session.setAttribute("clientx",clientx);
        }
        else
        {
            System.out.println("**** Re-using old dsession ****");
            dsession = (IDfSession) session.getAttribute("dsession");
            loginInfoObj = (IDfLoginInfo) session.getAttribute("loginInfoObj");
            sMgr = (IDfSessionManager) session.getAttribute("sMgr");
            client = (IDfClient) session.getAttribute("client");
            clientx = (IDfClientX) session.getAttribute("clientx");
        }
%>
<BODY BGCOLOR="#FFFFFF">
<%
        IDfId myId = clientx.getId("0b00007b800025d8"); //the BMC folder
        IDfSysObject startingProject = (IDfSysObject)dsession.getObject(myId);
     if( startingProject == null ) {
        System.out.println("Object can not be found.");
    } else {
        System.out.println("Object named " + startingProject.getObjectName() + " was found.");
//        System.out.println("Path is: " + startingProject.getPath(0));
    }

		String targetObject = null;
        IDfSysObject obj = null;
		if( request.getParameter("objId") != null )
		{
			targetObject = request.getParameter("objId");
            IDfId id = clientx.getId(targetObject);
            obj = (IDfSysObject)dsession.getObject(id);
		}
        else
        {
            obj = startingProject;
        }
        
        //set the value in the session to reverse the order for next time
		String parentId = null;

		if ( obj instanceof IDfFolder)
		{
System.out.println("Obj is a folder");
            IDfFolder targetProject = (IDfFolder) obj;
            if(obj.getRemoteId().equals(startingProject.getRemoteId()))
            {
System.out.println("On root");
   			   parentId = null;
            }
            else
            {
System.out.println("Trying to get parent");
               parentId = targetProject.getAncestorId(0);
            }

            TreeMap projects = new TreeMap();
            TreeMap files = new TreeMap();
//            IDfCollection contents = targetProject.getContents("object_name");
            IDfCollection contents = targetProject.getContents(null);
            while (contents.next())
            {
                String name = contents.getString("object_name");
                System.out.println("read object name: " + name);
                String oid = contents.getString("r_object_id");
                System.out.println("read object: " + oid);
                IDfId idid = clientx.getId(oid);
                IDfSysObject o = (IDfSysObject)dsession.getObject(idid);

                if (o instanceof IDfFolder)
                {
                    System.out.println("Adding as folder");
                    projects.put(name,(Object)o);
                }
                else
                {
                    System.out.println("Adding as file");
                    files.put(name,(Object)o);
                }
            }
            contents.close();
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
                <TR> Select Files to import from Documentum:</TR>
                <TR>
                    <TD CLASS="tableHeadingBasic" NOWRAP><IMG SRC="Images/openProj.gif" ALIGN="LEFT">Folder:&nbsp;<%=targetProject.getObjectName()%></TD>
<% if (parentId != null) { %>
                    <TD  CLASS="tableHeadingBasic" ALIGN="RIGHT" NOWRAP>
<A href="folderListing.jsp?objId=<%=URLEncoder.encode(parentId)%>"><IMG BORDER=0 SRC="Images/upProject.gif" BORDER="0"></A>
                    </TD>
<% } %>					
                </TR>
                </TABLE>

			<TABLE BGCOLOR=WHITE CELLSPACING=0 CELLPADDING=3 BORDER=0 WIDTH=100%>
			<%	
               Iterator iter = projects.values().iterator();
               while (iter.hasNext())
				{
                   IDfSysObject temp = (IDfSysObject) iter.next();
			%>
			<TR>
                        <TD WIDTH="65%" NOWRAP><A CLASS="standardHREF" border=0 href="folderListing.jsp?objId=<%=URLEncoder.encode(temp.getObjectId().toString())%>"><IMG BORDER=0 SRC="Images/Icon-Project.gif"></a> <%= temp.getObjectName() %></TD>
						<TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						<TD WIDTH="15%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						<TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
                        <TD WIDTH="15%" NOWRAP ALIGN="CENTER">
						</TD>
                        </TR>
			<%	} %>
			<%	
               iter = files.values().iterator();
               while (iter.hasNext())
				{
                   System.out.println("Pulling out from iter");
                   IDfSysObject sf = (IDfSysObject) iter.next();
                   String objId = sf.getObjectId().toString();
                   String objName = sf.getObjectName();
                   IDfId folderId = sf.getFolderId(0);
                   IDfFolder parentFolder = (IDfFolder) dsession.getObject(folderId);
                   String path = parentFolder.getFolderPath(0) + "/" + objName;
                   System.out.println("path=" + path);
                   System.out.println("objectId=" + objId);
                   System.out.println("objectName=" + objName);
			%>
			<TR><TD WIDTH="65%" NOWRAP><A CLASS="standardHREF" HREF="#" onClick="javascript:detail2Window = window.open('folderListing.jsp?objId=<%=URLEncoder.encode(objId)%>','detail2Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);><IMG BORDER=0 SRC="Images/Icon-File.gif" ALIGN="LEFT"></a><%= objName %></TD>
                         <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">&nbsp;</SPAN></TD>
						 <TD WIDTH="15%" NOWRAP ALIGN="RIGHT"><A CLASS="standardHREF" HREF="#" onClick="javascript:detail2Window = window.open('folderListing.jsp?objId=<%=URLEncoder.encode(objId)%>','detail2Window','resizable=yes,HEIGHT=350,WIDTH=450,scrollbars=yes');" return(false);>Details</A></TD>
						 <TD WIDTH="2.5%" NOWRAP><SPAN CLASS="standardText">|</SPAN></TD>
	                    <TD WIDTH="15%" NOWRAP ALIGN="LEFT"><A CLASS="standardHREF" HREF="tabFrame.jsp?type=f&path=<%=URLEncoder.encode(path)%>&mid=<%=URLEncoder.encode(objId)%>&status=<%=URLEncoder.encode(sf.getStatus())%>" TARGET="fileList">Add File</A></TD>
						</TR>
			<%	} %>
			</TABLE>

			</TD></TR></TABLE>
			
<br><br><a border=0 href="folderListing.jsp"><IMG BORDER=0 SRC="Images/goHome.gif"></a> Return to folder <B><%=startingProject.getObjectName()%></B>


<%
		}
		else
		{
            IDfSysObject targetFile = (IDfDocument ) obj;
//            parentId = targetFile.getAncestorId(0);
			%>
						<I>Properties of File</I> <B><%=targetFile.getObjectName()%></B><BR>
						<TABLE CELLSPACING=0 CELLPADDING=0 BORDER=1><TR><TD bgcolor=white>
						<TABLE CELLSPACING=0 CELLPADDING=3 BORDER=0>
							<TR><TD><B>ObjectId</B></TD><TD><%=targetFile.getObjectId()%></TD></TR>
							<TR><TD><B>State</B></TD><TD><%=targetFile.getStatus()%></TD></TR>
						</TABLE>
					</TD></TR></TABLE>
				<%
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
<%
}
catch (Exception e)
{
    System.out.println("Releasing session because of exception");
    if (sMgr!=null)
    {
        sMgr.release(dsession);
        session.setAttribute("dsession",null);
    }

    e.printStackTrace();
}
%>

