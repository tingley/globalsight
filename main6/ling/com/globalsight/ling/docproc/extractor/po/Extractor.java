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

import com.globalsight.ling.docproc.AbstractExtractor;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.machineTranslation.google.GoogleProxy;

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
    private static GlobalSightCategory c_logger = (GlobalSightCategory) 
    GlobalSightCategory.getLogger(Extractor.class.getName());

    public static final String typeSke = "Skeleton";
    public static final String typeTra = "Translatable";
    public static final String typeMsgID = POToken.MSGSID;
    public static final String typeMsgStr = POToken.MSGSTR;

    public static Map<String, String> sourceMap;
    public static Map<String, String> targetMap;
    
    private boolean isSetTargetLanguage = false;
    private List targetList;

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
        isSetTargetLanguage = false;
        targetList = new ArrayList();
        
        Output output = getOutput();
        output.setDataFormat(IFormatNames.FORMAT_PO);
        Parser parser = new Parser(readInput());
        List<String> lineList = parser.getLineList();

        String content = null, realContent = null;
        boolean isMsgID = false, isMsgIDPlural = false;
        boolean isMsgStr = false;
        List<String> msgIDList = new ArrayList<String>(), msgIDPluralList = new ArrayList<String>();
        List<String> msgStrList = new ArrayList<String>();
        int pluralLen = 2;

        for (int i = 0; i < lineList.size(); i++)
        {
            content = (String) lineList.get(i);
            realContent = getMsg(content);
            if (content.startsWith(POToken.MSGSID))
            {
                if (content.startsWith(POToken.MSGSID_PLURAL))
                {
                    isMsgIDPlural = true;
                    msgIDPluralList.add(realContent);
                    addOutput(output, typeSke, content + "\n", msgIDList, msgStrList);
                }
                else
                {
                    msgIDList.add(realContent);
                    isMsgID = true;
                    isMsgStr = false;
                    addOutput(output, typeSke, content + "\n", msgIDList, msgStrList);
                }
            }
            else if (content.startsWith(POToken.MSGSTR))
            {
                msgStrList.add(realContent);
                isMsgIDPlural = false;
                isMsgID = false;
                isMsgStr = true;
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
            }
            else
            {
                addMsgListAndComm(msgIDList, msgIDPluralList, isMsgIDPlural, pluralLen,
                        output, content, msgStrList, isMsgID, isMsgStr);
                isMsgID = false;
                isMsgIDPlural = false;
                isMsgStr = false;
            }

            content = null;
        }

        addMsgListAndComm(msgIDList, msgIDPluralList, isMsgIDPlural, pluralLen,
                output, content, msgStrList, isMsgID, isMsgStr);
        setTargetLanguage(msgStrList, output, true);
        isMsgID = false;
        isMsgIDPlural = false;
        isMsgStr = false;
        content = null;
        lineList = null;
    }

    /**
     * Add msg list and comment to output.
     * 
     * @param p_msgIDList
     *            The list of msgid.
     * @param p_msgIDPluralList
     *            The list of msgid_plural.
     * @param p_msgStrList
     *            The list of msgstr.
     * @param p_content
     *            The comment.
     */
    public void addMsgListAndComm(List<String> p_msgIDList,
            List<String> p_msgIDPluralList, boolean p_isMsgIDPlural,
            int p_pluralLen, Output p_output, String p_content,
            List<String> p_msgStrList, boolean p_isMsgID, boolean p_isMsgStr)
    {
        if (p_msgIDPluralList != null && p_msgIDPluralList.size() > 0
                && p_msgIDList != null && p_msgIDList.size() > 0)
        {
            int msgStr_Index = 0;
            p_output.addSkeleton(POToken.MSGSTR + "[" + (msgStr_Index++) + "] \"");
            addTranslatable(p_output, p_msgIDList.get(0), p_msgStrList.get(0));
            p_output.addSkeleton("\"\n");

            for (int i = 0; (i < p_msgIDPluralList.size())
                    && (msgStr_Index < p_pluralLen); i++)
            {
                String temp = p_msgIDPluralList.get(i);
                String target = temp;
                if ((i+1) < p_msgStrList.size())
                {
                    target = p_msgStrList.get(i+1);
                }

                if (temp.equals("\n"))
                {
                    p_output.addSkeleton(temp);
                }
                else if (temp.trim().length() < 1)
                {
                    p_output.addSkeleton("\"" + temp + "\"\n");
                }
                else
                {
                    p_output.addSkeleton(POToken.MSGSTR + "[" + (msgStr_Index++) + "] \"");
                    addTranslatable(p_output, temp, target);
                    p_output.addSkeleton("\"\n");
                }
            }
        }
        else if (p_msgIDList != null && p_msgIDList.size() > 0)
        {
            addOutput(p_output, typeTra, "", p_msgIDList, p_msgStrList);
        }

        if (p_content != null && (!p_content.startsWith(POToken.MSGSTR)))
        {
            addOutput(p_output, typeSke, p_content + "\n", p_msgIDList, p_msgStrList);
        }
        
        if (p_msgIDList != null && p_msgIDList.size() > 0)
        {
            String temp = p_msgIDList.get(0);
            if (temp != null && temp.trim().length() > 0)
            {
                setTargetLanguage(p_msgStrList, p_output, false);
            }
        }
        

        if (p_msgIDList != null)        p_msgIDList.clear();
        if (p_msgIDPluralList != null)  p_msgIDPluralList.clear();
        if (p_msgStrList != null)       p_msgStrList.clear();
    }

    /**
     * Add the content to output.
     */
    public static void addOutput(Output p_output, String p_type,
            String p_content, List<String> p_msgIDList,
            List<String> p_msgStrList)
    {
        if (p_type.equals(typeSke))
        {
            p_output.addSkeleton(p_content);
        }
        else if (p_type.equals(typeTra))
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
            else if (p_msgStrList == null
                    || p_msgStrList.size() < 1
                    || (p_msgStrList.size() == 1 && ("".equals(p_msgStrList
                            .get(0)))))
            {
                for (int i = 0; i < p_msgIDList.size(); i++)
                {
                    String content = (String) p_msgIDList.get(i);
                    addMsgStr(content, p_output, (i == 0 ? true : false), content);
                }
            }
            else if (p_msgIDList.size() == p_msgStrList.size())
            {
                for (int i = 0; i < p_msgIDList.size(); i++)
                {
                    String msgID = (String) p_msgIDList.get(i);
                    String msgStr = (String) p_msgStrList.get(i);
                    addMsgStr(msgID, p_output, (i == 0 ? true : false), msgStr);
                }
            }
            else
            {
                String source = getStringFromList(p_msgIDList);

                if (source == null || source.trim().length() < 1)
                {
                    addSkeleton(p_output, typeMsgStr, p_msgStrList);
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
    }

    public static String getStringFromList(List<String> p_list)
    {
        if (p_list == null || p_list.size() == 0)
            return "";

        String result = "";
        String tag = " ";
        for (String str : p_list)
        {
            result = result + str + tag;
        }

        return result;
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
    public static void addMsgStr(String p_content, Output p_output,
            boolean p_isHead, String p_target)
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
    public static String replaceSpecialCharacter(String p_str)
    {
        return p_str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void addSkeleton(Output p_output, String type, List p_list)
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

    public static void addSkeleton(Output p_output, String p_type,
            String p_strSkeleton, boolean p_isHead)
    {
        String msg = "\"" + p_strSkeleton + "\"\n";

        if (p_isHead)
        {
            if (typeMsgID.equals(p_type))
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
    public String getMsg(String p_content)
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

        return trimQuotationMarks(result);
    }

    public String trimQuotationMarks(String p_content)
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
     * @param p_msgStrList
     *            target String List
     * @param p_output
     * @param isLastLine
     *            if it is the last line of File
     */
    public void setTargetLanguage(List p_msgStrList, Output p_output, boolean isLastLine)
    {
        if (isSetTargetLanguage)
            return;

        String msgStr, target;
        if (p_msgStrList != null && p_msgStrList.size() > 0)
        {
            for (int i = 0; i < p_msgStrList.size(); i++)
            {
                msgStr = (String) p_msgStrList.get(i);
                if (msgStr != null)
                {
                    target = GoogleProxy.detectLanguage(msgStr);
                    if (target != null && target.trim().length() > 0)
                    {
                        targetList.add(target);
                    }
                }                
            }
        }

        if (targetList != null && ((targetList.size() > 30) || isLastLine))
        {
            Map<String, Integer> map = new HashMap<String, Integer>();
            String tar;
            for (int i = 0; i < targetList.size(); i++)
            {
                tar = (String) targetList.get(i);
                if (map.containsKey(tar))
                {
                    map.put(tar, map.get(tar) + 1);
                }
                else
                {
                    map.put(tar, 1);
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
                        isSetTargetLanguage = true;
                    }

                    targetList.clear();
                    break;
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
