package com.globalsight.ling.docproc.extractor.xml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlFilterTags
{
    private List<XmlFilterTag> whiteSpacePreserveTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> embeddedTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> transAttrTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterTag> contentInclTags = new ArrayList<XmlFilterTag>();
    private List<XmlFilterCDataTag> cdataPostFilterTags = new ArrayList<XmlFilterCDataTag>();
    private List<XmlFilterEntity> entities = new ArrayList<XmlFilterEntity>();
    private List<XmlFilterProcessIns> processIns = new ArrayList<XmlFilterProcessIns>();
    private List<XmlFilterSidTag> sidTags = new ArrayList<XmlFilterSidTag>();

    public XmlFilterTags()
    {
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

    public void setWhiteSpacePreserveTags(List<XmlFilterTag> whiteSpacePreserveTags)
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

    public void setCdataPostFilterTags(List<XmlFilterCDataTag> cdataPostFilterTags)
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
}
