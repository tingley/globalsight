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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "locale", "charset", "da" })
@XmlRootElement(name = "target")
public class Target implements Serializable
{

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String databaseMode;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String previewUrl;
    @XmlElement(required = true)
    protected String locale;
    @XmlElement(required = true)
    protected String charset;
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
     * Gets the value of the databaseMode property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDatabaseMode()
    {
        if (databaseMode == null)
        {
            return "final";
        } else
        {
            return databaseMode;
        }
    }

    /**
     * Sets the value of the databaseMode property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDatabaseMode(String value)
    {
        this.databaseMode = value;
    }

    /**
     * Gets the value of the previewUrl property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPreviewUrl()
    {
        if (previewUrl == null)
        {
            return "false";
        } else
        {
            return previewUrl;
        }
    }

    /**
     * Sets the value of the previewUrl property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPreviewUrl(String value)
    {
        this.previewUrl = value;
    }

    /**
     * Gets the value of the locale property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLocale()
    {
        return locale;
    }

    /**
     * Sets the value of the locale property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setLocale(String value)
    {
        this.locale = value;
    }

    /**
     * Gets the value of the charset property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCharset()
    {
        return charset;
    }

    /**
     * Sets the value of the charset property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setCharset(String value)
    {
        this.charset = value;
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
}
