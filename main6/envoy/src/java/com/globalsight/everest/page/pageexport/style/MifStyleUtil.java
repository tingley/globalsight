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
package com.globalsight.everest.page.pageexport.style;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.pageexport.style.mif.ItalicStyle;
import com.globalsight.everest.page.pageexport.style.mif.SubscriptStyle;
import com.globalsight.everest.page.pageexport.style.mif.SuperscriptStyle;
import com.globalsight.everest.page.pageexport.style.mif.UnderlineStyle;
import com.globalsight.everest.page.pageexport.style.mif.BoldStyle;
import com.globalsight.everest.page.pageexport.style.mif.Style;
import com.globalsight.util.FileUtil;

/**
 * A util class that used to handle the mif and fm style tags like <java>b, i ,
 * u, sub and sup</java>.
 * 
 * @see StyleUtil
 */
public class MifStyleUtil extends StyleUtil 
{
	static private final Logger s_logger = Logger
            .getLogger(MifStyleUtil.class);
	
	private static String REGEX_BPT = "<String `([^']*\\[#gs-[^\\]]*][^']*\\[/#gs-[^\\]]*][^']*)'>";
	private static String ENMPTY_FONT = "\r\n<Font\r\n<FTag `'>\r\n<FLocked No>\r\n > # end of Font";
	
	private List<Style> styles = new ArrayList<Style>();
	
	/**
	 * Do nothing. You can get more information from <java>DocxStyleUtil.java.</java>
	 */
	@Override
	public String preHandle(String content) 
	{
		return content;
	}

	/**
	 * Do nothing. You can get more information from <java>DocxStyleUtil.java.</java>
	 */
	@Override
	public String sufHandle(String content) 
	{
		return content;
	}

	/**
	 * Before exporting, the content includes some tags that like [#gs-b] or
	 * others, we should handle it.
	 * 
	 * @param content
	 * @return
	 */
	public String updateStringBeforExport(String content)
	{
		Pattern p = Pattern.compile(REGEX_BPT);
		Matcher m = p.matcher(content);
        while (m.find())
        {
        	String s = m.group();
        	String sValue = m.group(1);
        	String font = getFont(content, m.group());
        	String newS = handleStyle(sValue, font);
        	
        	String endFont = "\r\n" + font;
        	if (font == null)
        	{
        		font = "";
        		endFont = ENMPTY_FONT; 
        	}
        		
        	content = content.replace(font + s, newS + endFont);
        	m = p.matcher(content);
        }
        
        content = content.replaceAll("\\[[/]{0,1}#gs-[^\\]]*\\]", "");
        
        return content;
	}
	
	/**
	 * Updates the font tags and let the style tags play.
	 */
	@Override
	public void updateBeforeExport(String filePath) 
	{
		try 
		{
			String content = FileUtil.readFile(new File(filePath), "utf-8");
			Pattern p = Pattern.compile(REGEX_BPT);
			Matcher m = p.matcher(content);
	        while (m.find())
	        {
	        	String sValue = m.group(1);
	        	String font = getFont(content, m.group());
	        	
	        	handleStyle(sValue, font);
	        }
		} 
		catch (IOException e) 
		{
			s_logger.error(e);
		}
	}
	
	/**
	 * Records the style to <java>styles</java> list.
	 * @param style
	 */
	private void handleStyle(String style)
	{
		for (Style s : getAllStyles())
		{
			s.handleStyle(style, styles);
		}
	}
	
	/**
	 * Gets the font that includes the style formats.
	 * 
	 * @param font
	 *            the original font.
	 * @return String. Updated font.
	 */
	private String getUpdatedFont(String font)
	{
		for (Style s : styles)
		{
			font = s.getUpdateFont(font);
		}
		
		return font;
	}
	
	/**
	 * Gets all styles class.
	 * 
	 * @return List<Style>
	 */
	private List<Style> getAllStyles()
    {
        List<Style> styles = new ArrayList<Style>();
        styles.add(new BoldStyle());
        styles.add(new ItalicStyle());
        styles.add(new UnderlineStyle());
        styles.add(new SuperscriptStyle());
        styles.add(new SubscriptStyle());
        return styles;
    }
	
	/**
	 * Lists all styles that included in the string.
	 * 
	 * @param sValue
	 *            the string that maybe include some styles.
	 * @param regex
	 *            the regex of the style
	 * @return List<String>. All styles found.
	 */
	private List<String> getStyles(String sValue, String regex)
	{
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sValue);
		List<String> style = new ArrayList<String>();
		
        while (m.find())
        {
        	style.add(m.group());
        }
        
        return style;
	}
	
	/**
	 * Handle style of the string. The style tag will be moved and the style
	 * format will be added.
	 * 
	 * @param sValue
	 *            the string that maybe includes some style tags.
	 * @param font
	 *            the original font format.
	 * @return String. Updated string.
	 */
	private String handleStyle(String sValue, String font)
	{
		StringBuffer sb = new StringBuffer();
		
		List<String> preStyle = getStyles(sValue, "\\[#gs-[^\\]]*\\]");
		List<String> subStyle = getStyles(sValue, "\\[/#gs-[^\\]]*\\]");
		
		String style = getNextStyle(sValue, preStyle, subStyle);
		String sufString = sValue;
		
		while (style != null)
		{
			int n = sufString.indexOf(style);
			String preString = sufString.substring(0, n);
			sufString = sufString.substring(n + style.length());
			if (preString.length() > 0)
			{
				String newFont = getUpdatedFont(font);
				
				if (sb.length() > 0)
					sb.append("\r\n");
				
				/*
				 * Some times, the font will affect the following content. in
				 * order to solve the problem. add a empty font there
				 */
				if (newFont == null)
				{
					newFont = ENMPTY_FONT;
				}
				
				sb.append(newFont);
				sb.append("\r\n<String `" + preString + "'>");
			}

			handleStyle(style);
			style = getNextStyle(sufString, preStyle, subStyle);
		}		
		
		if (sufString.length() > 0)
		{
			if (sb.length() > 0)
				sb.append("\r\n");
			String newFont = getUpdatedFont(font);
			
			/*
			 * Some times, the font will affect the following content. in
			 * order to solve the problem. add a empty font there
			 */
			if (newFont == null)
			{
				newFont = ENMPTY_FONT;
			}
			
			sb.append(newFont);
			sb.append("\r\n<String `" + sufString + "'>");
		}
		
		return sb.toString();
	}
	
	/**
	 * One segment may include several style tags. And those tags should be
	 * handled one by one. So after handling one style tag, we need the method
	 * to get the next style tag.
	 * 
	 * @param sValue
	 *            the segment.
	 * @param preStyle
	 *            the prefix tags, like <b>.
	 * @param subStyle
	 *            the suffix tags, like </b>.
	 * @return found style or null.
	 */
	private String getNextStyle(String sValue, List<String> preStyle, List<String> subStyle)
	{
		int n1 = -1;
		int n2 = -1;
		
		if (preStyle.size() > 0)
		{
			String s = preStyle.get(0);
			n1 = sValue.indexOf(s);
		}
		
		if (subStyle.size() > 0)
		{
			String s = subStyle.get(0);
			n2 = sValue.indexOf(s);
		}
		
		if (n1 != -1 && (n1 < n2 || n2 == -1))
		{
			return preStyle.remove(0);
		}
		
		if (n2 != -1 && (n2 < n1 || n1 == -1))
			return subStyle.remove(0);
		
		return null;
	}
	
	/**
	 * Gets the font if exist of the specified string.
	 * 
	 * @param s
	 *            the all content.
	 * @param c
	 *            the specified string.
	 * @return found font or null
	 */
	private String getFont(String s, String c)
	{
		int n = s.indexOf(c);
		if (n <= 0)
			return null;
		
		String s2 = s.substring(0, n).trim();
		
		if (s2.endsWith("> # end of Font"))
		{
			int n3 = s2.lastIndexOf("<Font ");
			return s.substring(n3, n);
		}
		
		return null;
	}
}
