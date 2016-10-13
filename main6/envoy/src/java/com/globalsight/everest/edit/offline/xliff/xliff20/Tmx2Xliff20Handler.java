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
package com.globalsight.everest.edit.offline.xliff.xliff20;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import com.globalsight.everest.edit.offline.xliff.xliff20.document.Data;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Ec;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Pc;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Ph;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Sc;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.YesNo;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoOverrideMapItem;

/**
 * <p>
 * Handles diplomat parser events to convert a TMX to PTags.
 * </p>
 * <p>
 * NOTE: for this release, parsing of sub flows is turned off.
 * </p>
 * <p>
 * Subflows were to be handled by the editor.
 * </p>
 */
public class Tmx2Xliff20Handler
{
    static final int BPT = 1;
    static final int EPT = 2;
    static final int IT = 3;
    static final int SUB = 4;

    private Stack<Object> elementStack = new Stack<Object>();
    private List<Object> result = new ArrayList<Object>();
    private boolean inTag = false;
    private String tag = "";

    private List<Data> datas = new ArrayList<Data>();
    private String id;

    private boolean isSource = true;
    
    private PseudoData pseudoData = new PseudoData();

    /**
     * Returns the input string with PTags inserted in place of TMX.
     */
    public List<Object> getResult()
    {
        return result;
    }

    /**
     * Handles diplomat basic parser Start event.
     */
    public void handleStart()
    {
        return;
    }

    /**
     * Handles diplomat basic parser Stop event.
     * <p>
     * <ul>
     * <li>Sets the Pseudo2Tmx map</li>
     * <li>Then invokes the building of the Pseudo to native map.</li>
     * </ul>
     */
    public void handleStop()
    {
    }

    /**
     * End-tag event handler.
     * 
     * @param strName
     *            - the literal tag name
     * @param strOriginalTag
     *            - the complete raw token from the parser
     */
    public void handleEndTag(String strName, String strOriginalTag)
    {
        inTag = false;
        if ("ept".endsWith(strName) || "ph".endsWith(strName)
                || "it".endsWith(strName) || "mrk".endsWith(strName))
        {
            elementStack.pop();
        }
    }

    /**
     * Handles the event that start a bpt tag.
     * 
     * @param strTmxTagName
     * @param hAttributes
     * @param strOriginalString
     */
    private void startBpt(String strTmxTagName, Properties hAttributes,
            String strOriginalString)
    {
        Pc pc = new Pc();
        id = getId(hAttributes);
        pc.setId(id);
        
        String type = hAttributes.getProperty("type");
        if (type != null)
        {
            PseudoOverrideMapItem item = pseudoData.getOverrideMapItem(type);
            if (item != null && !item.m_bNumbered)
            {
                pc.setSubType(type);
            }
        }

        String internal = hAttributes.getProperty("internal");
        if ("yes".equals(internal))
        {
            pc.setCanDelete(YesNo.NO);
        }

        if (elementStack.size() > 0)
        {
            Object e = elementStack.peek();
            if (e instanceof Pc)
            {
                Pc pc2 = (Pc) e;
                pc2.getContent().add(pc);
            }
        }
        else
        {
            result.add(pc);
        }

        elementStack.push(pc);
    }

    /**
     * Handles the event that start a mrk tag.
     * 
     * @param strTmxTagName
     * @param hAttributes
     * @param strOriginalString
     */
    private void startMrk(String strTmxTagName, Properties hAttributes,
            String strOriginalString)
    {
        Pc pc = new Pc();
        id = getId(hAttributes);

        if (id == null)
        {
            // comment should be like "comment="internal text, i=2"
            String comment = hAttributes.getProperty("comment");
            if (comment != null)
            {
                int n = comment.indexOf("i=");
                if (n > 0)
                {
                    id = comment.substring(n + 2);
                }

                // is internal
                if (comment.contains("internal"))
                {
                    pc.setCanDelete(YesNo.NO);
                }
            }
        }

        pc.setId(id);

        String internal = hAttributes.getProperty("internal");
        if ("yes".equals(internal))
        {
            pc.setCanDelete(YesNo.NO);
        }

        if (elementStack.size() > 0)
        {
            Object e = elementStack.peek();
            if (e instanceof Pc)
            {
                Pc pc2 = (Pc) e;
                pc2.getContent().add(pc);
            }
        }
        else
        {
            result.add(pc);
        }

        elementStack.push(pc);
    }

    /**
     * Handles the event that start a ept tag.
     * 
     * @param strTmxTagName
     * @param hAttributes
     * @param strOriginalString
     */
    private void startEpt(String strTmxTagName, Properties hAttributes,
            String strOriginalString)
    {
        elementStack.peek();
    }
    
    public static String getId(Properties hAttributes)
    {
        String id = hAttributes.getProperty("x");
        
        if (id == null)
        {
            id = hAttributes.getProperty("i");
        }
        
        if (id == null)
        {
            id = hAttributes.getProperty("id");
        }
        
        return id;
    }

    /**
     * Handles the event that start a ph tag.
     * 
     * @param strTmxTagName
     * @param hAttributes
     * @param strOriginalString
     */
    private void startPh(String strTmxTagName, Properties hAttributes,
            String strOriginalString)
    {
        Ph ph = new Ph();
        id = getId(hAttributes);
        ph.setId(id);

        String internal = hAttributes.getProperty("internal");
        if ("yes".equals(internal))
        {
            ph.setCanDelete(YesNo.NO);
        }

        if (elementStack.size() > 0)
        {
            Object e = elementStack.peek();
            if (e instanceof Pc)
            {
                Pc pc2 = (Pc) e;
                pc2.getContent().add(ph);
            }
        }
        else
        {
            result.add(ph);
        }

        elementStack.push(ph);
    }

    /**
     * Handles the event that start an it tag.
     * 
     * @param strTmxTagName
     * @param hAttributes
     * @param strOriginalString
     */
    private void startIt(String strTmxTagName, Properties hAttributes,
            String strOriginalString)
    {
        id = getId(hAttributes);

        String pos = hAttributes.getProperty("pos");
        if ("end".equals(pos))
        {
            Ec ec = new Ec();
            ec.setId(id);
            ec.setIsolated(YesNo.YES);
            if (elementStack.size() > 0)
            {
                Object e = elementStack.peek();
                if (e instanceof Pc)
                {
                    Pc pc2 = (Pc) e;
                    pc2.getContent().add(ec);
                }
            }
            else
            {
                result.add(ec);
            }

            elementStack.push(ec);
        }
        else
        {
            Sc sc = new Sc();
            sc.setId(id);
            sc.setIsolated(YesNo.YES);

            if (elementStack.size() > 0)
            {
                Object e = elementStack.peek();
                if (e instanceof Pc)
                {
                    Pc pc2 = (Pc) e;
                    pc2.getContent().add(sc);
                }
            }
            else
            {
                result.add(sc);
            }

            elementStack.push(sc);
        }
    }

    /**
     * Start-tag event handler.
     * 
     * @param strTmxTagName
     *            - The literal tag name.
     * @param hAtributes
     *            - Tag attributes in the form of a hashtable.
     * @param strOriginalString
     *            - The complete raw token from the parser.
     */
    public void handleStartTag(String strTmxTagName, Properties hAttributes,
            String strOriginalString)
    {
        tag = strTmxTagName;
        inTag = true;
        if ("bpt".equals(strTmxTagName))
        {
            startBpt(strTmxTagName, hAttributes, strOriginalString);
        }
        else if ("ept".equals(strTmxTagName))
        {
            startEpt(strTmxTagName, hAttributes, strOriginalString);
        }
        else if ("mrk".equals(strTmxTagName))
        {
            startMrk(strTmxTagName, hAttributes, strOriginalString);
        }
        else if ("ph".equals(strTmxTagName))
        {
            startPh(strTmxTagName, hAttributes, strOriginalString);
        }
        else if ("it".equals(strTmxTagName))
        {
            startIt(strTmxTagName, hAttributes, strOriginalString);
        }
    }

    /**
     * Gets the data prefix.
     * 
     * @return
     */
    private String getDataPrefix()
    {
        return isSource() ? "s" : "t";
    }

    /**
     * Text event handler.
     * 
     * @param strText
     *            - the next text chunk from between the tags
     */
    public void handleText(String strText)
    {
        strText = com.globalsight.diplomat.util.XmlUtil.unescapeString(strText);

        if (inTag && !"mrk".equals(tag))
        {
            int i = datas.size() + 1;
            Data data = new Data();
            data.setId(getDataPrefix() + i);

            data.getContent().add(strText);
            datas.add(data);

            Object e = elementStack.peek();
            if (e instanceof Pc)
            {
                Pc pc = (Pc) e;
                if ("bpt".equals(tag))
                {
                    pc.setDataRefStart(getDataPrefix() + i);
                }
                else if ("ept".equals(tag))
                {
                    pc.setDataRefEnd(getDataPrefix() + i);
                }
                else
                {
                    System.out.println("wrong tag:" + tag);
                }
            }
            else if (e instanceof Ph)
            {
                Ph ph = (Ph) e;
                ph.setDataRef(getDataPrefix() + i);
            }
            else if (e instanceof Sc)
            {
                Sc sc = (Sc) e;
                sc.setDataRef(getDataPrefix() + i);
            }
            else if (e instanceof Ec)
            {
                Ec ec = (Ec) e;
                ec.setDataRef(getDataPrefix() + i);
            }
        }
        else
        {
            if (elementStack.size() > 0)
            {
                Object ob = elementStack.peek();
                if (ob instanceof Pc)
                {
                    Pc pc = (Pc) ob;
                    pc.getContent().add(strText);
                }
            }
            else
            {
                result.add(strText);
            }
        }
    }

    /**
     * Just handle as nomal text.
     * @param substring
     */
    public void handleIsExtractedText(String substring)
    {
        handleText(substring);
    }

    /**
     * TMX event handler, called by the framework. Don't worry about it...
     */
    public Tmx2Xliff20Handler()
    {
    }

    /**
     * @return the datas
     */
    public List<Data> getDatas()
    {
        return datas;
    }

    /**
     * @return the isSource
     */
    public boolean isSource()
    {
        return isSource;
    }

    /**
     * @param isSource
     *            the isSource to set
     */
    public void setSource(boolean isSource)
    {
        this.isSource = isSource;
    }
}
