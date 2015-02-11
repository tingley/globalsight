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

public class TranslateFormattedText implements java.io.Serializable
{
    private static final long serialVersionUID = 5680866075834357279L;

    private int dirId;

    private java.lang.String tplId;

    private java.lang.String strText;

    private java.lang.String fileType;

    public TranslateFormattedText()
    {
    }

    public TranslateFormattedText(int dirId, java.lang.String tplId,
            java.lang.String strText, java.lang.String fileType)
    {
        this.dirId = dirId;
        this.tplId = tplId;
        this.strText = strText;
        this.fileType = fileType;
    }

    /**
     * Gets the dirId value for this TranslateFormattedText.
     * 
     * @return dirId
     */
    public int getDirId()
    {
        return dirId;
    }

    /**
     * Sets the dirId value for this TranslateFormattedText.
     * 
     * @param dirId
     */
    public void setDirId(int dirId)
    {
        this.dirId = dirId;
    }

    /**
     * Gets the tplId value for this TranslateFormattedText.
     * 
     * @return tplId
     */
    public java.lang.String getTplId()
    {
        return tplId;
    }

    /**
     * Sets the tplId value for this TranslateFormattedText.
     * 
     * @param tplId
     */
    public void setTplId(java.lang.String tplId)
    {
        this.tplId = tplId;
    }

    /**
     * Gets the strText value for this TranslateFormattedText.
     * 
     * @return strText
     */
    public java.lang.String getStrText()
    {
        return strText;
    }

    /**
     * Sets the strText value for this TranslateFormattedText.
     * 
     * @param strText
     */
    public void setStrText(java.lang.String strText)
    {
        this.strText = strText;
    }

    /**
     * Gets the fileType value for this TranslateFormattedText.
     * 
     * @return fileType
     */
    public java.lang.String getFileType()
    {
        return fileType;
    }

    /**
     * Sets the fileType value for this TranslateFormattedText.
     * 
     * @param fileType
     */
    public void setFileType(java.lang.String fileType)
    {
        this.fileType = fileType;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj)
    {
        if (!(obj instanceof TranslateFormattedText))
            return false;
        TranslateFormattedText other = (TranslateFormattedText) obj;
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
                && this.dirId == other.getDirId()
                && ((this.tplId == null && other.getTplId() == null) || (this.tplId != null && this.tplId
                        .equals(other.getTplId())))
                && ((this.strText == null && other.getStrText() == null) || (this.strText != null && this.strText
                        .equals(other.getStrText())))
                && ((this.fileType == null && other.getFileType() == null) || (this.fileType != null && this.fileType
                        .equals(other.getFileType())));
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
        _hashCode += getDirId();
        if (getTplId() != null)
        {
            _hashCode += getTplId().hashCode();
        }
        if (getStrText() != null)
        {
            _hashCode += getStrText().hashCode();
        }
        if (getFileType() != null)
        {
            _hashCode += getFileType().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            TranslateFormattedText.class, true);

    static
    {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedText"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dirId");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "DirId"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tplId");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "TplId"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strText");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "strText"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fileType");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "FileType"));
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
