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
package com.globalsight.cxe.util.fileImport.eventFlow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "preMergeEvent", "postMergeEvent",
        "batchInfo", "exportBatchInfo", "source", "target", "da", "gsamd", "category",
        "capMessageId", "cxeRequestType" })
@XmlRootElement(name = "eventFlowXml")
public class EventFlowXml implements Serializable, Cloneable
{
    private static final long serialVersionUID = 2456941039805423735L;
    @XmlElement(required = true)
    protected String preMergeEvent;
    @XmlElement(required = true)
    protected String postMergeEvent;
    @XmlElement(required = true)
    protected BatchInfo batchInfo;
    @XmlElement(required = true)
    protected ExportBatchInfo exportBatchInfo;
    @XmlElement(required = true)
    protected Source source;
    protected Target target;
    protected List<Da> da;
    protected Gsamd gsamd;
    protected List<Category> category;
    protected String capMessageId;
    protected String cxeRequestType;

    /**
     * Gets the value of the preMergeEvent property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPreMergeEvent()
    {
        return preMergeEvent;
    }

    /**
     * Sets the value of the preMergeEvent property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPreMergeEvent(String value)
    {
        this.preMergeEvent = value;
    }

    /**
     * Gets the value of the postMergeEvent property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPostMergeEvent()
    {
        return postMergeEvent;
    }

    /**
     * Sets the value of the postMergeEvent property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPostMergeEvent(String value)
    {
        this.postMergeEvent = value;
    }

    /**
     * Gets the value of the batchInfo property.
     * 
     * @return possible object is {@link BatchInfo }
     * 
     */
    public BatchInfo getBatchInfo()
    {
        return batchInfo;
    }

    /**
     * Sets the value of the batchInfo property.
     * 
     * @param value
     *            allowed object is {@link BatchInfo }
     * 
     */
    public void setBatchInfo(BatchInfo value)
    {
        this.batchInfo = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return possible object is {@link Source }
     * 
     */
    public Source getSource()
    {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *            allowed object is {@link Source }
     * 
     */
    public void setSource(Source value)
    {
        this.source = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return possible object is {@link Target }
     * 
     */
    public Target getTarget()
    {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *            allowed object is {@link Target }
     * 
     */
    public void setTarget(Target value)
    {
        this.target = value;
    }

    /**
     * Gets the value of the da property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the da property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDa().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Da }
     * 
     * 
     */
    public List<Da> getDa()
    {
        if (da == null)
        {
            da = new ArrayList<Da>();
        }
        return this.da;
    }

    /**
     * Gets the value of the gsamd property.
     * 
     * @return possible object is {@link Gsamd }
     * 
     */
    public Gsamd getGsamd()
    {
        return gsamd;
    }

    /**
     * Sets the value of the gsamd property.
     * 
     * @param value
     *            allowed object is {@link Gsamd }
     * 
     */
    public void setGsamd(Gsamd value)
    {
        this.gsamd = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the category property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getCategory().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Category }
     * 
     * 
     */
    public List<Category> getCategory()
    {
        if (category == null)
        {
            category = new ArrayList<Category>();
        }
        return this.category;
    }

    /**
     * Gets the value of the capMessageId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCapMessageId()
    {
        return capMessageId;
    }

    /**
     * Sets the value of the capMessageId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setCapMessageId(String value)
    {
        this.capMessageId = value;
    }

    /**
     * Gets the value of the cxeRequestType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCxeRequestType()
    {
        return cxeRequestType;
    }

    /**
     * Sets the value of the cxeRequestType property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setCxeRequestType(String value)
    {
        this.cxeRequestType = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public EventFlowXml clone() throws CloneNotSupportedException
    {
        // need deep clone.
        ByteArrayOutputStream  byteOut = new ByteArrayOutputStream();             
        ObjectOutputStream out;
        try
        {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(this);                    
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());             
            ObjectInputStream in = new ObjectInputStream(byteIn);  
            
            return (EventFlowXml) in.readObject();
        } 
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }             
        
        return (EventFlowXml) super.clone();
    }

    public String getDisplayName()
    {
        return batchInfo.getDisplayName();
    }

    /**
     * @return the exportBatchInfo
     */
    public ExportBatchInfo getExportBatchInfo()
    {
        return exportBatchInfo;
    }

    /**
     * @param exportBatchInfo
     *            the exportBatchInfo to set
     */
    public void setExportBatchInfo(ExportBatchInfo exportBatchInfo)
    {
        this.exportBatchInfo = exportBatchInfo;
    }
    
    public Category getCategory(String p_categoryName)
    {
        if (category == null)
            return null;

        for (Category c : category)
        {
            if (c.getName().equals(p_categoryName))
            {
                return c;
            }
        }
        return null;
    }
    
    public String getSourceLocale()
    {
        Source s = getSource();
        if (s == null)
            return null;
        
        return s.getLocale();
    }
    
    public String getTargetLocale()
    {
        Target t = getTarget();
        if (t == null)
            return null;
        
        return t.getLocale();
    }
    
    public String getValue(String name)
    {
        for (Da d : getDa())
        {
            if (name.equalsIgnoreCase(d.getName()))
            {
                List<Dv> dvs = d.getDv();
                
                if (dvs.size() > 0)
                    return dvs.get(0).getvalue();
                
                return null;
            }
        }
        
        for (Category c : getCategory())
        {
            String value = c.getValue(name);
            if (value != null)
                return value;
        }
        
        return null;
    }
}
