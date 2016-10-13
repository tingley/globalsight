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
package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.ling.common.srccomment.SrcCmtXmlComment;
import com.globalsight.ling.common.srccomment.SrcCmtXmlTag;

public class XmlFilterTags
{
    private List<XmlFilterTag> whiteSpacePreserveTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> embeddedTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> transAttrTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> contentInclTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> internalTag = new ArrayList<XmlFilterTag>();
    private List<XmlFilterCDataTag> cdataPostFilterTags = new ArrayList<XmlFilterCDataTag>();
    private List<XmlFilterEntity> entities = new ArrayList<XmlFilterEntity>();
    private List<XmlFilterProcessIns> processIns = new ArrayList<XmlFilterProcessIns>();
    private List<XmlFilterSidTag> sidTags = new ArrayList<XmlFilterSidTag>();
    private List<SrcCmtXmlComment> srcCmtXmlComment = new ArrayList<SrcCmtXmlComment>();
    private List<SrcCmtXmlTag> srcCmtXmlTag = new ArrayList<SrcCmtXmlTag>();

    public List<SrcCmtXmlTag> getSrcCmtXmlTag()
    {
        return srcCmtXmlTag;
    }

    public void setSrcCmtXmlTag(List<SrcCmtXmlTag> srcCmtXmlTag)
    {
        this.srcCmtXmlTag = srcCmtXmlTag;
    }

    public XmlFilterTags()
    {
    }

    public List<XmlFilterTag> getInternalTag()
    {
        return internalTag;
    }

    public void setIntenalTag(List<XmlFilterTag> internalTag)
    {
        this.internalTag = internalTag;
    }

    public List<XmlFilterTag> getEmbeddedTags()
    {
        return embeddedTags;
    }

    public void setEmbeddedTags(List<XmlFilterTag> embeddedTags)
    {
        this.embeddedTags = embeddedTags;
    }

    public List<XmlFilterTag> getWhiteSpacePreserveTags()
    {
        return whiteSpacePreserveTags;
    }

    public void setWhiteSpacePreserveTags(
            List<XmlFilterTag> whiteSpacePreserveTags)
    {
        this.whiteSpacePreserveTags = whiteSpacePreserveTags;
    }

    public List<XmlFilterTag> getTransAttrTags()
    {
        return transAttrTags;
    }

    public void setTransAttrTags(List<XmlFilterTag> transAttrTags)
    {
        this.transAttrTags = transAttrTags;
    }

    public List<XmlFilterTag> getContentInclTags()
    {
        return contentInclTags;
    }

    public void setContentInclTags(List<XmlFilterTag> contentInclTags)
    {
        this.contentInclTags = contentInclTags;
    }

    public List<XmlFilterCDataTag> getCdataPostFilterTags()
    {
        return cdataPostFilterTags;
    }

    public void setCdataPostFilterTags(
            List<XmlFilterCDataTag> cdataPostFilterTags)
    {
        this.cdataPostFilterTags = cdataPostFilterTags;
    }

    public List<XmlFilterEntity> getEntities()
    {
        return entities;
    }

    public void setEntities(List<XmlFilterEntity> entities)
    {
        this.entities = entities;
    }

    public List<XmlFilterProcessIns> getProcessIns()
    {
        return processIns;
    }

    public void setProcessIns(List<XmlFilterProcessIns> processIns)
    {
        this.processIns = processIns;
    }

    public List<XmlFilterSidTag> getSidTags()
    {
        return sidTags;
    }

    public void setSidTags(List<XmlFilterSidTag> sidTags)
    {
        this.sidTags = sidTags;
    }

    public List<SrcCmtXmlComment> getSrcCmtXmlComment()
    {
        return srcCmtXmlComment;
    }

    public void setSrcCmtXmlComment(List<SrcCmtXmlComment> srcCmtXmlComment)
    {
        this.srcCmtXmlComment = srcCmtXmlComment;
    }
}
