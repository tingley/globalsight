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

public class TranslateFormattedText2Response implements java.io.Serializable
{
    private static final long serialVersionUID = 4213867215464422469L;

    private java.lang.String translateFormattedText2Result;

    private java.lang.String strError;

    public TranslateFormattedText2Response()
    {
    }

    public TranslateFormattedText2Response(
            java.lang.String translateFormattedText2Result,
            java.lang.String strError)
    {
        this.translateFormattedText2Result = translateFormattedText2Result;
        this.strError = strError;
    }

    /**
     * Gets the translateFormattedText2Result value for this
     * TranslateFormattedText2Response.
     * 
     * @return translateFormattedText2Result
     */
    public java.lang.String getTranslateFormattedText2Result()
    {
        return translateFormattedText2Result;
    }

    /**
     * Sets the translateFormattedText2Result value for this
     * TranslateFormattedText2Response.
     * 
     * @param translateFormattedText2Result
     */
    public void setTranslateFormattedText2Result(
            java.lang.String translateFormattedText2Result)
    {
        this.translateFormattedText2Result = translateFormattedText2Result;
    }

    /**
     * Gets the strError value for this TranslateFormattedText2Response.
     * 
     * @return strError
     */
    public java.lang.String getStrError()
    {
        return strError;
    }

    /**
     * Sets the strError value for this TranslateFormattedText2Response.
     * 
     * @param strError
     */
    public void setStrError(java.lang.String strError)
    {
        this.strError = strError;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj)
    {
        if (!(obj instanceof TranslateFormattedText2Response))
            return false;
        TranslateFormattedText2Response other = (TranslateFormattedText2Response) obj;
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
                && ((this.translateFormattedText2Result == null && other
                        .getTranslateFormattedText2Result() == null) || (this.translateFormattedText2Result != null && this.translateFormattedText2Result
                        .equals(other.getTranslateFormattedText2Result())))
                && ((this.strError == null && other.getStrError() == null) || (this.strError != null && this.strError
                        .equals(other.getStrError())));
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
        if (getTranslateFormattedText2Result() != null)
        {
            _hashCode += getTranslateFormattedText2Result().hashCode();
        }
        if (getStrError() != null)
        {
            _hashCode += getStrError().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            TranslateFormattedText2Response.class, true);

    static
    {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedText2Response"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("translateFormattedText2Result");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                "TranslateFormattedText2Result"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("strError");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "strError"));
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
