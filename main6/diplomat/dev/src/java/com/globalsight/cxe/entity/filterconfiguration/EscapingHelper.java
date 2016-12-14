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

package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.comparator.PriorityComparator;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.TagIndex;

public class EscapingHelper
{
	private static final Logger CATEGORY = Logger
			.getLogger(EscapingHelper.class);
	private static XmlEntities m_xmlEncoder = new XmlEntities();
	private static String statRegexStr = "<bpt([\\s\\S]*)><([\\s\\S]*)></bpt>";
	private static String endRegexStr = "<ept([\\s\\S]*)></([\\s\\S]*)></ept>";

	public static String handleString4Export(String oriStr, List<Escaping> es,
			String format, boolean noTag, boolean doDecode, String escapingChars)
	{
		return handleString4Export(oriStr, es, format, noTag, doDecode,
				escapingChars, false);
	}

	public static String handleString4Export(String oriStr, List<Escaping> es,
			String format, boolean noTag, boolean doDecode,
			String escapingChars, boolean isInCDATA)
	{
		if (oriStr == null || oriStr.length() == 0)
			return oriStr;

		if (es == null || es.size() == 0)
			return oriStr;

		// To be safe, do not escape anything for office 2010.
		if (IFormatNames.FORMAT_OFFICE_XML.equalsIgnoreCase(format))
		{
			return oriStr;
		}

		StringBuffer sb = new StringBuffer();

		List<TagIndex> tags = null;

		if (noTag)
		{
			TagIndex ti = TagIndex.createTagIndex(oriStr, false, 0,
					oriStr.length());
			tags = new ArrayList<TagIndex>();
			tags.add(ti);
		}
		else
		{
			tags = TagIndex.getContentIndexes(oriStr, false);
		}

		int count = tags.size();
		for (int i = 0; i < count; i++)
		{
			TagIndex ti = tags.get(i);
			if (ti.isTag)
			{
				if (IFormatNames.FORMAT_XML.equals(format) && isInCDATA)
				{
					// Escape tag content is dangerous...
					sb.append(handleTagContent4Export(ti.content, es, doDecode,
							format, escapingChars));
				}
				else
				{
					sb.append(ti.content);
				}
			}
			else
			{
				sb.append(handleString4Export(ti.content, es, doDecode, format,
						escapingChars));
			}
		}

		return sb.toString();
	}

	/**
	 * The text node value in tag is also need escape handling. i.e. <bpt i="2"
	 * type="font" x="2">&lt;font color=\&apos;#0063AD\&apos;&gt;</bpt>
	 * 
	 * Also supports sub segments.
	 */
	private static String handleTagContent4Export(String content,
			List<Escaping> es, boolean doDecode, String format,
			String escapingChars)
	{
		StringBuffer sub = new StringBuffer();
		List<String> splits = new ArrayList<String>();
		splitContent(content, splits);
		while (splits.size() == 3)
		{
			sub.append(splits.get(0));
			sub.append(handleString4Export(splits.get(1), es, doDecode, format,
					escapingChars));
			splitContent(splits.get(2), splits);
		}
		// splits.size == 1 or 2
		for (String str : splits)
		{
			sub.append(str);
		}

		return sub.toString();
	}

	private static void splitContent(String content, List<String> splits)
	{
		splits.clear();
		int index = content.indexOf(">");
		if (index > -1)
		{
			splits.add(content.substring(0, index + 1));
			String rest = content.substring(index + 1);
			index = rest.indexOf("<");
			if (index > -1)
			{
				String textNodeStr = rest.substring(0, index);
				splits.add(textNodeStr);
				splits.add(rest.substring(index));
			}
			else
			{
				splits.add(rest);
			}
		}
		else
		{
			splits.add(content);
		}
	}

	private static String handleString4Export(String ccc, List<Escaping> es,
			boolean doDecode, String format, String escapingChars)
	{
		StringBuffer sub = new StringBuffer();
		String preProcessed = null;
		String processed = null;
		ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;
		int length = ccc.length();
		for (int j = 0; j < length; j++)
		{
			char char1 = ccc.charAt(j);
			char char2 = (j + 1 < length) ? ccc.charAt(j + 1) : 'X';
			char char3 = (j + 2 < length) ? ccc.charAt(j + 2) : ' ';

			processed = handleChar4Export(es, sub.toString(), char1, char2,
					char3, format, escapingChars);
			// avoid double escape like "\\'".
			if ("\\".equals(preProcessed) && !"\\".equals(processed)
					&& processed.startsWith("\\"))
			{
				sub.append(char1);
			}
			else
			{
				sub.append(processed);
			}
			preProcessed = processed;
		}

		String subStr = doDecode ? m_xmlEncoder.encodeStringBasic(sub
				.toString()) : sub.toString();
		return subStr;
	}

	public static void handleOutput4Import(Output p_output, Filter mFilter)
	{
		if (mFilter == null || p_output == null)
		{
			return;
		}

		// get base filter (internal text filer) for main filter
		BaseFilter bf = null;
		if (mFilter instanceof BaseFilter)
		{
			bf = (BaseFilter) mFilter;
		}
		else
		{
			long filterId = mFilter.getId();
			String filterTableName = mFilter.getFilterTableName();
			bf = BaseFilterManager.getBaseFilterByMapping(filterId,
					filterTableName);
		}

		if (bf == null)
		{
			return;
		}

		// get internal texts
		List<Escaping> es = null;
		try
		{
			es = BaseFilterManager.getEscapings(bf);
			SortUtil.sort(es, new PriorityComparator());
		}
		catch (Exception e)
		{
			CATEGORY.error("Get escaping setting failed. ", e);
		}
		if (es == null || es.size() == 0)
		{
			return;
		}

		String format = p_output.getDataFormat();
		// handle internal text by segment
		for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
		{
			DocumentElement de = (DocumentElement) it.next();

			switch (de.type())
			{
				case DocumentElement.TRANSLATABLE:
				{
					TranslatableElement elem = (TranslatableElement) de;
					ArrayList segments = elem.getSegments();
					List<Character> processedChars = new ArrayList<Character>();

					if (segments != null && !segments.isEmpty())
					{
						for (Object object : segments)
						{
							SegmentNode snode = (SegmentNode) object;
							String segment = snode.getSegment();
							// protect internal text to avoid escape
							List<String> internalTexts = new ArrayList<String>();
							segment = InternalTextHelper.protectInternalTexts(
									segment, internalTexts);

							String result = handleString4Import(segment, es,
									format, false, processedChars);
							result = InternalTextHelper.restoreInternalTexts(
									result, internalTexts);
							snode.setSegment(result);
						}
					}

					if (processedChars != null && processedChars.size() > 0)
					{
						StringBuffer sb = new StringBuffer();
						for (Character ccc : processedChars)
						{
							sb.append(ccc);
						}

						elem.setEscapingChars(sb.toString());
					}

					break;
				}

				default:
					// skip all others
					break;
			}
		}
	}

	public static String handleString4Import(String oriStr, List<Escaping> es,
			String format, boolean isPureText, List<Character> processedChars)
	{
		if (oriStr == null || oriStr.length() <= 1)
			return oriStr;

		if (es == null || es.size() == 0)
			return oriStr;

		boolean doDecode = !isPureText;
		StringBuffer sb = new StringBuffer();
		List<TagIndex> tags = TagIndex.getContentIndexes(oriStr, isPureText);

		int count = tags.size();
		for (int i = 0; i < count; i++)
		{
			TagIndex ti = tags.get(i);

			if (ti.isTag)
			{
				if (isPureText)
				{
					String ccc = ti.content;
					String subStr = handleString4Import(ccc, es, doDecode,
							format, processedChars);
					sb.append(subStr);
				}
				else
				{
					sb.append(ti.content);
				}
			}
			else
			{
				if (isPureText)
				{
					sb.append(ti.content);
				}
				else
				{
					String ccc = ti.content;
					String subStr = handleString4Import(ccc, es, doDecode,
							format, processedChars);
					sb.append(subStr);
				}
			}
		}

		return sb.toString();
	}

	private static String handleString4Import(String ccc, List<Escaping> es,
			boolean doDecode, String format, List<Character> processedChars)
	{
		StringBuffer sub = new StringBuffer();
		ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;

		int length = ccc.length() - 1;
		// only 1 char, do not need to do un-escape
		if (length == 0)
		{
			sub.append(ccc);
		}
		else
		{
			int j = 0;
			for (; j < length; j++)
			{
				char char1 = ccc.charAt(j);
				char char2 = ccc.charAt(j + 1);
				char char3 = (j + 2 <= length) ? ccc.charAt(j + 2) : ' ';

				boolean processed = handleChar4Import(es, char1, char2);

				if (!processed)
				{
					sub.append(char1);
				}
				else
				{
					if (isSpecialFormat(format) && char1 == '\\'
							&& char2 == '\\')
					{
						sub.append(char1);
						j = j + 1;

						if (char3 != ' ' && processedChars != null
								&& !sub.toString().endsWith("\\\\")
								&& !processedChars.contains(char3))
						{
							processedChars.add(char3);
						}
					}
				}

				if (j == length - 1)
				{
					sub.append(char2);
				}
			}
		}

		String subStr = sub.toString();
		String result = doDecode ? m_xmlEncoder.encodeStringBasic(subStr)
				: subStr;
		return result;
	}

	private static boolean handleChar4Import(List<Escaping> es, char char1,
			char char2)
	{
		if (char1 == '\\')
		{
			for (Escaping escaping : es)
			{
				if (!escaping.isUnEscapeOnImport())
				{
					continue;
				}

				if ((char2 + "").equals(escaping.getCharacter()))
				{
					return true;
				}
			}
		}

		return false;
	}

	private static String handleChar4Export(List<Escaping> es, String before,
			char char1, char char2, char char3, String format,
			String escapingChars)
	{
		for (Escaping escaping : es)
		{
			if (!escaping.isReEscapeOnExport())
			{
				continue;
			}

			if ((char1 + "").equals(escaping.getCharacter()))
			{
				// process special chars in special format
				if (isSpecialFormat(format) && char1 == '\\')
				{
					if (escapingChars != null
							&& escapingChars.contains("" + char2))
					{
						return "\\" + char1;
					}

					if ("/:?*<>|\"".contains("" + char2))
					{
						return "" + char1;
					}

					if ("nrt".contains("" + char2))
					{
						// \\\\netapp\\HR
						if (before != null && before.endsWith("\\\\"))
						{
							return "\\" + char1;
						}

						// \\netapp\\HR
						if (Character.isLetter(char3))
						{
							return "\\" + char1;
						}

						return "" + char1;
					}
					else
					{
						return "\\" + char1;
					}
				}

				return "\\" + char1;
			}
		}

		return "" + char1;
	}

	private static boolean isSpecialFormat(String format)
	{
		return IFormatNames.FORMAT_JAVAPROP.equals(format)
				|| IFormatNames.FORMAT_HTML.equals(format)
				|| IFormatNames.FORMAT_PLAINTEXT.equals(format);
	}

	public static void newHandleOutput4Import(Output p_output, Filter mFilter)
	{
		if (mFilter == null || p_output == null)
		{
			return;
		}
		// get base filter (internal text filer) for main filter
		BaseFilter bf = null;
		if (mFilter instanceof BaseFilter)
		{
			bf = (BaseFilter) mFilter;
		}
		else
		{
			long filterId = mFilter.getId();
			String filterTableName = mFilter.getFilterTableName();
			bf = BaseFilterManager.getBaseFilterByMapping(filterId,
					filterTableName);
		}

		if (bf == null)
		{
			return;
		}

		// get internal texts
		List<Escaping> es = null;
		try
		{
			es = BaseFilterManager.getEscapings(bf);
			SortUtil.sort(es, new PriorityComparator());
		}
		catch (Exception e)
		{
			CATEGORY.error("Get escaping setting failed. ", e);
		}
		if (es == null || es.size() == 0)
		{
			return;
		}
		boolean isAssociateHtmlFilter = checkAssociatedHtmlFilter(mFilter);
		boolean isInCDATA = false;
		boolean isAttr = false;
		String format = p_output.getDataFormat();
		// handle internal text by segment
		for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
		{
			DocumentElement de = (DocumentElement) it.next();

			switch (de.type())
			{
				case DocumentElement.TRANSLATABLE:
				{
					TranslatableElement elem = (TranslatableElement) de;
					ArrayList segments = elem.getSegments();
					List<Character> processedChars = new ArrayList<Character>();

					if (segments != null && !segments.isEmpty())
					{
						for (Object object : segments)
						{
							SegmentNode snode = (SegmentNode) object;
							String segment = snode.getSegment();
							// protect internal text to avoid escape
							List<String> internalTexts = new ArrayList<String>();
							segment = InternalTextHelper.protectInternalTexts(
									segment, internalTexts);
							String contentType = getContentType(segment,
									format, isAttr, isInCDATA);
							String result = "";
							if (isAssociateHtmlFilter)
							{
								result = getEscapeString(segment, es, format,
										processedChars, contentType);
							}
							else
							{
								result = newHandleString4Import(segment, es,
										format, false, processedChars,
										contentType);
							}

							// String result = handleString4Import(segment, es,
							// format, false, processedChars);

							result = InternalTextHelper.restoreInternalTexts(
									result, internalTexts);
							snode.setSegment(result);
						}
					}

					if (processedChars != null && processedChars.size() > 0)
					{
						StringBuffer sb = new StringBuffer();
						for (Character ccc : processedChars)
						{
							sb.append(ccc);
						}

						elem.setEscapingChars(sb.toString());
					}

					break;
				}
				case DocumentElement.SKELETON:
				{
					String tmp = m_xmlEncoder
							.decodeStringBasic(((SkeletonElement) de)
									.getSkeleton());
					if (tmp.indexOf("<![CDATA[") > -1
							&& tmp.indexOf("]]") == -1)
					{
						isInCDATA = true;
					}
					if (isInCDATA && tmp.indexOf("]]") > -1)
					{
						isInCDATA = false;
					}
					if (tmp.indexOf("<![CDATA[") > -1 && tmp.indexOf("]]") > -1)
					{
						isInCDATA = tmp.indexOf("<![CDATA[") > tmp
								.indexOf("]]");
					}

					// attribute end
					if (isAttr && (tmp.startsWith("\"") || tmp.startsWith("'")))
					{
						isAttr = false;
					}

					// attribute start
					if (!isAttr
							&& tmp.matches("(?s).*?[a-zA-Z]+[\\s]*=[\\s]*[\"']$"))
					{
						isAttr = true;
					}
				}
				default:
					// skip all others
					break;
			}
		}
	}

	public static String newHandleString4Export(String oriStr,
			List<Escaping> es, String format, boolean noTag, boolean doDecode,
			String escapingChars, boolean isInCDATA, String contentType,
			boolean isAssociateHtmlFilter)
	{
		if (oriStr == null || oriStr.length() == 0)
			return oriStr;

		if (es == null || es.size() == 0)
			return oriStr;

		// To be safe, do not escape anything for office 2010.
		if (IFormatNames.FORMAT_OFFICE_XML.equalsIgnoreCase(format))
		{
			return oriStr;
		}

		StringBuffer sb = new StringBuffer();
		List<TagIndex> tags = null;
		if (noTag)
		{
			TagIndex ti = TagIndex.createTagIndex(oriStr, false, 0,
					oriStr.length());
			tags = new ArrayList<TagIndex>();
			tags.add(ti);
		}
		else
		{
			tags = TagIndex.getContentIndexes(oriStr, false);
		}

		int count = tags.size();
		if (count > 2)
		{
			sb.append(getSpecialContentForExport(tags, es, format, doDecode,
					escapingChars, contentType, isInCDATA));
		}
		else
		{
			for (int i = 0; i < count; i++)
			{
				TagIndex ti = tags.get(i);
				if (ti.isTag)
				{
					if (IFormatNames.FORMAT_XML.equals(format) && isInCDATA)
					{
						// Escape tag content is dangerous...
						sb.append(newHandleTagContent4Export(ti.content, es,
								doDecode, format, escapingChars, contentType));
					}
					else
					{
						sb.append(ti.content);
					}
				}
				else
				{
					if (checkHtmlNode(tags, i, isAssociateHtmlFilter)
							&& StringUtil.isEmpty(contentType))
					{
						sb.append(newHandleString4Export(ti.content, es, doDecode,
								format, escapingChars, "HtmlNode"));
					}
					else
					{
						sb.append(newHandleString4Export(ti.content, es, doDecode,
								format, escapingChars, contentType));
					}
				}
			}
		}

		return sb.toString();
	}

	private static String newHandleTagContent4Export(String content,
			List<Escaping> es, boolean doDecode, String format,
			String escapingChars, String contentType)
	{
		StringBuffer sub = new StringBuffer();
		List<String> splits = new ArrayList<String>();
		splitContent(content, splits);
		while (splits.size() == 3)
		{
			sub.append(splits.get(0));
			sub.append(newHandleString4Export(splits.get(1), es, doDecode,
					format, escapingChars, contentType));
			splitContent(splits.get(2), splits);
		}
		// splits.size == 1 or 2
		for (String str : splits)
		{
			sub.append(str);
		}

		return sub.toString();
	}

	private static String newHandleString4Export(String ccc, List<Escaping> es,
			boolean doDecode, String format, String escapingChars,
			String contenType)
	{
		StringBuffer sub = new StringBuffer();
		ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;
		for (Escaping escaping : es)
		{
			if (escaping.isCheckActive())
			{
				if (escaping.getActiveValue().equalsIgnoreCase("active"))
				{
					ccc = checkActiveForExport(ccc, escaping, format,
							escapingChars, contenType);
				}
				else if (escaping.getActiveValue().equalsIgnoreCase("inactive"))
				{
					ccc = checkInActiveForExport(ccc, escaping, format,
							escapingChars, contenType);
				}
			}
			else
			{
				ccc = checkActiveHandleChar4Export(ccc, escaping, format,
						escapingChars);
			}
		}
		sub.append(ccc);
		String subStr = doDecode ? m_xmlEncoder.encodeStringBasic(sub
				.toString()) : sub.toString();
		return subStr;
	}

	private static String checkActiveForExport(String ccc, Escaping escaping,
			String format, String escapingChars, String contentType)
	{
		if ((escaping.getPartConentValue().equalsIgnoreCase("cdata") && contentType
				.equalsIgnoreCase("CDATA"))
				|| (escaping.getPartConentValue().equalsIgnoreCase(
						"htmlXmlNode") && (contentType
						.equalsIgnoreCase("HtmlNode") || contentType
						.equalsIgnoreCase("XmlNode")))
				|| (escaping.getPartConentValue().equalsIgnoreCase(
						"xmlAttribute") && contentType
						.equalsIgnoreCase("xmlAttribute")))
		{
			ccc = checkActiveHandleChar4Export(ccc, escaping, format,
					escapingChars);
		}
		else if (escaping.getPartConentValue()
				.equalsIgnoreCase("startFinishes"))
		{
			boolean startIsRegex = escaping.isStartIsRegex();
			boolean finishIsRegex = escaping.isFinishIsRegex();
			String startStr = escaping.getStartPattern();
			String finishStr = escaping.getFinishPattern();
			List<String> contentList = extractOneLine(ccc, startStr, finishStr,
					startIsRegex, finishIsRegex);
			StringBuffer buffer = new StringBuffer();
			for (String con : contentList)
			{
				if (con.startsWith("match||"))
				{
					buffer.append(checkActiveHandleChar4Export(
							con.substring(("match||").length(), con.length()),
							escaping, format, escapingChars));
				}
				else if (con.startsWith("notmatch||"))
				{
					buffer.append(con.substring(("notmatch||").length(),
							con.length()));
				}
			}
			ccc = buffer.toString();
		}

		return ccc;
	}

	private static String checkInActiveForExport(String ccc, Escaping escaping,
			String format, String escapingChars, String contentType)
	{
		String returnStr = null;
		if ((escaping.getPartConentValue().equalsIgnoreCase("cdata") && contentType
				.equalsIgnoreCase("CDATA"))
				|| (escaping.getPartConentValue().equalsIgnoreCase(
						"htmlXmlNode") && (contentType
						.equalsIgnoreCase("HtmlNode") || contentType
						.equalsIgnoreCase("XmlNode")))
				|| (escaping.getPartConentValue().equalsIgnoreCase(
						"xmlAttribute") && contentType
						.equalsIgnoreCase("xmlAttribute")))
		{
			returnStr = ccc;
		}
		else if (escaping.getPartConentValue()
				.equalsIgnoreCase("startFinishes"))
		{
			boolean startIsRegex = escaping.isStartIsRegex();
			boolean finishIsRegex = escaping.isFinishIsRegex();
			String startStr = escaping.getStartPattern();
			String finishStr = escaping.getFinishPattern();
			List<String> contentList = extractOneLine(ccc, startStr, finishStr,
					startIsRegex, finishIsRegex);
			StringBuffer buffer = new StringBuffer();
			for (String con : contentList)
			{
				if (con.startsWith("match||"))
				{
					buffer.append(con.substring(("match||").length(),
							con.length()));
				}
				else if (con.startsWith("notmatch||"))
				{
					buffer.append(checkActiveHandleChar4Export(con.substring(
							("notmatch||").length(), con.length()), escaping,
							format, escapingChars));
				}
			}
			ccc = buffer.toString();
		}
		else
		{
			returnStr = checkActiveHandleChar4Export(ccc, escaping, format,
					escapingChars);
		}

		if (returnStr == null)
			returnStr = ccc;

		return returnStr;
	}

	private static String checkActiveHandleChar4Export(String ccc,
			Escaping escaping, String format, String escapingChars)
	{
		StringBuffer sub = new StringBuffer();
		String processed = null;
		String preProcessed = null;
		int length = ccc.length();
		for (int j = 0; j < length; j++)
		{
			char char1 = ccc.charAt(j);
			char char2 = (j + 1 < length) ? ccc.charAt(j + 1) : 'X';
			char char3 = (j + 2 < length) ? ccc.charAt(j + 2) : ' ';

			processed = newHandleChar4Export(escaping, sub.toString(), char1,
					char2, char3, format, escapingChars);
			if ("\\".equals(preProcessed) && !"\\".equals(processed)
					&& processed.startsWith("\\"))
			{
				sub.append(char1);
			}
			else
			{
				sub.append(processed);
			}
			preProcessed = processed;
		}

		return sub.toString();
	}

	private static String newHandleChar4Export(Escaping escaping,
			String before, char char1, char char2, char char3, String format,
			String escapingChars)
	{
		if (!escaping.isReEscapeOnExport())
		{
			return "" + char1;
		}
		// if char1 equals escaping.getCharacter()
		if ((char1 + "").equals(escaping.getCharacter()))
		{
			// process special chars in special format
			if (isSpecialFormat(format) && char1 == '\\')
			{
				if (escapingChars != null && escapingChars.contains("" + char2))
				{
					return "\\" + char1;
				}

				if ("/:?*<>|\"".contains("" + char2))
				{
					return "" + char1;
				}

				if ("nrt".contains("" + char2))
				{
					// \\\\netapp\\HR
					if (before != null && before.endsWith("\\\\"))
					{
						return "\\" + char1;
					}

					// \\netapp\\HR
					if (Character.isLetter(char3))
					{
						return "\\" + char1;
					}

					return "" + char1;
				}
				else
				{
					return escaping.getEscape() + char1;
				}
			}
			// process ordinary chars
			return escaping.getEscape() + char1;
		}

		// if char1 no equals escaping.getCharacter()
		return "" + char1;
	}

	private static String newHandleString4Import(String oriStr,
			List<Escaping> es, String format, boolean isPureText,
			List<Character> processedChars, String contentType)
	{
		if (oriStr == null || oriStr.length() <= 1)
			return oriStr;

		if (es == null || es.size() == 0)
			return oriStr;

		boolean doDecode = !isPureText;
		StringBuffer sb = new StringBuffer();
		List<TagIndex> tags = TagIndex.getContentIndexes(oriStr, isPureText);
		int count = tags.size();
		if (count > 2 && !isPureText)
		{
			sb.append(getSpecialContentForImport(tags, es, format, isPureText,
					processedChars, contentType));
		}
		else
		{
			for (int i = 0; i < count; i++)
			{
				TagIndex ti = tags.get(i);
				
				if (ti.isTag)
				{
					if (isPureText)
					{
						String ccc = ti.content;
						String subStr = newHandleString4Import(ccc, es, doDecode,
								format, processedChars, contentType);
						sb.append(subStr);
					}
					else
					{
						sb.append(ti.content);
					}
				}
				else
				{
					if (isPureText)
					{
						sb.append(ti.content);
					}
					else
					{
						String ccc = ti.content;
						String subStr = newHandleString4Import(ccc, es, doDecode,
								format, processedChars, contentType);
						sb.append(subStr);
					}
				}
			}
		}
		return sb.toString();
	}

	private static String newHandleString4Import(String ccc, List<Escaping> es,
			boolean doDecode, String format, List<Character> processedChars,
			String contentType)
	{
		StringBuffer sub = new StringBuffer();
		ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;
		int length = ccc.length() - 1;
		// only 1 char, do not need to do un-escape
		if (length == 0)
		{
			sub.append(ccc);
		}
		else
		{
			// es xun huan fang zai wai mian shi wei le kao lv
			// "start/finish regex"
			for (Escaping escaping : es)
			{
				if (escaping.isCheckActive())
				{
					if (escaping.getActiveValue().equalsIgnoreCase("active"))
					{
						ccc = checkActiveForImport(ccc, escaping, format,
								processedChars, contentType);
					}
					else if (escaping.getActiveValue().equalsIgnoreCase(
							"inactive"))
					{
						ccc = checkInActiveForImport(ccc, escaping, format,
								processedChars, contentType);
					}
				}
				else
				{
					ccc = noCheckActiveForImport(ccc, escaping, format,
							processedChars);
				}
			}
			sub.append(ccc);
		}

		String subStr = doDecode ? m_xmlEncoder.encodeStringBasic(sub
				.toString()) : sub.toString();
		return subStr;
	}

	private static String checkActiveForImport(String ccc, Escaping escaping,
			String format, List<Character> processedChars, String contentType)
	{
		if ((escaping.getPartConentValue().equalsIgnoreCase("cdata") && contentType
				.equalsIgnoreCase("CDATA"))
				|| (escaping.getPartConentValue().equalsIgnoreCase(
						"htmlXmlNode") && (contentType
						.equalsIgnoreCase("HtmlNode") || contentType
						.equalsIgnoreCase("XmlNode")))
				|| (escaping.getPartConentValue().equalsIgnoreCase(
						"xmlAttribute") && contentType
						.equalsIgnoreCase("xmlAttribute")))
		{
			ccc = checkActiveHandleChar4Import(ccc, escaping, format,
					processedChars);
		}
		else if (escaping.getPartConentValue()
				.equalsIgnoreCase("startFinishes"))
		{
			boolean startIsRegex = escaping.isStartIsRegex();
			boolean finishIsRegex = escaping.isFinishIsRegex();
			String startStr = escaping.getStartPattern();
			String finishStr = escaping.getFinishPattern();
			List<String> contentList = extractOneLine(ccc, startStr, finishStr,
					startIsRegex, finishIsRegex);
			StringBuffer buffer = new StringBuffer();
			for (String con : contentList)
			{
				if (con.startsWith("match||"))
				{
					buffer.append(checkActiveHandleChar4Import(
							con.substring(("match||").length(), con.length()),
							escaping, format, processedChars));
				}
				else if (con.startsWith("notmatch||"))
				{
					buffer.append(con.substring(("notmatch||").length(),
							con.length()));
				}
			}
			ccc = buffer.toString();
		}

		return ccc;
	}

	private static String checkInActiveForImport(String ccc, Escaping escaping,
			String format, List<Character> processedChars, String contentType)
	{
		String returnStr = null;
		String partContentValue = escaping.getPartConentValue();
		if ((partContentValue.equalsIgnoreCase("cdata") && contentType
				.equalsIgnoreCase("CDATA"))
				|| (partContentValue.equalsIgnoreCase("htmlXmlNode") && (contentType
						.equalsIgnoreCase("HtmlNode") || contentType
						.equalsIgnoreCase("XmlNode")))
				|| (partContentValue.equalsIgnoreCase("xmlAttribute") && contentType
						.equalsIgnoreCase("xmlAttribute")))
		{
			returnStr = ccc;
		}
		else if (escaping.getPartConentValue()
				.equalsIgnoreCase("startFinishes"))
		{
			boolean startIsRegex = escaping.isStartIsRegex();
			boolean finishIsRegex = escaping.isFinishIsRegex();
			String startStr = escaping.getStartPattern();
			String finishStr = escaping.getFinishPattern();
			List<String> contentList = extractOneLine(ccc, startStr, finishStr,
					startIsRegex, finishIsRegex);
			StringBuffer buffer = new StringBuffer();
			for (String con : contentList)
			{
				if (con.startsWith("match||"))
				{
					buffer.append(con.substring(("match||").length(),
							con.length()));
				}
				else if (con.startsWith("notmatch||"))
				{
					buffer.append(checkActiveHandleChar4Import(con.substring(
							("notmatch||").length(), con.length()), escaping,
							format, processedChars));
				}
			}
			ccc = buffer.toString();
		}
		else
		{
			returnStr = checkActiveHandleChar4Import(ccc, escaping, format,
					processedChars);
		}

		if (returnStr == null)
			returnStr = ccc;

		return returnStr;
	}

	private static String checkActiveHandleChar4Import(String ccc,
			Escaping escaping, String format, List<Character> processedChars)
	{
		StringBuffer sub = new StringBuffer();
		int length = ccc.length() - 1;
		if (length == 0)
		{
			return sub.append(ccc).toString();
		}
		int j = 0;
		for (; j < length; j++)
		{
			char char1 = ccc.charAt(j);
			char char2 = ccc.charAt(j + 1);
			char char3 = (j + 2 <= length) ? ccc.charAt(j + 2) : ' ';

			boolean processed = newHandleChar4Import(escaping, char1, char2);

			if (!processed)
			{
				sub.append(char1);
			}
			else
			{
				if (isSpecialFormat(format)
						&& (char1 + "").equals(escaping.getEscape())
						&& (char2 + "").equals(escaping.getEscape()))
				{
					sub.append(char1);
					j = j + 1;
					if (char3 != ' ' && processedChars != null
							&& !sub.toString().endsWith("\\\\")
							&& !processedChars.contains(char3))
					{
						processedChars.add(char3);
						if (j == length - 1)
						{
							sub.append(char3);
							j = j + 1;
						}
					}
				}
			}

			if (j == length - 1)
			{
				sub.append(char2);
			}
		}
		return sub.toString();
	}

	private static String noCheckActiveForImport(String ccc, Escaping escaping,
			String format, List<Character> processedChars)
	{
		StringBuffer sub = new StringBuffer();
		int length = ccc.length() - 1;
		if (length == 0)
		{
			return sub.append(ccc).toString();
		}
		int j = 0;
		for (; j < length; j++)
		{
			char char1 = ccc.charAt(j);
			char char2 = ccc.charAt(j + 1);
			char char3 = (j + 2 <= length) ? ccc.charAt(j + 2) : ' ';

			boolean processed = newHandleChar4Import(escaping, char1, char2);

			if (!processed)
			{
				sub.append(char1);
			}
			else
			{
				if (isSpecialFormat(format)
						&& (char1 + "").equals(escaping.getEscape())
						&& (char2 + "").equals(escaping.getEscape()))
				{
					sub.append(char1);
					j = j + 1;
					if (char3 != ' ' && processedChars != null
							&& !sub.toString().endsWith("\\\\")
							&& !processedChars.contains(char3))
					{
						processedChars.add(char3);
						if (j == length - 1)
						{
							sub.append(char3);
							j = j + 1;
						}
					}
				}
			}

			if (j == length - 1)
			{
				sub.append(char2);
			}
		}

		return sub.toString();
	}

	private static boolean newHandleChar4Import(Escaping escaping, char char1,
			char char2)
	{
		if ((char1 + "").equals(escaping.getEscape()))
		{
			if (escaping.isUnEscapeOnImport()
					&& (char2 + "").equals(escaping.getCharacter()))
			{
				return true;
			}
		}

		return false;
	}

	private static String getContentType(String segment, String format,
			boolean isAttr, boolean isInCDATA)
	{
		String contentType = "";
		if (isInCDATA)
		{
			contentType = "CDATA";
		}
		else if (IFormatNames.FORMAT_HTML.equals(format))
		{
			contentType = "HtmlNode";
		}
		else if (IFormatNames.FORMAT_XML.equals(format))
		{
			if (isAttr)
			{
				contentType = "xmlAttribute";
			}
			else
			{
				contentType = "XmlNode";
			}
		}

		return contentType;
	}

	public static List<String> extractOneLine(String ccc, String startStr,
			String finishStr, boolean startIsRegex, boolean finishRegex)
	{
		if (ccc == null || ccc.length() == 0)
		{
			return null;
		}
		List<Integer> extractIndexStartList = new ArrayList<Integer>();
		Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
		List<String> contentList = new ArrayList<String>();
		int extractIndexStart = -1;
		int extractIndexFinish = -1;

		if (StringUtil.isNotEmptyAndNull(startStr))
		{
			if (startIsRegex)
			{
				Pattern p = Pattern.compile(startStr);
				Matcher m = p.matcher(ccc);

				while (m.find())
				{
					extractIndexStart = m.start();
					extractIndexStartList.add(extractIndexStart);
				}
			}
			else
			{
				for (int k = 0; k < ccc.length();)
				{
					int i0 = -1;
					if (k == 0)
					{
						i0 = ccc.indexOf(startStr);
						if (i0 == -1)
							break;

						extractIndexStart = i0 + startStr.length();
						extractIndexStartList.add(i0);
						k = extractIndexStart;
					}
					else
					{
						i0 = ccc.indexOf(startStr, extractIndexStart);
						if (i0 == -1)
							break;

						extractIndexStart = i0 + startStr.length();
						extractIndexStartList.add(i0);
						k = extractIndexStart;
					}
				}
			}
		}

		// find the index of finish string's
		if (StringUtil.isNotEmptyAndNull(finishStr))
		{
			if (finishRegex)
			{
				Pattern p = Pattern.compile(finishStr);
				Matcher m = p.matcher(ccc);
				if (extractIndexStartList != null
						&& extractIndexStartList.size() > 0)
				{
					int length = extractIndexStartList.size();
					for (int i = 0; i < length; i++)
					{
						if (m.find(extractIndexStartList.get(i)))
						{
							extractIndexFinish = m.end();
							if (i < length - 1
									&& extractIndexFinish > extractIndexStartList
											.get(i)
									&& extractIndexFinish < extractIndexStartList
											.get(i + 1))
							{
								indexMap.put(extractIndexStartList.get(i),
										extractIndexFinish);
							}
							else if (i == length - 1
									&& extractIndexFinish > extractIndexStartList
											.get(i))
							{
								indexMap.put(extractIndexStartList.get(i),
										extractIndexFinish);
							}
							else continue;
						}
					}
				}
			}
			else
			{
				if (extractIndexStartList != null
						&& extractIndexStartList.size() > 0)
				{
					int length = extractIndexStartList.size();
					int i0 = -1;
					for (int i = 0; i < length; i++)
					{
						i0 = ccc.indexOf(finishStr,
								extractIndexStartList.get(i));
						if (i0 == -1)
							break;

						extractIndexFinish = i0 + finishStr.length();
						if (i < length - 1
								&& extractIndexFinish > extractIndexStartList
										.get(i)
								&& extractIndexFinish < extractIndexStartList
										.get(i + 1))
						{
							indexMap.put(extractIndexStartList.get(i),
									extractIndexFinish);
						}
						else if (i == length - 1
								&& extractIndexFinish > extractIndexStartList
										.get(i))
						{
							indexMap.put(extractIndexStartList.get(i),
									extractIndexFinish);
						}
						else continue;
					}
				}
			}
		}

		if (!indexMap.isEmpty())
		{
			Set<Integer> keySet = indexMap.keySet();
			List<Integer> keyList = new ArrayList<Integer>();
			keyList.addAll(keySet);
			Collections.sort(keyList);
			for (int i = 0; i < keyList.size(); i++)
			{
				if (i == 0)
				{
					String con01 = ccc.substring(0, keyList.get(i));
					if (StringUtil.isNotEmptyAndNull(con01))
					{
						contentList.add("notmatch||" + con01);
					}
				}
				String con02 = ccc.substring(keyList.get(i),
						indexMap.get(keyList.get(i)));
				contentList.add("match||" + con02);
				String con03 = "";
				if (i < keyList.size() - 1)
				{
					con03 = ccc.substring(indexMap.get(keyList.get(i)),
							keyList.get(i + 1));
				}
				else
				{
					con03 = ccc.substring(indexMap.get(keyList.get(i)),
							ccc.length());
				}
				if (StringUtil.isNotEmptyAndNull(con03))
				{
					contentList.add("notmatch||" + con03);
				}
			}
		}
		else
		{
			if (extractIndexStartList != null
					&& extractIndexStartList.size() > 0)
			{
				if (StringUtil.isNotEmptyAndNull(finishStr))
				{
					contentList.add("notmatch||" + ccc);
				}
				else
				{
					String con01 = ccc.substring(0,
							extractIndexStartList.get(0));
					String con02 = ccc.substring(extractIndexStartList.get(0),
							ccc.length());
					if (StringUtil.isNotEmptyAndNull(con01))
					{
						contentList.add("notmatch||" + con01);
					}
					if (StringUtil.isNotEmptyAndNull(con02))
					{
						contentList.add("match||" + con02);
					}
				}
			}
			else
			{
				contentList.add("notmatch||" + ccc);
			}
		}

		return contentList;
	}

	public static boolean checkAssociatedHtmlFilter(Filter mFilter)
	{
		if (mFilter != null)
		{
			String filterName = null;
			long filterId = -1;
			if (mFilter instanceof JavaPropertiesFilter)
			{
				filterName = ((JavaPropertiesFilter) mFilter)
						.getSecondFilterTableName();
				filterId = ((JavaPropertiesFilter) mFilter).getSecondFilterId();
			}
			else if (mFilter instanceof POFilter)
			{
				filterName = ((POFilter) mFilter).getSecondFilterTableName();
				filterId = ((POFilter) mFilter).getSecondFilterId();
			}

			if (filterName != null
					&& filterName.equalsIgnoreCase("html_filter")
					&& filterId != -1)
			{
				boolean isFilterExist = FilterHelper.isFilterExist(filterName,
						filterId);
				return isFilterExist;
			}
		}
		return false;
	}

	public static List<String> getHtmlNode(String p_str)
	{
		List<String> returnStr = new ArrayList<String>();
		HtmlEntities entities = new HtmlEntities();
		p_str = entities.decodeStringBasic(p_str);
		int ltIndex = p_str.indexOf("<");
		int gtIndex = p_str.indexOf(">");
		if (ltIndex == -1 && gtIndex == -1)
		{
			returnStr.add("normal||" + entities.encodeStringBasic(p_str));
			return returnStr;
		}

		while (ltIndex > -1 || gtIndex > -1)
		{
			String strA = "";

			if (ltIndex > -1 && gtIndex > -1)
			{
				if (gtIndex > ltIndex)
				{
					strA = p_str.substring(0, gtIndex + 1);
					p_str = p_str.substring(gtIndex + 1);

					int left = strA.lastIndexOf("<");
					String leftStr = strA.substring(0, left);
					leftStr = leftStr.replace("<", "&lt;");
					returnStr.add("normal||"
							+ entities.encodeStringBasic(leftStr));
					String rightStr = strA.substring(left);
					int newLtIndex = p_str.indexOf("<");
					int newGtIndex = p_str.indexOf(">");

					if (newLtIndex != -1 && newGtIndex != -1)
					{
						strA = p_str.substring(0, newGtIndex + 1);
						returnStr.add("htmlnode||"
								+ entities.encodeStringBasic(rightStr + strA));
						p_str = p_str.substring(newGtIndex + 1);
						ltIndex = p_str.indexOf("<");
						gtIndex = p_str.indexOf(">");
						if (ltIndex == -1 && gtIndex == -1)
						{
							returnStr.add("normal||"
									+ entities.encodeStringBasic(p_str));
						}
					}

					if (newLtIndex == -1 && newGtIndex == -1)
					{
						returnStr.add("normal||"
								+ entities.encodeStringBasic(rightStr + p_str));
						ltIndex = newLtIndex;
						gtIndex = newGtIndex;
					}
				}
				else
				{
					returnStr.add("normal||"
							+ entities.encodeStringBasic(p_str));
					ltIndex = -1;
					gtIndex = -1;
				}
			}
			else
			{
				returnStr.add("normal||" + entities.encodeStringBasic(p_str));
				ltIndex = -1;
				gtIndex = -1;
			}
		}

		return returnStr;
	}

	private static String getEscapeString(String segment, List<Escaping> es,
			String format, List<Character> processedChars, String contentType)
	{
		StringBuffer buffer = new StringBuffer();
		List<String> segmentList = getHtmlNode(segment);
		if (segmentList != null && segmentList.size() > 0)
		{
			for (String str : segmentList)
			{
				if (str.startsWith("normal||"))
				{
					buffer.append(newHandleString4Import(
							str.substring(("normal||").length()), es, format,
							false, processedChars, contentType));
				}
				else if (str.startsWith("htmlnode||"))
				{
					String newContentType = "";
					if (StringUtil.isEmpty(contentType))
						newContentType = "HtmlNode";
					else newContentType = contentType;

					buffer.append(newHandleString4Import(
							str.substring(("htmlnode||").length()), es, format,
							false, processedChars, newContentType));
				}
			}
		}
		return buffer.toString();
	}

	private static boolean checkHtmlNode(List<TagIndex> tags, int index,
			boolean isAssociateHtmlFilter)
	{
		Pattern startP = Pattern.compile(statRegexStr);
		Pattern endP = Pattern.compile(endRegexStr);
		int count = tags.size();
		if (index > 0
				&& index < count - 1
				&& startP
						.matcher(
								m_xmlEncoder.decodeStringBasic(tags
										.get(index - 1).content)).find()
				&& endP.matcher(
						m_xmlEncoder.decodeStringBasic(tags.get(index + 1).content))
						.find() && isAssociateHtmlFilter)
		{
			return true;
		}
		return false;
	}
	
	private static String getSpecialContentForImport(List<TagIndex> tags,
			List<Escaping> es, String format, boolean isPureText,
			List<Character> processedChars, String contentType)
	{
		StringBuffer sb = new StringBuffer();
		boolean doDecode = !isPureText;
		List<String> orderKey = new ArrayList<String>();
		Map<String, String> tagMap = new HashMap<String, String>();
		Map<String, String> contentMap = new HashMap<String, String>();
		for (int i = 0; i < tags.size(); i++)
		{
			TagIndex ti = tags.get(i);
			if (ti.isTag)
			{
				orderKey.add("tag" + i);
				tagMap.put("tag" + i, ti.content);
			}
			else
			{
				orderKey.add("content" + i);
				contentMap.put("content" + i, ti.content);
			}
		}

		if (!contentMap.isEmpty())
		{
			Map<String, String> newContentMap = new HashMap<String, String>();
			String content = "";
			Set<String> keySet = contentMap.keySet();
			List<String> keyList = new ArrayList<String>();
			keyList.addAll(keySet);
			Collections.sort(keyList);
			for (String key : keyList)
			{
				content += contentMap.get(key) + "|";
			}
			if (content.endsWith("|"))
			{
				content = content.substring(0, content.lastIndexOf("|"));
			}
			String returnStr = newHandleString4Import(content, es, doDecode,
					format, processedChars, contentType);
			String[] arrStr = returnStr.split("\\|");
			if (keyList.size() == arrStr.length)
			{
				for (int i = 0; i < keyList.size(); i++)
				{
					newContentMap.put(keyList.get(i), arrStr[i]);
				}
				for (String orderStr : orderKey)
				{
					if (tagMap.get(orderStr) != null)
					{
						sb.append(tagMap.get(orderStr));
					}
					if (newContentMap.get(orderStr) != null)
					{
						sb.append(newContentMap.get(orderStr));
					}
				}
			}
		}
		return sb.toString();
	}
	
	private static String getSpecialContentForExport(List<TagIndex> tags,
			List<Escaping> es, String format, boolean doDecode,
			String escapingChars, String contentType, boolean isInCDATA)
	{
		StringBuffer sb = new StringBuffer();
		List<String> orderKey = new ArrayList<String>();
		Map<String, String> tagMap = new HashMap<String, String>();
		Map<String, String> contentMap = new HashMap<String, String>();
		for (int i = 0; i < tags.size(); i++)
		{
			TagIndex ti = tags.get(i);
			if (ti.isTag)
			{
				orderKey.add("tag" + i);
				tagMap.put("tag" + i, ti.content);
			}
			else
			{
				orderKey.add("content" + i);
				contentMap.put("content" + i, ti.content);
			}
		}

		if (!contentMap.isEmpty() && !tagMap.isEmpty())
		{
			Map<String, String> newContentMap = new HashMap<String, String>();
			Map<String, String> newTagMap = new HashMap<String, String>();
			String content = "";
			Set<String> keySet = contentMap.keySet();
			List<String> keyList = new ArrayList<String>();
			keyList.addAll(keySet);
			Collections.sort(keyList);
			for (String key : keyList)
			{
				content += contentMap.get(key) + "|";
			}
			if (content.endsWith("|"))
			{
				content = content.substring(0, content.lastIndexOf("|"));
			}
			String returnStr = newHandleString4Export(content, es, doDecode,
					format, escapingChars, contentType);
			String[] arrStr = returnStr.split("\\|");
			if (keyList.size() == arrStr.length)
			{
				for (int i = 0; i < keyList.size(); i++)
				{
					newContentMap.put(keyList.get(i), arrStr[i]);
				}
			}

			Set<String> tagKeySet = tagMap.keySet();
			List<String> tagKeyList = new ArrayList<String>();
			tagKeyList.addAll(tagKeySet);
			Collections.sort(tagKeyList);
			if (IFormatNames.FORMAT_XML.equals(format) && isInCDATA)
			{
				// Escape tag content is dangerous...
				for (String tagKey : tagKeyList)
				{
					newTagMap.put(
							tagKey,
							newHandleTagContent4Export(tagMap.get(tagKey), es,
									doDecode, format, escapingChars,
									contentType));
				}
				for (String orderStr : orderKey)
				{
					if (newTagMap.get(orderStr) != null)
					{
						sb.append(newTagMap.get(orderStr));
					}
					if (newContentMap.get(orderStr) != null)
					{
						sb.append(newContentMap.get(orderStr));
					}
				}

			}
			else
			{
				for (String orderStr : orderKey)
				{
					if (tagMap.get(orderStr) != null)
					{
						sb.append(tagMap.get(orderStr));
					}
					if (newContentMap.get(orderStr) != null)
					{
						sb.append(newContentMap.get(orderStr));
					}
				}
			}
		}
		return sb.toString();
	}
}
