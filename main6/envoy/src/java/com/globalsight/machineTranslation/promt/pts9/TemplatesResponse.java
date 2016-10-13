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

public class TemplatesResponse implements java.io.Serializable
{
    private static final long serialVersionUID = -3197096576875119652L;
    
    private java.lang.Object[] templatesResult;

    public TemplatesResponse()
    {
    }

    public TemplatesResponse(java.lang.Object[] templatesResult)
    {
        this.templatesResult = templatesResult;
    }

    /**
     * Gets the templatesResult value for this TemplatesResponse.
     * 
     * @return templatesResult
     */
    public java.lang.Object[] getTemplatesResult()
    {
        return templatesResult;
    }

    /**
     * Sets the templatesResult value for this TemplatesResponse.
     * 
     * @param templatesResult
     */
    public void setTemplatesResult(java.lang.Object[] templatesResult)
    {
        this.templatesResult = templatesResult;
    }

    private java.lang.Object __equalsCalc = null;

    public synchronized boolean equals(java.lang.Object obj)
    {
        if (!(obj instanceof TemplatesResponse))
            return false;
        TemplatesResponse other = (TemplatesResponse) obj;
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
        _equals = true && ((this.templatesResult == null && other
                .getTemplatesResult() == null) || (this.templatesResult != null && java.util.Arrays
                .equals(this.templatesResult, other.getTemplatesResult())));
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
        if (getTemplatesResult() != null)
        {
            for (int i = 0; i < java.lang.reflect.Array
                    .getLength(getTemplatesResult()); i++)
            {
                java.lang.Object obj = java.lang.reflect.Array.get(
                        getTemplatesResult(), i);
                if (obj != null && !obj.getClass().isArray())
                {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
            TemplatesResponse.class, true);

    static
    {
        typeDesc
                .setXmlType(new javax.xml.namespace.QName(
                        "http://Promt.Pits.Translator/Services/",
                        ">TemplatesResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("templatesResult");
        elemField.setXmlName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "TemplatesResult"));
        elemField.setXmlType(new javax.xml.namespace.QName(
                "http://www.w3.org/2001/XMLSchema", "anyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName(
                "http://Promt.Pits.Translator/Services/", "anyType"));
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
