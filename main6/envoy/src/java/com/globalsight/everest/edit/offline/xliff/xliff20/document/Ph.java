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
 *       &lt;attribute name="canCopy" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" default="yes" />
 *       &lt;attribute name="canDelete" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" default="yes" />
 *       &lt;attribute name="canReorder" type="{urn:oasis:names:tc:xliff:document:2.0}yesNoFirstNo" default="yes" />
 *       &lt;attribute name="copyOf" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="disp" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="equiv" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="dataRef" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="subFlows" type="{http://www.w3.org/2001/XMLSchema}NMTOKENS" />
 *       &lt;attribute name="subType" type="{urn:oasis:names:tc:xliff:document:2.0}userDefinedValue" />
 *       &lt;attribute name="type" type="{urn:oasis:names:tc:xliff:document:2.0}attrType_type" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ph")
public class Ph
{

    @XmlAttribute
    protected YesNo canCopy;
    @XmlAttribute
    protected YesNo canDelete;
    @XmlAttribute
    protected YesNoFirstNo canReorder;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String copyOf;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String disp;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String equiv;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String dataRef;
    @XmlAttribute
    @XmlSchemaType(name = "NMTOKENS")
    protected List<String> subFlows;
    @XmlAttribute
    protected String subType;
    @XmlAttribute
    protected AttrTypeType type;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

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
     * Gets the value of the disp property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDisp()
    {
        return disp;
    }

    /**
     * Sets the value of the disp property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDisp(String value)
    {
        this.disp = value;
    }

    /**
     * Gets the value of the equiv property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getEquiv()
    {
        return equiv;
    }

    /**
     * Sets the value of the equiv property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setEquiv(String value)
    {
        this.equiv = value;
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
     * Gets the value of the dataRef property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDataRef()
    {
        return dataRef;
    }

    /**
     * Sets the value of the dataRef property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDataRef(String value)
    {
        this.dataRef = value;
    }

    /**
     * Gets the value of the subFlows property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the subFlows property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getSubFlows().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getSubFlows()
    {
        if (subFlows == null)
        {
            subFlows = new ArrayList<String>();
        }
        return this.subFlows;
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
