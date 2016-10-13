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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "da" })
@XmlRootElement(name = "category")
public class Category implements Serializable
{
    private static final long serialVersionUID = -1306434820520235827L;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    protected List<Da> da;

    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setName(String value)
    {
        this.name = value;
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
    
    public Da getDaByName(String name)
    {
        for (Da d : getDa())
        {
            if (name.equalsIgnoreCase(d.getName()))
            {
                return d;
            }
        }
        
        return null;
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
        
        return null;
    }

    public String getPostMergeEvent()
    {
        for (Da d : getDa())
        {
            if ("postMergeEvent".equalsIgnoreCase(d.getName()))
            {
                List<Dv> dvs = d.getDv();
                
                if (dvs.size() > 0)
                    return dvs.get(0).getvalue();
                
                return null;
            }
        }
        
        return null;
    }
    
    public void addValue(String name, String valie)
    {
        Da da = new Da();
        da.setName(name);
        Dv dv = new Dv();
        dv.setvalue(valie);
        da.getDv().add(dv);
        
        getDa().add(da);
    }
}
