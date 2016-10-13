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
@XmlRootElement(name = "source")
public class Source implements Serializable
{

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String name;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String dataSourceType;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String dataSourceId;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String formatType;
    @XmlAttribute(required = false)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String formatName;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String pageIsCxePreviewable;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String importRequestType;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String importInitiatorId;
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
     * Gets the value of the dataSourceType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDataSourceType()
    {
        return dataSourceType;
    }

    /**
     * Sets the value of the dataSourceType property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDataSourceType(String value)
    {
        this.dataSourceType = value;
    }

    /**
     * Gets the value of the dataSourceId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDataSourceId()
    {
        return dataSourceId;
    }

    /**
     * Sets the value of the dataSourceId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDataSourceId(String value)
    {
        this.dataSourceId = value;
    }

    /**
     * Gets the value of the formatType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getFormatType()
    {
        return formatType;
    }

    /**
     * Sets the value of the formatType property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setFormatType(String value)
    {
        this.formatType = value;
    }

    /**
     * Gets the value of the pageIsCxePreviewable property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPageIsCxePreviewable()
    {
        if (pageIsCxePreviewable == null)
        {
            return "false";
        } else
        {
            return pageIsCxePreviewable;
        }
    }

    /**
     * Sets the value of the pageIsCxePreviewable property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPageIsCxePreviewable(String value)
    {
        this.pageIsCxePreviewable = value;
    }

    /**
     * Gets the value of the importRequestType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getImportRequestType()
    {
        if (importRequestType == null)
        {
            return "l10n";
        } else
        {
            return importRequestType;
        }
    }

    /**
     * Sets the value of the importRequestType property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setImportRequestType(String value)
    {
        this.importRequestType = value;
    }

    /**
     * Gets the value of the importInitiatorId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getImportInitiatorId()
    {
        if (importInitiatorId == null)
        {
            return "";
        } else
        {
            return importInitiatorId;
        }
    }

    /**
     * Sets the value of the importInitiatorId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setImportInitiatorId(String value)
    {
        this.importInitiatorId = value;
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

    /**
     * @return the formatName
     */
    public String getFormatName()
    {
        return formatName;
    }

    /**
     * @param formatName
     *            the formatName to set
     */
    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
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
