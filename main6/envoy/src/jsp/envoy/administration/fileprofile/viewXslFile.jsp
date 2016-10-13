<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.util.AmbFileStoragePathUtils,
    com.globalsight.everest.company.CompanyThreadLocal,
            java.io.*"
    session="true"
%>
<xmp>
<%
	String filePath = request.getParameter("filePath");
    String companyId = request.getParameter("companyId");
    CompanyThreadLocal.getInstance().setIdValue(companyId);
	String docRoot = AmbFileStoragePathUtils.getXslDir().getPath();
    String fullPath = docRoot + filePath;
    File file = new File(fullPath);
    BufferedReader input = null;
    
    try {
	     input =  new BufferedReader(new FileReader(file));
	     String line = null; 
	     while (( line = input.readLine()) != null){
	         out.println(line);
	     }

	    /*InputStream in = new FileInputStream(file);
	    BufferedInputStream bin = new BufferedInputStream(in);
	    DataInputStream din = new DataInputStream(bin);
	    while(din.available()>0)
	    {
	       out.println(din.readLine());
	    }
	    in.close();
	    bin.close();
	    din.close();*/

    }
    catch (IOException e){
        out.print("Failed to get XSL file for reason: " + e.getMessage());
    }
    finally {
        try{
        	if(input != null)input.close();
        } catch (Exception exp){}
    }
%>
</xmp>