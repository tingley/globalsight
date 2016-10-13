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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
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
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}source"/>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}target" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="canResegment" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" />
 *       &lt;attribute name="state" type="{urn:oasis:names:tc:xliff:document:2.0}stateType" default="initial" />
 *       &lt;attribute name="subState" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "source", "target" })
@XmlRootElement(name = "segment")
public class Segment
{

    @XmlElement(required = true)
    protected Source source;
    protected Target target;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute
    protected YesNo canResegment;
    @XmlAttribute
    protected StateType state;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String subState;

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
     * Gets the value of the state property.
     * 
     * @return possible object is {@link StateType }
     * 
     */
    public StateType getState()
    {
        if (state == null)
        {
            return StateType.INITIAL;
        }
        else
        {
            return state;
        }
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *            allowed object is {@link StateType }
     * 
     */
    public void setState(StateType value)
    {
        this.state = value;
    }

    /**
     * Gets the value of the subState property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSubState()
    {
        return subState;
    }

    /**
     * Sets the value of the subState property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSubState(String value)
    {
        this.subState = value;
    }

}
