/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.pagehandler.edit.online;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.machineTranslation.MTHelper2;
import com.globalsight.util.edit.EditUtil;

public class MTTranslationHandler extends PageActionHandler
{
    static private final Logger logger = Logger
            .getLogger(MTTranslationHandler.class);
    
    @ActionHandler(action = MTHelper2.ACTION_GET_MT_TRANSLATION_MESSAGE, formClass = "")
    public void getMtTranslationMessage(HttpServletRequest request,
            HttpServletResponse response, Object form) throws Exception
	{
		HttpSession session = request.getSession();
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		EditorState state = (EditorState) sessionMgr
				.getAttribute(WebAppConstants.EDITORSTATE);

		ResourceBundle bundle = PageHandler.getBundle(session);
		String lb_matchResults = bundle.getString("lb_mt_match_results");

		// style (LTR or RTL)
		boolean b_rtl = EditUtil.isRTLLocale(state.getTargetLocale());
		String style = "";
		if (b_rtl == true)
		{
			style = "style='direction: rtl; unicode-bidi: embed;' align='right'";
		}

		ServletOutputStream out = response.getOutputStream();
		try
		{
			Map mtTranslationMessage = MTHelper2.getMtTranslationMessage(state);

			// MT result
			String engine_name = "";
			if (mtTranslationMessage != null)
			{
				engine_name = (String) mtTranslationMessage
						.get(MTHelper2.ENGINE_NAME);
				if (engine_name == null)
				{
					engine_name = "";
				}
				else
				{
					engine_name = " : " + engine_name + "_MT";
				}
			}

			// DO NOT format these codes !!!
			StringBuffer result = new StringBuffer();
			result.append("<div id=\"").append(MTHelper2.MT_TRANSLATION_DIV)
					.append("\" style=\"display:none\">").append("</div>");
			// HR
			result.append("<HR style=\"position: relative; top: 0; left: 0;\" COLOR=\"#0C1476\" WIDTH=\"95%\">");
			// DIV start
			result.append("<DIV STYLE=\"position: relative; margin-left: 5px;overflow: hidden;\">");
			// Table One
			result.append("<TABLE WIDTH=\"100%\" CELLPADDING=\"0\" CELLSPACING=\"0\" BORDER=\"0\">");
			result.append("    <TR>");
			result.append("        <TD><IMG SRC=\"/globalsight/images/spacer.gif\" WIDTH=\"5\" HEIGHT=\"1\"></TD>");
			result.append("        <TD WIDTH=\"100%\" id=\"idCheckMT\">");
			result.append("            <SPAN CLASS=\"standardTextBold\">")
					.append(lb_matchResults).append(engine_name)
					.append("</SPAN>");
			result.append("        </TD>");
			result.append("<TD class=\"standardTextBold\" valign=\"TOP\" align=\"right\">");
			result.append("<SPAN class=\"link\" TITLE=\"Click to fetch MT translation\"  onclick=\"mtTranslation()\" oncontextmenu=\"mtTranslation()\">");
			result.append("<font style=\"text-decoration:underline;\">")
					.append("Translate").append("</font>").append("</SPAN>")
					.append("</TD>");
			result.append("    </TR>");
			result.append("<TR id=\"blankLine\"><TD height=\"20\"></TD><TD height=\"20\"></TD><TD height=\"20\"></TD></TR>");
			result.append("</TABLE>");
			// Table Two
			result.append("<TABLE CELLPADDING=\"3\" CELLSPACING=\"0\" BORDER=\"0\" WIDTH=\"100%\">");
			result.append("    <TR>");
			result.append("        <TD><IMG SRC=\"/globalsight/images/spacer.gif\" WIDTH=\"10\" HEIGHT=\"3\"></TD>");

			result.append("    <TD VALIGN=\"TOP\" ALIGN=\"LEFT\" WIDTH=\"100%\">");
			result.append("        <TABLE CELLPADDING=\"2\" CELLSPACING=\"0\" BORDER=\"0\" WIDTH=\"96%\">");
			result.append("            <TR VALIGN=TOP>");
			result.append("                <TD id=\"idMtContents\" ")
					.append(style)
					.append(" class=\"clickable\" onclick=\"doClick()\">")
					.append("</TD>");
			result.append("            </TR>");
			result.append("        </TABLE>");
			result.append("    </TD>");
			result.append("    </TR>");
			result.append("</TABLE>");
			// DIV end
			result.append("</DIV>");

			Map<String, Object> returnValue = new HashMap();
			returnValue.put("mtMatch", result.toString());
			out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
		}
		catch (Exception e)
		{
			String s = "({\"error\" : " + JsonUtil.toJson(e.getMessage())
					+ "})";
			out.write(s.getBytes("UTF-8"));
			logger.error(e.getMessage(), e);
		}
		finally
		{
			out.close();
			pageReturn();
		}
	}
    
	@ActionHandler(action = MTHelper2.ACTION_GET_MT_TRANSLATION, formClass = "")
	public void getMtTranslation(HttpServletRequest request,
			HttpServletResponse response, Object form) throws Exception
	{
		HttpSession session = request.getSession();
		SessionManager sessionMgr = (SessionManager) session
				.getAttribute(WebAppConstants.SESSION_MANAGER);
		EditorState state = (EditorState) sessionMgr
				.getAttribute(WebAppConstants.EDITORSTATE);
		Map<String, Object> returnValue = new HashMap();
		ResourceBundle bundle = PageHandler.getBundle(session);
		String lb_noMTSegments = bundle.getString("lb_no_mt_match_results");

		ServletOutputStream out = response.getOutputStream();
		Map mtTranslation = MTHelper2.getMtTranslationForSegEditor(state);
		String translatedString = "";
		String translatedString_replaced = "";
		if (mtTranslation != null)
		{
			translatedString = (String) mtTranslation
					.get(MTHelper2.MT_TRANSLATION);

			if (translatedString != null)
			{
				translatedString_replaced = translatedString.replaceAll("\n",
						"");
			}
			else
			{
				returnValue.put("translatedString", lb_noMTSegments);
			}
		}
		
		returnValue.put("translatedString_replaced",
				translatedString_replaced);
		returnValue.put("translatedString", translatedString);
		
		out.write((JsonUtil.toObjectJson(returnValue)).getBytes("UTF-8"));
		 out.close();
         pageReturn();
	}
    
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }

    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response)
    {

    }
}
