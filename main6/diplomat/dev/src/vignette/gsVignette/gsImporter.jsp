<%@ page contentType="text/html; charset=UTF-8"
        errorPage="error.jsp" 
        import="java.util.Enumeration, 
        com.vignette.cms.client.beans.*, 
        com.globalsight.vignette.*,
        java.net.URL,
        java.util.*, 
        java.io.*,
		java.sql.*"
        session="true"
%>
<jsp:useBean id="fileHandler" class="com.globalsight.vignette.FileHandler" scope="session"/>
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
</HEAD>
<BODY>
<%
	Properties connectionProp = (Properties)session.getAttribute("properties");

	String action = request.getParameter("action");
	if (action != null && action.equals("doImport"))
	{
		String jobName = (String) request.getParameter("jobname"); 
		String fileProfileId = (String) request.getParameter("fpid"); 
		String versionFlag = (String) request.getParameter("versionflag");
		
        String targetProjectMid = (String) session.getAttribute("trgBaseProjectMid"); 
        String sourceProjectMid = (String) session.getAttribute("srcBaseProjectMid");
        String mids = sourceProjectMid + "|" + targetProjectMid;
        
		String returnStatus = (String) request.getParameter("returnstatus");
		Enumeration items = fileHandler.getItems();
	    String sys4_host = connectionProp.getProperty("sys4_host");
		URL url = new URL(sys4_host+"/VignetteImportServlet"); 
		VignetteImportRequester vir = new VignetteImportRequester(url);
		vir.setImportData(items,
	                          fileProfileId, 
                              mids,
	                          jobName, 
	                          returnStatus, 
	                          versionFlag); 
	    vir.upload();

		//clean out the files
        fileHandler.emptyFileHandler();
		%>
		<br><br><br>
	<center>
		The import request has been posted to GlobalSight System4.<br>
		Click <a href="<%= (String) connectionProp.getProperty("main_ui_url")%>" target="_top"><b> here </b> </a>
		to return to the main page.
	</center>
<% }
   else
   {
    String dbuser = connectionProp.getProperty("dbuser");
    String dbpass = connectionProp.getProperty("dbpass");
    String driver = connectionProp.getProperty("driver");
	String jdbc = connectionProp.getProperty("jdbc");
    
   //let the user pick a file profile and other info
%>
<center><b>Select the business rules for importing <br> Vignette content into System4:</b></center>
<center>
  <TABLE CELLSPACING="2" CELLPADDING="0" BORDER="0">
          <FORM TYPE="POST" ACTION="gsImporter.jsp">
		  	<input type="hidden" name="action" value="doImport">
              <tr>
                <td>Job Name:</b></td>
                <td ALIGN="left">
                  <INPUT name="jobname" size="20" value="vignetteJob">
                </td>
              </tr>
              <tr>
                <td>System4 File Profile:</b></td>
                 <td ALIGN="left">
					<SELECT name="fpid" value="">
<%
   Statement query = null;
   ArrayList ids = new ArrayList();
   ArrayList names = new ArrayList();
   try {
	   Class.forName(driver).newInstance();
	   Connection connection = (Connection) DriverManager.getConnection(jdbc,dbuser,dbpass);
	   String sql = "select id, name from file_profile";
	   query = connection.createStatement();
	   ResultSet results = query.executeQuery(sql);
	   while (results.next()) {
		   ids.add(results.getString(1));
   		   names.add(results.getString(2));
	       }
   }
   finally
   {
       if (query != null) { query.close(); }
   }

   for (int i=0; i < ids.size(); i++) {
       String fpid = (String) ids.get(i);
       String fpname = (String) names.get(i);
%>
                            <option value="<%=fpid%>"> <%=fpname%></option>
<% } %>
                 </SELECT>
                             <input type="HIDDEN" name="versionflag" value="false"/>
                
                </td>
              </tr>
			  
			  <input type=hidden value="Ready to Launch" name="returnstatus">
<%--			  
              <tr>
                <td>Return Status:</b></td>
                <td ALIGN="left">
                <SELECT name="returnstatus" value="">
                  <option value="Ready to Launch" >Ready to Launch</option>
                  <option value="Working" >Working</option>
                  <option value="Live" >Live</option>
                  <option value="Expired" >Expired</option>
                  <option value="Unknown" >Unknown</option>
                  <option value="Ready for Internal Review" >Ready for Internal Review</option>
                  <option value="Ready for Final Review" >Ready for Final Review</option>
                   </SELECT>
                </td>
              </tr>
--%>
			  <tr>
                <td align="center">
                  <INPUT TYPE="submit" name="save" Value="Import">
                </td>
              </tr>
            </FORM>
            </TABLE>
			</center>
<% } %>	
</BODY>
</HTML>

