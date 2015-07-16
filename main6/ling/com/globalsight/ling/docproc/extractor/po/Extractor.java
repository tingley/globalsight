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
package com.globalsight.ling.docproc.extractor.po;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.machineTranslation.google.GoogleProxy;
import com.globalsight.util.StringUtil;

/**
 * <p>
 * PO file extractor.
 * </p>
 * 
 * <p>
 * <b>Note:</b> The conversion of certain format related characters into
 * diplomat tags is handled by a separate tag controller object - referred to as
 * the TmxController. The controller has static rules that govern the creation
 * of certain diplomat tags. The static rules are specific to this parser. They
 * can be changed by editing the flags in the TmxController class.
 * </p>
 */
public class Extractor extends AbstractExtractor implements
        ExtractorExceptionConstants
{
    private static Logger logger = Logger.getLogger(Extractor.class.getName());

    public static final String TYPE_SKEL = "Skeleton";
    public static final String TYPE_TRANS = "Translatable";
    public static final String TYPE_MSGID = POToken.MSGSID;
    public static final String TYPE_MSGSTR = POToken.MSGSTR;

    public static Map<String, String> sourceMap;
    public static Map<String, String> targetMap;

    private boolean m_isSetTargetLanguage = false;

    private List<String> m_targetList;

    public Extractor()
    {
        super();
    }

    /**
     * <p>
     * This method is the top level entry point to start the extractor. The
     * output of the extraction process is stored in an Output object which was
     * passed to the extraction framework by the caller.
     * </p>
     * 
     * @see com.globalsight.ling.docproc.AbstractExtractor
     * @see com.globalsight.ling.docproc.Output
     */

    public void extract() throws ExtractorException
    {
        m_isSetTargetLanguage = false;
        m_targetList = new ArrayList<String>();

        Output output = getOutput();
        output.setDataFormat(IFormatNames.FORMAT_PO);
        Parser parser = new Parser(readInput());
        List<String> lineList = parser.getLineList();

        String content = null, realContent = null;
        boolean isMsgID = false, isMsgIDPlural = false;
        boolean isMsgStr = false, isMsgStrPlural = false;
        boolean isMsgCtxt = false;
        List<String> msgIDList = new ArrayList<String>();
        List<String> msgIDPluralList = new ArrayList<String>();
        List<String> msgStrList = new ArrayList<String>();

        for (int i = 0; i < lineList.size(); i++)
        {
            content = (String) lineList.get(i);
            realContent = getMsg(content);
            if (content.startsWith(POToken.MSGCTXT))
            {
            	isMsgCtxt = true;
                output.addSkeleton(content + "\n");
            }
            else if (content.startsWith(POToken.MSGSID))
            {
                if (content.startsWith(POToken.MSGSID_PLURAL))
                {
                    isMsgIDPlural = true;
                    msgIDPluralList.add(realContent);
                    output.addSkeleton(content + "\n");
                }
                else
                {
                    msgIDList.add(realContent);
                    isMsgID = true;
                    isMsgStr = false;
                    output.addSkeleton(content + "\n");
                }
                isMsgCtxt = false;
            }
            else if (content.startsWith(POToken.MSGSTR))
            {
                msgStrList.add(realContent);
                isMsgID = false;
                isMsgIDPlural = false;
                if (content.startsWith(POToken.MSGSTR_PLURAL))
                {
                    isMsgStr = false;
                    isMsgStrPlural = true;
                }
                else
                {
                    isMsgStr = true;
                    isMsgStrPlural = false;
                }
                isMsgCtxt = false;
            }
            else if (content.startsWith(POToken.QuotationMark)
                    && (content.length() > 0))
            {
                if (isMsgIDPlural)
                {
                    msgIDPluralList.add(realContent);
                    output.addSkeleton(content + "\n");
                }
                else if (isMsgID)
                {
                    msgIDList.add(realContent);
                    output.addSkeleton(content + "\n");
                }
                else if (isMsgStr)
                {
                    msgStrList.add(realContent);
                }
                else if (isMsgStrPlural)
                {
                    int lastIndex = msgStrList.size() - 1;
                    String temp = msgStrList.get(lastIndex) + realContent;
                    msgStrList.set(lastIndex, temp);
                }
                else if (isMsgCtxt)
                {
                    if (content != null)
                    {
                        output.addSkeleton(content + "\n");
                    }
                }
            }
            else
            {
                addMsgListAndComm(msgIDList, msgIDPluralList, output, msgStrList);
                if (content != null && (!content.startsWith(POToken.MSGSTR)))
                {
                    output.addSkeleton(content + "\n");
                }

                isMsgID = false;
                isMsgIDPlural = false;
                isMsgStr = false;
            }
        }

        addMsgListAndComm(msgIDList, msgIDPluralList, output, msgStrList);

        setTargetLanguage(msgIDList, msgIDPluralList, msgStrList, output, true);
    }

    /**
     * Add MSG list and comment to output.
     */
    private void addMsgListAndComm(List<String> p_msgIDList,
            List<String> p_msgIDPluralList, Output p_output,
            List<String> p_msgStrList)
    {
        if (p_msgIDPluralList != null && p_msgIDPluralList.size() > 0
                && p_msgIDList != null && p_msgIDList.size() > 0)
        {
            int msgStr_Index = 0;
            String source = getStringFromList(p_msgIDList);
            String target = p_msgStrList.get(msgStr_Index);
            p_output.addSkeleton(POToken.MSGSTR_PLURAL + (msgStr_Index++) + "] \"");
            addTranslatable(p_output, source, target);
            p_output.addSkeleton("\"\n");

            source = getStringFromList(p_msgIDPluralList);
            while (msgStr_Index < p_msgStrList.size())
            {
                target = p_msgStrList.get(msgStr_Index);

                if (source.equals("\n"))
                {
                    p_output.addSkeleton(source);
                }
                else if (source.trim().length() < 1)
                {
                    p_output.addSkeleton("\"" + source + "\"\n");
                }
                else
                {
                    p_output.addSkeleton(POToken.MSGSTR_PLURAL + (msgStr_Index++) + "] \"");
                    addTranslatable(p_output, source, target);
                    p_output.addSkeleton("\"\n");
                }
            }
        }
        else if (p_msgIDList != null && p_msgIDList.size() > 0)
        {
            outputTranslatable(p_output, p_msgIDList, p_msgStrList);
        }

        // Detect the language of target/msgStr, which is used for creating tuv.
        setTargetLanguage(p_msgIDList, p_msgIDPluralList,p_msgStrList, p_output, false);

        if (p_msgIDList != null)        p_msgIDList.clear();
        if (p_msgIDPluralList != null)  p_msgIDPluralList.clear();
        if (p_msgStrList != null)       p_msgStrList.clear();
    }

    private void outputTranslatable(Output p_output, List<String> p_msgIDList,
            List<String> p_msgStrList)
    {
        if (p_msgIDList == null || p_msgIDList.size() < 1)
        {
            if (p_msgStrList != null && p_msgStrList.size() > 0)
            {
                for (int i = 0; i < p_msgStrList.size(); i++)
                {
                    String content = (String) p_msgStrList.get(i);
                    addMsgStr(content, p_output, (i == 0 ? true : false), content);
                }
            }
            else
            {
                p_output.addSkeleton(POToken.MSGSTR + " \"\"\n");
            }
            return;
        }
        else
        {
            String source = getStringFromList(p_msgIDList);

            if (source == null || source.trim().length() < 1)
            {
                addSkeleton(p_output, TYPE_MSGSTR, p_msgStrList);
            }
            else
            {
                String target = getStringFromList(p_msgStrList);
                p_output.addSkeleton(POToken.MSGSTR + " \"");
                addTranslatable(p_output, source, target);
                p_output.addSkeleton("\"\n");
            }
        }
    }

    private String getStringFromList(List<String> p_list)
    {
        if (p_list == null || p_list.size() == 0)
            return "";

        StringBuffer result = new StringBuffer();
        for (String str : p_list)
        {
            result.append(str);
        }

        return result.toString();
    }

    /**
     * Add msgstr to output.
     * 
     * @param addContent
     *            msgstr content
     * @param p_output
     * @param p_isHead
     *            if need start with "msgstr "
     * 
     */
    private void addMsgStr(String p_content, Output p_output, boolean p_isHead,
            String p_target)
    {
        String msgStrHead = POToken.MSGSTR;
        if (p_content.trim().length() > 0)
        {
            if (p_isHead)
            {
                p_output.addSkeleton(msgStrHead + " \"");
                addTranslatable(p_output, p_content, p_target);
                p_output.addSkeleton("\"\n");
            }
            else
            {
                p_output.addSkeleton("\"");
                addTranslatable(p_output, p_content, p_target);
                p_output.addSkeleton("\"\n");
            }
        }
        else
        {
            if (p_isHead)
            {
                p_output.addSkeleton(msgStrHead + " \"" + p_content + "\"\n");
            }
            else
            {
                if (p_content.equals("\n"))
                {
                    p_output.addSkeleton(p_content);
                }
                else
                {
                    p_output.addSkeleton("\"" + p_content + "\"\n");
                }
            }
        }
    }

    public static void addTranslatable(Output p_output, String p_source,
            String p_target)
    {
        if (sourceMap == null)
        {
            sourceMap = new HashMap<String, String>();
            sourceMap.put("xliffPart", "source");
        }

        if (targetMap == null)
        {
            targetMap = new HashMap<String, String>();
            targetMap.put("xliffPart", "target");
        }

        String source, target;
        source = replaceSpecialCharacter(p_source);
        if (p_target == null || p_target.length() == 0)
        {
            target = source;
        }
        else
        {
            target = replaceSpecialCharacter(p_target);
        }

        p_output.addTranslatableTmx(source, null, sourceMap);
        p_output.addSkeleton("");
        p_output.addTranslatableTmx(target, null, targetMap);
    }

    /**
     * Replace some special character for msgstr, due word count issue.
     */
    private static String replaceSpecialCharacter(String p_str)
    {
		return p_str.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;");
    }

    private void addSkeleton(Output p_output, String type, List<String> p_list)
    {
        if (p_list == null || p_list.size() < 1)
        {
            addSkeleton(p_output, type, "", true);
        }
        else
        {
            for (int i = 0; i < p_list.size(); i++)
            {
                addSkeleton(p_output, type, (String) p_list.get(i), i == 0 ? true : false);
            }
        }

    }

    private void addSkeleton(Output p_output, String p_type,
            String p_strSkeleton, boolean p_isHead)
    {
        String msg = "\"" + p_strSkeleton + "\"\n";

        if (p_isHead)
        {
            if (TYPE_MSGID.equals(p_type))
            {
                msg = POToken.MSGSID + " " + msg;
            }
            else
            {
                msg = POToken.MSGSTR + " " + msg;
            }
        }

        p_output.addSkeleton(msg);
    }

    /**
     * Get the message from MsgID, MsgStr, Comment.
     * 
     * @param p_content
     * @return
     */
    private String getMsg(String p_content)
    {
        if (p_content.equals("\n"))
            return p_content;

        String result = p_content.trim();
        if (result.startsWith(POToken.MSGSID_PLURAL))
        {
            result = result.substring(POToken.MSGSID_PLURAL.length() + 1);
        }
        else if (result.startsWith(POToken.MSGSID))
        {
            result = result.substring(POToken.MSGSID.length() + 1);
        }

        if (result.startsWith(POToken.MSGSTR_PLURAL))
        {
            int index = result.indexOf(" ", POToken.MSGSTR_PLURAL.length());
            result = result.substring(index + 1);
        }
        else if (result.startsWith(POToken.MSGSTR))
        {
            result = result.substring(POToken.MSGSTR.length() + 1);
        }

        if (result.startsWith(POToken.MSGCTXT))
        {
            result = result.substring(POToken.MSGCTXT.length() + 1);
        }

        return trimQuotationMarks(result);
    }

    private String trimQuotationMarks(String p_content)
    {
        String result = p_content;
        if (result.startsWith(POToken.QuotationMark)
                && result.endsWith(POToken.QuotationMark))
        {
            result = result.substring(1, result.length() - 1);
        }

        return result;
    }
    
    /**
     * Sets the Target Language Of PO File into Output, 
     * which will be used for creating activity/task, 
     * such as target language in XLF File.
     * 
     * @param p_msgIDList
     *            msgid String List
     * @param p_msgIDPluralList
     *            msgid_plural String List
     * @param p_msgStrList
     *            msgstr String List
     * @param p_output
     * @param isLastLine
     *            if it is the last line of File
     */
    @SuppressWarnings("rawtypes")
    private void setTargetLanguage(List<String> p_msgIDList,
            List<String> p_msgIDPluralList, List<String> p_msgStrList,
            Output p_output, boolean isLastLine)
    {
        if (m_isSetTargetLanguage)
            return;
        
        String target;
        if (m_targetList != null && ((m_targetList.size() > 30) || isLastLine))
        {
            Map<String, Integer> map = new HashMap<String, Integer>();
            for (int i = 0; i < m_targetList.size(); i++)
            {
                target = (String) m_targetList.get(i);
                if (map.containsKey(target))
                {
                    map.put(target, map.get(target) + 1);
                }
                else
                {
                    map.put(target, 1);
                }
            }

            if(map==null || map.size()==0) 
                return;
            
            Set tarSet = map.keySet();
            Collection<Integer> tarNums = map.values();
            int maxNum = Collections.max(tarNums);
            Iterator it = tarSet.iterator();
            while (it.hasNext())
            {
                target = (String) it.next();
                int num = map.get(target);
                if (num == maxNum)
                {
                    if (target != null && target.trim().length() > 0)
                    {
                        p_output.setTargetLanguage(target);
                        m_isSetTargetLanguage = true;
                    }

                    m_targetList.clear();
                    break;
                }
            }
        }

        if (p_msgIDList == null || p_msgIDList.size() == 0
                || p_msgStrList == null || p_msgStrList.size() == 0)
        {
            return;
        }
        
        String msgID = getStringFromList(p_msgIDList).trim();
        String msgIDPlural = getStringFromList(p_msgIDPluralList).trim();
        String msgStr;
        
        if (msgIDPlural.length() == 0)
        {
            msgStr = getStringFromList(p_msgStrList).trim();
            if (!StringUtil.equalsIgnoreSpace(msgStr, msgID) && msgID.length()>0)
            {
                target = GoogleProxy.detectLanguage(msgStr);
                if (target != null && target.trim().length() > 0)
                {
                    m_targetList.add(target);
                }
            }
        }
        else if (p_msgStrList.size() > 1)
        {
            msgStr = p_msgStrList.get(0);
            msgStr = msgStr == null ? "" : msgStr.trim();
            if (!StringUtil.equalsIgnoreSpace(msgStr, msgID))
            {
                target = GoogleProxy.detectLanguage(msgStr);
                if (target != null && target.trim().length() > 0)
                {
                    m_targetList.add(target);
                }
            }

            msgStr = p_msgStrList.get(1);
            msgStr = msgStr == null ? "" : msgStr.trim();
            if (!StringUtil.equalsIgnoreSpace(msgStr, msgIDPlural))
            {
                target = GoogleProxy.detectLanguage(msgStr);
                if (target != null && target.trim().length() > 0)
                {
                    m_targetList.add(target);
                }
            }
        }
    }

    /**
     * <p>
     * Part of Jim's rules engine idea. Still required by the
     * ExtractorInterface.
     * </p>
     */
    public void loadRules() throws ExtractorException
    {
    }
}
