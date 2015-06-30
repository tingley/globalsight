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
package com.globalsight.ling.tw;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.everest.edit.offline.xliff.xliff20.Tmx2Xliff20;
import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.internal.InternalTag;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.ling.tw.internal.InternalTexts;
import com.globalsight.ling.tw.internal.OnlineInternalTag;

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
public class Tmx2PseudoHandler implements DiplomatBasicHandler
{
    static final int BPT = 1;
    static final int EPT = 2;
    static final int IT = 3; // including it, ut and ph
    static final int SUB = 4;
    static final int UNSET = -1;

    private static final String XML_OPEN_TAG = "<";
    private static final String XML_CLOSE_TAG = ">";

    private Stack m_elementStack = new Stack();
    private Stack m_extractedStack = new Stack();
    private Tmx2PseudoHandlerElement m_rootElement = new Tmx2PseudoHandlerElement();
    private Vector m_vSubflows = new Vector();

    private Hashtable<Object, String> m_hEpt2BptSrcIndexMap = new Hashtable<Object, String>();
    private HashMap<String, String> ept2bpt = new HashMap();
    // this is for tag that not has id. just like [b]
    private HashMap<String, String> ept2bpt2 = new HashMap();
    
    private int m_nextUniqIndex = 0;
    private Hashtable m_hTagMap = new Hashtable();
    private Hashtable m_missedhTagMap = new Hashtable();
    private PseudoData m_PseudoData;

    private int i_subCount = 0;

    private static ArrayList<String> m_specialTags = new ArrayList<String>();
    // GBS-3722
    public static final String R_MT_IDENTIFIER_LEADING_BPT = "<bpt internal=\"yes\" i=\"\\d+\" type=\"mtlid\">&lt;mtlid&gt;</bpt>";
    public static final String R_MT_IDENTIFIER_LEADING_EPT = "<ept i=\"\\d+\">&lt;/mtlid&gt;</ept>";
    public static final String R_MT_IDENTIFIER_TRAILING_BPT = "<bpt internal=\"yes\" i=\"\\d+\" type=\"mttid\">&lt;mttid&gt;</bpt>";
    public static final String R_MT_IDENTIFIER_TRAILING_EPT = "<ept i=\"\\d+\">&lt;/mttid&gt;</ept>";
    public static final String R_TEXT = "([\\d\\D]*?)";

    public static final String R_MT_IDENTIFIER_LEADING = R_MT_IDENTIFIER_LEADING_BPT
            + R_TEXT + R_MT_IDENTIFIER_LEADING_EPT;
    public static final String R_MT_IDENTIFIER_TRAILING = R_MT_IDENTIFIER_TRAILING_BPT
            + R_TEXT + R_MT_IDENTIFIER_TRAILING_EPT;

    public static Pattern P_MT_IDENTIFIER_LEADING = Pattern
            .compile(R_MT_IDENTIFIER_LEADING);
    public static Pattern P_MT_IDENTIFIER_TRAILING = Pattern
            .compile(R_MT_IDENTIFIER_TRAILING);

    static
    {
        m_specialTags.add("br");
        m_specialTags.add("/br");
    }

    /**
     * Returns the input string with PTags inserted in place of TMX.
     */
    public PseudoData getResult()
    {
        String s = m_rootElement.getText();

        // format for the presentation of sub flows
        if (m_vSubflows.size() > 0)
        {
            s += "\n\n";
        }

        // append sub flows
        for (int i = 0; i < m_vSubflows.size(); i++)
        {
            s += (String) m_vSubflows.elementAt(i) + "\n";
        }

        XmlEntities xmlDecoder = new XmlEntities();
        m_PseudoData.setPTagSourceString(xmlDecoder.decodeString(s));

        // The Pseudo2Native map is built in the stop event.
        return m_PseudoData;
    }

    /**
     * Handles diplomat basic parser Start event.
     */
    public void handleStart() throws DiplomatBasicParserException
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
    public void handleStop() throws DiplomatBasicParserException
    {
        // must be first
        m_PseudoData.m_hPseudo2TmxMap = m_hTagMap;
        m_PseudoData.m_missedhPseudo2TmxMap = m_missedhTagMap;
        m_PseudoData.setBaseUniqueIndex(m_nextUniqIndex + 1);

        // must be second
        Pseudo2NativeMapper Mapper = new Pseudo2NativeMapper();
        Mapper.buildMap(m_PseudoData);
    }

    /**
     * End-tag event handler.
     * 
     * @param p_strName
     *            - the literal tag name
     * @param p_strOriginalTag
     *            - the complete raw token from the parser
     */
    public void handleEndTag(String p_strName, String p_strOriginalTag)
    {
        if (i_subCount > 0)
        {
            if (p_strName.equals("sub"))
            {
                --i_subCount;
            }

            // handle as text then abort
            handleText(p_strOriginalTag);
            return;
        }

        Tmx2PseudoHandlerElement selfElement = (Tmx2PseudoHandlerElement) m_elementStack
                .pop();

        Tmx2PseudoHandlerElement currentElement = (Tmx2PseudoHandlerElement) m_elementStack
                .peek();

        if (selfElement.type == SUB)
        {
            m_vSubflows.addElement(selfElement.getText()
                    + PseudoConstants.PSEUDO_OPEN_TAG
                    + PseudoConstants.PSEUDO_END_TAG_MARKER
                    + selfElement.tagName + PseudoConstants.PSEUDO_CLOSE_TAG);
        }
        else
        {
            // BJB================
            // put original code inside "if" to keep from adding
            // unnumbered tags to hash unnumbereds one are addable and
            // will be built on the fly.
            if (Character.isDigit(selfElement.tagName
                    .charAt(selfElement.tagName.length() - 1))
                    || selfElement.getText().contains(
                            " isFromOfficeContent=\"yes\"")
                    || isSepicalTag(selfElement))
            {
                m_hTagMap.put(selfElement.tagName, selfElement.getText()
                        + p_strOriginalTag);

                // Assuming <sub> cannot be included in <sub>
                for (Enumeration e = selfElement.subTags.elements(); e
                        .hasMoreElements();)
                {
                    currentElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
                    currentElement.append((String) e.nextElement());
                    currentElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);
                }
            }
            else
            {
                int len = m_PseudoData.getSrcCompleteTagList().size();
                if (len > 0)
                {
                    TagNode lastTag = (TagNode) m_PseudoData
                            .getSrcCompleteTagList().get(len - 1);
                    m_missedhTagMap.put("" + lastTag.getSourceListIndex(),
                            selfElement.getText() + p_strOriginalTag);
                }
            }
        }
    }

    private boolean isSepicalTag(Tmx2PseudoHandlerElement selfElement)
    {
        if (selfElement == null || selfElement.tagName == null)
        {
            return false;
        }

        if (selfElement.type == BPT || selfElement.type == EPT)
        {
            String tag = selfElement.tagName.toLowerCase();
            return m_specialTags.contains(tag);
        }

        return false;
    }

    /**
     * Start-tag event handler.
     * 
     * @param p_strTmxTagName
     *            - The literal tag name.
     * @param p_hAtributes
     *            - Tag attributes in the form of a hashtable.
     * @param p_strOriginalString
     *            - The complete raw token from the parser.
     */
    public void handleStartTag(String p_strTmxTagName,
            Properties p_hAttributes, String p_strOriginalString)
            throws DiplomatBasicParserException
    {
        Tmx2PseudoHandlerElement currentElement = (Tmx2PseudoHandlerElement) m_elementStack
                .peek();
        String isExtracted = (String) p_hAttributes.get("isTranslate");
        Tmx2PseudoHandlerElement selfElement = new Tmx2PseudoHandlerElement();

        /*
         * NOTE: parsing of sub flows is turned off. All subflow content is
         * treated as text. ================================
         */
        if (i_subCount > 0 || p_strTmxTagName.equals("sub"))
        {
            if (p_strTmxTagName.equals("sub"))
            {
                ++i_subCount;
            }

            // handle as text then abort
            handleText(p_strOriginalString);
            return;
        }
        else
        {
            m_elementStack.push(selfElement);
        }

        try
        {
            m_nextUniqIndex = Math.max(m_nextUniqIndex,
                    Integer.parseInt((String) p_hAttributes.get("i")));
        }
        catch (NumberFormatException e)
        {
            // Original code - intentionally empty
            // Some tags will not have an "i" attribute
        }
        
        if (m_PseudoData.isXliff20File())
        {
            String PTag = Tmx2Xliff20.getTag(p_strTmxTagName, p_hAttributes, p_strOriginalString, ept2bpt, ept2bpt2);
            
            try
            {
                // capture source data
                m_PseudoData.addSourceTagItem(new TagNode(p_strTmxTagName,
                        PTag, p_hAttributes));
            }
            catch (TagNodeException e)
            {
                throw new DiplomatBasicParserException(e.toString());
            }

            selfElement.type = IT;
            selfElement.setText(p_strOriginalString);
            selfElement.tagName = PTag;

            currentElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
            currentElement.append(PTag);
            currentElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);
            
            return;
        }

        if (p_strTmxTagName.equals("bpt"))
        {
            String PTag = null;

            // create tag name
            PTag = m_PseudoData.makePseudoTagName(p_strTmxTagName,
                    p_hAttributes, "g");

            try
            {
                // capture source data
                int i_SourceListIdx = m_PseudoData
                        .addSourceTagItem(new TagNode(p_strTmxTagName, PTag,
                                p_hAttributes));

                // capture ept to bpt mapping for later use to copy attributes
                m_hEpt2BptSrcIndexMap.put(p_hAttributes.get("i"),
                        String.valueOf(i_SourceListIdx));
            }
            catch (TagNodeException e)
            {
                throw new DiplomatBasicParserException(e.toString());
            }

            selfElement.type = BPT;
            selfElement.setText(p_strOriginalString);
            selfElement.tagName = PTag;

            if ("yes".equals(p_hAttributes.getProperty("internal")))
            {
                currentElement.append("[");
            }
            else
            {
                currentElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
                currentElement.append(PTag);
                currentElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);

                if (isExtracted != null && Boolean.parseBoolean(isExtracted))
                {
                    if (!isContainsThisTag(m_rootElement, PTag))
                    {
                        m_extractedStack.push("true");
                        m_rootElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
                        m_rootElement.append(PTag);
                        m_rootElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);
                    }
                }
            }

        }
        else if (p_strTmxTagName.equals("ept"))
        {
            isExtracted = (m_extractedStack.size() > 0) ? (String) m_extractedStack
                    .peek() : "false";

            // lookup index to the source bpt
            int i_SrcIdx = -1;
            TagNode BPTItem = null;
            String PTag = null;

            try
            {
                String vvv = m_hEpt2BptSrcIndexMap.get(p_hAttributes.get("i"));
                if (vvv == null)
                {
                    if (m_PseudoData.isXliffXlfFile())
                    {
                        PTag = PseudoConstants.PSEUDO_END_TAG_MARKER
                                + m_PseudoData.makePseudoTagName(
                                        p_strTmxTagName, p_hAttributes, "g");
                    }
                    else
                    {
                        throw new DiplomatBasicParserException();
                    }
                }
                else
                {
                    i_SrcIdx = Integer.parseInt(vvv);

                    BPTItem = m_PseudoData.getSrcTagItem(i_SrcIdx);

                    if (BPTItem == null)
                    {
                        throw new DiplomatBasicParserException();
                    }
                }
            }
            catch (NumberFormatException e)
            {
                throw new DiplomatBasicParserException(" : ept i=\""
                        + p_hAttributes.get("i")
                        + "\"  has no parent bpt in this segment.");
            }

            Hashtable atts = p_hAttributes;
            // build tag using bpt data
            if (BPTItem != null)
            {
                PTag = PseudoConstants.PSEUDO_END_TAG_MARKER
                        + BPTItem.getPTagName();
                atts = BPTItem.getAttributes();
            }

            try
            {
                // capture source data for ept (re-using bpt attributes)
                m_PseudoData.addSourceTagItem(new TagNode(p_strTmxTagName,
                        PTag, atts));
            }
            catch (TagNodeException e)
            {
                throw new DiplomatBasicParserException(e.toString());
            }

            selfElement.type = EPT;
            selfElement.setText(p_strOriginalString);
            selfElement.tagName = PTag;

            boolean appended = false;
            if (BPTItem != null)
            {
                if ("yes".equals(p_hAttributes.getProperty("internal")))
                {
                    currentElement.append("[");
                    appended = true;
                }
                else if ("yes".equals(BPTItem.getAttributes().get("internal")))
                {
                    currentElement.append("]");
                    appended = true;
                }
            }

            if (!appended)
            {
                currentElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
                currentElement.append(PTag);
                currentElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);

                if (isExtracted != null && Boolean.parseBoolean(isExtracted))
                {
                    if (!isContainsThisTag(m_rootElement, PTag))
                    {
                        // m_extractedStack.push("false");
                        m_extractedStack.pop();
                        m_rootElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
                        m_rootElement.append(PTag);
                        m_rootElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);
                    }
                }
            }

        }
        else
        // it, ut or ph
        {
            String type = (String) p_hAttributes.get("type");
            String PTag;

            // NOTE: FOR FUTURE "PH" ADDABLES

            // To fix a bug where an "it" with type "b/u/i" becomes an
            // unnumbered "x", we changed the last parameter in the
            // call to makePseudoTagName() below to true. This forces
            // the method to number all "IT", "UT" and "PH" tags
            // regardless of type ( spec. for system3 ).

            // create pseudo tag name
            PTag = m_PseudoData.makePseudoTagName(p_strTmxTagName,
                    p_hAttributes, "x", true);
            // PTag = m_PseudoData.makePseudoTagName(
            // p_strTmxTagName, p_hAttributes, "x");

            try
            {
                // capture source data
                m_PseudoData.addSourceTagItem(new TagNode(p_strTmxTagName,
                        PTag, p_hAttributes));
            }
            catch (TagNodeException e)
            {
                throw new DiplomatBasicParserException(e.toString());
            }

            selfElement.type = IT;
            selfElement.setText(p_strOriginalString);
            selfElement.tagName = PTag;

            currentElement.append(PseudoConstants.PSEUDO_OPEN_TAG);
            currentElement.append(PTag);
            currentElement.append(PseudoConstants.PSEUDO_CLOSE_TAG);
        }
    }

    private boolean isContainsThisTag(Tmx2PseudoHandlerElement root, String tag)
    {
        return root.getText().indexOf("[" + tag + "]") != -1;
    }

    /**
     * Text event handler.
     * 
     * @param p_strText
     *            - the next text chunk from between the tags
     */
    public void handleText(String p_strText)
    {
        Tmx2PseudoHandlerElement currentElement = (Tmx2PseudoHandlerElement) m_elementStack
                .peek();

        currentElement.append(p_strText);
    }

    public void handleIsExtractedText(String substring)
    {
        m_rootElement.append(substring);
    }

    /**
     * TMX event handler, called by the framework. Don't worry about it...
     */
    public Tmx2PseudoHandler(PseudoData p_PseudoData)
    {
        super();

        m_PseudoData = p_PseudoData;
        // now initialized in that class
        // m_rootElement.setText("");
        m_elementStack.push(m_rootElement);
    }

    public String preProcessInternalText(String segment)
            throws DiplomatBasicParserException
    {
        return preProcessInternalText(segment, null);
    }

    public String preProcessInternalText(String segment, InternalTag internalTag)
            throws DiplomatBasicParserException
    {
        if (internalTag == null)
        {
            internalTag = new OnlineInternalTag();
        }

        InternalTextUtil util = new InternalTextUtil(internalTag);
        InternalTexts texts = util.preProcessInternalText(segment);
        try
        {
            m_PseudoData.addInternalTags(texts);
        }
        catch (TagNodeException e)
        {
            throw new DiplomatBasicParserException(e.getMessage());
        }

        return texts.getSegment();
    }

    /**
     * Adds MT Identifiers (leading&trailing) to source tag list so that they
     * will be taken as mandatory tags during the error checker.
     * 
     * @since GBS-3722
     */
    public void setMTIdentifiers(String segment)
            throws DiplomatBasicParserException
    {
        if (m_PseudoData.getMTIdentifierList().size() > 0)
        {
            // already added, just return.
            return;
        }
        try
        {
            Matcher m = P_MT_IDENTIFIER_LEADING.matcher(segment);
            if (m.find())
            {
                m_PseudoData.addMTIdentifierLeading("[" + m.group(1) + "]",
                        m.group());
            }

            m = P_MT_IDENTIFIER_TRAILING.matcher(segment);
            if (m.find())
            {
                m_PseudoData.addMTIdentifierTrailing("[" + m.group(1) + "]",
                        m.group());
            }
        }
        catch (TagNodeException e)
        {
            throw new DiplomatBasicParserException(e.getMessage());
        }
    }
}
