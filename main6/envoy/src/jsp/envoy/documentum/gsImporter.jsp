<%@ page contentType="text/html; charset=UTF-8"
        import="java.util.Enumeration, 
        com.globalsight.vignette.*,
        java.net.URL,
        java.util.*, 
        java.io.*,
		java.sql.*,
        com.globalsight.cxe.util.CxeProxy"
        session="true"
%>
<jsp:useBean id="fileHandler" class="com.globalsight.vignette.FileHandler" scope="session"/>
<%
System.out.println("Bye World");
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
%>
<HTML>
<HEAD>
</HEAD>
<BODY>
<%
//System.out.println("loading propes");	
//InputStream is = application.getResourceAsStream("/properties/dctm.properties");
//System.out.println("got propes");	
//	Properties connectionProp = new Properties();
//	connectionProp.load(is);
//	is.close();

	String action = request.getParameter("action");
	if (action != null && action.equals("doImport"))
	{
		String jobName = (String) request.getParameter("jobname"); 
		String fileProfileId = (String) request.getParameter("fpid");
        String batchId = jobName + System.currentTimeMillis();
        ArrayList items = Collections.list(fileHandler.getItems());
        int pageNum = 0;
        Integer pageCount = new Integer (items.size());
        Iterator iter = items.iterator();
        while (iter.hasNext())
        {
            pageNum++;
            Item item = (Item) iter.next();
            String objectId = item.getMid();
            String path = item.getPath();
            String userId = "0001";
            CxeProxy.importFromDocumentum(objectId, path, jobName, batchId, fileProfileId, 
                    pageCount, new Integer(pageNum), pageCount, new Integer(pageNum), false, null, userId);
        }
        fileHandler.emptyFileHandler();
		%>
		<br><br><br>
	<center>
		The import request has been posted to GlobalSight.<br>
		Click <a href="/globalsight/envoy/documentum/tabFrame.jsp?type=f&status=emptyFileHandler"><b> here </b> </a>
        to go back.
	</center>
<% }
   else
   {
//    String dbuser = connectionProp.getProperty("dbuser");
//    String dbpass = connectionProp.getProperty("dbpass");
//    String driver = connectionProp.getProperty("driver");
//	String jdbc = connectionProp.getProperty("jdbc");
String driver="oracle.jdbc.driver.OracleDriver";
String jdbc="jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=majestix)(PORT=1521)))(CONNECT_DATA=(SID=gsor)(SERVER=DEDICATED)))";
String dbpass="gscom57";
String dbuser="dragade62";
    
   //let the user pick a file profile and other info
%>
<center><b>Select the business rules for importing <br> Documentum content into GlobalSight:</b></center>
<center>
  <TABLE CELLSPACING="2" CELLPADDING="0" BORDER="0">
          <FORM TYPE="POST" ACTION="gsImporter.jsp">
		  	<input type="hidden" name="action" value="doImport">
              <tr>
                <td>Job Name:</b></td>
                <td ALIGN="left">
                  <INPUT name="jobname" size="20" value="dctmJob">
                </td>
              </tr>
              <tr>
                <td>GlobalSight File Profile:</b></td>
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

