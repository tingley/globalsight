<%@ page contentType="text/html; charset=UTF-8"
		import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
              com.globalsight.everest.util.system.SystemConfiguration,
              com.globalsight.everest.util.system.SystemConfigParamNames,
              java.text.Collator,
              java.io.BufferedReader,
			  java.io.FileInputStream,
              java.io.FilenameFilter,
			  java.io.InputStreamReader,
              java.io.FileReader,
			  java.io.File" session="true" 
%>
<%!
class ReadMeFilter implements FilenameFilter
{
 public ReadMeFilter() {}
 
 public boolean accept(File dir, String name)
 {
  if (name.endsWith("ReadMe.txt"))
    return true;
  return false;
 }
}

/**
 * <p>This class used to compare two patch document names. 
 * The format of documents name is XXXXXXER+Integer. Others can'r rightly compare.
 * You can use it as follow.
 * 
 * <p> Collections.sort(array, new PathcComparator());
 */
public class PatchComparator implements Comparator {
	
	private final String FLAG = "ER";
	 
	/**
	 * <p>Compares two patch documents names and compare which need show at first.
	 * 
	 * @param o1
	 *     The name of first patch documents name. The really type is string.   
	 * @param o2
	 *     The name of second patch documents name. The really type is string.
	 *     
	 * @return 
	 *     If the first patch documents name need show at first, Returns a Positive integer. Others return a Negative Integer. 
	 */
	 public int compare(Object o1, Object o2) 
	 {		
		 String patch1 = (String) o1;
		 String patch2 = (String) o2;
			
		 ArrayList list1 = separate(patch1);
		 ArrayList list2 = separate(patch2);
		 int number1 = Integer.parseInt((String)list1.get(0));
		 int number2 = Integer.parseInt((String)list2.get(0));
			
		 if(number1 == number2) 
		 {
			 String str1 = (String)list1.get(1);
			 String str2 = (String)list2.get(1);		
				
			 return Collator.getInstance().compare(str1, str2);
		 }
			
		 return number1 - number2;
	}
	 
	private ArrayList separate(String s) {
			
		int index = s.lastIndexOf(FLAG);
		String usefulStr = s.substring(index+2);
		int i = usefulStr.length();
		while (i > 0) 
		{
			String numberPart = usefulStr.substring(0,i);
			if (isInteger(numberPart)) 
			{
				break;
			} 
			i--;
		}
			
		ArrayList parts = new ArrayList();
		parts.add(usefulStr.substring(0,i));
	    parts.add(usefulStr.substring(i));		
			
		return parts;
	}
		
	private boolean isInteger(String s) {
			
		boolean isInteger = true;
		try
		{			
			Integer.parseInt(s);			
		}
		catch (Exception e)
		{
			isInteger = false;
		}
			
		return isInteger;
	} 
}
%>   
<%
        //locale bundle labels
        ResourceBundle bundle = PageHandler.getBundle(session);
        String lbInstalledPatches = bundle.getString("lb_installed_patches");
        String lbNoPatchesInstalled = bundle.getString("lb_no_patches_installed");


	String aboutUrl = "about.jsp";
	String exception = null;
    ArrayList values = new ArrayList();
	try
        {        
		    SystemConfiguration sc = SystemConfiguration.getInstance();
            StringBuffer patchesDirectory = new StringBuffer(
                sc.getStringParameter(SystemConfigParamNames.INSTALLATION_DATA_DIRECTORY));
            patchesDirectory.append(File.separator);
            patchesDirectory.append("ERs");
            File fl = new File(patchesDirectory.toString());
            if (!fl.exists())
                fl.mkdirs();

            File[] files = fl.listFiles(new ReadMeFilter());

            for (int i=0; i<files.length; i++)
            {
/*			InputStreamReader isr = null;
			String line = null;
			FileReader fileReader = new FileReader(files[i]);
			BufferedReader rd = new BufferedReader(fileReader);

			if ((line = rd.readLine()) != null)
			{
				values[i] = line;
			}
			rd.close();			
            }       
*/			
				//no need to show the first line of the readme, just show the readme name
				//in alphabetical order
                File file = files[i];
                String filename = file.getName();
				int idx = filename.indexOf("_ReadMe.txt");
                values.add(filename.substring(0,idx));
			}
            Collections.sort(values, new PatchComparator());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            exception = e.getMessage();
        }
%>

<HTML>
<HEAD>
<TITLE><%= lbInstalledPatches %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>

</HEAD>
<BODY BRCOLOR="#FFFFFF">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 20px; LEFT: 20px;">
<SPAN CLASS="mainHeading"><%= lbInstalledPatches %></SPAN>
<P>
<SPAN CLASS="standardText">
<!-- PATCH LIST START -->
<%
	if (values.size() > 0)
	{
        out.println("<ol>");
		for (int i=0; i< values.size(); i++)
		{
			out.print("<li>");
			out.print(values.get(i));
			out.println("</li>");
		}
        out.println("</ol>");
	}
	else
	{
		String val = exception == null ? lbNoPatchesInstalled : exception;
		out.println(val);
	}
%>
<!-- PATCH LIST END -->
<P>
<INPUT TYPE="BUTTON" NAME="OK" VALUE="OK" ONCLICK="location.replace('<%=aboutUrl%>')"> 
</DIV>
</BODY>
</HTML>
