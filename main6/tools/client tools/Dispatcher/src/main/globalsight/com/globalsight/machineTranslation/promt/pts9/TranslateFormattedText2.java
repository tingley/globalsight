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
package com.globalsight.machineTranslation.promt.pts9;

public class TranslateFormattedText2 implements java.io.Serializable
{
    private static final long serialVersionUID = -6199636619385418039L;

    private int iDirId;

    private java.lang.String strTplId;

    private java.lang.String strSourceText;

    private java.lang.String strFileType;

    public TranslateFormattedText2()
    {
    }

    public TranslateFormattedText2(int iDirId, java.lang.String strTplId,
            java.lang.String strSourceText, java.lang.String strFileType)
    {
        this.iDirId = iDirId;
        this.strTplId = strTplId;
        this.strSourceText = strSourceText;
        this.strFileType = strFileType;
    }

    /**
     * Gets the iDirId value for this TranslateFormattedText2.
     * 
     * @return iDirId
     */
    public int getIDirId()
    {
        return iDirId;
    }

    /**
     * Sets the iDirId value for this TranslateFormattedText2.
     * 
     * @param iDirId
     */
    public void setIDirId(int iDirId)
    {
        this.iDirId = iDirId;
    }

    /**
     * Gets the strTplId value for this TranslateFormattedText2.
     * 
     * @return strTplId
     */
    public java.lang.String getStrTplId()
    {
        return strTplId;
    }

    /**
     * Sets the strTplId value for this TranslateFormattedText2.
     * 
     * @param strTplId
     */
    public void setStrTplId(java.lang.String strTplId)
    {
        this.strTplId = strTplId;
    }

    /**
     * Gets the strSourceText value for this TranslateFormattedText2.
     * 
     * @return strSourceText
     */
    public java.lang.String getStrSourceText()
    {
        return strSourceText;
    }

    /**
     * Sets the strSourceText value for this TranslateFormattedText2.
     * 
     * @param strSourceText
     */
    public void setStrSourceText(java.lang.String strSourceText)
    {
        this.strSourceText = strSourceText;
    }

    /**
     * Gets the strFileType value for this TranslateFormattedText2.
     * 
     * @return strFileType
     */
    public java.lang.String getStrFileType()
    {
        return strFileType;
    }

    /**
     * Sets the strFileType value for this TranslateFormattedText2.
     * 
     * @param strFileType
     */
    public void setStrFileType(java.lang.String strFileType)
    {
        this.strFileType = strFileType;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj)
    {
        if (!(obj instanceof TranslateFormattedText2))
            return false;
        TranslateFormattedText2 other = (TranslateFormattedText2) obj;
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (__equalsCalc != null)
        {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && this.iDirId == other.getIDirId()
                && ((this.strTplId == null && other.getStrTplId() == null) || (this.strTplId != null && this.strTplId
                        .equals(other.getStrTplId())))
                && ((this.strSourceText == null && other.getStrSourceText() == null) || (this.strSourceText != null && this.strSourceText
                        .equals(other.getStrSourceText())))
                && ((this.strFileType == null && other.getStrFileType() == null) || (this.strFileType != null && this.strFileType
                        .equals(other.getStrFileType())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode()
    {
        if (__hashCodeCalc)
        {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getIDirId();
        if (getStrTplId() != null)
        {
            _hashCode += getStrTplId().hashCode();
        }
        if (getStrSourceText() != null)
        {
            _hashCode += getStrSourceText().hashCode();
        }
        if (getStrFileType() != null)
        {
            _hashCode += getStrFileType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            TranslateFormattedText2.class, true);

    static
    {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedText2"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDirId");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "iDirId"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strTplId");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "strTplId"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strSourceText");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "strSourceText"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strFileType");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "strFileType"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc()
    {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
            java.lang.String mechType, java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType)
    {
        return new org.apache.axis.encoding.ser.BeanSerializer(_javaType,
                _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
            java.lang.String mechType, java.lang.Class _javaType,
            javax.xml.namespace.QName _xmlType)
    {
        return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType,
                _xmlType, typeDesc);
    }

}
