package com.globalsight.util2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.globalsight.entity.FileMapped;
import com.globalsight.entity.FileProfile;
import com.globalsight.entity.Job;
import com.globalsight.entity.User;
import com.globalsight.util.UsefulTools;

public class HtmlUtils
{
	static private final String HTML_HEADER_START = "<HTML xmlns:m='http://www.w3.org/1998/Math/MathML'>\r\n<HEAD>\r\n"
			+ "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />\r\n";

	static private final String HTML_TITLE_START = "<TITLE>";

	static private final String HTML_TITLE_END = "</TITLE>\r\n";

	static private final String HTML_STYLESHEET = "<STYLE>\r\n"
			+ "BODY {\r\n"
			+ "       background-color: white;\r\n"
			+ "       font-family: Arial Unicode MS, Arial, Helvetica, sans-serif;\r\n"
			+ "       font-size: x-small;\r\n" + "       margin: 0px;\r\n"
			+ "     }\r\n" + "\r\n"
			+ "/* To avoid automatic paragraph spaces: */\r\n"
			+ "P, UL, OL { margin-top: 0px; margin-bottom: 0px; }\r\n" + "\r\n"
			+ "A { color: blue; }\r\n" + "\r\n" + ".vconceptGrp\r\n"
			+ "              {\r\n"
			+ "                border: 1px solid white;\r\n"
			+ "              }\r\n" + "\r\n" + ".vlanguageGrp,\r\n"
			+ ".vsourceLanguageGrp,\r\n" + ".vtargetLanguageGrp\r\n"
			+ "              {\r\n" + "                margin-left: 10px;\r\n"
			+ "                margin-top: 0.1em;\r\n"
			+ "                border: 1px solid white;\r\n"
			+ "              }\r\n" + ".vtermGrp\r\n" + "              {\r\n"
			+ "                margin-left: 10px;\r\n"
			+ "                margin-top: 0.1em;\r\n"
			+ "                border: 1px solid white;\r\n"
			+ "              }\r\n" + ".vtransacGrp\r\n"
			+ "              {\r\n" + "                margin-left: 10px;\r\n"
			+ "                display: block;\r\n" + "              }\r\n"
			+ "\r\n" + ".vconceptlabel\r\n" + "              {\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                margin-right: 0.5ex;\r\n"
			+ "              }\r\n" + "\r\n" + ".vconcept\r\n"
			+ "              {\r\n" + "                font-weight: bold;\r\n"
			+ "              }\r\n" + "\r\n" + ".vlanguagelabel\r\n"
			+ "              {\r\n" + "                display: none;\r\n"
			+ "                margin-right: 0.5ex;\r\n"
			+ "              }\r\n" + "\r\n" + ".vlanguage\r\n"
			+ "              {\r\n" + "                font-weight: bold;\r\n"
			+ "                font-style: italic;\r\n"
			+ "                color: cadetblue;\r\n" + "              }\r\n"
			+ "\r\n" + ".vtermlabel\r\n" + "              {\r\n"
			+ "                display: none;\r\n"
			+ "                margin-right: 0.5ex;\r\n"
			+ "              }\r\n" + "\r\n"
			+ ".vsourceLanguageGrp .vsearchterm\r\n" + "              {\r\n"
			+ "                font-size: small;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                color: darkmagenta;\r\n" + "              }\r\n"
			+ "\r\n" + ".vtargetLanguageGrp .vsearchterm\r\n"
			+ "              {\r\n" + "                font-size: medium;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                color: blue;\r\n" + "              }\r\n"
			+ "\r\n" + ".vlanguageGrp .vsearchterm\r\n" + "              {\r\n"
			+ "                font-size: small;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                color: black;\r\n" + "              }\r\n"
			+ "\r\n" + ".vsourceLanguageGrp .vterm\r\n" + "              {\r\n"
			+ "                font-size: small;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                color: mediumorchid;\r\n"
			+ "              }\r\n" + "\r\n" + ".vtargetLanguageGrp .vterm\r\n"
			+ "              {\r\n" + "                font-size: medium;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                color: blue;\r\n" + "              }\r\n"
			+ "\r\n" + ".vlanguageGrp .vterm\r\n" + "              {\r\n"
			+ "                font-size: small;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                color: black;\r\n" + "              }\r\n"
			+ "\r\n" + ".vfakeConceptGrp\r\n" + "              {\r\n"
			+ "                display: block;\r\n" + "              }\r\n"
			+ ".vfakeTermGrp {\r\n"
			+ "                border: 1px solid white;\r\n"
			+ "              }\r\n" + "\r\n" + ".vfieldGrp\r\n"
			+ "              {\r\n" + "                margin-left: 10px;\r\n"
			+ "                border: 1px solid white;\r\n"
			+ "              }\r\n" + ".vfieldlabel\r\n"
			+ "              {\r\n" + "                font-weight: bold;\r\n"
			+ "                margin-right: 0.5ex;\r\n"
			+ "              }\r\n" + ".vfieldvalue\r\n"
			+ "              {\r\n"
			+ "                font-weight: medium;\r\n"
			+ "              }\r\n" + ".vtransaclabel\r\n"
			+ "              {\r\n"
			+ "                font-size: xx-small;\r\n"
			+ "                font-weight: bold;\r\n"
			+ "                margin-right: 0.5ex;\r\n"
			+ "              }\r\n" + ".vtransacvalue\r\n"
			+ "              {\r\n"
			+ "                font-size: xx-small;\r\n"
			+ "                font-style: italic;\r\n"
			+ "                color: gray;\r\n" + "              }\r\n"
			+ "</STYLE>\r\n";

	static private final String HTML_HEADER_END_BODY_START = "</HEAD>\r\n<BODY>\r\n";

	static private final String HTML_TABLE_START = "<TABLE>\r\n";

	static private final String HTML_HORIZONTAL_RULE = "<HR width='100%'/>\r\n";

	static private final String NA = "N/A***";

	static private final String HTML_BLANKS = "&nbsp;&nbsp;&nbsp;&nbsp;";

	static private final String HTML_TABLE_END = HTML_HORIZONTAL_RULE
			+ "<FONT SIZE=\"-1\">***Company name with N/A means this user "
			+ "has not logon GlobalSight successfully before.<BR />"
			+ "***other fields with N/A means that these fields were not recorded by"
			+ " previous version (before DesktopIcon V3.0)</FONT>\r\n";

	static private final String HTML_BODY_END = "</BODY>\r\n</HTML>\r\n";

	static public String getUserInforInHTML(User p_user)
	{
		StringBuffer c = new StringBuffer();
		c.append(HTML_HEADER_START).append(HTML_TITLE_START);
		c.append("User Information | ").append(p_user.toString());
		c.append(HTML_TITLE_END).append(HTML_STYLESHEET);
		c.append(HTML_HEADER_END_BODY_START).append(HTML_TABLE_START);
		c.append("<P><BR /><BR /></P>\r\n");
		addTD(c, "<H1> User Report </H1>\r\n");
		addTD(c, HTML_HORIZONTAL_RULE);
		c.append(showOneUserInHTML(p_user));
		addTD(c, HTML_TABLE_END);
		c.append(HTML_BODY_END);

		return c.toString();
	}

	private static void addTD(StringBuffer p_c, String data)
	{
		p_c.append("<TR>\r\n<TD>").append(HTML_BLANKS).append(
				"</TD>\r\n<TD width=1300>\r\n");
		p_c.append(data);
		p_c.append("</TD>\r\n</TR>\r\n");
	}

	private static String showOneUserInHTML(User p_user)
	{
		StringBuffer c = new StringBuffer();
		StringBuffer table = new StringBuffer();
		String temp = "";
		addTD(c, "<H3>" + p_user + "</H3>\r\n");
		// user infor
		table.append("<TABLE border=1>\r\n");
		table.append("<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("User Name").append("</TH>\r\n");
		table.append("<TD>").append(p_user.getName()).append("</TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("Host URL").append("</TH>\r\n");
		temp = "http://" + p_user.getHost().getName() + ":"
				+ p_user.getHost().getPort() + "/globalsight/wl";
		table.append("<TD><a href=\"").append(temp).append("\" target=_blank>")
				.append(p_user.getHost().getName()).append("</a></TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("Host Port").append("</TH>\r\n");
		table.append("<TD>").append(p_user.getHost().getPort()).append(
				"</TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("company name").append(
				"</TH>\r\n");
		temp = "".equals(p_user.getCompanyName()) ? NA : p_user
				.getCompanyName();
		table.append("<TD>").append(temp).append("</TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("Enable Downloads").append(
				"</TH>\r\n");
		table.append("<TD>").append(p_user.isAutoDownload())
				.append("</TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("Automatic Download Minutes")
				.append("</TH>\r\n");
		table.append("<TD>").append(p_user.getMinutes()).append("</TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("Save Path of downloaded Jobs")
				.append("</TH>\r\n");
		table.append("<TD>").append(p_user.getSavepath()).append("</TD>\r\n");
		table.append("</TR>\r\n<TR>\r\n");
		table.append("<TH ALIGN=LEFT>").append("Download Jobs From").append(
				"</TH>\r\n");
		List list = Arrays.asList(p_user.getDownloadUsers());
		temp = (list.isEmpty()) ? "&lt;no added user&gt;" : UsefulTools
				.listToString(list);
		table.append("<TD>").append(temp).append("</TD>\r\n");
		table.append("</TR>\r\n");
		table.append("</TABLE>\r\n");
		addTD(c, table.toString());
		table.delete(0, table.length());

		// job information
		addTD(c, HTML_BLANKS);
		addTD(c, "<H4>Jobs created</H4>\r\n");
		table.append("<TABLE border=1>\r\n");
		List jobs = new ArrayList();
		boolean isOk = true;
		try
		{
			jobs = ConfigureHelperV2.readJobsByUser(p_user);
		}
		catch (Exception e)
		{
			isOk = false;
			table.append("<TR><TD>A error occured when reading jobs "
					+ "from configure file</TD><TD>");
			StringWriter w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));
			table.append(w.getBuffer());
			table.append("</TD></TR>");
		}

		if (isOk && jobs.size() > 0)
		{
			table.append("<TR>\r\n");
			table.append("<TH ALIGN=LEFT>").append("Job Name").append(
					"</TH>\r\n");
			table.append("<TH ALIGN=LEFT>").append("Create Date").append(
					"</TH>\r\n");
			table.append("<TH ALIGN=LEFT>").append("Download Date").append(
					"</TH>\r\n");
			table.append("<TH ALIGN=LEFT>").append("Download By").append(
					"</TH>\r\n");
			table.append("<TH ALIGN=LEFT>").append("File Profiles and Files")
					.append("</TH>\r\n");
			table.append("</TR>\r\n<TR>\r\n");
			for (Iterator iter = jobs.iterator(); iter.hasNext();)
			{
				Job job = (Job) iter.next();
				table.append("<TR>\r\n");
				table.append("<TD ALIGN=LEFT>").append(job.getName()).append(
						"</TD>\r\n");
				table.append("<TD ALIGN=LEFT>").append(job.getCreateDate())
						.append("</TD>\r\n");
				temp = (job.getDownDate() == null) ? "In progress" : job
						.getDownDate().toString();
				table.append("<TD ALIGN=LEFT>").append(temp)
						.append("</TD>\r\n");
				table.append("<TD ALIGN=LEFT>").append(job.getDownloadUser())
						.append("</TD>\r\n");
				List fms = job.getFileMappedList();
				temp = "";
				for (Iterator iterator = fms.iterator(); iterator.hasNext();)
				{
					FileMapped fm = (FileMapped) iterator.next();
					FileProfile fp = fm.getFileProfile();
					if ("N/A".equalsIgnoreCase(fp.getName())
							|| "N\\A".equalsIgnoreCase(fp.getName()))
					{
						temp = NA;
						break;
					}
					else
					{
						temp = temp + fm.getFile().getAbsolutePath()
								+ "  fileprofile:&nbsp;&nbsp;" + fp.getName()
								+ "<BR />";
					}
				}
				table.append("<TD ALIGN=LEFT>").append(temp)
						.append("</TD>\r\n");
				table.append("</TR>\r\n<TR>\r\n");
			}
		}
		else if (isOk)
		{
			table.append("<TR><TD><B>No jobs for this user</B></TD></TR>");
		}

		table.append("</TABLE>\r\n");
		addTD(c, table.toString());
		return c.toString();
	}
}
