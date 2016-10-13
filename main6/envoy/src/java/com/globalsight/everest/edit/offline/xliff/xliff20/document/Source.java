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
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
 *       &lt;group ref="{urn:oasis:names:tc:xliff:document:2.0}inline" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/>
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}space"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "content" })
@XmlRootElement(name = "source")
public class Source
{

    @XmlElementRefs({
            @XmlElementRef(name = "sc", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Sc.class),
            @XmlElementRef(name = "cp", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Cp.class),
            @XmlElementRef(name = "mrk", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Mrk.class),
            @XmlElementRef(name = "sm", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Sm.class),
            @XmlElementRef(name = "em", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Em.class),
            @XmlElementRef(name = "ec", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Ec.class),
            @XmlElementRef(name = "pc", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Pc.class),
            @XmlElementRef(name = "ph", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Ph.class) })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    protected String lang;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;

    /**
     * Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Sc }
     * {@link Cp } {@link Mrk } {@link Sm } {@link String } {@link Em } {@link Ec }
     * {@link Ph } {@link Pc }
     * 
     * 
     */
    public List<Object> getContent()
    {
        if (content == null)
        {
            content = new ArrayList<Object>();
        }
        return this.content;
    }

    /**
     * Gets the value of the lang property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLang()
    {
        return lang;
    }

    /**
     * Sets the value of the lang property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setLang(String value)
    {
        this.lang = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSpace()
    {
        return space;
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

}
