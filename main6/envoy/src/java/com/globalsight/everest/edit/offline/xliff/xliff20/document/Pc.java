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
 *       &lt;attribute name="canCopy" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" default="yes" />
 *       &lt;attribute name="canDelete" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" default="yes" />
 *       &lt;attribute name="canOverlap" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" />
 *       &lt;attribute name="canReorder" type="{urn:oasis:names:tc:xliff:document:2.0}yesNoFirstNo" default="yes" />
 *       &lt;attribute name="copyOf" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="dispEnd" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="dispStart" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="equivEnd" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="equivStart" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="dataRefEnd" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="dataRefStart" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="subFlowsEnd" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS" />
 *       &lt;attribute name="subFlowsStart" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS" />
 *       &lt;attribute name="subType" type="{urn:oasis:names:tc:xliff:document:2.0}userDefinedValue" />
 *       &lt;attribute name="type" type="{urn:oasis:names:tc:xliff:document:2.0}attrType_type" />
 *       &lt;attribute name="dir" type="{urn:oasis:names:tc:xliff:document:2.0}dirValue" />
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
@XmlRootElement(name = "pc")
public class Pc
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
    @XmlAttribute
    protected YesNo canCopy;
    @XmlAttribute
    protected YesNo canDelete;
    @XmlAttribute
    protected YesNo canOverlap;
    @XmlAttribute
    protected YesNoFirstNo canReorder;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String copyOf;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String dispEnd;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String dispStart;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String equivEnd;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String equivStart;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String dataRefEnd;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String dataRefStart;
    @XmlAttribute
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> subFlowsEnd;
    @XmlAttribute
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> subFlowsStart;
    @XmlAttribute
    protected String subType;
    @XmlAttribute
    protected AttrTypeType type;
    @XmlAttribute
    protected DirValue dir;
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
     * Gets the value of the canCopy property.
     * 
     * @return possible object is {@link YesNo }
     * 
     */
    public YesNo getCanCopy()
    {
        if (canCopy == null)
        {
            return YesNo.YES;
        }
        else
        {
            return canCopy;
        }
    }

    /**
     * Sets the value of the canCopy property.
     * 
     * @param value
     *            allowed object is {@link YesNo }
     * 
     */
    public void setCanCopy(YesNo value)
    {
        this.canCopy = value;
    }

    /**
     * Gets the value of the canDelete property.
     * 
     * @return possible object is {@link YesNo }
     * 
     */
    public YesNo getCanDelete()
    {
        if (canDelete == null)
        {
            return YesNo.YES;
        }
        else
        {
            return canDelete;
        }
    }

    /**
     * Sets the value of the canDelete property.
     * 
     * @param value
     *            allowed object is {@link YesNo }
     * 
     */
    public void setCanDelete(YesNo value)
    {
        this.canDelete = value;
    }

    /**
     * Gets the value of the canOverlap property.
     * 
     * @return possible object is {@link YesNo }
     * 
     */
    public YesNo getCanOverlap()
    {
        return canOverlap;
    }

    /**
     * Sets the value of the canOverlap property.
     * 
     * @param value
     *            allowed object is {@link YesNo }
     * 
     */
    public void setCanOverlap(YesNo value)
    {
        this.canOverlap = value;
    }

    /**
     * Gets the value of the canReorder property.
     * 
     * @return possible object is {@link YesNoFirstNo }
     * 
     */
    public YesNoFirstNo getCanReorder()
    {
        if (canReorder == null)
        {
            return YesNoFirstNo.YES;
        }
        else
        {
            return canReorder;
        }
    }

    /**
     * Sets the value of the canReorder property.
     * 
     * @param value
     *            allowed object is {@link YesNoFirstNo }
     * 
     */
    public void setCanReorder(YesNoFirstNo value)
    {
        this.canReorder = value;
    }

    /**
     * Gets the value of the copyOf property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCopyOf()
    {
        return copyOf;
    }

    /**
     * Sets the value of the copyOf property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setCopyOf(String value)
    {
        this.copyOf = value;
    }

    /**
     * Gets the value of the dispEnd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDispEnd()
    {
        return dispEnd;
    }

    /**
     * Sets the value of the dispEnd property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDispEnd(String value)
    {
        this.dispEnd = value;
    }

    /**
     * Gets the value of the dispStart property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDispStart()
    {
        return dispStart;
    }

    /**
     * Sets the value of the dispStart property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDispStart(String value)
    {
        this.dispStart = value;
    }

    /**
     * Gets the value of the equivEnd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getEquivEnd()
    {
        return equivEnd;
    }

    /**
     * Sets the value of the equivEnd property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setEquivEnd(String value)
    {
        this.equivEnd = value;
    }

    /**
     * Gets the value of the equivStart property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getEquivStart()
    {
        return equivStart;
    }

    /**
     * Sets the value of the equivStart property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setEquivStart(String value)
    {
        this.equivStart = value;
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
     * Gets the value of the dataRefEnd property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDataRefEnd()
    {
        return dataRefEnd;
    }

    /**
     * Sets the value of the dataRefEnd property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDataRefEnd(String value)
    {
        this.dataRefEnd = value;
    }

    /**
     * Gets the value of the dataRefStart property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDataRefStart()
    {
        return dataRefStart;
    }

    /**
     * Sets the value of the dataRefStart property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDataRefStart(String value)
    {
        this.dataRefStart = value;
    }

    /**
     * Gets the value of the subFlowsEnd property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the subFlowsEnd property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getSubFlowsEnd().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getSubFlowsEnd()
    {
        if (subFlowsEnd == null)
        {
            subFlowsEnd = new ArrayList<String>();
        }
        return this.subFlowsEnd;
    }

    /**
     * Gets the value of the subFlowsStart property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the subFlowsStart property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getSubFlowsStart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getSubFlowsStart()
    {
        if (subFlowsStart == null)
        {
            subFlowsStart = new ArrayList<String>();
        }
        return this.subFlowsStart;
    }

    /**
     * Gets the value of the subType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSubType()
    {
        return subType;
    }

    /**
     * Sets the value of the subType property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSubType(String value)
    {
        this.subType = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return possible object is {@link AttrTypeType }
     * 
     */
    public AttrTypeType getType()
    {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *            allowed object is {@link AttrTypeType }
     * 
     */
    public void setType(AttrTypeType value)
    {
        this.type = value;
    }

    /**
     * Gets the value of the dir property.
     * 
     * @return possible object is {@link DirValue }
     * 
     */
    public DirValue getDir()
    {
        return dir;
    }

    /**
     * Sets the value of the dir property.
     * 
     * @param value
     *            allowed object is {@link DirValue }
     * 
     */
    public void setDir(DirValue value)
    {
        this.dir = value;
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
