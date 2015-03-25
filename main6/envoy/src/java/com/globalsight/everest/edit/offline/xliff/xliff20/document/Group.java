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
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;

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
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}notes" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}unit"/>
 *           &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}group"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="canResegment" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" />
 *       &lt;attribute name="translate" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" />
 *       &lt;attribute name="srcDir" type="{urn:oasis:names:tc:xliff:document:2.0}dirValue" />
 *       &lt;attribute name="trgDir" type="{urn:oasis:names:tc:xliff:document:2.0}dirValue" />
 *       &lt;attribute name="type" type="{urn:oasis:names:tc:xliff:document:2.0}userDefinedValue" />
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}space"/>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "any", "notes", "unitOrGroup" })
@XmlRootElement(name = "group")
public class Group
{

    @XmlAnyElement(lax = true)
    protected List<Object> any;
    protected Notes notes;
    @XmlElements({ @XmlElement(name = "group", type = Group.class),
            @XmlElement(name = "unit", type = Unit.class) })
    protected List<Object> unitOrGroup;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String name;
    @XmlAttribute
    protected YesNo canResegment;
    @XmlAttribute
    protected YesNo translate;
    @XmlAttribute
    protected DirValue srcDir;
    @XmlAttribute
    protected DirValue trgDir;
    @XmlAttribute
    protected String type;
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String space;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny()
    {
        if (any == null)
        {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the notes property.
     * 
     * @return possible object is {@link Notes }
     * 
     */
    public Notes getNotes()
    {
        return notes;
    }

    /**
     * Sets the value of the notes property.
     * 
     * @param value
     *            allowed object is {@link Notes }
     * 
     */
    public void setNotes(Notes value)
    {
        this.notes = value;
    }

    /**
     * Gets the value of the unitOrGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the unitOrGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getUnitOrGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Group }
     * {@link Unit }
     * 
     * 
     */
    public List<Object> getUnitOrGroup()
    {
        if (unitOrGroup == null)
        {
            unitOrGroup = new ArrayList<Object>();
        }
        return this.unitOrGroup;
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
     * Gets the value of the canResegment property.
     * 
     * @return possible object is {@link YesNo }
     * 
     */
    public YesNo getCanResegment()
    {
        return canResegment;
    }

    /**
     * Sets the value of the canResegment property.
     * 
     * @param value
     *            allowed object is {@link YesNo }
     * 
     */
    public void setCanResegment(YesNo value)
    {
        this.canResegment = value;
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
     * Gets the value of the srcDir property.
     * 
     * @return possible object is {@link DirValue }
     * 
     */
    public DirValue getSrcDir()
    {
        return srcDir;
    }

    /**
     * Sets the value of the srcDir property.
     * 
     * @param value
     *            allowed object is {@link DirValue }
     * 
     */
    public void setSrcDir(DirValue value)
    {
        this.srcDir = value;
    }

    /**
     * Gets the value of the trgDir property.
     * 
     * @return possible object is {@link DirValue }
     * 
     */
    public DirValue getTrgDir()
    {
        return trgDir;
    }

    /**
     * Sets the value of the trgDir property.
     * 
     * @param value
     *            allowed object is {@link DirValue }
     * 
     */
    public void setTrgDir(DirValue value)
    {
        this.trgDir = value;
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
