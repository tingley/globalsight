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
package com.globalsight.everest.edit.offline.xliff.xliff20.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}file" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="srcLang" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="trgLang" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}space default="default""/>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "file" })
@XmlRootElement(name = "xliff")
public class Xliff
{

    @XmlElement(required = true)
    protected List<File> file;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String version;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String trgLang;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String srcLang;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the file property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the file property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getFile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link File }
     * 
     * 
     */
    public List<File> getFile()
    {
        if (file == null)
        {
            file = new ArrayList<File>();
        }
        return this.file;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setVersion(String value)
    {
        this.version = value;
    }

    /**
     * Gets the value of the srcLang property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSrcLang()
    {
        return srcLang;
    }

    /**
     * Sets the value of the srcLang property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSrcLang(String value)
    {
        this.srcLang = value;
    }

    /**
     * Gets the value of the trgLang property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTrgLang()
    {
        return trgLang;
    }

    /**
     * Sets the value of the trgLang property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setTrgLang(String value)
    {
        this.trgLang = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSpace()
    {
        if (space == null)
        {
            return "default";
        }
        else
        {
            return space;
        }
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSpace(String value)
    {
        this.space = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed
     * property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and the value is the string
     * value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute by
     * updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes()
    {
        return otherAttributes;
    }

}
