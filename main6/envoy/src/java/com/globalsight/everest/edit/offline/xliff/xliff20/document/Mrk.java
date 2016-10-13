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
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
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
 *       &lt;group ref="{urn:oasis:names:tc:xliff:document:2.0}inline" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="translate" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" />
 *       &lt;attribute name="type" type="{urn:oasis:names:tc:xliff:document:2.0}attrType_typeForMrk" />
 *       &lt;attribute name="ref" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "content" })
@XmlRootElement(name = "mrk")
public class Mrk
{

    @XmlElementRefs({
            @XmlElementRef(name = "sc", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Sc.class),
            @XmlElementRef(name = "cp", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Cp.class),
            @XmlElementRef(name = "mrk", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Mrk.class),
            @XmlElementRef(name = "sm", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Sm.class),
            @XmlElementRef(name = "em", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Em.class),
            @XmlElementRef(name = "ec", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Ec.class),
            @XmlElementRef(name = "ph", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Ph.class),
            @XmlElementRef(name = "pc", namespace = "urn:oasis:names:tc:xliff:document:2.0", type = Pc.class) })
    @XmlMixed
    protected List<Object> content;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute
    protected YesNo translate;
    @XmlAttribute
    protected String type;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String ref;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String value;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

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
     * {@link Pc } {@link Ph }
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
     * Gets the value of the id property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getId()
    {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setId(String value)
    {
        this.id = value;
    }

    /**
     * Gets the value of the translate property.
     * 
     * @return possible object is {@link YesNo }
     * 
     */
    public YesNo getTranslate()
    {
        return translate;
    }

    /**
     * Sets the value of the translate property.
     * 
     * @param value
     *            allowed object is {@link YesNo }
     * 
     */
    public void setTranslate(YesNo value)
    {
        this.translate = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setType(String value)
    {
        this.type = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getRef()
    {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setRef(String value)
    {
        this.ref = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue()
    {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setValue(String value)
    {
        this.value = value;
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
