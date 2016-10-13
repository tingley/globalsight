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

public class TranslateFormattedTextResponse implements java.io.Serializable
{
    private static final long serialVersionUID = -7684631675044168295L;
    
    private java.lang.String translateFormattedTextResult;

    public TranslateFormattedTextResponse()
    {
    }

    public TranslateFormattedTextResponse(
            java.lang.String translateFormattedTextResult)
    {
        this.translateFormattedTextResult = translateFormattedTextResult;
    }

    /**
     * Gets the translateFormattedTextResult value for this
     * TranslateFormattedTextResponse.
     * 
     * @return translateFormattedTextResult
     */
    public java.lang.String getTranslateFormattedTextResult()
    {
        return translateFormattedTextResult;
    }

    /**
     * Sets the translateFormattedTextResult value for this
     * TranslateFormattedTextResponse.
     * 
     * @param translateFormattedTextResult
     */
    public void setTranslateFormattedTextResult(
            java.lang.String translateFormattedTextResult)
    {
        this.translateFormattedTextResult = translateFormattedTextResult;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj)
    {
        if (!(obj instanceof TranslateFormattedTextResponse))
            return false;
        TranslateFormattedTextResponse other = (TranslateFormattedTextResponse) obj;
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
        _equals = true && ((this.translateFormattedTextResult == null && other
                .getTranslateFormattedTextResult() == null) || (this.translateFormattedTextResult != null && this.translateFormattedTextResult
                .equals(other.getTranslateFormattedTextResult())));
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
        if (getTranslateFormattedTextResult() != null)
        {
            _hashCode += getTranslateFormattedTextResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            TranslateFormattedTextResponse.class, true);

    static
    {
        typeDesc.setXmlType(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                ">TranslateFormattedTextResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("translateFormattedTextResult");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/",
                "TranslateFormattedTextResult"));
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
