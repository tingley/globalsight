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
package com.globalsight.ling.docproc;

import java.util.*;

import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.ling.docproc.extractor.xliff.WSConstants;
import com.globalsight.util.edit.EditUtil;

public abstract class Segmentable
{
    // Chunk of text (a set of segments)
    protected StringBuffer chunk = null;

    // List of SegmentNode. If it's null, it's not segmented.
    protected ArrayList segments = null;

    private int wordcount = 0;
    private String dataType = null;
    private String type = null;
    private String sid = null;
    private Map xliffAttributes = null;
    private boolean m_isPreserveWS = false;
    private String isLocalized = null;
    private int inddPageNum = 0;
    private String escapingChars;
    
    public int getInddPageNum()
    {
        return inddPageNum;
    }

    public void setInddPageNum(int inddPageNum)
    {
        this.inddPageNum = inddPageNum;
    }
    
    public String getEscapingChars()
    {
        return escapingChars;
    }

    public void setEscapingChars(String escapingChars)
    {
        this.escapingChars = EditUtil.encodeXmlEntities(escapingChars);
    }

    public boolean isPreserveWhiteSpace()
    {
        return m_isPreserveWS;
    }
    
    public void setPreserveWhiteSpace(boolean isPreserveWS)
    {
        m_isPreserveWS = isPreserveWS;
    }
    
    public String getChunk()
    {
        if (chunk == null)
        {
            return null;
        }

        return chunk.toString();
    }

    public String getDataType()
    {
        return dataType;
    }

    public String getType()
    {
        return type;
    }

    public int getWordcount()
    {
        return wordcount;
    }
    
    public void setXliffPart(Map p_part) {
        xliffAttributes = p_part;
    }
    
    public Map getXliffPart() {
        return xliffAttributes;
    }

    public float getTmScoreFromXlfPart()
    {
        Object score = null;
        if (xliffAttributes != null)
        {
            score = xliffAttributes.get(WSConstants.IWS_TM_SCORE);
        }
        if (score != null)
        {
            try
            {
                return Float.parseFloat((String) score);
            }
            catch (Exception e)
            {

            }
        }
        return 0;
    }

    public String getXliffPartByName()
    {
        if (xliffAttributes == null)
            return "";

        String result = (String) xliffAttributes.get("xliffPart");

        return result != null ? result : "";

    }

    private void segmentsToDiplomat(DiplomatAttribute diplomatAttribute,
            XmlWriter writer)
    {
        Properties attribs = new Properties();
        int id = 1;

        for (int i = 0, max = segments.size(); i < max; i++)
        {
            SegmentNode node = (SegmentNode) segments.get(i);

            attribs.clear();
            attribs.setProperty(DiplomatNames.Attribute.SEGMENTID, Integer
                    .toString(id++));
            if (node.getWordCount() != 0)
            {
                attribs.setProperty(DiplomatNames.Attribute.WORDCOUNT, Integer
                        .toString(node.getWordCount()));
            }
            
            if (m_isPreserveWS)
            {
                attribs.setProperty(DiplomatNames.Attribute.PRESERVEWHITESPACE, "yes");
            }
            
            if (node.getSrcComment() != null && !"".equals(node.getSrcComment().trim()))
            {
                attribs.setProperty("srcComment", node.getSrcComment());
            }

            writer.element(DiplomatNames.Element.SEGMENT, attribs, node
                    .getSegment(), false);
        }
    }

    public void appendChunk(String chunk)
    {
        this.chunk.append(chunk);
    }

    public void setChunk(String chunk)
    {
        if (chunk == null)
        {
            this.chunk = null;
        }
        else
        {
            this.chunk = new StringBuffer(chunk);
        }
    }

    public void setDataType(String dataType)
    {
        this.dataType = dataType;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setWordcount(int wordcount)
    {
        this.wordcount = wordcount;
    }

    protected void toDiplomatString(DiplomatAttribute diplomatAttribute,
            XmlWriter writer, String elementName)
    {
        Properties attribs = new Properties();
        attribs.setProperty(DiplomatNames.Attribute.BLOCKID, Integer
                .toString(diplomatAttribute.getId()));
        diplomatAttribute.incrementId();

        // when the datatype is different from the root datatype
        if (dataType != null
                && !dataType.equals(diplomatAttribute.getDataType()))
        {
            attribs.setProperty(DiplomatNames.Attribute.DATATYPE, dataType);
        }

        if (type != null)
        {
            attribs.setProperty(DiplomatNames.Attribute.TYPE, type);
        }

        if (wordcount != 0)
        {
            attribs.setProperty(DiplomatNames.Attribute.WORDCOUNT, Integer
                    .toString(wordcount));
        }

        if (sid != null)
        {
            attribs.setProperty("sid", sid);
        }
        
        if (inddPageNum != 0)
        {
            attribs.setProperty(DiplomatNames.Attribute.INDDPAGENUM,
                    "" + inddPageNum);
        }
        
        if (escapingChars != null && escapingChars.length() > 0)
        {
            attribs.setProperty(DiplomatNames.Attribute.ESCAPINGCHARS,
                    escapingChars);
        }
        
        if(xliffAttributes != null && xliffAttributes.size() > 0) {
            Set set = xliffAttributes.entrySet();
            Iterator i = set.iterator();
            
            while(i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                attribs.setProperty(me.getKey().toString(), 
                                    me.getValue().toString());
            }
        }

        // If it's not segmented, output it as is.
        if (segments == null)
        {
            writer.element(elementName, attribs, getChunk(), false);
        }

        // If it's segmented, output <segment> elements
        else
        {
            writer.startElement(elementName, attribs);
            segmentsToDiplomat(diplomatAttribute, writer);
            writer.endElement();
        }
    }

    public int getSegmentCount()
    {
        if (segments == null)
        {
            return 0;
        }

        return segments.size();
    }

    public String getSid()
    {
        return sid;
    }

    /**
     * As SID may contain "<", ">", "'", """ or standalone "&", need encode them
     * to avoid possible parse error. But the SID should not be encoded
     * repeatedly.
     * 
     * @param sid
     */
    public void setSid(String sid)
    {
        this.sid = EditUtil.encodeXmlEntities(sid);
    }

    public String getIsLocalized() {
        return this.isLocalized;
    }
    
    public void setIsLocalized(String p_isLocalized) {
        this.isLocalized = p_isLocalized;
    }
}
