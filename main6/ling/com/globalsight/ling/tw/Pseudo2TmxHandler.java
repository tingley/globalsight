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

import java.util.Hashtable;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.docproc.IFormatNames;

public class Pseudo2TmxHandler implements PseudoBaseHandler
{
    private StringBuffer resultTmx = new StringBuffer();
    private StringBuffer resultNative = new StringBuffer();

    private PseudoData m_PseudoData;

    private Hashtable m_hPTag2NextUniqueIndex = null;
    private Hashtable m_hType2BptIndexStack = new Hashtable();
    private int m_nNextUniqueIndex;

    /**
     * Constructor
     * 
     * @param p_PData
     *            , the current PseudoData object.
     */
    public Pseudo2TmxHandler(PseudoData p_PData)
    {
        super();

        this.m_PseudoData = p_PData;
        this.m_nNextUniqueIndex = p_PData.getBaseUniqueIndex();
    }

    /**
     * Returns a TMX string which is the result of this PTag to TMX conversion.
     */
    public String getResult()
    {
        return resultTmx.toString();
    }

    /**
     * Returns a native string which is the result of this PTag to TMX
     * conversion.
     */
    public String getNativeResult()
    {
        return resultNative.toString();
    }

    /**
     * Handles the Pseudo Parsers "tag" event.
     * 
     * @param tagName
     *            - the pseudo tag content
     * @param originalString
     *            - the full tag including delimiters
     */
    public void processTag(String tagName, String originalString)
            throws PseudoParserException
    {
        // Lookup the TMX string
        String tmxString = (String) m_PseudoData.m_hPseudo2TmxMap.get(tagName);
        if (tmxString != null)
        {
            resultTmx.append(tmxString);
            return;
        }

        String prefixString = "";
        String sufferString = "";

        tmxString = m_PseudoData.getInternalTexts().get("[" + tagName + "]");
        if (tmxString == null)
        {
            // GBS-3722
            tmxString = m_PseudoData.getMTIdentifiers()
                    .get("[" + tagName + "]");
        }

        while (tmxString == null && tagName.startsWith("[[")
                && tagName.endsWith("]"))
        {
            prefixString += "[";
            tagName = tagName.substring(2, tagName.length() - 1);
            sufferString += "]";

            tmxString = (String) m_PseudoData.m_hPseudo2TmxMap.get(tagName);
            if (tmxString == null)
                tmxString = m_PseudoData.getInternalTexts().get(
                        "[" + tagName + "]");
        }

        if (tmxString != null)
        {
            resultTmx.append(prefixString).append(tmxString)
                    .append(sufferString);
            return;
        }
        
        if (m_PseudoData.isXliff20File())
        {
            if (tagName.startsWith("g"))
            {
                String i = tagName.substring(1);
                tmxString = (String) m_PseudoData.m_missedhPseudo2TmxMap.get(i);
                
                if (tmxString != null)
                {
                    resultTmx.append(tmxString);
                    return;
                }
            }
        }

        // If not found in unique source mappings, try looking in
        // the addables map. The I Attribute for addables
        // automatically starts one above the highest index that
        // was in the original source.
        tmxString = getAddableTMX(tagName);
        if (tmxString == null || tmxString.length() == 0)
        {
            // This should have been taken care of by the pseudo
            // error checker. This should have been either a
            // well-formed error as the result of trying to match
            // a ept index to a bpt or, an illegal tag error.
            tmxString = "";
        }

        // Append the TMX string
        resultTmx.append(tmxString);

    }

    /**
     * Handles the parsers Text event
     * 
     * @param strText
     *            , the next chunk of text from the text parser.
     */
    public void processText(String strText)
    {
        resultTmx.append(strText);
        resultNative.append(strText);
    }

    /**
     * <p>
     * Returns the full TMX representation of an addable PTag.
     * </p>
     * 
     * <p>
     * The TMX "i" attribute is automatically adjusted to be unique within the
     * segment.
     * </p>
     * 
     * @param p_PTag
     *            - the addable PTag you want to search for.
     * @return the TMX representation of the PTag if found, otherwise null.
     */
    public String getAddableTMX(String p_PTag)
    {
        StringBuffer tmp = new StringBuffer();
        String pTagName;
        String tmxTagName;
        String tmxContent;
        String tmxPairIndex = "";
        PseudoOverrideMapItem POMI;
        boolean isPaired = false;
        boolean isStartTag = false;
        String overrideMapKey = (String) m_PseudoData.isAddableTag(p_PTag);
        TagNode tagnode = m_PseudoData.findSrcItemByTrgName(p_PTag);
        XmlEntities codec = new XmlEntities();
        String dataType = m_PseudoData.getDataType();

        if (overrideMapKey == null)
        {
            return null;
        }
        else
        {
            POMI = (PseudoOverrideMapItem) m_PseudoData
                    .getOverrideMapItem(overrideMapKey);
        }

        boolean isRealPaired = (tagnode != null && !POMI.m_bPaired && tagnode
                .isPaired());

        // now BUILD the string using the addable's attributes.

        // detect/get the tmx data for a PAIRED END ptag
        //
        // - a paired end begins with a slash, others <ph><it><ut> do
        // not have an end tag at all.
        if (p_PTag.startsWith(String
                .valueOf(PseudoConstants.PSEUDO_END_TAG_MARKER)))
        {
            if (isRealPaired)
            {
                tmxTagName = tagnode.getTmxType();
                tmxContent = tagnode.getPTagName();
                tmxContent = "<" + tmxContent + ">";
                tmxContent = codec.encodeString(tmxContent);
            }
            else
            {
                tmxTagName = (String) POMI.m_hAttributes
                        .get(PseudoConstants.ADDABLE_TMX_ENDPAIRTAG);
                tmxContent = codec.encodeString((String) POMI.m_hAttributes
                        .get(PseudoConstants.ADDABLE_ENDPAIR_HTML_CONTENT));

                // GBS-3460
                Hashtable tmxMissedMap = m_PseudoData.getMissedPseudo2TmxMap();
                if (tagnode == null
                        && dataType != null
                        && (IFormatNames.FORMAT_HTML.equals(dataType) || IFormatNames.FORMAT_XML
                                .equals(dataType)))
                {
                    // newly added tag
                    tmxContent = codec.encodeString(tmxContent);
                }
                else if (tagnode != null && tmxMissedMap != null
                        && tmxMissedMap.size() > 0)
                {
                    String oriTagString = (String) tmxMissedMap.get(""
                            + tagnode.getSourceListIndex());
                    if (oriTagString != null)
                    {
                        Pattern p = Pattern
                                .compile("<[^>]+>([\\d\\D]+)</[^>]+>");
                        Matcher m = p.matcher(oriTagString);
                        if (m.find())
                        {
                            tmxContent = m.group(1);
                        }
                    }
                }
            }

            // Retrieve the I attribute value from the corresponding
            // stack.
            Stack s = (Stack) m_hType2BptIndexStack.get(p_PTag.substring(1)
                    .toLowerCase());

            // NOTE: well-formed test should catch missing bpt - so
            // stack should exist
            tmxPairIndex = (String) s.pop();
        }
        else
        {
            if (isRealPaired)
            {
                tmxTagName = tagnode.getTmxType();
                tmxContent = tagnode.getPTagName();
                tmxContent = "<" + tmxContent + ">";
                tmxContent = codec.encodeString(tmxContent);
            }
            else
            {
                // get the tmx data for a START ptag.
                // This is for either an underlying UNPAIRED(<ph><it><ut>)
                // or a PAIRED(<bpt><ept>).
                tmxTagName = (String) POMI.m_hAttributes
                        .get(PseudoConstants.ADDABLE_TMX_TAG);
                tmxContent = codec.encodeString((String) POMI.m_hAttributes
                        .get(PseudoConstants.ADDABLE_HTML_CONTENT));

                // GBS-3460
                Hashtable tmxMissedMap = m_PseudoData.getMissedPseudo2TmxMap();
                if (tagnode == null
                        && dataType != null
                        && (IFormatNames.FORMAT_HTML.equals(dataType) || IFormatNames.FORMAT_XML
                                .equals(dataType)))
                {
                    // newly added tag
                    tmxContent = codec.encodeString(tmxContent);
                }
                else if (tagnode != null && tmxMissedMap != null
                        && tmxMissedMap.size() > 0)
                {
                    String oriTagString = (String) tmxMissedMap.get(""
                            + tagnode.getSourceListIndex());
                    if (oriTagString != null)
                    {
                        Pattern p = Pattern
                                .compile("<[^>]+>([\\d\\D]+)</[^>]+>");
                        Matcher m = p.matcher(oriTagString);
                        if (m.find())
                        {
                            tmxContent = m.group(1);
                        }
                    }
                }
            }
            isStartTag = true;

            // if paired type, manage the Stack for out going I
            // attribute.
            if (POMI.m_bPaired || (tagnode != null && tagnode.isPaired()))
            {
                // Push next index to stack
                Stack s = (Stack) m_hType2BptIndexStack.get(p_PTag
                        .toLowerCase());

                if (s == null)
                {
                    // create stack
                    s = new Stack();
                    m_hType2BptIndexStack.put(p_PTag.toLowerCase(), s);
                }

                m_nNextUniqueIndex++;
                tmxPairIndex = Integer.toString(m_nNextUniqueIndex);
                s.push(tmxPairIndex);
            }
        }

        if (tmxTagName == null)
        {
            // should use an exception -- ..
            return null;
        }

        // Build it
        // NOTE: I switched the order of "type" and "i" attrib
        // for Nils on 1/16/01.
        // This should be beta build 53 or 54
        tmp.append('<');
        tmp.append(tmxTagName);
        tmp.append(' ');

        if (POMI.m_bPaired || isRealPaired)
        {
            tmp.append("i=\"");
            tmp.append(tmxPairIndex);
            tmp.append("\" ");
        }

        if (isStartTag)
        {
            tmp.append("type=\"");
            tmp.append(POMI.m_hAttributes.get(PseudoConstants.ADDABLE_TMX_TYPE));
            tmp.append("\" ");

            tmp.append("erasable=\"");
            tmp.append(POMI.m_hAttributes
                    .get(PseudoConstants.ADDABLE_ATTR_ERASABLE));
            tmp.append("\"");

            // tmp.append("movable=\"" + POMI.m_hAttributes.get(
            // PseudoConstants.ADDABLE_ATTR_MOVABLE) + "\" ");
        }

        tmp.append('>');
        tmp.append(tmxContent);
        tmp.append("</");
        tmp.append(tmxTagName);
        tmp.append(">");

        return tmp.toString();
    }
}
