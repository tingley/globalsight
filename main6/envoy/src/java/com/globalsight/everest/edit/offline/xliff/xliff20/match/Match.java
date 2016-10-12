//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.06 at 01:08:14 ���� CST 
//


package com.globalsight.everest.edit.offline.xliff.xliff20.match;

import java.math.BigDecimal;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.globalsight.everest.edit.offline.xliff.xliff20.document.OriginalData;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Source;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.Target;
import com.globalsight.everest.edit.offline.xliff.xliff20.document.YesNo;
import com.globalsight.everest.edit.offline.xliff.xliff20.metadata.Metadata;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:metadata:2.0}metadata" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}originalData" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}source"/>
 *         &lt;element ref="{urn:oasis:names:tc:xliff:document:2.0}target"/>
 *         &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="matchQuality" type="{urn:oasis:names:tc:xliff:matches:2.0}similarity" />
 *       &lt;attribute name="matchSuitability" type="{urn:oasis:names:tc:xliff:matches:2.0}similarity" />
 *       &lt;attribute name="origin" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="ref" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="reference" type="{urn:oasis:names:tc:xliff:document:2.0}yesNo" default="no" />
 *       &lt;attribute name="similarity" type="{urn:oasis:names:tc:xliff:matches:2.0}similarity" />
 *       &lt;attribute name="subType" type="{urn:oasis:names:tc:xliff:document:2.0}userDefinedValue" />
 *       &lt;attribute name="type" type="{urn:oasis:names:tc:xliff:matches:2.0}typeValues" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "metadata",
    "originalData",
    "source",
    "target",
    "any"
})
@XmlRootElement(name = "match")
public class Match {

    @XmlElement(namespace = "urn:oasis:names:tc:xliff:metadata:2.0")
    protected Metadata metadata;
    @XmlElement(namespace = "urn:oasis:names:tc:xliff:document:2.0")
    protected OriginalData originalData;
    @XmlElement(namespace = "urn:oasis:names:tc:xliff:document:2.0", required = true)
    protected Source source;
    @XmlElement(namespace = "urn:oasis:names:tc:xliff:document:2.0", required = true)
    protected Target target;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NMTOKEN")
    protected String id;
    @XmlAttribute
    protected String matchQuality;
    @XmlAttribute
    protected BigDecimal matchSuitability;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String origin;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anyURI")
    protected String ref;
    @XmlAttribute
    protected YesNo reference;
    @XmlAttribute
    protected BigDecimal similarity;
    @XmlAttribute
    protected String subType;
    @XmlAttribute
    protected TypeValues type;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link Metadata }
     *     
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link Metadata }
     *     
     */
    public void setMetadata(Metadata value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the originalData property.
     * 
     * @return
     *     possible object is
     *     {@link OriginalData }
     *     
     */
    public OriginalData getOriginalData() {
        return originalData;
    }

    /**
     * Sets the value of the originalData property.
     * 
     * @param value
     *     allowed object is
     *     {@link OriginalData }
     *     
     */
    public void setOriginalData(OriginalData value) {
        this.originalData = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setSource(Source value) {
        this.source = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link Target }
     *     
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link Target }
     *     
     */
    public void setTarget(Target value) {
        this.target = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the matchQuality property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public String getMatchQuality() {
        return matchQuality;
    }

    /**
     * Sets the value of the matchQuality property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchQuality(String value) {
        this.matchQuality = value;
    }

    /**
     * Gets the value of the matchSuitability property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMatchSuitability() {
        return matchSuitability;
    }

    /**
     * Sets the value of the matchSuitability property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMatchSuitability(BigDecimal value) {
        this.matchSuitability = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRef(String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link YesNo }
     *     
     */
    public YesNo getReference() {
        if (reference == null) {
            return YesNo.NO;
        } else {
            return reference;
        }
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link YesNo }
     *     
     */
    public void setReference(YesNo value) {
        this.reference = value;
    }

    /**
     * Gets the value of the similarity property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSimilarity() {
        return similarity;
    }

    /**
     * Sets the value of the similarity property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSimilarity(BigDecimal value) {
        this.similarity = value;
    }

    /**
     * Gets the value of the subType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Sets the value of the subType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubType(String value) {
        this.subType = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link TypeValues }
     *     
     */
    public TypeValues getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link TypeValues }
     *     
     */
    public void setType(TypeValues value) {
        this.type = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
